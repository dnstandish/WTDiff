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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.lang.ref.WeakReference;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class NodeTreeCellRenderer extends DefaultTreeCellRenderer {

    private WeakReference<Object> emphasizedNode = null;
    private Font normalFont;
    private Font emphasizedFont;
    public void setEmphasizedNode(WeakReference<Object> wr) {
        if ( emphasizedNode == null) {
            normalFont = getFont();
            emphasizedFont = normalFont.deriveFont(Font.BOLD);            
        }
        emphasizedNode = wr;
    }
    
    public void clearEmphasizedNode() {
        emphasizedNode = null;
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent( tree,  value,  sel,  expanded,  leaf,  row,  hasFocus);
        if ( emphasizedNode != null ) {
            if ( value == emphasizedNode.get() ) {
                setForeground(Color.RED);  //TODO should not be hardcoded here
                setFont(emphasizedFont);
            } else {                
                setForeground(sel ? textSelectionColor: textNonSelectionColor);
                setFont(normalFont);
            }
        } else {
            setForeground(sel ? textSelectionColor: textNonSelectionColor);
            setFont(normalFont);            
        }
        return this;
    }
}
