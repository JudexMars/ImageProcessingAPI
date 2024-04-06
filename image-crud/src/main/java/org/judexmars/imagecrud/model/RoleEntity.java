package org.judexmars.imagecrud.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Representation of the role entity in database.
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "role")
@ToString
public class RoleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "name")
  private String name;

  @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
  @ToString.Exclude
  private List<AccountEntity> accounts = new ArrayList<>();

  @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "role_privilege",
      joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id")
  )
  @ToString.Exclude
  private List<PrivilegeEntity> privileges;

  /**
   * Get authorities for this role.
   *
   * <p>
   * This method returns a stream of granted authorities.
   * The stream includes the role name and all privileges names.
   * </p>
   *
   * @return Stream of granted authorities
   */
  public Stream<? extends GrantedAuthority> getAuthorities() {
    return Stream.concat(
        Stream.of(new SimpleGrantedAuthority(name)),
        privileges.stream().map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
    );
  }
}
