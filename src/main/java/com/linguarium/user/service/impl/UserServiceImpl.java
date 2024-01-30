package com.linguarium.user.service.impl;

import com.linguarium.auth.dto.SocialProvider;
import com.linguarium.auth.dto.UserInfo;
import com.linguarium.auth.dto.request.RegistrationRequest;
import com.linguarium.auth.exception.OAuth2AuthenticationProcessingException;
import com.linguarium.auth.exception.UserAlreadyExistsAuthenticationException;
import com.linguarium.auth.model.LocalUser;
import com.linguarium.card.repository.CardTagRepository;
import com.linguarium.card.repository.TagRepository;
import com.linguarium.config.security.oauth2.user.OAuth2UserInfo;
import com.linguarium.config.security.oauth2.user.OAuth2UserInfoFactory;
import com.linguarium.translator.model.Language;
import com.linguarium.user.model.Learner;
import com.linguarium.user.model.Profile;
import com.linguarium.user.model.User;
import com.linguarium.user.repository.UserRepository;
import com.linguarium.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.linguarium.util.GeneralUtils.generateId;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    CardTagRepository cardTagRepository;
    TagRepository tagRepository;

    @Override
    public User findUserByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void deleteAccount(User user) {
        userRepository.delete(user);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public void changeUsername(String username, Long id) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.changeUsername(username, id);
        }
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(value = "transactionManager")
    public User registerNewUser(final RegistrationRequest registrationRequest)
            throws UserAlreadyExistsAuthenticationException {
        if (registrationRequest.getUserID() != null && userRepository.existsById(registrationRequest.getUserID())) {
            throw new UserAlreadyExistsAuthenticationException("User with id "
                    + registrationRequest.getUserID() + " already exists");
        } else if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new UserAlreadyExistsAuthenticationException("User with email id "
                    + registrationRequest.getEmail() + " already exists");
        } else if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new UserAlreadyExistsAuthenticationException("User with username "
                    + registrationRequest.getUsername() + " already exists");
        }
        User user = buildUser(registrationRequest);
        LocalDateTime now = LocalDateTime.now();
        user.setRegistered(now);
        user.setUsername(generateId()); //TODO set real username

        Learner learner = new Learner();
        learner.setUser(user);
        learner.setTargetLangs(Set.of(Language.EN.name()));
        user.setLearner(learner);

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setLastLogin(now);
        profile.setProfilePicLink(registrationRequest.getProfilePicLink()); //TODO null?
        user.setProfile(profile);

        user = userRepository.save(user);
        return user;
    }

    @Override
    @Transactional
    public LocalUser processUserRegistration(String registrationId,
                                             Map<String, Object> attributes,
                                             OidcIdToken idToken,
                                             OidcUserInfo userInfo) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        if (!StringUtils.hasLength(oAuth2UserInfo.getName())) {
            throw new OAuth2AuthenticationProcessingException("Name not found from OAuth2 provider");
        } else if (!StringUtils.hasLength(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        RegistrationRequest userDetails = toUserRegistrationObject(registrationId, oAuth2UserInfo);
        User user = findUserByEmail(oAuth2UserInfo.getEmail());
        if (user != null) {
            if (!user.getProvider().equals(registrationId)
                    && !user.getProvider().equals(SocialProvider.LOCAL.getProviderType())) {
                throw new OAuth2AuthenticationProcessingException(
                        "Looks like you're signed up with "
                                + user.getProvider()
                                + " account. Please use your "
                                + user.getProvider()
                                + " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(userDetails);
        }

        return new LocalUser(user, attributes, idToken, userInfo);
    }

    @Override
    @Transactional
    public UserInfo buildUserInfo(User user) {
        Learner learner = user.getLearner();
        Profile profile = user.getProfile();

        List<String> tags = cardTagRepository.getLearnersTags(user.getLearner())
                .stream().map(tagId -> tagRepository
                        .findById(tagId)
                        .orElseThrow()
                        .getText())
                .collect(Collectors.toList());

        return new UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),

                profile.getUiLang().name(),
                profile.getProfilePicLink(),
                profile.getBackground(),
                profile.getStreak(),

                learner.getTargetLangs(),
                learner.getFluentLangs(),

                tags);
    }

    private User buildUser(final RegistrationRequest formDTO) {
        User user = new User();
        user.setUsername(formDTO.getUsername());
        user.setEmail(formDTO.getEmail());
        user.setPassword(passwordEncoder.encode(formDTO.getPassword()));
        user.setProfile(Profile.builder().profilePicLink(formDTO.getProfilePicLink()).build());
        user.setProvider(formDTO.getSocialProvider().getProviderType());
        user.setProviderUserId(formDTO.getProviderUserId());
        return user;
    }

    private RegistrationRequest toUserRegistrationObject(String registrationId, OAuth2UserInfo oAuth2UserInfo) {
        return RegistrationRequest.builder()
                .providerUserId(oAuth2UserInfo.getId())
                .email(oAuth2UserInfo.getEmail())
                .profilePicLink(oAuth2UserInfo.getImageUrl())
                .socialProvider(SocialProvider.toSocialProvider(registrationId))
                .password("changeit")
                .build();
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.getProfile().setProfilePicLink(oAuth2UserInfo.getImageUrl());
        return userRepository.save(existingUser);
    }
}
