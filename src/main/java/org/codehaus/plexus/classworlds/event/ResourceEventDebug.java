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
public class ResourceEventDebug
    extends ClassEventDebug
    implements ResourceEventListener
{

    private void log( Strategy strategy, String message )
    {
        System.out.println( "[classworlds " + getClassName( strategy.getClass() ) +
            "] " + message );
    }

    public void lookup( String name,
                        Strategy strategy )
    {
        log( strategy, "Lookup res: " + name );
    }

    public void failed( String name,
                        Strategy strategy )
    {
        log( strategy, "Failed res: " + name );
    }

    public void found( String name,
                       Strategy strategy,
                       URL found )
    {
        log( strategy, "Found res : " + name + " (" + found + ")" );
    }
}
