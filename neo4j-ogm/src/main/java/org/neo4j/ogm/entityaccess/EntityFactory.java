/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.model.NodeModel;
import org.neo4j.ogm.model.RelationshipModel;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;

import java.util.*;

public class EntityFactory {

    private final Map<String, String> taxaLeafClass = new HashMap<>();

    private final MetaData metadata;

    public EntityFactory(MetaData metadata) {
        this.metadata = metadata;
    }

    /**
     * Constructs a new object based on the class mapped to the labels on the given {@link NodeModel}.  In the
     * case of multiple labels, only the one that identifies a class in the domain will be used, and if there
     * are any ambiguities in which label to use then an exception will be thrown.
     *
     * @param nodeModel The {@link NodeModel} from which to determine the type
     * @return A new instance of the class that corresponds to the node label, never <code>null</code>
     * @throws MappingException if it's not possible to resolve or instantiate a class from the given argument
     */
    public <T> T newObject(NodeModel nodeModel) {
        return instantiateObjectFromTaxa(nodeModel.getLabels());
    }

    /**
     * Constructs a new object based on the class mapped to the type in the given {@link RelationshipModel}.
     *
     * @param edgeModel The {@link RelationshipModel} from which to determine the type
     * @return A new instance of the class that corresponds to the relationship type, never <code>null</code>
     * @throws MappingException if it's not possible to resolve or instantiate a class from the given argument
     */
    public <T> T newObject(RelationshipModel edgeModel) {
        return instantiateObjectFromTaxa(edgeModel.getType());
    }

    private <T> T instantiateObjectFromTaxa(String... taxa) {

        if (taxa.length == 0) {
            throw new MappingException("Cannot map to a class with no taxa by which to determine the class name.");
        }

        String fqn = resolve(taxa);

        try {
            @SuppressWarnings("unchecked")
            Class<T> className = (Class<T>) Class.forName(fqn);
            return className.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MappingException("Unable to instantiate class: " + fqn, e);
        }
    }

    private String resolve(String... taxa) {

        String fqn = taxaLeafClass.get(Arrays.toString(taxa));

        if (fqn == null) {
            ClassInfo classInfo = metadata.resolve(taxa);
            if (classInfo != null) {
                taxaLeafClass.put(Arrays.toString(taxa), fqn=classInfo.name());
            } else {
                throw new MappingException("Could not resolve a single base class from " + taxa);
            }
        }
        return fqn;
    }

}