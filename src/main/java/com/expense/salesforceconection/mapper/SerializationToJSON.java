package com.expense.salesforceconection.mapper;

//TODO clear text

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerializationToJSON {

    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String COLON = ":";

    public static String serializeUsername(String username) {
        return String.format("{\"body\": \"%s\"}", username);
    }

    public static String serializeUsernameAndPassword(String username, String password) {
//        Map<String, String> map = new HashMap<>();
//        map.put("username", username);
//        map.put("password", password);
//        return String.format("{\"body\": %s", map.toString());
        return String.format("{\"body\": \"%1$s # %2$s\"}", username, password);
    }

//    public static String serializeMap(Map<String,String> stringMap){
//        List<String> elements = new ArrayList<>();
//        for (String key: stringMap.keySet()){
//            String element = String.format("\"%1$s\": \"%2$s\" ,", key, stringMap.get(key));
//            elements.add(element);
//        }
//
//
//        String result = String.format("{\"body\": {      "
//    }

    public static void main(String[] args) {
        System.out.println(serializeUsernameAndPassword("bla@bal.com", "123"));
    }
}
