package org.codehaus.plexus.classworlds;

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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UrlUtilsTest {

    @Test
    void normalizeUrlPath() {
        assertEquals("org/codehaus/Test.class", UrlUtils.normalizeUrlPath("org/codehaus/Test.class"));
        assertEquals("org/Test.class", UrlUtils.normalizeUrlPath("org/codehaus/../Test.class"));
        assertEquals("../../some.jar/org/Test.class", UrlUtils.normalizeUrlPath("../../some.jar/org/Test.class"));
    }

    @Test
    void normalizeUrlPathWithLeadingSlash() {
        assertEquals("org/Test.class", UrlUtils.normalizeUrlPath("/org/Test.class"));
        assertEquals("test.txt", UrlUtils.normalizeUrlPath("/test.txt"));
        assertEquals("a/b/c", UrlUtils.normalizeUrlPath("/a/b/c"));
    }

    @Test
    void normalizeUrlPathWithMultipleDotDots() {
        assertEquals("a/../Test.class", UrlUtils.normalizeUrlPath("a/b/../../Test.class"));
        assertEquals("org/codehaus/../Test.class", UrlUtils.normalizeUrlPath("org/codehaus/plexus/../../Test.class"));
    }

    @Test
    void normalizeUrlPathWithDotDotAtBeginning() {
        assertEquals("../org/Test.class", UrlUtils.normalizeUrlPath("../org/Test.class"));
        assertEquals("../../org/Test.class", UrlUtils.normalizeUrlPath("../../org/Test.class"));
    }

    @Test
    void normalizeUrlPathWithDotDotInMiddle() {}

    @Test
    void normalizeUrlPathWithConsecutiveDotDots() {
        assertEquals(
                "org/codehaus/a/b/../../Test.class",
                UrlUtils.normalizeUrlPath("org/codehaus/plexus/../a/b/../../Test.class"));
    }

    @Test
    void normalizeUrlPathWithTrailingSlash() {
        assertEquals("org/", UrlUtils.normalizeUrlPath("/org/"));
    }

    @Test
    void normalizeUrlPathEmptyString() {
        assertEquals("", UrlUtils.normalizeUrlPath(""));
    }

    @Test
    void normalizeUrlPathOnlySlash() {
        assertEquals("", UrlUtils.normalizeUrlPath("/"));
    }

    @Test
    void normalizeUrlPathComplexPath() {
        assertEquals(
                "org/codehaus/plexus/a/b/Test.class", UrlUtils.normalizeUrlPath("org/codehaus/plexus/a/b/Test.class"));
        assertEquals(
                "org/codehaus/plexus/Test.class",
                UrlUtils.normalizeUrlPath("org/codehaus/plexus/../plexus/Test.class"));
    }

    @Test
    void getURLsWithClassLoader() throws Exception {
        URL url1 = new URL("file:/tmp/test1.jar");
        URL url2 = new URL("file:/tmp/test2.jar");
        URL[] urls = {url1, url2};

        URLClassLoader classLoader = new URLClassLoader(urls);

        Set<URL> result = UrlUtils.getURLs(classLoader);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(url1));
        assertTrue(result.contains(url2));
    }

    @Test
    void getURLsWithEmptyClassLoader() throws Exception {
        URLClassLoader classLoader = new URLClassLoader(new URL[0]);

        Set<URL> result = UrlUtils.getURLs(classLoader);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getURLsWithSingleURL() throws Exception {
        URL url = new URL("file:/tmp/test.jar");
        URLClassLoader classLoader = new URLClassLoader(new URL[] {url});

        Set<URL> result = UrlUtils.getURLs(classLoader);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(url));
    }
}
