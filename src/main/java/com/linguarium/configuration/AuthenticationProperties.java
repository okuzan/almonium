package com.linguarium.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationProperties {
    Auth auth = new Auth();
    OAuth2 oauth2 = new OAuth2();

    @Setter
    @Getter
    public static class Auth {
        private String tokenSecret;
        private long tokenExpirationMSec;
    }

    @Getter
    public static final class OAuth2 {
        private final List<String> authorizedRedirectUris = new ArrayList<>();
    }
}
