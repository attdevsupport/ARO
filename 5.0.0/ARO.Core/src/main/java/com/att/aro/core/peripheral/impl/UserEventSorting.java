package com.att.aro.core.peripheral.impl;

import java.util.Comparator;

import com.att.aro.core.peripheral.pojo.UserEvent;

public class UserEventSorting implements Comparator<UserEvent> {
	@Override
	public int compare(UserEvent uEvent1, UserEvent uEvent2) {
		return Double.valueOf(uEvent1.getPressTime()).compareTo(uEvent2.getPressTime());
	}
}
