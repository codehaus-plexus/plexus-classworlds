package org.codehaus.plexus.classworlds.launcher;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

/**
 * Event based launcher configuration parser, delegating effective configuration handling to ConfigurationHandler.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @author Jason van Zyl
 * @author Igor Fedorenko
 * @see ConfigurationHandler
 */
public class ConfigurationParser
{
    public static final String MAIN_PREFIX = "main is";

    public static final String SET_PREFIX = "set";

    public static final String IMPORT_PREFIX = "import";

    public static final String LOAD_PREFIX = "load";

    /**
     * Optionally spec prefix.
     */
    public static final String OPTIONALLY_PREFIX = "optionally";

    private ConfigurationHandler handler;

    private Properties systemProperties;

    public ConfigurationParser( ConfigurationHandler handler, Properties systemProperties )
    {
        this.handler = handler;
        this.systemProperties = systemProperties;
    }

    /**
     * Parse launcher configuration file and send events to the handler.
     * 
     * @param is the inputstream
     * @throws IOException when IOException occurs
     * @throws ConfigurationException when ConfigurationException occurs
     * @throws DuplicateRealmException when realm already exists
     * @throws NoSuchRealmException when realm doesn't exist 
     */
    public void parse( InputStream is )
        throws IOException, ConfigurationException, DuplicateRealmException, NoSuchRealmException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( is, StandardCharsets.UTF_8 ) );

        String line;

        int lineNo = 0;

        boolean mainSet = false;
        
        String curRealm = null;

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

                String mainClassName = filter( conf.substring( 0, fromLoc ).trim() );

                String mainRealmName = filter( conf.substring( fromLoc + 4 ).trim() );

                this.handler.setAppMain( mainClassName, mainRealmName );

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
                    defaultValue = filter( conf.substring( defaultLoc + 7 ).trim() );

                    if ( property == null )
                    {
                        property = conf.substring( 0, defaultLoc ).trim();
                    }
                    else
                    {
                        propertiesFileName = conf.substring( 0, defaultLoc ).trim();
                    }
                }

                String value = systemProperties.getProperty( property );

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
                            properties.load( Files.newInputStream( Paths.get( propertiesFileName ) ) );

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
                    systemProperties.setProperty( property, value );
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

                handler.addRealm( realmName );

                curRealm = realmName;
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

                handler.addImportFrom( relamName, importSpec );

            }
            else if ( line.startsWith( LOAD_PREFIX ) )
            {
                String constituent = line.substring( LOAD_PREFIX.length() ).trim();

                constituent = filter( constituent );

                if ( constituent.contains( "*" ) )
                {
                    loadGlob( constituent, false /*not optionally*/ );
                }
                else
                {
                    File file = new File( constituent );

                    if ( file.exists() )
                    {
                        handler.addLoadFile( file );
                    }
                    else
                    {
                        try
                        {
                          handler.addLoadURL( new URL( constituent ) );
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

                if ( constituent.contains( "*" ) )
                {
                    loadGlob( constituent, true /*optionally*/ );
                }
                else
                {
                    File file = new File( constituent );

                    if ( file.exists() )
                    {
                        handler.addLoadFile( file );
                    }
                    else
                    {
                        try
                        {
                            handler.addLoadURL( new URL( constituent ) );
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

        reader.close();
    }

    /**
     * Load a glob into the specified classloader.
     *
     * @param line       The path configuration line.
     * @param optionally Whether the path is optional or required
     * @throws MalformedURLException If the line does not represent
     *                               a valid path element.
     * @throws FileNotFoundException If the line does not represent
     *                               a valid path element in the filesystem.
     * @throws ConfigurationException will never occur (thrown for backwards compatibility)
     */
    protected void loadGlob( String line,
                             boolean optionally )
        throws MalformedURLException, FileNotFoundException, ConfigurationException
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

        File[] matches = dir.listFiles( ( dir1, name ) -> {
            if ( !name.startsWith( prefix ) )
            {
                return false;
            }

            if ( !name.endsWith( suffix ) )
            {
                return false;
            }

            return true;
        } );

        for ( File match : matches )
        {
            handler.addLoadFile( match );
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
        StringBuilder result = new StringBuilder();

        int cur = 0;
        int textLen = text.length();

        int propStart;
        int propStop;

        String propName;
        String propValue;

        while ( cur < textLen )
        {
            propStart = text.indexOf( "${", cur );

            if ( propStart < 0 )
            {
                break;
            }

            result.append( text, cur, propStart );

            propStop = text.indexOf( "}", propStart );

            if ( propStop < 0 )
            {
                throw new ConfigurationException( "Unterminated property: " + text.substring( propStart ) );
            }

            propName = text.substring( propStart + 2, propStop );

            propValue = systemProperties.getProperty( propName );

            /* do our best if we are not running from surefire */
            if ( propName.equals( "basedir" ) && ( propValue == null || propValue.equals( "" ) ) )
            {
                propValue = ( new File( "" ) ).getAbsolutePath();

            }

            if ( propValue == null )
            {
                throw new ConfigurationException( "No such property: " + propName );
            }
            result.append( propValue );

            cur = propStop + 1;
        }

        result.append( text.substring( cur ) );

        return result.toString();
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
