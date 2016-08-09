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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.OperationSupportTester;
import org.wtdiff.util.CompareController;
import org.wtdiff.util.xml.Snapshotter;

public class TestSnapshotter {

    FileSystemTestHelper helper;
    private OperationSupportTester ost;
    
    @Before
    public void setUp() throws Exception {
        helper = new FileSystemTestHelper();
        ost = new OperationSupportTester();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testUsage() {
        assertTrue(Snapshotter.usage().startsWith("usage"));
    }

    @Test
    public void testBasicSnapshot() throws IOException {
        File testDir = helper.createTestDir("testBasicSnapshot");
        File testSubDir = helper.createTestDir("sd", testDir);
        helper.createTestFile("aFile", "aFile content", testSubDir);
        File saveFile = new File( testDir, "snapshot.xml");
        saveFile.deleteOnExit();
        Snapshotter snapper = new Snapshotter();
        int status = snapper.createSnapshot(testSubDir.getPath(), saveFile.getPath() );
        assertEquals(0, status);
        
        CompareController controller = new CompareController();
        controller.setOldRoot(testSubDir.getPath());
        controller.setNewRoot(saveFile.getPath());
        controller.compare();
        assertTrue(controller.getCompareRootNode().areSame());
    }
    
    @Test
    public void testNonexistentRoot() throws IOException {
        File testDir = helper.createTestDir("testNonexistentRoot");
        File noexistFile = new File( testDir, "noexist");
        File saveFile = new File( testDir, "snapshot.xml");
        Snapshotter snapper = new Snapshotter();
        int status = snapper.createSnapshot(noexistFile.getPath(), saveFile.getPath() );
        assertEquals(8, status);
        assertFalse(saveFile.exists());
    }

    @Test
    public void testNoReadRoot() throws IOException {
        File testDir = helper.createTestDir("testNoReadRoot");
        File testSubDir = helper.createTestDir("sd", testDir);
        File noReadFile = helper.createTestFile("noread", "noread content", testSubDir);
        ost.setReadable(noReadFile, false);
        File saveFile = new File( testDir, "snapshot.xml");
        Snapshotter snapper = new Snapshotter();
        int status = snapper.createSnapshot(noReadFile.getPath(), saveFile.getPath() );
        assertEquals(8, status);
        assertFalse(saveFile.exists());
    }

    @Test
    public void testIOError() throws IOException {
        File testDir = helper.createTestDir("testIOError");
        File testSubDir = helper.createTestDir("sd", testDir);
        helper.createTestFile("aFile", "aFIle content", testSubDir);
        File saveFile = new File( testDir, "snapshot.xml");
        saveFile.createNewFile();
        saveFile.deleteOnExit();
        ost.setWritable(saveFile, false);

        Snapshotter snapper = new Snapshotter();
        int status = snapper.createSnapshot(testSubDir.getPath(), saveFile.getPath() );
        assertEquals(16, status);
        ost.setWritable(saveFile, true);
    }

    
}
