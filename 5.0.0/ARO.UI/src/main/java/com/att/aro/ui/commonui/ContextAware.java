package com.att.aro.ui.commonui;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.att.aro.core.AROConfig;

public class ContextAware {

	private static ApplicationContext configContext = new AnnotationConfigApplicationContext(AROConfig.class);

	private ContextAware(){
		super();
	}

	public static ApplicationContext getAROConfigContext(){
		return configContext;
	}
	
}
