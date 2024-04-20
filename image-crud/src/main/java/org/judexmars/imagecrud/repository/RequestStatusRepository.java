package org.judexmars.imagecrud.repository;

import java.util.Optional;
import org.judexmars.imagecrud.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for request statuses.
 */
@Repository
public interface RequestStatusRepository extends JpaRepository<RequestStatus, Integer> {

  Optional<RequestStatus> findByName(String name);
}
