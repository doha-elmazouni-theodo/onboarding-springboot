package com.theodo.springblueprint.common.domain.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Role {
    USER(1),
    ADMIN(2);

    final int id;
}
