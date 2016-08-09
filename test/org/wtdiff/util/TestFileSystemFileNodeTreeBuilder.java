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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileSystemNodeTreeBuilder;
import org.wtdiff.util.LoggingErrorHandler;
import org.wtdiff.util.NoHandleErrorHandler;
import org.wtdiff.util.Node;
import org.wtdiff.util.FileNode.FileType;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFileSystemFileNodeTreeBuilder   {

    private boolean testSymlinks;
    private boolean testSpecialFile;
    private ErrorHandler noHandler = new NoHandleErrorHandler();
    private static final Logger logger = LogManager.getRootLogger();
    private OperationSupportTester ost;
    
    @Before
    public void setUp() throws Exception {
        ost = new OperationSupportTester();
        testSymlinks = ost.isSymlinkSupported();
        testSpecialFile = ost.isSpecialFileSupported();
    }
    
    @Test
    public void testNoExistRoot() throws IOException {

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder("noexist");
        try {
            builder.buildTree(noHandler);
            fail("buildTree should throw exception if root doesn't exist");
        } catch (IOException ioe) {
            // normal
        }
        
    }
    
    @Test
    public void testOneFileOnly() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestFile("tfile", "tfile-content");

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(file.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);
        
        assertEquals(file.getParent(), rootNode.getRoot());
        
        assertEquals( 0, rootNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 1, rootNode.getLeaves().size() ); // should be 1 file
        assertEquals(file.getName(), ((Node)rootNode.getLeaves().get(0)).getName() );
    }
    
    @Test
    public void testOneDirOnly() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestDir("tdir");

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(file.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);

        assertEquals(file.getCanonicalPath(), rootNode.getRoot());
        
        assertEquals( 0, rootNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 0, rootNode.getLeaves().size() ); // should be no files
        assertEquals(file.getName(), rootNode.getName() );
    }

    @Test
    public void testDirPermFail() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestDir("tdir");

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(file.getCanonicalPath());

        ost.setExecutable(file, false);
        try {
            builder.buildTree(noHandler);
            fail("build tree should throw IO exceptions when dir is not executable");
        } catch (IOException ioe) {
            // this is expected
        }
        ost.setExecutable(file, true);
        ost.setReadable(file, false);
        try {
            builder.buildTree(noHandler);
            fail("build tree should throw IO exceptions when dir is not readable");
        } catch (IOException ioe) {
            // this is expected
        }
        ost.setReadable(file, true);
    }

    @Test
    public void testSubdirs() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("tdir");
        File sdir = helper.createTestDir("sdir", dir);
        File tfile = helper.createTestFile("tfile", "tfile-content", dir);
        File sfile = helper.createTestFile("sfile", "sfile-content", sdir);

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(dir.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);

        assertEquals( 1, rootNode.getDirs().size() ); // should be 1 subdirectories
        assertEquals( 1, rootNode.getLeaves().size() ); // should be 1 files

        assertEquals(tfile.getName(), ((Node)rootNode.getLeaves().get(0)).getName() );
        assertEquals(sdir.getName(), ((Node)rootNode.getDirs().get(0)).getName() );

        DirNode sNode =(DirNode)rootNode.getDirs().get(0);
        assertEquals( 0, sNode.getDirs().size() ); // should be no subdirectories
        assertEquals( 1, sNode.getLeaves().size() ); // should be 1 files
        assertEquals(sfile.getName(), ((Node)sNode.getLeaves().get(0)).getName() );
    }

    @Test
    public void testSubdirsPermFail() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("tdir");
        File sdir1 = helper.createTestDir("sdir1", dir);
        File sdir2 = helper.createTestDir("sdir2", dir);

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(dir.getCanonicalPath());

        // subdirectory permission problem should throw exception unless error handler handlesthe error
        ost.setExecutable(sdir1, false);
        ErrorHandler loggingHandler = new LoggingErrorHandler(logger,  false);
        try {
            builder.buildTree(loggingHandler);
            fail("subdirectory no executable should throw excetpion");
        } catch (IOException ioe) {
            // this should happen
        }
        assertTrue(loggingHandler.encounteredError());
        ost.setExecutable(sdir1, true);

        ost.setReadable(sdir2, false);
        try {
            builder.buildTree(loggingHandler);
            fail("subdirectory no executable should throw excetpion");
        } catch (IOException ioe) {
            // this should happen
        }
        assertTrue(loggingHandler.encounteredError());
        ost.setReadable(sdir2, true);

        ost.setExecutable(sdir1,false);
        ost.setReadable(sdir2, false);
        LoggingErrorHandler loggingHandler2 = new LoggingErrorHandler(logger,  true);
        builder.buildTree(loggingHandler2);
        assertTrue(loggingHandler2.encounteredError());
    }

    /**
     * root symlink ok and resolve to what ever symlink points to (regular file case)
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkFileRoot() throws IOException {
        assumeTrue( testSymlinks );
        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestFile("tfile", "tfile-content");
        File symlink = helper.createTestSymlink(file, "testSymlinkRootFile");


        FileSystemNodeTreeBuilder builder1 = new FileSystemNodeTreeBuilder(file.getCanonicalPath());
        FileSystemNodeTreeBuilder builder2 = new FileSystemNodeTreeBuilder(symlink.getPath());
        DirNode fileRootNode = builder1.buildTree(noHandler);
        DirNode symlinkRootNode = builder2.buildTree(noHandler);
        
        FileNode fileNode = (FileNode)fileRootNode.getLeaves().get(0);
        FileNode symlinkNode = (FileNode)symlinkRootNode.getLeaves().get(0);
        assertEquals(symlink.getParent(), symlinkRootNode.getRoot() );
        assertEquals(FileNode.FileType.REGFILE, symlinkNode.getFileType() );
        
        assertTrue(symlinkNode.compareDetails(fileNode, FileNode.CONTENT_METHOD_CONTENT));
        
    }

    /**
     * root symlink ok and resolve to what ever symlink points to (directory case)
     * 
     * @throws IOException
     */
    @Test
    public void testSymlinkDirRoot() throws IOException {
        assumeTrue( testSymlinks );
        
        String tFileName = "tfile";
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("tdir");
        helper.createTestFile(tFileName, "tfile-content", dir);
        File symlink = helper.createTestSymlink(dir, "testSymlinkRootFile");


        FileSystemNodeTreeBuilder builder1 = new FileSystemNodeTreeBuilder(dir.getCanonicalPath());
        FileSystemNodeTreeBuilder builder2 = new FileSystemNodeTreeBuilder(symlink.getPath());
        DirNode dirRootNode = builder1.buildTree(noHandler);
        DirNode symlinkRootNode = builder2.buildTree(noHandler);
        
        assertEquals(1,dirRootNode.getLeaves().size());
        assertEquals(1,symlinkRootNode.getLeaves().size());
        
        FileNode node1 = (FileNode)dirRootNode.getLeaves().get(0);
        FileNode node2 = (FileNode)symlinkRootNode.getLeaves().get(0);
        assertEquals(symlink.getPath(), symlinkRootNode.getRoot() );
        assertEquals(FileNode.FileType.REGFILE, node2.getFileType() );
        assertEquals(tFileName, node2.getName());
        assertTrue(node2.compareDetails(node1, FileNode.CONTENT_METHOD_CONTENT));
        
    }
    
    /**
     * root bad symlink throws exception 
     * 
     * @throws IOException
     */
    @Test
    public void testBadSymlinkRoot() throws IOException {
        assumeTrue( testSymlinks );
        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File symlink = helper.createTestBadSymlink("badsymlink"); 

        FileSystemNodeTreeBuilder builder2 = new FileSystemNodeTreeBuilder(symlink.getPath());
        
        try {
            builder2.buildTree(noHandler);
            fail("attempt to build tree from bad symlink should throw exception");
        } catch (IOException ioe) {
            // this is what should happen
        }
        
        
    }
    
    /**
     * root special file ok
     * 
     * @throws Exception
     */
    @Test
    public void testSpecialRoot() throws Exception {
        assumeTrue( testSpecialFile );

        FileSystemTestHelper helper = new FileSystemTestHelper();
        File special = helper.createTestFifo("special"); 

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(special.getPath());
        
        DirNode rootNode = builder.buildTree(noHandler);
        FileNode fileNode = (FileNode)rootNode.getLeaves().get(0);
        assertEquals(special.getParent(), rootNode.getRoot() );
        assertEquals(FileNode.FileType.SPECIAL, fileNode.getFileType() );
        
    }
    
    /**
     * dir including symlink handles symlink correctly
     *  
     * @throws IOException
     */
    @Test
    public void testSymlinkUnderRoot() throws IOException {
        assumeTrue(testSymlinks);
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("tdir");
        File tfile = helper.createTestFile("tfile", "tfile-content", dir);
        File symlink = helper.createTestSymlink(tfile, "symlink", dir);

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(dir.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);

        assertEquals( 2, rootNode.getLeaves().size() ); // should be 2 files
        FileNode child1 = (FileNode)rootNode.getLeaves().get(0);
        FileNode child2 = (FileNode)rootNode.getLeaves().get(1);
        if ( tfile.getName().equals(child2.getName())) {
            FileNode temp = child1;
            child1 = child2;
            child2 = temp;
        }
        assertEquals(symlink.getName(), child2.getName() );            
        assertEquals(FileType.SYMLINK, child2.getFileType() );            
    }
    
    /** 
     * dir including special file handles special file correctly
     * 
     * @throws Exception
     */
    @Test
    public void testSpecialUnderRoot() throws Exception {
        assumeTrue(testSpecialFile);
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("tdir");
        File special = helper.createTestFifo("fifo", dir);

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(dir.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);

        assertEquals( 1, rootNode.getLeaves().size() ); // should be 1 files
        FileNode child = (FileNode)rootNode.getLeaves().get(0);
        assertEquals(special.getName(), child.getName() );            
        assertEquals(FileType.SPECIAL, child.getFileType() );            
    }
    
    /**
     *  symlinks under root are not followed
     *   
     * @throws IOException
     */
    @Test
    public void testSymlinkNotFollowed() throws IOException {
        assumeTrue(testSymlinks);
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File dir = helper.createTestDir("tdir");
        File sdir = helper.createTestDir("sdir", dir);
        File symlink = helper.createTestSymlink(sdir, "symlink", dir);

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(dir.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);

        assertEquals( 1, rootNode.getLeaves().size() ); // should be 1 file
        assertEquals( 1, rootNode.getDirs().size() ); // should be 1 dir
        FileNode child = (FileNode)rootNode.getLeaves().get(0);
        assertEquals(symlink.getName(), child.getName() );            
        assertEquals(FileType.SYMLINK, child.getFileType() );            
    }
    
}
