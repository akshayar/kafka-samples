package com.aksh.kafka;

import com.aksh.kafka.avro.fake.TradeData;
import com.aksh.kafka.faker.FakeRandomGenerator;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;


public class KafkaPublisherTradeData extends AbstractKafkaPublisher{

    @Value("${intervalMs:100}")
    int intervalMs=100;

    @Value("${KafkaPublisherTradeData.topicName:data-stream-ingest-trade}")
    private String topicName;

    @Value("${numberOfPartitions:2}")
    private int numberOfPartitions;

    @Value("${replicationFactor:2}")
    private short replicationFactor;

    @Value("${kafkaConfigFile:src/main/resources/config/kafka.properties}")
    private String kafkaConfigFile;


    @Autowired
    private FakeRandomGenerator fakeRandomGenerator;

    @Value("${KafkaPublisherTradeData.fakeGeneratorJSScript:src/main/resources/generate-data.js}")
    private String dataGeneratorScript;

    @Value("${maxMessages:-1}")
    private int maxMessages;

    @Value("${KafkaPublisherTradeData.serializationMechanism:AVRO}")
    private String serializationMechanism;

    @PostConstruct
    void init() throws Exception {
        publish();
    }

    private  boolean isAvro(){
        return "AVRO".equalsIgnoreCase(serializationMechanism);
    }

    @Override
    protected Producer createProducer() throws IOException {
        Properties kafkaConfig=loadConfig(kafkaConfigFile);
        createTopic(topicName,kafkaConfig,numberOfPartitions,replicationFactor);
        // Add additional properties.
        kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        Producer producer=null;
        if(isAvro()){
            producer = new KafkaProducer(kafkaConfig,new StringSerializer(),AvroSerdes.get(TradeData.class).serializer());
        }else{
            kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringSerializer");
            kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer(kafkaConfig);
        }
        return producer;
    }

    @Override
    protected void publishData(Producer producer) throws Exception {

        int i = 0;
        while (true && (maxMessages== -1 || maxMessages > i )) {
            if(isAvro()){
                publishAvro(producer,i);
            }else{
                publishJson(producer,i);
            }
            sleep(intervalMs);
            i++;
        }
    }

    private void publishAvro(Producer producer, int i) throws Exception {
        TradeData tradeData=(TradeData) fakeRandomGenerator.createPayloadObject(TradeData.class,null);
        System.out.println("Kafka AVRO Push TradeData: " + tradeData);
        producer.send(new ProducerRecord<String, TradeData>(topicName, i +"", tradeData), new Callback() {

            public void onCompletion(RecordMetadata m, Exception e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
                }
            }
        });
    }
    protected void publishJson(Producer producer, int i) throws Exception{
        String tradeData= fakeRandomGenerator.createPayload(TradeData.class,dataGeneratorScript);

        System.out.println("Kafka JSON Push TradeData: " + tradeData);
        producer.send(new ProducerRecord<String, String>(topicName, i +"", tradeData), new Callback() {

            public void onCompletion(RecordMetadata m, Exception e) {
                if (e != null) {
                    e.printStackTrace();
                } else {
                    System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
                }
            }
        });
    }



}
