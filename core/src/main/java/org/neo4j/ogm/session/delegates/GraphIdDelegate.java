/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.delegates;

import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.utils.EntityUtils;

/**
 * @author Luanne Misquitta
 */
public class GraphIdDelegate extends SessionDelegate {

    public GraphIdDelegate(Neo4jSession session) {
        super(session);
    }

    public Long resolveGraphIdFor(Object possibleEntity) {
        if (possibleEntity != null) {
            ClassInfo classInfo = session.metaData().classInfo(possibleEntity);
            try {
                if (classInfo != null) {
                    Long id = EntityUtils.identity(possibleEntity, session.metaData());
                    if (id >= 0) {
                        return (long) id;
                    }
                }
            } catch (MetadataException me) {
                //Possibly no identity field on the entity. One example is an Enum- it won't have an identity field.
                return null;
            }
        }
        return null;
    }

    public boolean detachNodeEntity(Long id) {
        return session.context().detachNodeEntity(id);
    }

    public boolean detachRelationshipEntity(Long id) {
        return session.context().detachRelationshipEntity(id);
    }
}
