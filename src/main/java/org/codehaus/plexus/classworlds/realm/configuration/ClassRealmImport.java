package org.codehaus.plexus.classworlds.realm.configuration;

public class ClassRealmImport
{
    String importRealm;
    String importSpecification;

    public ClassRealmImport( String importRealm, String importSpecification )
    {
        this.importRealm = importRealm;
        this.importSpecification = importSpecification;
    }

    public String getImportRealm()
    {
        return importRealm;
    }

    public String getImportSpecification()
    {
        return importSpecification;
    }    
}