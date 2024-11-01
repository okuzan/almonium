package com.almonium.auth.common.controller;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.annotation.Auth;
import com.almonium.auth.common.dto.request.EmailRequestDto;
import com.almonium.auth.common.dto.response.PrincipalDto;
import com.almonium.auth.common.exception.BadAuthActionRequest;
import com.almonium.auth.common.model.entity.Principal;
import com.almonium.auth.common.service.AuthMethodManagementService;
import com.almonium.auth.token.service.impl.AuthTokenService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class AuthManagementController {
    AuthMethodManagementService authMethodManagementService;
    AuthTokenService authTokenService;

    @GetMapping("/providers")
    public ResponseEntity<List<PrincipalDto>> getAuthProviders(@Auth Principal auth) {
        return ResponseEntity.ok(
                authMethodManagementService.getAuthProviders(auth.getUser().getId()));
    }

    @PostMapping("/email-verification/request")
    public ResponseEntity<?> requestEmailVerification(@Auth Principal auth) {
        boolean verified =
                authMethodManagementService.isEmailVerified(auth.getUser().getId());
        if (verified) {
            throw new BadAuthActionRequest("Email is already verified");
        }

        authMethodManagementService.sendEmailVerification(auth.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email-availability")
    public ResponseEntity<Boolean> checkEmailAvailability(@RequestBody EmailRequestDto request) {
        boolean isAvailable = authMethodManagementService.isEmailAvailable(request.email());
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/email-verified")
    public ResponseEntity<?> isEmailVerified(@Auth Principal auth) {
        boolean verified =
                authMethodManagementService.isEmailVerified(auth.getUser().getId());
        return ResponseEntity.ok(verified);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Auth Principal auth, HttpServletResponse response) {
        authTokenService.revokeRefreshTokensByUser(auth.getUser());
        authTokenService.clearTokenCookies(response);
        return ResponseEntity.ok().build();
    }
}
