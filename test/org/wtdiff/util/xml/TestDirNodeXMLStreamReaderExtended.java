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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.xml.DirNodeXMLStreamReader;

@RunWith(Parameterized.class)
public class TestDirNodeXMLStreamReaderExtended {

    private String testFile;
    private boolean isExpectedReadOk;
    private String exceptionMessage;
    public TestDirNodeXMLStreamReaderExtended(String testName, boolean isReadOk, String message) {
        super();
        testFile = testName;
        isExpectedReadOk = isReadOk;
        exceptionMessage = message;
       }

    @Parameters(name= "{0}-{1}")
    public static Iterable<Object[]> data() {        
        return Arrays.asList( 
            new Object[][] { 
                { "badsnap00", false, "Premature end of file." },
                { "badsnap01", false, "Content is not allowed in prolog." },
                { "badsnap02", false, "expected <file-tree-snapshot> got <some-elem>" },
                { "badsnap03", false, "text content not allowed in <file-tree-snapshot>" },
                { "badsnap04", false, "<file-tree-snapshot> unexpected child element <bad>" },
                { "badsnap05", false, "<file-tree-snapshot> missing <snapshot>" },
                { "badsnap06", false, "duplicate element <capture-time>" },
                { "badsnap07", false, "duplicate element <capture-root>" },
                { "badsnap08", false, "unexpected attribute root for element <capture-root>" },
                { "badsnap09", false, "<file-tree-snapshot> unexpected child element <digest-name>" },
                { "badsnap10", false, "text content not allowed in <digests-available>" },
                { "badsnap11", false, "unexpected attribute this for element <file-tree-snapshot>" },
                { "badsnap12", false, "unexpected attribute this for element <digests-available>" },
                { "badsnap13", false, "<digests-available> unexpected child element <digests-available>" },
                { "badsnap14", false, "text content not allowed in <digest-name>" },
                { "badsnap15", false, "text content not allowed in <file-tree-snapshot>" },
                { "badsnap16", false, "unexpected attribute this for element <snapshot>" },
                { "badsnap17", false, "text content not allowed in <snapshot>" },
                { "badsnap18", false, "<snapshot> unexpected child element <dirs>" },
                { "badsnap19", false, "missing attribute name for element <dir>" },
                { "badsnap20", false, "unexpected attribute this for element <dir>" },
                { "badsnap21", false, "text content not allowed in <dir>" },
                { "badsnap22", false, "text content not allowed in <dir>" },
                { "badsnap23", false, "text content not allowed in <dir>" },
                { "badsnap24", false, "<snapshot> unexpected child element <dir>" },
                { "badsnap25", false, "unexpected attribute x for element <files>" },
                { "badsnap26", false, "missing attribute name for element <file>" },
                { "badsnap27", false, "missing attribute size for element <file>" },
                { "badsnap28", false, "missing attribute time for element <file>" },
                { "badsnap29", false, "missing attribute istext for element <file>" },
                { "badsnap30", false, "missing attribute type for element <file>" },
                { "badsnap31", false, "unexpected attribute this for element <file>" },
                { "badsnap32", false, "empty name for <file>" },
                { "badsnap33", false, "bad file size 0x0" },
                { "badsnap34", false, "bad file date 2015-04-15T07:35:30.X00+0000" },
                { "badsnap35", false, "file istext neither yes nor no x" },
                { "badsnap36", false, "file unknown file type what?" },
                { "badsnap37", false, "text content not allowed in <file>" },
                { "badsnap38", false, "<file> unexpected child element <badelem>" },
                { "badsnap39", false, "missing attribute name for element <digest>" },
                { "badsnap40", false, "unexpected attribute other for element <digest>" },
                { "badsnap41", false, "empty <digest> or odd number of chars " },
                { "badsnap42", false, "empty <digest> or odd number of chars 123" },
                { "badsnap43", false, "bad <digest> data ab8x" },
                { "badsnap44", false, "duplicate <digest> with name CRC32" },
                { "badsnap45", false, "<digest> only applicable for regular files" },
                { "badsnap46", false, "missing attribute target for element <linkto>" },
                { "badsnap47", false, "unexpected attribute other for element <linkto>" },
                { "badsnap48", false, "empty target attribute value" },
                { "badsnap49", false, "<linkto> only applicable for symbolic links" },
                { "badsnap50", false, "size does not match linkto length" },
                { "badsnap51", false, "text content not allowed in <linkto>" },
                { "badsnap52", false, "more than one <linkto> element" },
                { "badsnap53", false, "<file> unexpected child element <dirs>" },
                { "badsnap54", false, "text content not allowed in <file>" },
                { "badsnap55", false, "negative file size -2" },
                { "badsnap56", false, "duplicate file/directory name dupfile in directory 123" },
                { "badsnap57", false, "duplicate file/directory name dupdir in directory 123" },
                { "badsnap58", false, "duplicate file/directory name dupfiledir in directory 123" },
                { "badsnap59", false, "unexpected attribute what for element <dirs>" },
                { "badsnap60", false, "text content not allowed in <dirs>" },
                { "badsnap61", false, "<dirs> unexpected child element <file>" },
                { "goodsnap62", true, "" },
                { "badsnap63", false, "empty name for <dir>" },
                { "badsnap64", false, "<file-tree-snapshot> unexpected child element <user>" },
                { "badsnap65", false, "<capture-root> unexpected child element <user>" },
                { "goodsnap66", true, "" },
                { "goodsnap67", true, "" },
                { "badsnap68", false, "<dir> unexpected child element <dir>" },
                { "goodsnap69", true, "" },
                { "badsnap70", false, "<dir> can only have one <dirs> element" },
                { "badsnap71", false, "<dir> can only have one <files> element" },
                { "badsnap72", false, "malformed encoded value 123%X for <dir> name attribute" }, // this may be implementation dependent
                { "badsnap73", false, "malformed encoded value n%Z for <file> name attribute" }, // this may be implementation dependent
        } );
//        };
//        try {
//            URLDecoder.decode("%Z", "UTF-8");
//        } catch (IllegalArgumentException iae) {
//            testList.add( new Object[] { "badsnap72", false, "malformed encoded value 123%X for <dir> name attribute" });
//            testList.add( new Object[]{ "badsnap73", false, "malformed encoded value n%Z for <file> name attribute" } );
//        } catch (UnsupportedEncodingException e) {
//            skip("");
//            warn("");
//        }
    }
    
    private InputStream getTestInputStream() {
        String resourceName =  "data/org/wtdiff/util/xml/TestDirNodeXMLStreamReaderExtended/" + testFile + ".xml";
        InputStream is =   this.getClass().getClassLoader().getResourceAsStream(
            resourceName
            );
        assertNotNull("Could not get test data " + resourceName, is);
        return is;
    }
    
    @Test
    public void test() throws IOException, XMLStreamException {
        try ( InputStream is = getTestInputStream()) {
            DirNodeXMLStreamReader reader= new DirNodeXMLStreamReader();
            try {
                reader.readSnapshot(is);
                if ( ! isExpectedReadOk ) {
                    fail("expected read to fail, but no exception thrown");
                }
            } catch (XMLStreamException e) {
                System.out.println(testFile + ":" + e.getMessage());
                if ( e.getNestedException() != null )
                    e.getNestedException().printStackTrace();
                if ( isExpectedReadOk ) {
                    throw e;
                }
                
                assertTrue( e.getMessage() + " DOES NOT END WITH " + exceptionMessage, 
                    e.getMessage().endsWith(exceptionMessage) );
                // otherwise we expected the exception
            }
            System.out.println(testFile + " " + isExpectedReadOk);
//            fail("Not yet implemented");
        }
    }

}
