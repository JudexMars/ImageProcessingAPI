package org.judexmars.imagecrud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Representation of the Privilege entity in database.
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "privilege")
public class PrivilegeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name")
  private String name;

  @ManyToMany(mappedBy = "privileges")
  private List<RoleEntity> roles;
}
