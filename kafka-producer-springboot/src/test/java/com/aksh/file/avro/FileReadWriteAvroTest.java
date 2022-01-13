package com.aksh.file.avro;

import com.aksh.kafka.faker.JSRandomDataGenerator;
import org.junit.Test;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class FileReadWriteAvroTest {
    @Test
    public void testReadWrite() throws  Exception{
        JSRandomDataGenerator randomDataGenerator=new JSRandomDataGenerator();


        List<Properties> dataToWrite= IntStream.of(3).mapToObj(i->{
            Properties data= null;
            try {
                data = randomDataGenerator.createPayloadObject(null,null);
                System.out.println(data);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }).collect(Collectors.toList());

        FileReadWriteAvro readWriteAvro=new FileReadWriteAvro();
        String fileName=System.currentTimeMillis()+"test.avro";
        String fileSchema="src/main/resources/config/avro/com/aksh/kafka/avro/fake/TradeData.avsc";
        readWriteAvro.writeDataPropertiesObject(fileSchema,fileName, dataToWrite);

        List<Properties> readData=readWriteAvro.readDattaAsPropertiesObject(fileSchema,fileName);
        System.out.println(readData);
        IntStream.of(dataToWrite.size()-1).forEach(i->{
            Properties dataIn=dataToWrite.get(i);
            Properties dataRead=readData.get(i);
            assertEquals(dataIn.keySet(),dataRead.keySet());
            dataIn.keySet().stream().forEach(key->{
                assertEquals(dataIn.get(key).toString(),dataRead.get(key).toString());
            });
        });

    }

}