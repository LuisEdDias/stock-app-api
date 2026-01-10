package lat.luisdias.stock_app_main_service.auth.entities.user;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.auth.dto.admin.StoreRootUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.StoreUserDTO;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(unique = true, nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private UserRoles role;
    @Column(nullable = false)
    private boolean twoFAuth = false;
    private String twoFASecret;


    public User(StoreUserDTO userDTO) {
        this.email = userDTO.email();
        this.nickname = userDTO.username();
        this.password = new BCryptPasswordEncoder().encode(userDTO.password());
        this.role = userDTO.role();
    }

    public User(StoreRootUserDTO rootUserDTO) {
        this.email = rootUserDTO.email();
        this.nickname = rootUserDTO.username();
        this.password = new BCryptPasswordEncoder().encode(rootUserDTO.password());
        this.role = UserRoles.ROOT;
    }

    protected User() {}

    public void updatePassword(String password) {
        this.password = new BCryptPasswordEncoder().encode(password);
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void enableTwoFAuth(String twoFASecret) {
        this.twoFASecret = twoFASecret;
        this.twoFAuth = true;
    }

    public boolean isTwoFAuth() {
        return twoFAuth;
    }

    public String getTwoFASecret() {
        return twoFASecret;
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

    public String getNickname() {
        return nickname;
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

    public Long getId() {
        return id;
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
