/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderRole;
import org.openmrs.RelationshipType;

import java.util.List;

/**
 *  Database methods for {@link org.openmrs.api.ProviderRoleService}.
 */
public interface ProviderRoleDAO {

    /*
     * Base Methods for saving and loading provider roles
     */

    /**
     * Gets all Provider Roles in the database
     *
     * @param includeRetired whether to include retired providers or not.
     * @return list of al provider roles in the system
     */
    public List<ProviderRole> getAllProviderRoles(boolean includeRetired);

    /**
     * Gets the provider role referenced by the specified id
     *
     * @param id
     * @return providerRole
     */
    public ProviderRole getProviderRole(Integer id);

    /**
     * Gets the provider role referenced by the specified uui
     *
     * @param uuid
     * @return providerRole
     */
    public ProviderRole getProviderRoleByUuid(String uuid);

    /**
     * Gets the list of provider roles that support the specified relationship type
     * (Excludes retired provider roles)
     *
     * @param relationshipType
     * @return list of provider roles that support that relationship type
     */
    public List<ProviderRole> getProviderRolesByRelationshipType(RelationshipType relationshipType);

    /**
     * Returns all provider roles that are able to supervise the specified provider role
     * (Excluded retired provider roles)
     *
     * @param providerRole
     * @return the provider roles that can supervise the specified provider role
     */
    public List<ProviderRole> getProviderRolesBySuperviseeProviderRole(ProviderRole providerRole);

    /**
     * Saves/updates a provider role
     *
     * @param role the provider role to save
     * @return provider role
     */
    public ProviderRole saveProviderRole(ProviderRole role);

    /**
     * Deletes a provider role
     *
     * @param role the provider role to delete
     */
    public void deleteProviderRole(ProviderRole role);

	/**
	 * Gets all providers associated with the current person
	 *
	 * @param person
	 * @param includeRetired whether or not to include retired providers
	 * @return all providers associated with the current person
	 */
	public List<Provider> getProvidersByPerson(Person person, boolean includeRetired);

	/**
	 * Gets all providers with the selected provider roles
	 *
	 * @param roles
	 * @param includeRetired whether or not to include retired providers
	 * @return all providers with the selected provider roles
	 */
	public List<Provider> getProvidersByProviderRoles(List<ProviderRole> roles, boolean includeRetired);
}
