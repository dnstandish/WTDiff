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
package org.wtdiff.util.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DiffWriter {

    public enum ChangeType {
        COMMON,
        NEW_ONLY,
        OLD_ONLY,
        WARNING,
        CHANGED,
    }

    private NonprintingCharStyle nonprintingStyle = NonprintingCharStyle.ASIS;
    private int tabWidth = 0;
    private boolean isNumberLines = false;
    protected String lineNumberFormat = "%03d"; // default to 3 digits

    public abstract void append(List<String> oldLines, int oldBeginLineOffset, List<String> newLines, int newBeginLineOffset);

    public abstract void newChange(ChangeType type, int oldBegin, int oldEnd, int newBegin, int newEnd);

    public void totalLinesHint(int nLines) {
        lineNumberFormat = "%0" + numDigits(nLines) + "d";
    }
    
    private int numDigits(int n) {
        int numDigits;
        if ( n == 0 ) 
            numDigits = 1;
        else if ( n < 0 )
            numDigits = (int)Math.log10( (double)(-n) ) + 2;
        else
            numDigits = (int)Math.log10( (double)(n) ) + 1;
        
        return numDigits;
    }

    public void setControlCharaterHandling(NonprintingCharStyle nonPrintingCharacterHandling) {
        if ( nonPrintingCharacterHandling == null )
            throw new IllegalArgumentException("BUG attempt to set control character handling style to null");
        nonprintingStyle = nonPrintingCharacterHandling;        
    }
    
    public NonprintingCharStyle getControlCharaterHandling() {
        return nonprintingStyle;        
    }
    
    public void setTabWidth(int width) {
        tabWidth = width;
    }

    public int getTabWidth() {
        return tabWidth;
    }
    
    public void setNumberLines(boolean numberLines) {
        isNumberLines = numberLines;
    }
    
    public boolean isNumberLines() {
        return isNumberLines;
    }

    protected String adjustNonprintingCharacters(String s) {
        String newS = s;
        switch ( nonprintingStyle ) {
        case ASIS:
            break;
        case BOX:
            newS = TextUtil.boxContolChars(s);
            break;
        case ESCAPE:
            newS = TextUtil.visibleContolChars(s);
            break;
        case REMOVE:
            newS = TextUtil.removeContolChars(s);
            break;
        default:
            throw new IllegalArgumentException( "BUG invalid non-printing character handling setting " + nonprintingStyle );
            //break;
        }
        return newS;
    }

    protected String formatLine(String line) {
        String newLine = adjustNonprintingCharacters(line);
        // tab expansion
        if ( getTabWidth() > 0 ) {
            newLine = TextUtil.expandTabs(newLine, getTabWidth());
        }
        return newLine;
    }
    
    protected String lineNumberToString( int lineNo ) {
        return String.format(lineNumberFormat, lineNo);
    }
}
