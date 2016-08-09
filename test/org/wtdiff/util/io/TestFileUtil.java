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
package org.wtdiff.util.io;

import static org.junit.Assert.*;

import java.io.File;


import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.io.FileUtil;
import org.junit.After;

public class TestFileUtil    {

    private File testDir1;
    private File tfile1;
    private File testDir3;

    @Before
    public void setUp() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        testDir1 = helper.createTestDir("dir1");
        tfile1 = helper.createTestFile("tfile1", "tfile1-content\n", testDir1);
        testDir3 = helper.createTestDir("dir3");

    }

    @Test
    public void testBestExistingDirFromString() {
        assertEquals( 
            testDir1.getPath(), 
            FileUtil.bestExistingDirFromString(testDir1.getPath(), testDir3.getPath())
        );
        assertEquals( 
            testDir1.getPath(), 
            FileUtil.bestExistingDirFromString(tfile1.getPath(), testDir3.getPath())
        );
        assertEquals( 
            testDir1.getPath(), 
            FileUtil.bestExistingDirFromString((new File(testDir1, "noexist")).getPath(), testDir3.getPath())
        );
        
        File noDir = (new File(testDir1, "nodir"));
        assertEquals( 
            testDir1.getPath(), 
            FileUtil.bestExistingDirFromString((new File(noDir, "noexist")).getPath(), testDir3.getPath())
        );
        
        assertEquals( 
            testDir3.getPath(), 
            FileUtil.bestExistingDirFromString((new File("noexist")).getPath(), testDir3.getPath())
        );
        
        File noDir2 = new File("nodir2");
        assertEquals( 
            testDir3.getPath(), 
            FileUtil.bestExistingDirFromString((new File(noDir2, "noexist")).getPath(), testDir3.getPath())
        );

        assertEquals( 
            testDir3.getPath(), 
            FileUtil.bestExistingDirFromString("", testDir3.getPath())
        );

        assertEquals( 
            testDir3.getPath(), 
            FileUtil.bestExistingDirFromString(null, testDir3.getPath())
        );
    }

}
