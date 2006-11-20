package org.codehaus.classworlds;

/*
 $Id$

 Copyright 2002 (C) The Werken Company. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "classworlds" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Werken Company.  For written permission,
    please contact bob@werken.com.

 4. Products derived from this Software may not be called "classworlds"
    nor may "classworlds" appear in their names without prior written
    permission of The Werken Company. "classworlds" is a registered
    trademark of The Werken Company.

 5. Due credit should be given to The Werken Company.
    (http://classworlds.werken.com/).

 THIS SOFTWARE IS PROVIDED BY THE WERKEN COMPANY AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE WERKEN COMPANY OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * <code>Launcher</code> configurator.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @author <a href="mailto:jason@zenplex.com">Jason van Zyl</a>
 * @version $Id$
 */
public class Configurator
{
    public static final String MAIN_PREFIX = "main is";

    public static final String SET_PREFIX = "set";

    public static final String IMPORT_PREFIX = "import";

    public static final String LOAD_PREFIX = "load";

    /**
     * Optionally spec prefix.
     */
    public static final String OPTIONALLY_PREFIX = "optionally";

    /**
     * The launcher to configure.
     */
    private Launcher launcher;

    private ClassWorld world;

    /**
     * Processed Realms.
     */
    private Map configuredRealms;

    /**
     * Construct.
     *
     * @param launcher The launcher to configure.
     */
    public Configurator( Launcher launcher )
    {
        this.launcher = launcher;

        configuredRealms = new HashMap();
    }

    /**
     * Construct.
     *
     * @param world The classWorld to configure.
     */
    public Configurator( ClassWorld world )
    {
        setClassWorld( world );
    }

    /**
     * set world.
     * this setter is provided so you can use the same configurator to configure several "worlds"
     *
     * @param world The classWorld to configure.
     */
    public void setClassWorld( ClassWorld world )
    {
        this.world = world;

        configuredRealms = new HashMap();
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
        BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );

        if ( world == null )
        {
            world = new ClassWorld();
        }

        ClassLoader foreignClassLoader = null;

        if ( this.launcher != null )
        {
            foreignClassLoader = this.launcher.getSystemClassLoader();
        }

        ClassRealm curRealm = null;

        String line = null;

        int lineNo = 0;

        boolean mainSet = false;

        while ( true )
        {
            line = reader.readLine();

            if ( line == null )
            {
                break;
            }

            ++lineNo;
            line = line.trim();

            if ( canIgnore( line ) )
            {
                continue;
            }

            if ( line.startsWith( MAIN_PREFIX ) )
            {
                if ( mainSet )
                {
                    throw new ConfigurationException( "Duplicate main configuration", lineNo, line );
                }

                String conf = line.substring( MAIN_PREFIX.length() ).trim();

                int fromLoc = conf.indexOf( "from" );

                if ( fromLoc < 0 )
                {
                    throw new ConfigurationException( "Missing from clause", lineNo, line );
                }

                String mainClassName = conf.substring( 0, fromLoc ).trim();

                String mainRealmName = conf.substring( fromLoc + 4 ).trim();

                if ( this.launcher != null )
                {
                    this.launcher.setAppMain( mainClassName, mainRealmName );
                }

                mainSet = true;
            }
            else if ( line.startsWith( SET_PREFIX ) )
            {
                String conf = line.substring( SET_PREFIX.length() ).trim();

                int usingLoc = conf.indexOf( " using" ) + 1;

                String property = null;
                String propertiesFileName = null;
                if ( usingLoc > 0 )
                {
                    property = conf.substring( 0, usingLoc ).trim();

                    propertiesFileName = filter( conf.substring( usingLoc + 5 ).trim() );

                    conf = propertiesFileName;
                }

                String defaultValue = null;

                int defaultLoc = conf.indexOf( " default" ) + 1;

                if ( defaultLoc > 0 )
                {
                    defaultValue = conf.substring( defaultLoc + 7 ).trim();

                    if ( property == null )
                    {
                        property = conf.substring( 0, defaultLoc ).trim();
                    }
                    else
                    {
                        propertiesFileName = conf.substring( 0, defaultLoc ).trim();
                    }
                }

                String value = System.getProperty( property );

                if ( value != null )
                {
                    continue;
                }

                if ( propertiesFileName != null )
                {
                    File propertiesFile = new File( propertiesFileName );

                    if ( propertiesFile.exists() )
                    {
                        Properties properties = new Properties();

                        try
                        {
                            properties.load( new FileInputStream( propertiesFileName ) );

                            value = properties.getProperty( property );
                        }
                        catch ( Exception e )
                        {
                            // do nothing
                        }
                    }
                }

                if ( value == null && defaultValue != null )
                {
                    value = defaultValue;
                }

                if ( value != null )
                {
                    value = filter( value );
                    System.setProperty( property, value );
                }
            }
            else if ( line.startsWith( "[" ) )
            {
                int rbrack = line.indexOf( "]" );

                if ( rbrack < 0 )
                {
                    throw new ConfigurationException( "Invalid realm specifier", lineNo, line );
                }

                String realmName = line.substring( 1, rbrack );

                curRealm = world.newRealm( realmName, foreignClassLoader );

                // Stash the configured realm for subsequent association processing.
                configuredRealms.put( realmName, curRealm );
            }
            else if ( line.startsWith( IMPORT_PREFIX ) )
            {
                if ( curRealm == null )
                {
                    throw new ConfigurationException( "Unhandled import", lineNo, line );
                }

                String conf = line.substring( IMPORT_PREFIX.length() ).trim();

                int fromLoc = conf.indexOf( "from" );

                if ( fromLoc < 0 )
                {
                    throw new ConfigurationException( "Missing from clause", lineNo, line );
                }

                String importSpec = conf.substring( 0, fromLoc ).trim();

                String relamName = conf.substring( fromLoc + 4 ).trim();

                curRealm.importFrom( relamName, importSpec );

            }
            else if ( line.startsWith( LOAD_PREFIX ) )
            {
                String constituent = line.substring( LOAD_PREFIX.length() ).trim();

                constituent = filter( constituent );

                if ( constituent.indexOf( "*" ) >= 0 )
                {
                    loadGlob( constituent, curRealm );
                }
                else
                {
                    File file = new File( constituent );

                    if ( file.exists() )
                    {
                        curRealm.addURL( file.toURL() );
                    }
                    else
                    {
                        try
                        {
                            curRealm.addURL( new URL( constituent ) );
                        }
                        catch ( MalformedURLException e )
                        {
                            throw new FileNotFoundException( constituent );
                        }
                    }
                }
            }
            else if ( line.startsWith( OPTIONALLY_PREFIX ) )
            {
                String constituent = line.substring( OPTIONALLY_PREFIX.length() ).trim();

                constituent = filter( constituent );

                if ( constituent.indexOf( "*" ) >= 0 )
                {
                    loadGlob( constituent, curRealm, true );
                }
                else
                {
                    File file = new File( constituent );

                    if ( file.exists() )
                    {
                        curRealm.addURL( file.toURL() );
                    }
                    else
                    {
                        try
                        {
                            curRealm.addURL( new URL( constituent ) );
                        }
                        catch ( MalformedURLException e )
                        {
                            // swallow
                        }
                    }
                }
            }
            else
            {
                throw new ConfigurationException( "Unhandled configuration", lineNo, line );
            }
        }

        // Associate child realms to their parents.
        associateRealms();

        if ( this.launcher != null )
        {
            this.launcher.setWorld( world );
        }

        reader.close();
    }

    /**
     * Associate parent realms with their children.
     */
    protected void associateRealms()
    {
        List sortRealmNames = new ArrayList( configuredRealms.keySet() );

        // sort by name
        Comparator comparator = new Comparator()
        {
            public int compare( Object o1,
                                Object o2 )
            {
                String g1 = (String) o1;
                String g2 = (String) o2;
                return g1.compareTo( g2 );
            }
        };

        Collections.sort( sortRealmNames, comparator );

        // So now we have something like the following for defined
        // realms:
        //
        // root
        // root.maven
        // root.maven.plugin
        //
        // Now if the name of a realm is a superset of an existing realm
        // the we want to make child/parent associations.

        for ( Iterator i = sortRealmNames.iterator(); i.hasNext(); )
        {
            String realmName = (String) i.next();

            int j = realmName.lastIndexOf( '.' );

            if ( j > 0 )
            {
                String parentRealmName = realmName.substring( 0, j );

                ClassRealm parentRealm = (ClassRealm) configuredRealms.get( parentRealmName );

                if ( parentRealm != null )
                {
                    ClassRealm realm = (ClassRealm) configuredRealms.get( realmName );

                    realm.setParent( parentRealm );
                }
            }
        }
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
    protected void loadGlob( String line,
                             ClassRealm realm )
        throws MalformedURLException, FileNotFoundException
    {
        loadGlob( line, realm, false );
    }

    /**
     * Load a glob into the specified classloader.
     *
     * @param line       The path configuration line.
     * @param realm      The realm to populate
     * @param optionally Whether the path is optional or required
     * @throws MalformedURLException If the line does not represent
     *                               a valid path element.
     * @throws FileNotFoundException If the line does not represent
     *                               a valid path element in the filesystem.
     */
    protected void loadGlob( String line,
                             ClassRealm realm,
                             boolean optionally )
        throws MalformedURLException, FileNotFoundException
    {
        File globFile = new File( line );

        File dir = globFile.getParentFile();
        if ( !dir.exists() )
        {
            if ( optionally )
            {
                return;
            }
            else
            {
                throw new FileNotFoundException( dir.toString() );
            }
        }

        String localName = globFile.getName();

        int starLoc = localName.indexOf( "*" );

        final String prefix = localName.substring( 0, starLoc );

        final String suffix = localName.substring( starLoc + 1 );

        File[] matches = dir.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir,
                                   String name )
            {
                if ( !name.startsWith( prefix ) )
                {
                    return false;
                }

                if ( !name.endsWith( suffix ) )
                {
                    return false;
                }

                return true;
            }
        } );

        for ( int i = 0; i < matches.length; ++i )
        {
            realm.addURL( matches[i].toURL() );
        }
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
        String result = "";

        int cur = 0;
        int textLen = text.length();

        int propStart = -1;
        int propStop = -1;

        String propName = null;
        String propValue = null;

        while ( cur < textLen )
        {
            propStart = text.indexOf( "${", cur );

            if ( propStart < 0 )
            {
                break;
            }

            result += text.substring( cur, propStart );

            propStop = text.indexOf( "}", propStart );

            if ( propStop < 0 )
            {
                throw new ConfigurationException( "Unterminated property: " + text.substring( propStart ) );
            }

            propName = text.substring( propStart + 2, propStop );

            propValue = System.getProperty( propName );

            if ( propValue == null )
            {
                throw new ConfigurationException( "No such property: " + propName );
            }
            result += propValue;

            cur = propStop + 1;
        }

        result += text.substring( cur );

        return result;
    }

    /**
     * Determine if a line can be ignored because it is
     * a comment or simply blank.
     *
     * @param line The line to test.
     * @return <code>true</code> if the line is ignorable,
     *         otherwise <code>false</code>.
     */
    private boolean canIgnore( String line )
    {
        return ( line.length() == 0 || line.startsWith( "#" ) );
    }
}

