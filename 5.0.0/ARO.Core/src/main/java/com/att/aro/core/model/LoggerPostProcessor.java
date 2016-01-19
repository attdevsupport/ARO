package com.att.aro.core.model;

import java.lang.reflect.Field;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;

@Component
public class LoggerPostProcessor implements BeanPostProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(LoggerPostProcessor.class);
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		Field[] fields = bean.getClass().getDeclaredFields();
		for(Field field : fields){
			if(ILogger.class.isAssignableFrom(field.getType()) && field.getAnnotation(InjectLogger.class) != null){
				ILogger log = new LoggerImpl(bean.getClass().getName());
				try {
					field.setAccessible(true);
					field.set(bean, log);
				} catch (IllegalArgumentException e) {
					LOG.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

}
