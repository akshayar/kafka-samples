package com.aksh.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KafkaEvent;
import com.google.gson.Gson;

import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

public class MskHandler implements RequestHandler<KafkaEvent, String> {
    private static final String SUCCESS="SUCCESS";
    private static final Gson gson=new Gson();

    @Override
    public String handleRequest(KafkaEvent input, Context context) {
        context.getLogger().log("EventSource:"+input.getEventSource()+",EventSourceArn"+input.getEventSourceArn());
        Optional.ofNullable(input.getRecords()).orElse(Collections.emptyMap()).forEach((key,value)->{
            context.getLogger().log("Key:"+key+",value:"+value);
            Optional.ofNullable(value).orElse(Collections.emptyList()).forEach(r->processSingleRecord(context,r));
        });
        return SUCCESS;
    }

    private void processSingleRecord(Context context,KafkaEvent.KafkaEventRecord record){
        context.getLogger().log(gson.toJson(record));
        context.getLogger().log("Value:"+base64Decode(record));
    }

    private byte[] base64Decode(KafkaEvent.KafkaEventRecord kafkaEventRecord) {
        return Base64.getDecoder().decode(kafkaEventRecord.getValue().getBytes());
    }
}
