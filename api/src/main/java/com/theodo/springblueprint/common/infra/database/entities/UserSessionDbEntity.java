package com.theodo.springblueprint.common.infra.database.entities;

import com.theodo.springblueprint.features.authentication.domain.entities.UserSession;
import com.theodo.springblueprint.features.authentication.domain.valueobjects.RefreshToken;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_sessions", uniqueConstraints = @UniqueConstraint(columnNames = "refresh_token"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSessionDbEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Nullable private Integer id;

    @Column(name = "refresh_token")
    @NotNull private String refreshToken;

    @Column(name = "expiration_date")
    @NotNull private Instant expirationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @NotNull private UserDbEntity associatedUser;

    public static UserSessionDbEntity from(UserSession userSession, UserDbEntity userDbEntity) {
        UserSessionDbEntity userSessionDbEntity = new UserSessionDbEntity();
        userSessionDbEntity.refreshToken(userSession.refreshToken().value());
        userSessionDbEntity.expirationDate(userSession.expirationDate());
        userSessionDbEntity.associatedUser(userDbEntity);
        return userSessionDbEntity;
    }

    public UserSession toUserSession() {
        return new UserSession(new RefreshToken(refreshToken), expirationDate, associatedUser.toUserPrincipal());
    }
}
