package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.att.aro.core.BaseTest;
import com.att.aro.core.peripheral.pojo.UserEvent;
import com.att.aro.core.peripheral.pojo.UserEvent.UserEventType;

public class UserEventSortingTest extends BaseTest {

	@Test
	public void compare() throws IOException {
		
		List<UserEvent> userEvents = new ArrayList<UserEvent>();
		
		userEvents.add(new UserEvent(UserEventType.SCREEN_PORTRAIT, 1.1, 1.6));
		userEvents.add(new UserEvent(UserEventType.SCREEN_LANDSCAPE, 1.2, 1.7));
		userEvents.add(new UserEvent(UserEventType.SCREEN_LANDSCAPE, 1.0, 1.5));
		userEvents.add(new UserEvent(UserEventType.KEY_POWER, 0.0, 1.5));
		userEvents.add(new UserEvent(UserEventType.KEY_RED, 1.411421287928E2, 1.5));
		userEvents.add(new UserEvent(UserEventType.KEY_RED, 1.411421287928E9, 1.5));
		userEvents.add(new UserEvent(UserEventType.SCREEN_TOUCH, 1.41142128792812E12, 1.5));
		
		Collections.sort(userEvents, new UserEventSorting());

		assertEquals(1.0, userEvents.get(1).getPressTime(), 0);
		assertEquals(1.1, userEvents.get(2).getPressTime(), 0);
		assertEquals(1.2, userEvents.get(3).getPressTime(), 0);
		
	}
 
}           
