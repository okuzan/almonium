package com.almonium.user.core.service.impl;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.model.entity.Principal;
import com.almonium.auth.local.model.entity.LocalPrincipal;
import com.almonium.user.core.dto.UserInfo;
import com.almonium.user.core.exception.NoPrincipalFoundException;
import com.almonium.user.core.mapper.UserMapper;
import com.almonium.user.core.model.entity.User;
import com.almonium.user.core.repository.UserRepository;
import com.almonium.user.core.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public UserInfo buildUserInfoFromUser(User user) {
        return userMapper.userToUserInfo(getByEmail(user.getEmail()));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
    }

    // TODO fix. Eagerly fetches all user details, may be redundant for some use cases
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public void changeUsernameById(String username, long id) {
        userRepository.changeUsername(username, id);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return findByEmail(email)
                .map(user -> {
                    if (user.getPrincipals().isEmpty()) {
                        throw new NoPrincipalFoundException("User exists without any principals: " + email);
                    }

                    return getLocalPrincipal(user)
                            .orElseThrow(() -> new NoPrincipalFoundException(
                                    "Use %s to access your account instead of email and password."
                                            .formatted(collectProvidersNames(user))));
                })
                .orElseThrow(() -> new BadCredentialsException("Email or password are incorrect"));
    }

    @Override
    public User getUserWithPrincipals(long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    @Override
    public Optional<LocalPrincipal> getLocalPrincipal(User user) {
        return user.getPrincipals().stream()
                .filter(principal -> principal instanceof LocalPrincipal)
                // to omit ephemeral principal waiting for email confirmation
                .filter(principal -> principal.getEmail().equals(user.getEmail()))
                .map(principal -> (LocalPrincipal) principal)
                .findFirst();
    }

    @Override
    public Optional<LocalPrincipal> getUnverifiedLocalPrincipal(User user) {
        return user.getPrincipals().stream()
                .filter(principal -> principal instanceof LocalPrincipal)
                .filter(principal -> !principal.getEmail().equals(user.getEmail()))
                .map(principal -> (LocalPrincipal) principal)
                .findFirst();
    }

    private static String collectProvidersNames(User user) {
        return user.getPrincipals().stream()
                .map(Principal::getProvider)
                .map(Enum::name)
                .map(provider -> provider.substring(0, 1).toUpperCase()
                        + provider.substring(1).toLowerCase())
                .collect(Collectors.joining(", "));
    }
}
