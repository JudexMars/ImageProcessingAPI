package org.judexmars.imagecrud.repository;

import org.judexmars.imagecrud.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for request statuses.
 */
@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Integer> {

    Optional<RequestStatus> findByName(String name);
}
