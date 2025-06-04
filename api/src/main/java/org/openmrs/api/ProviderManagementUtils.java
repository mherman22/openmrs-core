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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.openmrs.util.PrivilegeConstants.GP_RESTRICTED_ROLES;

public class ProviderManagementUtils {
	/**
	 * Returns true/false whether the relationship is active on the current date
	 * <p>
	 * Note that if a relationship ends on a certain date, it is not considered active on that date
	 *
	 * @param relationship
	 * @return
	 */
	public static boolean isRelationshipActive(Relationship relationship) {

		Date startDate = clearTimeComponent(relationship.getStartDate());
		Date endDate = relationship.getEndDate() != null ? clearTimeComponent(relationship.getEndDate()) : null;
		Date currentDate = clearTimeComponent(new Date());

		if (endDate != null && startDate.after(endDate)) {
			throw new APIException("relationship start date cannot be after end date: relationship id " + relationship.getId());
		}

		return (startDate.before(currentDate) || startDate.equals(currentDate))
			&& (endDate == null || endDate.after(currentDate));
	}


	/**
	 * Filters retired providers out of the list of passed providers
	 *
	 * @param providers
	 */
	public static void filterRetired(Collection<Provider> providers) {
		CollectionUtils.filter(providers, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return !((Provider) o).isRetired();
			}
		});
	}

	/**
	 * Filters out all relationship types that are not associated with a provider role
	 * (Does not filter out retired relationship types associated with a provider role)
	 *
	 * @param relationships
	 */
	public static void filterNonProviderRelationships(Collection<Relationship> relationships) {
		final List<RelationshipType> providerRelationshipTypes = Context.getService(ProviderRoleService.class).getAllProviderRoleRelationshipTypes(true);
		CollectionUtils.filter(relationships, new Predicate() {
			@Override
			public boolean evaluate(Object o) {
				return providerRelationshipTypes.contains(((Relationship) o).getRelationshipType());
			}
		});

	}

	/**
	 * Given a Date object, returns a Date object for the same date but with the time component (hours, minutes, seconds & milliseconds) removed
	 */
	public static Date clearTimeComponent(Date date) {
		// Get Calendar object set to the date and time of the given Date object
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		// Set time fields to zero
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

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
