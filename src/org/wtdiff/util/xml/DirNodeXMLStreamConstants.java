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
package org.wtdiff.util.xml;

public interface DirNodeXMLStreamConstants {
    public static final String ELEMENT_HOME = "home";
    public static final String ELEMENT_OS = "os";
    public static final String ELEMENT_HOST = "host";
    public static final String ELEMENT_CAPTURE_TIME = "capture-time";
    public static final String ELEMENT_CURRENT_DIR = "current-dir";
    public static final String ELEMENT_USER_COMMENT = "user-comment";
    public static final String ELEMENT_USER = "user";
    public static final String ELEMENT_FILE_TREE_SNAPSHOT = "file-tree-snapshot";
    public static final String ELEMENT_CAPTURE_ROOT = "capture-root";
    public static final String ELEMENT_DIGESTS_AVAILABLE = "digests-available";
    public static final String ELEMENT_DIGEST_NAME = "digest-name";
    public static final String ELEMENT_SNAPSHOT = "snapshot";
    public static final String ELEMENT_DIR = "dir";
    public static final String ELEMENT_FILES = "files";
    public static final String ELEMENT_DIGEST = "digest";
    public static final String ELEMENT_FILE = "file";
    public static final String ELEMENT_LINKTO = "linkto";
    public static final String ELEMENT_DIRS = "dirs";
    
    public static final String ATTR_DIGEST_NAME_NAME = "name";
    public static final String ATTR_DIR_NAME = "name";
    public static final String ATTR_FILE_NAME = "name";
    public static final String ATTR_FILE_SIZE = "size";
    public static final String ATTR_FILE_TIME = "time";
    public static final String ATTR_FILE_ISTEXT = "istext";
    public static final String ATTR_FILE_TYPE = "type";
    public static final String ATTR_DIGEST_NAME = "name";
    public static final String ATTR_LINKTO_TARGET = "target";
    
    public static final String FILE_TYPE_REGFILE = "regfile";
    public static final String FILE_TYPE_SPECIAL = "special";
    public static final String FILE_TYPE_SYMLINK = "symlink";
    
    public static final String FILE_ISTEXT_YES = "yes";
    public static final String FILE_ISTEXT_NO = "no";
    
    public static final String FILE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    public static final String DIGEST_CRC32 = "CRC32";  // TODO should be in a FileNode constants
    public static final String DIGEST_MD5 = "MD5";  // TODO should be in a FileNode constants

}
