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

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Command-line invokable application launcher.
 * <p/>
 * <p/>
 * This launcher class assists in the creation of classloaders and <code>ClassRealm</code>s
 * from a configuration file and the launching of the application's <code>main</code>
 * method from the correct class loaded through the correct classloader.
 * </p>
 * <p/>
 * <p/>
 * The path to the configuration file is specified using the <code>classworlds.conf</code>
 * system property, typically specified using the <code>-D</code> switch to
 * <code>java</code>.
 * </p>
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 * @version $Id$
 */
public class EmbeddedLauncher
    extends Launcher
{
    private static String LAUNCH_METHOD = "execute";

    public EmbeddedLauncher()
    {
        super();
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /**
     * Set the application's main entrypoint.
     *
     * @param mainClassName The main class name.
     * @param mainRealmName The realm to load the class from.
     */
    public void setAppMain( String mainClassName,
                            String mainRealmName )
    {
        this.mainClassName = mainClassName;
        this.mainRealmName = mainRealmName;
    }

    /**
     * Retrieve the main entry realm name.
     *
     * @return The main entry realm name.
     */
    public String getMainRealmName()
    {
        return this.mainRealmName;
    }

    /**
     * Retrieve the main entry class name.
     *
     * @return The main entry class name.
     */
    public String getMainClassName()
    {
        return this.mainClassName;
    }

    /**
     * Retrieve the main entry class.
     *
     * @return The main entry class.
     * @throws ClassNotFoundException If the class cannot be found.
     * @throws NoSuchRealmException   If the specified main entry realm does not exist.
     */
    public Class getMainClass()
        throws ClassNotFoundException, NoSuchRealmException
    {
        return getMainRealm().loadClass( getMainClassName() );
    }

    /**
     * Retrieve the main entry realm.
     *
     * @return The main entry realm.
     * @throws NoSuchRealmException If the specified main entry realm does not exist.
     */
    public ClassRealm getMainRealm()
        throws NoSuchRealmException
    {
        return getWorld().getRealm( getMainRealmName() );
    }

    /**
     * Retrieve the enhanced main entry method.
     *
     * @return The enhanced main entry method.
     * @throws ClassNotFoundException If the main entry class cannot be found.
     * @throws NoSuchMethodException  If the main entry method cannot be found.
     * @throws NoSuchRealmException   If the main entry realm cannot be found.
     */
    protected Method getEnhancedMainMethod()
        throws ClassNotFoundException, NoSuchMethodException, NoSuchRealmException
    {
        Method[] methods = getMainClass().getMethods();

        for ( int i = 0; i < methods.length; ++i )
        {
            final Method method = methods[i];

            if ( !LAUNCH_METHOD.equals( method.getName() ) )
            {
                continue;
            }

            int modifiers = method.getModifiers();

            if ( !( Modifier.isPublic( modifiers ) ) )
            {
                continue;
            }

            if ( method.getReturnType() != Void.TYPE )
            {
                continue;
            }

            Class[] paramTypes = method.getParameterTypes();

            if ( paramTypes.length != 2 )
            {
                continue;
            }

            if ( paramTypes[0] != String[].class )
            {
                continue;
            }

            if ( paramTypes[1] != ClassWorld.class )
            {
                continue;
            }

            return method;
        }

        throw new NoSuchMethodException( "public void execute(ClassWorld world)" );
    }

    /**
     * Attempt to launch the application through the enhanced main method.
     * <p/>
     * <p/>
     * This will seek a method with the exact signature of:
     * </p>
     * <p/>
     * <pre>
     *  public static void main(String[] args, ClassWorld world)
     *  </pre>
     *
     * @throws ClassNotFoundException If the main entry class cannot be found.
     * @throws IllegalAccessException If the method cannot be accessed.
     * @throws java.lang.reflect.InvocationTargetException
     *                                If the target of the invokation is
     *                                invalid.
     * @throws NoSuchMethodException  If the main entry method cannot be found.
     * @throws NoSuchRealmException   If the main entry realm cannot be found.
     */
    protected void launchX()
        throws ClassNotFoundException, IllegalAccessException,
        InvocationTargetException, NoSuchMethodException, NoSuchRealmException
    {
        ClassRealm mainRealm = getMainRealm();
        Class mainClass = getMainClass();
        Method mainMethod = getEnhancedMainMethod();

        Thread.currentThread().setContextClassLoader( mainRealm.getClassLoader() );

        mainMethod.invoke( mainClass, new Object[]{getWorld()} );
    }

    // ------------------------------------------------------------
    //     Class methods
    // ------------------------------------------------------------

    public void launch()
        throws Exception
    {
        String classworldsConf = System.getProperty( CLASSWORLDS_CONF );

        InputStream is = null;

        if ( classworldsConf != null )
        {
            is = new FileInputStream( classworldsConf );
        }
        else
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            if ( "true".equals( System.getProperty( "classworlds.bootstrapped" ) ) )
            {
                is = cl.getResourceAsStream( UBERJAR_CONF_DIR + CLASSWORLDS_CONF );
            }
            else
            {
                is = cl.getResourceAsStream( CLASSWORLDS_CONF );
            }
        }

        if ( is == null )
        {
            throw new Exception( "classworlds configuration not specified nor found "
                                 + "in the classpath" );
        }

        configure( is );

        try
        {
            launchX();
        }
        catch ( InvocationTargetException e )
        {
            // Decode ITE (if we can)
            Throwable t = e.getTargetException();
            if ( t instanceof Exception )
            {
                throw (Exception) t;
            }
            if ( t instanceof Error )
            {
                throw (Error) t;
            }

            // Else just toss the ITE
            throw e;
        }
    }
}
