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

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.xml.stream.XMLStreamException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.OperationSupportTester;
import org.wtdiff.util.ui.SnapshotDialog;
import org.wtdiff.util.ui.CompareTreeModel;
import org.wtdiff.util.xml.DirNodeXMLStreamReader;

import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;
import abbot.tester.ButtonTester;
import abbot.tester.JButtonTester;

public class TestSnapshotDialog  extends CommonComponentTestFixture {

    JFrame testFrame;
    private FileSystemTestHelper helper;
    private OperationSupportTester ost;

    
    @Before
    public void setUp() throws Exception {
        testFrame = new JFrame();
        helper = new FileSystemTestHelper();
        ost = new OperationSupportTester();
    }

    @After
    public void tearDown() throws Exception {
    }

    private JTextField getCaptureField(JDialog dialog) throws ComponentNotFoundException, MultipleComponentsFoundException  {
        labelMatcher.setText("Capture");
        JLabel fileLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        return (JTextField)getFinder().find(fileLabel.getParent(), textFieldMatcher);
    }

    private JTextArea getCommentArea(JDialog dialog) throws ComponentNotFoundException, MultipleComponentsFoundException {
        labelMatcher.setText("Comment");
        JLabel commentLabel;
        commentLabel = (JLabel)getFinder().find(dialog, labelMatcher);
        return (JTextArea)getFinder().find(commentLabel.getParent(),
            new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JTextArea;
                }
        });
    }
    
    private JTextField getSaveFileField(JDialog dialog) throws ComponentNotFoundException, MultipleComponentsFoundException  {
        labelMatcher.setText("File");
        JLabel fileLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        return (JTextField)getFinder().find(fileLabel.getParent(), textFieldMatcher);
    }
    
    private JCheckBox getCrc32Box(JDialog dialog) throws ComponentNotFoundException, MultipleComponentsFoundException {
        return (JCheckBox)getFinder().find(dialog,
            new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JCheckBox && "CRC32".equals(((JCheckBox)c).getText()) ;
            }
        });
    }
    
    private JCheckBox getMd5Box(JDialog dialog) throws ComponentNotFoundException, MultipleComponentsFoundException {
        return (JCheckBox)getFinder().find(dialog,
            new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JCheckBox && "MD5".equals(((JCheckBox)c).getText()) ;
            }
        });
    }

    @Test
    public void testBasic() throws ComponentNotFoundException, MultipleComponentsFoundException {
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot((new File("a", "d")).getPath());
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField captureTextField = getCaptureField(dialog);
            assertEquals(dn.getRoot(), captureTextField.getText());
            
            JTextArea commentTextArea = getCommentArea(dialog);
            assertEquals("", commentTextArea.getText());
            
            JTextField fileTextArea = getSaveFileField(dialog);
            assertEquals(dn.getName() + ".xml", fileTextArea.getText());
            
            getCrc32Box(dialog);
            
            getMd5Box(dialog);
            
            clickCancel(dialog);
            assertFalse(dialog.isVisible());
        }
        {
            tree.addSelectionRow(1);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, fn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField captureTextField = getCaptureField(dialog);
            String expectedCaptureText = (new File(dn.getRoot(), fn.getName())).getPath();
            assertEquals(expectedCaptureText, captureTextField.getText());
            
            JTextField fileTextArea = getSaveFileField(dialog);
            assertEquals(dn.getName() + "_" + fn.getName() + ".xml", fileTextArea.getText());
            
            clickCancel(dialog);
        }
        
    }

    @Test
    public void testRootEmptyName() throws ComponentNotFoundException, MultipleComponentsFoundException {
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setRoot("a.zip");
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField captureTextField = getCaptureField(dialog);
            assertEquals(dn.getRoot(), captureTextField.getText());
            
            JTextField fileTextArea = getSaveFileField(dialog);
            assertEquals("snapshot.xml", fileTextArea.getText());
            
            getCrc32Box(dialog);
            
            getMd5Box(dialog);
            
            clickCancel(dialog);
        }
        {
            tree.addSelectionRow(1);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, fn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField captureTextField = getCaptureField(dialog);
            String expectedCaptureText = (new File(dn.getRoot(), fn.getName())).getPath();
            assertEquals(expectedCaptureText, captureTextField.getText());
            
            JTextField fileTextArea = getSaveFileField(dialog);
            assertEquals(fn.getName() + ".xml", fileTextArea.getText());
            
            clickCancel(dialog);
        }
        
    }

    @Test
    public void testRootEmptyRoot() throws ComponentNotFoundException, MultipleComponentsFoundException {
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField captureTextField = getCaptureField(dialog);
            assertEquals("", captureTextField.getText());
            
            JTextField fileTextArea = getSaveFileField(dialog);
            assertEquals("snapshot.xml", fileTextArea.getText());
            
            getCrc32Box(dialog);
            
            getMd5Box(dialog);
            
            clickCancel(dialog);
        }
        {
            tree.addSelectionRow(1);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, fn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField captureTextField = getCaptureField(dialog);
            String expectedCaptureText = fn.getName();
            assertEquals(expectedCaptureText, captureTextField.getText());
            
            JTextField fileTextArea = getSaveFileField(dialog);
            assertEquals(fn.getName() + ".xml", fileTextArea.getText());
            
            clickCancel(dialog);
        }
        
    }

    @Test
    public void testFileNotSet() throws ComponentNotFoundException, MultipleComponentsFoundException {
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot("d");
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);
            
            JTextField fileTextArea = getSaveFileField(dialog);
            fileTextArea.setText("");
            
            JCheckBox crc32box = getCrc32Box(dialog);
            crc32box.setSelected(true);
            
            clickWrite(dialog);
            verifyErrorDialogMessage("File not set", dialog);
            clickCancel(dialog);
        }
    }

    private DirNodeXMLStreamReader readFile(File snapshot) throws XMLStreamException, IOException {
        DirNodeXMLStreamReader reader = new DirNodeXMLStreamReader();
        try ( FileInputStream in = new FileInputStream(snapshot) ) {
            reader.readSnapshot(in);
        }
        return reader;
        
    }
    private List<String> getAvailableDigests(File snapshot) throws XMLStreamException, IOException {
//      DirNodeXMLStreamReader reader = new DirNodeXMLStreamReader();
//      try ( FileInputStream in = new FileInputStream(snapshot) ) {
//          reader.readSnapshot(in);
//      }
//      return reader.getAvailableDigests();
      return readFile(snapshot).getAvailableDigests();
  }
      
    private String getComment(File snapshot) throws XMLStreamException, IOException {
//      DirNodeXMLStreamReader reader = new DirNodeXMLStreamReader();
//      try ( FileInputStream in = new FileInputStream(snapshot) ) {
//          reader.readSnapshot(in);
//      }
//      return reader.getAvailableDigests();
      return readFile(snapshot).getSnapshotInfo().get(DirNodeXMLStreamReader.ELEMENT_USER_COMMENT);
  }
      
    @Test
    public void testNoDigestSelected() throws ComponentNotFoundException, MultipleComponentsFoundException, IOException, XMLStreamException {
        File testDir = helper.createTestDir("testNoDigestSelected");
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot("d");
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            final SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);
            
            File saveFile = new File(testDir, "nodigest.xml");
            saveFile.deleteOnExit();
            JTextField fileTextArea = getSaveFileField(dialog);
            fileTextArea.setText(saveFile.getPath());
            
            JCheckBox crc32box = getCrc32Box(dialog);
            crc32box.setSelected(false);
            JCheckBox md5box = getMd5Box(dialog);
            md5box.setSelected(false);
            
            clickWrite(dialog);
            {
                JDialog confirmDialog = (JDialog)getFinder().find(new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JDialog
                            && c != dialog;
                    }
                });
                
                JLabel messageLabel = (JLabel)getFinder().find(confirmDialog, new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JLabel;
                    }
                });
                assertEquals("No digests selected. Continue?", messageLabel.getText());
    
                clickCancel(confirmDialog);
            }            
            
            assertTrue(dialog.isVisible());
            assertFalse( saveFile.exists() );
            
            clickWrite(dialog);
            {
                JDialog confirmDialog = (JDialog)getFinder().find(new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JDialog
                            && c != dialog;
                    }
                });
                clickOK(confirmDialog);
                
                JDialog confirmMessage = (JDialog)getFinder().find(new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JDialog
                            && c != dialog;
                    }
                });

                JLabel messageLabel = (JLabel)getFinder().find(confirmMessage, new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JLabel;
                    }
                });
                assertEquals("Snapshot written to " + saveFile.getPath(), messageLabel.getText());
                clickOK(confirmMessage);
            }         
            assertFalse(dialog.isVisible());
            assertTrue( saveFile.exists() );
            
            
//            DirNodeXMLStreamReader reader = new DirNodeXMLStreamReader();
//            try ( FileInputStream in = new FileInputStream(saveFile) ) {
//                reader.readSnapshot(in);
//            }
            assertEquals(0, getAvailableDigests(saveFile).size());
            
        }
    }

    private void digestCheck(String testName, boolean includeCrc32, boolean includeMd5) throws Exception {
        assertTrue(includeCrc32 || includeMd5);
        File testDir = helper.createTestDir(testName);
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot("d");
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            final SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);
            
            File saveFile = new File(testDir, "nodigest.xml");
            saveFile.deleteOnExit();
            saveFile.delete();
            JTextField fileTextArea = getSaveFileField(dialog);
            fileTextArea.setText(saveFile.getPath());
            
            JCheckBox crc32box = getCrc32Box(dialog);
            crc32box.setSelected(includeCrc32);
            JCheckBox md5box = getMd5Box(dialog);
            md5box.setSelected(includeMd5);
            
            clickWrite(dialog);
            {
                JOptionPane confirmMessage = (JOptionPane)getFinder().find(new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JOptionPane //JDialog
                            && c != dialog;
                    }
                });
//                ContainerDumper.dump("", confirmMessage);
                JLabel messageLabel = (JLabel)getFinder().find(confirmMessage, new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JLabel;
                    }
                });
                assertEquals("Snapshot written to " + saveFile.getPath(), messageLabel.getText());
                clickOK(confirmMessage);
            }         
            assertFalse(dialog.isVisible());
            assertTrue( saveFile.exists() );
            
            int expectedCount = 0;
            
            if ( includeCrc32 )
                expectedCount++;
            
            if ( includeMd5 )
                expectedCount++;
            List<String> digests = getAvailableDigests(saveFile);
            assertEquals(expectedCount, digests.size());
            if ( includeCrc32 )
                assertTrue( digests.contains("CRC32"));
            if ( includeMd5 )
                assertTrue( digests.contains("MD5"));
            
            
        }
    }

    @Test
    public void testDigests() throws Exception {
        digestCheck("testCrc32", true, false);
        digestCheck("testMd5", false, true);
        digestCheck("testCrc32Md5", true, true);
    }

    @Test
    public void testComment() throws Exception {
        File testDir = helper.createTestDir("testComment");
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot("d");
        String comment = "This is a comment";
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        {
            tree.addSelectionRow(0);
            TreePath path = tree.getSelectionPath();
            tree.clearSelection();
            final SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
            dialog.pack();
            dialog.setVisible(true);

            JTextArea commentTextArea = getCommentArea(dialog);
            commentTextArea.setText(comment);

            File saveFile = new File(testDir, "nodigest.xml");
            saveFile.delete();
            JTextField fileTextArea = getSaveFileField(dialog);
            fileTextArea.setText(saveFile.getPath());
            
            JCheckBox crc32box = getCrc32Box(dialog);
            crc32box.setSelected(true);
            
            clickWrite(dialog);
            {
                JOptionPane confirmMessage = (JOptionPane)getFinder().find(new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JOptionPane //JDialog
                            && c != dialog;
                    }
                });
                JLabel messageLabel = (JLabel)getFinder().find(confirmMessage, new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JLabel;
                    }
                });
                assertEquals("Snapshot written to " + saveFile.getPath(), messageLabel.getText());
                clickOK(confirmMessage);
            }         
            assertFalse(dialog.isVisible());
            assertTrue( saveFile.exists() );
            
            String actual = getComment(saveFile);
            assertEquals(comment, actual);
            saveFile.delete();            
        }
    }

    @Test
    public void testWriteError() throws Exception {
        File testDir = helper.createTestDir("testWriteError");
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot("d");
        String comment = "This is a comment";
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        
        tree.addSelectionRow(0);
        TreePath path = tree.getSelectionPath();
        tree.clearSelection();
        final SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
        dialog.pack();
        dialog.setVisible(true);

        JTextArea commentTextArea = getCommentArea(dialog);
        commentTextArea.setText(comment);

        File saveFile = new File(testDir, "snapshot.xml");
        saveFile.createNewFile();
        saveFile.deleteOnExit();
        ost.setWritable(saveFile, false);
        JTextField fileTextArea = getSaveFileField(dialog);
        fileTextArea.setText(saveFile.getPath());
        
        JCheckBox crc32box = getCrc32Box(dialog);
        crc32box.setSelected(true);
        
        clickWrite(dialog);
        
        JOptionPane confirmMessage = (JOptionPane)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JOptionPane //JDialog
                    && c != dialog;
            }
        });
        int messageType = confirmMessage.getMessageType();
        assertEquals(JOptionPane.ERROR_MESSAGE, messageType);
        clickOK(confirmMessage);
                 
        assertTrue(dialog.isVisible());
        ost.setWritable(saveFile, true);
        clickCancel(dialog);
        assertFalse(dialog.isVisible());
            
    }

    @Test
    public void testFolderButton() throws Exception {
        File testDir = helper.createTestDir("testFolderButton");
        MockFileNode fn = new MockFileNode("test");
        DirNode dn = new DirNode(fn);
        dn.setName("d");
        dn.setRoot("d");
        CompareTreeModel model = new CompareTreeModel(dn);
        JTree tree = new JTree(model);
        
        tree.addSelectionRow(0);
        TreePath path = tree.getSelectionPath();
        tree.clearSelection();
        final SnapshotDialog dialog = new SnapshotDialog(testFrame, path, dn);
        dialog.pack();
        dialog.setVisible(true);

        File saveFile = new File(testDir, "snapshot.xml");
        saveFile.createNewFile();
        saveFile.deleteOnExit();
        JTextField fileTextArea = getSaveFileField(dialog);
        fileTextArea.setText(saveFile.getPath());
        
        JCheckBox crc32box = getCrc32Box(dialog);
        crc32box.setSelected(true);
        
        JButton folderButton = (JButton)getFinder().find(fileTextArea.getParent(), new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JButton
                    && "".equals(((JButton)c).getText());
            }
        });
        JButtonTester bTester = new JButtonTester();
        bTester.actionClick(folderButton);

        JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JFileChooser;
            }
        });
        assertEquals(testDir, chooser.getCurrentDirectory());
        chooser.cancelSelection();

        clickCancel(dialog);
        assertFalse(dialog.isVisible());
            
    }

}
