package com.almonium.auth.common.dto.response;

import com.almonium.auth.common.model.enums.AuthProviderType;
import java.time.Instant;

public record PrincipalDto(AuthProviderType provider, boolean emailVerified, String email, Instant createdAt) {}
