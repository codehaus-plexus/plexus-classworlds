
package com.vladium.utils;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

// ----------------------------------------------------------------------------
/**
 * A simple registry API to accompany the java.lang.ClassLoader hack described
 * at the end of the article. Add
 * <PRE><CODE>
 * ClassLoaderRegistry.register (this);
 * </CODE></PRE>
 * to the end of both java.lang.ClassLoader constructors (you have to do this
 * independently for every JDK version, the same hacked version cannot be used
 * across all of them). This class needs to be in the bootstrap classpath to
 * work as intended.
 *  
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov</a>, 2003
 */
public
abstract class ClassLoaderRegistry // this class is not instantiable
{
    // public: ................................................................
    
    /**
     * Adds 'loader' to the weak set maintained by this registry.
     */
    public static void register (final ClassLoader loader)
    {
        if (loader != null)
        {
            synchronized (CLASSLOADER_SET)
            {
                CLASSLOADER_SET.put (loader, null);
            }
        }
    }
    
    /**
     * Returns an enumeration of all class loaders currently registered
     * in the JVM. Note that this may not include all class loaders ever'
     * created because this registry uses weak keys as class loader references.
     * 
     * @param ClassLoader array [never null].
     */
    public static ClassLoader [] getClassLoaders ()
    {
        final List /* ClassLoader */ resultList = new LinkedList ();
        
        synchronized (CLASSLOADER_SET)
        {
            // we are using a weak map: be careful when traversing the key set
            // [it's unsafe to use resultList.addAll(CLASSLOADER_SET.keySet())]
            
            for (Iterator keys = CLASSLOADER_SET.keySet ().iterator (); keys.hasNext (); )
            {
                // note that WeakHashMap guarantees that the weak key will not be
                // cleared between hasNext() and next():
                resultList.add (keys.next ());
            }
        }
        
        final ClassLoader [] result = new ClassLoader [resultList.size ()];
        resultList.toArray (result);
        
        return result; 
    }
    
    // protected: .............................................................

    // package: ...............................................................
    
    // private: ...............................................................
    
    
    private ClassLoaderRegistry () {} // this class is not extendible
    
    // this field is used as a 'weak set' to avoid interfering with class and
    // class loader unloading and garbage collection: 
    private static final WeakHashMap /* ClassLoader->null */ CLASSLOADER_SET =
        new WeakHashMap (); 

} // end of class
// ----------------------------------------------------------------------------