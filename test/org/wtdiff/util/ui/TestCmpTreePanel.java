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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.junit.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.LeafComparisonResult;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.OperationSupportTester;
import org.wtdiff.util.ReadFailMockFileNode;
import org.wtdiff.util.CompareController;
import org.wtdiff.util.FileNode.FileType;
import org.wtdiff.util.CompareController.NodeRole;
import org.wtdiff.util.ui.DialogErrorHandler;
import org.wtdiff.util.ui.DiffFrame;
import org.wtdiff.util.ui.NodePropertiesDialog;
import org.wtdiff.util.ui.SnapshotDialog;
import org.wtdiff.util.ui.CmpTreePanel;

import junit.extensions.abbot.*;
import abbot.tester.*;
import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;
import abbot.finder.matchers.*;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;

public class TestCmpTreePanel extends CommonComponentTestFixture {

    /* 
     * dir1/
     *     tfile1
     * dir2/
     *     tfile2
     * dir3/
     *     dirnoread/
     * dir4/
     *     tfilenoread/
    */
    File testDir1;
    File tfile1;
    File testDir2;
    File tfile2;
    File testDir3;
    File testDirNoread;
    File testDir4;
    File tfileNoread;

    @Before
    public void setUp() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        testDir1 = helper.createTestDir("dir1");
        tfile1 = helper.createTestFile("tfile1", "tfile1-content\n", testDir1);
        testDir2 = helper.createTestDir("dir2");
        tfile2 = helper.createTestFile("tfile2", "tfile2-content\n", testDir2);
        testDir3 = helper.createTestDir("dir3");
        testDirNoread = helper.createTestDir("dirnoread", testDir3);
        OperationSupportTester ost = new OperationSupportTester();
        ost.setReadable(testDirNoread, false);
        testDir4 = helper.createTestDir("dir4");
        tfileNoread = helper.createTestFile("tfilenoread", "tfilenoread-content\n", testDir4);
        ost.setReadable(tfileNoread, false);

    }
    
    protected Matcher loadJButtonMatcher = new Matcher() {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Load".equals(((JButton)c).getText());
        }        
    };

    protected void clickLoad(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton cancelButton = (JButton)getFinder().find(parent, loadJButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(cancelButton);
    }

    protected Matcher compareJButtonMatcher = new Matcher() {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Compare".equals(((JButton)c).getText());
        }        
    };

    protected void clickCompare(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton cancelButton = (JButton)getFinder().find(parent, compareJButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(cancelButton);
    }

    private void panelTest(NodeRole role) throws Exception {

        CompareController controller = new CompareController();
        assertNull(controller.getRootNode(role));
        CmpTreePanel panel = new CmpTreePanel(role, controller);
        showFrame(panel);
        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        String path = pathField.getText();
        assertEquals("", path);
        
        pathField.setText( testDir1.getPath() );
        assertNull( controller.getRootNode( role ) );
        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        DirNode node = (DirNode)model.getRoot();
        assertNull(node.getRoot());
        clickLoad(panel);

        assertEquals(testDir1.getPath(), controller.getRoot(role));
        model = tree.getModel();
        node = (DirNode)model.getRoot();
        assertEquals(testDir1.getPath(), node.getRoot());
        
        ContainerTester treeTester = new JTreeTester();        
        int rowCount = tree.getRowCount();
        Rectangle rect = tree.getRowBounds( rowCount - 1 );
        Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
        
        treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
        {
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Root Here", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );
            
            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionClick((JMenuItem)mElem[2]);
            NodePropertiesDialog dialog = (NodePropertiesDialog)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof NodePropertiesDialog;
                }
            });
            clickClose(dialog);
        }
        
        // capture snapshot
        treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
        {
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            JMenuItemTester miTester = new JMenuItemTester();
            MenuElement[] mElem = pMenu.getSubElements();
            miTester.actionClick((JMenuItem)mElem[1]);
            SnapshotDialog snapDialog = (SnapshotDialog)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof SnapshotDialog;
                }
            });
            clickCancel(snapDialog);
        }
        
    }
    @Test
    public void testPanelPreloadedController() throws Exception {

        CompareController controller = new CompareController();
        controller.setNewRoot(testDir1.getPath());
        CmpTreePanel panel = new CmpTreePanel(NodeRole.NEW_ROOT, controller);
        showFrame(panel);
        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        String path = pathField.getText();
        assertEquals(testDir1.getPath(), path);
        
        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        assertEquals(testDir1.getPath(), controller.getRoot(NodeRole.NEW_ROOT));
        TreeModel model = tree.getModel();
        DirNode node = (DirNode)model.getRoot();
        assertEquals(testDir1.getPath(), node.getRoot());
    }

    @Test
    public void testRootAction() throws Exception {
        // d1/ { d12/ , f1 }
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir1 = helper.createTestDir("d1");
        File f1 = helper.createTestFile("f1", "tfile1-content\n", dir1);
        File dir12 = helper.createTestDir("d12", dir1);
        CompareController controller = new CompareController();
        controller.setOldRoot(dir1.getPath());
        controller.setNewRoot(dir12.getPath());

        assertEquals( controller.getOldRootNode(),
            controller.getOldCompareRootNode() );

        CmpTreePanel panel = new CmpTreePanel(NodeRole.OLD_ROOT, controller);
        showFrame(panel);
//Thread.sleep(10000);            
        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
//        TreeModel model = tree.getModel();
        TreeCellRenderer cellRenderer = tree.getCellRenderer();
        ContainerTester treeTester = new JTreeTester();        
        assertEquals( 3, tree.getRowCount()) ;
        {
            int row = 1;
            Rectangle rect = tree.getRowBounds( row ); // d12
            Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
            TreePath pathToD12 = tree.getPathForLocation(rowCenter.x, rowCenter.y);
            Object d12Object = pathToD12.getLastPathComponent();
            Component compD12Object = cellRenderer.getTreeCellRendererComponent(tree, d12Object, false, false, false, row, false);
            assertFalse( compD12Object.getFont().isBold() );
            treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Root Here", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );
            
            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionClick((JMenuItem)mElem[0]);
            
            assertSame( controller.getOldRootNode().getDirs().get(0),
                controller.getOldCompareRootNode() );

            compD12Object = cellRenderer.getTreeCellRendererComponent(tree, d12Object, false, false, false, row, false);
            assertTrue( compD12Object.getFont().isBold() );

        }
        {
            int row = 1;
            Rectangle rect = tree.getRowBounds( row ); // d12
            Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
            TreePath pathToD12 = tree.getPathForLocation(rowCenter.x, rowCenter.y);
            Object d12Object = pathToD12.getLastPathComponent();
            Component compD12Object = cellRenderer.getTreeCellRendererComponent(tree, d12Object, false, false, false, row, false);
            assertTrue( compD12Object.getFont().isBold() );
            treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Unroot", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );

            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionClick((JMenuItem)mElem[0]);
            assertSame( controller.getOldRootNode(),
                controller.getOldCompareRootNode() );
            compD12Object = cellRenderer.getTreeCellRendererComponent(tree, d12Object, false, false, false, row, false);
            assertFalse( compD12Object.getFont().isBold() );
        }
        {
            int row = 2;
            Rectangle rect = tree.getRowBounds( row ); // f1
            Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
            TreePath pathToF1 = tree.getPathForLocation(rowCenter.x, rowCenter.y);
            Object f1Object = pathToF1.getLastPathComponent();
            Component compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertFalse( compF1Object.getFont().isBold() );
            treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Root Here", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );

            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionClick((JMenuItem)mElem[0]);
            
            assertNotSame( controller.getOldRootNode(),
                controller.getOldCompareRootNode() );
            assertSame(controller.getOldRootNode().getLeaves().get(0),
                controller.getOldCompareRootNode().getLeaves().get(0) );
            compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertTrue( compF1Object.getFont().isBold() );
        }
        {
            int row = 2;
            Rectangle rect = tree.getRowBounds( row ); // f1
            Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
            TreePath pathToF1 = tree.getPathForLocation(rowCenter.x, rowCenter.y);
            Object f1Object = pathToF1.getLastPathComponent();
            Component compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertTrue( compF1Object.getFont().isBold() );
            treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Unroot", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );

            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionClick((JMenuItem)mElem[0]);
            
            assertSame( controller.getOldRootNode(),
                controller.getOldCompareRootNode() );
            compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertFalse( compF1Object.getFont().isBold() );
        }
        {
            int row = 2;
            Rectangle rect = tree.getRowBounds( row ); // f1
            Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
            TreePath pathToF1 = tree.getPathForLocation(rowCenter.x, rowCenter.y);
            Object f1Object = pathToF1.getLastPathComponent();
            Component compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertFalse( compF1Object.getFont().isBold() );
            treeTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Root Here", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );

            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionClick((JMenuItem)mElem[0]);
            
            assertNotSame( controller.getOldRootNode(),
                controller.getOldCompareRootNode() );
            assertSame(controller.getOldRootNode().getLeaves().get(0),
                controller.getOldCompareRootNode().getLeaves().get(0) );
            compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertTrue( compF1Object.getFont().isBold() );

            int newRow = 1;
            Rectangle newRect = tree.getRowBounds( newRow ); // d12
            Point newRowCenter = new Point(newRect.x + newRect.width/2, newRect.y + newRect.height/2);
            TreePath pathToD12 = tree.getPathForLocation(newRowCenter.x, newRowCenter.y);
            Object d12Object = pathToD12.getLastPathComponent();
            Component compD12Object = cellRenderer.getTreeCellRendererComponent(tree, d12Object, false, false, false, newRow, false);
            assertFalse( compD12Object.getFont().isBold() );
            treeTester.actionClick(tree, new ComponentLocation(newRowCenter) , MouseEvent.BUTTON3_MASK);
            pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            mElem = pMenu.getSubElements();
            assertEquals(3, mElem.length);
            assertEquals("Root Here", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Capture snapshot", ((JMenuItem)mElem[1]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[2]).getText() );

            miTester.actionClick((JMenuItem)mElem[0]);
//Thread.sleep(30000);            
            compD12Object = cellRenderer.getTreeCellRendererComponent(tree, d12Object, false, false, false, newRow, false);
            assertTrue( compD12Object.getFont().isBold() );
            compF1Object = cellRenderer.getTreeCellRendererComponent(tree, f1Object, false, false, false, row, false);
            assertFalse( compF1Object.getFont().isBold() );
            
        }
    }

    @Test
    public void testOldPanel() throws Exception {
        panelTest( NodeRole.OLD_ROOT );
    }
    
    @Test
    public void testNewPanel() throws Exception {
        panelTest( NodeRole.NEW_ROOT );
    }

    @Test
    public void testComparePanel() throws Exception {
        CompareController controller = new CompareController();
        assertNull(controller.getOldRoot());
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        assertEquals( "", pathField.getText() );
        
        controller.setOldRoot(testDir1.getPath());
        
        assertNull( controller.getRootNode( NodeRole.CMP_ROOT ) );
        assertEquals( "", pathField.getText() );
        
        controller.setNewRoot(testDir2.getPath());

        assertNull( controller.getRootNode( NodeRole.CMP_ROOT ) );
        assertEquals( testDir1.getPath() + " <> " + testDir2.getPath() , pathField.getText() );

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        DirNode node = (DirNode)model.getRoot();
        assertNull(node);
        clickCompare(panel);

        model = tree.getModel();
        node = (DirNode)model.getRoot();
        assertNull(node.getRoot());
        ContainerTester compTester = new JTreeTester();        
        int rowCount = tree.getRowCount();
        Rectangle rect = tree.getRowBounds( rowCount - 1 );
        Point rowCenter = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
        
        compTester.actionClick(tree, new ComponentLocation(rowCenter) , MouseEvent.BUTTON3_MASK);
        JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JPopupMenu;
            }
        });
        MenuElement[] mElem = pMenu.getSubElements();
        assertEquals(2, mElem.length);
        assertEquals("Diff", ((JMenuItem)mElem[0]).getText() );
        assertEquals("Properties", ((JMenuItem)mElem[1]).getText() );
        JMenuItemTester miTester = new JMenuItemTester();
        miTester.actionClick((JMenuItem)mElem[1]);
        NodePropertiesDialog dialog = (NodePropertiesDialog)getFinder().find( new Matcher() {
            public boolean matches(Component c) {
                return c instanceof NodePropertiesDialog;
            }
        });
        clickClose(dialog);
    }
    
    
    @Test
    public void testBadPathLoad() throws Exception {
        CompareController controller = new CompareController();
        assertNull(controller.getRootNode(NodeRole.NEW_ROOT));
        CmpTreePanel panel = new CmpTreePanel(NodeRole.NEW_ROOT, controller);
        showFrame(panel);
        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        String path = pathField.getText();
        assertEquals("", path);
        
        assertNull( controller.getRootNode( NodeRole.NEW_ROOT ) );

        JButton loadButton = (JButton)getFinder().find(panel, loadJButtonMatcher);
        
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(loadButton);

        verifyErrorDialogMessage("path not set", null);
    
        pathField.setText("noexist");
        bTester.actionClick(loadButton);

        verifyErrorDialogMessage("path does not exist", null);

    }

    @Test
    public void testExceptionCatch() throws Exception {
        CompareController controller = new CompareController();
        assertNull(controller.getRootNode(NodeRole.NEW_ROOT));
        CmpTreePanel panel = new CmpTreePanel(NodeRole.NEW_ROOT, controller);
        showFrame(panel);
        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        String path = pathField.getText();
        assertEquals("", path);

        pathField.setText( testDirNoread.getPath() );
        System.out.println(pathField.getText());
        
        assertNull( controller.getRootNode( NodeRole.NEW_ROOT ) );
        clickLoad(panel);
        verifyErrorDialogMessage(testDirNoread.getPath() + " permission denied", null);

    }
    @Test
    public void testErrorHandled() throws Exception {
        CompareController controller = new CompareController();
        DialogErrorHandler handler = new DialogErrorHandler();
        controller.setErrorHandler( handler );
        assertNull(controller.getRootNode(NodeRole.NEW_ROOT));
        CmpTreePanel panel = new CmpTreePanel(NodeRole.NEW_ROOT, controller);
        showFrame(panel);
        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        String path = pathField.getText();
        assertEquals("", path);

        pathField.setText( testDirNoread.getPath() );
        
        assertNull( controller.getRootNode( NodeRole.NEW_ROOT ) );
        JButton loadButton = (JButton)getFinder().find(panel, loadJButtonMatcher);
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(loadButton);       
        {
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
            
            assertEquals(testDirNoread.getPath() + " permission denied", messageLabel.getText());

            clickCancel(dialog);

            verifyErrorDialogMessage(testDirNoread.getPath() + " permission denied", dialog);
        }
        
        bTester.actionClick(loadButton);       
        {
            JDialog dialog = (JDialog)getFinder().find(new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JDialog
                        && true;
                }
            });
            JButton ignoreButton = (JButton)getFinder().find(dialog, new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JButton
                        && "Ignore".equals(((JButton)c).getText());
                }
            });
            
            bTester.actionClick(ignoreButton);

            verifyErrorDialogMessage("there were ignored problems loading " + testDirNoread.getPath(), dialog);
            
        }        
    }
    
    @Test
    public void testCompareExceptionCatch() throws Exception {
        CompareController controller = new CompareController();
        assertNull(controller.getRootNode(NodeRole.NEW_ROOT));
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        controller.setNewRoot(testDir4.getPath());
        controller.setOldRoot(testDir4.getPath());
        clickCompare(panel);
        verifyErrorDialogMessage(tfileNoread.getPath(), null, false);
    }

    
    @Test
    public void testCompareErrorHandled() throws Exception {
        CompareController controller = new CompareController();
        DialogErrorHandler handler = new DialogErrorHandler();
        controller.setErrorHandler( handler );

        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);

        showFrame(panel);

        controller.setNewRoot(testDir4.getPath());
        controller.setOldRoot(testDir4.getPath());

        clickCompare(panel);
        {
            JDialog dialog = (JDialog)getFinder().find(new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JDialog
                        && true;
                }
            });
            
            JButton ignoreAllButton = (JButton)getFinder().find(dialog, new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JButton
                        && "Ignore All".equals(((JButton)c).getText());
                }
            });
            
            JButtonTester bTester = new JButtonTester(); 
            bTester.actionClick(ignoreAllButton);

            verifyErrorDialogMessage("there were ignored problems comparing some files", dialog);            
        }        

    }

    @Test
    public void testPanelFolder() throws Exception {

        CompareController controller = new CompareController();
        assertNull(controller.getRootNode(NodeRole.NEW_ROOT));

        CmpTreePanel panel = new CmpTreePanel(NodeRole.NEW_ROOT, controller);
        Frame frame = showFrame(panel);

        JTextField pathField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        String path = pathField.getText();
        assertEquals("", path);

        JButton folderButton = (JButton)getFinder().find(frame, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JButton
                    && "".equals(((JButton)c).getText());
            }
        });
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(folderButton);       
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            File cwd = new File( System.getProperty("user.dir") );
            assertEquals(cwd, chooser.getCurrentDirectory());
            chooser.cancelSelection();
        }

        pathField.setText(testDir1.getPath());
        bTester.actionClick(folderButton);       
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            assertEquals(testDir1, chooser.getCurrentDirectory());
            chooser.cancelSelection();
        }        
        
        pathField.setText(tfile1.getPath());
        bTester.actionClick(folderButton);       
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            
    
            assertEquals(testDir1, chooser.getCurrentDirectory());

            chooser.cancelSelection();

        }        

        pathField.setText("noexist");
        bTester.actionClick(folderButton);       
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            
            File cwd = new File( System.getProperty("user.dir") );
            assertEquals(cwd, chooser.getCurrentDirectory());

            chooser.cancelSelection();

        }

        pathField.setText("noexist/file");
        bTester.actionClick(folderButton);       
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            
        
            File cwd = new File( System.getProperty("user.dir") );
            assertEquals(cwd, chooser.getCurrentDirectory());
            chooser.cancelSelection();
        }

        pathField.setText(testDir1.getPath());
        bTester.actionClick(folderButton);       
        {
            JFileChooser chooser = (JFileChooser)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JFileChooser;
                }
            });
            
            JTextField fileName = (JTextField)getFinder().find(chooser, new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JTextField                        ;
                }
            });
            fileName.setText(tfile1.getName());
            clickSelect(chooser);
            
            assertEquals( tfile1.getPath(), pathField.getText() );
        }        
        
    }
    
    private void popupClickItem(int itemIndex) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JPopupMenu;
            }
        });
        MenuElement[] mElem = pMenu.getSubElements();
        assertTrue(((JMenuItem)mElem[0]).isEnabled());
        JMenuItemTester miTester = new JMenuItemTester();
        miTester.actionClick((JMenuItem)mElem[itemIndex]);        
    }
    
    @Test
    public void testDiffAction() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File d1 = helper.createTestDir("d1");
        File d2 = helper.createTestDir("d2");
        Date now = new Date();
        MockFileNode mfText1 = new MockFileNode("text1", "test1", now);
        MockFileNode mfText2 = new MockFileNode("text1", "test2", now);
        CompareController controller = new CompareController();
        
        controller.setOldRoot(d1.getPath());
        controller.setNewRoot(d2.getPath());
        DirNode d1node = controller.getOldCompareRootNode();
        d1node.addLeaf(mfText1);
        DirNode d2node = controller.getNewCompareRootNode();
        d2node.addLeaf(mfText2);
        controller.compare();
        
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        
        DirNode root = (DirNode)model.getRoot();
        assertEquals(1, model.getChildCount(root));
        ContainerTester treeTester = new JTreeTester();        

        Rectangle folderRect = tree.getRowBounds( 0 );        
        Point folderCenter = new Point(folderRect.x + folderRect.width/2, folderRect.y + folderRect.height/2);
        {
            
            treeTester.actionClick(tree, new ComponentLocation(folderCenter) , MouseEvent.BUTTON3_MASK);
            JPopupMenu pMenu = (JPopupMenu)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JPopupMenu;
                }
            });
            MenuElement[] mElem = pMenu.getSubElements();
            assertEquals(2, mElem.length);
            assertEquals("Diff", ((JMenuItem)mElem[0]).getText() );
            assertEquals("Properties", ((JMenuItem)mElem[1]).getText() );
            assertFalse(((JMenuItem)mElem[0]).isEnabled());
            JMenuItemTester miTester = new JMenuItemTester();
            miTester.actionKeyStroke(KeyEvent.VK_ESCAPE);  // TODO is this intruding into look and feel ?
        }

        Rectangle fileRect = tree.getRowBounds( 1 );        
        Point fileCenter = new Point(fileRect.x + fileRect.width/2, fileRect.y + fileRect.height/2);
        {
            
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            DiffFrame diffFrame = (DiffFrame)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof DiffFrame && c.isVisible();
                }
            });
            
            diffFrame.dispose();
        }
        {
            mfText1.setContentMethodCost(FileNode.COST_IMPOSSIBLE);
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("Cannot Diff, no content for " + mfText1.getName(), null);
            mfText1.setContentMethodCost(FileNode.COST_EASY);
        }
        {
            mfText2.setContentMethodCost(FileNode.COST_IMPOSSIBLE);
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("Cannot Diff, no content for " + mfText2.getName(), null);
            mfText2.setContentMethodCost(FileNode.COST_EASY);
        }

        {
            mfText1.setFileType(FileType.SPECIAL);
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("Cannot Diff, " + mfText1.getName() + " is not a regular file", null);
            mfText1.setFileType(FileType.REGFILE);
        }

    
    }

    @Test
    public void testDiffActionBinary() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File d1 = helper.createTestDir("d1");
        File d2 = helper.createTestDir("d2");
        Date now = new Date();
        byte[] binaryContent1 = {0, 1, 2, 3, 4, 5, 6};
        byte[] binaryContent2 = {0, 1, 2, 3, 4, 5, 7};
        MockFileNode mfBinary1 = new MockFileNode("binary", binaryContent1, now);
        MockFileNode mfBinary2 = new MockFileNode("binary", binaryContent2, now);
        CompareController controller = new CompareController();
        
        controller.setOldRoot(d1.getPath());
        controller.setNewRoot(d2.getPath());
        DirNode d1node = controller.getOldCompareRootNode();
        d1node.addLeaf(mfBinary1);
        DirNode d2node = controller.getNewCompareRootNode();
        d2node.addLeaf(mfBinary2);
        controller.compare();
        
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        
        DirNode root = (DirNode)model.getRoot();
        assertEquals(1, model.getChildCount(root));
        ContainerTester treeTester = new JTreeTester();        

        Rectangle fileRect = tree.getRowBounds( 1 );        
        Point fileCenter = new Point(fileRect.x + fileRect.width/2, fileRect.y + fileRect.height/2);
        {
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("Cannot diff, " + mfBinary1.getName() + " is not text", null);
        }
        
    }
    
    @Test
    public void testDiffActionEmpty() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File d1 = helper.createTestDir("d1");
        File d2 = helper.createTestDir("d2");
        Date now = new Date();
        MockFileNode mfText1 = new MockFileNode("text1", "test1", now);
        MockFileNode mfText2 = new MockFileNode("text1", "", now);
        CompareController controller = new CompareController();
        
        controller.setOldRoot(d1.getPath());
        controller.setNewRoot(d2.getPath());
        DirNode d1node = controller.getOldCompareRootNode();
        d1node.addLeaf(mfText1);
        DirNode d2node = controller.getNewCompareRootNode();
        d2node.addLeaf(mfText2);
        controller.compare();
        
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        
        DirNode root = (DirNode)model.getRoot();
        assertEquals(1, model.getChildCount(root));
        ContainerTester treeTester = new JTreeTester();        

        Rectangle fileRect = tree.getRowBounds( 1 );        
        Point fileCenter = new Point(fileRect.x + fileRect.width/2, fileRect.y + fileRect.height/2);
        {
            
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            DiffFrame diffFrame = (DiffFrame)getFinder().find( new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof DiffFrame && c.isVisible();
                }
            });
            
            diffFrame.dispose();
        }
    }
    
    @Test
    public void testDiffActionPopulateError() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File d1 = helper.createTestDir("d1");
        File d2 = helper.createTestDir("d2");
        Date now = new Date();
        MockFileNode mfText1 = new MockFileNode("text1", "test1", now);
        MockFileNode mfText2 = new MockFileNode("text1", "test2", now);
        CompareController controller = new CompareController();
        
        controller.setOldRoot(d1.getPath());
        controller.setNewRoot(d2.getPath());
        DirNode d1node = controller.getOldCompareRootNode();
        d1node.addLeaf(mfText1);
        DirNode d2node = controller.getNewCompareRootNode();
        d2node.addLeaf(mfText2);
        controller.compare();
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        
        DirNode root = (DirNode)model.getRoot();
        assertEquals(1, model.getChildCount(root));
        ContainerTester treeTester = new JTreeTester();        

        Rectangle fileRect = tree.getRowBounds( 1 );        
        Point fileCenter = new Point(fileRect.x + fileRect.width/2, fileRect.y + fileRect.height/2);
        {
            mfText1.setName("rename1");
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("Unexpected error populating path to old file", null);
            mfText1.setName("text1");
        }
        
        {
            mfText2.setName("rename2");
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("Unexpected error populating path to new file", null);
            mfText2.setName("text1");
        }
        
    }
    
    @Test
    public void testDiffActionIOException() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File d1 = helper.createTestDir("d1");
        File d2 = helper.createTestDir("d2");
        Date now = new Date();
        ReadFailMockFileNode mfText1 = new ReadFailMockFileNode("text1", "test1", now, 0);
        mfText1.disableFailure();
        ReadFailMockFileNode mfText2 = new ReadFailMockFileNode("text1", "test2", now, 0);
        mfText2.disableFailure();
        CompareController controller = new CompareController();
        
        controller.setOldRoot(d1.getPath());
        controller.setNewRoot(d2.getPath());
        DirNode d1node = controller.getOldCompareRootNode();
        d1node.addLeaf(mfText1);
        DirNode d2node = controller.getNewCompareRootNode();
        d2node.addLeaf(mfText2);
        controller.compare();
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        
        DirNode root = (DirNode)model.getRoot();
        assertEquals(1, model.getChildCount(root));
        ContainerTester treeTester = new JTreeTester();        

        Rectangle fileRect = tree.getRowBounds( 1 );        
        Point fileCenter = new Point(fileRect.x + fileRect.width/2, fileRect.y + fileRect.height/2);
        {
            mfText1.enableFailure();
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("artificial test IO exception", null);
            mfText1.disableFailure();
        }
        
        mfText1.isText(); // side effect
        mfText2.isText();

        {
            mfText1.enableFailure();
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("artificial test IO exception", null);
            mfText1.disableFailure();
        }
        
        {
            mfText2.enableFailure();
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            popupClickItem(0);
            verifyErrorDialogMessage("artificial test IO exception", null);
            mfText2.disableFailure();
        }
        
    }
    
    @Test
    public void testNoPopup() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File d1 = helper.createTestDir("d1");
        File d2 = helper.createTestDir("d2");
        Date now = new Date();
        MockFileNode mfText1 = new MockFileNode("text1", "test1", now);
        MockFileNode mfText2 = new MockFileNode("text1", "test2", now);
        CompareController controller = new CompareController();
        
        controller.setOldRoot(d1.getPath());
        controller.setNewRoot(d2.getPath());
        DirNode d1node = controller.getOldCompareRootNode();
        d1node.addLeaf(mfText1);
        DirNode d2node = controller.getNewCompareRootNode();
        d2node.addLeaf(mfText2);
        controller.compare();
        
        CmpTreePanel panel = new CmpTreePanel(NodeRole.CMP_ROOT, controller);
        showFrame(panel);

        JTree tree = (JTree)getFinder().find(panel, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JTree;
            }
        });
        TreeModel model = tree.getModel();
        
        DirNode root = (DirNode)model.getRoot();
        assertEquals(1, model.getChildCount(root));
        ContainerTester treeTester = new JTreeTester();        

        Rectangle folderRect = tree.getRowBounds( 0 );        
        Point folderCenter = new Point(folderRect.x + folderRect.width/2, folderRect.y + folderRect.height/2);
        {
            
            treeTester.actionClick(tree, new ComponentLocation(folderCenter) , MouseEvent.BUTTON1_MASK);
            try {
                getFinder().find( new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JPopupMenu;
                    }
                });
                fail("popup menu should not appear for button 1");
            } catch ( ComponentNotFoundException cnfe) {
                // this is supposed to happen
            }
        }

        Rectangle fileRect = tree.getRowBounds( 1 );        
        Point fileCenter = new Point(fileRect.x + 3*fileRect.width/2, fileRect.y + 3*fileRect.height/2);
        {
            treeTester.actionClick(tree, new ComponentLocation(fileCenter) , MouseEvent.BUTTON3_MASK);
            try {
                getFinder().find( new Matcher() {
                    public boolean matches(Component c) {
                        return c instanceof JPopupMenu;
                    }
                });
                fail("popup menu should not appear if not on file");
            } catch ( ComponentNotFoundException cnfe) {
                // this is supposed to happen
            }
        }
    
    }

}
