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

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

public class TwoDocumentDiffWriter extends DocumentDiffWriter {

    private StyledDocument oldDoc;
    private StyledDocument newDoc;
    private ChangeType currentType = ChangeType.COMMON;
    
    private List<ChangeRange> oldChangeList = new ArrayList<>();
    private List<ChangeRange> newChangeList = new ArrayList<>();

    public TwoDocumentDiffWriter(StyledDocument oldD, StyledDocument newD) {
        oldDoc = oldD;
        initStyleSets(oldDoc);
        newDoc = newD;
    }

    @Override
    public void append(List<String> oldLines, int oldBeginLineOffset, List<String> newLines, int newBeginLineOffset)  {
        
        int oldBegin = oldDoc.getLength();
        int newBegin = newDoc.getLength();

        StyleSet sSet = styleSetForChangeType(currentType);
        String indicator = "  ";
        if ( currentType == ChangeType.OLD_ONLY ) {                 
            indicator = "- ";
        } else if ( currentType == ChangeType.NEW_ONLY ) {                 
            indicator = "+ ";
        } else if ( currentType == ChangeType.CHANGED )  {
            indicator = "! ";
        }

        try {
            int oldLineNo = oldBeginLineOffset;
            for( String line: oldLines ) {
                oldLineNo++;
                if ( isNumberLines() ) {
                    oldDoc.insertString(oldDoc.getLength(), lineNumberToString(oldLineNo) , sSet.lineno);
                }
                oldDoc.insertString(oldDoc.getLength(), indicator, sSet.indicator);
                oldDoc.insertString(oldDoc.getLength(), formatLine(line)  + LS, sSet.text);
            }
            int newLineNo = newBeginLineOffset;
            for( String line: newLines ) {
                newLineNo++;
                if ( isNumberLines() ) {
                    newDoc.insertString(newDoc.getLength(), lineNumberToString(newLineNo) , sSet.lineno);
                }
                newDoc.insertString(newDoc.getLength(), indicator, sSet.indicator);
                newDoc.insertString(newDoc.getLength(), formatLine(line) + LS, sSet.text);

            }
            if ( oldLines.size() > newLines.size() ) {
                for( int i = oldLines.size() - newLines.size(); i > 0 ; i-- ) {
                    newDoc.insertString(newDoc.getLength(), LS, sSet.text);
                }
            } else {
                for( int i = newLines.size() - oldLines.size(); i > 0 ; i-- ) {
                    oldDoc.insertString(oldDoc.getLength(), LS, sSet.text);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        int oldEnd = oldDoc.getLength();
        int newEnd = newDoc.getLength();
        if ( currentType != ChangeType.COMMON ) {
            oldChangeList.add( new ChangeRange(oldBegin, oldEnd));
            newChangeList.add( new ChangeRange(newBegin, newEnd));
        }

    }

    @Override
    public void newChange(ChangeType type, int oldBegin, int oldEnd, int newBegin, int newEnd) {
        currentType = type;
            switch ( type ) {
                case COMMON:
                    break;
                case NEW_ONLY:
                    break;
                case OLD_ONLY:
                    break;
                case WARNING:
                    break;
                case CHANGED:
                    break;
                default:
                    throw new IllegalArgumentException( "BUG unknown change type " + type);
            }
    }

    public int numberOfDifferences() {
        if (oldChangeList.size() != newChangeList.size()) {
            throw new IllegalStateException(
                "old and new change list sizes different " + oldChangeList.size() 
                + " vs " + newChangeList.size()
            );
        }
        return oldChangeList.size();
    }
    
    public List<ChangeRange> getOldChangeRanges() {
        return oldChangeList;
    }
    
    public List<ChangeRange> getNewChangeRanges() {
        return newChangeList;
    }
    

}
