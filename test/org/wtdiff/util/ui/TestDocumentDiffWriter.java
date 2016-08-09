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

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ui.DocumentDiffWriter;
import org.wtdiff.util.ui.DocumentDiffWriter.StyleSet;

public class TestDocumentDiffWriter {

    class TestDocDiffWriter extends DocumentDiffWriter {
        
        public void doInitStyleSets(StyledDocument doc) {
            initStyleSets(doc);
        }
        
        public StyleSet getStyleSet(String type) {
            switch (type) {
                case "CHANGED":
                    return changed;
                case "COMMON":
                    return common;
                case "NEW_ONLY":
                    return newOnly;
                case "OLD_ONLY":
                    return oldOnly;
                case "WARNING":
                    return warning;
                case "INFO":
                    return info;
                default:
                    throw new IllegalArgumentException("bad style type " + type);
                
            }
        }
        
        @Override
        public void append(List<String> oldLines, int oldBeginLineOffset,
            List<String> newLines, int newBeginLineOffset) {
            // do nothing
            
        }

        @Override
        public void newChange(ChangeType type, int oldBegin, int oldEnd,
            int newBegin, int newEnd) {
            // do nothing
            
        }
        
    }

    private void clearPreferences() throws BackingStoreException {
        Preferences packagePref = Preferences.userNodeForPackage(DocumentDiffWriter.class);
        if ( packagePref.nodeExists(DocumentDiffWriter.class.getSimpleName() ) ) {
            packagePref.node(DocumentDiffWriter.class.getSimpleName()).removeNode();
        }        
    }
    @Before
    public void setUp() throws Exception {
        clearPreferences();
        Preferences packagePref = Preferences.userNodeForPackage(DocumentDiffWriter.class);
        Preferences superPref = packagePref.node(DocumentDiffWriter.class.getSimpleName() + "/" + DocumentDiffWriter.STYLE_VERSION);
        for ( String name: new String[] {"info", "common", "oldonly", "newonly", "changed", "warning"} ) {
            Preferences textPref = superPref.node(name + "/text");
            Preferences indicatorPref = superPref.node( name + "/indicator");
            Preferences linenoPref = superPref.node(name + "/lineno");
            
            textPref.putInt("fgcolor", Color.WHITE.getRGB());
            textPref.putInt("bgcolor", Color.WHITE.getRGB());

            indicatorPref.putInt("fgcolor", Color.WHITE.getRGB());
            indicatorPref.putInt("bgcolor", Color.BLACK.getRGB());
            
            linenoPref.putInt("fgcolor", Color.BLACK.getRGB());
            linenoPref.putInt("bgcolor", Color.BLACK.getRGB());
            
            if ( "info".equals(name) ) {
                textPref.putBoolean(Font.MONOSPACED, true);
                textPref.putBoolean("bold", true);
                textPref.putBoolean("italic", true);
            }
            else if ( "common".equals(name) ) {
                textPref.putBoolean(Font.MONOSPACED, true);
                textPref.putBoolean("bold", true);
                textPref.putBoolean("italic", false);
            }
            else if ( "oldonly".equals(name) ) {
                textPref.putBoolean(Font.MONOSPACED, true);
                textPref.putBoolean("bold", false);
                textPref.putBoolean("italic", true);
            }
            else if ( "newonly".equals(name) ) {
                textPref.putBoolean(Font.MONOSPACED, true);
                textPref.putBoolean("bold", false);
                textPref.putBoolean("italic", false);
            }
            else if ( "changed".equals(name) ) {
                textPref.putBoolean(Font.MONOSPACED, false);
                textPref.putBoolean("bold", true);
                textPref.putBoolean("italic", true);
            }
            else if ( "warning".equals(name) ) {
                textPref.putBoolean(Font.MONOSPACED, false);
                textPref.putBoolean("bold", false);
                textPref.putBoolean("italic", true);
            }
            else {
                throw new Exception("TESTBUG unexpected name " + name);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        clearPreferences();
    }

    private void checkMonoBoldItalic( Style s, boolean isMonospaced, boolean isBold, boolean isItalic ) {
        if ( isMonospaced ) {
            assertTrue( s.isDefined(StyleConstants.FontFamily));
            assertEquals( Font.MONOSPACED, StyleConstants.getFontFamily(s));
        } else {
            assertNotEquals( Font.MONOSPACED, StyleConstants.getFontFamily(s));
        }
        
        if ( isBold ) {
            assertTrue( s.isDefined(StyleConstants.Bold));
        }
        assertEquals(isBold,  StyleConstants.isBold(s));
        
        if ( isItalic ) {
            assertTrue( s.isDefined(StyleConstants.Italic));
        }
        assertEquals( isItalic,  StyleConstants.isItalic(s));
        
    }
    @Test
    public void testStyleByType() {
        TestDocDiffWriter writer = new TestDocDiffWriter();
        DefaultStyledDocument doc = new DefaultStyledDocument(new StyleContext());
        Style defStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defStyle, Font.DIALOG);
        StyleConstants.setBold(defStyle, false);
        StyleConstants.setItalic(defStyle, false);
        writer.doInitStyleSets(doc);
        
        {
            StyleSet info = writer.getStyleSet("INFO");
            checkMonoBoldItalic( info.text, true, true, true);
            StyleSet common = writer.getStyleSet("COMMON");
            checkMonoBoldItalic( common.text, true, true, false);
            StyleSet oldOnly = writer.getStyleSet("OLD_ONLY");
            checkMonoBoldItalic( oldOnly.text, true, false, true);
            StyleSet newOnly = writer.getStyleSet("NEW_ONLY");
            checkMonoBoldItalic( newOnly.text, true, false, false);
            StyleSet changed = writer.getStyleSet("CHANGED");
            checkMonoBoldItalic( changed.text, false, true, true);
            StyleSet warning = writer.getStyleSet("WARNING");
            checkMonoBoldItalic( warning.text, false, false, true);
        }
        //fail("Not yet implemented");
    }

    @Test
    public void testStyleByRole() {
        TestDocDiffWriter writer = new TestDocDiffWriter();
        DefaultStyledDocument doc = new DefaultStyledDocument(new StyleContext());
        Style defStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defStyle, Font.DIALOG);
        StyleConstants.setBold(defStyle, false);
        StyleConstants.setItalic(defStyle, false);
        StyleConstants.setForeground(defStyle, Color.RED);
        StyleConstants.setBackground(defStyle, Color.GREEN);
        writer.doInitStyleSets(doc);
        
        {
            StyleSet info = writer.getStyleSet("INFO");
            Style text = info.text;
            assertEquals(Color.WHITE, StyleConstants.getForeground(text));
            assertEquals(Color.WHITE, StyleConstants.getBackground(text));

            Style indicator = info.indicator;
            assertEquals(Color.WHITE, StyleConstants.getForeground(indicator));
            assertEquals(Color.BLACK, StyleConstants.getBackground(indicator));

            Style lineno = info.lineno;
            assertEquals(Color.BLACK, StyleConstants.getForeground(lineno));
            assertEquals(Color.BLACK, StyleConstants.getBackground(lineno));

        }
    }

}
