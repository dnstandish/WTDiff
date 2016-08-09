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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.LoggingErrorHandler;
import org.wtdiff.util.OperationSupportTester;
import org.wtdiff.util.xml.XMLTreeBuilder;

public class TestXMLTreeBuilder {

    private static final Logger logger = LogManager.getRootLogger();

    FileSystemTestHelper helper;
    String data = "<file-tree-snapshot><digests-available/><snapshot><dir name=\"n\"></dir></snapshot></file-tree-snapshot>";
    String dataNoName = "<file-tree-snapshot><digests-available/><snapshot>\n<dir name=\"\"></dir></snapshot></file-tree-snapshot>";
    String looksLikeData = "<file-tree-snapshot></file-tree-snapshot>";
    String notSnapshotData = "<file-treez-snapshot></file-treez-snapshot>";
    String dtd = "<!DOCTYPE file-tree-snapshot SYSTEM \"file-tree-snapshot.dtd\">\n";
    String xmlHeader = "<?xml version=\"1.0\"?>";

    File lookslikeSnapshot;
    File isSnapshot;
    File isSnapshotNoName;
    File isSnapshotWithDtd;
    File notSnapshot;
    File binFile;
    File dirFile;
    File noReadSnapshotFile;

    
    @Before
    public void setUp() throws Exception {
        helper = new FileSystemTestHelper();

        lookslikeSnapshot = helper.createTestFile("lookslikeSnapshot", xmlHeader + looksLikeData);
        isSnapshot = helper.createTestFile("isSnapshot", xmlHeader + data);
        isSnapshotNoName = helper.createTestFile("isSnapshotNoName", xmlHeader + dataNoName);
        isSnapshotWithDtd = helper.createTestFile("isSnapshot", xmlHeader + dtd + data);
        notSnapshot = helper.createTestFile("notSnapshot", xmlHeader + notSnapshotData);
        binFile = helper.createTestFile("binFile", new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        dirFile = helper.createTestDir("aDir");
        noReadSnapshotFile = helper.createTestFile("noReadSnapshotFile", xmlHeader + data);
        OperationSupportTester ost = new OperationSupportTester();
        ost.setReadable(noReadSnapshotFile, false);
    }

    @Test
    public void testIsSnapshot() throws IOException {

        
        //XMLTreeBuilder builder = new XMLTreeBuilder();
        assertTrue(XMLTreeBuilder.isXMLSnapshot(lookslikeSnapshot.getPath()));
        assertTrue(XMLTreeBuilder.isXMLSnapshot(isSnapshot.getPath()));
        assertTrue(XMLTreeBuilder.isXMLSnapshot(isSnapshotWithDtd.getPath()));
        assertFalse(XMLTreeBuilder.isXMLSnapshot(notSnapshot.getPath()));
        assertFalse(XMLTreeBuilder.isXMLSnapshot(binFile.getPath()));
        assertFalse(XMLTreeBuilder.isXMLSnapshot(dirFile.getPath()));
        try {
            assertFalse(XMLTreeBuilder.isXMLSnapshot("noexist"));
            fail("isXMLSnapshot() should throw exception if file doesn't exist");
        } catch(IOException ioe) {
            // this should happen
        }
        try {
            XMLTreeBuilder.isXMLSnapshot(noReadSnapshotFile.getPath());
            fail("isXMLSnapshot() should throw exception if file not readable exist");
        } catch(IOException ioe) {
            // this should happen
        }
        
    }

    private void notSnapshotHandler(String path) throws IOException {
        
        ErrorHandler h = null;
        try {
            h = new LoggingErrorHandler(logger,  false);
       
            XMLTreeBuilder builder = new XMLTreeBuilder(path);
            builder.buildTree(h);
            fail("exception should have been thrown");
        } catch (IOException ioe) {
            // this should happen
            assertTrue(h.encounteredError());
        }

        try {
            h = new LoggingErrorHandler(logger,  true);
           
            XMLTreeBuilder builder = new XMLTreeBuilder(path);
            builder.buildTree(h);
            fail("exception should have been thrown even if handler set to ignore");
        } catch (IOException ioe) {
            // this should happen
            assertTrue(h.encounteredError());
        }
        
        
    }
    @Test
    public void testBuildTree() throws IOException {
        ErrorHandler h;
        {
            h = new LoggingErrorHandler(logger,  false);
       
            XMLTreeBuilder builder = new XMLTreeBuilder(isSnapshot.getPath());
            DirNode dir = builder.buildTree(h);
            assertEquals(isSnapshot.getName(), dir.getName());
            assertEquals(isSnapshot.getPath(), dir.getRoot());
            assertEquals(1, dir.getDirs().size());
            assertEquals("n", dir.getDirs().get(0).getName());
            assertFalse(h.encounteredError());
        }
        {
            h = new LoggingErrorHandler(logger,  false);
       
            XMLTreeBuilder builder = new XMLTreeBuilder(isSnapshotNoName.getPath());
            DirNode dir = builder.buildTree(h);
            assertEquals(isSnapshotNoName.getName(), dir.getName());
            assertEquals(isSnapshotNoName.getPath(), dir.getRoot());
            assertEquals(0, dir.getDirs().size());
            assertFalse(h.encounteredError());
        }
        try {
            h = new LoggingErrorHandler(logger,  false);
       
            XMLTreeBuilder builder = new XMLTreeBuilder(lookslikeSnapshot.getPath());
            builder.buildTree(h);
            fail("exception should have been thrown");
        } catch (IOException ioe) {
            // this should happen
            assertTrue(h.encounteredError());
        }
        
        notSnapshotHandler(lookslikeSnapshot.getPath());
        notSnapshotHandler("noexist");
        notSnapshotHandler(dirFile.getPath());
        notSnapshotHandler(noReadSnapshotFile.getPath());
        
    }
}
