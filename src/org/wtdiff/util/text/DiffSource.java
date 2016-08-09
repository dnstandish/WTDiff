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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.ListIterator;

public class DiffSource implements DiffData {
    
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
  

    private InputStreamSource source;
    private boolean isForcedLineSep = false;
    private TextUtil.LineSeparator lineSep;
    private boolean isForcedEncoding = false;
    private Charset encoding = DEFAULT_CHARSET;
    private List <String> lines = null; 
        
    private boolean missingFinalLineSep = false;
    
    public DiffSource(InputStreamSource source) throws IOException {
        this.source = source;
         determineLineSep();
    }

    private void determineLineSep() throws IOException { 
        try ( InputStream is = source.getInputStream() ;
            Reader r = new InputStreamReader(is, encoding )
        ) {
            lineSep = TextUtil.guessLineSeparator(r);
        }
    }

    public String getName() {
        return source.getName();
    }
    public void forceLineSep(TextUtil.LineSeparator newLineSep) throws IOException {
        if ( lineSep != newLineSep ) {
            clearLines();
            if ( newLineSep == null ) {
                isForcedLineSep = false;
                determineLineSep();
            } 
            else {
                lineSep = newLineSep;
                isForcedLineSep = true;
            }
        } 
        else if (newLineSep != null ){
            isForcedLineSep = true;
        }
    }
    
    public TextUtil.LineSeparator getLineSep() {
        return lineSep;
    }
    
    public void forceEncoding(Charset newEncoding) throws IOException {
        if ( ! encoding.equals(newEncoding) ) {
            clearLines();
            if ( newEncoding == null ) {
                isForcedEncoding = false;
                encoding = DEFAULT_CHARSET;
            } else {
                encoding = newEncoding;
                isForcedEncoding = true;
            }
            if ( ! isForcedLineSep ) {
                determineLineSep();
            }
        } else if ( newEncoding != null ){
            isForcedEncoding = true;            
        }
    }

    public Charset getEncoding() {
        return encoding;
    }
    
    private synchronized void clearLines() {
        lines = null;
    }

    private void load() throws IOException {
        missingFinalLineSep = false;
      try ( InputStream is = source.getInputStream() ;
          Reader r = new InputStreamReader(is, encoding )
       ) {
          lines = TextUtil.readerToLines(r, lineSep);
          for( ListIterator<String> iter = lines.listIterator() ; iter.hasNext(); ) {
              String line = iter.next();
              String newLine = TextUtil.removeTrailingLineSeparator(line, lineSep);
              if ( newLine.equals(line) )
                  missingFinalLineSep = true;
              else
                  iter.set(newLine);
          }
      }
    }

    public synchronized List<String> getLines() throws IOException {
        if ( lines == null )
            load();
        return lines;
    }
 
    public boolean isMissingFinalLineSep() {
        return missingFinalLineSep;
    }
}
