package com.almonium.auth.common.controller;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.annotation.Auth;
import com.almonium.auth.common.annotation.RequireRecentLogin;
import com.almonium.auth.common.dto.request.EmailRequestDto;
import com.almonium.auth.common.dto.response.UnlinkProviderResponse;
import com.almonium.auth.common.model.entity.Principal;
import com.almonium.auth.common.model.enums.AuthProviderType;
import com.almonium.auth.common.service.SensitiveAuthActionService;
import com.almonium.auth.local.dto.request.LocalAuthRequest;
import com.almonium.auth.local.dto.request.PasswordRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequireRecentLogin
public class SensitiveActionsController {
    SensitiveAuthActionService sensitiveAuthActionService;

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Auth Principal auth, @Valid @RequestBody PasswordRequestDto request) {
        sensitiveAuthActionService.changePassword(auth.getUser().getId(), request.password());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-changes/request")
    public ResponseEntity<?> requestEmailChange(@Auth Principal auth, @RequestBody EmailRequestDto request) {
        sensitiveAuthActionService.requestEmailChange(auth.getUser().getId(), request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-changes/request/resend")
    public ResponseEntity<?> resendEmailChangeRequest(@Auth Principal auth) {
        sensitiveAuthActionService.resendEmailChangeRequest(auth.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/email-changes/request")
    public ResponseEntity<?> deleteEmailChangeRequest(@Auth Principal auth) {
        sensitiveAuthActionService.cancelEmailChangeRequest(auth.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-changes/link-local")
    public ResponseEntity<?> linkLocalWithNewEmail(@Auth Principal auth, @Valid @RequestBody LocalAuthRequest request) {
        sensitiveAuthActionService.linkLocalWithNewEmail(auth.getUser().getId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/local")
    public ResponseEntity<?> addLocalLogin(
            @Auth Principal auth, @Valid @RequestBody PasswordRequestDto passwordRequestDto) {
        sensitiveAuthActionService.linkLocal(auth.getUser().getId(), passwordRequestDto.password());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/providers/{provider}")
    public ResponseEntity<UnlinkProviderResponse> unlinkProvider(
            @Auth Principal auth, @PathVariable AuthProviderType provider) {
        sensitiveAuthActionService.unlinkAuthMethod(auth.getUser().getId(), provider);
        boolean isCurrentPrincipalBeingUnlinked = provider == auth.getProvider();
        return ResponseEntity.ok(new UnlinkProviderResponse(isCurrentPrincipalBeingUnlinked));
    }
}
