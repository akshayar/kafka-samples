var FakerHelper = Java.type('com.aksh.kafka.fake.FakerHelper');
var Properties = Java.type('java.util.Properties');
var TimeUnit = Java.type('java.util.concurrent.TimeUnit') ;
var outValue = new Properties();
var faker= FakerHelper.faker;
var timestamp = FakerHelper.faker.date().past(2, TimeUnit.HOURS).getTime() ;
var buyPrice = FakerHelper.faker.number().randomDouble(2,10,100) ;
var sellPrice = FakerHelper.faker.number().randomDouble(2,10,100) ;

outValue.put('tradeId',FakerHelper.faker.numerify('##########'));
outValue.put('symbol',FakerHelper.faker.regexify('[A-Z]{4}'));
outValue.put('quantity',FakerHelper.faker.number().randomDouble(2,10,100));
outValue.put('price',FakerHelper.faker.number().randomDouble(2,10,100));
outValue.put('timestamp', timestamp);
outValue.put('description',FakerHelper.faker.regexify('This is a description [a-z1-9]{10}'));
outValue.put('traderName',FakerHelper.faker.regexify('Trader [a-z1-9]{10}'));
outValue.put('traderFirm',FakerHelper.faker.regexify('Trader Firm [a-z1-9]{10}'));
outValue.put('orderId',FakerHelper.faker.numerify('##########'));
outValue.put('portfolioId',FakerHelper.faker.numerify('##########'));
outValue.put('customerId',FakerHelper.faker.numerify('##########'));
outValue.put( 'buy',FakerHelper.faker.bool().bool());
outValue.put( 'orderTimestamp', timestamp+'');
outValue.put( 'currentPosition',FakerHelper.faker.number().digits(4));
outValue.put( 'buyPrice',buyPrice) ;
outValue.put( 'sellPrice',sellPrice);
outValue.put( 'profit',sellPrice-buyPrice);


