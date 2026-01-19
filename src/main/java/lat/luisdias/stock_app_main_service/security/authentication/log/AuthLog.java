package lat.luisdias.stock_app_main_service.security.authentication.log;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "auth_log")
public class AuthLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_ip")
    private String userIp;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "action")
    private String action;
    @Column(name = "location")
    @Embedded
    private GeoLocationVO location;
    @Column(name = "success")
    private boolean success;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public AuthLog() {}

    public AuthLog(Builder builder) {
        this.userIp = builder.userIp;
        this.userId = builder.userId;
        this.action = builder.action;
        this.success = builder.success;
        this.timestamp = builder.timestamp;
        this.location = builder.location;
    }

    public static class Builder {
        private String userIp;
        private Long userId;
        private String action;
        private GeoLocationVO location;
        private boolean success;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder userIp(String userIp) {
            this.userIp = userIp;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder location(GeoLocationVO location) {
            this.location = location;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public AuthLog build() {
            return new AuthLog(this);
        }
    }

    public long getId() {
        return id;
    }

    public String getUserIp() {
        return userIp;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public GeoLocationVO getLocation() {
        return location;
    }

    public boolean isSuccess() {
        return success;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
