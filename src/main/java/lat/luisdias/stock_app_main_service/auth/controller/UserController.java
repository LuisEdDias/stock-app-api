package lat.luisdias.stock_app_main_service.auth.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.auth.dto.admin.StoreRootUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.GetUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.StoreUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.UpdateEmailDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.UpdatePasswordDTO;
import lat.luisdias.stock_app_main_service.auth.entities.logs.UserLog;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.services.user.UserLogService;
import lat.luisdias.stock_app_main_service.auth.services.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;

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
    @PreAuthorize("hasAuthority('ROOT')")
    public ResponseEntity<Page<GetUserDTO>> findAll(
            @PageableDefault(sort = {"id"}) Pageable pageable,
            @AuthenticationPrincipal User user
    ){
        var page = userService.findAll(pageable);
        logger.info("USER ID {}( {} ) GOT ALL USERS",user.getId(), user.getNickname());
        return ResponseEntity.ok().body(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOT') or #id == authentication.principal.getId()")
    public ResponseEntity<GetUserDTO> findById(
            @PathVariable @Positive(message = "{validation.only_positive_number}") @P("id") Long id,
            @AuthenticationPrincipal User user
    ){
        logger.info("USER ID {}( {} ) GOT DATA from USER ID {}",user.getId(), user.getNickname(), id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping("/create")
    @Transactional
    @PreAuthorize("hasAuthority('ROOT')")
    public ResponseEntity<GetUserDTO> store(
            @Valid @RequestBody StoreUserDTO storeUserDTO,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal User user
            ) {
        var newUser = userService.storeUser(storeUserDTO);
        var uri = uriBuilder.path("/v1/user/{id}").buildAndExpand(newUser.id()).toUri();
        logger.info(
                "USER ID {}( {} ) CREATED by USER ID {}( {} )",
                newUser.id(),
                newUser.nickname(),
                user.getId(),
                user.getNickname()
        );
        entryLog(
                user.getId(),
                user.getNickname(),
                "CREATE USER",
                newUser.id()
        );
        return ResponseEntity.created(uri).body(newUser);
    }

    @PutMapping("/email-update")
    @Transactional
    public ResponseEntity<?> updateEmail(
            @Valid @RequestBody UpdateEmailDTO updateEmailDTO,
            @AuthenticationPrincipal User user
    ) {
        userService.updateEmail(updateEmailDTO, user);
        logger.info(
                "USER ID {}( {} ) UPDATED EMAIL to {}",
                user.getId(),
                user.getNickname(),
                maskEmail(updateEmailDTO.email())
        );
        entryLog(
                user.getId(),
                user.getNickname(),
                "UPDATE EMAIL to " + updateEmailDTO.email(),
                user.getId()
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password-update")
    @Transactional
    public ResponseEntity<?> updatePassword(
            @Valid @RequestBody UpdatePasswordDTO updatePasswordDTO,
            @AuthenticationPrincipal User user
    ) {
        userService.updatePassword(updatePasswordDTO, user);
        logger.info("USER ID {}( {} ) UPDATED PASSWORD",user.getId(), user.getNickname());
        entryLog(user.getId(), user.getNickname(), "UPDATE PASSWORD", user.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Transactional
    @PreAuthorize("hasAuthority('ROOT')")
    public ResponseEntity<?> delete(
            @PathVariable @Positive(message = "{validation.only_positive_number}") Long id,
            @AuthenticationPrincipal User user
    ) {
        userService.deleteUser(id);
        logger.info("USER ID {} DELETED by USER ID {}( {} )", id, user.getNickname(), user.getId());
        entryLog(user.getId(), user.getNickname(), "DELETE", user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-root")
    public ResponseEntity<String> storeRootUser(
            @RequestBody @Valid StoreRootUserDTO storeRootUserDTO,
            UriComponentsBuilder uriBuilder
    ) {
        HashMap<String, String> creationMap = userService.storeRootUser(storeRootUserDTO);
        var uri = uriBuilder.path("/v1/user/{id}").buildAndExpand(creationMap.get("userId")).toUri();
        logger.info(
                "ROOT USER was CREATED with ID: {} and EMAIL: {}",
                creationMap.get("userId"),
                maskEmail(storeRootUserDTO.email())
        );

        entryLog(
                Long.valueOf(creationMap.get("userId")),
                storeRootUserDTO.username(),
                "ROOT USER was CREATED with ID: "
                        + creationMap.get("userId")
                        + " and EMAIL: "
                        + maskEmail(storeRootUserDTO.email()),
                null);
        return ResponseEntity.created(uri).body(creationMap.get("twoFAQrcode"));
    }

    private void entryLog(Long userId, String userNickname, String action, Long userTargetId){
        userLogService.save(
                new UserLog.Builder()
                        .setUserId(userId)
                        .setUserNickname(userNickname)
                        .setAction(action)
                        .setUserTargetId(userTargetId)
                        .build()
        );
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        return atIndex > 2 ? email.substring(0, 2) + "***" + email.substring(atIndex) : "***" + email.substring(atIndex);
    }
}
