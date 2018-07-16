package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by 林成峰 on 2018/4/7.
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);


    private static ObjectMapper objectMapper = new ObjectMapper();

    static {

        //對象的所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);

        //取消默認轉換timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        //忽略空Bean轉json的錯誤
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        //所有的日期格式都統以為以下的樣式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //忽略在json字符串中存在，但是在java對象中不存在對應屬性的情況，防止錯誤。
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> String obj2String(T obj){

        if(obj == null){

            return null;
        }

        try {

            return obj instanceof String ? (String)obj : objectMapper.writeValueAsString(obj);

        } catch (Exception e) {

            logger.warn("Parse Object to String error", e);

            return null;
        }
    }

    public static <T> String obj2StringPretty(T obj){

        if(obj == null){

            return null;
        }

        try {

            return obj instanceof String ? (String)obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);

        } catch (Exception e) {

            logger.warn("Parse Pretty Object to String error", e);

            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<T> clazz){

        if(StringUtils.isEmpty(str) || clazz == null){

            return null;
        }

        try {

            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str, clazz);

        } catch (Exception e) {

            logger.warn("Parse String to Object error", e);

            return null;
        }
    }

    public static <T> T string2Obj(String str, TypeReference<T> typeReference){

        if(StringUtils.isEmpty(str) || typeReference == null){

            return null;
        }

        try {

            return (T)(typeReference.getType().equals(String.class)? str : objectMapper.readValue(str, typeReference));

        } catch (Exception e) {

            logger.warn("Parse String to Object error", e);

            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?>... elementClass){

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClass);

        try {

            return objectMapper.readValue(str, javaType);

        } catch (Exception e) {

            logger.warn("Parse String to Object error", e);

            return null;
        }
    }

    public static void main(String[] args) {

        User user = new User();
        user.setId(1);
        user.setEmail("test");

        User user2 = new User();
        user2.setId(2);
        user2.setEmail("test2");

        String userJson = JsonUtil.obj2String(user);

        String userJsonPretty = JsonUtil.obj2StringPretty(user);

        logger.info("userJson:" + userJson);
        logger.info("userJsonPretty:" + userJsonPretty);

        User user1 = JsonUtil.string2Obj(userJson, User.class);

        List<User> userList = Lists.newArrayList();
        userList.add(user);
        userList.add(user2);

        String userListStr = JsonUtil.obj2StringPretty(userList);

        logger.info("===================");
        logger.info(userListStr);

        List<User> userListObj1 = JsonUtil.string2Obj(userListStr, new TypeReference<List<User>>() {});

        List<User> userListObj2 = JsonUtil.string2Obj(userListStr, List.class, User.class);

        System.out.println("end");
    }
}
