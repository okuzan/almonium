package com.almonium.user.core.controller;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.analyzer.translator.model.enums.Language;
import com.almonium.auth.common.annotation.Auth;
import com.almonium.auth.common.model.entity.Principal;
import com.almonium.user.core.dto.LanguageSetupRequest;
import com.almonium.user.core.dto.LanguageUpdateRequest;
import com.almonium.user.core.dto.UserInfo;
import com.almonium.user.core.dto.UsernameAvailability;
import com.almonium.user.core.dto.UsernameUpdateRequest;
import com.almonium.user.core.service.LearnerService;
import com.almonium.user.core.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    LearnerService learnerService;

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(@Auth Principal auth) {
        return ResponseEntity.ok(userService.buildUserInfoFromUser(auth.getUser()));
    }

    @GetMapping("/{username}/availability/")
    public ResponseEntity<UsernameAvailability> checkUsernameAvailability(@PathVariable String username) {
        boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(new UsernameAvailability(isAvailable));
    }

    @PutMapping("/me/username")
    public ResponseEntity<Void> updateUsername(@RequestBody UsernameUpdateRequest request, @Auth Principal auth) {
        userService.changeUsernameById(request.newUsername(), auth.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/langs")
    public ResponseEntity<Void> setupLanguages(@RequestBody LanguageSetupRequest request, @Auth Principal auth) {
        learnerService.setupLanguages(
                request.fluentLangs(), request.targetLangs(), auth.getUser().getLearner());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/langs/target/{code}")
    public ResponseEntity<Void> addTargetLanguage(@PathVariable Language code, @Auth Principal auth) {
        learnerService.addTargetLanguage(code, auth.getUser().getLearner().getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me/langs/target/{code}")
    public ResponseEntity<Void> removeTargetLanguage(@PathVariable Language code, @Auth Principal auth) {
        learnerService.removeTargetLanguage(code, auth.getUser().getLearner().getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/langs/fluent")
    public ResponseEntity<Void> updateFluentLanguages(
            @Valid @RequestBody LanguageUpdateRequest request, @Auth Principal auth) {
        learnerService.updateFluentLanguages(request.langCodes(), auth.getUser().getLearner());
        return ResponseEntity.noContent().build();
    }
}
