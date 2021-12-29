package com.aksh.kafka;

import com.aksh.kafka.fake.JSRandomDataGenerator;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Component
public class KafkaPublisherGenericRecord extends AbstractKafkaPublisher{

    @Value("${intervalMs:100}")
    int intervalMs=100;

    @Value("${topicName:data-stream-ingest}")
    private String topicName;

    @Value("${numberOfPartitions:2}")
    private int numberOfPartitions;

    @Value("${replicationFactor:2}")
    private short replicationFactor;

    @Value("${kafkaConfigFile:src/main/resources/kafka.properties}")
    private String kafkaConfigFile;

    @Autowired
    private JSRandomDataGenerator jsRandomDataGenerator;


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
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        Producer producer = new KafkaProducer(kafkaConfig);
        return producer;
    }


    @Override
    protected void publishData(Producer producer) throws Exception {

        String schema= FileCopyUtils.copyToString(new FileReader("src/main/avro/com/aksh/kafka/avro/fake/TradeData.avsc"));
        Schema.Parser parser=new Schema.Parser();
        Schema avroSchema=parser.parse(schema);

        int i = 0;
        while (true && (maxMessages== -1 || maxMessages > i )) {
            GenericRecord avroRecord=new GenericData.Record(avroSchema);
            Properties generatedValues= jsRandomDataGenerator.evaluateFromJS(dataGeneratorScript);
            generatedValues.entrySet().stream().forEach(entry->{
                avroRecord.put(entry.getKey()+"",entry.getValue());
            });

            System.out.println("Kafka Push: " + avroRecord);
            producer.send(new ProducerRecord<String, GenericRecord>(topicName, i+"", avroRecord), new Callback() {

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
