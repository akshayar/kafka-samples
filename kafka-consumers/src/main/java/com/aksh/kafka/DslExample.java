package com.aksh.kafka;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class DslExample {
    public static void main(String[] args) {
        try{
            Properties config = Common.loadConfig("src/main/resources/kafka.properties");

            config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            String topicName=config.get("source.topic").toString();
            StreamsBuilder builder =new StreamsBuilder();
            KStream<String,String> stream=builder.stream("topicName");
            stream.foreach(
                    (key,value)->{
                        System.out.println("DSL Hello:"+value);
                        try{
                            Thread.sleep(10000);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
            );


            KafkaStreams streams=new KafkaStreams(builder.build(),config);
            streams.setUncaughtExceptionHandler(new StreamsUncaughtExceptionHandler() {
                @Override
                public StreamThreadExceptionResponse handle(Throwable exception) {
                    System.out.println("StreamThreadExceptionResponse");
                    exception.printStackTrace();
                    return StreamThreadExceptionResponse.SHUTDOWN_APPLICATION;
                }
            });
            streams.start();
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        }catch (Exception e){
            e.printStackTrace();
        }


    }



}
