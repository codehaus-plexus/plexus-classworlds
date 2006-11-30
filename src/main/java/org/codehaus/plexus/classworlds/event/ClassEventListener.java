package org.codehaus.plexus.classworlds.event;

import org.codehaus.plexus.classworlds.strategy.Strategy;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 29, 2006
 * @version: $Id$
 */
public interface ClassEventListener
{
    void lookup( String name, Strategy strategy );

    void failed( String name, Strategy strategy, Exception reason );

    void found( String name, Strategy strategy, Class found );
}
