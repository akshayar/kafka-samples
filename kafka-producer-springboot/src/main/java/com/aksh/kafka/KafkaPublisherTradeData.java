package com.aksh.kafka;

import com.aksh.kafka.avro.fake.TradeData;
import com.aksh.kafka.fake.FakeRandomGenerator;
import com.mitchseymour.kafka.serialization.avro.AvroSerdes;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;

@Component
public class KafkaPublisherTradeData extends AbstractKafkaPublisher{

    @Value("${intervalMs:100}")
    int intervalMs=100;

    @Value("${topicNameTradeData:data-stream-ingest-trade}")
    private String topicName;

    @Value("${numberOfPartitions:2}")
    private int numberOfPartitions;

    @Value("${replicationFactor:2}")
    private short replicationFactor;

    @Value("${kafkaConfigFile:src/main/resources/kafka.properties}")
    private String kafkaConfigFile;


    @Autowired
    private FakeRandomGenerator fakeRandomGenerator;

    @Value("${dataGeneratorScript:src/main/resources/generate-data.js}")
    private String dataGeneratorScript;

    @Value("${maxMessages:-1}")
    private int maxMessages;

    @PostConstruct
    void init() throws Exception {
        publish();
    }


    @Override
    protected Producer createProducer() throws IOException {
        Properties kafkaConfig=loadConfig(kafkaConfigFile);
        createTopic(topicName,kafkaConfig,numberOfPartitions,replicationFactor);
        // Add additional properties.
        kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        Producer producer = new KafkaProducer(kafkaConfig,new StringSerializer(),AvroSerdes.get(TradeData.class).serializer());
        return producer;
    }

    @Override
    protected void publishData(Producer producer) throws Exception {

        int i = 0;
        while (true && (maxMessages== -1 || maxMessages > i )) {
            TradeData tradeData=(TradeData) fakeRandomGenerator.createPayloadObject(TradeData.class,null);


            System.out.println("Kafka Push TradeData: " + tradeData);
            producer.send(new ProducerRecord<String, TradeData>(topicName, i+"", tradeData), new Callback() {

                public void onCompletion(RecordMetadata m, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
                    }
                }
            });
            sleep(intervalMs);
            i++;
        }
    }




}
