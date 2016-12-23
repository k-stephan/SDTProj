package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import com.macys.sdt.framework.utils.rest.services.Canvas;
import com.macys.sdt.framework.utils.rest.services.Categories;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class MediaService {

    public static Statement statement;
    public static Connection connection;
    public static JSONObject queries;
    public static int thumbnailFlag = 0, widgetFlag = 0, bannerFlag = 0, mediaBannerFlag = 0;
    public static boolean bannerData = false;
    public static Map mediaGroupTypeData, mediaComponentTypeData;
    public static List<JSONObject> finalMediaData = new ArrayList<>();
    public static List<HashMap> allCanvasRowIds = new ArrayList<>();
    public static List<HashMap> allMediaGroupIds = new ArrayList<>();
    public static List<HashMap> allMediaComponentIds = new ArrayList<>();
    public static Date customDate;
    public static JSONObject contextData;


    public static List<Map> getFinalContextualizeCanvasData(String[] mediaNamesArray, String rowType, String[] contextAttrNames, String[] contextAttrValues) throws Throwable {
        setupConnection();
        if (contextData == null) {
            contextData = Utils.getContextualizeMedia();
        }
        if (((contextData.getJSONObject("GROUP").has(mediaNamesArray[0]) && contextData.getJSONObject("GROUP").getString(mediaNamesArray[0]).contains("row_contextualize_media")) || (contextData.getJSONObject("COMPONENT").has(mediaNamesArray[0]) && contextData.getJSONObject("COMPONENT").getString(mediaNamesArray[0]).contains("row_contextualize_media"))) && (rowType.contains("101") || rowType.contains("0"))) {
            return getRowContextualizeCanvasId(mediaNamesArray.clone(), rowType, contextAttrNames, contextAttrValues);
        } else {
            return getContextualizeData(mediaNamesArray.clone(), rowType, contextAttrNames, contextAttrValues);
        }
    }

    /*
    # To Get only row_contextualized media details based upon media_name, row_type ,context_attr_name,context_attr_value
    # @param[String[], String, String[], String[]] expected media names, row type and context attr name and value
    # @return[Array[Hashes]] return all media details for only row contextualize media ex => copy_block, text, recently_review, Horizontal_rule
    # EX [{:canvas_id=>"401130", :canvas_row_id=>5025325, :media_name => 'dhhfj.jpg', :text=>"Alt", :description=>"Ad"},{....}]
    #
    */
    public static List<Map> getRowContextualizeCanvasId(String[] mediaNamesArray, String rowType, String[] contextAttrNames, String[] contextAttrValues) throws Throwable {
        List canvasData = new ArrayList<>();
        try {
            setupConnection();
            if (queries == null) {
                queries = Utils.getSqlQueries();
            }
            if (contextData == null) {
                contextData = Utils.getContextualizeMedia();
            }
            if (customDate == null) {
                customDate = getCustomDate();
            }
            String mediaType = ((contextData.getJSONObject("GROUP").has(mediaNamesArray[0])) ? "GROUP" : "COMPONENT");
            List<String> canvasRowIds = getCanvasRowIds(queries.getJSONObject("media_service")
                    .get("canvas_layout_attribute_data").toString(), contextAttrNames, contextAttrValues, 4);
            PreparedStatement preparedStatement;
            ResultSet resultSet;
            if (mediaType.equals("GROUP")) {
                List<String[]> values = new ArrayList<>();
                values.add(canvasRowIds.toArray(new String[canvasRowIds.size()]));
                values.add(mediaNamesArray);
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("media_group_data").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                preparedStatement.setString(1, rowType);
                resultSet = preparedStatement.executeQuery();
            } else {
                List<String[]> values = new ArrayList<>();
                values.add(canvasRowIds.toArray(new String[canvasRowIds.size()]));
                values.add(mediaNamesArray);
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("media_component_data").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                preparedStatement.setString(1, rowType);
                resultSet = preparedStatement.executeQuery();
            }
            while (resultSet.next()) {
                Map row = new HashMap<>();
                row.put("canvasId", resultSet.getString("canvas_id"));
                row.put("canvasRowId", resultSet.getString("canvas_row_id"));
                row.put("text", resultSet.getString("text"));
                row.put("description", resultSet.getString("description"));
                row.put("mediaName", resultSet.getString("media_name"));
                row.put("canvasRowSeq", resultSet.getString("seq_nbr"));
                row.put("mediaTypeDesc", resultSet.getString("media_type_desc"));
                canvasData.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return canvasData;
    }

    /*
    #
    # To Get all contextualized, non contextualized media details based upon media_name, row_type ,context_attr_name,context_attr_value
    # @param[String[], String, String[], String[]] expected media names, row type and context attr name and value
    # @return[Array[Hashes]] return all non contextualized and contextualized media details
    # EX [{:canvas_id=>"401130", :canvas_row_id=>5025325, :media_key=>632522, :media_type_desc=>"AD", :media_info=>[{:media_key=>632522, :media_name=>"Row1.3).png", :text=>"Alt", :description=>"Ad"}]},{....}]
    #
    */
    public static List<Map> getContextualizeData(String[] mediaNamesArray, String rowType, String[] contextAttrNames, String[] contextAttrValues) throws Throwable {
        List<Map> finalMediaInfo = new ArrayList<>();
        List<String[]> context = new ArrayList<>();
        List<String> canvasRowIds = new ArrayList<>();
        context.add(contextAttrNames);
        context.add(contextAttrValues);
        int count = (Arrays.asList(contextAttrNames).contains("INHERITABLE_ROW_TYPE")) ? 5 : 4;
        try {
            setupConnection();
            if (queries == null) {
                queries = Utils.getSqlQueries();
            }
            if (customDate == null) {
                customDate = getCustomDate();
            }
            canvasRowIds = getCanvasRowIds(queries.getJSONObject("media_service").getString("canvas_layout_attribute_data"), contextAttrNames, contextAttrValues, count);
            List<String[]> values = new ArrayList<>();
            values.add(canvasRowIds.toArray(new String[canvasRowIds.size()]));
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_canvas_row_type_id").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
            preparedStatement.setString(1, rowType);
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map> rowLevelContextData = new ArrayList<>();
            while (resultSet.next()) {
                Map contextMapData = new HashMap<>();
                contextMapData.put("canvasId", resultSet.getString("canvas_id"));
                contextMapData.put("canvasRowId", resultSet.getString("canvas_row_id"));
                contextMapData.put("mediaKey", resultSet.getString("media_key"));
                contextMapData.put("mediaKeyType", resultSet.getString("media_key_type"));
                contextMapData.put("pageId", resultSet.getString("page_id"));
                contextMapData.put("canvasRowSeq", resultSet.getString("canvas_row_seq"));
                rowLevelContextData.add(contextMapData);
            }
            rowLevelContextData.removeIf(row -> row.get("mediaKey") == null);
            List<String> groupMediaKeys = rowLevelContextData.stream()
                    .filter(row -> (row.get("mediaKeyType").toString().equals("GROUP") && row.get("mediaKey") != null))
                    .map(row -> row.get("mediaKey").toString())
                    .collect(Collectors.toList());
            List<String> componentMediaKeys = rowLevelContextData.stream()
                    .filter(row -> (row.get("mediaKeyType").toString().equals("COMPONENT") && row.get("mediaKey") != null))
                    .map(row -> row.get("mediaKey").toString())
                    .collect(Collectors.toList());
            finalMediaInfo = rowLevelContextData;
            finalMediaInfo = getContextualizeMedia(mediaNamesArray, groupMediaKeys, componentMediaKeys, context, finalMediaInfo);
            //            String[] mediaNames = {"PRODUCT_PANEL_NA", "PRODUCT_PANEL_CATEGORY", "PRODUCT_PANEL_CATEGORY_FACET", "THUMBNAIL_GRID"};
            //            if (ListUtils.subtract(Arrays.asList(mediaNamesArray), Arrays.asList(mediaNames)).size() == 0)
            //                finalMediaInfo.removeIf(data -> data.get("mediaTypeDesc") == null || (data.keySet().contains("mediaInfo") && data.get("mediaInfo").toString().equals("[]")));
            if (thumbnailFlag != 1) {
                finalMediaInfo.removeIf(data -> !data.containsKey("mediaInfo"));
            }
            //            finalMediaInfo.removeIf(data -> data.containsKey("mediaInfo") && ((List)data.get("mediaInfo")).isEmpty());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return finalMediaInfo;
    }

    public static List<Map> getContextualizeMedia(String[] mediaNamesArray, List groupMediaKeys, List componentMediaKeys, List<String[]> context, List<Map> finalMediaInfo) throws Throwable {
        if (mediaGroupTypeData == null) {
            mediaGroupTypeData = getMediaGroupTypes();
        }
        if (mediaComponentTypeData == null) {
            mediaComponentTypeData = getMediaComponentTypes();
        }
        if (Arrays.asList(mediaNamesArray).contains("THUMBNAIL_GRID")) {
            thumbnailFlag = 1;
        }
        if (Arrays.asList(mediaNamesArray).contains("BANNER_MACHINE")) {
            bannerFlag = 1;
        }
        if ((Arrays.asList(mediaNamesArray).contains("BANNER_MACHINE_SLIDESHOW")) || (Arrays.asList(mediaNamesArray).contains("BANNER_MACHINE_WIDGET"))) {
            mediaBannerFlag = 1;
            for (int index = 0; index < mediaNamesArray.length; index++) {
                if (mediaNamesArray[index].equals("BANNER_MACHINE_SLIDESHOW")) {
                    mediaNamesArray[index] = "SLIDESHOW";
                }
                if (mediaNamesArray[index].equals("BANNER_MACHINE_WIDGET")) {
                    mediaNamesArray[index] = "WIDGET";
                }
            }
        }
        if (Arrays.asList(mediaNamesArray).contains("WIDGET")) {
            widgetFlag = 1;
        }
        if (Arrays.asList(mediaNamesArray).contains("MEDIA_ADS")) {
            for (int index = 0; index < mediaNamesArray.length; index++)
                if (mediaNamesArray[index].equals("MEDIA_ADS")) {
                    mediaNamesArray[index] = "THUMBNAIL_GRID";
                }
        }

        List<String> componentContextualizeMedia = new ArrayList<>();
        List componentNonContextualizeMedia = new ArrayList<>();
        List<String> groupContextualizeMedia = new ArrayList<>();
        List groupNonContextualizeMedia = new ArrayList<>();
        List<String> catIconMediaKeys = new ArrayList<>();
        //Get all group media key with expected media
        int imageMapFlag = 0, adFlag = 0, popupFlag = 0;
        if (!groupMediaKeys.isEmpty()) {
            if (Arrays.asList(mediaNamesArray).contains("AD")) {
                adFlag = 1;
            }
            //For IMAGE_MAP media type replace with AD
            if (Arrays.asList(mediaNamesArray).contains("IMAGE_MAP")) {
                for (int index = 0; index < mediaNamesArray.length; index++)
                    if (mediaNamesArray[index].equals("IMAGE_MAP")) {
                        mediaNamesArray[index] = "AD";
                    }
                imageMapFlag = 1;
            }
            if (Arrays.asList(mediaNamesArray).contains("CUSTOM_POPUP")) {
                for (int index = 0; index < mediaNamesArray.length; index++)
                    if (mediaNamesArray[index].equals("CUSTOM_POPUP")) {
                        mediaNamesArray[index] = "AD";
                    }
                popupFlag = 1;
            }
            if (mediaBannerFlag == 2 && bannerFlag != 1) {
                bannerFlag = 1;
            }
            List<List> groupData = getMediaTypeDescription(groupMediaKeys, "group", mediaNamesArray, finalMediaInfo);
            if (Arrays.asList(mediaNamesArray).contains("CATEGORY_ICON")) {
                catIconMediaKeys = (List) groupData.get(0).stream()
                        .filter(data -> (((Map) data).get("mediaTypeDesc").equals("CATEGORY_ICON")))
                        .map(data -> ((Map) data).get("mediaKey").toString())
                        .collect(Collectors.toList());
            }
            if (catIconMediaKeys.isEmpty()) {
                groupContextualizeMedia = (List) groupData.get(0).stream()
                        .map(data -> ((Map) data).get("mediaKey").toString())
                        .collect(Collectors.toList());
            } else {
                groupContextualizeMedia = (List) groupData.get(0).stream()
                        .filter(data -> (!((Map) data).get("mediaTypeDesc").equals("CATEGORY_ICON")))
                        .map(data -> ((Map) data).get("mediaKey").toString())
                        .collect(Collectors.toList());
            }
            groupNonContextualizeMedia = (List) groupData.get(groupData.size() - 1).stream()
                    .map(data -> ((Map) data).get("mediaKey").toString())
                    .collect(Collectors.toList());
        }
        //Get all component media key with expected media
        if (!componentMediaKeys.isEmpty()) {
            List<List> componentData = getMediaTypeDescription(componentMediaKeys, "component", mediaNamesArray, finalMediaInfo);
            componentContextualizeMedia = (List) componentData.get(0).stream()
                    .map(data -> ((Map) data).get("mediaKey").toString())
                    .collect(Collectors.toList());
            componentNonContextualizeMedia = (List) componentData.get(componentData.size() - 1).stream()
                    .map(data -> ((Map) data).get("mediaKey").toString())
                    .collect(Collectors.toList());
        }
        if (mediaNamesArray.length != 0) {
            finalMediaInfo.removeIf(type -> type == null);
            finalMediaInfo.removeIf(type -> !type.containsKey("mediaTypeDesc"));
        }
        if (thumbnailFlag == 1) {
            return finalMediaInfo;
        }
        //Get all group media data for direct media
        if (!(groupContextualizeMedia.isEmpty() && catIconMediaKeys.isEmpty())) {
            //get the data for Image map and AD media type
            List mediaGroupDataOne = new ArrayList<>();
            List mediaGroupDataTwo = new ArrayList<>();
            List mediaGroupDataSecond = new ArrayList<>();
            List<Map> mediaGroupData = new ArrayList<>();
            if (customDate == null) {
                customDate = getCustomDate();
            }
            List mediaGroupIds = getGroupIdsWithMultipleContext(context.get(0), context.get(context.size() - 1), "media_group_attribute_data");
            if (!catIconMediaKeys.isEmpty()) {
                List<String[]> values = new ArrayList<>();
                List<String> finalMediaKeys = ListUtils.sum(mediaGroupIds, catIconMediaKeys);
                values.add(finalMediaKeys.toArray(new String[finalMediaKeys.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_group_media_context_by_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();
                mediaGroupDataOne = getMediaGroupDataFromResult(resultSet, "group");
                values.clear();
                values.add(catIconMediaKeys.toArray(new String[catIconMediaKeys.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("without_group_media_context_by_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                resultSet = preparedStatement.executeQuery();
                mediaGroupDataTwo = getMediaGroupDataFromResult(resultSet, "group");
                if (!mediaGroupDataTwo.isEmpty()) {
                    mediaGroupDataOne.addAll(mediaGroupDataTwo);
                }
            }
            if (bannerFlag == 1) {
                List<String[]> values = new ArrayList<>();
                values.add(groupContextualizeMedia.toArray(new String[groupContextualizeMedia.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();
                mediaGroupData = getMediaGroupDataFromResult(resultSet, "group");
                values.clear();
                values.add(groupContextualizeMedia.toArray(new String[groupContextualizeMedia.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("without_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                resultSet = preparedStatement.executeQuery();
                mediaGroupDataTwo = getMediaGroupDataFromResult(resultSet, "group");
                if (!mediaGroupDataTwo.isEmpty()) {
                    mediaGroupData.addAll(mediaGroupDataTwo);
                }
            } else {
                List<String> finalMediaKeys = ListUtils.sum(mediaGroupIds, groupContextualizeMedia);
                mediaGroupData = getGroupMediaContextData(finalMediaKeys, "with_group_media_context", customDate.toString());
                mediaGroupDataSecond = getGroupMediaContextData(groupContextualizeMedia, "without_group_media_context", customDate.toString());
                if (!mediaGroupDataSecond.isEmpty()) {
                    mediaGroupData.addAll(mediaGroupDataSecond);
                }
                if (!mediaGroupDataOne.isEmpty()) {
                    mediaGroupData.addAll(mediaGroupDataOne);
                }
            }
            if (imageMapFlag == 1 || adFlag == 1 || popupFlag == 1) {
                List<String> adComponentIds = mediaGroupData.stream()
                        .filter(type -> type.get("mediaTypeDesc").equals("AD"))
                        .map(type -> type.get("componentId").toString())
                        .collect(Collectors.toList());

                List<String[]> values = new ArrayList<>();
                values.add(adComponentIds.toArray(new String[adComponentIds.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_component_id_in_media_parameter").toString(), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();

                List<Map> mediaParameterData = getMediaParameterDataFromResult(resultSet);
                List<String> refIds = mediaParameterData.stream()
                        .map(type -> type.get("refId").toString())
                        .collect(Collectors.toList());
                values.clear();
                values.add(refIds.toArray(new String[refIds.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_static_link_url").toString(), values, "string"));
                resultSet = preparedStatement.executeQuery();

                List<Map> popData = getMediaParameterDataFromResult(resultSet);
                List popupComponentIds = popData.stream()
                        .map(pop -> pop.get("componentId").toString())
                        .collect(Collectors.toList());
                mediaGroupData = mediaGroupData.stream().map(type -> {
                    if (mediaParameterData.stream().anyMatch(param -> type.get("componentId").toString().equals(param.get("componentId").toString()))) {
                        if (mediaParameterData.stream().anyMatch(param -> (param.get("linkType").toString().equals("4") || popupComponentIds.contains(param.get("componentId").toString())))) {
                            type.put("mediaTypeDesc", "CUSTOM_POPUP");
                        }
                        if (mediaParameterData.stream().anyMatch(param -> (!param.get("regionCoordinates").equals("") || param.get("regionCoordinates") != null))) {
                            type.put("mediaTypeDesc", "IMAGE_MAP");
                        }
                    }
                    return type;
                }).collect(Collectors.toList());
                List<Map> finalMediaGroupData = mediaGroupData;
                finalMediaInfo = finalMediaInfo.stream().map(type -> {
                    if (finalMediaGroupData.stream().anyMatch(mgType -> mgType.get("mediaKey").toString().equals(type.get("mediaKey").toString()) && mgType.get("mediaTypeDesc").equals("IMAGE_MAP"))) {
                        type.put("mediaTypeDesc", "IMAGE_MAP");
                    }
                    if (finalMediaGroupData.stream().anyMatch(mgType -> mgType.get("mediaKey").toString().equals(type.get("mediaKey").toString()) && mgType.get("mediaTypeDesc").equals("CUSTOM_POPUP"))) {
                        type.put("mediaTypeDesc", "CUSTOM_POPUP");
                    }
                    return type;
                }).collect(Collectors.toList());
                if (adFlag == 0) {
                    finalMediaInfo.removeIf(type -> type.get("mediaTypeDesc").equals("AD"));
                }
                if (imageMapFlag == 0) {
                    finalMediaInfo.removeIf(type -> type.get("mediaTypeDesc").equals("IMAGE_MAP"));
                }
                if (popupFlag == 0) {
                    finalMediaInfo.removeIf(type -> type.get("mediaTypeDesc").equals("CUSTOM_POPUP"));
                }
            }
            finalMediaInfo = updateMediaInformation(mediaNamesArray, mediaGroupData, finalMediaInfo);
        }
        if (!componentContextualizeMedia.isEmpty()) {
            List componentMediaLevelContextData;
            if (bannerFlag == 1) {
                List<String[]> values = new ArrayList<>();
                values.add(componentContextualizeMedia.toArray(new String[componentContextualizeMedia.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();
                componentMediaLevelContextData = getMediaGroupDataFromResult(resultSet, "group");
                values.clear();
                values.add(componentContextualizeMedia.toArray(new String[componentContextualizeMedia.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("without_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                resultSet = preparedStatement.executeQuery();
                List mediaComponentDataTwo = getMediaGroupDataFromResult(resultSet, "group");
                if (!mediaComponentDataTwo.isEmpty()) {
                    componentMediaLevelContextData.addAll(mediaComponentDataTwo);
                }
            } else {
                List mediaComponentIds = getGroupIdsWithMultipleContext(context.get(0), context.get(context.size() - 1), "media_component_attribute_data");
                List<String> finalMediaKeys = ListUtils.sum(mediaComponentIds, componentContextualizeMedia);
                componentMediaLevelContextData = getGroupMediaContextData(finalMediaKeys, "with_component_media_context", customDate.toString());
                List mediaComponentDataSecond = getGroupMediaContextData(finalMediaKeys, "without_component_media_context", customDate.toString());
                if (!mediaComponentDataSecond.isEmpty()) {
                    componentMediaLevelContextData.addAll(mediaComponentDataSecond);
                }
            }
            finalMediaInfo = updateMediaInformation(mediaNamesArray, componentMediaLevelContextData, finalMediaInfo);
        }
        if (!groupNonContextualizeMedia.isEmpty()) {
            finalMediaInfo = getIndirectContextualizeData(groupNonContextualizeMedia, context.get(0), context.get(context.size() - 1), finalMediaInfo, "GROUP");
        }
        if (!componentNonContextualizeMedia.isEmpty()) {
            finalMediaInfo = getIndirectContextualizeData(componentNonContextualizeMedia, context.get(0), context.get(context.size() - 1), finalMediaInfo, "COMPONENT");
        }
        return finalMediaInfo;
    }

    public static List<Map> getGroupMediaContextData(List<String> mediaKeys, String queryName, String customDate) throws Throwable {
        List<String[]> values = new ArrayList<>();
        values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
        String sqlQuery = updatedQuery((queries.getJSONObject("media_service").getString(queryName).replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string");
        if (queryName.equals("without_group_media_context")) {
            sqlQuery = sqlQuery.replace("media_parameter.text, ", "");
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        ResultSet resultSet = preparedStatement.executeQuery();
        return getMediaGroupDataFromResult(resultSet, (queryName.contains("group") ? "group" : "component"));
    }

    public static List<Map> getIndirectContextualizeData(List<String> mediaKeys, String[] attrNames, String[] attrValues, List<Map> finalMediaInfo, String mediaType) throws Throwable {
        List<String[]> context = new ArrayList<>();
        context.add(attrNames);
        context.add(attrValues);
        if (customDate == null) {
            customDate = getCustomDate();
        }
        List mediaGroupIds = getGroupIdsWithMultipleContext(attrNames, attrValues, "media_group_attribute_data");
        List<Map> mediaGroupComponentData = new ArrayList<>();
        if (mediaBannerFlag == 1) {
            mediaBannerFlag = 2;
        }
        if (mediaType.equals("GROUP")) {
            if (bannerFlag == 1) {
                List<String> finalMediaKeys = ListUtils.sum(mediaGroupIds, mediaKeys);
                List<Map> mediaGroupData = getGroupMediaContextData(finalMediaKeys, "with_group_media_context", customDate.toString());
                List<Map> mediaGroupDataSecond = getGroupMediaContextData(mediaKeys, "without_group_media_context", customDate.toString());
                if (!mediaGroupDataSecond.isEmpty()) {
                    mediaGroupData.addAll(mediaGroupDataSecond);
                }
                for (Map media : mediaGroupData) {
                    Map type = new HashMap<>();
                    type.put("mediaGroupId", media.get("mediaKey"));
                    type.put("componentId", media.get("componentId"));
                    mediaGroupComponentData.add(type);
                }
            } else {
                List<String[]> values = new ArrayList<>();
                values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_media_group_component").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    Map type = new HashMap<>();
                    type.put("mediaGroupId", resultSet.getString("media_group_id"));
                    type.put("componentId", resultSet.getString("component_id"));
                    mediaGroupComponentData.add(type);
                }
            }
            mediaKeys = mediaGroupComponentData.stream()
                    .map(mgc -> mgc.get("componentId").toString())
                    .collect(Collectors.toList());
        } else {
            List componentIds = getGroupIdsWithMultipleContext(attrNames, attrValues, "media_component_attribute_data");
            List<String> finalMediaKeys = ListUtils.sum(mediaKeys, componentIds);
            List<String[]> values = new ArrayList<>();
            values.add(finalMediaKeys.toArray(new String[finalMediaKeys.size()]));
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_media_component_id").toString(), values, "string"));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<String> mediaComponentData = new ArrayList<>();
            while (resultSet.next())
                mediaComponentData.add(resultSet.getString("component_id"));
            values.clear();
            values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("without_component_media_context").toString(), values, "string"));
            resultSet = preparedStatement.executeQuery();
            List<String> mediaComponentDataSecond = new ArrayList<>();
            while (resultSet.next())
                mediaComponentDataSecond.add(resultSet.getString("component_id"));
            if (!mediaComponentDataSecond.isEmpty()) {
                mediaComponentData.addAll(mediaComponentDataSecond);
            }
            mediaKeys = mediaComponentData;
        }
        List<Map> mediaParameterData = new ArrayList<>();
        List<String[]> values = new ArrayList<>();
        values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
        PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_link_type").toString(), values, "string"));
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map type = new HashMap<>();
            type.put("mediaKey", resultSet.getString("component_id"));
            type.put("refId", resultSet.getString("ref_id"));
            type.put("seqNumber", resultSet.getString("seq_nbr"));
            type.put("mediaTypeDesc", resultSet.getString("link_type_desc"));
            type.put("parameterId", resultSet.getString("parameter_id"));
            type.put("linkType", resultSet.getString("link_type"));
            type.put("parameterText", resultSet.getString("text"));
            mediaParameterData.add(type);
        }
        if (widgetFlag == 1) {
            String mode = (Arrays.asList(attrValues).contains("WEDDING_REGISTRY")) ? "REGISTRY" : "SITE";
            List<String> paramIds = mediaParameterData.stream()
                    .map(param -> param.get("parameterId").toString())
                    .collect(Collectors.toList());
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
            values.clear();
            values.add(paramIds.toArray(new String[paramIds.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_widget_data").replaceAll("<= \\?", "<= '" + dateFormat.format(customDate) + "'").replaceAll(">= \\?", ">= '" + dateFormat.format(customDate) + "'")), values, "int").replaceAll("\\?", mode));
            resultSet = preparedStatement.executeQuery();
            List mediaParameterAttrData = new ArrayList<>();
            while (resultSet.next())
                mediaParameterAttrData.add(resultSet.getString("parameter_id"));
            final List finalMediaParameterAttrData = mediaParameterAttrData;
            if (!mediaParameterAttrData.isEmpty()) {
                mediaParameterData.removeIf(param -> !finalMediaParameterAttrData.contains(param.get("parameterId").toString()));
            }
        }
        if (mediaType.equals("GROUP")) {
            mediaParameterData = mediaParameterData.stream().map(param -> {
                if (mediaGroupComponentData.stream().anyMatch(gc -> gc.get("componentId").toString().equals(param.get("mediaKey")))) {
                    param.put("mediaKey", mediaGroupComponentData.stream().filter(mg -> mg.get("componentId").toString().equals(param.get("mediaKey"))).map(mg -> mg.get("mediaGroupId")).collect(Collectors.toList()).get(0));
                }
                return param;
            }).collect(Collectors.toList());
        }
        List<Map> pdata = mediaParameterData;
        if (bannerFlag == 1) {
            String[] linkTypes = {"1", "2", "3"};
            pdata.removeIf(param -> Arrays.asList(linkTypes).contains(param.get("linkType").toString()));
            List<String> paramIds = pdata.stream()
                    .map(param -> param.get("parameterId").toString())
                    .collect(Collectors.toList());
            List<Map> mediaParamAttrData = new ArrayList<>();
            values.clear();
            values.add(paramIds.toArray(new String[paramIds.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_media_param_attr_name").toString(), values, "int"));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map type = new HashMap<>();
                type.put("attrValue", resultSet.getString("attr_value"));
                type.put("parameterId", resultSet.getString("parameter_id"));
                mediaParamAttrData.add(type);
            }
            pdata = pdata.stream().map(param -> {
                param.put("deviceType", mediaParamAttrData.stream()
                        .filter(attr -> attr.get("parameterId").toString().equals(param.get("parameterId").toString()))
                        .map(attr -> attr.get("attrValue")).collect(Collectors.toList()));
                return param;
            }).collect(Collectors.toList());
        }
        if (finalMediaInfo.stream().anyMatch(info -> info.containsKey("refIds"))) {
            finalMediaInfo.removeIf(info -> ((List) info.get("refIds")).isEmpty());
            List<Map> finalMediaParameterData = mediaParameterData;
            finalMediaInfo = finalMediaInfo.stream().map(info -> {
                List finalRefIds = new ArrayList<>();
                ((List<String>) info.get("refIds")).stream().map(ref -> {
                    if (finalMediaParameterData.stream().anyMatch(param -> param.get("mediaKey").toString().equals(ref))) {
                        finalRefIds.addAll(finalMediaParameterData.stream()
                                .filter(param -> param.get("mediaKey").toString().equals(ref))
                                .map(param -> param.get("refId").toString())
                                .collect(Collectors.toList()));
                    } else {
                        finalRefIds.add(ref);
                    }
                    return finalRefIds;
                });
                info.put("refIds", finalRefIds);
                return info;
            }).collect(Collectors.toList());
            List<Map> paramData = new ArrayList<>();
            if (bannerFlag == 1) {
                paramData = mediaParameterData.stream().map(param -> {
                    Map type = new HashMap<>();
                    type.put("mediaKey", param.get("refId"));
                    type.put("seqNumber", param.get("seqNumber"));
                    type.put("mediaTypeDesc", param.get("mediaTypeDesc"));
                    type.put("parameterText", param.get("parameterText"));
                    type.put("deviceType", param.get("deviceType"));
                    return type;
                }).collect(Collectors.toList());
                String[] empty = {};
                updateMediaInformation(empty, paramData, finalMediaInfo);
            }
        } else {
            List<Map> finalMediaParameterData = mediaParameterData;
            finalMediaInfo = finalMediaInfo.stream().map(info -> {
                info.put("refIds", finalMediaParameterData.stream()
                        .filter(param -> (param.get("mediaKey").toString().equals(info.get("mediaKey").toString())))
                        .map(param -> param.get("refId").toString()).distinct()
                        .collect(Collectors.toList()));
                return info;
            }).collect(Collectors.toList());
            if (bannerFlag == 1) {
                List<String> linkTypeRefIds = mediaParameterData.stream()
                        .filter(param -> (param.get("linkType").toString().equals("3")))
                        .map(param -> param.get("refId").toString())
                        .collect(Collectors.toList());
                values.clear();
                values.add(linkTypeRefIds.toArray(new String[linkTypeRefIds.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_static_link_url_ref_ids").toString(), values, "string"));
                resultSet = preparedStatement.executeQuery();
                List<Map> staticData = new ArrayList<>();
                while (resultSet.next()) {
                    Map type = new HashMap<>();
                    type.put("staticLinkId", resultSet.getString("static_link_id"));
                    type.put("urlText", resultSet.getString("url_text"));
                    staticData.add(type);
                }
                List<Map> finalPdata = pdata;
                finalMediaInfo = finalMediaInfo.stream().map(info -> {
                    Map type = new HashMap<>();
                    finalPdata.stream().filter(param -> param.get("mediaKey").toString().equals(info.get("mediaKey").toString())).map(param -> {
                        type.put("refId", param.get("refId"));
                        type.put("linkType", param.get("linkType"));
                        type.put("text", param.get("parameterText"));
                        type.put("deviceType", param.get("deviceType"));
                        if (staticData.stream().anyMatch(url -> (url.get("staticLinkId").toString().equals(param.get("refId").toString())))) {
                            type.put("url", staticData.stream()
                                    .filter(url -> (url.get("staticLinkId").toString().equals(param.get("refId").toString())))
                                    .map(url -> url.get("urlText")).collect(Collectors.toList()).get(0));
                        } else {
                            type.put("url", null);
                        }
                        return type;
                    });
                    info.put("bannerMachine", type);
                    return info;
                }).collect(Collectors.toList());
            }
        }
        List groupRefIds = mediaParameterData.stream()
                .filter(param -> (param.get("mediaTypeDesc").toString().contains("MEDIA")))
                .map(param -> param.get("refId").toString())
                .collect(Collectors.toList());
        List componentRefIds = mediaParameterData.stream()
                .filter(param -> (param.get("mediaTypeDesc").toString().contains("MEDIACOMPONENT")))
                .map(param -> param.get("refId").toString())
                .collect(Collectors.toList());
        String[] empty = {};
        finalMediaInfo = getContextualizeMedia(empty, groupRefIds, componentRefIds, context, finalMediaInfo);
        return finalMediaInfo;
    }

    public static List<Map> updateMediaInformation(String[] mediaNamesArray, List<Map> mediaGroupData, List<Map> finalMediaInfo) throws Throwable {
        finalMediaInfo.removeIf(type -> (type.keySet().contains("refIds") && type.get("refIds") == null));
        finalMediaInfo.forEach(type -> {
            final List<Map> refMediaInfo = new ArrayList<>();
            if (mediaNamesArray.length == 0) {
                ((ArrayList) type.get("refIds")).forEach(ref -> {
                    if (mediaGroupData.stream().anyMatch(media -> media.get("mediaKey").toString().equals(ref.toString()))) {
                        refMediaInfo.add(mediaGroupData.stream().filter(media -> media.get("mediaKey").toString().equals(ref)).findFirst().get());
                    }
                });
            } else {
                if (mediaGroupData.stream().anyMatch(media -> media.get("mediaKey").toString().equals(type.get("mediaKey").toString()))) {
                    refMediaInfo.add(mediaGroupData.stream().filter(media -> media.get("mediaKey").toString().equals(type.get("mediaKey").toString())).findFirst().get());
                }
            }
            if (!refMediaInfo.isEmpty()) {
                if (!type.containsKey("mediaInfo") || type.get("mediaInfo") == null) {
                    type.put("mediaInfo", refMediaInfo);
                } else {
                    ((List) type.get("mediaInfo")).addAll(refMediaInfo);
                }
            }
        });
        return finalMediaInfo;
    }

    public static List<Map> getMediaParameterDataFromResult(ResultSet resultSet) throws Throwable {
        List<Map> mediaParameterData = new ArrayList<>();
        while (resultSet.next()) {
            Map type = new HashMap<>();
            type.put("parameterId", resultSet.getString("parameter_id"));
            type.put("componentId", resultSet.getString("component_id"));
            type.put("refId", resultSet.getString("ref_id"));
            type.put("regionCoordinates", resultSet.getString("region_coordinates"));
            type.put("linkType", resultSet.getString("link_type"));
            type.put("seqNumber", resultSet.getString("seq_nbr"));
            type.put("text", resultSet.getString("text"));
            mediaParameterData.add(type);
        }
        return mediaParameterData;
    }

    public static List<Map> getMediaGroupDataFromResult(ResultSet resultSet, String mediaType) throws Throwable {
        List<Map> finalGroupData = new ArrayList<>();
        boolean isTextExists = false;
        ResultSetMetaData rsmd = resultSet.getMetaData();
        for (int index = 1; index <= rsmd.getColumnCount(); index++)
            if (rsmd.getColumnName(index).equalsIgnoreCase("text")) {
                isTextExists = true;
            }
        while (resultSet.next()) {
            Map type = new HashMap<>();
            type.put("mediaKey", resultSet.getString((mediaType.equals("group") ? "media_group_id" : "component_id")));
            type.put("componentId", resultSet.getString("component_id"));
            type.put("mediaName", resultSet.getString("media_name"));
            if (isTextExists) {
                type.put("text", resultSet.getString("text"));
            }
            type.put("description", resultSet.getString("description"));
            type.put("mediaTypeDesc", mediaGroupTypeData.get(resultSet.getString((mediaType.equals("group") ? "media_group_type" : "media_type"))));
            finalGroupData.add(type);
        }
        return finalGroupData;
    }

    public static List<List> getMediaTypeDescription(List<String> mediaKeys, String mediaType, String[] mediaNamesArray, List<Map> finalMediaInfo) throws Throwable {
        List<Map> mediaTypes = new ArrayList<>();
        setupConnection();
        if (queries == null) {
            queries = Utils.getSqlQueries();
        }
        if (customDate == null) {
            customDate = getCustomDate();
        }
        String mediaKeyName = (mediaType.equals("group") ? "media_group_id" : "component_id");
        String mediaTypeName = (mediaType.equals("group") ? "media_group_type" : "media_type");
        Map mediaTypeDesc = (mediaType.equals("group") ? mediaGroupTypeData : mediaComponentTypeData);
        ResultSet resultSet;
        List<String[]> values = new ArrayList<>();
        values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
        String query = (mediaType.equals("group") ? (updatedQuery((queries.getJSONObject("media_service").getString("with_media_group_ids").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string")) : (updatedQuery(queries.getJSONObject("media_service").get("with_media_component_id").toString(), values, "string")));
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map type = new HashMap<>();
            type.put("mediaKey", resultSet.getString(mediaKeyName));
            type.put("mediaTypeDesc", mediaTypeDesc.get(resultSet.getString(mediaTypeName)));
            mediaTypes.add(type);
        }
        if (mediaType.equals("component") && widgetFlag == 1 && mediaBannerFlag == 2) {
            mediaBannerFlag = 1;
        }
        boolean isBannerMachine = mediaTypes.stream().anyMatch(type -> (type.get("mediaTypeDesc").toString().equals("BANNER_MACHINE")));
        boolean conditionOne = (bannerFlag == 1 && mediaBannerFlag != 2 && !isBannerMachine && mediaType.equals("group"));
        boolean conditionTwo = (mediaBannerFlag == 2 && bannerFlag == 1 && !isBannerMachine && mediaType.equals("group"));
        finalMediaInfo.removeIf(info -> (info.containsKey("mediaTypeDesc") && (info.get("mediaTypeDesc") == null || info.get("mediaTypeDesc").toString().equals("null"))));
        bannerData = finalMediaInfo.stream().anyMatch(info -> (info.containsKey("mediaTypeDesc") && info.get("mediaTypeDesc").equals("BANNER_MACHINE")));
        if (bannerFlag == 1 && (isBannerMachine && mediaBannerFlag != 1 && mediaBannerFlag != 0)) {
            List<String> mediaIds = new ArrayList<>();
            mediaIds.addAll(mediaTypes.stream()
                    .filter(type -> (type.get("mediaTypeDesc").toString().equals("BANNER_MACHINE")))
                    .map(type -> type.get("mediaKey").toString())
                    .collect(Collectors.toList()));
            List<String> mediaRowIds = new ArrayList<>();
            if (mediaBannerFlag != 2 && bannerFlag == 1 && widgetFlag != 1) {
                mediaRowIds.addAll(finalMediaInfo.stream()
                        .filter(info -> (mediaIds.contains(info.get("mediaKey").toString())))
                        .map(info -> info.get("canvasRowId").toString())
                        .collect(Collectors.toList()));
            }
            if (mediaBannerFlag == 2 && bannerFlag == 1) {
                finalMediaInfo.stream().map(info -> {
                    if (mediaIds.stream().anyMatch(id -> (((List) info.get("refIds")).contains(id)))) {
                        mediaRowIds.add(info.get("canvasRowId").toString());
                    }
                    return info;
                });
            }
            if (!mediaRowIds.isEmpty()) {
                finalMediaInfo.removeIf(info -> (!mediaRowIds.contains(info.get("canvasRowId").toString())));
            }
            if (mediaBannerFlag == 2 && bannerFlag == 1) {
                finalMediaInfo = finalMediaInfo.stream().map(info -> {
                    info.put("refIds", ((List<String>) info.get("refId")).stream().filter(ref -> mediaIds.contains(ref)).collect(Collectors.toList()));
                    return info;
                }).collect(Collectors.toList());
            }
            if (!mediaRowIds.isEmpty()) {
                bannerData = true;
            }
        } else if ((conditionOne || conditionTwo) && !bannerData && !isBannerMachine) {
            Assert.fail("ERROR - DATA: Banner Machine media data not found");
        } else {
            if (mediaNamesArray.length != 0 && (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") || Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR"))) {
                String attributeValue = (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") ? "NA" : (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR") ? "CUSTRATING" : null));
                List<String> mediaIds = mediaTypes.stream().map(type -> type.get("mediaKey").toString()).collect(Collectors.toList());
                values.clear();
                values.add(mediaIds.toArray(new String[mediaIds.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_header_link_sort_criteria").toString(), values, "string"));
                preparedStatement.setString(1, attributeValue);
                resultSet = preparedStatement.executeQuery();
                List<String> finalMediaKeys = new ArrayList<>();
                while (resultSet.next())
                    finalMediaKeys.add(resultSet.getString("component_id"));
                mediaTypes.removeIf(type -> (!type.get("mediaTypeDesc").equals("PRODUCT_PANEL_CATEGORY")) ? (!(Arrays.asList(mediaNamesArray).contains(type.get("mediaTypeDesc").toString()))) : false);
                mediaTypes.removeIf(type -> (type.get("mediaTypeDesc").equals("PRODUCT_PANEL_CATEGORY")) ? (!(finalMediaKeys.contains(type.get("mediaKey").toString()))) : false);
            } else if (bannerFlag == 1 && mediaBannerFlag == 0) {
                List<Map> mediaTypeData = new ArrayList<>();
                List<String> bannerCanvasRowIds = finalMediaInfo.stream()
                        .filter(info -> (info.get("mediaTypeDesc").equals("BANNER_MACHINE")))
                        .map(info -> info.get("canvasRowId").toString())
                        .collect(Collectors.toList());
                List<String> mediaCanvasRowIds = mediaTypes.stream()
                        .filter(info -> (info.get("mediaTypeDesc").equals("BANNER_MACHINE")))
                        .map(info -> info.get("canvasRowId").toString())
                        .collect(Collectors.toList());
                List<Map> finalFinalMediaInfo = finalMediaInfo;
                mediaTypeData = mediaTypes.stream().map(type -> {
                    if (finalFinalMediaInfo.stream().anyMatch(info -> (type.get("mediaKey").toString().equals(info.get("mediaKey").toString())))) {
                        type.put("canvasRowId", finalFinalMediaInfo.stream()
                                .filter(info -> (type.get("mediaKey").toString().equals(info.get("mediaKey").toString())))
                                .map(info -> info.get("canvasRowId").toString())
                                .collect(Collectors.toList()).get(0));
                    }
                    return type;
                }).collect(Collectors.toList());
                final List<String> finalBannerCanvasRowIds = bannerCanvasRowIds;
                final List<String> finalMediaCanvasRowIds = mediaCanvasRowIds;
                if (mediaNamesArray.length != 0) {
                    mediaTypeData.removeIf(type -> ((!(finalBannerCanvasRowIds.contains(type.get("canvasRowId").toString())) && !(finalMediaCanvasRowIds.contains(type.get("canvasRowId").toString()))) || !(Arrays.asList(mediaNamesArray).contains(type.get("mediaTypeDesc").toString()))));
                }
            } else {
                if (mediaNamesArray.length != 0) {
                    mediaTypes.removeIf(type -> (!Arrays.asList(mediaNamesArray).contains(type.get("mediaTypeDesc").toString())));
                }
            }
        }
        if (!(mediaNamesArray.length == 0 && mediaTypes.isEmpty())) {
            finalMediaInfo.removeIf(info -> info == null);
            finalMediaInfo = finalMediaInfo.stream().map(info -> {
                if (mediaTypes.stream().anyMatch(type -> (type.get("mediaKey").toString().equals(info.get("mediaKey").toString())))) {
                    info.put("mediaTypeDesc", mediaTypes.stream().filter(type -> (type.get("mediaKey").toString().equals(info.get("mediaKey").toString()))).findFirst().get().get("mediaTypeDesc").toString());
                }
                return info;
            }).collect(Collectors.toList());
        }
        String[] medias = {"row_contextualize_media", "contextualize_media"};
        List<Map> contextualizeMedia = new ArrayList<>();
        List<Map> nonContextualizeMedia = new ArrayList<>();
        if (contextData == null) {
            contextData = Utils.getContextualizeMedia();
        }
        for (Map type : mediaTypes) {
            if (contextData.getJSONObject(mediaType.toUpperCase()).has(type.get("mediaTypeDesc").toString().toUpperCase()) && Arrays.asList(medias).contains(contextData.getJSONObject(mediaType.toUpperCase()).getString(type.get("mediaTypeDesc").toString().toUpperCase()))) {
                contextualizeMedia.add(type);
            }
            if (contextData.getJSONObject(mediaType.toUpperCase()).has(type.get("mediaTypeDesc").toString().toUpperCase()) && contextData.getJSONObject(mediaType.toUpperCase()).getString(type.get("mediaTypeDesc").toString().toUpperCase()).equals("non_contextualize_media")) {
                nonContextualizeMedia.add(type);
            }
        }
        List finalMediaDataSet = new ArrayList<>();
        finalMediaDataSet.add(contextualizeMedia);
        finalMediaDataSet.add(nonContextualizeMedia);
        return finalMediaDataSet;
    }

    public static Map getMediaGroupTypes() throws Throwable {
        Map mediaGroupTypeData = new HashMap<>();
        setupConnection();
        if (queries == null) {
            queries = Utils.getSqlQueries();
        }
        ResultSet resultSet = statement.executeQuery(queries.getJSONObject("media_service").get("select_all_media_group_type").toString());
        while (resultSet.next())
            mediaGroupTypeData.put(resultSet.getString("media_group_type"), resultSet.getString("media_group_type_desc"));
        return mediaGroupTypeData;
    }

    public static Map getMediaComponentTypes() throws Throwable {
        Map mediaComponentTypeData = new HashMap<>();
        setupConnection();
        if (queries == null) {
            queries = Utils.getSqlQueries();
        }
        ResultSet resultSet = statement.executeQuery(queries.getJSONObject("media_service").get("select_all_media_component_type").toString());
        while (resultSet.next())
            mediaComponentTypeData.put(resultSet.getString("media_type"), resultSet.getString("media_type_desc"));
        return mediaComponentTypeData;
    }

    public static List getCanvasRowIds(String query, String[] contextAttrNames, String[] contextAttrValues, int count) throws Throwable {
        List<String> canvasRowIds = new ArrayList<>();
        if (!allCanvasRowIds.isEmpty()) {
            if (allCanvasRowIds.stream().anyMatch(canvas -> ((Arrays.asList(canvas.get("contextAttrNames")).containsAll(Arrays.asList(contextAttrNames))) && (Arrays.asList(canvas.get("contextAttrValues")).containsAll(Arrays.asList(contextAttrValues))) && (Integer.parseInt(canvas.get("count").toString()) == count)))) {
                canvasRowIds = (List) allCanvasRowIds.stream().filter(canvas -> ((Arrays.asList(canvas.get("contextAttrNames")).containsAll(Arrays.asList(contextAttrNames))) && (Arrays.asList(canvas.get("contextAttrValues")).containsAll(Arrays.asList(contextAttrValues))) && (Integer.parseInt(canvas.get("count").toString()) == count)))
                        .findFirst().get().get("canvasRowIds");
            }
            if (!canvasRowIds.isEmpty()) {
                return canvasRowIds;
            }
        }
        setupConnection();
        List<String[]> values = new ArrayList<>();
        values.add(contextAttrNames);
        values.add(contextAttrValues);
        PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(query, values, "string"));
        preparedStatement.setInt(1, count);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
            canvasRowIds.add(resultSet.getString("canvas_row_id"));
        HashMap canvas = new HashMap<>();
        canvas.put("contextAttrNames", contextAttrNames);
        canvas.put("contextAttrValues", contextAttrValues);
        canvas.put("count", count);
        canvas.put("canvasRowIds", canvasRowIds);
        allCanvasRowIds.add(canvas);
        return canvasRowIds;
    }

    public static String updatedQuery(String query, List values, String type) throws Throwable {
        for (int index = 0; index < values.size(); index++)
            if (query.contains("IN (?)")) {
                query = query.replaceFirst("IN \\(\\?\\)", "IN (" + getStringFromArray((String[]) values.get(index), type) + ")");
            }
        return query;
    }

    public static List getGroupIdsWithMultipleContext(String[] contextAttrNames, String[] contextAttrValues, String queryName) throws Throwable {
        if (!((queryName.equals("media_group_attribute_data") ? allMediaGroupIds : allMediaComponentIds).isEmpty())) {
            return (queryName.equals("media_group_attribute_data") ? allMediaGroupIds : allMediaComponentIds).stream()
                    .filter(group -> ((Arrays.asList(group.get("contextAttrNames")).containsAll(Arrays.asList(contextAttrNames))) && (Arrays.asList(group.get("contextAttrValues")).containsAll(Arrays.asList(contextAttrValues))) && (group.get("queryName").equals(queryName))))
                    .map(group -> group.get("mediaGroupIds"))
                    .collect(Collectors.toList());
        }
        List<String[]> values = new ArrayList<>();
        values.add(contextAttrNames);
        values.add(contextAttrValues);
        PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get(queryName).toString(), values, "string"));
        ResultSet resultSet = preparedStatement.executeQuery();
        List<String> mediaGroupIds = new ArrayList<>();
        while (resultSet.next())
            mediaGroupIds.add(resultSet.getString(queryName.equals("media_group_attribute_data") ? "media_group_id" : "component_id"));
        HashMap group = new HashMap<>();
        group.put("contextAttrNames", contextAttrNames);
        group.put("contextAttrValues", contextAttrValues);
        group.put("queryName", queryName);
        group.put("mediaGroupIds", mediaGroupIds);
        (queryName.equals("media_group_attribute_data") ? allMediaGroupIds : allMediaComponentIds).add(group);
        return mediaGroupIds;
    }

    public static List getCategoryId(List<String> canvasIds, String pageType, List<Map<String, String>> context, String site, String[] tempMediaNames) throws Throwable {
        String mode, regionCode;
        List categoryCanvasData = new ArrayList<>();
        mode = context.stream().filter(con -> con.containsKey("SHOPPING_MODE")).map(con -> con.get("SHOPPING_MODE")).findFirst().get();
        regionCode = context.stream().filter(con -> con.containsKey("SHOPPING_MODE")).map(con -> con.get("REGION_CODE")).findFirst().get();
        try {
            setupConnection();
            if (queries == null) {
                queries = Utils.getSqlQueries();
            }
            List<String[]> values = new ArrayList<>();
            values.add(canvasIds.toArray(new String[canvasIds.size()]));
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_canvas_id_false").toString(), values, "string"));
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Map> data = getCanvasCategoryData(resultSet);
            values.clear();
            values.add(canvasIds.toArray(new String[canvasIds.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_canvas_id_true").toString(), values, "string"));
            resultSet = preparedStatement.executeQuery();
            List<Map> data1 = getCanvasCategoryData(resultSet);
            data.addAll(data1);
            List<String> categoryIds = data.stream().map(cat -> cat.get("categoryId").toString()).collect(Collectors.toList());
            values.clear();
            values.add(categoryIds.toArray(new String[categoryIds.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_category_id_false").toString(), values, "string"));
            resultSet = preparedStatement.executeQuery();
            List<Map> canvasCatIdData = getCanvasCategoryData(resultSet);
            values.clear();
            values.add(categoryIds.toArray(new String[categoryIds.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_category_id_true").toString(), values, "string"));
            resultSet = preparedStatement.executeQuery();
            List<Map> canvasCatIdData1 = getCanvasCategoryData(resultSet);
            canvasCatIdData.addAll(canvasCatIdData1);
            categoryIds.clear();
            categoryIds = canvasCatIdData.stream().map(a -> a.get("categoryId").toString()).distinct().collect(Collectors.toList());
            List<Map> finalCanvasCatIdData = canvasCatIdData;
            canvasCatIdData = categoryIds.stream().map(id -> {
                if (finalCanvasCatIdData.stream().anyMatch(cat -> cat.get("categoryId").toString().equals(id))) {
                    return finalCanvasCatIdData.stream().filter(cat -> cat.get("categoryId").toString().equals(id)).findFirst().get();
                } else {
                    return null;
                }
            }).collect(Collectors.toList());
            canvasCatIdData.removeIf(id -> id == null);
            categoryIds.clear();
            List<Map> finalcanvasCatIdData1 = canvasCatIdData;
            data.removeIf(one -> finalcanvasCatIdData1.stream()
                    .anyMatch(two -> (one.get("categoryId").equals(two.get("categoryId")) && !one.get("canvasId").equals(two.get("canvasId")))));
            categoryIds = data.stream().map(one -> one.get("categoryId").toString()).collect(Collectors.toList());
            if (customDate == null) {
                customDate = getCustomDate();
            }
            List<String> catIds = new ArrayList<>();
            List<String> chanelCatIds = new ArrayList<>();
            if (!pageType.equals("Home Page")) {
                String queryName = "category_site_" + (mode.equals("SITE") ? (regionCode.equals("US") ? "us" : "intl") : "reg");
                values.clear();
                Assert.assertFalse("ERROR - DATA: Category_ids are not available in site database with expected media:'" + String.join(", ", tempMediaNames) + "'", categoryIds.isEmpty());
                values.add(categoryIds.toArray(new String[categoryIds.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString(queryName).replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "int"));
                preparedStatement.setString(1, pageType);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                    catIds.add(resultSet.getString("cat_id"));
                Collections.shuffle(catIds);
                catIds.removeIf(cat -> cat.equals("8237") || cat.equals("65577"));
                catIds.removeIf(cat -> cat.equals("1003990") && mode.equals("WEDDING_REGISTRY"));
                catIds.removeIf(cat -> Arrays.asList(tempMediaNames).contains("SLIDESHOW") && cat.equals("1002984"));
                values.clear();
                Assert.assertFalse("ERROR - DATA: Category_ids are not available in site database with expected media:" + String.join(", ", tempMediaNames) + " after excluding all chanel brand categories", catIds.isEmpty());
                values.add(catIds.toArray(new String[catIds.size()]));
                resultSet = statement.executeQuery(updatedQuery((queries.getJSONObject("media_service").getString("category_site_chanel").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                while (resultSet.next())
                    chanelCatIds.add(resultSet.getString("cat_id"));
                if (!chanelCatIds.isEmpty()) {
                    catIds.removeAll(chanelCatIds);
                }
                int fetchCount = ((regionCode.equals("INTL") || !mode.equals("SITE")) ? ((catIds.size() > 3) ? 3 : catIds.size()) : 1);
                for (String cat : catIds) {
                    if (categoryCanvasData.size() >= fetchCount) {
                        break;
                    }
                    if (Categories.category(cat).has("externalHostUrl")) {
                        continue;
                    }
                    boolean response = Categories.activeCategory(cat);
                    String canvasId = null;
                    if (data.stream().anyMatch(catData -> catData.get("categoryId").toString().equals(cat))) {
                        canvasId = data.stream().filter(catData -> catData.get("categoryId").toString().equals(cat)).map(d -> d.get("canvasId").toString()).collect(Collectors.toList()).get(0);
                    }
                    JSONObject mediaData = Canvas.contextPoolMedia(canvasId, cat, context, "SITE", (new ArrayList<>()));
                    List<JSONObject> mediaContent = new ArrayList<>();
                    for (int index = 0; index < mediaData.getJSONObject("canvases").getJSONArray("canvas").length(); index++) {
                        JSONObject jsonObject = mediaData.getJSONObject("canvases").getJSONArray("canvas").getJSONObject(index);
                        for (int index1 = 0; index1 < jsonObject.getJSONArray("rows").length(); index1++) {
                            JSONObject jsonObject1 = jsonObject.getJSONArray("rows").getJSONObject(index1);
                            for (int index2 = 0; index2 < jsonObject1.getJSONArray("zones").length(); index2++) {
                                JSONObject jsonObject2 = jsonObject1.getJSONArray("zones").getJSONObject(index2);
                                for (int index3 = 0; index3 < jsonObject2.getJSONArray("contents").length(); index3++) {
                                    mediaContent.add(jsonObject2.getJSONArray("contents").getJSONObject(index3));
                                }
                            }
                        }
                    }
                    if (response && String.join(" ", tempMediaNames).contains("PRODUCT")) {
                        for (JSONObject content : mediaContent) {
                            if (content.has("attributes") && content.getJSONArray("attributes").length() != 0) {
                                for (int index = 0; index < content.getJSONArray("attributes").length(); index++) {
                                    if (content.getString("contentType").equals(tempMediaNames[0]) && (content.getJSONArray("attributes").length() != 0) && content.getJSONArray("attributes").getJSONObject(index).getString("name").equals("POOL_ROW_COUNT") && content.getJSONArray("attributes").getJSONObject(index).getInt("value") != 0) {
                                        if (content.getJSONArray("contentlinks").length() != 0 && content.getJSONArray("contentlinks").length() >= 3) {
                                            finalMediaData.add(content);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (String.join(" ", tempMediaNames).contains("PRODUCT_PANEL_NA") || String.join(" ", tempMediaNames).contains("PRODUCT_PANEL_BAZAAR")) {
                                if (content.has("attributes") && content.getJSONArray("attributes").length() != 0) {
                                    for (int index = 0; index < content.getJSONArray("attributes").length(); index++) {
                                        if (content.getJSONArray("attributes").getJSONObject(index).getString("name").toString().equals("HEADER_LINK_SORT_CRITERIA") && content.getJSONArray("attributes").getJSONObject(index).getString("value") != null) {
                                            finalMediaData.add(content);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                if (content.getString("contentType").equals(tempMediaNames[0]) && (content.has("attributes") && content.getJSONArray("attributes").length() != 0) && (content.has("contentlinks") && content.getJSONArray("contentlinks").length() != 0 && content.getJSONArray("contentlinks").length() >= 3)) {
                                    finalMediaData.add(content);
                                    break;
                                }
                            }
                        }
                        if (!finalMediaData.isEmpty()) {
                            categoryCanvasData.add(getCanvasAndComponentForCategory(data, tempMediaNames, cat, pageType));
                        }
                    } else if (response && String.join(" ", tempMediaNames).contains("FLEXIBLE_POOL")) {
                        List<JSONObject> mData = new ArrayList<>();
                        for (JSONObject content : mediaContent)
                            if (content.getString("contentGroupType").equals("FLEXIBLE_POOL")) {
                                mData.add(content);
                            }
                        if (mData.size() >= 4) {
                            finalMediaData = mData;
                        }
                        if (!finalMediaData.isEmpty()) {
                            categoryCanvasData.add(getCanvasAndComponentForCategory(data, tempMediaNames, cat, pageType));
                        }
                    } else if (response && String.join(" ", tempMediaNames).contains("MEDIA_ADS")) {
                        List<JSONObject> mediaZoneData = new ArrayList<>();
                        for (int index = 0; index < mediaData.getJSONObject("canvases").getJSONArray("canvas").length(); index++) {
                            JSONObject jsonObject = mediaData.getJSONObject("canvases").getJSONArray("canvas").getJSONObject(index);
                            for (int index1 = 0; index1 < jsonObject.getJSONArray("rows").length(); index1++) {
                                JSONObject jsonObject1 = jsonObject.getJSONArray("rows").getJSONObject(index1);
                                for (int index2 = 0; index2 < jsonObject1.getJSONArray("zones").length(); index2++) {
                                    mediaZoneData.add(jsonObject1.getJSONArray("zones").getJSONObject(index2));
                                }
                            }
                        }
                        JSONObject gridSection = null;
                        for (JSONObject zone : mediaZoneData)
                            for (int index = 0; index < zone.getJSONArray("contents").length(); index++) {
                                if ((zone.getJSONArray("contents").getJSONObject(index).getString("contentType")).equals("THUMBNAIL_GRID")) {
                                    gridSection = zone;
                                    break;
                                }
                            }
                        JSONObject mediaAdConent = gridSection.getJSONObject("nested-Content");
                        List keys = new ArrayList<>();
                        for (int index = 0; index < mediaAdConent.getJSONArray("entry").length(); index++)
                            keys.add(mediaAdConent.getJSONArray("entry").getJSONObject(index).getString("key"));
                        if (mediaAdConent != null && keys.size() >= 0) {
                            categoryCanvasData.add(getCanvasAndComponentForCategory(data, tempMediaNames, cat, pageType));
                        }
                    } else {
                        if (response) {
                            categoryCanvasData.add(getCanvasAndComponentForCategory(data, tempMediaNames, cat, pageType));
                        }
                    }
                }
            } else {
                categoryCanvasData.add(getCanvasAndComponentForCategory(data, tempMediaNames, (site.equals("mcom") ? "62678" : "1001111"), pageType));
            }
            Assert.assertFalse("ERROR - DATA: Unable to find category form site database with expected media:'" + String.join(", ", tempMediaNames) + "'", categoryCanvasData.isEmpty());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryCanvasData;
    }

    /*
        Get canvas and component data from category
         @param[List<Map>, String[], String, String]
         @return[List]
     */
    public static List getCanvasAndComponentForCategory(List<Map> data, String[] mediaNames, String catId, String pageType) throws Throwable {
        List finalData = new ArrayList<>();
        Assert.assertFalse("ERROR - DATA: Unable to find category form SDP with expected media:'" + mediaNames.toString() + "'", catId == null);
        String canvasId = null;
        if (data.stream().anyMatch(cate -> (cate.get("categoryId").toString().equals(catId)))) {
            canvasId = data.stream().filter(cate -> (cate.get("categoryId").toString().equals(catId))).map(cate -> cate.get("canvasId").toString()).findFirst().get();
        } else {
            Assert.fail("ERROR - DATA: Unable to find canvas_id with expected media:'" + mediaNames.toString() + "' for " + pageType + " page");
        }
        String componentId = (!Arrays.asList(mediaNames).stream().anyMatch(name -> name.contains("PRODUCT"))) ? null : String.valueOf(finalMediaData.get(0).getInt("componentID"));
        finalData.add(catId);
        finalData.add(canvasId);
        finalData.add(componentId);
        return finalData;
    }

    /*
        Get canvas category data using SQL resultSet
        @param[ResultSet] SQL data
        @return[List<Map>]
     */
    public static List<Map> getCanvasCategoryData(ResultSet resultSet) throws Throwable {
        List<Map> canvasCatData = new ArrayList<>();

        while (resultSet.next()) {
            Map val = new HashMap<>();
            val.put("ruleId", resultSet.getString("rule_id"));
            val.put("categoryId", resultSet.getString("category_id"));
            val.put("canvasId", resultSet.getString("canvas_id"));
            canvasCatData.add(val);
        }
        return canvasCatData;
    }

    public static Date getCustomDate() throws Throwable {

        try {
            setupConnection();
            ResultSet rs = statement.executeQuery(Utils.getSqlQueries().get("custom_date").toString());
            if (rs.next()) {
                customDate = rs.getTimestamp("timestamp_value");
            }
            Timestamp currentTimeStamp = new Timestamp(System.currentTimeMillis());
            customDate = customDate == null ? currentTimeStamp : customDate;
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        return customDate;
    }

    /*
        To setup DB connection
     */
    private static void setupConnection() throws Throwable {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                System.out.println("Error occure while craeting database connection" + e.getMessage());
            }
        }
    }


    private static String getStringFromArray(String[] array, String type) {
        if (type.equals("int")) {
            return Arrays.asList(array).toString().replace("[", "").replace(", ", ", ").replace("]", "");
        } else {
            return Arrays.asList(array).toString().replace("[", "'").replace(", ", "', '").replace("]", "'");
        }
    }
}
