import json
import os
import threading
import redis

from kafka import KafkaProducer, KafkaConsumer
from minio_service import (
    create_minio_client,
    download_image,
    upload_image)
from rembg import remove

# Configure redis connection
REDIS_HOST = os.getenv('REDIS_HOST', 'remove-background-processor-redis')
REDIS_PORT = os.getenv('REDIS_PORT', 6382)
REDIS_PASSWORD = os.getenv('REDIS_PASSWORD', "6k_j76,dDUl_")
redis_client = redis.Redis(
    host=REDIS_HOST,
    port=REDIS_PORT,
    password=REDIS_PASSWORD,
    decode_responses=True
)

# Configure minio connection
my_endpoint = os.getenv('MINIO_ENDPOINT', 'localhost:9000')
my_access_key = os.getenv('MINIO_ACCESS_KEY', 'minioadmin')
my_secret_key = os.getenv('MINIO_SECRET_KEY', 'minioadmin')
my_main_bucket_name = os.getenv('MINIO_BUCKET', 'minio-storage')
my_minor_bucket_name = os.getenv('MINIO_BUCKET', 'minio-temp')
my_secure = os.getenv('MINIO_SECURE', 'false').lower() in ('true', '1', 't')

minio_client = create_minio_client(my_endpoint, my_access_key, my_secret_key)

ssl_keyfile = os.getenv("KAFKA_SSL_KEYFILE")
ssl_certfile = os.getenv("KAFKA_SSL_CERTFILE")
sasl_mechanism = os.getenv("KAFKA_SASL_MECHANISM", "PLAIN")
sasl_plain_username = os.getenv("KAFKA_SASL_PLAIN_USERNAME", "admin")
sasl_plain_password = os.getenv("KAFKA_SASL_PLAIN_PASSWORD", "admin-secret")
ssl_cafile = os.getenv("KAFKA_SSL_CAFILE")
security_protocol = os.getenv("KAFKA_SECURITY_PROTOCOL", "SASL_SSL")


def process(message):
    redis_value = redis_client.get(message.value["requestId"])
    if redis_value == message.value["imageId"]:
        return

    data = message.value
    filters = data['filters']
    if filters[0] != 'REMOVE_BACKGROUND':
        return

    image_bytes = download_image(minio_client,
                                 my_main_bucket_name,
                                 data['imageId'])
    if image_bytes is None:
        image_bytes = download_image(minio_client,
                                     my_minor_bucket_name,
                                     data['imageId'])

    output = remove(image_bytes)

    redis_client.set(message.value["requestId"],
                     message.value["imageId"],
                     ex=86400)

    filters.remove('REMOVE_BACKGROUND')
    if not filters:
        next_link = upload_image(minio_client,
                                 my_main_bucket_name,
                                 output)
    else:
        next_link = upload_image(minio_client,
                                 my_minor_bucket_name,
                                 output)
    data['imageId'] = next_link

    if not filters:
        my_producer.send(my_done_topic, value=data)
    else:
        my_producer.send(my_wip_topic, value=data)


def create_producer(bootstrap_servers, acks):
    return KafkaProducer(bootstrap_servers=bootstrap_servers,
                         value_serializer=lambda x:
                         json.dumps(x).encode('utf-8'),
                         acks=acks,
                         ssl_keyfile=ssl_keyfile,
                         ssl_certfile=ssl_certfile,
                         security_protocol=security_protocol,
                         sasl_mechanism=sasl_mechanism,
                         sasl_plain_username=sasl_plain_username,
                         sasl_plain_password=sasl_plain_password,
                         ssl_cafile=ssl_cafile,
                         ssl_check_hostname=False,
                         api_version=(0, 10, 1))


def consume_messages(consumer_id, bootstrap_servers, topic_name, group_id):
    consumer = KafkaConsumer(topic_name,
                             group_id=group_id,
                             bootstrap_servers=bootstrap_servers,
                             auto_offset_reset='earliest',
                             value_deserializer=lambda x:
                             json.loads(x.decode('utf-8')),
                             ssl_cafile=ssl_cafile,
                             ssl_certfile=ssl_certfile,
                             ssl_keyfile=ssl_keyfile,
                             security_protocol=security_protocol,
                             sasl_mechanism=sasl_mechanism,
                             sasl_plain_username=sasl_plain_username,
                             sasl_plain_password=sasl_plain_password,
                             ssl_check_hostname=False,
                             enable_auto_commit=False)
    print(f"Consumer {consumer_id} is starting.")
    for message in consumer:
        print(f"Consumer {consumer_id} received: {message.value}")
        try:
            process(message)
            consumer.commit()
        except KeyError:
            print('Received message is invalid')


def start_consumers(n, bootstrap_servers, topic_name, group_id):
    threads = []
    for i in range(n):
        t = threading.Thread(target=consume_messages,
                             args=(i, bootstrap_servers,
                                   topic_name,
                                   group_id))
        threads.append(t)
        t.start()
    for t in threads:
        t.join()


# Configure kafka connection
my_bootstrap_servers = os.getenv('BOOTSTRAP_SERVERS',
                                 'localhost:9092')
my_wip_topic = os.getenv('WIP_TOPIC', 'images.wip')
my_done_topic = os.getenv('DONE_TOPIC', 'images.done')
my_group_id = 'remove-background-consumer-group-1'
consumers_amount = 2

my_producer = create_producer(my_bootstrap_servers, 'all')
start_consumers(consumers_amount,
                my_bootstrap_servers,
                my_wip_topic,
                my_group_id)
