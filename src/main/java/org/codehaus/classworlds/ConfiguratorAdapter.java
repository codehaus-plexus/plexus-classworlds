package org.codehaus.classworlds;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by IntelliJ IDEA.
 *
 * @uthor: Andrew Williams
 * @since: Nov 25, 2006
 * @version: $Id$
 */
public class ConfiguratorAdapter
    extends Configurator
{

    public static ConfiguratorAdapter getInstance( org.codehaus.plexus.classworlds.launcher.Configurator newConfig,
                                                   Launcher launcher )
    {
        ConfiguratorAdapter adapter = new ConfiguratorAdapter( newConfig, launcher );

        return adapter;
    }

    public static ConfiguratorAdapter getInstance( org.codehaus.plexus.classworlds.launcher.Configurator newConfig,
                                                   ClassWorld world )
    {
        ConfiguratorAdapter adapter = new ConfiguratorAdapter( newConfig, world );

        return adapter;
    }

    private org.codehaus.plexus.classworlds.launcher.Configurator config;

    private ConfiguratorAdapter( org.codehaus.plexus.classworlds.launcher.Configurator config, Launcher launcher )
    {
        super( launcher );
        this.config = config;
    }

    private ConfiguratorAdapter( org.codehaus.plexus.classworlds.launcher.Configurator config, ClassWorld world )
    {
        super( world );
        this.config = config;
    }

    public void associateRealms()
    {
        config.associateRealms();
    }

    public void configureAdapter( InputStream is )
        throws IOException, MalformedURLException, ConfigurationException, DuplicateRealmException, NoSuchRealmException
    {
        try
        {
            config.configure( is );
        }
        catch ( org.codehaus.plexus.classworlds.launcher.ConfigurationException e )
        {
            throw new ConfigurationException( e.getMessage() );
        }
        catch ( org.codehaus.plexus.classworlds.realm.DuplicateRealmException e )
        {
            throw new DuplicateRealmException( ClassWorldAdapter.getInstance( e.getWorld() ), e.getId() );
        }
        catch ( org.codehaus.plexus.classworlds.realm.NoSuchRealmException e )
        {
            throw new NoSuchRealmException( ClassWorldAdapter.getInstance( e.getWorld() ), e.getId() );
        }
    }

}
