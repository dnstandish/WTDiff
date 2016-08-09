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
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.wtdiff.util.text.DiffWriter;
import org.wtdiff.util.text.DiffWriter.ChangeType;
import org.wtdiff.util.ui.DocumentDiffWriter.ChangeRange;
import org.wtdiff.util.ui.DocumentDiffWriter.StyleSet;

public class NormalDocumentDiffWriter extends DocumentDiffWriter {

    private StyledDocument doc;
    private Style defStyle;
    private ChangeType currentType = ChangeType.COMMON;
    private List<ChangeRange> changeList = new ArrayList<>();
    
    public NormalDocumentDiffWriter(StyledDocument aDoc) {
        doc = aDoc;
        initStyleSets(doc);
        defStyle = doc.getStyle(StyleContext.DEFAULT_STYLE);
    }

    @Override
    public void append(List<String> oldLines, int oldBeginLineOffset, List<String> newLines, int newBeginLineOffset)  {
        int begin = doc.getLength();
        try {
            int oldLineNo =  oldBeginLineOffset;
            for( String line: oldLines ) {
                oldLineNo++;
                if ( currentType == ChangeType.COMMON ) {                
                    if ( isNumberLines() ) {
                        doc.insertString(doc.getLength(), lineNumberToString(oldLineNo), common.lineno);
                    }
                    doc.insertString(doc.getLength(), "  ", common.indicator);
                    doc.insertString(doc.getLength(), formatLine(line)  + LS, common.text); 
                } else if ( currentType == ChangeType.WARNING ){    
                    if ( isNumberLines() ) {
                        doc.insertString(doc.getLength(), lineNumberToString(oldLineNo), warning.lineno);
                    }
                    doc.insertString(doc.getLength(), "< ", warning.indicator);
                    doc.insertString(doc.getLength(), formatLine(line)  + LS, warning.text);                    
                } else {    
                    if ( isNumberLines() ) {
                        doc.insertString(doc.getLength(), lineNumberToString(oldLineNo), oldOnly.lineno);
                    }
                    doc.insertString(doc.getLength(), "< ", oldOnly.indicator);
                    doc.insertString(doc.getLength(), formatLine(line)  + LS, oldOnly.text);
                }
            }
            if ( currentType == ChangeType.CHANGED )
                doc.insertString(doc.getLength(), "---" + LS, info.text);
            
            if ( currentType != ChangeType.COMMON ) {
                StyleSet sSet = currentType == ChangeType.WARNING ? warning : newOnly;
                int newLineNo = newBeginLineOffset;
                for( String line: newLines ) {
                    newLineNo++;
                    if ( isNumberLines() )
                        doc.insertString(doc.getLength(), lineNumberToString(newLineNo), sSet.lineno);
                    doc.insertString(doc.getLength(), "> ", sSet.indicator);                
                    doc.insertString(doc.getLength(), formatLine(line)  + LS, sSet.text);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        int end = doc.getLength();
        if ( currentType != ChangeType.COMMON ) {
            changeList.add( new ChangeRange(begin, end));
        }
    }

    private String numberRange(int begin, int end) {
        if ( begin == end )
            return Integer.toString(begin+1);
        if ( begin > end )
            return Integer.toString(begin);
        else
            return Integer.toString(begin+1) + "," + Integer.toString(end+1);
    }
    @Override
    public void newChange(ChangeType type, int oldBegin, int oldEnd, int newBegin, int newEnd) {
        currentType = type;
        try {
            switch ( type ) {
                case COMMON:
                    break;
                case NEW_ONLY:
                    doc.insertString(doc.getLength(), "" + numberRange(oldBegin,oldEnd) + "a" + numberRange(newBegin,newEnd)  + LS, info.text);
                    break;
                case OLD_ONLY:
                    doc.insertString(doc.getLength(), "" + numberRange(oldBegin,oldEnd) +  "d" + numberRange(newBegin,newEnd)  + LS, info.text);
                    break;
                case WARNING:
                    break;
                case CHANGED:
                    doc.insertString(doc.getLength(), "" + numberRange(oldBegin,oldEnd) +  "c" + numberRange(newBegin,newEnd)  + LS, info.text);
                    break;
                default:
                    throw new IllegalArgumentException( "BUG unknown change type " + type);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public int numberOfDifferences() {
        return changeList.size();
    }
    
    public List<ChangeRange> getChangeRanges() {
        return changeList;
    }
    


}
