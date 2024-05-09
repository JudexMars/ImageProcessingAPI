import unittest
import uuid
from unittest.mock import patch, MagicMock

from src.minio_service import create_minio_client, upload_image, download_image


class TestMinioFunctions(unittest.TestCase):

    @patch('minio_service.Minio')
    def test_create_minio_client(self, mock_minio):
        """Test creation of a Minio client."""
        client = create_minio_client(endpoint='http://localhost:9000', access_key='minioadmin', secret_key='minioadmin')
        mock_minio.assert_called_once_with(
            endpoint='http://localhost:9000',
            access_key='minioadmin',
            secret_key='minioadmin',
            secure=False
        )
        self.assertIsNotNone(client)

    @patch('minio_service.Minio')
    def test_upload_image(self, mock_minio):
        """Test uploading an image successfully."""
        client = MagicMock()
        mock_minio.return_value = client
        client.bucket_exists.return_value = False

        # Set up the client's methods that would be called
        client.make_bucket = MagicMock()
        client.put_object = MagicMock()
        link = uuid.uuid4()

        with patch('minio_service.uuid.uuid4', return_value=link):
            link_returned = upload_image(client, 'test-bucket', b'test-image-data')
            self.assertEqual(link_returned, str(link))
            client.make_bucket.assert_called_once_with('test-bucket')
            client.put_object.assert_called_once_with('test-bucket', str(link), unittest.mock.ANY,
                                                      length=len(b'test-image-data'))

    @patch('minio_service.Minio')
    def test_download_image(self, mock_minio):
        """Test downloading an image."""
        client = MagicMock()
        mock_minio.return_value = client
        mock_response = MagicMock()
        mock_response.read.return_value = b'test-image-data'
        client.get_object.return_value = mock_response

        image_data = download_image(client, 'test-bucket', 'image-link')
        client.get_object.assert_called_once_with('test-bucket', 'image-link')
        self.assertEqual(image_data, b'test-image-data')


if __name__ == '__main__':
    unittest.main()
