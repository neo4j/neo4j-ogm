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

package org.neo4j.ogm.session;

import org.neo4j.ogm.session.event.Observer;

/**
 * A {@link Session} serves as the main point of integration for the Neo4j OGM.  All the publicly-available capabilities of the
 * framework are defined by this interface.
 *
 * @see SessionFactory
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public interface Session extends
        Observer,
        Capability.LoadOne,
        Capability.LoadByIds,
        Capability.LoadByInstances,
        Capability.LoadByType,
        Capability.Save,
        Capability.Delete,
        Capability.Transactions,
        Capability.ExecuteQueries,
        Capability.GraphId {

	/**
	 * Retrieves the last bookmark used in this session when used in a Neo4j Causal Cluster.
	 *
	 * This bookmark can be used to ensure the cluster is consistent before performing a read/write.
	 *
	 * @return The last used bookmark String on this session.
	 */
	String getLastBookmark();

	/**
	 * Sets the bookmark to use on this session. Useful when resuming a user session with a causal cluster.
	 *
	 * @param bookmark The last used bookmark String that this session should start from.
	 */
	void lastBookmark(String bookmark);
}
