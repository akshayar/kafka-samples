from kafka import KafkaConsumer
import sys
print("\nArguments 1:", sys.argv[1])
print("\nArguments 2:", sys.argv[2])
print("\nArguments 3:", sys.argv[3])
streamName = sys.argv[1]
groupName = sys.argv[2]
bootstraServers = sys.argv[3]
# To consume latest messages and auto-commit offsets
consumer = KafkaConsumer(streamName,
                         group_id=groupName,
                         bootstrap_servers=[bootstraServers])
                         
for message in consumer:
    # message value and key are raw bytes -- decode if necessary!
    # e.g., for unicode: `message.value.decode('utf-8')`
    print ("%s:%d:%d: key=%s value=%s" % (message.topic, message.partition,
                                          message.offset, message.key,
                                          message.value))
