import unittest
from unittest.mock import patch, MagicMock

from my_kafka import create_producer, consume_messages, start_consumers


class TestKafkaFunctions(unittest.TestCase):

    @patch('my_kafka.KafkaProducer')
    def test_create_producer(self, mock_kafka_producer):
        """Test creation of a Kafka producer with specific settings."""
        create_producer('localhost:9092', 'all')
        mock_kafka_producer.assert_called_once_with(
            bootstrap_servers='localhost:9092',
            value_serializer=unittest.mock.ANY,
            acks='all'
        )

    @patch('my_kafka.KafkaConsumer')
    @patch('my_kafka.process')
    def test_consume_messages(self, mock_process, mock_kafka_consumer):
        """Test consume_messages handling a Kafka message correctly."""
        consumer_instance = MagicMock()
        consumer_instance.__iter__.return_value = iter([MagicMock(value={'key': 'value'})])
        mock_kafka_consumer.return_value = consumer_instance

        consume_messages(0, 'localhost:9092', 'test-topic', 'test-group')
        mock_process.assert_called_once()

    @patch('my_kafka.threading.Thread')
    def test_start_consumers(self, mock_thread):
        """Test if threads are correctly started for consumers."""
        start_consumers(3, 'localhost:9092', 'test-topic', 'test-group')
        self.assertEqual(mock_thread.call_count, 3)


if __name__ == '__main__':
    unittest.main()
