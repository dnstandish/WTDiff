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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.BadLocationException;

import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.text.NonprintingCharStyle;
import org.wtdiff.util.text.TextUtil;
import org.wtdiff.util.text.TextUtil.LineSeparator;
import org.wtdiff.util.ui.DiffNavigationListener;
import org.wtdiff.util.ui.DiffNavigator;
import org.wtdiff.util.ui.DiffNavigator.DiffNavigationEvent;
import org.wtdiff.util.ui.DiffPanel.DiffType;

import abbot.finder.Matcher;
import abbot.tester.JButtonTester;


public class TestDiffNavigator  extends CommonComponentTestFixture {

    private class TestListener implements DiffNavigationListener {
        private DiffNavigationEvent lastEvent = null;
        private int eventCount = 0;
        @Override
        public synchronized void diffNavigationEvent(DiffNavigationEvent eventType) {
            lastEvent = eventType;
            eventCount++;
        }
        public synchronized int getEventCount() {
            return eventCount;
        }

        public synchronized DiffNavigationEvent getLastEvent() {
            return lastEvent;
        }
        public synchronized void reset() {
            lastEvent = null;
            eventCount = 0;
        }
    }

    private class ExceptionHolder {
        Throwable exception = null;
        public synchronized void storeExcpetion(Throwable e) {
            exception = e;
        }
        public synchronized Throwable getException()  {
            return exception;
        }
        public synchronized void reset()  {
            exception = null;
        }
    }

    private void checkListeners(int expectedCount, DiffNavigationEvent expectedEvent, TestListener ... listeners) {
        for(int i = 0 ; i < listeners.length; i++) {
            assertEquals(expectedCount, listeners[i].getEventCount());
            assertEquals(expectedEvent, listeners[i].getLastEvent());
        }
    }

    private void resetListeners(TestListener ... listeners) {
        for(int i = 0 ; i < listeners.length; i++) {
            listeners[i].reset();
        }
    }
    
    private void checkDiffNavigation(int nListeners) throws Exception {

        TestListener[] listeners = new TestListener[nListeners];
        for(int i = 0 ; i < nListeners; i++) {
            listeners[i] = new TestListener();
        }
        
        final DiffNavigator nav = new DiffNavigator();
        
        for(int i = 0 ; i < nListeners; i++) {
            nav.addDiffNavigationListener(listeners[i]);
        }

        final Frame frame = showFrame( nav );

        JLabel navLabel =  (JLabel) getFinder().find(new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof JLabel;
            }
        });
        
        JButton prevButton =  (JButton) getFinder().find(nav, new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof BasicArrowButton &&
                    ((BasicArrowButton)c).getDirection() == SwingConstants.WEST;
            }
        });
        
        JButton nextButton =  (JButton) getFinder().find(nav, new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof BasicArrowButton &&
                    ((BasicArrowButton)c).getDirection() == SwingConstants.EAST;
            }
        });
        
        JButtonTester bTester = new JButtonTester();
        
        assertEquals("0/0", navLabel.getText());
        checkListeners(0, null, listeners);
        assertFalse(prevButton.isEnabled());
        assertFalse(nextButton.isEnabled());
        
        bTester.actionClick(nextButton);
        assertEquals("0/0", navLabel.getText());
        checkListeners(0, null, listeners);
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                nav.setNumChanges(11);
            }
            
        });
        assertEquals("0/11", navLabel.getText());
        assertTrue(prevButton.isEnabled());
        assertTrue(nextButton.isEnabled());
        checkListeners(0, null, listeners);

        bTester.actionClick(nextButton);
        assertEquals("0/11", navLabel.getText());
        checkListeners(1, DiffNavigationEvent.NEXT, listeners);
        resetListeners(listeners);
        invokeAndWait( new Runnable () {
            @Override
            public void run() {           
                nav.setNumChanges(2);
                nav.setCurrentChange(1);
            }            
        });

        assertEquals("1/2", navLabel.getText());
        checkListeners(0, null, listeners);
        assertTrue(prevButton.isEnabled());
        assertTrue(nextButton.isEnabled());
        bTester.actionClick(prevButton);
        assertEquals("1/2", navLabel.getText());
        checkListeners(1, DiffNavigationEvent.PREV, listeners);
        resetListeners(listeners);
        
        bTester.actionClick(prevButton);
        bTester.actionClick(nextButton);
        assertEquals("1/2", navLabel.getText());
        checkListeners(2, DiffNavigationEvent.NEXT, listeners);
        resetListeners(listeners);
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {           
                frame.removeAll();
            }            
        });
        
    }

    @Test
    public void testDiffNavigationNoListeners() throws Exception {

        checkDiffNavigation(0);
    }

    @Test
    public void testDiffNavigationOneListener() throws Exception {
        
        checkDiffNavigation(1);

    }
    @Test
    public void testDiffNavigationTwoListeners() throws Exception {

        checkDiffNavigation(2);

    }

    @Test
    public void testDiffNavigationBadChange() throws Exception {

        final ExceptionHolder eHolder = new ExceptionHolder();
        final DiffNavigator nav = new DiffNavigator();
        
        final Frame frame = showFrame( nav );

        JLabel navLabel =  (JLabel) getFinder().find(new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof JLabel;
            }
        });
        
        assertEquals("0/0", navLabel.getText());
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() { 
                try {
                    nav.setNumChanges(-1);
                } catch (Throwable t) {
                    eHolder.storeExcpetion(t);
                }
            }
            
        });
        assertNotNull( eHolder.getException() );
        assertTrue( eHolder.getException() instanceof IllegalArgumentException );
        eHolder.reset();
        assertEquals("0/0", navLabel.getText());
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {           
                try {
                    nav.setNumChanges(2);
                } catch (Throwable t) {
                    eHolder.storeExcpetion(t);
                }
            }            
        });
        assertNull( eHolder.getException() );
        assertEquals("0/2", navLabel.getText());

        invokeAndWait( new Runnable () {
            @Override
            public void run() {           
                try {
                    nav.setCurrentChange(-1);
                } catch (Throwable t) {
                    eHolder.storeExcpetion(t);
                }
            }            
        });
        assertNotNull( eHolder.getException() );
        assertTrue( eHolder.getException() instanceof IllegalArgumentException );
        eHolder.reset();
        assertEquals("0/2", navLabel.getText());

        invokeAndWait( new Runnable () {
            @Override
            public void run() {           
                frame.removeAll();
            }            
        });
        
    }

    @Test
    public void testRemoveListener() throws Exception {

        TestListener listener1 = new TestListener();
        TestListener listener2 = new TestListener();
        final DiffNavigator nav = new DiffNavigator();

        // no problem if listener not there
        nav.removeDiffNavigationListener(listener1);

        nav.addDiffNavigationListener(listener1);
        nav.addDiffNavigationListener(listener2);

        final Frame frame = showFrame( nav );

//        JLabel navLabel =  (JLabel) getFinder().find(new  Matcher() {
//            public boolean matches(Component c) {
//                return c instanceof JLabel;
//            }
//        });
        
//        JButton prevButton =  (JButton) getFinder().find(nav, new  Matcher() {
//            public boolean matches(Component c) {
//                return c instanceof BasicArrowButton &&
//                    ((BasicArrowButton)c).getDirection() == SwingConstants.WEST;
//            }
//        });
        
        JButton nextButton =  (JButton) getFinder().find(nav, new  Matcher() {
            public boolean matches(Component c) {
                return c instanceof BasicArrowButton &&
                    ((BasicArrowButton)c).getDirection() == SwingConstants.EAST;
            }
        });
        

        invokeAndWait( new Runnable () {
            @Override
            public void run() {                
                nav.setNumChanges(11);
            }
            
        });

        JButtonTester bTester = new JButtonTester();
        
//        assertEquals("0/11", navLabel.getText());
        bTester.actionClick(nextButton);
        checkListeners(1, DiffNavigationEvent.NEXT, listener1, listener2);
        resetListeners(listener1, listener2);
        
        nav.removeDiffNavigationListener(listener1);
        bTester.actionClick(nextButton);
        checkListeners(0, null, listener1);
        checkListeners(1, DiffNavigationEvent.NEXT, listener2);
        resetListeners(listener1, listener2);
        
        nav.removeDiffNavigationListener(listener2);
        bTester.actionClick(nextButton);
        checkListeners(0, null, listener1, listener2);
        resetListeners(listener1, listener2);
        
        
        invokeAndWait( new Runnable () {
            @Override
            public void run() {           
                frame.removeAll();
            }            
        });
        
    }

}
