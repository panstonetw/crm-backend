package com.panstone.controller;

import jakarta.annotation.Resource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

public abstract class BaseController {

	@Resource
	private MessageSource messageSource;

	protected String getText(String code, Object... params) {
		Locale locale = LocaleContextHolder.getLocale();
		String message;
		try {
			message = messageSource.getMessage(code, params, locale);
		} catch (Exception e) {
			message = code;
		}
		return message;
	}

}
