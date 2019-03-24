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

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JTextField;


import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;
import abbot.tester.JButtonTester;

import junit.extensions.abbot.ComponentTestFixture;

public class CommonComponentTestFixture   extends ComponentTestFixture {
    
    // TODO can this be achieved via ComponentTestFixture.showModalDialog() ?
    protected class ModalDialogThread extends Thread {
        private JDialog dialog;
        public ModalDialogThread(JDialog d) {
            dialog = d;
        }
        public void run() {
            dialog.setVisible(true);
        }
    };

    protected class JLabelAndTextMatcher implements Matcher {
        private String matcherText;
        public void setText(String text) {
            matcherText = text;
            
        }
        public boolean matches(Component c) {
            return c instanceof JLabel
                && matcherText.equals(((JLabel)c).getText());
        }
    }
    protected class JTextFieldMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JTextField;
        }
    }
    protected class OKJButtonMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "OK".equals(((JButton)c).getText());
        }
    }
    protected class CancelJButtonMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Cancel".equals(((JButton)c).getText());
        }
    }
    protected class CloseJButtonMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Close".equals(((JButton)c).getText());
        }
    }
    protected class SelectJButtonMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Select".equals(((JButton)c).getText());
        }
    }
    protected class WriteJButtonMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Write".equals(((JButton)c).getText());
        }
    }

    protected class HorizontalJScrollBarMatcher implements Matcher {
        public boolean matches(Component c) {
            return c instanceof JScrollBar
                && ((JScrollBar)c).getOrientation() == JScrollBar.HORIZONTAL;
        }
    }
    
    protected class JMenuBarMatcher implements Matcher {
        
        public boolean matches(Component c) {
            return c instanceof JMenuBar;
        }
    }

    protected class JMenuMatcher implements Matcher {
        
        private String text;
        public JMenuMatcher(String text) {
            this.text = text;
        }
        
        public boolean matches(Component c) {
            return c instanceof JMenu &&
                text.equals(((JMenu)c).getText());
        }
    }
    
    protected class JMenuItemMatcher implements Matcher {
        
        private String text;
        public JMenuItemMatcher(String text) {
            this.text = text;
        }
        
        public boolean matches(Component c) {
            return c instanceof JMenuItem &&
                text.equals(((JMenuItem)c).getText());
        }
    }

    protected class JCheckBoxMatcher implements Matcher {
        
        public JCheckBoxMatcher() {
        }
        
        public boolean matches(Component c) {
            return c instanceof JCheckBox;
        }
    }

    protected class JCheckBoxMenuItemMatcher implements Matcher {
        
        private String text;
        public JCheckBoxMenuItemMatcher(String text) {
            this.text = text;
        }
        
        public boolean matches(Component c) {
            return c instanceof JCheckBoxMenuItem &&
                text.equals(((JCheckBoxMenuItem)c).getText());
        }
    }

    protected JLabelAndTextMatcher labelMatcher = new JLabelAndTextMatcher();
    protected JTextFieldMatcher textFieldMatcher = new JTextFieldMatcher();
    protected JCheckBoxMatcher jCheckBoxMatcher = new JCheckBoxMatcher();
    protected OKJButtonMatcher okButtonMatcher = new OKJButtonMatcher();
    protected CancelJButtonMatcher cancelButtonMatcher = new CancelJButtonMatcher();
    protected CloseJButtonMatcher closeButtonMatcher = new CloseJButtonMatcher();
    protected SelectJButtonMatcher selectButtonMatcher = new SelectJButtonMatcher();
    protected WriteJButtonMatcher writeButtonMatcher = new WriteJButtonMatcher();
    protected HorizontalJScrollBarMatcher hJScrollBarMatcher = new HorizontalJScrollBarMatcher();
    
    protected JScrollBar findHorizontalJScrollbar(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        return (JScrollBar)getFinder().find(parent, hJScrollBarMatcher);        
    }
    
    protected void verifyErrorDialogMessage(String message, final JDialog excludeDialog) throws MultipleComponentsFoundException {
        verifyErrorDialogMessage(message, excludeDialog, false);        

    }
    
    protected void verifyErrorDialogMessage(String message, final JDialog excludeDialog, boolean exact) throws MultipleComponentsFoundException {        
        try {
            JDialog dialog = (JDialog)getFinder().find(new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JDialog
                        && c != excludeDialog;
                }
            });

            JButton okButton = (JButton)getFinder().find(dialog, new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JButton
                        && "OK".equals(((JButton)c).getText());
                }
            });
            
            JLabel messageLabel = (JLabel)getFinder().find(dialog, new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JLabel;
                }
            });
            if ( exact ) {
            	assertEquals(message, messageLabel.getText());
            } else {
            	assertTrue( message.contains(message) );
            }
            
            JButtonTester bTester = new JButtonTester();  
            bTester.actionClick(okButton);
        } catch ( ComponentNotFoundException cnfe) {
            fail("verifyErrorDialogMessage failure finding components");
//            return false;
        }
//        return true;
    }

    protected void clickOK(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton okButton = (JButton)getFinder().find(parent, okButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(okButton);
    }

    protected void clickCancel(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton cancelButton = (JButton)getFinder().find(parent, cancelButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(cancelButton);
    }

    protected void clickClose(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton closeButton = (JButton)getFinder().find(parent, closeButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(closeButton);
    }

    protected void clickSelect(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton selectButton = (JButton)getFinder().find(parent, selectButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(selectButton);
    }

    protected void clickWrite(Container parent) throws ComponentNotFoundException, MultipleComponentsFoundException {
        JButton writeButton = (JButton)getFinder().find(parent, writeButtonMatcher);
        JButtonTester bTester = new JButtonTester();         
        bTester.actionClick(writeButton);
    }
}
