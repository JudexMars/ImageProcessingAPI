package org.judexmars.imagecrud.mapper;

import org.judexmars.imagecrud.dto.image.ImageDto;
import org.judexmars.imagecrud.dto.image.ImageResponseDto;
import org.judexmars.imagecrud.model.ImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for {@link ImageEntity}, {@link ImageDto} and {@link ImageResponseDto}.
 */
@Mapper(componentModel = "spring")
public interface ImageMapper {

  ImageDto toImageDto(ImageEntity image);

  ImageEntity toImageEntity(ImageDto imageDto);

  @Mapping(target = "imageId", source = "id")
  ImageResponseDto toImageResponseDto(ImageEntity image);
}
