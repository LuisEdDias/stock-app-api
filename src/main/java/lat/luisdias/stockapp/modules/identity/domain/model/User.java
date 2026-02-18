package lat.luisdias.stockapp.modules.identity.domain.model;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.security.identity.AccountStatus;
import lat.luisdias.stockapp.shared.exception.exceptions.DomainInvariantViolationException;
import lat.luisdias.stock_app_main_service.security.authorization.UserRole;
import lat.luisdias.stock_app_main_service.security.authorization.securitygroup.SecurityGroup;

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

    @Column(nullable = false, unique = true, updatable = false)
    private String nicknameNormalized;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus = AccountStatus.PENDING_ACTIVATION;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    private Instant lastLogin;

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
            String nickname,
            UserRole role
    ) {
        this.subject = Objects.requireNonNull(subject);
        this.email = normalizeEmail(email);
        setNickname(nickname);
        this.role = Objects.requireNonNull(role);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void active() {
        if (this.accountStatus == AccountStatus.ACTIVE) return;
        this.accountStatus = AccountStatus.ACTIVE;
        setUpdated();
    }

    public void  disable() {
        if (this.accountStatus == AccountStatus.INACTIVE) return;
        this.accountStatus = AccountStatus.INACTIVE;
        setUpdated();
    }

    public void changeEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (this.email.equals(normalizedEmail)) return;
        this.email = normalizedEmail;
        setUpdated();
    }

    public void addSecurityGroup(SecurityGroup group) {
        if (securityGroups.contains(group)) return;
        this.securityGroups.add(group);
        setUpdated();
    }

    public void removeSecurityGroup(SecurityGroup group) {
        if (this.securityGroups.isEmpty() || !this.securityGroups.contains(group)) return;
        this.securityGroups.remove(group);
        setUpdated();
    }

    public void removeAllSecurityGroups() {
        if (this.securityGroups.isEmpty()) return;
        this.securityGroups.clear();
        setUpdated();
    }

    public void registerLogin() {
        this.lastLogin = Instant.now();
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

    public String getNicknameNormalized() {
        return nicknameNormalized;
    }

    public String getNickname() {
        return nickname;
    }

    public UserRole getRole() {
        return role;
    }

    public Set<SecurityGroup> getSecurityGroups() {
        return Collections.unmodifiableSet(securityGroups);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank())
            throw new DomainInvariantViolationException("exception.email_required");
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
            throw new DomainInvariantViolationException("exception.email_invalid");
        return email.trim().toLowerCase();
    }

    private void setNickname(String nickname) {
        if (nickname == null || nickname.isBlank())
            throw new DomainInvariantViolationException("exception.nickname_required");
        String cleaned = nickname.replaceAll("( {2,})", " ").trim();
        this.nickname = cleaned;
        this.nicknameNormalized = cleaned.toLowerCase();
    }

    private void setUpdated() {
        this.updatedAt = Instant.now();
    }
}
