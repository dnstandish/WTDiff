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
import java.text.DateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

import org.wtdiff.util.DirNode;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.LeafComparisonResult;
import org.wtdiff.util.OperationSupportTester;
import org.wtdiff.util.CompareController;
import org.wtdiff.util.FileNode.FileType;
import org.wtdiff.util.ui.NodePropertiesDialog;

import abbot.finder.Matcher;
import abbot.tester.JButtonTester;

import junit.extensions.abbot.ComponentTestFixture;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestNodePropertiesDialog  extends ComponentTestFixture {
    private class JLabelAndTextMatcher implements Matcher {
        private String matcherText;
        public void setText(String text) {
            matcherText = text;
            
        }
        public boolean matches(Component c) {
            return c instanceof JLabel
                && matcherText.equals(((JLabel)c).getText());
        }
    }
    private class JTextFieldMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JTextField;
        }
    }
    private class CloseJButtonMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Close".equals(((JButton)c).getText());
        }
    }
    private JLabelAndTextMatcher labelMatcher = new JLabelAndTextMatcher();
    private JTextFieldMatcher textFieldMatcher = new JTextFieldMatcher();
    private CloseJButtonMatcher closeButtonMatcher = new CloseJButtonMatcher();
    
    /**
     * dir1/
     * dir1/subdir1
     * dir1/tfile1
     * dir1/tfile3
     * dir1/tfile4
     * dir1/tpipe
     * dir1/tsym -> tfile1
     * dir2/
     * dir2/tfile1  - different
     * dir2/tfile2
     * dir1/tfile4
     */
    private File testDir1;
    private File tfile1;
    private File testDir2;
    private JFrame testFrame;
    private CompareController controller;
    private boolean testSymlink;
    private boolean testSpecialFile;
    
    @Before
    public void setUp() throws Exception {
        OperationSupportTester tester = new OperationSupportTester();
        testSymlink = tester.isSymlinkSupported();
        testSpecialFile = tester.isSpecialFileSupported();

        FileSystemTestHelper helper = new FileSystemTestHelper();
        testDir1 = helper.createTestDir("dir1");
        helper.createTestDir( "subdir1", testDir1 );
        tfile1 = helper.createTestFile("tfile1", "tfile1-content\n", testDir1);
        helper.createTestFile("tfile3", "tfile3-content\n", testDir1);
        helper.createTestFile("tfile4", "tfile4-content\n", testDir1);
        if ( testSymlink ) {
            helper.createTestSymlink("tfile1", "tsym", testDir1);
        }
        if ( testSpecialFile ) {
            helper.createTestFifo("tpipe", testDir1);
        }
        testDir2 = helper.createTestDir("dir2");
        helper.createTestFile("tfile2", "tfile2-content\n", testDir2);
        helper.createTestFile("tfile1", "tfile1m-content\n", testDir2);
        helper.createTestFile("tfile4", "tfile4-content\n", testDir2);
        testFrame = new JFrame();        
        controller = new CompareController();
        controller.setOldRoot(testDir1.getPath());
        controller.setNewRoot(testDir2.getPath());
        controller.compare();
    }

    @Test
    public void testDirNode() throws Exception {
        DirNode dirNode = controller.getOldRootNode();
        TreePath tPath = new TreePath(new Object[] { dirNode });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, tPath, dirNode);
        dialog.pack();
        dialog.setVisible(true);
        labelMatcher.setText("Path:");
        JLabel pathLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField pathTextArea = (JTextField)getFinder().find(pathLabel.getParent(), textFieldMatcher);
        assertEquals(dirNode.getRoot(), pathTextArea.getText());
        labelMatcher.setText("Name:");
        JLabel nameLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
        JTextField nameTextArea = (JTextField)getFinder().find(nameLabel.getParent(), textFieldMatcher);
        assertEquals(dirNode.getName(), nameTextArea.getText());
        labelMatcher.setText("Subfolders:");
        JLabel subfoldersLabel = (JLabel)getFinder().find(dialog, labelMatcher);
        JTextField subfoldersTextArea = (JTextField)getFinder().find(subfoldersLabel.getParent(), textFieldMatcher);
        assertEquals( dirNode.getDirs().size(), Integer.valueOf(subfoldersTextArea.getText()).intValue() );
        labelMatcher.setText("Files:");
        JLabel filesLabel = (JLabel)getFinder().find(dialog, labelMatcher);
        JTextField filesTextArea = (JTextField)getFinder().find(filesLabel.getParent(), textFieldMatcher);
        assertEquals( dirNode.getLeaves().size(), Integer.valueOf(filesTextArea.getText()).intValue() );
        JButton closeButton = (JButton)getFinder().find(dialog, closeButtonMatcher);
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(closeButton);

        DirNode subDirNode = dirNode.getDirs().get(0);
        TreePath subtPath = new TreePath(new Object[] { dirNode, subDirNode });
        NodePropertiesDialog subDialog = new NodePropertiesDialog( testFrame, subtPath, subDirNode);
        subDialog.pack();
        subDialog.setVisible(true);
        labelMatcher.setText("Path:");
        JLabel subPathLabel = (JLabel)getFinder().find(subDialog, labelMatcher); 
        JTextField subPathTextArea = (JTextField)getFinder().find(subPathLabel.getParent(), textFieldMatcher);
        String subPath = dirNode.getRoot() + File.separator + subDirNode.getName();
        assertEquals(subPath, subPathTextArea.getText());
        closeButton = (JButton)getFinder().find(subDialog, closeButtonMatcher);
        bTester.actionClick(closeButton);
        

        //ContainerDumper.dump("", dialog);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//        
//        }

    }
    @Test
    public void testFileNode() throws Exception {
        DirNode dirNode = controller.getNewRootNode();
        FileNode tfile1Node = (FileNode)dirNode.getLeaves().get(0);
        TreePath tfile1Path = new TreePath(new Object[] { dirNode, tfile1Node });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, tfile1Path, tfile1Node);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Path:
            labelMatcher.setText("Path:");
            JLabel pathLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField pathTextArea = (JTextField)getFinder().find(pathLabel.getParent(), textFieldMatcher);
            String subPath = dirNode.getRoot() + File.separator + tfile1Node.getName();
            assertEquals( subPath, pathTextArea.getText() );
        }
        {
            //Name:
            labelMatcher.setText("Name:");
            JLabel nameLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField nameTextArea = (JTextField)getFinder().find(nameLabel.getParent(), textFieldMatcher);
            assertEquals( tfile1Node.getName(), nameTextArea.getText() );
        }
        {
            //Type:
            labelMatcher.setText("Type:");
            JLabel typeLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField typeTextArea = (JTextField)getFinder().find(typeLabel.getParent(), textFieldMatcher);
            assertEquals( "regular file", typeTextArea.getText() );
        }
        {
            //Size:
            labelMatcher.setText("Size:");
            JLabel sizeLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField sizeTextArea = (JTextField)getFinder().find(sizeLabel.getParent(), textFieldMatcher);
            assertEquals( String.valueOf(tfile1Node.getSize()), sizeTextArea.getText() );
        }
        {
            //Time:
            labelMatcher.setText("Time:");
            JLabel timeLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField timeTextArea = (JTextField)getFinder().find(timeLabel.getParent(), textFieldMatcher);
            String date = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG).format(new Date(tfile1Node.getTime()));
            assertEquals( date, timeTextArea.getText() );
            
        }
        {
            //Is Text:
            labelMatcher.setText("Is Text:");
            JLabel isTextLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField isTextTextArea = (JTextField)getFinder().find(isTextLabel.getParent(), textFieldMatcher);
            assertEquals( "yes", isTextTextArea.getText() );
        }
        JButton closeButton = (JButton)getFinder().find(dialog, closeButtonMatcher);
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(closeButton);
    }
    @Test
    public void testSymlinkNode() throws Exception {
        
        if ( ! testSymlink )
            return;
        
        DirNode dirNode = controller.getOldRootNode();
        FileNode symlinkNode = null; 
        for ( Leaf node : dirNode.getLeaves() ) {
            if ( ((FileNode)node).getFileType() == FileType.SYMLINK ) {
                symlinkNode = (FileNode)node;
            }
        }
        assertNotNull(symlinkNode);
        
        TreePath symlinkPath = new TreePath(new Object[] { dirNode, symlinkNode });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, symlinkPath, symlinkNode);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Type:
            labelMatcher.setText("Type:");
            JLabel typeLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField typeTextArea = (JTextField)getFinder().find(typeLabel.getParent(), textFieldMatcher);
            assertEquals( "symbolic link", typeTextArea.getText() );
        }
        {
            //Links to:
            labelMatcher.setText("Links to:");
            JLabel linksToLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField linksToTextArea = (JTextField)getFinder().find(linksToLabel.getParent(), textFieldMatcher);
            assertEquals( tfile1.getName(), linksToTextArea.getText() );
        }

        JButton closeButton = (JButton)getFinder().find(dialog, closeButtonMatcher);
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(closeButton);
    }
    @Test
    public void testSpecialNode() throws Exception {
        
        if ( ! testSpecialFile )
            return;
        
        DirNode dirNode = controller.getOldRootNode();
        FileNode specialNode = null; 
        for ( Leaf node : dirNode.getLeaves() ) {
            if ( ((FileNode)node).getFileType() == FileType.SPECIAL ) {
                specialNode = (FileNode)node;
            }
        }
        assertNotNull(specialNode);
        
        TreePath specialPath = new TreePath(new Object[] { dirNode, specialNode });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, specialPath, specialNode);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Type:
            labelMatcher.setText("Type:");
            JLabel typeLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField typeTextArea = (JTextField)getFinder().find(typeLabel.getParent(), textFieldMatcher);
            assertEquals( "special file", typeTextArea.getText() );
        }

        JButton closeButton = (JButton)getFinder().find(dialog, closeButtonMatcher);
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(closeButton);
    }

    @Test
    public void testComparisonNodeChagned() throws Exception {
        DirNode cmpRoot = controller.getCompareRootNode();
        LeafComparisonResult tfile1Node = (LeafComparisonResult)cmpRoot.getLeaves().get(0);
        TreePath tfile1Path = new TreePath(new Object[] { cmpRoot, tfile1Node });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, tfile1Path, tfile1Node);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Path:
            labelMatcher.setText("Path:");
            JLabel pathLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField pathTextArea = (JTextField)getFinder().find(pathLabel.getParent(), textFieldMatcher);
            String subPath = cmpRoot.getName() + File.separator + tfile1Node.getName();
            assertEquals( subPath, pathTextArea.getText() );
        }
        {
            //Name:
            labelMatcher.setText("Name:");
            JLabel nameLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField nameTextArea = (JTextField)getFinder().find(nameLabel.getParent(), textFieldMatcher);
            assertEquals( tfile1Node.getName(), nameTextArea.getText() );
        }
        {
            //Name:
            labelMatcher.setText("Status:");
            JLabel statusLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField statusTextArea = (JTextField)getFinder().find(statusLabel.getParent(), textFieldMatcher);
            assertEquals( "changed", statusTextArea.getText() );
        }
        
        
    }
    @Test
    public void testComparisonNodeSame() throws Exception {
        DirNode cmpRoot = controller.getCompareRootNode();
        LeafComparisonResult tfile4Node = null;
        for( Leaf leaf: cmpRoot.getLeaves()) {
            if ( "tfile4".equals(leaf.getName()) )
                tfile4Node = (LeafComparisonResult)leaf;
        }
        TreePath tfile4Path = new TreePath(new Object[] { cmpRoot, tfile4Node });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, tfile4Path, tfile4Node);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Name:
            labelMatcher.setText("Status:");
            JLabel statusLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField statusTextArea = (JTextField)getFinder().find(statusLabel.getParent(), textFieldMatcher);
            assertEquals( "unchanged", statusTextArea.getText() );
        }
    }
    
    @Test
    public void testComparisonNodeNew() throws Exception {
        DirNode cmpRoot = controller.getCompareRootNode();
        LeafComparisonResult tfile2Node = null;
        for( Leaf leaf: cmpRoot.getLeaves()) {
            if ( "tfile2".equals(leaf.getName()) )
                tfile2Node = (LeafComparisonResult)leaf;
        }
        TreePath tfile2Path = new TreePath(new Object[] { cmpRoot, tfile2Node });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, tfile2Path, tfile2Node);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Name:
            labelMatcher.setText("Status:");
            JLabel statusLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField statusTextArea = (JTextField)getFinder().find(statusLabel.getParent(), textFieldMatcher);
            assertEquals( "new", statusTextArea.getText() );
        }
    }
    
    @Test
    public void testComparisonNodeDeleted() throws Exception {
        DirNode cmpRoot = controller.getCompareRootNode();
        LeafComparisonResult tfile3Node = null;
        for( Leaf leaf: cmpRoot.getLeaves()) {
            if ( "tfile3".equals(leaf.getName()) )
                tfile3Node = (LeafComparisonResult)leaf;
        }
        TreePath tfile3Path = new TreePath(new Object[] { cmpRoot, tfile3Node });
        NodePropertiesDialog dialog = new NodePropertiesDialog( testFrame, tfile3Path, tfile3Node);
        dialog.pack();
        dialog.setVisible(true);
        {
            //Name:
            labelMatcher.setText("Status:");
            JLabel statusLabel = (JLabel)getFinder().find(dialog, labelMatcher); 
            JTextField statusTextArea = (JTextField)getFinder().find(statusLabel.getParent(), textFieldMatcher);
            assertEquals( "deleted", statusTextArea.getText() );
        }
    }
    
    
}