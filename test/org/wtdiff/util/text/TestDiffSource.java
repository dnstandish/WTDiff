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
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;


import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.text.DiffSource;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.text.TextUtil;
import org.wtdiff.util.text.TextUtil.LineSeparator;
import org.junit.After;
public class TestDiffSource  {

    private FileSystemTestHelper helper;
    private static String testFileName = "test";
    private static String testFileContent = "test-content" + TextUtil.DEFAULT_SEP;
    private File testFile;

    private Charset defaultCharset = Charset.defaultCharset();
    private Charset utf8 = Charset.forName("UTF-8");
    private Charset utf16 = Charset.forName("UTF-16");
    private Charset utf16le = Charset.forName("UTF-16LE");
    private Charset iso88591 = Charset.forName("ISO-8859-1");
    private Charset usAscii = Charset.forName("US-ASCII");
    
    @Before
    public void setUp() throws Exception {
        helper = new FileSystemTestHelper();
        testFile = helper.createTestFile(testFileName, testFileContent);
    }

    @Test
    public void testGetName() throws IOException {
        FileInputStreamSource source = new FileInputStreamSource(testFile);
        DiffSource ds = new DiffSource(source);
        assertEquals(testFile.getPath(), ds.getName());
    }

    @Test
    public void testGetLineSep() throws IOException {
        {
            String testFileNameLf = "testLF";
            String testFileContentLf = "testLF-content" + new String(LineSeparator.LF.chars());
            File testFileLf = helper.createTestFile(testFileNameLf, testFileContentLf);
            FileInputStreamSource source = new FileInputStreamSource(testFileLf);
            DiffSource ds = new DiffSource(source);
            assertEquals( LineSeparator.LF, ds.getLineSep());
        }
        {
            String testFileNameCrLf = "testCRLF";
            String testFileContentCrLf = "testCRLF-content" + new String(LineSeparator.CRLF.chars());
            File testFileCrLf = helper.createTestFile(testFileNameCrLf, testFileContentCrLf);
            FileInputStreamSource source = new FileInputStreamSource(testFileCrLf);
            DiffSource ds = new DiffSource(source);
            assertEquals( LineSeparator.CRLF, ds.getLineSep());
        }        
    }
    
    @Test
    public void testEncoding() throws IOException {
        {
            FileInputStreamSource source = new FileInputStreamSource(testFile);
            DiffSource ds = new DiffSource(source);
            assertEquals(defaultCharset, ds.getEncoding());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(testFileContent, lines.get(0));            
        }
        
        {
            String testFileNameUTF8 = "testUTF8";
            String content = "testUTF8-cont\u00E9nt";
            byte[] testFileContentUTF8 = (content + new String(TextUtil.DEFAULT_SEP.chars())).getBytes(utf8);            
            File testFileUTF8 = helper.createTestFile(testFileNameUTF8, testFileContentUTF8);
            FileInputStreamSource source = new FileInputStreamSource(testFileUTF8);
            DiffSource ds = new DiffSource(source);
            
            assertEquals(defaultCharset, ds.getEncoding());
            ds.forceEncoding(utf8);
            assertEquals(utf8, ds.getEncoding());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(content, lines.get(0));            

            ds.forceEncoding(null);
            assertEquals(defaultCharset, ds.getEncoding());

        }

        {
            String testFileName88591 = "test88591";
            String content = "testUTF8-cont\u00E9nt";
            byte[] testFileContent88591 = (content + new String(TextUtil.DEFAULT_SEP.chars())).getBytes(iso88591);            
            File testFile88591 = helper.createTestFile(testFileName88591, testFileContent88591);
            FileInputStreamSource source = new FileInputStreamSource(testFile88591);
            DiffSource ds = new DiffSource(source);
            
            assertEquals(defaultCharset, ds.getEncoding());
            ds.forceEncoding(iso88591);
            assertEquals(iso88591, ds.getEncoding());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(content, lines.get(0));            

            ds.forceEncoding(usAscii);
            assertEquals(usAscii, ds.getEncoding());
            lines = ds.getLines();
            assertEquals(1, lines.size());
            assertFalse(content.equals(lines.get(0)));
        }
    
        {
            String testFileNameUtf16 = "testUTF16";
            String content = "testUTF16-cont\u00E9nt";
            byte[] testFileContentUtf16 = (content + new String(TextUtil.DEFAULT_SEP.chars())).getBytes(utf16);            
            File testFileUtf16 = helper.createTestFile(testFileNameUtf16, testFileContentUtf16);
            FileInputStreamSource source = new FileInputStreamSource(testFileUtf16);
            DiffSource ds = new DiffSource(source);
            
            assertEquals(defaultCharset, ds.getEncoding());
            ds.forceEncoding(utf16);
            assertEquals(utf16, ds.getEncoding());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(content, lines.get(0));            
        }

        {
            String testFileNameAscii = "testAscii";
            String content = "testAscii-content";
            byte[] testFileContentAscii = (content + new String(TextUtil.DEFAULT_SEP.chars())).getBytes(usAscii);           
            File testFileAscii = helper.createTestFile(testFileNameAscii, testFileContentAscii);
            FileInputStreamSource source = new FileInputStreamSource(testFileAscii);
            DiffSource ds = new DiffSource(source);
            
            assertEquals(defaultCharset, ds.getEncoding());
            ds.forceEncoding(usAscii);
            assertEquals(usAscii, ds.getEncoding());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(content, lines.get(0));            
        }
        {   // forcing encoding does not reset forced line separator
            String testFileNameUTF8cr = "testUTF8cr";
            String content = "testUTF8-cont\u00E9nt\r\r";
            byte[] testFileContentUTF8cr = (content).getBytes(utf8);            
            File testFileUTF8cr = helper.createTestFile(testFileNameUTF8cr, testFileContentUTF8cr);
            FileInputStreamSource source = new FileInputStreamSource(testFileUTF8cr);
            DiffSource ds = new DiffSource(source);
            
            assertEquals(LineSeparator.CR, ds.getLineSep());
            ds.forceLineSep(LineSeparator.LF);
            ds.forceEncoding(iso88591);
            assertEquals(iso88591, ds.getEncoding());
            assertEquals(LineSeparator.LF, ds.getLineSep());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(new String(testFileContentUTF8cr, iso88591), lines.get(0));
            
        }

        {   // forcing encoding resets unforced line separator
            String testFileNameUTF16LEcr = "testUTF16LEcr";
            String content = "testUTF16LE-cont\u00E9nt\u010A\u020A\u030A\r";
            byte[] testFileContentUTF16LEcr = (content).getBytes(utf16le);            
            File testFileUTF16LEcr = helper.createTestFile(testFileNameUTF16LEcr, testFileContentUTF16LEcr);
            FileInputStreamSource source = new FileInputStreamSource(testFileUTF16LEcr);
            DiffSource ds = new DiffSource(source);
            
            // under iso-8859-1 \u010a looks like 2 characters \x01, \x0a
            // thus there are 3 LF's and one CR in content, so should guess LF as line separator
            // in this situation
            ds.forceEncoding(iso88591);
            assertEquals(LineSeparator.LF, ds.getLineSep());
            ds.forceEncoding(utf16le);
            assertEquals(utf16le, ds.getEncoding());
            assertEquals(LineSeparator.CR, ds.getLineSep());
            List<String> lines = ds.getLines();
            assertEquals(1, lines.size());
            assertEquals(content, lines.get(0) + "\r");
        }


    }

    @Test
    public void testForceLineSep() throws IOException {
        String line1 ="test2-";
        String line2 = "content";
        {
            String testFileName2CrLf = "test2CRLF";
            String testFileContent2CrLf = line1 + "\r\n" + line2 + "\r\n";
            File testFile2CrLf = helper.createTestFile(testFileName2CrLf, testFileContent2CrLf);
            FileInputStreamSource source = new FileInputStreamSource(testFile2CrLf);
            DiffSource ds = new DiffSource(source);
            assertEquals( LineSeparator.CRLF, ds.getLineSep());
            List<String> lines = ds.getLines();
            assertEquals(2, lines.size());
            assertEquals(line1, lines.get(0));            
            assertEquals(line2, lines.get(1));
            // should get the same thing if we do it again 
            lines = ds.getLines();
            assertEquals(2, lines.size());
            assertEquals(line1, lines.get(0));            
            assertEquals(line2, lines.get(1));
            // should get the same thing if we do it again 
            ds.forceLineSep(LineSeparator.CRLF);
            lines = ds.getLines();
            assertEquals(2, lines.size());
            assertEquals(line1, lines.get(0));            
            assertEquals(line2, lines.get(1));

            ds.forceLineSep(LineSeparator.CR);
            lines = ds.getLines();
            assertEquals(3, lines.size());
            assertEquals(line1, lines.get(0));            
            assertEquals("\n" + line2, lines.get(1));
            assertEquals("\n", lines.get(2));
            assertTrue(ds.isMissingFinalLineSep());
            assertEquals( LineSeparator.CR, ds.getLineSep());
            
            ds.forceLineSep(null);
            assertEquals( LineSeparator.CRLF, ds.getLineSep());
            lines = ds.getLines();
            assertEquals(2, lines.size());
            assertEquals(line1, lines.get(0));            
            assertEquals(line2, lines.get(1));
            
            ds.forceLineSep(LineSeparator.LF);
            lines = ds.getLines();
            assertEquals(2, lines.size());
            assertEquals(line1+ "\r", lines.get(0));            
            assertEquals(line2+ "\r", lines.get(1));            
            assertEquals( LineSeparator.LF, ds.getLineSep());

        }
        
    }
    
//    @Test
//    public void testWhiteSpace() throws IOException {
//        
//        String testFileNameWS = "testWS";
//        String testFileContentWS = " a  b \n";
//        File testFileWS = helper.createTestFile(testFileNameWS, testFileContentWS);
//        FileInputStreamSource source = new FileInputStreamSource(testFileWS);
//        DiffSource ds = new DiffSource(source);
//        assertFalse(ds.isCompactWhiteSpace());
//        assertFalse(ds.isIgnoreWhiteSpace());
//        assertFalse(ds.isTrimWhiteSpace());
//        List<String>  lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals(" a  b ", lines.get(0));
//        
//        ds.setCompactWhiteSpace(true);
//        assertTrue(ds.isCompactWhiteSpace());
//        assertFalse(ds.isIgnoreWhiteSpace());
//        assertFalse(ds.isTrimWhiteSpace());
//        lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals(" a b ", lines.get(0));
//        
//        ds.setTrimWhiteSpace(true);
//        assertTrue(ds.isCompactWhiteSpace());
//        assertFalse(ds.isIgnoreWhiteSpace());
//        assertTrue(ds.isTrimWhiteSpace());
//        lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals("a b", lines.get(0));
//
//        ds.setIgnoreWhiteSpace(true);
//        assertTrue(ds.isCompactWhiteSpace());
//        assertTrue(ds.isIgnoreWhiteSpace());
//        assertTrue(ds.isTrimWhiteSpace());
//        lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals("ab", lines.get(0));
//
//        ds.setCompactWhiteSpace(false);
//        assertFalse(ds.isCompactWhiteSpace());
//        assertTrue(ds.isIgnoreWhiteSpace());
//        assertTrue(ds.isTrimWhiteSpace());
//        lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals("ab", lines.get(0));
//
//        ds.setTrimWhiteSpace(false);
//        assertFalse(ds.isCompactWhiteSpace());
//        assertTrue(ds.isIgnoreWhiteSpace());
//        assertFalse(ds.isTrimWhiteSpace());
//        lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals("ab", lines.get(0));
//
//        ds.setIgnoreWhiteSpace(false);
//        ds.setTrimWhiteSpace(true);
//        assertFalse(ds.isCompactWhiteSpace());
//        assertFalse(ds.isIgnoreWhiteSpace());
//        assertTrue(ds.isTrimWhiteSpace());
//        lines = ds.getMassagedLines();
//        assertEquals(1, lines.size());
//        assertEquals("a  b", lines.get(0));
//
//        ds.setCompactWhiteSpace(false);
//        assertFalse(ds.isCompactWhiteSpace());
//        ds.setCompactWhiteSpace(false);
//        assertFalse(ds.isCompactWhiteSpace());
//        ds.setCompactWhiteSpace(true);
//        assertTrue(ds.isCompactWhiteSpace());
//        ds.setCompactWhiteSpace(true);
//        assertTrue(ds.isCompactWhiteSpace());
//
//        ds.setIgnoreWhiteSpace(false);
//        assertFalse(ds.isIgnoreWhiteSpace());
//        ds.setIgnoreWhiteSpace(false);
//        assertFalse(ds.isIgnoreWhiteSpace());
//        ds.setIgnoreWhiteSpace(true);
//        assertTrue(ds.isIgnoreWhiteSpace());
//        ds.setIgnoreWhiteSpace(true);
//        assertTrue(ds.isIgnoreWhiteSpace());
//
//        ds.setTrimWhiteSpace(false);
//        assertFalse(ds.isTrimWhiteSpace());
//        ds.setTrimWhiteSpace(false);
//        assertFalse(ds.isTrimWhiteSpace());
//        ds.setTrimWhiteSpace(true);
//        assertTrue(ds.isTrimWhiteSpace());
//        ds.setTrimWhiteSpace(true);
//        assertTrue(ds.isTrimWhiteSpace());
//    }
    
    @Test
    public void testReadError() throws IOException {
        Date now = new Date();
        try {
            ExceptionInputStreamSource source = new ExceptionInputStreamSource("readErrror", now.getTime(),"readErrorContent", 0);
            DiffSource ds = new DiffSource(source);
            ds.getLineSep();
            fail("IOexception not thrown");
        } catch (IOException ioe) {
            // this is supposed to happen
        }
        
        {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < 10000; i++) {
                sb.append("\r\n");
            }
            ExceptionInputStreamSource source = new ExceptionInputStreamSource("readErrror1000", now.getTime(), sb.toString(), 9000);
            DiffSource ds = new DiffSource(source);
            ds.getLineSep();
            try {            
                ds.getLines();                
                fail("IOexception not thrown");
            } catch (IOException ioe) {
                // this is supposed to happen
            }
        }
    }
}
