package org.judexmars.imagecrud.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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

    public Stream<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(
                Stream.of(new SimpleGrantedAuthority(name)),
                privileges.stream().map(privilege -> new SimpleGrantedAuthority(privilege.getName()))
        );
    }
}
