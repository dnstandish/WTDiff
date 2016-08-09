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
package org.wtdiff.util;

import java.util.Date;
import java.io.*;

public class ReadSlowMockFileNode extends MockFileNode {

    private class SlowInputStream extends InputStream {
        private InputStream stream;
        private int maxBytesPerRead;
        
        public SlowInputStream(InputStream inputStream, int maxBytes) {
            if ( maxBytes <= 0 )
                throw new IllegalArgumentException("BUG in test code maxBytes must be positive" );
            stream = inputStream;
            maxBytesPerRead = maxBytes;
        }
        
        public int read() throws IOException {
            if (maxBytesPerRead == 0)
                return 0;
            return stream.read();
        }
        
        public int read(byte[] b) throws IOException {
            if ( b.length > maxBytesPerRead )
                return read(b, 0, maxBytesPerRead);
            return read(b, 0, b.length);
        }
        
        public int read(byte[] b, int off, int len) throws IOException {
            if ( len >  maxBytesPerRead)
                return stream.read(b, off, maxBytesPerRead);
            return stream.read(b, off, len);
        }
    }
    
    private int maxBytesPerRead;
    
    public ReadSlowMockFileNode(String name, byte[] content, Date time, int maxBytes) {
        super(name, content, time);
        maxBytesPerRead = maxBytes;
    }

    public ReadSlowMockFileNode(String name, String content, Date time, int maxBytes) {
        super(name, content, time);
        maxBytesPerRead = maxBytes;
    }

    public ReadSlowMockFileNode(String name, int maxBytes) {
        super(name);
        maxBytesPerRead = maxBytes;
    }

    public InputStream getInputStream() throws IOException {
        return new SlowInputStream(super.getInputStream(), maxBytesPerRead);
    }
}
