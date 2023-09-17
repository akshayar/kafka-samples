package com.aksh.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.errors.TopicExistsException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public abstract class AbstractKafkaPublisher {
    protected void sleep(long intervalMs) {
        try {
            Thread.sleep(intervalMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void publish() throws Exception {
        Producer producer = createProducer();
        Executors.newSingleThreadExecutor().submit(()-> {
            try {
                publishData(producer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            producer.flush();
            System.out.println("Shutting down");
            producer.close();
        }));
    }

    abstract protected  Producer createProducer () throws IOException;
    abstract protected  void publishData(Producer producer) throws Exception;

    protected void createTopic(final String topic,
                                   final Properties cloudConfig, int numberOfPartitions, short replicationFactor) {
        final NewTopic newTopic = new NewTopic(topic, numberOfPartitions,replicationFactor);
        try (final AdminClient adminClient = AdminClient.create(cloudConfig)) {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (final InterruptedException | ExecutionException e) {
            // Ignore if TopicExistsException, which may be valid if topic exists
            if (!(e.getCause() instanceof TopicExistsException)) {
                throw new RuntimeException(e);
            }
        }
    }

    public Properties loadConfig(final String configFile) throws IOException {
        if (!Files.exists(Paths.get(configFile))) {
            throw new IOException(configFile + " not found.");
        }
        final Properties cfg = new Properties();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            cfg.load(inputStream);
        }
        return cfg;
    }
}
