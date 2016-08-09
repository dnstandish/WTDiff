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
package org.wtdiff.util.ui;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.text.DiffWriter;
import org.wtdiff.util.text.DiffWriter.ChangeType;
import org.wtdiff.util.ui.NormalDocumentDiffWriter;
import org.junit.After;

public class TestNormalDocumentDiffWriter {
    
    private static String ls = System.getProperty("line.separator");
    @Before
    public void setUp() throws Exception {
    }
    @Test
    public void testChangeInfo() throws Exception  {
        StyledDocument doc = new DefaultStyledDocument();
        
        DiffWriter w = new NormalDocumentDiffWriter(doc);
        int startIndex = doc.getLength();
        w.newChange(ChangeType.NEW_ONLY, 0, -1, 0, 0);        
        assertEquals("0a1" + ls, doc.getText(startIndex, doc.getLength()) );
                
        startIndex = doc.getLength();
        w.newChange(ChangeType.NEW_ONLY, 1, 0, 1, 2);        
//        System.out.println(oldEndIndex);
//        System.out.println(doc.getText(1, doc.getLength()));
        assertEquals("1a2,3" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );

        startIndex = doc.getLength();
        w.newChange(ChangeType.OLD_ONLY, 2, 2, 2, 1);        
        assertEquals("3d2" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );
        
        startIndex = doc.getLength();
        w.newChange(ChangeType.OLD_ONLY, 2, 3, 2, 1);        
        assertEquals("3,4d2" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );
        
        startIndex = doc.getLength();
        w.newChange(ChangeType.CHANGED, 3, 3, 2, 2);        
        assertEquals("4c3" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );
        
        startIndex = doc.getLength();
        w.newChange(ChangeType.CHANGED, 4, 5, 2, 2);        
        assertEquals("5,6c3" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );
        
        startIndex = doc.getLength();
        w.newChange(ChangeType.CHANGED, 10, 11, 45, 46);        
        assertEquals("11,12c46,47" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );
        
        startIndex = doc.getLength();
        w.newChange(ChangeType.COMMON, 20, 22, 50, 52);        
        assertEquals(startIndex, doc.getLength());
        
        startIndex = doc.getLength();
        w.newChange(ChangeType.WARNING, 20, 22, 50, 52);        
        assertEquals(startIndex, doc.getLength());
        
    }

    @Test
    public void testAppend() throws Exception  {
        StyledDocument doc = new DefaultStyledDocument();
        
        DiffWriter w = new NormalDocumentDiffWriter(doc);
        int startIndex = doc.getLength();
        w.append(new ArrayList<String>(0), 0, new ArrayList<String>(0), 0);
        //w.newChange(ChangeType.NEW_ONLY, 0, -1, 0, 0);        
        assertEquals(startIndex, doc.getLength());

        w.append(
            Arrays.asList("Common"), 0,
            Arrays.asList("Common"), 0
        );
//        assertEquals("  " , doc.getText(startIndex, 2) );
//        startIndex += 2;
        assertEquals("  Common" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );

        w.newChange(ChangeType.COMMON, 20, 22, 50, 52);        

        startIndex = doc.getLength();

        w.append(
            Arrays.asList("Common1", "Common2"), 0,
            Arrays.asList("Common1", "Common2"), 0
        );
        assertEquals("  Common1" + ls + "  Common2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        
        w.newChange(ChangeType.OLD_ONLY, 20, 20, 50, 49);
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old"), 19,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("< Old" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
    
        w.newChange(ChangeType.OLD_ONLY, 30, 31, 50, 49);
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old1", "Old2"), 29,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("< Old1" + ls + "< Old2" + ls, 
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        
        w.newChange(ChangeType.NEW_ONLY, 20, 19, 50, 50);
        startIndex = doc.getLength();        
        w.append(
            new ArrayList<String>(0), 19,
            Arrays.asList("New"), 50
        );
        assertEquals("> New" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.NEW_ONLY, 30, 29, 60, 61);
        startIndex = doc.getLength();        
        w.append(
            new ArrayList<String>(0), 29,
            Arrays.asList("New1", "New2"), 60
        );
        assertEquals("> New1" + ls + "> " + "New2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 40, 40, 70, 70);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("< Old" + ls + "---" + ls + "> New" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 41, 42, 71, 71);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old1", "Old2"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("< Old1" + ls + "< Old2" + ls + "---" + ls + "> New" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 43, 43, 72, 73);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old"), 43,
            Arrays.asList("New1", "New2"), 72
        );
        assertEquals("< Old" + ls + "---" + ls + "> New1" + ls + "> New2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 44, 45, 74, 75);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old1", "Old2"), 44,
            Arrays.asList("New1", "New2"), 74
        );
        assertEquals("< Old1" + ls + "< Old2" + ls + "---" + ls + "> New1" + ls + "> New2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        
    }

    @Test
    public void testAppendWithLineNos() throws Exception  {
        StyledDocument doc = new DefaultStyledDocument();
        
        DiffWriter w = new NormalDocumentDiffWriter(doc);
        w.setNumberLines(true);
        w.totalLinesHint(10);
        
        int startIndex = doc.getLength();
        w.append(new ArrayList<String>(0), 0, new ArrayList<String>(0), 0);
        //w.newChange(ChangeType.NEW_ONLY, 0, -1, 0, 0);        
        assertEquals(startIndex, doc.getLength());

        w.append(
            Arrays.asList("Common"), 0,
            Arrays.asList("Common"), 1
        );
//        assertEquals("  " , doc.getText(startIndex, 2) );
//        startIndex += 2;
        assertEquals("01  Common" + ls, doc.getText(startIndex, doc.getLength() - startIndex) );

        w.newChange(ChangeType.COMMON, 20, 22, 50, 52);        

        startIndex = doc.getLength();

        w.append(
            Arrays.asList("Common1", "Common2"), 0,
            Arrays.asList("Common1", "Common2"), 1
        );
        assertEquals("01  Common1" + ls + "02  Common2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        
        w.newChange(ChangeType.OLD_ONLY, 20, 20, 50, 49);
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old"), 19,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("20< Old" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
    
        w.newChange(ChangeType.OLD_ONLY, 30, 31, 50, 49);
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old1", "Old2"), 29,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("30< Old1" + ls + "31< Old2" + ls, 
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        
        w.newChange(ChangeType.NEW_ONLY, 20, 19, 50, 50);
        startIndex = doc.getLength();        
        w.append(
            new ArrayList<String>(0), 19,
            Arrays.asList("New"), 50
        );
        assertEquals("51> New" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.NEW_ONLY, 30, 29, 60, 61);
        startIndex = doc.getLength();        
        w.append(
            new ArrayList<String>(0), 29,
            Arrays.asList("New1", "New2"), 60
        );
        assertEquals("61> New1" + ls + "62> " + "New2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 40, 40, 70, 70);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("41< Old" + ls + "---" + ls + "71> New" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 41, 42, 71, 71);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old1", "Old2"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("41< Old1" + ls + "42< Old2" + ls + "---" + ls + "71> New" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 43, 43, 72, 73);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old"), 43,
            Arrays.asList("New1", "New2"), 72
        );
        assertEquals("44< Old" + ls + "---" + ls + "73> New1" + ls + "74> New2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.CHANGED, 44, 45, 74, 75);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("Old1", "Old2"), 44,
            Arrays.asList("New1", "New2"), 74
        );
        assertEquals("45< Old1" + ls + "46< Old2" + ls + "---" + ls + "75> New1" + ls + "76> New2" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        w.newChange(ChangeType.WARNING, 46, 46, 76, 76);        
        startIndex = doc.getLength();        
        w.append(
            Arrays.asList("WarnOld"), 46,
            Arrays.asList("WarnNew"), 76
        );
        assertEquals("47< WarnOld" + ls + "77> WarnNew" + ls,
            doc.getText(startIndex, doc.getLength() - startIndex) );
        
        
    }


}
