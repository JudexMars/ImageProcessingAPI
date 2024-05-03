import io
import uuid

from minio import Minio
from minio.error import S3Error


def create_minio_client(endpoint, access_key, secret_key, secure=False):
    client = Minio(endpoint,
                   access_key=access_key,
                   secret_key=secret_key,
                   secure=secure)
    return client


def upload_image(client, bucket_name, image_data):
    try:
        if not client.bucket_exists(bucket_name):
            client.make_bucket(bucket_name)

        image_stream = io.BytesIO(image_data)
        link = uuid.uuid4()
        client.put_object(bucket_name, link, image_stream, length=len(image_data))
        print(f"Uploaded image to {bucket_name}/{link}")
        return link
    except S3Error as e:
        print(f"Failed to upload image to bucket {bucket_name}: {e}")


def download_image(client, bucket_name, link):
    try:
        response = client.get_object(bucket_name, link)
        image_data = response.read()
        print(f"Downloaded {link} from bucket {bucket_name}")
        return image_data
    except S3Error as e:
        print(f"Failed to download {link} from bucket {bucket_name}: {e}")
