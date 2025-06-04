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

import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderRole;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.openmrs.util.PrivilegeConstants.PROVIDER_MANAGEMENT_API_PRIVILEGE;
import static org.openmrs.util.PrivilegeConstants.PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE;

public interface ProviderRoleService extends OpenmrsService {
	// TODO: make sure we are handling excluding/including retired metadata in a logical manner

	/*
	 * Basic methods for operating on provider roles
	 */

	/**
	 * Gets all Provider Roles in the database
	 *
	 * @param includeRetired whether to include retired provider roles or not.
	 * @return list of all provider roles in the system
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getAllProviderRoles(boolean includeRetired);

	/**
	 * Returns the provider roles associated with the specified provider
	 *
	 * @param provider
	 * @return the provider role associated with the specified provider
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getProviderRoles(Person provider);


	/**
	 * Gets all the provider roles that can server as supervisors of the specified provider
	 *  (Excludes retired providers)
	 *
	 * @param provider
	 * @return the list of provider roles that can supervise the specific provider
	 * @should throw API Exception if the provider is null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getProviderRolesThatCanSuperviseThisProvider(Person provider);

	/**
	 * Returns all the valid roles that the specified provider can supervise
	 *
	 * @param provider
	 * @return all the valid roles that the specified provider can supervise
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getProviderRolesThatProviderCanSupervise(Person provider);

	/**
	 * Assigns a provider role to a person
	 *
	 * @param provider the provider whose role we wish to set
	 * @param role the role to set
	 * @param identifier the identifier to associate with this provider/role combination (mandatory)
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public void assignProviderRoleToPerson(Person provider, ProviderRole role, String identifier);

	/**
	 * Unassigns a provider role from a person by retiring the provider associated with that role
	 *
	 * @param provider
	 * @param role
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public void unassignProviderRoleFromPerson(Person provider, ProviderRole role);

	/**
	 * Purges a provider role from a person by purging the provider associated with that role
	 *
	 * @param provider
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public void purgeProviderRoleFromPerson(Person provider, ProviderRole role);

	/**
	 * Returns whether or not the passed person has one or more associated providers (unretired or retired)
	 * (So note that a person that only is associated with retired Provider objects is still consider a "provider")
	 *
	 * @param person
	 * @return whether or not the passed person has one or more associated providers
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public boolean isProvider(Person person);

	/**
	 * Gets all providers whose role is in the list of specified roles
	 *
	 * @param roles
	 * @return all providers with one of the specified roles
	 * @should throw APIException if roles are empty or null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<Person> getProvidersAsPersonsByRoles(List<ProviderRole> roles);

	/**
	 * Gets all providers whose role is in the list of specified roles
	 *
	 * @param roles
	 * @return all providers with one of the specified roles
	 * @should throw APIException if roles are empty or null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<Provider> getProvidersByRoles(List<ProviderRole> roles);

	/**
	 * Gets all providers with the specified role
	 * (Excludes retired providers)
	 *
	 * @param role
	 * @return list of providers with the specified role
	 * @should throw APIException if role is null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<Person> getProvidersAsPersonsByRole(ProviderRole role);

	/**
	 * Gets all providers that support the specified relationship type
	 *  (Excludes retired providers)
	 *
	 * @param relationshipType
	 * @return the list of providers that support the specified relationship type
	 * @should throw API Exception if relationship type is null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<Person> getProvidersAsPersonsByRelationshipType(RelationshipType relationshipType);

	/**
	 * Returns whether or not the passed provider has the specified provider role
	 *
	 * @param provider
	 * @param role
	 * @return whether or not the passed provider has the specified provider role
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public boolean hasRole(Person provider, ProviderRole role);

	/**
	 * Returns true if the specified provider can support the specified relationship type, false otherwise
	 *
	 * @param provider
	 * @param relationshipType
	 * @return true if the specified provider can support the specified relationship type, false otherwise
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public boolean supportsRelationshipType(Person provider, RelationshipType relationshipType);

	/**
	 * Returns true if the specified supervisor can supervise the specified supervisee, false otherwise
	 *
	 * @param supervisor
	 * @param supervisee
	 * @return true if the specified supervisor can supervise the specified supervisee, false otherwise
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public boolean canSupervise(Person supervisor, Person supervisee);

	/**
	 * Gets restricted Provider Roles in the database
	 *
	 * @param includeRetired whether to include retired provider roles or not.
	 * @return list of restricted provider roles in the system
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getRestrictedProviderRoles(boolean includeRetired);

	/**
	 * Gets the provider role referenced by the specified id
	 *
	 * @param id
	 * @return providerRole
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public ProviderRole getProviderRole(Integer id);

	/**
	 * Gets the provider role referenced by the specified uui
	 *
	 * @param uuid
	 * @return providerRole
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public ProviderRole getProviderRoleByUuid(String uuid);

	/**
	 * Returns all the provider roles that support the specified relationship type
	 * (Excludes retired provider roles)
	 *
	 * @param relationshipType
	 * @return the provider roles that support that relationship type
	 * @should throw exception if relationshipType is null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getProviderRolesByRelationshipType(RelationshipType relationshipType);

	/**
	 * Returns all provider roles that are able to supervise the specified provider role
	 * (Excludes retired provider roles)
	 *
	 * @param providerRole
	 * @return the provider roles that can supervise the specified provider role
	 * @should throw exception if providerRole is null
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<ProviderRole> getProviderRolesBySuperviseeProviderRole(ProviderRole providerRole);

	/**
	 * Saves/updates a provider role
	 *
	 * @param role the provider role to save
	 * @return the saved provider role
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public ProviderRole saveProviderRole(ProviderRole role);

	/**
	 * Retires a provider role
	 * @param role the role to retire
	 * @param reason the reason the role is being retired
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public void retireProviderRole(ProviderRole role, String reason);

	/**
	 * Unretires a provider role
	 * @param role the role to unretire
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public void unretireProviderRole(ProviderRole role);

	/**
	 * Deletes a provider role
	 *
	 * @param role the provider role to delete
	 */
	@Authorized(PROVIDER_MANAGEMENT_API_PRIVILEGE)
	public void purgeProviderRole(ProviderRole role) throws ProviderRoleInUseException;

	/**
	 * Get all the relationship types associated with provider roles
	 *
	 * @param includeRetired whether to include retired relationship types or not.
	 * @return all the relationship types associated with provider roles
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<RelationshipType> getAllProviderRoleRelationshipTypes(boolean includeRetired);

	@Transactional(readOnly = true)
	List<Provider> getProvidersByPerson(Person person, boolean includeRetired);
}
