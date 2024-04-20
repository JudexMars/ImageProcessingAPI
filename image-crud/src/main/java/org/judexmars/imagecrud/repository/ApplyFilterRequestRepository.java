package org.judexmars.imagecrud.repository;

import java.util.UUID;
import org.judexmars.imagecrud.model.ApplyFilterRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for user's requests to apply filters on image.
 */
@Repository
public interface ApplyFilterRequestRepository
    extends JpaRepository<ApplyFilterRequestEntity, UUID> {
}
