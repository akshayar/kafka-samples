package com.aksh.kafka.faker;

import com.github.javafaker.Faker;

import java.util.Date;

public class FakerHelper {
    public static Faker faker=new Faker();

    public static long getEpochInSeconds(Date date){
        return date.getTime()/1000;
    }
}
