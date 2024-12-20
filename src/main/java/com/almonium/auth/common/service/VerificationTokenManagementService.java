package com.almonium.auth.common.service;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.local.exception.InvalidVerificationTokenException;
import com.almonium.auth.local.model.entity.LocalPrincipal;
import com.almonium.auth.local.model.entity.VerificationToken;
import com.almonium.auth.local.model.enums.TokenType;
import com.almonium.auth.local.repository.LocalPrincipalRepository;
import com.almonium.auth.local.repository.VerificationTokenRepository;
import com.almonium.auth.local.service.TokenGenerator;
import com.almonium.infra.email.dto.EmailDto;
import com.almonium.infra.email.model.dto.EmailContext;
import com.almonium.infra.email.service.AuthTokenEmailComposerService;
import com.almonium.infra.email.service.EmailService;
import com.almonium.user.core.model.entity.User;
import com.almonium.user.core.service.UserService;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class VerificationTokenManagementService {

    @NonFinal
    @Value("${app.auth.verification-token.length}")
    int tokenLength;

    @NonFinal
    @Value("${app.auth.verification-token.lifetime}")
    int tokenLifetime;

    VerificationTokenRepository verificationTokenRepository;
    TokenGenerator tokenGenerator;
    AuthTokenEmailComposerService emailComposerService;
    EmailService emailService;
    UserService userService;
    LocalPrincipalRepository localPrincipalRepository;

    public Optional<VerificationToken> findValidEmailVerificationToken(long userId) {
        User user = userService.getUserWithPrincipals(userId);

        return userService
                .getUnverifiedLocalPrincipal(user)
                .or(() -> userService.getLocalPrincipal(user))
                .flatMap(localPrincipal -> {
                    Optional<VerificationToken> token = verificationTokenRepository.findByPrincipalAndTokenTypeIn(
                            localPrincipal, Set.of(TokenType.EMAIL_VERIFICATION, TokenType.EMAIL_CHANGE_VERIFICATION));

                    if (token.isPresent() && token.get().getExpiresAt().isAfter(Instant.now())) {
                        return token;
                    }

                    if (token.isPresent()) {
                        verificationTokenRepository.delete(token.get());
                        localPrincipalRepository.delete(localPrincipal);
                    }
                    return Optional.empty();
                });
    }

    public void createAndSendVerificationToken(LocalPrincipal localPrincipal, TokenType tokenType) {
        String token = tokenGenerator.generateOTP(tokenLength);
        verificationTokenRepository
                .findByPrincipalAndTokenTypeIn(localPrincipal, Set.of(tokenType))
                .ifPresent(verificationTokenRepository::delete);
        VerificationToken verificationToken = new VerificationToken(localPrincipal, token, tokenType, tokenLifetime);
        verificationTokenRepository.save(verificationToken);
        var emailContext = new EmailContext<>(tokenType, Map.of(AuthTokenEmailComposerService.TOKEN_ATTRIBUTE, token));
        EmailDto emailDto = emailComposerService.composeEmail(localPrincipal.getEmail(), emailContext);
        emailService.sendEmail(emailDto);
        log.info("Verification token sent to {}", localPrincipal.getEmail());
    }

    public VerificationToken validateAndDeleteTokenOrThrow(String token, TokenType expectedType) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new InvalidVerificationTokenException("Token is invalid or has been used"));

        if (verificationToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidVerificationTokenException("Verification token has expired");
        }

        if (verificationToken.getTokenType() != expectedType) {
            throw new InvalidVerificationTokenException("Invalid token type: should be " + expectedType + " but got "
                    + verificationToken.getTokenType() + " instead");
        }

        verificationTokenRepository.delete(verificationToken);
        return verificationToken;
    }

    public void deleteToken(VerificationToken verificationToken) {
        verificationTokenRepository.delete(verificationToken);
    }
}
