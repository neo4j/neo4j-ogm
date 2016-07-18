/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.domain.food.entities.notScanned;

import java.math.RoundingMode;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.domain.food.converter.RiskConverter;
import org.neo4j.ogm.domain.food.entities.scanned.Risk;

/**
 * @author Luanne Misquitta
 */
public class Pizza {
	public Long id;

	public Risk strokeRisk;

	@Convert(RiskConverter.class)
	public Risk diabetesRisk;

	@Property(name = "riskCancer")
	public Risk cancerRisk;

	@Convert(RiskConverter.class)
	@Property(name = "riskHT")
	public Risk hypertensionRisk;

	public RoundingMode roundingMode; //a java enum, to test that these are converted too
}
