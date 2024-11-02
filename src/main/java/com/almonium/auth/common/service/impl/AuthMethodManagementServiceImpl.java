package com.almonium.auth.common.service.impl;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.dto.response.PrincipalDto;
import com.almonium.auth.common.mapper.PrincipalMapper;
import com.almonium.auth.common.repository.PrincipalRepository;
import com.almonium.auth.common.service.AuthMethodManagementService;
import com.almonium.user.core.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class AuthMethodManagementServiceImpl implements AuthMethodManagementService {
    UserRepository userRepository;
    PrincipalRepository principalRepository;
    PrincipalMapper principalMapper;

    @Override
    public List<PrincipalDto> getAuthProviders(long id) {
        return principalMapper.toDto(principalRepository.findByUserId(id));
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }
}
