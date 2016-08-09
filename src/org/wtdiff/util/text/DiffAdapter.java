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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import difflib.Patch;

public abstract class DiffAdapter {

    private DiffData oldDiffData;
    private DiffData newDiffData;
    private DiffWriter diffWriter;
    
    public DiffAdapter(DiffData oldData, DiffData newData, DiffWriter w) throws IOException {
        oldDiffData = oldData;
        newDiffData = newData;
        diffWriter = w;        
        w.totalLinesHint( maxLineCount(oldData, newData) );
    }

    private int maxLineCount(DiffData oldData, DiffData newData) throws IOException {
        int nOld = oldData.getLines().size();
        int nNew = newData.getLines().size();
        return nOld > nNew ? nOld: nNew;
    }
    
    
    public abstract void format(Patch patch) throws IOException ;

    protected String getOldName() {
        return oldDiffData.getName();
    }
    
    protected String getNewName() {
        return newDiffData.getName();
    }
    
    protected boolean isOldMissingFinalLineSep() {
        return oldDiffData.isMissingFinalLineSep();
    }
    
    protected boolean isNewMissingFinalLineSep() {
        return newDiffData.isMissingFinalLineSep();
    }
    
    protected List<String> getOldLines() throws IOException {
        return oldDiffData.getLines();
    }
    
    protected List<String> getNewLines() throws IOException {
        return newDiffData.getLines();
    }
    
    protected DiffWriter getDiffWriter() {
        return diffWriter;
    }
    

    protected List<String> missingNewLineWarningList(String name) {
        ArrayList<String> list = new ArrayList<String>(1);
        list.add( 
            MessageFormat.format(
                Messages.getString("DiffFormatter.missing_linesep"),
                name
            )
        );
        return list;
    }

}
