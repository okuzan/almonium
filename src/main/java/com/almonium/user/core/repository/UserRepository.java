package com.almonium.user.core.repository;

import com.almonium.user.core.model.entity.User;
import com.almonium.user.friendship.model.projection.UserToFriendProjection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("select u.id as id, u.username as username, u.email as email from User u where u.email = :email")
    Optional<UserToFriendProjection> findFriendByEmail(String email);

    @EntityGraph(value = "graph.User.details", type = EntityGraph.EntityGraphType.LOAD)
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<UserToFriendProjection> findUserById(long id);

    @EntityGraph(attributePaths = {"principals"})
    Optional<User> findById(long id);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);
}
