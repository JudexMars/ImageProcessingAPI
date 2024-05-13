package org.judexmars.imagecrud.repository;

import org.judexmars.imagecrud.model.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

    boolean existsImageByIdIn(List<UUID> ids);

    List<ImageEntity> findAllByIdIn(List<UUID> ids);

    boolean existsImageByLink(String link);

    List<ImageEntity> findByAuthorId(UUID accountId);

    boolean existsImageById(UUID id);
}
