package lat.luisdias.stockapp.shared.domain.support;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * Abstract base class for all persistent domain entities.
 *
 * <p>This class provides a unified structure for entity identity, concurrency control,
 * and automated auditing. By using {@link MappedSuperclass}, its attributes are
 * inherited by concrete entities without requiring a separate table for the base class.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 * <li><b>Identity:</b> Managed via a {@link GenerationType#IDENTITY} strategy.</li>
 * <li><b>Optimistic Locking:</b> Uses a version field to prevent "Lost Update"
 * scenarios in concurrent environments.</li>
 * <li><b>Automated Auditing:</b> Leverages {@link AuditingEntityListener} to
 * automatically manage creation and modification timestamps.</li>
 * </ul>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Version number used for optimistic locking control.
     * Automatically incremented by the persistence provider on every update.
     */
    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
