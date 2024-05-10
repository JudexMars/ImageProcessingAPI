package org.judexmars.imageprocessor.unit

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.judexmars.imageprocessor.dto.ImageStatusMessage
import org.judexmars.imageprocessor.service.Processor
import org.judexmars.imageprocessor.service.ProcessorListener
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.kafka.support.Acknowledgment
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ProcessorListenerTest {
    private val redisTemplate = mock<RedisTemplate<String, String>>()
    private val valueOperations = mock<ValueOperations<String, String>>()
    private val processors = listOf(mock<Processor>())
    private val acknowledgment = mock<Acknowledgment>()

    @Test
    fun `test listen method with different imageId`() {
        // Given
        val requestId = UUID.randomUUID()
        val imageId = UUID.randomUUID()
        val imageStatusMessage =
            ImageStatusMessage(
                requestId = requestId,
                imageId = imageId,
                filters = emptyList(),
                props = emptyMap(),
            )
        val consumerRecord = ConsumerRecord("images.wip", 0, 0L, "key", imageStatusMessage)

        whenever(redisTemplate.opsForValue()).thenReturn(valueOperations)
        whenever(valueOperations.get(requestId.toString())).thenReturn(null) // Return a different imageId

        val listener = ProcessorListener(processors, redisTemplate)

        // When
        listener.listen(consumerRecord, acknowledgment)

        // Then
        verify(valueOperations).set(requestId.toString(), imageId.toString())
        verify(processors[0]).process(imageStatusMessage)
        verify(acknowledgment).acknowledge()
    }
}
