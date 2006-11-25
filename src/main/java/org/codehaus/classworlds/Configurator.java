package org.codehaus.classworlds;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.launcher.Configurator
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class Configurator
{
    private ConfiguratorAdapter config;

    /** Construct.
     *
     *  @param launcher The launcher to configure.
     */
    public Configurator( Launcher launcher )
    {
        config = ConfiguratorAdapter.getInstance(
            new org.codehaus.plexus.classworlds.launcher.Configurator( launcher ), launcher );
    }

    /** Construct.
     *
     *  @param world The classWorld to configure.
     */
    public Configurator( ClassWorld world )
    {
        config = ConfiguratorAdapter.getInstance(
            new org.codehaus.plexus.classworlds.launcher.Configurator(
                ClassWorldReverseAdapter.getInstance( world ) ), world );
    }

    /** set world.
     *  this setter is provided so you can use the same configurator to configure several "worlds"
     *
     *  @param world The classWorld to configure.
     */
    public void setClassWorld( ClassWorld world )
    {
        config.setClassWorld( world );
    }

    /**
     * Configure from a file.
     *
     * @param is The config input stream
     * @throws IOException             If an error occurs reading the config file.
     * @throws MalformedURLException   If the config file contains invalid URLs.
     * @throws ConfigurationException  If the config file is corrupt.
     * @throws DuplicateRealmException If the config file defines two realms with the same id.
     * @throws NoSuchRealmException    If the config file defines a main entry point in
     *                                 a non-existent realm.
     */
    public void configure( InputStream is )
        throws IOException, MalformedURLException, ConfigurationException, DuplicateRealmException, NoSuchRealmException
    {
        config.configureAdapter( is );
    }

    /**
     * Associate parent realms with their children.
     */
    protected void associateRealms()
    {
        config.associateRealms();
    }

    /**
     * Load a glob into the specified classloader.
     *
     * @param line  The path configuration line.
     * @param realm The realm to populate
     * @throws MalformedURLException If the line does not represent
     *                               a valid path element.
     * @throws FileNotFoundException If the line does not represent
     *                               a valid path element in the filesystem.
     */
    protected void loadGlob( String line, ClassRealm realm )
        throws MalformedURLException, FileNotFoundException
    {
        loadGlob( line, realm, false );
    }

    /**
     * Load a glob into the specified classloader.
     *
     * @param line  The path configuration line.
     * @param realm The realm to populate
     * @param optionally Whether the path is optional or required
     * @throws MalformedURLException If the line does not represent
     *                               a valid path element.
     * @throws FileNotFoundException If the line does not represent
     *                               a valid path element in the filesystem.
     */
    protected void loadGlob( String line, ClassRealm realm, boolean optionally )
        throws MalformedURLException, FileNotFoundException
    {
        config.loadGlob( line, realm, optionally );
    }

    /**
     * Filter a string for system properties.
     *
     * @param text The text to filter.
     * @return The filtered text.
     * @throws ConfigurationException If the property does not
     *                                exist or if there is a syntax error.
     */
    protected String filter( String text )
        throws ConfigurationException
    {
        return config.filter( text );
    }

}

