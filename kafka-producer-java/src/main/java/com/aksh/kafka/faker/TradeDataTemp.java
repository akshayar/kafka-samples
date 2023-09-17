package com.aksh.kafka.faker;

import lombok.Data;
import lombok.ToString;

import java.util.concurrent.TimeUnit;
@Data  @ToString
public class TradeDataTemp {


//    @Getter(AccessLevel.NONE)
//    @Setter(AccessLevel.NONE)
//    private Date dateTime=faker.date().past(2, TimeUnit.HOURS);
    private String tradeId=FakerHelper.faker.numerify("##########");
    private String symbol=FakerHelper.faker.regexify("[A-Z]{4}");
    private double quantity=FakerHelper.faker.number().randomDouble(2,10,100);
    private double price=FakerHelper.faker.number().randomDouble(2,10,100);;
    private long timestamp=FakerHelper.faker.date().past(2, TimeUnit.HOURS).getTime();
    private String description=FakerHelper.faker.regexify("This is a description [a-z1-9]{10}");
    private String traderName=FakerHelper.faker.regexify("Trader [a-z1-9]{10}");
    private String traderFirm=FakerHelper.faker.regexify("Trader Firm [a-z1-9]{10}");
    private String orderId=FakerHelper.faker.numerify("##########");
    private String portfolioId=FakerHelper.faker.numerify("##########");
    private String customerId=FakerHelper.faker.numerify("##########");
    private boolean buy=FakerHelper.faker.bool().bool();
    private String orderTimestamp=timestamp+"";
    private String currentPosition=FakerHelper.faker.number().digits(4);
    private double buyPrice=FakerHelper.faker.number().randomDouble(2,10,100);
    private double sellPrice=FakerHelper.faker.number().randomDouble(2,10,100);
    private double profit=sellPrice-buyPrice;
}
