package com.aksh.kafka;

import com.aksh.kafka.avro.fake.TradeData;
import com.aksh.kafka.faker.FakeRandomGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;


public class KafkaPublisherTradeData extends AbstractKafkaPublisher{
    private ObjectMapper mapper=new ObjectMapper();

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

    @Value("${partitionKey:}")
    private String partitionKey;

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
            sleep(intervalMs);
            if(isAvro()){
                publishAvro(producer,i);
            }else{
                publishJson(producer,i);
            }

            i++;
        }
    }

    private void publishAvro(Producer producer, int i) throws Exception {
        TradeData tradeData=(TradeData) fakeRandomGenerator.createPayloadObject(TradeData.class,null);

        String key=i+"";
        if(StringUtils.isEmpty(partitionKey))
            { key=tradeData.getSymbol();}
        else
            {key= tradeData.get(partitionKey)+"";}
        System.out.println("Kafka AVRO Push TradeData: " + tradeData+",key:"+key);
        producer.send(new ProducerRecord<String, TradeData>(topicName, key, tradeData), new Callback() {

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
        String key=i+"";
        if(!StringUtils.isEmpty(partitionKey))
        {
            JsonNode node=mapper.readTree(tradeData);key= node.get(partitionKey).asText();
        }

        System.out.println("key:"+key+"Kafka JSON Push TradeData: " + tradeData);
        producer.send(new ProducerRecord<String, String>(topicName, key, tradeData), new Callback() {

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
