package org.judexmars.imagecrud.config;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.dto.imagefilters.BasicRequestStatus;
import org.judexmars.imagecrud.model.PrivilegeEntity;
import org.judexmars.imagecrud.model.RequestStatus;
import org.judexmars.imagecrud.model.RoleEntity;
import org.judexmars.imagecrud.repository.PrivilegeRepository;
import org.judexmars.imagecrud.repository.RequestStatusRepository;
import org.judexmars.imagecrud.repository.RoleRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Application startup data loader.
 */
@Component
@RequiredArgsConstructor
public class DataLoader {

  private final PrivilegeRepository privilegeRepository;

  private final RoleRepository roleRepository;

  private final RequestStatusRepository requestStatusRepository;

  /**
   * Fills database with essential entities, needed for application's functioning.
   * 
   */
  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void run() {
    initPrivileges();
    initRoles();
    initStatuses();
  }

  private void initPrivileges() {

    var privileges = List.of(
        new PrivilegeEntity().setName("UPLOAD_IMAGE"),
        new PrivilegeEntity().setName("DOWNLOAD_IMAGE"),
        new PrivilegeEntity().setName("DELETE_IMAGE")
    );

    Predicate<String> condition = (String x) -> privilegeRepository.findByName(x).isEmpty();

    for (var privilege : privileges) {
      createIf(privilege, privilegeRepository, () -> condition.test(privilege.getName()));
    }
  }

  private void initRoles() {

    // Пока без админа из-за ненадобности

    var viewerPrivileges = List.of(
        privilegeRepository.findByName("UPLOAD_IMAGE").orElseThrow(),
        privilegeRepository.findByName("DOWNLOAD_IMAGE").orElseThrow(),
        privilegeRepository.findByName("DELETE_IMAGE").orElseThrow()
    );

    var roles = List.of(
        new RoleEntity().setName("ROLE_VIEWER").setPrivileges(viewerPrivileges)
    );

    Predicate<String> condition = (String x) -> roleRepository.findByName(x).isEmpty();

    for (var role : roles) {
      createIf(role, roleRepository, () -> condition.test(role.getName()));
    }
  }

  private void initStatuses() {

    Predicate<String> condition = (String x) -> requestStatusRepository.findByName(x).isEmpty();

    for (var status : BasicRequestStatus.values()) {
      createIf(new RequestStatus().setName(status.name()),
          requestStatusRepository, () -> condition.test(status.name()));
    }
  }

  private <T> void createIf(T entity, JpaRepository<T, ?> repository, BooleanSupplier condition) {
    if (condition.getAsBoolean()) {
      repository.save(entity);
    }
  }

}
