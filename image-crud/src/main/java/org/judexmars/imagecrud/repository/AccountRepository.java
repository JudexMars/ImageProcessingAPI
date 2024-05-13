package org.judexmars.imagecrud.repository;

import org.judexmars.imagecrud.model.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {


    Optional<AccountEntity> findByUsername(String username);
}
