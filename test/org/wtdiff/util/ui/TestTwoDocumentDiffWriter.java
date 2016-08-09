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
import org.wtdiff.util.ui.TwoDocumentDiffWriter;
import org.junit.After;

public class TestTwoDocumentDiffWriter {
    
    private static String ls = System.getProperty("line.separator");
    @Before
    public void setUp() throws Exception {
    }
    @Test
    public void testChangeInfo() throws Exception  {
        StyledDocument doc1 = new DefaultStyledDocument();
        StyledDocument doc2 = new DefaultStyledDocument();
        
        DiffWriter w = new TwoDocumentDiffWriter(doc1, doc2);
        int startIndex1 = doc1.getLength();
        int startIndex2 = doc2.getLength();
                
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.newChange(ChangeType.NEW_ONLY, 1, 0, 1, 2);        
        assertEquals("", doc1.getText(startIndex1, doc1.getLength()) );
        assertEquals("", doc2.getText(startIndex2, doc1.getLength()) );

        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.newChange(ChangeType.OLD_ONLY, 2, 3, 2, 1);        
        assertEquals("", doc1.getText(startIndex1, doc1.getLength()) );
        assertEquals("", doc2.getText(startIndex2, doc1.getLength()) );
        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.newChange(ChangeType.CHANGED, 10, 11, 45, 46);        
        assertEquals("", doc1.getText(startIndex1, doc1.getLength()) );
        assertEquals("", doc2.getText(startIndex2, doc1.getLength()) );
        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.newChange(ChangeType.COMMON, 20, 22, 50, 52);        
        assertEquals("", doc1.getText(startIndex1, doc1.getLength()) );
        assertEquals("", doc2.getText(startIndex2, doc1.getLength()) );
        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.newChange(ChangeType.WARNING, 20, 22, 50, 52);        
        assertEquals("", doc1.getText(startIndex1, doc1.getLength()) );
        assertEquals("", doc2.getText(startIndex2, doc1.getLength()) );
        
    }

    @Test
    public void testAppend() throws Exception  {
        StyledDocument doc1 = new DefaultStyledDocument();
        StyledDocument doc2 = new DefaultStyledDocument();
        
        DiffWriter w = new TwoDocumentDiffWriter(doc1, doc2);
        int startIndex1 = doc1.getLength();
        int startIndex2 = doc2.getLength();
        w.append(new ArrayList<String>(0), 0, new ArrayList<String>(0), 0);
        assertEquals(startIndex1, doc1.getLength());

        w.append(
            Arrays.asList("Old"), 0,
            Arrays.asList("New"), 0
        );
        assertEquals("  Old" + ls, doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("  New" + ls, doc2.getText(startIndex2, doc2.getLength() - startIndex2) );

        w.newChange(ChangeType.COMMON, 20, 22, 50, 52);        

        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();

        w.append(
            Arrays.asList("Oommon1", "Oommon2"), 0,
            Arrays.asList("Nommon1", "Nommon2"), 0
        );
        assertEquals("  Oommon1" + ls + "  Oommon2" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("  Nommon1" + ls + "  Nommon2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        
        w.newChange(ChangeType.OLD_ONLY, 20, 20, 50, 49);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old"), 19,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("- Old" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals(ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
    
        w.newChange(ChangeType.OLD_ONLY, 30, 31, 50, 49);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old1", "Old2"), 29,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("- Old1" + ls + "- Old2" + ls, 
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals(ls+ ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        
        w.newChange(ChangeType.NEW_ONLY, 20, 19, 50, 50);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            new ArrayList<String>(0), 19,
            Arrays.asList("New"), 50
        );
        assertEquals(ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("+ New" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.NEW_ONLY, 30, 29, 60, 61);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            new ArrayList<String>(0), 29,
            Arrays.asList("New1", "New2"), 60
        );
        assertEquals(ls + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("+ New1" + ls + "+ " + "New2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 40, 40, 70, 70);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("! Old" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("! New" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 41, 42, 71, 71);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old1", "Old2"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("! Old1" + ls + "! Old2" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("! New" + ls + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 43, 43, 72, 73);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old"), 43,
            Arrays.asList("New1", "New2"), 72
        );
        assertEquals("! Old" + ls + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("! New1" + ls + "! New2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 44, 45, 74, 75);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old1", "Old2"), 44,
            Arrays.asList("New1", "New2"), 74
        );
        assertEquals("! Old1" + ls + "! Old2" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("! New1" + ls + "! New2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        
    }

    @Test
    public void testAppendWithLineNos() throws Exception  {
        StyledDocument doc1 = new DefaultStyledDocument();
        StyledDocument doc2 = new DefaultStyledDocument();
        
        DiffWriter w = new TwoDocumentDiffWriter(doc1, doc2);
        w.setNumberLines(true);
        w.totalLinesHint(100);
        
        int startIndex1 = doc1.getLength();
        int startIndex2 = doc2.getLength();
        w.append(new ArrayList<String>(0), 0, new ArrayList<String>(0), 0);
        //w.newChange(ChangeType.NEW_ONLY, 0, -1, 0, 0);        
        assertEquals(startIndex1, doc1.getLength());
        assertEquals(startIndex2, doc2.getLength());

        w.append(
            Arrays.asList("Oommon"), 0,
            Arrays.asList("Nommon"), 1
        );
        assertEquals("001  Oommon" + ls, doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("002  Nommon" + ls, doc2.getText(startIndex2, doc2.getLength() - startIndex2) );

        w.newChange(ChangeType.COMMON, 20, 22, 50, 52);        

        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();

        w.append(
            Arrays.asList("Oommon1", "Oommon2"), 0,
            Arrays.asList("Nommon1", "Nommon2"), 1
        );
        assertEquals("001  Oommon1" + ls + "002  Oommon2" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("002  Nommon1" + ls + "003  Nommon2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        
        w.newChange(ChangeType.OLD_ONLY, 20, 20, 50, 49);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old"), 19,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("020- Old" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals(ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
    
        w.newChange(ChangeType.OLD_ONLY, 30, 31, 50, 49);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old1", "Old2"), 29,
            new ArrayList<String>(0), 49
        );
        
        assertEquals("030- Old1" + ls + "031- Old2" + ls, 
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals(ls + ls, 
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        
        w.newChange(ChangeType.NEW_ONLY, 20, 19, 50, 50);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            new ArrayList<String>(0), 19,
            Arrays.asList("New"), 50
        );
        assertEquals(ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("051+ New" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.NEW_ONLY, 30, 29, 60, 61);
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            new ArrayList<String>(0), 29,
            Arrays.asList("New1", "New2"), 60
        );
        assertEquals(ls + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("061+ New1" + ls + "062+ New2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 40, 40, 70, 70);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("041! Old" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("071! New" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 41, 42, 71, 71);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old1", "Old2"), 40,
            Arrays.asList("New"), 70
        );
        assertEquals("041! Old1" + ls + "042! Old2" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("071! New" + ls + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 43, 43, 72, 73);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old"), 43,
            Arrays.asList("New1", "New2"), 72
        );
        assertEquals("044! Old" + ls + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("073! New1" + ls + "074! New2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        
        w.newChange(ChangeType.CHANGED, 44, 45, 74, 75);        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        w.append(
            Arrays.asList("Old1", "Old2"), 44,
            Arrays.asList("New1", "New2"), 74
        );
        assertEquals("045! Old1" + ls + "046! Old2" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("075! New1" + ls + "076! New2" + ls,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        w.newChange(ChangeType.CHANGED, 44, 45, 74, 75);        
        
        startIndex1 = doc1.getLength();
        startIndex2 = doc2.getLength();
        
        w.newChange(ChangeType.WARNING, 46, 46, 76, 76);        
        w.append(
            Arrays.asList("WarnOld"), 46,
            Arrays.asList("WarnNew"), 76
        );
        assertEquals("047  WarnOld" + ls,
            doc1.getText(startIndex1, doc1.getLength() - startIndex1) );
        assertEquals("077  WarnNew" + ls ,
            doc2.getText(startIndex2, doc2.getLength() - startIndex2) );
        

        
    }

}
