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

import java.io.CharConversionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.util.ResolverUtil.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.FileNode.FileType;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class DirNodeXMLStreamReader implements DirNodeXMLStreamConstants {
    
    private List<String> availableDigests = new ArrayList<>();
    private HashMap<String,String> snapshotInfo = new HashMap<>();
    private DateFormat dateTimeFormat = new SimpleDateFormat(FILE_TIME_FORMAT_STRING);
    
    private void clearSnapshotInfo() {
        snapshotInfo = new HashMap<>();
    }
    
    public Map<String,String> getSnapshotInfo() {
        return snapshotInfo;        
    }
    public List<String> getAvailableDigests() {
        return availableDigests;
    }
    private XMLStreamReader createReader(InputStream input) throws XMLStreamException {
        XMLInputFactory fact = XMLInputFactory.newInstance();
        // simplify things by requesting that adjacent CHARACTER and or CDATA is combined
        fact.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
        // SECURITY dtd support opens security hole via accessing URIs and entity expansion 
        fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        fact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);        
        return fact.createXMLStreamReader(input);
        
    }
    public boolean isSnapshot(InputStream input) throws IOException {
        XMLStreamReader reader = null;
        try  {
            reader = createReader(input);
            while ( reader.hasNext() ) {
                int type = reader.next();
                switch ( type ) {
                case XMLStreamConstants.START_ELEMENT:
                    if ( ELEMENT_FILE_TREE_SNAPSHOT.equals(reader.getLocalName()) ) {
                        return true;
                    } else {
                        return false;
                    }
                case XMLStreamConstants.DTD:
                    break;
                default:
                }
            }
        } catch (XMLStreamException e) {
            // binary data may explode as a MalformedByteSequence, which is a kind of IO exception,
            // but does not reflect a read error in the input.  It simply indicates that this is not
            // XML
            if ( e.getNestedException() instanceof CharConversionException )
                return false;
            
            if ( e.getNestedException() instanceof IOException )
                throw  (IOException)e.getNestedException();
            
            return false;
        } finally  {
            if ( reader != null ) {
                try { reader.close(); } catch (Exception e) {};
            }
        }
        return false;
    }

    public synchronized DirNode readSnapshot(InputStream input) throws XMLStreamException, IOException  {
        clearSnapshotInfo();
        DirNode topNode = null;
        XMLStreamReader reader = null;
        try {
            reader = createReader(input);
            while ( reader.hasNext() ) {
                int type = reader.next();
                switch ( type ) {
                case XMLStreamConstants.START_ELEMENT:
                    if ( ELEMENT_FILE_TREE_SNAPSHOT.equals(reader.getLocalName()) ) {
                        noAttributeCheck(reader, ELEMENT_FILE_TREE_SNAPSHOT);
                        topNode = processFileTreeSnapshot(reader);
                    } else {                         
                        throw new XMLStreamException(
                            MessageFormat.format(
                                Messages.getString("DirNodeXMLStreamReader.unexpected_element"),
                                ELEMENT_FILE_TREE_SNAPSHOT,
                                reader.getLocalName()
                            ),
                            reader.getLocation());
                    }
                    break;
                case XMLStreamConstants.DTD:
                    break; // OK
                case XMLStreamConstants.CHARACTERS: 
                case XMLStreamConstants.CDATA:
                    whitespaceCheck(reader);
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    break;                
                case XMLStreamConstants.END_DOCUMENT:
                    if ( topNode == null ) {
                        throw new XMLStreamException(
                            MessageFormat.format(
                                Messages.getString("DirNodeXMLStreamReader.missing_element"),
                                ELEMENT_FILE_TREE_SNAPSHOT,
                                reader.getLocalName()
                            ),
                        reader.getLocation());
                    }
                    return topNode;
                case XMLStreamConstants.COMMENT:
                    break;
                default:
                    throw new XMLStreamException(
                        MessageFormat.format(
                            Messages.getString("DirNodeXMLStreamReader.unexpected_parse_event"),
                            type
                        ),
                        reader.getLocation());
                }
            }
        } catch (XMLStreamException xse) {
            if ( xse.getNestedException() instanceof IOException )
                throw  (IOException)xse.getNestedException();
            throw xse;
        } finally  {
            if ( reader != null ) {
                try { reader.close(); } catch (Exception e) {};
            }
        }
        throw new IOException("unexpected end of <" + ELEMENT_FILE_TREE_SNAPSHOT + ">");
    }
    
    private String getElement(XMLStreamReader reader, String parent, String ... allowedElements) throws XMLStreamException {
        
        while ( reader.hasNext() ) {
            int type = reader.next();
            switch ( type ) {
            case XMLStreamConstants.START_ELEMENT:
                String element = reader.getLocalName();
                for ( String allowed: allowedElements ) {
                    if ( allowed.equals(element) )
                        return element;
                }
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_child_element"),
                        parent,
                        element
                    ),
                    reader.getLocation());
                
            case XMLStreamConstants.END_ELEMENT:
                return null;
            case XMLStreamConstants.CHARACTERS: 
            case XMLStreamConstants.CDATA:
                whitespaceCheck(reader, parent);
                break;
            case XMLStreamConstants.END_DOCUMENT:
                    throw new XMLStreamException(
                        MessageFormat.format(
                            Messages.getString("DirNodeXMLStreamReader.unexpected_end_of_document"),
                            parent
                        ),
                    reader.getLocation());
            case XMLStreamConstants.COMMENT:
                break;
            default:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_parse_event_in"),
                        type,
                        parent
                    ),
                    reader.getLocation());
            }
        }
        return null;
    }

    private void whitespaceCheck(XMLStreamReader reader) throws XMLStreamException {
        if ( ! reader.isWhiteSpace() ) {
            throw new XMLStreamException(
                Messages.getString("DirNodeXMLStreamReader.text_content_not_allowed"),
                reader.getLocation());
        }
    }

    private void whitespaceCheck(XMLStreamReader reader, String name) throws XMLStreamException {
        if ( ! reader.isWhiteSpace() ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.text_content_not_allowed_in"),
                    name
                ),
                reader.getLocation());
        }
    }

    private void noAttributeCheck(XMLStreamReader reader, String name) throws XMLStreamException {
        if ( reader.getAttributeCount() > 0 ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.unexpected_attribute"),
                    reader.getAttributeLocalName(0),
                    name
                ),
                reader.getLocation());
        }
    }

    private HashMap<String,String> exactAttributeCheck(XMLStreamReader reader, String element,  String ... names) throws XMLStreamException {
        HashMap<String,String> valueByName = new HashMap<>();
        for( String name: names ) {
            valueByName.put(name, null);
        }
        for ( int i = 0; i < reader.getAttributeCount(); i++ ) {
            String name = reader.getAttributeLocalName(i);
            if ( !valueByName.containsKey(name) ) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_attribute"),
                        name,
                        element
                    ),
                    reader.getLocation());
            }
            if ( valueByName.get(name) != null ) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.duplicate_attribute"),
                        name,
                        element
                    ),
                    reader.getLocation());
                
            }
            valueByName.put(name,  reader.getAttributeValue(i));
        }
        for( String name: names ) {
            if ( valueByName.get(name) == null ) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.missing_attribute"),
                        name,
                        element
                    ),
                    reader.getLocation());
            }
        }
        return valueByName;
    }

    private void skipToEndElement(XMLStreamReader reader, String element) throws XMLStreamException {
        while ( reader.hasNext() ) {
            int type = reader.next();
            switch ( type ) {
            case XMLStreamConstants.START_ELEMENT:
                String childElement =  reader.getLocalName();
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_child_element"),
                        element,
                        childElement
                    ),
                    reader.getLocation()
                );    
                
//            case XMLStreamConstants.ATTRIBUTE:
//                throw new XMLStreamException(
//                    "<" + element + ">" + "unexpected attribute parsing event", reader.getLocation() 
//                    );
            case XMLStreamConstants.END_ELEMENT:
                return; // <------------------------------NORMAL return point is here ***
            case XMLStreamConstants.CHARACTERS: 
            case XMLStreamConstants.CDATA:
                whitespaceCheck(reader, element);
                break;
            case XMLStreamConstants.COMMENT:
                break;
            case XMLStreamConstants.END_DOCUMENT:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_end_of_document"),
                        element
                    ),
                    reader.getLocation());
            default:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_parse_event_in"),
                        type,
                        element
                    ),
                    reader.getLocation()
                );
            }
        }
        throw new XMLStreamException(
            MessageFormat.format(
                Messages.getString("DirNodeXMLStreamReader.unexpected_end_of_element"),
                element
            ),
            reader.getLocation()
        );

    }
    private DirNode processFileTreeSnapshot(XMLStreamReader reader) throws XMLStreamException {
        DirNode topNode = null;
        String element;
        while ( (element = getElement(reader, ELEMENT_FILE_TREE_SNAPSHOT, ELEMENT_SNAPSHOT, ELEMENT_CAPTURE_TIME, ELEMENT_CAPTURE_ROOT, 
         ELEMENT_USER, ELEMENT_HOME, ELEMENT_CURRENT_DIR, ELEMENT_OS, ELEMENT_HOST, ELEMENT_USER_COMMENT, ELEMENT_DIGESTS_AVAILABLE)) != null ) {
            switch (element) {
            case ELEMENT_SNAPSHOT:
                topNode = processSnapshot(reader);
                skipToEndElement(reader, ELEMENT_FILE_TREE_SNAPSHOT);
                return topNode;
            case ELEMENT_CAPTURE_TIME:
                processInfoElement(reader, element, false);
                //TODO long captureTime
                break;
            case ELEMENT_CAPTURE_ROOT:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_USER:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_HOME:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_CURRENT_DIR:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_OS:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_HOST:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_USER_COMMENT:
                processInfoElement(reader, element, true);
                break;
            case ELEMENT_DIGESTS_AVAILABLE:
                processDigestsAvailable(reader);
                break;
            default:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.bug_child_element"),
                        ELEMENT_FILE_TREE_SNAPSHOT,
                        element
                    ),
                    reader.getLocation());
            }
        }
        throw new XMLStreamException(
            MessageFormat.format(
                Messages.getString("DirNodeXMLStreamReader.missing_child_element"),
                ELEMENT_FILE_TREE_SNAPSHOT,
                ELEMENT_SNAPSHOT
            ),
            reader.getLocation()
        );
    }

    private void processDigestsAvailable(XMLStreamReader reader) throws XMLStreamException {
        noAttributeCheck(reader, ELEMENT_DIGESTS_AVAILABLE);
        while ( ( getElement( reader, ELEMENT_DIGESTS_AVAILABLE, ELEMENT_DIGEST_NAME) ) != null ) {
            String digestName = exactAttributeCheck(reader, ELEMENT_DIGEST_NAME, ATTR_DIGEST_NAME_NAME).get(ATTR_DIGEST_NAME_NAME);
            availableDigests.add( digestName );
            skipToEndElement(reader, ELEMENT_DIGEST_NAME);
            
        }
    }

    private void processInfoElement(XMLStreamReader reader, String element, boolean decode) throws XMLStreamException {
        noAttributeCheck(reader, element);
        String text = getText(reader, element);
        if ( decode )
            text = decodeSpecial(text);
        
        addInfoElement(reader.getLocation(), element, text);
        return;
    }


    private String getText(XMLStreamReader reader, String element) throws XMLStreamException {
        StringBuilder sb = new StringBuilder();
        while ( reader.hasNext() ) {
            int type = reader.next();
            switch ( type ) {
            case XMLStreamConstants.START_ELEMENT:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_child_element"),
                        element,
                        reader.getLocalName()
                    ),
                    reader.getLocation()
                );
            case XMLStreamConstants.END_ELEMENT:
                return sb.toString();
            case XMLStreamConstants.CHARACTERS: 
            case XMLStreamConstants.CDATA:
                sb.append(reader.getText());
                break;
            case XMLStreamConstants.END_DOCUMENT:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_end_of_document"),
                        element
                    ),
                    reader.getLocation()
                );
            case XMLStreamConstants.COMMENT:
                break;
            case XMLStreamConstants.ATTRIBUTE:
            default:
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.unexpected_parse_event_in"),
                        type,
                        element
                    ),
                    reader.getLocation()
                );
            }
        }
        throw new XMLStreamException(
            MessageFormat.format(
                Messages.getString("DirNodeXMLStreamReader.unexpected_end_of_element"),
                element
            ),
            reader.getLocation()
        );
    }

    private void addInfoElement(Location loc, String element, String value) throws XMLStreamException {
        if ( snapshotInfo.containsKey(element) ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.duplicate_element"),
                    element
                ),
                loc
             );
        }
        else {
            // trim() may result in an inaccuracy in capture-root if it begins/ends with a space 
            snapshotInfo.put(element, value.trim() );   
        }
    }

    private DirNode processSnapshot( XMLStreamReader reader ) throws XMLStreamException {        
        noAttributeCheck(reader, ELEMENT_SNAPSHOT);
        DirNode topNode = null;
        getElement(reader, ELEMENT_SNAPSHOT, ELEMENT_DIR);
        topNode = processDir(reader, true);
        skipToEndElement(reader, ELEMENT_SNAPSHOT);
        return topNode;
    }
    
    private DirNode processDir( XMLStreamReader reader, boolean isTop ) throws XMLStreamException {
        List<Leaf> files = null;
        List<DirNode> dirs = null;
        String name = exactAttributeCheck(reader, ELEMENT_DIR, ATTR_DIR_NAME).get(ATTR_DIR_NAME);
        try {
            name = decodeSpecial(name);
        } catch ( IllegalArgumentException ila ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.bad_encode_attribute"),
                    name,
                    ELEMENT_DIR,
                    ATTR_DIR_NAME
                ),
                reader.getLocation()
             );                         
        }

        if ( isTop && "".equals(name) ) {
            // empty name is OK but only for top directory 
        } else {
            checkName(reader, ELEMENT_DIR, name);
        }
        String element;
        while ( ( element = getElement(reader, ELEMENT_DIR, ELEMENT_FILES, ELEMENT_DIRS) ) != null ) {
            if ( ELEMENT_DIRS.equals(element) ) {
                    if ( dirs != null ) {
                        throw new XMLStreamException(
                            MessageFormat.format(
                                Messages.getString("DirNodeXMLStreamReader.parent_multiple_child"),
                                ELEMENT_DIR,
                                ELEMENT_DIRS
                             ),
                             reader.getLocation()
                         );
                    }
                    dirs = processDirs(reader);    
            } else if ( ELEMENT_FILES.equals(element) ) {
                if ( files != null ) {
                        throw new XMLStreamException(
                            MessageFormat.format(
                                Messages.getString("DirNodeXMLStreamReader.parent_multiple_child"),
                                ELEMENT_DIR,
                                ELEMENT_FILES
                             ),
                             reader.getLocation()
                         );
                    }
                    files = processFiles(reader);
            }
        }
        if ( files == null ) {
            files = new ArrayList<>(0);
        }
        if ( dirs == null ) {
            dirs = new ArrayList<>(0);
        }
        uniqueNamesCheck(reader, name, files, dirs);
        return new DirNode(name, files, dirs);
        
    }
    
    private void uniqueNamesCheck(XMLStreamReader reader, String dirName, List<Leaf> files, List<DirNode> dirs) throws XMLStreamException {
        HashSet<String> names = new HashSet<>();
        for( Leaf node: files ) {
            String name = node.getName();
            if ( names.contains(name) ) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.duplicate_file_or_dir_name"),
                        name,
                        dirName
                     ),
                     reader.getLocation()
                 );
            }
            names.add(name);
        }
        for( DirNode node: dirs ) {
            String name = node.getName();
            if ( names.contains(name) ) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.duplicate_file_or_dir_name"),
                        name,
                        dirName
                    ),
                    reader.getLocation()
                 );
            }
            names.add(name);
        }        
    }

    private List<DirNode> processDirs( XMLStreamReader reader ) throws XMLStreamException  {
        List<DirNode> dirs = new ArrayList<>();
        noAttributeCheck(reader, ELEMENT_DIRS);
        while ( getElement(reader, ELEMENT_DIRS, ELEMENT_DIR) != null ) {
            dirs.add( processDir(reader, false) );
        }
        return dirs;
    }

    private List<Leaf> processFiles( XMLStreamReader reader ) throws XMLStreamException {
        List<Leaf> files = new ArrayList<>();
        noAttributeCheck(reader, ELEMENT_FILES);
        while ( getElement(reader, ELEMENT_FILES, ELEMENT_FILE) != null ) {
            files.add( processFile(reader) );
        }
        return files;

    }
    
    private String decodeSpecial(String s) throws XMLStreamException, IllegalArgumentException {
        if ( s.indexOf('%') < 0 && s.indexOf('+') < 0 )
            return s;
        
        try {
            // a badly encoded string may throw IllegalArgumentException, or
            // it may leave illegal characters alone.  The behaviour is implementation dependent. 
            // UnsupportedEncodingException would happen if "UTF-8" charset not supported.  SHOULDN'T HAPPEN, ... but
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.bug_decode"),
                    s,
                    e.getLocalizedMessage()
                )
            );
        }
    }

 
    private FileNode processFile( XMLStreamReader reader ) throws XMLStreamException   {
        String name = null;
        Long size = null;
        Long mtime = null;
        Boolean isText = null;
        FileNode.FileType fileType = null;
        
        HashMap<String, byte[]> digests = new HashMap<>();;
        String linkTo = null;
        
        HashMap<String,String> attrByName = exactAttributeCheck(reader, ELEMENT_FILE, ATTR_FILE_NAME, ATTR_FILE_SIZE, ATTR_FILE_TIME, ATTR_FILE_ISTEXT, ATTR_FILE_TYPE);
        name = attrByName.get(ATTR_FILE_NAME);
        try {
            name = decodeSpecial(name);
        } catch ( IllegalArgumentException ila ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.malformed_name"),
                    name,
                    ELEMENT_FILE,
                    ATTR_FILE_NAME
                ),
                reader.getLocation()
            );
        }
        checkName(reader, ELEMENT_FILE, name);
        try {
            size = new Long( attrByName.get(ATTR_FILE_SIZE));
            if ( size < 0 ) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.file_size_negative"),
                        size
                    ),
                    reader.getLocation()
                );  
            }
        } catch (NumberFormatException nfe) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.file_size_bad"),
                    attrByName.get(ATTR_FILE_SIZE)
                ),
                reader.getLocation()
            );  
        }
        try {
            mtime  = new Long(dateTimeFormat.parse(attrByName.get(ATTR_FILE_TIME)).getTime());
        } catch (ParseException pe) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.file_time_bad"),
                    attrByName.get(ATTR_FILE_TIME)
                ),
                reader.getLocation()
            );  
        }
        String isTextString = attrByName.get(ATTR_FILE_ISTEXT);
        if ( FILE_ISTEXT_YES.equals(isTextString) ) { 
            isText = Boolean.TRUE;
        } else if ( FILE_ISTEXT_NO.equals(isTextString) ) {
            isText = Boolean.FALSE;
        } else {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.file_istext_bad"),
                    ATTR_FILE_ISTEXT,
                    FILE_ISTEXT_YES, 
                    FILE_ISTEXT_NO,
                    isTextString
                ),
                reader.getLocation()
            );
        }

        String typeString = attrByName.get(ATTR_FILE_TYPE);
        if ( FILE_TYPE_REGFILE.equalsIgnoreCase(typeString)) { 
            fileType = FileType.REGFILE;
        } else if ( FILE_TYPE_SPECIAL.equalsIgnoreCase(typeString)) {
            fileType = FileType.SPECIAL;
        } else if ( FILE_TYPE_SYMLINK.equalsIgnoreCase(typeString))
            fileType = FileType.SYMLINK;
        else {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.file_type_bad"),
                    
                    typeString
                ),
                reader.getLocation()
            );
        }
        
        String element;
        while( ( element = getElement(reader, ELEMENT_FILE, ELEMENT_DIGEST, ELEMENT_LINKTO) ) != null ) {
            if ( ELEMENT_DIGEST.equals(element) ) { 
                if ( fileType != FileType.REGFILE ) {
                    throw new XMLStreamException(
                        MessageFormat.format(
                            Messages.getString("DirNodeXMLStreamReader.file_digests_not_applicable"),
                            ELEMENT_DIGEST
                        ),
                        reader.getLocation()
                    );
                }
                processDigest(reader, digests);
            } else if ( ELEMENT_LINKTO.equals(element) ) {
                if ( fileType != FileType.SYMLINK ) {
                    throw new XMLStreamException(
                        MessageFormat.format(
                            Messages.getString("DirNodeXMLStreamReader.file_linkto_not_applicable"),
                            ELEMENT_LINKTO
                        ),
                        reader.getLocation()
                    );
                }
                else if ( linkTo != null ) {
                    throw new XMLStreamException(
                        MessageFormat.format(
                            Messages.getString("DirNodeXMLStreamReader.file_multiple_linkto"),
                            ELEMENT_LINKTO
                        ),
                        reader.getLocation()
                    );
                }
                linkTo = processLinkTo(reader);
            }
        }

        try {
            return buildFileNode(name,size,mtime,isText,fileType,linkTo,digests);
        } catch (IllegalArgumentException ila) {
            throw new XMLStreamException(ila.getMessage(), reader.getLocation());
        }

    }

    private void checkName(XMLStreamReader reader, String element, String name) throws XMLStreamException {
        if ( name.length() == 0 ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.empty_name"),
                    element
                ),
                reader.getLocation()
            );
        }
        // should we check for characters '/' '\\' ':' or '\0' ?
    }

    private FileNode buildFileNode(String name, Long size, Long mtime,
        Boolean isText, FileType fileType, String linkTo,
        HashMap<String, byte[]> digests) throws IllegalArgumentException  {
        return new SnapshotFileNode( name, size, mtime, isText, fileType, linkTo, digests);
        
    }

    private String processLinkTo( XMLStreamReader reader ) throws XMLStreamException {

        String target = exactAttributeCheck(reader, ELEMENT_LINKTO, ATTR_LINKTO_TARGET).get(ATTR_LINKTO_TARGET);
        target = decodeSpecial(target);
        if ( target.length() == 0 ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.empty_linkto_target"),
                    ELEMENT_LINKTO,
                    ATTR_LINKTO_TARGET
                ),
                reader.getLocation()
            );
        }
        skipToEndElement(reader, ELEMENT_LINKTO);
        return target;
    }

    
    private void processDigest(XMLStreamReader reader, HashMap<String, byte[]> digests) throws XMLStreamException {
        String name = exactAttributeCheck(reader, ELEMENT_DIGEST, ATTR_DIGEST_NAME).get(ATTR_DIGEST_NAME);
        if ( digests.containsKey(name) ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.file_duplicate_digest"),
                    ELEMENT_DIGEST,
                    ATTR_DIGEST_NAME,
                    name
                ),
                reader.getLocation()
            );
        }
        String digestString = getText(reader, ELEMENT_DIGEST).trim();
        if ( digestString.length() == 0 || digestString.length() % 2 != 0 ) {
            throw new XMLStreamException(
                MessageFormat.format(
                    Messages.getString("DirNodeXMLStreamReader.digest_badnum_char"),
                    ELEMENT_DIGEST,
                    digestString
                ),
                reader.getLocation()
            );
        }
        ArrayList<Byte> byteList = new ArrayList<>();
        for(int i = 0; i < digestString.length(); i+= 2) {
            try {
                byte b = Integer.valueOf(digestString.substring(i,  i+2), 16).byteValue();
                byteList.add(b);
            } catch (NumberFormatException nfe) {
                throw new XMLStreamException(
                    MessageFormat.format(
                        Messages.getString("DirNodeXMLStreamReader.digest_bad_data"),
                        ELEMENT_DIGEST,
                        digestString
                    ),
                    reader.getLocation()
                );
            }
        }
        byte[] digest = new byte[byteList.size()];
        for(int i = 0 ; i < byteList.size(); i++) {
            digest[i] = byteList.get(i);
        }                
        digests.put(name, digest);
        return;

    }

}
