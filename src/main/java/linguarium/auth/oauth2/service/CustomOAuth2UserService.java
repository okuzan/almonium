package linguarium.auth.oauth2.service;

import static lombok.AccessLevel.PRIVATE;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import linguarium.auth.common.enums.AuthProviderType;
import linguarium.auth.oauth2.exception.OAuth2AuthenticationException;
import linguarium.auth.oauth2.model.enums.OAuth2Intent;
import linguarium.auth.oauth2.model.userinfo.OAuth2UserInfo;
import linguarium.auth.oauth2.model.userinfo.OAuth2UserInfoFactory;
import linguarium.auth.oauth2.util.CookieUtils;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    ProviderAuthServiceImpl authService;
    OAuth2UserInfoFactory userInfoFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        AuthProviderType provider = AuthProviderType.valueOf(
                oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OAuth2UserInfo userInfo = userInfoFactory.getOAuth2UserInfo(provider, attributes);

        validateProviderUserInfo(userInfo);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        OAuth2Intent intent = CookieUtils.getCookie(request, CookieUtils.INTENT_PARAM_COOKIE_NAME)
                .map(cookie -> OAuth2Intent.valueOf(cookie.getValue().toUpperCase()))
                .orElse(OAuth2Intent.SIGN_IN);

        try {
            return authService.authenticate(userInfo, attributes, intent);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException("Authentication failed", ex);
        }
    }

    private void validateProviderUserInfo(OAuth2UserInfo oAuth2UserInfo) {
        if (!StringUtils.hasLength(oAuth2UserInfo.getName())) {
            throw new OAuth2AuthenticationException("Name not found from OAuth2 provider");
        }

        if (!StringUtils.hasLength(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
    }
}
