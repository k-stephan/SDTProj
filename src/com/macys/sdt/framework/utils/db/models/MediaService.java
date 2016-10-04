package com.macys.sdt.framework.utils.db.models;

import com.macys.sdt.framework.utils.Utils;
import com.macys.sdt.framework.utils.db.utils.DBUtils;
import org.apache.commons.collections4.ListUtils;
import org.json.JSONObject;
import org.junit.Assert;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class MediaService {
    public Statement statement;
    public Connection connection;
    public JSONObject queries;
    public int thumbnailFlag = 0, widgetFlag = 0, bannerFlag = 0, mediaBannerFlag = 0;
    public boolean bannerData = false;
    public Map mediaGroupTypeData, mediaComponentTypeData;
    public List<JSONObject> finalMediaData = new ArrayList<>();
    public Date customDate;

    /*
    # To Get only row_contextualized media details based upon media_name, row_type ,context_attr_name,context_attr_value
    # @param[String[], String, String[], String[]] expected media names, row type and context attr name and value
    # @return[Array[Hashes]] return all media details for only row contextualize media ex => copy_block, text, recently_review, Horizontal_rule
    # EX [{:canvas_id=>"401130", :canvas_row_id=>5025325, :media_name => 'dhhfj.jpg', :text=>"Alt", :description=>"Ad"},{....}]
    #
    */
    public List<Map> getRowContextualizeCanvasId(String[] mediaNamesArray, String rowType, String[] contextAttrNames, String[] contextAttrValues) throws Throwable {
        List canvasData = new ArrayList<>();
        try {
            setupConnection();
            if (queries == null) {
                queries = Utils.getSqlQueries();
            }
            JSONObject contextData = Utils.getContextualizeMedia();
            String mediaType = ((contextData.getJSONObject("GROUP").has(mediaNamesArray[0])) ? "GROUP" : "COMPONENT");
            List<String> canvasRowIds = new ArrayList<>();
            canvasRowIds = getCanvasRowIds(queries.getJSONObject("media_service").get("canvas_layout_attribute_data").toString(), contextAttrNames, contextAttrValues, 4);
            if (customDate == null) {
                customDate = DBUtils.getCustomDate();
            }
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
    public List<Map> getContextualizeData(String[] mediaNamesArray, String rowType, String[] contextAttrNames, String[] contextAttrValues) throws Throwable {
        List<Map> finalMediaInfo = new ArrayList<>();
        List context = new ArrayList<>();
        context.add(contextAttrNames);
        context.add(contextAttrValues);
        int count = (Arrays.asList(contextAttrNames).contains("INHERITABLE_ROW_TYPE")) ? 5 : 4;
        try {
            setupConnection();
            if (queries == null) {
                queries = Utils.getSqlQueries();
            }
            List<String> canvasRowIds = new ArrayList<>();
            canvasRowIds = getCanvasRowIds(queries.getJSONObject("media_service").getString("canvas_layout_attribute_data"), contextAttrNames, contextAttrValues, count);
            if (customDate == null) {
                customDate = DBUtils.getCustomDate();
            }
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
            List<String> groupMediaKeys = new ArrayList<>();
            List<String> componentMediaKeys = new ArrayList<>();
            Iterator<Map> iterator = rowLevelContextData.iterator();
            while (iterator.hasNext()) {
                Map row = iterator.next();
                if (row.get("mediaKeyType").toString().equals("GROUP") && row.get("mediaKey") != null) {
                    groupMediaKeys.add(row.get("mediaKey").toString());
                }
                if (row.get("mediaKeyType").toString().equals("COMPONENT") && row.get("mediaKey") != null) {
                    componentMediaKeys.add(row.get("mediaKey").toString());
                }
                if (row.get("mediaKey") == null) {
                    iterator.remove();
                }
            }
            finalMediaInfo = rowLevelContextData;
            finalMediaInfo = getContextualizeMedia(mediaNamesArray, groupMediaKeys, componentMediaKeys, context, finalMediaInfo);
            iterator = finalMediaInfo.iterator();
            String[] mediaNames = {"PRODUCT_PANEL_NA", "PRODUCT_PANEL_CATEGORY", "PRODUCT_PANEL_CATEGORY_FACET", "THUMBNAIL_GRID"};
            if (ListUtils.subtract(Arrays.asList(mediaNamesArray), Arrays.asList(mediaNames)).size() == 0) {
                while (iterator.hasNext()) {
                    Map data = (Map) iterator.next();
                    if (data.get("mediaTypeDesc") == null) {
                        iterator.remove();
                    }
                }
            } else {
                if (thumbnailFlag != 1) {
                    while (iterator.hasNext()) {
                        Map data = (Map) iterator.next();
                        if (!data.keySet().contains("mediaInfo")) {
                            iterator.remove();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return finalMediaInfo;
    }

    public List<Map> getContextualizeMedia(String[] mediaNamesArray, List groupMediaKeys, List componentMediaKeys, List<String[]> context, List<Map> finalMediaInfo) throws Throwable {
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
                for (Object data : (groupData.get(0)))
                    if (((Map) data).get("mediaTypeDesc").equals("CATEGORY_ICON")) {
                        catIconMediaKeys.add(((Map) data).get("mediaKey").toString());
                    }
            }
            if (catIconMediaKeys.isEmpty()) {
                for (Object data : (groupData.get(0)))
                    groupContextualizeMedia.add(((Map) data).get("mediaKey").toString());
            } else {
                for (Object data : (groupData.get(0)))
                    if (!((Map) data).get("mediaTypeDesc").equals("CATEGORY_ICON")) {
                        groupContextualizeMedia.add(((Map) data).get("mediaKey").toString());
                    }
            }
            for (Object data : (groupData.get(groupData.size() - 1)))
                groupNonContextualizeMedia.add(((Map) data).get("mediaKey"));
        }
        //Get all component media key with expected media
        if (!componentMediaKeys.isEmpty()) {
            List<List> componentData = getMediaTypeDescription(componentMediaKeys, "component", mediaNamesArray, finalMediaInfo);
            for (Object data : (componentData.get(0)))
                componentContextualizeMedia.add(((Map) data).get("mediaKey").toString());
            for (Object data : (componentData.get(componentData.size() - 1)))
                componentNonContextualizeMedia.add(((Map) data).get("mediaKey"));
        }
        Iterator iterator = finalMediaInfo.iterator();
        if (mediaNamesArray.length != 0) {
            while (iterator.hasNext()) {
                Map type = (Map) iterator.next();
                if (type == null || !type.keySet().contains("mediaTypeDesc")) {
                    iterator.remove();
                }
            }
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
                customDate = DBUtils.getCustomDate();
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
                List<Map> mediaParameterData = new ArrayList<>();
                List<String> refIds = new ArrayList<>();
                List<Map> popData = new ArrayList<>();
                List<String> adComponentIds = new ArrayList<>();
                for (Map type : mediaGroupData)
                    if (type.get("mediaTypeDesc").equals("AD")) {
                        adComponentIds.add(type.get("componentId").toString());
                    }
                List<String[]> values = new ArrayList<>();
                values.add(adComponentIds.toArray(new String[adComponentIds.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_component_id_in_media_parameter").toString(), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();
                mediaParameterData = getMediaParameterDataFromResult(resultSet);
                for (Map type : mediaParameterData)
                    refIds.add(type.get("refId").toString());
                values.clear();
                values.add(refIds.toArray(new String[refIds.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_static_link_url").toString(), values, "string"));
                resultSet = preparedStatement.executeQuery();
                popData = getMediaParameterDataFromResult(resultSet);
                List popupComponentIds = new ArrayList<>();
                for (Map pop : popData)
                    popupComponentIds.add(pop.get("componentId"));
                List<String> groupComponentIds = new ArrayList<>();
                for (Map type : mediaGroupData)
                    groupComponentIds.add(type.get("componentId").toString());
                for (Map param : mediaParameterData) {
                    if (groupComponentIds.contains(param.get("componentId").toString())) {
                        for (Map type : mediaGroupData) {
                            if (type.get("componentId").toString().equals(param.get("componentId").toString())) {
                                if (param.get("linkType").toString().equals("4") || (popupComponentIds.contains(param.get("componentId").toString()))) {
                                    type.put("mediaTypeDesc", "CUSTOM_POPUP");
                                }
                                if (!(param.get("regionCoordinates") == null || param.get("regionCoordinates").equals(""))) {
                                    type.put("mediaTypeDesc", "IMAGE_MAP");
                                }
                            }
                        }
                    }
                }
                for (Map type : finalMediaInfo) {
                    for (Map mgType : mediaGroupData)
                        if (mgType.get("mediaKey").toString().equals(type.get("mediaKey").toString())) {
                            type.put("mediaTypeDesc", mgType.get("mediaTypeDesc").toString());
                        }
                }
                iterator = finalMediaInfo.iterator();
                while (iterator.hasNext()) {
                    Map type = (Map) iterator.next();
                    if (type.get("mediaTypeDesc") == null) {
                        iterator.remove();
                    } else {
                        if (adFlag == 0 && type.get("mediaTypeDesc").toString().equals("AD")) {
                            iterator.remove();
                        } else if (imageMapFlag == 0 && type.get("mediaTypeDesc").toString().equals("IMAGE_MAP")) {
                            iterator.remove();
                        } else if (popupFlag == 0 && type.get("mediaTypeDesc").toString().equals("CUSTOM_POPUP")) {
                            iterator.remove();
                        }
                    }
                }
            }
            finalMediaInfo = updateMediaInformation(mediaNamesArray, groupNonContextualizeMedia, mediaGroupData, finalMediaInfo);
        }
        if (!componentContextualizeMedia.isEmpty()) {
            List componentMediaLevelContextData = new ArrayList<>();
            if (bannerFlag == 1) {
                List<String[]> values = new ArrayList<>();
                List mediaComponentDataTwo = new ArrayList<>();
                values.add(componentContextualizeMedia.toArray(new String[componentContextualizeMedia.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                ResultSet resultSet = preparedStatement.executeQuery();
                componentMediaLevelContextData = getMediaGroupDataFromResult(resultSet, "group");
                values.clear();
                values.add(componentContextualizeMedia.toArray(new String[componentContextualizeMedia.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("without_media_parameter").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                resultSet = preparedStatement.executeQuery();
                mediaComponentDataTwo = getMediaGroupDataFromResult(resultSet, "group");
                if (!mediaComponentDataTwo.isEmpty()) {
                    componentMediaLevelContextData.addAll(mediaComponentDataTwo);
                }
            } else {
                List mediaComponentIds = getGroupIdsWithMultipleContext(context.get(0), context.get(context.size() - 1), "media_component_attribute_data");
                List mediaComponentDataSecond = new ArrayList<>();
                List<String> finalMediaKeys = ListUtils.sum(mediaComponentIds, componentContextualizeMedia);
                componentMediaLevelContextData = getGroupMediaContextData(finalMediaKeys, "with_component_media_context", customDate.toString());
                mediaComponentDataSecond = getGroupMediaContextData(finalMediaKeys, "without_component_media_context", customDate.toString());
                if (!mediaComponentDataSecond.isEmpty()) {
                    componentMediaLevelContextData.addAll(mediaComponentDataSecond);
                }
            }
            finalMediaInfo = updateMediaInformation(mediaNamesArray, componentNonContextualizeMedia, componentMediaLevelContextData, finalMediaInfo);
        }
        if (!groupNonContextualizeMedia.isEmpty()) {
            finalMediaInfo = getIndirectContextualizeData(groupNonContextualizeMedia, context.get(0), context.get(context.size() - 1), finalMediaInfo, "GROUP");
        }
        if (!componentNonContextualizeMedia.isEmpty()) {
            finalMediaInfo = getIndirectContextualizeData(componentNonContextualizeMedia, context.get(0), context.get(context.size() - 1), finalMediaInfo, "COMPONENT");
        }
        return finalMediaInfo;
    }

    public List<Map> getGroupMediaContextData(List<String> mediaKeys, String queryName, String customDate) throws Throwable {
        List<Map> mediaGroupData = new ArrayList<>();
        List<String[]> values = new ArrayList<>();
        values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
        String sqlQuery = updatedQuery((queries.getJSONObject("media_service").getString(queryName).replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string");
        if (queryName.equals("without_group_media_context")) {
            sqlQuery = sqlQuery.replace("media_parameter.text, ", "");
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
        ResultSet resultSet = preparedStatement.executeQuery();
        mediaGroupData = getMediaGroupDataFromResult(resultSet, (queryName.contains("group") ? "group" : "component"));
        return mediaGroupData;
    }

    public List<Map> getIndirectContextualizeData(List<String> mediaKeys, String[] attrNames, String[] attrValues, List<Map> finalMediaInfo, String mediaType) throws Throwable {
        List<String[]> context = new ArrayList<>();
        context.add(attrNames);
        context.add(attrValues);
        if (customDate == null) {
            customDate = DBUtils.getCustomDate();
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
            List mKeys = new ArrayList<>();
            for (Map mgc : mediaGroupComponentData) {
                mKeys.add(mgc.get("componentId"));
            }
            mediaKeys = mKeys;
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
            String mode = (Arrays.asList(attrValues).contains("REGISTRY")) ? "REGISTRY" : "SITE";
            List<String> paramIds = new ArrayList<>();
            for (Map param : mediaParameterData)
                paramIds.add(param.get("parameterId").toString());
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
            values.clear();
            values.add(paramIds.toArray(new String[paramIds.size()]));
            preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_widget_data").replaceAll("<= \\?", "<= '" + dateFormat.format(customDate) + "'").replaceAll(">= \\?", ">= '" + dateFormat.format(customDate) + "'")), values, "int").replaceAll("\\?", mode));
            resultSet = preparedStatement.executeQuery();
            List mediaParameterAttrData = new ArrayList<>();
            while (resultSet.next())
                mediaParameterAttrData.add(resultSet.getString("parameter_id"));
            Iterator iterator = mediaParameterData.iterator();
            if (!mediaParameterAttrData.isEmpty()) {
                while (iterator.hasNext()) {
                    Map param = (Map) iterator.next();
                    if (!mediaParameterAttrData.contains(param.get("parameterId"))) {
                        iterator.remove();
                    }
                }
            }
        }
        if (mediaType.equals("GROUP")) {
            for (Map param : mediaParameterData) {
                for (Map groupComponent : mediaGroupComponentData) {
                    if (groupComponent.get("componentId").equals(param.get("mediaKey"))) {
                        param.put("mediaKey", groupComponent.get("mediaGroupId"));
                    }
                }
            }
        }
        if (bannerFlag == 1) {
            String[] linkTypes = {"1", "2", "3"};
            List<String> paramIds = new ArrayList<>();
            Iterator iterator = mediaParameterData.iterator();
            while (iterator.hasNext()) {
                Map param = (Map) iterator.next();
                if (Arrays.asList(linkTypes).contains(param.get("linkType"))) {
                    iterator.remove();
                } else {
                    paramIds.add(param.get("parameterId").toString());
                }
            }
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
            for (Map param : mediaParameterData)
                for (Map attr : mediaParamAttrData)
                    if (attr.get("parameterId").toString().equals(param.get("parameterId").toString())) {
                        param.put("deviceType", attr.get("attrValue"));
                    }
        }
        boolean isRefIdExists = false;
        for (Map info : finalMediaInfo)
            if (info.keySet().contains("refIds")) {
                isRefIdExists = true;
            }
        if (isRefIdExists) {
            Iterator iterator = finalMediaInfo.iterator();
            while (iterator.hasNext()) {
                Map info = (Map) iterator.next();
                if (info.keySet().contains("refIds") && ((List) info.get("refIds")).isEmpty()) {
                    iterator.remove();
                }
                List finalRefIds = new ArrayList<>();
                for (String ref : (List<String>) info.get("refIds")) {
                    int findFlag = 0;
                    for (Map param : mediaParameterData) {
                        if (param.get("mediaKey").toString().equals(ref)) {
                            findFlag = 1;
                        }
                    }
                    if (findFlag == 0) {
                        finalRefIds.add(ref);
                    } else {
                        for (Map param : mediaParameterData)
                            if (param.get("mediaKey").toString().equals(ref)) {
                                finalRefIds.add(param.get("refId"));
                            }
                    }
                }
                info.put("refIds", finalRefIds);
            }
            List<Map> paramData = new ArrayList<>();
            if (bannerFlag == 1) {
                List paraRefIds = new ArrayList<>();
                for (Map param : mediaParameterData) {
                    Map type = new HashMap<>();
                    type.put("mediaKey", param.get("refId"));
                    type.put("seqNumber", param.get("seqNumber"));
                    type.put("mediaTypeDesc", param.get("mediaTypeDesc"));
                    type.put("parameterText", param.get("parameterText"));
                    type.put("deviceType", param.get("deviceType"));
                    paraRefIds.add(param.get("refId"));
                    paramData.add(type);
                }
                String[] empty = {};
                updateMediaInformation(empty, paraRefIds, paramData, finalMediaInfo);
            }
        } else {
            for (Map info : finalMediaInfo) {
                List paramRefs = new ArrayList<>();
                for (Map param : mediaParameterData) {
                    if (param.get("mediaKey").toString().equals(info.get("mediaKey").toString())) {
                        paramRefs.add(param.get("refId"));
                    }
                }
                info.put("refIds", paramRefs);
            }
            if (bannerFlag == 1) {
                List<String> linkTypeRefIds = new ArrayList<>();
                for (Map parm : mediaParameterData)
                    if (parm.get("linkType").toString().equals("3")) {
                        linkTypeRefIds.add(parm.get("refId").toString());
                    }
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
                for (Map info : finalMediaInfo) {
                    for (Map param : mediaParameterData) {
                        Map type = new HashMap<>();
                        if (param.get("mediaKey").toString().equals(info.get("mediaKey").toString())) {
                            type.put("refId", param.get("refId"));
                            type.put("linkType", param.get("linkType"));
                            type.put("text", param.get("parameterText"));
                            type.put("deviceType", param.get("deviceType"));
                            String urlText = null;
                            for (Map url : staticData) {
                                if (url.get("staticLinkId").toString().equals(param.get("refId").toString())) {
                                    urlText = url.get("urlText").toString();
                                    break;
                                }
                            }
                            type.put("url", urlText);
                        }
                        info.put("bannerMachine", type);
                    }
                }
            }
        }
        List groupRefIds = new ArrayList<>();
        List componentRefIds = new ArrayList<>();
        for (Map param : mediaParameterData) {
            if (param.get("mediaTypeDesc").toString().contains("MEDIA")) {
                groupRefIds.add(param.get("refId").toString());
            }
            if (param.get("mediaTypeDesc").toString().contains("MEDIACOMPONENT")) {
                componentRefIds.add(param.get("refId").toString());
            }
        }
        String[] empty = {};
        finalMediaInfo = getContextualizeMedia(empty, groupRefIds, componentRefIds, context, finalMediaInfo);
        return finalMediaInfo;
    }

    public List<Map> updateMediaInformation(String[] mediaNamesArray, List groupNonContextualizeMedia, List<Map> mediaGroupData, List<Map> finalMediaInfo) throws Throwable {
        Iterator iterator = finalMediaInfo.iterator();
        while (iterator.hasNext()) {
            Map type = (Map) iterator.next();
            if (type.keySet().contains("refIds") && type.get("refIds") == null) {
                iterator.remove();
            }
        }
        for (Map type : finalMediaInfo) {
            List refMediaInfo = new ArrayList<>();
            if (mediaNamesArray.length == 0) {
                for (Object ref : (List) type.get("refIds"))
                    for (Map media : mediaGroupData)
                        if (media.get("mediaKey").equals(ref.toString())) {
                            refMediaInfo.add(media);
                        }
            } else {
                for (Map media : mediaGroupData)
                    if (media.get("mediaKey").equals(type.get("mediaKey"))) {
                        refMediaInfo.add(media);
                    }
            }
            if (!refMediaInfo.isEmpty()) {
                if (!type.keySet().contains("mediaInfo") || type.get("mediaInfo") == null) {
                    type.put("mediaInfo", refMediaInfo);
                } else {
                    refMediaInfo.addAll((List) type.get("mediaInfo"));
                    type.put("mediaInfo", refMediaInfo);
                }
            }
        }
        return finalMediaInfo;
    }

    public List<Map> getMediaParameterDataFromResult(ResultSet resultSet) throws Throwable {
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

    public List<Map> getMediaGroupDataFromResult(ResultSet resultSet, String mediaType) throws Throwable {
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

    public List<List> getMediaTypeDescription(List<String> mediaKeys, String mediaType, String[] mediaNamesArray, List<Map> finalMediaInfo) throws Throwable {
        List<Map> mediaTypes = new ArrayList<>();
        setupConnection();
        if (queries == null) {
            queries = Utils.getSqlQueries();
        }
        ResultSet resultSet;
        if (mediaType.equals("group")) {
            if (customDate == null) {
                customDate = DBUtils.getCustomDate();
            }
            List<String[]> values = new ArrayList<>();
            values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString("with_media_group_ids").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map type = new HashMap<>();
                type.put("mediaKey", resultSet.getString("media_group_id"));
                type.put("mediaTypeDesc", mediaGroupTypeData.get(resultSet.getString("media_group_type")));
                mediaTypes.add(type);
            }
        } else {
            List<String[]> values = new ArrayList<>();
            values.add(mediaKeys.toArray(new String[mediaKeys.size()]));
            PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_media_component_id").toString(), values, "string"));
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Map type = new HashMap<>();
                type.put("mediaKey", resultSet.getString("component_id"));
                type.put("mediaTypeDesc", mediaComponentTypeData.get(resultSet.getString("media_type")));
                mediaTypes.add(type);
            }
        }
        if (mediaType.equals("component") && widgetFlag == 1 && mediaBannerFlag == 2) {
            mediaBannerFlag = 1;
        }
        boolean isBannerMachine = false;
        for (Map type : mediaTypes)
            if (type.get("mediaTypeDesc").toString().equals("BANNER_MACHINE")) {
                isBannerMachine = true;
            }
        boolean conditionOne = (bannerFlag == 1 && mediaBannerFlag != 2 && !isBannerMachine && mediaType.equals("group"));
        boolean conditionTwo = (mediaBannerFlag == 2 && bannerFlag == 1 && !isBannerMachine && mediaType.equals("group"));
        Iterator iterator = finalMediaInfo.iterator();
        while (iterator.hasNext()) {
            Map info = (Map) iterator.next();
            if (info.containsKey("mediaTypeDesc") && (info.get("mediaTypeDesc") == null || info.get("mediaTypeDesc").toString().equals("null"))) {
                iterator.remove();
            }
        }
        iterator = finalMediaInfo.iterator();
        while (iterator.hasNext()) {
            Map info = (Map) iterator.next();
            if (info.containsKey("mediaTypeDesc") && info.get("mediaTypeDesc").equals("BANNER_MACHINE")) {
                bannerData = true;
            }
        }
        if (bannerFlag == 1 && (isBannerMachine && mediaBannerFlag != 1 && mediaBannerFlag != 0)) {
            List<String> mediaIds = new ArrayList<>();
            for (Map type : mediaTypes)
                if (type.get("mediaTypeDesc").toString().equals("BANNER_MACHINE")) {
                    mediaIds.add(type.get("mediaKey").toString());
                }
            List<String> mediaRowIds = new ArrayList<>();
            if (mediaBannerFlag != 2 && bannerFlag == 1 && widgetFlag != 1) {
                for (Map info : finalMediaInfo)
                    if (mediaIds.contains(info.get("mediaKey").toString())) {
                        mediaRowIds.add(info.get("canvasRowId").toString());
                    }
            }
            if (mediaBannerFlag == 2 && bannerFlag == 1) {
                for (Map info : finalMediaInfo)
                    for (String id : mediaIds)
                        if (((List) info.get("refIds")).contains(id)) {
                            mediaRowIds.add(info.get("canvasRowId").toString());
                        }
            }
            iterator = finalMediaInfo.iterator();
            if (!mediaRowIds.isEmpty()) {
                while (iterator.hasNext()) {
                    Map info = (Map) iterator.next();
                    if (!mediaRowIds.contains(info.get("canvasRowId").toString())) {
                        iterator.remove();
                    }
                }
            }
            if (mediaBannerFlag == 2 && bannerFlag == 1) {
                for (Map info : finalMediaInfo) {
                    List<String> refs = new ArrayList<>();
                    for (String ref : (List<String>) info.get("refId"))
                        if (mediaIds.contains(ref)) {
                            refs.add(ref);
                        }
                    info.put("refIds", refs);
                }
            }
            if (!mediaRowIds.isEmpty()) {
                bannerData = true;
            }

        } else if ((conditionOne || conditionTwo) && !bannerData && !isBannerMachine) {
            org.junit.Assert.fail("ERROR - DATA: Banner Machine media data not found");
        } else {
            if (mediaNamesArray.length != 0 && (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") || Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR"))) {
                String attributeValue = (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_NA") ? "NA" : (Arrays.asList(mediaNamesArray).contains("PRODUCT_PANEL_BAZAAR") ? "CUSTRATING" : null));
                List<String> mediaIds = new ArrayList<>();
                for (Map type : mediaTypes)
                    mediaIds.add(type.get("mediaKey").toString());
                List<String[]> values = new ArrayList<>();
                values.add(mediaIds.toArray(new String[mediaIds.size()]));
                PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get("with_header_link_sort_criteria").toString(), values, "string"));
                preparedStatement.setString(1, attributeValue);
                resultSet = preparedStatement.executeQuery();
                List<String> finalMediaKeys = new ArrayList<>();
                while (resultSet.next())
                    finalMediaKeys.add(resultSet.getString("component_id"));
                iterator = mediaTypes.iterator();
                while (iterator.hasNext()) {
                    Map type = (Map) iterator.next();
                    if (((!type.get("mediaTypeDesc").toString().equals("PRODUCT_PANEL_CATEGORY")) ? (!(Arrays.asList(mediaNamesArray).contains(type.get("mediaTypeDesc").toString()))) : false) || ((!type.get("mediaTypeDesc").toString().equals("PRODUCT_PANEL_CATEGORY")) ? (!(finalMediaKeys.contains(type.get("mediaKey").toString()))) : false)) {
                        iterator.remove();
                    }
                }
            } else if (bannerFlag == 1 && mediaBannerFlag == 0) {
                List<String> bannerCanvasRowIds = new ArrayList<>();
                for (Map info : finalMediaInfo)
                    if (info.get("mediaTypeDesc").equals("BANNER_MACHINE")) {
                        bannerCanvasRowIds.add(info.get("canvasRowId").toString());
                    }
                List<String> mediaCanvasRowIds = new ArrayList<>();
                for (Map type : mediaTypes)
                    if (type.get("mediaTypeDesc").equals("BANNER_MACHINE")) {
                        mediaCanvasRowIds.add(type.get("canvasRowId").toString());
                    }
                List<Map> mediaTypeData = new ArrayList<>();
                for (Map type : mediaTypes) {
                    String canvasRowId = null;
                    for (Map info : finalMediaInfo) {
                        if (type.get("mediaKey").toString().equals(info.get("mediaKey").toString())) {
                            canvasRowId = info.get("canvasRowId").toString();
                            break;
                        }
                    }
                    type.put("canvasRowId", canvasRowId);
                    mediaTypeData.add(type);
                }
                iterator = mediaTypeData.iterator();
                while (iterator.hasNext()) {
                    Map type = (Map) iterator.next();
                    if ((!(bannerCanvasRowIds.contains(type.get("canvasRowId").toString())) && !(mediaCanvasRowIds.contains(type.get("canvasRowId").toString()))) || !(Arrays.asList(mediaNamesArray).contains(type.get("mediaTypeDesc").toString()))) {
                        iterator.remove();
                    }
                }
            } else {
                iterator = mediaTypes.iterator();
                if (mediaNamesArray.length != 0) {
                    while (iterator.hasNext()) {
                        Map type = (Map) iterator.next();
                        if (!Arrays.asList(mediaNamesArray).contains(type.get("mediaTypeDesc").toString())) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        if (mediaNamesArray.length != 0 && mediaTypes.size() > 0) {
            iterator = finalMediaInfo.iterator();
            while (iterator.hasNext()) {
                Map info = (Map) iterator.next();
                if (info == null) {
                    iterator.remove();
                }
                String finalMediaType = null;
                for (Map type : mediaTypes)
                    if (type.get("mediaKey").toString().equals(info.get("mediaKey").toString())) {
                        finalMediaType = type.get("mediaTypeDesc").toString();
                        break;
                    }
                info.put("mediaTypeDesc", finalMediaType);
            }
        }
        String[] medias = {"row_contextualize_media", "contextualize_media"};
        List<Map> contextualizeMedia = new ArrayList<>();
        List<Map> nonContextualizeMedia = new ArrayList<>();
        JSONObject contextData = Utils.getContextualizeMedia();
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

    public Map getMediaGroupTypes() throws Throwable {
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

    public Map getMediaComponentTypes() throws Throwable {
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

    public List getCanvasRowIds(String query, String[] contextAttrNames, String[] contextAttrValues, int count) throws Throwable {
        setupConnection();
        List<String[]> values = new ArrayList<>();
        values.add(contextAttrNames);
        values.add(contextAttrValues);
        PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(query, values, "string"));
        preparedStatement.setInt(1, count);
        List<String> canvasRowIds = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
            canvasRowIds.add(resultSet.getString("canvas_row_id"));
        return canvasRowIds;
    }

    public String updatedQuery(String query, List values, String type) throws Throwable {
        for (int index = 0; index < values.size(); index++) {
            if (query.contains("IN (?)")) {
                query = query.replaceFirst("IN \\(\\?\\)", "IN (" + getStringFromArray((String[]) values.get(index), type) + ")");
            }
        }
        return query;
    }

    public List getGroupIdsWithMultipleContext(String[] contextAttrNames, String[] contextAttrValues, String queryName) throws Throwable {
        List<String[]> values = new ArrayList<>();
        values.add(contextAttrNames);
        values.add(contextAttrValues);
        PreparedStatement preparedStatement = connection.prepareStatement(updatedQuery(queries.getJSONObject("media_service").get(queryName).toString(), values, "string"));
        ResultSet resultSet = preparedStatement.executeQuery();
        List<String> mediaGroupIds = new ArrayList<>();
        while (resultSet.next()) {
            if (queryName.equals("media_group_attribute_data")) {
                mediaGroupIds.add(resultSet.getString("media_group_id"));
            } else {
                mediaGroupIds.add(resultSet.getString("component_id"));
            }
        }
        return mediaGroupIds;
    }

    public List getCategoryId(List<String> canvasIds, String pageType, List<Map<String, String>> context, String site, String[] tempMediaNames) throws Throwable {
        String mode = "", regionCode = "";
        List categoryCanvasData = new ArrayList<>();
        for (Map con : context) {
            mode = mode + con.get("SHOPPING_MODE").toString();
            regionCode = regionCode + con.get("REGION_CODE").toString();
        }
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
            List<String> categoryIds = new ArrayList<>();
            for (Map cat : data) {
                categoryIds.add(cat.get("categoryId").toString());
            }
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
            List<Map> mainData = new ArrayList<>();

            categoryIds.clear();
            for (Map a : canvasCatIdData)
                ((ArrayList) categoryIds).add(a.get("categoryId"));
            LinkedHashSet<String> lhs = new LinkedHashSet<String>();
            lhs.addAll(categoryIds);
            categoryIds.clear();
            categoryIds.addAll(lhs);
            List<Map> can = new ArrayList<>();
            for (String name : categoryIds) {
                Iterator iterator = canvasCatIdData.iterator();
                while (iterator.hasNext()) {
                    Map type = (Map) iterator.next();
                    if (type.get("categoryId").toString().equals(name)) {
                        can.add(type);
                        break;
                    }
                }
            }
            categoryIds.clear();
            mainData.clear();
            for (Map one : data) {
                for (Map two : can) {
                    if (one.get("categoryId").equals(two.get("categoryId")) && one.get("canvasId").equals(two.get("canvasId"))) {
                        mainData.add(one);
                        break;
                    }
                }
            }
            categoryIds.clear();
            for (Map one : mainData)
                categoryIds.add(one.get("categoryId").toString());
            if (customDate == null) {
                customDate = DBUtils.getCustomDate();
            }
            List<String> catIds = new ArrayList<>();
            List<String> chanelCatIds = new ArrayList<>();
            if (!pageType.equals("Home Page")) {
                String queryName = null;
                if (mode.equals("SITE") && regionCode.equals("US")) {
                    queryName = "category_site_us";
                } else if (mode.equals("SITE") && regionCode.equals("INTL")) {
                    queryName = "category_site_intl";
                } else {
                    queryName = "category_site_reg";
                }
                values.clear();
                if (categoryIds.isEmpty()) {
                    Assert.fail("ERROR - DATA: Category_ids are not available in site database with expected media:'" + Arrays.asList(tempMediaNames).toString() + "'");
                }
                values.add(categoryIds.toArray(new String[categoryIds.size()]));
                preparedStatement = connection.prepareStatement(updatedQuery((queries.getJSONObject("media_service").getString(queryName).replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "int"));
                preparedStatement.setString(1, pageType);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next())
                    catIds.add(resultSet.getString("cat_id"));
                if (catIds.contains("8237")) {
                    catIds.remove("8237");
                }
                if (catIds.contains("65577")) {
                    catIds.remove("65577");
                }
                if (catIds.contains("1003990") && mode.equals("WEDDING_REGISTRY")) {
                    catIds.remove("1003990");
                }
                if (Arrays.asList(tempMediaNames).contains("SLIDESHOW") && catIds.contains("1002984")) {
                    catIds.remove("1002984");
                }
                if (catIds.isEmpty()) {
                    org.junit.Assert.fail("ERROR - DATA: Category_ids are not available in site database with expected media:" + String.join(", ", tempMediaNames) + " after exluding all chanel brand categories");
                }
                values.clear();
                values.add(catIds.toArray(new String[catIds.size()]));
                resultSet = statement.executeQuery(updatedQuery((queries.getJSONObject("media_service").getString("category_site_chanel").replaceAll("<= \\?", "<= '" + customDate.toString() + "'").replaceAll(">= \\?", ">= '" + customDate.toString() + "'")), values, "string"));
                while (resultSet.next()) {
                    chanelCatIds.add(resultSet.getString("cat_id"));
                }
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
                    for (Map catData : mainData)
                        if (catData.get("categoryId").toString().equals(cat)) {
                            canvasId = catData.get("canvasId").toString();
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
                                if (content.getString("contentType").equals(tempMediaNames[0]) && (content.has("attributes") && content.getJSONArray("attributes").length() != 0) && content.getJSONArray("contentlinks").length() != 0 && content.getJSONArray("contentlinks").length() >= 3) {
                                    finalMediaData.add(content);
                                    break;
                                }
                            }
                        }
                        if (finalMediaData != null) {
                            categoryCanvasData.add(getCanvasAndComponentForCategory(mainData, tempMediaNames, cat, pageType));
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
                            categoryCanvasData.add(getCanvasAndComponentForCategory(mainData, tempMediaNames, cat, pageType));
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
                            categoryCanvasData.add(getCanvasAndComponentForCategory(mainData, tempMediaNames, cat, pageType));
                        }
                    }
                    if (response) {
                        categoryCanvasData.add(getCanvasAndComponentForCategory(mainData, tempMediaNames, cat, pageType));
                    }
                }
            } else {
                String catId = (site.equals("mcom") ? "62678" : "1001111");
                categoryCanvasData.add(getCanvasAndComponentForCategory(mainData, tempMediaNames, catId, pageType));
            }
            if (categoryCanvasData.isEmpty()) {
                org.junit.Assert.fail("ERROR - DATA: Unable to find category form site database with expected media:'" + tempMediaNames + "'");
            }
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
    public List getCanvasAndComponentForCategory(List<Map> data, String[] mediaNames, String catId, String pageType) throws Throwable {
        List finalData = new ArrayList<>();
        if (catId == null) {
            org.junit.Assert.fail("ERROR - DATA: Unable to find category form SDP with expected media:'" + mediaNames.toString() + "'");
        }
        String canvasId = null;
        for (Map cate : data)
            if (cate.get("categoryId").toString().equals(catId)) {
                canvasId = cate.get("canvasId").toString();
            }
        if (canvasId == null) {
            org.junit.Assert.fail("ERROR - DATA: Unable to find canvas_id with expected media:'" + mediaNames.toString() + "' for " + pageType + " page");
        }
        String componentId = (!Arrays.asList(mediaNames).toString().contains("PRODUCT")) ? null : String.valueOf(finalMediaData.get(0).getInt("componentID"));
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
    public List<Map> getCanvasCategoryData(ResultSet resultSet) throws Throwable {
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

    /*
        To setup DB connection
     */
    private void setupConnection() throws Throwable {
        if (statement == null) {
            try {
                connection = DBUtils.setupDBConnection();
                statement = connection.createStatement();
            } catch (Exception e) {
                System.out.println("Error occure while craeting database connection" + e.getMessage());
            }
        }
    }


    private String getStringFromArray(String[] array, String type) {
        if (type.equals("int")) {
            return Arrays.asList(array).toString().replace("[", "").replace(", ", ", ").replace("]", "");
        } else {
            return Arrays.asList(array).toString().replace("[", "'").replace(", ", "', '").replace("]", "'");
        }
    }
}
