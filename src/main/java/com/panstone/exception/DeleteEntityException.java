package com.panstone.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class DeleteEntityException extends RuntimeException {

	private Object value;
	private String errorCode;

}
