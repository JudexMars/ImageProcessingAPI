package org.judexmars.imagecrud.repository;

import java.util.Optional;
import java.util.UUID;
import org.judexmars.imagecrud.model.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing account entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {


  Optional<AccountEntity> findByUsername(String username);
}
