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
import java.util.prefs.BackingStoreException;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.wtdiff.util.text.DiffWriter;

public abstract class DocumentDiffWriter extends DiffWriter {

    public static final String STYLE_VERSION = "0";
    protected static final String LS = System.getProperty("line.separator");

    public class ChangeRange {
        final public int begin;
        final public int end;
        
        public ChangeRange(int b, int e) {
            begin = b;
            end = e;
        }
    }
    
    

    protected class StyleSet {
        public Style text;
        public Style indicator;
        public Style lineno;
    }
    
    protected StyleSet info;
    protected StyleSet common;
    protected StyleSet oldOnly;
    protected StyleSet newOnly;
    protected StyleSet changed;
    protected StyleSet warning;
    
    public DocumentDiffWriter() {
    }
    
    protected void initStyleSets(StyledDocument doc) {
        try {
            Style defStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setBackground(defStyle, Color.WHITE); // TODO should be in prefs too!
//            Color bg = StyleConstants.getBackground(defStyle);
//            System.out.println("default text bg: a" + bg.getAlpha() + "r" + bg.getRed() + "g" + bg.getGreen() + "b" + bg.getBlue());

            DocStylePreferences docPrefs = new DocStylePreferences(DocumentDiffWriter.class, STYLE_VERSION);
            info = createStyleSet(docPrefs, doc, defStyle, "info");
            common = createStyleSet(docPrefs, doc, defStyle, "common");
            oldOnly = createStyleSet(docPrefs, doc, defStyle, "oldonly");
            newOnly = createStyleSet(docPrefs, doc, defStyle, "newonly");
            changed = createStyleSet(docPrefs, doc, defStyle, "changed");
            warning = createStyleSet(docPrefs, doc, defStyle, "warning");
            
        } catch (BackingStoreException e) {
            // TODO should log the error, then base everything on default
            e.printStackTrace();
        }
    }

    private StyleSet createStyleSet(DocStylePreferences docPrefs, StyledDocument doc, Style defStyle, String setName)  {
        StyleSet set = new StyleSet();
        set.text = doc.addStyle(setName + "/text", defStyle);
        try {
            docPrefs.applyPreferences(set.text, setName, "text");
            set.indicator = doc.addStyle(setName + "/indicator", defStyle);
            docPrefs.applyPreferences(set.indicator, setName, "indicator");
            set.lineno = doc.addStyle(setName + "/lineno", defStyle);
            docPrefs.applyPreferences(set.lineno, setName, "lineno");
        } catch (BackingStoreException e) {
            // TODO should log the error, then base everything on default
            e.printStackTrace();
        }        

        return set;
    }
    
    protected StyleSet styleSetForChangeType(ChangeType type) {
        switch (type) {
            case CHANGED:
                return changed;
            case COMMON:
                return common;
            case NEW_ONLY:
                return newOnly;
            case OLD_ONLY:
                return oldOnly;
            case WARNING:
                return warning;
            default:
                throw new IllegalArgumentException( "BUG unknown change type " + type);            
        }
    }
    


}
