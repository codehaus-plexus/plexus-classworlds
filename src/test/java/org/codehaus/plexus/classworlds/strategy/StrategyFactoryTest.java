package org.codehaus.plexus.classworlds.strategy;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrategyFactoryTest {

    @Test
    void testGetStrategyDefault() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        Strategy strategy = StrategyFactory.getStrategy(realm);

        assertNotNull(strategy);
        assertInstanceOf(SelfFirstStrategy.class, strategy);
    }

    @Test
    void testGetStrategyWithDefaultHint() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        Strategy strategy = StrategyFactory.getStrategy(realm, "default");

        assertNotNull(strategy);
        assertInstanceOf(SelfFirstStrategy.class, strategy);
    }

    @Test
    void testGetStrategyWithCustomHint() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        Strategy strategy = StrategyFactory.getStrategy(realm, "custom");

        assertNotNull(strategy);
        assertSame(realm, strategy.getRealm());
    }

    @Test
    void testGetStrategyWithNullHint() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        Strategy strategy = StrategyFactory.getStrategy(realm, null);

        assertNotNull(strategy);
        assertInstanceOf(SelfFirstStrategy.class, strategy);
    }

    @Test
    void testGetStrategyMultipleCalls() throws Exception {
        ClassWorld world = new ClassWorld();
        ClassRealm realm = world.newRealm("testRealm");

        Strategy strategy1 = StrategyFactory.getStrategy(realm);
        Strategy strategy2 = StrategyFactory.getStrategy(realm, "default");

        assertNotNull(strategy1);
        assertNotNull(strategy2);
        assertInstanceOf(SelfFirstStrategy.class, strategy1);
        assertInstanceOf(SelfFirstStrategy.class, strategy2);
    }
}
