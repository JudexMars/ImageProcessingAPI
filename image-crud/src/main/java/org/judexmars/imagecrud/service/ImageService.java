package org.judexmars.imagecrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.image.ImageDto;
import org.judexmars.imagecrud.dto.image.ImageResponseDto;
import org.judexmars.imagecrud.dto.image.UploadImageResponseDto;
import org.judexmars.imagecrud.exception.DeleteFileException;
import org.judexmars.imagecrud.exception.ImageNotFoundException;
import org.judexmars.imagecrud.exception.UploadFailedException;
import org.judexmars.imagecrud.mapper.ImageMapper;
import org.judexmars.imagecrud.model.ImageEntity;
import org.judexmars.imagecrud.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    private final ImageMapper mapper;

    private final S3Service minioService;

    private final AccountService accountService;

    /**
     * Check if all ids from the list are present in image table
     *
     * @param imageIds ids
     * @return true / false
     */
    public boolean existAll(List<UUID> imageIds) {
        return imageRepository.existsImageByIdIn(imageIds);
    }

    /**
     * Get all images with provided ids
     *
     * @param imageIds ids
     * @return list of all corresponding images
     */
    public List<ImageEntity> getAllImages(List<UUID> imageIds) {
        return imageRepository.findAllByIdIn(imageIds);
    }

    /**
     * Get meta information of the image with provided id
     *
     * @param id specified id
     * @return {@link ImageDto} representation of the image
     * @throws ImageNotFoundException if there's no image with this id
     */
    public ImageDto getImageMeta(UUID id) throws ImageNotFoundException {
        var imageOptional = imageRepository.findById(id);
        if (imageOptional.isEmpty()) {
            throw new ImageNotFoundException(String.valueOf(id));
        }
        return mapper.toImageDto(imageOptional.get());
    }

    /**
     * Returns byte array of image file
     *
     * @param id id of the desired image
     * @return binary file
     * @throws Exception if image is not found or can't be downloaded for some reason
     */
    public byte[] downloadImage(String id) throws Exception {
        var image = getImageMeta(UUID.fromString(id));
        return minioService.downloadImage(image.link());
    }

    /**
     * Upload new image
     *
     * @param file binary file of image
     * @return meta information of this image as {@link ImageDto}
     * @throws UploadFailedException if the image cannot be uploaded
     */
    public UploadImageResponseDto uploadImage(MultipartFile file, String username) throws UploadFailedException {
        try {
            var image = minioService.uploadImage(file);
            var author = accountService.getEntityByUsername(username);
            var meta = imageRepository.save(mapper.toImageEntity(image).setAuthor(author));
            return new UploadImageResponseDto(meta.getId().toString());
        } catch (Exception ex) {
            throw new UploadFailedException();
        }
    }

    public void deleteImage(UUID id) {
        var image = getImageMeta(id);
        imageRepository.deleteById(id);
        try {
            minioService.deleteImage(image.link());
        } catch (Exception e) {
            throw new DeleteFileException();
        }
    }

    public List<ImageResponseDto> getImagesOfUser(UUID accountId) {
        return imageRepository.findByAuthorId(accountId)
                .stream()
                .map(mapper::toImageResponseDto)
                .collect(Collectors.toList());
    }
}