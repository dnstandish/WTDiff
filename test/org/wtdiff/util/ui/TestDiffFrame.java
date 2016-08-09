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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.junit.Before;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.CompareController;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.DiffSource;
import org.wtdiff.util.text.ExceptionInputStreamSource;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.text.NonprintingCharStyle;
import org.wtdiff.util.text.TextUtil;
import org.wtdiff.util.text.DiffController.SourceType;
import org.wtdiff.util.text.TextUtil.LineSeparator;
import org.wtdiff.util.ui.DiffFrame;
import org.wtdiff.util.ui.DiffOpenDialog;
import org.wtdiff.util.ui.DiffPanel;
import org.wtdiff.util.ui.LimitedDigitsTextField;
import org.wtdiff.util.ui.DiffPanel.DiffType;

import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.tester.JButtonTester;

import junit.extensions.abbot.ComponentTestFixture;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Test;

public class TestDiffFrame  extends CommonComponentTestFixture {

    FileInputStreamSource tfileSourceCRLF;
    FileInputStreamSource tfilePlusSourceLF;
//    FileInputStreamSource tfile123Source;
//    FileInputStreamSource tfile12s3Source;
//    FileInputStreamSource tfile12ss3Source;
//    FileInputStreamSource tfile12ss3sSource;

    @Before
    public void setUp() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File tfile = helper.createTestFile("tfile", "tfile\tcontent\r\n" );
        tfileSourceCRLF = new FileInputStreamSource(tfile);
        File tfilePlus = helper.createTestFile("tfilePlus", "tfile\tcontent\n\tplus\n");
        tfilePlusSourceLF = new FileInputStreamSource(tfilePlus);

//        File tfile123 = helper.createTestFile("tfile123", "123" );
//        File tfile12s3 = helper.createTestFile("tfile12s3", "12 3" );
//        File tfile12ss3 = helper.createTestFile("tfile12ss3", "12  3" );
//        File tfile12ss3s = helper.createTestFile("tfile12ss3s", "12  3 " );
//
//        tfile123Source = new FileInputStreamSource(tfile123);
//        tfile12s3Source = new FileInputStreamSource(tfile12s3);
//        tfile12ss3Source = new FileInputStreamSource(tfile12ss3);
//        tfile12ss3sSource = new FileInputStreamSource(tfile12ss3s);

    }

    private final Object closeListenerLock(final DiffFrame f) {
        final Object lock = new Object();
        f.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) {
                    synchronized (lock) {
                        f.setVisible(false);
                        lock.notify();
                    }
                }
           }
        );
        return lock;
    }
    
    private void checkClosed(final  DiffFrame f, final Object lock) {
        synchronized(lock) {
            try {
                lock.wait(1000);
                if (f.isVisible()) {
                    fail("Frame not closed after exit menu item clicked");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private void frameDispose(final DiffFrame f) {
        invokeAndWait( new Runnable() {
            public void run() {
                f.dispose();
            }
        });
    }
    
    @Test
    public void testMenuStandalone() throws Exception {
        final DiffFrame appFrame = new DiffFrame( );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
    
        final Object lock  = closeListenerLock(appFrame);
        appFrame.setVisible(true);

        JMenuBar menuBar = (JMenuBar)getFinder().find( appFrame, new JMenuBarMatcher() );
        JMenu helpMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("Help") );
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(helpMenu);
        JMenuItem aboutItem  = (JMenuItem)getFinder().find( menuBar, new JMenuItemMatcher("About") );
        bTester.actionClick(aboutItem);
        JDialog aboutDialog = (JDialog)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JDialog;
            }
        });
        this.clickOK(aboutDialog);
        
        JMenu fileMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("File") );
        bTester = new JButtonTester(); 
        bTester.actionClick(fileMenu);
        try {
            getFinder().find( menuBar, new JMenuItemMatcher("Close") );
            fail("Close menu item should not exist when stand alone");
        } catch (ComponentNotFoundException cne){
            // this should happen
        }

        JMenuItem openItem = (JMenuItem) getFinder().find( menuBar, new JMenuItemMatcher("Open...") );
        bTester.actionClick(openItem);
        JDialog openDialog = (JDialog)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof DiffOpenDialog;
            }
        });
        clickCancel(openDialog);
        
        bTester.actionClick(fileMenu);
        JMenuItem exitItem = (JMenuItem)getFinder().find( menuBar, new JMenuItemMatcher("Exit") );
        bTester.actionClick(exitItem);
        checkClosed(appFrame, lock);
//        appFrame.dispose();
        frameDispose(appFrame);
    }

    @Test
    public void testMenuNotStandalone() throws Exception {
        DiffController controller = new DiffController();
        final DiffFrame appFrame = new DiffFrame( false, controller );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
        final Object lock  = closeListenerLock(appFrame);

        appFrame.setVisible(true);
        
        JMenuBar menuBar = (JMenuBar)getFinder().find( appFrame, new JMenuBarMatcher() );
        JMenu fileMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("File") );
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(fileMenu);
        //Thread.sleep(20000);
        try {
            getFinder().find( menuBar, new JMenuItemMatcher("Open...") );
            fail("Open... menu item should not exist when stand alone");
        } catch (ComponentNotFoundException cne){
            // this should happen
        }
        try {
            getFinder().find( menuBar, new JMenuItemMatcher("Exit") );
            fail("Exit menu item should not exist when stand alone");
        } catch (ComponentNotFoundException cne){
            // this should happen
        }
        JMenuItem closeItem = (JMenuItem)getFinder().find( menuBar, new JMenuItemMatcher("Close") );
        bTester.actionClick(closeItem);
        checkClosed(appFrame, lock);
//        appFrame.dispose();
        frameDispose(appFrame);
    }

    private boolean hasItemOfType(JComboBox combo, Class type) {
        for( int i = 0 ; i < combo.getItemCount(); i++) {
            Object item =  combo.getItemAt(i);
            if ( item != null && type.isInstance(item) ) {
                return true;
            }
//            System.out.println(combo.getItemAt(i));
        }
        return false;
    }

    private boolean hasItemString(JComboBox combo, String s) {
        for( int i = 0 ; i < combo.getItemCount(); i++) {
            Object item =  combo.getItemAt(i);
            if ( item instanceof String &&  s.equals(item) ) {
                return true;
            }
//            System.out.println(combo.getItemAt(i));
        }
        return false;
    }

    private JComboBox<LineSeparator> getLineSepComboBox(Container container) throws Exception {
        return (JComboBox<LineSeparator>)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof JComboBox &&
                    hasItemOfType((JComboBox)c, LineSeparator.class);
            }
        });
    }
    
    private JComboBox<Charset> getCharsetComboBox(Container container) throws Exception {
        return (JComboBox<Charset>)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof JComboBox &&
                    hasItemOfType((JComboBox)c, Charset.class);
            }
        });
    }

    private JComboBox<String> getWhitespaceComboBox(Container container) throws Exception {
        return (JComboBox<String>)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof JComboBox &&
                    hasItemString((JComboBox)c, "trim");
            }
        });
    }
    

    private void checkSourceAgainstController(DiffController controller) throws Exception {
        DiffFrame appFrame = new DiffFrame( false, controller );
        appFrame.setVisible(true);
        
        JLabelAndTextMatcher labelMatcher = new JLabelAndTextMatcher();
        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find( appFrame, labelMatcher ); 
        JTextField oldNameField = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher );
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find( appFrame, labelMatcher ); 
        JTextField newNameField = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher );
        JComboBox<LineSeparator> oldLineSepBox = getLineSepComboBox(oldLabel.getParent());
        JComboBox<LineSeparator> newLineSepBox = getLineSepComboBox(newLabel.getParent());
        JComboBox<Charset> oldCharset = getCharsetComboBox(oldLabel.getParent());
        JComboBox<Charset> newCharset = getCharsetComboBox(newLabel.getParent());

        assertEquals( controller.getOldSourceName(), oldNameField.getText() );
        assertEquals( controller.getNewSourceName(), newNameField.getText() );
        assertEquals( controller.getEncoding(SourceType.OLD), oldCharset.getSelectedItem() );
        assertEquals( controller.getEncoding(SourceType.NEW), newCharset.getSelectedItem() );
        assertEquals( controller.getLineSep(SourceType.OLD), oldLineSepBox.getSelectedItem() );
        assertEquals( controller.getLineSep(SourceType.NEW), newLineSepBox.getSelectedItem() );
        
//        appFrame.dispose();
        frameDispose(appFrame);
    }
    @Test
    public void testSourceInit() throws Exception {
        DiffController controller = new DiffController();
        controller.setOldSource(tfileSourceCRLF);
        controller.setNewSource(tfilePlusSourceLF);
//        LineSeparator controllerOldLineSep = controller.getLineSep(SourceType.OLD);
//        LineSeparator controllerNewLineSep = controller.getLineSep(SourceType.NEW);        
        Charset controllerOldCharset = controller.getEncoding(SourceType.OLD);
        Charset controllerNewCharset = controller.getEncoding(SourceType.NEW);
        checkSourceAgainstController(controller);
        
        Charset defCharset = Charset.defaultCharset();
        Charset otherCharset = Charset.forName("UTF-16"); //TODO assumes default not UTF-16

        if ( ! otherCharset.equals(controllerOldCharset) ) {
            controller.forceEncoding(SourceType.OLD, otherCharset);
            controller.forceEncoding(SourceType.NEW, defCharset);
            checkSourceAgainstController(controller);
        }
        
        if ( ! otherCharset.equals(controllerNewCharset) ) {
            controller.forceEncoding(SourceType.OLD, defCharset);
            controller.forceEncoding(SourceType.NEW, otherCharset);
            checkSourceAgainstController(controller);
        }
    }
    
    @Test
    public void testSource() throws Exception {
        DiffController controller = new DiffController();
        DiffFrame appFrame = new DiffFrame( false, controller );
        
        appFrame.setVisible(true);
        
        JLabelAndTextMatcher labelMatcher = new JLabelAndTextMatcher();
        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find( appFrame, labelMatcher ); 
        JTextField oldNameField = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher );
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find( appFrame, labelMatcher ); 
        JTextField newNameField = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher );
        JComboBox<LineSeparator> oldLineSepBox = getLineSepComboBox(oldLabel.getParent());
        JComboBox<LineSeparator> newLineSepBox = getLineSepComboBox(newLabel.getParent());
        JComboBox<Charset> oldCharset = getCharsetComboBox(oldLabel.getParent());
        JComboBox<Charset> newCharset = getCharsetComboBox(newLabel.getParent());
        
        Charset defCharset = Charset.defaultCharset();
        Charset otherCharset = Charset.forName("UTF-16"); //TODO assumes default not UTF-16

        assertEquals("", oldNameField.getText());
        assertFalse(oldNameField.isEditable());
        assertEquals("", newNameField.getText());
        assertFalse(newNameField.isEditable());
        assertEquals( null, oldLineSepBox.getSelectedItem());
        assertEquals( null, newLineSepBox.getSelectedItem());
        assertEquals( Charset.defaultCharset(), oldCharset.getSelectedItem());
        assertEquals( Charset.defaultCharset(), newCharset.getSelectedItem());
        oldCharset.setSelectedItem(defCharset);
        newCharset.setSelectedItem(otherCharset);
        assertEquals( defCharset, oldCharset.getSelectedItem());
        assertEquals( otherCharset, newCharset.getSelectedItem());
        oldCharset.setSelectedItem(otherCharset);
        newCharset.setSelectedItem(defCharset);
        assertEquals( otherCharset, oldCharset.getSelectedItem());
        assertEquals( defCharset, newCharset.getSelectedItem());

        newCharset.setSelectedItem(otherCharset);
        controller.setOldSource(tfileSourceCRLF);
        assertEquals(tfileSourceCRLF.getName(), oldNameField.getText());        
        assertEquals("", newNameField.getText());
        System.out.println( oldLineSepBox.getItemAt(oldLineSepBox.getSelectedIndex()) );
        //Thread.sleep(100000);
        assertEquals( LineSeparator.CRLF, oldLineSepBox.getSelectedItem());
        assertEquals( null, newLineSepBox.getSelectedItem());
        assertEquals( defCharset, oldCharset.getSelectedItem());
        assertEquals( defCharset, controller.getEncoding(SourceType.OLD));
        assertEquals( otherCharset, newCharset.getSelectedItem());
        
        
        oldCharset.setSelectedItem(otherCharset);
        assertEquals( otherCharset, controller.getEncoding(SourceType.OLD));
        controller.getLineSep(SourceType.OLD);
        newCharset.setSelectedItem(otherCharset);
        controller.setNewSource(tfilePlusSourceLF);
        assertEquals(tfileSourceCRLF.getName(), oldNameField.getText());        
        assertEquals(tfilePlusSourceLF.getName(), newNameField.getText());
        assertEquals( LineSeparator.CRLF, oldLineSepBox.getSelectedItem());
        assertEquals( LineSeparator.LF, newLineSepBox.getSelectedItem());
        assertEquals( otherCharset, oldCharset.getSelectedItem());
        assertEquals( defCharset, newCharset.getSelectedItem());
        assertEquals( otherCharset, controller.getEncoding(SourceType.OLD));
        assertEquals( defCharset, controller.getEncoding(SourceType.NEW));

        oldCharset.setSelectedItem(defCharset);
        newCharset.setSelectedItem(otherCharset);
        assertEquals( defCharset, controller.getEncoding(SourceType.OLD));
        assertEquals( otherCharset, controller.getEncoding(SourceType.NEW));
        
        ContainerDumper.dump("", appFrame);
        
//        appFrame.dispose();
        frameDispose(appFrame);
    }

    private boolean isControllerWhitespaceSetting(DiffController c, String s) {
        switch (s) {
        case "":
            return ! c.isCompactWhiteSpace() && ! c.isIgnoreWhiteSpace() && ! c.isTrimWhiteSpace();
        case "trim":
            return ! c.isCompactWhiteSpace() && ! c.isIgnoreWhiteSpace() && c.isTrimWhiteSpace();
        case "normalize":
            return c.isCompactWhiteSpace() && ! c.isIgnoreWhiteSpace() && ! c.isTrimWhiteSpace();
        case "normalize trim":
            return c.isCompactWhiteSpace() && ! c.isIgnoreWhiteSpace() && c.isTrimWhiteSpace();
        case "ignore":
            return ! c.isCompactWhiteSpace() && c.isIgnoreWhiteSpace() && ! c.isTrimWhiteSpace();
        default:
            return false;
        }
    }
    
    private void wsConsistentcyCheck(DiffController c) throws Exception {

        DiffFrame appFrame = new DiffFrame( false, c );
        
        appFrame.setVisible(true);
    
        JComboBox<String>  wsCombo = getWhitespaceComboBox(appFrame);

        assertTrue( isControllerWhitespaceSetting(c, (String)wsCombo.getSelectedItem()) );
        
//        appFrame.dispose();
        frameDispose(appFrame);
    }
    @Test
    public void testWhiteSpaceInit() throws Exception {
        DiffController controller = new DiffController();
        controller.setIgnoreWhiteSpace(false);
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);
        
        wsConsistentcyCheck(controller);

        controller.setIgnoreWhiteSpace(true);
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);

        wsConsistentcyCheck(controller);

        controller.setIgnoreWhiteSpace(false);
        controller.setCompactWhiteSpace(true);
        controller.setTrimWhiteSpace(false);

        wsConsistentcyCheck(controller);
        
        controller.setIgnoreWhiteSpace(false);
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(true);
        
        wsConsistentcyCheck(controller);
        System.gc();
        controller.setIgnoreWhiteSpace(false);
        controller.setCompactWhiteSpace(true);
//        Thread.sleep(20000);
        controller.setTrimWhiteSpace(true);

        wsConsistentcyCheck(controller);
    }

    @Test
    public void testWhiteSpaceControls() throws Exception {
        DiffController controller = new DiffController();
        controller.setIgnoreWhiteSpace(false);
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);

        DiffFrame appFrame = new DiffFrame( false, controller );
        
        appFrame.setVisible(true);
    
        JComboBox<String>  wsCombo = getWhitespaceComboBox(appFrame);

        assertEquals( "" , wsCombo.getSelectedItem() );
        
        wsCombo.setSelectedItem("trim");
        assertFalse(controller.isIgnoreWhiteSpace());
        assertFalse(controller.isCompactWhiteSpace());
        assertTrue(controller.isTrimWhiteSpace());
        
        wsCombo.setSelectedItem("normalize");
        assertFalse(controller.isIgnoreWhiteSpace());
        assertTrue(controller.isCompactWhiteSpace());
        assertFalse(controller.isTrimWhiteSpace());

        wsCombo.setSelectedItem("normalize trim");
        assertFalse(controller.isIgnoreWhiteSpace());
        assertTrue(controller.isCompactWhiteSpace());
        assertTrue(controller.isTrimWhiteSpace());

        wsCombo.setSelectedItem("ignore");
        assertTrue(controller.isIgnoreWhiteSpace());
        assertFalse(controller.isCompactWhiteSpace());
        assertFalse(controller.isTrimWhiteSpace());

        wsCombo.setSelectedItem("");
        assertFalse(controller.isIgnoreWhiteSpace());
        assertFalse(controller.isCompactWhiteSpace());
        assertFalse(controller.isTrimWhiteSpace());

//        appFrame.dispose();
        frameDispose(appFrame);
    }

    @Test
    public void testDiff() throws Exception {
        DiffController controller = new DiffController();
        
        DiffFrame appFrame = new DiffFrame( false, controller );

        appFrame.setVisible(true);

        controller.setOldSource(tfileSourceCRLF);
        controller.setNewSource(tfilePlusSourceLF);

        assertFalse(controller.haveDiff());
        
        JButton diffButton = (JButton)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JButton &&
                    "Diff".equals(((JButton)comp).getText());
            } 
        });
        
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(diffButton);

        assertTrue(controller.haveDiff());

//        appFrame.dispose();
        frameDispose(appFrame);
    }        

    @Test
    public void testShow() throws Exception {
        DiffController controller = new DiffController();
        
        DiffFrame appFrame = new DiffFrame( false, controller );

        appFrame.setVisible(true);

        controller.setOldSource(tfileSourceCRLF);
        controller.setNewSource(tfilePlusSourceLF);

        assertFalse(controller.haveDiff());

        JButtonTester bTester = new JButtonTester();         
        
        // clicking on show button does not trigger diff
        JButton showButton = (JButton)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JButton &&
                    "Show".equals(((JButton)comp).getText());
            } 
        });
        bTester.actionClick(showButton);
        assertFalse(controller.haveDiff());

        JButton diffButton = (JButton)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JButton &&
                    "Diff".equals(((JButton)comp).getText());
            } 
        });
        bTester.actionClick(diffButton);
        assertTrue(controller.haveDiff());

        // get the diff panel so we can get at the text

        DiffPanel panel = (DiffPanel)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof DiffPanel;
            } 
        });
  
        // set diff output to a known state, not showing line numbers
        panel.setNumberLines(false);
        panel.setDiffStyle(DiffType.NORMAL);
        bTester.actionClick(showButton);

        JTextPane paneOrig = (JTextPane)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JTextPane;
            } 
        });
        
        // get the diff text in this state 
        String nonumberTest = paneOrig.getText();
        
        // simply changing the show line numbers setting does not change the diff output
        panel.setNumberLines(true);
        JTextPane paneNew1 = (JTextPane)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JTextPane;
            } 
        });
        String numberTestNoShow = paneNew1.getText();
        assertEquals(nonumberTest, numberTestNoShow);
        
        // clicking show changes the diff output, in this case now showing line numbers
        bTester.actionClick(showButton);
        
        JTextPane paneNew2 = (JTextPane)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JTextPane;
            } 
        });
        String numberTestShow = paneNew2.getText();
        assertNotEquals(nonumberTest, numberTestShow);
        
//        appFrame.dispose();
        frameDispose(appFrame);
    }        

    private JComboBox<NonprintingCharStyle> getNonPrintingComboBox(Container container) throws Exception {
        return (JComboBox<NonprintingCharStyle>)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof JComboBox &&
                    hasItemString((JComboBox)c, "escape");
            }
        });
    }

    private JComboBox<String> getDiffStyleComboBox(Container container) throws Exception {
        return (JComboBox<String>)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof JComboBox &&
                    hasItemString((JComboBox)c, "side by side");
            }
        });
    }

    private LimitedDigitsTextField getTabWidthTextField(Container container) throws Exception {
        return (LimitedDigitsTextField)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof LimitedDigitsTextField;
            }
        });
    }
    
    private JCheckBox getNumberlinesCheckBox(Container container) throws Exception {
        return (JCheckBox)getFinder().find( container, new Matcher () {
            public boolean matches(Component c) {
                return c instanceof JCheckBox &&
                    "number lines".equals(((JCheckBox)c).getText());
            }
        });
    }


    @Test
    public void testDisplaySetting() throws Exception {
        DiffController controller = new DiffController();
        
        DiffFrame appFrame = new DiffFrame( false, controller );

        appFrame.setVisible(true);

        
        DiffPanel panel = (DiffPanel)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof DiffPanel;
            } 
        });
        
        JComboBox<NonprintingCharStyle> npscCombo = getNonPrintingComboBox(appFrame);
        assertEquals(panel.getNonprintingStyle().localizedString(), npscCombo.getSelectedItem());
        npscCombo.setSelectedItem(NonprintingCharStyle.BOX.localizedString());
        assertEquals(NonprintingCharStyle.BOX, panel.getNonprintingStyle());
        npscCombo.setSelectedItem(NonprintingCharStyle.ESCAPE.localizedString());
        assertEquals(NonprintingCharStyle.ESCAPE, panel.getNonprintingStyle());
        
        JCheckBox numberLinesCheckBox = getNumberlinesCheckBox(appFrame);
        assertEquals(panel.isNumberLines(), numberLinesCheckBox.isSelected());
        numberLinesCheckBox.setSelected(true);
        numberLinesCheckBox.doClick();
        assertFalse(panel.isNumberLines());
        numberLinesCheckBox.doClick();
        assertTrue(panel.isNumberLines());
        
        LimitedDigitsTextField tabWidthField = getTabWidthTextField(appFrame);
        assertEquals(panel.getTabWidth(), tabWidthField.getValue());
        tabWidthField.setText("13");
        
        
        JButton diffButton = (JButton)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JButton &&
                    "Diff".equals(((JButton)comp).getText());
            } 
        });
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(diffButton);

        assertEquals(13, panel.getTabWidth());
        
        JComboBox<String> diffTypeCombo = getDiffStyleComboBox(appFrame);
        assertEquals(panel.getDiffStyle().localizedString(), diffTypeCombo.getSelectedItem());
        diffTypeCombo.setSelectedItem("side by side");
        assertEquals(DiffPanel.DiffType.SIDE_BY_SIDE, panel.getDiffStyle());
        diffTypeCombo.setSelectedItem("normal");
        assertEquals(DiffPanel.DiffType.NORMAL, panel.getDiffStyle());
        
//        appFrame.dispose();
        frameDispose(appFrame);
    }        

    @Test
    public void testIOException() throws Exception
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < 10000; i++) {
            sb.append("\r\n");
        }
        Date now = new Date();
        ExceptionInputStreamSource source = new ExceptionInputStreamSource("readErrror1000", now.getTime(), sb.toString(), 9000);
//        DiffSource ds = new DiffSource(source);
//        ds.getLineSep();
//        try {            
//            ds.getLines();                
//            fail("IOexception not thrown");
//        } catch (IOException ioe) {
//            // this is supposed to happen
//        }
        DiffController controller = new DiffController();
        
        DiffFrame appFrame = new DiffFrame( false, controller );

        appFrame.setVisible(true);

        controller.setOldSource(source);
        controller.setNewSource(source);
        
        JButton diffButton = (JButton)getFinder().find(appFrame, new Matcher() {
            public boolean matches(Component comp) {
                return comp instanceof JButton &&
                    "Diff".equals(((JButton)comp).getText());
            } 
        });
//        Thread.sleep(10000);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(diffButton);

        JDialog dialog = (JDialog)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JDialog
                    && true;
            }
        });

        JLabel messageLabel = (JLabel)getFinder().find(dialog, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JLabel;
            }
        });
        
        //Thread.sleep(10000);
        //System.out.println(messageLabel.getText());
        assertEquals("artificial test IO exception", messageLabel.getText());

        JButton okButton = (JButton)getFinder().find(dialog, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JButton
                    && "OK".equals(((JButton)c).getText());
            }
        });
        
        bTester.actionClick(okButton);
            
//        Thread.sleep(10000);

//        appFrame.dispose();
        frameDispose(appFrame);
    }

}
