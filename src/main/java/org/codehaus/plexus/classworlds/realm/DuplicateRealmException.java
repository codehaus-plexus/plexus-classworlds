package org.codehaus.plexus.classworlds.realm;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.ClassWorldException;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Indicates an attempt to add a <code>ClassRealm</code> to a
 * <code>ClassWorld</code> with a duplicate id.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class DuplicateRealmException extends ClassWorldException {
    // ------------------------------------------------------------
    //     Instance members
    // ------------------------------------------------------------

    /**
     * The realm id.
     */
    private String id;

    // ------------------------------------------------------------
    //     Constructors
    // ------------------------------------------------------------

    /**
     * Construct.
     *
     * @param world The world.
     * @param id    The realm id.
     */
    public DuplicateRealmException(ClassWorld world, String id) {
        super(world, id);
        this.id = id;
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /**
     * Retrieve the duplicate realm id.
     *
     * @return The id.
     */
    public String getId() {
        return this.id;
    }
}
