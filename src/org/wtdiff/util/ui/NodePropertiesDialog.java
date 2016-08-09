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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import org.wtdiff.util.*;
import org.wtdiff.util.FileNode.FileType;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;

public class NodePropertiesDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 8556872013089611059L;
    
    private JButton okButton;
    
    public NodePropertiesDialog(Frame parent, TreePath path, Node node) {
        super( parent, node.getName() );
        setLayout(new BorderLayout());
        
        Box mainBox = Box.createVerticalBox();
        mainBox.add(createLabledComponent(
            Messages.getString( "NodePropertiesDialog.label_path" ),
            pathToString(path)));
        mainBox.add(createLabledComponent(
            Messages.getString( "NodePropertiesDialog.label_name" ),
            node.getName()));
        
        // type dependent stuff
        if ( node instanceof ComparisonResult ) {
            ComparisonResult cr = (ComparisonResult)node;
            String status = Messages.getString( "NodePropertiesDialog.status_changed" );
            if ( cr.isMissing1() ) {
                status = Messages.getString( "NodePropertiesDialog.status_new" );
            } else if ( cr.isMissing2() ) {
                status = Messages.getString( "NodePropertiesDialog.status_deleted" );                
            } else if ( cr.areSame() ) {
                status = Messages.getString( "NodePropertiesDialog.status_unchanged" );
            }
            mainBox.add( createLabledComponent(
                Messages.getString( "NodePropertiesDialog.label_status" ),
                status ) );
        }
        else if ( node instanceof DirNode ) {
            int nDirs = ((DirNode)node).getDirs().size();
            mainBox.add( createLabledComponent( 
                Messages.getString( "NodePropertiesDialog.label_subfolders" ),
                Integer.toString(nDirs) ) );
            int nLeaves = ((DirNode)node).getLeaves().size();
            mainBox.add( createLabledComponent( 
                Messages.getString( "NodePropertiesDialog.label_files" ),
                Integer.toString(nLeaves) ) );
        }
        else if ( node instanceof FileNode ) {
            FileNode fn = (FileNode)node;
            String type = null;
            switch ( fn.getFileType() ) {
            case REGFILE:
                type = Messages.getString( "NodePropertiesDialog.file_type_regular" );
                break;
            case SPECIAL :
                type = Messages.getString( "NodePropertiesDialog.file_type_special" );
                break;
            case SYMLINK :
                type = Messages.getString( "NodePropertiesDialog.file_type_symlink" );
                break;
            }
            mainBox.add( createLabledComponent(
                Messages.getString( "NodePropertiesDialog.label_type" ),
                type ) );
            if ( fn.getFileType() == FileType.SYMLINK )  {
                mainBox.add( createLabledComponent( 
                    Messages.getString( "NodePropertiesDialog.label_links_to" ),
                    fn.getLinkTo() ) );
            }
            long size = fn.getSize();
            mainBox.add( createLabledComponent( 
                Messages.getString("NodePropertiesDialog.label_size"),
                Long.toString(size) ) );
            long time = fn.getTime();
            String date = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG).format(new Date(time));
            mainBox.add( createLabledComponent( 
                Messages.getString("NodePropertiesDialog.label_time"),
                date ) );
            String isTextString = null; 
            try {
                if ( fn.isText() ) {
                    isTextString = Messages.getString( "NodePropertiesDialog.file_is_text_true" );
                } else {
                    isTextString = Messages.getString( "NodePropertiesDialog.file_is_text_false" );
                }
            } catch ( IOException ioe ) {
                isTextString = Messages.getString( "NodePropertiesDialog.file_is_text_unknown" );
            }
            mainBox.add( createLabledComponent( 
                Messages.getString("NodePropertiesDialog.label_is_text"),
                isTextString ) );
        }
        add(mainBox, "North");
        // ok button
        okButton = new JButton(
            Messages.getString("NodePropertiesDialog.button_close")
            );
        okButton.addActionListener(this);
        add( okButton, "South" );
        
        
    }

    private String pathToString(TreePath path) {
        Object[] oPath = path.getPath();
        File full = null;
        for( Object part : oPath ) {            
            String s = ((Node)part).getName();
            if ( full == null || full.getName().length() == 0 ) {
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

    @Override
    public void actionPerformed(ActionEvent event) {

        Object object = event.getSource();
        if ( object == okButton ) {
            setVisible( false );
        }
            
    }

}
