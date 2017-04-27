/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.annotation.GenerationType;
import org.neo4j.ogm.domain.annotations.ids.InvalidAnnotations;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;

public class IdAnnotationTest {

	private MetaData metaData;

	@Before
	public void setUp() throws Exception {
		metaData = new MetaData("org.neo4j.ogm.domain.annotations.ids");
	}

	@Test
	@Ignore("to be implemented")
	public void shouldSupportClassWithoutId() throws Exception {

		ValidAnnotations.WithoutId entity = new ValidAnnotations.WithoutId();

		ClassInfo classInfo = metaData.classInfo(entity);
		assertThat(classInfo.primaryIndexField()).isNull();
	}

	@Test
	public void shouldFindBasicId() throws Exception {

		ValidAnnotations.Basic entity = new ValidAnnotations.Basic();

		ClassInfo classInfo = metaData.classInfo(entity);
		assertThat(classInfo.primaryIndexField().getName()).isNotNull().isEqualTo("identifier");
	}

	@Test
	public void shouldFindBasicChild() throws Exception {

		ValidAnnotations.BasicChild entity = new ValidAnnotations.BasicChild();

		ClassInfo classInfo = metaData.classInfo(entity);
		assertThat(classInfo.primaryIndexField().getName()).isNotNull().isEqualTo("identifier");
	}

	@Test
	public void shouldFindIdAndGenerationType() throws Exception {

		ValidAnnotations.IdAndGenerationType entity = new ValidAnnotations.IdAndGenerationType();

		ClassInfo classInfo = metaData.classInfo(entity);
		assertThat(classInfo.primaryIndexField()).isNotNull();
		assertThat(classInfo.idGenerationStrategy()).isNotNull().isEqualTo(GenerationType.UUID);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectTwoIdsOnSameClass() throws Exception {

		metaData.classInfo(new InvalidAnnotations.TwoIdsOnSameClass()).primaryIndexField();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectBothIdAndPrimaryIndexOnDifferentProperty() throws Exception {

		metaData.classInfo(new InvalidAnnotations.BothIdAndPrimaryIndexOnDifferentProperty()).primaryIndexField();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectChildHasPrimaryIndexExtendsAndParentHasId() throws Exception {

		metaData.classInfo(new InvalidAnnotations.ChildHasPrimaryIndexExtendsAndParentHasId()).primaryIndexField();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectUuidGenerationStrategyWithIdTypeNotUuid() throws Exception {

		metaData.classInfo(new InvalidAnnotations.UuidGenerationStrategyWithIdTypeNotUuid()).primaryIndexField();
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldRejectGeneratedValueWithoutID() throws Exception {

		metaData.classInfo(new InvalidAnnotations.GeneratedValueWithoutID()).primaryIndexField();
	}
}