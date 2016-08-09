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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ui.DocStylePreferences;

public class TestDocStylePreferences {

    private Preferences packagePref;
    private String simpleClassName = getClass().getSimpleName();
    @Before
    public void setUp() throws Exception {
        packagePref = Preferences.userNodeForPackage(getClass());
        clearTestPreferences();
    }

    @After
    public void tearDown() throws Exception {
    }

    private void clearTestPreferences() throws BackingStoreException {
        Preferences classPref = packagePref.node(simpleClassName);
        classPref.removeNode();
        packagePref.flush();
    }
    
    private void clearTestPreferencesVersion(String version) throws BackingStoreException {
        Preferences versioniedClassPref = packagePref.node(simpleClassName + "/" + version);
        versioniedClassPref.removeNode();
        packagePref.flush();
    }
    
    
    @Test
    public void testLoad() throws BackingStoreException {
        clearTestPreferences();
        assertFalse( packagePref.nodeExists(simpleClassName + "/0" ));
        new DocStylePreferences(getClass(), "0");
        assertTrue( packagePref.nodeExists(simpleClassName + "/0" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/0/test1" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/0/test1/sub1" ));
        Preferences sub01Pref = packagePref.node(simpleClassName + "/0/test1/sub1");
        assertTrue( sub01Pref.getBoolean(Font.MONOSPACED, false) );
        sub01Pref.putBoolean(Font.MONOSPACED, false);
        assertFalse( sub01Pref.getBoolean(Font.MONOSPACED, true) );
        // If preference exists then do not load from prefs file
        new DocStylePreferences(getClass(), "0");
        assertFalse( sub01Pref.getBoolean(Font.MONOSPACED, true) );
        
        // preferences are independent between versions
        new DocStylePreferences(getClass(), "1");
        assertTrue( packagePref.nodeExists(simpleClassName + "/0" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/0/test1" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/0/test1/sub1" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/1" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/1/test1" ));
        assertTrue( packagePref.nodeExists(simpleClassName + "/1/test1/sub1" ));
        sub01Pref = packagePref.node(simpleClassName + "/0/test1/sub1");
        assertFalse( sub01Pref.getBoolean(Font.MONOSPACED, true) );
        
        Preferences sub11Pref = packagePref.node(simpleClassName + "/1/test1/sub1");
        assertTrue( sub11Pref.getBoolean(Font.MONOSPACED, false) );
        
    }

    @Test
    public void testMonospaced() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "0");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(defStyle, Font.DIALOG);
        {
            Style sub1Style = sc.addStyle("sub1", defStyle);
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub1Style));
            stylePref.applyPreferences(sub1Style, "test1", "sub1");
            assertEquals( Font.MONOSPACED, StyleConstants.getFontFamily(sub1Style));
        }
        {
            Style sub2Style = sc.addStyle("sub2", defStyle);
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub2Style));
            stylePref.applyPreferences(sub2Style, "test1", "sub2");
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub2Style));
        }
        {
            Style nosubStyle = sc.addStyle("nosub", defStyle);
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(nosubStyle));
            stylePref.applyPreferences(nosubStyle, "test1", "nosub");
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(nosubStyle));
        }
    }

    @Test
    public void testFontStyle() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "0");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        
        StyleConstants.setBold(defStyle, false);
        StyleConstants.setItalic(defStyle, false);
        {
            Style sub1Style = sc.addStyle("sub1", defStyle);
            assertFalse( StyleConstants.isBold(sub1Style) );
            assertFalse( StyleConstants.isItalic(sub1Style) );
            stylePref.applyPreferences(sub1Style, "test1", "sub1");
            assertTrue( StyleConstants.isBold(sub1Style) );
            assertFalse( StyleConstants.isItalic(sub1Style) );
        }
        {
            Style sub2Style = sc.addStyle("sub2", defStyle);
            assertFalse( StyleConstants.isBold(sub2Style) );
            assertFalse( StyleConstants.isItalic(sub2Style) );
            stylePref.applyPreferences(sub2Style, "test1", "sub2");
            assertFalse( StyleConstants.isBold(sub2Style) );
            assertTrue( StyleConstants.isItalic(sub2Style) );
        }
        {
            Style sub3Style = sc.addStyle("sub3", defStyle);
            assertFalse( StyleConstants.isBold(sub3Style) );
            assertFalse( StyleConstants.isItalic(sub3Style) );
            stylePref.applyPreferences(sub3Style, "test1", "sub3");
            assertFalse( sub3Style.isDefined(StyleConstants.Bold) );
            assertFalse( sub3Style.isDefined(StyleConstants.Italic) );
        }
    }

    @Test
    public void testForeground() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "0");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setForeground(defStyle, Color.BLACK);
        
        {
            Style sub1Style = sc.addStyle("sub1", defStyle);
            assertEquals(Color.BLACK, StyleConstants.getForeground(sub1Style) );
            stylePref.applyPreferences(sub1Style, "test1", "sub1");
            assertNotEquals(Color.BLACK, StyleConstants.getForeground(sub1Style) );
            assertEquals(new Color(0x101010), StyleConstants.getForeground(sub1Style) );
        }
        {
            Style sub2Style = sc.addStyle("sub2", defStyle);
            assertEquals(Color.BLACK, StyleConstants.getForeground(sub2Style) );
            stylePref.applyPreferences(sub2Style, "test1", "sub2");
            assertNotEquals(Color.BLACK, StyleConstants.getForeground(sub2Style) );
            assertEquals(new Color(0x202020), StyleConstants.getForeground(sub2Style) );
        }
        {
            Style sub3Style = sc.addStyle("sub3", defStyle);
            assertEquals(Color.BLACK, StyleConstants.getForeground(sub3Style) );
            stylePref.applyPreferences(sub3Style, "test1", "sub3");
            assertEquals(Color.BLACK, StyleConstants.getForeground(sub3Style) );
            assertFalse(sub3Style.isDefined(StyleConstants.Foreground) );
        }
    }
    
    @Test
    public void testBackground() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "0");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setBackground(defStyle, Color.WHITE);
        
        {
            Style sub1Style = sc.addStyle("sub1", defStyle);
            assertEquals(Color.WHITE, StyleConstants.getBackground(sub1Style) );
            stylePref.applyPreferences(sub1Style, "test1", "sub1");
            assertNotEquals(Color.WHITE, StyleConstants.getBackground(sub1Style) );
            assertEquals(new Color(0x010101), StyleConstants.getBackground(sub1Style) );
        }
        {
            Style sub2Style = sc.addStyle("sub2", defStyle);
            assertEquals(Color.WHITE, StyleConstants.getBackground(sub2Style) );
            stylePref.applyPreferences(sub2Style, "test1", "sub2");
            assertNotEquals(Color.WHITE, StyleConstants.getBackground(sub2Style) );
            assertEquals(new Color(0x020202), StyleConstants.getBackground(sub2Style) );
        }
        {
            Style sub3Style = sc.addStyle("sub3", defStyle);
            assertEquals(Color.WHITE, StyleConstants.getBackground(sub3Style) );
            stylePref.applyPreferences(sub3Style, "test1", "sub3");
            assertEquals(Color.WHITE, StyleConstants.getBackground(sub3Style) );
            assertTrue(sub3Style.isDefined(StyleConstants.Background) );
        }
    }

    @Test
    public void testMissingVersion() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "9");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        
        StyleConstants.setFontFamily(defStyle, Font.DIALOG);
        StyleConstants.setBold(defStyle, true);
        StyleConstants.setItalic(defStyle, true);
        StyleConstants.setForeground(defStyle, Color.RED);
        StyleConstants.setBackground(defStyle, Color.BLUE);
        {
            Style sub1Style = sc.addStyle("sub1", defStyle);
            assertTrue( StyleConstants.isBold(sub1Style) );
            assertTrue( StyleConstants.isItalic(sub1Style) );
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub1Style));
            assertEquals(Color.RED, StyleConstants.getForeground(sub1Style) );
            assertEquals(Color.BLUE, StyleConstants.getBackground(sub1Style) );
            stylePref.applyPreferences(sub1Style, "test1", "sub1");
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub1Style));
            assertEquals(Color.RED, StyleConstants.getForeground(sub1Style) );
            assertEquals(Color.BLUE, StyleConstants.getBackground(sub1Style) );
            assertTrue(sub1Style.isDefined(StyleConstants.Background) );
        }
    }

    @Test
    public void testMissingStyleName() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "0");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        
        StyleConstants.setFontFamily(defStyle, Font.DIALOG);
        StyleConstants.setBold(defStyle, true);
        StyleConstants.setItalic(defStyle, true);
        StyleConstants.setForeground(defStyle, Color.RED);
        StyleConstants.setBackground(defStyle, Color.BLUE);
        {
            Style sub9Style = sc.addStyle("sub9", defStyle);
            assertTrue( StyleConstants.isBold(sub9Style) );
            assertTrue( StyleConstants.isItalic(sub9Style) );
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub9Style));
            assertEquals(Color.RED, StyleConstants.getForeground(sub9Style) );
            assertEquals(Color.BLUE, StyleConstants.getBackground(sub9Style) );
            stylePref.applyPreferences(sub9Style, "test1", "sub9");
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub9Style));
            assertEquals(Color.RED, StyleConstants.getForeground(sub9Style) );
            assertEquals(Color.BLUE, StyleConstants.getBackground(sub9Style) );
            assertTrue(sub9Style.isDefined(StyleConstants.Background) );
        }
    }

    @Test
    public void testEmptyValues() throws BackingStoreException {
        clearTestPreferences();
        DocStylePreferences stylePref= new DocStylePreferences(getClass(), "2");
        StyleContext sc = new StyleContext();
        Style defStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        
        StyleConstants.setFontFamily(defStyle, Font.DIALOG);
        StyleConstants.setBold(defStyle, true);
        StyleConstants.setItalic(defStyle, true);
        StyleConstants.setForeground(defStyle, Color.RED);
        StyleConstants.setBackground(defStyle, Color.BLUE);
        {
            Style sub1Style = sc.addStyle("sub1", defStyle);
            assertTrue( StyleConstants.isBold(sub1Style) );
            assertTrue( StyleConstants.isItalic(sub1Style) );
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub1Style));
            assertEquals(Color.RED, StyleConstants.getForeground(sub1Style) );
            assertEquals(Color.BLUE, StyleConstants.getBackground(sub1Style) );
            
            stylePref.applyPreferences(sub1Style, "test1", "sub1");
            
            assertEquals( Font.DIALOG, StyleConstants.getFontFamily(sub1Style));
            assertFalse( StyleConstants.isBold(sub1Style) );
            assertFalse( StyleConstants.isItalic(sub1Style) );
            assertEquals(Color.RED, StyleConstants.getForeground(sub1Style) );
            assertTrue(sub1Style.isDefined(StyleConstants.Foreground) );
            assertEquals(Color.BLUE, StyleConstants.getBackground(sub1Style) );
            assertTrue(sub1Style.isDefined(StyleConstants.Background) );
        }
    }

}
