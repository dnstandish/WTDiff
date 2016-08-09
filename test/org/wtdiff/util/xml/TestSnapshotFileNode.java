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
import java.util.Date;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileNode.FileType;
import org.wtdiff.util.xml.SnapshotFileNode;

public class TestSnapshotFileNode {

    @Test
    public void testRegFileBasic() throws IOException {
        Date now = new Date();
        HashMap<String, byte[]> digests = new HashMap<>();
        digests.put("CRC32", new byte[] {10, -13, 127, 9} ); // 0af37f09
        byte[] md5Bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, -1, -2, -3, -4, -5, -6, -7, -8};
        digests.put("MD5", md5Bytes);
        SnapshotFileNode  node = new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.REGFILE, null, digests);
        
        assertEquals("name", node.getName());
        assertEquals(now.getTime(), node.getTime());
        assertEquals(999_999_999_999L, node.getSize());
        assertFalse( node.isText());
        assertEquals( 0x0af37f09, node.getCrc());
        assertArrayEquals(md5Bytes, node.getMd5());
        assertEquals("", node.getLinkTo());
        assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC), 0.0);
        assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5), 0.0);
        assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT), 0.0);
        assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT), 0.0);
        try {
            node.getInputStream();
            fail("getInputStream() should throw exception fro regular file");
        } catch (IOException ioe) {
            // this should happen
        }
    }

    @Test
    public void testRegFileDigest() throws IOException {
        Date now = new Date();
        byte[] crc32Bytes = new byte[] {10, -13, 127, 9}; // 0af37f09
        byte[] md5Bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, -1, -2, -3, -4, -5, -6, -7, -8};

        {
            HashMap<String, byte[]> digests = new HashMap<>();
            digests.put("CRC32", crc32Bytes );
            //digests.put("MD5", md5Bytes);
            SnapshotFileNode  node = new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.REGFILE, null, digests);
        
            assertEquals( 0x0af37f09, node.getCrc());
            try {
                node.getMd5();
                fail( "getMd5() should throw exception if no md5");
            } catch ( IOException ioe ) {
                // this should happen
            }
            assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC), 0.0);
            assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5), 0.0);
        }
        {
            HashMap<String, byte[]> digests = new HashMap<>();
            //digests.put("CRC32", crc32Bytes );
            digests.put("MD5", md5Bytes);
            SnapshotFileNode  node = new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.REGFILE, null, digests);

            assertArrayEquals(md5Bytes, node.getMd5());
            try {
                node.getCrc();
                fail( "getCrc() should throw exception if no crc32");
            } catch ( IOException ioe ) {
                // this should happen
            }
            assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC), 0.0);
            assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5), 0.0);
        }
        {
            HashMap<String, byte[]> digests = new HashMap<>();
            //digests.put("CRC32", crc32Bytes );
            digests.put("MD5", md5Bytes);
            SnapshotFileNode  node = new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.REGFILE, null, digests);

            assertArrayEquals(md5Bytes, node.getMd5());
            try {
                node.getCrc();
                fail( "getCrc() should throw exception if no crc32");
            } catch ( IOException ioe ) {
                // this should happen
            }
            assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC), 0.0);
            assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5), 0.0);
        }
        {
            HashMap<String, byte[]> digests = new HashMap<>();
            digests.put("CRC32",  new byte[] {10, -13, 127, 9, 1} );
            try {
                new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.REGFILE, null, digests);
                fail("contructor should throw exception if CRC32 more than 4 bytes");
            } catch (IllegalArgumentException iae) {
                // this should happen
            }
        }
       
    }
    
    @Test
    public void testSpecial() throws IOException {
        Date now = new Date();
        HashMap<String, byte[]> digests = new HashMap<>();
        digests.put("CRC32", new byte[] {10, -13, 127, 9} ); // 0af37f09
        byte[] md5Bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, -1, -2, -3, -4, -5, -6, -7, -8};
        digests.put("MD5", md5Bytes);
        SnapshotFileNode  node = new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.SPECIAL, null, digests);
        
        assertEquals("name", node.getName());
        assertEquals(now.getTime(), node.getTime());
        assertEquals(999_999_999_999L, node.getSize());
        assertFalse( node.isText());
        assertEquals( 0, node.getCrc());
        try {
            node.getMd5();
            fail("special files getMd5() should throw exception");
        } catch (IOException ioe) {
            // this should happen
        }
        assertEquals("", node.getLinkTo());
        assertEquals( FileNode.COST_HARD, node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC), 0.0);
        assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5), 0.0);
        assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT), 0.0);
        assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT), 0.0);
        try (InputStream is = node.getInputStream() ) {
            assertEquals( -1, is.read() ); // immediate EOF
        }
    }

    @Test
    public void testSymlink() throws IOException {
        Date now = new Date();
        HashMap<String, byte[]> digests = new HashMap<>();
        digests.put("CRC32", new byte[] {10, -13, 127, 9} ); // 0af37f09
        byte[] md5Bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, -1, -2, -3, -4, -5, -6, -7, -8};
        digests.put("MD5", md5Bytes);
        SnapshotFileNode  node = new SnapshotFileNode("name", 7, now.getTime(), false, FileType.SYMLINK, "../fred", digests);
        
        assertEquals("name", node.getName());
        assertEquals(now.getTime(), node.getTime());
        assertEquals(7, node.getSize());
        assertFalse( node.isText());
        assertEquals( 0x67b46f6e, node.getCrc());
        try {
            node.getMd5();
            fail("symlink files getMd5() should throw exception");
        } catch (IOException ioe) {
            // this should happen
        }
        assertEquals("../fred", node.getLinkTo());
        assertEquals( FileNode.COST_HARD, node.getContentMethodCost(FileNode.CONTENT_METHOD_CRC), 0.0);
        assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_MD5), 0.0);
        assertEquals( FileNode.COST_EASY, node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT), 0.0);
        assertEquals( FileNode.COST_IMPOSSIBLE, node.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT), 0.0);
        try (InputStream is = node.getInputStream() ) {
            byte[] buf = new byte[7];
            assertEquals(7, is.read(buf) );
            assertEquals( -1, is.read() ); // immediate EOF
            Assert.assertArrayEquals("../fred".getBytes(), buf);
        }
        
        try {
            new SnapshotFileNode("name", 999_999_999_999L, now.getTime(), false, FileType.SYMLINK, "../fred", digests);
            fail("symlink contruction should throw exception is size doesn't match lengh of linkto");
        } catch (IllegalArgumentException ila) {
            // this should happen
        }

    }

}
