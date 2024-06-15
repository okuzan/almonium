package com.almonium.auth.oauth2.model.userinfo;

import com.almonium.auth.common.enums.AuthProviderType;
import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {
    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public AuthProviderType getProvider() {
        return AuthProviderType.APPLE;
    }

    @Override
    public String getId() {
        return getStringAttribute("sub");
    }

    @Override
    public String getName() {
        // Apple returns the name only once during the first authorization
        return getNestedStringAttribute("name.firstName") + " " + getNestedStringAttribute("name.lastName");
    }

    @Override
    public String getEmail() {
        return getStringAttribute("email");
    }

    @Override
    public String getImageUrl() {
        return null; // Apple does not provide an image URL
    }
}
