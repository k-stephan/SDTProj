package com.macys.sdt.framework.utils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;

public class UtilsTest {

    @Test
    public void testExecuteCMD() throws Exception {
        Assert.assertNotNull(Utils.executeCMD(Utils.isWindows()? "cd" : "pwd"));
    }

    @Test
    public void testReadTextFile() throws Exception {
        File file = new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/valid_promo_codes.json");
        Assert.assertNotNull(Utils.readTextFile(file));
    }

    @Test
    public void testCreateDirectory() throws Exception {
        String dirName = "testCreateDirectory";
        File dir = new File(dirName);
        Utils.createDirectory(dirName);
        Assert.assertTrue(dir.exists());
        Utils.createDirectory(dirName, true);
        Assert.assertTrue(dir.exists());
        Assert.assertTrue(dir.delete());
    }

    @Test
    public void testGetScenarioShaKey() throws Exception {
        String key1 = Utils.getScenarioShaKey("features/mcom/some_feature_file.feature", "Some scenario name");
        String key2 = Utils.getScenarioShaKey("features/mcom/some_feature_file.feature", "Some other scenario name");
        Assert.assertNotNull(key1);
        Assert.assertNotNull(key2);
        Assert.assertNotEquals(key1, key2);
    }

    @Test
    public void testJsonPretty() throws Exception {
        JSONObject jsonObj = new JSONObject("{\"Key1\":\"value1\",\"key2\":\"value2\"}");
        String prettyJson = "{\n" +
                "  \"map\": {\n" +
                "    \"Key1\": \"value1\",\n" +
                "    \"key2\": \"value2\"\n" +
                "  }\n" +
                "}";
        Assert.assertEquals(prettyJson, Utils.jsonPretty(jsonObj));
    }

    @Test
    public void testWriteSmallBinaryFile() throws Exception {
        String data = "some test to write in a binary file";
        File file = new File("testWriteSmallBinaryFile");
        Utils.writeSmallBinaryFile(data.getBytes(), file);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.delete());
    }

    @Test
    public void testToDuration() throws Exception {
        Assert.assertEquals("1 Days 10 Hours 17 Minutes 36 Seconds", Utils.toDuration(123456789));
    }

    @Test
    public void testWriteBinaryFile() throws Exception {
        String data = "some test to write in a binary file";
        File file = new File("testWriteBinaryFile");
        Utils.writeBinaryFile(data.getBytes(), file, false);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(file.delete());
    }

    @Test
    public void testParseInt() throws Exception {
        Assert.assertEquals(92, Utils.parseInt("92", 0));
        Assert.assertEquals(1234, Utils.parseInt("1,234", 0));
        Assert.assertEquals(123, Utils.parseInt("123.4", 0));
        Assert.assertEquals(1, Utils.parseInt("abc", 1));
        Assert.assertEquals(0, Utils.parseInt(null, 0));
        Assert.assertEquals(12, Utils.parseInt(12.2f, 0));
        Assert.assertEquals(17, Utils.parseInt(17.4d, 0));
    }

    @Test
    public void testGherkinToJson() throws Exception {
        String featurePath = "src/test/java/com/macys/sdt/framework/resources/sample_gherkin.feature";
        Assume.assumeTrue(new File(featurePath).exists());
        try {
            Utils.gherkinToJson(false, featurePath);
            Utils.gherkinToJson(true, featurePath);
        } catch (Exception e) {
            Assert.fail("Failed gherkinToJson : " + e.getMessage());
        }
    }

    @Test
    public void testIsOSX() throws Exception {
        String osName = System.getProperty("os.name");
        System.setProperty("os.name", "Mac OS X");
        Assert.assertTrue(Utils.isOSX());
        Assert.assertFalse(Utils.isWindows());
        Assert.assertFalse(Utils.isWindows8());
        Assert.assertFalse(Utils.isLinux());
        System.setProperty("os.name", osName);
    }

    @Test
    public void testIsWindows() throws Exception {
        String osName = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 7");
        Assert.assertFalse(Utils.isOSX());
        Assert.assertTrue(Utils.isWindows());
        Assert.assertFalse(Utils.isWindows8());
        Assert.assertFalse(Utils.isLinux());
        System.setProperty("os.name", osName);
    }

    @Test
    public void testIsWindows8() throws Exception {
        String osName = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 8");
        Assert.assertFalse(Utils.isOSX());
        Assert.assertTrue(Utils.isWindows());
        Assert.assertTrue(Utils.isWindows8());
        Assert.assertFalse(Utils.isLinux());
        System.setProperty("os.name", osName);
    }

    @Test
    public void testIsLinux() throws Exception {
        String osName = System.getProperty("os.name");
        System.setProperty("os.name", "Linux");
        Assert.assertFalse(Utils.isOSX());
        Assert.assertFalse(Utils.isWindows());
        Assert.assertFalse(Utils.isWindows8());
        Assert.assertTrue(Utils.isLinux());
        System.setProperty("os.name", osName);
    }
}