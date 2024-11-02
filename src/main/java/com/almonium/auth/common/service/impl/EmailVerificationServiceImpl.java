package com.almonium.auth.common.service.impl;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.exception.BadAuthActionRequest;
import com.almonium.auth.common.model.entity.Principal;
import com.almonium.auth.common.repository.PrincipalRepository;
import com.almonium.auth.common.service.EmailVerificationService;
import com.almonium.auth.common.service.VerificationTokenManagementService;
import com.almonium.auth.local.dto.response.VerificationTokenDto;
import com.almonium.auth.local.mapper.VerificationTokenMapper;
import com.almonium.auth.local.model.entity.LocalPrincipal;
import com.almonium.auth.local.model.enums.TokenType;
import com.almonium.user.core.model.entity.User;
import com.almonium.user.core.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class EmailVerificationServiceImpl implements EmailVerificationService {
    UserService userService;
    VerificationTokenManagementService tokenService;
    VerificationTokenManagementServiceImpl verificationTokenManagementService;
    PrincipalRepository principalRepository;
    VerificationTokenMapper verificationTokenMapper;

    @Override
    public void sendEmailVerification(long id) {
        User user = userService.getById(id);
        LocalPrincipal localPrincipal = userService
                .getLocalPrincipal(user)
                .orElseThrow(() -> new BadAuthActionRequest(
                        "Email verification is not available without local authentication method"));

        tokenService.createAndSendVerificationToken(localPrincipal, TokenType.EMAIL_VERIFICATION);
    }

    @Override
    public boolean isEmailVerified(long id) {
        return principalRepository.findByUserId(id).stream().anyMatch(Principal::isEmailVerified);
    }

    @Override
    public Optional<VerificationTokenDto> getLastEmailVerificationToken(long id) {
        return verificationTokenManagementService
                .findValidEmailVerificationToken(id)
                .map(verificationTokenMapper::toDto);
    }
}
