package com.theodo.springblueprint.common.infra.database.jparepositories;

import com.theodo.springblueprint.common.infra.database.entities.UserDbEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<UserDbEntity, UUID> {
    Optional<UserDbEntity> findByUsername(String userName);
}
