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
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ComparisonDirNode;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.FileNode;
import org.wtdiff.util.FileSystemNodeTreeBuilder;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.LeafComparisonResult;
import org.wtdiff.util.LoggingErrorHandler;
import org.wtdiff.util.TreeComparor;

public class TestTreeComparor  {

    private ArrayList<Leaf> emptyList = new ArrayList<>();
    
    @Test
    public void testEmptyTrees() throws IOException {
        DirNode d1 = new DirNode( "d1", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        
        TreeComparor tc = new TreeComparor(false, false);
        ComparisonDirNode cdn = tc.compare(d1, d2);
        assertTrue( "Empty trees not the same", cdn.areSame());
        assertFalse( "Empty tree missing 1", cdn.isMissing1());
        assertFalse( "Empty tree missing 2", cdn.isMissing2());
    }
    
    @Test
    public void testNullTrees() throws IOException {
        DirNode d = new DirNode( "d", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        
        TreeComparor tc = new TreeComparor(false, false);
        ComparisonDirNode cdn = tc.compare(d, null);
        assertFalse( "null tree is the same", cdn.areSame());
        assertFalse( "tree not missing 1", cdn.isMissing1());
        assertTrue( "null tree not missing 2", cdn.isMissing2());
        cdn = tc.compare(null, d);
        assertFalse( "null tree is the same", cdn.areSame());
        assertTrue( "tree not missing 1", cdn.isMissing1());
        assertFalse( "null tree missing 2", cdn.isMissing2());
        cdn = tc.compare(null, null);
        assertFalse( "null null trees same", cdn.areSame());
        assertTrue( "tree not missing 1", cdn.isMissing1());
        assertTrue( "null tree not missing 2", cdn.isMissing2());
    }
    
    @Test
    public void testTrivialTrees() throws IOException {
        MockFileNode f = new MockFileNode("a", "AAAA", new Date());
        ArrayList<Leaf> a = new ArrayList<>();
        a.add(f);
        DirNode d1 = new DirNode( "d1", a, new ArrayList<DirNode>() );
        DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        DirNode d3 = new DirNode( "d3", a, new ArrayList<DirNode>() );
        
        TreeComparor tc = new TreeComparor(false, false);
        {
            ComparisonDirNode cdn = tc.compare(d1, d2);
            assertFalse( "Empty tree d2 not different from non-empty tree d1", cdn.areSame());
            assertFalse( "Tree d1 missing top", cdn.isMissing1());
            assertFalse( "Empty tree d2 missing top", cdn.isMissing2());
            
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("d1,d2 compare should have exactly one leaf",
                    1, leaves.size() );
            LeafComparisonResult cr = (LeafComparisonResult) leaves.get(0);
            assertEquals("leaf name not a'", "a", cr.getName() );
            assertFalse("leaf is same even though missing from d2",cr.areSame() );
            assertFalse("leaf is missing from d1", cr.isMissing1());
            assertTrue("leaf is not missing from d2", cr.isMissing2());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("d1,d2 compare should have no dirs",
                    0, dirs.size() );
            
        }
        {
            ComparisonDirNode cdn = tc.compare(d2, d3);
            assertFalse( "Empty tree d2 not different from non-empty tree d1", cdn.areSame());
            assertFalse( "Tree d3 missing top", cdn.isMissing2());
            assertFalse( "Empty tree d2 missing top", cdn.isMissing1());
            
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("d2,d3 compare should have exactly one leaf",
                    1, leaves.size() );
            LeafComparisonResult cr = (LeafComparisonResult) leaves.get(0);
            assertEquals("leaf name not a'", "a", cr.getName() );
            assertFalse("leaf is same even though missing from d2",cr.areSame() );
            assertFalse("leaf is missing from d3", cr.isMissing2());
            assertTrue("leaf is not missing from d2", cr.isMissing1());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("d2,d3 compare should have no dirs",
                    0, dirs.size() );
            
        }        
        {
            ComparisonDirNode cdn = tc.compare(d1, d3);
            assertTrue( "d1 and d3 are not the same", cdn.areSame());
            assertFalse( "Tree d1 missing top", cdn.isMissing1());
            assertFalse( "Tree d3 missing top", cdn.isMissing2());
            
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("d1,d3 compare should have exactly one leaf",
                    1, leaves.size() );
            LeafComparisonResult cr = (LeafComparisonResult) leaves.get(0);
            assertEquals("leaf name not a'", "a", cr.getName() );
            assertTrue("leaf should be the same",cr.areSame() );
            assertFalse("leaf is missing from d1", cr.isMissing1());
            assertFalse("leaf is missing from d3", cr.isMissing2());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("d1,d3 compare should have no dirs",
                    0, dirs.size() );
            
        }

    }
    
    @Test
    public void testSimpleTrees() throws IOException {
        MockFileNode af = new MockFileNode("a", "AAAA", new Date());
        ArrayList<Leaf> aList = new ArrayList<>();
        aList.add(af);
        
        MockFileNode apf = new MockFileNode("a", "AAAAA", new Date());
        ArrayList<Leaf> apList = new ArrayList<>();
        apList.add(apf);
        
        MockFileNode bf = new MockFileNode("b", "AAAA", new Date());
        ArrayList<Leaf> bList = new ArrayList<>();
        bList.add(bf);
        
        
        
        DirNode d2 = new DirNode( "2", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        DirNode d3 = new DirNode( "3", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        DirNode d2a = new DirNode( "2", aList, new ArrayList<DirNode>() );
        DirNode d2ap = new DirNode( "2", apList, new ArrayList<DirNode>() );
        DirNode d2b = new DirNode( "2", bList, new ArrayList<DirNode>() );
        
        DirNode d1 = new DirNode( "1", new ArrayList<Leaf>(), new ArrayList<DirNode>() );
        DirNode d12;
        {
            ArrayList<DirNode> dirs = new ArrayList<>();
            dirs.add(d2);
            d12 = new DirNode("1", emptyList, dirs);
        }
        DirNode d13;
        {
            ArrayList<DirNode> dirs = new ArrayList<>();
            dirs.add(d3);
            d13 = new DirNode("1", emptyList, dirs);
        }
        DirNode d12a;
        {
            ArrayList<DirNode> dirs = new ArrayList<>();
            dirs.add(d2a);
            d12a = new DirNode("1", emptyList, dirs);
        }
        DirNode d12ap;
        {
            ArrayList<DirNode> dirs = new ArrayList<>();
            dirs.add(d2ap);
            d12ap = new DirNode("1", emptyList, dirs);
        }
        DirNode d12b;
        {
            ArrayList<DirNode> dirs = new ArrayList<>();
            dirs.add(d2b);
            d12b = new DirNode("1", emptyList, dirs);
        }

        TreeComparor tc = new TreeComparor(false, false);
        // d1 and d12 have no files but only d12 has a subdir
        {
            ComparisonDirNode cdn = tc.compare(d1, d12);        
            assertFalse("d1 should have top", cdn.isMissing1());
            assertFalse("d12 should have top", cdn.isMissing2());
            assertFalse("d1 and d12 should be different", cdn.areSame());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals( "should be no comparison leaves", 0, leaves.size() );
            List<DirNode> dirs = cdn.getDirs();
            assertEquals( "should be one subdir", 1, dirs.size() );
            ComparisonDirNode cdn2 = (ComparisonDirNode) dirs.get(0);
            assertTrue("d1 should not have subdir 2", cdn2.isMissing1() );
            assertFalse( "d12 should have a subdir 2", cdn2.isMissing2());
            assertFalse("subdir 2 should not be same since doesn't exist in d1", cdn2.areSame() );
        }
        {   // try with d12, d1 - i.e. reversed
            ComparisonDirNode cdn = tc.compare(d12, d1);        
            assertFalse("d12 should have top", cdn.isMissing1());
            assertFalse("d1 should have top", cdn.isMissing2());
            assertFalse("d12 and d1 should be different", cdn.areSame());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals( "should be no comparison leaves", 0, leaves.size() );
            List<DirNode> dirs = cdn.getDirs();
            assertEquals( "should be one subdir", 1, dirs.size() );
            ComparisonDirNode cdn2 = (ComparisonDirNode) dirs.get(0);
            assertFalse("d12 should have subdir 2", cdn2.isMissing1() );
            assertTrue( "d1 should not have a subdir 2", cdn2.isMissing2());
            assertFalse("subdir 2 should not be same since doesn't exist in d1", cdn2.areSame() );
        }
        // d12 and d13 have no files but but different subdirs
        {
            ComparisonDirNode cdn = tc.compare(d12, d13);    
            assertFalse("d12 and d13 should be different", cdn.areSame());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals( "should be no comparison leaves", 0, leaves.size() );
            List<DirNode> dirs = cdn.getDirs();
            assertEquals( "should be two subdir", 2, dirs.size() );
            ComparisonDirNode cdna = (ComparisonDirNode) dirs.get(0);
            ComparisonDirNode cdnb = (ComparisonDirNode) dirs.get(1);
            ComparisonDirNode cdn2;
            ComparisonDirNode cdn3;
            if (cdna.getName().equals("2")) {
                cdn2 = cdna;
                cdn3 = cdnb;
            } else {
                assertEquals("unexpected name", "3", cdna.getName());
                cdn3 = cdna;
                cdn2 = cdnb;
            }
            assertFalse("d12 should have subdir 2", cdn2.isMissing1() );
            assertTrue( "d13 should not have a subdir 2", cdn2.isMissing2());
            assertFalse("subdir 2 should not be same since doesn't exist in d13", cdn2.areSame() );
            
            assertTrue("d12 should not have subdir 3", cdn3.isMissing1() );
            assertFalse( "d13 should have a subdir 3", cdn3.isMissing2());
            assertFalse("subdir 3 should not be same since doesn't exist in d12", cdn3.areSame() );
        }
        // d12a d12ap same except for contennt of "a"
        {
            ComparisonDirNode cdn = tc.compare(d12a, d12ap);
            List<DirNode> dirs1 = cdn.getDirs();
            assertEquals( "should be two subdir", 1, dirs1.size() );
            ComparisonDirNode cdn2 = (ComparisonDirNode) dirs1.get(0);
            
            assertEquals( "2 should not have subdirs", 0, cdn2.getDirs().size() );
            List<Leaf> leaves2 = cdn2.getLeaves();;
            assertEquals( "2 should have 1 leaf", 1, leaves2.size() );

            LeafComparisonResult aResult = (LeafComparisonResult) leaves2.get(0);
            assertEquals( "expected namme a", "a", aResult.getName() );
            assertFalse( "d12a should have a", aResult.isMissing1() );
            assertFalse( "d12a should have a", aResult.isMissing2() );
            assertFalse( "a should not be the samme", aResult.areSame() );
        }
        // d12a d12b have different leaves
        {
            ComparisonDirNode cdn = tc.compare(d12a, d12b);
            List<DirNode> dirs1 = cdn.getDirs();
            assertEquals( "should be two subdir", 1, dirs1.size() );
            ComparisonDirNode cdn2 = (ComparisonDirNode) dirs1.get(0);
            
            assertEquals( "2 should not have subdirs", 0, cdn2.getDirs().size() );
            List<Leaf> leaves2 = cdn2.getLeaves();;
            assertEquals( "2 should have 2 leaves", 2, leaves2.size() );
    
            LeafComparisonResult aResult = (LeafComparisonResult) leaves2.get(0);
            LeafComparisonResult bResult = (LeafComparisonResult) leaves2.get(1);
            if ( aResult.getName().equals("b") ) {
                bResult = (LeafComparisonResult) leaves2.get(0);
                aResult = (LeafComparisonResult) leaves2.get(1);
            }
            
            assertFalse( "d12a should have a", aResult.isMissing1() );
            assertTrue( "d12b should not have a", aResult.isMissing2() );
            
            assertTrue( "d12a should not have b", bResult.isMissing1() );
            assertFalse( "d12b should have b", bResult.isMissing2() );
        }
    }
    
    @Test
    public void testSimpleTreesIgnoreCase() throws IOException {
        MockFileNode af = new MockFileNode("a", "AAAA", new Date());
        ArrayList<Leaf> aList = new ArrayList<>();
        aList.add(af);
        
        MockFileNode apf = new MockFileNode("a", "AAAAA", new Date());
        ArrayList<Leaf> apList = new ArrayList<>();
        apList.add(apf);
        
        MockFileNode Af = new MockFileNode("A", "AAAA", new Date());
        ArrayList<Leaf> AList = new ArrayList<>();
        AList.add(Af);

        // d/a
        // D/a
        // D/A
        // d/a*
        DirNode dda = new DirNode( "d", aList, new ArrayList<DirNode>() );
        DirNode dDa = new DirNode( "D", aList, new ArrayList<DirNode>() );
        DirNode dDA = new DirNode( "D", AList, new ArrayList<DirNode>() );
        DirNode ddam = new DirNode( "d", apList, new ArrayList<DirNode>() );
        
        TreeComparor tc = new TreeComparor(true, false);
        {
            ComparisonDirNode cdn = tc.compare(dda, dDa);
            assertTrue("d/a should be same as D/a", cdn.areSame() );            
        }
        {
            ComparisonDirNode cdn = tc.compare(dda, dDA);
            assertTrue("d/a should be same as D/A", cdn.areSame() );            
        }
        {
            ComparisonDirNode cdn = tc.compare(dDa, dDA);
            assertTrue("D/a should be same as D/A", cdn.areSame() );            
        }
        
        {
            ComparisonDirNode cdn = tc.compare(dda, ddam);
            assertFalse("d/a should not be same as d/a*", cdn.areSame() );                        
        }
        {
            ComparisonDirNode cdn = tc.compare(dDa, ddam);
            assertFalse("D/a should not be same as d/a*", cdn.areSame() );                        
        }
        {
            ComparisonDirNode cdn = tc.compare(dDA, ddam);
            assertFalse("D/A should not be same as d/a*", cdn.areSame() );                        
        }

        TreeComparor tc2 = new TreeComparor(false, false);
        {
            ComparisonDirNode cdn = tc2.compare(dDa, dDA);
            assertFalse("D/a should not be same as D/A", cdn.areSame() );            
        }
    }
    @Test
    public void testFilesDuplicateException() throws IOException {
        MockFileNode aaf = new MockFileNode("aa", "aa", new Date());

        DirNode daa = new DirNode(aaf);
        List<Leaf> llaaaa = new ArrayList<Leaf>(2);
        llaaaa.add(aaf);
        llaaaa.add(aaf);
        DirNode daaaa = new DirNode("daaaa", llaaaa, new ArrayList<DirNode>(0));
        try {
            TreeComparor tc = new TreeComparor(false, false);
            tc.compare(daaaa, daa);
            fail("Comparison should throw exception if dir has two members with exact same name");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
        try {
            TreeComparor tc = new TreeComparor(true, false);
            tc.compare(daaaa, daa);
            fail("Comparison should throw exception if dir has two members with exact same name");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
    }
    @Test
    public void testFilesDegenerateTreesIgnoreCase() throws IOException {
        MockFileNode aaf = new MockFileNode("aa", "aa", new Date());
        MockFileNode Aaf = new MockFileNode("Aa", "Aa", new Date());
        MockFileNode aAf = new MockFileNode("aA", "aA", new Date());
        MockFileNode AAf = new MockFileNode("AA", "AA", new Date());
        
        DirNode daa = new DirNode(aaf);
        DirNode dAa = new DirNode(Aaf);
        List<Leaf> llaaAa = new ArrayList<Leaf>(2);
        llaaAa.add(aaf);
        llaaAa.add(Aaf);
        DirNode daaAa = new DirNode("daaAa", llaaAa, new ArrayList<DirNode>(0));
        List<Leaf> llaaaA = new ArrayList<Leaf>(2);
        llaaaA.add(aaf);
        llaaaA.add(aAf);
        DirNode daaaA = new DirNode("daaaA", llaaaA, new ArrayList<DirNode>(0));
        List<Leaf> llaaAA = new ArrayList<Leaf>(2);
        llaaAA.add(aaf);
        llaaAA.add(AAf);
        DirNode daaAA = new DirNode("daaAA", llaaAA, new ArrayList<DirNode>(0));
        List<Leaf> llAaAA = new ArrayList<Leaf>(2);
        llAaAA.add(Aaf);
        llAaAA.add(AAf);
        DirNode dAaAA = new DirNode("daaAA", llAaAA, new ArrayList<DirNode>(0));
        List<Leaf> llaaAaAA = new ArrayList<Leaf>(3);
        llaaAaAA.add(aaf);
        llaaAaAA.add(Aaf);
        llaaAaAA.add(AAf);
        DirNode daaAaAA = new DirNode("daaAaAA", llaaAaAA, new ArrayList<DirNode>(0));

        TreeComparor tc = new TreeComparor(true, false);
        // aa  aa ... -Aa
        // Aa          aa
        {
            ComparisonDirNode cdn = tc.compare(daaAa, daa);
            cdn.dump(".","");
            assertFalse("daaAa,daa should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertFalse("Aa should not be missing from 1", lcr0.isMissing1());
            assertTrue("Aa should be missing from 2", lcr0.isMissing2());
            assertTrue("aa should be the same", lcr1.areSame());
        }
        // aa  aa  ... +Aa
        //     Aa       aa
        {
            ComparisonDirNode cdn = tc.compare(daa, daaAa);
            cdn.dump(".","");
            assertFalse("daa,daaAa should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertTrue("Aa should be missing from 1", lcr0.isMissing1());
            assertFalse("Aa should not be missing from 2", lcr0.isMissing2());
            assertTrue("aa should be the same", lcr1.areSame());
        }
        // aa  Aa ... -aa
        // Aa          AA
        {
            ComparisonDirNode cdn = tc.compare(daaAa, dAa);
            cdn.dump(".","");
            assertFalse("daaAa,dAa should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertFalse("aa should not be missing from 1", lcr1.isMissing1());
            assertTrue("aa should be missing from 2", lcr1.isMissing2());
            assertTrue("Aa should be the same", lcr0.areSame());
        }
        // if no exact matches then ignore case comparison should be determinate
        // AA  aa ... *AA
        // Aa         -Aa
        {
            ComparisonDirNode cdn = tc.compare(dAaAA, daa);
            cdn.dump(".","");
            assertFalse("dAaAA,daa should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "AA");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "Aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertFalse("Aa should not be missing from 1", lcr1.isMissing1());
            assertTrue("Aa should be missing from 2", lcr1.isMissing2());
            assertFalse("AA should not be the same", lcr0.areSame());
        }
        // if no exact matches then ignore case comparison should be determinate
        // aa  Aa ... -AA
        // AA         *aa
        {
            ComparisonDirNode cdn = tc.compare(daaAA, dAa);
            cdn.dump(".","");
            assertFalse("daaAA,dAa should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "AA");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertFalse("AA should not be missing from 1", lcr0.isMissing1());
            assertTrue("AA should be missing from 2", lcr0.isMissing2());
            assertFalse("aa should not be the same", lcr1.areSame());
        }
        // aa  aa ... Aa
        // Aa  Aa     aa
        {
            ComparisonDirNode cdn = tc.compare(daaAa, daaAa);
            cdn.dump(".","");
            assertTrue("daaAa,daaAa should be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertTrue("aa should be the same", lcr1.areSame());
            assertTrue("Aa should be the same", lcr0.areSame());
        }
        // aa  aa ... *Aa
        // Aa  aA      aa
        {
            ComparisonDirNode cdn = tc.compare(daaAa, daaaA);
            cdn.dump(".","");
            assertFalse("daaAa,daaaA should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 2, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            assertTrue("aa should be the same", lcr1.areSame());
            assertFalse("Aa should not be the same", lcr0.areSame());
        }
        // aa  aa ... Aa
        // Aa  Aa     aa
        // AA        -AA
        {
            ComparisonDirNode cdn = tc.compare(daaAaAA, daaAa);
            cdn.dump(".","");
            assertFalse("daaAaAA,daaAa should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 3, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "AA");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(2).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            LeafComparisonResult lcr2 = (LeafComparisonResult)leaves.get(2);
            assertTrue("AA should be missing same from 2", lcr0.isMissing2());
            assertTrue("Aa should be the same", lcr1.areSame());
            assertTrue("aa should be the same", lcr2.areSame());
        }
        // aa  aa ... AA
        // Aa  AA    -Aa
        // AA         aa
        {
            ComparisonDirNode cdn = tc.compare(daaAaAA, daaAA);
            cdn.dump(".","");
            assertFalse("daaAaAA,daaAA should not be same",cdn.areSame());
            assertEquals("comparison should not have any dirNodes", 0, cdn.getDirs().size());
            List<Leaf> leaves = cdn.getLeaves();
            assertEquals("comparison Leaves", 3, leaves.size());
            assertEquals("comparison Leaves", leaves.get(0).getName(), "AA");
            assertEquals("comparison Leaves", leaves.get(1).getName(), "Aa");
            assertEquals("comparison Leaves", leaves.get(2).getName(), "aa");
            LeafComparisonResult lcr0 = (LeafComparisonResult)leaves.get(0);
            LeafComparisonResult lcr1 = (LeafComparisonResult)leaves.get(1);
            LeafComparisonResult lcr2 = (LeafComparisonResult)leaves.get(2);
            assertTrue("AA should be the same", lcr0.areSame());
            assertTrue("Aa should be missing from 2", lcr1.isMissing2());
            assertTrue("aa should be the same", lcr2.areSame());
        }
        
    }
    private DirNode createDirnodeWIthMatchingFile(String name) {
        MockFileNode f = new MockFileNode(name, name, new Date());
        List<Leaf> l = new ArrayList<Leaf>(1);
        l.add(f);
        return new DirNode(name, l, new ArrayList<DirNode>(0));        
    }
    
    @Test
    public void testDirsDuplicateException() throws IOException {
        DirNode aa = createDirnodeWIthMatchingFile("aa");
        
        DirNode daa = new DirNode(aa);
        List<DirNode> laaaa = new ArrayList<>(2);
        laaaa.add(aa);
        laaaa.add(aa);
        DirNode daaaa = new DirNode("daaaa", new ArrayList<Leaf>(0), laaaa);

        try {
            TreeComparor tc = new TreeComparor(false, false);
            tc.compare(daaaa, daa);
            fail("Comparison should throw exception if dir has two members with exact same name");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
        try {
            TreeComparor tc = new TreeComparor(true, false);
            tc.compare(daaaa, daa);
            fail("Comparison should throw exception if dir has two members with exact same name");
        } catch (IllegalArgumentException iae) {
            // this is supposed to happen
        }
    }
    
    @Test
    public void testDirsDegenerateTreesIgnoreCase() throws IOException {
        DirNode aa = createDirnodeWIthMatchingFile("aa");
        DirNode Aa = createDirnodeWIthMatchingFile("Aa");
        DirNode aA = createDirnodeWIthMatchingFile("aA");
        DirNode AA = createDirnodeWIthMatchingFile("AA");
        
        DirNode daa = new DirNode(aa);
        DirNode dAa = new DirNode(Aa);
        List<DirNode> laaAa = new ArrayList<>(2);
        laaAa.add(aa);
        laaAa.add(Aa);
        DirNode daaAa = new DirNode("daaAa", new ArrayList<Leaf>(0), laaAa);
        List<DirNode> laaaA = new ArrayList<>(2);
        laaaA.add(aa);
        laaaA.add(aA);
        DirNode daaaA = new DirNode("daaaA", new ArrayList<Leaf>(0), laaaA);
        List<DirNode> laaAA = new ArrayList<>(2);
        laaAA.add(aa);
        laaAA.add(AA);
        DirNode daaAA = new DirNode("daaAA", new ArrayList<Leaf>(0), laaAA);
        List<DirNode> lAaAA = new ArrayList<>(2);
        lAaAA.add(Aa);
        lAaAA.add(AA);
        DirNode dAaAA = new DirNode("dAaAA", new ArrayList<Leaf>(0), lAaAA);
        List<DirNode> laaAaAA = new ArrayList<>(3);
        laaAaAA.add(aa);
        laaAaAA.add(Aa);
        laaAaAA.add(AA);
        DirNode daaAaAA = new DirNode("daaAaAA", new ArrayList<Leaf>(0), laaAaAA);

        TreeComparor tc = new TreeComparor(true, false);
        // aa  aa ... -Aa
        // Aa          aa
        {
            ComparisonDirNode cdn = tc.compare(daaAa, daa);
            cdn.dump(".","");
            assertFalse("daaAa,daa should not be same",cdn.areSame());
            assertEquals("comparison should have 2 dirs", 2, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(1).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertFalse("Aa should not be missing from 1", cr0.isMissing1());
            assertTrue("Aa should be missing from 2", cr0.isMissing2());
            assertTrue("aa should be the same", cr1.areSame());
        }
        // aa  aa  ... +Aa
        //     Aa       aa
        {
            ComparisonDirNode cdn = tc.compare(daa, daaAa);
            cdn.dump(".","");
            assertFalse("daa,daaAa should not be same",cdn.areSame());
            assertEquals("comparison should not have 2 dirs", 2, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(1).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertTrue("Aa should be missing from 1", cr0.isMissing1());
            assertFalse("Aa should not be missing from 2", cr0.isMissing2());
            assertTrue("aa should be the same", cr1.areSame());
        }
        // aa  Aa ... -aa
        // Aa          AA
        {
            ComparisonDirNode cdn = tc.compare(daaAa, dAa);
            cdn.dump(".","");
            assertFalse("daaAa,dAa should not be same",cdn.areSame());
            assertEquals("comparison should have 2 dirs", 2, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(1).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertFalse("aa should not be missing from 1", cr1.isMissing1());
            assertTrue("aa should be missing from 2", cr1.isMissing2());
            assertTrue("Aa should be the same", cr0.areSame());
        }
        // if no exact matches then ignore case comparison should be determinate
        // AA  aa ... *AA
        // Aa         -Aa
        {
            ComparisonDirNode cdn = tc.compare(dAaAA, daa);
            cdn.dump(".","");
            assertFalse("dAaAa,daa should not be same",cdn.areSame());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            assertEquals("comparison should have 2 dirs", 2, cdn.getDirs().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "AA");
            assertEquals("comparison dirs", dirs.get(1).getName(), "Aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertFalse("Aa should not be missing from 1", cr1.isMissing1());
            assertTrue("Aa should be missing from 2", cr1.isMissing2());
            assertFalse("AA should not be the same", cr0.areSame());
        }
        // if no exact matches then ignore case comparison should be determinate
        // aa  Aa ... -AA
        // AA         *aa
        {
            ComparisonDirNode cdn = tc.compare(daaAA, dAa);
            cdn.dump(".","");
            assertFalse("daaAA,dAa should not be same",cdn.areSame());
            assertEquals("comparison should have 2 dirs", 2, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "AA");
            assertEquals("comparison dirs", dirs.get(1).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertFalse("AA should not be missing from 1", cr0.isMissing1());
            assertTrue("AA should be missing from 2", cr0.isMissing2());
            assertFalse("aa should not be the same", cr1.areSame());
        }

        // aa  aa ... Aa
        // Aa  Aa     aa
        {
            ComparisonDirNode cdn = tc.compare(daaAa, daaAa);
            cdn.dump(".","");
            assertTrue("daaAa,daaAa should be same",cdn.areSame());
            assertEquals("comparison should not have 2 dirs", 2, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(1).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertTrue("aa should be the same", cr1.areSame());
            assertTrue("Aa should be the same", cr0.areSame());
        }
        // aa  aa ... *Aa
        // Aa  aA      aa
        {
            ComparisonDirNode cdn = tc.compare(daaAa, daaaA);
            cdn.dump(".","");
            assertFalse("daaAa,daaaA should not be same",cdn.areSame());
            assertEquals("comparison should have 2 dirs", 2, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(1).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            assertTrue("aa should be the same", cr1.areSame());
            assertFalse("Aa should not be the same", cr0.areSame());
        }
        // aa  aa ... Aa
        // Aa  Aa     aa
        // AA        -AA
        {
            ComparisonDirNode cdn = tc.compare(daaAaAA, daaAa);
            cdn.dump(".","");
            assertFalse("daaAaAA,daaAa should not be same",cdn.areSame());
            assertEquals("comparison should have 3 dirs", 3, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "AA");
            assertEquals("comparison dirs", dirs.get(1).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(2).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            ComparisonDirNode cr2 = (ComparisonDirNode)dirs.get(2);

            assertTrue("AA should be missing same from 2", cr0.isMissing2());
            assertTrue("Aa should be the same", cr1.areSame());
            assertTrue("aa should be the same", cr2.areSame());
        }
        // aa  aa ... AA
        // Aa  AA    -Aa
        // AA         aa
        {
            ComparisonDirNode cdn = tc.compare(daaAaAA, daaAA);
            cdn.dump(".","");
            assertFalse("daaAaAA,daaAA should not be same",cdn.areSame());
            assertEquals("comparison should have 3 dirs", 3, cdn.getDirs().size());
            assertEquals("comparison 0 Leaves", 0, cdn.getLeaves().size());
            List<DirNode> dirs = cdn.getDirs();
            assertEquals("comparison dirs", dirs.get(0).getName(), "AA");
            assertEquals("comparison dirs", dirs.get(1).getName(), "Aa");
            assertEquals("comparison dirs", dirs.get(2).getName(), "aa");
            ComparisonDirNode cr0 = (ComparisonDirNode)dirs.get(0);
            ComparisonDirNode cr1 = (ComparisonDirNode)dirs.get(1);
            ComparisonDirNode cr2 = (ComparisonDirNode)dirs.get(2);
            
            assertTrue("AA should be the same", cr0.areSame());
            assertTrue("Aa should be missing from 2", cr1.isMissing2());
            assertTrue("aa should be the same", cr2.areSame());
        }
        
    }
    
    @Test
    public void testSimpleTreesText() throws IOException {
        MockFileNode afCRLF = new MockFileNode("a", "AAAA\r\n", new Date());
        
        MockFileNode apfCRLF = new MockFileNode("a", "AAAAA\r\n", new Date());
        
        MockFileNode afLF = new MockFileNode("a", "AAAA\n", new Date());


        DirNode daCRLF = new DirNode( afCRLF  );
        DirNode dapCRLF = new DirNode( apfCRLF  );
        DirNode daLF = new DirNode( afLF  );
        DirNode binAfLF = new DirNode( new MockFileNode("a", "\0AAAA\n", new Date()) );
        DirNode binAfCRLF = new DirNode( new MockFileNode("a", "\0AAAA\r\n", new Date()) );
        
        TreeComparor tcText = new TreeComparor(false, true);
        {
            ComparisonDirNode cdn = tcText.compare(daCRLF, daLF);
            assertTrue("a CRLF LF should be same", cdn.areSame() );            
        }
        {
            ComparisonDirNode cdn = tcText.compare(daCRLF, dapCRLF);
            assertFalse("a should not be same as a*", cdn.areSame() );                        
        }
        {
            ComparisonDirNode cdn = tcText.compare(binAfLF, binAfCRLF);
            assertFalse("CRLF CR with null char should not be same", cdn.areSame() );                        
        }
        
        TreeComparor tc = new TreeComparor(false, false);
        {
            ComparisonDirNode cdn = tc.compare(daCRLF, daLF);
            assertFalse("CRLF vs LF difference should be significant if text compare not in effect", cdn.areSame() );                        
        }
        
    }
    
    private FileNode.ContentMethod usedMethod(TunableCompareMethodMockFileNode f1, TunableCompareMethodMockFileNode f2 ) {
        if ( f1.getUsedMethod() == null ) {
            return f2.getUsedMethod();
        } else if ( f2.getUsedMethod() == null ) {
            return f1.getUsedMethod();
        }
        assertEquals( f1.getUsedMethod(), f2.getUsedMethod() );
        return f1.getUsedMethod();
        
    }
    @Test
    public void testCompareMethodChoice() throws IOException {
        TunableCompareMethodMockFileNode f1text = new TunableCompareMethodMockFileNode("a", "AAAA1\r\n", new Date());
        TunableCompareMethodMockFileNode f2text = new TunableCompareMethodMockFileNode("a", "AAAA2\r\n", new Date());
        TunableCompareMethodMockFileNode f3bin = new TunableCompareMethodMockFileNode("a", "3\0\1\2\3\4\5\6", new Date());
        TunableCompareMethodMockFileNode f4bin = new TunableCompareMethodMockFileNode("a", "4\0\1\2\3\4\5\6", new Date());
        DirNode d1text = new DirNode(f1text);
        DirNode d2text = new DirNode(f2text);
        DirNode d3bin = new DirNode(f3bin);
        DirNode d4bin = new DirNode(f4bin);
        
        // even if file is text should not compare by text if cost impossible
        
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_HARD);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_HARD);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_EASY);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_IMPOSSIBLE);
        
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_HARD);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_HARD);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_EASY);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_IMPOSSIBLE);
        
        TreeComparor tctext = new TreeComparor(false, true);
        tctext.compare(d1text, d2text);
        assertEquals("Should not use text compare if method is impossible", 
                FileNode.CONTENT_METHOD_CONTENT, usedMethod(f1text, f2text) );
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();

        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        tctext.compare(d1text, d2text);
        assertEquals("Should not use text compare if method is impossible", 
                FileNode.CONTENT_METHOD_CONTENT, usedMethod(f1text, f2text) );
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();
        
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_IMPOSSIBLE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        tctext.compare(d1text, d2text);
        assertEquals("Should not use text compare if method is impossible", 
                FileNode.CONTENT_METHOD_CONTENT, usedMethod(f1text, f2text) );
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();
        
        
        // should throw exception if all method costs are impossible
        
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_IMPOSSIBLE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_IMPOSSIBLE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_IMPOSSIBLE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_IMPOSSIBLE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_IMPOSSIBLE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_IMPOSSIBLE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_IMPOSSIBLE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_IMPOSSIBLE);
        try {
            tctext.compare(d1text, d2text);
            fail("should throw exception if all method costs are impossible");
        } catch (IllegalArgumentException iae) {
            // this exception is expected
        }
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();
        
        // should throw exception no method possible for both files
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_EASY);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_EASY);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_EASY);
        try {
            tctext.compare(d1text, d2text);
            fail("should throw exception if all method costs are impossible");
        } catch (IllegalArgumentException iae) {
            // this exception is expected
        }
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();
        
        // if both files are text and cost not impossible, should compare text 
        // text compare takes precedence if tree comparor set for text compare and 
        // both files are text and cost not impossible even if other method has lower cost 
        // text compare only if comparor set for text compare
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_MODERATE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_EASY);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_IMPOSSIBLE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_HARD);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_MODERATE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_EASY);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_IMPOSSIBLE);
        f2text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_HARD);
        tctext.compare(d1text, d2text);
        assertEquals("Should use text compare method", 
            FileNode.CONTENT_METHOD_CONTENT_TEXT, usedMethod(f1text, f2text) );
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();
        

        TreeComparor tcbin = new TreeComparor(false, false);
        
        tcbin.compare(d1text, d2text);
        assertEquals("Should not use text compare method", 
            FileNode.CONTENT_METHOD_CRC, usedMethod(f1text, f2text) );
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();
        
        // both files must be text for should compare text 
        tctext.compare(d1text, d2text);
        assertEquals("Should use text compare method", 
            FileNode.CONTENT_METHOD_CONTENT_TEXT, usedMethod(f1text, f2text) );
        f1text.resetUsedMethod();
        f2text.resetUsedMethod();

        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_IMPOSSIBLE);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_IMPOSSIBLE);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_IMPOSSIBLE);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_IMPOSSIBLE);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_HARD);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);

        tctext.compare(d3bin, d4bin);
        assertEquals("Should use content compare method", 
            FileNode.CONTENT_METHOD_CONTENT, usedMethod(f3bin, f4bin) );
        f3bin.resetUsedMethod();
        f4bin.resetUsedMethod();
        
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_IMPOSSIBLE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_IMPOSSIBLE);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_HARD);
        f1text.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        tctext.compare(d1text, d3bin);
        assertEquals("Should use content compare method", 
            null, usedMethod(f1text, f3bin) );
//        FileNode.CONTENT_METHOD_CONTENT, usedMethod(f1text, f3bin) );
        f1text.resetUsedMethod();
        f3bin.resetUsedMethod();
        
        tctext.compare(d3bin, d1text);
        assertEquals("Should use content compare method", 
            null, usedMethod(f1text, f3bin) );
//        FileNode.CONTENT_METHOD_CONTENT, usedMethod(f1text, f3bin) );
        f1text.resetUsedMethod();
        f3bin.resetUsedMethod();

        
        // should pick lowest mutual cost method 
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_EASY);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        tctext.compare(d3bin, d4bin);
        assertEquals("Should use content compare method", 
            FileNode.CONTENT_METHOD_CRC, usedMethod(f3bin, f4bin) );
        f3bin.resetUsedMethod();
        f4bin.resetUsedMethod();

        tctext.compare(d4bin, d3bin);
        assertEquals("Should use content compare method", 
            FileNode.CONTENT_METHOD_CRC, usedMethod(f3bin, f4bin) );
        f3bin.resetUsedMethod();
        f4bin.resetUsedMethod();

        // should pick lowest mutual cost method 
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_EASY);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        tctext.compare(d3bin, d4bin);
        assertEquals("Should use content compare method", 
            FileNode.CONTENT_METHOD_MD5, usedMethod(f3bin, f4bin) );
        f3bin.resetUsedMethod();
        f4bin.resetUsedMethod();

        
        
        // 2 moderates better than 1 easy + 1 hard
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_MODERATE);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_HARD);
        f3bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_MD5, FileNode.COST_MODERATE);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CRC, FileNode.COST_MODERATE);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT, FileNode.COST_EASY);
        f4bin.setContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT, FileNode.COST_EASY);
        tctext.compare(d4bin, d3bin);
        assertEquals("Should use content compare method", 
            FileNode.CONTENT_METHOD_CRC, usedMethod(f3bin, f4bin) );
        f3bin.resetUsedMethod();
        f4bin.resetUsedMethod();
        
        
        
    }
    
    @Test
    public void testFileErrors() throws IOException {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File file = helper.createTestFile("tfile", "tfile-content");

        Logger logger = LogManager.getRootLogger();
        ErrorHandler noHandler = new LoggingErrorHandler(logger,  false);
        ErrorHandler yesHandler = new LoggingErrorHandler(logger,  true);

        FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(file.getCanonicalPath());
        DirNode rootNode = builder.buildTree(noHandler);

        TreeComparor tcbin = new TreeComparor(false, false);
        TreeComparor tctext = new TreeComparor(false, true);
        OperationSupportTester ost = new OperationSupportTester();
        assertTrue( ost.setReadable(file, false) );
        try {
            tcbin.compare(rootNode, rootNode);
            fail("by default compare should fail if nonredable file encountered");
        } catch ( Exception e ) {
            // this should happen
        }
        try {
            tctext.compare(rootNode, rootNode);
            fail("by default compare should fail if nonredable file encountered");
        } catch ( Exception e ) {
            // this should happen
        }
        
        tcbin.setErrorHandler(yesHandler);
        {
            ComparisonDirNode compareResults = tcbin.compare(rootNode, rootNode);
            assertEquals( 1, compareResults.getLeaves().size());
            LeafComparisonResult leafResult = (LeafComparisonResult)compareResults.getLeaves().get(0);
            assertEquals( "tfile", leafResult.getName() );
            assertFalse( leafResult.areSame() );
        }

        tctext.setErrorHandler(yesHandler);
        {
            ComparisonDirNode compareResults = tctext.compare(rootNode, rootNode);
            assertEquals( 1, compareResults.getLeaves().size());
            LeafComparisonResult leafResult = (LeafComparisonResult)compareResults.getLeaves().get(0);
            assertEquals( "tfile", leafResult.getName() );
            assertFalse( leafResult.areSame() );
        }

        
        tcbin.setErrorHandler(noHandler);
        try {
            tcbin.compare(rootNode, rootNode);
            fail("by default compare should fail if nonredable file encountered");
        } catch ( Exception e ) {
            // this should happen
        }
        
        file.delete();
        try {
            tcbin.compare(rootNode, rootNode);
            fail("by default compare should fail if file no longer exists");
        } catch ( Exception e ) {
            // this should happen
        }
        tcbin.setErrorHandler(yesHandler);
        {
            ComparisonDirNode compareResults = tcbin.compare(rootNode, rootNode);
            assertEquals( 1, compareResults.getLeaves().size());
            LeafComparisonResult leafResult = (LeafComparisonResult)compareResults.getLeaves().get(0);
            assertEquals( "tfile", leafResult.getName() );
            assertFalse( leafResult.areSame() );
        }
    }
    

}
