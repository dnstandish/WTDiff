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

import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ComparisonDirNode;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.Leaf;

public class TestComparisonDirNode  {

        @Test
        public void testShortConstructor() {
            DirNode d1 = new DirNode( "d1", new ArrayList <Leaf> (), new ArrayList <DirNode> () );
            {
                ComparisonDirNode cDirNode = new ComparisonDirNode(d1, d1,true, true, false);
                assertEquals(d1.getName(), cDirNode.getName() );
                assertEquals(d1.getName(), cDirNode.getName1() );
                assertEquals(d1.getName(), cDirNode.getName2() );
                assertFalse( "expected d1 not missing 1", cDirNode.isMissing1() );
                assertFalse( "expected d1 not missing 2", cDirNode.isMissing2() );
                assertTrue( "expected d1 has both", cDirNode.haveBoth() );
                assertFalse( "expected 1 and 1 not same", cDirNode.areSame() );
                assertEquals(0, cDirNode.getDirs().size());
                assertEquals(0, cDirNode.getLeaves().size());
                assertNotSame( cDirNode.getDirs(), cDirNode.getLeaves() );
                cDirNode.setAreSame(true);
                assertTrue( "expected 1 and 1 same", cDirNode.areSame() );
                cDirNode.dump(" ", "");  // excercises this debug code but doesn't test it
            }
            {
                ComparisonDirNode cDirNode1Only = new ComparisonDirNode(d1, null, true, false, false);
                assertEquals(d1.getName(), cDirNode1Only.getName() );
                assertEquals(d1.getName(), cDirNode1Only.getName1() );
                assertEquals("", cDirNode1Only.getName2() );
                assertFalse( "expected d1 not missing 1", cDirNode1Only.isMissing1() );
                assertTrue( "expected d1 missing 2", cDirNode1Only.isMissing2() );
            }
            {
                ComparisonDirNode cDirNode2Only = new ComparisonDirNode(null, d1, false, true, false);
                assertEquals(d1.getName(), cDirNode2Only.getName() );
                assertEquals("", cDirNode2Only.getName1() );
                assertEquals(d1.getName(), cDirNode2Only.getName2() );
                assertTrue( "expected d1 missing 1", cDirNode2Only.isMissing1() );
                assertFalse( "expected d1 not missing 2", cDirNode2Only.isMissing2() );
            }
        }
        
        @Test
        public void testConstructorWithLists() {
            DirNode d1 = new DirNode( "d1", new ArrayList <Leaf> (), new ArrayList <DirNode> () );
            ArrayList <Leaf> lLeaves = new ArrayList <> ();
            ArrayList <DirNode> lDirs = new ArrayList <> ();
            {
                ComparisonDirNode cDirNode = new ComparisonDirNode(d1, d1,true, true, false, lLeaves, lDirs );
                assertFalse( "expected d1 not missing 1", cDirNode.isMissing1() );
                assertFalse( "expected d1 not missing 2", cDirNode.isMissing2() );
                assertTrue( "expected d1 has both", cDirNode.haveBoth() );
                assertFalse( "expected 1 and 1 not same", cDirNode.areSame() );
                assertSame( lDirs, cDirNode.getDirs() );
                assertSame( lLeaves, cDirNode.getLeaves() );
                assertEquals("*" + d1.toString(), cDirNode.toString());
            }
            {
                ComparisonDirNode cDirNode = new ComparisonDirNode(d1, null, true, false, false, lLeaves, lDirs );
                assertFalse( "expected d1 not missing 1", cDirNode.isMissing1() );
                assertTrue( "expected d1 missing 2", cDirNode.isMissing2() );
                assertFalse( "expected d1 does not have both", cDirNode.haveBoth() );
                assertFalse( "expected 1 and 1 not same", cDirNode.areSame() );
                assertEquals("-" + d1.toString(), cDirNode.toString());
            }
            {
                ComparisonDirNode cDirNode = new ComparisonDirNode(null, d1, false, true, false, lLeaves, lDirs );
                assertTrue( "expected d1 missing 1", cDirNode.isMissing1() );
                assertFalse( "expected d1 not missing 2", cDirNode.isMissing2() );
                assertFalse( "expected d1 does not have both", cDirNode.haveBoth() );
                assertFalse( "expected 1 and 1 not same", cDirNode.areSame() );
                assertEquals( "+" + d1.toString(), cDirNode.toString());
            }
            {
                ComparisonDirNode cDirNode = new ComparisonDirNode(d1, d1, true, true, true, lLeaves, lDirs );
                assertFalse( "expected d1 not missing 1", cDirNode.isMissing1() );
                assertFalse( "expected d1 not missing 2", cDirNode.isMissing2() );
                assertTrue( "expected d1 has both", cDirNode.haveBoth() );
                assertTrue( "expected 1 and 2 same", cDirNode.areSame() );
                assertEquals(d1.toString(), cDirNode.toString());
            }
        }
        
}
