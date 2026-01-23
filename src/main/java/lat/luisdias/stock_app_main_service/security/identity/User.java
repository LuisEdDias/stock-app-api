package lat.luisdias.stock_app_main_service.security.identity;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.security.authorization.UserRole;
import lat.luisdias.stock_app_main_service.security.authorization.securitygroup.SecurityGroup;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID subject;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private Instant passwordExpiryDate;

    @Column(nullable = false, unique = true, updatable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Column(nullable = false)
    private boolean twoFactorEnabled = false;

    private String twoFactorSecret;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant lastLogin;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_security_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<SecurityGroup> securityGroups = new HashSet<>();

    protected User() {}

    public User(
            UUID subject,
            String email,
            String passwordHash,
            String nickname,
            UserRole role,
            AccountStatus accountStatus
    ) {
        this.subject = Objects.requireNonNull(subject);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.nickname = Objects.requireNonNull(nickname);
        this.role = Objects.requireNonNull(role);
        this.accountStatus = Objects.requireNonNull(accountStatus);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.passwordExpiryDate = this.createdAt;
    }

    public void changePassword(String newPasswordHash, Duration passwordExpiryDuration) {
        this.passwordHash = Objects.requireNonNull(newPasswordHash);
        this.passwordExpiryDate = Instant.now().plus(passwordExpiryDuration);
    }

    public void changeEmail(String email) {
        this.email = Objects.requireNonNull(email);
    }

    public void changeAccountStatus(AccountStatus newAccountStatus) {
        this.accountStatus = Objects.requireNonNull(newAccountStatus);
    }

    public void enableTwoFactor(String secret) {
        this.twoFactorSecret = Objects.requireNonNull(secret);
        this.twoFactorEnabled = true;
    }

    public void disableTwoFactor() {
        this.twoFactorSecret = null;
        this.twoFactorEnabled = false;
    }

    public void addSecurityGroup(SecurityGroup group) {
        this.securityGroups.add(group);
    }

    public void removeSecurityGroup(SecurityGroup group) {
        this.securityGroups.remove(group);
    }

    public void removeAllSecurityGroups() {
        new HashSet<>(securityGroups)
                .forEach(this::removeSecurityGroup);
    }

    public void setLastLogin() {
        this.lastLogin = Instant.now();
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public void setFailedLoginAttempts(int maxAttempts, Duration lockDuration) {
        this.failedLoginAttempts++;

        if (failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = Instant.now().plus(lockDuration);
            this.failedLoginAttempts = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public UUID getSubject() {
        return subject;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getPasswordExpiryDate() {
        return passwordExpiryDate;
    }

    public String getNickname() {
        return nickname;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return accountStatus.equals(AccountStatus.ACTIVE);
    }

    public boolean isLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public String getTwoFactorSecret() {
        return twoFactorSecret;
    }

    public Set<SecurityGroup> getSecurityGroups() {
        return Collections.unmodifiableSet(securityGroups);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return subject != null && subject.equals(other.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(subject);
    }
}
