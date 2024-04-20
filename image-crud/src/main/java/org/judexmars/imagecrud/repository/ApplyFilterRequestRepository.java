package org.judexmars.imagecrud.repository;

import org.judexmars.imagecrud.model.ApplyFilterRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for user's requests to apply filters on image.
 */
@Repository
public interface ApplyFilterRequestRepository
        extends JpaRepository<ApplyFilterRequestEntity, UUID> {
}
