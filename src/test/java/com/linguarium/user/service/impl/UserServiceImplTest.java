package com.linguarium.user.service.impl;

import com.linguarium.auth.dto.SocialProvider;
import com.linguarium.auth.dto.UserInfo;
import com.linguarium.auth.dto.request.SignUpRequest;
import com.linguarium.auth.exception.UserAlreadyExistsAuthenticationException;
import com.linguarium.auth.model.LocalUser;
import com.linguarium.card.model.Tag;
import com.linguarium.card.repository.CardTagRepository;
import com.linguarium.card.repository.TagRepository;
import com.linguarium.translator.model.Language;
import com.linguarium.translator.repository.LanguageRepository;
import com.linguarium.user.model.User;
import com.linguarium.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.linguarium.user.service.impl.LearnerServiceTest.getUser;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CardTagRepository cardTagRepository;
    @Mock
    TagRepository tagRepository;
    @Mock
    LanguageRepository languageRepository;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, cardTagRepository, tagRepository, languageRepository);
    }

    @Test
    @DisplayName("Should register new user")
    void givenValidUserDetailsFromGoogleProvider_whenProcessUserRegistration_thenReturnsLocalUser() {
        UserServiceImpl userServiceSpy = spy(userService);

        String sub = "101868015518714862283";
        String profilePicLink = "https://lh3.googleusercontent.com/a/AAcHTtdmMGFI1asVb1fp_pQ1ypkJqEHmI6Ug67ntQfLHYNqapw=s94-c";
        String password = "changeit";
        String registrationId = "google";
        Map<String, Object> attributes = Map.ofEntries(
                entry("at_hash", "RkJFPU-iZS_amRETFhGrdA"),
                entry("sub", sub),
                entry("email_verified", true),
                entry("iss", "https://accounts.google.com"),
                entry("given_name", "John"),
                entry("locale", "uk"),
                entry("nonce", "K3TiqNu1cgnErWX962crIutE8YiEjuQAd3PDzUV0E5M"),
                entry("picture", "https://lh3.googleusercontent.com/a/AAcHTtdmMGFI1asVb1fp_pQ1ypkJqEHmI6Ug67ntQfLHYNqapw=s94-c"),
                entry("aud", new String[]{"832714080763-hj64thg1sghaubbg9m6qd288mbv09li6.apps.googleusercontent.com"}),
                entry("azp", "832714080763-hj64thg1sghaubbg9m6qd288mbv09li6.apps.googleusercontent.com"),
                entry("name", "John Wick"),
                entry("exp", "2023-06-27T15:00:44Z"),
                entry("family_name", "Wick"),
                entry("iat", "2023-06-27T14:00:44Z"),
                entry("email", "johnwick@gmail.com")
        );

        OidcIdToken idToken = new OidcIdToken("random_token_value", null, null, attributes);
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("johnwick@gmail.com")
                .password(password)
                .providerUserId(sub)
                .socialProvider(SocialProvider.GOOGLE)
                .profilePicLink(profilePicLink)
                .build();

        User user = User.builder()
                .email("johnwick@gmail.com")
                .password(password)
                .providerUserId(sub)
                .build();

        doReturn(user).when(userServiceSpy).registerNewUser(signUpRequest);

        LocalUser result = userServiceSpy.processUserRegistration(registrationId, attributes, idToken, null);

        verify(userServiceSpy).registerNewUser(eq(signUpRequest));

        assertThat(result).isNotNull();
        assertThat(result.getIdToken()).isEqualTo(idToken);
        assertThat(result.getAttributes()).isEqualTo(attributes);
        assertThat(result.getUserInfo()).isNull();
        assertThat(result.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should throw an exception when trying to register user with existing userId")
    public void givenExistingUserId_whenRegisterNewUser_thenThrowUserAlreadyExistsAuthenticationException() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUserID(1L);

        when(userRepository.existsById(signUpRequest.getUserID())).thenReturn(true);

        assertThrows(UserAlreadyExistsAuthenticationException.class,
                () -> userService.registerNewUser(signUpRequest));

        verify(userRepository).existsById(signUpRequest.getUserID());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw an exception when trying to register user with existing email")
    public void givenExistingUserEmail_whenRegisterNewUser_thenThrowUserAlreadyExistsAuthenticationException() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setEmail("john@example.com");

        when(userRepository.existsByEmail(signUpRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsAuthenticationException.class,
                () -> userService.registerNewUser(signUpRequest));

        verify(userRepository).existsByEmail(signUpRequest.getEmail());
        verify(userRepository, never()).existsById(anyLong());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).flush();
    }

    @Test
    @DisplayName("Should throw an exception when trying to register user with existing username")
    public void givenExistingUsername_whenRegisterNewUser_thenThrowUserAlreadyExistsAuthenticationException() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("john");

        when(userRepository.existsByUsername(signUpRequest.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsAuthenticationException.class, () -> userService.registerNewUser(signUpRequest));

        verify(userRepository).existsByUsername(signUpRequest.getUsername());
        verify(userRepository, never()).existsById(anyLong());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).flush();
    }

    @Test
    @DisplayName("Should change username")
    public void givenUsername_whenChangeUsername_thenUsernameChanged() {
        String username = "newUsername";
        Long id = 1L;

        when(userRepository.existsByUsername(username)).thenReturn(false);

        userService.changeUsername(username, id);

        verify(userRepository).existsByUsername(username);
        verify(userRepository).changeUsername(username, id);
    }

    @Test
    @DisplayName("Shouldn't change username for existing username")
    public void givenExistingUsername_whenChangeUsername_thenUsernameNotChanged() {
        String username = "existingUsername";
        Long id = 1L;

        when(userRepository.existsByUsername(username)).thenReturn(true);

        userService.changeUsername(username, id);

        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).changeUsername(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should return user optional for existing user")
    public void givenExistingUser_whenFindUserById_thenReturnUserOptional() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> result = userService.findUserById(userId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(user);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return empty optional for non existing user")
    public void givenNonExistingUser_whenFindUserById_thenReturnEmptyOptional() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findUserById(userId);

        assertThat(result).isEmpty();
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should delete user account")
    public void givenUser_whenDeleteAccount_thenRepositoryDeleteIsCalled() {
        User user = new User();

        userService.deleteAccount(user);

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Should return user if email exists")
    public void givenExistentEmail_whenFindByEmail_thenReturnUser() {
        String email = "john@example.com";
        User expectedUser = new User();
        expectedUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(expectedUser);

        User actualUser = userService.findUserByEmail(email);

        assertThat(expectedUser).isEqualTo(actualUser);
    }

    @Test
    @DisplayName("Should return null if email doesn't exist")
    public void givenNonExistentEmail_whenFindByEmail_thenReturnNull() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        User actualUser = userService.findUserByEmail(email);

        assertThat(actualUser).isNull();
    }

    @Test
    @DisplayName("Should build UserInfo from LocalUser")
    void givenLocalUser_whenBuildUserInfo_thenReturnUserInfo() {
        User user = getUser();

        OidcIdToken idToken = mock(OidcIdToken.class);
        OidcUserInfo oidcUserInfo = mock(OidcUserInfo.class);

        LocalUser localUser = new LocalUser(
                user.getEmail(),
                user.getPassword(),
                user,
                idToken,
                oidcUserInfo);

        Set<Long> tagIds = Set.of(1L, 2L, 3L);
        when(cardTagRepository.getLearnersTags(user.getLearner())).thenReturn(tagIds);
        when(tagRepository.getById(anyLong())).thenAnswer(invocation -> {
            Long tagId = invocation.getArgument(0);
            return new Tag(tagId, "Tag " + tagId);
        });

        UserInfo userInfo = userService.buildUserInfo(localUser);

        assertThat(userInfo).isNotNull()
                .extracting(UserInfo::getId, UserInfo::getUsername, UserInfo::getEmail, UserInfo::getUiLang,
                        UserInfo::getProfilePicLink, UserInfo::getBackground, UserInfo::getStreak)
                .containsExactly("1", "john", "john@example.com", Language.ENGLISH.getCode(),
                        "profile.jpg", "background.jpg", 5);
        assertThat(userInfo.getTags()).containsExactlyInAnyOrder("tag_1", "tag_2", "tag_3");
        assertThat(userInfo.getTargetLangs()).containsExactlyInAnyOrder(Language.GERMAN.getCode(), Language.FRENCH.getCode());
        assertThat(userInfo.getFluentLangs()).containsExactlyInAnyOrder(Language.SPANISH.getCode(), Language.RUSSIAN.getCode());

        verify(cardTagRepository).getLearnersTags(user.getLearner());
        tagIds.forEach(tagId -> verify(tagRepository).getById(eq(tagId)));
    }
}