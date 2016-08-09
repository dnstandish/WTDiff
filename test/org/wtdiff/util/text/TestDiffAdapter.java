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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.text.TextUtil.LineSeparator;
import org.junit.After;

public class TestDiffAdapter {

    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSimple() throws IOException {
        List<String> oldLines = Arrays.asList("1", "2", "3");
        List<String> newLines = Arrays.asList("a", "b", "c");
        SimpleDiffData oldData = new SimpleDiffData("old", oldLines, LineSeparator.CR, false);
        SimpleDiffData newData = new SimpleDiffData("new", newLines, LineSeparator.LF, true);

        SimpleTestDiffWriter w = new SimpleTestDiffWriter();
        DummyDiffAdapter a = new DummyDiffAdapter(oldData, newData, w);
        
        assertEquals("old", a.theOldName());
        assertEquals("new", a.theNewName());
        assertEquals(oldLines, a.theOldLines());
        assertEquals(newLines, a.theNewLines());
        assertFalse( a.theOldIsMissingFinalLineSep());
        assertTrue( a.theNewIsMissingFinalLineSep() );
        assertEquals(w, a.theDiffWriter());
        
        List<String> warningList = a.theMissingFinalLineSepWarning("_NAME_");
        assertEquals( 1, warningList.size() );
        assertTrue( warningList.get(0).contains("_NAME_") );
        
    }
    
    @Test
    public void testLineCountHinting() throws IOException {

        List<String> nineLines = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
        List<String> tenLines = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");
        SimpleDiffData nineData = new SimpleDiffData("nine", nineLines, LineSeparator.CR, false);
        SimpleDiffData tenData = new SimpleDiffData("ten", tenLines, LineSeparator.LF, true);

        {
            SimpleTestDiffWriter w = new SimpleTestDiffWriter();
            new DummyDiffAdapter(nineData, tenData, w);        
            w.append(Arrays.asList("1"), 1, Arrays.asList("2"), 2);
            assertEquals(
                "02[1\n]\n03[2\n]\n",
                w.toString()
            );
            w.reset();
        }

        {
            SimpleTestDiffWriter w = new SimpleTestDiffWriter();
            new DummyDiffAdapter(tenData, nineData, w);        
            w.append(Arrays.asList("1"), 1, Arrays.asList("2"), 2);
            assertEquals(
                "02[1\n]\n03[2\n]\n",
                w.toString()
            );
            w.reset();
        }
    }
    
    

}
