package com.panstone.domain.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityModifyException extends RuntimeException {

    private Object value;
    private String errorCode;

    public EntityModifyException(Object value, String errorCode, String message) {
        super(message);
        this.value = value;
        this.errorCode = errorCode;
    }

}
