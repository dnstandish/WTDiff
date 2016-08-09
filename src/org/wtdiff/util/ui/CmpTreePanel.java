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

import javax.swing.*;
import java.awt.BorderLayout;
import javax.swing.Box;
import java.awt.event.*;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.*;

import org.wtdiff.util.*;
import org.wtdiff.util.FileNode.FileType;
import org.wtdiff.util.CompareController.NodeRole;
import org.wtdiff.util.io.FileUtil;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.InputStreamSource;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import java.lang.ref.WeakReference;

/**
 * Panel for dirnode/leaf based tree used for specifying/loading old or new tree, and 
 * compare tree.     
 * 
 * @author davidst
 *
 */
public class CmpTreePanel extends JPanel implements RootNodeListener, ActionListener, MouseListener, PopupMenuListener {

    private static final long serialVersionUID = -7918207024051540333L;
    
    /**
     * what are we? the old dir ? the new dir? or the comparison result dir?
     */
    private CompareController.NodeRole type;
    /**
     * Controller that is the heart of the application
     */
    private CompareController controller;
    
    private JButton actionButton;
    private JButton folderButton = null;
    private JTextField path;
    private JScrollPane scrollPane;
    private JTree tree;
    private JMenuItem diffItem;
    private JMenuItem rootItem;
    private JMenuItem unrootItem;
    private JMenuItem snapshotItem;
    private JMenuItem propertiesItem;
    private Object actionObject;
    private TreePath actionPath;
    private JPopupMenu popup;
    private NodeTreeCellRenderer treeCellRenderer;

    private WeakReference<Object> forcedRoot = null;
    //private WeakReference<TreePath> forcedPath = null;

    private CompareTreeModel model;
    /**
     * Constructor needs the role of this tree, and the controller
     * 
     * @param treeType 
     * @param compareController
     */
    public CmpTreePanel(NodeRole treeType, CompareController compareController) {
        type = treeType;
        controller = compareController;
        Box horizontalBox = Box.createHorizontalBox();
        setLayout(new BorderLayout());
        String root;
        DirNode rootNode;

        controller.addRootNodeListener(treeType, this);
        
        rootNode = compareController.getRootNode(treeType);
        if ( rootNode == null ) {
            root = compareController.getRoot(treeType);
            if (root == null )
                root = ""; //$NON-NLS-1$
        } else {
            if ( treeType == NodeRole.CMP_ROOT ) {
                root = compareController.getRoot(treeType);
            } else {
                root = rootNode.getRoot();
            }
        }
//// coberta 2.0.3 has problems with conditional version of this code
//        actionButton = new JButton( 
//            treeType == NodeRole.CMP_ROOT ? 
//                Messages.getString("CmpTreePanel.button_compare"): 
//                    Messages.getString("CmpTreePanel.button_load") 
//        ); //$NON-NLS-1$ //$NON-NLS-2$
        if (treeType == NodeRole.CMP_ROOT )
            actionButton = new JButton( Messages.getString("CmpTreePanel.button_compare") ); //$NON-NLS-1$ //$NON-NLS-2$
        else
            actionButton = new JButton( Messages.getString("CmpTreePanel.button_load") ); //$NON-NLS-1$ //$NON-NLS-2$
            
        actionButton.addActionListener(this);
        
        path = new JTextField(root);
        if ( treeType == NodeRole.CMP_ROOT )
            path.setEditable(false);

        if ( rootNode == null)
            model = createTreeModel(root);
        else
            model = createTreeModel(rootNode);
        
        treeCellRenderer = new NodeTreeCellRenderer();
        tree = new JTree(model);
        tree.setCellRenderer(treeCellRenderer);
        tree.addMouseListener(this);
        scrollPane = new JScrollPane(tree);
        horizontalBox.add(actionButton);
        if ( treeType != NodeRole.CMP_ROOT ) {
            folderButton = new JButton( javax.swing.plaf.metal.MetalIconFactory.getTreeFolderIcon() );
            folderButton.addActionListener(this);
            horizontalBox.add(folderButton);
        }
        horizontalBox.add(path);
        this.add(horizontalBox, "North"); //$NON-NLS-1$
        this.add(scrollPane, "Center"); //$NON-NLS-1$
    }

    /**
     * create tree model rooted on empty dirnode with given name
     *  
     * @param name
     * @return
     */
    private CompareTreeModel createTreeModel(String name) {
        return createTreeModel( new DirNode(name, new ArrayList<Leaf>(), new ArrayList<DirNode>() ) );
    }
    
    /**
     * create tree model rooted on given DirNode
     * 
     * @param d
     * @return
     */
    private CompareTreeModel createTreeModel(DirNode d) {
        return new CompareTreeModel(d);
    }
    
    /* (non-Javadoc)
     * @see org.wtdiff.util.RootNodeListener#rootNodeChanged(org.wtdiff.util.DirNode)
     */
    public void rootNodeChanged(DirNode rootNode) {
        model = createTreeModel(rootNode);
        treeCellRenderer.clearEmphasizedNode();
        tree.setModel( model );
        if ( type == NodeRole.CMP_ROOT ) {
            path.setText( controller.getCompareRoot() );
        } else {
            forcedRoot = null;
            //forcedPath = null;
            treeCellRenderer.clearEmphasizedNode();
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();
        
        if ( object == actionButton ) {
            try {
                if ( type != NodeRole.CMP_ROOT ) {                
                    String pathName =  path.getText();
                    if ( pathName == null || pathName.equals("") ) { //$NON-NLS-1$
                        JOptionPane.showMessageDialog(
                            null, 
                            Messages.getString("CmpTreePanel.message_path_not_set"),  //$NON-NLS-1$
                            Messages.getString("CmpTreePanel.title_path_not_set"),  //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    File pathFile = new File(pathName);
                    if ( ! pathFile.exists() ) {
                        JOptionPane.showMessageDialog(
                            null, 
                            Messages.getString("CmpTreePanel.message_path_no_exist"),  //$NON-NLS-1$
                            Messages.getString("CmpTreePanel.title_path_no_exist"),  //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE
                            );
                        return;
                    }
                    if ( type == NodeRole.OLD_ROOT )
                        controller.setOldRoot(pathName); //TODO clear tree model to reduce memory use
                    else
                        controller.setNewRoot(pathName); //TODO clear tree model to reduce memory use
                    
                    ErrorHandler handler = controller.getErrorHandler();
                    if ( handler.encounteredError() ) {
                        JOptionPane.showMessageDialog(
                            null, 
                            MessageFormat.format(
                                Messages.getString("CmpTreePanel.message_problem_loading"),  //$NON-NLS-1$
                                pathName
                            ),
                            Messages.getString("CmpTreePanel.title_problem_loading"),  //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE
                        );
                        handler.reset();
                    }
                } else {
                    controller.compare();
                    ErrorHandler handler = controller.getErrorHandler();
                    if ( handler.encounteredError() ) {
                        JOptionPane.showMessageDialog(
                            null, 
                            Messages.getString("CmpTreePanel.message_problem_comparing"),  //$NON-NLS-1$
                            Messages.getString("CmpTreePanel.title_problem_comparing"),  //$NON-NLS-1$
                            JOptionPane.ERROR_MESSAGE
                        );
                        handler.reset();
                    }
                }
            } catch ( IOException ioe ) {
                JOptionPane.showMessageDialog(
                    null, 
                    ioe.getMessage(), 
                    Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE
                );
            }            
        } else if ( object == folderButton ) {
            String pathString  = FileUtil.bestExistingDirFromString(path.getText() , ".");
//            String pathString = "."; //$NON-NLS-1$
//            if ( path.getText() != null && path.getText().length() > 0 ) {
//                String fileOrDir = path.getText();
//                File pathFile = new File(fileOrDir);
//                if ( ! pathFile.isDirectory() ) {
//                    String parent = pathFile.getParent();
//                    if ( parent != null ) {
//                        File parentFile = new File(parent);
//                        if ( parentFile.exists() ) {
//                            pathString = parent;
//                        }
//                    }
//                } else if ( pathFile.exists() ) { 
//                    pathString = pathFile.getPath();
//                }
//            }
            JFileChooser c = new JFileChooser(new File(pathString));
            c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            c.setDialogTitle( 
                type == NodeRole.OLD_ROOT ? 
                    Messages.getString("CmpTreePanel.title_select_old"): 
                        Messages.getString("CmpTreePanel.title_select_new") 
            ); //$NON-NLS-1$ //$NON-NLS-2$
            int result = c.showDialog(this, Messages.getString("CmpTreePanel.file_select_approve_button")); //$NON-NLS-1$
            if ( result == JFileChooser.APPROVE_OPTION ) {
                path.setText( c.getSelectedFile().toString() );
            }
            
        } else if ( object == rootItem ) { 
            controller.forceCompareRoot(type, treePathToNames(actionPath));            
            forcedRoot = new WeakReference<>(actionObject);
            //forcedPath = new WeakReference<>(actionPath);
            treeCellRenderer.setEmphasizedNode(forcedRoot);
            model.presentationChange(this, actionPath);
            //tree.repaint();
        } else if ( object == unrootItem ) { 
            controller.unforceCompareRoot(type);            
            forcedRoot = null;
            //forcedPath = null;
            treeCellRenderer.clearEmphasizedNode();
            model.presentationChange(this, actionPath);
            //tree.repaint();
        } else if ( object == diffItem ) { 
            launchDiff(actionPath);
            
        } else if ( object == propertiesItem ) { 
            NodePropertiesDialog dialog = new NodePropertiesDialog(
                (JFrame)SwingUtilities.getRoot(this), 
                actionPath,
                (Node)actionObject
            );
            dialog.pack();
            dialog.setVisible(true);
        } else if ( object == snapshotItem ) { 
            SnapshotDialog dialog = new SnapshotDialog(
                (JFrame)SwingUtilities.getRoot(this), 
                actionPath,
                (Node)actionObject
            );
            dialog.pack();
            dialog.setVisible(true);
        }
        
    }

    private void launchDiff(TreePath comparePath) {
        DirNode oldRoot = controller.getOldCompareRootNode();        
        DirNode newRoot = controller.getNewCompareRootNode();        
        ArrayList<Node> oldNodes = new ArrayList<>(comparePath.getPathCount());
        ArrayList<Node> newNodes = new ArrayList<>(comparePath.getPathCount());
        List<String> oldNames = treePathToNamesExcludeRoot(comparePath, true);
        List<String> newNames = treePathToNamesExcludeRoot(comparePath, false);
        if ( ! oldRoot.populatePathByNames(oldNames, oldNodes) ) {
            JOptionPane.showMessageDialog(
                null, 
                Messages.getString("CmpTreePanel.unexpected_cannot_populate_old"), 
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if ( ! newRoot.populatePathByNames(newNames, newNodes) ) {
            JOptionPane.showMessageDialog(
                null, 
                Messages.getString("CmpTreePanel.unexpected_cannot_populate_new"), 
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        if ( ! checkCanDiff(oldNodes.get( oldNodes.size() - 1 )) || 
            ! checkCanDiff(newNodes.get( newNodes.size() - 1 )) ) {
            return;
        }
            
        final FileNode oldFile = (FileNode)oldNodes.get( oldNodes.size() - 1 );
        final FileNode newFile = (FileNode)newNodes.get( newNodes.size() - 1 );
            
        try {
        
            DiffController diffController = new DiffController();
            diffController.setOldSource( new InputStreamSource() {
                public InputStream getInputStream() throws IOException {
                    return oldFile.getInputStream();
                }
                public String getName() {
                    return oldFile.getName(); //TODO should be path
                }
                public long getTime() {
                    return oldFile.getTime();
                } } );
            diffController.setNewSource( new InputStreamSource() {
                public InputStream getInputStream() throws IOException {
                    return newFile.getInputStream();
                }
                public String getName() {
                    return newFile.getName(); //TODO should be path
                }
                public long getTime() {
                    return newFile.getTime();
                } } );
            DiffFrame df = new DiffFrame(false, diffController);
            df.setVisible(true);
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                null, 
                ioe.getMessage(), 
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private List<String> treePathToNamesExcludeRoot(TreePath path, boolean useName1) {
        ArrayList<String> names = new ArrayList<>(path.getPathCount());
        boolean isFirst = true;
        for( Object o : path.getPath() ) {
            if ( ! isFirst ) {
                names.add( 
                    useName1 ? ((ComparisonResult)o).getName1() :
                        ((ComparisonResult)o).getName2() 
                );
            }
            isFirst = false;
        }
        return names;
    }

    private List<String> treePathToNames(TreePath path) {
        ArrayList<String> names = new ArrayList<>(path.getPathCount());
        for( Object o : path.getPath() ) {
                names.add( ((Node)o).getName() ); 
        }
        return names;
    }

    private boolean checkCanDiff(Node node) {
        if ( !( node instanceof FileNode ) ) {
            JOptionPane.showMessageDialog(
                null, 
                MessageFormat.format(
                    Messages.getString("CmpTreePanel.diff_nregfile"),  //$NON-NLS-1$
                    node.getName()
                ),
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;            
        }
        FileNode fNode = (FileNode)node;

        if ( fNode.getFileType() != FileType.REGFILE ) {
            JOptionPane.showMessageDialog(
                null, 
                MessageFormat.format(
                    Messages.getString("CmpTreePanel.diff_nregfile"),  //$NON-NLS-1$
                    node.getName()
                ),
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;            
        }

        if ( !fNode.isContentAccessible() ) {
            JOptionPane.showMessageDialog(
                null, 
                MessageFormat.format(
                    Messages.getString("CmpTreePanel.diff_no_content"),  //$NON-NLS-1$
                    fNode.getName()
                ),
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;            
        }
        try {
            if ( !(fNode.isText() || fNode.getSize() == 0 ) ) {
                JOptionPane.showMessageDialog(
                    null, 
                    MessageFormat.format(
                        Messages.getString("CmpTreePanel.diff_ntext_or_empty"),  //$NON-NLS-1$
                        fNode.getName()
                    ),                        
                    Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE
                );
                return false;            
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                null, 
                ioe.getMessage(), 
                Messages.getString("CmpTreePanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return true;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if ( e.getButton() != MouseEvent.BUTTON3 )
            return;
        
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        actionPath = tree.getPathForLocation(e.getX(), e.getY());
        if(selRow != -1) {
            actionObject = actionPath.getLastPathComponent();
            popup = new JPopupMenu();
            if ( type == NodeRole.CMP_ROOT ) {
                boolean enabled = true; 
                if ( ! (actionObject instanceof LeafComparisonResult
                  && ((LeafComparisonResult)actionObject).haveBoth() ) ) {
                    enabled = false;
                }
                diffItem = new JMenuItem(
                    Messages.getString("CmpTreePanel.menu_diff") 
                );
                diffItem.setEnabled(enabled);
                popup.add(diffItem);
                diffItem.addActionListener(this);
            }
            else {
                if ( forcedRoot != null && actionObject == forcedRoot.get() ) {
                    unrootItem = new JMenuItem(
                        Messages.getString("CmpTreePanel.menu_unroot_here") 
                    );
                    popup.add(unrootItem);
                    unrootItem.addActionListener(this);                    
                } else {
                    rootItem = new JMenuItem(
                        Messages.getString("CmpTreePanel.menu_root_here") 
                    );
                    popup.add(rootItem);
                    rootItem.addActionListener(this);                    
                }
                snapshotItem = new JMenuItem(
                    Messages.getString("CmpTreePanel.menu_snapshot") 
                );
                popup.add(snapshotItem);
                snapshotItem.addActionListener(this);                    
            }
            propertiesItem = new JMenuItem(
                Messages.getString("CmpTreePanel.menu_properties") 
            );
            popup.add(propertiesItem);
            propertiesItem.addActionListener(this);
            popup.addPopupMenuListener(this);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // don't care
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // don't care
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // don't care
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // don't care
        
    }

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        // don't case
        
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // don't care
        
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
        if ( e.getSource() == popup ) {
            // clear references that tie into current compare tree
            actionPath = null;
            actionObject = null;
        }
    }
}
