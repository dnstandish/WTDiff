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

import java.util.List;

import org.wtdiff.util.text.DiffWriter;

public class SimpleTestDiffWriter extends DiffWriter {

    public StringBuilder history = new StringBuilder();
    
    public void reset() {
        history = new StringBuilder();
    }
    
    public String toString() {
        return history.toString();
    }
    
    @Override
    public void append(List<String> oldLines, int oldBeginLineOffset,
        List<String> newLines, int newBeginLineOffset) {
        history.append(lineNumberToString(oldBeginLineOffset+1) + "[");
        for( String line: oldLines ) {
            history.append(formatLine(line) + "\n");
        }
        history.append("]\n");
        history.append(lineNumberToString(newBeginLineOffset+1) + "[");
        for( String line: newLines ) {
            history.append(formatLine(line) + "\n");
        }
        history.append("]\n");
    }

    @Override
    public void newChange(ChangeType type, int oldBegin, int oldEnd,
        int newBegin, int newEnd) {
        history.append(type.toString() + "[" + oldBegin + "," + oldEnd + "," + newBegin + "," + newEnd + "]" + "\n");

    }

}
