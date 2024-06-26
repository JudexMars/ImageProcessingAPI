package org.judexmars.imagecrud.unit.service

import org.judexmars.imagecrud.dto.imagefilters.ApplyImageFiltersResponse
import org.judexmars.imagecrud.dto.imagefilters.FilterType
import org.judexmars.imagecrud.dto.kafka.ImageStatusMessage
import org.judexmars.imagecrud.model.AccountEntity
import org.judexmars.imagecrud.model.ApplyFilterRequestEntity
import org.judexmars.imagecrud.model.ImageEntity
import org.judexmars.imagecrud.model.RequestStatus
import org.judexmars.imagecrud.repository.ApplyFilterRequestRepository
import org.judexmars.imagecrud.repository.RequestStatusRepository
import org.judexmars.imagecrud.service.ImageFiltersService
import org.judexmars.imagecrud.service.ImageService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.util.Optional
import java.util.UUID

internal class ImageFiltersServiceTest {
    private val kafkaTemplate: KafkaTemplate<String, ImageStatusMessage> = mock()

    private val imageService: ImageService = mock()

    private val applyFilterRequestRepository: ApplyFilterRequestRepository = mock()

    private val requestStatusRepository: RequestStatusRepository = mock()

    @Test
    @DisplayName("Apply 2 filters to some image")
    fun applyFiltersToImage() {
        // Given
        val imageFiltersService =
            ImageFiltersService(
                kafkaTemplate,
                imageService,
                mock(),
                applyFilterRequestRepository,
                requestStatusRepository,
                mock(),
            )
        val imageId = UUID.randomUUID()
        val filters = listOf(FilterType.CROP, FilterType.REVERSE_COLORS)
        val savedRequest = ApplyFilterRequestEntity().setRequestId(UUID.randomUUID())
        val responseDto =
            ApplyImageFiltersResponse(savedRequest.requestId)

        doReturn(savedRequest).whenever(applyFilterRequestRepository).save(any())
        doReturn(Optional.of(RequestStatus().setName("WIP"))).whenever(requestStatusRepository).findByName("WIP")
        doReturn(ImageEntity().setId(imageId)).whenever(imageService).getImageMetaAsEntitySafely(imageId, null)

        // When
        val result = imageFiltersService.applyFilters(imageId, filters, emptyMap(), null)

        // Then
        assertEquals(responseDto, result)
        verify(kafkaTemplate).send(any(), any())
        verifyNoMoreInteractions(kafkaTemplate)
    }

    @Test
    @DisplayName("Consume done image")
    fun consumeDoneImage() {
        // Given
        val imageFiltersService =
            ImageFiltersService(
                kafkaTemplate,
                imageService,
                mock(),
                applyFilterRequestRepository,
                requestStatusRepository,
                mock(),
            )
        val imageId = UUID.randomUUID()
        val requestId = UUID.randomUUID()
        val requestEntity =
            ApplyFilterRequestEntity().apply {
                setRequestId(requestId)
                setImage(ImageEntity().setId(imageId))
            }

        doReturn(Optional.of(requestEntity)).whenever(applyFilterRequestRepository).findById(requestId)
        doReturn(Optional.of(RequestStatus().setName("DONE"))).whenever(requestStatusRepository).findByName("DONE")
        doReturn(ImageEntity().setId(imageId).setAuthor(AccountEntity()).setFilename("")).whenever(
            imageService,
        ).getImageMetaAsEntity(
            any(),
        )

        // When
        imageFiltersService.processDoneImage(
            ImageStatusMessage(
                imageId.toString(),
                requestId.toString(),
                emptyList(),
                emptyMap(),
            ),
        )

        // Then
        verify(applyFilterRequestRepository).findById(requestId)
        verify(applyFilterRequestRepository).save(any())
    }
}
