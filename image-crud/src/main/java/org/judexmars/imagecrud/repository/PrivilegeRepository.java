package org.judexmars.imagecrud.repository;

import java.util.Optional;
import org.judexmars.imagecrud.model.PrivilegeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing privilege entities.
 */
@Repository
public interface PrivilegeRepository extends JpaRepository<PrivilegeEntity, Long> {
  Optional<PrivilegeEntity> findByName(String name);
}
