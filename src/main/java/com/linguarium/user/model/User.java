package com.linguarium.user.model;

import com.linguarium.auth.dto.AuthProvider;
import com.linguarium.friendship.model.Friendship;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@NamedEntityGraph(
        name = "graph.User.details",
        attributeNodes = {
            @NamedAttributeNode(value = "learner", subgraph = "learner.details"),
            @NamedAttributeNode("profile")
        },
        subgraphs = {
            @NamedSubgraph(
                    name = "learner.details",
                    attributeNodes = {@NamedAttributeNode("targetLangs"), @NamedAttributeNode("fluentLangs")})
        })
@Entity
@Table(name = "auth")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class User implements OAuth2User, UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(nullable = false)
    String password;

    @Column(unique = true)
    String email;

    @Column(unique = true)
    String username;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    LocalDateTime registered;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    AuthProvider provider = AuthProvider.LOCAL;

    String providerUserId;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    Profile profile;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    Learner learner;

    @OneToMany(mappedBy = "requestee")
    @OnDelete(action = OnDeleteAction.CASCADE)
    Set<Friendship> incomingFriendships;

    @OneToMany(mappedBy = "requester")
    @OnDelete(action = OnDeleteAction.CASCADE)
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

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // OAuth2User methods
    @Transient
    @Builder.Default
    Map<String, Object> attributes = new HashMap<>();

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }
}
