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


public class SizeHasChangedMockFileNode extends MockFileNode {
    
    private long realSize;
    
    public SizeHasChangedMockFileNode( String name, String content, long size, Date time ) {
        super( name, content, time );
        realSize = size;
    }

    public SizeHasChangedMockFileNode( String name, byte[] content, long size, Date time ) {
        super( name, content, time );
        realSize = size;
    }
    
    public long getSize() {
        return  realSize;
    }
}

