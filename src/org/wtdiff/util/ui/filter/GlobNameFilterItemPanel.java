package org.wtdiff.util.ui.filter;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.wtdiff.util.filter.GlobNameFilter;
import org.wtdiff.util.filter.NodeFilter;

public class GlobNameFilterItemPanel extends FilterItemPanel {

    public enum IncludeExclude {
        INCLUDE,
        EXCLUDE;
        
        public String localizedString() {
            return Messages.getString("Filter.IncludeExclude." + this.toString());
        }
    }
    
    private String glob;
    private JTextField globText;
    private JCheckBox selected;
    private JComboBox<String> includeExclude;

    public GlobNameFilterItemPanel() {
        this(null);
    }
     
    public GlobNameFilterItemPanel(GlobNameFilter f) {
        if ( f == null )
            glob = "";
        else
            glob = f.getGlob();
        
        globText = new JTextField(glob, 10);
        selected = new JCheckBox();
        selected.setSelected(false);
        includeExclude = new JComboBox<>();
        includeExclude.addItem(IncludeExclude.EXCLUDE.localizedString());
        includeExclude.addItem(IncludeExclude.INCLUDE.localizedString());
        includeExclude.setSelectedIndex(0);
        includeExclude.setEnabled(false);
        
        this.setLayout(new BorderLayout());
        
        this.add(selected, "West");
        this.add(includeExclude, "Center");
        this.add(globText, "East");
    }
     
    @Override
    public boolean isSelected() {
        return  selected.isSelected();
    }

    @Override
    public boolean isInitialized() {
        return globText.getText().length() > 0;
    }

    @Override
    public NodeFilter getFilter() {
        return new GlobNameFilter(globText.getText());
    }

        
}
