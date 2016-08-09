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
import java.io.OutputStream;

public class ExceptionOutputStream extends OutputStream {

    private int failOffset;
    private int offset;
    
    public ExceptionOutputStream(int errorOffset) {
        failOffset = errorOffset;
        offset = 0;
    }

    @Override
    public void write(int b) throws IOException {
        if ( offset == failOffset) {
            throw new IOException("mock write exception");
        }
        offset++;
    }
}
