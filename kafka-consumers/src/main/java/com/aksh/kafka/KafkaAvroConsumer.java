package com.aksh.kafka;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;

import java.util.Arrays;
import java.util.Properties;

public class KafkaAvroConsumer {
    private static String topicName = "data-stream-ingest-3";

    public static void main(String[] args) throws Exception{
        Properties props = Common.loadConfig("src/main/resources/kafka.properties");

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group1");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("schema.registry.url", "http://localhost:8081");


        String topic = topicName;
        final Consumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(props);
        consumer.subscribe(Arrays.asList(topic));

        try {
            while (true) {
                ConsumerRecords<String, GenericRecord> records = consumer.poll(100);
                for (ConsumerRecord<String, GenericRecord> record : records) {
                    System.out.printf("offset = %d, key = %s, value = %s \n", record.offset(), record.key(), record.value());
                }
            }
        } finally {
            consumer.close();
        }
    }
}
