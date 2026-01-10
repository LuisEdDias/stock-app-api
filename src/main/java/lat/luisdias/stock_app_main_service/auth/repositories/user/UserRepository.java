package lat.luisdias.stock_app_main_service.auth.repositories.user;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByRole(UserRoles role);
}
