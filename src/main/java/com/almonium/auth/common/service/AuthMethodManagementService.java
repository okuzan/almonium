package com.almonium.auth.common.service;

import com.almonium.auth.common.dto.response.PrincipalDto;
import java.util.List;

public interface AuthMethodManagementService {
    void changeEmail(String token);

    boolean isEmailAvailable(String email);

    List<PrincipalDto> getAuthProviders(long id);

    void verifyEmail(String token);

    void resetPassword(String token, String newPassword);
}
