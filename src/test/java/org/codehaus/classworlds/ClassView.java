package org.codehaus.classworlds;

public class ClassView
{
    /**
     * * Formats Class information for debug output purposes.
     * *
     * * @param clz  the Class to print information for
     * *
     * * @return a String describing the Class in detail
     */
    public static String toString( Class clz )
    {
        if ( clz.isPrimitive() )
        {
            return clz.toString();
        }
        else if ( clz.isArray() )
        {
            return "Array of " + toString( clz.getComponentType() );
        }
        else if ( clz.isInterface() )
        {
            return toInterfaceString( clz, "" );
        }
        else
        {
            return toClassString( clz, "" );
        }
    }

    /**
     * * Formats Class information for debug output purposes.
     * *
     * * @param clz      the Class to print information for
     * * @param sIndent  the indentation to precede each line of output
     * *
     * * @return a String describing the Class in detail
     */
    private static String toClassString( Class clz, String sIndent )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( sIndent )
            .append( "Class " )
            .append( clz.getName() )
            .append( "  (" )
            .append( toString( clz.getClassLoader() ) )
            .append( ')' );

        sIndent += "  ";

        Class[] aclz = clz.getInterfaces();
        for ( int i = 0, c = aclz.length; i < c; ++i )
        {
            sb.append( '\n' )
                .append( toInterfaceString( aclz[i], sIndent ) );
        }

        clz = clz.getSuperclass();
        if ( clz != null )
        {
            sb.append( '\n' )
                .append( toClassString( clz, sIndent ) );
        }

        return sb.toString();
    }

    /**
     * * Formats interface information for debug output purposes.
     * *
     * * @param clz      the interface Class to print information for
     * * @param sIndent  the indentation to precede each line of output
     * *
     * * @return a String describing the interface Class in detail
     */
    private static String toInterfaceString( Class clz, String sIndent )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( sIndent )
            .append( "Interface " )
            .append( clz.getName() )
            .append( "  (" )
            .append( toString( clz.getClassLoader() ) )
            .append( ')' );

        Class[] aclz = clz.getInterfaces();
        for ( int i = 0, c = aclz.length; i < c; ++i )
        {
            clz = aclz[i];

            sb.append( '\n' )
                .append( toInterfaceString( clz, sIndent + "  " ) );
        }

        return sb.toString();
    }

    /**
     * * Format a description for the specified ClassLoader object.
     * *
     * * @param loader  the ClassLoader instance (or null)
     * *
     * * @return a String description of the ClassLoader
     */
    private static String toString( ClassLoader loader )
    {
        if ( loader == null )
        {
            return "System ClassLoader";
        }

        return "ClassLoader class=" + loader.getClass().getName()
            + ", hashCode=" + loader.hashCode();
    }
}
