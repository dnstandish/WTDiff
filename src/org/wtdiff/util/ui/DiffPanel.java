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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.wtdiff.util.text.DiffAdapter;
import org.wtdiff.util.text.DiffChangeListener;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FullDiffAdapter;
import org.wtdiff.util.text.NonprintingCharStyle;
import org.wtdiff.util.text.NormalDiffAdapter;
import org.wtdiff.util.ui.DiffNavigator.DiffNavigationEvent;
import org.wtdiff.util.ui.DocumentDiffWriter.ChangeRange;
import org.wtdiff.util.ui.text.NoWrapEditorKit;

public class DiffPanel extends JPanel implements ActionListener, DiffChangeListener, DiffNavigationListener {
    
    public enum DiffType {
        NORMAL,
        CONTEXT,
        UNIFIED,
        SIDE_BY_SIDE;
        
        public String localizedString() {
            return Messages.getString("DiffPanel.DiffType." + this.toString());
        }
    }

    private DiffController controller;
    private DiffType diffStyle = DiffType.NORMAL;
    private NonprintingCharStyle nonprintingStyle = NonprintingCharStyle.ASIS;
    private boolean isNumberLines = false;
    private int tabWidth = 0;
    private boolean haveDiff = false;
    private DiffNavigator navigator = null;
    private int nChanges;
    private List<ChangeRange> oldChanges;
    private List<ChangeRange> newChanges;
    private JTextPane oldTextPane;
    private JTextPane newTextPane;
    private int currentChangeNo;
    
    public DiffPanel(DiffController diffController) {
        controller = diffController;
        controller.addDiffChangeListener(this);
        //displayDiff();
    }

    public void setDiffNavigator( DiffNavigator n ) {
        if ( navigator != null ) {
            navigator.removeDiffNavigationListener(this);
        }
        navigator = n;
        if ( navigator != null ) {
            navigator.addDiffNavigationListener(this);
        }
    }
    public void displayDiff()  {
        if ( navigator != null) {
//            navigator.SetEnabled(false);
            navigator.setNumChanges(0);
        }
        if ( haveDiff ) {
//            System.out.println("have diff, removing components");
            this.removeAll();
            nChanges = 0;
            oldChanges = new ArrayList<ChangeRange>(0);
            newChanges = new ArrayList<ChangeRange>(0);
            oldTextPane = null;
            newTextPane = null;
        }
        if ( haveDiff && ! controller.haveDiff() ) {
            haveDiff = false;
            //this.setLayout(new FlowLayout());
            this.repaint();
            return;
        }
        if ( ! controller.haveDiff() )
            return;
//        System.err.println(Thread.currentThread());
        if ( DiffType.SIDE_BY_SIDE == diffStyle ) {
            doublePaneDiff();
        } else {
            singlePaneDiff();
        }
        haveDiff = true;
    }
    
    private void singlePaneDiff() {
        this.setLayout(new GridLayout(1,1));
//        JTextPane oldTextPane;
        JScrollPane scrollPanel1;
        StyleContext sc = new StyleContext();
        DefaultStyledDocument doc1 = new DefaultStyledDocument(sc);

//        StyledDocument doc1;
//        textPane1 = new NoWrapTextPane();
//        scrollPanel1 = new JScrollPane(textPane1, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        doc1 = textPane1.getStyledDocument();
        NormalDocumentDiffWriter w = new NormalDocumentDiffWriter(doc1);
        w.setControlCharaterHandling(nonprintingStyle);
        w.setNumberLines(isNumberLines);
        w.setTabWidth(tabWidth);
        try {
            DiffAdapter fmt;
            if ( DiffType.NORMAL == diffStyle ) {
                fmt = new NormalDiffAdapter( controller.getOldData(), controller.getNewData(), w);
            } else {
                fmt = new FullDiffAdapter( controller.getOldData(), controller.getNewData(), w);
            }
            fmt.format( controller.getDiff() );
        } catch ( IOException ioe ) {
            JOptionPane.showMessageDialog(
                null, 
                ioe.getMessage(), 
                Messages.getString("DiffPanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );

        }
//        oldTextPane = new NoWrapTextPane();
//        // if we set the editor kit after setting the document, modelToView throws a null pointer exception
//        oldTextPane.setEditorKit(new NoWrapEditorKit());
//        oldTextPane.setDocument(doc1);
        oldTextPane = new NoWrapTextPane(doc1);
        scrollPanel1 = new JScrollPane(oldTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        oldTextPane.setBackground( oldTextPane.getBackground().darker() );
        oldTextPane.setEditable(false);
        this.add(scrollPanel1);
        this.validate();
        nChanges = w.numberOfDifferences();
        oldChanges = w.getChangeRanges();
        if ( nChanges > 0 ) {
            if ( navigator != null ) {
                navigator.setNumChanges(w.numberOfDifferences());
//                navigator.SetEnabled(true);
            }
            makeChangeVisible(1);
//            ChangeRange range = oldChanges.get(0);
//            try {
//                Rectangle viewRectEnd = oldTextPane.modelToView(range.end);
//                oldTextPane.setCaretPosition(range.end);
//                if ( viewRectEnd != null )
//                    oldTextPane.scrollRectToVisible(viewRectEnd);
//                if ( range.begin > 0)
//                    oldTextPane.setCaretPosition(range.begin-1);  // line above to include indicator
//                else
//                    oldTextPane.setCaretPosition(range.begin);
//                if ( navigator != null ) {
//                    navigator.setNumChanges(w.numberOfDifferences());
//                    navigator.setCurrentChange(1);
//                    navigator.SetEnabled(true);
//                }
//            } catch (BadLocationException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
        
    }
    
    private void makeChangeVisible(int changeNo) {
//        Rectangle delme2 = oldTextPane.getVisibleRect(); //TODO DEBUC
//      System.out.println("model to view " + viewRectOldEnd.x + "," + viewRectOldEnd.y);
//        System.out.println("visible rect " + delme2.x + "," + delme2.y + ", h " + delme2.height + ", w " + delme2.width);
        
        if ( nChanges == 0 || changeNo > nChanges || changeNo < 1)
            return;
        // need to position both old and new text panes
        // otherwise Panel gets into a frenzy due to
        // conflicts between caret positions and coupled scrolling
        ChangeRange oldRange = oldChanges.get(changeNo - 1);
        ChangeRange newRange = null;
        if ( newTextPane != null)
            newRange = newChanges.get(changeNo - 1);
        try {
            Rectangle viewRectOldEnd = oldTextPane.modelToView(oldRange.end);
//            if ( viewRectOldEnd != null)
//                System.out.println("model to view " + viewRectOldEnd.x + "," + viewRectOldEnd.y);
//            Rectangle delme = oldTextPane.getVisibleRect(); 
//            System.out.println("model to view " + viewRectOldEnd.x + "," + viewRectOldEnd.y);
//            System.out.println("visible rect " + delme.x + "," + delme.y + ", h " + delme.height + ", w " + delme.width);
            Rectangle viewRectNewEnd = null;
            if ( newTextPane != null)
                viewRectNewEnd = newTextPane.modelToView(newRange.end);
            oldTextPane.setCaretPosition(oldRange.end);
            if ( newTextPane != null )
                newTextPane.setCaretPosition(newRange.end);
            if ( viewRectOldEnd != null )
                oldTextPane.scrollRectToVisible(viewRectOldEnd);
            if ( viewRectNewEnd != null )
                newTextPane.scrollRectToVisible(viewRectNewEnd);
            if ( newTextPane != null ) {
                oldTextPane.setCaretPosition(oldRange.begin);
                newTextPane.setCaretPosition(newRange.begin);
            } else {
                oldTextPane.setCaretPosition(oldRange.begin - 1);
            }
            currentChangeNo = changeNo;
            if ( navigator != null ) {
                navigator.setCurrentChange(changeNo);
            }
        } catch (BadLocationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//        delme2 = oldTextPane.getVisibleRect(); 
//      System.out.println("model to view " + viewRectOldEnd.x + "," + viewRectOldEnd.y);
//        System.out.println("visible rect " + delme2.x + "," + delme2.y + ", h " + delme2.height + ", w " + delme2.width);
        
    }
    private void doublePaneDiff() {
        this.setLayout(new GridLayout(1,2));
        StyleContext sc = new StyleContext();
        DefaultStyledDocument doc1 = new DefaultStyledDocument(sc);
        DefaultStyledDocument doc2 = new DefaultStyledDocument(sc);
        JScrollPane scrollPane1;
        JScrollPane scrollPane2;
        TwoDocumentDiffWriter w = new TwoDocumentDiffWriter(doc1, doc2);
        w.setControlCharaterHandling(nonprintingStyle);
        w.setNumberLines(isNumberLines);
        w.setTabWidth(tabWidth);
        try {
            DiffAdapter fmt;
            fmt = new FullDiffAdapter( controller.getOldData(), controller.getNewData(), w);
            fmt.format( controller.getDiff() );
        } catch ( IOException ioe ) {
            JOptionPane.showMessageDialog(
                null, 
                ioe.getMessage(), 
                Messages.getString("DiffPanel.title_error"),  //$NON-NLS-1$
                JOptionPane.ERROR_MESSAGE
            );

        }
        oldTextPane = new NoWrapTextPane(doc1);
//        // if we set the editor kit after setting the document, modelToView throws a null pointer exception
//        oldTextPane.setEditorKit(new NoWrapEditorKit());
//        oldTextPane.setDocument(doc1);
        newTextPane = new NoWrapTextPane(doc2);
//        newTextPane.setEditorKit(new NoWrapEditorKit());
//        newTextPane.setDocument(doc2);
        // TODO idea use file names as column header
        scrollPane1 = new JScrollPane(oldTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2 = new JScrollPane(newTextPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane2.getVerticalScrollBar().setModel(scrollPane1.getVerticalScrollBar().getModel());
        oldTextPane.setBackground( oldTextPane.getBackground().darker() );
        newTextPane.setBackground( newTextPane.getBackground().darker() );
        oldTextPane.setEditable(false);
        newTextPane.setEditable(false);
        this.add(scrollPane1, BorderLayout.EAST);
        this.add(scrollPane2, BorderLayout.WEST);
        this.validate();
        nChanges = w.numberOfDifferences();
        oldChanges = w.getOldChangeRanges();
        newChanges = w.getNewChangeRanges();
        if ( nChanges > 0 ) {
            if ( navigator != null ) {
                navigator.setNumChanges(nChanges);
//                navigator.SetEnabled(true);
            }
            makeChangeVisible(1);
//            // need to position both old and new text panes
//            // otherwise Panel gets into a frenzy due to
//            // conflicts between caret positions and coupled scrolling
//            ChangeRange oldRange = oldChanges.get(0);
//            ChangeRange newRange = newChanges.get(0);
//            try {
//                Rectangle viewRectOldEnd = oldTextPane.modelToView(oldRange.end);
//                Rectangle viewRectNewEnd = newTextPane.modelToView(newRange.end);
//                oldTextPane.setCaretPosition(oldRange.end);
//                newTextPane.setCaretPosition(newRange.end);
//                if ( viewRectOldEnd != null )
//                    oldTextPane.scrollRectToVisible(viewRectOldEnd);
//                if ( viewRectNewEnd != null )
//                    newTextPane.scrollRectToVisible(viewRectNewEnd);
//                oldTextPane.setCaretPosition(oldRange.begin);
//                newTextPane.setCaretPosition(newRange.begin);
//                if ( navigator != null ) {
//                    navigator.setNumChanges(w.numberOfDifferences());
//                    navigator.setCurrentChange(1);
//                    navigator.SetEnabled(true);
//                }
//            } catch (BadLocationException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }

    }

    public DiffType getDiffStyle() {
        return diffStyle;
    }
    
    public void setDiffStyle(DiffType type) {
        if ( type != null && !diffStyle.equals(type) ) {
            diffStyle = type;
        }
    }

    public NonprintingCharStyle getNonprintingStyle() {
        return nonprintingStyle;
    }
    
    public void setNonprintingStyle(NonprintingCharStyle style) {
        if ( style != null && !nonprintingStyle.equals(style) ) {
            nonprintingStyle = style;
        }
    }

    public boolean isNumberLines() {
        return isNumberLines;
    }
    
    public void setNumberLines(boolean numberLines) {
        if ( numberLines !=  isNumberLines ) {
            isNumberLines = numberLines;
        }
    }

    public int getTabWidth() {
        return tabWidth;
    }
    
    public void setTabWidth(int width) {
        if ( width !=  tabWidth ) {
            tabWidth = width;
        }
    }

    @Override
    public void diffChanged() {
        displayDiff();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void diffNavigationEvent(DiffNavigationEvent eventType) {
        if ( nChanges == 0 )
            return; // nothing to do
        
        int newChangeNo = currentChangeNo;
        
        if ( eventType == DiffNavigationEvent.NEXT)
            newChangeNo++;
        else if ( eventType == DiffNavigationEvent.PREV)
            newChangeNo--;
        
        if ( newChangeNo < 1 )
            newChangeNo = nChanges;
        else if ( newChangeNo > nChanges )
            newChangeNo = 1
            ;
        makeChangeVisible(newChangeNo);
    }
}
