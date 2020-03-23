/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.session.event;

/**
 * Specialized {@link Event} which is fired before an entity is saved. If the application receives an {@link Event} that
 * returns {@link }Type#POST_SAVE} from {@link Event#getLifeCycle()}, the event can safely be down casted.
 *
 * @author Michael J. Simons
 * @since 3.2.11
 */
public final class PostSaveEvent extends PersistenceEvent {

    private final boolean wasNew;

    public PostSaveEvent(Object affectedObject, boolean wasNew) {

        super(affectedObject, TYPE.POST_SAVE);
        this.wasNew = wasNew;
    }

    /**
     * @return True, if the object was new before it was saved.
     */
    public boolean wasNew() {
        return wasNew;
    }
}
