package com.aksh.kafka;

import com.aksh.kafka.faker.JSRandomDataGenerator;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


public class KafkaPublisherGenericRecord extends AbstractKafkaPublisher{

    @Value("${intervalMs:100}")
    int intervalMs=100;

    @Value("${KafkaPublisherGenericRecord.topicName:data-stream-ingest}")
    private String topicName;

    @Value("${numberOfPartitions:2}")
    private int numberOfPartitions;

    @Value("${replicationFactor:2}")
    private short replicationFactor;

    @Value("${kafkaConfigFile:src/main/resources/config/kafka.properties}")
    private String kafkaConfigFile;

    @Autowired
    private JSRandomDataGenerator jsRandomDataGenerator;


    @Value("${KafkaPublisherGenericRecord.fakeGeneratorJSScript:src/main/resources/config/generate-data.js}")
    private String dataGeneratorScript;

    @Value("${maxMessages:-1}")
    private int maxMessages;

    @Value("${KafkaPublisherGenericRecord.avroSchemaPath:src/main/resources/config/avro/com/aksh/kafka/avro/fake/TradeData.avsc}")
    private String avroSchemaPath;

    @Value("${KafkaPublisherGenericRecord.serializationMechanism:AVRO}")
    private String serializationMechanism;

    Schema avroSchema;

    @PostConstruct
    void init() throws Exception {
        if(isAvro()){
            String schema= FileCopyUtils.copyToString(new FileReader(avroSchemaPath));
            Schema.Parser parser=new Schema.Parser();
            avroSchema=parser.parse(schema);
        }
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
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        if(isAvro()){
            kafkaConfig.put("schema.registry.url", "http://127.0.0.1:8081");
            kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        }else{
            kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        }


        Producer producer = new KafkaProducer(kafkaConfig);
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

    private void publishAvro(Producer producer, int i) {
        GenericRecord avroRecord=new GenericData.Record(avroSchema);
        Properties generatedValues= jsRandomDataGenerator.evaluateFromJS(dataGeneratorScript);
        generatedValues.entrySet().stream().forEach(entry->{
            avroRecord.put(entry.getKey()+"",entry.getValue());
        });

        System.out.println("Kafka Push AVRO GenericRecord: " + avroRecord);
        try{
            producer.send(new ProducerRecord<String, GenericRecord>(topicName, i +"", avroRecord), new Callback() {

                public void onCompletion(RecordMetadata m, Exception e) {
                    if (e != null) {
                        e.printStackTrace();
                    } else {
                        System.out.printf("Produced record to topic %s partition [%d] @ offset %d%n", m.topic(), m.partition(), m.offset());
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();

        }


    }

    protected void publishJson(Producer producer, int i) throws Exception{
        String json= jsRandomDataGenerator.createPayload(null,dataGeneratorScript);

        System.out.println("Kafka JSON Push: " + json);
        producer.send(new ProducerRecord<String, String>(topicName, i +"", json), new Callback() {

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
