import json, unittest
from io import BytesIO
from unittest.mock import patch, Mock, ANY
from src.app import process, create_producer, consume_messages, start_consumers
from PIL import Image


class TestImageProcessor(unittest.TestCase):
    @patch('app.redis_client.get')
    @patch('app.redis_client.set')
    @patch('app.download_image')
    @patch('app.upload_image')
    @patch('app.my_producer.send')
    def test_process(self, mock_send, mock_upload, mock_download, mock_redis_get, mock_redis_set):
        # Given
        image = Image.new('RGB', (100, 100), 'red')
        img_byte_arr = BytesIO()
        image.save(img_byte_arr, format='JPEG')
        img_byte_arr = img_byte_arr.getvalue()
        mock_download.return_value = img_byte_arr
        mock_redis_get.return_value = None

        mock_upload.return_value = 'new_image_link'

        message = Mock()
        message.value = {'requestId': 'req123', 'filters': ['REMOVE_BACKGROUND'], 'imageId': 'test_image_id'}

        # When
        process(message)

        # Then
        mock_download.assert_called_once_with(ANY, 'minio-storage', 'test_image_id')
        mock_upload.assert_called_once()
        mock_send.assert_called_once()

    @patch('app.KafkaProducer')
    def test_create_producer(self, mock_kafka_producer):
        # when
        create_producer('localhost:9092', 'all')

        # then
        mock_kafka_producer.assert_called_once()

    @patch('app.KafkaConsumer')
    @patch('app.process')
    def test_consume_messages(self, mock_process, mock_kafka_consumer):
        # given
        mock_consumer_instance = mock_kafka_consumer.return_value
        mock_consumer_instance.__iter__.return_value = iter([
            Mock(value=json.dumps({'filters': ['REMOVE_BACKGROUND'], 'imageId': 'test_image_id'}).encode('utf-8'))
        ])

        # when
        consume_messages(0, 'localhost:9092', 'some_topic', 'some_group')

        # then
        mock_process.assert_called()

    @patch('app.threading.Thread')
    def test_start_consumers(self, mock_thread):
        # Given
        mock_thread_instance = Mock()
        mock_thread.return_value = mock_thread_instance

        # When
        start_consumers(2, 'localhost:9092', 'some_topic', 'some_group')

        # Then
        assert mock_thread.call_count == 2
        assert mock_thread_instance.start.call_count == 2
        assert mock_thread_instance.join.call_count == 2
