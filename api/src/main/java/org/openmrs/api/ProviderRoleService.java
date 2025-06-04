package org.openmrs.api;

import org.openmrs.ProviderRole;
import org.openmrs.RelationshipType;
import org.openmrs.annotation.Authorized;

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
	public void purgeProviderRole(ProviderRole role)
		throws ProviderRoleInUseException;

	/**
	 * Get all the relationship types associated with provider roles
	 *
	 * @param includeRetired whether to include retired relationship types or not.
	 * @return all the relationship types associated with provider roles
	 */
	@Authorized(value = { PROVIDER_MANAGEMENT_API_PRIVILEGE, PROVIDER_MANAGEMENT_API_READ_ONLY_PRIVILEGE }, requireAll = false)
	public List<RelationshipType> getAllProviderRoleRelationshipTypes(boolean includeRetired);
}
