package cn.yanss.m.cookhouse.api.utils;

import cn.yanss.m.util.LogUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class MapperUtils {

    private static ObjectMapper mapper = null;

    static {
        mapper = new ObjectMapper();
    }

    private MapperUtils() {
    }

    /**
     * javaBean、列表数组转换为json字符串
     */
    public static String obj2json(Object obj) {
        try{
            return mapper.writeValueAsString(obj);
        }catch (JsonProcessingException e){
            LogUtils.create().methodName("obj2json").message(e.getMessage()).error();
            return null;
        }
    }

    /**
     * javaBean、列表数组转换为json字符串,忽略空值
     */
    public static String obj2jsonIgnoreNull(Object obj) throws JsonProcessingException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(obj);
    }

    /**
     * json 转JavaBean
     */

    public static <T> T json2pojo(String jsonString, Class<T> clazz) throws IOException {
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return mapper.readValue(jsonString, clazz);
    }

    /**
     * json字符串转换为map
     */
    public static Map<String, Object> json2map(String jsonString) throws IOException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.readValue(jsonString, Map.class);
    }

    /**
     * json字符串转换为map
     */
    public static <T> Map<String, T> json2map(String jsonString, Class<T> clazz) throws IOException {
        Map<String, Map<String, Object>> map = mapper.readValue(jsonString, new TypeReference<Map<String, T>>() {
        });
        Map<String, T> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), map2pojo(entry.getValue(), clazz));
        }
        return result;
    }

    /**
     * 将以json方式提交的字符串封装成Bean对象
     *
     * @param json json对象
     * @param cls  bean的class
     * @return Object
     */
    public static <T> T createBean(String json, Class<T> cls) {
        if (json == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mapper.setDateFormat(formatter);
        try {
            return mapper.readValue(json, cls);
        } catch (IOException e) {
            LogUtils.create().methodName("MapperUtils-->createBean").message(e.getMessage()).error();
            return null;
        }
    }

    /**
     * 深度转换json成map
     *
     * @param json
     * @return
     */
    public static Map<String, Object> json2mapDeeply(String json) throws Exception {
        return json2MapRecursion(json);
    }

    /**
     * 把json解析成list，如果list内部的元素存在jsonString，继续解析
     *
     * @param json
     * @return
     * @throws Exception
     */
    private static List<Object> json2ListRecursion(String json) throws Exception {
        if (json == null) {
            return Collections.emptyList();
        }
        List<Object> list = mapper.readValue(json, List.class);
        for (Object obj : list) {
            if (null != obj && obj instanceof String) {
                String str = (String) obj;
                if (str.startsWith("[")) {
                    json2ListRecursion(str);
                } else if (obj.toString().startsWith("{")) {
                    json2MapRecursion(str);
                }
            }
        }

        return list;
    }

    /**
     * 把json解析成map，如果map内部的value存在jsonString，继续解析
     *
     * @param json
     * @return
     * @throws Exception
     */
    private static Map<String, Object> json2MapRecursion(String json) throws Exception {
        if (json == null) {
            return null;
        }

        Map<String, Object> map = mapper.readValue(json, Map.class);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object obj = entry.getValue();
            if (obj != null && obj instanceof String) {
                String str = ((String) obj);

                if (str.startsWith("[")) {
                    List<?> list = json2ListRecursion(str);
                    map.put(entry.getKey(), list);
                } else if (str.startsWith("{")) {
                    Map<String, Object> mapRecursion = json2MapRecursion(str);
                    map.put(entry.getKey(), mapRecursion);
                }
            }
        }

        return map;
    }

    /**
     * 与javaBean json数组字符串转换为列表
     */
    public static <T> List<T> json2list(String jsonArrayStr, Class<T> clazz) {
        try{
            JavaType javaType = getCollectionType(ArrayList.class, clazz);
            return mapper.readValue(jsonArrayStr, javaType);
        }catch (IOException e){
            LogUtils.create().methodName("json2list").message(e.getMessage()).error();
            return Collections.EMPTY_LIST;
        }

    }

    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    /**
     * map  转JavaBean
     */
    public static <T> T map2pojo(Map map, Class<T> clazz) {
        return mapper.convertValue(map, clazz);
    }

    /**
     * map 转json
     *
     * @param map
     * @return
     */
    public static String mapToJson(Map map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            LogUtils.create().methodName("MapperUtils-->mapToJson").message(e.getMessage()).error();
        }
        return "";
    }

    /**
     * map  转JavaBean
     */
    public static <T> T obj2pojo(Object obj, Class<T> clazz) {
        return mapper.convertValue(obj, clazz);
    }
}
