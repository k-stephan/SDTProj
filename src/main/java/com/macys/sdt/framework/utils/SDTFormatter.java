package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import gherkin.deps.com.google.gson.GsonBuilder;
import gherkin.deps.net.iharder.Base64;
import gherkin.formatter.Formatter;
import gherkin.formatter.NiceAppendable;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SDTFormatter implements Reporter, Formatter {
    private final List<Map<String, Object>> featureMaps = new ArrayList<>();
    private final NiceAppendable out;

    private Map<String, Object> featureMap;
    private String uri;
    private List<Map> beforeHooks = new ArrayList<>();
    private File m_fcucumberJson = new File(MainRunner.logs + "cucumber.json");

    public SDTFormatter(Appendable out) {
        this.out = new NiceAppendable(out);
    }

    private Map getCurrentStep(Phase phase) {
        String target = phase.ordinal() <= Phase.match.ordinal() ? Phase.match.name() : Phase.result.name();
        Map lastWithValue = null;
        for (Map stepOrHook : getSteps()) {
            if (stepOrHook.get(target) == null) {
                return stepOrHook;
            } else {
                lastWithValue = stepOrHook;
            }
        }
        return lastWithValue;
    }

    @Override
    public void uri(String uri) {
        this.uri = uri;
    }

    @Override
    public void feature(Feature feature) {
        featureMap = feature.toMap();
        featureMap.put("uri", uri);
        featureMaps.add(featureMap);
    }

    @Override
    public void background(Background background) {
        getFeatureElements().add(background.toMap());
    }

    @Override
    public void scenario(Scenario scenario) {
        getFeatureElements().add(scenario.toMap());
        if (beforeHooks.size() > 0) {
            getFeatureElement().put("before", beforeHooks);
            beforeHooks = new ArrayList<>();
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        getFeatureElements().add(scenarioOutline.toMap());
    }

    @Override
    public void examples(Examples examples) {
        getAllExamples().add(examples.toMap());
    }

    @Override
    public void step(Step step) {
        getSteps().add(step.toMap());
    }

    @Override
    public void match(Match match) {
        getCurrentStep(Phase.match).put("match", match.toMap());
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        final Map<String, String> embedding = new HashMap<>();
        embedding.put("mime_type", mimeType);
        embedding.put("data", Base64.encodeBytes(data));
        getEmbeddings().add(embedding);
    }

    @Override
    public void write(String text) {
        getOutput().add(text);
    }

    @Override
    public void result(Result result) {
        if (!result.getStatus().equals("passed")) {
            System.err.println(" --> " + result.getStatus().toUpperCase());
        }
        HashMap<String, Object> map = new HashMap<>(result.toMap());
        if (!result.getStatus().equals("passed")) {
            String tempUri = this.uri;
            if (ScenarioHelper.isScenarioOutline()) {
                tempUri += ScenarioHelper.outlineCount;
            }
            String screenShot = Utils.getScenarioShaKey(tempUri, ":" + this.getSteps().size()) + ".png";
            StepUtils.browserScreenCapture(screenShot);
            map.put("screen_shot", screenShot);
        }
        getCurrentStep(Phase.result).put("result", map);
    }

    @Override
    public void before(Match match, Result result) {
        beforeHooks.add(buildHookMap(match, result));
    }

    @Override
    public void after(Match match, Result result) {
        List<Map> hooks = getFeatureElement().get("after");
        if (hooks == null) {
            hooks = new ArrayList<>();
            getFeatureElement().put("after", hooks);
        }
        hooks.add(buildHookMap(match, result));
    }

    private Map buildHookMap(final Match match, final Result result) {
        final Map hookMap = new HashMap();
        hookMap.put("match", match.toMap());
        hookMap.put("result", result.toMap());
        return hookMap;
    }

    public void appendDuration(final int timestamp) {
        final Map result = (Map) getCurrentStep(Phase.result).get("result");
        // check to make sure result exists (scenario outlines do not have results yet)
        if (result != null) {
            //convert to nanoseconds
            final long nanos = timestamp * 1000000000L;
            result.put("duration", nanos);
        }
    }

    @Override
    public void eof() {
    }

    @Override
    public void done() {
        HashMap<String, Object> job = new HashMap<>();
        job.put("run", featureMaps);
        job.put("environment_variables", System.getenv());
        job.put("start_time", MainRunner.startTime);
        job.put("end_time", System.currentTimeMillis());
        String json = gson().toJson(job);
        //        out.append(json);
        Utils.writeSmallBinaryFile(json.getBytes(), m_fcucumberJson);
        // We're *not* closing the stream here.
        // https://github.com/cucumber/gherkin/issues/151
        // https://github.com/cucumber/cucumber-jvm/issues/96
    }

    @Override
    public void close() {
        out.close();
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        // NoOp
    }

    private List<Map<String, Object>> getFeatureElements() {
        List<Map<String, Object>> featureElements = (List) featureMap.get("elements");
        if (featureElements == null) {
            featureElements = new ArrayList<>();
            featureMap.put("elements", featureElements);
        }
        return featureElements;
    }

    private Map<String, List<Map>> getFeatureElement() {
        if (getFeatureElements().size() > 0) {
            return (Map) getFeatureElements().get(getFeatureElements().size() - 1);
        } else {
            return null;
        }
    }

    private List<Map> getAllExamples() {
        List<Map> allExamples = getFeatureElement().get("examples");
        if (allExamples == null) {
            allExamples = new ArrayList<>();
            getFeatureElement().put("examples", allExamples);
        }
        return allExamples;
    }

    private List<Map> getSteps() {
        List<Map> steps = getFeatureElement().get("steps");
        if (steps == null) {
            steps = new ArrayList<>();
            getFeatureElement().put("steps", steps);
        }
        return steps;
    }

    private List<Map<String, String>> getEmbeddings() {
        List<Map<String, String>> embeddings = (List<Map<String, String>>) getCurrentStep(Phase.embedding).get("embeddings");
        if (embeddings == null) {
            embeddings = new ArrayList<>();
            getCurrentStep(Phase.embedding).put("embeddings", embeddings);
        }
        return embeddings;
    }

    private List<String> getOutput() {
        List<String> output = (List<String>) getCurrentStep(Phase.output).get("output");
        if (output == null) {
            output = new ArrayList<String>();
            getCurrentStep(Phase.output).put("output", output);
        }
        return output;
    }

    protected gherkin.deps.com.google.gson.Gson gson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private enum Phase {step, match, embedding, output, result}
}