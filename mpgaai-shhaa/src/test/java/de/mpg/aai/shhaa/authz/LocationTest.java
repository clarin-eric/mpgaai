/*
 * Copyright 2014 computing center garching of the max planck society.
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
package de.mpg.aai.shhaa.authz;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author twagoo
 */
public class LocationTest {

    /**
     * Test of getTarget method, of class Location.
     */
    @Test
    public void testGetTarget() {
        Location location = new Location("/my/path", Matcher.MATCHMODE_CASE_SENSITIVE);
        assertEquals("/my/path", location.getTarget());
    }

    /**
     * Test of matchesPath method, of class Location.
     */
    @Test
    public void testMatchesPathCaseSensitive() {
        Location location = new Location("/my/path", Matcher.MATCHMODE_CASE_SENSITIVE);
        assertTrue(location.matchesPath("/my/path"));
        assertFalse(location.matchesPath("/MY/PATH"));
    }

    /**
     * Test of matchesPath method, of class Location.
     */
    @Test
    public void testMatchesPathCaseIgnore() {
        Location location = new Location("/my/path", Matcher.MATCHMODE_CASE_IGNORE);
        assertTrue(location.matchesPath("/my/path"));
        assertTrue(location.matchesPath("/MY/PATH"));
    }

    /**
     * Test of matchesPath method, of class Location.
     */
    @Test
    public void testMatchesPathWildcard() {
        Location location = new Location("/my/*", Matcher.MATCHMODE_CASE_SENSITIVE);
        assertTrue(location.matchesPath("/my/"));
        assertTrue(location.matchesPath("/my/path1"));
        assertTrue(location.matchesPath("/my/path2"));
        assertFalse(location.matchesPath("/my"));
        assertFalse(location.matchesPath("/some/other/path"));

        location = new Location("/my/*", Matcher.MATCHMODE_CASE_IGNORE);
        assertTrue(location.matchesPath("/MY/"));
        assertTrue(location.matchesPath("/mY/path1"));
        assertTrue(location.matchesPath("/My/path2"));
    }

    /**
     * Test of matchesMethod method, of class Location.
     */
    @Test
    public void testMatchesMethod() {
        // no methods specified
        Location location = new Location("/my/path", Matcher.MATCHMODE_CASE_SENSITIVE, new String[]{});
        assertTrue(location.matchesMethod("GET"));
        assertTrue(location.matchesMethod("POST"));
        assertTrue(location.matchesMethod("PUT"));
        assertTrue(location.matchesMethod("DELETE"));

        // one method
        location = new Location("/my/path", Matcher.MATCHMODE_CASE_SENSITIVE, new String[]{"GET"});
        assertTrue(location.matchesMethod("GET"));
        assertTrue(location.matchesMethod("get"));
        assertFalse(location.matchesMethod("POST"));

        // multiple methods
        location = new Location("/my/path", Matcher.MATCHMODE_CASE_SENSITIVE, new String[]{"PUT", "POST", "DELETE"});
        assertTrue(location.matchesMethod("put"));
        assertTrue(location.matchesMethod("POST"));
        assertTrue(location.matchesMethod("delete"));
        assertFalse(location.matchesMethod("GET"));
    }

}
