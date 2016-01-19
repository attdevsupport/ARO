package com.att.aro.core.datacollector.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import static org.junit.Assert.*;
import com.att.aro.core.BaseTest;
import com.att.aro.core.datacollector.DataCollectorType;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDataCollectorManager;
import com.att.aro.core.datacollector.impl.DataCollectorManagerImpl;

public class DataCollectorManagerImplTest extends BaseTest {
	DataCollectorManagerImpl cmg;
	@Test
	public void test(){
		IDataCollector ios = Mockito.mock(IDataCollector.class);
		Mockito.when(ios.getType()).thenReturn(DataCollectorType.IOS);
		
		List<IDataCollector> collist = new ArrayList<IDataCollector>();
		collist.add(ios);
		cmg = (DataCollectorManagerImpl)context.getBean(IDataCollectorManager.class);
		ApplicationContext context = Mockito.mock(ApplicationContext.class);
		String[] arr = {"ios"};
		Mockito.when(context.getBeanNamesForType((Class<?>) Mockito.any())).thenReturn(arr);
		Mockito.when(context.getBean("ios")).thenReturn(ios);
		List<IDataCollector> collectors = cmg.getAvailableCollectors(context);
		//TODO: check size 
		IDataCollector res = cmg.getIOSCollector();
		assertNotNull(res);
	}
	
	@Test
	public void test1(){

//		IDataCollector ios = Mockito.mock(IDataCollector.class);
//		Mockito.when(ios.getType()).thenReturn(DataCollectorType.IOS);
		
		List<IDataCollector> collist = new ArrayList<IDataCollector>();

		IDataCollector defaultCollector = Mockito.mock(IDataCollector.class);
		Mockito.when(defaultCollector.getType()).thenReturn(DataCollectorType.DEFAULT);
		collist.add(defaultCollector);
		
		IDataCollector iosCollector = Mockito.mock(IDataCollector.class);
		Mockito.when(iosCollector.getType()).thenReturn(DataCollectorType.IOS);
		collist.add(iosCollector);
		
		IDataCollector rootedCollector = Mockito.mock(IDataCollector.class);
		Mockito.when(rootedCollector.getType()).thenReturn(DataCollectorType.ROOTED_ANDROID);
		collist.add(rootedCollector);
		
		IDataCollector nonRootedCollector = Mockito.mock(IDataCollector.class);
		Mockito.when(nonRootedCollector.getType()).thenReturn(DataCollectorType.NON_ROOTED_ANDROID);
		collist.add(nonRootedCollector);
		
		cmg = (DataCollectorManagerImpl)context.getBean(IDataCollectorManager.class);
		ApplicationContext context = Mockito.mock(ApplicationContext.class);
		String[] arr = {"ios", "default", "rooted", "nonrooted"};
		Mockito.when(context.getBeanNamesForType((Class<?>) Mockito.any())).thenReturn(arr);
		Mockito.when(context.getBean("ios")).thenReturn(iosCollector);
		Mockito.when(context.getBean("default")).thenReturn(defaultCollector);
		Mockito.when(context.getBean("rooted")).thenReturn(rootedCollector);
		Mockito.when(context.getBean("nonrooted")).thenReturn(nonRootedCollector);
		
		List<IDataCollector> collectors = cmg.getAvailableCollectors(context);

		IDataCollector res = cmg.getIOSCollector();
		assertNotNull(res);
		IDataCollector res1 = cmg.getNorootedDataCollector();
		assertNotNull(res1);
		IDataCollector res2 = cmg.getRootedDataCollector();
		assertNotNull(res2);
		
		assertEquals(3, collectors.size());
	}
}
