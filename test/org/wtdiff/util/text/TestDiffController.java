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
import org.wtdiff.util.text.DiffChangeListener;
import org.wtdiff.util.text.DiffController;
import org.wtdiff.util.text.FileInputStreamSource;
import org.wtdiff.util.text.DiffController.SourceType;
import org.wtdiff.util.text.TextUtil.LineSeparator;
import org.junit.After;

public class TestDiffController   {

    private Charset defaultCharset = Charset.defaultCharset();
    private Charset utf8 = Charset.forName("UTF-8");
    private Charset utf16 = Charset.forName("UTF-16");
    private Charset utf16le = Charset.forName("UTF-16LE");
    private Charset iso88591 = Charset.forName("ISO-8859-1");
    private Charset usAscii = Charset.forName("US-ASCII");

    private String fileNameCr = "testCr"; 
    private String contentCr = "content-Cr\r1\r2\r3\r";
    private File fileCr;
    private String fileNameCrLf = "testCrLf"; 
    private String contentCrLf = "content-CrLf\r\n1\r\n2\r\n3\r\n";
    private File fileCrLf;
    private FileSystemTestHelper helper;

    private class SimpleListener implements DiffChangeListener {

        private boolean notified = false;
        
        @Override
        public void diffChanged() {
            notified = true;
            
        }
        public void reset() {
            notified = false;
        }
        
        public boolean isNotified() {
            return notified;
        }
    }
    
    @Before
    public void setUp() throws Exception {
        helper = new FileSystemTestHelper();
        fileCr = helper.createTestFile(fileNameCr, contentCr);
        fileCrLf = helper.createTestFile(fileNameCrLf, contentCrLf);
    }

    private void exerciseSetSource(boolean useListener) throws IOException {

        FileInputStreamSource fileSourceCr = new FileInputStreamSource( fileCr );
        FileInputStreamSource fileSourceCrLf = new FileInputStreamSource( fileCrLf );

        SimpleListener listener = new SimpleListener();
        assertFalse( listener.notified );

        DiffController controller = new DiffController();        

        if ( useListener )
            controller.addDiffChangeListener(listener);
        assertFalse( listener.notified );        
        assertNull( controller.getOldSourceName() );
        assertNull( controller.getNewSourceName() );
        assertFalse( listener.notified );

        
        controller.setOldSource(fileSourceCr);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( fileCr.getPath(), controller.getOldSourceName() );
        assertNull( controller.getNewSourceName() );
        assertFalse( listener.notified );
        
        controller.setNewSource(fileSourceCrLf);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( fileCr.getPath(), controller.getOldSourceName() );
        assertEquals( fileCrLf.getPath(), controller.getNewSourceName() );
        assertFalse( listener.notified );

        controller.setOldSource( null );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertNull( controller.getOldSourceName() );
        assertEquals( fileCrLf.getPath(), controller.getNewSourceName() );

        controller.setNewSource(null);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertNull( controller.getOldSourceName() );
        assertNull( controller.getNewSourceName() );
        
    }
    
    @Test
    public void testSource() throws IOException {
        exerciseSetSource(false);
        exerciseSetSource(true);
    }
    
    private void exerciseLineSep(boolean useListener) throws IOException {
        FileInputStreamSource fileSourceCr = new FileInputStreamSource( fileCr );
        FileInputStreamSource fileSourceCrLf = new FileInputStreamSource( fileCrLf );

        SimpleListener listener = new SimpleListener();
        assertFalse( listener.notified );

        DiffController controller = new DiffController();        

        if ( useListener )
            controller.addDiffChangeListener(listener);
        
        assertNull( controller.getLineSep(SourceType.OLD) );
        assertNull( controller.getLineSep(SourceType.NEW) );
        assertFalse( listener.notified );

        controller.forceLineSepSourceType( SourceType.OLD, LineSeparator.CR );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        controller.forceLineSepSourceType( SourceType.NEW, LineSeparator.CR );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        controller.setOldSource(fileSourceCr);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals(LineSeparator.CR, controller.getLineSep(SourceType.OLD));
        assertNull( controller.getLineSep(SourceType.NEW) );
        controller.forceLineSepSourceType( SourceType.OLD, LineSeparator.LF );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        controller.setNewSource(fileSourceCrLf);
        assertEquals(LineSeparator.LF, controller.getLineSep(SourceType.OLD));
        assertEquals(LineSeparator.CRLF, controller.getLineSep(SourceType.NEW));
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        controller.forceLineSepSourceType( SourceType.NEW, LineSeparator.LF );
        assertEquals(LineSeparator.LF, controller.getLineSep(SourceType.OLD));
        assertEquals(LineSeparator.LF, controller.getLineSep(SourceType.NEW));
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        controller.forceLineSepSourceType( SourceType.OLD, null );
        assertEquals(LineSeparator.CR, controller.getLineSep(SourceType.OLD));
        assertEquals(LineSeparator.LF, controller.getLineSep(SourceType.NEW));
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }

        controller.forceLineSepSourceType( SourceType.NEW, null );
        assertEquals(LineSeparator.CR, controller.getLineSep(SourceType.OLD));
        assertEquals(LineSeparator.CRLF, controller.getLineSep(SourceType.NEW));
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }

        controller.setOldSource(fileSourceCrLf);
        controller.setNewSource(fileSourceCrLf);
        listener.reset();
        controller.diff();
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertTrue( controller.haveDiff() );
        assertEquals( 0, controller.getDiff().getDeltas().size() );  // the first line
        
        controller.forceLineSepSourceType( SourceType.NEW, LineSeparator.CR );
        assertFalse( controller.haveDiff() );
        controller.diff();
        assertEquals(1, controller.getDiff().getDeltas().size() );
        
    }

    @Test
    public void testLineSep() throws IOException {
        exerciseLineSep(false);
        exerciseLineSep(true);
    }

    private void exerciseEncoding(boolean useListener) throws IOException {
        FileInputStreamSource fileSourceCr = new FileInputStreamSource( fileCr );
        FileInputStreamSource fileSourceCrLf = new FileInputStreamSource( fileCrLf );

        SimpleListener listener = new SimpleListener();
        assertFalse( listener.notified );

        DiffController controller = new DiffController();        

        if ( useListener )
            controller.addDiffChangeListener(listener);

        assertNull( controller.getEncoding(SourceType.OLD) );
        assertNull( controller.getEncoding(SourceType.NEW) );
        assertFalse( listener.notified );

        controller.forceEncoding(SourceType.OLD, usAscii );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertNull( controller.getEncoding(SourceType.OLD) );
        assertNull( controller.getEncoding(SourceType.NEW) );
        
        controller.forceEncoding(SourceType.NEW, usAscii );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertNull( controller.getEncoding(SourceType.OLD) );
        assertNull( controller.getEncoding(SourceType.NEW) );
        
        controller.setOldSource(fileSourceCr);
        assertEquals( defaultCharset, controller.getEncoding(SourceType.OLD) );
        assertNull( controller.getEncoding(SourceType.NEW) );
        controller.forceEncoding(SourceType.NEW, usAscii );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( defaultCharset, controller.getEncoding(SourceType.OLD) );
        assertNull( controller.getEncoding(SourceType.NEW) );

        
        controller.setNewSource(fileSourceCrLf);
        assertEquals( defaultCharset, controller.getEncoding(SourceType.OLD) );
        assertEquals( defaultCharset, controller.getEncoding(SourceType.NEW) );
        listener.reset();
        controller.forceEncoding(SourceType.OLD, usAscii );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( usAscii, controller.getEncoding(SourceType.OLD) );
        assertEquals( defaultCharset, controller.getEncoding(SourceType.NEW) );

        controller.forceEncoding(SourceType.NEW, usAscii );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( usAscii, controller.getEncoding(SourceType.OLD) );
        assertEquals( usAscii, controller.getEncoding(SourceType.NEW) );
        
        controller.forceEncoding(SourceType.NEW, null );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( usAscii, controller.getEncoding(SourceType.OLD) );
        assertEquals( defaultCharset, controller.getEncoding(SourceType.NEW) );
        
        controller.forceEncoding(SourceType.OLD, null );
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertEquals( defaultCharset, controller.getEncoding(SourceType.OLD) );
        assertEquals( defaultCharset, controller.getEncoding(SourceType.NEW) );

        //TODO test content interpretation.  need to further implement diff

    }
    
    @Test
    public void testEncoding() throws IOException {
        exerciseEncoding(false);
        exerciseEncoding(true);
    }

    private void exerciseWhiteSpaceNoSource(boolean useListener) throws IOException {
        SimpleListener listener = new SimpleListener();
        assertFalse( listener.notified );

        DiffController controller = new DiffController();        

        if ( useListener )
            controller.addDiffChangeListener(listener);

        assertFalse( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        controller.setCompactWhiteSpace(true);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertTrue( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        controller.setCompactWhiteSpace(false);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertFalse( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        
        controller.setIgnoreWhiteSpace(true);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertFalse( controller.isCompactWhiteSpace() );
        assertTrue( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        
        controller.setIgnoreWhiteSpace(false);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertFalse( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        
        controller.setTrimWhiteSpace(true);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertFalse( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertTrue( controller.isTrimWhiteSpace() );
        
        controller.setTrimWhiteSpace(false);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertFalse( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        
        controller.setCompactWhiteSpace(true);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertTrue( controller.isCompactWhiteSpace() );
        assertFalse( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );
        
        controller.setIgnoreWhiteSpace(true);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertTrue( controller.isCompactWhiteSpace() );
        assertTrue( controller.isIgnoreWhiteSpace() );
        assertFalse( controller.isTrimWhiteSpace() );

        controller.setTrimWhiteSpace(true);
        if ( useListener ) {
            assertTrue(listener.notified);
            listener.reset();
        }
        assertTrue( controller.isCompactWhiteSpace() );
        assertTrue( controller.isIgnoreWhiteSpace() );
        assertTrue( controller.isTrimWhiteSpace() );

        controller.setCompactWhiteSpace(true);
        assertFalse(listener.notified);
        controller.setIgnoreWhiteSpace(true);
        assertFalse(listener.notified);
        controller.setTrimWhiteSpace(true);
        assertFalse(listener.notified);
    }
    
    @Test
    public void testWhiteSpaceNoSource() throws IOException {
        exerciseWhiteSpaceNoSource(false);
        exerciseWhiteSpaceNoSource(true);
    }

    @Test
    public void testWhiteSpace() throws IOException {
        File fileAB = helper.createTestFile("ab", "AB");
        File fileASB = helper.createTestFile("asb", "A B");
        File fileASSB = helper.createTestFile("assb", "A  B");
        File fileSASBS = helper.createTestFile("sasbs", " A B ");
        FileInputStreamSource fileSourceAB = new FileInputStreamSource( fileAB );
        FileInputStreamSource fileSourceASB = new FileInputStreamSource( fileASB );
        FileInputStreamSource fileSourceASSB = new FileInputStreamSource( fileASSB );
        FileInputStreamSource fileSourceSASBS = new FileInputStreamSource( fileSASBS );

        DiffController controller = new DiffController();        
        controller.setOldSource(fileSourceAB);
        controller.setNewSource(fileSourceASB);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.diff();
        assertTrue(controller.haveDiff());
        assertTrue( controller.getDiff().getDeltas().size() > 0 );
        controller.setCompactWhiteSpace(true);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.diff();
        assertTrue(controller.haveDiff());
        assertTrue( controller.getDiff().getDeltas().size() > 0 );
        controller.setTrimWhiteSpace(true);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.diff();
        assertTrue(controller.haveDiff());        
        assertTrue( controller.getDiff().getDeltas().size() > 0 );
        controller.setIgnoreWhiteSpace(true);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.diff();
        assertTrue(controller.haveDiff());        
        assertTrue( controller.getDiff().getDeltas().size() == 0 );
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.diff();
        assertTrue(controller.haveDiff());        
        assertTrue( controller.getDiff().getDeltas().size() == 0 );
        
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);
        controller.setIgnoreWhiteSpace(false);
        controller.setOldSource(fileSourceASB);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.setNewSource(fileSourceASSB);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() > 0 );
        controller.setCompactWhiteSpace(true);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() == 0 );
        
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);
        controller.setIgnoreWhiteSpace(false);
        controller.setNewSource(fileSourceSASBS);
        assertFalse(controller.haveDiff());
        assertNull(controller.getDiff());
        controller.setOldSource(fileSourceASB);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() > 0 );
        controller.setTrimWhiteSpace(true);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() == 0 );
        
        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(false);
        controller.setIgnoreWhiteSpace(true);
        controller.setOldSource(fileSourceAB);
        controller.setNewSource(fileSourceASB);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() == 0 );

        controller.setCompactWhiteSpace(true);
        controller.setTrimWhiteSpace(false);
        controller.setIgnoreWhiteSpace(false);
        controller.setOldSource(fileSourceASB);
        controller.setNewSource(fileSourceASSB);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() == 0 );

        controller.setCompactWhiteSpace(false);
        controller.setTrimWhiteSpace(true);
        controller.setIgnoreWhiteSpace(false);
        controller.setOldSource(fileSourceASB);
        controller.setNewSource(fileSourceSASBS);
        controller.diff();
        assertTrue( controller.getDiff().getDeltas().size() == 0 );

        
        
    }
    
    @Test
    public void testReadError() throws IOException {
        Date now = new Date();
        DiffController controller = new DiffController();        
        try {
            ExceptionInputStreamSource source = new ExceptionInputStreamSource("readErrror", now.getTime(),"readErrorContent", 0);
            controller.setOldSource(source);
            controller.getLineSep(SourceType.OLD);
            fail("IOexception not thrown");
        } catch (IOException ioe) {
            // this is supposed to happen
        }
        {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < 10000; i++) {
                sb.append("\r\n");
            }
            ExceptionInputStreamSource sourceOld = new ExceptionInputStreamSource("readErrror1000OOld", now.getTime(), sb.toString(), 9000);
            ExceptionInputStreamSource sourceNew = new ExceptionInputStreamSource("readErrror1000ONew", now.getTime(), sb.toString(), 9000);
            controller.setOldSource(sourceOld);
            controller.setNewSource(sourceNew);
            controller.getLineSep(SourceType.OLD);
            controller.getLineSep(SourceType.NEW);
            try {            
                controller.diff();                
                fail("IOexception not thrown");
            } catch (IOException ioe) {
                // this is supposed to happen
            }
        }

    }
    @Test
    public void testDiffBasic() throws IOException {
        File fileEmpty = helper.createTestFile("Empty", "");
        SimpleListener listener = new SimpleListener();
        assertFalse( listener.notified );
        {
            FileInputStreamSource empty = new FileInputStreamSource( fileEmpty );

            DiffController controller = new DiffController();
            controller.addDiffChangeListener(listener);
            assertFalse( controller.haveDiff() );
            listener.reset();
            
            controller.diff();
            assertFalse( controller.haveDiff() );
            assertFalse( listener.notified );
            controller.setOldSource(empty);
            listener.reset();
            
            controller.diff();
            assertFalse( controller.haveDiff() );
            assertFalse( listener.notified );
            
            assertNull( controller.getDiff() );
        }
        
        {
            FileInputStreamSource empty = new FileInputStreamSource( fileEmpty );

            DiffController controller = new DiffController();
            controller.addDiffChangeListener(listener);
            assertFalse( controller.haveDiff() );
            controller.setNewSource(empty);
            listener.reset();
            controller.diff();
            assertFalse( controller.haveDiff() );
            assertFalse( listener.notified );
            
            assertNull( controller.getDiff() );
        }
        
        {
            FileInputStreamSource empty1 = new FileInputStreamSource( fileEmpty );
            FileInputStreamSource empty2 = new FileInputStreamSource( fileEmpty );

            DiffController controller = new DiffController();
            controller.addDiffChangeListener(listener);
            controller.setOldSource(empty1);
            controller.setNewSource(empty2);
            listener.reset();
            controller.diff();
            assertTrue( controller.haveDiff() );
            assertTrue( listener.notified );
            
            assertNotNull( controller.getDiff() );
        }
        
    }    
    @Test
    public void testGetData() throws IOException {
        File file1Line = helper.createTestFile("OneLine", "1\n");
        File file2Lines = helper.createTestFile("TwoLines", "2.1\n2.2\n");
        DiffController controller = new DiffController();
        controller.setOldSource( new FileInputStreamSource( file1Line ) );
        controller.setNewSource( new FileInputStreamSource( file2Lines ) );
        
        List<String> lines1 = controller.getOldData().getLines();
        assertEquals(1, lines1.size());
        assertEquals("1", lines1.get(0));
        
        List<String> lines2 = controller.getNewData().getLines();
        assertEquals(2, lines2.size());
        assertEquals("2.1", lines2.get(0));
        assertEquals("2.2", lines2.get(1));
    }
}
