package com.aksh.kafka;

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public class KafkaAvroConsumer {
    private static String TOPIC_NAME = "data-stream-ingest-3";

    public static void main(String[] args) throws Exception{
        Properties props = Common.loadConfig("src/main/resources/kafka-avro.properties");
        String topicName= Optional.ofNullable(props.get("source.topic")).map(Objects::toString).orElse(TOPIC_NAME);
        final Consumer<String, GenericRecord> consumer = new KafkaConsumer<String, GenericRecord>(props);
        consumer.subscribe(Arrays.asList(topicName));

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
