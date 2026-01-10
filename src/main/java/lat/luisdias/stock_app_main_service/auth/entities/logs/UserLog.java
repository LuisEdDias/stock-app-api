package lat.luisdias.stock_app_main_service.auth.entities.logs;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "user_log")
public class UserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_nickname")
    private String userNickname;
    @Column(name = "action")
    private String action;
    @Column(name = "user_target_id")
    private Long userTargetId;
    @Temporal(TemporalType.TIMESTAMP)
    private Timestamp timestamp;

    public UserLog() {}

    public UserLog(Builder builder){
        this.userId = builder.userId;
        this.userNickname = builder.userNickname;
        this.action = builder.action;
        this.userTargetId = builder.userTargetId;
        this.timestamp = builder.timestamp;
    }


    public static class Builder {
        private Long userId;
        private String userNickname;
        private String action;
        private Long userTargetId;
        private final Timestamp timestamp = new Timestamp(Instant.now().toEpochMilli());

        public Builder setUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setUserNickname(String userNickname) {
            this.userNickname = userNickname;
            return this;
        }

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public Builder setUserTargetId(Long userTargetId) {
            this.userTargetId = userTargetId;
            return this;
        }

        public UserLog build() {
            return new UserLog(this);
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public Long getUserTargetId() {
        return userTargetId;
    }

    public String getAction() {
        return action;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
