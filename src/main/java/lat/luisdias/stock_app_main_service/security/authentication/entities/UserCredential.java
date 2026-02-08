package lat.luisdias.stock_app_main_service.security.authentication.entities;

import jakarta.persistence.*;
import lat.luisdias.stock_app_main_service.infra.exceptions.DomainInvariantViolationException;
import lat.luisdias.stock_app_main_service.security.identity.User;

import java.time.Duration;
import java.time.Instant;

@Entity
@Table(name = "user_credentials")
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false)
    private String encryptedPassword;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @Column(nullable = false)
    private Instant passwordExpiryDate;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;

    protected UserCredential() {}

    public UserCredential(User user, String encryptedPassword, Instant passwordExpiryDate) {
        this.user = user;
        this.encryptedPassword = encryptedPassword;
        setPasswordExpiryDate(passwordExpiryDate);
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void changePassword(String passwordHash, Duration passwordExpiryDuration) {
        this.encryptedPassword = passwordHash;
        setPasswordExpiryDate(Instant.now().plus(passwordExpiryDuration));
        this.failedLoginAttempts = 0;
    }

    public void setPasswordExpiryDate(Instant passwordExpiryDate) {
        if (!passwordExpiryDate.isAfter(Instant.now()))
            throw new DomainInvariantViolationException("exception.passwordExpiryDate.invalid");

        this.passwordExpiryDate = passwordExpiryDate;
    }

    public void setFailedLoginAttempts(int maxAttempts, Duration lockDuration) {
        this.failedLoginAttempts++;

        if (failedLoginAttempts >= maxAttempts) {
            this.lockedUntil = Instant.now().plus(lockDuration);
            this.failedLoginAttempts = 0;
        }
    }

    public void resetLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    public boolean isLocked() {
        return lockedUntil != null && Instant.now().isBefore(lockedUntil);
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public Instant getPasswordExpiryDate() {
        return passwordExpiryDate;
    }

    public int getLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}
