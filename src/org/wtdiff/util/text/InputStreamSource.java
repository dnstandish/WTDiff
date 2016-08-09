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

public interface InputStreamSource {

    /**
     * Get input stream for this source
     * @return input stream
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException;
    
    /**
     * Get name of this source, for example file name
     * 
     * @return
     */
    public String getName();
    
    /**
     * get time of this source, for example file modification time
     * 
     * @return
     */
    public long getTime();
    
}
