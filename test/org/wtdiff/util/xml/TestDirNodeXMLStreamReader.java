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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ExceptionInputStream;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.xml.DirNodeXMLStreamReader;
import org.wtdiff.util.xml.DirNodeXMLStreamWriter;
import org.junit.After;
import org.junit.Before;

public class TestDirNodeXMLStreamReader {

    @Test
    public void testIsSnapshot() throws IOException {
        String data = "<file-tree-snapshot><digests-available/><snapshot><dir name=\"n\"></dir></snapshot></file-tree-snapshot>";
        String badData = "<file-treez-snapshot><digests-available/><snapshot><dir name=\"n\"></dir></snapshot></file-treez-snapshot>";
        String dtd = "<!DOCTYPE file-tree-snapshot SYSTEM \"file-tree-snapshot.dtd\">\n";
        String xmlHeader = "<?xml version=\"1.0\"?>";
        String xmlHeaderUtf16 = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
        
        ByteArrayInputStream bis;

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        bis = new ByteArrayInputStream((xmlHeader).getBytes("UTF-8"));
        assertFalse(reader.isSnapshot(bis));
        bis.close();

        bis = new ByteArrayInputStream(data.getBytes("UTF-8"));
        assertTrue(reader.isSnapshot(bis));
        bis.close();


        bis = new ByteArrayInputStream((xmlHeader + badData).getBytes("UTF-8"));
        assertFalse(reader.isSnapshot(bis));
        bis.close();
        
        bis = new ByteArrayInputStream((xmlHeader + data).getBytes("UTF-8"));
        assertTrue(reader.isSnapshot(bis));
        bis.close();
        
        bis = new ByteArrayInputStream((xmlHeaderUtf16 + data).getBytes("UTF-16"));
        assertTrue(reader.isSnapshot(bis));
        bis.close();
        
        bis = new ByteArrayInputStream(new byte[] {});
        assertFalse(reader.isSnapshot(bis));
        bis.close();
        
        bis = new ByteArrayInputStream(new byte[] {0, 1, 2, 3, 4});
        assertFalse(reader.isSnapshot(bis));
        bis.close();
        
        bis = new ByteArrayInputStream(("some random text").getBytes("UTF-8"));
        assertFalse(reader.isSnapshot(bis));
        bis.close();
        
        bis = new ByteArrayInputStream((xmlHeader  + "junk" + data).getBytes("UTF-8"));
        assertFalse(reader.isSnapshot(bis));
        bis.close();

        bis = new ByteArrayInputStream((xmlHeader  + dtd).getBytes("UTF-8"));
        assertFalse(reader.isSnapshot(bis));
        bis.close();

        bis = new ByteArrayInputStream((xmlHeader + dtd + data).getBytes("UTF-8"));
        assertTrue(reader.isSnapshot(bis));
        bis.close();
    }
    
    @Test
    public void testIsSnapshotIOException() throws IOException {
        String data = "<file-tree-snapshot><digests-available/><snapshot><dir name=\"n\"></dir><snapshot></file-tree-snapshot>";
        String xmlHeader = "<?xml version=\"1.0\"?>";
        
        ByteArrayInputStream bis;

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();
        
        bis = new ByteArrayInputStream((xmlHeader + data).getBytes("UTF-8"));
        try {
            reader.isSnapshot( new ExceptionInputStream(bis, 0));
            fail("isSnapshot swallowed IOException");
        } catch (IOException ioe) {
            // this should happen
        }
        bis.close();
        
        bis = new ByteArrayInputStream((xmlHeader + data).getBytes("UTF-8"));
        try {
            reader.isSnapshot( new ExceptionInputStream(bis, 40));
            fail("isSnapshot swallowed IOException");
        } catch (IOException ioe) {
            // this should happen
        }
        bis.close();
    }    

    private File writeSnapshot(DirNode dir, List<String> digests) throws IOException, XMLStreamException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File outDir = helper.createTestDir("outDir");
        File outFile = helper.createTestFile("testout.xml", "", outDir);
        try ( FileOutputStream os = new FileOutputStream(outFile)){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, digests);
            writer.writeDirNodeSnapShot(dir);
        }
        return outFile;
    }

    @Test
    public void testReadSnapshotBasic() throws IOException, XMLStreamException {
        DirNode dirNode = new DirNode("fred", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        try ( FileInputStream is = new FileInputStream(outFile) ) {
            DirNode topDir = reader.readSnapshot(is);            
            assertEquals("fred", topDir.getName());
            assertEquals(0, topDir.getLeaves().size());
            assertEquals(0, topDir.getDirs().size());
        }
    }

    @Test
    public void testReadSnapshotBadEntity() throws IOException, XMLStreamException {
        String xmlHeader = "<?xml version=\"1.0\"?>";
        String dataEntityInText = "<file-tree-snapshot><user>&myname;</user><snapshot><dir name=\"n\"></dir></snapshot></file-tree-snapshot>";
        String dataEntityInAttr = "<file-tree-snapshot><snapshot><dir name=\"&fname;\"></dir></snapshot></file-tree-snapshot>";
        

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        try (ByteArrayInputStream bis = new ByteArrayInputStream((xmlHeader + dataEntityInText).getBytes("UTF-8"))) {
            reader.readSnapshot( bis );
            fail("bad enity should throw exception");
        } catch (XMLStreamException xse) {
            //this should happen
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream((xmlHeader + dataEntityInAttr).getBytes("UTF-8"))) {
            reader.readSnapshot( bis );
            fail("bad enity should throw exception");
        } catch (XMLStreamException xse) {
            //this should happen
        }
    
    }

    @Test
    public void testReadSnapshotMetaInfo() throws IOException, XMLStreamException {
        String xmlPre = "<?xml version=\"1.0\"?>\n" + 
            "<file-tree-snapshot>\n";
        String xmlPost = "<snapshot><dir name=\"n\"></dir></snapshot></file-tree-snapshot>";
        
        String timeElement = "<capture-time>2015-04-28T02:50:45.693+0000</capture-time>";
        String rootElement = "<capture-root>%2Fhome%2Ffredt%2Ftmp%2Ftest</capture-root>";
        String userElement = "<user>funny+guy</user>";
        String homeElement = "<home>%2Fhome%2Ffunny%20guy</home>";
        String cwdElement = "<current-dir>%2Fsome%2Fwhere%2Fover%2Fthe%2Frainbow</current-dir>";
        String osElement = "<os>s+t+r+a+n+g+e</os>";
        String hostElement = "<host>3rd+rock</host>";
        String commentElement = "<user-comment>gk%0Adgfas</user-comment>";

        String xmlMeta = timeElement + rootElement + userElement + homeElement + cwdElement + osElement + hostElement + commentElement;
        
        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        try (ByteArrayInputStream bis = new ByteArrayInputStream((xmlPre + xmlMeta + xmlPost).getBytes("UTF-8"))) {
            reader.readSnapshot( bis );            
        } catch (XMLStreamException xse) {
            //this should happen
        }

        Map<String,String> metaValues = reader.getSnapshotInfo();
        
        assertEquals( "2015-04-28T02:50:45.693+0000",  metaValues.get( "capture-time" ) );
        assertEquals( "/home/fredt/tmp/test",  metaValues.get( "capture-root" ) );
        assertEquals( "funny guy",  metaValues.get( "user" ) );
        assertEquals( "/home/funny guy",  metaValues.get( "home" ) );
        assertEquals( "/some/where/over/the/rainbow",  metaValues.get( "current-dir" ) );
        assertEquals( "s t r a n g e",  metaValues.get( "os" ) );
        assertEquals( "3rd rock",  metaValues.get( "host" ) );
        assertEquals( "gk\ndgfas",  metaValues.get( "user-comment" ) );
    
    }

    @Test
    public void testReadSnapshotUTF16() throws IOException, XMLStreamException {
        String data = "<file-tree-snapshot><digests-available/><snapshot><dir name=\"n\"></dir></snapshot></file-tree-snapshot>";
        String xmlHeaderUtf16 = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>";
        

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        ByteArrayInputStream bis;
        
        bis = new ByteArrayInputStream((xmlHeaderUtf16 + data).getBytes("UTF-16"));
        DirNode top = reader.readSnapshot( bis );
        assertEquals("n", top.getName());
        bis.close();

    }

    @Test
    public void testReadSnapshotIOException() throws IOException, XMLStreamException {
        String data = "<file-tree-snapshot><digests-available/><snapshot><dir name=\"n\"></dir><snapshot></file-tree-snapshot>";
        String xmlHeader = "<?xml version=\"1.0\"?>";
        

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        ByteArrayInputStream bis;
        
        bis = new ByteArrayInputStream((xmlHeader + data).getBytes("UTF-8"));
        try {
            reader.readSnapshot( new ExceptionInputStream(bis, 0));
            fail("readSnapshot swallowed IOException");
        } catch (IOException ioe) {
            // expected
        }
        bis.close();

    }

    @Test
    public void testReadSnapshotBadXml() throws IOException, XMLStreamException {
        // note z in </file-treez-snapshot> 
        String badData = "<file-tree-snapshot><digests-available/><snapshot><dir name=\"n\"></dir></snapshot></file-treez-snapshot>";
        String xmlHeader = "<?xml version=\"1.0\"?>";
        

        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        ByteArrayInputStream bis;
        
        bis = new ByteArrayInputStream((xmlHeader + badData).getBytes("UTF-8"));
        try {
            reader.readSnapshot( bis );
            fail("readSnapshot expected XMLStreamException");
        } catch (XMLStreamException xse) {
            // expected
        }
        bis.close();

    }

    @Test 
    public void testIsSnapshotBinary() throws IOException {
        // these bytes will cause an a malformed byte sequence when interpretted as UTF-8 
        char[] chars = {
            0xed, 0xac, 0x05, 0x00, 0x72, 0x73, 0x32, 0x00, 0x65, 0x6e, 0x2e, 0x74, 0x6f, 0x73, 0x72, 0x75,
        };
      
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte)chars[i];
           // System.out.println(bytes[i]);
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();

        assertFalse(reader.isSnapshot( bis ));
        bis.close();
        
    }
}
