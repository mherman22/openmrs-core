/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttributeType;
import org.openmrs.RelationshipType;
import org.openmrs.api.context.Context;
import org.openmrs.ProviderRole;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for providerRoleService.
 */
public class  ProviderRoleServiceTest extends BaseContextSensitiveTest {

	// TODO: add some more tests of the retired use cases

	protected static final String XML_DATASET_PATH = "org/openmrs/api/include/";

	protected static final String XML_DATASET = "ProviderRole-dataset.xml";

	private  ProviderRoleService providerRoleService;

	@BeforeEach
	public void init() throws Exception {
		// execute the provider management test dataset
		executeDataSet(XML_DATASET_PATH + XML_DATASET);

		// initialize the service
		providerRoleService = Context.getService(ProviderRoleService.class);
	}

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(ProviderRoleService.class));
	}

	@Test
	public void getAllProviderRoles_shouldGetAllProviderRoles() {
		List<ProviderRole> roles = providerRoleService.getAllProviderRoles(true);
		int roleCount = roles.size();
		assertEquals(12, roleCount);

		roles = providerRoleService.getAllProviderRoles(true);
		roleCount = roles.size();
		assertEquals(12, roleCount);
	}

	@Test
	public void getAllProviderRoles_shouldGetAllProviderRolesExcludingRetired() {
		List<ProviderRole> roles = providerRoleService.getAllProviderRoles(false);
		int roleCount = roles.size();
		assertEquals(11, roleCount);
	}

	@Test
	public void getProviderRole_shouldGetProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);
		assertEquals(Integer.valueOf(1002), role.getId());
		assertEquals("Binome supervisor", role.getName());
	}

	@Test
	public void confirmRelationshipTypesAndSuperviseesProperlyAssociatedWithProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);

		// just check the counts as a sanity check
		assertEquals(2, role.getRelationshipTypes().size());
		assertEquals(1, role.getSuperviseeProviderRoles().size());
	}

	@Test
	public void confirmProviderAttributeTypesProperlyAssociatedWithProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1001);

		// just check the counts as a sanity check
		assertEquals(2, role.getProviderAttributeTypes().size());
	}

	@Test
	public void getProviderRole_shouldReturnNullIfNoProviderForId() {
		assertNull(providerRoleService.getProviderRole(200));
	}

	@Test
	public void getProviderRoleByUuid_shouldGetProviderRoleByUuid() {
		ProviderRole role = providerRoleService.getProviderRoleByUuid("db7f523f-27ce-4bb2-86d6-6d1d05312bd5");
		assertEquals(Integer.valueOf(1003), role.getId());
		assertEquals("Cell supervisor", role.getName());
	}

	@Test
	public void getProviderRoleByUuid_shouldReturnNUllIfNoProviderForUuid() {
		ProviderRole role = providerRoleService.getProviderRoleByUuid("zzz");
	}

	@Test
	public void getProviderRolesByRelationshipType_shouldGetAllProviderRolesThatSupportRelationshipType() {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1002);
		List<ProviderRole> providerRoles = providerRoleService.getProviderRolesByRelationshipType(relationshipType);
		assertEquals(5, providerRoles.size());

		// confirm that the right provider roles have been returned
		Iterator<ProviderRole> i = providerRoles.iterator();

		while (i.hasNext()) {
			ProviderRole providerRole = i.next();
			int id = providerRole.getId();

			if (id == 1001 || id == 1002  || id == 1006 || id == 1009 || id == 1011) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providerRoles.size());
	}

	@Test
	public void getProviderRolesByRelationshipType_shouldReturnEmptyListForRelationshipTypeNotAssociatedWithAnyProviderRoles() {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1);  // a relationship type in the standard test dataset
		List<ProviderRole> providerRoles = providerRoleService.getProviderRolesByRelationshipType(relationshipType);
		assertEquals(0, providerRoles.size());
	}

	@Test
	public void getProviderRolesByRelationshipType_shouldThrowExceptionIfRelationshipTypeNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.getProviderRolesByRelationshipType(null);
		});
	}

	@Test
	public void getProviderRolesBySuperviseeProviderRole_shouldGetAllProviderRolesThatCanSuperviseeProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1001);
		List<ProviderRole> providerRoles = providerRoleService.getProviderRolesBySuperviseeProviderRole(role);
		assertEquals(5, providerRoles.size());

		// confirm that the right provider roles have been returned
		Iterator<ProviderRole> i = providerRoles.iterator();

		while (i.hasNext()) {
			ProviderRole providerRole = i.next();
			int id = providerRole.getId();

			if (id == 1002 || id == 1003  || id == 1004 || id == 1005 || id == 1008) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providerRoles.size());
	}

	@Test
	public void getProviderRolesBySuperviseeProviderRole_shouldReturnEmptyListForProviderRoleThatHasNoSupervisorRoles() {
		ProviderRole role = providerRoleService.getProviderRole(1004);
		List<ProviderRole> providerRoles = providerRoleService.getProviderRolesBySuperviseeProviderRole(role);
		assertEquals(0, providerRoles.size());
	}

	@Test
	public void getProviderRolesBySuperviseeProviderRole_shouldThrowExceptionIfProviderRoleNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.getProviderRolesBySuperviseeProviderRole(null);
		});
	}

	@Test
	public void saveProviderRole_shouldSaveBasicProviderRole() {
		ProviderRole role = new ProviderRole();
		role.setName("Some provider role");
		Context.getService(ProviderRoleService.class).saveProviderRole(role);
		assertEquals(13, providerRoleService.getAllProviderRoles(true).size());
	}

	@Test
	public void saveProviderRole_shouldSaveProviderRoleWithProviderAttributeTypes() {
		ProviderRole role = new ProviderRole();
		role.setName("Some provider role");

		Set<ProviderAttributeType> attributeTypes = new HashSet<ProviderAttributeType>();
		attributeTypes.add(Context.getProviderService().getProviderAttributeType(1001));
		attributeTypes.add(Context.getProviderService().getProviderAttributeType(1002));

		Context.getService(ProviderRoleService.class).saveProviderRole(role);
		assertEquals(13, providerRoleService.getAllProviderRoles(true).size());
	}

	@Test
	public void deleteProviderRole_shouldDeleteProviderRole() throws Exception {
		ProviderRole role = providerRoleService.getProviderRole(1012);
		providerRoleService.purgeProviderRole(role);
		assertEquals(11, providerRoleService.getAllProviderRoles(true).size());
		assertNull(providerRoleService.getProviderRole(1012));
	}

	@Test
	public void deleteProviderRole_shouldFailIfForeignKeyConstraintExists() throws Exception {
		assertThrows(ProviderRoleInUseException.class, () -> {
			ProviderRole role = providerRoleService.getProviderRole(1002);
			providerRoleService.purgeProviderRole(role);
		});
	}

	@Test
	public void retireProviderRole_shouldRetireProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);
		providerRoleService.retireProviderRole(role, "test");
		assertEquals(10, providerRoleService.getAllProviderRoles(false).size());

		role = providerRoleService.getProviderRole(1002);
		assertTrue(role.isRetired());
		assertEquals("test", role.getRetireReason());

	}

	@Test
	public void unretireProviderRole_shouldUnretireProviderRole() {
		ProviderRole role = providerRoleService.getProviderRole(1002);
		providerRoleService.retireProviderRole(role, "test");
		assertEquals(10, providerRoleService.getAllProviderRoles(false).size());

		role = providerRoleService.getProviderRole(1002);
		providerRoleService.unretireProviderRole(role);
		assertFalse(role.isRetired());
	}

	@Test
	public void getAllProviderRoleRelationshipTypes_shouldGetAllProviderRelationshipTypes() {
		List<RelationshipType> relationshipTypes = providerRoleService.getAllProviderRoleRelationshipTypes(true);
		assertEquals(3, relationshipTypes.size());

		// double-check to make sure the are the correct relationships
		// be iterating through and removing the three that SHOULD be there
		Iterator<RelationshipType> i = relationshipTypes.iterator();

		while (i.hasNext()) {
			RelationshipType relationshipType = i.next();
			int id = relationshipType.getId();

			if (id == 1001 || id == 1002  || id == 1003) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, relationshipTypes.size());
	}

	@Test
	public void getAllProviderRoleRelationshipTypes_shouldGetAllNonRetiredProviderRelationshipTypes() {
		// retire one of the relationship types
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1003);
		Context.getPersonService().retireRelationshipType(relationshipType, "test");

		// verify that there are now only 2
		List<RelationshipType> relationshipTypes = providerRoleService.getAllProviderRoleRelationshipTypes(false);
		assertEquals(2, relationshipTypes.size());

		// double-check to make sure the are the correct relationships
		// be iterating through and removing the three that SHOULD be there
		Iterator<RelationshipType> i = relationshipTypes.iterator();

		while (i.hasNext()) {
			relationshipType = i.next();
			int id = relationshipType.getId();

			if (id == 1001 || id == 1002) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, relationshipTypes.size());
	}

	@Test
	public void getProviderRoles_shouldGetProviderRoles() {
		Person provider = Context.getPersonService().getPerson(2);
		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(Integer.valueOf(2), (Integer) roles.size());

		// double-check to make sure the are the correct roles
		// be iterating through and removing the two that SHOULD be there
		Iterator<ProviderRole> i = roles.iterator();

		while (i.hasNext()) {
			ProviderRole role = i.next();
			int id = role.getId();

			if (id == 1001 || id == 1005 ) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, roles.size());
	}

	@Test
	public void getProviderRoles_shouldReturnEmptySetForProviderWithNoRole()  {
		Person provider = Context.getProviderService().getProvider(1002).getPerson();
		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(Integer.valueOf(0), (Integer) roles.size());
	}

	@Test
	public void getProviderRoles_shouldIgnoreRetiredRoles() {
		Person provider = Context.getPersonService().getPerson(2);
		// retire one provider object associated with this person
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");

		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(Integer.valueOf(1), (Integer) roles.size());
		assertEquals(Integer.valueOf(1005), roles.get(0).getId());
	}

	@Test
	public void assignProviderRoleToPerson_shouldAssignProviderRole() {
		// add a new role to the existing provider
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = providerRoleService.getProviderRole(1003);
		providerRoleService.assignProviderRoleToPerson(provider, role, "123");

		// the provider should now have two roles
		List<ProviderRole> providerRoles = providerRoleService.getProviderRoles(provider);
		assertEquals(2, providerRoles.size());

		// double-check to make sure the are the correct roles
		// be iterating through and removing the two that SHOULD be there
		Iterator<ProviderRole> i = providerRoles.iterator();

		while (i.hasNext()) {
			ProviderRole providerRole = i.next();
			int id = providerRole.getId();

			if (id == 1002 || id == 1003) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providerRoles.size());
	}

	@Test
	public void assignProviderRoleToPerson_shouldNotFailIfProviderAlreadyHasRole() {
		// add a role that the provider already has
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = providerRoleService.getProviderRole(1002);
		providerRoleService.assignProviderRoleToPerson(provider, role, "123");

		// the provider should still only have one role
		List<ProviderRole> providerRoles = providerRoleService.getProviderRoles(provider);
		assertEquals(1, providerRoles.size());
		assertEquals(new Integer(1002), providerRoles.get(0).getId());
	}

	@Test
	public void assignProviderRoleToPerson_shouldFailIfUnderlyingPersonVoided() {
		assertThrows(APIException.class, () -> {
			Person provider = Context.getProviderService().getProvider(1006).getPerson();
			ProviderRole role = providerRoleService.getProviderRole(1002);

			// void this person, then attempt to add a role to it
			Context.getPersonService().voidPerson(provider, "test");
			providerRoleService.assignProviderRoleToPerson(provider, role, "123");
		});
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldUnassignRoleFromProvider() {
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = providerRoleService.getProviderRole(1002);
		providerRoleService.unassignProviderRoleFromPerson(provider, role);

		assertEquals(0, providerRoleService.getProviderRoles(provider).size());

		Provider p = Context.getProviderService().getProvider(1006);
		assertTrue(p.isRetired());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldLeaveOtherRoleUntouched() {
		// get the provider with two roles
		Person provider = Context.getPersonService().getPerson(2);

		// unassign one of these roles
		providerRoleService.unassignProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1001));

		// verify that only the other role remains
		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(1, roles.size());
		assertEquals(new Integer(1005), roles.get(0).getId());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldNotFailIfProviderDoesNotHaveRole() {
		// get a binome
		Person provider = Context.getPersonService().getPerson(6);

		// unassign some other role
		providerRoleService.unassignProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1002));

		// verify that the binome role still remains
		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(1,roles.size());
		assertEquals(new Integer(1001), roles.get(0).getId());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldNotFailIfProviderHasNoRoles() {
		// get the provider with no roles
		Person provider = Context.getPersonService().getPerson(1);

		// unassign some role that this person does not have
		providerRoleService.unassignProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1002));

		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(0, roles.size());
	}

	@Test
	public void unassignProviderRoleFromPerson_shouldNotFailIfPersonIsNotProvider() {
		// get the provider with no roles
		Person provider = Context.getPersonService().getPerson(502);

		// unassign some role
		providerRoleService.unassignProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1002));
		assertTrue(!providerRoleService.isProvider(provider));
	}

	@Test
	public void purgeProviderRoleFromPerson_shouldPurgeRoleFromProvider() {
		Person provider = Context.getProviderService().getProvider(1006).getPerson();
		ProviderRole role = providerRoleService.getProviderRole(1002);

		providerRoleService.purgeProviderRoleFromPerson(provider, role);

		assertEquals(0, providerRoleService.getProviderRoles(provider).size());

		assertNull(Context.getProviderService().getProvider(1006));
	}

	@Test
	public void purgeProviderRoleFromPerson_shouldLeaveOtherRoleUntouched() {
		// get the provider with two roles
		Person provider = Context.getPersonService().getPerson(2);

		// purge one of these roles
		providerRoleService.purgeProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1001));

		// verify that only the other role remains
		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(1, roles.size());
		assertEquals(new Integer(1005), roles.get(0).getId());
	}

	@Test
	public void purgeProviderRoleFromPerson_shouldNotFailIfProviderDoesNotHaveRole() {
		// get a binome
		Person provider = Context.getPersonService().getPerson(6);

		// purge some other role
		providerRoleService.purgeProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1002));

		// verify that the binome role still remains
		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(1,roles.size());
		assertEquals(new Integer(1001), roles.get(0).getId());
	}

	@Test
	public void purgeProviderRoleFromPerson_shouldNotFailIfProviderHasNoRoles() {
		// get the provider with no roles
		Person provider = Context.getPersonService().getPerson(1);

		// purge some role that this person does not have
		providerRoleService.purgeProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1002));

		List<ProviderRole> roles = providerRoleService.getProviderRoles(provider);
		assertEquals(0, roles.size());
	}

	@Test
	public void purgeProviderRoleFromPerson_shouldNotFailIfPersonIsNotProvider() {
		// get the provider with no roles
		Person provider = Context.getPersonService().getPerson(502);

		// purge some role
		providerRoleService.purgeProviderRoleFromPerson(provider, providerRoleService.getProviderRole(1002));
		assertFalse(providerRoleService.isProvider(provider));
	}

	@Test
	public void getProvidersByRole_shouldGetProvidersByRole() {
		ProviderRole role = providerRoleService.getProviderRole(1001);
		List<Person> providers = providerRoleService.getProvidersAsPersonsByRole(role);

		// there should be three providers with the binome role
		assertEquals(3, providers.size());

		// double-check to make sure the are the correct providers
		// be iterating through and removing the three that SHOULD be there
		Iterator<Person> i = providers.iterator();

		while (i.hasNext()) {
			Person provider = i.next();
			int id = provider.getId();

			if (id == 2 || id == 6  || id == 7) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providers.size());
	}

	@Test
	public void getProvidersByRole_shouldIgnoreRetiredProviders() {

		// retire a provider
		Provider providerToRetire = Context.getProviderService().getProvider(1003);
		Context.getProviderService().retireProvider(providerToRetire, "test");

		ProviderRole role = providerRoleService.getProviderRole(1001);
		List<Person> providers = providerRoleService.getProvidersAsPersonsByRole(role);

		// there should now only be three providers with the binome role
		assertEquals(2, providers.size());

		// double-check to make sure the are the correct providers
		// be iterating through and removing the three that SHOULD be there
		Iterator<Person> i = providers.iterator();

		while (i.hasNext()) {
			Person provider = i.next();
			int id = provider.getId();

			if (id == 6  || id == 7) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providers.size());
	}

	@Test
	public void getProvidersByRole_shouldFailIfCalledWithNull() {
		assertThrows(APIException.class, () -> {
			List<Person> providers = providerRoleService.getProvidersAsPersonsByRole(null);
		});
	}

	@Test
	public void getProvidersByRoles_shouldGetProvidersByRole() {

		// retire a provider
		Provider providerToRetire = Context.getProviderService().getProvider(1003);
		Context.getProviderService().retireProvider(providerToRetire, "test");

		List<ProviderRole> roles = new ArrayList<ProviderRole>();
		roles.add(providerRoleService.getProviderRole(1001));
		roles.add(providerRoleService.getProviderRole(1002));

		List<Person> providers = providerRoleService.getProvidersAsPersonsByRoles(roles);

		// there should be four providers with the binome  or binome supervisor role
		assertEquals(3, providers.size());

		// double-check to make sure the are the correct providers
		// be iterating through and removing the three that SHOULD be there
		Iterator<Person> i = providers.iterator();

		while (i.hasNext()) {
			Person provider = i.next();
			int id = provider.getId();

			if (id == 6  || id == 7 || id == 8) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providers.size());
	}

	@Test
	public void getProvidersByRoles_shouldIgnoreRetiredRoles() {
		List<ProviderRole> roles = new ArrayList<ProviderRole>();
		roles.add(providerRoleService.getProviderRole(1001));
		roles.add(providerRoleService.getProviderRole(1002));

		List<Person> providers = providerRoleService.getProvidersAsPersonsByRoles(roles);

		// there should be four providers with the binome  or binome supervisor role
		assertEquals(4, providers.size());

		// double-check to make sure the are the correct providers
		// be iterating through and removing the three that SHOULD be there
		Iterator<Person> i = providers.iterator();

		while (i.hasNext()) {
			Person provider = i.next();
			int id = provider.getId();

			if (id == 2 || id == 6  || id == 7 || id == 8) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providers.size());
	}

	@Test
	public void getProvidersByRoles_shouldFailIfCalledWithNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.getProvidersAsPersonsByRole(null);
		});
	}
	
	@Test
	public void getProvidersByRelationshipType_shouldReturnProvidersThatSupportRelationshipType() {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1002);
		List<Person> providers = providerRoleService.getProvidersAsPersonsByRelationshipType(relationshipType);

		// there should be four providers (the 3 binomes, the binome supervisor, and the accompagnateur) that support the accompagnateur relationship

		assertEquals(5, providers.size());

		// double-check to make sure the are the correct providers
		// be iterating through and removing the three that SHOULD be there
		Iterator<Person> i = providers.iterator();

		while (i.hasNext()) {
			Person provider = i.next();
			int id = provider.getId();

			if (id == 2 || id == 6  || id == 7 || id == 8 || id == 9) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, providers.size());
	}

	@Test
	public void getProvidersByRelationshipType_shouldReturnEmptyListIfNoMatchingProvidersFound() {
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1);   // a relationship type from the standard test data
		List<Person> providers = providerRoleService.getProvidersAsPersonsByRelationshipType(relationshipType);
		assertEquals(0, providers.size());

		// also try a relationship type that has a matching role, but no providers have that role
		relationshipType = Context.getPersonService().getRelationshipType(1003);
		providers = providerRoleService.getProvidersAsPersonsByRelationshipType(relationshipType);
		assertEquals(0, providers.size());
	}

	@Test
	public void getProvidersByRelationshipType_shouldFailIfCalledWithNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.getProvidersAsPersonsByRelationshipType(null);
		});
	}

	@Test
	public void getProviderRolesThatCanSuperviseThisProvider_shouldReturnProviderRolesThatCanSuperviseProvider() {
		Person provider = Context.getPersonService().getPerson(2);
		List<ProviderRole> roles = providerRoleService.getProviderRolesThatCanSuperviseThisProvider(provider);
		assertEquals(5, roles.size());

		// double-check to make sure the are the correct roles
		// be iterating through and removing the three that SHOULD be there
		Iterator<ProviderRole> i = roles.iterator();

		while (i.hasNext()) {
			ProviderRole role = i.next();

			int id = role.getId();

			if (id == 1002 || id == 1003 || id == 1004 || id == 1005 || id == 1008) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, roles.size());
	}

	@Test
	public void getProviderRolesThatCanSuperviseThisProvider_shouldReturnEmptyListIfNoMatchingProvidersFound() {
		Person provider = Context.getPersonService().getPerson(501);
		List<ProviderRole> roles = providerRoleService.getProviderRolesThatCanSuperviseThisProvider(provider);
		assertEquals(0, roles.size());
	}

	@Test
	public void getProviderRolesThatCanSuperviseThisProvider_shouldFailIfCalledWithNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.getProviderRolesThatCanSuperviseThisProvider(null);
		});
	}

	@Test
	public void isProvider_shouldReturnTrue() {
		assertTrue(providerRoleService.isProvider(Context.getPersonService().getPerson(2)));
	}

	@Test
	public void isProvider_shouldReturnFalse() {
		assertFalse(providerRoleService.isProvider(Context.getPersonService().getPerson(502)));
	}

	@Test
	public void isProvider_shouldReturnTrueEvenIfAllAssociatedProvidersRetired() {
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1009), "test");

		assertTrue(providerRoleService.isProvider(Context.getPersonService().getPerson(2)));
	}

	@Test
	public void isProvider_shouldFailIfPersonNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.isProvider(null);
		});
	}

	@Test
	public void hasRole_shouldReturnTrue() {
		ProviderRole role1 = Context.getService(ProviderRoleService.class).getProviderRole(1001);
		ProviderRole role2 = Context.getService(ProviderRoleService.class).getProviderRole(1005);
		Person provider = Context.getPersonService().getPerson(2);

		assertTrue(providerRoleService.hasRole(provider, role1));
		assertTrue(providerRoleService.hasRole(provider, role2));
	}

	@Test
	public void hasRole_shouldReturnFalse() {
		ProviderRole role = Context.getService(ProviderRoleService.class).getProviderRole(1002);
		Person provider = Context.getPersonService().getPerson(2);
		assertFalse(providerRoleService.hasRole(provider, role));
	}

	@Test
	public void hasRole_shouldReturnFalseIfRoleRetired() {
		ProviderRole role = Context.getService(ProviderRoleService.class).getProviderRole(1001);
		Person provider = Context.getPersonService().getPerson(2);

		// retire the provider object associated with this role
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");

		assertFalse(providerRoleService.hasRole(provider, role));
	}

	@Test
	public void hasRole_shouldReturnFalseIfProviderHasNoRoles() {
		ProviderRole role = Context.getService(ProviderRoleService.class).getProviderRole(1002);
		Person provider = Context.getPersonService().getPerson(1);
		assertFalse(providerRoleService.hasRole(provider, role));
	}

	@Test
	public void hasRole_shouldReturnFalseIfPersonIsNotProvider() {
		ProviderRole role = Context.getService(ProviderRoleService.class).getProviderRole(1002);
		Person provider = Context.getPersonService().getPerson(502);
		assertFalse(providerRoleService.hasRole(provider, role));
	}

	@Test
	public void supportsRelationshipType_shouldFailIfRelationshipTypeIsNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.supportsRelationshipType(Context.getProviderService().getProvider(1).getPerson(), null);
		});
	}

	@Test
	public void supportsRelationshipType_shouldReturnFalseIfProviderHasNoRole() {
		Person provider = Context.getProviderService().getProvider(1002).getPerson();
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001);
		assertFalse(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void supportsRelationshipType_shouldReturnTrueIfProviderSupportsRelationshipType() {
		Person provider = Context.getProviderService().getProvider(1003).getPerson();  // binome
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001);  // binome relationship
		assertTrue(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void supportsRelationshipType_shouldReturnTrueIfProviderAssociatedWithRoleRetired() {
		Person provider = Context.getProviderService().getProvider(1003).getPerson();  // binome
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001);  // binome relationship

		// retire the provider object associated with this role
		Context.getProviderService().retireProvider(Context.getProviderService().getProvider(1003), "test");

		assertFalse(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void supportsRelationshipType_shouldReturnFalseIfProviderDoesNotSupportsRelationshipType() {
		Person provider = Context.getProviderService().getProvider(1007).getPerson(); // accompagneteur
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001); // binome relationship
		assertFalse(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void supportsRelationshipType_shouldReturnFalseIfProviderPersonHasNoRole() {
		Person provider = Context.getProviderService().getProvider(1002).getPerson();
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001);
		assertFalse(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void supportsRelationshipType_shouldReturnTrueIfProviderPersonSupportsRelationshipType() {
		Person provider = Context.getProviderService().getProvider(1003).getPerson();  // binome
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001);  // binome relationship
		assertTrue(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void supportsRelationshipType_shouldReturnFalseIfProviderPersonDoesNotSupportsRelationshipType() {
		Person provider = Context.getProviderService().getProvider(1007).getPerson(); // accompagneteur
		RelationshipType relationshipType = Context.getPersonService().getRelationshipType(1001); // binome relationship
		assertFalse(providerRoleService.supportsRelationshipType(provider, relationshipType));
	}

	@Test
	public void getProviderRolesThatProviderCanSupervise_shouldReturnRolesThatProviderCanSupervise() {
		Person provider = Context.getPersonService().getPerson(2); // person who is both a binome supervisor and a community health nurse
		List<ProviderRole> roles = providerRoleService.getProviderRolesThatProviderCanSupervise(provider);
		assertEquals(new Integer (3), (Integer) roles.size());

		// double-check to make sure the are the correct roles
		// by iterating through and removing the two that SHOULD be there
		Iterator<ProviderRole> i = roles.iterator();

		while (i.hasNext()) {
			ProviderRole role = i.next();
			int id = role.getId();

			if (id == 1001 || id == 1002 || id == 1011) {
				i.remove();
			}
		}

		// list should now be empty
		assertEquals(0, roles.size());

	}

	@Test
	public void getProviderRolesThatProviderCanSupervise_shouldReturnEmptyListIfProviderRoleDoesNotSupportSupervision() {
		Person provider = Context.getPersonService().getPerson(6); // person who just a binome
		List<ProviderRole> roles = providerRoleService.getProviderRolesThatProviderCanSupervise(provider);
		assertEquals(new Integer (0), (Integer) roles.size());
	}

	@Test
	public void getProviderRolesThatProviderCanSupervise_shouldReturnEmptyListIfPersonIsNotProvider() {
		Person provider = Context.getPersonService().getPerson(502); // person who is not a provider
		List<ProviderRole> roles = providerRoleService.getProviderRolesThatProviderCanSupervise(provider);
		assertEquals(new Integer (0), (Integer) roles.size());
	}

	@Test
	public void getProviderRolesThatProviderCanSupervise_shouldThrowExceptionIfProviderNull() {
		assertThrows(APIException.class, () -> {
			providerRoleService.getProviderRolesThatProviderCanSupervise(null);
		});
	}

	@Test
	public void canSupervise_shouldReturnTrue() {
		Person supervisor = Context.getPersonService().getPerson(8);  // binome supervisor
		Person supervisee = Context.getPersonService().getPerson(6);    // binome
		assertTrue(providerRoleService.canSupervise(supervisor, supervisee));
	}
}
