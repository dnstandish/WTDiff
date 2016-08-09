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

import org.wtdiff.util.text.DiffWriter.ChangeType;

import difflib.Chunk;
import difflib.Delta;
import difflib.Patch;
import difflib.Delta.TYPE;

public class NormalDiffAdapter extends DiffAdapter {

    public NormalDiffAdapter(DiffData oldData, DiffData newData, DiffWriter w ) throws IOException{
        super( oldData,  newData, w );
    }

    @Override
    public void format(Patch patch) throws IOException {
        DiffWriter w = getDiffWriter();
        for (Delta delta: patch.getDeltas()) {
            DiffWriter.ChangeType type;
            switch ( delta.getType() ) {
            case INSERT: type = ChangeType.NEW_ONLY; break;
            case DELETE: type = ChangeType.OLD_ONLY; break;
            case CHANGE: type = ChangeType.CHANGED; break;
            default:
                throw new IllegalArgumentException("BUG Unknown diff delta change type " + delta.getType());
            }

            w.newChange( 
                type,
                delta.getOriginal().getPosition(),
                delta.getOriginal().last(),
                delta.getRevised().getPosition(),
                delta.getRevised().last()
                );
            List<String> oldLines = getOldLines().subList(delta.getOriginal().getPosition(), delta.getOriginal().last() + 1);
            List<String> newLines = getNewLines().subList(delta.getRevised().getPosition(), delta.getRevised().last() + 1);
            w.append( oldLines, delta.getOriginal().getPosition(), newLines, delta.getRevised().getPosition() );
        }
        if ( isOldMissingFinalLineSep() && ! isNewMissingFinalLineSep() ) {
            w.newChange(ChangeType.WARNING, getOldLines().size(), getOldLines().size(), getNewLines().size() , getNewLines().size()-1);
            w.append( 
                missingNewLineWarningList(getOldName()), getOldLines().size()-1, 
                new ArrayList<String>(0), getNewLines().size()-1
            );
        }
        if ( ! isOldMissingFinalLineSep() && isNewMissingFinalLineSep() ) {
            w.newChange(ChangeType.WARNING, getOldLines().size(), getOldLines().size()-1, getNewLines().size() , getNewLines().size());
            w.append( 
                new ArrayList<String>(0), getOldLines().size()-1, 
                missingNewLineWarningList(getNewName()) , getNewLines().size()-1
            );
        }
    }
}
