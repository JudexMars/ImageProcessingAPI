package org.judexmars.imagecrud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

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
