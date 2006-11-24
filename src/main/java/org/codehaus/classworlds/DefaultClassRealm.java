package org.codehaus.classworlds;

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.realm.ClassRealm
 * provided for legacy code
 *
 * @author Andrew Williams
 * @version $Id$
 */
public class DefaultClassRealm
    extends ClassRealm
{
    public DefaultClassRealm( ClassWorld world, String id )
    {
        this( world, id, null );
    }

    public DefaultClassRealm( ClassWorld world, String id, ClassLoader foreignClassLoader )
    {
        super( world, id, foreignClassLoader );
    }

// TODO - determine if we need to support this
    /**
     *  Adds a byte[] class definition as a constituent for locating classes.
     *  Currently uses BytesURLStreamHandler to hold a reference of the byte[] in memory.
     *  This ensures we have a unifed URL resource model for all constituents.
     *  The code to cache to disk is commented out - maybe a property to choose which method?
     *
     *  @param constituent class name
     *  @param b the class definition as a byte[]
     */
/*    public void addConstituent(String constituent,
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
    }*/
}
