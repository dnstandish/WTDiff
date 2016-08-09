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
import org.wtdiff.util.text.DiffWriter;
import org.wtdiff.util.text.NormalDiffAdapter;
import org.wtdiff.util.text.TextUtil.LineSeparator;

import difflib.DiffUtils;
import difflib.Patch;
import org.junit.After;

public class TestNormalDiffAdapter {

    
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEmptyDiff() throws IOException {
        List<String> oldLines = Arrays.asList("1", "2", "3");
        List<String> newLines = Arrays.asList("1", "2", "3");
        {
            SimpleDiffData oldData = new SimpleDiffData("old", oldLines, LineSeparator.CR, false);
            SimpleDiffData newData = new SimpleDiffData("new", newLines, LineSeparator.LF, false);
            Patch patch = DiffUtils.diff(oldData.getLines(), newData.getLines());
            SimpleTestDiffWriter w = new SimpleTestDiffWriter();
            NormalDiffAdapter a = new NormalDiffAdapter(oldData, newData, w);
    
            a.format(patch);
            
            assertEquals("", w.history.toString());
        }

        {
            SimpleDiffData oldData = new SimpleDiffData("old", oldLines, LineSeparator.CR, true);
            SimpleDiffData newData = new SimpleDiffData("new", newLines, LineSeparator.LF, true);
            Patch patch = DiffUtils.diff(oldData.getLines(), newData.getLines());
            SimpleTestDiffWriter w = new SimpleTestDiffWriter();
            NormalDiffAdapter a = new NormalDiffAdapter(oldData, newData, w);
    
            a.format(patch);
            
            assertEquals("", w.history.toString());
        }

        {
            SimpleDiffData oldData = new SimpleDiffData("old", oldLines, LineSeparator.CR, true);
            SimpleDiffData newData = new SimpleDiffData("new", newLines, LineSeparator.LF, false);
            Patch patch = DiffUtils.diff(oldData.getLines(), newData.getLines());
            SimpleTestDiffWriter w = new SimpleTestDiffWriter();
            NormalDiffAdapter a = new NormalDiffAdapter(oldData, newData, w);
    
            a.format(patch);
//            System.out.println(w.history);
            StringBuilder expected = new StringBuilder();
            expected.append(DiffWriter.ChangeType.WARNING);
            expected.append("[3,3,3,2]\n");
            assertTrue( w.history.toString().startsWith(expected.toString()));
            String[] actual = w.history.toString().split("\\n");
            assertEquals(4, actual.length);
            assertTrue(actual[1].startsWith("3["));
            assertTrue(actual[1].contains("old"));
            assertEquals("]", actual[2]);
            assertEquals("3[]", actual[3]);
            
        }

        {
            SimpleDiffData oldData = new SimpleDiffData("old", oldLines, LineSeparator.CR, false);
            SimpleDiffData newData = new SimpleDiffData("new", newLines, LineSeparator.LF, true);
            Patch patch = DiffUtils.diff(oldData.getLines(), newData.getLines());
            SimpleTestDiffWriter w = new SimpleTestDiffWriter();
            NormalDiffAdapter a = new NormalDiffAdapter(oldData, newData, w);
    
            a.format(patch);
//            System.out.println(w.history);
            StringBuilder expected = new StringBuilder();
            expected.append(DiffWriter.ChangeType.WARNING);
            expected.append("[3,2,3,3]\n");
            assertTrue( w.history.toString().startsWith(expected.toString()));
            String[] actual = w.history.toString().split("\\n");
            assertEquals(4, actual.length);
            assertEquals("3[]", actual[1]);
            assertTrue(actual[2].startsWith("3["));
            assertTrue(actual[2].contains("new"));
            assertEquals("]", actual[3]);
            
        }
    }
    
    private void diffOutTest(List<String> oldLines, List<String> newLines, String expected) throws IOException {
        SimpleDiffData oldData = new SimpleDiffData("old", oldLines, LineSeparator.CR, false);
        SimpleDiffData newData = new SimpleDiffData("new", newLines, LineSeparator.LF, false);
        Patch patch = DiffUtils.diff(oldData.getLines(), newData.getLines());
        SimpleTestDiffWriter w = new SimpleTestDiffWriter();
        NormalDiffAdapter a = new NormalDiffAdapter(oldData, newData, w);
        a.format(patch);
        assertEquals(expected, w.history.toString());
        
    }
    
    @Test
    public void testSingleDelete() throws IOException {
        diffOutTest(
            Arrays.asList("1", "2", "3"), 
            Arrays.asList("2", "3"),
            "" + DiffWriter.ChangeType.OLD_ONLY + "[0,0,0,-1]\n1[1\n]\n1[]\n"
        );

        diffOutTest(
            Arrays.asList("1", "2", "3"), 
            Arrays.asList("3"),
            "" + DiffWriter.ChangeType.OLD_ONLY + "[0,1,0,-1]\n1[1\n2\n]\n1[]\n"
        );

        
        diffOutTest(
            Arrays.asList("1", "2", "3"), 
            Arrays.asList("1", "3"),
            "" + DiffWriter.ChangeType.OLD_ONLY + "[1,1,1,0]\n2[2\n]\n2[]\n"
        );

        diffOutTest(
            Arrays.asList("1", "2", "3"), 
            Arrays.asList("1", "2"),
            "" + DiffWriter.ChangeType.OLD_ONLY + "[2,2,2,1]\n3[3\n]\n3[]\n"
        );

        diffOutTest(
            Arrays.asList("1", "2", "3"), 
            Arrays.asList("1"),
            "" + DiffWriter.ChangeType.OLD_ONLY + "[1,2,1,0]\n2[2\n3\n]\n2[]\n"
        );
    }
    
    @Test
    public void testSingleAdd() throws IOException {

        diffOutTest(
            Arrays.asList("2", "3"), 
            Arrays.asList("1", "2", "3"), 
            "" + DiffWriter.ChangeType.NEW_ONLY + "[0,-1,0,0]\n1[]\n1[1\n]\n"
        );

        diffOutTest(
            Arrays.asList("3"), 
            Arrays.asList("1", "2", "3"), 
            "" + DiffWriter.ChangeType.NEW_ONLY + "[0,-1,0,1]\n1[]\n1[1\n2\n]\n"
        );

        diffOutTest(
            Arrays.asList("1", "3"), 
            Arrays.asList("1", "2", "3"), 
            "" + DiffWriter.ChangeType.NEW_ONLY + "[1,0,1,1]\n2[]\n2[2\n]\n"
        );
    
        diffOutTest(
            Arrays.asList("1", "2"), 
            Arrays.asList("1", "2", "3"), 
            "" + DiffWriter.ChangeType.NEW_ONLY + "[2,1,2,2]\n3[]\n3[3\n]\n"
        );
    
        diffOutTest(
            Arrays.asList("1"), 
            Arrays.asList("1", "2", "3"), 
            "" + DiffWriter.ChangeType.NEW_ONLY + "[1,0,1,2]\n2[]\n2[2\n3\n]\n"
        );
    
    }

    @Test
    public void testSingleChange() throws IOException {

        diffOutTest(
            Arrays.asList("1.1", "2", "3"), 
            Arrays.asList("1.2", "2", "3"), 
            "" + DiffWriter.ChangeType.CHANGED + "[0,0,0,0]\n1[1.1\n]\n1[1.2\n]\n"
        );
    
        diffOutTest(
            Arrays.asList("1.1", "2.1", "3"), 
            Arrays.asList("1.2", "2.2", "3"), 
            "" + DiffWriter.ChangeType.CHANGED + "[0,1,0,1]\n1[1.1\n2.1\n]\n1[1.2\n2.2\n]\n"
        );
    
        diffOutTest(
            Arrays.asList("1", "2", "3.1"), 
            Arrays.asList("1", "2", "3.2"), 
            "" + DiffWriter.ChangeType.CHANGED + "[2,2,2,2]\n3[3.1\n]\n3[3.2\n]\n"
        );
    
    }

    @Test
    public void testTwoDifferences() throws IOException {
        {
            diffOutTest(
                Arrays.asList("1.1", "2", "3", "4"), 
                Arrays.asList("1.2", "2", "3"), 
                "" + DiffWriter.ChangeType.CHANGED + "[0,0,0,0]\n1[1.1\n]\n1[1.2\n]\n" +
                "" + DiffWriter.ChangeType.OLD_ONLY + "[3,3,3,2]\n4[4\n]\n4[]\n"
            );
        }
    
    }

}
