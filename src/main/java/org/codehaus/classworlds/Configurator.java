package org.codehaus.classworlds;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.launcher.Configurator
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class Configurator
    extends org.codehaus.plexus.classworlds.launcher.Configurator
{
    /** Construct.
     *
     *  @param launcher The launcher to configure.
     */
    public Configurator( Launcher launcher )
    {
        super( launcher );
    }

    /** Construct.
     *
     *  @param world The classWorld to configure.
     */
    public Configurator( ClassWorld world )
    {
        super( world );
    }
}

