import os

from rembg import remove

from my_kafka import *
from my_minio import *


def process(message):
    data = message.value
    filters = data['filters']
    if filters[0] != 'REMOVE_BACKGROUND':
        return
    image_bytes = download_image(minio_client, my_bucket_name, data['imageId'])
    output = remove(image_bytes)
    next_link = upload_image(minio_client, my_bucket_name, output)
    data['imageId'] = next_link
    filters.remove('REMOVE_BACKGROUND')
    updated_message = json.dumps(data).encode('utf-8')
    my_producer.send(my_wip_topic, value=updated_message)


if __name__ == "__main__":
    # Configure kafka connection
    my_bootstrap_servers = os.getenv('BOOTSTRAP_SERVERS', 'localhost:9092')
    my_wip_topic = os.getenv('WIP_TOPIC', 'images.wip')
    my_done_topic = os.getenv('DONE_TOPIC', 'images.done')
    my_producer = create_producer(my_bootstrap_servers, 'all')
    my_group_id = 'remove-background-consumer-group-1'
    consumers_amount = 3
    start_consumers(consumers_amount, my_bootstrap_servers, my_wip_topic, my_group_id)

    # Configure minio connection
    my_endpoint = os.getenv('MINIO_ENDPOINT', 'localhost:9000')
    my_access_key = os.getenv('MINIO_ACCESS_KEY', 'minioadmin')
    my_secret_key = os.getenv('MINIO_SECRET_KEY', 'minioadmin')
    my_bucket_name = os.getenv('MINIO_BUCKET', 'minio-storage')
    my_secure = os.getenv('MINIO_SECURE', 'false').lower() in ('true', '1', 't')

    minio_client = create_minio_client(my_endpoint, my_access_key, my_secret_key)
