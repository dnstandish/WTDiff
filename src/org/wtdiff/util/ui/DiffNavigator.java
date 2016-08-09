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

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;

public class DiffNavigator extends Box implements ActionListener {
    
    public enum DiffNavigationEvent { NEXT, PREV };
    
    private JButton prevButton;
    private JButton nextButton;
    private JLabel currentChangeLabel;
    private int currentChange = 0;
    private int numChanges = 0;
    //private boolean isEnabled = false;
    
    private List<DiffNavigationListener> listeners = new ArrayList<>();
    
//    public DiffNavigator() {
//        this(0,0);
//    }
    
    public DiffNavigator() {
        super(BoxLayout.X_AXIS);
        prevButton = new BasicArrowButton(SwingConstants.WEST);
        prevButton.setEnabled(false);
        currentChangeLabel = new JLabel(); 
        setChangeLabel();
        currentChangeLabel.setEnabled(false);
        nextButton = new BasicArrowButton(SwingConstants.EAST);
//        JButton nextButton = new JButton("\u25ba");
        nextButton.setEnabled(false);
        
        prevButton.addActionListener(this);
        nextButton.addActionListener(this);
        
        this.add(Box.createHorizontalStrut(5));
        this.add(prevButton);
        this.add(Box.createHorizontalStrut(2));
        this.add(currentChangeLabel);
        this.add(Box.createHorizontalStrut(2));
        this.add(nextButton);
        this.add(Box.createHorizontalStrut(5));
    }
//    public DiffNavigator(int curChange, int totChanges ) {
//        super(BoxLayout.X_AXIS);
//        currentChange = curChange;
//        numChanges = totChanges;
////        isEnabled = totChanges > 0;
//        prevButton = new BasicArrowButton(SwingConstants.WEST);
//        prevButton.setEnabled(totChanges > 0);
//        currentChangeLabel = new JLabel(); 
//        setChangeLabel();
//        currentChangeLabel.setEnabled(totChanges > 0);
//        nextButton = new BasicArrowButton(SwingConstants.EAST);
////        JButton nextButton = new JButton("\u25ba");
//        nextButton.setEnabled(totChanges > 0);
//        
//        prevButton.addActionListener(this);
//        nextButton.addActionListener(this);
//        
//        this.add(Box.createHorizontalStrut(5));
//        this.add(prevButton);
//        this.add(Box.createHorizontalStrut(2));
//        this.add(currentChangeLabel);
//        this.add(Box.createHorizontalStrut(2));
//        this.add(nextButton);
//        this.add(Box.createHorizontalStrut(5));
//    }

    public void addDiffNavigationListener(DiffNavigationListener listener) {
        listeners.add(listener);
    }
    
    public void removeDiffNavigationListener(DiffNavigationListener listener) {
        listeners.remove(listener);
    }
    
    private void setChangeLabel() {
        currentChangeLabel.setText( "" + currentChange + "/" + numChanges );
    }
    
    public void setNumChanges(int num) {
        if ( num < 0 ) {
            throw new IllegalArgumentException(Messages.getString("DiffNavigator.num_change_neg"));
        }
        numChanges = num;
        if ( numChanges == 0 ) {
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            currentChangeLabel.setEnabled(false);
        } else {       
            prevButton.setEnabled(true);
            nextButton.setEnabled(true);
            currentChangeLabel.setEnabled(true);
        }
        currentChange = 0;
        
        setChangeLabel();
    }
    
    public void setCurrentChange(int num) {
        if ( num < 0 ) {
            throw new IllegalArgumentException(Messages.getString("DiffNavigator.cur_change_neg"));
        }
        currentChange = num;
        setChangeLabel();
    }
    
//    public void SetEnabled(boolean enabled) {
//        isEnabled = enabled;
//        currentChangeLabel.setEnabled(enabled);
//        if ( isEnabled && numChanges > 0 ) {
//            prevButton.setEnabled(true);
//            nextButton.setEnabled(true);
//        } else {
//            prevButton.setEnabled(false);
//            nextButton.setEnabled(false);            
//        }
//    }

    private void notifyListeners(DiffNavigationEvent type) {
        for(DiffNavigationListener listener: listeners) {
            listener.diffNavigationEvent(type);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        DiffNavigationEvent type = null;
        if ( e.getSource() == nextButton ) {
            notifyListeners(DiffNavigationEvent.NEXT);
        } else if ( e.getSource() == prevButton ) {
            notifyListeners(DiffNavigationEvent.PREV);
        }
    }
   
}
