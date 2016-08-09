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

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.wtdiff.util.DirNode;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.FileNode.FileType;


public class DirNodeXMLStreamWriter implements DirNodeXMLStreamConstants {

//    final public static String DIGEST_CRC32 = "CRC32";
//    final public static String DIGEST_MD5 = "MD5";
    // digests
    private List<String> digests;
//    private String userComment;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    private OutputStream outStream;
    private XMLStreamWriter writer;
/*
    <user>davidst</user>
    <home>/home/davidst/tmp></home>
    <current-dir>/home/davidst/prog/java/adhoc</current-dir>
    <os>Linux</os>
    <host>weeble</host>
*/
    
    public DirNodeXMLStreamWriter(OutputStream out, List<String> digestTypes) throws XMLStreamException {
        outStream = out;
        digests = digestTypes;
        checkDigests(digests);
        dateFormat = new SimpleDateFormat(FILE_TIME_FORMAT_STRING);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // is this really useful?
        XMLOutputFactory fact = XMLOutputFactory.newInstance();
        writer = fact.createXMLStreamWriter(out);
    }

//    public DirNodeXMLStreamWriter(OutputStream out, List<String> digestTypes, String comment) throws XMLStreamException {
//        outStream = out;
//        userComment = comment;
//        digests = digestTypes;
//        checkDigests(digests);
//        dateFormat = new SimpleDateFormat(FILE_TIME_FORMAT_STRING);
//        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); // is this really useful?
//        XMLOutputFactory fact = XMLOutputFactory.newInstance();
//        writer = fact.createXMLStreamWriter(out);
//    }

    private void checkDigests(List<String> list) {
        HashSet<String> digestSet = new HashSet<>();
        for ( String digest: digests ) {
            if ( digestSet.contains(digest) ) {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamWriter.digest_duplciate"),
                        digest
                    )
                );
            } 
            digestSet.add(digest);
            if ( !DIGEST_CRC32.equals( digest )
               && !DIGEST_MD5.equals( digest ) ) {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamWriter.digest_unknown"),
                        digest
                    )
                );
            }
        }
        
    }
    private void nl()  throws XMLStreamException {
        writer.writeCharacters("\n");
    }
    
    public synchronized void writeDirNodeSnapShot(DirNode dir) throws XMLStreamException, IOException {
        writeDirNodeSnapShot(dir, null);
    }
    
    public synchronized void writeDirNodeSnapShot(DirNode dir, String userComment) throws XMLStreamException, IOException {
        writeDirNodeSnapShot( dir, userComment, dir.getRoot() );
    }
    
    public synchronized void writeDirNodeSnapShot(DirNode dir, String userComment, String root) throws XMLStreamException, IOException {
        /*
        FileOutputStream delmeOut = new FileOutputStream("delme.xml");
        MD5OutputStream md5Stream = new MD5OutputStream(delmeOut);
        XMLStreamWriter writer = fact.createXMLStreamWriter(md5Stream);
        */
        writer.writeStartDocument("1.0");
        nl();
        writer.writeDTD("<!DOCTYPE file-tree-snapshot SYSTEM \"file-tree-snapshot.dtd\"> ");
        nl();
        writer.writeStartElement(ELEMENT_FILE_TREE_SNAPSHOT);
        nl();
        writer.writeStartElement(ELEMENT_CAPTURE_TIME);
        writer.writeCharacters(dateFormat.format(new Date()));
        writer.writeEndElement(); //capture-time
        nl();
        writer.writeStartElement(ELEMENT_CAPTURE_ROOT);
        writer.writeCharacters(encodeSpecial(root));  // TODO use portable path represention?
        writer.writeEndElement(); //capture-root        
        nl();
        writer.writeStartElement(ELEMENT_USER);
        writer.writeCharacters(encodeSpecial(System.getProperty("user.name")));
        writer.writeEndElement();
        nl();
        writer.writeStartElement(ELEMENT_HOME);
        writer.writeCharacters(encodeSpecial(System.getProperty("user.home")));
        writer.writeEndElement();
        nl();
        writer.writeStartElement(ELEMENT_CURRENT_DIR);
        writer.writeCharacters(encodeSpecial(System.getProperty("user.dir")));
        writer.writeEndElement();
        nl();
        writer.writeStartElement(ELEMENT_OS);
        writer.writeCharacters(encodeSpecial(System.getProperty("os.name")));
        writer.writeEndElement();
        nl();
//        writer.writeStartElement(ELEMENT_HOST);
//        writer.writeCharacters(hostnameUnix());
//        writer.writeEndElement();
//        writer.writeCharacters("\n");                
//        writer.flush();
        if (userComment != null || "".equals(userComment)) {
            writer.writeStartElement(ELEMENT_USER_COMMENT);
            writer.writeCharacters(encodeSpecial(userComment));
            writer.writeEndElement();
            nl();
        }
        writer.writeStartElement(ELEMENT_DIGESTS_AVAILABLE);
        nl();
        for( String digest: digests) { 
            writer.writeStartElement(ELEMENT_DIGEST_NAME);
            writer.writeAttribute(ATTR_DIGEST_NAME_NAME, digest);
            writer.writeEndElement(); //digest-name (note CRC32 is not actually a messageDigest algorithm
            nl();
        }
        writer.writeEndElement();  // digest-available
        writer.writeStartElement(ELEMENT_SNAPSHOT);
        nl();
        writeDirNode(dir);
        writer.writeEndElement();  // snapshot
        nl();
        writer.writeEndElement();  // file-tree-snapshot
        writer.close();
    }

    private void writeDirNode(DirNode dir) throws XMLStreamException, IOException {
        writer.writeStartElement(ELEMENT_DIR);
        writer.writeAttribute(ATTR_DIR_NAME, encodeSpecial(dir.getName()));
        nl();
        if ( dir.getLeaves().size() > 0 ) {
            writeFileLeaves(dir.getLeaves());
        }
        if ( dir.getDirs().size() > 0 ) {
            writeDirNodes(dir.getDirs());
        }
        writer.writeEndElement();  // dir
        nl();
        
        
    }

    private void writeDirNodes(List<DirNode> dirs) throws XMLStreamException, IOException {
        writer.writeStartElement(ELEMENT_DIRS);
        nl();
        for (DirNode dir: dirs) {
            writeDirNode( dir );
        }
        writer.writeEndElement();  // dirs
        nl();
        
    }

    private void writeFileLeaves(List<Leaf> leaves) throws XMLStreamException, IOException {
        writer.writeStartElement(ELEMENT_FILES);
        nl();
        for (Leaf leaf: leaves) {
            if ( ! ( leaf instanceof FileNode ) ) {
                throw new IllegalArgumentException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamWriter.bug_leaf_not_file"),
                        leaf.getClass(),
                        leaf.getName()
                    )
                );
            }
            
            writeFileNode((FileNode) leaf);
        }
        writer.writeEndElement();  // files
        nl();
        
        
    }

    private String encodeSpecial(String s) throws IOException {
        if ( s == null )
            return "";
        
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IOException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamWriter.bug_unsupported_encoding"),
                    e.getLocalizedMessage(),
                    s
                )
            );
        }
            
    }
    
    private void writeFileNode(FileNode leaf) throws XMLStreamException, IOException {
        writer.writeStartElement(ELEMENT_FILE);
        // note bug(?) in XMLStreamWriter, doesn't convert newlines and other special characters to entities
        // see http://www.w3.org/TR/2000/WD-xml-c14n-20000119.html#charescaping
        // and http://stackoverflow.com/questions/2004386/how-to-save-newlines-in-xml-attribute
        writer.writeAttribute( ATTR_FILE_NAME, encodeSpecial(leaf.getName()));
        writer.writeAttribute( ATTR_FILE_SIZE, Long.toString(leaf.getSize()));
        writer.writeAttribute( ATTR_FILE_TIME, dateFormat.format( new Date(leaf.getTime()) ) );
        if ( leaf.isText() )
            writer.writeAttribute(ATTR_FILE_ISTEXT, FILE_ISTEXT_YES);
        else
            writer.writeAttribute(ATTR_FILE_ISTEXT, FILE_ISTEXT_NO);

        String type = null;
        switch( leaf.getFileType() ) {
        case REGFILE:
            type = FILE_TYPE_REGFILE;
            break;
        case SPECIAL:
            type = FILE_TYPE_SPECIAL;
            break;
        case SYMLINK:
            type = FILE_TYPE_SYMLINK;
            break;
        default:
            throw new IllegalArgumentException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamWriter.bug_unknown_file_type"),
                    leaf.getName(),
                    leaf.getFileType()
                )
            );
        }
        writer.writeAttribute(ATTR_FILE_TYPE, type);
        if ( leaf.getFileType() == FileType.REGFILE ) {
            nl();
            writeDigests(leaf);
        }
        else if ( leaf.getFileType() == FileType.SYMLINK ) {
            nl();
            writeLinkTo(leaf);
        }
        writer.writeEndElement();  // file
        nl();
    }

    private void writeLinkTo(FileNode leaf) throws XMLStreamException, IOException {
        writer.writeStartElement(ELEMENT_LINKTO);
        String target = encodeSpecial(leaf.getLinkTo());
        writer.writeAttribute(ATTR_LINKTO_TARGET, target);
        writer.writeEndElement();
        nl();
        
    }

    private void writeDigests(FileNode leaf) throws IOException, XMLStreamException {
        for(String digestName: digests) {
            switch (digestName) {
            case DIGEST_CRC32:
                // TODO if CRC or MD5 not possible should we flag this to the user?
                if ( leaf.getContentMethodCost(FileNode.CONTENT_METHOD_CRC) < FileNode.COST_IMPOSSIBLE)
                    writeCrc32(leaf);
                break;
            case DIGEST_MD5:
                if ( leaf.getContentMethodCost(FileNode.CONTENT_METHOD_MD5) < FileNode.COST_IMPOSSIBLE)
                    writeMD5(leaf);
                break;
            default:
                throw new IllegalStateException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamWriter.digest_unknown"),
                        digestName
                    )
                );
            }
        }        
    }

    private void writeMD5(FileNode leaf) throws IOException, XMLStreamException {
        byte[] digest = leaf.getMd5();
        StringBuilder sb = new StringBuilder();
        for( byte b : digest ) {
            sb.append( String.format("%02x", 0xff & (int)b) );
        }
        writer.writeStartElement(ELEMENT_DIGEST);
        writer.writeAttribute(ATTR_DIGEST_NAME, DIGEST_MD5);
        writer.writeCharacters(sb.toString());
        writer.writeEndElement();
        nl();

    }

    private void writeCrc32(FileNode leaf) throws IOException, XMLStreamException {
        String crc32 = String.format("%08x", leaf.getCrc());
        writer.writeStartElement(ELEMENT_DIGEST);
        writer.writeAttribute(ATTR_DIGEST_NAME, "CRC32");
        writer.writeCharacters(crc32);
        writer.writeEndElement();
        nl();
    }

//    public void close() throws XMLStreamException {
//        writer.close(); // does not close underlying stream
//    }
}
