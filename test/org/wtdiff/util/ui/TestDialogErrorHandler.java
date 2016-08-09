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

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ui.DialogErrorHandler;

import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.MultipleComponentsFoundException;
import abbot.tester.JButtonTester;

public class TestDialogErrorHandler extends CommonComponentTestFixture {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private void recursiveGetJButtons(Container container, List<JButton> buttons) {
        for ( Component c: container.getComponents() ) {
            if ( c instanceof JButton ) {
                buttons.add((JButton)c);
            } else if ( c instanceof Container ) {
                recursiveGetJButtons( (Container)c, buttons);
            }
        }
    }
    
    private class HandlerThread extends Thread {
        Boolean wasHandled = null;
        boolean isLogOnlyThread;
        DialogErrorHandler handler;
        Exception excpetion;
        public HandlerThread(DialogErrorHandler h, Exception e, boolean tryLog) {
            isLogOnlyThread = tryLog;
            handler = h;
            excpetion = e;
        }
        
        public boolean wasHandled() {
            return wasHandled;
        }
        public void run() {
            if (isLogOnlyThread) {
                handler.logError(excpetion);
            } else {
                if ( handler.handleError(excpetion) )
                    wasHandled = Boolean.TRUE;
                else
                    wasHandled = Boolean.FALSE;
            }          
        }        
    }
    private void startHandlerThread(HandlerThread t) throws InterruptedException {
        t.start();
        Thread.sleep(500);
    }
    
    @Test
    public void testLogError() throws ComponentNotFoundException, MultipleComponentsFoundException, InterruptedException {

        DialogErrorHandler handler = new DialogErrorHandler();
        
        String message = "testLogError";
        Exception exception = new Exception(message);
        HandlerThread ht = new HandlerThread(handler,exception, true);
        startHandlerThread( ht );

        // brings up a message dialog
        JDialog dialog = (JDialog)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JDialog;
            }
        });
        
        List<JButton> buttons = new ArrayList<>();
        recursiveGetJButtons(dialog, buttons);
        assertEquals(1, buttons.size());
        
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
        assertEquals(message, messageLabel.getText());
        JButtonTester bTester = new JButtonTester();  
        bTester.actionClick(okButton);

        ht.join(1000);
        assertFalse(ht.isAlive());
        
        assertTrue( handler.encounteredError() );
    }

    private void clickOn(String desired, List<JButton> buttons) {
        JButtonTester bTester = new JButtonTester();  
        for(JButton b : buttons) {
            if ( desired.equals( b.getText() ) ) {
//                b.doClick();
                bTester.actionClick(b);
                return;
            }
        }
        fail("button with label " + desired + "does not exist");
    }
    
    @Test
    public void testHandleErrorCancel() throws ComponentNotFoundException, MultipleComponentsFoundException, InterruptedException {

        DialogErrorHandler handler = new DialogErrorHandler();
        
        String message = "testHandleErrorCancel";
        Exception exception = new Exception(message);
        HandlerThread ht = new HandlerThread(handler,exception, false);
        startHandlerThread( ht );
        
        // brings up a message dialog
        JDialog dialog = (JDialog)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JDialog;
            }
        });
        
        List<JButton> buttons = new ArrayList<>();
        recursiveGetJButtons(dialog, buttons);
        assertEquals(3, buttons.size());
        
        JLabel messageLabel = (JLabel)getFinder().find(dialog, new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JLabel;
            }
        });
        assertEquals(message, messageLabel.getText());
        
        boolean foundCancelButton = false;
        JButton cancelButton = null;
        boolean foundIgnoreButton = false;
        boolean foundIgnoreAllButton = false;
        for(JButton b : buttons) {
            String text = b.getText();
            switch ( text ) {
                case "Cancel":
                    foundCancelButton = true;
                    cancelButton = b;
                    break;
                    
                case "Ignore":
                    foundIgnoreButton = true;
                    break;

                case "Ignore All":
                    foundIgnoreAllButton = true;
                    break;
                    
                default:
                    fail("unexpected button label " + text);
                    break;
            }
        }
        assertTrue(foundCancelButton);
        assertTrue(foundIgnoreButton);
        assertTrue(foundIgnoreAllButton);
        
        JButtonTester bTester = new JButtonTester();  
        bTester.actionClick(cancelButton);
//        cancelButton.doClick();

        ht.join(1000);
        assertFalse(ht.isAlive());
        assertFalse(ht.wasHandled());
        assertTrue( handler.encounteredError() );

    }
    
    @Test
    public void testHandleErrorIgnore() throws ComponentNotFoundException, MultipleComponentsFoundException, InterruptedException {

        DialogErrorHandler handler = new DialogErrorHandler();
        
        String message = "testHandleErrorIgnore";
        Exception exception = new Exception(message);
        {
            HandlerThread ht = new HandlerThread(handler,exception, false);
            startHandlerThread(ht);
            
            JDialog dialog = (JDialog)getFinder().find(new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JDialog;
                }
            });
    
            List<JButton> buttons = new ArrayList<>();
            recursiveGetJButtons(dialog, buttons);
            clickOn("Ignore", buttons);
    
            ht.join(1000);
            assertFalse(ht.isAlive());
            assertTrue(ht.wasHandled());
            assertTrue( handler.encounteredError() );
        }
        {  // if we give it another exception to handle it will prompt again
            HandlerThread ht = new HandlerThread(handler,exception, false);
            startHandlerThread(ht);

            JDialog dialog = (JDialog)getFinder().find(new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JDialog;
                }
            });
    
            List<JButton> buttons = new ArrayList<>();
            recursiveGetJButtons(dialog, buttons);
            clickOn("Ignore", buttons);
    
            ht.join(1000);
            assertFalse(ht.isAlive());
            assertTrue(ht.wasHandled());
            assertTrue( handler.encounteredError() );
        }
    }
    @Test
    public void testHandleErrorIgnoreAll() throws ComponentNotFoundException, MultipleComponentsFoundException, InterruptedException {

        DialogErrorHandler handler = new DialogErrorHandler();
        
        String message = "testHandleErrorIgnoreAll";
        Exception exception = new Exception(message);
        {
            HandlerThread ht = new HandlerThread(handler,exception, false);
            startHandlerThread(ht);

            JDialog dialog = (JDialog)getFinder().find(new Matcher() {
                public boolean matches(Component c) {
                    return c instanceof JDialog;
                }
            });
    
            List<JButton> buttons = new ArrayList<>();
            recursiveGetJButtons(dialog, buttons);
            clickOn("Ignore All", buttons);
    
            ht.join(1000);
            assertFalse(ht.isAlive());
            assertTrue(ht.wasHandled());
            assertTrue( handler.encounteredError() );
        }
        {  // if we give it another exception to handle it will not prompt again
            HandlerThread ht = new HandlerThread(handler,exception, false);
            startHandlerThread(ht);

            ht.join(1000);
            assertFalse(ht.isAlive());
            assertTrue(ht.wasHandled());
            assertTrue( handler.encounteredError() );
        }
    }
}
