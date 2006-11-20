package org.codehaus.classworlds.strategy;

import org.codehaus.classworlds.ClassRealm;

/**
 * StrategyFactory loads a strategy, either default or from a given hint.
 *
 * @uthor: <a href="mailto:andy@handyande.co.uk">Andrew Williams</a>
 * @since: Nov 19, 2006
 * @version: $Id$
 */
public class StrategyFactory {

    public static Strategy getStrategy( ClassRealm realm )
    {
        return getStrategy( realm, null );
    }

    public static Strategy getStrategy( ClassRealm realm, String hint )
    {
        // Here we shall check hint to load non-default strategies

        Strategy ret = new DefaultStrategy();
        ret.setRealm( realm );

        return ret;
    }

    // TODO might need to add variants that take a ClassLoader as a parameter?
}
