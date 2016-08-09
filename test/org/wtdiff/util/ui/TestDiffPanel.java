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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.text.NonprintingCharStyle;
import org.wtdiff.util.text.TextUtil;
import org.wtdiff.util.text.TextUtil.LineSeparator;
import org.wtdiff.util.ui.DiffNavigator;
import org.wtdiff.util.ui.DiffPanel;
import org.wtdiff.util.ui.DiffNavigator.DiffNavigationEvent;
import org.wtdiff.util.ui.DiffPanel.DiffType;

import abbot.finder.Matcher;


public class TestDiffPanel  extends CommonComponentTestFixture {

    private class ExceptionHolder {
        Exception exception = null;
        public synchronized void storeExcpetion(Exception e) {
            exception = e;
        }
        public synchronized void throwExceptionIfPresent() throws Exception {
            if (exception != null) 
                throw new Exception(exception);
        }
    }
    
    FileInputStreamSource tfileSource;
    FileInputStreamSource tfilePlusSource;
    private FileInputStreamSource longSource1;
    private FileInputStreamSource longSource2;

    @Before
    public void setUp() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File tfile = helper.createTestFile("tfile", "tfile\tcontent\n");
        tfileSource = new FileInputStreamSource(tfile);
        File tfilePlus = helper.createTestFile("tfilePlus", "tfile\tcontent\n\tplus\n");
        tfilePlusSource = new FileInputStreamSource(tfilePlus);
        StringBuilder sb1 = new StringBuilder("1.1");
        StringBuilder sb2 = new StringBuilder("2.1");
        for(int i = 0; i < 100; i++) {
            sb1.append(System.getProperty("line.separator"));
            sb2.append(System.getProperty("line.separator"));
        }
        sb1.append("1.2");
        sb2.append("2.2");
        sb1.append(System.getProperty("line.separator"));
        sb2.append(System.getProperty("line.separator"));
        File longFile1 = helper.createTestFile("longFile1", sb1.toString() );
        File longFile2 = helper.createTestFile("longFile2", sb2.toString() );
        longSource1 = new FileInputStreamSource(longFile1);
        longSource2 = new FileInputStreamSource(longFile2);
    }

    private boolean isContainerEmpty(Container c) {
        return c.getComponentCount() == 0;        
    }
    
    private List<JTextPane> getJTextPanes(Container container) {
      ArrayList<JTextPane> list = new ArrayList<>();
      return getJTextPanes( container, list);
  }
    private List<JTextPane> getJTextPanes(Container container, List<JTextPane> list) {
      for( Component comp: container.getComponents() ) {
          if ( comp instanceof JTextPane)
              list.add( (JTextPane)comp );
          else if ( comp instanceof Container) 
              getJTextPanes( (Container)comp, list);
      }
      return list;
  }

    private List<String> getLines(JTextPane pane) throws IOException {
        String text = pane.getText();
        Document doc = pane.getDocument();
		String docText = null;
        try {
			docText = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        text = docText; // TODO WIP
        StringReader sr = new StringReader(text);
        LineSeparator lineSep = TextUtil.guessLineSeparator(sr);
        sr.close();
        
        sr = new StringReader(text);
        List<String> lines =TextUtil.readerToLines(sr, lineSep);
        sr.close();
        
        List<String> newLines = new ArrayList<>();        
        for( String line: lines ) {
            newLines.add(TextUtil.removeTrailingLineSeparator(line, lineSep));
        }
        return newLines;
    }

    private void runDisplayDiff(final  DiffPanel panel ) throws InvocationTargetException, InterruptedException { 
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                panel.displayDiff();
            }
        });
    }

    @Test
    public void testNoDiff() throws Exception {
        final DiffController controller = new DiffController();
        
        DiffPanel panel = new DiffPanel( controller );
        
        showFrame( panel );

        assertTrue( isContainerEmpty(panel) );
        final ExceptionHolder eHolder = new ExceptionHolder();
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                try {
                    controller.setOldSource(tfileSource);
                } catch (IOException e) {
                    eHolder.storeExcpetion(e);
                }
            }
            
        });
        eHolder.throwExceptionIfPresent();
        
        assertTrue( isContainerEmpty(panel) );

        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                try {
                    controller.setNewSource(tfilePlusSource);
                } catch (IOException e) {
                    eHolder.storeExcpetion(e);
                }
            }
        });
        eHolder.throwExceptionIfPresent();
        assertTrue( isContainerEmpty(panel) );

        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                try {
                    controller.diff();
                } catch (IOException e) {
                    eHolder.storeExcpetion(e);
                }
            }
        });
        eHolder.throwExceptionIfPresent();
        assertFalse( isContainerEmpty(panel) );
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                try {
                    controller.setNewSource(tfilePlusSource);
                } catch (IOException e) {
                    eHolder.storeExcpetion(e);
                }
            }
            
        });
        eHolder.throwExceptionIfPresent();
        assertTrue( isContainerEmpty(panel) );
    }
    
    @Test
    public void testNormalDiff() throws Exception {

        /*
         * Basic verification of normal diff display.
         * Should have single text pane and that text pane
         * should contain expected text.
         */
        DiffController controller = new DiffController();
        controller.setOldSource(tfileSource);
        controller.setNewSource(tfilePlusSource);
        controller.diff();        
        
        DiffPanel panel = new DiffPanel( controller );
        panel.setDiffStyle(DiffPanel.DiffType.NORMAL);
        assertEquals(DiffPanel.DiffType.NORMAL,  panel.getDiffStyle());
        
        panel.setNumberLines(false);
        panel.setTabWidth(0);
        panel.setNonprintingStyle(NonprintingCharStyle.ASIS);
        
        showFrame( panel );
        runDisplayDiff(panel);

        assertFalse( isContainerEmpty(panel) );
        List<JTextPane> textPaneList = getJTextPanes(panel);
        assertEquals(1, textPaneList.size());
        
        List<String> lines = getLines(textPaneList.get(0));
        
        assertEquals("1a2", lines.get(0));
        assertEquals("> \tplus", lines.get(1));
    }
    
    @Test
    public void testSideBySideDiff() throws Exception {
        
        /*
         * Basic verification of side by side diff display.
         * Should have two texts pane and those text pane
         * should contain expected text.
         */

        DiffController controller = new DiffController();
        controller.setOldSource(tfileSource);
        controller.setNewSource(tfilePlusSource);
        controller.diff();        
        
        DiffPanel panel = new DiffPanel( controller );
        panel.setDiffStyle(DiffType.SIDE_BY_SIDE);
        assertEquals(DiffPanel.DiffType.SIDE_BY_SIDE,  panel.getDiffStyle());

        panel.setNumberLines(false);
        panel.setTabWidth(0);
        panel.setNonprintingStyle(NonprintingCharStyle.ASIS);
        
        
        showFrame( panel );
        runDisplayDiff(panel);

        assertFalse( isContainerEmpty(panel) );
        List<JTextPane> textPaneList = getJTextPanes(panel);
        
        assertEquals(2, textPaneList.size());
        JTextPane leftPane = textPaneList.get(0);
        JTextPane rightPane = textPaneList.get(1);
        if ( leftPane.getLocation().x > rightPane.getLocation().x ) {
            JTextPane temp = leftPane;
            leftPane = rightPane;
            rightPane = temp;
        }
        List<String> leftLines = getLines(leftPane);
        List<String> rightLines = getLines(rightPane);
        
        assertEquals( 2 , leftLines.size());
        assertEquals("  tfile\tcontent", leftLines.get(0));
        assertEquals("", leftLines.get(1));
        assertEquals( 2 , rightLines.size());
        assertEquals("  tfile\tcontent", rightLines.get(0));
        assertEquals("+ \tplus", rightLines.get(1));
    }
    
    @Test
    public void testNonPrinting() throws Exception {
        /*
         * Test that output escapes/boxes control characters.
         * Only test for normal diff (perhaps should test for
         * side by side as well).  Note that ASIS case is already 
         * tested in basic test*Diff tests 
         */
        DiffController controller = new DiffController();
        controller.setOldSource(tfileSource);
        controller.setNewSource(tfilePlusSource);
        controller.diff();        
        
        DiffPanel panel = new DiffPanel( controller );
        panel.setDiffStyle(DiffPanel.DiffType.NORMAL);
        panel.setNumberLines(false);
        panel.setTabWidth(0);
        panel.setNonprintingStyle(NonprintingCharStyle.ESCAPE);
        assertEquals(NonprintingCharStyle.ESCAPE, panel.getNonprintingStyle());
        showFrame( panel );
        runDisplayDiff(panel);

        assertFalse( isContainerEmpty(panel) );
        List<JTextPane> textPaneList = getJTextPanes(panel);
        assertEquals(1, textPaneList.size());
        
        List<String> lines = getLines(textPaneList.get(0));
        assertEquals("1a2", lines.get(0));
        assertEquals("> \\tplus", lines.get(1));
        
        panel.setNonprintingStyle(NonprintingCharStyle.BOX);
        assertEquals(NonprintingCharStyle.BOX, panel.getNonprintingStyle());
        runDisplayDiff(panel);
        
        assertFalse( isContainerEmpty(panel) );
        textPaneList = getJTextPanes(panel);
        assertEquals(1, textPaneList.size());
        
        lines = getLines(textPaneList.get(0));
        assertEquals("1a2", lines.get(0));
        assertEquals("> \u25afplus", lines.get(1));
        
    }

    @Test
    public void testTabWith() throws Exception {
        /*
         * Verify that tab expansion setting affects output.
         * Only test for normal diff (perhaps should test for
         * side by side diff as well).  Note that no expansion case is already 
         * tested in basic test*Diff tests 
         */
        DiffController controller = new DiffController();
        controller.setOldSource(tfileSource);
        controller.setNewSource(tfilePlusSource);
        controller.diff();        
        
        DiffPanel panel = new DiffPanel( controller );
        panel.setDiffStyle(DiffPanel.DiffType.NORMAL);
        panel.setNumberLines(false);
        panel.setTabWidth(2);
        assertEquals(2, panel.getTabWidth());
        panel.setNonprintingStyle(NonprintingCharStyle.ASIS);
        
        showFrame( panel );
        runDisplayDiff(panel);

        assertFalse( isContainerEmpty(panel) );
        List<JTextPane> textPaneList = getJTextPanes(panel);
        assertEquals(1, textPaneList.size());
        
        List<String> lines = getLines(textPaneList.get(0));
        
        assertEquals("1a2", lines.get(0));
        assertEquals(">   plus", lines.get(1));
    }
    
    @Test
    public void testNumberLines() throws Exception {
        /*
         * Verify that number lines setting affects output.
         * Only test for side by side diff (perhaps should test for
         * normal diff as well).  Note that no number lines case is already 
         * tested in basic test*Diff tests 
         */

        DiffController controller = new DiffController();
        controller.setOldSource(tfileSource);
        controller.setNewSource(tfilePlusSource);
        controller.diff();        
        
        DiffPanel panel = new DiffPanel( controller );
        panel.setDiffStyle(DiffPanel.DiffType.SIDE_BY_SIDE);
        panel.setNumberLines(true);
        assertEquals(true, panel.isNumberLines());
        panel.setTabWidth(0);
        panel.setNonprintingStyle(NonprintingCharStyle.ASIS);
        
        showFrame( panel );
        runDisplayDiff(panel);

        assertFalse( isContainerEmpty(panel) );
        List<JTextPane> textPaneList = getJTextPanes(panel);
        
        assertEquals(2, textPaneList.size());
        JTextPane leftPane = textPaneList.get(0);
        JTextPane rightPane = textPaneList.get(1);
        if ( leftPane.getLocation().x > rightPane.getLocation().x ) {
            JTextPane temp = leftPane;
            leftPane = rightPane;
            rightPane = temp;
        }
        List<String> leftLines = getLines(leftPane);
        List<String> rightLines = getLines(rightPane);
        
        assertEquals( 2 , leftLines.size());
        assertEquals("1  tfile\tcontent", leftLines.get(0));
        assertEquals("", leftLines.get(1));
        assertEquals( 2 , rightLines.size());
        assertEquals("1  tfile\tcontent", rightLines.get(0));
        assertEquals("2+ \tplus", rightLines.get(1));
    }

    private void checkTextPanePositionVisible(JTextPane textPane, int textPos, boolean expected) throws BadLocationException {
        
        Rectangle rect = textPane.modelToView(textPos);
        Rectangle visible = textPane.getVisibleRect();
        assertEquals(expected, visible.contains(rect.x, rect.y));

    }
    
    @Test
    public void testDiffNavigationNormal() throws Exception {

        final DiffController controller = new DiffController();
        controller.setOldSource(longSource1);
        controller.setNewSource(longSource2);
        
        DiffNavigator nav = new DiffNavigator();
        final DiffPanel panel = new DiffPanel( controller );
        panel.setDiffNavigator(nav);
        panel.setDiffStyle(DiffPanel.DiffType.CONTEXT);
        panel.setNumberLines(false);
        panel.setTabWidth(0);
        panel.setNonprintingStyle(NonprintingCharStyle.ASIS);
        
        JPanel parent = new JPanel();
        parent.setLayout(new GridLayout(1,2));        
        parent.add(nav);
        parent.add(panel);

        showFrame( parent, new Dimension(500, 200) );

        assertEquals(DiffPanel.DiffType.CONTEXT,  panel.getDiffStyle());
        
        JLabel navLabel =  (JLabel) getFinder().find(new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof JLabel;
            }
        });
        
        assertTrue( isContainerEmpty(panel) );
        assertEquals("0/0", navLabel.getText());
        final ExceptionHolder eHolder = new ExceptionHolder();
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                try {
                    controller.diff();
                } catch (IOException e) {
                    eHolder.storeExcpetion(e);
                }   
            }
            
        });
        eHolder.throwExceptionIfPresent();
        assertFalse( isContainerEmpty(panel) );
        assertEquals("1/2", navLabel.getText());

        List<JTextPane> textPaneList = getJTextPanes(panel);
        assertEquals(1, textPaneList.size());
        JTextPane textPane = textPaneList.get(0);
        checkTextPanePositionVisible(textPane, 0, true);
//        Rectangle rect = textPane.modelToView(0);
//        Rectangle visible = textPane.getVisibleRect();
//        assertTrue( visible.contains(rect.x, rect.y));
        
//        int last = textPaneList.get(0).getText().length() - 1;
        int last = textPaneList.get(0).getDocument().getLength() - 1;
        checkTextPanePositionVisible(textPane, last, false);
//        rect = textPane.modelToView(last);
//        assertFalse( visible.contains(rect.x, rect.y));
        
//        List<String> lines = getLines(textPaneList.get(0));
//        System.out.println(lines.size());
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                panel.diffNavigationEvent(DiffNavigationEvent.NEXT);
            }
            
        });
        assertEquals("2/2", navLabel.getText());
        checkTextPanePositionVisible(textPane, 0, false);
        checkTextPanePositionVisible(textPane, last, true);
//        visible = textPane.getVisibleRect();
//        rect = textPane.modelToView(0);
//        assertFalse( visible.contains(rect.x, rect.y));
//        rect = textPane.modelToView(last);
//        assertTrue( visible.contains(rect.x, rect.y));
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                panel.diffNavigationEvent(DiffNavigationEvent.NEXT);
            }
            
        });
        assertEquals("1/2", navLabel.getText());
        checkTextPanePositionVisible(textPane, 0, true);
        checkTextPanePositionVisible(textPane, last, false);
//        visible = textPane.getVisibleRect();
//        rect = textPane.modelToView(0);
//        assertFalse( visible.contains(rect.x, rect.y));
//        rect = textPane.modelToView(last);
//        assertTrue( visible.contains(rect.x, rect.y));

        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                panel.diffNavigationEvent(DiffNavigationEvent.PREV);
            }
            
        });
        assertEquals("2/2", navLabel.getText());
        checkTextPanePositionVisible(textPane, 0, false);
        checkTextPanePositionVisible(textPane, last, true);

    }
 
    @Test
    public void testDiffNavigationSideBySide() throws Exception {

        final DiffController controller = new DiffController();
        controller.setOldSource(longSource1);
        controller.setNewSource(longSource2);
        
        DiffNavigator nav = new DiffNavigator();
        final DiffPanel panel = new DiffPanel( controller );
        panel.setDiffNavigator(nav);
        panel.setDiffStyle(DiffPanel.DiffType.SIDE_BY_SIDE);
        panel.setNumberLines(false);
        panel.setTabWidth(0);
        panel.setNonprintingStyle(NonprintingCharStyle.ASIS);
        
        JPanel parent = new JPanel();
        parent.setLayout(new GridLayout(1,2));        
        parent.add(nav);
        parent.add(panel);

        showFrame( parent, new Dimension(500, 200) );

        assertEquals(DiffPanel.DiffType.SIDE_BY_SIDE,  panel.getDiffStyle());
        
        JLabel navLabel =  (JLabel) getFinder().find(new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof JLabel;
            }
        });
        
        assertTrue( isContainerEmpty(panel) );
        assertEquals("0/0", navLabel.getText());
        final ExceptionHolder eHolder = new ExceptionHolder();
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                try {
                    controller.diff();
                } catch (IOException e) {
                    eHolder.storeExcpetion(e);
                }   
            }
            
        });
        eHolder.throwExceptionIfPresent();
        assertFalse( isContainerEmpty(panel) );
        assertEquals("1/2", navLabel.getText());

        List<JTextPane> textPaneList = getJTextPanes(panel);
        assertEquals(2, textPaneList.size());
//        int last = textPaneList.get(0).getText().length() - 1; // note given test data both text panes should have the same amount of text
        int last = textPaneList.get(0).getDocument().getLength() - 1; // note given test data both text panes should have the same amount of text
        for( JTextPane textPane : textPaneList) {
            checkTextPanePositionVisible(textPane, 0, true);
            checkTextPanePositionVisible(textPane, last, false);
        }
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                panel.diffNavigationEvent(DiffNavigationEvent.NEXT);
            }
            
        });
        assertEquals("2/2", navLabel.getText());
        for( JTextPane textPane : textPaneList) {
            checkTextPanePositionVisible(textPane, 0, false);
            checkTextPanePositionVisible(textPane, last, true);
        }
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                panel.diffNavigationEvent(DiffNavigationEvent.NEXT);
            }
            
        });
        assertEquals("1/2", navLabel.getText());
        for( JTextPane textPane : textPaneList) {
            checkTextPanePositionVisible(textPane, 0, true);
            checkTextPanePositionVisible(textPane, last, false);
        }
    }

}
