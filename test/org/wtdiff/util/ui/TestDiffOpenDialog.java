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
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.OperationSupportTester;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.ui.DiffOpenDialog;

import abbot.finder.Matcher;
import abbot.tester.JButtonTester;

import junit.extensions.abbot.ComponentTestFixture;
import static org.junit.Assert.*;
import org.junit.After;

public class TestDiffOpenDialog   extends CommonComponentTestFixture {

    private File testDir1;
    private File tfile1;
    private File tfile11;
    private File testDir2;
    private File tfile2;
    private File tfile22;
    private JFrame testFrame;

    @Before
    public void setUp() throws IOException  {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        testDir1 = helper.createTestDir("dir1");
        helper.createTestDir( "subdir1", testDir1 );
        tfile1 = helper.createTestFile("tfile1", "tfile1-content\n", testDir1);
        tfile11 = helper.createTestFile("tfile11", "tfile11-content\n", testDir1);
        testDir2 = helper.createTestDir("dir2");
        tfile2 = helper.createTestFile("tfile2", "tfile2-content\n", testDir2);
        tfile22 = helper.createTestFile("tfile2", "tfile2-content\n", testDir2);
        
        testFrame = new JFrame();        

    }
    
    private void dialogControllerConstructorCheck(DiffController controller, String expectedOld, String expectedNew) throws Exception {
        
        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        assertEquals(expectedOld, oldTextArea.getText());
        
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);
        assertEquals(expectedNew, newTextArea.getText());
        
        clickCancel(dialog);
                
        if ( dialogThread.isAlive() ) {
            try { 
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        
    }
    
    @Test
    public void testConstructor() throws Exception {
        DiffController controller = new DiffController();
        
        assertNull(controller.getOldSourceName());
        assertNull(controller.getNewSourceName());
        
        dialogControllerConstructorCheck(controller, "", "");
        
        controller.setOldSource(new FileInputStreamSource(tfile1));
        controller.setNewSource(new FileInputStreamSource(tfile2));

        dialogControllerConstructorCheck(controller, controller.getOldSourceName(), controller.getNewSourceName());
        
    }
    
    @Test 
    public void testEmptyFile() throws Exception {
        DiffController controller = new DiffController();
        
        assertNull(controller.getOldSourceName());
        assertNull(controller.getNewSourceName());

        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        clickOK(dialog);

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);

        oldTextArea.setText(tfile1.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage("Missing file name", dialog);
        
        oldTextArea.setText("");
        newTextArea.setText(tfile1.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage("Missing file name", dialog);

        oldTextArea.setText(tfile2.getPath());
        newTextArea.setText(tfile1.getPath());
        clickOK(dialog);
        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(tfile2.getPath(), controller.getOldSourceName());
        assertEquals(tfile1.getPath(), controller.getNewSourceName());
    }

    @Test 
    public void testNonexistentEmptyFile() throws Exception {
        DiffController controller = new DiffController();
        
        assertNull(controller.getOldSourceName());
        assertNull(controller.getNewSourceName());

        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);

        File noexist = new File(testDir1, "noexist1");
        oldTextArea.setText(noexist.getPath());
        newTextArea.setText(tfile1.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage(noexist.getPath() + " does not exist", dialog);
        
        oldTextArea.setText(tfile1.getPath());
        newTextArea.setText(noexist.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage(noexist.getPath() + " does not exist", dialog);

        oldTextArea.setText(tfile1.getPath());
        newTextArea.setText(tfile2.getPath());
        clickOK(dialog);
        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(tfile1.getPath(), controller.getOldSourceName());
        assertEquals(tfile2.getPath(), controller.getNewSourceName());
    }

    @Test 
    public void testNotARegularFile() throws Exception {
        DiffController controller = new DiffController();
        
        assertNull(controller.getOldSourceName());
        assertNull(controller.getNewSourceName());

        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);

        oldTextArea.setText(testDir1.getPath());
        newTextArea.setText(tfile1.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage(testDir1.getPath() + " is not a regular file", dialog);
        
        oldTextArea.setText(tfile1.getPath());
        newTextArea.setText(testDir1.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage(testDir1.getPath() + " is not a regular file", dialog);

        oldTextArea.setText(tfile1.getPath());
        newTextArea.setText(tfile2.getPath());
        clickOK(dialog);
        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(tfile1.getPath(), controller.getOldSourceName());
        assertEquals(tfile2.getPath(), controller.getNewSourceName());
    }

    @Test 
    public void testFileNotReadable() throws Exception {
        DiffController controller = new DiffController();
        
        assertNull(controller.getOldSourceName());
        assertNull(controller.getNewSourceName());

        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);

        OperationSupportTester ost = new OperationSupportTester();
        ost.setReadable(tfile1, false);
        oldTextArea.setText(tfile2.getPath());
        newTextArea.setText(tfile1.getPath());
        clickOK(dialog);
        verifyErrorDialogMessage(tfile1.getPath() + " is not readable", dialog);
        ost.setReadable(tfile1, true);
        
        ost.setReadable(tfile2, false);
        clickOK(dialog);
        verifyErrorDialogMessage(tfile2.getPath() + " is not readable", dialog);
        ost.setReadable(tfile2, true);

        clickCancel(dialog);
        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
    }

    @Test
    public void testCancelDoesNotUpdateController() throws Exception {
        DiffController controller = new DiffController();
        
        controller.setOldSource(new FileInputStreamSource(tfile1));
        controller.setNewSource(new FileInputStreamSource(tfile2));

        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);

        oldTextArea.setText(tfile2.getPath());
        newTextArea.setText(tfile1.getPath());
        clickCancel(dialog);
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(tfile1.getPath(), controller.getOldSourceName());
        assertEquals(tfile2.getPath(), controller.getNewSourceName());
    }

    @Test
    public void testFolderButton() throws Exception {
        DiffController controller = new DiffController();

        DiffOpenDialog dialog = new DiffOpenDialog( testFrame, controller);
        dialog.pack();
        ModalDialogThread dialogThread = new ModalDialogThread(dialog);
        dialogThread.start();

        labelMatcher.setText("Old");
        JLabel oldLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField oldTextArea = (JTextField)getFinder().find(oldLabel.getParent(), textFieldMatcher);
        oldTextArea.setText(tfile1.getPath());
        
        labelMatcher.setText("New");
        JLabel newLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField newTextArea = (JTextField)getFinder().find(newLabel.getParent(), textFieldMatcher);
        newTextArea.setText(tfile2.getPath());

        
        JButton oldFolderButton = (JButton)getFinder().find(oldLabel.getParent(), new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JButton
                    && "".equals(((JButton)c).getText());
            }
        });
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(oldFolderButton);
        
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            assertEquals(testDir1, chooser.getCurrentDirectory());
//            assertEquals(tfile1.getParentFile(), chooser.getCurrentDirectory());
//            String pathTop = tfile1.getParentFile().getName();
//            labelMatcher.setText(pathTop);
//            JLabel where = (JLabel)getFinder().find(chooser, labelMatcher);
//            assertNotNull(where);
            //clickCancel(chooser);
            chooser.cancelSelection();
        }        
        assertEquals(tfile1.getPath(), oldTextArea.getText());
//Thread.sleep(20000);
        bTester.actionClick(oldFolderButton);
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            
            JTextField fileName = (JTextField)getFinder().find(chooser, textFieldMatcher);
            fileName.setText(tfile11.getName());
            clickSelect(chooser);
//            chooser.approveSelection();
//Thread.sleep(20000);
        }        
        assertEquals(tfile11.getPath(), oldTextArea.getText());

        JButton newFolderButton = (JButton)getFinder().find(newLabel.getParent(), new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JButton
                    && "".equals(((JButton)c).getText());
            }
        });

        bTester.actionClick(newFolderButton);
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            
            JTextField fileName = (JTextField)getFinder().find(chooser, textFieldMatcher);
            fileName.setText(tfile22.getName());
            clickSelect(chooser);
        }        
        assertEquals(tfile22.getPath(), newTextArea.getText());

        clickCancel(dialog);
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
    }
}
