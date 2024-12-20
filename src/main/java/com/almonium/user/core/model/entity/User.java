package com.almonium.user.core.model.entity;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.auth.common.model.entity.Principal;
import com.almonium.subscription.model.entity.PlanSubscription;
import com.almonium.user.friendship.model.entity.Friendship;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NamedEntityGraph(
        name = "graph.User.details",
        attributeNodes = {
            @NamedAttributeNode(value = "learner", subgraph = "learner.details"),
            @NamedAttributeNode("profile"),
            @NamedAttributeNode("planSubscriptions")
        },
        subgraphs = {
            @NamedSubgraph(
                    name = "learner.details",
                    attributeNodes = {@NamedAttributeNode("targetLangs"), @NamedAttributeNode("fluentLangs")})
        })
@Entity
@Table(name = "user_core")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String email;

    boolean emailVerified;

    String username;

    @CreatedDate
    Instant registered;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    List<Principal> principals = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    Profile profile;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    Learner learner;

    @OneToMany(mappedBy = "requestee")
    Set<Friendship> incomingFriendships;

    @OneToMany(mappedBy = "requester")
    Set<Friendship> outgoingFriendships;

    @PrePersist
    private void prePersist() {
        if (profile == null) {
            profile = Profile.builder().user(this).build();
        }
        if (learner == null) {
            learner = Learner.builder().user(this).build();
        }
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    Set<PlanSubscription> planSubscriptions;

    String stripeCustomerId;
}
