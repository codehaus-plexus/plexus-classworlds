package org.codehaus.plexus.classworlds.event;

import org.codehaus.plexus.classworlds.strategy.Strategy;

import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 29, 2006
 * @version: $Id$
 */
public interface ResourceEventListener
{
    void lookup( String name, Strategy strategy );

    void failed( String name, Strategy strategy );

    void found( String name, Strategy strategy, URL found );

/* TODO: decide if these would be useful
    void findStarted( String name, Strategy strategy );

    void findReturned( String name, Strategy strategy, Enumeration returned );
*/
}
