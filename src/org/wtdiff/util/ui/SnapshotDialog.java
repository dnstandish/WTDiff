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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

import org.wtdiff.util.*;
import org.wtdiff.util.FileNode.FileType;
import org.wtdiff.util.CompareController.NodeRole;
import org.wtdiff.util.xml.DirNodeXMLStreamWriter;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

public class SnapshotDialog extends JDialog implements ActionListener {

//    private static final long serialVersionUID = 8556872013089611059L;
    
    private JButton saveButton;
    private JButton cancelButton;
    private JTextArea userCommentArea;
    private JCheckBox crcCheckBox;
    private JCheckBox md5CheckBox;
    private JButton folderButton;
    private JTextField saveFileTextField;
    private DirNode dirNode;
    private String pathString;
    
    public SnapshotDialog(Frame parent, TreePath path, Node node) {
        super( parent, 
            MessageFormat.format(
                Messages.getString( "SnapshotDialog.title" ), node.getName()
            )
        );
        pathString = pathToString(path);
        setLayout(new BorderLayout());
        
        Box mainBox = Box.createVerticalBox();
        mainBox.add(createLabledComponent(
            Messages.getString( "SnapshotDialog.label_path" ),
            pathToString(path)));
//        mainBox.add(createLabledComponent(
//            Messages.getString( "NodePropertiesDialog.label_name" ),
//            node.getName()));
        
        if ( node instanceof DirNode ) {
            dirNode = (DirNode)node;
        }
        else if ( node instanceof FileNode ) {
            FileNode fn = (FileNode)node;
            dirNode = new DirNode(fn);
        }
        crcCheckBox = new JCheckBox("CRC32");
        md5CheckBox = new JCheckBox("MD5");
        userCommentArea = new JTextArea(4, 50);
        userCommentArea.setLineWrap( true );
        userCommentArea.setWrapStyleWord(true);
        userCommentArea.setMaximumSize(userCommentArea.getPreferredSize());
        JScrollPane commentPane = new JScrollPane(userCommentArea);
        Box commentBox = createLabledComponent(Messages.getString("SnapshotDialog.label_comment"), commentPane);
        mainBox.add(crcCheckBox);
        mainBox.add(md5CheckBox);
        mainBox.add(commentBox);

        Box saveBox = Box.createHorizontalBox();
        JLabel fileLabel = new JLabel(Messages.getString("SnapshotDialog.label_save_file"));
        saveFileTextField = new JTextField(pathToFileName(path));
        folderButton = new JButton( javax.swing.plaf.metal.MetalIconFactory.getTreeFolderIcon() );
        folderButton.addActionListener(this);
        saveBox.add(fileLabel);
        saveBox.add(saveFileTextField);
        saveBox.add(folderButton);
        mainBox.add(saveBox);
        // ok button
        Box buttonBox = Box.createHorizontalBox();
        saveButton = new JButton(
            Messages.getString("SnapshotDialog.button_save")
            );
        saveButton.addActionListener(this);
        buttonBox.add(saveButton);
//        add( saveButton, "South" );
        cancelButton = new JButton(
            Messages.getString("SnapshotDialog.button_cancel")
            );
        cancelButton.addActionListener(this);
        buttonBox.add(cancelButton);
        mainBox.add( buttonBox );
        add(mainBox, "North");
        
    }

    private String pathToString(TreePath path) { //TODO DRY code duplication
        Object[] oPath = path.getPath();
        File full = null;
        for( Object part : oPath ) {            
            String s = ((Node)part).getName();
            if ( full == null || full.getPath().length() == 0 ) {
                String root = ((Node)part).getRoot();
                if ( root == null )
                    full = new File(s);
                else
                    full = new File(root);
            }
            else {
                full = new File(full, s);
            }            
        }        
        return full.getPath();
    }
    
    private String pathToFileName(TreePath path) {
        Object[] oPath = path.getPath();
        String full = null;
        for( Object part : oPath ) {
            // the characters slash, backslash, and colon may have meaning in the 
            // context of the file system as path separators, and thus not valid
            // as part of a file name.   Replace with  underscores.
            String s = ((Node)part).getName().replace('/', '_').replace('\\', '_').replace(':', '_');
            //String s = ((Node)part).getName().replace("/:\\", "____");
            if ( full == null ) {
                if ( ! "".equals(s) ) {
                    full = s;
                }
            }
            else {
                full = full + "_" + s;
            }
        }
        if ( full == null || "".equals(full) ) {
            full = "snapshot";
        }
        return full + ".xml";
    }
    
    
    private Box createLabledComponent(String label, String value ) {
        JLabel jl = new JLabel(label);
        JTextField jt = new JTextField(value);
        jt.setEditable(false);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalStrut(10));
        box.add(jl);
        box.add(Box.createHorizontalStrut(5));
        box.add(jt);
        box.add( Box.createHorizontalGlue() );
        box.add(Box.createHorizontalStrut(10));
        return box;
    }

    private Box createLabledComponent(String label, Component comp ) {
        JLabel jl = new JLabel(label);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalStrut(10));
        box.add(jl);
        box.add(Box.createHorizontalStrut(5));
        box.add(comp);
        box.add( Box.createHorizontalGlue() );
        box.add(Box.createHorizontalStrut(10));
        return box;
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        Object object = event.getSource();
        if ( object == saveButton ) {
            String saveAs = saveFileTextField.getText();
            if ( saveAs.length() == 0 ) {
                JOptionPane.showMessageDialog(
                    this, 
                    Messages.getString("SnapshotDialog.file_not_set"),
                    Messages.getString("SnapshotDialog.title_save_error"),  //$NON-NLS-1$
                    JOptionPane.ERROR_MESSAGE
                );
                
            } else {
                boolean success = false;
                try {
                    success = writeSnapshot(saveAs);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                        this, 
                        e.getMessage(),
                        Messages.getString("SnapshotDialog.title_save_error"),  //$NON-NLS-1$
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                if ( success ) {
                    JOptionPane.showMessageDialog(
                        null, 
                        MessageFormat.format(
                            Messages.getString("SnapshotDialog.save_success"),  //$NON-NLS-1$
                            saveFileTextField.getText()
                        ),
                        Messages.getString("SnapshotDialog.title_success"),  //$NON-NLS-1$
                        JOptionPane.INFORMATION_MESSAGE
                    );
    
                    setVisible( false );
                }
            }
        }
        else if ( object == cancelButton ) {
            setVisible( false );
        }
        else if ( object == folderButton ) {
            JFileChooser c = new JFileChooser();
            String saveFileText = saveFileTextField.getText();
            if ( saveFileText != null && !"".equals(saveFileText) ) {
                File saveFile = new File(saveFileText);
                File saveDir = saveFile.getAbsoluteFile().getParentFile();
                c.setCurrentDirectory(saveDir);
                c.setSelectedFile(saveFile);
            }
            c.setFileSelectionMode(JFileChooser.FILES_ONLY);
            c.setDialogTitle( 
                    Messages.getString("SnapshotDialog.title_select") 
            ); //$NON-NLS-1$ //$NON-NLS-2$
            int result = c.showDialog(this, Messages.getString("SnapshotDialog.file_select_button")); //$NON-NLS-1$
            if ( result == JFileChooser.APPROVE_OPTION ) {
                saveFileTextField.setText( c.getSelectedFile().toString() );
            }
        }
            
    }

    private boolean writeSnapshot(String saveAs) throws Exception {
        File saveFile = new File(saveFileTextField.getText());
        ArrayList<String> digests = new ArrayList<>();

        if ( crcCheckBox.isSelected() ) 
            digests.add("CRC32");
        if ( md5CheckBox.isSelected() )
            digests.add("MD5");
        
        if ( digests.size() == 0  ) {
            int choice = JOptionPane.showConfirmDialog(
                null, 
                Messages.getString("SnapshotDialog.no_digests"),  //$NON-NLS-1$
                Messages.getString("SnapshotDialog.title_save_error"),  //$NON-NLS-1$
                JOptionPane.OK_CANCEL_OPTION
            );
            if ( choice != JOptionPane.OK_OPTION ) {
                return false;
            }
            
        }
        try ( FileOutputStream out = new FileOutputStream(saveFile) ) {
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(out, digests);
            if ( dirNode.getRoot() == null ) {
                writer.writeDirNodeSnapShot(dirNode, userCommentArea.getText().trim(), pathString);                
            } else {
                writer.writeDirNodeSnapShot(dirNode, userCommentArea.getText().trim());
            }
        }
        return true;
    }
}
