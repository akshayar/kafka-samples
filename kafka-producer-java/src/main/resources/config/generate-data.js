var FakerHelper = Java.type('com.aksh.kafka.faker.FakerHelper');
var Properties = Java.type('java.util.Properties');
var TimeUnit = Java.type('java.util.concurrent.TimeUnit') ;
var outValue = new Properties();
var faker= FakerHelper.faker;
var dateTimeRandom=faker.date().past(2, TimeUnit.HOURS);
var epochMilliseconds = dateTimeRandom.getTime();
var epochSeconds= FakerHelper.getEpochInSeconds(dateTimeRandom);
var buyPrice = faker.number().randomDouble(2,10,100) ;
var sellPrice = faker.number().randomDouble(2,10,100) ;
var symbol = faker.options().option("AAPL","INFY","AMZN","GOOG","IBM") ;
var tradeId=faker.numerify('##########') ;
var hhmmssdateTime=FakerHelper.getHHMMSSTime();

outValue.put('record_key',symbol);
outValue.put('event_time',hhmmssdateTime);
outValue.put('ticker',symbol);
outValue.put('price',faker.number().randomDouble(2,10,100));

/**
outValue.put('orderId',"order"+tradeId);
outValue.put('portfolioId',"port"+tradeId);
outValue.put('customerId',"cust"+tradeId);
outValue.put('symbol',symbol);
outValue.put('timestamp', epochSeconds);
outValue.put( 'orderTimestamp', epochSeconds+'');
outValue.put('description',symbol+" Description of trade");
outValue.put('traderName',symbol+" Trader");
outValue.put('traderFirm',symbol+" Trader Firm");
outValue.put( 'buy',faker.bool().bool());
outValue.put( 'currentPosition',faker.number().digits(4));
outValue.put('quantity',faker.number().randomDouble(2,10,100));
outValue.put('price',faker.number().randomDouble(2,10,100));
outValue.put( 'buyPrice',buyPrice) ;
outValue.put( 'sellPrice',sellPrice);
outValue.put( 'profit',sellPrice-buyPrice);
*/