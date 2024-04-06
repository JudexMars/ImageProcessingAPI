package org.judexmars.imagecrud.repository;

import java.util.Optional;
import java.util.UUID;
import org.judexmars.imagecrud.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing role entities.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {


  Optional<RoleEntity> findByName(String name);
}
