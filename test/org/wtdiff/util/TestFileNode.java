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
package org.wtdiff.util;

import java.util.*;
import java.io.IOException;

import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileNode.FileType;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFileNode  {

    private static byte[] md5Collision1 = {
        (byte)0xd1,(byte)0x31,(byte)0xdd,(byte)0x02,
        (byte)0xc5,(byte)0xe6,(byte)0xee,(byte)0xc4,
        (byte)0x69,(byte)0x3d,(byte)0x9a,(byte)0x06,
        (byte)0x98,(byte)0xaf,(byte)0xf9,(byte)0x5c,
        (byte)0x2f,(byte)0xca,(byte)0xb5,(byte)0x87 /**/,
        (byte)0x12,(byte)0x46,(byte)0x7e,(byte)0xab,
        (byte)0x40,(byte)0x04,(byte)0x58,(byte)0x3e,
        (byte)0xb8,(byte)0xfb,(byte)0x7f,(byte)0x89,
        (byte)0x55,(byte)0xad,(byte)0x34,(byte)0x06,
        (byte)0x09,(byte)0xf4,(byte)0xb3,(byte)0x02,
        (byte)0x83,(byte)0xe4,(byte)0x88,(byte)0x83,
        (byte)0x25,(byte)0x71/**/,(byte)0x41,(byte)0x5a,
        (byte)0x08,(byte)0x51,(byte)0x25,(byte)0xe8,
        (byte)0xf7,(byte)0xcd,(byte)0xc9,(byte)0x9f,
        (byte)0xd9,(byte)0x1d,(byte)0xbd,(byte)0xf2/**/,
        (byte)0x80,(byte)0x37,(byte)0x3c,(byte)0x5b,
        (byte)0xd8,(byte)0x82,(byte)0x3e,(byte)0x31,
        (byte)0x56,(byte)0x34,(byte)0x8f,(byte)0x5b,
        (byte)0xae,(byte)0x6d,(byte)0xac,(byte)0xd4,
        (byte)0x36,(byte)0xc9,(byte)0x19,(byte)0xc6,
        (byte)0xdd,(byte)0x53,(byte)0xe2,(byte)0xb4/**/,
        (byte)0x87,(byte)0xda,(byte)0x03,(byte)0xfd,
        (byte)0x02,(byte)0x39,(byte)0x63,(byte)0x06,
        (byte)0xd2,(byte)0x48,(byte)0xcd,(byte)0xa0,
        (byte)0xe9,(byte)0x9f,(byte)0x33,(byte)0x42,
        (byte)0x0f,(byte)0x57,(byte)0x7e,(byte)0xe8,
        (byte)0xce,(byte)0x54,(byte)0xb6,(byte)0x70,
        (byte)0x80,(byte)0xa8/**/,(byte)0x0d,(byte)0x1e,
        (byte)0xc6,(byte)0x98,(byte)0x21,(byte)0xbc,
        (byte)0xb6,(byte)0xa8,(byte)0x83,(byte)0x93,
        (byte)0x96,(byte)0xf9,(byte)0x65,(byte)0x2b/**/,
        (byte)0x6f,(byte)0xf7,(byte)0x2a,(byte)0x70
    };
    private static byte[] md5Collision2 = {
        (byte)0xd1,(byte)0x31,(byte)0xdd,(byte)0x02,
        (byte)0xc5,(byte)0xe6,(byte)0xee,(byte)0xc4,
        (byte)0x69,(byte)0x3d,(byte)0x9a,(byte)0x06,
        (byte)0x98,(byte)0xaf,(byte)0xf9,(byte)0x5c,
        (byte)0x2f,(byte)0xca,(byte)0xb5,(byte)0x07 /**/,
        (byte)0x12,(byte)0x46,(byte)0x7e,(byte)0xab,
        (byte)0x40,(byte)0x04,(byte)0x58,(byte)0x3e,
        (byte)0xb8,(byte)0xfb,(byte)0x7f,(byte)0x89,
        (byte)0x55,(byte)0xad,(byte)0x34,(byte)0x06,
        (byte)0x09,(byte)0xf4,(byte)0xb3,(byte)0x02,
        (byte)0x83,(byte)0xe4,(byte)0x88,(byte)0x83,
        (byte)0x25,(byte)0xf1/**/,(byte)0x41,(byte)0x5a,
        (byte)0x08,(byte)0x51,(byte)0x25,(byte)0xe8,
        (byte)0xf7,(byte)0xcd,(byte)0xc9,(byte)0x9f,
        (byte)0xd9,(byte)0x1d,(byte)0xbd,(byte)0x72/**/,
        (byte)0x80,(byte)0x37,(byte)0x3c,(byte)0x5b,
        (byte)0xd8,(byte)0x82,(byte)0x3e,(byte)0x31,
        (byte)0x56,(byte)0x34,(byte)0x8f,(byte)0x5b,
        (byte)0xae,(byte)0x6d,(byte)0xac,(byte)0xd4,
        (byte)0x36,(byte)0xc9,(byte)0x19,(byte)0xc6,
        (byte)0xdd,(byte)0x53,(byte)0xe2,(byte)0x34/**/,
        (byte)0x87,(byte)0xda,(byte)0x03,(byte)0xfd,
        (byte)0x02,(byte)0x39,(byte)0x63,(byte)0x06,
        (byte)0xd2,(byte)0x48,(byte)0xcd,(byte)0xa0,
        (byte)0xe9,(byte)0x9f,(byte)0x33,(byte)0x42,
        (byte)0x0f,(byte)0x57,(byte)0x7e,(byte)0xe8,
        (byte)0xce,(byte)0x54,(byte)0xb6,(byte)0x70,
        (byte)0x80,(byte)0x28/**/,(byte)0x0d,(byte)0x1e,
        (byte)0xc6,(byte)0x98,(byte)0x21,(byte)0xbc,
        (byte)0xb6,(byte)0xa8,(byte)0x83,(byte)0x93,
        (byte)0x96,(byte)0xf9,(byte)0x65,(byte)0xab/**/,
        (byte)0x6f,(byte)0xf7,(byte)0x2a,(byte)0x70
    };
    private static byte[] md5CollisionMd5 = {
        (byte)0x79,(byte)0x05,(byte)0x40,(byte)0x25,
        (byte)0x25,(byte)0x5f,(byte)0xb1,(byte)0xa2,
        (byte)0x6e,(byte)0x4b,(byte)0xc4,(byte)0x22,
        (byte)0xae,(byte)0xf5,(byte)0x4e,(byte)0xb4
    };
    
    private byte[] byteFilledArray(int size, byte b) {
        byte[] bArray = new byte[size];
        Arrays.fill(bArray, b);
        return bArray;
    }
    
    @Test
    public void testContentCompareSimple() throws IOException {
        Date now = new Date(0);
        MockFileNode empty1 = new MockFileNode("1", new byte[0], now);
        MockFileNode empty2 = new MockFileNode("2", new byte[0], now);
        
        assertTrue("Empty content file nodes should compare true", empty1.compareDetails(empty2, FileNode.CONTENT_METHOD_CONTENT));
        assertTrue("Empty content file nodes should compare true", empty2.compareDetails(empty1, FileNode.CONTENT_METHOD_CONTENT));
        
        byte[] b1a = "a".getBytes("US-ASCII");
        byte[] b2a = "aa".getBytes("US-ASCII");
        byte[] b1b = "b".getBytes("US-ASCII");
        
        MockFileNode fNodeA1 = new MockFileNode("fnameA1", b1a, now);
        MockFileNode fNodeA2 = new MockFileNode("fnameA2", b2a, now);
        MockFileNode fNodeB1 = new MockFileNode("fnameB1", b1b, now);
        
        assertTrue("fNodeA1 content compares false with self", fNodeA1.compareDetails(fNodeA1, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse("fNodeA1 content compares true fNodeA2", fNodeA1.compareDetails(fNodeA2, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse("fNodeA2 content compares true fNodeA1", fNodeA2.compareDetails(fNodeA1, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse("fNodeA1 content compares true fNodeB1", fNodeA1.compareDetails(fNodeB1, FileNode.CONTENT_METHOD_CONTENT));
        
        assertFalse("fNodeA1 content compares true with empty1", fNodeA1.compareDetails(empty1, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse("empty1 content compares true with fNodeA1", empty1.compareDetails(fNodeA1, FileNode.CONTENT_METHOD_CONTENT));
    }
    
    @Test
    public void testContentCompareBadMethod() throws IOException {
        Date now = new Date(0);
        MockFileNode empty1 = new MockFileNode("1", new byte[0], now);
        MockFileNode empty2 = new MockFileNode("2", new byte[0], now);
        
        try {
            // cheat to get a bad compare method
            FileNode.ContentMethod badMethod = new  FileNode.ContentMethod();
            empty1.compareDetails(empty2, badMethod);
            fail("bad content method type should throw exception");
        } catch (IllegalArgumentException iae) {
            // should happen
        }
    }

    public void expectDifferentContent(FileNode fn1, FileNode fn2) throws IOException  {
        assertFalse(fn1.getName() + " should be different than " + fn2.getName(), fn1.compareDetails(fn2, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(fn2.getName() + " should be different than " + fn1.getName(), fn2.compareDetails(fn1, FileNode.CONTENT_METHOD_CONTENT));
    }
    
    public void expectDifferentTextContent(FileNode fn1, FileNode fn2) throws IOException  {
        assertFalse(fn1.getName() + " should be different than " + fn2.getName(), fn1.compareDetails(fn2, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertFalse(fn2.getName() + " should be different than " + fn1.getName(), fn2.compareDetails(fn1, FileNode.CONTENT_METHOD_CONTENT_TEXT));
    }
    
    public void expectSameTextContent(FileNode fn1, FileNode fn2) throws IOException  {
        assertTrue(fn1.getName() + " should be same content as " + fn2.getName(), fn1.compareDetails(fn2, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertTrue(fn2.getName() + " should be same content as " + fn1.getName(), fn2.compareDetails(fn1, FileNode.CONTENT_METHOD_CONTENT_TEXT));
    }

    public void expectDifferentCrc32Content(FileNode fn1, FileNode fn2) throws IOException  {
        assertFalse(fn1.getName() + " should be same content as " + fn2.getName(), fn1.compareDetails(fn2, FileNode.CONTENT_METHOD_CRC));
        assertFalse(fn2.getName() + " should be same content as " + fn1.getName(), fn2.compareDetails(fn1, FileNode.CONTENT_METHOD_CRC));
    }

    public void expectSameCrc32Content(FileNode fn1, FileNode fn2) throws IOException  {
        assertTrue(fn1.getName() + " should be same content as " + fn2.getName(), fn1.compareDetails(fn2, FileNode.CONTENT_METHOD_CRC));
        assertTrue(fn2.getName() + " should be same content as " + fn1.getName(), fn2.compareDetails(fn1, FileNode.CONTENT_METHOD_CRC));
    }
    
    @Test
    public void testContentCompareBoundary() throws IOException {
        Date now = new Date(0);
        int BOUNDARY_SIZE = 512;
        byte[] bLessA = byteFilledArray(BOUNDARY_SIZE-1, (byte)'a');
        byte[] bA = byteFilledArray(BOUNDARY_SIZE, (byte)'a');
        byte[] bPlussA = byteFilledArray(BOUNDARY_SIZE+1, (byte)'a');
        byte[] bA0m = byteFilledArray(BOUNDARY_SIZE, (byte)'a');
        bA0m[0] = (byte)'b';
        byte[] bALastM = byteFilledArray(BOUNDARY_SIZE, (byte)'a');
        bALastM[BOUNDARY_SIZE-1] = (byte)'b';
        byte[] bPlussALastM = byteFilledArray(BOUNDARY_SIZE+1, (byte)'a');
        bPlussALastM[BOUNDARY_SIZE] = (byte)'b';
        
        MockFileNode fNodeLessA = new MockFileNode("fNodeLessA", bLessA, now);
        MockFileNode fNodeA = new MockFileNode("fnameA", bA, now);
        MockFileNode fNodeAPlus = new MockFileNode("fNodeAPlus", bPlussA, now);
        MockFileNode fNodeA0M = new MockFileNode("fNodeA0M", bA0m, now);
        MockFileNode fNodeALastM = new MockFileNode("fNodeALastM", bALastM, now);
        MockFileNode fNodeAPlusLastM = new MockFileNode("fNodeAPlusLastM", bPlussALastM, now);
        
        expectDifferentContent(fNodeLessA, fNodeA);
        expectDifferentContent(fNodeA, fNodeLessA);
        expectDifferentContent(fNodeA, fNodeAPlus);
        expectDifferentContent(fNodeA, fNodeA0M);
        expectDifferentContent(fNodeA, fNodeALastM);
        expectDifferentContent(fNodeAPlus, fNodeAPlusLastM);
    }
    
    @Test
    public void testIsText() throws IOException {
        Date now = new Date(0);

        MockFileNode empty = new MockFileNode("empty", new byte[0], now);
        assertFalse("empty file should not be text", empty.isText());

        MockFileNode asc037 = new MockFileNode("asc037", "\037\037\037\037\037", now);
        assertFalse("asc037 file should be text", asc037.isText());
        
        MockFileNode spaces = new MockFileNode("spaces", "     ", now);
        assertTrue("spaces file should be text", spaces.isText());
        
        MockFileNode tilde = new MockFileNode("tilde", "~~~~~", now);
        assertTrue("tilde file should be text", tilde.isText());

        MockFileNode del = new MockFileNode("del", "\177\177\177\177\177", now);
        assertFalse("del file should not be text", del.isText());

        MockFileNode mfCRLF = 
            new MockFileNode("mfCRLF", "\r\nA\r\n", now);
        assertTrue("mfCRLF should be text", mfCRLF.isText());
        
        MockFileNode mfNull = 
            new MockFileNode("mfNull", "AAAAAAA\0BBBBB", now);
        assertFalse("mfNull should not be text", mfNull.isText());
        
        MockFileNode mfCRLFOneAccent = 
            new MockFileNode("mfCRLFOneAccent", "\r\nA\u00c0\r\n", now);
        assertTrue("mfCRLFOne should be text", mfCRLFOneAccent.isText());
        
        MockFileNode mfCRLFTwoAccent = 
            new MockFileNode("mfCRLFOneAccent", "\r\nA\u00c0\u00f0", now);
        assertFalse("mfCRLFTwoAccent should not be text", mfCRLFTwoAccent.isText());
        
        
    }
    @Test
    public void testContentCompareText() throws IOException {
        Date now = new Date(0);

        MockFileNode mfCRLF = 
            new MockFileNode("mfCRLF", "\r\nA\r\n", now);
        MockFileNode mfCR = 
            new MockFileNode("mfCR", "\rA\r", now);
        MockFileNode mfLF = 
            new MockFileNode("mfCR", "\nA\n", now);
        
        MockFileNode mfBCRLF = 
            new MockFileNode("mfBCRLF", "\n\rB\r\n", now);
        MockFileNode mfBCR = 
            new MockFileNode("mfCR", "\rB\r", now);
        MockFileNode mfBLF = 
            new MockFileNode("mfCR", "\nB\n", now);
        
        expectSameTextContent(mfCRLF, mfCR);
        expectSameTextContent(mfCRLF, mfLF);
        expectSameTextContent(mfLF, mfCR);
        
        expectDifferentTextContent(mfCRLF, mfBCRLF);
        expectDifferentTextContent(mfCRLF, mfBLF);
        expectDifferentTextContent(mfLF, mfBCR);
        
        
        // A\r
        // B\r
        // \rA
        // \rB
        MockFileNode mfAFirst = new MockFileNode("mfAFirst", "A\r", now);
        MockFileNode mfBFirst = new MockFileNode("mfBFirst", "B\r", now);
        expectDifferentTextContent(mfAFirst, mfBFirst);
        
        MockFileNode mfALast = new MockFileNode("mfALast", "\rA", now);
        MockFileNode mfBLast = new MockFileNode("mfBLast", "\rB", now);
        expectDifferentTextContent(mfALast, mfBLast);
        
        MockFileNode mfNoCZ = new MockFileNode("mfNoCZ", "AB", now);
        MockFileNode mfMidCZ = new MockFileNode("mfMidCZ", "A\032B" , now);
        MockFileNode mfEndCZ = new MockFileNode("mfEndCZ", "AB\032", now);
        
        expectSameTextContent(mfNoCZ, mfEndCZ);
        expectDifferentTextContent(mfNoCZ, mfMidCZ);
        
        
    }

    @Test
    public void testMd5() throws IOException {
        Date now = new Date(0);
        MockFileNode mfEmpty = new MockFileNode("mfEmpty1", new byte[] {}, now);
        byte[] mdEmpty = { 
            (byte)0xd4,(byte)0x1d,(byte)0x8c,(byte)0xd9,
            (byte)0x8f,(byte)0x00,(byte)0xb2,(byte)0x04,
            (byte)0xe9,(byte)0x80,(byte)0x09,(byte)0x98,
            (byte)0xec,(byte)0xf8,(byte)0x42,(byte)0x7e 
        };

        assertTrue( Arrays.equals(mdEmpty, mfEmpty.getMd5()) );
        
        MockFileNode mf1 = new MockFileNode("mf1", "1", now);        
        byte[] md1 = {
            (byte)0xc4,(byte)0xca,(byte)0x42,(byte)0x38,
            (byte)0xa0,(byte)0xb9,(byte)0x23,(byte)0x82,
            (byte)0x0d,(byte)0xcc,(byte)0x50,(byte)0x9a,
            (byte)0x6f,(byte)0x75,(byte)0x84,(byte)0x9b
        };
        assertTrue( Arrays.equals(md1, mf1.getMd5()) );
        
        byte[] b511 = new byte[511];
        Arrays.fill(b511, (byte)'a');
        MockFileNode mf511 = new MockFileNode( "mf511", b511, now );
        byte[] md511 = {
            (byte)0x3b,(byte)0xa3,(byte)0x48,(byte)0x5f,
            (byte)0x24,(byte)0x2a,(byte)0x58,(byte)0x59,
            (byte)0xf4,(byte)0x41,(byte)0x7c,(byte)0xcf,
            (byte)0x00,(byte)0x4c,(byte)0xd7,(byte)0x4c
        };
        assertTrue( Arrays.equals(md511, mf511.getMd5()) );
        
        byte[] b512 = new byte[512];
        Arrays.fill(b512, (byte)'b');
        MockFileNode mf512 = new MockFileNode( "mf512", b512, now );
        byte[] md512 = {
            (byte)0xba,(byte)0x4f,(byte)0x52,(byte)0xe4,
            (byte)0xd5,(byte)0xd9,(byte)0x7c,(byte)0x1b,
            (byte)0xcf,(byte)0xab,(byte)0x88,(byte)0xc6,
            (byte)0xaf,(byte)0xe2,(byte)0xcc,(byte)0xe6
        };
        
        assertTrue( Arrays.equals(md512, mf512.getMd5()) );
        
        byte[] b513 = new byte[513];
        Arrays.fill(b513, (byte)'c');
        MockFileNode mf513 = new MockFileNode( "mf513", b513, now );
        byte[] md513 = {
            (byte)0x0f,(byte)0xb7,(byte)0x9b,(byte)0xef,
            (byte)0x3e,(byte)0xbf,(byte)0xc9,(byte)0x3b,
            (byte)0x2e,(byte)0xde,(byte)0x7b,(byte)0x91,
            (byte)0x0b,(byte)0x6e,(byte)0x10,(byte)0x32,
        };
        
        assertTrue( Arrays.equals(md513, mf513.getMd5()) );
     
        MockFileNode mfcol1 = new MockFileNode( "mfcol1", md5Collision1, now );
        MockFileNode mfcol2 = new MockFileNode( "mfcol2", md5Collision1, now );
        assertTrue( Arrays.equals(md5CollisionMd5, mfcol1.getMd5()) );
        assertTrue( Arrays.equals(md5CollisionMd5, mfcol2.getMd5()) );
        
    }
    
    @Test
    public void testContentCompareCrc() throws IOException {
        Date now = new Date(0);
        MockFileNode mfEmpty1 = new MockFileNode("mfEmpty1", new byte[] {}, now);
        MockFileNode mfEmtpy2 = new MockFileNode("mfEmtpy2", new byte[] {}, now);
        expectSameCrc32Content(mfEmpty1, mfEmtpy2);

        // these sequences of 8 bytes both have crc32 of 2782600890
        MockFileNode mf8_2782600890_1 = 
            new MockFileNode("mf8_2782600890_1", new byte[] {-39, -8, 119, -23, 67, -61, -43, -65}, now);
        MockFileNode mf8_2782600890_2 = 
            new MockFileNode("mf8_2782600890_1", new byte[] {58, 8, 122, 111, 29, 6, -82, 127}, now);

        assertEquals(
            mf8_2782600890_1 + " should have size 8", 
            mf8_2782600890_1.getSize(),
            8
        );
        assertEquals(
            mf8_2782600890_1 + " should have crc 2782600890", 
            mf8_2782600890_1.getCrc(),
            2782600890L             
        );
        assertEquals(
            mf8_2782600890_2 + " should have size 8", 
            mf8_2782600890_2.getSize(),
            8
        );
        assertEquals(
            mf8_2782600890_2 + " should have crc 2782600890", 
            mf8_2782600890_2.getCrc(),
            2782600890L                             
        );
        
        expectSameCrc32Content(mf8_2782600890_1, mf8_2782600890_1);
        expectSameCrc32Content(mf8_2782600890_1, mf8_2782600890_2);
        expectSameCrc32Content(mf8_2782600890_2, mf8_2782600890_1);
     
        expectDifferentContent(mf8_2782600890_1, mf8_2782600890_2);
        
        MockFileNode mf8_4209318346 = 
            new MockFileNode("mf8_4209318346", new byte[] {116, -113, 114, 78, 117, -42, 83, -66}, now);
        MockFileNode mf9_4209318346 = 
            new MockFileNode("mf9_4209318346", new byte[] {23, 111, 22, 40, -102, 20, 69, -59, 63}, now);
        assertEquals(
            mf8_4209318346 + " should have size 8", 
            mf8_4209318346.getSize(),
            8
        );
        assertEquals(
            mf8_4209318346 + " should have crc 4209318346", 
            mf8_4209318346.getCrc(),
            4209318346L
        );
        assertEquals(
            mf9_4209318346 + " should have size 8", 
            mf9_4209318346.getSize(),
            9
        );
        assertEquals(
            mf9_4209318346 + " should have crc 4209318346", 
            mf9_4209318346.getCrc(),
            4209318346L
        );
        expectDifferentCrc32Content(mf8_4209318346, mf9_4209318346);
        expectDifferentCrc32Content(mf8_4209318346, mf9_4209318346);

    }

    @Test
    public void testContentCompareMd5() throws IOException {
        Date now = new Date(0);
        MockFileNode mfCol1 = new MockFileNode("mfCol1", md5Collision1, now);
        MockFileNode mfCol2 = new MockFileNode("mfCol2", md5Collision2, now);
        MockFileNode mfColforce1 = new MockFileNode("mfColForce1", "mfColForce1-content", now);
        MockFileNode mfColforce1a = new MockFileNode("mfColForce1a", "mfColForce1-content", now);
        MockFileNode mfColforce2 = new MockFileNode("mfColForce2", "mfColForce2-content", now);
        
        // md5 same size same content same
        assertTrue(
            mfCol1.compareDetails( mfCol1, FileNode.CONTENT_METHOD_MD5 )
        );
        
        // md5 same size same content different
        assertTrue(
            mfCol1.compareDetails( mfCol2, FileNode.CONTENT_METHOD_MD5 )
        );
        assertFalse(
            mfCol1.compareDetails( mfCol2, FileNode.CONTENT_METHOD_CONTENT )
        );

        // md5 same size different content different
        mfColforce1.forceMd5( md5CollisionMd5 );
        assertFalse(
            mfCol1.compareDetails( mfColforce1, FileNode.CONTENT_METHOD_MD5 )
        );
        assertFalse(
            mfCol1.compareDetails( mfColforce1, FileNode.CONTENT_METHOD_CONTENT )
        );
        
        // md5 different size same content different
        byte[] mdOther = Arrays.copyOf(md5CollisionMd5, md5CollisionMd5.length);
        mdOther[mdOther.length - 1] = 
            mdOther[mdOther.length - 1] == (byte)0 ? (byte)1: (byte)0; 
        mfColforce1.forceMd5( md5CollisionMd5 );
        mfColforce2.forceMd5( mdOther );
        assertFalse(
            mfColforce1.compareDetails( mfColforce2, FileNode.CONTENT_METHOD_MD5 )
        );
        
        // md5 different size different content different
        mfColforce1.forceMd5( mdOther );
        assertFalse(
            mfCol1.compareDetails( mfColforce1, FileNode.CONTENT_METHOD_MD5 )
        );

        // md5 different size same content same (cheat)
        mfColforce1.forceMd5( md5CollisionMd5 );
        mfColforce1a.forceMd5( mdOther );
        assertFalse(
            mfColforce1.compareDetails( mfColforce1a, FileNode.CONTENT_METHOD_MD5 )
        );
        assertTrue(
            mfColforce1.compareDetails( mfColforce1a, FileNode.CONTENT_METHOD_CONTENT )
        );
        
        
    }
    
    @Test
    public void testReadErrors() {
        Date now = new Date(0);
        {
            ReadFailMockFileNode mfEmptyFailImmediate = 
                new ReadFailMockFileNode("mfEmpty", "", now, 0);
            try {
                mfEmptyFailImmediate.isText();
                fail("expected exception not thrown");
            } catch (IOException ioe) { }
        }
        
        {
            ReadFailMockFileNode mfNonEmptyFailImmediate = 
                new ReadFailMockFileNode("mfNonEmpty", "x", now, 0);
            try {
                mfNonEmptyFailImmediate.isText();
                fail("expected exception not thrown");
            } catch (IOException ioe) {   }
        }
        
        {
            ReadFailMockFileNode mfNonEmptyFail = 
                new ReadFailMockFileNode("mfNonEmpty", "xxxx", now, 3);
            try {
                mfNonEmptyFail.isText();
                fail("expected exception not thrown");
            } catch (IOException ioe) { }
        }
        
        {
            ReadFailMockFileNode mfEmptyFailImmediate = 
                new ReadFailMockFileNode("mfEmpty", "", now, 0);
            MockFileNode mfEmpty = new MockFileNode("");
            try {
                mfEmptyFailImmediate.compareDetails(mfEmpty, FileNode.CONTENT_METHOD_CONTENT);
                fail("expected exception not thrown");
            } catch (IOException ioe) { }
        }

        {
            ReadFailMockFileNode mfFail = 
                new ReadFailMockFileNode("mfFail", "xxxxx", now, 5);
            MockFileNode mf = new MockFileNode("xxxxx");
            try {
                mfFail.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT);
                fail("expected exception not thrown");
            } catch (IOException ioe) { }
        }
        
        {
            ReadFailMockFileNode mfFail = 
                new ReadFailMockFileNode("mfFail", "xxxxx", now, 5);
            MockFileNode mf = new MockFileNode("xxxxx");
            try {
                mf.compareDetails(mfFail, FileNode.CONTENT_METHOD_CONTENT);
                fail("expected exception not thrown");
            } catch (IOException ioe) { }
        }
    }

    @Test
    public void testShortRead() throws IOException {
        Date now = new Date(0);
        
        ReadSlowMockFileNode mfTextAtEnd = 
            new ReadSlowMockFileNode("mfTextAtEnd", "\u00c0\r\nabc", now, 1);
        assertTrue("mfTextAtEnd should be text", mfTextAtEnd.isText());
        
        ReadSlowMockFileNode mfTextAtBegin = 
            new ReadSlowMockFileNode("mfTextAtBegin", "abc\u00c0\u00f0", now, 3);
        assertFalse("mfTextAtBegin should not be text", mfTextAtBegin.isText());
        
        ReadSlowMockFileNode mfSlow = 
            new ReadSlowMockFileNode("mfSlow", "xxxxxS", now, 1);
        MockFileNode mf = new MockFileNode("xxxxxS");
        assertTrue(mf.compareDetails(mfSlow, FileNode.CONTENT_METHOD_CONTENT));
        assertTrue(mf.compareDetails(mfSlow, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertTrue(mfSlow.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT));
        assertTrue(mfSlow.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        
        ReadSlowMockFileNode mfSlowDifferent = 
            new ReadSlowMockFileNode("mfSlowDifferent", "xxxxxT", now, 1);
        assertFalse(mf.compareDetails(mfSlowDifferent, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(mf.compareDetails(mfSlowDifferent, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertFalse(mfSlowDifferent.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(mfSlowDifferent.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        
        SizeHasChangedMockFileNode shortMf = new SizeHasChangedMockFileNode("shortMf", "xxxxx", "xxxxxS".length(), now);
        SizeHasChangedMockFileNode longtMf = new SizeHasChangedMockFileNode("longtMf", "xxxxxSx", "xxxxxS".length(), now);
        assertFalse(mf.compareDetails(shortMf, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(mf.compareDetails(longtMf, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(shortMf.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(longtMf.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT));
        assertFalse(mf.compareDetails(shortMf, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertFalse(mf.compareDetails(longtMf, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertFalse(shortMf.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        assertFalse(longtMf.compareDetails(mf, FileNode.CONTENT_METHOD_CONTENT_TEXT));
        
        
    }

    @Test
    public void testIsContentAccessible() {
        MockFileNode mf = new MockFileNode("OK");
        mf.setContentMethodCost(FileNode.COST_VERY_HARD);
        assertTrue(mf.isContentAccessible());
        mf.setContentMethodCost(FileNode.COST_IMPOSSIBLE);
        assertFalse(mf.isContentAccessible());
        mf.setContentMethodCost(FileNode.COST_IMPOSSIBLE / 2);
        assertTrue(mf.isContentAccessible());
        mf.setContentMethodCost(FileNode.COST_IMPOSSIBLE * 2);
        assertFalse(mf.isContentAccessible());
    }
    
    @Test
    public void testLinkTo() {
        Date now = new Date(0);

        MockFileNode mf = new MockFileNode("MF");
        assertEquals(FileType.REGFILE, mf.getFileType());
        assertEquals("", mf.getLinkTo());

        MockFileNode mfSymLink = new MockFileNode("SL", "other file", now);
        mfSymLink.setFileType(FileType.SYMLINK);
        assertEquals("other file", mfSymLink.getLinkTo());
        
        ReadFailMockFileNode mfSymlinkFail = 
            new ReadFailMockFileNode("mfSymlinkFile", "never", now, 0);
        mfSymlinkFail.setFileType(FileType.SYMLINK);
        assertEquals("?", mfSymlinkFail.getLinkTo());
        
    }
}
