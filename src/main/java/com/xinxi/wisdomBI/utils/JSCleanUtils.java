package com.xinxi.wisdomBI.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 蒲月理想
 */
@Component
public class JSCleanUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static String cleanJsonString(String jsonString) {
        // 去除多余的逗号
        String cleanedJson = jsonString.replaceAll(",+(?=\\s*[]}])", "");
        // 去除首尾空白
        cleanedJson = cleanedJson.trim();
        // 去除注释 //xxxx
        cleanedJson = cleanedJson.replaceAll("//.*", "");
        // 将单引号替换为双引号
        cleanedJson = cleanedJson.replaceAll("'", "\"");
        // 保留双引号内的内容
        cleanedJson = cleanedJson.replaceAll("\"[^\"]*?([\\u4e00-\\u9fa5]+)[^\"]*?\"", "$0");
        // 添加双引号到键
        cleanedJson = cleanedJson.replaceAll("\\b(\\w+)\\b(?=\\s*:)", "\"$1\"");
        // 去除多余的空格
        cleanedJson = cleanedJson.replaceAll("\\s+", " ");
       // 去除引号里面多余的空白
        cleanedJson = removeSpacesInQuotes(cleanedJson);
        // 格式化：
        return formatJson(cleanedJson);
    }
    public static String formatJson(String json) {
        return json.replaceAll("(?<=\\{)\\s*\\{", "{")
                .replaceAll("(?<=\\[)\\s*\\[", "[")
                .replaceAll("\\\\\"", "\"");
    }
    public static String removeSpacesInQuotes(String input) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            // 去除双引号内的空格
            String cleanContent = matcher.group(1).replaceAll("\\s+", "");
            matcher.appendReplacement(sb, "\"" + cleanContent + "\"");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
    public static boolean isValidJson(String json) {
        try {
            // 尝试解析为对象
            new JSONObject(json);
        } catch (JSONException e1) {
            try {
                // 尝试解析为数组
                new JSONArray(json);
            } catch (JSONException e2) {
                // 若两者都抛出异常，则不是有效 JSON
                return false;
            }
        }
        // 解析成功
        return true;
    }
    // 测试：
    public static void main(String[] args) {
       // 首先测试错误的
       String jsonString = "{\n" +
               "    \"  title\": {\n" +
               "        \"text\": \"用户增长情况\"\n" +
               "    },\n" +
               "    \"  tooltip\": {\n" +
               "        \"trigger\": \"axis\"\n" +
               "    },\n" +
               "    \"  legend\": {\n" +
               "        \"data\": [\"用户数\"]\n" +
               "    },\n" +
               "    \"grid\": {\n" +
               "        \"left\": \"3%\",\n" +
               "        \"right\": \"4%\",\n" +
               "        \"bottom\": \"3%\",\n" +
               "        \"containLabel\": true\n" +
               "    },\n" +
               "    \"toolbox\": {\n" +
               "        \"feature\": {\n" +
               "            \"saveAsImage\": {}\n" +
               "        }\n" +
               "    },\n" +
               "    \"xAxis\": {\n" +
               "        \"type\": \"category\",\n" +
               "        \"boundaryGap\": false,\n" +
               "        \"data\": [\"1号\", \"2号\", \"3号\", \"4号\", \"5号\", \"6号\", \"7号\"]\n" +
               "    },\n" +
               "    \"yAxis\": {\n" +
               "        \"type\": \"value\"\n" +
               "    },\n" +
               "    \"series\": [\n" +
               "        {\n" +
               "            \"name\": \"用户数\",\n" +
               "            \"type\": \"line\",\n" +
               "            \"data\": [10, 20, 30, 90, 0, 10, 20],\n" +
               "            \"areaStyle\": {}\n" +
               "        }\n" +
               "    ]\n" +
               "}";

        System.out.println(isValidJson(jsonString));
        String cleanedJsonString = cleanJsonString(jsonString);
       System.out.println(cleanedJsonString);
        System.out.println(isValidJson(jsonString));
    }
}
