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
import java.util.List;

import org.wtdiff.util.text.DiffAdapter;
import org.wtdiff.util.text.DiffData;
import org.wtdiff.util.text.DiffWriter;

import difflib.Patch;

public class DummyDiffAdapter extends DiffAdapter {

    public DummyDiffAdapter(DiffData oldData, DiffData newData, DiffWriter w)
        throws IOException {
        super(oldData, newData, w);
    }

    @Override
    public void format(Patch patch) throws IOException {
        

    }

    // methods exposing protected methods for unit test purposes
    
    public String theOldName() {
        return getOldName();
    }
    
    public String theNewName() {
        return getNewName();
    }
    
    public List<String> theOldLines() throws IOException {
        return getOldLines();
    }
    
    public List<String> theNewLines()  throws IOException {
        return getNewLines();
    }
    
    public DiffWriter theDiffWriter() {
        return getDiffWriter();
    }
    
    public boolean theOldIsMissingFinalLineSep() {
        return isOldMissingFinalLineSep();
    }

    public boolean theNewIsMissingFinalLineSep() {
        return isNewMissingFinalLineSep();
    }
    
    public List<String> theMissingFinalLineSepWarning(String name) {
        return this.missingNewLineWarningList(name);
    }
}
