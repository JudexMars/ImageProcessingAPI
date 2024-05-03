package org.judexmars.imagecrud.unit.service

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.judexmars.imagecrud.dto.imagefilters.ApplyImageFiltersResponseDto
import org.judexmars.imagecrud.dto.imagefilters.FilterType
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage
import org.judexmars.imagecrud.model.ApplyFilterRequestEntity
import org.judexmars.imagecrud.model.RequestStatus
import org.judexmars.imagecrud.repository.ApplyFilterRequestRepository
import org.judexmars.imagecrud.repository.RequestStatusRepository
import org.judexmars.imagecrud.service.ImageFiltersService
import org.judexmars.imagecrud.service.ImageService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.*
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import java.util.*

internal class ImageFiltersServiceTest {

    private val kafkaTemplate: KafkaTemplate<String, ImageStatusMessage> = mock()

    private val imageService: ImageService = mock()

    private val applyFilterRequestRepository: ApplyFilterRequestRepository = mock()

    private val requestStatusRepository: RequestStatusRepository = mock()

    private val ack: Acknowledgment = mock()

    @Test
    @DisplayName("Apply 2 filters to some image")
    fun applyFiltersToImage() {
        // Given
        val imageFiltersService = ImageFiltersService(
            kafkaTemplate,
            imageService,
            applyFilterRequestRepository,
            requestStatusRepository
        )
        val imageId = UUID.randomUUID()
        val filters = listOf(FilterType.CROP, FilterType.REVERSE_COLORS)
        val savedRequest = ApplyFilterRequestEntity().setRequestId(UUID.randomUUID())
        val responseDto = ApplyImageFiltersResponseDto(savedRequest.requestId)

        doReturn(savedRequest).whenever(applyFilterRequestRepository).save(any())
        doReturn(Optional.of(RequestStatus().setName("WIP"))).whenever(requestStatusRepository).findByName("WIP")

        // When
        val result = imageFiltersService.applyFilters(imageId, filters, emptyMap())

        // Then
        assertEquals(responseDto, result)
        verify(kafkaTemplate).send(any(), any())
        verifyNoMoreInteractions(kafkaTemplate)
    }

    @Test
    @DisplayName("Consume done image")
    fun consumeDoneImage() {
        // Given
        val imageFiltersService = ImageFiltersService(
            kafkaTemplate,
            imageService,
            applyFilterRequestRepository,
            requestStatusRepository
        )
        val imageId = UUID.randomUUID()
        val requestId = UUID.randomUUID()
        val requestEntity = ApplyFilterRequestEntity().apply {
            setRequestId(requestId)
        }

        doReturn(Optional.of(requestEntity)).whenever(applyFilterRequestRepository).findById(requestId)
        doReturn(Optional.of(RequestStatus().setName("DONE"))).whenever(requestStatusRepository).findByName("DONE")

        // When
        imageFiltersService.consumeDoneImage(
            ConsumerRecord(
                "",
                0,
                0,
                "",
                ImageStatusMessage(imageId.toString(), requestId.toString(), emptyList(), emptyMap())
            ), ack
        )

        // Then
        verify(applyFilterRequestRepository).findById(requestId)
        verify(applyFilterRequestRepository).save(any())
        verify(ack).acknowledge()
        verifyNoMoreInteractions(applyFilterRequestRepository, ack)
    }
}