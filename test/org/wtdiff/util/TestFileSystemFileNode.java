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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;

import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileSystemFileNode;
import org.wtdiff.util.FileNode.FileType;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFileSystemFileNode   {

    private boolean testSymlinks;
    private boolean testSpecialFile;

    @Before
    public void setUp() throws Exception {
        OperationSupportTester tester = new OperationSupportTester();
        testSymlinks = tester.isSymlinkSupported();
        testSpecialFile = tester.isSpecialFileSupported();
        //this.oneTimeSetUp()
    }
    
    @Test
    public void testNonexistentFile() throws IOException {
        try {
            new FileSystemFileNode( Paths.get("noexist"), FileType.REGFILE );
            fail("attempted creation of file system file node should throw exception if file doesn't exist");
        } catch ( IOException ioe ) {
            // do nothing, this is expected
        }
    }

    @Test
    public void testFileSystemNode() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestFile("tfile", "tfile-content");
        FileSystemFileNode node = new FileSystemFileNode(Paths.get(file.getPath()), FileType.REGFILE);
        
        assertEquals("tfile", node.getName());
        assertEquals(file.lastModified(), node.getTime());
        assertEquals(file.length(), node.getSize());
        
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
        
        MockFileNode mockNode = new MockFileNode("x", "tfile-content", new Date(file.lastModified()) );
        
        assertEquals(mockNode.getCrc(), node.getCrc());
        assertEquals(mockNode.getCrc(), node.getCrc()); // 2nd time may use cached value
        assertTrue( Arrays.equals(mockNode.getMd5(), node.getMd5()) );
        assertTrue( Arrays.equals(mockNode.getMd5(), node.getMd5()) ); // 2nd time may use cached value
        
        assertTrue(node.compareDetails(node, FileNode.CONTENT_METHOD_CONTENT));
    }
    
    /** correctly identifies file types (at those supported under OS) getFileType()
     *  symlinks and special files are not text.
     */
    @Test
    public void testFileTypeIndentification() throws Exception {
        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestFile("tfile", "tfile-content");
        
        FileSystemFileNode fileNode = new FileSystemFileNode( Paths.get( file.getPath() ) );
        assertEquals(FileType.REGFILE, fileNode.getFileType());
        FileSystemFileNode fileNode2 = new FileSystemFileNode( Paths.get( file.getPath() ), FileType.REGFILE );
        assertEquals(FileType.REGFILE, fileNode2.getFileType());
        
        
        if ( testSymlinks ) {
            File symlink = helper.createTestSymlink(file, "testSymlink");
            FileSystemFileNode symlinkNode = new FileSystemFileNode( Paths.get( symlink.getPath() ) );
            assertEquals(FileType.SYMLINK, symlinkNode.getFileType());
            assertFalse(symlinkNode.isText());
            FileSystemFileNode symlinkNode2 = new FileSystemFileNode( Paths.get( symlink.getPath() ), FileType.SYMLINK );
            assertEquals(FileType.SYMLINK, symlinkNode2.getFileType());
            
            File dir = helper.createTestDir("dir");
            File symlinkDir = helper.createTestSymlink(dir, "testSymlinkDir");
            FileSystemFileNode symlinkNode3 = new FileSystemFileNode( Paths.get( symlinkDir.getPath() ), FileType.SYMLINK );
            assertEquals(FileType.SYMLINK, symlinkNode3.getFileType());
            
        }

        if ( testSpecialFile ) {
            File special = helper.createTestFifo("fifo");
            FileSystemFileNode specialNode = new FileSystemFileNode( Paths.get( special.getPath() ) );
            assertEquals(FileType.SPECIAL, specialNode.getFileType());
            assertFalse(specialNode.isText());
            FileSystemFileNode specialNode2 = new FileSystemFileNode( Paths.get( special.getPath() ), FileType.SPECIAL );
            assertEquals(FileType.SPECIAL, specialNode2.getFileType());
        }
    }

    /**
     * throws ill arg except is given directory
     * @throws Exception
     */
    @Test
    public void testDirectoryError() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("adir");

        try {
            new FileSystemFileNode( Paths.get( dir.getPath() ) );
            fail("creation of file node from directory shoulod throw exception");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
        
    }
    
    /**
     * bad symlink is OK
     * @throws Exception
     */
    @Test
    public void testBadSymlinkOK() throws Exception {

        assumeTrue( testSymlinks );

        FileSystemTestHelper helper = new FileSystemTestHelper();
        
        File symlink = helper.createTestBadSymlink("badlink");
        FileSystemFileNode n1 = new FileSystemFileNode( Paths.get( symlink.getPath() ) );
        assertEquals(FileType.SYMLINK, n1.getFileType());
        FileSystemFileNode n2 = new FileSystemFileNode( Paths.get( symlink.getPath() ), FileType.SYMLINK );
        assertEquals(FileType.SYMLINK, n2.getFileType());

    }

    /**
     * comparison between different types yields false
     * comparison of special files always yields false 
     * 
     * @throws Exception
     */
    @Test
    public void testComparisonDifferingTypes() throws Exception {
        
        assumeTrue( testSymlinks  || testSpecialFile );

        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestFile("tfile", "tfile");
        FileSystemFileNode fileNode = new FileSystemFileNode( Paths.get( file.getPath() ) );
        File symlink;
        FileSystemFileNode symlinkNode = null;
        File special;
        FileSystemFileNode specialNode = null;
        
        if ( testSymlinks ) {
            symlink = helper.createTestSymlink(new File("tfile"), "symlink-tfile");
            symlinkNode = new FileSystemFileNode( Paths.get( symlink.getPath() ) );
        }
        if ( testSpecialFile ) {
            special = helper.createTestFifo("fifo");
            specialNode = new FileSystemFileNode( Paths.get( special.getPath() ) );
        }
        
        assertTrue( fileNode.compareDetails(fileNode, FileNode.CONTENT_METHOD_CONTENT) );

        if (testSymlinks) {
            assertFalse( fileNode.compareDetails(symlinkNode, FileNode.CONTENT_METHOD_CONTENT) );
            assertFalse( symlinkNode.compareDetails(fileNode, FileNode.CONTENT_METHOD_CONTENT) );
            assertTrue( symlinkNode.compareDetails(symlinkNode, FileNode.CONTENT_METHOD_CONTENT) );
            if ( testSpecialFile ) {
                assertFalse( specialNode.compareDetails(symlinkNode, FileNode.CONTENT_METHOD_CONTENT) );
                assertFalse( symlinkNode.compareDetails(specialNode, FileNode.CONTENT_METHOD_CONTENT) );
            }
        }

        if ( testSpecialFile ) {
            special = helper.createTestFifo("fifo2");
            specialNode = new FileSystemFileNode( Paths.get( special.getPath() ) );
            assertFalse( fileNode.compareDetails(specialNode, FileNode.CONTENT_METHOD_CONTENT) );
            assertFalse( specialNode.compareDetails(fileNode, FileNode.CONTENT_METHOD_CONTENT) );
            // handling of special files is limited to awareness.  comparison forced to false
            assertFalse( specialNode.compareDetails(specialNode, FileNode.CONTENT_METHOD_CONTENT) );
        }
        
    }
    
    private String readContentsToString(FileNode f) throws IOException {
        StringBuffer sb = new StringBuffer();
        try (InputStream is = f.getInputStream() ; InputStreamReader ir = new InputStreamReader(is) ) { 
            while (true) {
                int  c =  ir.read();
                if (c < 0)
                    break;
                sb.append((char)c);
            }
        }
        return sb.toString();
    }

    /**
     * symlink content is path to what is pointed to
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkContent() throws IOException {

        assumeTrue( testSymlinks );

        String tFileName = "tfile";
        String noFileName = "nofile";
        FileSystemTestHelper helper = new FileSystemTestHelper();
        helper.createTestFile(tFileName, "tfile-content");
        //FileSystemFileNode fileNode = new FileSystemFileNode( Paths.get( file.getPath() ) );
        File symlink = helper.createTestSymlink(new File(tFileName), "symlink-tfile");
        FileSystemFileNode symlinkNode = new FileSystemFileNode( Paths.get( symlink.getPath() ) );
        File badSymlink = helper.createTestSymlink(new File(noFileName), "symlink-nofile");
        FileSystemFileNode badSymlinkNode = new FileSystemFileNode( Paths.get( badSymlink.getPath() ) );

        String contents = readContentsToString(symlinkNode );
        assertEquals(tFileName, contents);
        assertEquals(tFileName, symlinkNode.getLinkTo() );
        
        String badContents = readContentsToString(badSymlinkNode );
        assertEquals(noFileName, badContents);
        assertEquals(noFileName, badSymlinkNode.getLinkTo() );
    }

    /**
     * special file content empty
     * 
     * @throws IOException
     */
    @Test
    public void testSpecialContent() throws Exception {

        assumeTrue( testSpecialFile );
        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File special = helper.createTestFifo("fifo");
        FileSystemFileNode specialNode = new FileSystemFileNode( Paths.get( special.getPath() ) );

        String contents = readContentsToString(specialNode );
        assertEquals("", contents);
    }
    
    
    /**
     * symlink comparison does not resolve difference between absolute and relative path
     * (i.e. should be different)
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkRelVsAbs() throws IOException  {
        assumeTrue( testSymlinks );

        String tFileName = "tfile";
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File tFile  = helper.createTestFile(tFileName, "tfile-content");

        File relSymlink = helper.createTestSymlink(new File(tFileName), "symlink-rel");
        FileSystemFileNode relSymlinkNode = new FileSystemFileNode( Paths.get( relSymlink.getPath() ) );
        
        File absSymlink = helper.createTestSymlink(tFile.getAbsoluteFile(), "symlink-abs");
        FileSystemFileNode absSymlinkNode = new FileSystemFileNode( Paths.get( absSymlink.getPath() ) );

        assertFalse(relSymlinkNode.compareDetails(absSymlinkNode, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(absSymlinkNode.compareDetails(relSymlinkNode, FileNode.CONTENT_METHOD_CONTENT));
        
    }

    /**
     * symlink time is that of symlink, not what it points to
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkTime() throws IOException  {
        assumeTrue( testSymlinks );

        String tFileName = "tfile";
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File tFile  = helper.createTestFile(tFileName, "tfile-content");
        // file system may only have 1 second precision, so drop milliseconds
        long tenMinutesAgo = (System.currentTimeMillis() / 1000) * 1000 - 60 * 10 * 1000;
        tFile.setLastModified(tenMinutesAgo);
        FileSystemFileNode tFileNode = new FileSystemFileNode( Paths.get( tFile.getPath() ) );

        File symlink = helper.createTestSymlink(new File(tFileName), "symlink-tfile");
        FileSystemFileNode symlinkNode = new FileSystemFileNode( Paths.get( symlink.getPath() ) );

        long symlinkTime = Files.getLastModifiedTime( Paths.get(symlink.getPath()), LinkOption.NOFOLLOW_LINKS).toMillis();
        
        assertTrue(tenMinutesAgo != symlinkTime);
        assertEquals(tenMinutesAgo, tFileNode.getTime());
        assertEquals(symlinkTime, symlinkNode.getTime());
        
    }

    /**
     * symlink size is length of path string
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkSize() throws IOException  {
        assumeTrue( testSymlinks );

        String tFileName = "tfile";
        FileSystemTestHelper helper = new FileSystemTestHelper();
        helper.createTestFile(tFileName, "tfile-content");

        File symlink = helper.createTestSymlink(new File(tFileName), "symlink-tfile");
        FileSystemFileNode symlinkNode = new FileSystemFileNode( Paths.get( symlink.getPath() ) );

        assertEquals(tFileName.length(), symlinkNode.getSize());
        
    }

    /** 
     * symlink with path string including unicode characters
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkContentUnicode() throws IOException {

        assumeTrue( testSymlinks );

        String tFileName = "E\u266dminor";
        FileSystemTestHelper helper = new FileSystemTestHelper();
        helper.createTestFile(tFileName, "e flat minor has six flats");
        //FileSystemFileNode fileNode = new FileSystemFileNode( Paths.get( file.getPath() ) );
        File symlink = helper.createTestSymlink(new File(tFileName), "symlink-tfile");
        FileSystemFileNode symlinkNode = new FileSystemFileNode( Paths.get( symlink.getPath() ) );

        String contents = readContentsToString(symlinkNode );
        assertEquals(tFileName, contents);
        assertEquals(tFileName, symlinkNode.getLinkTo());
    }
    
}
