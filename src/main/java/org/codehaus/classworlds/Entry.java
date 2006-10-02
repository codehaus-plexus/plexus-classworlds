package org.codehaus.classworlds;

/*
 * Copyright 2001-2006 The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Import description entry.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Entry
    implements Comparable
{
    private final ClassRealm realm;

    private final String pkgName;

    Entry( ClassRealm realm,
           String pkgName )
    {
        this.realm = realm;

        this.pkgName = pkgName;
    }

    // ------------------------------------------------------------
    //     Instance methods
    // ------------------------------------------------------------

    /**
     * Retrieve the realm.
     *
     * @return The realm.
     */
    ClassRealm getRealm()
    {
        return this.realm;
    }

    /**
     * Retrieve the page name.
     *
     * @return The package name.
     */
    String getPackageName()
    {
        return this.pkgName;
    }

    /**
     * Determine if the classname matches the package
     * described by this entry.
     *
     * @param classname The class name to test.
     * @return <code>true</code> if this entry matches the
     *         classname, otherwise <code>false</code>.
     */
    boolean matches( String classname )
    {
        return classname.startsWith( getPackageName() );
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     java.lang.Comparable
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    /**
     * Compare this entry to another for relative ordering.
     * <p/>
     * <p/>
     * The natural ordering of Entry objects is reverse-alphabetical
     * based upon package name.
     * </p>
     *
     * @param thatObj The object to compare.
     * @return -1 if this object sorts before that object, 0
     *         if they are equal, or 1 if this object sorts
     *         after that object.
     */
    public int compareTo( Object thatObj )
    {
        Entry that = (Entry) thatObj;

        // We are reverse sorting this list, so that
        // we get longer matches first:
        //
        //     com.werken.foo.bar
        //     com.werken.foo
        //     com.werken

        return ( getPackageName().compareTo( that.getPackageName() ) ) * -1;
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     java.lang.Object
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    /**
     * Test this entry for equality to another.
     * <p/>
     * <p/>
     * Consistent with {@link #compareTo}, this method tests
     * for equality purely on the package name.
     * </p>
     *
     * @param thatObj The object to compare
     * @return <code>true</code> if the two objects are
     *         semantically equivalent, otherwise <code>false</code>.
     */
    public boolean equals( Object thatObj )
    {
        Entry that = (Entry) thatObj;

        return getPackageName().equals( that.getPackageName() );
    }

    /**
     * <p/>
     * Consistent with {@link #equals}, this method creates a hashCode
     * based on the packagename.
     * </p>
     */
    public int hashCode()
    {
        return getPackageName().hashCode();
    }
}
