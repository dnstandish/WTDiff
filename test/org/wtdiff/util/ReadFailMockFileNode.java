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

public class ReadFailMockFileNode extends MockFileNode {

    private int failureOffset;
    private boolean failureEnabled = true;
    public ReadFailMockFileNode(String name, byte[] content, Date time, int errorOffset) {
        super(name, content, time);
        failureOffset = errorOffset;
    }

    public ReadFailMockFileNode(String name, String content, Date time, int errorOffset) {
        super(name, content, time);
        failureOffset = errorOffset;
    }

    public ReadFailMockFileNode(String name, int errorOffset) {
        super(name);
        failureOffset = errorOffset;
    }

    public void disableFailure() {
        failureEnabled = false;
    }
    
    public void enableFailure() {
        failureEnabled = true;
    }
    
    public InputStream getInputStream() throws IOException {
        if ( failureEnabled )
            return new ExceptionInputStream(super.getInputStream(), failureOffset);
        else
            return super.getInputStream();
    }
}
