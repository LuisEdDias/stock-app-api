package lat.luisdias.stock_app_main_service.security.identity;

import lat.luisdias.stock_app_main_service.security.authorization.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findBySubject(UUID subject);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u.role) FROM User u WHERE u.role = :userRole")
    int countAllByUserRole(@Param("userRole") UserRole userRole);
}
