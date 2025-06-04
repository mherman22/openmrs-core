/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderRole;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.ProviderManagementUtils;
import org.openmrs.api.ProviderRoleInUseException;
import org.openmrs.api.ProviderRoleService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProviderRoleDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProviderRoleServiceImpl extends BaseOpenmrsService implements ProviderRoleService {

    // TODO: (??? --not sure what this comment means anymore?) add checks to make sure person is not voided automatically when appropriate (in the assignment classes?)

    protected final Log log = LogFactory.getLog(this.getClass());

    private ProviderRoleDAO providerRoleDAO;

    /**
     * @param providerRoleDAO the dao to set
     */
    public void setProviderRoleDAO(ProviderRoleDAO providerRoleDAO) {
        this.providerRoleDAO = providerRoleDAO;
    }

    /**
     * @return the dao
     */
    public ProviderRoleDAO getProviderRoleDAO() {
        return providerRoleDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getAllProviderRoles(boolean includeRetired) {
        return providerRoleDAO.getAllProviderRoles(includeRetired);
    }

	/**
	 * Returns the provider roles associated with the specified provider
	 *
	 * @param provider
	 * @return the provider role associated with the specified provider
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ProviderRole> getProviderRoles(Person provider) {
		if (provider == null) {
			throw new APIException("Provider cannot be null");
		}

		if (!isProvider(provider)) {
			// return empty list if this person is not a provider
			return new ArrayList<ProviderRole>();
		}

		// otherwise, collect all the roles associated with this provider
		// (we use a set to avoid duplicates at this point)
		Set<ProviderRole> providerRoles = new HashSet<ProviderRole>();

		Collection<Provider> providers = getProvidersByPerson(provider, false);

		for (Provider p : providers) {
			if (p.getProviderRole() != null) {
				providerRoles.add(p.getProviderRole());
			}
		}

		return new ArrayList<>(providerRoles);
	}

	/**
	 * Gets all the provider roles that can server as supervisors of the specified provider
	 * (Excludes retired providers)
	 *
	 * @param provider
	 * @return the list of provider roles that can supervise the specific provider
	 * @should throw API Exception if the provider is null
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ProviderRole> getProviderRolesThatCanSuperviseThisProvider(Person provider) {

		if (provider == null) {
			throw new APIException("Provider cannot be null");
		}

		// first fetch all the roles for this provider
		List<ProviderRole> providerRoles = getProviderRoles(provider);


		// now fetch the roles that can supervise the roles this provider has
		Set<ProviderRole> providerRolesThatCanSupervise = new HashSet<ProviderRole>();

		for (ProviderRole providerRole : providerRoles) {
			List<ProviderRole> roles = Context.getService(ProviderRoleService.class).getProviderRolesBySuperviseeProviderRole(providerRole);
			if (roles != null && roles.size() > 0) {
				providerRolesThatCanSupervise.addAll(roles);
			}
		}

		return new ArrayList<ProviderRole>(providerRolesThatCanSupervise);
	}

	/**
	 * Returns all the valid roles that the specified provider can supervise
	 *
	 * @param provider
	 * @return all the valid roles that the specified provider can supervise
	 */
	@Override
	@Transactional(readOnly = true)
	public List<ProviderRole> getProviderRolesThatProviderCanSupervise(Person provider) {

		if (provider == null) {
			throw new APIException("Provider cannot be null");
		}

		Set<ProviderRole> rolesThatProviderCanSupervise = new HashSet<ProviderRole>();

		// iterate through all the provider roles this provider supports
		for (ProviderRole role : getProviderRoles(provider)) {
			// add all roles that this role can supervise
			if (role.getSuperviseeProviderRoles() != null && role.getSuperviseeProviderRoles().size() > 0) {
				rolesThatProviderCanSupervise.addAll(role.getSuperviseeProviderRoles());
			}
		}

		return new ArrayList<ProviderRole> (rolesThatProviderCanSupervise);
	}

	/**
	 * Assigns a provider role to a person
	 *
	 * @param provider   the provider whose role we wish to set
	 * @param role       the role to set
	 * @param identifier the identifier to associate with this provider/role combination (mandatory)
	 */
	@Override
	@Transactional
	public void assignProviderRoleToPerson(Person provider, ProviderRole role, String identifier) {

		if (provider == null) {
			throw new APIException("Cannot set provider role: provider is null");
		}

		if (role == null) {
			throw new APIException("Cannot set provider role: role is null");
		}

		if (provider.isVoided()) {
			throw new APIException("Cannot set provider role: underlying person has been voided");
		}

		if (hasRole(provider,role)) {
			// if the provider already has this role, do nothing
			return;
		}

		// create a new provider object and associate it with this person
		Provider p = new Provider();
		p.setPerson(provider);
		p.setIdentifier(identifier);
		p.setProviderRole(role);
		Context.getProviderService().saveProvider(p);
	}

	/**
	 * Unassigns a provider role from a person by retiring the provider associated with that role
	 *
	 * @param provider
	 * @param role
	 */
	@Override
	@Transactional
	public void unassignProviderRoleFromPerson(Person provider, ProviderRole role) {

		if (provider == null) {
			throw new APIException("Cannot set provider role: provider is null");
		}

		if (role == null) {
			throw new APIException("Cannot set provider role: role is null");
		}

		if (!hasRole(provider,role)) {
			// if the provider doesn't have this role, do nothing
			return;
		}

		// note that we don't check to make sure this provider is a person

		// iterate through all the providers and retire any with the specified role
		for (Provider p : getProvidersByPerson(provider, true)) {
			if (p.getProviderRole().equals(role)) {
				Context.getProviderService().retireProvider(p, "removing provider role " + role + " from " + provider);
			}
		}
	}

	/**
	 * Purges a provider role from a person by purging the provider associated with that role
	 *
	 * @param provider
	 * @param role
	 */
	@Override
	@Transactional
	public void purgeProviderRoleFromPerson(Person provider, ProviderRole role) {
		if (provider == null) {
			throw new APIException("Cannot set provider role: provider is null");
		}

		if (role == null) {
			throw new APIException("Cannot set provider role: role is null");
		}

		if (!hasRole(provider,role)) {
			// if the provider doesn't have this role, do nothing
			return;
		}

		// note that we don't check to make sure this provider is a person

		// iterate through all the providers and purge any with the specified role
		for (Provider p : getProvidersByPerson(provider, true)) {
			if (p.getProviderRole().equals(role)) {
				Context.getProviderService().purgeProvider(p);
			}
		}
	}

	/**
	 * Returns whether or not the passed person has one or more associated providers (unretired or retired)
	 * (So note that a person that only is associated with retired Provider objects is still consider a "provider")
	 *
	 * @param person
	 * @return whether or not the passed person has one or more associated providers
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean isProvider(Person person) {

		if (person == null) {
			throw new APIException("Person cannot be null");
		}

		Collection<Provider> providers = getProvidersByPerson(person, true);
		return providers == null || providers.size() == 0 ? false : true;
	}

	/**
	 * Gets all providers whose role is in the list of specified roles
	 *
	 * @param roles
	 * @return all providers with one of the specified roles
	 * @should throw APIException if roles are empty or null
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Person> getProvidersAsPersonsByRoles(List<ProviderRole> roles) {
		return providersToPersons(getProvidersByRoles(roles));
	}

	/**
	 * Gets all providers whose role is in the list of specified roles
	 *
	 * @param roles
	 * @return all providers with one of the specified roles
	 * @should throw APIException if roles are empty or null
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Provider> getProvidersByRoles(List<ProviderRole> roles) {
		// not allowed to pass null or empty set here
		if (roles == null || roles.isEmpty()) {
			throw new APIException("Roles cannot be null or empty");
		}
		return providerRoleDAO.getProvidersByProviderRoles(roles, false);
	}

	/**
	 * Gets all providers with the specified role
	 * (Excludes retired providers)
	 *
	 * @param role
	 * @return list of providers with the specified role
	 * @should throw APIException if role is null
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Person> getProvidersAsPersonsByRole(ProviderRole role) {
		// not allowed to pass null here
		if (role == null) {
			throw new APIException("Role cannot be null");
		}

		List<ProviderRole> roles = new ArrayList<ProviderRole>();
		roles.add(role);
		return getProvidersAsPersonsByRoles(roles);
	}

	/**
	 * Gets all providers that support the specified relationship type
	 * (Excludes retired providers)
	 *
	 * @param relationshipType
	 * @return the list of providers that support the specified relationship type
	 * @should throw API Exception if relationship type is null
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Person> getProvidersAsPersonsByRelationshipType(RelationshipType relationshipType) {

		if (relationshipType == null) {
			throw new  APIException("Relationship type cannot be null");
		}

		// first fetch the roles that support this relationship type, then fetch all the providers with those roles
		List<ProviderRole> providerRoles = Context.getService(ProviderRoleService.class).getProviderRolesByRelationshipType(relationshipType);
		if (providerRoles == null || providerRoles.size() == 0) {
			return new ArrayList<Person>();  // just return an empty list
		}
		else {
			return getProvidersAsPersonsByRoles(providerRoles);
		}
	}

	/**
	 * Returns whether or not the passed provider has the specified provider role
	 *
	 * @param provider
	 * @param role
	 * @return whether or not the passed provider has the specified provider role
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean hasRole(Person provider, ProviderRole role) {

		if (provider == null) {
			throw new APIException("Provider cannot be null");
		}

		if (role == null) {
			throw new APIException("Role cannot be null");
		}

		return getProviderRoles(provider).contains(role);
	}

	/**
	 * Returns true if the specified provider can support the specified relationship type, false otherwise
	 *
	 * @param provider
	 * @param relationshipType
	 * @return true if the specified provider can support the specified relationship type, false otherwise
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean supportsRelationshipType(Person provider, RelationshipType relationshipType) {

		Collection<Provider> providers = getProvidersByPerson(provider, false);

		if (providers == null || providers.size() == 0) {
			return false;
		}

		for (Provider p : providers) {
			if (supportsRelationshipType(p, relationshipType)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns true if the specified supervisor can supervise the specified supervisee, false otherwise
	 *
	 * @param supervisor
	 * @param supervisee
	 * @return true if the specified supervisor can supervise the specified supervisee, false otherwise
	 */
	@Override
	@Transactional(readOnly = true)
	public boolean canSupervise(Person supervisor, Person supervisee) {

		if (supervisor == null) {
			throw new APIException("Supervisor cannot be null");
		}

		if (supervisee == null) {
			throw new APIException("Provider cannot be null");
		}

		// return false if supervisor and supervisee are the same person!
		if (supervisor.equals(supervisee)) {
			return false;
		}

		// get all the provider roles the supervisor can supervise
		List<ProviderRole> rolesThatProviderCanSupervisee = getProviderRolesThatProviderCanSupervise(supervisor);

		// get all the roles associated with the supervisee
		List<ProviderRole> superviseeProviderRoles = getProviderRoles(supervisee);

		return ListUtils.intersection(rolesThatProviderCanSupervisee, superviseeProviderRoles).size() > 0 ? true : false;
	}

	/**
     * Gets restricted Provider Roles in the database
     *
     * @param includeRetired whether or not to include retired provider roles
     * @return list of restricted provider roles in the system
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getRestrictedProviderRoles(boolean includeRetired) {

        List<ProviderRole> uiProviderRoles = new ArrayList<ProviderRole>();
        List<ProviderRole> allProviderRoles = getAllProviderRoles(includeRetired);
        if (allProviderRoles != null && allProviderRoles.size() > 0 ) {
            List<String> restrictedRolesGP = ProviderManagementUtils.getRestrictedRolesGP();
            if (restrictedRolesGP != null && restrictedRolesGP.size() > 0) {
                for (ProviderRole role : allProviderRoles) {
                    for (String gp : restrictedRolesGP) {
                        if (StringUtils.equals(role.getUuid(), gp)) {
                            uiProviderRoles.add(role);
                            break;
                        }
                    }
                }
                return uiProviderRoles;
            }
        }
        return allProviderRoles;
    }


    @Override
    @Transactional(readOnly = true)
    public ProviderRole getProviderRole(Integer id) {
        return providerRoleDAO.getProviderRole(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderRole getProviderRoleByUuid(String uuid) {
        return providerRoleDAO.getProviderRoleByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getProviderRolesByRelationshipType(RelationshipType relationshipType) {
        if (relationshipType == null) {
            throw new APIException("relationshipType cannot be null");
        }
        else {
            return providerRoleDAO.getProviderRolesByRelationshipType(relationshipType);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getProviderRolesBySuperviseeProviderRole(ProviderRole providerRole) {
        if (providerRole == null) {
            throw new APIException("providerRole cannot be null");
        }
        else {
            return providerRoleDAO.getProviderRolesBySuperviseeProviderRole(providerRole);
        }
    }

    @Override
    @Transactional
    public ProviderRole saveProviderRole(ProviderRole role) {
        return providerRoleDAO.saveProviderRole(role);
    }

    @Override
    @Transactional
    public void retireProviderRole(ProviderRole role, String reason) {
        // BaseRetireHandler handles retiring the object
        providerRoleDAO.saveProviderRole(role);
    }

    @Override
    @Transactional
    public void unretireProviderRole(ProviderRole role) {
        // BaseUnretireHandler handles unretiring the object
        providerRoleDAO.saveProviderRole(role);
    }

    @Override
    @Transactional
    public void purgeProviderRole(ProviderRole role) throws ProviderRoleInUseException {

        // first, remove this role as supervisee from any roles that can supervise it
        for (ProviderRole r : getProviderRolesBySuperviseeProviderRole(role)) {
            r.getSuperviseeProviderRoles().remove(role);
            Context.getService(ProviderRoleService.class).saveProviderRole(r);   // call through service so AOP save handler picks this up
        }

        try {
            providerRoleDAO.deleteProviderRole(role);
            Context.flushSession();  // shouldn't really have to do this, but we do to force a commit so that the exception will be thrown if necessary
        }
        catch (PersistenceException e) {
			throw new ProviderRoleInUseException("Cannot purge provider role. Most likely it is currently linked to an existing provider ", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationshipType> getAllProviderRoleRelationshipTypes(boolean includeRetired) {

        Set<RelationshipType> relationshipTypes = new HashSet<RelationshipType>();

        for (ProviderRole providerRole : getAllProviderRoles(includeRetired)) {

            if (includeRetired) {
                relationshipTypes.addAll(providerRole.getRelationshipTypes());
            }
            // filter out any retired relationships
            else {
                relationshipTypes.addAll(CollectionUtils.select(providerRole.getRelationshipTypes(), new Predicate() {
                    @Override
                    public boolean evaluate(Object relationshipType) {
                        return !((RelationshipType) relationshipType).getRetired();
                    }
                }));
            }
        }

        return new ArrayList<RelationshipType>(relationshipTypes);
    }

	@Transactional(readOnly = true)
	@Override
	public List<Provider> getProvidersByPerson(Person person, boolean includeRetired) {
		return providerRoleDAO.getProvidersByPerson(person, includeRetired);
	}

	/**
	 * Utility methods
	 */
	private List<Person> providersToPersons(List<Provider> providers) {

		if (providers == null) {
			return null;
		}

		Set<Person> persons = new HashSet<Person>();

		// note that simply ignores providers that are not person, as the module cannot handle them (and I believe that it has been determined that OpemMRS won't support them)
		for (Provider provider : providers) {
			if (provider.getPerson() != null) {
				persons.add(provider.getPerson());
			}
			else {
				log.warn("Ignoring provider " + provider.getId() + " because they are not a person");
			}
		}

		return new ArrayList<Person>(persons);
	}

	private boolean supportsRelationshipType(Provider provider, RelationshipType relationshipType) {

		if (provider == null) {
			throw new APIException("Provider should not be null");
		}

		if (relationshipType == null) {
			throw new APIException("Relationship type should not be null");
		}

		// if this provider has no role, return false
		if (provider.getProviderRole() == null) {
			return false;
		}
		// otherwise, test if the provider's role supports the specified relationship type
		else {
			return provider.getProviderRole().supportsRelationshipType(relationshipType);
		}
	}
}
