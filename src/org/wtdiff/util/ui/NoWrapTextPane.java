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

import javax.swing.JTextPane;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

import org.wtdiff.util.ui.text.NoWrapEditorKit;

/**
 * A version of JTextPane where text will not wrap to fit parent containers width.
 * Useful inside of a scrollpane, if text is too wide then horizontal scroll bars
 * appear.
 * 
 * @author davidst
 *
 */
public class NoWrapTextPane extends JTextPane {
    
    //see http://www.java2s.com/Code/Java/Swing-JFC/NonWrappingWrapTextPane.htm
  
    public NoWrapTextPane(StyledDocument doc) {
        super(doc);
    }

    public NoWrapTextPane() {
        super();
    }

    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();

        if ( parent == null )
            return true;
        
        ComponentUI ui = getUI();
        return ui.getPreferredSize(this).width < parent.getWidth();
    }
        
    protected EditorKit createDefaultEditorKit() {
        return new NoWrapEditorKit();
    }

    
}