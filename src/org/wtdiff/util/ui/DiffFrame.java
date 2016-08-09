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
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;

import org.wtdiff.util.About;
import org.wtdiff.util.text.DiffChangeListener;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.text.InputStreamSource;
import org.wtdiff.util.text.NonprintingCharStyle;
import org.wtdiff.util.text.TextUtil;
import org.wtdiff.util.text.DiffController.SourceType;
import org.wtdiff.util.text.TextUtil.LineSeparator;


public class DiffFrame extends JFrame implements ActionListener, DiffChangeListener {

    public enum WhitespaceHandling {
        ASIS,
        TRIM,
        COMPACT,
        COMPACT_TRIM,
        IGNORE;
        
        public String localizedString() {
            return Messages.getString("DiffFrame.WhitespaceHandling." + this.toString());
        }
    }
    
    private boolean isStandAlone = false;
    private DiffController controller;
    private JMenuItem openMenuItem;
    private JMenuItem exitMenuItem;
    private JMenuItem closeMenuItem;
    private JMenuItem aboutMenuItem;
    private JTextField oldFileName;
    private JTextField newFileName = new JTextField();
    private JComboBox<TextUtil.LineSeparator> oldLineSep;
    private JComboBox<Charset> oldCharset;
    private JComboBox<Charset> newCharset;
    private JComboBox<TextUtil.LineSeparator> newLineSep;
    private JButton diffButton;
    private DiffPanel diffPanel;
    private JComboBox<String> diffTypeOption;
    private JComboBox<String> whitespaceOption;
    private JComboBox<String> nonprintingOption;
    private JCheckBox numberLines;
    private LimitedDigitsTextField tabWidthField;
    private JButton displayButton;
    private JButton prevButton;
    private JLabel currentChangeLabel;
    private JButton nextButton;
    
    public DiffFrame() {
        this(true, new DiffController());
    }
    

    public DiffFrame(boolean isStandAloneFrame, DiffController diffController) {
        isStandAlone = isStandAloneFrame;
        controller = diffController;
//TODO        controller.setErrorHandler(new DialogErrorHandler());
        Dimension screenDim = getScreenSize();
        // 80% full screen 
        setSize((int)(0.8*screenDim.width) ,(int)(0.8*screenDim.height)); 
        setTitle(Messages.getString("DiffFrame.frame_title"));
        this.setResizable(true);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Messages.getString("DiffFrame.menu_file"));
        fileMenu.setMnemonic(KeyEvent.VK_F);    
        menuBar.add(fileMenu);
        openMenuItem = new JMenuItem(Messages.getString("DiffFrame.menu_file_open"), KeyEvent.VK_O);
        exitMenuItem = new JMenuItem(Messages.getString("DiffFrame.menu_file_exit"), KeyEvent.VK_X);
        closeMenuItem = new JMenuItem(Messages.getString("DiffFrame.menu_file_close"), KeyEvent.VK_C);
        
        JMenu helpMenu = new JMenu(Messages.getString("DiffFrame.menu_help"));
        helpMenu.setMnemonic(KeyEvent.VK_H);    
        aboutMenuItem = new JMenuItem(Messages.getString("DiffFrame.menu_help_about"), KeyEvent.VK_A);
        
        helpMenu.add(aboutMenuItem);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        fileMenu.setMnemonic(KeyEvent.VK_F);    
        if ( isStandAlone ) {
            fileMenu.add(openMenuItem);
            fileMenu.add(exitMenuItem);
            openMenuItem.addActionListener(this);
            exitMenuItem.addActionListener(this);
        } else {
            fileMenu.add(closeMenuItem);            
            closeMenuItem.addActionListener(this);
        }
        aboutMenuItem.addActionListener(this);
        setJMenuBar(menuBar);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        Box filesBox = Box.createVerticalBox();
        
        JLabel oldLabel = new JLabel(Messages.getString("DiffFrame.file_old_label"));
        oldFileName = createFileNameField(controller.getOldSourceName());
        
        oldLineSep = createLineSepComboBox(SourceType.OLD);
        oldLineSep.addActionListener(this);
        
        oldCharset = new JComboBox<Charset>();
        addCharsetItems(oldCharset);
        initializeEncodingFromController(SourceType.OLD, oldCharset);
        oldCharset.addActionListener(this);
        
        Box oldFileBox = createHorizontalBoxedComponents(oldLabel, oldFileName, oldLineSep, oldCharset);

        JLabel newLabel = new JLabel(Messages.getString("DiffFrame.file_new_label"));
        newFileName = createFileNameField(controller.getNewSourceName());
        
        newLineSep = createLineSepComboBox(SourceType.NEW);
        newLineSep.addActionListener(this);
        
        newCharset = new JComboBox<Charset>();
        addCharsetItems(newCharset);
        initializeEncodingFromController(SourceType.NEW, newCharset);
        newCharset.addActionListener(this);
        
        Box newFileBox = createHorizontalBoxedComponents(newLabel, newFileName, newLineSep, newCharset);

        filesBox.add(Box.createVerticalStrut(5));
        filesBox.add(oldFileBox);
        filesBox.add(Box.createVerticalStrut(2));
        filesBox.add(newFileBox);
        filesBox.add(Box.createVerticalStrut(5));
//        filesBox.setBorder(BorderFactory.createEtchedBorder());
        filesBox.setBorder(
            BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
                Messages.getString("DiffFrame.border_title_source"))
        );
        
        Box whitespaceBox = Box.createVerticalBox();
        Box wsLabelBox = Box.createHorizontalBox();
        wsLabelBox.add(Box.createHorizontalGlue());
        wsLabelBox.add(new JLabel(Messages.getString("DiffFrame.label_whitespace_handling")));
        wsLabelBox.add(Box.createHorizontalGlue());        
        whitespaceBox.add(wsLabelBox);
        
        whitespaceOption = createWhiteSpaceCombo();
        whitespaceOption.addActionListener(this);
        
        whitespaceBox.add(whitespaceOption);
        whitespaceBox.add(Box.createVerticalStrut(5));
        whitespaceBox.add(Box.createVerticalGlue());
        
        Box diffButtonBox = Box.createVerticalBox();
        diffButton = new JButton(Messages.getString("DiffFrame.button_diff"));
        diffButton.addActionListener(this);        
        diffButtonBox.add(Box.createVerticalGlue());
        diffButtonBox.add(diffButton);
        diffButtonBox.add(Box.createVerticalGlue());
        Box diffBox = this.createHorizontalBoxedComponents(whitespaceBox, diffButtonBox);
        diffBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
            Messages.getString("DiffFrame.border_title_diff"))
        );
        
        Box diffTypeBox = Box.createVerticalBox();        
        diffTypeOption = new JComboBox<>();
        for( DiffPanel.DiffType diffType: DiffPanel.DiffType.values() ) {
            diffTypeOption.addItem(diffType.localizedString());
        }
        diffTypeOption.setMaximumSize(diffTypeOption.getPreferredSize());
        diffTypeOption.addActionListener(this);
        Box diffTypeLabelBox = Box.createHorizontalBox();
        diffTypeLabelBox.add(Box.createHorizontalGlue());
        diffTypeLabelBox.add(new JLabel(Messages.getString("DiffFrame.label_diffStyle")));
        diffTypeLabelBox.add(Box.createHorizontalGlue());
        diffTypeBox.add(diffTypeLabelBox);
        diffTypeBox.add(diffTypeOption);
        diffTypeBox.add(Box.createVerticalGlue());
        
        nonprintingOption = createNonPrintingCombo();
        nonprintingOption.addActionListener(this);
        
        Box nonPrintingBox = Box.createVerticalBox();
        Box nonPrintingLabelBox = Box.createHorizontalBox();
        nonPrintingLabelBox.add(Box.createHorizontalGlue());
        nonPrintingLabelBox.add(new JLabel(Messages.getString("DiffFrame.label_special_characters")));
        nonPrintingLabelBox.add(Box.createHorizontalGlue());
        nonPrintingBox.add(nonPrintingLabelBox);
        nonPrintingBox.add(nonprintingOption);
        nonPrintingBox.add(Box.createVerticalGlue());
        
        Box otherOptionsBox = Box.createVerticalBox();
        Box numberLinesBox = Box.createHorizontalBox();
        numberLines = new JCheckBox(Messages.getString("DiffFrame.label_number_lines"));
        numberLines.addActionListener(this);
        numberLinesBox.add(numberLines);
        otherOptionsBox.add(numberLinesBox);
        Box tabWidthBox = Box.createHorizontalBox();
        tabWidthField = new LimitedDigitsTextField(3);
        tabWidthBox.add(tabWidthField);
        tabWidthBox.add(new JLabel(Messages.getString("DiffFrame.label_tab_width")));
        otherOptionsBox.add(tabWidthBox);
        tabWidthField.setMaximumSize(tabWidthField.getPreferredSize());  // TODO is this needed?
        
        displayButton = new JButton(Messages.getString("DiffFrame.button_show"));
        displayButton.addActionListener(this);
        displayButton.setEnabled( controller.haveDiff() );
        //Box dbBox = createHorizontalBoxedComponents(Box.createHorizontalGlue(), displayButton, Box.createHorizontalGlue());
        Box dbBox = Box.createHorizontalBox();
        dbBox.add(Box.createHorizontalGlue());
        dbBox.add(displayButton);
        dbBox.add(Box.createHorizontalGlue());
        prevButton = new BasicArrowButton(SwingConstants.WEST);
        prevButton.setEnabled(false);
        currentChangeLabel = new JLabel("0/0"); 
        currentChangeLabel.setEnabled(false);
        nextButton = new BasicArrowButton(SwingConstants.EAST);
//        JButton nextButton = new JButton("\u25ba");
        nextButton.setEnabled(false);
        Box changeBox = createHorizontalBoxedComponents(prevButton, currentChangeLabel, nextButton);
        Box displayButtonBox = Box.createVerticalBox();
        DiffNavigator diffNav = new DiffNavigator();
        displayButtonBox.add(dbBox);
        displayButtonBox.add(Box.createVerticalStrut(3));
//        displayButtonBox.add(changeBox);
        displayButtonBox.add(diffNav);
        
        Box displayBox = createHorizontalBoxedComponents(diffTypeBox, nonPrintingBox, otherOptionsBox, displayButtonBox);
        displayBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), 
            Messages.getString("DiffFrame.border_title_display"))
        );
        
        Box ControlBox = Box.createHorizontalBox();
        ControlBox.add(filesBox);
        ControlBox.add(diffBox);
        ControlBox.add(displayBox);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(ControlBox, "North");
        controller.addDiffChangeListener(this);
        diffPanel = new DiffPanel(controller);
        diffPanel.setNumberLines(numberLines.isSelected());
        diffPanel.setDiffNavigator(diffNav);
        contentPane.add(diffPanel);
        diffPanel.displayDiff();
    }

    /**
     * Determine display screen size using default screen
     * 
     * @return dimensions of screen
     */
    private Dimension getScreenSize() {  //TODO DRY
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        return new Dimension(bounds.width, bounds.height);
    }

    private JTextField createFileNameField(String sourceName) {
        JTextField fileNameField = new JTextField();
        fileNameField.setText(sourceName);
        fileNameField.setColumns(20);
        fileNameField.setEditable(false);
        
        return fileNameField;
    }
    
    private JComboBox<TextUtil.LineSeparator> createLineSepComboBox(SourceType type) {
        JComboBox<TextUtil.LineSeparator> lineSepCombo = new JComboBox<TextUtil.LineSeparator>();
        lineSepCombo.addItem(null);
        lineSepCombo.addItem(TextUtil.LineSeparator.CR);
        lineSepCombo.addItem(TextUtil.LineSeparator.CRLF);
        lineSepCombo.addItem(TextUtil.LineSeparator.LF);
        initializeLineSeparatorFromController(type, lineSepCombo);
        return lineSepCombo;
    }
    
    private Box createHorizontalBoxedComponents(JComponent ... comps) {
        Box box = Box.createHorizontalBox();
        //box.add(Box.createHorizontalGlue());
        for(int i = 0 ; i < comps.length; i++ ) {
            if ( i == 0 ) {
                box.add(Box.createHorizontalStrut(5));
            } else {
                box.add(Box.createHorizontalStrut(2));
            }
            box.add(comps[i]);
        }
        box.add(Box.createHorizontalStrut(5));
        return box;
    }

    private JComboBox<String> createWhiteSpaceCombo() {
    
        JComboBox<String> combo = new JComboBox<>();
        combo.addItem(WhitespaceHandling.ASIS.localizedString());
        combo.addItem(WhitespaceHandling.TRIM.localizedString());
        combo.addItem(WhitespaceHandling.COMPACT.localizedString());
        combo.addItem(WhitespaceHandling.COMPACT_TRIM.localizedString());
        combo.addItem(WhitespaceHandling.IGNORE.localizedString());
        if ( controller.isIgnoreWhiteSpace()) {
            combo.setSelectedItem(WhitespaceHandling.IGNORE.localizedString());
        } else if ( controller.isTrimWhiteSpace() ) {
            if ( controller.isCompactWhiteSpace() ) {
                combo.setSelectedItem(WhitespaceHandling.COMPACT_TRIM.localizedString());            
            } else {
                combo.setSelectedItem(WhitespaceHandling.TRIM.localizedString());
            }
        } else if ( controller.isCompactWhiteSpace() ) {
            combo.setSelectedItem(WhitespaceHandling.COMPACT.localizedString());
        } else {
            combo.setSelectedItem(WhitespaceHandling.ASIS.localizedString());            
        }
        combo.setMaximumSize(combo.getPreferredSize());
        return combo;
    }

    private JComboBox<String> createNonPrintingCombo() {
        JComboBox<String>combo = new JComboBox<>();
        
        for(NonprintingCharStyle style: NonprintingCharStyle.values() ) {
            combo.addItem(style.localizedString());
        }
        combo.setMaximumSize(combo.getPreferredSize());
        return combo;
    }
    
    private void initializeLineSeparatorFromController(SourceType which, JComboBox<TextUtil.LineSeparator> comboBox ) {
        try {
            LineSeparator lineSep = controller.getLineSep(which);
            if ( lineSep != null ) {
                comboBox.setSelectedItem(lineSep);
            }
        } catch (IOException ioe ) {
            ioe.printStackTrace(); // TODO can we give an error dialog ?
        }
    }
    
    private void initializeEncodingFromController(SourceType which, JComboBox<Charset> comboBox ) {
        Charset encoding = controller.getEncoding(which);
        if ( encoding != null ) {
            comboBox.setSelectedItem(encoding);
        }
    }
    
    private void addCharsetItems(JComboBox<Charset> comboBox) {
        ArrayList<Charset> ourCharsets = new ArrayList<>();
        ourCharsets.add(Charset.defaultCharset());
        for( Charset charset: new Charset[] {
         StandardCharsets.UTF_8,
         StandardCharsets.ISO_8859_1,
         StandardCharsets.US_ASCII,
         StandardCharsets.UTF_16,
         StandardCharsets.UTF_16BE,
         StandardCharsets.UTF_16LE,
         controller.getEncoding(SourceType.OLD),
         controller.getEncoding(SourceType.NEW)   } ) {
            if (  charset != null && ! ourCharsets.contains(charset)) {
                ourCharsets.add(charset);
            }
        }
        String win1252Name = "windows-1252";
        if ( Charset.isSupported(win1252Name) ) {
            Charset win1252 = Charset.forName(win1252Name);
            if ( ! ourCharsets.contains(win1252)) {
                ourCharsets.add(win1252);
            }
            
        }
        for ( Charset charset: ourCharsets ) {
            comboBox.addItem(charset);
        }
    }
    
    private void setControllerWhitespaceHandling(String whitespaceString) {
        WhitespaceHandling handlingChoice = null;
        for( WhitespaceHandling handling : WhitespaceHandling.values() ) {
            if ( handling.localizedString().equalsIgnoreCase(whitespaceString)) {
                handlingChoice = handling;
                break;
            }
        }
        switch (handlingChoice) {
        case ASIS:
            controller.setCompactWhiteSpace(false);
            controller.setIgnoreWhiteSpace(false);
            controller.setTrimWhiteSpace(false);
            break;
        case TRIM:
            controller.setCompactWhiteSpace(false);
            controller.setIgnoreWhiteSpace(false);
            controller.setTrimWhiteSpace(true);
            break;
        case COMPACT:
            controller.setCompactWhiteSpace(true);
            controller.setIgnoreWhiteSpace(false);
            controller.setTrimWhiteSpace(false);
            break;
        case COMPACT_TRIM:
            controller.setCompactWhiteSpace(true);
            controller.setIgnoreWhiteSpace(false);
            controller.setTrimWhiteSpace(true);
            break;
        case IGNORE:
            controller.setCompactWhiteSpace(false);
            controller.setIgnoreWhiteSpace(true);
            controller.setTrimWhiteSpace(false);
            break;
         default:
             //TODO bug
         }
    }
    
    private void setPanelDiffType(String diffTypeString) {
        DiffPanel.DiffType choice = null;
        for( DiffPanel.DiffType type : DiffPanel.DiffType.values() ) {
            if ( type.localizedString().equalsIgnoreCase(diffTypeString)) {
                choice = type;
                break;
            }
        }
        if ( choice != null ) {
            diffPanel.setDiffStyle(choice);
        }
    }
    @Override
    public void actionPerformed(ActionEvent event) {

        if ( event.getSource() == aboutMenuItem ) {
            System.out.println("about"); //TODO about dialog
            JOptionPane.showMessageDialog(this, About.aboutText());
        }
        else if ( isStandAlone ) {
            if ( event.getSource() == exitMenuItem ) {
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            } else if ( event.getSource() == openMenuItem ) {
                DiffOpenDialog dialog = new DiffOpenDialog(
                    (JFrame)SwingUtilities.getRoot(this), 
                    controller
                );
                dialog.pack();
                dialog.setVisible(true);
            }
        } else if ( event.getSource() == closeMenuItem ) {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
        
        try {
            if ( event.getSource() == diffButton ) {
                diffPanel.setTabWidth(tabWidthField.getValue());
                controller.diff();
            } else if ( event.getSource() == diffTypeOption ) {
                String style = diffTypeOption.getItemAt(diffTypeOption.getSelectedIndex());
                setPanelDiffType(style);
                //diffPanel.setDiffStyle(style);
            } else if ( event.getSource() == nonprintingOption ) {
                String styleText = nonprintingOption.getItemAt(nonprintingOption.getSelectedIndex());
                for( NonprintingCharStyle style : NonprintingCharStyle.values() ) {
                    if ( style.localizedString().equalsIgnoreCase(styleText)) {
                        diffPanel.setNonprintingStyle(style);
                        break;
                    }
                }
            } else if ( event.getSource() == numberLines ) {
                diffPanel.setNumberLines(numberLines.isSelected());
            } else if ( event.getSource() == oldLineSep ) {
                TextUtil.LineSeparator lineSep = oldLineSep.getItemAt(oldLineSep.getSelectedIndex());
                controller.forceLineSepSourceType(SourceType.OLD, lineSep);
            } else if ( event.getSource() == newLineSep ) {
                TextUtil.LineSeparator lineSep = newLineSep.getItemAt(newLineSep.getSelectedIndex());
                controller.forceLineSepSourceType(SourceType.NEW, lineSep);
            } else if ( event.getSource() == oldCharset ) {
                Charset encoding = oldCharset.getItemAt(oldCharset.getSelectedIndex());
                controller.forceEncoding(SourceType.OLD, encoding);
            } else if ( event.getSource() == newCharset ) {
                Charset encoding = newCharset.getItemAt(newCharset.getSelectedIndex());
                controller.forceEncoding(SourceType.NEW, encoding);
            } else if ( event.getSource() ==  whitespaceOption ) {
                String option = whitespaceOption.getItemAt(whitespaceOption.getSelectedIndex());
                setControllerWhitespaceHandling(option);
            } else if ( event.getSource() == displayButton ) {
                diffPanel.setTabWidth(tabWidthField.getValue());
                diffPanel.displayDiff();
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(
                null, 
                ioe.getMessage(), 
                Messages.getString("DiffFrame.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateEncoding(JComboBox<Charset> comp, SourceType type) {
        Charset currentVal = comp.getItemAt(comp.getSelectedIndex());
        Charset newVal = controller.getEncoding(type);
        if ( newVal != null && currentVal != newVal )
            comp.setSelectedItem(newVal);
    }

    private void updateLineSep(JComboBox<TextUtil.LineSeparator> comp, SourceType type) throws IOException  {
        TextUtil.LineSeparator currentVal = comp.getItemAt(comp.getSelectedIndex());
        
        TextUtil.LineSeparator newVal = controller.getLineSep(type);
        
        if ( newVal == currentVal )
            return;
        
        comp.setSelectedItem(newVal);
    }
    
    @Override
    public void diffChanged() {
//        System.out.println("Diff changed event");
//        String currentOld = oldFileName.getText();
        displayButton.setEnabled( controller.haveDiff() );
        oldFileName.setText(controller.getOldSourceName());
        newFileName.setText(controller.getNewSourceName());
        updateEncoding( oldCharset, SourceType.OLD );
        updateEncoding( newCharset, SourceType.NEW );
        try {
            updateLineSep( oldLineSep, SourceType.OLD );
            updateLineSep( newLineSep, SourceType.NEW );
        } catch (IOException ioe) {
            //TODO this is not right
            ioe.printStackTrace();
        }
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        DiffController c = new DiffController();
        try {
            if ( args.length > 0) {
                c.setOldSource(new FileInputStreamSource(new File(args[0])));
            }
            if ( args.length > 1) {
                c.setNewSource(new FileInputStreamSource(new File(args[1])));
            }        
            c.diff();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        DiffFrame appFrame = new DiffFrame(true, c);
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) { System.exit(0); }
            }
        );

        appFrame.setVisible(true);

    }



}
