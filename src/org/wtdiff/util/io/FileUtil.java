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
package org.wtdiff.util.io;

import java.io.File;

public class FileUtil {

    private FileUtil() {}

    /** 
     * Determine closest thing to an existing directory from given path string.
     * If thing is an existing directory then return itself. 
     * If thing is a file then return its parent directory.
     * If parent directory does not exist, then try its parent.
     * And so on.  If no existing directory is found then return provided default.
     * 
     * @param origPath
     * @param defaultPath
     * @return
     */
    static public String bestExistingDirFromString(String origPath, String defaultPath) {
        if ( origPath == null || origPath.length() == 0 )
            return defaultPath;
        
        File origFile = new File(origPath);
        if ( origFile.exists() && origFile.isDirectory() )
             return origPath;
        
        return bestExistingDirFromString(origFile.getParent(), defaultPath);
    }
}
