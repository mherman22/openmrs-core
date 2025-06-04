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
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.openmrs.ProviderRole;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.ProviderManagementUtils;
import org.openmrs.api.ProviderRoleInUseException;
import org.openmrs.api.ProviderRoleService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProviderRoleDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProviderRoleServiceImpl extends BaseOpenmrsService implements ProviderRoleService {

    // TODO: (??? --not sure what this comment means anymore?) add checks to make sure person is not voided automatically when appropriate (in the assignment classes?)

    protected final Log log = LogFactory.getLog(this.getClass());

    private ProviderRoleDAO dao;

    private static RelationshipType supervisorRelationshipType = null;

    /**
     * @param dao the dao to set
     */
    public void setDao(ProviderRoleDAO dao) {
        this.dao = dao;
    }

    /**
     * @return the dao
     */
    public ProviderRoleDAO getDao() {
        return dao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getAllProviderRoles(boolean includeRetired) {
        return dao.getAllProviderRoles(includeRetired);
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
        return dao.getProviderRole(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderRole getProviderRoleByUuid(String uuid) {
        return dao.getProviderRoleByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getProviderRolesByRelationshipType(RelationshipType relationshipType) {
        if (relationshipType == null) {
            throw new APIException("relationshipType cannot be null");
        }
        else {
            return dao.getProviderRolesByRelationshipType(relationshipType);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderRole> getProviderRolesBySuperviseeProviderRole(ProviderRole providerRole) {
        if (providerRole == null) {
            throw new APIException("providerRole cannot be null");
        }
        else {
            return dao.getProviderRolesBySuperviseeProviderRole(providerRole);
        }
    }

    @Override
    @Transactional
    public ProviderRole saveProviderRole(ProviderRole role) {
        return dao.saveProviderRole(role);
    }

    @Override
    @Transactional
    public void retireProviderRole(ProviderRole role, String reason) {
        // BaseRetireHandler handles retiring the object
        dao.saveProviderRole(role);
    }

    @Override
    @Transactional
    public void unretireProviderRole(ProviderRole role) {
        // BaseUnretireHandler handles unretiring the object
        dao.saveProviderRole(role);
    }

    @Override
    @Transactional
    public void purgeProviderRole(ProviderRole role)
            throws ProviderRoleInUseException {

        // first, remove this role as supervisee from any roles that can supervise it
        for (ProviderRole r : getProviderRolesBySuperviseeProviderRole(role)) {
            r.getSuperviseeProviderRoles().remove(role);
            Context.getService(ProviderRoleService.class).saveProviderRole(r);   // call through service so AOP save handler picks this up
        }

        try {
            dao.deleteProviderRole(role);
            Context.flushSession();  // shouldn't really have to do this, but we do to force a commit so that the exception will be thrown if necessary
        }
        catch (ConstraintViolationException e) {
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
}
