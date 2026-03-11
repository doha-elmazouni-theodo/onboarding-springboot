package com.theodo.springblueprint.common.infra.database.entities;

import com.theodo.springblueprint.common.domain.valueobjects.EncodedPassword;
import com.theodo.springblueprint.common.domain.valueobjects.Role;
import com.theodo.springblueprint.common.domain.valueobjects.Username;
import com.theodo.springblueprint.common.utils.collections.Immutable;
import com.theodo.springblueprint.features.authentication.domain.entities.UserCredential;
import com.theodo.springblueprint.features.authentication.domain.entities.UserPrincipal;
import com.theodo.springblueprint.features.users.domain.entities.User;
import com.theodo.springblueprint.features.users.domain.valueobjects.NewUser;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.collections.api.set.ImmutableSet;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDbEntity {

    @Id
    @Column(name = "id")
    @NotNull private UUID id;

    @Column(name = "username", unique = true)
    @NotNull private String username;

    @Column(name = "password")
    @NotNull private String password;

    @Column(name = "enabled")
    @Nullable private Boolean enabled;

    @Column(name = "name")
    @NotNull private String name;

    @Column(name = "created_at", updatable = false)
    @NotNull private Instant createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @NotNull private Collection<RoleDbEntity> roles = new ArrayList<>();

    public static UserDbEntity from(NewUser newUser) {
        List<RoleDbEntity> roles = newUser.roles().stream().map(RoleDbEntity::newFromRole).toList();
        final UserDbEntity userDbEntity = new UserDbEntity();
        userDbEntity.id(newUser.id());
        userDbEntity.name(newUser.name());
        userDbEntity.username(newUser.username().value());
        userDbEntity.password(newUser.encodedPassword().value());
        userDbEntity.createdAt(newUser.creationDateTime());
        userDbEntity.roles(roles);
        return userDbEntity;
    }

    public User toUser() {
        return new User(id, name, new Username(username), createdAt, getRoles());
    }

    public UserPrincipal toUserPrincipal() {
        return new UserPrincipal(id, new Username(username), getRoles());
    }

    public UserCredential toUserCredential() {
        return new UserCredential(toUserPrincipal(), new EncodedPassword(password()));
    }

    @NotNull private ImmutableSet<Role> getRoles() {
        return Immutable.collectSet(roles, RoleDbEntity::toRole);
    }
}
