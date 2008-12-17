package org.codehaus.plexus.classworlds.launcher;

/*
 * Copyright 2001-2006 Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File;
import java.net.URL;

import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;


/**
 * Receive notification of the logical content of launcher configuration.
 *
 * @author Igor Fedorenko
 */
public interface ConfigurationHandler {

  void setAppMain(String mainClassName, String mainRealmName);

  void addRealm(String realmName) throws DuplicateRealmException;

  void addImportFrom(String relamName, String importSpec) throws NoSuchRealmException;

  void addLoadFile(File file);

  void addLoadURL(URL url);

}
