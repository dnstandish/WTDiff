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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;

import org.wtdiff.util.CompareController;
import org.wtdiff.util.ui.DirCmpFrame;
import org.wtdiff.util.ui.CommonComponentTestFixture.JMenuBarMatcher;
import org.wtdiff.util.ui.CommonComponentTestFixture.JMenuItemMatcher;
import org.wtdiff.util.ui.CommonComponentTestFixture.JMenuMatcher;

import abbot.finder.Matcher;
import abbot.tester.JButtonTester;

import junit.extensions.abbot.ComponentTestFixture;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDirCmpFrame  extends CommonComponentTestFixture {

    private class JMenuBarMatcher implements Matcher {
        
        public boolean matches(Component c) {
            return c instanceof JMenuBar;
        }
    }

    private class JMenuMatcher implements Matcher {
        
        private String text;
        public JMenuMatcher(String text) {
            this.text = text;
        }
        
        public boolean matches(Component c) {
            return c instanceof JMenu &&
                text.equals(((JMenu)c).getText());
        }
    }
    
    private class JMenuItemMatcher implements Matcher {
        
        private String text;
        public JMenuItemMatcher(String text) {
            this.text = text;
        }
        
        public boolean matches(Component c) {
            return c instanceof JMenuItem &&
                text.equals(((JMenuItem)c).getText());
        }
    }

    private class JCheckBoxMenuItemMatcher implements Matcher {
        
        private String text;
        public JCheckBoxMenuItemMatcher(String text) {
            this.text = text;
        }
        
        public boolean matches(Component c) {
            return c instanceof JCheckBoxMenuItem &&
                text.equals(((JCheckBoxMenuItem)c).getText());
        }
    }

    @Test
    public void testExitMenuItem() throws Exception {
        final DirCmpFrame appFrame = new DirCmpFrame(  );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
        final Object lock = new Object();
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) {
                    synchronized (lock) {
                        appFrame.setVisible(false);
                        lock.notify();
                    }
                }
           }
        );
    
        appFrame.setVisible(true);
        
        JMenuBar menuBar = (JMenuBar)getFinder().find( appFrame, new JMenuBarMatcher() );
        JMenu fileMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("File") );
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(fileMenu);
        JMenuItem exitItem = (JMenuItem)getFinder().find( menuBar, new JMenuItemMatcher("Exit") );
        bTester.actionClick(exitItem);
        synchronized(lock) {
            try {
                lock.wait(1000);
                if (appFrame.isVisible()) {
                    fail("Frame not closed after exit menu item clicked");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        appFrame.dispose();
    }

    @Test
    public void testHelpAboutMenuItem() throws Exception {
        final DirCmpFrame appFrame = new DirCmpFrame(  );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
        final Object lock = new Object();
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) {
                    synchronized (lock) {
                        appFrame.setVisible(false);
                        lock.notify();
                    }
                }
           }
        );
    
        appFrame.setVisible(true);
        
        JMenuBar menuBar = (JMenuBar)getFinder().find( appFrame, new JMenuBarMatcher() );
        JMenu helpMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("Help") );
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(helpMenu);
        JMenuItem aboutItem  = (JMenuItem)getFinder().find( menuBar, new JMenuItemMatcher("About") );
        bTester.actionClick(aboutItem);
        JDialog aboutDialog = (JDialog)getFinder().find(new Matcher() {
            public boolean matches(Component c) {
                return c instanceof JDialog;
            }
        });
        clickOK(aboutDialog);
        
        appFrame.dispatchEvent(new WindowEvent(appFrame, WindowEvent.WINDOW_CLOSING));
        synchronized(lock) {
            try {
                lock.wait(1000);
                if (appFrame.isVisible()) {
                    fail("Frame not closed after exit menu item clicked");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        appFrame.dispose();
    }

    @Test
    public void testIgnoreNameCaseMenuItem() throws Exception {
        CompareController controller = new CompareController();
        final DirCmpFrame appFrame = new DirCmpFrame( controller );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
        final Object lock = new Object();
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) {
                    synchronized (lock) {
                        appFrame.setVisible(false);
                        lock.notify();
                    }
                }
           }
        );
    
        appFrame.setVisible(true);
        
        JMenuBar menuBar = (JMenuBar)getFinder().find( appFrame, new JMenuBarMatcher() );
        JMenu optionMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("Options") );
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(optionMenu);
        
        JCheckBoxMenuItem ignoreCaseItem = (JCheckBoxMenuItem)getFinder().find( optionMenu, new JCheckBoxMenuItemMatcher("Ignore name case") );
        bTester.actionClick(ignoreCaseItem);
        assertTrue( controller.getIgnoreNameCase());
        
        appFrame.dispatchEvent(new WindowEvent(appFrame, WindowEvent.WINDOW_CLOSING));
        synchronized(lock) {
            try {
                lock.wait(1000);
                if (appFrame.isVisible()) {
                    fail("Frame not closed after exit menu item clicked");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        appFrame.dispose();
    }
    
    @Test
    public void testTextCompareMenuItem() throws Exception {
        CompareController controller = new CompareController();
        final DirCmpFrame appFrame = new DirCmpFrame( controller );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
        final Object lock = new Object();
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) {
                    synchronized (lock) {
                        appFrame.setVisible(false);
                        lock.notify();
                    }
                }
           }
        );
    
        appFrame.setVisible(true);
        
        JMenuBar menuBar = (JMenuBar)getFinder().find( appFrame, new JMenuBarMatcher() );
        JMenu optionMenu = (JMenu)getFinder().find( menuBar, new JMenuMatcher("Options") );
        JButtonTester bTester = new JButtonTester(); 
        bTester.actionClick(optionMenu);
        
        JCheckBoxMenuItem textCompareItem = (JCheckBoxMenuItem)getFinder().find( optionMenu, new JCheckBoxMenuItemMatcher("Text compare") );
        bTester.actionClick(textCompareItem);
        assertTrue( controller.getTextCompare());
        
        appFrame.dispatchEvent(new WindowEvent(appFrame, WindowEvent.WINDOW_CLOSING));
        synchronized(lock) {
            try {
                lock.wait(1000);
                if (appFrame.isVisible()) {
                    fail("Frame not closed after exit menu item clicked");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        appFrame.dispose();
    }
    

}
