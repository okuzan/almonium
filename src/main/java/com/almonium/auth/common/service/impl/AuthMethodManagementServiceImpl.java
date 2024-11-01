package com.almonium.auth.common.service.impl;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.model.entity.Principal;
import com.almonium.auth.common.model.enums.AuthProviderType;
import com.almonium.auth.common.repository.PrincipalRepository;
import com.almonium.auth.common.service.AuthMethodManagementService;
import com.almonium.auth.common.service.SensitiveAuthActionService;
import com.almonium.auth.common.service.VerificationTokenManagementService;
import com.almonium.auth.local.model.entity.LocalPrincipal;
import com.almonium.auth.local.model.entity.VerificationToken;
import com.almonium.auth.local.model.enums.TokenType;
import com.almonium.auth.local.repository.LocalPrincipalRepository;
import com.almonium.auth.local.service.impl.PasswordEncoderService;
import com.almonium.user.core.exception.NoPrincipalFoundException;
import com.almonium.user.core.model.entity.User;
import com.almonium.user.core.repository.UserRepository;
import com.almonium.user.core.service.UserService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class AuthMethodManagementServiceImpl implements AuthMethodManagementService {
    UserService userService;
    UserRepository userRepository;
    SensitiveAuthActionService sensitiveAuthActionService;
    PasswordEncoderService passwordEncoderService;
    PrincipalRepository principalRepository;
    VerificationTokenManagementService verificationTokenManagementService;
    LocalPrincipalRepository localPrincipalRepository;

    @Override
    public List<AuthProviderType> getAuthProviders(long id) {
        return principalRepository.findByUserId(id).stream()
                .map(Principal::getProvider)
                .toList();
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }

    @Override
    public void sendEmailVerification(long id) {
        User user = userService.getById(id);
        LocalPrincipal localPrincipal = userService
                .getLocalPrincipal(user)
                .orElseThrow(() ->
                        new NoPrincipalFoundException("Local auth method not found for user: " + user.getEmail()));

        verificationTokenManagementService.createAndSendVerificationToken(localPrincipal, TokenType.EMAIL_VERIFICATION);
    }

    @Transactional
    @Override
    public void changeEmail(String token) {
        VerificationToken verificationToken =
                verificationTokenManagementService.getValidTokenOrThrow(token, TokenType.EMAIL_CHANGE);
        Principal localPrincipal = verificationToken.getPrincipal();
        localPrincipal.setEmailVerified(true);
        principalRepository.save(localPrincipal);
        verificationTokenManagementService.deleteToken(verificationToken);
        log.info(
                "Email changed for local authentication method of user: {}",
                localPrincipal.getUser().getEmail());

        long userId = localPrincipal.getUser().getId();
        User user = userService.getUserWithPrincipals(userId);
        user.setEmail(localPrincipal.getEmail());
        userRepository.save(user);

        // If app enforces single password per user, unlink all other auth methods with old password
        List<Principal> principalsToUnlink = user.getPrincipals().stream()
                .filter(principal -> !Objects.equals(principal.getEmail(), localPrincipal.getEmail()))
                .toList();

        principalsToUnlink.forEach(
                principal -> sensitiveAuthActionService.unlinkAuthMethod(userId, principal.getProvider()));
        log.info(
                "{} authentications with old password unlinked for user: {}",
                principalsToUnlink.size(),
                user.getEmail());
    }

    @Override
    public boolean isEmailVerified(long id) {
        return principalRepository.findByUserId(id).stream().anyMatch(Principal::isEmailVerified);
    }

    @Override
    public void verifyEmail(String token) {
        VerificationToken verificationToken =
                verificationTokenManagementService.getValidTokenOrThrow(token, TokenType.EMAIL_VERIFICATION);
        LocalPrincipal principal = verificationToken.getPrincipal();
        principal.setEmailVerified(true);
        localPrincipalRepository.save(principal);
        verificationTokenManagementService.deleteToken(verificationToken);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken =
                verificationTokenManagementService.getValidTokenOrThrow(token, TokenType.PASSWORD_RESET);
        LocalPrincipal principal = verificationToken.getPrincipal();
        principal.setPassword(passwordEncoderService.encodePassword(newPassword));
        localPrincipalRepository.save(principal);
        verificationTokenManagementService.deleteToken(verificationToken);
    }
}
