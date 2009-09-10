package org.codehaus.classworlds;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.launcher.Launcher
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class Launcher
    extends org.codehaus.plexus.classworlds.launcher.Launcher
{
    public Launcher()
    {
    }


    // ------------------------------------------------------------
    //     Class methods
    // ------------------------------------------------------------

    /**
     * Launch the launcher from the command line.
     * Will exit using System.exit with an exit code of 0 for success, 100 if there was an unknown exception,
     * or some other code for an application error.
     *
     * @param args The application command-line arguments.
     */
    public static void main( String[] args )
    {
        org.codehaus.plexus.classworlds.launcher.Launcher.main( args );
    }

    /**
     * Launch the launcher.
     *
     * @param args The application command-line arguments.
     * @return an integer exit code
     * @throws Exception If an error occurs.
     */
    public static int mainWithExitCode( String[] args )
        throws Exception
    {
        return org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode( args );
    }
}
