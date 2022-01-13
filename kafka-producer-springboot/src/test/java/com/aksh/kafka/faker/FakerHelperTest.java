package com.aksh.kafka.faker;

import org.junit.Test;

import static org.junit.Assert.*;

public class FakerHelperTest {

    @Test
    public void test(){
        System.out.println(FakerHelper.faker.options().option("AAPL","INFY","IBM"));;
    }

}