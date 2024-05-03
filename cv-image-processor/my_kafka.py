import json
import threading

from kafka import KafkaProducer, KafkaConsumer

from main import process


def create_producer(bootstrap_servers, acks):
    return KafkaProducer(bootstrap_servers=bootstrap_servers,
                         value_serializer=lambda x: json.dumps(x).encode('utf-8'),
                         acks=acks)


def consume_messages(consumer_id, bootstrap_servers, topic_name, group_id):
    consumer = KafkaConsumer(topic_name,
                             group_id=group_id,
                             bootstrap_servers=bootstrap_servers,
                             auto_offset_reset='earliest',
                             value_deserializer=lambda x: json.loads(x.decode('utf-8')))
    print(f"Consumer {consumer_id} is starting.")
    for message in consumer:
        print(f"Consumer {consumer_id} received: {message.value}")
        data = message.value
        try:
            process(message)
        except KeyError:
            print('Received message is invalid')


def start_consumers(n, bootstrap_servers, topic_name, group_id):
    threads = []
    for i in range(n):
        t = threading.Thread(target=consume_messages, args=(i, bootstrap_servers, topic_name, group_id))
        threads.append(t)
        t.start()
    for t in threads:
        t.join()
