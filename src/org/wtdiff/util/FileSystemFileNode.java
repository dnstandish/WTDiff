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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.zip.CRC32;

/**
 * Concrete file node class.
 * 
 * @author davidst
 *
 */
public class FileSystemFileNode extends FileNode {
    
    /**
     * The file
     */
    private File file;
    /**
     * Size of this file
     */
    private long size;
    /**
     * Mode time of this file
     */
    private long modTime;
    /**
     * What type of files is this?
     */
    private FileType fileType;
    /**
     * CRC32 of file content. Lazily initiated, NULL if we haven't computed it yet.
     */
    private Long crc;
    /**
      * MD5 sum of file content. Lazily initiated, NULL if we haven't computed it yet.
      */
     private byte[] md5;

    /**
     * For symbolic links this is what link points to 
     */
    private String linkTo;
    
    /**
     * Construct from a path
     * 
     * @param p
     * @throws IOException
     */
    public FileSystemFileNode(Path p) throws IOException {
        this(p, 
            Files.isSymbolicLink(p)?FileType.SYMLINK: 
                Files.isRegularFile(p) ? FileType.REGFILE :
                    ! Files.isDirectory(p) ? FileType.SPECIAL:
                        null
                        );
    }
    
    /**
     * Construct from a path given type
     * 
     * @param p
     * @param type
     * @throws IOException
     */
    public FileSystemFileNode(Path p, FileType type) throws IOException {
        
        if ( ! Files.exists(p, LinkOption.NOFOLLOW_LINKS) ) {
            throw new IOException(
                MessageFormat.format(
                    Messages.getString("FileSystemFileNode.file_no_exist"), //$NON-NLS-1$
                    p
                )
            );
        }
        file = p.toFile();
        fileType = type;
        size = file.length();
        modTime = file.lastModified(); // TODO may get more time resolution via java.nio.file.Files.getLastModifiedTime()
        setName( file.getName() );
        
        if ( type == FileType.SYMLINK ) {
            modTime = Files.getLastModifiedTime(p, LinkOption.NOFOLLOW_LINKS).toMillis();
            linkTo = Files.readSymbolicLink(p).toString();
            size = linkTo.getBytes().length;
        } else if ( type !=  FileType.REGFILE && type != FileType.SPECIAL) {
            throw new IllegalArgumentException(
                MessageFormat.format(
                    Messages.getString("FileSystemFileNode.bad_file_type"), //$NON-NLS-1$
                    p
                )
            );
        }
    }
    /**
     * What type of file is this (most commonly regular file)
     */
    public FileType getFileType() {
        return fileType;
    }
    /**
     * How hard is it to use given content method with this tyep of FileNode.
     * 
     * @return cost (difficulty) of using given contant comparison method
     */
    public double getContentMethodCost(ContentMethod method) {
        // comparison by content is the easiest (at least the first time). 
        // determining the CRC32 is only a little more work 
        if ( method == CONTENT_METHOD_CRC ) {
            return COST_MODERATE;
        }
        else if ( method == CONTENT_METHOD_MD5 ) {
            return COST_HARD;
        }
        else if ( method == CONTENT_METHOD_CONTENT ) {
            return COST_EASY;
        }
        else if ( method == CONTENT_METHOD_CONTENT_TEXT ) {
            return COST_EASY;
        }
        // otherwise can't perform given comparison method
        return COST_IMPOSSIBLE;
    }

    /**
     * Return CRC32 of file content.  Note client will also need to check size.
     * The CRC32 isn't good enough for to guard against malicious file tampering.
     * For that should need a cryptographic hash.
     */
    public synchronized long getCrc() throws IOException {
        if (crc == null) {
            CRC32 crc32 = new CRC32();
            InputStream in = null;
            try {
                in = getInputStream();
                int bufSize = 512;  // Define this elsewhere?
                byte[] b = new byte[bufSize];
                int n;
                while( (n = in.read(b)) > 0 ) {
                    crc32.update(b, 0, n);
                }
                crc = new Long(crc32.getValue());
            }
            finally {
                if ( in != null ) in.close();
            }
        }
        return crc.longValue();
    }

    /**
     * Return MD5Sum of file content.  Note client will also need to check size.
     * The CRC32 isn't good enough for to guard against malicious file tampering.
     * For that should need a cryptographic hash.
     */
     public synchronized byte[] getMd5() throws IOException {
         if (md5 == null) {
             md5 = calculateMd5();
         }
         return md5;
     }

    /**
     * Return an input stream for this file.  It is the callers responsibility
     * to close the steam.  
     */
    public InputStream getInputStream() throws IOException {
        if ( fileType == FileType.SYMLINK ) {
            return new ByteArrayInputStream(linkTo.getBytes());  // TODO may not be correct in default charset. use ISO-LATIN1?
        }
        
        if ( fileType == FileType.SPECIAL ) {
            return new ByteArrayInputStream(new byte[0]);            
        }
        
        return new BufferedInputStream( new FileInputStream(file) );
    }

    /**
     * Size of this file
     */
    public long getSize() {
        return size;
    }

    /**
     * File timestamp
     */
    public long getTime() {
        return modTime;
    }
    

}
