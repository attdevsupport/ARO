/**
 * Copyright 2016 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.att.aro.ui.view.menu.profiles;

import java.util.Collection;

import com.att.aro.core.configuration.pojo.Profile;
import com.att.aro.ui.model.DataTableModel;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * Implements the Data Model for the Profiles Table on the Profile Selection
 * Dialog.
 */
class ProfileListTableModel extends DataTableModel<Profile> {

	private static final long serialVersionUID = 1L;

	private static final int PROFILE_NAME = 0;
	private static final int PROFILE_TYPE = 1;

	public ProfileListTableModel(Collection<Profile> profileNames) {
		super(new String[] { "", "" }, profileNames);
	}

	@Override
	protected Object getColumnValue(Profile item, int columnIndex) {
		if (columnIndex == PROFILE_NAME) {
			return item.getName();
		} else if (columnIndex == PROFILE_TYPE) {
			return ResourceBundleHelper.getEnumString(item.getProfileType());
		}
		return null;
	}
}
