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
package org.wtdiff.util.text;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;


import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.text.FileInputStreamSource;
import org.junit.After;
import org.junit.Before;

public class TestFileInputStreamSource  {

    private static String testFileName = "test";
    private static String testFileContent = "test-content";
    private File testFile;
    @Before
    public void setUp() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        testFile = helper.createTestFile(testFileName, testFileContent);
    }
    
    @Test
    public void testGetName() {
        FileInputStreamSource source = new FileInputStreamSource(testFile);
        assertEquals(testFile.getPath(), source.getName());
    }

    @Test
    public void testGetTime() {
        FileInputStreamSource source = new FileInputStreamSource(testFile);
        assertEquals( testFile.lastModified(), source.getTime() );
    }
    
    @Test
    public void testGetInputStream() throws IOException {
        FileInputStreamSource source = new FileInputStreamSource(testFile);
        try (InputStreamReader reader = new InputStreamReader(source.getInputStream(), "UTF-8")) {
            char[] cbuf = new char[testFileContent.length()];
            int nChars = reader.read(cbuf);
            assertEquals( testFileContent.length(), nChars );
            assertEquals( testFileContent, new String( cbuf) );
            assertEquals( -1, reader.read() );
        }
    }
}
