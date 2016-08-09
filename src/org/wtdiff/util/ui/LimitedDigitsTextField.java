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

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

public class LimitedDigitsTextField extends JTextField {
    
    private int maxDigits;
    public LimitedDigitsTextField(int maxCols) {
        super(maxCols);
        
        if ( maxCols <= 0 )
            throw new IllegalArgumentException("maxCols not greater than zero.");
        
        maxDigits = maxCols;
    }

    public int getValue() {
        String text = getText();
        if ( text == null || text.length() == 0 )
            return 0;
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
    protected Document createDefaultModel() {
        return new LimitedDigitsDocument();
    }
    
    private class LimitedDigitsDocument extends PlainDocument {
        
        public void insertString(int offs, String str, AttributeSet a)
            throws BadLocationException {

            if (str == null) {
                return;
            }
            char[] chars = str.toCharArray();
            StringBuilder sb = new StringBuilder();
            int maxToInsert = maxDigits - getLength();
            if ( maxToInsert > chars.length)
                maxToInsert = chars.length;
            for (int i = 0; i < maxToInsert; i++) {
                if ( Character.isDigit(chars[i]) )
                    sb.append(chars[i]);
            }
            super.insertString(offs, sb.toString(), a);
        }
    }
}
