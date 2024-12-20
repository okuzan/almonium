package com.almonium.auth.local.service;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.factory.PrincipalFactory;
import com.almonium.auth.common.service.UserAuthenticationService;
import com.almonium.auth.common.service.VerificationTokenManagementService;
import com.almonium.auth.local.dto.request.LocalAuthRequest;
import com.almonium.auth.local.dto.response.JwtAuthResponse;
import com.almonium.auth.local.exception.EmailNotVerifiedException;
import com.almonium.auth.local.exception.UserAlreadyExistsException;
import com.almonium.auth.local.model.entity.LocalPrincipal;
import com.almonium.auth.local.model.enums.TokenType;
import com.almonium.auth.local.repository.LocalPrincipalRepository;
import com.almonium.auth.token.dto.response.JwtTokenResponse;
import com.almonium.user.core.factory.UserFactory;
import com.almonium.user.core.model.entity.User;
import com.almonium.user.core.repository.UserRepository;
import com.almonium.user.core.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class PublicLocalAuthService {
    // services
    AuthenticationManager authenticationManager;
    UserAuthenticationService userAuthenticationServiceImpl;
    VerificationTokenManagementService verificationTokenManagementService;
    UserService userService;
    PrincipalFactory principalFactory;
    UserFactory userFactory;
    // repositories
    UserRepository userRepository;
    LocalPrincipalRepository localPrincipalRepository;

    @NonFinal
    @Value("${app.auth.email-verification-required}")
    boolean emailVerificationRequired;

    public JwtAuthResponse login(LocalAuthRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = validateAndGetLocalPrincipal(request);

        JwtTokenResponse tokenResponse = userAuthenticationServiceImpl.authenticateUser(user, response, authentication);

        return new JwtAuthResponse(
                tokenResponse.accessToken(), tokenResponse.refreshToken(), userService.buildUserInfoFromUser(user));
    }

    public void register(LocalAuthRequest request) {
        validateRegisterRequest(request);
        User user = userFactory.createUserWithDefaultPlan(request.email(), false);
        LocalPrincipal localPrincipal = principalFactory.createLocalPrincipal(user, request);
        localPrincipalRepository.save(localPrincipal);
        verificationTokenManagementService.createAndSendVerificationToken(localPrincipal, TokenType.EMAIL_VERIFICATION);
    }

    public void requestPasswordReset(String email) {
        localPrincipalRepository
                .findByEmail(email)
                .ifPresent(principal -> verificationTokenManagementService.createAndSendVerificationToken(
                        principal, TokenType.PASSWORD_RESET));
    }

    private User validateAndGetLocalPrincipal(LocalAuthRequest request) {
        // loadUserByUsername is executed prior to this, thus IllegalState - it should always return a user
        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User with email " + request.email() + " not found"));

        if (emailVerificationRequired && !user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email needs to be verified before logging in.");
        }
        return user;
    }

    private void validateRegisterRequest(LocalAuthRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
        }
    }
}
