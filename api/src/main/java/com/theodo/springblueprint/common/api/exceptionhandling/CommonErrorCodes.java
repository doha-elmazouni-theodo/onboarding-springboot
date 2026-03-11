package com.theodo.springblueprint.common.api.exceptionhandling;

import com.theodo.springblueprint.common.api.exceptionhandling.base.ErrorCode;
import lombok.Getter;

@Getter
public enum CommonErrorCodes implements ErrorCode {
    UNAUTHORIZED_ERROR("errors.unauthorized");

    private final String code;

    CommonErrorCodes(String code) {
        this.code = code;
    }
}
