package org.wtdiff.util.ui.filter;

import org.wtdiff.util.filter.NodeFilter;

public interface FilterItem {

        public boolean isSelected();
        public boolean isInitialized();
        //public boolean isValidFilter();
        public NodeFilter getFilter();
        
}
