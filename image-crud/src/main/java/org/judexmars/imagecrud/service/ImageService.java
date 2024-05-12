package org.judexmars.imagecrud.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.image.ImageBinaryDto;
import org.judexmars.imagecrud.dto.image.ImageDto;
import org.judexmars.imagecrud.dto.image.ImageResponseDto;
import org.judexmars.imagecrud.dto.image.UploadImageResponseDto;
import org.judexmars.imagecrud.exception.DeleteFileException;
import org.judexmars.imagecrud.exception.ImageNotFoundException;
import org.judexmars.imagecrud.exception.UploadFailedException;
import org.judexmars.imagecrud.mapper.ImageMapper;
import org.judexmars.imagecrud.model.ImageEntity;
import org.judexmars.imagecrud.model.ImageEntity;
import org.judexmars.imagecrud.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for working with images.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

  private final ImageRepository imageRepository;

  private final ImageMapper mapper;

  private final S3Service minioService;

  private final AccountService accountService;

  /**
   * Get meta information of the image with provided id.
   *
   * @param id specified id
   * @return {@link ImageDto} representation of the image
   * @throws ImageNotFoundException if there's no image with this id
   */
  public ImageDto getImageMeta(UUID id) throws ImageNotFoundException {
    var image = getImageMetaAsEntitySafely(id, accountId);
    return mapper.toImageDto(image);
  }

  /**
   * Returns byte array of image file.
   *
   * @param id id of the desired image
   * @return binary file
   * @throws Exception if image is not found or can't be downloaded for some reason
   */
  public ImageBinaryDto downloadImage(UUID id, UUID accountId) throws Exception {
    var image = getImageMeta(id, accountId);
    return new ImageBinaryDto(image.filename(),
        image.size(), minioService.downloadImage(image.link()));
  }

  /**
   * Upload new image.
   *
   * @param file binary file of image
   * @return meta information of this image as {@link ImageDto}
   * @throws UploadFailedException if the image cannot be uploaded
   */
  public UploadImageResponseDto uploadImage(MultipartFile file, String username)
      throws UploadFailedException {
    try {
      var image = minioService.uploadImage(file);
      var author = accountService.getEntityByUsername(username);
      var meta = imageRepository.save(mapper.toImageEntity(image).setAuthor(author));
      return new UploadImageResponseDto(meta.getId().toString());
    } catch (Exception ex) {
      throw new UploadFailedException();
    }
  }

  /**
   * Delete image with provided id.
   *
   * @param id id of the image to delete
   */
  public void deleteImage(UUID id) {
    var image = getImageMeta(id, accountId);
    imageRepository.deleteById(id);
    try {
      minioService.deleteImage(image.link());
    } catch (Exception e) {
      throw new DeleteFileException();
    }
  }

  /**
   * Get all images of user with provided id.
   *
   * @param accountId id of the user
   * @return {@link List} of user's images
   */
  public List<ImageResponseDto> getImagesOfUser(UUID accountId) {
    return imageRepository.findByAuthorId(accountId)
        .stream()
        .map(mapper::toImageResponseDto)
        .collect(Collectors.toList());
  }

  /**
   * Safely get image as entity.
   *
   * @param id id of the image
   * @param accountId id of the account requesting this
   * @return image entity
   */
  public ImageEntity getImageMetaAsEntitySafely(UUID id, UUID accountId) {
    var image = getImageMetaAsEntity(id);
    if (!image.getAuthor().getId().equals(accountId)) {
      throw new ImageNotFoundException(String.valueOf(id));
    }
    return image;
  }

  /**
   * Get image as entity.
   *
   * @param id id of the image
   * @return image entity
   */
  public ImageEntity getImageMetaAsEntity(UUID id) {
    return imageRepository.findById(id)
        .orElseThrow(() -> new ImageNotFoundException(String.valueOf(id)));
  }
}