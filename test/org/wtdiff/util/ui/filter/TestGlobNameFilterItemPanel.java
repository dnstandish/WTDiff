package org.wtdiff.util.ui.filter;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.wtdiff.util.filter.GlobNameFilter;
import org.wtdiff.util.ui.CommonComponentTestFixture;

import org.junit.Test;
import abbot.finder.matchers.*;


public class TestGlobNameFilterItemPanel extends CommonComponentTestFixture {

    @Test
    public void testNoFilter() throws Exception {
        GlobNameFilterItemPanel panel = new GlobNameFilterItemPanel();
        showFrame(panel);
        
        JTextField globField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        assertEquals( "", globField.getText() );
        
        JComboBox includeExclude = (JComboBox)getFinder().find(new ClassMatcher(JComboBox.class));
        assertFalse(includeExclude.isEnabled());
        assertEquals("Exclude", includeExclude.getSelectedItem());
        
        assertFalse(panel.isSelected());
        JCheckBox selected = (JCheckBox)getFinder().find(new ClassMatcher(JCheckBox.class));
        assertFalse(selected.isSelected());
        selected.setSelected(true);
        assertTrue(panel.isSelected());
        
        assertFalse(panel.isInitialized());
        {
            GlobNameFilter filter = (GlobNameFilter)panel.getFilter();
            assertEquals("", filter.getGlob());
        }
        globField.setText("abc");
        assertTrue(panel.isInitialized());
        {
            GlobNameFilter filter = (GlobNameFilter)panel.getFilter();
            assertEquals("abc", filter.getGlob());
        }
        globField.setText("");
        assertFalse(panel.isInitialized());
        {
            GlobNameFilter filter = (GlobNameFilter)panel.getFilter();
            assertEquals("", filter.getGlob());
        }
    }

    @Test
    public void testWithFilter() throws Exception {
        
        GlobNameFilter origFilter = new GlobNameFilter("*.bak");
        GlobNameFilterItemPanel panel = new GlobNameFilterItemPanel(origFilter);
        showFrame(panel);
        
        JTextField globField = (JTextField)getFinder().find(new ClassMatcher(JTextField.class));
        assertEquals( "*.bak", globField.getText() );
        
        assertTrue(panel.isInitialized());
        {
            GlobNameFilter filter = (GlobNameFilter)panel.getFilter();
            assertEquals("*.bak", filter.getGlob());
        }
        globField.setText("");
        assertFalse(panel.isInitialized());
        {
            GlobNameFilter filter = (GlobNameFilter)panel.getFilter();
            assertEquals("", filter.getGlob());
            assertFalse( origFilter == filter);  // does not modify original filter
        }
        System.out.println("end of test");
    }

}
