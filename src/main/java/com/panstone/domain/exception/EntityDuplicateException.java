package com.panstone.domain.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntityDuplicateException extends RuntimeException {

    private String filed;
    private Object value;
    private String errorCode;

    public EntityDuplicateException(String filed, Object value, String errorCode, String message) {
        super(message);
        this.filed = filed;
        this.value = value;
        this.errorCode = errorCode;
    }

}
