package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import com.sysadminanywhere.directory.service.UsersService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    /**
     * Получение всех пользователей с постраничным выводом и фильтрацией
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserEntry>> getAll(
            @ParameterObject Pageable pageable,
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            Page<UserEntry> result = usersService.getAll(pageable, filters, attributes);
            log.info("Retrieved users with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение списка пользователей без постраничного вывода
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserEntry>> getList(
            @RequestParam String filters,
            @RequestParam String[] attributes) {

        try {
            List<UserEntry> result = usersService.getAll(filters, attributes);
            log.info("Retrieved users list with filters");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid LDAP filter: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error retrieving users list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Получение пользователя по CN (Common Name)
     */
    @GetMapping("/{cn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserEntry> getByCN(@PathVariable String cn) {
        try {
            if (cn == null || cn.isBlank()) {
                log.warn("Invalid CN provided");
                return ResponseEntity.badRequest().build();
            }

            UserEntry result = usersService.getByCN(cn);
            log.info("Retrieved user by CN: {}", cn);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error retrieving user by CN: {}", cn, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Создание нового пользователя
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserEntry> add(@Valid @RequestBody AddUserDto addUser) {
        try {
            validateAddUserDto(addUser);

            UserEntry result = usersService.add(
                    addUser.getDistinguishedName(),
                    addUser.getCn(),
                    addUser.getDisplayName(),
                    addUser.getFirstName(),
                    addUser.getLastName(),
                    addUser.getInitials(),
                    addUser.getPassword(),
                    addUser.isCannotChangePassword(),
                    addUser.isPasswordNeverExpires(),
                    addUser.isAccountDisabled(),
                    addUser.isMustChangePassword()
            );

            log.info("User created: {}", addUser.getCn());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid user data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Обновление пользователя
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserEntry> update(@Valid @RequestBody UserEntry user) {
        try {
            if (user == null || user.getDistinguishedName() == null || user.getDistinguishedName().isBlank()) {
                log.warn("Invalid user data for update");
                return ResponseEntity.badRequest().build();
            }

            UserEntry result = usersService.update(user);
            log.info("User updated: {}", user.getDistinguishedName());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Удаление пользователя
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @RequestParam @NotBlank(message = "DistinguishedName cannot be empty") String distinguishedName) {

        try {
            usersService.delete(distinguishedName);
            log.info("User deleted: {}", distinguishedName);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("Error deleting user: {}", distinguishedName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user"));
        }
    }

    /**
     * Сброс пароля пользователя
     */
    @PostMapping("/resetpassword")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            if (resetPasswordDto == null || resetPasswordDto.getDistinguishedName() == null ||
                resetPasswordDto.getDistinguishedName().isBlank()) {

                log.warn("Invalid distinguished name for password reset");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "DistinguishedName cannot be empty"));
            }

            if (resetPasswordDto.getPassword() == null || resetPasswordDto.getPassword().isBlank()) {
                log.warn("Invalid password for password reset");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Password cannot be empty"));
            }

            usersService.resetPassword(resetPasswordDto.getDistinguishedName(), resetPasswordDto.getPassword());
            log.warn("Password reset for user: {}", resetPasswordDto.getDistinguishedName());

            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));

        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset password"));
        }
    }

    /**
     * Изменение статуса учетной записи пользователя
     */
    @PostMapping("/changeuac")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeUserAccountControl(
            @Valid @RequestBody ChangeUserAccountControlDto changeUserAccountControlDto) {

        try {
            if (changeUserAccountControlDto == null || changeUserAccountControlDto.getUser() == null) {
                log.warn("Invalid user for UAC change");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User cannot be empty"));
            }

            usersService.changeUserAccountControl(
                    changeUserAccountControlDto.getUser(),
                    changeUserAccountControlDto.isCannotChangePassword(),
                    changeUserAccountControlDto.isPasswordNeverExpires(),
                    changeUserAccountControlDto.isAccountDisabled(),
                    changeUserAccountControlDto.isMustChangePassword());

            log.info("User account control changed for: {}", changeUserAccountControlDto.getUser());

            return ResponseEntity.ok(Map.of("message", "User account control updated successfully"));

        } catch (Exception e) {
            log.error("Error changing user account control", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to change user account control"));
        }
    }

    /**
     * Валидация AddUserDto
     */
    private void validateAddUserDto(AddUserDto addUser) {
        if (addUser == null || addUser.getDistinguishedName() == null || addUser.getDistinguishedName().isBlank()) {
            throw new IllegalArgumentException("DistinguishedName cannot be empty");
        }
        if (addUser.getCn() == null || addUser.getCn().isBlank()) {
            throw new IllegalArgumentException("CN cannot be empty");
        }
        if (addUser.getPassword() == null || addUser.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }
}