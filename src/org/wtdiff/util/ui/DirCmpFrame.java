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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.wtdiff.util.*;
import org.wtdiff.util.filter.CompositeNodeFilter;
import org.wtdiff.util.ui.filter.CompositeFilterDialog;

import javax.swing.*;


/**
 * GUI interface to application
 *  
 * @author davidst
 *
 */
public class DirCmpFrame extends JFrame implements ActionListener {
    private static final long serialVersionUID = 4596822751378775626L;

    private JMenuItem exitMenuItem;
    private JCheckBoxMenuItem isTextCompareCheckBox;
    private JCheckBoxMenuItem isIgnoreCaseCompareCheckBox;
    private JMenuItem filterMenuItem;
    private JMenuItem aboutMenuItem;
    
    /**
     * heart of application is this controller 
     */
    private CompareController compareController;


    /**
     * Construct w/o a pre-existing controller
     */
    public DirCmpFrame() {
        this( new CompareController() );
    }
        
    
    /**
     * Construct using given controller
     * 
     * @param controller
     */
    public DirCmpFrame(CompareController controller) {
        compareController = controller;
        controller.setErrorHandler(new DialogErrorHandler());
        Dimension screenDim = getScreenSize();
        // 80% full screen 
        setSize((int)(0.8*screenDim.width) ,(int)(0.8*screenDim.height)); 
        setTitle(Messages.getString("DirCmpFrame.frame_title")); //$NON-NLS-1$
        this.setResizable(true);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Messages.getString("DirCmpFrame.menu_file")); //$NON-NLS-1$
        fileMenu.setMnemonic(KeyEvent.VK_F);
        
        JMenu optionsMenu = new JMenu(Messages.getString("DirCmpFrame.menu_options")); //$NON-NLS-1$
        optionsMenu.setMnemonic(KeyEvent.VK_O);
        
        JMenu helpMenu = new JMenu(Messages.getString("DirCmpFrame.menu_help")); //$NON-NLS-1$
        helpMenu.setMnemonic(KeyEvent.VK_H);

        menuBar.add(fileMenu);
        exitMenuItem = new JMenuItem(Messages.getString("DirCmpFrame.menu_file_exit"), KeyEvent.VK_X) ; //$NON-NLS-1$
        fileMenu.add(exitMenuItem);
        menuBar.add(optionsMenu);
        isTextCompareCheckBox = new JCheckBoxMenuItem(Messages.getString("DirCmpFrame.menu_options_text_compare"), compareController.getTextCompare()); //$NON-NLS-1$
        optionsMenu.add(isTextCompareCheckBox);
        isIgnoreCaseCompareCheckBox = new JCheckBoxMenuItem(Messages.getString("DirCmpFrame.menu_options_ignore_name_case"), compareController.getIgnoreNameCase()); //$NON-NLS-1$
        optionsMenu.add(isIgnoreCaseCompareCheckBox);
        filterMenuItem = new JMenuItem(Messages.getString("DirCmpFrame.menu_options_filter"), KeyEvent.VK_F); //$NON-NLS-1$
        filterMenuItem.addActionListener(this);
        optionsMenu.add(filterMenuItem);

        aboutMenuItem = new JMenuItem(Messages.getString("DirCmpFrame.menu_help_about"), KeyEvent.VK_A);
        helpMenu.add(aboutMenuItem);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
        exitMenuItem.addActionListener(this);
        isTextCompareCheckBox.addActionListener(this);
        isIgnoreCaseCompareCheckBox.addActionListener(this);
        aboutMenuItem.addActionListener(this);
        
        Container contentPane = getContentPane();
        JPanel oldPanel = new CmpTreePanel(CompareController.NodeRole.OLD_ROOT, compareController);
        JPanel newPanel = new CmpTreePanel(CompareController.NodeRole.NEW_ROOT, compareController);
        JPanel cmpPanel = new CmpTreePanel(CompareController.NodeRole.CMP_ROOT, compareController);

        JSplitPane topSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane inSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        contentPane.add(topSplitPane);
        topSplitPane.setLeftComponent(inSplitPane);
        topSplitPane.setRightComponent(cmpPanel);
        topSplitPane.setResizeWeight(0.5);
        inSplitPane.setTopComponent(oldPanel);
        inSplitPane.setBottomComponent(newPanel);
        inSplitPane.setResizeWeight(0.5);
        

    }
    
    /**
     * Determine display screen size using default screen
     * 
     * @return dimensions of screen
     */
    private Dimension getScreenSize() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        return new Dimension(bounds.width, bounds.height);
    }
    
    public void actionPerformed(ActionEvent event) {
        if ( event.getSource() == exitMenuItem ) {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else if ( event.getSource() == isTextCompareCheckBox ) {
            compareController.setTextCompare(isTextCompareCheckBox.isSelected());
        }
        else if ( event.getSource() == isIgnoreCaseCompareCheckBox ) {
            compareController.setIgnoreNameCase(isIgnoreCaseCompareCheckBox.isSelected());
        }   
        else if ( event.getSource() == filterMenuItem ) {
            CompositeFilterDialog filterDialog = new CompositeFilterDialog(this, compareController.getFilter());
            if ( filterDialog.showDialog() == CompositeFilterDialog.DIALOG_RESULT.OK ) {
                CompositeNodeFilter filter = filterDialog.getFilter();
                compareController.setFilter(filter);
            }
        }   
        else if ( event.getSource() == aboutMenuItem ) {
            JOptionPane.showMessageDialog(this, About.aboutText());
        }   
        
    }
    
    /**
     * Application may be launched via this JFrame
     * 
     * @param args ignored
     */
    static public void main(String[] args) {
        DirCmpFrame appFrame = new DirCmpFrame();
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) { System.exit(0); }
            }
        );

        appFrame.setVisible(true);
    }
}
