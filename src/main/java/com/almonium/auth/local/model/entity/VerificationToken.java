package com.almonium.auth.local.model.entity;

import com.almonium.auth.local.model.enums.TokenType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(targetEntity = LocalPrincipal.class)
    @JoinColumn(name = "principal_id")
    private LocalPrincipal principal;

    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    public VerificationToken(LocalPrincipal principal, String token, TokenType tokenType, long minutes) {
        this.principal = principal;
        this.token = token;
        this.tokenType = tokenType;
        expiryDate = LocalDateTime.now().plusMinutes(minutes);
    }
}
