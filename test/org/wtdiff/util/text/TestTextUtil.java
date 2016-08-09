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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import org.wtdiff.util.ExceptionInputStream;
import org.wtdiff.util.text.TextUtil;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestTextUtil   {

    @Test
    public void testRemoveWhiteSpace() {
        
        assertEquals(
            "",
            TextUtil.removeWhiteSpace("")
        );
        assertEquals(
            "",
            TextUtil.removeWhiteSpace(" ")
        );
        assertEquals(
            "",
            TextUtil.removeWhiteSpace("\n")
        );
        assertEquals(
            "",
            TextUtil.removeWhiteSpace("  ")
        );
        
        assertEquals(
            "a",
            TextUtil.removeWhiteSpace(" a")
        );
        assertEquals(
            "a",
            TextUtil.removeWhiteSpace("a ")
        );
        assertEquals(
            "ab",
            TextUtil.removeWhiteSpace(" a b ")
        );
        
    }
    
    @Test
    public void testVisibleContolChars() {
        assertEquals(
            "",
            TextUtil.visibleContolChars("")
        );
        assertEquals(
            " ",
            TextUtil.visibleContolChars(" ")
        );
        assertEquals(
            "\\n",
            TextUtil.visibleContolChars("\n")
        );
        assertEquals(
            "\\r",
            TextUtil.visibleContolChars("\r")
        );
        assertEquals(
            "\\t",
            TextUtil.visibleContolChars("\t")
        );
        assertEquals(
            "\\f",
            TextUtil.visibleContolChars("\f")
        );
        
        assertEquals(
            "^@",
            TextUtil.visibleContolChars("\0")
        );
        assertEquals(
            "^_",
            TextUtil.visibleContolChars("\037")
        );
        assertEquals(
            "<7F>",
            TextUtil.visibleContolChars("\177")
        );
        assertEquals(
            "<80>",
            TextUtil.visibleContolChars("\200")
        );
        assertEquals(
            "<9F>",
            TextUtil.visibleContolChars("\237")
        );
        assertEquals(
            "\240",
            TextUtil.visibleContolChars("\240")
        );
        assertEquals(
            "\\tabc\\r\\n",
            TextUtil.visibleContolChars("\tabc\r\n")
        );
    }
    
    @Test
    public void testBoxContolChars() {
        assertEquals(
            "",
            TextUtil.boxContolChars("")
        );
        assertEquals(
            " ",
            TextUtil.boxContolChars(" ")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\n")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\r")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\t")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\f")
        );
        
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\0")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\037")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\177")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\200")
        );
        assertEquals(
            "\u25af",
            TextUtil.boxContolChars("\237")
        );
        assertEquals(
            "\240",
            TextUtil.boxContolChars("\240")
        );
        assertEquals(
            "\u25afabc\u25af\u25af",
            TextUtil.boxContolChars("\tabc\r\n")
        );
        
    }

    @Test
    public void testRemoveContolChars() {
        assertEquals(
            "",
            TextUtil.removeContolChars("")
        );
        assertEquals(
            " ",
            TextUtil.removeContolChars(" ")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\n")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\r")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\t")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\f")
        );
        
        assertEquals(
            "",
            TextUtil.removeContolChars("\0")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\037")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\177")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\200")
        );
        assertEquals(
            "",
            TextUtil.removeContolChars("\237")
        );
        assertEquals(
            "\240",
            TextUtil.visibleContolChars("\240")
        );
        assertEquals(
            "abc",
            TextUtil.removeContolChars("\tabc\r\n")
        );
    }
    
    
    
    @Test
    public void testNormalizeWhiteSpace() {
        assertEquals(
            "",
            TextUtil.normalizeWhiteSpace("")
        );
        assertEquals(
            " ",
            TextUtil.normalizeWhiteSpace(" ")
        );
        assertEquals(
            " ",
            TextUtil.normalizeWhiteSpace("  ")
        );
        assertEquals(
            " a",
            TextUtil.normalizeWhiteSpace(" a")
        );
        assertEquals(
            " a",
            TextUtil.normalizeWhiteSpace("  a")
        );
        assertEquals(
            "a ",
            TextUtil.normalizeWhiteSpace("a ")
        );
        assertEquals(
            "a ",
            TextUtil.normalizeWhiteSpace("a  ")
        );
        assertEquals(
            "a ",
            TextUtil.normalizeWhiteSpace("a\n")
        );
        assertEquals(
            "a ",
            TextUtil.normalizeWhiteSpace("a\r\n")
        );
        assertEquals(
            "a b",
            TextUtil.normalizeWhiteSpace("a\r\nb")
        );
    }

    @Test
    public void testNormalizeTrimWhiteSpace() {
        assertEquals(
            "",
            TextUtil.normalizeTrimWhiteSpace("")
        );
        assertEquals(
            "",
            TextUtil.normalizeTrimWhiteSpace(" ")
        );
        assertEquals(
            "",
            TextUtil.normalizeTrimWhiteSpace("  ")
        );
        assertEquals(
            "a",
            TextUtil.normalizeTrimWhiteSpace(" a")
        );
        assertEquals(
            "a",
            TextUtil.normalizeTrimWhiteSpace("  a")
        );
        assertEquals(
            "a",
            TextUtil.normalizeTrimWhiteSpace("a ")
        );
        assertEquals(
            "a",
            TextUtil.normalizeTrimWhiteSpace("a  ")
        );
        assertEquals(
            "a",
            TextUtil.normalizeTrimWhiteSpace("a\n")
        );
        assertEquals(
            "a",
            TextUtil.normalizeTrimWhiteSpace("a\r\n")
        );
        assertEquals(
            "a b",
            TextUtil.normalizeTrimWhiteSpace("a\r\nb")
        );
    }

    @Test
    public void testCharsToLineSeparator() {
        assertEquals( null , TextUtil.charsToLineSeparator(null));
        assertEquals( TextUtil.LineSeparator.CR , TextUtil.charsToLineSeparator(new char[] {'\r'}));
        assertEquals( TextUtil.LineSeparator.CRLF , TextUtil.charsToLineSeparator(new char[] {'\r','\n'}));
        assertEquals( TextUtil.LineSeparator.LF , TextUtil.charsToLineSeparator(new char[] {'\n'}));
        assertEquals( null , TextUtil.charsToLineSeparator(new char[] {'a'}));
    }
    
    @Test
    public void testReadLine() throws Exception {
//        char[] lf = { '\n' };
//        char[] cr = { '\r' };
//        char[] crlf = { '\r', '\n' };
//        char[] abc = { 'a', 'b', 'c' };
        assertEquals(
            "",
            TextUtil.readLine( new StringReader(""), TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "b",
            TextUtil.readLine( new StringReader("b"), TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "\n",
            TextUtil.readLine( new StringReader("\n"), TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "\n",
            TextUtil.readLine( new StringReader("\nb"), TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "\nb",
            TextUtil.readLine( new StringReader("\nb"), TextUtil.LineSeparator.CRLF )
        );
        assertEquals(
            "\r\n",
            TextUtil.readLine( new StringReader("\r\nb"), TextUtil.LineSeparator.CRLF )
        );
        assertEquals(
            "\r",
            TextUtil.readLine( new StringReader("\r\nb"), TextUtil.LineSeparator.CR )
        );
//        assertEquals(
//            "\r\nbabc",
//            TextUtil.readLine( new StringReader("\r\nbabcd"), abc )
//        );
        
        StringReader sr1 = new StringReader("1\n2\n3");
        assertEquals(
            "1\n",
            TextUtil.readLine( sr1, TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "2\n",
            TextUtil.readLine( sr1, TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "3",
            TextUtil.readLine( sr1, TextUtil.LineSeparator.LF )
        );
        assertEquals(
            "",
            TextUtil.readLine( sr1, TextUtil.LineSeparator.LF )
        );
        
        byte[] content1n2n3 = "1\n2\n3".getBytes("UTF-8");
        ByteArrayInputStream bis = new ByteArrayInputStream(content1n2n3);
        ExceptionInputStream eis = new ExceptionInputStream(bis, 3); // will throw exception on read after 3rd byte  
        InputStreamReader sr2 = new InputStreamReader(eis, "UTF-8"); // note reader may read ahead tripping exception early
        try {
            TextUtil.readLine( sr2, TextUtil.LineSeparator.LF );
            TextUtil.readLine( sr2, TextUtil.LineSeparator.LF );
            fail("expected IOException not thrown");
        } catch ( IOException ioe) {
            // this is normal
        }

    }
    
    @Test
    public void testReaderToLines() throws Exception {
        String lineSep = System.getProperty("line.separator");
        {
            StringReader sr1 = new StringReader("1" + lineSep + "2" + lineSep + "3");
            List<String> lines = TextUtil.readerToLines(sr1);
            assertEquals( 3, lines.size());
            assertEquals("1" + lineSep, lines.get(0));
            assertEquals("2" + lineSep, lines.get(1));
            assertEquals("3", lines.get(2));
        }
        {
            StringReader sr2 = new StringReader("");
            List<String> lines = TextUtil.readerToLines(sr2);
            assertEquals( 0, lines.size());
        }
        {
            StringReader sr3 = new StringReader("a");
            List<String> lines = TextUtil.readerToLines(sr3);
            assertEquals( 1, lines.size());
            assertEquals("a", lines.get(0));
        }
        {
            StringReader sr4 = new StringReader("x" + lineSep );
            List<String> lines = TextUtil.readerToLines(sr4);
            assertEquals( 1, lines.size());
            assertEquals("x" + lineSep , lines.get(0));
        }
    }
    @Test
    public void testReaderToLinesWithLineSep() throws Exception {
//        char[] cr = { '\r' };
//        char[] crlf = { '\r', '\n' };
//        char[] lf = { '\n' };
        {
            StringReader sr1 = new StringReader("1\n2\n3");
            List<String> lines = TextUtil.readerToLines(sr1,TextUtil.LineSeparator.LF);
            assertEquals( 3, lines.size());
            assertEquals("1\n", lines.get(0));
            assertEquals("2\n", lines.get(1));
            assertEquals("3", lines.get(2));
        }
        {
            StringReader sr1 = new StringReader("1\n2\n3");
            List<String> lines = TextUtil.readerToLines(sr1,TextUtil.LineSeparator.CR);
            assertEquals( 1, lines.size());
            assertEquals("1\n2\n3", lines.get(0));
        }
        {
            StringReader sr1 = new StringReader("1\n2\n3");
            List<String> lines = TextUtil.readerToLines(sr1,TextUtil.LineSeparator.CRLF);
            assertEquals( 1, lines.size());
            assertEquals("1\n2\n3", lines.get(0));
        }
        
        {
            StringReader sr2 = new StringReader("");
            List<String> lines = TextUtil.readerToLines(sr2,TextUtil.LineSeparator.LF);
            assertEquals( 0, lines.size());
        }
        {
            StringReader sr3 = new StringReader("a");
            List<String> lines = TextUtil.readerToLines(sr3,TextUtil.LineSeparator.CR);
            assertEquals( 1, lines.size());
            assertEquals("a", lines.get(0));
        }
        {
            StringReader sr4 = new StringReader("x\r\n");
            List<String> lines = TextUtil.readerToLines(sr4,TextUtil.LineSeparator.CRLF);
            assertEquals( 1, lines.size());
            assertEquals("x\r\n" , lines.get(0));
        }
        {
            StringReader sr5 = new StringReader("x\r\n");
            List<String> lines = TextUtil.readerToLines(sr5,TextUtil.LineSeparator.CR);
            assertEquals( 2, lines.size());
            assertEquals("x\r" , lines.get(0));
            assertEquals("\n" , lines.get(1));
        }
        {
            StringReader sr5 = new StringReader("x\r\n");
            List<String> lines = TextUtil.readerToLines(sr5,TextUtil.LineSeparator.LF);
            assertEquals( 1, lines.size());
            assertEquals("x\r\n" , lines.get(0));
        }
        {
            StringReader sr5 = new StringReader("x\r\r\ny\r\r\n");
            List<String> lines = TextUtil.readerToLines(sr5,TextUtil.LineSeparator.CRLF);
            assertEquals( 2, lines.size());
            assertEquals("x\r\r\n" , lines.get(0));
            assertEquals("y\r\r\n" , lines.get(1));
        }
    }
    
    @Test
    public void testDefaultLineSeparator() {
        assertEquals(System.getProperty("line.separator"), new String(TextUtil.DEFAULT_SEP.chars())); 
    }
    
    @Test
    public void testGuessLineSeparator() throws Exception {
        //TextUtil.LineSeparator defLineSep = System.getProperty("line.separator");
        {
            StringReader sr = new StringReader("");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.DEFAULT_SEP, lsep);
        }
        {
            StringReader sr = new StringReader("\r");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.CR, lsep);
        }
        {
            StringReader sr = new StringReader("\r\n");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.CRLF, lsep);
        }
        {
            StringReader sr = new StringReader("\n");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.LF, lsep);
        }
        {
            StringReader sr = new StringReader("a\nb\r\n");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.CRLF, lsep);
        }
        {
            StringReader sr = new StringReader("a\rb\r\n");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.CRLF, lsep);
        }
        {
            StringReader sr = new StringReader("a\rb\r\nc\r");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.CR, lsep);
        }
        {
            StringReader sr = new StringReader("a\nb\r\nc\n");        
            TextUtil.LineSeparator lsep = TextUtil.guessLineSeparator(sr);
            assertEquals(TextUtil.LineSeparator.LF, lsep);
        }
    }
    
    @Test
    public void testRemoveTrailingLineSeparator() {
//        char[] cr = { '\r' };
//        char[] crlf = { '\r', '\n' };
        //char[] lf = { '\n' };
        assertEquals(
            "",
            TextUtil.removeTrailingLineSeparator("", TextUtil.LineSeparator.CR)
        );
        assertEquals(
            "\n",
            TextUtil.removeTrailingLineSeparator("\n", TextUtil.LineSeparator.CR)
        );
        assertEquals(
            "",
            TextUtil.removeTrailingLineSeparator("\r", TextUtil.LineSeparator.CR)
        );
        assertEquals(
            "\n",
            TextUtil.removeTrailingLineSeparator("\n\r", TextUtil.LineSeparator.CR)
        );
        assertEquals(
            "a",
            TextUtil.removeTrailingLineSeparator("a", TextUtil.LineSeparator.CR)
        );
        assertEquals(
            "a",
            TextUtil.removeTrailingLineSeparator("a\r", TextUtil.LineSeparator.CR)
        );
        
        assertEquals(
            "",
            TextUtil.removeTrailingLineSeparator("", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "\n",
            TextUtil.removeTrailingLineSeparator("\n", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "\r",
            TextUtil.removeTrailingLineSeparator("\r", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "",
            TextUtil.removeTrailingLineSeparator("\r\n", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "a",
            TextUtil.removeTrailingLineSeparator("a", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "a",
            TextUtil.removeTrailingLineSeparator("a\r\n", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "a\r",
            TextUtil.removeTrailingLineSeparator("a\r\r\n", TextUtil.LineSeparator.CRLF)
        );
        assertEquals(
            "a\r\nb",
            TextUtil.removeTrailingLineSeparator("a\r\nb", TextUtil.LineSeparator.CRLF)
        );
    }
    
    @Test
    public void testExpandTabs() {
        assertEquals( "\t", TextUtil.expandTabs("\t", -1) );
        assertEquals( "a", TextUtil.expandTabs("a", -1) );
        assertEquals( "\t", TextUtil.expandTabs("\t", 0) );
        assertEquals( "a", TextUtil.expandTabs("a", 0) );
        
        assertEquals( " ", TextUtil.expandTabs("\t", 1) );
        assertEquals( "a", TextUtil.expandTabs("a", 1) );
        assertEquals( " a", TextUtil.expandTabs("\ta", 1) );
        assertEquals( "a ", TextUtil.expandTabs("a\t", 1) );
        assertEquals( "  a", TextUtil.expandTabs("\t\ta", 1) );
        assertEquals( "a  ", TextUtil.expandTabs("a\t\t", 1) );
        assertEquals( "  ", TextUtil.expandTabs("\t", 2) );
        assertEquals( "a", TextUtil.expandTabs("a", 2) );
        assertEquals( "  a", TextUtil.expandTabs("\ta", 2) );
        assertEquals( "a ", TextUtil.expandTabs("a\t", 2) );
        assertEquals( "   a", TextUtil.expandTabs("\ta", 3) );
        assertEquals( "a  ", TextUtil.expandTabs("a\t", 3) );
        assertEquals( "ab ", TextUtil.expandTabs("ab\t", 3) );
        assertEquals( "abc   ", TextUtil.expandTabs("abc\t", 3) );
        assertEquals( "      a", TextUtil.expandTabs("\t\ta", 3) );
        assertEquals( "a     ", TextUtil.expandTabs("a\t\t", 3) );
        assertEquals( "a  b  ", TextUtil.expandTabs("a\tb\t", 3) );
        assertEquals( "a  b     c", TextUtil.expandTabs("a\tb\t\tc", 3) );
        
        
    }
}
