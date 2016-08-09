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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import junit.extensions.abbot.ComponentTestFixture;

import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.CompareController.NodeRole;
import org.wtdiff.util.ui.NoWrapTextPane;

import abbot.finder.Matcher;
import org.junit.After;

public class TestNoWrapTextPane  extends CommonComponentTestFixture {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testTextNoScrollBarNeeded() throws Exception {
        
        // If line of text fits width then no scroll bar needed.
        // This is trivial behaviour.
        
        NoWrapTextPane textPane = new NoWrapTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        Style defStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
        doc.insertString(0, "short sentence", defStyle);
        textPane.setEditable(false);
        
        JScrollPane scrollPane = 
            new JScrollPane(
                textPane, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            );
        
        Frame frame = showFrame(
            scrollPane, 
            new Dimension( textPane.getPreferredSize().width + 100, textPane.getPreferredSize().height + 100 )
         );
        JScrollBar sb =this.findHorizontalJScrollbar(scrollPane);
        assertFalse(sb.isVisible());
        
        frame.remove(scrollPane);
    }
    
    @Test
    public void testTextScrollBarNeeded() throws Exception {
        
        // If line of text exceeds width then scroll bar needed.
        // This is key.  A normal text pane would prefer to wrap the text.
        
        NoWrapTextPane textPane = new NoWrapTextPane();
        StyledDocument doc = textPane.getStyledDocument();
        Style defStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
        doc.insertString(0, "the quick brown fox jumped over the lazy dog ", defStyle);
        textPane.setEditable(false);
        
        JScrollPane scrollPane = 
            new JScrollPane(
                textPane, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
            );
        Frame frame = showFrame(
            scrollPane, 
            new Dimension( textPane.getPreferredSize().width - 100, textPane.getPreferredSize().height + 100 ) 
        );
        JScrollBar sb =this.findHorizontalJScrollbar(scrollPane);
        assertTrue(sb.isVisible());
        
        frame.remove(scrollPane);
    }

}
