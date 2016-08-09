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

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * Helper class for creating a temporary zip file for testing.
 * 
 *  helper = new ZipTestHelper();
 *  helper.addTestZipDir("a/");
 *  helper.addTestZipFile("a/b", "concents of file b");
 *  zipFile = helper.createTestZipFile("a.zip");
 *  helper.clear() ; // clear out accumulated dirs and files so we can start fresh
 *  
 * @author davidst
 *
 */
public class ZipTestHelper {

    /**
     * Simple class to store the things we are going to put in the zip
     * 
     * @author davidst
     *
     */
    private class Entry {
        /**
         * is this entry a directory?
         */
        public boolean isDir = false;
        /**
         * this entries name
         */
        public String name;
        /**
         * This entries file content if is is a file
         */
        public String content = "";
        
        /**
         * Modification time of file if it is a file
         */
        public Date time;
        
        /**
         * A directory entry
         * 
         * @param dirName
         */
        public Entry(String dirName) {
            name = dirName;
            isDir=true;
        }
        
        /**
         * A file entry with content
         *  
         * @param fileName
         * @param content
         */
        public Entry(String fileName, String content, Date time) {
            name = fileName;
            isDir=false;
            this.content = content;
            this.time = time;
        }
    }
    
    /**
     * temporary directory where we create our zip file.  
     * will be
     */
    private File tempDir;

    /**
     * an ordered list for remembering the things we are going to put in this zip 
     */
    private ArrayList<Entry> list = new ArrayList<>();

    /**
     * Constructor for a helper that can be used for creating one zip
     * 
     * @throws IOException
     */
    public ZipTestHelper() throws IOException {   
        setupTempDir();
    }
    
    /**
     * Creates a temp directory in which to create the zip file.  It will be deleted when
     * the program exits
     *  
     * @throws IOException
     */
    private void setupTempDir() throws IOException {
        tempDir = File.createTempFile("dir", "tmp");
        if ( tempDir.exists() ) {
            if ( ! tempDir.isFile() ) 
                throw new IOException("tempDir " + tempDir.getAbsolutePath() + " unexpected state");
            tempDir.delete();
        }
        if ( ! tempDir.mkdir() )
            throw new IOException("could not create tempDir " + tempDir.getAbsolutePath());
        tempDir.deleteOnExit();
    }

    /**
     * Add a file with given content for our zip
     * 
     * @param name
     * @param content
     * @throws IOException
     */
    public void addTestZipFile(String name, String content, Date time) throws IOException {
        list.add(new Entry(name, content, time));
    }
    /**
     * Add a directory with given name for our zip
     * @param name
     * @throws IOException
     */
    public void addTestZipDir(String name) throws IOException {
        list.add(new Entry(name));
    }

    /**
     * Create a zip with accumlated list of content.
     *   
     * @param name
     * @return
     * @throws IOException
     */
    public File createTestZipFile(String name) throws IOException {       
        File testZipFile = new File(tempDir, name);        
        testZipFile.deleteOnExit();
        
        FileOutputStream out = null;
        ZipOutputStream zipOut = null;
        try {
            out = new FileOutputStream(testZipFile);
            zipOut = new ZipOutputStream(out);
            for( Iterator<Entry> iter = list.iterator(); iter.hasNext(); ) {
                Entry entry = (Entry) iter.next();
                ZipEntry ze = new ZipEntry(entry.name);
                if ( ! entry.isDir ) {
                    ze.setTime( entry.time.getTime() );
                }
                zipOut.putNextEntry(ze);
                if ( ! entry.isDir ) {
                    zipOut.write(entry.content.getBytes("UTF-8")); // should we use ISO-8859-1 ?
                }
                zipOut.closeEntry();
            }
            zipOut.close();
            out = null;
            zipOut = null;
        } finally {
            if ( zipOut != null) try { zipOut.close(); } catch (Exception e) {}
            else if ( out != null) try { out.close(); } catch (Exception e) {}
        }
        return testZipFile;
    }
    
    /**
     * Clear out our list of content
     *
     */
    public void clear() {
        list.clear();
    }
}
