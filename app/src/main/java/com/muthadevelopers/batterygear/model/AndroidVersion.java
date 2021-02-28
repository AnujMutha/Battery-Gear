package com.muthadevelopers.batterygear.model;

import java.util.HashMap;

public class AndroidVersion{

    static HashMap<Integer,String> map = new HashMap<>();

    public static void initDB(){
        map.put(21,"Lollipop 5.0");
        map.put(22,"Lollipop 5.1");
        map.put(23,"Marshmallow");
        map.put(24,"Nougat 7.0");
        map.put(25,"Nougat 7.1");
        map.put(26,"Oreo 8.0");
        map.put(27,"Oreo 8.1");
        map.put(28,"Pie");
        map.put(29,"Android Q");
        map.put(30,"Android R");
    }

    public static String getAndroidCodeNames(int apiCode) {
        initDB();
        if(map.get(apiCode) != null || !map.get(apiCode).equalsIgnoreCase("")){
            return map.get(apiCode);
        }
        return "Unknown";
    }

}