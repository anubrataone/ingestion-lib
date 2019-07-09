package com.att.ingestion.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParameterInfo {

	public enum Type {
		NONE, INCLUDE_RESOURCE_LOADER
	}
	
	public Type type() default Type.NONE;

}
