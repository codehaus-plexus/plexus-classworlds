package org.codehaus.plexus.classworlds.event;

import org.codehaus.plexus.classworlds.strategy.Strategy;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 29, 2006
 * @version: $Id$
 */
public class ClassEventDebug
    implements ClassEventListener
{
    private static String getClassName( Class in )
    {
        String name = in.getName();

        int pos = name.lastIndexOf( '.' );
        if ( pos == -1 )
            return name;

        return name.substring( pos + 1 );
    }

    private void log( Strategy strategy, String message )
    {
        System.out.println( "[classworlds " + getClassName( strategy.getClass() ) +
            "] " + message );
    }

    public void lookup( String name,
                        Strategy strategy )
    {
        log( strategy, "Lookup: " + name );
    }

    public void failed( String name,
                        Strategy strategy,
                        Exception reason )
    {
        log( strategy, "Failed: " + name + " (" + reason.getClass().getName() +
            ": " + reason.getMessage() + ")");
    }

    public void found( String name,
                       Strategy strategy,
                       Class found )
    {
        log( strategy, "Found : " + name );
    }
}
