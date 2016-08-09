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

import java.io.IOException;
import java.io.InputStream;

public class ExceptionInputStream extends InputStream {
    private InputStream stream;
    private int failOffset;
    private int offset;
    
    public ExceptionInputStream(InputStream inputStream, int errorOffset) {
        stream = inputStream;
        failOffset = errorOffset;
        offset = 0;
    }
    
    public int read()  throws IOException {
        if ( offset == failOffset )
                throw new IOException("artificial test IO exception");
        offset++;
        return stream.read();
    }
    
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        if ( offset+len >= failOffset )
            throw new IOException("artificial test IO exception");
        int nRead = stream.read(b, off, len);
        offset += nRead;
        return nRead;
    }
}
