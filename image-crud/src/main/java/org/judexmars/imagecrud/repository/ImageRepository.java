package org.judexmars.imagecrud.repository;

import java.util.List;
import java.util.UUID;
import org.judexmars.imagecrud.model.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing image entities.
 */
@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

  List<ImageEntity> findByAuthorId(UUID accountId);

}
