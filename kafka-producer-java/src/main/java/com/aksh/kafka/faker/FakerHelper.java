package com.aksh.kafka.faker;

import com.github.javafaker.Faker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FakerHelper {
    public static Faker faker=new Faker();

    public static long getEpochInSeconds(Date date){
        return date.getTime()/1000;
    }

    public static String getHHMMSSTime(){
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date(System.currentTimeMillis()));
    }
}
