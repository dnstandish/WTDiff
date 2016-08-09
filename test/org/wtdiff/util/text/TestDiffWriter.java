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

import java.util.ArrayList;
import java.util.List;


import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.wtdiff.util.text.NonprintingCharStyle;

public class TestDiffWriter   {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testControlCharacterHandlingSetGet() {
        SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
        List<String> oldLines = new ArrayList<>();
        List<String> newLines = new ArrayList<>();
        oldLines.add("\r");
        
        // defaults to as is 
        assertEquals( NonprintingCharStyle.ASIS, testWriter.getControlCharaterHandling() );

        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[\r\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();
        
        // setting to escape sets to escape
        testWriter.setControlCharaterHandling(NonprintingCharStyle.ESCAPE);
        assertEquals( NonprintingCharStyle.ESCAPE, testWriter.getControlCharaterHandling() );

        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[\\r\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();
    
        // setting to box sets to box
        testWriter.setControlCharaterHandling(NonprintingCharStyle.BOX);
        assertEquals( NonprintingCharStyle.BOX, testWriter.getControlCharaterHandling() );

        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[\u25af\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();
        
        // setting to remove sets to remove
        testWriter.setControlCharaterHandling(NonprintingCharStyle.REMOVE);
        assertEquals( NonprintingCharStyle.REMOVE, testWriter.getControlCharaterHandling() );

        newLines.add("1\u0003z\r");
        testWriter.append(oldLines, 0, newLines, 0);
        
        assertEquals(
            "001[\n]\n001[1z\n]\n",
            testWriter.toString()
        );
        testWriter.reset();
        
        
        // setting to null will throw an exception 
        try {
            testWriter.setControlCharaterHandling(null);
            fail("invalid contral char setting should throw exception");
        } catch (IllegalArgumentException iae) {
            // this is normal
        }
        testWriter.reset();
    }

    @Test
    public void testIsNumberLinesSetGet() {
        SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
        
        // defaults to false 
        assertFalse( testWriter.isNumberLines() );

        // setting to true sets to true
        testWriter.setNumberLines(true);
        assertTrue( testWriter.isNumberLines() );

        // setting to false sets to false
        testWriter.setNumberLines(false);
        assertFalse( testWriter.isNumberLines() );
    }

    @Test
    public void testTabWidthSetGet() {
        SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
        List<String> oldLines = new ArrayList<>();
        List<String> newLines = new ArrayList<>();
        oldLines.add("1\t2");
        
        // defaults to 0, i.e. no change to tab chars 
        assertEquals( 0, testWriter.getTabWidth() );
        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[1\t2\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();

        
        // setting to 1 sets to 1
        testWriter.setTabWidth(1);
        assertEquals( 1, testWriter.getTabWidth() );
        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[1 2\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();

        // setting to 3 sets to 3
        testWriter.setTabWidth(3);
        assertEquals( 3, testWriter.getTabWidth() );
        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[1  2\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();

        // setting to 0 sets to 0
        testWriter.setTabWidth(0);
        assertEquals( 0, testWriter.getTabWidth() );
        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[1\t2\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();

        // setting to -1 sets to -1 (also no change to tabs)
        testWriter.setTabWidth(-1);
        assertEquals( -1, testWriter.getTabWidth() );
        testWriter.append(oldLines, 0, newLines, 0);
        assertEquals(
            "001[1\t2\n]\n001[]\n",
            testWriter.toString()
        );
        testWriter.reset();

    }

    @Test
    public void testNumberLineFormat() {
        List<String> oldLines = new ArrayList<>();
        List<String> newLines = new ArrayList<>();
        oldLines.add("1");
        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // defaults to 3, i.e. no change to tab chars
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "001[1\n]\n001[]\n",
                testWriter.toString()
            );
            testWriter.reset();

            testWriter.append(oldLines, 110, newLines, 0);
            assertEquals(
                "111[1\n]\n001[]\n",
                testWriter.toString()
            );
            testWriter.reset();
            
            testWriter.append(oldLines, 1110, newLines, 0);
            assertEquals(
                "1111[1\n]\n001[]\n",
                testWriter.toString()
            );
        }
        
        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // 0 hint 
            testWriter.totalLinesHint(0);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "1[1\n]\n1[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }

        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // -1 hint -> 2 
            testWriter.totalLinesHint(-1);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "01[1\n]\n01[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }

        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // 9 hint -> 1 
            testWriter.totalLinesHint(9);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "1[1\n]\n1[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }

        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // 10 hint -> 2 
            testWriter.totalLinesHint(10);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "01[1\n]\n01[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }

        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // 19 hint -> 2 
            testWriter.totalLinesHint(19);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "01[1\n]\n01[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }

        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // 99999 hint -> 5 
            testWriter.totalLinesHint(99999);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "00001[1\n]\n00001[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }
        
        {
            SimpleTestDiffWriter testWriter = new SimpleTestDiffWriter();
            // 100000 hint -> 6 
            testWriter.totalLinesHint(100000);
            testWriter.append(oldLines, 0, newLines, 0);
            assertEquals(
                "000001[1\n]\n000001[]\n",
                testWriter.toString()
            );
            testWriter.reset();
        }
        
    }

}
