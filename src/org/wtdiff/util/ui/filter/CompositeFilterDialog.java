package org.wtdiff.util.ui.filter;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.wtdiff.util.filter.CompositeNodeFilter;
import org.wtdiff.util.filter.GlobNameFilter;
import org.wtdiff.util.filter.NodeFilter;

public class CompositeFilterDialog extends JDialog implements ActionListener {

    public enum DIALOG_RESULT {
        OK,
        CANCEL,
        CLOSED;
    }
    
    DIALOG_RESULT result = DIALOG_RESULT.CLOSED; 
    //CompositeNodeFilter compositeFilter;
    List<FilterItemPanel> filterList;
    
    JButton addButton;
    JButton removeButton;
    JButton okButton;
    JButton cancelButton;
    JScrollPane filtersScrollPane;
    Box controlsBox;
    Box filtersPanel;
    Box buttonsBox;
    
    public CompositeFilterDialog(Frame parent, CompositeNodeFilter cf) {
        super(parent, Messages.getString("CompositeNodeFilterDialog.dialog_title"), true);
        
        addButton = new JButton("Add");
        addButton.addActionListener(this);
        removeButton = new JButton("Remove");
        removeButton.addActionListener(this);
        
        controlsBox = Box.createHorizontalBox();
        controlsBox.add( Box.createHorizontalStrut(10));
        controlsBox.add(addButton);
        controlsBox.add( Box.createHorizontalStrut(5));
        controlsBox.add(removeButton);
        controlsBox.add( Box.createHorizontalStrut(10));

        add(controlsBox,"North");
        
        filtersPanel = Box.createVerticalBox();
        filtersPanel.add( Box.createVerticalStrut(5));
        
        filtersScrollPane = new JScrollPane(filtersPanel);
        filterList = new ArrayList<FilterItemPanel>();
        if ( cf == null ) {
            //compositeFilter = new CompositeNodeFilter();
        }
        else {
            //compositeFilter = cf;
            for ( NodeFilter f: cf.filters() ) {
                FilterItemPanel panel = new GlobNameFilterItemPanel( (GlobNameFilter)f ); //TODO change hard code cast to factory
                filterList.add(panel);
                filtersPanel.add(panel);
                filtersPanel.add( Box.createVerticalStrut(5));
            }
        }
        add(filtersScrollPane,"Center");

        okButton = new JButton(Messages.getString("CompositeNodeFilterDialog.button_ok"));
        okButton.addActionListener(this);
        cancelButton = new JButton(Messages.getString("CompositeNodeFilterDialog.button_cancel"));
        cancelButton.addActionListener(this);
        buttonsBox = Box.createHorizontalBox();

        buttonsBox.add(Box.createHorizontalStrut(10));
        buttonsBox.add(okButton);
        buttonsBox.add(Box.createHorizontalStrut(5));
        buttonsBox.add(cancelButton);
        buttonsBox.add(Box.createHorizontalStrut(10));
        add(buttonsBox,"South");

    }
    @Override
    public void actionPerformed(ActionEvent event) {
        if ( event.getSource() == addButton ) {
            FilterItemPanel panel = new GlobNameFilterItemPanel( ); //TODO change filter class to dialog
            filterList.add(panel);
            filtersPanel.add(panel);
            filtersPanel.add(Box.createVerticalStrut(5));
            pack();
        }
        else if ( event.getSource() == removeButton ) {
            for (int i = filterList.size() - 1; i >= 0 ; i-- ) {
                FilterItemPanel panel = filterList.get(i);
                if ( panel.isSelected() ) {
                    filterList.remove(i);
                    filtersPanel.remove(panel);
                    pack();
                }
            }
        }
        else if ( event.getSource() == okButton ) {
            result = DIALOG_RESULT.OK;
            setVisible(false);
        }
        else if ( event.getSource() == cancelButton ) {
            result = DIALOG_RESULT.CANCEL;
            setVisible(false);
        }
    }
    
    public DIALOG_RESULT showDialog() {
        pack();
        setVisible(true);
        return result;
    }
    
    public CompositeNodeFilter getFilter() {
        
        CompositeNodeFilter newFilter = new CompositeNodeFilter();
        for( FilterItemPanel p: filterList) {
            if ( p.isInitialized() ) {
                newFilter.add(p.getFilter());
            }
        }
        return newFilter;
    }
}
