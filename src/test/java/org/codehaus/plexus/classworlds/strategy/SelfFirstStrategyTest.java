package org.codehaus.plexus.classworlds.strategy;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SelfFirstStrategyTest {

    @Test
    void testConstructor() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        SelfFirstStrategy strategy = new SelfFirstStrategy(realm);

        assertNotNull(strategy);
        assertSame(realm, strategy.getRealm());
    }

    @Test
    void testConstructorWithNullRealm() {
        SelfFirstStrategy strategy = new SelfFirstStrategy(null);
        assertNotNull(strategy);
    }

    @Test
    void testStrategyType() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        SelfFirstStrategy strategy = new SelfFirstStrategy(realm);

        assertNotNull(strategy);
    }
}
