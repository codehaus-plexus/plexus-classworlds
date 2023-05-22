package org.codehaus.classworlds;

/*
 * Copyright 2001-2010 Codehaus Foundation.
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

/**
 * A compatibility wrapper for org.codehaus.plexus.classworlds.launcher.Launcher
 * provided for legacy code
 *
 * @author Andrew Williams
 */
@Deprecated
public class Launcher extends org.codehaus.plexus.classworlds.launcher.Launcher {
    public Launcher() {}

    // ------------------------------------------------------------
    //     Class methods
    // ------------------------------------------------------------

    /**
     * Launch the launcher from the command line.
     * Will exit using System.exit with an exit code of 0 for success, 100 if there was an unknown exception,
     * or some other code for an application error.
     *
     * @param args The application command-line arguments.
     */
    public static void main(String[] args) {
        org.codehaus.plexus.classworlds.launcher.Launcher.main(args);
    }

    /**
     * Launch the launcher.
     *
     * @param args The application command-line arguments.
     * @return an integer exit code
     * @throws Exception If an error occurs.
     */
    public static int mainWithExitCode(String[] args) throws Exception {
        return org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode(args);
    }
}
