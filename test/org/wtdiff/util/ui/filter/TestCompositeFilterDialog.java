package org.wtdiff.util.ui.filter;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.wtdiff.util.filter.CompositeNodeFilter;
import org.wtdiff.util.filter.GlobNameFilter;
import org.wtdiff.util.filter.NodeFilter;
import org.wtdiff.util.ui.CommonComponentTestFixture;
import org.wtdiff.util.ui.filter.CompositeFilterDialog.DIALOG_RESULT;

import org.junit.Before;
import org.junit.Test;

import abbot.finder.ComponentNotFoundException;
import abbot.finder.Matcher;
import abbot.finder.matchers.*;
import abbot.tester.JButtonTester;


public class TestCompositeFilterDialog extends CommonComponentTestFixture {

    protected class CompositeFilterDialogThread extends Thread {
        private CompositeFilterDialog dialog;
        private volatile DIALOG_RESULT result;
        public CompositeFilterDialogThread(CompositeFilterDialog d) {
            dialog = d;
        }
        public void run() {
            result = dialog.showDialog();
            System.out.println(result);
        }
        public DIALOG_RESULT getResult() {
            return result;
        }
    };

    private Matcher addButtonMatcher = new Matcher() { 
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Add".equals(((JButton)c).getText());
        }
    };

    private Matcher removeButtonMatcher = new Matcher() { 
        public boolean matches(Component c) {
            return c instanceof JButton
                && "Remove".equals(((JButton)c).getText());
        }
    };

    // note that when this matcher is used a the finder will always throw a ComponenetNotFoundExcption
    // it collects the matching components into a list as a side effect
    private class GlobNameFilterItemPanelCollector implements Matcher {
        ArrayList<GlobNameFilterItemPanel> panels= new ArrayList<>();
        public boolean matches(Component c) {
            if ( c instanceof GlobNameFilterItemPanel ) {
                panels.add( (GlobNameFilterItemPanel)c );
            }
            return false;
        }
        public List<GlobNameFilterItemPanel> getPanels() {
            return panels;
        }
        public void reset() {
            panels.clear();
        }
    };

    
    private Matcher globNameFilterItemPanelMatcher = new Matcher() { 
        public boolean matches(Component c) {
            return c instanceof GlobNameFilterItemPanel;
        }
    };

    private JFrame testFrame;
    
    @Before
    public void setUp() {
        testFrame = new JFrame();
    }
    
    @Test
    public void testNoFilter() throws Exception {
        CompositeFilterDialog dialog = new CompositeFilterDialog(testFrame,null);
        CompositeFilterDialogThread dialogThread=  new CompositeFilterDialogThread(dialog);
        showModalDialog(dialogThread);
        
        // there is an OK button
        JButton okButtom = (JButton)getFinder().find(dialog, okButtonMatcher);
        // there is an Cancel button
        JButton cancel = (JButton)getFinder().find(dialog, cancelButtonMatcher);
        // there is an Add button
        JButton add = (JButton)getFinder().find(dialog, addButtonMatcher);
        // there is a Remove button
        JButton remove = (JButton)getFinder().find(dialog, removeButtonMatcher);

        try {
            GlobNameFilterItemPanel notFound = (GlobNameFilterItemPanel)getFinder().find(dialog, globNameFilterItemPanelMatcher);
            fail();
        } catch (ComponentNotFoundException e) {
            // this is expected
        }
        
        clickCancel(dialog);        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            System.out.println("alive, will sleep");
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(DIALOG_RESULT.CANCEL, dialogThread.getResult());   
        assertEquals(0, dialog.getFilter().size());
    }

    public void testOK() throws Exception {
        CompositeFilterDialog dialog = new CompositeFilterDialog(testFrame,null);
        CompositeFilterDialogThread dialogThread=  new CompositeFilterDialogThread(dialog);
        showModalDialog(dialogThread);

        clickOK(dialog);        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            System.out.println("alive, will sleep");
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(DIALOG_RESULT.OK, dialogThread.getResult());   
        assertEquals(0, dialog.getFilter().size());
    }
    
    @Test
    public void testAddRemove() throws Exception {
        CompositeFilterDialog dialog = new CompositeFilterDialog(testFrame,null);
        CompositeFilterDialogThread dialogThread=  new CompositeFilterDialogThread(dialog);
        showModalDialog(dialogThread);
        
        JButtonTester bTester = new JButtonTester();         
        //bTester.actionClick(closeButton);
        
        // there is an OK button
        JButton okButtom = (JButton)getFinder().find(dialog, okButtonMatcher);
        // there is an Cancel button
        JButton cancel = (JButton)getFinder().find(dialog, cancelButtonMatcher);
        // there is an Add button
        JButton add = (JButton)getFinder().find(dialog, addButtonMatcher);
        // there is a Remove button
        JButton remove = (JButton)getFinder().find(dialog, removeButtonMatcher);

        try {
            GlobNameFilterItemPanel notFound = (GlobNameFilterItemPanel)getFinder().find(dialog, globNameFilterItemPanelMatcher);
            fail();
        } catch (ComponentNotFoundException e) {
            // this is expected
        }
        
        bTester.actionClick(add);
        GlobNameFilterItemPanel foundOne = (GlobNameFilterItemPanel)getFinder().find(dialog, globNameFilterItemPanelMatcher);
        assertEquals("", ((GlobNameFilter) foundOne.getFilter()).getGlob());
        
        GlobNameFilterItemPanelCollector collector = new GlobNameFilterItemPanelCollector();
        try {
            GlobNameFilterItemPanel notFound = (GlobNameFilterItemPanel)getFinder().find(dialog, collector);
        } catch (ComponentNotFoundException e) {
            // this is expected
        }
        assertEquals(1, collector.getPanels().size());
        collector.reset();
        
        bTester.actionClick(add);
        try {
            GlobNameFilterItemPanel notFound = (GlobNameFilterItemPanel)getFinder().find(dialog, collector);
        } catch (ComponentNotFoundException e) {
            // this is expected
        }

        List<GlobNameFilterItemPanel> panels = collector.getPanels();
        assertEquals(2, panels.size());
        // assume order of found panels is order in container
        assertEquals(foundOne, panels.get(0));
        // all the panels have empty glob, so should be filtered out
        assertEquals(0, dialog.getFilter().size());
        
        JTextField textField = (JTextField)getFinder().find(foundOne, textFieldMatcher);
        textField.setText("*");
        assertEquals(1, dialog.getFilter().size());
        
        GlobNameFilterItemPanel foundTwo = panels.get(1);
        JCheckBox checkBox = (JCheckBox) getFinder().find(foundTwo, jCheckBoxMatcher);
        
        checkBox.setSelected(true);

        panels.clear();
        collector.reset();
        foundTwo = null;
        //foundOne = null;
        bTester.actionClick(remove);
        try {
            getFinder().find(dialog, collector);
        } catch (ComponentNotFoundException e) {
            // this is expected
        }

        panels = collector.getPanels();
        assertEquals(1, panels.size());
        // assume order of found panels is order in container
        assertEquals(foundOne, panels.get(0));
        // all the panels have empty glob, so should be filtered out
        assertEquals(1, dialog.getFilter().size());
        
        clickOK(dialog);        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            System.out.println("alive, will sleep");
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(DIALOG_RESULT.OK, dialogThread.getResult());   
        assertEquals(1, dialog.getFilter().size());
    }

    public void testConstruct() throws Exception {
        GlobNameFilter f1 = new GlobNameFilter("1");
        GlobNameFilter f2 = new GlobNameFilter("2");
        CompositeNodeFilter cf = new CompositeNodeFilter();
        cf.add(f1);
        cf.add(f2);
        CompositeFilterDialog dialog = new CompositeFilterDialog(testFrame,cf);
        CompositeFilterDialogThread dialogThread=  new CompositeFilterDialogThread(dialog);
        showModalDialog(dialogThread);

        GlobNameFilterItemPanelCollector collector = new GlobNameFilterItemPanelCollector();
        try {
            GlobNameFilterItemPanel notFound = (GlobNameFilterItemPanel)getFinder().find(dialog, collector);
        } catch (ComponentNotFoundException e) {
            // this is expected
        }
        assertEquals(2, collector.getPanels().size());
        
        clickOK(dialog);        
        if ( dialogThread.isAlive() ) {
            Thread.sleep(10000);
            System.out.println("alive, will sleep");
            if ( dialogThread.isAlive() ) {
                fail("cancel did not close dialog");
            }
        }
        assertEquals(DIALOG_RESULT.OK, dialogThread.getResult());   
        
        CompositeNodeFilter cfout = dialog.getFilter();
        List<NodeFilter> list = cfout.filters();
        
        assertEquals(f1.getGlob(), ((GlobNameFilter)list.get(0)).getGlob() );
        assertEquals(f2.getGlob(), ((GlobNameFilter)list.get(1)).getGlob() );
    }
    

}
