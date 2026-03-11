package com.theodo.springblueprint.common.api.exceptionhandling;

import com.theodo.springblueprint.common.api.exceptionhandling.base.ErrorCode;
import lombok.Getter;

@Getter
public enum SpringErrorCodes implements ErrorCode {
    ACCESS_DENIED_ERROR("errors.access_denied"),
    INPUT_FORMAT_ERROR("errors.input_bad_format"),
    INPUT_VALIDATION_ERROR("errors.input_failed_validation"),
    MISSING_BODY_ERROR("errors.missing_body");

    private final String code;

    SpringErrorCodes(String code) {
        this.code = code;
    }
}
