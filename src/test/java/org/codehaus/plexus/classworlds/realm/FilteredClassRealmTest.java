package org.codehaus.plexus.classworlds.realm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.classworlds.AbstractClassWorldsTestCase;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FilteredClassRealmTest extends AbstractClassWorldsTestCase
{
    private ClassWorld world;

    @Before
    public void setUp()
    {
        this.world = new ClassWorld();
    }

    @Test
    public void testLoadClassAndResourcesFiltered()
        throws Exception
    {
        // only allow loading resources whose names start with "a."
        Set<String> allowedResourcePrefixes = new HashSet<>();
        allowedResourcePrefixes.add( "a." );
        allowedResourcePrefixes.add( "a/Aa" );
        ClassRealm realmA = this.world.newRealm( "realmA",  allowedResourcePrefixes );

        assertThrows( ClassNotFoundException.class, () -> realmA.loadClass( "a.Aa" ) );
        realmA.addURL( getJarUrl( "a.jar" ) );

        assertNotNull( realmA.loadClass( "a.Aa" ) );
        assertThrows( ClassNotFoundException.class, () -> realmA.loadClass( "a.A" ) );

        assertNull( realmA.getResource( "common.properties" ) );
        assertFalse( realmA.getResources( "common.properties" ).hasMoreElements() );
        
        assertNotNull( realmA.getResource( "a.properties" ) );
        assertTrue( realmA.getResources( "a.properties" ).hasMoreElements() );
    }

}
