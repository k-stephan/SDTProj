package com.macys.sdt.shared.steps.website;

import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Navigate;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.runner.MainRunner;
import com.macys.sdt.framework.utils.StepUtils;
import com.macys.sdt.framework.utils.db.models.MediaService;
import com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse.WebsiteMcomFlexTemplatePage;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.collections4.ListUtils;
import org.junit.Assert;
import org.openqa.selenium.WebElement;

import java.util.*;

public class ContextualContent extends StepUtils {

    WebsiteMcomFlexTemplatePage flexPage = new WebsiteMcomFlexTemplatePage();
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
        String mode = null, regionCode = null;
        for (Map<String, String> set : context) {
            mode = set.get("SHOPPING_MODE");
            regionCode = set.get("REGION_CODE");
        }
        String[] contextAttrNames = context.get(0).keySet().toArray(new String[context.get(0).keySet().size()]);
        String[] contextAttrValues = context.get(0).values().toArray(new String[context.get(0).values().size()]);
        String[] tempMediaNames = mediaNamesArray.clone();
        List<Map> finalMediaData = new ArrayList<>();
        //Fetching data from db and manipulating is taking significant time, and thus resulting in timeouts
        // as a workaround pause the pagehangwatchdog and resume after this task
        StepUtils.pausePageHangWatchDog();
        finalMediaData = MediaService.getFinalContextualizeCanvasData(mediaNamesArray.clone(), rowType, contextAttrNames, contextAttrValues);
        StepUtils.resumePageHangWatchDog();
        for (int index = 0; index < mediaNamesArray.length; index++) {
            mediaNamesArray[index] = ((mediaNamesArray[index].equals("MEDIA_ADS")) ? "THUMBNAIL_GRID" : mediaNamesArray[index]);
            mediaNamesArray[index] = ((mediaNamesArray[index].equals("BANNER_MACHINE_WIDGET")) ? "WIDGET" : mediaNamesArray[index]);
            mediaNamesArray[index] = ((mediaNamesArray[index].equals("BANNER_MACHINE_SLIDESHOW")) ? "SLIDESHOW" : mediaNamesArray[index]);
        }
        List<Map> canvasRowData = new ArrayList<>();
        for (Map media : finalMediaData) {
            boolean isRowFound = false;
            int rowIndex = 0;
            for (Map row : canvasRowData) {
                if (row.containsKey("rowId")) {
                    if (row.get("rowId").toString().equals(media.get("canvasRowId").toString())) {
                        isRowFound = true;
                        rowIndex = canvasRowData.indexOf(row);
                        break;
                    }
                }
            }
            if (isRowFound) {
                ((List) canvasRowData.get(rowIndex).get("mediaTypes")).add(media.get("mediaTypeDesc").toString());
            } else {
                Map dummy = new HashMap();
                dummy.put("rowId", media.get("canvasRowId").toString());
                List<String> dummyList = new ArrayList<>();
                dummyList.add(media.get("mediaTypeDesc").toString());
                dummy.put("mediaTypes", dummyList);
                canvasRowData.add(dummy);
            }
        }
        List<Map> expectedRowData = new ArrayList<>();
        for (Map canvas : canvasRowData) {
            boolean isMediaFlag = false;
            for (String name : ((List<String>) canvas.get("mediaTypes")))
                if (name.equals("PRODUCT_PANEL_CATEGORY") || Arrays.asList(mediaNamesArray).contains(name))
                    isMediaFlag = true;
            if ((Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") || Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR")) ? isMediaFlag : ((ListUtils.subtract(Arrays.asList(mediaNamesArray), (List) canvas.get("mediaTypes"))).size() == 0))
                expectedRowData.add(canvas);
        }
        List<Map> tempData = new ArrayList<>();
        for (Map data : finalMediaData) {
            for (Map mdata : expectedRowData) {
                if (mdata.get("rowId").toString().equals(data.get("canvasRowId").toString())) {
                    tempData.add(data);
                    break;
                }
            }
        }
        finalMediaData = tempData;
        List<Map> newTempData = new ArrayList<>();
        if (pageType.equals("Home Page")) {
            for (Map data : finalMediaData)
                if (Arrays.asList(mediaNamesArray).contains(data.get("mediaTypeDesc").toString()) && mediaNamesArray.length == 1)
                    newTempData.add(data);
            finalMediaData = newTempData;
        } else {
            for (Map data : finalMediaData) {
                for (String name : mediaNamesArray) {
                    if (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") || Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR")) {
                        if (name.equals(data.get("mediaTypeDesc").toString()) || data.get("mediaTypeDesc").toString().equals("PRODUCT_PANEL_CATEGORY"))
                            newTempData.add(data);
                    } else if (name.equals(data.get("mediaTypeDesc").toString()))
                        newTempData.add(data);
                }
            }
            finalMediaData = newTempData;
        }
        List<String> canvasIds = new ArrayList<>();
        for (Map data : finalMediaData)
            canvasIds.add(data.get("canvasId").toString());
        if (canvasIds.isEmpty())
            Assert.fail("ERROR - DATA: Data is not available for expected media:'" + mediaNames + "' in site database");
        List<List> categoryAndCanvasIds = MediaService.getCategoryId(canvasIds, pageType, context, site, tempMediaNames);
        String categoryId = null;
        for (List categoryAndCanvasId : categoryAndCanvasIds) {
            categoryId = categoryAndCanvasId.get(0).toString();
            String canvasId = categoryAndCanvasId.get(1).toString();
            if (categoryAndCanvasId.get(2) != null)
                componentId = categoryAndCanvasId.get(2).toString();
            finalExpectedData = new ArrayList<>();
            for (Map data : finalMediaData)
                if (data.get("canvasId").toString().equals(canvasId))
                    finalExpectedData.add(data);
            if (pageType.equals("Home Page")) {
                if (regionCode.equals("INTL"))
                    if (!Elements.elementPresent("home.goto_us_site"))
                        Assert.fail("User is not in international site!!");
            } else {
                if (mode.equals("SITE"))
                    Navigate.visit("http://www." + MainRunner.url.split("\\.")[1] + ".fds.com/shop/?id=" + categoryId);
                else
                    Navigate.visit("http://www." + MainRunner.url.split("\\.")[1] + ".fds.com/shop/wedding-registry/?id=" + categoryId);
            }
            Wait.forPageReady();
            try {
                pageVerifications(pageType);
                if (!url().contains(categoryId))
                    Assert.fail("ERROR - ENV: Redirected to some other category instead:'" + categoryId + "' category");
                if (String.join(" ", tempMediaNames).contains("MEDIA_ADS") && (pageType.equals("Sub Splash") || pageType.equals("Browse")))
                    if (flexPage.getMediaAdDetails().isEmpty())
                        Assert.fail("ERROR - DATA: Media Ads are not available on thumbnail grid");
                break;
            } catch (AssertionError e) {
                System.out.println(pageType + " page is not loaded for category:" + categoryId + " in context: " + context.toString() + " ");
            }
        }
        if (!pageType.equals("Home Page"))
            pageVerifications(pageType);
        mainPageType = pageType;
    }

    @Then("^I should see \"([^\"]*)\" on the page in \"([^\"]*)\" row$")
    public void I_should_see_on_the_page_in_row(String mediaNames, String rowType) throws Throwable {
        mediaNamesArray = mediaNames.split(", ");
        if (mediaNames.contains("MEDIA_ADS")) {
            for (int index = 0; index < mediaNamesArray.length; index++)
                if (mediaNamesArray[index].equals("MEDIA_ADS"))
                    mediaNamesArray[index] = "THUMBNAIL_GRID";
            mediaAdsFlag = true;
        }
        seqNumbers = flexPage.getSequenceByRowTypes(rowType);
        boolean sequenceSet = false;
        for (Object seqNumber : seqNumbers) {
            actualMediaType = flexPage.getRowMediaByRowTypeSeqNumber(rowType, seqNumber.toString(), true, false);
            // Below code is to skip empty rows.
            if(((Map)actualMediaType.get(0)).get("mediaType") == null)
                continue;
            if (Arrays.asList(mediaNamesArray).contains("SLIDESHOW") || Arrays.asList(mediaNamesArray).contains("WIDGET"))
                for (int index = 0; index < actualMediaType.size(); index++) {
                    if (((Map)actualMediaType.get(index)).get("mediaType").toString().equals("banner_machine_slideshow"))
                        ((Map)actualMediaType.get(index)).put("mediaType", "slideshow");
                    if (((Map)actualMediaType.get(index)).get("mediaType").toString().equals("banner_machine_widget"))
                        ((Map)actualMediaType.get(index)).put("mediaType", "widget");
                }
            String[] productPool = {"PRODUCT_PANEL_CATEGORY_FACET", "PRODUCT_PANEL_POOL", "PRODUCT_PANEL_CATEGORY", "PRODUCT_PANEL_NA", "PRODUCT_PANEL_BAZAAR"};
            for (int index = 0; index < mediaNamesArray.length; index++)
                if (Arrays.asList(productPool).contains(mediaNamesArray[index]))
                    mediaNamesArray[index] = "PRODUCT_POOL";
            if ((((Map) actualMediaType.get(0)).get("mediaType") != null) && ((Map) actualMediaType.get(0)).get("mediaType").toString().equalsIgnoreCase(mediaNamesArray[0])) {
                Iterator iterator = actualMediaType.iterator();
                while (iterator.hasNext()) {
                    Map type = (Map) iterator.next();
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
    }

    @And("^I should see respective media as per astra data$")
    public void I_should_see_respective_media_as_per_astra_data() throws Throwable {
        if (sequence != -1) {
            Iterator iterator = finalExpectedData.iterator();
            while (iterator.hasNext()) {
                Map data = (Map) iterator.next();
                if (data.get("mediaTypeDesc").toString().toLowerCase().contains("product"))
                    data.put("mediaTypeDesc", "PRODUCT_POOL");
                if (data.get("mediaTypeDesc").toString().contains("MEDIA_ADS"))
                    data.put("mediaTypeDesc", "THUMBNAIL_GRID");
                if (data.containsKey("mediaKey") && !data.get("mediaKey").toString().equals(componentId) && data.get("mediaTypeDesc").toString().equals("PRODUCT_POOL"))
                    iterator.remove();
            }
            Map<String, List<Map>> groupResults = groupBy(finalExpectedData, "canvasRowId");
            boolean isSequenceExists = false;
            List<Integer> dbSequences = new ArrayList<>();
            int dbSequence = -1;
            for (List<Map> data : groupResults.values())
                for (Map type : data)
                    if (type.containsKey("canvasRowSeq"))
                        dbSequences.add(Integer.parseInt(type.get("canvasRowSeq").toString()));
            if (dbSequences.size() > 0){
                dbSequence = dbSequences.indexOf(Collections.min(dbSequences));
                isSequenceExists = true;
            }
            int index = 0;
            for (List<Map> data : groupResults.values()) {
                List names = new ArrayList<>();
                for (Map type : data)
                    names.add(type.get("mediaTypeDesc").toString());
                boolean seqCondition = (isSequenceExists ? (dbSequence == index) : true);
                if (ListUtils.subtract(names, Arrays.asList(mediaNamesArray)).isEmpty() && seqCondition) {
                    finalExpectedData = data;
                    break;
                }
                index++;
            }
            if (finalExpectedData.isEmpty())
                Assert.fail("ERROR - DATA : Unable to find data in site database");
            List<Map> mediaDetails = new ArrayList<>();
            if (mainRowType.equals("0") || (Arrays.asList(mediaNamesArray).contains("PRODUCT_POOL"))) {
                for (String seq : seqNumbers) {
                    for (Map one : flexPage.getRowMediaByRowTypeSeqNumber(mainRowType, seq, true, true))
                        mediaDetails.add(one);
                }
            } else {
                for (Map one : flexPage.getRowMediaByRowTypeSeqNumber(mainRowType, String.valueOf(sequence), true, true))
                    mediaDetails.add(one);
            }
            boolean isBannerMachineSlide = false;
            for (Map data : mediaDetails)
                if (data.get("mediaType").toString().equals("banner_machine_slideshow") && ((Arrays.asList(mediaNamesArray).contains("BANNER_MACHINE_SLIDESHOW")) || (Arrays.asList(mediaNamesArray).contains("SLIDESHOW"))))
                    isBannerMachineSlide = true;
            verifyMediaNames(isBannerMachineSlide, mediaDetails);
            List<String> names = new ArrayList<>();
            for (Map type : finalExpectedData)
                names.add(type.get("mediaTypeDesc").toString().replace(" ", "_").toLowerCase());
            for (String name : names) {
                List<Map> uiData = new ArrayList<>();
                List<Map> dbData = new ArrayList<>();
                if (isBannerMachineSlide) {
                    iterator = mediaDetails.iterator();
                    while (iterator.hasNext()) {
                        Map type = (Map) iterator.next();
                        if (!type.get("mediaType").toString().equals("banner_machine_slideshow"))
                            iterator.remove();
                    }
                    uiData = mediaDetails;
                } else {
                    iterator = mediaDetails.iterator();
                    while (iterator.hasNext()) {
                        Map type = (Map) iterator.next();
                        if (!type.get("mediaType").toString().equals(name))
                            iterator.remove();
                    }
                    uiData = mediaDetails;
                }
                iterator = finalExpectedData.iterator();
                while (iterator.hasNext()) {
                    Map type = (Map) iterator.next();
                    if (!type.get("mediaTypeDesc").toString().toLowerCase().replace(" ", "_").equals(name))
                        iterator.remove();
                }
                dbData = finalExpectedData;
                String errorMessage = "ERROR - APP: Expected media type: '" + name + "' is not displayed";
                switch (name) {
                    case "widget":
                        int counter = 0;
                        for (Map data : uiData)
                            if (((Map)data.get("mediaInfo")).isEmpty())
                                counter++;
                        if (counter == uiData.size())
                            System.out.println("Widget is collapsed due to time constraint");
                        else {
                            List<String> media = new ArrayList<>();
                            for (Map data : dbData)
                                for (Map info : (List<Map>) data.get("mediaInfo"))
                                    media.add(info.get("mediaTypeDesc").toString());
                            List<String> actualMedia = new ArrayList<>();
                            for (Map data : uiData) {
                                for (Map type : ((List<Map>) data.get("mediaInfo"))) {
                                    if (type.get("panelType").toString().equals("IMAGE"))
                                        actualMedia.add("AD");
                                    else
                                        actualMedia.add(type.get("panelType").toString());
                                }
                            }
                            for (String mediaName : actualMedia) {
                                if (!media.contains(mediaName))
                                    Assert.fail(errorMessage);

                            }
                        }
                        break;
                    case "category_icon":
                        String textErrorMessage = "ERROR - APP: Category icons text is not displayed with media type:'" + name + "'";
                        String imageErrorMessage = "ERROR - APP: Category icon images are not displayed with media type:'" + name + "'";
                        List<String> dbText = new ArrayList<>();
                        List<String> catIconText = new ArrayList<>();
                        List<String> dbImageNames = new ArrayList<>();
                        List<String> uiImageNames = new ArrayList<>();
                        for (Map data : dbData) {
                            for (Map type : ((List<Map>) data.get("mediaInfo"))) {
                                dbText.add(type.get("text").toString().toLowerCase().replace(" and ", " ").replace(" & ", " "));
                                dbImageNames.add(type.get("mediaName").toString().toLowerCase());
                            }
                        }
                        for (Map data : uiData) {
                            for (Map type : ((List<Map>) data.get("mediaInfo"))) {
                                if (!type.get("text").equals(null))
                                    catIconText.add(type.get("text").toString().toLowerCase().replace(" and ", " ").replace(" & ", " "));
                                uiImageNames.add(type.get("image").toString().toLowerCase());
                            }
                        }
                        for (String text : catIconText)
                            if (!dbText.contains(text))
                                Assert.fail(textErrorMessage);
                        if (!ListUtils.subtract(dbImageNames, uiImageNames).isEmpty())
                            Assert.fail(imageErrorMessage);
                        break;
                    case "slideshow":
                        if (isBannerMachineSlide) {
                            List<String> actualC2Slides = new ArrayList<>();
                            List<String> expectedSlides = new ArrayList<>();
                            for(Map data : uiData)
                                for(Map type : (List<Map>)((Map)data.get("mediaInfo")).get("bannerMachineSlideData"))
                                    if(type.containsKey("c2SlideData"))
                                        actualC2Slides.add(((HashMap)type.get("c2SlideData")).get("imageName").toString());
                            for(Map data : dbData)
                                for(Map type : (List<Map>)data.get("mediaInfo"))
                                    expectedSlides.add(type.get("mediaName").toString().split(".jsp")[0]);
                            for (String slide : actualC2Slides)
                                if (!expectedSlides.contains(slide))
                                    Assert.fail("ERROR - APP: Slide Show is not displayed with valid media resource:'" + slide + "' as per DB:'" + expectedSlides + "'");
                        } else {
                            List<String> uiInfo = new ArrayList<>();
                            List<String> dbInfo = new ArrayList<>();
                            for (Map data : uiData)
                                for (String image : ((List<String>) ((Map) data.get("mediaInfo")).get("slideshowImages")))
                                    uiInfo.add(image);
                            for (Map data : dbData)
                                for (Map image : ((List<Map>) data.get("mediaInfo")))
                                    dbInfo.add(image.get("mediaName").toString());
                            if (!ListUtils.subtract(uiInfo, dbInfo).isEmpty())
                                Assert.fail(errorMessage);
                        }
                        break;
                    case "flexible_pool":
                        String titleErrorMessage = "ERROR - APP: Flexible title is not displayed with media type:'" + name + "'";
                        String headerErrorMessage = "ERROR - APP: Flexible header is not displayed with media type:'" + name + "'";
                        List<String> uiTitleList = new ArrayList<>();
                        List<String> dbTitleList = new ArrayList<>();
                        List<String> uiHeaderList = new ArrayList<>();
                        List<String> dbHeaderList = new ArrayList<>();
                        for (Map data : dbData)
                            for (Map one : ((List<Map>) data.get("mediaInfo")))
                                if (!one.get("mediaName").equals(""))
                                    dbTitleList.add(one.get("description").toString());
                        for (Map data : dbData)
                            for (Map one : ((List<Map>) data.get("mediaInfo")))
                                if (one.get("mediaName").equals(""))
                                    dbHeaderList.add(one.get("description").toString());
                        for (Map data : uiData)
                            for (WebElement type : ((List<WebElement>) ((Map) data.get("mediaInfo")).get("flexTitle")))
                                uiTitleList.add(type.getText());
                        for (Map data : uiData)
                            uiHeaderList.add(((WebElement) (((Map) data.get("mediaInfo")).get("flexHeader"))).getText().split("\n")[0]);
                        if (!ListUtils.subtract(dbTitleList, uiTitleList).isEmpty())
                            Assert.fail(titleErrorMessage);
                        if (!ListUtils.subtract(dbHeaderList, uiHeaderList).isEmpty())
                            Assert.fail(headerErrorMessage);
                        break;
                    case "ad":
                        List<String> mediaNamesList = new ArrayList<>();
                        List<String> uiMediaNamesList = new ArrayList<>();
                        for (Map data : dbData)
                            for (Map type : ((List<Map>) data.get("mediaInfo")))
                                mediaNamesList.add(type.get("mediaName").toString());
                        String dbAdSource = mediaNamesList.get(0);
                        String adErrorMessage = "ERROR - APP: Expected media type:'ad' source:'" + dbAdSource + "' is not displayed as per astra";
                        for (Map data : uiData)
                            uiMediaNamesList.add(((Map) data.get("mediaInfo")).get("imageName").toString());
                        if (dbAdSource != null)
                            if (!uiMediaNamesList.contains(dbAdSource))
                                Assert.fail(adErrorMessage);
                        break;
                    case "recently_reviewed":
                        List<String> textList = new ArrayList<>();
                        for (Map data : dbData)
                            textList.add(data.get("text").toString());
                        if (textList.contains("Recently Reviewed"))
                            if (uiData.isEmpty())
                                Assert.fail("ERROR - APP: Recently review data is not displayed");
                        break;
                    case "image_map":
                    case "custom_popup":
                        List<String> dbImageMapNames = new ArrayList<>();
                        List<String> uiImageMapNames = new ArrayList<>();
                        for (Map data : dbData)
                            for (Map type : ((List<Map>) data.get("mediaInfo")))
                                dbImageMapNames.add(type.get("mediaName").toString());
                        for (Map data : uiData)
                            uiImageMapNames.add(((Map) data.get("mediaInfo")).get("imageName").toString());
                        if (!ListUtils.subtract(dbImageMapNames, uiImageMapNames).isEmpty())
                            Assert.fail(errorMessage);
                        break;
                    case "video":
                        List dbVideoTitles = new ArrayList<>();
                        List uiVideoTitles = new ArrayList<>();
                        for (Map data : dbData)
                            for (Map type : ((List<Map>) data.get("mediaInfo")))
                                if (type.get("description").toString().equals("VIDEO_TITLE"))
                                    dbVideoTitles.add(type.get("text").toString());
                        for (Map data : uiData)
                            uiVideoTitles.add(((Map)data.get("mediaInfo")).get("videoTitle").toString());
                        if (!ListUtils.subtract(dbVideoTitles, uiVideoTitles).isEmpty())
                            Assert.fail(errorMessage);
                        break;
                    case "text":
                        List<String> dbTextData = new ArrayList<>();
                        List<String> uiTextData = new ArrayList<>();
                        for (Map data : dbData)
                            dbTextData.add(data.get("text").toString().toLowerCase());
                        for (Map data : uiData)
                            uiTextData.add(((Map) data.get("mediaInfo")).get("text").toString().toLowerCase());
                        if (dbTextData.size() == 0)
                            for (Map data : dbData)
                                dbTextData.add(data.get("text").toString().toLowerCase());
                        if (!ListUtils.subtract(dbTextData, uiTextData).isEmpty())
                            Assert.fail(errorMessage);
                        break;
                    case "copy_block":
                        List<String> dbCopyData = new ArrayList<>();
                        List<String> uiCopyData = new ArrayList<>();
                        for (Map data : dbData)
                            dbCopyData.add(data.get("text").toString().toLowerCase());
                        for (Map data : uiData)
                            uiCopyData.add(((Map) data.get("mediaInfo")).get("text").toString().toLowerCase());
                        if (!ListUtils.subtract(dbCopyData, uiCopyData).isEmpty())
                            Assert.fail(errorMessage);
                        break;
                    case "thumbnail_grid":
                        if (mediaAdsFlag) {
                            List<String> mediGridNames = new ArrayList<>();
                            List<String> uiMediGridNames = new ArrayList<>();
                            for (Map data : dbData)
                                for (Object type : ((List) data.get("mediaInfo")))
                                    mediGridNames.add(((Map)type).get("mediaName").toString());
                            boolean dataFound = false;
                            for (Map data : uiData)  {
                                if(((Map)data.get("mediaInfo")).get("thumbnailGridExists").getClass().equals(Boolean.class))
                                    dataFound = ((Boolean)((Map)data.get("mediaInfo")).get("thumbnailGridExists")).booleanValue();
                                else
                                    uiMediGridNames.add(((List)((Map) data.get("mediaInfo")).get("thumbnailGridExists")).get(0).toString());
                            }
                            for (String grid : uiMediGridNames)
                                if (mediGridNames.contains(grid))
                                    dataFound = true;
                            if (!dataFound)
                                Assert.fail(errorMessage);
                        } else {
                            List<String> uiMediaTypeDesc = new ArrayList<>();
                            List<String> dbMediaTypeDesc = new ArrayList<>();
                            for (Map data : dbData)
                                dbMediaTypeDesc.add(data.get("mediaTypeDesc").toString().toLowerCase());
                            for (Map data : uiData)
                                uiMediaTypeDesc.add(data.get("mediaType").toString().toLowerCase());
                            if (!ListUtils.subtract(dbMediaTypeDesc, uiMediaTypeDesc).isEmpty())
                                Assert.fail(errorMessage);
                        }
                        break;
                    case "horizontal_rule":
                        Map horizontalRule = new HashMap<>();
                        for (Map data : dbData)
                            if (data.containsKey("text") || !data.get("text").equals(null) || data.get("text").toString().contains("Horizontal Rule")) {
                                horizontalRule = data;
                                break;
                            }
                        if (horizontalRule.isEmpty())
                            for (Map data : uiData)
                                if (!((boolean) ((Map) data.get("mediaInfo")).get("horizontalRuleExists")))
                                    Assert.fail(errorMessage);
                        break;
                    case "product_pool":
                        List<String> poolData = new ArrayList<>();
                        List<String> uiPoolData = new ArrayList<>();
                        for (Map data : dbData)
                            for (Map type : ((List<Map>) data.get("mediaInfo")))
                                poolData.add(type.get("text").toString().toLowerCase());
                        for (Map data : uiData)
                            uiPoolData.add(((Map) data.get("mediaInfo")).get("title").toString().toLowerCase());
                        if (!uiPoolData.contains(poolData.get(0)))
                            Assert.fail(errorMessage);
                        break;
                    case "jsp":
                        Map jspData = new HashMap<>();
                        for (Map data : dbData)
                            if (data.get("mediaTypeDesc").toString().contains("JSP")) {
                                jspData = data;
                                break;
                            }
                        if (jspData.equals(null))
                            for (Map data : uiData)
                                if (!((boolean) ((Map) data.get("mediaInfo")).get("jspExists")))
                                    Assert.fail(errorMessage);
                        break;
                    default:
                        Assert.fail("ERROR -ENV : Invalid media type!!");
                }
            }
        } else {
            System.out.println("Widget is collapsed");
        }
    }

    public void verifyMediaNames(boolean isBannerMachineSlide, List<Map> mediaDetails) throws Throwable {
        List<String> names = new ArrayList<>();
        for (Map type : finalExpectedData)
            names.add(type.get("mediaTypeDesc").toString().replace(" ", "_").toLowerCase());
        List<String> mediaNames = new ArrayList<>();
        for (Map type : mediaDetails) {
            if (isBannerMachineSlide)
                mediaNames.add(((type.get("mediaType").toString().equals("banner_machine_slideshow")) ? "slideshow" : type.get("mediaType").toString()));
            else
                mediaNames.add(((type.get("mediaType").toString().equals("banner_machine_widget")) ? "widget" : type.get("mediaType").toString()));
        }
        if (!ListUtils.subtract(names, mediaNames).isEmpty())
            Assert.fail("Media are mismatch in DB and UI");
    }

    public Map<String, List<Map>> groupBy(List<Map> originalData, String key) throws Throwable {
        Map<String, List<Map>> hashMap = new HashMap<String, List<Map>>();
        for (Map data : originalData) {
            String mainKey = data.get(key).toString();
            if (!hashMap.containsKey(mainKey))
                hashMap.put(mainKey, (new ArrayList<Map>()));
            hashMap.get(mainKey).add(data);
        }
        return hashMap;
    }

    public void pageVerifications(String pageType) throws Throwable {
        if (pageType.equals("Category Splash"))
            Assert.assertTrue("User is not redirected to category_splash page", onPage("category_splash"));
        else if (pageType.equals("Sub Splash"))
            Assert.assertTrue("User is not redirected to category_sub_splash page", onPage("category_sub_splash"));
        else
            Assert.assertTrue("User is not redirected to category_browse page", onPage("category_browse"));
    }
}
