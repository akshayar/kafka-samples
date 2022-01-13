package com.aksh.kafka;

import com.aksh.kafka.avro.fake.TradeData;
import com.aksh.kafka.faker.JSRandomDataGenerator;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import org.apache.commons.beanutils.BeanUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

public class KafkaPublisherTradeDataTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAvroSerDe() throws Exception {
        JSRandomDataGenerator randomDataGenerator=new JSRandomDataGenerator();
        Properties data=randomDataGenerator.createPayloadObject(null,null);
        System.out.println(data);
        TradeData dataTrade=new TradeData();
        data.entrySet().stream().forEach(entry->{
            try {
                BeanUtils.setProperty(dataTrade,entry.getKey()+"",entry.getValue());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        System.out.println(dataTrade);
        byte[] avroData=AvroSerdes.get(TradeData.class).serializer().serialize("test",dataTrade);
        System.out.println(new String(avroData));


    }
}