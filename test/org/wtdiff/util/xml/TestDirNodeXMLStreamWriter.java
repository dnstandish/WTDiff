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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.DummyOutputStream;
import org.wtdiff.util.ExceptionOutputStream;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileSystemTestHelper;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.ReadFailMockFileNode;
import org.wtdiff.util.FileNode.ContentMethod;
import org.wtdiff.util.FileNode.FileType;
import org.wtdiff.util.xml.DirNodeXMLStreamWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.junit.After;
import org.junit.Before;

public class TestDirNodeXMLStreamWriter {

    /**
     * Load snapshot from a file validating against DTD.
     * 
     * @param fileName
     * @return Document parsed from filename 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document loadit(String fileName) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);  // NOTE this is a security problem under normal situations
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setXIncludeAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        builder.setErrorHandler(new ErrorHandler () {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                System.err.println("my error " + exception);
                throw exception;
            }
            @Override
            public void error(SAXParseException exception) throws SAXException {
                System.err.println("my error " + exception);
                throw exception;
                
            }
            @Override
            public void fatalError(SAXParseException exception)
                throws SAXException {
                System.err.println("my error " + exception);
                throw exception;
            }
        }
        );
        
        // resolve DTD via class loader.  Anything else, throw an exception
        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                if (systemId != null  
                    && ( systemId.equals("file-tree-snapshot.dtd") 
                        || systemId.endsWith("/file-tree-snapshot.dtd") ) ) {
                    InputStream is = this.getClass().getClassLoader().getResourceAsStream("org/wtdiff/util/xml/file-tree-snapshot.dtd");
                    if ( is == null) 
                        throw new IOException("failed to load tree-snapshot.dtd via ClassLoader");
                    return  new InputSource(is);
                } else {
                    throw new SAXException("unexpected entity: publicId  " + publicId + " systemId " + systemId);
                }
            }
        });
        
        Document doc;
        try (FileInputStream in = new FileInputStream(fileName) ) {
            doc = builder.parse(in);
        }
        DocumentType docType = doc.getDoctype();
        assertTrue(docType != null);
        assertEquals("file-tree-snapshot.dtd",  docType.getSystemId());
        return doc;
    }

    /**
     * Get the single element in document with given tag.  Note that element
     * can be anywhere in the document tree.  If there is not exactly one element
     * a test failure results.
     *  
     * @param doc
     * @param name
     * @return
     */
    private Element getSingleElementByTag(Document doc, String name) {
        NodeList list = doc.getElementsByTagName(name);
        assertEquals(1, list.getLength());
        return (Element) list.item(0);
    }
    
    /**
     * Get immediate child notes of type element.
     * 
     * @param elem
     * @return
     */
    private List<Element> getChildElements(Element elem) {
        
        ArrayList<Element> elements = new ArrayList<>();
        NodeList nodes = elem.getChildNodes();
        for(int  i = 0 ; i < nodes.getLength(); i++ ) {
            Node node = nodes.item(i);
            if ( node.getNodeType() == Node.ELEMENT_NODE ) {
                elements.add((Element)node);
            }
        }
        return elements;
    }
    
    /**
     * Check if one of the elements in list has an attribute with given name 
     * 
     * @param list NodeList containing only Elements
     * @param text name of attribute
     * @return
     */
    private boolean hasElementWIthAttrText(NodeList list, String text) {
        for ( int  i = 0 ; i < list.getLength(); i++ ) {
            Element elem = (Element)(list.item(i));
            if ( text.equals( elem.getAttribute("name") ) )
                return true;
        }
        return false;
    }
    
    /**
     * Convert string representation of digest bytes into a byte array.
     * String representation should have 2 characters per byte.
     * 
     * @param s
     * @return
     */
    private byte[] digestStringToBytes(String s) {
        assertEquals(0, s.length() % 2);
        byte[] bytes = new byte[s.length()/2];
        int iBytes = 0;
        for( int i = 0 ; i < s.length(); i += 2) {
            byte b = (byte)Integer.parseInt(s.substring(i, i+2), 16);
            bytes[iBytes++] = b;
        }
        return bytes;
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
    
    private void checkDigestsAvailable(Document doc, List<String> digests) {
        NodeList list = doc.getElementsByTagName("digest-name");
        assertEquals(digests.size(), list.getLength());
        for (String digest: digests ) {
            assertTrue( hasElementWIthAttrText(list, digest) );
        }
    }
    /**
     * Verify expected "meta" information about snapshot
     * 
     * @param doc Document to check
     * @param min low end of range for capture time
     * @param max high end of range for capture time
     * @param root of root DirNode of original tree
     * @throws ParseException
     * @throws DOMException 
     * @throws UnsupportedEncodingException 
     */
    private void checkMetaData(Document doc, Date min, Date max, String root) throws ParseException, UnsupportedEncodingException, DOMException {
        Element captureTimeElem = getSingleElementByTag(doc, "capture-time");
        String captureTimeString = captureTimeElem.getTextContent();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Long captureTime  = new Long(df1.parse(captureTimeString).getTime());
        assertTrue( captureTime >= min.getTime());
        assertTrue( captureTime <= max.getTime());        
        assertEquals(root, decode(getSingleElementByTag(doc, "capture-root").getTextContent()));
        assertEquals(System.getProperty("user.name"), decode(getSingleElementByTag(doc, "user").getTextContent()));
        assertEquals(System.getProperty("user.home"), decode(getSingleElementByTag(doc, "home").getTextContent()));
        assertEquals(System.getProperty("user.dir"), decode(getSingleElementByTag(doc, "current-dir").getTextContent()));
        assertEquals(System.getProperty("os.name"), decode(getSingleElementByTag(doc, "os").getTextContent()));
        
        checkDigestsAvailable(doc, Arrays.asList("CRC32", "MD5"));
    }
    
    private String decode(String s) throws UnsupportedEncodingException {
        
        return URLDecoder.decode(s, "UTF-8");
            
    }
    
    @Test
    public void testBasic() throws IOException, XMLStreamException, ParserConfigurationException, SAXException, ParseException {
        DirNode dirNode = new DirNode("adir", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        dirNode.setRoot("silly root");
        
        Date before = new Date();
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Date after = new Date();
        
        Document doc = loadit(outFile.getPath());
        checkMetaData(doc, before, after, dirNode.getRoot());
        Element dirElem = getSingleElementByTag(doc, "dir");
        assertEquals("adir", dirElem.getAttribute("name"));
    }
    
    @Test
    public void testSingleTextFile() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String textFileName = "singleTextFile";
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date fileDate  = df1.parse("2015-01-01T01:02:03.456+0500");
        MockFileNode fileNode = new MockFileNode(textFileName, textFileName, fileDate);
        DirNode dirNode = new DirNode(fileNode);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        
        Element fileElem = getSingleElementByTag(doc, "file");
        assertEquals(textFileName, fileElem.getAttribute("name"));
        assertEquals(textFileName.getBytes().length, Long.valueOf(fileElem.getAttribute("size")).longValue());
        String timeString =   fileElem.getAttribute("time");
        Date elemFileTime = df1.parse(timeString);
        assertEquals(fileDate, elemFileTime);
        assertEquals("yes", fileElem.getAttribute("istext"));
        assertEquals("regfile", fileElem.getAttribute("type"));
//        <file name="h1" size="0" time="2014-08-06T23:12:15.000+0000" istext="no" type="regfile">
        NodeList digestList = fileElem.getElementsByTagName("digest");
        assertEquals(2, digestList.getLength());
        int digestCount = 0;
        for( int i = 0; i < digestList.getLength(); i++ ) {
            Element digest = (Element)digestList.item(i);
            if ( "CRC32".equals(digest.getAttribute("name"))) {
                digestCount++;
                assertEquals( fileNode.getCrc(), Long.parseLong(digest.getTextContent().trim(), 16));
            }
            if ( "MD5".equals(digest.getAttribute("name"))) {
                digestCount++;
                byte[] digestBytes = digestStringToBytes( digest.getTextContent().trim() );
                assertTrue( Arrays.equals(fileNode.getMd5(), digestBytes) );
            }
        }
        assertEquals(2, digestCount);
        assertEquals( 0, fileElem.getElementsByTagName("linkto").getLength() );
    }

    @Test
    public void testDigestControl() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String textFileName = "singleTextFile";
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date fileDate  = df1.parse("2015-01-04T01:02:03.456+0500");
        MockFileNode fileNode = new MockFileNode(textFileName, textFileName, fileDate);
        DirNode dirNode = new DirNode(fileNode);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32"));
        Document doc = loadit(outFile.getPath());
        
        checkDigestsAvailable(doc, Arrays.asList("CRC32"));
        Element fileElem = getSingleElementByTag(doc, "file");
        NodeList digestList = fileElem.getElementsByTagName("digest");
        assertEquals(1, digestList.getLength());
        assertEquals( "CRC32", ((Element)digestList.item(0)).getAttribute("name") );

        outFile = writeSnapshot(dirNode, Arrays.asList("MD5"));
        doc = loadit(outFile.getPath());
        
        checkDigestsAvailable(doc, Arrays.asList("MD5"));
        fileElem = getSingleElementByTag(doc, "file");
        digestList = fileElem.getElementsByTagName("digest");
        assertEquals(1, digestList.getLength());
        assertEquals( "MD5", ((Element)digestList.item(0)).getAttribute("name") );
        
        ArrayList<String> emptyList = new ArrayList<>(0); 
        outFile = writeSnapshot(dirNode, emptyList);
        doc = loadit(outFile.getPath());
        
        checkDigestsAvailable(doc, emptyList);
        fileElem = getSingleElementByTag(doc, "file");
        digestList = fileElem.getElementsByTagName("digest");
        assertEquals(0, digestList.getLength());
    }

    @Test
    public void testDigestPerCost() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        MockFileNode fileNodeNoCrc32 = new MockFileNode("nocrc32file", "nocrc32", new Date()) {
            public long getCrc() {
                throw new IllegalArgumentException("CRC32 impossible, should not ask for it");
            }
            public double getContentMethodCost(ContentMethod method) {
                if ( method == CONTENT_METHOD_CRC)
                    return COST_IMPOSSIBLE;
                return super.getContentMethodCost(method);
            }
        };
        DirNode dirNodeNoCrc32 = new DirNode(fileNodeNoCrc32);
        
        File outFile = writeSnapshot(dirNodeNoCrc32, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        
        checkDigestsAvailable(doc, Arrays.asList("CRC32", "MD5"));
        Element fileElem = getSingleElementByTag(doc, "file");
        NodeList digestList = fileElem.getElementsByTagName("digest");
        assertEquals(1, digestList.getLength());
        assertEquals( "MD5", ((Element)digestList.item(0)).getAttribute("name") );
        
        MockFileNode fileNodeNoMd5 = new MockFileNode("noMD5file", "noMD5", new Date()) {
            public byte[] getMd5() {
                throw new IllegalArgumentException("MD5 impossible, should not ask for it");
            }
            public double getContentMethodCost(ContentMethod method) {
                if ( method == CONTENT_METHOD_MD5)
                    return COST_IMPOSSIBLE;
                return super.getContentMethodCost(method);
            }
        };
        DirNode dirNodeNoMd5 = new DirNode(fileNodeNoMd5);
        
        outFile = writeSnapshot(dirNodeNoMd5, Arrays.asList("CRC32", "MD5"));
        doc = loadit(outFile.getPath());
        checkDigestsAvailable(doc, Arrays.asList("CRC32", "MD5"));
        fileElem = getSingleElementByTag(doc, "file");
        digestList = fileElem.getElementsByTagName("digest");
        assertEquals(1, digestList.getLength());
        assertEquals( "CRC32", ((Element)digestList.item(0)).getAttribute("name") );
        
        
    }
    @Test
    public void testSingleBinaryFile() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String binaryFileName = "singleBinaryFile";        
        String binaryContent = "\u0000\u0001\u0002\u0003";
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date fileDate  = df1.parse("2015-01-02T01:02:03.456+0500");
        MockFileNode fileNode = new MockFileNode(binaryFileName, binaryContent, fileDate);
        DirNode dirNode = new DirNode(fileNode);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        
        Element fileElem = getSingleElementByTag(doc, "file");
        assertEquals(binaryFileName, fileElem.getAttribute("name"));
        assertEquals(binaryContent.getBytes().length, Long.valueOf(fileElem.getAttribute("size")).longValue());
        assertEquals("no", fileElem.getAttribute("istext"));
        assertEquals("regfile", fileElem.getAttribute("type"));
        NodeList digestList = fileElem.getElementsByTagName("digest");
        assertEquals(2, digestList.getLength());
        int digestCount = 0;
        for( int i = 0; i < digestList.getLength(); i++ ) {
            Element digest = (Element)digestList.item(i);
            if ( "CRC32".equals(digest.getAttribute("name"))) {
                digestCount++;
                assertEquals( fileNode.getCrc(), Long.parseLong(digest.getTextContent().trim(), 16));
            }
            if ( "MD5".equals(digest.getAttribute("name"))) {
                digestCount++;
                byte[] digestBytes = digestStringToBytes( digest.getTextContent().trim() );
                assertTrue( Arrays.equals(fileNode.getMd5(), digestBytes) );
            }
        }
        assertEquals(2, digestCount);
        assertEquals( 0, fileElem.getElementsByTagName("linkto").getLength() );
    }

    @Test
    public void testSingleSpecialFile() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String specialFileName = "singleSpecialFile";        
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date fileDate  = df1.parse("2015-01-02T01:02:03.456+0500");
        MockFileNode fileNode = new MockFileNode(specialFileName, "", fileDate);
        fileNode.setFileType(FileType.SPECIAL);
        DirNode dirNode = new DirNode(fileNode);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        
        Element fileElem = getSingleElementByTag(doc, "file");
        assertEquals(specialFileName, fileElem.getAttribute("name"));
        assertEquals( 0, Long.valueOf(fileElem.getAttribute("size")).longValue());
        assertEquals("no", fileElem.getAttribute("istext"));
        assertEquals("special", fileElem.getAttribute("type"));
        NodeList digestList = fileElem.getElementsByTagName("digest");
        assertEquals(0, digestList.getLength());
        assertEquals( 0, fileElem.getElementsByTagName("linkto").getLength() );
    }

    @Test
    public void testSingleSymlinklFile() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String symlinkFileName = "singleSpecialFile";        
        String linkTo = "another file";
        String linkToEncoded = "another+file";
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Date fileDate  = df1.parse("2015-01-02T01:02:03.456+0500");
        MockFileNode fileNode = new MockFileNode(symlinkFileName, linkTo , fileDate);
        fileNode.setFileType(FileType.SYMLINK);
        
        DirNode dirNode = new DirNode(fileNode);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        
        Element fileElem = getSingleElementByTag(doc, "file");
        assertEquals(symlinkFileName, fileElem.getAttribute("name"));
        assertEquals( linkTo.getBytes().length, Long.valueOf(fileElem.getAttribute("size")).longValue());
        assertEquals("no", fileElem.getAttribute("istext"));
        assertEquals("symlink", fileElem.getAttribute("type"));
        NodeList digestList = fileElem.getElementsByTagName("digest");
        assertEquals(0, digestList.getLength());
        assertEquals( 1, fileElem.getElementsByTagName("linkto").getLength() );
        Element linkToElement = (Element)fileElem.getElementsByTagName("linkto").item(0);
        assertEquals(linkToEncoded, linkToElement.getAttribute("target"));
        
    }

    @Test
    public void testSingleSubDir() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String subDirName = "subdir";
        String dirName = "subdir";
        DirNode subdirNode = new DirNode(subDirName, new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        DirNode dirNode = new DirNode(subdirNode);
        dirNode.setName(dirName);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        
        Element snapShot = getSingleElementByTag(doc, "snapshot");
        List<Element> topDirs = this.getChildElements(snapShot);
        assertEquals(1, topDirs.size() );
        Element topDir = topDirs.get(0);
        assertEquals(dirName, topDir.getAttribute("name"));
        assertEquals(0, topDir.getElementsByTagName("file").getLength());
        
        assertEquals(1, topDir.getElementsByTagName("dir").getLength());
        Element subDir = (Element) topDir.getElementsByTagName("dir").item(0);
        assertEquals(subDirName, subDir.getAttribute("name"));
    }

    enum FileOrDirs { FILES, DIRS };
    
    public void checkNameEncoding(FileOrDirs which) throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String spaceFileName = "s b";
        String newlineFileName = "s\n";
        String qquoteFileName = "\"s\"";
        String ampFileName = "s&";
        String percentFileName = "1%";
        List<String> fileNames = Arrays.asList(spaceFileName, newlineFileName, qquoteFileName, ampFileName, percentFileName);
        List<Leaf> leaves = new ArrayList<Leaf>();
        List<DirNode> dirs = new ArrayList<DirNode>();
        DirNode dirNode = new DirNode("aDir", leaves, dirs);
        for( String fileName: fileNames ) {
            if ( which == FileOrDirs.FILES ) {
                dirNode.addLeaf( new MockFileNode(fileName) );
            } else {
                DirNode subDir = new DirNode(fileName, new ArrayList<Leaf>(), new ArrayList<DirNode>());
                dirNode.addDir(subDir);
            }
        }
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());

        NodeList fileList = doc.getElementsByTagName(which == FileOrDirs.FILES? "file" : "dir");
        if ( which == FileOrDirs.FILES ) {
            assertEquals( fileNames.size(), fileList.getLength() );
        } else {
            // dirs has one extra, the top dir
            assertEquals( fileNames.size()+1, fileList.getLength() );
        }
        for( String fileName: fileNames ) {
            boolean haveMatch = false;
            for( int j = 0 ; j < fileList.getLength(); j++) {
                Element elem = (Element) fileList.item(j);
                assertFalse(fileName.equals(elem.getAttribute("name")));
                String encodedName = URLEncoder.encode(fileName, "UTF-8");
                if ( encodedName.equals(elem.getAttribute("name"))) {
                    assertFalse(haveMatch);
                    haveMatch = true;
                }
            }
            assertTrue(haveMatch);
        }
        
    }
    
    @Test
    public void testNameEncoding() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        checkNameEncoding( FileOrDirs.FILES );
        checkNameEncoding( FileOrDirs.DIRS );
    }
    
    @Test
    public void testSymlinkEncoding() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        HashMap<String, String> fileToTarget = new HashMap<>();
        fileToTarget.put( "spaceLink", "s b" );
        fileToTarget.put( "newlineLink", "s\n" );
        fileToTarget.put( "qquotelink", "\"s\"" );
        fileToTarget.put( "ampLink", "s&" );
        fileToTarget.put( "percentLink", "1%" );
        List<Leaf> leaves = new ArrayList<Leaf>();
        List<DirNode> dirs = new ArrayList<DirNode>();
        DirNode dirNode = new DirNode("aDir", leaves, dirs);
        for ( Map.Entry<String, String> entry : fileToTarget.entrySet() ) {
            String fileName = entry.getKey();
            String target = entry.getValue();
            MockFileNode node =  new MockFileNode(fileName);
            node.setFileType(FileType.SYMLINK);
            node.setLinkTo(target);
            dirNode.addLeaf( node );
            
        }
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());

        NodeList fileList = doc.getElementsByTagName("file");
        assertEquals( fileToTarget.size(), fileList.getLength() );
        for ( Map.Entry<String, String> entry : fileToTarget.entrySet() ) {
            String fileName = entry.getKey();
            String target = entry.getValue();
            String encodedTarget = URLEncoder.encode(target, "UTF-8");

            boolean haveMatch = false;
            for( int j = 0 ; j < fileList.getLength(); j++) {
                Element elem = (Element) fileList.item(j);
                List<Element> linkToElem = getChildElements(elem);
                assertEquals(1, linkToElem.size());
                String targetAttr = linkToElem.get(0).getAttribute("target");

                if ( fileName.equals( elem.getAttribute("name") ) ) {
                    assertFalse(haveMatch);
                    assertNotEquals( target, targetAttr);
                    assertEquals( encodedTarget, targetAttr);
                    haveMatch = true;
                }
            }
            assertTrue(haveMatch);
        }        
    }
    
    @Test
    public void testFileSize() throws ParseException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        String intMaxFileName = "int_max";
        String longMaxFileName = "long_max";

        MockFileNode intMaxNode = new MockFileNode("int_max") {
            public long getSize() {
                return Integer.MAX_VALUE;
            }
        };
        MockFileNode longMaxNode = new MockFileNode("long_max") {
            public long getSize() {
                return Long.MAX_VALUE;
            }
        };

        DirNode dirNode = new DirNode("aDir", new ArrayList<Leaf>(), new ArrayList<DirNode>(0));
        dirNode.addLeaf(intMaxNode);
        dirNode.addLeaf(longMaxNode);
        
        File outFile = writeSnapshot(dirNode, Arrays.asList("CRC32", "MD5"));
        Document doc = loadit(outFile.getPath());
        NodeList fileList = doc.getElementsByTagName("file");
        assertEquals( 2, fileList.getLength() );
        for( int j = 0 ; j < fileList.getLength(); j++) {
                Element elem = (Element) fileList.item(j);
                long expectedSize = -1;
                if ( intMaxFileName.equals(elem.getAttribute("name")) ) {
                    expectedSize = Integer.MAX_VALUE;
                } else if ( longMaxFileName.equals(elem.getAttribute("name")) ) {
                    expectedSize = Long.MAX_VALUE;
                } else {
                    fail( " unexpected file node name " + elem.getAttribute("name"));
                }
                String sizeString = elem.getAttribute("size");
                long size = Long.parseLong(sizeString);
                assertEquals( expectedSize, size );
        }
        
    }
    
    @Test
    public void testIOErrorWrite() {
        DirNode dirNode = new DirNode("adir", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        dirNode.setRoot("silly root");
        dirNode.addLeaf( new MockFileNode("mock file 1"));
        dirNode.addLeaf( new MockFileNode("mock file 2"));
        dirNode.addLeaf( new MockFileNode("mock file 3"));
        try ( OutputStream os = new ExceptionOutputStream(0)){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32", "MD5"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter swallowed IO or XMLStream Exception");
        } catch (IOException ioe) {
            // one of these is supposed to happen
        } catch (XMLStreamException ioe) {
            // one of these is supposed to happen
        }
        
        try ( OutputStream os = new ExceptionOutputStream(100)){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32", "MD5"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter swallowed IO or XMLStreamException");
        } catch (IOException ioe) {
            // one of these is supposed to happen
        } catch (XMLStreamException ioe) {
            // one of these is supposed to happen
        }        
    }

    @Test
    public void testIOErrorRead() {
        DirNode dirNode = new DirNode("adir", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        dirNode.setRoot("silly root");
        ReadFailMockFileNode failNode = new ReadFailMockFileNode("readfail", 0);
        dirNode.addLeaf( failNode );
        try ( OutputStream os = new DummyOutputStream()){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter swallowed IO or XMLStream Exception");
        } catch (IOException ioe) {
            // one of these is supposed to happen
        } catch (XMLStreamException ioe) {
            // one of these is supposed to happen
        }
        
        try ( OutputStream os = new DummyOutputStream()){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("MD5"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter swallowed IO or XMLStream Exception");
        } catch (IOException ioe) {
            // one of these is supposed to happen
        } catch (XMLStreamException ioe) {
            // one of these is supposed to happen
        }
        
    }

    @Test
    public void testLeafNotFile() throws XMLStreamException, IOException {
        DirNode dirNode = new DirNode("adir", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        dirNode.setRoot("silly root");
        dirNode.addLeaf( new MockFileNode("mock file 1"));
        dirNode.addLeaf( new MockFileNode("mock file 2"));
        Leaf notAFileNode = new Leaf();
        notAFileNode.setName("notAFileNode");
        dirNode.addLeaf( notAFileNode );
        try ( OutputStream os = new DummyOutputStream()){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32", "MD5"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter accepted non file as leaf node");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
        
    }

    @Test
    public void testBadDigestList() throws XMLStreamException, IOException {
        DirNode dirNode = new DirNode("adir", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        dirNode.setRoot("silly root");
        dirNode.addLeaf( new MockFileNode("mock file 1"));
        dirNode.addLeaf( new MockFileNode("mock file 2"));
        try ( OutputStream os = new DummyOutputStream()){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32", "MD5", "VERYSECURE"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter accepted unknown digest");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }

        try ( OutputStream os = new DummyOutputStream()){
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32", "CRC32"));
            writer.writeDirNodeSnapShot(dirNode);
            fail("DirNodeXMLStreamWriter accepted duplciate digest");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
        
    }

    @Test
    public void testMetadataEncoding() throws DOMException, IOException, XMLStreamException, ParserConfigurationException, SAXException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File outDir = helper.createTestDir("outDirMeta");
        File outFile = helper.createTestFile("testout.xml", "", outDir);

        String testRoot = "silly/root";
        DirNode dirNode = new DirNode("adir", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
        dirNode.setRoot(testRoot);

        String testUserName = "a%joke";
        String testUserHome = "/user/home%";
        String testUserDir = "/user/dir%";
        String testOsName = "Os\nName";
        
        String testComment = "a \ntest \u0008 comment+a";
        
        String userName = System.getProperty("user.name");
        String userHome = System.getProperty("user.home");
        String userDir = System.getProperty("user.dir");
        String osName = System.getProperty("os.name");
        
        try ( FileOutputStream os = new FileOutputStream(outFile)){
            System.setProperty("user.name", testUserName);
            System.setProperty("user.home", testUserHome);
            System.setProperty("user.dir",testUserDir);
            System.setProperty("os.name", testOsName);
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(os, Arrays.asList("CRC32", "MD5"));
            writer.writeDirNodeSnapShot(dirNode, testComment);
        } finally {
            System.setProperty("user.name", userName);
            System.setProperty("user.home", userHome);
            System.setProperty("user.dir",userDir);
            System.setProperty("os.name", osName);
        }

        
        Document doc = loadit(outFile.getPath());
        assertNotEquals( testRoot, getSingleElementByTag(doc, "capture-root").getTextContent());
        assertNotEquals( testUserName,getSingleElementByTag(doc, "user").getTextContent());
        assertNotEquals( testUserHome, getSingleElementByTag(doc, "home").getTextContent());
        assertNotEquals( testUserDir, getSingleElementByTag(doc, "current-dir").getTextContent());
        assertNotEquals( testOsName, getSingleElementByTag(doc, "os").getTextContent());
        assertNotEquals( testComment, getSingleElementByTag(doc, "user-comment").getTextContent());

        assertEquals( testRoot, decode(getSingleElementByTag(doc, "capture-root").getTextContent()));
        assertEquals( testUserName,decode(getSingleElementByTag(doc, "user").getTextContent()));
        assertEquals( testUserHome, decode(getSingleElementByTag(doc, "home").getTextContent()));
        assertEquals( testUserDir, decode(getSingleElementByTag(doc, "current-dir").getTextContent()));
        assertEquals( testOsName, decode(getSingleElementByTag(doc, "os").getTextContent()));
        assertEquals( testComment, decode(getSingleElementByTag(doc, "user-comment").getTextContent()));
        
    }
}
