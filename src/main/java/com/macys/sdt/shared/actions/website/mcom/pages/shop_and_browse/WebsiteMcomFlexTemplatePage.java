package com.macys.sdt.shared.actions.website.mcom.pages.shop_and_browse;

import com.macys.sdt.framework.interactions.Clicks;
import com.macys.sdt.framework.interactions.Elements;
import com.macys.sdt.framework.interactions.Wait;
import com.macys.sdt.framework.utils.StepUtils;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.*;

public class WebsiteMcomFlexTemplatePage extends StepUtils {

    public List getSequenceByRowTypes(String rowType) {
        List<String> seqNumbers = new ArrayList<>();
        List<WebElement> rowElements = rowElements();
        for (WebElement element : rowElements) {
            if (element.getAttribute("id").contains("row_" + rowType)) {
                String[] id = element.getAttribute("id").split("_");
                seqNumbers.add(id[id.length - 1]);
            }
        }
        return seqNumbers;
    }

    public List<Map> getMediaAdDetails() {
        List<Map> mediaAdDetails = new ArrayList<>();
        List<WebElement> refreshedThumbnailWrappers = Elements.findElement(By.id("thumbnails")).findElements(By.className("productThumbnail"));
        for (int index = 0; index < refreshedThumbnailWrappers.size(); index++) {
            if (refreshedThumbnailWrappers.get(index).isDisplayed() && refreshedThumbnailWrappers.get(index).getAttribute("id").contains("gridMedia")) {
                Map type = new HashMap<>();
                type.put("mediaAdPosition", index + 1);
                type.put("mediaImageSrc", refreshedThumbnailWrappers.get(index).findElement(By.tagName("img")).getAttribute("src"));
                mediaAdDetails.add(type);
            }
        }
        return mediaAdDetails;
    }

    public List<WebElement> rowElements() {
        List<WebElement> rowElements = new ArrayList<>();
        WebElement rowContainer = Elements.findElement(By.id((macys() ? "catSplash" : ((Elements.elementPresent(By.id("catSplash"))) ? "catSplash" : "bl_hp_main"))));
        for (WebElement element : rowContainer.findElements(By.className("row"))) {
            if (element.getAttribute("id").contains("row")) {
                rowElements.add(element);
            }
        }
        return rowElements;
    }

    public List<Map> getRowMediaByRowTypeSeqNumber(String rowType, String seqNumber, boolean exceptEmptyRow, boolean c2Flag) {
        List rowMedia = new ArrayList<>();
        String rowTypeId = "row_" + rowType + "_" + seqNumber;
        String[] oneRows = {"0", "101"};
        String[] otherRows = {"2", "4", "6", "8"};
        if (Arrays.asList(oneRows).contains(rowType)) {
            rowMedia.add(getZoneMedia(rowType, rowTypeId, exceptEmptyRow, c2Flag, null));
        } else if (Arrays.asList(otherRows).contains(rowType)) {
            int zoneCount = (rowType.equals("2")) ? 3 : (rowType.equals("4") ? 4 : 8);
            for (int columnIndex = 0; columnIndex < zoneCount; columnIndex++) {
                String zoneId = null;
                if (columnIndex == 0)
                    zoneId = "row" + rowTypeId.split("_")[rowTypeId.split("_").length - 1] + "_column1";
                if (columnIndex == 1)
                    zoneId = "row" + rowTypeId.split("_")[rowTypeId.split("_").length - 1] + "_stacked1";
                if (columnIndex > 1) {
                    String[] fewOtherRows = {"2", "4"};
                    if (Arrays.asList(fewOtherRows).contains(rowType))
                        zoneId = "row" + rowTypeId.split("_")[rowTypeId.split("_").length - 1] + "_stacked" + columnIndex;
                    else
                        zoneId = "row" + rowTypeId.split("_")[rowTypeId.split("_").length - 1] + "_grid" + (columnIndex - 1);
                }
                rowMedia.add(getZoneMedia(rowType, rowTypeId, exceptEmptyRow, c2Flag, zoneId));
            }
        } else {
            for (int columnIndex = 0; columnIndex < Integer.parseInt(rowType.split("")[rowType.split("").length - 1]); columnIndex++) {
                // Generate zone element ID for the given row eventType and sequence number based on index (ex: row_type_id: 'row1_column1')
                String zoneId = "row" + rowTypeId.split("_")[rowTypeId.split("_").length - 1] + "_column" + (columnIndex + 1);
                rowMedia.add(getZoneMedia(rowType, rowTypeId, exceptEmptyRow, c2Flag, zoneId));
            }
        }
        return rowMedia;
    }

    public Map getZoneMedia(String rowType, String rowTypeId, boolean exceptEmptyRow, boolean c2Flag, String zoneId) {
        String[] oneRows = {"0", "101"};
        String mediaType = null;
        Map mediaData = new HashMap<>();
        if (Arrays.asList(oneRows).contains(rowType)) {
            mediaType = getMediaTypeByRow(getRowElement(rowTypeId), exceptEmptyRow);
        } else {
            mediaType = getMediaTypeByRow(getRowZoneElement(rowTypeId, zoneId), exceptEmptyRow);
        }
        mediaData.put("mediaType", mediaType);
        if (c2Flag) {
            if (Arrays.asList(oneRows).contains(rowType)) {
                mediaData.put("mediaInfo", getMediaValues(getRowElement(rowTypeId), mediaType, exceptEmptyRow, c2Flag));
            } else {
                mediaData.put("mediaInfo", getMediaValues(getRowZoneElement(rowTypeId, zoneId), mediaType, exceptEmptyRow, c2Flag));
            }
        }
        return mediaData;
    }

    public Map getMediaValues(WebElement rowElement, String mediaType, boolean exceptEmptyRow, boolean c2Flag) {
        Map mediaData = new HashMap<>();
        if (mediaType == null)
            mediaType = getMediaTypeByRow(rowElement, exceptEmptyRow);
        if(mediaType == null)
            Assert.fail("ERROR - APP: Invalid media eventType (null)!!");
        switch (mediaType) {
            case "video":
                mediaData = getVideoMediaData(rowElement);
                break;
            case "slideshow":
                if (c2Flag) {
                    List slideshowImages = new ArrayList<>();
                    for (WebElement element : rowElement.findElement(By.className("slideContainer")).findElements(By.className("bannerSlides"))) {
                        String[] image = element.findElement(By.tagName("img")).getAttribute("src").split("/");
                        slideshowImages.add(image[image.length - 1]);
                    }
                    mediaData.put("slideshowImages", slideshowImages);
                } else
                    mediaData.put("slideshowElements", rowElement.findElement(By.className("slideContainer")).findElements(By.className("bannerSlides")));
                break;
            case "recently_reviewed":
                WebElement element = rowElement.findElements(By.id("splashReviews")).size() > 0 ? rowElement.findElement(By.id("splashReviews")) : rowElement.findElement(By.id("reviewsRow"));
                mediaData.put("recentlyReviewedElements", element.findElements(By.id("splashReview")));
                break;
            case "flexible_pool":
                if (c2Flag) {
                    mediaData = getFlexPoolData(rowElement);
                } else
                    mediaData.put("flexiblePoolElements", rowElement.findElements(By.className("flexPool")));
                break;
            case "image_map":
                if (c2Flag) {
                    String[] imageNames = rowElement.findElement(By.tagName("img")).getAttribute("src").split("/");
                    mediaData.put("imageName", imageNames[imageNames.length - 1]);
                } else {
                    mediaData.put("imageAreaElements", rowElement.findElements(By.tagName("area")));
                }
                break;
            case "ad":
            case "image":
                if (c2Flag) {
                    String[] imageNames = rowElement.findElement(By.tagName("img")).getAttribute("src").split("/");
                    mediaData.put("imageName", imageNames[imageNames.length - 1]);
                } else {
                    mediaData.put("imageElements", rowElement.findElements(By.tagName("img")));
                }
                break;
            case "category_icon":
                if (c2Flag) {
                    mediaData.put("catIconData", getCatIconData(rowElement));
                } else {
                    mediaData.put("catIconElements", rowElement.findElements(By.className("adCatIcon")));
                }
                break;
            case "horizontal_rule":
                if (c2Flag)
                    mediaData.put("horizontalRuleExists", rowElement.findElements(By.className("row-hr")).size() > 0);
                else
                    mediaData.put("horizontalRuleElement", rowElement.findElement(By.className("row-hr")));
                break;
            case "text":
                if (c2Flag)
                    mediaData.put("text", rowElement.findElement(By.className("adFlexText")).findElement(By.className("textComponent")).getText());
                else
                    mediaData.put("textElement", rowElement.findElement(By.className("adFlexText")).findElement(By.className("textComponent")));
                break;
            case "custom_popup":
                if (c2Flag) {
                    for (WebElement linkElement : rowElement.findElements(By.tagName("a"))) {
                        if (linkElement.getAttribute("id").contains("popUpLink_")) {
                            String[] imageNames = linkElement.findElement(By.tagName("img")).getAttribute("src").split("/");
                            mediaData.put("imageName", imageNames[imageNames.length - 1]);
                        }
                    }
                } else {
                    List popupElements = new ArrayList<>();
                    for (WebElement linkElement : rowElement.findElements(By.tagName("a"))) {
                        if (linkElement.getAttribute("id").contains("popUpLink_"))
                            popupElements.add(linkElement);
                    }
                    mediaData.put("popupElements", popupElements);
                }
                break;
            case "thumbnail_grid":
                if (c2Flag) {
                    List<WebElement> gridElements = new ArrayList<>();
                    for (WebElement gridElement : rowElement.findElement(By.id("thumbnails")).findElements(By.tagName("li"))) {
                        if (macys()) {
                            if (gridElement.getAttribute("id").contains("gridMedia"))
                                gridElements.add(gridElement);
                        } else {
                            if (gridElement.findElements(By.className("mediaAd")).size() > 0)
                                gridElements.add(gridElement);
                        }
                    }
                    if (gridElements.size() == 0)
                        mediaData.put("thumbnailGridExists", (rowElement.getAttribute("class").contains("rowThumbnail") || rowElement.findElements(By.id("breadcrumbs")).size() > 0));
                    else {
                        List imageNames = new ArrayList<>();
                        for (WebElement gridElement : gridElements) {
                            String[] names = gridElement.findElement(By.tagName("img")).getAttribute("src").split("/");
                            imageNames.add(names[names.length - 1]);
                        }
                        mediaData.put("thumbnailGridExists", imageNames);
                    }
                } else
                    mediaData.put("thumbnailGridElement", rowElement.findElements(By.className("rowThumbnail")).size() > 0 ? (rowElement.findElement(By.className("rowThumbnail"))) : (rowElement.findElement(By.className("breadcrumbs"))));
                break;
            case "product_pool":
                mediaData = getProductPoolContent(rowElement);
                break;
            case "self_select_product_pool":
                mediaData = getSelfSelectProductPoolContent(rowElement);
                break;
            case "widget":
                mediaData = getWidgetContents(rowElement);
                break;
            case "jsp":
                List<WebElement> listElements = new ArrayList<>();
                for(WebElement element1 : rowElement.findElements(By.tagName("ul")))
                    for(WebElement element2 : element1.findElements(By.tagName("li")))
                        listElements.add(element2);
                mediaData.put("jspExists", ((rowElement.findElements(By.tagName("style")).size() > 0) || ((listElements.size() > 0) ? ((!listElements.get(0).findElements(By.tagName("link")).isEmpty()) && (listElements.get(0).findElement(By.tagName("link")).getAttribute("href").contains(".css"))) : false)));
                break;
            case "banner_machine":
                mediaData = getBannerMachineData(rowElement);
                break;
            case "banner_machine_slideshow":
                mediaData = getBannerMachineDataFromSlides(rowElement);
                break;
            case "banner_machine_widget":
                mediaData = getBannerMachineDataFromWidget(rowElement);
                break;
            case "copy_block":
                WebElement copyElement = rowElement.findElement(By.xpath("..//..//..//..//.."));
                if (c2Flag)
                    mediaData.put("text", ((copyElement.findElements(By.id("copyBlockContainer")).size() > 0) ? copyElement.findElement(By.id("copyBlockContainer")) : ((copyElement.findElement(By.xpath("..")).findElements(By.id("copyBlockContainer")).size() > 0) ? copyElement.findElement(By.xpath("..")).findElement(By.id("copyBlockContainer")) : rowElement.findElement(By.id("copyBlockArea")))).findElement(By.tagName("h2")).getText().toLowerCase());
                else
                    mediaData.put("copyElement", copyElement.findElement(By.id("copyBlockContainer")).isEnabled() ? copyElement.findElement(By.id("copyBlockContainer")) : (copyElement.findElement(By.xpath("..")).findElement(By.id("copyBlockContainer")).isEnabled() ? copyElement.findElement(By.xpath("..")).findElement(By.id("copyBlockContainer")) : rowElement.findElement(By.id("copyBlockArea"))));
                break;
            default:
                Assert.fail("ERROR - APP: Invalid media eventType");
        }
        return mediaData;
    }

    public Map getBannerMachineDataFromWidget(WebElement rowElement) {
        Map bannerWidgetData = new HashMap<>();
        List widgetData = new ArrayList<>();
        List<WebElement> widgetButtons = (macys() ? (rowElement.findElement(By.className("navBtns")).findElements(By.tagName("div"))) : (rowElement.findElement(By.id("fw_navBarTabs")).findElements(By.tagName("li"))));
        WebElement widgetContentSection = rowElement.findElement(By.id("widgetContentId"));
        List<WebElement> widgetElements = widgetContentSection.findElements(By.className("mediagroup"));
        for (int index = 0; index < widgetButtons.size(); index++) {
            Clicks.click(widgetButtons.get(index));
            WebElement bmWidgetElement = widgetElements.get(index).findElement(By.className((macys() ? "bm-main-wrapper" : "bannerImage")));
            if (bmWidgetElement.isEnabled()) {
                Map widget = new HashMap<>();
                widget.put("bmSlideSeqNumber", (index + 1));
                widget.put("bannerMachineData", getBannerMachineData(widgetElements.get(index)));
                widgetData.add(widget);
            }
        }
        bannerWidgetData.put("bannerMachineWidgetData", widgetData);
        return bannerWidgetData;
    }

    public Map getBannerMachineDataFromSlides(WebElement rowElement) {
        Map bannerSildeData = new HashMap<>();
        List slidesData = new ArrayList<>();
        List<WebElement> slideButtons = rowElement.findElements(By.className("slideNumberNode"));
        List<WebElement> slideElements = rowElement.findElements(By.className("bannerSlides"));
        for (int index = 0; index < slideButtons.size(); index++) {
            Clicks.click(slideButtons.get(index));
            WebElement bmSlideElement = slideElements.get(index).findElements(By.className((macys() ? "bm-main-wrapper" : "bannerImage"))).size() > 0 ? slideElements.get(index).findElement(By.className((macys() ? "bm-main-wrapper" : "bannerImage"))) : null;
            Map bannerData = new HashMap<>();
            if (bmSlideElement != null) {
                bannerData.put("bmSlideSeqNumber", (index + 1));
                bannerData.put("bannerMachineData", getBannerMachineData(slideElements.get(index)));
            } else {
                WebElement sElement = slideElements.get(index).findElements(By.tagName("link")).size() > 0 ? slideElements.get(index).findElement(By.tagName("link")) : null;
                bannerData.put("bmSlideSeqNumber", (index + 1));
                Map c2Slide = new HashMap<>();
                String imageName = null;
                if (sElement != null && sElement.getAttribute("href").contains(".css")) {
                    String[] names = sElement.getAttribute("href").split("/");
                    imageName = names[names.length - 1].split(".css")[0];
                } else {
                    String[] names = slideElements.get(index).findElement(By.tagName("img")).getAttribute("src").split("/");
                    imageName = names[names.length - 1];
                }
                c2Slide.put("imageName", imageName);
                bannerData.put("c2SlideData", c2Slide);
            }
            slidesData.add(bannerData);
        }
        bannerSildeData.put("bannerMachineSlideData", slidesData);
        return bannerSildeData;
    }

    public Map getBannerMachineData(WebElement rowElement) {
        Map bannerMachineData = new HashMap<>();
        if (macys()) {
            String displayType = null;
            for (String type : (rowElement.findElement(By.className("bm-main-wrapper")).findElement(By.xpath("..")).getAttribute("class").split(" "))) {
                String[] dummyArray = {"huge", "large", "medium", "small"};
                if (Arrays.asList(dummyArray).contains(type)) {
                    displayType = type.toLowerCase();
                }
            }
            WebElement urgentDetailElement, urgentDetailFullElement = null, urgentDetailShortElement = null, urgentHeadlineElement, urgentHeadlineFullElement = null, urgentHeadlineShortElement = null, urgencyPreHeadlineElement = null, preHeadlineElement, preHeadlineFullElement = null, preHeadlineShortElement = null, headlineElement, bmImageSelectionElement, headlineFullElement = null, headlineShortElement = null, bmImageElement;
            urgentDetailElement = rowElement.findElements(By.className("urgent-detail")).size() > 0 ? rowElement.findElement(By.className("urgent-detail")) : null;
            if (urgentDetailElement != null) {
                urgentDetailFullElement = urgentDetailElement.findElement(By.className("fullText"));
                urgentDetailShortElement = urgentDetailElement.findElement(By.className("shortText"));
            }
            urgentHeadlineElement = rowElement.findElements(By.className("urgent-headline")).size() > 0 ? rowElement.findElement(By.className("urgent-headline")) : null;
            if (urgentHeadlineElement != null) {
                urgentHeadlineFullElement = urgentHeadlineElement.findElement(By.className("fullText"));
                urgentHeadlineShortElement = urgentHeadlineElement.findElement(By.className("shortText"));
            }
            urgencyPreHeadlineElement = rowElement.findElements(By.className("urgent-preheadline")).size() > 0 ? rowElement.findElement(By.className("urgent-preheadline")) : null;
            preHeadlineElement = rowElement.findElements(By.className("preheadline")).size() > 0 ? rowElement.findElement(By.className("preheadline")) : null;
            if (preHeadlineElement != null) {
                preHeadlineFullElement = preHeadlineElement.findElement(By.className("fullText"));
                preHeadlineShortElement = preHeadlineElement.findElement(By.className("shortText"));
            }
            headlineElement = rowElement.findElement(By.className("headline"));
            bmImageSelectionElement = rowElement.findElements(By.className("bm-main-wrapper")).size() > 0 ? rowElement.findElement(By.className("bm-main-wrapper")) : null;
            if (headlineElement != null) {
                headlineFullElement = headlineElement.findElement(By.className("fullText"));
                headlineShortElement = headlineElement.findElement(By.className("shortText"));
            }
            String imageClassName = ((displayType.equals("large") || displayType.equals("huge")) ? "imageLarge" : "imageSmall");
            bmImageElement = (bmImageSelectionElement != null ? bmImageSelectionElement.findElement(By.className(imageClassName)) : null);
            WebElement bmDetailTextElement = null, bmDetailLinkElement = null, subHeadlineElement = null, promoTextElement = null, promoCodeElement = null;
            if (bmImageSelectionElement != null) {
                bmDetailTextElement = bmImageSelectionElement.findElement(By.className("detailText"));
                bmDetailLinkElement = bmImageSelectionElement.findElement(By.className("detailLink"));
                subHeadlineElement = bmImageSelectionElement.findElements(By.className("subheadline")).size() > 0 ? bmImageSelectionElement.findElement(By.className("subheadline")) : null;
                promoTextElement = bmImageSelectionElement.findElements(By.className("promoText")).size() > 0 ? bmImageSelectionElement.findElement(By.className("promoText")) : null;
                promoCodeElement = bmImageSelectionElement.findElements(By.className("promoCode")).size() > 0 ? bmImageSelectionElement.findElement(By.className("promoCode")) : null;
            }
            bannerMachineData.put("bannerMachineImageSrc", ((bmImageElement != null && bmImageElement != null) ? bmImageElement.getAttribute("src") : null));
            Map bannerMachineText = new HashMap<>();
            bannerMachineText.put("preHeadlineText", ((preHeadlineFullElement != null && preHeadlineFullElement.isEnabled()) ? preHeadlineFullElement.getText() : null));
            bannerMachineText.put("preHeadlineShortText", ((preHeadlineShortElement != null && preHeadlineShortElement.isEnabled()) ? preHeadlineShortElement.getText() : null));
            bannerMachineText.put("headlineText", ((headlineFullElement != null && headlineFullElement.isEnabled()) ? headlineFullElement.getText() : null));
            bannerMachineText.put("headlineShortText", ((headlineShortElement != null && headlineShortElement.isEnabled()) ? headlineShortElement.getText() : null));
            bannerMachineText.put("subHeadlineText", ((subHeadlineElement != null && subHeadlineElement.isEnabled()) ? subHeadlineElement.getText() : null));
            bannerMachineText.put("promoCode", ((promoCodeElement != null && promoCodeElement.isEnabled()) ? promoCodeElement.getText() : null));
            bannerMachineText.put("promoCodeText", ((promoTextElement != null && promoTextElement.isEnabled()) ? promoTextElement.getText() : null));
            bannerMachineText.put("detailsText", ((bmDetailTextElement != null && bmDetailTextElement.isEnabled()) ? bmDetailTextElement.getText() : null));
            bannerMachineText.put("detailsLink", ((bmDetailLinkElement != null && bmDetailLinkElement.isEnabled()) ? ((bmDetailLinkElement.findElements(By.tagName("a")).size() > 0) ? bmDetailLinkElement.findElement(By.tagName("a")).getAttribute("innerHTML") : bmDetailLinkElement.getAttribute("innerHTML")) : null));
            bannerMachineText.put("urgencyPreHeadlineText", ((urgencyPreHeadlineElement != null && urgencyPreHeadlineElement.isEnabled()) ? urgencyPreHeadlineElement.getText() : null));
            bannerMachineText.put("urgencyPreHeadlineShortText", ((urgencyPreHeadlineElement != null && urgencyPreHeadlineElement.isEnabled()) ? urgencyPreHeadlineElement.getText() : null));
            bannerMachineText.put("urgencyHeadlineText", ((urgentHeadlineFullElement != null && urgentHeadlineFullElement.isEnabled()) ? urgentHeadlineFullElement.getText() : null));
            bannerMachineText.put("urgencyHeadlineShortText", ((urgentHeadlineShortElement != null && urgentHeadlineShortElement.isEnabled()) ? urgentHeadlineShortElement.getText() : null));
            bannerMachineText.put("urgencyDetailText", ((urgentDetailFullElement != null && urgentDetailFullElement.isEnabled()) ? urgentDetailFullElement.getText() : null));
            bannerMachineText.put("urgencyDetailShortText", ((urgentDetailShortElement != null && urgentDetailShortElement.isEnabled()) ? urgentDetailShortElement.getText() : null));
            bannerMachineData.put("displayClass", displayType);
            bannerMachineData.put("bannerMachineText", bannerMachineText);
        } else {
            WebElement bannerContainerElement, bannerImageElement, bannerLinkSectionElement, urgentDetailElement;
            List bannerMachineLinks = new ArrayList<>();
            bannerContainerElement = rowElement.findElement(By.className("bannerImage"));
            bannerImageElement = bannerContainerElement.findElement(By.tagName("img"));
            bannerLinkSectionElement = ((rowElement.findElements(By.className("bannerLinks")).size() > 0) ? rowElement.findElement(By.className("bannerLinks")) : null);
            urgentDetailElement = ((rowElement.findElements(By.className("bannerText")).size() > 0) ? rowElement.findElement(By.className("bannerText")) : null);
            List bannerLinks = new ArrayList<>();
            if (bannerLinkSectionElement != null) {
                for (WebElement bannerLink : bannerLinkSectionElement.findElements(By.tagName("a"))) {
                    Map links = new HashMap<>();
                    links.put("linkText", bannerLink.getText());
                    links.put("linkUrl", bannerLink.getAttribute("href"));
                    bannerLinks.add((HashMap) links);
                }
            }
            bannerMachineData.put("bannerMachineImageSrc", (bannerImageElement.isEnabled() ? bannerImageElement.getAttribute("src") : null));
            bannerMachineData.put("urgencyDetailText", (urgentDetailElement != null ? urgentDetailElement.findElement(By.tagName("span")).getAttribute("innerHTML") : null));
            bannerMachineData.put("bannerMachineLinks", bannerLinks);
        }
        return bannerMachineData;
    }

    public Map getWidgetContents(WebElement rowElement) {
        Map widgetContents = new HashMap<>();
        WebElement widgetContainer = rowElement.findElement(By.id((macys() ? "flexWidget_25" : "flexWidget")));
        List<WebElement> widgetButtons = (macys() ? widgetContainer.findElement(By.className("navBtns")).findElements(By.tagName("div")) : widgetContainer.findElement(By.id("fw_navBarTabs")).findElements(By.tagName("li")));
        System.out.print("Total buttons in widget: "+widgetButtons.size());
        List panels = new ArrayList<>();
        WebElement panelContainer = widgetContainer.findElement(By.id((macys() ? "widgetContentId" : "panelContainer")));
        if (widgetButtons.size() > 1) {
            List<WebElement> panelElements = panelContainer.findElements(By.className(macys() ? "active" : "fw_panel"));
            for (int index = 0; index < widgetButtons.size(); index++) {
                String widgetPanelTitle = (macys() ? widgetButtons.get(index).getAttribute("title") : widgetButtons.get(index).getText());
                Clicks.click(widgetButtons.get(index));
                Wait.untilElementPresent(By.className(panelElements.get(index).getAttribute("class")));
                Map panelData = getCommonWidgetData(panelElements.get(index));
                panelData.put("title", widgetPanelTitle);
                panels.add(panelData);
            }
        } else {
            WebElement panelElement = panelContainer.findElements(By.className("active")).size() > 0 ? panelContainer.findElement(By.className("active")) : null;
            if (panelElement != null)
                panels.add(getCommonWidgetData(panelElement));
        }
        return widgetContents;
    }

    public Map getCommonWidgetData(WebElement panelElement) {
        Map panelData = new HashMap<>();
        panelData.put("panelType", (macys() ? panelElement.findElement(By.id("pType")).getAttribute("value") : getMediaTypeByRow(panelElement, false)));
        panelData.put("shopAllExists", panelElement.findElement(By.className("shopAllLink")).isDisplayed());
        return panelData;
    }

    public Map getSelfSelectProductPoolContent(WebElement rowElement) {
        Map productPoolData = new HashMap<>();
        String rowId = rowElement.getAttribute("id");
        List<String> categories = new ArrayList<>();
        for (WebElement element : rowElement.findElements(By.cssSelector("a.selfSelectLink"))) {
            categories.add(element.getText());
        }
        for (String text : categories) {
            Clicks.click(rowElement.findElement(By.linkText(text)));
            Wait.untilElementPresent(By.xpath(".//a[contains(@class,'selfSelectLink active') and contains(text(),'" + text + "')]"));
            productPoolData.put(rowElement.findElement(By.cssSelector("a.selfSelectLink.active")).getText(), getProductPoolContent(rowElement));
        }
        return productPoolData;
    }

    public Map getProductPoolContent(WebElement rowElement) {
        Map productPoolData = new HashMap<>();
        String seqNumber = macys() ? rowElement.findElement(By.id("panelRow")).getAttribute("value") : "0";
        boolean shopAllExists = rowElement.findElement(By.className((macys() ? "shopAll" : "shopAllLink"))).findElement(By.tagName("a")).isDisplayed();
        boolean titleExists = rowElement.findElement(By.className((macys() ? "adText" : "adFlexText"))).isDisplayed();
        List productIds = new ArrayList<>();
        if (macys()) {
            for (WebElement element : rowElement.findElements(By.className("li-poolThumbnails" + seqNumber))) {
                productIds.add(Integer.parseInt(element.findElement(By.tagName("input")).getAttribute("id").split("_")[0]));
            }
        } else {
            for (WebElement element : rowElement.findElements(By.className("productThumbnail"))) {
                productIds.add(Integer.parseInt(element.getAttribute("id")));
            }
        }
        productPoolData.put("seqNumber", seqNumber);
        productPoolData.put("title", titleExists ? rowElement.findElement(By.className((macys() ? "adText" : "adFlexText"))).getText() : "");
        productPoolData.put("productIds", productIds);
        productPoolData.put("shopAllExists", shopAllExists);
        productPoolData.put("shopAllHref", shopAllExists ? rowElement.findElement(By.className((macys() ? "shopAll" : "shopAllLink"))).findElement(By.tagName("a")).getAttribute("href") : "");
        return productPoolData;
    }

    public Map getCatIconData(WebElement rowElement) {
        Map catIconData = new HashMap<>();
        for (WebElement element : rowElement.findElements(By.className("adCatIcon"))) {
            WebElement catIconElement = element.findElement(By.className("adFlexText"));
            String[] imageNames = element.findElement(By.tagName("img")).getAttribute("src").split("/");
            catIconData.put("catIconText", (catIconElement.isDisplayed() ? catIconElement.getText() : null));
            catIconData.put("catIconImage", imageNames[imageNames.length - 1]);
        }
        return catIconData;
    }

    public Map getFlexPoolData(WebElement rowElement) {
        Map flexData = new HashMap<>();
        flexData.put("flexHeader", rowElement.findElement(By.className("flexPoolHeaderText")));
        flexData.put("flexTitle", rowElement.findElements(By.className("flexPool")));
        return flexData;
    }

    public Map getVideoMediaData(WebElement rowElement) {
        Map videoData = new HashMap<>();
        List<WebElement> videoElements = rowElement.findElements(By.xpath(macys() ? "//div[@class='columned']/ul/li/script" : "//ul[contains(@class, 'columned')]/li/script"));
        List<String> scriptData = new ArrayList<>();
        for (WebElement element : videoElements) {
            String idText = null;
            if (macys())
                idText = element.findElement(By.xpath("..//..//..//..")).getAttribute("id");
            else
                idText = element.findElement(By.xpath("..//..//..")).getAttribute("id");
            if (idText.equals(rowElement.getAttribute("id"))) {
                scriptData.add(element.getAttribute("innerHTML"));
            }
        }
        List<String> titles = new ArrayList<>();
        for (String data : scriptData) {
            if (data.contains("vidTitle")) {
                for (String line : data.split("\n")) {
                    if (line.contains("vidTitle")) {
                        for (String temp : line.split("\"")) {
                            titles.add(temp);
                        }
                    }
                }
            }
        }
        videoData.put("videoTitle", titles.get(0).split("'")[1]);
        return videoData;
    }

    public WebElement getRowElement(String rowId) {
        WebElement rowElement = null;
        List<WebElement> rowElements = rowElements();
        for (WebElement element : rowElements) {
            if (element.getAttribute("id").equals(rowId)) {
                rowElement = element;
            }
        }
        if (rowElement == null)
            Assert.fail("Element not displayed on page with id: " + rowId);
        return rowElement;
    }

    public WebElement getRowZoneElement(String rowId, String rowZoneId) {
        WebElement rowZoneElement = null;
        String rowType = rowId.split("_")[1];
        String[] types = {"2", "4", "6", "8"};
        if (macys() && (Arrays.asList(types).contains(rowType))) {
            WebElement columnElement = getRowElement(rowId).findElement(By.className("columned")).findElement(By.id(rowZoneId));
            WebElement stackElement = getRowElement(rowId).findElement(By.className("stacked")).findElement(By.id(rowZoneId));
            WebElement gridElement = getRowElement(rowId).findElement(By.className("stacked")).findElement(By.className("grid")).findElement(By.id(rowZoneId));
            if (columnElement.isDisplayed())
                rowZoneElement = columnElement;
            if (stackElement.isDisplayed())
                rowZoneElement = stackElement;
            if (gridElement.isDisplayed())
                rowZoneElement = gridElement;
        } else {
            rowZoneElement = getRowElement(rowId).findElement(By.className("columned")).findElement(By.id(rowZoneId));
        }
        if (rowZoneElement == null)
            Assert.fail("Element not displayed on page with id: " + rowId);
        return rowZoneElement;
    }

    public boolean bannerMachineSlideshowExists(WebElement rowElement) {
        boolean exists = false;
        WebElement slideSectionElement;
        if (rowElement.findElements(By.className("slideContainer")).size() > 0) {
            slideSectionElement = rowElement.findElement(By.className("slideContainer"));
            exists = (slideSectionElement.findElements(By.className((macys() ? "bm-main-wrapper" : "bannerImage"))).size() > 0) || (slideSectionElement.findElements(By.className((macys() ? "bm-main" : "bannerText"))).size() > 0) || (slideSectionElement.findElements(By.className("bannerLinks")).size() > 0);
        }
        return exists;
    }

    public boolean bannerMachineWidgetExists(WebElement rowElement) {
        boolean exists = false;
        if (rowElement.findElements(By.id((macys() ? "flexWidget_25" : "flexWidget"))).size() > 0) {
            WebElement widgetSectionElement = rowElement.findElement(By.id((macys() ? "flexWidget_25" : "flexWidget")));
            exists = (widgetSectionElement.findElements(By.className((macys() ? "bm-main-wrapper" : "bannerImage"))).size() > 0) || (widgetSectionElement.findElements(By.className((macys() ? "bm-main" : "bannerText"))).size() > 0) || (widgetSectionElement.findElements(By.className("bannerLinks")).size() > 0) || (widgetSectionElement.findElements(By.id("widgetContentId")).size() > 0);
        }
        return exists;
    }

    public boolean selfSelectProductPoolExists(WebElement rowElement) {
        boolean exists = false;
        exists = (rowElement.findElements(By.cssSelector("a.selfSelectLink")).size() > 0);
        return exists;
    }

    public boolean bannerMachineExists(WebElement rowElement) {
        boolean exists = false;
        exists = (rowElement.findElements(By.className((macys() ? "bm-main" : "bannerText"))).size() > 0) || (rowElement.findElements(By.className((macys() ? "bm-main-wrapper" : "bannerImage"))).size() > 0) || (rowElement.findElements(By.className((macys() ? "bm-urgency" : "bannerText"))).size() > 0) || ((rowElement.findElements(By.tagName("div")).size() > 0) ? (rowElement.findElement(By.tagName("div")).getAttribute("id").contains("bannerMachine")) : false);
        return exists;
    }

    public boolean slideShowExists(WebElement rowElement) {
        boolean exists = false;
        exists = (rowElement.findElements(By.className("slideContainer")).size() > 0);
        return exists;
    }

    public boolean thumbnailGridExists(WebElement rowElement) {
        boolean exists = false;
        exists = (rowElement.getAttribute("class").contains("rowThumbnail")) || ((rowElement.findElements(By.id("breadcrumbs"))).size() > 0);
        return exists;
    }

    public boolean widgetExists(WebElement rowElement) {
        boolean exists = false;
        exists = rowElement.findElements(By.id((macys() ? "flexWidget_25" : "flexWidget"))).size() > 0;
        return exists;
    }

    public boolean videoExists(WebElement rowElement) {
        boolean exists = false;
        for (WebElement element : rowElement.findElements(By.tagName("div"))) {
            if (element.getAttribute("id").contains("vid_"))
                exists = true;
        }
        if(!exists)
            exists = (rowElement.findElements(By.className("overlayVideo")).size() > 0);
        return exists;
    }

    public boolean customPopupExists(WebElement rowElement) {
        boolean exists = false;
        for (WebElement element : rowElement.findElements(By.tagName("a"))) {
            exists = element.getAttribute("id").contains("popUpLink_");
        }
        return exists;
    }

    public boolean splashReviewExists(WebElement rowElement) {
        boolean exists = false;
        exists = (rowElement.findElements(By.id("splashReviews")).size() > 0) || (rowElement.findElements(By.id("reviewsRow")).size() > 0);
        return exists;
    }

    public boolean productPoolExists(WebElement rowElement) {
        boolean exists = false;
        exists = (rowElement.findElements(By.className("carouselProductPool")).size() > 0) || (rowElement.findElements(By.className("productThumbnail")).size() > 0);
        return exists;
    }

    public boolean flexiblePoolExists(WebElement rowElement) {
        boolean exists = false;
        exists = rowElement.findElements(By.className("flexPool")).size() > 0;
        return exists;
    }

    public boolean horizontalRuleExists(WebElement rowElement) {
        boolean exists = false;
        exists = rowElement.findElements(By.className("row-hr")).size() > 0;
        return exists;
    }

    public boolean jspExists(WebElement rowElement) {
        boolean exists = false;
        if ((rowElement.findElements(By.tagName("ul")).size() > 0) && (rowElement.findElement(By.tagName("ul")).findElements(By.tagName("li")).size() > 0)) {
            WebElement listElement = rowElement.findElement(By.tagName("ul")).findElement(By.tagName("li"));
            exists = (rowElement.findElements(By.tagName("style")).size() > 0) || (rowElement.findElements(By.className("reg-home-main-wrapper")).size() > 0) || (rowElement.findElements(By.id("iw-vpreview")).size() > 0) || (!(listElement.isDisplayed() ? (!(listElement.findElements(By.tagName("link")).size() > 0)) : true) && (listElement.isDisplayed() ? listElement.findElement(By.tagName("link")).getAttribute("href").contains(".css") : true));
        }
        return exists;
    }

    public boolean imageMapExists(WebElement rowElement) {
        boolean exists = false;
        exists = rowElement.findElements(By.tagName("area")).size() > 0;
        return exists;
    }

    public boolean catIconsExists(WebElement rowElement) {
        boolean exists = false;
        exists = rowElement.findElements(By.className("adCatIcon")).size() > 0;
        return exists;
    }

    public boolean imageExists(WebElement rowElement) {
        boolean exists = false;
        for (WebElement element : rowElement.findElements(By.tagName("a"))) {
            if (element.getAttribute("href").contains("javascript:pop"))
                exists = true;
        }
        exists = (rowElement.findElements(By.tagName("img")).size() > 0);
        return exists;
    }

    public boolean simpleTextExists(WebElement rowElement) {
        boolean exists = false;
        if (rowElement.findElements(By.className("adFlexText")).size() > 0)
            exists = rowElement.findElement(By.className("adFlexText")).findElement(By.className("textComponent")).isDisplayed();
        return exists;
    }

    public boolean copyBlockExists(WebElement rowElement) {
        boolean exists = false;
        WebElement element = rowElement.findElement(By.xpath("..//..//..//..//.."));
        exists = (element.findElements(By.id("copyBlockContainer")).size() > 0) || (rowElement.findElements(By.id("copyBlockArea")).size() > 0) || (element.findElement(By.xpath("..")).findElements(By.id("copyBlockContainer")).size() > 0);
        return exists;
    }

    public boolean productPanelCategoryExists(WebElement rowElement) {
        boolean exists = false;
        String text = (rowElement.findElements(By.className("shopAllLink")).size() > 0) ? rowElement.findElement(By.className("shopAllLink")).getText() : "";
        exists = text.contains("Shop All");
        return exists;
    }

    public boolean productPanelPoolExists(WebElement rowElement) {
        boolean exists = false;
        exists = rowElement.findElements(By.className("shopAllLink")).size() > 0 ? rowElement.findElement(By.className("shopAllLink")).getText().contains("Shop All") : false;
        return exists;
    }

    public String getMediaTypeByRow(WebElement rowElement, boolean exceptEmptyRow) {
        String mediaType = null;
        if (bannerMachineSlideshowExists(rowElement)) {
            mediaType = "banner_machine_slideshow";
        } else if (bannerMachineWidgetExists(rowElement)) {
            mediaType = "banner_machine_widget";
        } else if (selfSelectProductPoolExists(rowElement)) {
            mediaType = "self_select_product_pool";
        } else if (bannerMachineExists(rowElement)) {
            mediaType = "banner_machine";
        } else if (slideShowExists(rowElement)) {
            mediaType = "slideshow";
        } else if (thumbnailGridExists(rowElement)) {
            mediaType = "thumbnail_grid";
        } else if (widgetExists(rowElement)) {
            mediaType = "widget";
        } else if (videoExists(rowElement)) {
            mediaType = "video";
        } else if (customPopupExists(rowElement)) {
            mediaType = "custom_popup";
        } else if (splashReviewExists(rowElement)) {
            mediaType = "recently_reviewed";
        } else if (productPoolExists(rowElement)) {
            mediaType = "product_pool";
        } else if (flexiblePoolExists(rowElement)) {
            mediaType = "flexible_pool";
        } else if (horizontalRuleExists(rowElement)) {
            mediaType = "horizontal_rule";
        } else if (jspExists(rowElement)) {
            mediaType = "jsp";
        } else if (imageMapExists(rowElement)) {
            mediaType = "image_map";
        } else if (catIconsExists(rowElement)) {
            mediaType = "category_icon";
        } else if (imageExists(rowElement)) {
            mediaType = "ad";
        } else if (simpleTextExists(rowElement)) {
            mediaType = "text";
        } else if (copyBlockExists(rowElement)) {
            mediaType = "copy_block";
        } else if (productPanelCategoryExists(rowElement)) {
            mediaType = "product_panel_category";
        } else if (productPanelPoolExists(rowElement)) {
            mediaType = "product_panel_pool";
        } else if (exceptEmptyRow) {
            System.out.println("ERROR - DATA: Invalid media eventType found in row " + rowElement.getAttribute("id") + " on page");
        } else {
            Assert.fail("ERROR - DATA: Invalid media eventType found in row " + rowElement.getAttribute("id") + " on page");
        }
        return mediaType;
    }
}
