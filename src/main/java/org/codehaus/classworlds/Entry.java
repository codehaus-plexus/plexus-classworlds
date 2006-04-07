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

/**
 * Import description entry.
 *
 * @author <a href="mailto:bob@eng.werken.com">bob mcwhirter</a>
 */
public class Entry implements Comparable
{
    private final ClassRealm realm;

    private final String pkgName;

    Entry( ClassRealm realm, String pkgName )
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
