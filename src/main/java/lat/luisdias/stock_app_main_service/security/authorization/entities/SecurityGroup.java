package lat.luisdias.stock_app_main_service.security.authorization.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "security_groups")
public class SecurityGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_permissions",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")

    )
    private Set<Permission> permissions = new HashSet<>();

    protected SecurityGroup() {}

    public SecurityGroup(String name, String description) {
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }

    public void updateName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void addPermission(Permission permission) {
        this.permissions.add(Objects.requireNonNull(permission));
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(Objects.requireNonNull(permission));
    }

    public void removeAllPermissions() {
        this.permissions.clear();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SecurityGroup that = (SecurityGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
