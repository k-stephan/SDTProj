package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.db.models.MediaService;
import com.macys.sdt.shared.actions.website.mcom.panels.shop_and_browse.FlexTemplatePanel;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class ContextualContent extends StepUtils {

    FlexTemplatePanel flexPanel = new FlexTemplatePanel();
    private String componentId, mainPageType, mainRowType, mainRowTypeId;
    private List<Map> finalExpectedData;
    private String[] mediaNamesArray;
    private boolean mediaAdsFlag;
    private List<String> seqNumbers;
    private List actualMediaType;
    private int sequence;

    @When("^I navigate to \"([^\"]*)\" category with \"([^\"]*)\" in \"([^\"]*)\" for context$")
    public void I_navigate_to_category_with_media_in_row_for_context(String pageType, String mediaNames, String rowType, List<Map<String, String>> context) throws Throwable {
        if (MainRunner.debugMode)
            System.out.println("Media: " + mediaNames);
        String[] mediaNamesArray = mediaNames.split(", ");
        String site = macys() ? "mcom" : "bcom";
        String mode, regionCode;
        mode = context.stream().filter(con -> con.containsKey("SHOPPING_MODE")).map(con -> con.get("SHOPPING_MODE")).findFirst().get();
        regionCode = context.stream().filter(con -> con.containsKey("SHOPPING_MODE")).map(con -> con.get("REGION_CODE")).findFirst().get();
        String[] contextAttrNames = context.get(0).keySet().toArray(new String[context.get(0).keySet().size()]);
        String[] contextAttrValues = context.get(0).values().toArray(new String[context.get(0).values().size()]);
        String[] tempMediaNames = mediaNamesArray.clone();
        List<Map> finalMediaData;
        //Fetching data from db and manipulating is taking significant time, and thus resulting in timeouts
        // as a workaround pause the pagehangwatchdog and resume after this task
        pausePageHangWatchDog();
        finalMediaData = MediaService.getFinalContextualizeCanvasData(mediaNamesArray.clone(), rowType, contextAttrNames, contextAttrValues);
        resumePageHangWatchDog();
        System.out.println("--> Captured required media data from database!!");
        for (int index = 0; index < mediaNamesArray.length; index++) {
            mediaNamesArray[index] = ((mediaNamesArray[index].equals("MEDIA_ADS")) ? "THUMBNAIL_GRID" : mediaNamesArray[index]);
            mediaNamesArray[index] = ((mediaNamesArray[index].equals("BANNER_MACHINE_WIDGET")) ? "WIDGET" : mediaNamesArray[index]);
            mediaNamesArray[index] = ((mediaNamesArray[index].equals("BANNER_MACHINE_SLIDESHOW")) ? "SLIDESHOW" : mediaNamesArray[index]);
        }
        List<Map> canvasRowData = new ArrayList<>();
        pausePageHangWatchDog();
        finalMediaData.forEach(media -> {
            if (canvasRowData.stream().anyMatch(row -> (row.get("rowId").toString().equals(media.get("canvasRowId").toString())))) {
                ((List) canvasRowData.stream()
                        .filter(row -> (row.get("rowId").toString().equals(media.get("canvasRowId").toString())))
                        .findFirst().get().get("mediaTypes")).add(media.get("mediaTypeDesc").toString());
            } else {
                Map dummy = new HashMap();
                dummy.put("rowId", media.get("canvasRowId").toString());
                List<String> dummyList = new ArrayList<>();
                dummyList.add(media.get("mediaTypeDesc").toString());
                dummy.put("mediaTypes", dummyList);
                canvasRowData.add(dummy);
            }
        });
        List<Map> expectedRowData = canvasRowData.stream()
                .filter(canvas -> ((Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_CATEGORY") || Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA")) ? (((List<String>)canvas.get("mediaTypes")).stream().anyMatch(type -> type.equals("PRODUCT_PANEL_CATEGORY") || Arrays.asList(mediaNamesArray).contains(type))) : (ListUtils.subtract(Arrays.asList(mediaNamesArray), ((List<String>) canvas.get("mediaTypes")))).isEmpty()))
                .collect(Collectors.toList());
        finalMediaData = finalMediaData.stream()
                .filter(data -> (expectedRowData.stream().anyMatch(mdata -> (mdata.get("rowId").toString().equals(data.get("canvasRowId").toString())))))
                .collect(Collectors.toList());
        List<Map> newTempData = new ArrayList<>();
        if (pageType.equals("Home Page")) {
            finalMediaData = finalMediaData.stream()
                    .filter(data -> (Arrays.asList(mediaNamesArray).contains(data.get("mediaTypeDesc").toString()) && mediaNamesArray.length == 1))
                    .collect(Collectors.toList());
        } else {
            finalMediaData.forEach(data -> {
                for (String name : mediaNamesArray) {
                    if (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") || Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR")) {
                        if (name.equals(data.get("mediaTypeDesc").toString()) || data.get("mediaTypeDesc").toString().equals("PRODUCT_PANEL_CATEGORY"))
                            newTempData.add(data);
                    } else if (name.equals(data.get("mediaTypeDesc").toString()))
                        newTempData.add(data);
                }
            });
            finalMediaData = newTempData;
        }
        resumePageHangWatchDog();
        List<String> canvasIds = finalMediaData.stream().map(data -> data.get("canvasId").toString()).distinct().collect(Collectors.toList());
        Assert.assertFalse("ERROR - DATA: Data is not available for expected media:'" + mediaNames + "' in site database", canvasIds.isEmpty());
        pausePageHangWatchDog();
        List<List> categoryAndCanvasIds = MediaService.getCategoryId(canvasIds, pageType, context, site, tempMediaNames);
        System.out.println("--> Captured required media data from FCC service!!");
        String categoryId;
        for (List categoryAndCanvasId : categoryAndCanvasIds) {
            categoryId = categoryAndCanvasId.get(0).toString();
            String canvasId = categoryAndCanvasId.get(1).toString();
            if (categoryAndCanvasId.get(2) != null)
                componentId = categoryAndCanvasId.get(2).toString();
            finalExpectedData = new ArrayList<>();
            finalExpectedData = finalMediaData.stream().filter(data -> (data.get("canvasId").toString().equals(canvasId))).collect(Collectors.toList());
            if (pageType.equals("Home Page")) {
                Assert.assertTrue("User is not in international site!!", (regionCode.equals("INTL") && Elements.anyPresent("home.goto_us_site")));
            } else {
                if (mode.equals("SITE"))
                    Navigate.visit("http://www." + MainRunner.url.split("\\.")[1] + ".fds.com/shop/?id=" + categoryId);
                else
                    Navigate.visit("http://www." + MainRunner.url.split("\\.")[1] + ".fds.com/shop/wedding-registry/?id=" + categoryId);
            }
            Wait.forPageReady();
            try {
                pageVerifications(pageType);
                Assert.assertTrue("ERROR - ENV: Redirected to some other category instead:'" + categoryId + "' category", url().contains(categoryId));
                if (String.join(" ", (CharSequence[]) tempMediaNames).contains("MEDIA_ADS") && (pageType.equals("Sub Splash") || pageType.equals("Browse")))
                    Assert.assertFalse("ERROR - DATA: Media Ads are not available on thumbnail grid", flexPanel.getMediaAdDetails().isEmpty());
                break;
            } catch (AssertionError e) {
                System.out.println(pageType + " page is not loaded for category:" + categoryId + " in context: " + context.toString() + " ");
            }
        }
        if (!pageType.equals("Home Page"))
            pageVerifications(pageType);
        mainPageType = pageType;
        resumePageHangWatchDog();
    }

    @Then("^I should see \"([^\"]*)\" on the page in \"([^\"]*)\" row$")
    public void I_should_see_on_the_page_in_row(String mediaNames, String rowType) throws Throwable {
        StepUtils.pausePageHangWatchDog();
        mediaNamesArray = mediaNames.split(", ");
        if (mediaNames.contains("MEDIA_ADS")) {
            for (int index = 0; index < mediaNamesArray.length; index++)
                if (mediaNamesArray[index].equals("MEDIA_ADS"))
                    mediaNamesArray[index] = "THUMBNAIL_GRID";
            mediaAdsFlag = true;
        }
        seqNumbers = flexPanel.getSequenceByRowTypes(rowType);
        boolean sequenceSet = false;
        for (Object seqNumber : seqNumbers) {
            actualMediaType = flexPanel.getRowMediaByRowTypeSeqNumber(rowType, seqNumber.toString(), true, false);
            // Below code is to skip empty rows.
            if(((Map)actualMediaType.get(0)).get("mediaType") == null)
                continue;
            if (Arrays.asList(mediaNamesArray).contains("SLIDESHOW") || Arrays.asList(mediaNamesArray).contains("WIDGET"))
                for (Object anActualMediaType : actualMediaType) {
                    if (((Map) anActualMediaType).get("mediaType").toString().equals("banner_machine_slideshow"))
                        ((Map) anActualMediaType).put("mediaType", "slideshow");
                    if (((Map) anActualMediaType).get("mediaType").toString().equals("banner_machine_widget"))
                        ((Map) anActualMediaType).put("mediaType", "widget");
                }
            String[] productPool = {"PRODUCT_PANEL_CATEGORY_FACET", "PRODUCT_PANEL_POOL", "PRODUCT_PANEL_CATEGORY", "PRODUCT_PANEL_NA", "PRODUCT_PANEL_BAZAAR"};
            for (int index = 0; index < mediaNamesArray.length; index++)
                if (Arrays.asList(productPool).contains(mediaNamesArray[index]))
                    mediaNamesArray[index] = "PRODUCT_POOL";
            if ((((Map) actualMediaType.get(0)).get("mediaType") != null) && ((Map) actualMediaType.get(0)).get("mediaType").toString().equalsIgnoreCase(mediaNamesArray[0])) {
                for (Object anActualMediaType : actualMediaType) {
                    Map type = (Map) anActualMediaType;
                    if ((!type.get("mediaType").toString().contains("widget")) && (Arrays.asList(mediaNamesArray).contains("WIDGET"))) {
                        sequence = -1;
                        sequenceSet = true;
                    } else {
                        sequence = Integer.parseInt(seqNumber.toString());
                        sequenceSet = true;
                    }
                }
            }
            if (sequenceSet)
                break;
        }
        mainRowType = rowType;
        mainRowTypeId = "row_" + rowType + "_" + String.valueOf(sequence);
        if (!sequenceSet)
            Assert.fail("ERROR - DATA: Navigated category may have inherited canvas id, so we cannot find row type:'" + rowType + "' with media:'" + mediaNames + "' on the page");
        System.out.println("--> Required media is present in page!!");
        StepUtils.resumePageHangWatchDog();
    }

    @And("^I should see respective media as per astra data$")
    public void I_should_see_respective_media_as_per_astra_data() throws Throwable {
        StepUtils.pausePageHangWatchDog();
        if (sequence != -1) {
            finalExpectedData.forEach(data -> {
                if (data.get("mediaTypeDesc").toString().toLowerCase().contains("product"))
                    data.put("mediaTypeDesc", "PRODUCT_POOL");
                if (data.get("mediaTypeDesc").toString().contains("MEDIA_ADS"))
                    data.put("mediaTypeDesc", "THUMBNAIL_GRID");
            });
            finalExpectedData.removeIf(data -> (data.containsKey("mediaKey") && !data.get("mediaKey").toString().equals(componentId) && data.get("mediaTypeDesc").toString().equals("PRODUCT_POOL")));
            Map<String, List<Map>> groupResults = groupBy(finalExpectedData, "canvasRowId");
            boolean isSequenceExists = false;
            List<Integer> dbSequences = new ArrayList<>();
            int dbSequence = -1;
            groupResults.values().forEach(data -> dbSequences.addAll(data.stream().filter(type -> (type.containsKey("canvasRowSeq")))
                    .map(type -> Integer.parseInt(type.get("canvasRowSeq").toString()))
                    .collect(Collectors.toList())));
            if (dbSequences.size() > 0){
                dbSequence = dbSequences.indexOf(Collections.min(dbSequences));
                isSequenceExists = true;
            }
            int index = 0;
            for (List<Map> data : groupResults.values()) {
                List names = data.stream().map(type -> type.get("mediaTypeDesc").toString()).collect(Collectors.toList());
                boolean seqCondition = !isSequenceExists || dbSequence == index;
                if (ListUtils.subtract(names, Arrays.asList(mediaNamesArray)).isEmpty() && seqCondition) {
                    finalExpectedData = data;
                    break;
                }
                index++;
            }
            Assert.assertFalse("ERROR - DATA : Unable to find data in site database", finalExpectedData.isEmpty());
            List<Map> mediaDetails = new ArrayList<>();
            if (mainRowType.equals("0") || (Arrays.asList(mediaNamesArray).contains("PRODUCT_POOL"))) {
                for (String seq : seqNumbers)
                    mediaDetails = flexPanel.getRowMediaByRowTypeSeqNumber(mainRowType, seq, true, true).stream().collect(Collectors.toList());
            } else {
                mediaDetails = flexPanel.getRowMediaByRowTypeSeqNumber(mainRowType, String.valueOf(sequence), true, true).stream().collect(Collectors.toList());
            }
            boolean isBannerMachineSlide = mediaDetails.stream().anyMatch(data -> (data.get("mediaType").toString().equals("banner_machine_slideshow") && ((Arrays.asList(mediaNamesArray).contains("BANNER_MACHINE_SLIDESHOW")) || (Arrays.asList(mediaNamesArray).contains("SLIDESHOW")))));
            verifyMediaNames(isBannerMachineSlide, mediaDetails);
            List<String> names = mediaDetails.stream().map(type -> type.get("mediaType").toString().replace(" ", "_").toLowerCase()).collect(Collectors.toList());
            for (String name : names) {
                List<Map> uiData;
                List<Map> dbData;
                mediaDetails.removeIf(type -> (!type.get("mediaType").toString().equals((isBannerMachineSlide ? "banner_machine_slideshow" : name))));
                uiData = mediaDetails;
                finalExpectedData.removeIf(type -> (!type.get("mediaTypeDesc").toString().toLowerCase().replace(" ", "_").equals(name)));
                dbData = finalExpectedData;
                String errorMessage = "ERROR - APP: Expected media type: '" + name + "' is not displayed";
                switch (name) {
                    case "widget":
                        int counter = (int)uiData.stream().filter(data -> (((Map)data.get("mediaInfo")).isEmpty())).count();
                        if (counter == uiData.size())
                            System.out.println("Widget is collapsed due to time constraint");
                        else {
                            List<String> media = new ArrayList<>();
                            for (Map data : dbData)
                                media.addAll(((List<Map>) data.get("mediaInfo")).stream().map(info -> info.get("mediaTypeDesc").toString()).collect(Collectors.toList()));
                            List<String> actualMedia = new ArrayList<>();
                            for (Map data : uiData){
                                actualMedia.addAll(((List<Map>) data.get("mediaInfo")).stream().map(type -> type.get("panelType").toString()).collect(Collectors.toList()));
                                actualMedia.stream().filter(type -> type.equals("IMAGE")).map(type -> "AD");
                            }
                            actualMedia.forEach(mediaName -> Assert.assertTrue(errorMessage, media.contains(mediaName)));
                        }
                        break;
                    case "category_icon":
                        String textErrorMessage = "ERROR - APP: Category icons text is not displayed with media type:'" + name + "'";
                        String imageErrorMessage = "ERROR - APP: Category icon images are not displayed with media type:'" + name + "'";
                        List<String> dbText = new ArrayList<>();
                        List<String> catIconText = new ArrayList<>();
                        List<String> dbImageNames = new ArrayList<>();
                        List<String> uiImageNames = new ArrayList<>();
                        dbData.forEach(data -> {
                            dbText.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                    .map(type -> type.get("text").toString().toLowerCase().replace(" and ", " ").replace(" & ", " "))
                                    .collect(Collectors.toList()));
                            dbText.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                    .map(type -> type.get("mediaName").toString().toLowerCase())
                                    .collect(Collectors.toList()));
                        });
                        uiData.forEach(data -> {
                            catIconText.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                    .filter(type -> type.get("text") != null)
                                    .map(type -> (type.get("text").toString().toLowerCase().replace(" and ", " ").replace(" & ", " ")))
                                    .collect(Collectors.toList()));
                            uiImageNames.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                    .map(type -> type.get("image").toString().toLowerCase())
                                    .collect(Collectors.toList()));
                        });
                        catIconText.forEach(text -> Assert.assertTrue(textErrorMessage, dbText.contains(text)));
                        Assert.assertTrue(imageErrorMessage, ListUtils.subtract(dbImageNames, uiImageNames).isEmpty());
                        break;
                    case "slideshow":
                        if (isBannerMachineSlide) {
                            List<String> actualC2Slides = new ArrayList<>();
                            List<String> expectedSlides = new ArrayList<>();
                            uiData.forEach(data -> actualC2Slides.addAll(((List<Map>) ((Map) data.get("mediaInfo")).get("bannerMachineSlideData")).stream()
                                    .filter(type -> type.containsKey("c2SlideData"))
                                    .map(type -> ((HashMap) type.get("c2SlideData")).get("imageName").toString())
                                    .collect(Collectors.toList())));
                            dbData.forEach(data -> expectedSlides.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                    .map(type -> type.get("mediaName").toString().split(".jsp")[0])
                                    .collect(Collectors.toList())));
                            actualC2Slides.forEach(slide -> Assert.assertTrue("ERROR - APP: Slide Show is not displayed with valid media resource:'" + slide + "' as per DB:'" + expectedSlides + "'", expectedSlides.contains(slide)));
                        } else {
                            List<String> uiInfo = new ArrayList<>();
                            List<String> dbInfo = new ArrayList<>();
                            uiData.forEach(data -> uiInfo.addAll((((List<String>) ((Map) data.get("mediaInfo")).get("slideshowImages"))).stream().map(image -> image).collect(Collectors.toList())));
                            dbData.forEach(data -> dbInfo.addAll(((List<Map>) data.get("mediaInfo")).stream().map(image -> image.get("mediaName").toString()).collect(Collectors.toList())));
                            Assert.assertTrue(errorMessage, ListUtils.subtract(uiInfo, dbInfo).isEmpty());
                        }
                        break;
                    case "flexible_pool":
                        String titleErrorMessage = "ERROR - APP: Flexible title is not displayed with media type:'" + name + "'";
                        String headerErrorMessage = "ERROR - APP: Flexible header is not displayed with media type:'" + name + "'";
                        List<String> uiTitleList = new ArrayList<>();
                        List<String> dbTitleList = new ArrayList<>();
                        List<String> uiHeaderList = new ArrayList<>();
                        List<String> dbHeaderList = new ArrayList<>();
                        dbData.forEach(data -> {
                            dbTitleList.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                    .filter(one -> !one.get("mediaName").equals(""))
                                    .map(one -> one.get("description").toString())
                                    .collect(Collectors.toList()));
                            dbHeaderList.addAll((((List<Map>) data.get("mediaInfo"))).stream()
                                    .filter(one -> one.get("mediaName").equals(""))
                                    .map(one -> one.get("description").toString())
                                    .collect(Collectors.toList()));
                        });
                        uiData.forEach(data -> {
                            uiTitleList.addAll((((List<WebElement>) ((Map) data.get("mediaInfo")).get("flexTitle"))).stream()
                                    .map(WebElement::getText).collect(Collectors.toList()));
                            uiHeaderList.add(((WebElement) (((Map) data.get("mediaInfo")).get("flexHeader"))).getText().split("\n")[0]);
                        });
                        Assert.assertTrue(titleErrorMessage, ListUtils.subtract(dbTitleList, uiTitleList).isEmpty());
                        Assert.assertTrue(headerErrorMessage, ListUtils.subtract(dbHeaderList, uiHeaderList).isEmpty());
                        break;
                    case "ad":
                        List<String> mediaNamesList = new ArrayList<>();
                        List<String> uiMediaNamesList;
                        dbData.forEach(data -> mediaNamesList.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                .map(type -> type.get("mediaName").toString())
                                .collect(Collectors.toList())));
                        String dbAdSource = mediaNamesList.get(0);
                        String adErrorMessage = "ERROR - APP: Expected media type:'ad' source:'" + dbAdSource + "' is not displayed as per astra";
                        uiMediaNamesList = uiData.stream()
                                .map(data -> ((Map) data.get("mediaInfo")).get("imageName").toString())
                                .collect(Collectors.toList());
                        if (dbAdSource != null)
                            Assert.assertTrue(adErrorMessage, uiMediaNamesList.contains(dbAdSource));
                        break;
                    case "recently_reviewed":
                        List<String> textList;
                        textList = dbData.stream().map(data -> data.get("text").toString()).collect(Collectors.toList());
                        Assert.assertFalse("ERROR - APP: Recently review data is not displayed", (textList.contains("Recently Reviewed") && uiData.isEmpty()));
                        break;
                    case "image_map":
                    case "custom_popup":
                        List<String> dbImageMapNames = new ArrayList<>();
                        List<String> uiImageMapNames;
                        dbData.forEach(data -> dbImageMapNames.addAll(((List<Map>) data.get("mediaInfo")).stream().map(type -> type.get("mediaName").toString()).collect(Collectors.toList())));
                        uiImageMapNames = uiData.stream().map(data -> ((Map) data.get("mediaInfo")).get("imageName").toString()).collect(Collectors.toList());
                        Assert.assertTrue(errorMessage, ListUtils.subtract(dbImageMapNames, uiImageMapNames).isEmpty());
                        break;
                    case "video":
                        List dbVideoTitles = new ArrayList<>();
                        List uiVideoTitles;
                        dbData.forEach(data -> dbVideoTitles.addAll(((List<Map>) data.get("mediaInfo")).stream()
                                .filter(type -> type.get("description").toString().equals("VIDEO_TITLE"))
                                .map(type -> type.get("text").toString())
                                .collect(Collectors.toList())));
                        uiVideoTitles = uiData.stream().map(data -> ((Map)data.get("mediaInfo")).get("videoTitle").toString()).collect(Collectors.toList());
                        Assert.assertTrue(errorMessage, ListUtils.subtract(dbVideoTitles, uiVideoTitles).isEmpty());
                        break;
                    case "text":
                        List<String> dbTextData;
                        List<String> uiTextData;
                        dbTextData = dbData.stream().map(data -> data.get("text").toString().toLowerCase()).collect(Collectors.toList());
                        uiTextData = uiData.stream().map(data -> ((Map) data.get("mediaInfo")).get("text").toString().toLowerCase()).collect(Collectors.toList());
                        if (dbTextData.size() == 0)
                            dbTextData = dbData.stream().map(data -> data.get("text").toString().toLowerCase()).collect(Collectors.toList());
                        Assert.assertTrue(errorMessage, ListUtils.subtract(dbTextData, uiTextData).isEmpty());
                        break;
                    case "copy_block":
                        List<String> dbCopyData;
                        List<String> uiCopyData;
                        dbCopyData = dbData.stream().map(data -> data.get("text").toString().toLowerCase()).collect(Collectors.toList());
                        uiCopyData = uiData.stream().map(data -> ((Map) data.get("mediaInfo")).get("text").toString().toLowerCase()).collect(Collectors.toList());
                        Assert.assertTrue(errorMessage, ListUtils.subtract(dbCopyData, uiCopyData).isEmpty());
                        break;
                    case "thumbnail_grid":
                        if (mediaAdsFlag) {
                            List<String> mediGridNames = new ArrayList<>();
                            List<String> uiMediGridNames;
                            dbData.forEach(data -> mediGridNames.addAll(((List<Object>) data.get("mediaInfo")).stream()
                                    .map(type -> ((Map)type).get("mediaName").toString())
                                    .collect(Collectors.toList())));
                            boolean dataFound;
                            if(uiData.stream().anyMatch(data -> (((Map)data.get("mediaInfo")).get("thumbnailGridExists").getClass().equals(Boolean.class))))
                                dataFound = uiData.stream().map(data -> (Boolean)((Map)data.get("mediaInfo")).get("thumbnailGridExists")).findFirst().get();
                            else {
                                uiMediGridNames = uiData.stream()
                                        .map(data -> ((List)((Map) data.get("mediaInfo")).get("thumbnailGridExists")).get(0).toString())
                                        .collect(Collectors.toList());
                                dataFound = uiMediGridNames.stream().anyMatch(mediGridNames::contains);
                            }
                            Assert.assertTrue(errorMessage, dataFound);
                        } else {
                            List<String> uiMediaTypeDesc;
                            List<String> dbMediaTypeDesc;
                            dbMediaTypeDesc = dbData.stream()
                                    .map(data -> data.get("mediaTypeDesc").toString().toLowerCase())
                                    .collect(Collectors.toList());
                            uiMediaTypeDesc = uiData.stream()
                                    .map(data -> data.get("mediaType").toString().toLowerCase())
                                    .collect(Collectors.toList());
                            Assert.assertTrue(errorMessage, ListUtils.subtract(dbMediaTypeDesc, uiMediaTypeDesc).isEmpty());
                        }
                        break;
                    case "horizontal_rule":
                        Map horizontalRule;
                        horizontalRule = dbData.stream()
                                .filter(data -> (data.containsKey("text") || data.get("text") != null || data.get("text").toString().contains("Horizontal Rule")))
                                .findFirst().get();
                        if (horizontalRule.isEmpty())
                            uiData.forEach(data -> Assert.assertTrue(errorMessage, ((boolean) ((Map) data.get("mediaInfo")).get("horizontalRuleExists"))));
                        break;
                    case "product_pool":
                        List<String> poolData = new ArrayList<>();
                        List<String> uiPoolData;
                        dbData.forEach(data -> poolData.addAll(((List<Map>) data.get("mediaInfo")).stream().map(type -> type.get("text").toString().toLowerCase()).collect(Collectors.toList())));
                        uiPoolData = uiData.stream().map(data -> ((Map) data.get("mediaInfo")).get("title").toString().toLowerCase()).collect(Collectors.toList());
                        Assert.assertTrue(errorMessage, uiPoolData.contains(poolData.get(0)));
                        break;
                    case "jsp":
                        if(!dbData.stream().anyMatch(data -> data.get("mediaTypeDesc").toString().contains("JSP")))
                            uiData.forEach(data -> Assert.assertTrue(errorMessage, ((boolean) ((Map) data.get("mediaInfo")).get("jspExists"))));
                        break;
                    default:
                        Assert.fail("ERROR - ENV : Required media type data is not displayed in UI!!");
                }
            }
        } else {
            System.out.println("Widget is collapsed");
        }
        System.out.println("--> Required media data is present in page!!");
        StepUtils.resumePageHangWatchDog();
    }

    public void verifyMediaNames(boolean isBannerMachineSlide, List<Map> mediaDetails) throws Throwable {
        List<String> names;
        names = finalExpectedData.stream().map(type -> type.get("mediaTypeDesc").toString().replace(" ", "_").toLowerCase()).collect(Collectors.toList());
        List<String> mediaNames = new ArrayList<>();
        mediaDetails.forEach(type -> {
            if (isBannerMachineSlide)
                mediaNames.add(((type.get("mediaType").toString().equals("banner_machine_slideshow")) ? "slideshow" : type.get("mediaType").toString()));
            else
                mediaNames.add(((type.get("mediaType").toString().equals("banner_machine_widget")) ? "widget" : type.get("mediaType").toString()));
        });
        Assert.assertTrue("Media are mismatch in DB and UI", ListUtils.subtract(names, mediaNames).isEmpty());
    }

    public Map<String, List<Map>> groupBy(List<Map> originalData, String key) throws Throwable {
        Map<String, List<Map>> hashMap = new HashMap<>();
        for (Map data : originalData) {
            String mainKey = data.get(key).toString();
            if (!hashMap.containsKey(mainKey))
                hashMap.put(mainKey, (new ArrayList<>()));
            hashMap.get(mainKey).add(data);
        }
        return hashMap;
    }

    public void pageVerifications(String pageType) throws Throwable {
        switch (pageType) {
            case "Category Splash":
                Assert.assertTrue("User is not redirected to category_splash page", onPage("category_splash"));
                break;
            case "Sub Splash":
                Assert.assertTrue("User is not redirected to category_sub_splash page", onPage("category_sub_splash"));
                break;
            default:
                Assert.assertTrue("User is not redirected to category_browse page", onPage("category_browse"));
                break;
        }
    }
}
