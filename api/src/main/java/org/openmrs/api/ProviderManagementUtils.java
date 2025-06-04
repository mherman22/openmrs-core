/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.context.Context;

import java.util.Arrays;
import java.util.List;

import static org.openmrs.util.PrivilegeConstants.GP_RESTRICTED_ROLES;

public class ProviderManagementUtils {

	/**
	 * Returns a List of roles that are configured via the GP_RESTRICTED_ROLES to be included in the UI
	 * @return List<String>
	 */
	public static List<String> getRestrictedRolesGP() {
		List<String> restrictedRoles = null;
		String gpRestrictedRoles = Context.getAdministrationService().getGlobalProperty(GP_RESTRICTED_ROLES);
		if (StringUtils.isNotBlank(gpRestrictedRoles)) {
			restrictedRoles = Arrays.asList(gpRestrictedRoles.split("\\s*,\\s*"));
		}

		return restrictedRoles;
	}
}
