package org.codehaus.plexus.classworlds.realm.configuration;

import java.util.TreeSet;

public class ClassRealmConfiguration
{
    String id;
    TreeSet imports = new TreeSet();
    TreeSet exports = new TreeSet();
    TreeSet loads = new TreeSet();

    public ClassRealmConfiguration( String id )
    {
        this.id = id;
    }

    public void addImport( String importRealm, String importSpecification )
    {
        imports.add( new ClassRealmImport( importRealm, importSpecification ) );
    }

    public void addExport( String exportStatement )
    {
        exports.add( exportStatement );
    }

    public void addLoad( String loadStatement )
    {
        loads.add( loadStatement );
    }

    public String getId()
    {
        return id;
    }

    public TreeSet getImports()
    {
        return imports;
    }

    public TreeSet getExports()
    {
        return exports;
    }

    public TreeSet getLoads()
    {
        return loads;
    }
}