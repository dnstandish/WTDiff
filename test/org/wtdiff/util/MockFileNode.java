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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.*;

import org.wtdiff.util.FileNode;



public class MockFileNode extends FileNode {
    private static String ENCODING = "UTF-8";
    private FileType fileType = FileType.REGFILE;
    byte[] fileContent;
    String linkTo;
    Date fileTime;
    boolean isForceMd5 = false;
    byte[] digestMd5;
    public MockFileNode(String name) {
        this(name, name, new Date(0));
    }
    
    public MockFileNode(String name, String content, Date time) {
        setName(name);
        fileTime = time;
        try {
            fileContent = content.getBytes(ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Expected encoding is not supported: "+ ENCODING);
        }
    }
    
    public MockFileNode(String name, byte[] content, Date time) {
        setName(name);
        fileTime = time;
        fileContent = content;
    }
    
    
    public void forceMd5(byte[] md5) {
        digestMd5 = md5;
        isForceMd5 = true;
    }
    
    public byte[] getMd5() throws IOException {
        if ( isForceMd5 )
            return digestMd5;
        return super.calculateMd5();
    }
    
    public void setFileType(FileType type) {
        fileType = type;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setLinkTo(String target) {
        linkTo = target;
        fileContent = linkTo.getBytes();
    }


    public long getCrc() throws IOException { 
        CRC32 crc = new CRC32();
        crc.update(fileContent);
        return crc.getValue();
    }
    public long getSize() { return fileContent.length;}
    public long getTime() { return fileTime.getTime();}
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream( fileContent );
    }
    // simple content method cost - the same for all methods
    private double contentMethodCost = COST_EASY;
    public double getContentMethodCost(ContentMethod method) {
        return contentMethodCost;
    }
    public void setContentMethodCost(double cost) {
        contentMethodCost = cost;
    }

}
