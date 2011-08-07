package org.codehaus.plexus.classworlds.event;

import java.net.URL;

import org.codehaus.plexus.classworlds.strategy.Strategy;

/**
 * A simple aspect to hook event code in when compiling with debug enabled
 *
 * @uthor: Andrew Williams
 * @since: 1.2-alpha-15
 * @version: $Id$
 */
aspect ListenerAspect
{
    // TODO: here we want a proper listener registering system, not just a debugger
    private ClassEventDebug classDebugger = new ClassEventDebug();
    private ResourceEventDebug resourceDebugger = new ResourceEventDebug();

    pointcut loadClass( String name, Strategy strategy ):
        args( name ) && target( strategy ) &&
        call( Class Strategy.loadClass( String ) );

    before( String name, Strategy strategy ):
        loadClass( name, strategy )
    {
        classDebugger.lookup( name, strategy );
    }

    after( String name, Strategy strategy ) returning( Class result ):
        loadClass( name, strategy )
    {
        classDebugger.found( name, strategy, result );
    }

    after( String name, Strategy strategy ) throwing( Exception e ):
        loadClass( name, strategy )
    {
        classDebugger.failed( name, strategy, e );
    }

    pointcut getResource( String name, Strategy strategy ):
        args( name ) && target( strategy ) &&
        call( URL Strategy.getResource( String ) );

    before( String name, Strategy strategy ):
        getResource( name, strategy )
    {
        resourceDebugger.lookup( name, strategy );
    }

    after( String name, Strategy strategy ) returning( URL result ):
        getResource( name, strategy )
    {
        if ( result == null )
        {
            resourceDebugger.failed( name, strategy );
        }
        else
        {
            resourceDebugger.found( name, strategy, result );
        }
    }
}
