package com.macys.sdt.framework.utils;

import com.macys.sdt.framework.runner.MainRunner;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Runtime.getRuntime;

/**
 * Tests for Utils
 */
public class UtilsTest {

    @Test
    public void testExecuteCMD() throws Exception {
        Assert.assertNotNull(Utils.executeCMD(Utils.isWindows()? "cd" : "pwd"));
    }

    @Test
    public void testReadTextFile() throws Exception {
        File file = new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/valid_promo_codes.json");
        Assume.assumeTrue(file.exists());
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
        Assume.assumeTrue(dir.delete());
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
        Assume.assumeTrue(file.delete());
    }

    @Test
    public void testToDuration() throws Exception {
        Assert.assertEquals("1 Days 10 Hours 17 Minutes 36.789 Seconds", Utils.toDuration(123456789));
    }

    @Test
    public void testWriteBinaryFile() throws Exception {
        String data = "some test to write in a binary file";
        File file = new File("testWriteBinaryFile");
        Utils.writeBinaryFile(data.getBytes(), file, false);
        Assert.assertTrue(file.exists());
        Assume.assumeTrue(file.delete());
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
        String featurePath = "src/test/java/com/macys/sdt/framework/Features/website/mcom/test.feature";
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

    @Test @Ignore("Not working in Jenkins")
    public void testDesktopCapture() throws Exception {
        File imgFile = new File("desktopCapture.png");
        FileOutputStream outputStream = new FileOutputStream(imgFile);
        Utils.desktopCapture(outputStream);
        outputStream.close();
        Assert.assertTrue(imgFile.exists());
        Assume.assumeTrue(imgFile.delete());
    }

    @Test
    public void testGetResourceFile() throws Exception {
        Assume.assumeTrue(new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/orderable_products.json").exists());

        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";
        Assert.assertTrue(Utils.getResourceFile("orderable_products.json").exists());

        //fallback to website resources
        MainRunner.currentURL = "http://m.qa0codemacys.fds.com";
        Assert.assertTrue(Utils.getResourceFile("orderable_products.json").exists());

        //no file found
        Assert.assertFalse(Utils.getResourceFile("not_present.json").exists());

        //absolute file path
        Assert.assertTrue(Utils.getResourceFile("src/test/java/com/macys/sdt/framework/resources/data/website/mcom", "orderable_products.json").exists());

        //absolute file path, no file found
        Assert.assertFalse(Utils.getResourceFile("src/test/java/com/macys/sdt/framework/resources/data/website/mcom", "not_present.json").exists());

        MainRunner.url = null;
        MainRunner.currentURL = "";
        MainRunner.project = null;
        MainRunner.projectDir = null;
    }

    @Test
    public void testListToString() throws Exception {
        List<String> list = new ArrayList<>();
        list.add("Hello");
        list.add("World!");
        list.add("Testing");
        String strList = Utils.listToString(list, " ", new String[]{"!"});
        Assert.assertEquals("Hello Testing", strList);
    }

    @Test
    public void testReadSmallBinaryFile() throws Exception {
        File file = new File("readSmallBinaryFile");
        Assert.assertNull(Utils.readSmallBinaryFile(file));
        byte[] data = "some test to write in a binary file".getBytes();
        Assume.assumeTrue(Utils.writeSmallBinaryFile(data, file));
        Assert.assertArrayEquals(data, Utils.readSmallBinaryFile(file));
        Assume.assumeTrue(file.delete());
    }

    @Test
    public void testJsonArrayToList() throws Exception {
        JSONArray jArray = new JSONArray("[{\"a\": 1, \"b\": 2}, {\"c\": 3, \"d\": 4}]");
        ArrayList<JSONObject> aList = Utils.jsonArrayToList(jArray);
        Assert.assertFalse(aList.isEmpty());
        Assert.assertEquals("{\"a\":1,\"b\":2}", aList.get(0).toString());
        Assert.assertEquals("{\"c\":3,\"d\":4}", aList.get(1).toString());
    }

    @Test
    public void testGetEEUrl() throws Exception {
        Assert.assertNotNull(Utils.getEEUrl());
    }

    @Test
    public void testGetSqlQueries() throws Exception {
        Assume.assumeTrue(new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/queries.json").exists());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";

        JSONObject sqlQueries = Utils.getSqlQueries();
        Assert.assertNotNull(sqlQueries);
        Assert.assertNotNull(sqlQueries.get("custom_date"));

        MainRunner.url = null;
        MainRunner.project = null;
        MainRunner.projectDir = null;
    }

    @Test
    public void testGetVirtualReturns() throws Exception {
        Assume.assumeTrue(new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/return_orders.json").exists());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";

        HashMap<String, String> options = new HashMap<>();
        options.put("return_order", "intransit");
        JSONObject order = Utils.getVirtualReturns(options);
        Assert.assertNotNull(order);
        Assert.assertEquals("UNITTESTIN", order.get("order_number"));

        MainRunner.url = null;
        MainRunner.project = null;
        MainRunner.projectDir = null;
    }

    @Test
    public void testGetOrderNumber() throws Exception {
        Assume.assumeTrue(new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/order_mods_data.json").exists());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";

        String orderNumber = Utils.getOrderNumber("processing");
        Assert.assertNotNull(orderNumber);
        Assert.assertEquals("UNITTESTP", orderNumber);

        MainRunner.url = null;
        MainRunner.project = null;
        MainRunner.projectDir = null;
    }

    @Test
    public void testDecryptPassword() throws Exception {
        Assume.assumeTrue(new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/password.json").exists());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";

        String pwd = Utils.decryptPassword("11_AU0QXqYqq/tRJXonlBjwew==");
        Assert.assertNotNull(pwd);
        Assert.assertEquals("Macys123", pwd);

        MainRunner.url = null;
        MainRunner.project = null;
        MainRunner.projectDir = null;
    }

    @Test
    public void testGetContextualizeMedia() throws Exception {
        Assume.assumeTrue(new File("src/test/java/com/macys/sdt/framework/resources/data/website/mcom/contextualize_media.json").exists());
        MainRunner.url = "http://www.qa0codemacys.fds.com";
        MainRunner.project = "framework";
        MainRunner.projectDir = "src/test/java/com/macys/sdt/framework";

        Assert.assertNotNull(Utils.getContextualizeMedia());

        MainRunner.url = null;
        MainRunner.project = null;
        MainRunner.projectDir = null;
    }

    @Test
    public void testProcessWatchDog() throws Exception {
        Assume.assumeFalse("Not working in windows, Need Fix", Utils.isWindows());
        try {
            Process simpleProcess = getRuntime().exec(Utils.isWindows() ? "cd" : "pwd");
            Utils.ProcessWatchDog wd = new Utils.ProcessWatchDog(simpleProcess, 1000, "testProcessWatchDog");
            wd.run();
        } catch (Exception e) {
            Assert.fail("Failed testProcessWatchDog : " + e.getMessage());
        }
    }

    @Test
    public void testThreadWatchDog() throws Exception {

        // For initLogs
        MainRunner.workspace = "";
        Utils.redirectSOut();
        Utils.resetSOut();

        try {
            class SimpleThread extends Thread {
                public void run() {
                    System.out.println("Test Thread Watch Dog");
                }
            }
            SimpleThread simpleThread = new SimpleThread();
            Utils.ThreadWatchDog wd = new Utils.ThreadWatchDog(simpleThread, 1000, "testThreadWatchDog", null);
            wd.run();
        } catch (Exception e) {
            Assert.fail("Failed testProcessWatchDog : " + e.getMessage());
        }
    }
}