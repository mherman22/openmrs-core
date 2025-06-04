/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.ProviderRoleService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

public class ProviderRoleTest extends BaseContextSensitiveTest {

	protected static final String XML_DATASET_PATH = "org/openmrs/api/include/";

	protected static final String XML_DATASET = "ProviderRole-dataset.xml";

	private ProviderRoleService providerRoleService;

	@BeforeEach
	public void init() throws Exception {
		// execute the provider management test dataset
		executeDataSet(XML_DATASET_PATH + XML_DATASET);

		// initialize the service
		providerRoleService = Context.getService(ProviderRoleService.class);
	}

	@Test
	public void shouldCreateNewProviderRole() {
		new ProviderRole();
	}

	@Test
	public void shouldSpecifyWhetherRoleIsSupervisorRole() {
		ProviderRole role = providerRoleService.getProviderRole(1001);
		assertFalse(role.isSupervisorRole());

		role = providerRoleService.getProviderRole(1002);
		assertTrue(role.isSupervisorRole());

		role = providerRoleService.getProviderRole(1003);
		assertTrue(role.isSupervisorRole());
	}

	@Test
	public void shouldSpecifyWhetherRoleIsDirectCareRole() {
		ProviderRole role = providerRoleService.getProviderRole(1001);
		assertTrue(role.isDirectPatientCareRole());

		role = providerRoleService.getProviderRole(1002);
		assertTrue(role.isDirectPatientCareRole());

		role = providerRoleService.getProviderRole(1003);
		assertFalse(role.isDirectPatientCareRole());
	}

	@Test
	public void shouldTestWhetherRoleSupportsRelationshipType() {
		ProviderRole role =  providerRoleService.getProviderRole(1011);
		RelationshipType binome = Context.getPersonService().getRelationshipType(1001);
		RelationshipType accompagneteur =  Context.getPersonService().getRelationshipType(1002);

		assertFalse(role.supportsRelationshipType(binome));
		assertTrue(role.supportsRelationshipType(accompagneteur));
	}

	@Test
	public void equalsTest() {
		ProviderRole role1 = providerRoleService.getProviderRole(1001);
		ProviderRole role2 = providerRoleService.getProviderRole(1001);
		ProviderRole role3 = providerRoleService.getProviderRole(1002);
		
		assertEquals(role1, role2);
		assertNotEquals(role1, role3);
	}
}
