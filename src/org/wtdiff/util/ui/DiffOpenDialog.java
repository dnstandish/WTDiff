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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.wtdiff.util.io.FileUtil;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FileInputStreamSource;

public class DiffOpenDialog extends JDialog implements ActionListener {

    private static int MIN_FILENAME_TEXT_FIELD_LENGTH = 20;
    private DiffController controller;
    private String oldFileName;
    private String newFileName;
    private JButton oldFolderButton; 
    private JButton newFolderButton; 
    private JButton okButton;
    private JButton cancelButton;
    private JTextField oldJText; 
    private JTextField newJText;
    
    public DiffOpenDialog(Frame parent, DiffController diffController) {
        super(parent, Messages.getString("DiffOpenDialog.dialog_title"), true);
        controller = diffController;
        oldFileName = controller.getOldSourceName();
        newFileName = controller.getNewSourceName();
        JLabel oldLabel = new JLabel(Messages.getString("DiffOpenDialog.old_label"));
        oldFolderButton = new JButton( javax.swing.plaf.metal.MetalIconFactory.getTreeFolderIcon() );
        oldFolderButton.addActionListener(this);
        oldJText = new JTextField(oldFileName);
        if ( oldJText.getColumns() < MIN_FILENAME_TEXT_FIELD_LENGTH) {
            oldJText.setColumns(MIN_FILENAME_TEXT_FIELD_LENGTH);
        }
        oldJText.setEditable(true);
        Box oldBox = Box.createHorizontalBox();
        oldBox.add(Box.createHorizontalStrut(10));
        oldBox.add(oldLabel);
        oldBox.add(Box.createHorizontalStrut(5));
        oldBox.add(oldFolderButton);
        oldBox.add(Box.createHorizontalStrut(5));
        oldBox.add(oldJText);
        oldBox.add( Box.createHorizontalGlue() );
        oldBox.add(Box.createHorizontalStrut(10));

        JLabel newLabel = new JLabel(Messages.getString("DiffOpenDialog.new_label"));
        newFolderButton = new JButton( javax.swing.plaf.metal.MetalIconFactory.getTreeFolderIcon() );
        newFolderButton.addActionListener(this);
        newJText = new JTextField(newFileName);
        newJText.setEditable(true);
        Box newBox = Box.createHorizontalBox();
        newBox.add(Box.createHorizontalStrut(10));
        newBox.add(newLabel);
        newBox.add(Box.createHorizontalStrut(5));
        newBox.add(newFolderButton);
        newBox.add(Box.createHorizontalStrut(5));
        newBox.add(newJText);
        newBox.add( Box.createHorizontalGlue() );
        newBox.add(Box.createHorizontalStrut(10));
        
        Box mainBox = Box.createVerticalBox();
        mainBox.add(oldBox);
        mainBox.add(newBox);

        add(mainBox, "North");
        // ok button
        okButton = new JButton(
            Messages.getString("DiffOpenDialog.button_ok")
            );
        okButton.addActionListener(this);
        cancelButton = new JButton(
            Messages.getString("DiffOpenDialog.button_cancel")
            );
        cancelButton.addActionListener(this);
        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add( Box.createHorizontalGlue() );
        buttonBox.add( okButton );
        buttonBox.add(Box.createHorizontalStrut(5));
        buttonBox.add( cancelButton );
        buttonBox.add( Box.createHorizontalGlue() );
        add( buttonBox, "South" );

    }
    @Override
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();
        if ( object == okButton ) {
            if ( checkFile( oldJText.getText() ) && checkFile( newJText.getText() ) ) {
                try {
                    FileInputStreamSource oldSource = new FileInputStreamSource( new File (oldJText.getText()) );
                    FileInputStreamSource newSource = new FileInputStreamSource( new File (newJText.getText()) );
                    controller.setOldSource( oldSource ); 
                    controller.setNewSource( newSource ); 
                    setVisible( false );
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(
                        null, 
                        ioe.getMessage(), 
                        Messages.getString("DiffOpenDialog.title_error"),  //$NON-NLS-1$
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
        else if ( object == cancelButton ) {
            setVisible( false );
        }
        else if ( object == oldFolderButton ) {
            chooseFile(oldJText, DiffController.SourceType.OLD);
        }
        else if ( object == newFolderButton ) {
            chooseFile(newJText, DiffController.SourceType.NEW);
        }
    }

    private boolean checkFile(String path) {
        if ( path == null || path.equals("") ) {
            JOptionPane.showMessageDialog(
                null, 
                Messages.getString("DiffOpenDialog.empty_name"),  //$NON-NLS-1$
                Messages.getString("DiffOpenDialog.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        Path f = Paths.get(path);
        if ( ! f.toFile().exists() ) { // nio Files.exists(path) returns false in Windows7 if missing read permission
            JOptionPane.showMessageDialog(
                null, 
                MessageFormat.format(
                    Messages.getString("DiffOpenDialog.no_exist"),  //$NON-NLS-1$
                    path
                ),
                Messages.getString("DiffOpenDialog.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        if ( ! Files.isRegularFile(f) ) {
            JOptionPane.showMessageDialog(
                null, 
                MessageFormat.format(
                    Messages.getString("DiffOpenDialog.not_reg_file"),  //$NON-NLS-1$
                    path
                ),
                Messages.getString("DiffOpenDialog.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;            
        }
        
        if ( ! Files.isReadable(f) ) {
            JOptionPane.showMessageDialog(
                null, 
                MessageFormat.format(
                    Messages.getString("DiffOpenDialog.no_read"),  //$NON-NLS-1$
                    path
                ),
                Messages.getString("DiffOpenDialog.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
            return false;            
        }
        
        return true;
    }
    
    private void chooseFile( JTextField path , DiffController.SourceType type ) {
        String pathString  = FileUtil.bestExistingDirFromString(path.getText() , ".");
//        String pathString = "."; //$NON-NLS-1$
//        if ( path.getText() != null && path.getText().length() > 0 ) {
//            String fileOrDir = path.getText();
//            File pathFile = new File(fileOrDir);
//            if ( ! pathFile.isDirectory() ) {
//                String parent = pathFile.getParent();
//                if ( parent != null ) {
//                    File parentFile = new File(parent);
//                    if ( parentFile.exists() ) {
//                        pathString = parent;
//                    }
//                }
//            } else if ( pathFile.exists() ) { 
//                pathString = pathFile.getPath();
//            }
//        }
        JFileChooser c = new JFileChooser(new File(pathString));
        c.setFileSelectionMode(JFileChooser.FILES_ONLY);
        c.setDialogTitle( 
            type == DiffController.SourceType.OLD ? 
                Messages.getString("DiffOpenDialog.title_select_old"): 
                    Messages.getString("DiffOpenDialog.title_select_new") 
        ); //$NON-NLS-1$ //$NON-NLS-2$
        int result = c.showDialog(this, Messages.getString("DiffOpenDialog.file_select_approve_button")); //$NON-NLS-1$
        if ( result == JFileChooser.APPROVE_OPTION ) {
            path.setText( c.getSelectedFile().toString() );
        }
    }
}
