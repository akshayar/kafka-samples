package com.aksh.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class BeanConfiguration {
    @Value("${publisherType:KafkaPublisherGenericRecord}")
    private String publisherType;

    @Bean
    public AbstractKafkaPublisher createPublisher(){
        if("KafkaPublisherGenericRecord".equalsIgnoreCase(publisherType)){
            return new KafkaPublisherGenericRecord();
        }else{
            return new KafkaPublisherTradeData();
        }
    }

}
