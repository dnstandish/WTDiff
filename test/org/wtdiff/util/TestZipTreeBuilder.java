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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ComparisonDirNode;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.NoHandleErrorHandler;
import org.wtdiff.util.Node;
import org.wtdiff.util.TreeComparor;
import org.wtdiff.util.ZipTreeBuilder;

public class TestZipTreeBuilder   {

    private ErrorHandler noHandler = new NoHandleErrorHandler();

    private ErrorHandler ignoreErrorHandler = new ErrorHandler() {
        private boolean encounteredError = false;
        @Override
        public boolean handleError(Exception e) {
            encounteredError = true;
            return true;
        }

        @Override
        public void logError(Exception e) {
            encounteredError = true;
        }

        @Override
        public boolean encounteredError() {
            return encounteredError;
        }

        @Override
        public void reset() {
            encounteredError = false;
        }
        
    };
    
    @Test
    public void testOneFileOnly() throws IOException {
        Date now = new Date();
        ZipTestHelper helper = new ZipTestHelper();
        helper.addTestZipFile("tfile", "tfile-content", now);
        // also create a mock node we can compare to
        MockFileNode mockNode = new MockFileNode("tfile", "tfile-content", now);
        MockFileNode otherMockNode = new MockFileNode("other", "other-content", now);
        
        File zipFile = helper.createTestZipFile("tfile.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        
        assertEquals(zipFile.getCanonicalPath(), rootNode.getRoot());
        
        assertEquals( 0, rootNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 1, rootNode.getLeaves().size() ); // should be 1 file
        assertEquals("tfile", ((Node)rootNode.getLeaves().get(0)).getName() );
        assertEquals("tfile-content".length(), ((FileNode)rootNode.getLeaves().get(0)).getSize() );
        
        FileNode node = (FileNode)rootNode.getLeaves().get(0);

        assertEquals("tfile-content".length(),  node.getSize());      
        // zip entry time truncated to even seconds while Date is milliseconds
        //assertEquals( now.getTime(), node.getTime() );
        assertTrue( (now.getTime() - node.getTime()) < 2000);

        assertTrue("file content comparison should not be impossible", 
                node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT) != FileNode.COST_IMPOSSIBLE);
        assertTrue("file content comparison should not be not set", 
                node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT) != FileNode.COST_NOT_SET);
        assertTrue("file crc comparison should not be impossible", 
                node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC) != FileNode.COST_IMPOSSIBLE);
        assertTrue("file crc comparison should not be not set", 
                node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC) != FileNode.COST_NOT_SET);
        assertTrue("file md5 comparison should not be impossible", 
            node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5) != FileNode.COST_IMPOSSIBLE);
        assertTrue("file md5 comparison should not be not set", 
            node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5) != FileNode.COST_NOT_SET);

        assertTrue("file content text comparison should not be impossible", 
                node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT) != FileNode.COST_IMPOSSIBLE);
        assertTrue("file content comparison should not be not set", 
                node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT) != FileNode.COST_NOT_SET);

        
        assertTrue( node.compareDetails(mockNode, FileNode.CONTENT_METHOD_CONTENT) ); 
        assertTrue( node.compareDetails(mockNode, FileNode.CONTENT_METHOD_CONTENT_TEXT) ); 
        assertTrue( node.compareDetails(mockNode, FileNode.CONTENT_METHOD_CRC) ); 
        assertTrue( node.compareDetails(mockNode, FileNode.CONTENT_METHOD_MD5) ); 

        assertFalse( node.compareDetails(otherMockNode, FileNode.CONTENT_METHOD_CONTENT) ); 
        
        assertTrue( mockNode.compareDetails(node, FileNode.CONTENT_METHOD_CONTENT) ); 
        assertTrue( mockNode.compareDetails(node, FileNode.CONTENT_METHOD_CONTENT_TEXT) ); 
        assertTrue( mockNode.compareDetails(node, FileNode.CONTENT_METHOD_CRC) ); 
        assertTrue( mockNode.compareDetails(node, FileNode.CONTENT_METHOD_MD5) ); 
    }

    private String getTestInputPath(String testFile) {
        String resourceName =  "data/org/wtdiff/util/TestZipTreeBuilder/" + testFile;
        URL url =   this.getClass().getClassLoader().getResource(
            resourceName
            );
        assertNotNull("Could not get test data " + resourceName, url);
        return url.getPath();
    }

    @Test
    public void testDuplicateFile() throws IOException {
        // The java zip api will not permit creation of a zip file with two entries with
        // the same name.  Thus we cannot use ZipTestHelper.  Instead use an externally
        // generated test zip.
        String doubleZipPath = getTestInputPath("DoubleZip-0.zip");
        System.out.println(doubleZipPath);
        ZipTreeBuilder builder = new ZipTreeBuilder(doubleZipPath);
        try {
            builder.buildTree(noHandler);
            fail("duplicate file name in zip should throw exception");
        } catch (ZipException ze) {
            // this should happen
        }
        ignoreErrorHandler.reset();
        DirNode dNode = builder.buildTree(ignoreErrorHandler);
        assertTrue(ignoreErrorHandler.encounteredError());
        ignoreErrorHandler.reset();
        dNode.dump(" ", "");
        assertEquals( 0 , dNode.getLeaves().size() );
        assertEquals( 1 , dNode.getDirs().size() );
        assertEquals( 1 , dNode.getDirs().get(0).getLeaves().size() );
        assertEquals( 0 , dNode.getDirs().get(0).getDirs().size() );
    }

    @Test
    public void testOneDirOnly() throws IOException {
        ZipTestHelper helper = new ZipTestHelper();
        helper.addTestZipDir("tdir/");
        // also craete a mock node we can compare to 
        
        File zipFile = helper.createTestZipFile("tdir.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        
        assertEquals(zipFile.getCanonicalPath(), rootNode.getRoot());
        
        assertEquals( 1, rootNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 0, rootNode.getLeaves().size() ); // should be 1 file
        assertEquals("tdir", ((Node)rootNode.getDirs().get(0)).getName() );
        
    }

    @Test
    public void testDirsAndFile() throws IOException {
        ZipTestHelper helper = new ZipTestHelper();
        Date now = new Date();

        helper.addTestZipDir("tdir/");
        helper.addTestZipFile("tdir/sdir/sfile", "sfile-content", now);
        helper.addTestZipFile("tfile", "tfile-content", now);
        
        File zipFile = helper.createTestZipFile("tfile.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        
        assertEquals( 1, rootNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 1, rootNode.getLeaves().size() ); // should be 1 file
        assertEquals("tfile", ((Node)rootNode.getLeaves().get(0)).getName() );
        assertEquals("tdir", ((Node)rootNode.getDirs().get(0)).getName() );
            
        DirNode tNode =  (DirNode)rootNode.getDirs().get(0);
        assertEquals( 1, tNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 0, tNode.getLeaves().size() ); // should be 1 file
        
        assertEquals("sdir", ((Node)tNode.getDirs().get(0)).getName() );
        DirNode sNode = (DirNode)tNode.getDirs().get(0);
        assertEquals( 0, sNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 1, sNode.getLeaves().size() ); // should be 1 file
        assertEquals("sfile", ((Node)sNode.getLeaves().get(0)).getName() );
        
    }

    @Test
    public void testDirs() throws IOException {
        ZipTestHelper helper = new ZipTestHelper();

        helper.addTestZipDir("tdir/");
        helper.addTestZipDir("adir/");
        helper.addTestZipDir("bdir/");
        
        File zipFile = helper.createTestZipFile("tfile.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        assertEquals( 3, rootNode.getDirs().size() ); // should be no subdirectories
    }        

    @Test
    public void testAbsoluteZip() throws IOException {
        ZipTestHelper helper = new ZipTestHelper();
        Date now = new Date();

        helper.addTestZipDir("/"); // note this root is ignored
        helper.addTestZipDir("//");  // and this one too
        helper.addTestZipFile("/tfile", "tfile-content", now);
        
        File zipFile = helper.createTestZipFile("tfile.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        //rootNode.dump(" ", ".");
        assertEquals( 0, rootNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 1, rootNode.getLeaves().size() ); // should one file
        assertEquals( "tfile", rootNode.getLeaves().get(0).getName() ); // file name should be ...
        
    }        
    @Test
    public void testNoexistZip() throws IOException {
        ZipTreeBuilder builder = new ZipTreeBuilder("noexist.zip");
        try {
            builder.buildTree(noHandler);
            fail("attempt to build zip tree from nonexistent file should throw exception");
        } catch ( IOException ioe ) {
            // this is supposed to happen
        }
        
        ZipTreeBuilder builder2 = new ZipTreeBuilder("");
        try {
            builder2.buildTree(noHandler);
            fail("attempt to build zip tree from empty path should throw exception");
        } catch ( IOException ioe ) {
            // this is supposed to happen
        }
        
    }
    
    @Test
    public void testDirsAndFilesUnordered() throws IOException {
        ZipTestHelper helper = new ZipTestHelper();
        Date now = new Date();

        helper.addTestZipDir("tdir/");
        helper.addTestZipFile("td/f1", "tdf1-content", now);
        helper.addTestZipDir("td/");
        helper.addTestZipFile("tdir/sdir/sfile", "sfile-content", now);
        helper.addTestZipFile("tfile", "tfile-content", now);
        helper.addTestZipFile("td/f2", "tdf2-content", now);
        
        File zipFile = helper.createTestZipFile("tfile.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        
        FileNode tfile = new MockFileNode("tfile", "tfile-content", now);
        FileNode f1 = new MockFileNode("f1", "tdf1-content", now);
        FileNode sfile = new MockFileNode("sfile", "sfile-content", now);
        FileNode f2 = new MockFileNode("f2", "tdf2-content", now);
        
        DirNode sdir = new DirNode( "sdir", new ArrayList<Leaf>(0), new ArrayList <DirNode> (0) );
        sdir.addLeaf(sfile);
        DirNode tdir = new DirNode( "tdir", new ArrayList<Leaf>(0), new ArrayList <DirNode> (0) );
        tdir.addDir(sdir);
        DirNode td = new DirNode( "td", new ArrayList<Leaf>(0), new ArrayList <DirNode> (0) );
        td.addLeaf(f1);
        td.addLeaf(f2);
        
        DirNode otherRootNode = new DirNode( "", new ArrayList<Leaf>(0), new ArrayList <DirNode> (0) );
        otherRootNode.addLeaf(tfile);
        otherRootNode.addDir(td);
        otherRootNode.addDir(tdir);

        TreeComparor tc = new TreeComparor(false, false);
        ComparisonDirNode cdn = tc.compare(rootNode, otherRootNode);
        assertTrue("zip tree and mock tree should be same", cdn.areSame());
    }

    @Test
    public void testZipFileInputStream() throws IOException {
        Date now = new Date();
        ZipTestHelper helper = new ZipTestHelper();
        helper.addTestZipFile("tfile", "tfile-content", now);
        File zipFile = helper.createTestZipFile("testOneFileOnly.zip");
        ZipTreeBuilder builder = new ZipTreeBuilder(zipFile.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        
        List<Leaf> l = rootNode.getLeaves();
        FileNode f = (FileNode) l.get(0);
        InputStream is = f.getInputStream();
        
        int intChar = is.read();
        assertEquals("first character", 't', (char)intChar);
        assertTrue("avaiable should be greater than zero", is.available() > 0);
        if ( is.markSupported() ) {
            is.read();
            is.reset();
        }
        byte [] buf1 = new byte[2];
        assertEquals("read into 2 byte buffer should return 2",  is.read(buf1), 2);
        String s1 = new String( buf1, StandardCharsets.US_ASCII);
        assertEquals("fi", s1);

        byte [] buf2 = { (byte)' ', (byte)' ', 0, 0 };
        is.read(buf2, 2, 2);
        String s2 = new String( buf2, StandardCharsets.US_ASCII);
        assertEquals("  le", s2);
        
        assertEquals(1, is.skip(1));
        
        byte [] buf3 = new byte[7];
        assertEquals("should read 7 bytes into byte[7]", 7,  is.read(buf3));
        assertEquals("content", new String( buf3, StandardCharsets.US_ASCII));
        
        assertEquals("should be end of file", -1, is.read() );
        assertEquals("should be end of file", -1, is.read() ); //again
        
        is.close();
        is.close();
        
        try {
            is.read();
            fail("read on closed stream should raise exception");
        } catch ( IOException ioe ) {
            // expected exception
        }
    }

}
