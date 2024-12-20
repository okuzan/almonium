package com.almonium.user.core.service.impl;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

import com.almonium.user.core.model.entity.Profile;
import com.almonium.user.core.repository.ProfileRepository;
import com.almonium.user.core.service.ProfileService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = PRIVATE)
class ProfileServiceTest {

    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    ProfileService profileService;

    @DisplayName("Should increase streak if last login is on the previous day")
    @Test
    void givenLastLoginIsPreviousDay_whenUpdateLoginStreak_thenStreakIsIncreased() {
        Profile profile = new Profile();
        LocalDateTime lastLogin = LocalDateTime.now().minusDays(1);
        profile.setLastLogin(lastLogin);
        profile.setStreak(5);
        profileService.updateLoginStreak(profile);

        assertThat(profile.getStreak()).isEqualTo(6);
        assertThat(profile.getLastLogin().toLocalDate()).isEqualTo(LocalDate.now());
    }

    @DisplayName("Should reset streak if last login is not on the previous day")
    @Test
    void givenLastLoginNotPreviousDay_whenUpdateLoginStreak_thenStreakIsReset() {
        Profile profile = new Profile();
        LocalDateTime lastLogin = LocalDateTime.now().minusDays(2);
        profile.setLastLogin(lastLogin);
        profile.setStreak(5);

        profileService.updateLoginStreak(profile);

        assertThat(profile.getStreak()).isEqualTo(1);
        assertThat(profile.getLastLogin().toLocalDate()).isEqualTo(LocalDate.now());
    }
}
