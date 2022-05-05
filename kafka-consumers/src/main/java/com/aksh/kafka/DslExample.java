package com.aksh.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class DslExample {
    public static void main(String[] args) {
        try{
            StreamsBuilder builder =new StreamsBuilder();
            KStream<String,String> stream=builder.stream("data-stream-ingest-3");
            stream.foreach(
                    (key,value)->{
                        System.out.println("DSL Hello:"+value);
                    }
            );
            Properties config = Common.loadConfig("src/main/resources/kafka.properties");

            config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


            KafkaStreams streams=new KafkaStreams(builder.build(),config);
            streams.start();
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        }catch (Exception e){
            e.printStackTrace();
        }


    }

}
