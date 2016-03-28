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

package org.neo4j.ogm.response.model;


import org.neo4j.ogm.model.QueryStatistics;

/**
 * Holds read only statistics about query execution.
 * The field names do not follow convention as they are mapped directly from the JSON received from the Neo4j API.
 *
 * @author Luanne Misquitta
 */
public class QueryStatisticsModel implements QueryStatistics {

	private boolean contains_updates;
	private int nodes_created;
	private int nodes_deleted;
	private int properties_set;
	private int relationships_created;
	private int relationships_deleted; // embedded result calls it this
    private int relationship_deleted;  // http response calls it this. go figure
	private int labels_added;
	private int labels_removed;
	private int indexes_added;
	private int indexes_removed;
	private int constraints_added;
	private int constraints_removed;

	public boolean containsUpdates() {
		return contains_updates;
	}

	public int getNodesCreated() {
		return nodes_created;
	}

	public int getNodesDeleted() {
		return nodes_deleted;
	}

	public int getPropertiesSet() {
		return properties_set;
	}

	public int getRelationshipsCreated() {
		return relationships_created;
	}

    // could be set by different drivers, but only one property will be set
	public int getRelationshipsDeleted() {
		return relationships_deleted + relationship_deleted;
	}

	public int getLabelsAdded() {
		return labels_added;
	}

	public int getLabelsRemoved() {
		return labels_removed;
	}

	public int getIndexesAdded() {
		return indexes_added;
	}

	public int getIndexesRemoved() {
		return indexes_removed;
	}

	public int getConstraintsAdded() {
		return constraints_added;
	}

	public int getConstraintsRemoved() {
		return constraints_removed;
	}

	public void setContains_updates(boolean contains_updates) {
		this.contains_updates = contains_updates;
	}

	public void setNodes_created(int nodes_created) {
		this.nodes_created = nodes_created;
	}

	public void setNodes_deleted(int nodes_deleted) {
		this.nodes_deleted = nodes_deleted;
	}

	public void setProperties_set(int properties_set) {
		this.properties_set = properties_set;
	}

	public void setRelationships_created(int relationships_created) {
		this.relationships_created = relationships_created;
	}

    // property name in embedded driver
	public void setRelationships_deleted(int relationships_deleted) {
		this.relationships_deleted = relationships_deleted;
	}

	public void setLabels_added(int labels_added) {
		this.labels_added = labels_added;
	}

	public void setLabels_removed(int labels_removed) {
		this.labels_removed = labels_removed;
	}

	public void setIndexes_added(int indexes_added) {
		this.indexes_added = indexes_added;
	}

	public void setIndexes_removed(int indexes_removed) {
		this.indexes_removed = indexes_removed;
	}

	public void setConstraints_added(int constraints_added) {
		this.constraints_added = constraints_added;
	}

	public void setConstraints_removed(int constraints_removed) {
		this.constraints_removed = constraints_removed;
	}

    public int getRelationship_deleted() {
        return relationship_deleted + relationships_deleted;
    }

    // property name in http response
    public void setRelationship_deleted(int relationship_deleted) {
        this.relationship_deleted = relationship_deleted;
    }
}
