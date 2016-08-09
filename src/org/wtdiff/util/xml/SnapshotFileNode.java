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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.CRC32;

import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileNode.ContentMethod;
import org.wtdiff.util.FileNode.FileType;

class SnapshotFileNode extends FileNode {
    private FileType fileType;
    private long size;
    private long modTime;
    private boolean isText;
    private HashMap<String, byte[]> digests;
    private String linkTo;
    
    public SnapshotFileNode(String name, long size2, long mtime, boolean isText2, 
      FileType fileType2, String linkTo2, HashMap<String, byte[]> digests2) throws IllegalArgumentException {
        size = size2;
        modTime = mtime;
        isText = isText2;
        fileType = fileType2;
        digests = digests2;
        setName(name);
        checkCrc32();
        if ( fileType == FileType.SYMLINK ) {
            linkTo = linkTo2;
            if ( size != linkTo.getBytes().length ) {
                throw new IllegalArgumentException("size does not match linkto length");
            }
        }       
    }

    private void checkCrc32()  throws IllegalArgumentException {
        if ( digests.containsKey(DirNodeXMLStreamReader.DIGEST_CRC32) ) {
            if ( digests.get(DirNodeXMLStreamReader.DIGEST_CRC32).length > 4 )
                throw new IllegalArgumentException("CRC32 longer than 4 bytes");
        }
    }
    
    @Override
    public FileType getFileType() {
        return fileType;
    }

    @Override
    public long getTime() {
        return modTime;
    }

    @Override
    public long getSize() {
        return size;
    }

    private long calculateCrc(byte[] bytes)  {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
    
    @Override
    public long getCrc() throws IOException {
        byte[] bytes;
        if ( fileType == FileType.SYMLINK ) {
            return calculateCrc(linkTo.getBytes());
        } else if ( fileType == FileType.SPECIAL ) {
            return calculateCrc(new byte[0]);
        }
        
        if ( ! digests.containsKey(DirNodeXMLStreamReader.DIGEST_CRC32) ) {
            throw new IOException("snapshot missing " + DirNodeXMLStreamReader.DIGEST_CRC32 + " for " + getName());
        } else {
            bytes = digests.get(DirNodeXMLStreamReader.DIGEST_CRC32);
        }
        long crc32 = 0;
        for ( byte b : bytes ) {
            //int unsigned = b < 0 ? 256+b : b;
            int unsigned = 0xff & (int)b;
            crc32 = crc32 * 256 + unsigned;
        }
        return crc32;
    }

    @Override
    public byte[] getMd5() throws IOException {
        byte[] bytes;
        if ( fileType == FileType.SYMLINK ) {
            throw new IOException(DirNodeXMLStreamReader.DIGEST_MD5 + " not available for symbolic links, " + getName());
        } else if ( fileType == FileType.SPECIAL ) {
            throw new IOException(DirNodeXMLStreamReader.DIGEST_MD5 + " not available for special files, " + getName());
        } else if ( ! digests.containsKey(DirNodeXMLStreamReader.DIGEST_MD5) ) {
            throw new IOException("snapshot missing " + DirNodeXMLStreamReader.DIGEST_MD5 + " for " + getName());
        } else {
            bytes = digests.get(DirNodeXMLStreamReader.DIGEST_MD5);
        }
        
        return bytes;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if ( fileType == FileType.SYMLINK ) {
            return new ByteArrayInputStream(linkTo.getBytes());  // TODO may not be correct in default charset. use ISO-LATIN1?
        }
        
        if ( fileType == FileType.SPECIAL ) {
            return new ByteArrayInputStream(new byte[0]);            
        }
        throw new IOException("content not available for " + getName());
    }

    @Override
    public double getContentMethodCost(ContentMethod method) {
        if ( method == CONTENT_METHOD_CONTENT ) {
            if ( fileType == FileType.SYMLINK ) {
                return COST_EASY;
            } else if ( fileType == FileType.SPECIAL ) {
                return COST_EASY;
            } else {
                return COST_IMPOSSIBLE;
            }
        } else if ( method == CONTENT_METHOD_CONTENT_TEXT ) {
            return COST_IMPOSSIBLE;
        } else if ( method == CONTENT_METHOD_CRC ) {
            if ( fileType == FileType.SYMLINK ) {
                return COST_HARD;
            } else if ( fileType == FileType.SPECIAL ) {
                return COST_HARD;
            } else if ( digests.containsKey(DirNodeXMLStreamReader.DIGEST_CRC32)){
                return COST_EASY;
            } else {
                return COST_IMPOSSIBLE;
            }                            
        } else if ( method == CONTENT_METHOD_MD5 ) {
            if ( fileType == FileType.SYMLINK ) {
                return COST_IMPOSSIBLE;
            } else if ( fileType == FileType.SPECIAL ) {
                return COST_IMPOSSIBLE;
            } else if ( digests.containsKey(DirNodeXMLStreamReader.DIGEST_MD5)){
                return COST_EASY;
            } else {
                return COST_IMPOSSIBLE;
            }
        }
        return COST_IMPOSSIBLE;
    }

    @Override
    public boolean isText() {
        return isText;
    }


}