package com.aksh.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.Record;

import java.util.Properties;

public class ProcessorApiExample {
    public static void main(String[] args) throws  Exception{
        Topology topology=new Topology();
        topology.addSource("testTopic","data-stream-ingest-3");
        topology.addProcessor("processor",SayHelloProcessor::new,"testTopic");
        Properties config = Common.loadConfig("src/main/resources/kafka.properties");
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "dev1");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        KafkaStreams streams=new KafkaStreams(topology,config);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public static class SayHelloProcessor implements Processor<String,String,Void,Void>{

        @Override
        public void init(ProcessorContext<Void, Void> context) {

        }

        @Override
        public void process(Record<String, String> record) {
            System.out.println("(Processor API) Hello, " + record.value());
        }

        @Override
        public void close() {

        }
    }
}
