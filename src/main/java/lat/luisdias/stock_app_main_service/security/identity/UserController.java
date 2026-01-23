package lat.luisdias.stock_app_main_service.security.identity;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.infra.DataMask;
import lat.luisdias.stock_app_main_service.security.authentication.AuthenticatedUser;
import lat.luisdias.stock_app_main_service.security.authorization.annotations.RequiresPermission;
import lat.luisdias.stock_app_main_service.security.identity.dto.GetUserDTO;
import lat.luisdias.stock_app_main_service.security.identity.dto.StoreUserDTO;
import lat.luisdias.stock_app_main_service.security.identity.dto.UpdateEmailDTO;
import lat.luisdias.stock_app_main_service.security.identity.dto.UpdatePasswordDTO;
import lat.luisdias.stock_app_main_service.security.identity.log.UserLog;
import lat.luisdias.stock_app_main_service.security.identity.log.UserLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@Validated
@RequestMapping("/v1/user")
public class UserController {
    private final UserService userService;
    private final UserLogService userLogService;
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, UserLogService userLogService) {
        this.userService = userService;
        this.userLogService = userLogService;
    }

    @GetMapping("/all")
    public ResponseEntity<Page<GetUserDTO>> findAll(
            @PageableDefault(sort = {"id"}) Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser user
            ){
        var page = userService.findAll(pageable);
        logger.info("USER ID {}( {} ) GOT ALL USERS",user.subject(), user.nickname());
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/{subject}")
    @RequiresPermission("USER")
    public ResponseEntity<GetUserDTO> findById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") @P("subject") UUID subject,
            @AuthenticationPrincipal User user
    ){
        logger.info("USER ID {}( {} ) GOT DATA from USER ID {}",user.getId(), user.getNickname(), subject);
        return ResponseEntity.ok(userService.findByPublicId(subject));
    }

    @PostMapping("/create")
    @Transactional
    @RequiresPermission("USER:CR")
    public ResponseEntity<GetUserDTO> store(
            @Valid @RequestBody StoreUserDTO storeUserDTO,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal AuthenticatedUser user
            ) {
        var newUser = userService.storeUser(storeUserDTO);
        var uri = uriBuilder.path("/v1/user/{id}").buildAndExpand(newUser.id()).toUri();
        logger.info(
                "USER ID {}( {} ) CREATED by USER ID {}( {} )",
                newUser.id(),
                newUser.nickname(),
                user.subject(),
                user.nickname()
        );
        entryLog(
                user.subject(),
                user.nickname(),
                "CREATE USER",
                UUID.fromString(newUser.subject())
        );
        return ResponseEntity.created(uri).body(newUser);
    }

    @PutMapping("/email-update")
    @Transactional
    @RequiresPermission("USER:UP")
    public ResponseEntity<?> updateEmail(
            @Valid @RequestBody UpdateEmailDTO updateEmailDTO,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        userService.updateEmail(updateEmailDTO);
        logger.info(
                "USER ID {}( {} ) UPDATED EMAIL to {}",
                user.subject(),
                user.nickname(),
                DataMask.maskEmail(updateEmailDTO.email())
        );
        entryLog(
                user.subject(),
                user.nickname(),
                "UPDATE EMAIL to " + updateEmailDTO.email(),
                user.subject()
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password-update")
    @Transactional
    @RequiresPermission("USER:UP")
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        userService.updatePassword(updatePasswordDTO);
        logger.info("USER ID {}( {} ) UPDATED PASSWORD",user.subject(), user.nickname());
        entryLog(user.subject(), user.nickname(), "UPDATE PASSWORD", user.subject());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{subject}")
    @Transactional
    @RequiresPermission("USER:DL")
    public ResponseEntity<?> delete(
            @PathVariable UUID subject,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        userService.deleteUser(subject);
        logger.info("USER ID {} DELETED by USER ID {}( {} )", subject, user.nickname(), user.subject());
        entryLog(user.subject(), user.nickname(), "DELETE", user.subject());
        return ResponseEntity.noContent().build();
    }

    private void entryLog(UUID userId, String userNickname, String action, UUID userTargetId){
        userLogService.save(
                new UserLog.Builder()
                        .setUserId(userId)
                        .setUserNickname(userNickname)
                        .setAction(action)
                        .setUserTargetId(userTargetId)
                        .build()
        );
    }
}
