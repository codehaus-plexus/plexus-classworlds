package org.codehaus.classworlds;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.realm.ClassRealm
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class DefaultClassRealm
    implements ClassRealm
{
    private ClassRealmAdapter adapter;

    public DefaultClassRealm( ClassWorld world, String id )
    {
        this( world, id, null );
    }

    public DefaultClassRealm( ClassWorld world, String id, ClassLoader foreignClassLoader )
    {
        this.adapter = ClassRealmAdapter.getInstance(
            new org.codehaus.plexus.classworlds.realm.ClassRealm(
                ClassWorldReverseAdapter.getInstance( world ), id, foreignClassLoader ) );
    }

    public URL[] getConstituents()
    {
        return adapter.getConstituents();
    }

    public ClassRealm getParent()
    {
        return adapter.getParentRealm();
    }

    public void setParent( ClassRealm parent )
    {
        adapter.setParent( parent );
    }

    public String getId()
    {
        return adapter.getId();
    }

    public ClassWorld getWorld()
    {
        return adapter.getWorld();
    }

    public void importFrom( String realmId, String packageName )
        throws NoSuchRealmException
    {
        adapter.importFrom( realmId, packageName );
    }

    public void addConstituent( URL constituent )
    {
        adapter.addConstituent( constituent );
    }

    /**
     *  Adds a byte[] class definition as a constituent for locating classes.
     *  Currently uses BytesURLStreamHandler to hold a reference of the byte[] in memory.
     *  This ensures we have a unifed URL resource model for all constituents.
     *  The code to cache to disk is commented out - maybe a property to choose which method?
     *
     *  @param constituent class name
     *  @param b the class definition as a byte[]
     */
    public void addConstituent(String constituent,
                               byte[] b) throws ClassNotFoundException
    {
        try
        {
            File path, file;
            if (constituent.lastIndexOf('.') != -1)
            {
                path = new File("byteclass/" + constituent.substring(0, constituent.lastIndexOf('.') + 1).replace('.', File.separatorChar));

                file = new File(path, constituent.substring(constituent.lastIndexOf('.') + 1) + ".class");
            }
            else
            {
                path = new File("byteclass/");

                file = new File(path, constituent + ".class");
            }

            addConstituent( new URL( null,
                                     file.toURL().toExternalForm(),
                                     new BytesURLStreamHandler(b) ) );
        }
        catch (java.io.IOException e)
        {
            throw new ClassNotFoundException( "Couldn't load byte stream.", e );
        }
    }

    public ClassRealm locateSourceRealm( String classname )
    {
        return adapter.locateSourceRealm( classname );
    }

    public ClassLoader getClassLoader()
    {
        return adapter.getClassLoader();
    }

    public ClassRealm createChildRealm( String id )
        throws DuplicateRealmException
    {
        return adapter.createChildRealm( id );
    }

    // ----------------------------------------------------------------------
    // ClassLoader API
    // ----------------------------------------------------------------------

    public Class loadClass( String name )
        throws ClassNotFoundException
    {
        return adapter.loadClass( name );
    }

    public URL getResource( String name )
    {
        return adapter.getResource( name );
    }

    public InputStream getResourceAsStream( String name )
    {
        return adapter.getResourceAsStream( name );
    }

    public Enumeration findResources(String name)
        throws IOException
	{
		return adapter.findResources( name );
	}

    public void display()
    {
        adapter.display();
    }
}
