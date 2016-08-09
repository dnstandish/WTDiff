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

import java.io.File;
import java.io.IOException;

import org.wtdiff.util.DirCmp;
import org.wtdiff.util.DirCmp.Result;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDirCmp  {

    File tfile;
    File tfile2;
    File tFile;
    File tfileNoPerm;
    File tFILE;
    File testDir1;
    File testDir2;
    File testDir3;
    File testDir4;
    File testDir5;
    File testDir6;
    @Before
    public void setUp() throws IOException {
        OperationSupportTester ost = new OperationSupportTester();
//        boolean degenerateNamesSupported = ost.allowsDegenerateFileNames();
        FileSystemTestHelper helper = new FileSystemTestHelper();
        testDir1 = helper.createTestDir("dir1");
        testDir2 = helper.createTestDir("dir2");
        testDir3 = helper.createTestDir("dir3");
        testDir4 = helper.createTestDir("dir4");
        testDir5 = helper.createTestDir("dir4");
        testDir5 = helper.createTestDir("dir4");
        tfile = helper.createTestFile("tfile", "tfile-content\n", testDir1);
        tfile2 = helper.createTestFile("tfile", "tfile-content\r\n", testDir2);
        tFile = helper.createTestFile("tFile", "tfile-content\n", testDir5);
        tFILE = helper.createTestFile("tFILE", "tfile-content\r\n", testDir6);
        tfileNoPerm = helper.createTestFile("tfile", "tfile-content\r\n", testDir3);
        ost.setReadable(tfileNoPerm, false);
        File dir4sub = helper.createTestDir("dir4sub",testDir4);
        ost.setReadable(dir4sub, false);
        
    }

    /*
     * Note -gui option not tested  
     */
    
    @Test
    public void testNoArgs() throws Exception {
        String[] noArgs = {};
        DirCmp.Result result = DirCmp.process(noArgs);
        assertEquals(Result.HELP, result);
    }
    
    @Test
    public void testBadOpt() throws Exception {
        String[] args = {"-b", tfile.getPath(), tFile.getPath()};
        DirCmp.Result result = DirCmp.process(args);
        assertEquals(Result.HELP, result);
    }
    
    
    @Test
    public void testOneArg() throws Exception {
        String[] args = {tfile.getPath()};
        DirCmp.Result result = DirCmp.process(args);
        assertEquals(Result.HELP, result);
    }
    
    @Test
    public void testTwoArgsSame() throws Exception {
        String[] args = {tfile.getPath(), tfile.getPath()};
        DirCmp.Result result = DirCmp.process(args);
        assertEquals(Result.SAME, result);
    }
    
    @Test
    public void testTwoArgsDifferent() throws Exception {
        String[] args = {tfile.getPath(), tFile.getPath()};
        DirCmp.Result result = DirCmp.process(args);
        assertEquals(Result.DIFFERENT, result);
    }

    @Test
    public void testHelp() throws Exception {
        String[] noargsh = {"-h"};
        DirCmp.Result result = DirCmp.process(noargsh);
        assertEquals(Result.HELP, result);

        String[] argsh = {"-h", tfile.getPath(), tFile.getPath()};
        result = DirCmp.process(argsh);
        assertEquals(Result.HELP, result);
        
        String[] argshelp = {"--help", tfile.getPath(), tFile.getPath()};
        result = DirCmp.process(argshelp);
        assertEquals(Result.HELP, result);
    }
    
    @Test
    public void testAbout() throws Exception {
        String[] noargsh = {"-a"};
        DirCmp.Result result = DirCmp.process(noargsh);
        assertEquals(Result.HELP, result);

        String[] argsh = {"-a", tfile.getPath(), tFile.getPath()};
        result = DirCmp.process(argsh);
        assertEquals(Result.HELP, result);
        
        String[] argshelp = {"--about", tfile.getPath(), tFile.getPath()};
        result = DirCmp.process(argshelp);
        assertEquals(Result.HELP, result);
    }
    
    @Test
    public void testIgnoreCase() throws Exception {
        String[] args1 = {tfile.getPath(), tFile.getPath()};
        DirCmp.Result result = DirCmp.process(args1);
        assertEquals(Result.DIFFERENT, result);
        
        String[] args2 = {"-i", tfile.getPath(), tFile.getPath()};
        result = DirCmp.process(args2);
        assertEquals(Result.SAME, result);        

        String[] args3 = {"-i", tfile.getPath(), tFILE.getPath()};
        result = DirCmp.process(args3);
        assertEquals(Result.DIFFERENT, result);        

        String[] args4 = {"--ignorecase", tfile.getPath(), tFile.getPath()};
        result = DirCmp.process(args4);
        assertEquals(Result.SAME, result);        

    }
    @Test
    public void testTextCompare() throws Exception {

        String[] args1 = {tfile.getPath(), tfile2.getPath()};
        DirCmp.Result result = DirCmp.process(args1);
        assertEquals(Result.DIFFERENT, result);    

        String[] args2 = {"-t", tfile.getPath(), tfile2.getPath()};
        result = DirCmp.process(args2);
        assertEquals(Result.SAME, result);        

        String[] args3 = {"--textcompare", tfile.getPath(), tfile2.getPath()};
        result = DirCmp.process(args3);
        assertEquals(Result.SAME, result);

    }
    
    @Test
    public void testIgnorePermErrorOption() throws Exception {
        
        {
            String[] args = {testDir2.getPath(), testDir3.getPath()};
            DirCmp.Result result = DirCmp.process(args);
            assertEquals(Result.FAILED, result);    
        }
        
        {
            String[] args = {"-p", testDir2.getPath(), testDir3.getPath()};
            DirCmp.Result result = DirCmp.process(args);
            assertEquals(Result.DIFFERENT_WITH_ERRORS, result);    
        }

        {
            String[] args = {"--ignorepermerror", testDir2.getPath(), testDir3.getPath()};
            DirCmp.Result result = DirCmp.process(args);
            assertEquals(Result.DIFFERENT_WITH_ERRORS, result);    
        }

        {
            String[] args = {"--ignorepermerror", testDir4.getPath(), testDir4.getPath()};
            DirCmp.Result result = DirCmp.process(args);
            assertEquals(Result.SAME_WITH_ERRORS, result);    
        }
        
    }
    
    @Test
    public void testResult() {
        assertEquals( 0, Result.SAME.getExitCode() );
        assertEquals( 4, Result.SAME_WITH_ERRORS.getExitCode() );
        assertEquals( 8, Result.DIFFERENT.getExitCode() );
        assertEquals( 12, Result.DIFFERENT_WITH_ERRORS.getExitCode() );
        assertEquals( 9, Result.HELP.getExitCode() );
        assertEquals( 16, Result.FAILED.getExitCode() );
    }
}
