/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.codehaus.plexus.classworlds.realm;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.eclipse.transformer.TransformException;
import org.eclipse.transformer.action.ActionContext;
import org.eclipse.transformer.action.ActionType;
import org.eclipse.transformer.action.ByteData;
import org.eclipse.transformer.action.impl.ClassActionImpl;
import org.eclipse.transformer.action.impl.SelectionRuleImpl;
import org.eclipse.transformer.action.impl.ServiceLoaderConfigActionImpl;
import org.eclipse.transformer.action.impl.SignatureRuleImpl;
import org.eclipse.transformer.action.impl.ZipActionImpl;
import org.eclipse.transformer.util.FileUtils;
import org.eclipse.transformer.util.SignatureUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ClassRealm} that integrates {@link org.eclipse.transformer.Transformer}.
 *
 * @since TBD
 */
public class TransformerClassRealm extends ClassRealm {
    private final ZipActionImpl jarAction;
    private final ClassActionImpl classAction;
    private final ServiceLoaderConfigActionImpl serviceConfigAction;

    public static TransformerClassRealm newJakartaTransformerClassRealm(
            ClassWorld world, String id, ClassLoader baseClassLoader) {
        Logger logger = LoggerFactory.getLogger(TransformerClassRealm.class);
        ActionContext context = new ActionContext(
                logger,
                new SelectionRuleImpl(logger, getIncludes(), getExcludes()),
                new SignatureRuleImpl(
                        logger, getToJakartaRenames(), null, null, null, null, null, Collections.emptyMap()));

        ZipActionImpl jarAction = new ZipActionImpl(context, ActionType.JAR, false);
        ClassActionImpl classAction = jarAction.addUsing(ClassActionImpl::new);
        ServiceLoaderConfigActionImpl configAction = jarAction.addUsing(ServiceLoaderConfigActionImpl::new);
        return new TransformerClassRealm(world, id, baseClassLoader, jarAction, classAction, configAction);
    }

    private static Map<String, String> getIncludes() {
        Map<String, String> includes = new HashMap<>();
        includes.put("*", "");
        // includes.put("org/apache/*", "");
        // includes.put("org/codehaus/*", "");
        // includes.put("org/mojohaus/*", "");
        // includes.put("org/eclipse/*", "");
        return includes;
    }

    private static Map<String, String> getExcludes() {
        Map<String, String> excludes = new HashMap<>();
        excludes.put("org/google/inject/*", "");
        excludes.put("org/codehaus/plexus/classworlds/*", "");
        return excludes;
    }

    private static Map<String, String> getToJakartaRenames() {
        Map<String, String> renames = new HashMap<>();
        renames.put("javax.inject.*", "jakarta.inject");
        return renames;
    }

    /**
     * Creates a new class realm.
     *
     * @param world The class world this realm belongs to, must not be <code>null</code>.
     * @param id The identifier for this realm, must not be <code>null</code>.
     * @param baseClassLoader The base class loader for this realm, may be <code>null</code> to use the bootstrap class
     * @param jarAction the action to apply to each jar file loaded through this class loader.
     * @param classAction the action to apply to each class loaded through this class loader.
     * @param serviceConfigAction the action to apply to each service configuration file loaded through this class loader.
     */
    public TransformerClassRealm(
            ClassWorld world,
            String id,
            ClassLoader baseClassLoader,
            ZipActionImpl jarAction,
            ClassActionImpl classAction,
            ServiceLoaderConfigActionImpl serviceConfigAction) {
        super(world, id, baseClassLoader);
        this.jarAction = jarAction;
        this.classAction = classAction;
        this.serviceConfigAction = serviceConfigAction;
    }

    protected ZipActionImpl getJarAction() {
        return jarAction;
    }

    protected ClassActionImpl getClassAction() {
        return classAction;
    }

    protected ServiceLoaderConfigActionImpl getServiceConfigAction() {
        return serviceConfigAction;
    }

    protected boolean selectResource(String resourceName) {
        return getJarAction().selectResource(resourceName);
    }

    public String getResourceName(String className) {
        return SignatureUtils.classNameToResourceName(className);
    }

    protected boolean acceptClass(String resourceName) {
        return getClassAction().acceptResource(resourceName);
    }

    protected InputStream applyClass(String resourceName, InputStream inputStream) throws TransformException {
        ClassActionImpl classAction = getClassAction();
        ByteData inputData = classAction.collect(resourceName, inputStream);
        ByteData outputData = classAction.apply(inputData);
        return outputData.stream();
    }

    protected boolean acceptServiceConfig(String resourceName) {
        ServiceLoaderConfigActionImpl configAction = getServiceConfigAction();
        return ((configAction != null) && configAction.acceptResource(resourceName));
    }

    protected InputStream applyServiceConfig(String resourceName, InputStream inputStream) throws TransformException {
        ServiceLoaderConfigActionImpl configAction = getServiceConfigAction();
        if (configAction == null) {
            return inputStream;
        } else {
            ByteData inputData = configAction.collect(resourceName, inputStream);
            ByteData outputData = configAction.apply(inputData);
            return outputData.stream();
        }
    }

    protected class TransformClassURLStreamHandler extends URLStreamHandler {
        private final URL baseURL;

        protected TransformClassURLStreamHandler(URL baseURL) {
            this.baseURL = baseURL;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new TransformClassURLConnection(baseURL);
        }
    }

    protected class TransformClassURLConnection extends URLConnection {
        private final URLConnection baseConnection;

        protected TransformClassURLConnection(URL baseURL) throws IOException {
            super(baseURL);
            this.baseConnection = this.getURL().openConnection();
        }

        public URLConnection getBaseConnection() {
            return baseConnection;
        }

        @Override
        public void connect() throws IOException {
            getBaseConnection().connect();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            URLConnection useBaseConnection = getBaseConnection();
            String baseName = useBaseConnection.getURL().toString();
            InputStream baseStream = useBaseConnection.getInputStream();
            return applyClass(baseName, baseStream);
        }
    }

    protected class TransformConfigURLStreamHandler extends URLStreamHandler {
        private final URL baseURL;

        protected TransformConfigURLStreamHandler(URL baseURL) {
            this.baseURL = baseURL;
        }

        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new TransformConfigURLConnection(baseURL);
        }
    }

    protected class TransformConfigURLConnection extends URLConnection {
        private final URLConnection baseConnection;

        protected TransformConfigURLConnection(URL baseURL) throws IOException {
            super(baseURL);
            this.baseConnection = this.getURL().openConnection();
        }

        public URLConnection getBaseConnection() {
            return baseConnection;
        }

        @Override
        public void connect() throws IOException {
            getBaseConnection().connect();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            URLConnection useBaseConnection = getBaseConnection();
            String baseName = useBaseConnection.getURL().toString();
            InputStream baseStream = useBaseConnection.getInputStream();
            return applyServiceConfig(baseName, baseStream);
        }
    }

    protected URL transformAsClass(URL baseURL) {
        String baseText = baseURL.toString();
        try {
            return new URL(null, baseText, new TransformClassURLStreamHandler(baseURL));
        } catch (MalformedURLException e) {
            return baseURL;
        }
    }

    protected URL transformAsServiceConfig(URL baseURL) {
        String baseText = baseURL.toString();
        try {
            return new URL(null, baseText, new TransformConfigURLStreamHandler(baseURL));
        } catch (MalformedURLException e) {
            return baseURL;
        }
    }

    @Override
    public URL findResource(String name) {
        URL baseURL = super.findResource(name);
        if (baseURL == null) {
            return null;
        } else {
            return transform(name, baseURL);
        }
    }

    protected URL transform(String name, URL baseURL) {
        if (!selectResource(name)) {
            return baseURL;
        } else if (acceptClass(name)) {
            return transformAsClass(baseURL);
        } else if (acceptServiceConfig(name)) {
            return transformAsServiceConfig(baseURL);
        } else {
            return baseURL;
        }
    }

    private final String SISU_JAVAX_RESOURCE = "META-INF/sisu/javax.inject.Named";
    private final String SISU_JAKARTA_RESOURCE = "META-INF/sisu/jakarta.inject.Named";

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        if (SISU_JAKARTA_RESOURCE.equals(name)) {
            return doFindSisuIndexes();
        } else {
            return doFindResources(name);
        }
    }

    protected Enumeration<URL> doFindSisuIndexes() throws IOException {
        Enumeration<URL> javax = super.findResources(SISU_JAKARTA_RESOURCE);
        Enumeration<URL> jakarta = super.findResources(SISU_JAVAX_RESOURCE);
        Vector<URL> indexes = new Vector<>();
        while (javax.hasMoreElements()) {
            indexes.add(javax.nextElement());
        }
        while (jakarta.hasMoreElements()) {
            indexes.add(jakarta.nextElement());
        }
        return indexes.elements();
    }

    protected Enumeration<URL> doFindResources(String name) throws IOException {
        Enumeration<URL> baseURLs = super.findResources(name);
        if (!baseURLs.hasMoreElements()) {
            return baseURLs;
        } else if (!selectResource(name)) {
            return baseURLs;
        } else {
            Vector<URL> transformedURLs = new Vector<>();
            while (baseURLs.hasMoreElements()) {
                transformedURLs.add(transform(name, baseURLs.nextElement()));
            }
            return transformedURLs.elements();
        }
    }

    @Override
    protected Class<?> findClassInternal(String className) throws ClassNotFoundException {
        String resourceName = getResourceName(className);
        if (!selectResource(resourceName)) {
            return super.findClassInternal(className);
        }
        URL res = loadResourceFromSelf(resourceName);
        if (res == null) {
            throw new ClassNotFoundException(resourceName);
        }
        try (InputStream classStream = res.openStream()) {
            if (classStream == null) {
                throw new ClassNotFoundException(className);
            }
            ByteBuffer classData;
            try {
                classData = FileUtils.read(className, classStream);
            } catch (IOException e) {
                throw new ClassNotFoundException(className, e);
            }
            return super.defineClass(className, classData.array(), classData.arrayOffset(), classData.limit());
        } catch (IOException e) {
            throw new ClassNotFoundException(className, e);
        }
    }
}
