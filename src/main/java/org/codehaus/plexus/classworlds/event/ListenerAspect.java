package org.codehaus.plexus.classworlds.event;

import org.codehaus.plexus.classworlds.strategy.Strategy;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 29, 2006
 * @version: $Id$
 */
aspect ListenerAspect
{
    // TODO: here we want a proper listener registering system, not just a debugger
    private ClassEventDebug debugger = new ClassEventDebug();

    pointcut loadClass( String name, Strategy strategy ):
        args( name ) && target( strategy ) &&
        call( Class Strategy.loadClass( String ) );

    before( String name, Strategy strategy ):
        loadClass( name, strategy )
    {
        debugger.lookup( name, strategy );
    }

    after( String name, Strategy strategy ) returning( Class result ):
        loadClass( name, strategy )
    {
        debugger.found( name, strategy, result );
    }

    after( String name, Strategy strategy ) throwing( Exception e ):
        loadClass( name, strategy )
    {
        debugger.failed( name, strategy, e );
    }
}
