<?xml version="1.0"?>

<document>

  <properties>
    <title>App Launching</title>
    <author email="bob@eng.werken.com">bob mcwhirter</author>
  </properties>

  <body>

  <section name="Launcher Introduction">

    <subsection name="Purpose"> 

      <p>
      In order to reduce the number of classloading projects,
      <b>Plexus Classworlds</b> replaces forehead
      for application launching.  
      </p>

      <p>
      The main problems to solve in application launching include
      locating all of application's JARs, configuring the initial
      classloaders, and invoking the <code>main</code> entry method.
      </p>

      <p>
      The <a href="apidocs/index.html?org/codehaus/plexus/classworlds/launcher/package-summary.html">launcher facilities</a>
      of <b>Classworlds</b> simplify
      the process of locating application jars.  A common idiom is
      to have a script which starts the JVM with only the 
      <code>plexus-classworlds.jar</code> in the classpath and a system
      property to specify the location of a launcher configuration.
      Additionally, typically a property specifying the application installation
      location is passed on the command-line.
      </p>

<source><![CDATA[
$JAVA_HOME/bin/java \
    -classpath $APP_HOME/boot/plexus-classworlds-2.5.2.jar \
    -Dclassworlds.conf=$APP_HOME/etc/classworlds.conf \
    -Dapp.home=$APP_HOME \
    org.codehaus.plexus.classworlds.launcher.Launcher \
    $*
]]></source>

    </subsection>

  </section>

  <section name="Configuration">

    <subsection name="Entry Point Definition">

      <p>
      The entry-point class and realm must be specified
      using the <code>main is</code> directive before
      specifying realm definitions. 
      </p>

<source><![CDATA[
main is com.werken.projectz.Server from app
]]></source>

    </subsection>

    <subsection name="System Properties Definition">

      <p>
      System properties can be set before and after the entry point, but before realms:
      </p>

<source><![CDATA[
set <property> [[using <properties filename>]] [[default <default value>]]
]]></source>

    </subsection>

    <subsection name="Realm Definitions">

      <p>
      At least one <b>Classworlds</b> realm must be defined
      within the configuration file.  The syntax for starting a
      realm definition is <code>[realm.name]</code>.  All lines
      following the realm header are considered directives for 
      that realm.  The realm definition continues either until
      another realm is defined or until the end of the file is
      reached.  
      </p>

<source><![CDATA[
[realm.one]
    ...
    ...
[realm.two]
    ...
    ...
[realm.three]
    ...
    ...
]]></source>

      <p>
      Within a realm definition, three directives are available:
      <code>load</code>, <code>optionally</code> and <code>import</code>.  
      </p>

      <p>
      The <code>load</code> and <code>optionally</code>
      directives specify a class source to be used for loading
      classes in the realm: the only difference is that in case of absent source,
      <code>load</code> fails but <code>optionally</code> does not.
      Any loaded source that contain a star (<code>*</code>) in the file name is
      replaced by the list of files that match the filename prefix and suffix.
      System properties may be referred to using <code>${propname}</code> notation.
      The <code>load</code> and <code>optionally</code> directives are equivalent to the
      <code>addURL(..)</code> method of <code>ClassRealm</code>.
      </p>

<source><![CDATA[
[app]
    load ${app.home}/lib/*.jar
    optionally ${app.home}/lib/ext/*.jar
    load ${tools.jar}
]]></source>

      <p>
      The <code>import</code> directive specifies that certain
      packages should be imported and loaded by way of another
      realm.  The <code>import</code> directive is equivalent
      to the <code>importFrom(..)</code> method of
      <code>ClassRealm</code>.
      </p>

<source><![CDATA[
[app]
    ...
  
[subcomponent]
    import com.werken.projectz.Foo from app
    ...
]]></source>

    </subsection>

    <subsection name="Entry point methods">

      <p>
      <b>Classworlds</b> can be used to invoke any existing
      application's <code>main()</code> method.  Using the standard
      entry point does not allow for gaining access to the 
      <code>ClassWorld</code> of the application, but not all 
      applications will need it at run-time.
      </p>

      <p>
      For those applications that do require the <code>ClassWorld</code>
      instance, an alternative entry-point method signature can be
      provided.  Simply add a <code>ClassWorld</code> parameter to 
      the standard <code>main</code> parameter list.
      </p>

<source><![CDATA[
public class MyApp
{
    public static void main( String[] args, ClassWorld world )
    {
        ...     
    }
}
]]></source>

    </subsection>

  </section>

  </body>

</document>