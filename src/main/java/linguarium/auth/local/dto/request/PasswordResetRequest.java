package linguarium.auth.local.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(@NotBlank String email) {}
