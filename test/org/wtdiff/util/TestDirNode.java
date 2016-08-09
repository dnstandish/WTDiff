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
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.Node;

public class TestDirNode  {

        @Test
        public void testConstructor() {
            {
                ArrayList <Leaf> lLeaves = new ArrayList <> ();
                ArrayList <DirNode> lDirs = new ArrayList <> ();
                DirNode d1 = new DirNode( "d1", lLeaves, lDirs );
                assertEquals( "d1", d1.getName() );
                assertSame( lLeaves, d1.getLeaves() );
                assertSame( lDirs, d1.getDirs() );
                
                DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
                d2.addDir( d1 );
                List<DirNode> ld2 = d2.getDirs();
                assertEquals( 1, ld2.size() );
                assertSame(d1, ld2.get(0) );
                
                MockFileNode f = new MockFileNode("f");
                d2.addLeaf(f);
                List<Leaf> lf2 = d2.getLeaves();
                assertEquals( 1, lf2.size() );
                assertSame( f, lf2.get(0) );
                  
                d2.dump("-", ".");
                
            }
            {
                MockFileNode f = new MockFileNode("f");
                DirNode d = new DirNode( f );
                assertEquals( 1, d.getLeaves().size() );
                assertEquals( 0, d.getDirs().size() );                
            }
            {
                DirNode d1 = new DirNode( "d1", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
                DirNode d2 = new DirNode( d1 );
                assertEquals( 0, d2.getLeaves().size() );
                assertEquals( 1, d2.getDirs().size() );                
            }
        }
        
        @Test
        public void testSort() {
            DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            DirNode d3 = new DirNode( "d3", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            DirNode d1 = new DirNode( d3 );
            d1.addDir(d2);
            MockFileNode f1 = new MockFileNode("f1");
            MockFileNode f2 = new MockFileNode("f2");
            d1.addLeaf(f2);
            d1.addLeaf(f1);
            d2.addLeaf(f2);
            d2.addLeaf(f1);
            d3.addLeaf(f2);
            d3.addLeaf(f1);
            
            d1.sort();
            
            assertEquals(d2, d1.getDirs().get(0));
            assertEquals(d3, d1.getDirs().get(1));
            assertEquals(f1, d1.getLeaves().get(0));
            assertEquals(f2, d1.getLeaves().get(1));
            assertEquals(f1, d2.getLeaves().get(0));
            assertEquals(f2, d2.getLeaves().get(1));
            assertEquals(f1, d3.getLeaves().get(0));
            assertEquals(f2, d3.getLeaves().get(1));
        }
        
        @Test
        public void testPopulatePathByNames() {
            DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            DirNode d3 = new DirNode( "d3", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            DirNode d4 = new DirNode( "d4", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            DirNode d1 = new DirNode( d3 );
            d1.addDir(d2);
            d3.addDir(d4);
            MockFileNode f1a = new MockFileNode("f1a");
            MockFileNode f1b = new MockFileNode("f1b");
            MockFileNode f2a = new MockFileNode("f2a");
            MockFileNode f3a = new MockFileNode("f3a");
            MockFileNode f4a = new MockFileNode("f1a"); // note, smae name as f1a
            d1.addLeaf(f1a);
            d1.addLeaf(f1b);
            d2.addLeaf(f2a);
            d3.addLeaf(f3a);
            d4.addLeaf(f4a);
            { // find direct leaf OK
                List<String> path = Arrays.asList("f1a");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(f1a), nodes );
            }
            { // find other direct leaf OK
                List<String> path = Arrays.asList("f1b");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(f1b), nodes );
            }
            { // find direct dir OK
                List<String> path = Arrays.asList("d2");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(d2), nodes );
            }
            { // find leaf by path d2/f2a
                List<String> path = Arrays.asList("d2", "f2a");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(d2, f2a), nodes );
            }
            {  // f2a is not directly under d1 
                List<String> path = Arrays.asList("f2a");
                List<Node> nodes = new ArrayList<Node>();
                assertFalse( d1.populatePathByNames(path, nodes) );
            }
            { // find dir by path d3/d4
                List<String> path = Arrays.asList("d3", "d4");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(d3, d4), nodes );
            }
            { // find file by path d3/d4/f1a
                List<String> path = Arrays.asList("d3", "d4", "f1a");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(d3, d4, f4a), nodes );
            }
            { // find file by path d3/f3a
                List<String> path = Arrays.asList("d3", "f3a");
                List<Node> nodes = new ArrayList<Node>();
                assertTrue( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(d3, f3a), nodes );
            }
            { // empty path should return false
                List<String> path = Arrays.asList();
                List<Node> nodes = new ArrayList<Node>();
                assertFalse( d1.populatePathByNames(path, nodes) );
                assertEquals( Arrays.asList(), nodes );
            }
            
        }
}
