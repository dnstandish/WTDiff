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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//import org.wtdiff.util.Messages;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.RootNodeListener;
import org.wtdiff.util.CompareController.NodeRole;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.Delta.TYPE;

public class DiffController {

    public enum SourceType { OLD, NEW };
    
    
    private DiffSource oldSourceInfo;
    private DiffSource newSourceInfo;
    
    private boolean ignoreWhiteSpace = false;
    private boolean compactWhiteSpace = false;
    private boolean trimWhiteSpace = false;
    
    private Patch patch = null;
    
    private List<DiffChangeListener> diffChangeListeners = new ArrayList<>();
    
    public DiffController() {
        
    }

    public void addDiffChangeListener( DiffChangeListener listener ) {
        diffChangeListeners.add(listener);
    }
    
    private void notifyDiffChangeListeners() {
        for(DiffChangeListener l : diffChangeListeners) {
            l.diffChanged();
        }
    }

    public boolean isIgnoreWhiteSpace() {
        return ignoreWhiteSpace;
    }

    public void setIgnoreWhiteSpace(boolean ignoreWhiteSpace) {
        if ( ignoreWhiteSpace != this.ignoreWhiteSpace) {
            this.ignoreWhiteSpace = ignoreWhiteSpace;
            patch = null;
            notifyDiffChangeListeners();
        }
    }

    public boolean isCompactWhiteSpace() {
        return compactWhiteSpace;
    }

    public void setCompactWhiteSpace(boolean compactWhiteSpace) {
        if ( this.compactWhiteSpace != compactWhiteSpace ) {
            this.compactWhiteSpace = compactWhiteSpace;
            patch = null;
            notifyDiffChangeListeners();
        }
    }

    public boolean isTrimWhiteSpace() {
        return trimWhiteSpace;
    }

    public void setTrimWhiteSpace(boolean trimWhiteSpace) {
        if ( this.trimWhiteSpace != trimWhiteSpace ) {
            this.trimWhiteSpace = trimWhiteSpace;
            patch = null;
            notifyDiffChangeListeners();
        }
    }

    public void setOldSource(InputStreamSource old) throws IOException {
        if ( old == null )
            oldSourceInfo = null;
        else
            oldSourceInfo = new DiffSource(old);
        patch = null;
        notifyDiffChangeListeners();
    }

    public void setNewSource(InputStreamSource news) throws IOException  {
        if ( news == null )
            newSourceInfo = null;
        else
            newSourceInfo = new DiffSource(news); 
        patch = null;
        notifyDiffChangeListeners();
    }
    
    public String getOldSourceName() {
        return oldSourceInfo == null ? null: oldSourceInfo.getName();
    }
    
    public DiffData getOldData() {
        return oldSourceInfo;
    }
    
    public String getNewSourceName() {
        return newSourceInfo == null ? null: newSourceInfo.getName();
    }
    
    public DiffData getNewData() {
        return newSourceInfo;
    }
    
    private DiffSource getSourceInfo(SourceType which) {
        return  which == SourceType.NEW ?
            newSourceInfo: oldSourceInfo;
    }

    public void forceEncoding(SourceType which, Charset encoding) throws IOException {
        DiffSource si = getSourceInfo(which);
        if ( si != null )
            si.forceEncoding( encoding );
        patch = null;
        notifyDiffChangeListeners();
    }

    public Charset getEncoding(SourceType which) {        
        DiffSource si = which == SourceType.NEW ? newSourceInfo : oldSourceInfo;
        return si == null ? null : si.getEncoding();
    }
    
    public void forceLineSepSourceType(SourceType which, TextUtil.LineSeparator lineSep) throws IOException {
        DiffSource si = getSourceInfo(which);
        if ( si != null )
            si.forceLineSep(lineSep);
        patch = null;
        notifyDiffChangeListeners();
    }

    public TextUtil.LineSeparator getLineSep(SourceType which)  throws IOException {        
        DiffSource si = which == SourceType.NEW ? newSourceInfo : oldSourceInfo;
        return si == null ? null : si.getLineSep();
    }
    



    public void diff() throws IOException {
        if ( oldSourceInfo == null || newSourceInfo ==null )
            return;
        List<String> oldLines = oldSourceInfo.getLines();
        List<String> newLines = newSourceInfo.getLines();
        
        List<String> oldMassaged = massageLines(oldLines);
        List<String> newMassaged = massageLines(newLines);
        
        patch = DiffUtils.diff(oldMassaged, newMassaged);
        notifyDiffChangeListeners();
    }

    private List<String> massageLines(List<String> lines) {
        if ( !ignoreWhiteSpace && !compactWhiteSpace && !trimWhiteSpace )
            return lines;
        
        ArrayList<String> massaged = new ArrayList<>( lines.size());
        for ( String line: lines ) {
            String newLine = line;
            if ( ignoreWhiteSpace ) {
                newLine = TextUtil.removeWhiteSpace(newLine);
            } 
            else if ( compactWhiteSpace ) {
                if ( trimWhiteSpace )
                    newLine = TextUtil.normalizeTrimWhiteSpace(newLine);
                else
                    newLine = TextUtil.normalizeWhiteSpace(newLine);
            } else if ( trimWhiteSpace ) {
                newLine = newLine.trim();
            }
            massaged.add(newLine);
        }
        return massaged;
    }

    public boolean haveDiff() {
        return patch != null;
    }
    
    public Patch getDiff() {
        return patch;
    }

}
