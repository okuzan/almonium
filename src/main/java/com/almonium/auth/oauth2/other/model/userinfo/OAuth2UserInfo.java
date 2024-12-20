package com.almonium.auth.oauth2.other.model.userinfo;

import com.almonium.auth.common.model.enums.AuthProviderType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class OAuth2UserInfo {
    public static final String SUB = "sub";
    public static final String EMAIL = "email";
    public static final String EMAIL_VERIFIED = "email_verified";

    protected Map<String, Object> attributes;

    public abstract AuthProviderType getProvider();

    public abstract String getId();

    public abstract String getFirstName();

    public abstract String getLastName();

    public String getName() {
        String firstName = getFirstName();
        String lastName = getLastName();
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public abstract String getEmail();

    public abstract String getAvatarUrl();

    public abstract boolean isEmailVerified();

    protected String getStringAttribute(String attributeName) {
        Object value = attributes.get(attributeName);
        return (value instanceof String) ? (String) value : null;
    }

    protected String getNestedStringAttribute(String nestedAttributeName) {
        String[] nestedAttributes = nestedAttributeName.split("\\.");
        Object currentObj = attributes;
        for (String attr : nestedAttributes) {
            if (currentObj instanceof Map) {
                currentObj = ((Map<?, ?>) currentObj).get(attr);
            } else {
                return null;
            }
        }
        return (currentObj instanceof String) ? (String) currentObj : null;
    }
}
