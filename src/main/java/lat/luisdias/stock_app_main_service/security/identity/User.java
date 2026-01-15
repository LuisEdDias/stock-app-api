package lat.luisdias.stock_app_main_service.security.identity.entities;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.security.entities.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;
    @Column(nullable = false, unique = true, updatable = false)
    private String nickname;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
    @Column(nullable = false)
    private boolean twoFAuth = false;
    private String twoFASecret;

    protected User() {}

    public User(
            UUID publicId,
            String email,
            String password,
            String nickname,
            UserRole role
    ) {
        this.publicId = publicId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void enableTwoFAuth(String twoFASecret) {
        this.twoFASecret = twoFASecret;
        this.twoFAuth = true;
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public boolean isTwoFAuth() {
        return twoFAuth;
    }

    public String getTwoFASecret() {
        return twoFASecret;
    }

    public UserRole getRole() {
        return role;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
