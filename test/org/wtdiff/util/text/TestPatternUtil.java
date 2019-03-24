/*
Copyright 2015 David Standish

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package org.wtdiff.util.text;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

import org.wtdiff.util.ExceptionInputStream;
import org.wtdiff.util.text.TextUtil;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPatternUtil   {

    @Test
    public void testSimpleGlobToRegexp() {
        
        assertEquals(
            "^$",
            PatternUtil.SimpleGlobToRegexp("")
        );
        assertEquals(
            "^a$",
            PatternUtil.SimpleGlobToRegexp("a")
        );
        assertEquals(
            "^\\.$",
            PatternUtil.SimpleGlobToRegexp(".")
        );
        assertEquals(
            "^a\\.$",
            PatternUtil.SimpleGlobToRegexp("a.")
        );
        assertEquals(
            "^\\.a$",
            PatternUtil.SimpleGlobToRegexp(".a")
        );
        assertEquals(
            "^\\\\a$",
            PatternUtil.SimpleGlobToRegexp("\\a")
        );
        assertEquals(
            "^a.$",
            PatternUtil.SimpleGlobToRegexp("a?")
        );
        assertEquals(
            "^.*\\.txt$",
            PatternUtil.SimpleGlobToRegexp("*.txt")
        );
        assertEquals(
            "^\\{a\\}$",
            PatternUtil.SimpleGlobToRegexp("{a}")
        );
        assertEquals(
            "^\\(a\\)$",
            PatternUtil.SimpleGlobToRegexp("(a)")
        );
        assertEquals(
            "^[abc]$",
            PatternUtil.SimpleGlobToRegexp("[abc]")
        );
        assertEquals(
            "^a\\+$",
            PatternUtil.SimpleGlobToRegexp("a+")
        );
    }
    
    @Test
    public void testSimpleGlobToPattern() {
         Pattern p = PatternUtil.SimpleGlobToPattern("*.bak");
         assertTrue(p.matcher("a.bak").matches());
         assertFalse(p.matcher("a.bakk").matches());
         assertFalse(p.matcher("bak").matches());

         p = PatternUtil.SimpleGlobToPattern("*.b[ab]k");
         assertTrue(p.matcher("a.bak").matches());
         assertTrue(p.matcher(".bbk").matches());
         
    }
}
