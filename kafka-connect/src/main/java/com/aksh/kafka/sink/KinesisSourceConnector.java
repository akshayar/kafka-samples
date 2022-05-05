package com.aksh.kafka.sink;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KinesisSourceConnector extends SourceConnector {
    public static final String TOPIC_CONFIG = "topic";
    public static final String KINESIS_CONFIG = "kinesisStream";
    public static final String TASK_BATCH_SIZE_CONFIG = "batch.size";
    public static final String REGION_CONFIG = "region";
    public static final int DEFAULT_TASK_BATCH_SIZE = 2000;

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(KINESIS_CONFIG, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "Kinesis Stream Name")
            .define(TOPIC_CONFIG, ConfigDef.Type.LIST, ConfigDef.Importance.HIGH, "The topic to publish data to")
            .define(TASK_BATCH_SIZE_CONFIG, ConfigDef.Type.INT, DEFAULT_TASK_BATCH_SIZE, ConfigDef.Importance.LOW,
                    "The maximum number of records the Source task can read from file one time")
            .define(REGION_CONFIG, ConfigDef.Type.STRING, null, ConfigDef.Importance.HIGH, "Region");;

    private String streamName;
    private String topic;
    private int batchSize;
    private String region;

    @Override
    public void start(Map<String, String> props) {
        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        streamName = parsedConfig.getString(KINESIS_CONFIG);
        List<String> topics = parsedConfig.getList(TOPIC_CONFIG);
        if (topics.size() != 1) {
            throw new ConfigException("'topic' in FileStreamSourceConnector configuration requires definition of a single topic");
        }
        topic = topics.get(0);
        batchSize = parsedConfig.getInt(TASK_BATCH_SIZE_CONFIG);
        region=parsedConfig.getString(REGION_CONFIG);

    }

    @Override
    public Class<? extends Task> taskClass() {
        return KinesisStreamSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {

        ArrayList<Map<String, String>> configs = new ArrayList<>();
        // Only one input stream makes sense.
        Map<String, String> config = new HashMap<>();
        if (streamName != null)
            config.put(KINESIS_CONFIG, streamName);

        if (region != null)
            config.put(REGION_CONFIG, region);

        config.put(TOPIC_CONFIG, topic);
        config.put(TASK_BATCH_SIZE_CONFIG, String.valueOf(batchSize));
        configs.add(config);
        return configs;

    }

    @Override
    public void stop() {

    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }
}
