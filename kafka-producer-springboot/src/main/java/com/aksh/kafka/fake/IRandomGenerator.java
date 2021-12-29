package com.aksh.kafka.fake;

public interface IRandomGenerator<T> {
    public String createPayload(Class<T> type,String templateFile) throws Exception;
    public  T createPayloadObject(Class<T> type,String templateFile) throws Exception;
}
