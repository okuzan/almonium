package linguarium.auth.oauth2.repository;

import java.util.Optional;
import linguarium.auth.oauth2.model.entity.Principal;
import linguarium.auth.oauth2.model.enums.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrincipalRepository extends JpaRepository<Principal, Long> {
    Optional<Principal> findByProviderAndProviderUserId(AuthProviderType provider, String providerUserId);
}