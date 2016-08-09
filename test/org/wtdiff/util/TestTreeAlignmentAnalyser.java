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
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.TreeAlignmentAnalyser;

public class TestTreeAlignmentAnalyser  {

    @Test
    public void testFindBestDepthAlignment() {
        TreeAlignmentAnalyser taa = new TreeAlignmentAnalyser(false);
        // a/ {1,2,3}
        // b/ {a/ {1,2,3}
        // b/{3, a/{1,2}
        // c/
        DirNode di;
        {
            ArrayList<Leaf> a = new ArrayList<>(3);
            a.add( new MockFileNode("1") );
            a.add( new MockFileNode("2") );
            a.add( new MockFileNode("3") );
            di = new DirNode("a", a, new ArrayList<DirNode>(0));
        }
        DirNode dii;
        {
            ArrayList<Leaf> a = new ArrayList<>(3);
            a.add( new MockFileNode("1") );
            a.add( new MockFileNode("2") );
            a.add( new MockFileNode("3") );
            DirNode da = new DirNode("a", a, new ArrayList<DirNode>(0));
            ArrayList<DirNode> b = new ArrayList<>();
            b.add(da);
            dii = new DirNode("b", new ArrayList<Leaf>(0), b);
        }
        DirNode diii;
        {
            ArrayList<Leaf> a = new ArrayList<>(3);
            a.add( new MockFileNode("1") );
            a.add( new MockFileNode("2") );
            DirNode da = new DirNode("a", a, new ArrayList<DirNode>(0));
            ArrayList<DirNode> b = new ArrayList<>();
            b.add(da);
            ArrayList<Leaf> c = new ArrayList<>();
            c.add( new MockFileNode("3") );
            diii = new DirNode("b", c, b);
        }
        DirNode d321;
        {
            DirNode d1 = new DirNode("1", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
            ArrayList<DirNode> a1 = new ArrayList<>(1);
            a1.add(d1);
            DirNode d21 = new DirNode("2", new ArrayList<Leaf>(0), a1);
            ArrayList<DirNode> a2 = new ArrayList<>(1);
            a2.add(d21);
            d321 = new DirNode("3", new ArrayList<Leaf>(0), a2);
        }
        DirNode d123;
        {
            DirNode d3 = new DirNode("3", new ArrayList<Leaf>(0), new ArrayList<DirNode>(0));
            ArrayList<DirNode> a3 = new ArrayList<>(1);
            a3.add(d3);
            DirNode d23 = new DirNode("2", new ArrayList<Leaf>(0), a3);
            ArrayList<DirNode> a2 = new ArrayList<>(1);
            a2.add(d23);
            d123 = new DirNode("3", new ArrayList<Leaf>(0), a2);
        }
        
        assertEquals("Exepected best depth alignment", 0, taa.findBestDepthAlignment(di, di) );
        assertEquals("Exepected best depth alignment", -1, taa.findBestDepthAlignment(di, dii) );
        assertEquals("Exepected best depth alignment", 1, taa.findBestDepthAlignment(dii, di) );
        assertEquals("Exepected best depth alignment", 0, taa.findBestDepthAlignment(dii, diii) );
        assertEquals("Exepected best depth alignment", 0, taa.findBestDepthAlignment(diii, dii) );
        assertEquals("Exepected best depth alignment", -1, taa.findBestDepthAlignment(di, diii) );
        assertEquals("Exepected best depth alignment", 0, taa.findBestDepthAlignment(d321, d123) );
        assertEquals("Exepected best depth alignment", 0, taa.findBestDepthAlignment(d123, d321) );
        
        
    }
    @Test
    public void testFindBestDepthAlignmentCase() {
        TreeAlignmentAnalyser taa = new TreeAlignmentAnalyser(true);
        // a/ {a,b,c}
        // 2/ {A/ {A,B,C}
        DirNode di;
        {
            ArrayList<Leaf> a = new ArrayList<>(3);
            a.add( new MockFileNode("a") );
            a.add( new MockFileNode("b") );
            a.add( new MockFileNode("c") );
            di = new DirNode("a", a, new ArrayList<DirNode>(0));
        }
        DirNode dii;
        {
            ArrayList<Leaf> a = new ArrayList<>(3);
            a.add( new MockFileNode("A") );
            a.add( new MockFileNode("B") );
            a.add( new MockFileNode("C") );
            DirNode da = new DirNode("A", a, new ArrayList<DirNode>(0));
            ArrayList<DirNode> b = new ArrayList<>();
            b.add(da);
            dii = new DirNode("B", new ArrayList<Leaf>(0), b);
        }
        assertEquals("Exepected best depth alignment", -1, taa.findBestDepthAlignment(di, dii) );
        assertEquals("Exepected best depth alignment", 1, taa.findBestDepthAlignment(dii, di) );

        TreeAlignmentAnalyser tab = new TreeAlignmentAnalyser(false);
        assertEquals("Exepected best depth alignment", 0, tab.findBestDepthAlignment(di, dii) );
        assertEquals("Exepected best depth alignment", 0, tab.findBestDepthAlignment(dii, di) );

    }
    
    private DirNode createD1() {
        // a/{1,2}
        MockFileNode f1 = new MockFileNode("1");
        MockFileNode f2 = new MockFileNode("2");
        ArrayList<Leaf> a = new ArrayList<>(2);
        a.add(f1);
        a.add(f2);
        DirNode da = new DirNode("a", a, new ArrayList<DirNode>(0));
        ArrayList<DirNode> d = new ArrayList<>(1);
        d.add(da);
        return new DirNode("d1", new ArrayList<Leaf>(0), d );
    }
    
    private DirNode createD2() {
        // {1,2}
        MockFileNode f1 = new MockFileNode("1");
        MockFileNode f2 = new MockFileNode("2");
        ArrayList<Leaf> a = new ArrayList<>(2);
        a.add(f1);
        a.add(f2);
        return new DirNode("d2", a, new ArrayList<DirNode>(0));
   
    }
    
    private DirNode createD3() {
        // a/{2, a/{1,2}, b/{1,2, a/{1,2}}}

        ArrayList<Leaf> a12 = new ArrayList<>(2);
        
        MockFileNode f1 = new MockFileNode("1");
        MockFileNode f2 = new MockFileNode("2");

        a12.add(f1);
        a12.add(f2);
        DirNode daa = new DirNode("a", a12, new ArrayList<DirNode>(0));
        
        DirNode daba = new DirNode("a", a12, new ArrayList<DirNode>(0));

        ArrayList<Leaf> bf = new ArrayList<>(1);
        ArrayList<DirNode> bd = new ArrayList<>(1);
        //bf.add(f1);
        bf.add(f2);
        bd.add(daba);
        DirNode dab = new DirNode("b", bf, bd);

        ArrayList<Leaf> af = new ArrayList<>(1);
        ArrayList<DirNode> ad = new ArrayList<>(2);
        af.add(f2);
        ad.add(daa);
        ad.add(dab);
        DirNode da = new DirNode("a", af, ad);
        
//        ArrayList<Leaf> ba2 = new ArrayList<>(1);
//        ba2.add(f2);
//        DirNode dba = new DirNode("a", ba2, new ArrayList<DirNode>(0));
        
        ArrayList<DirNode> d = new ArrayList<>(1);
        d.add(da);
        //d.add(db);
        return new DirNode("d3", new ArrayList<Leaf>(0), d);
    }
    
    private DirNode createD4() {
        // a/{1,2, a/{1}}, b/{1, a/{1,2}}}
        MockFileNode f1 = new MockFileNode("1");
        MockFileNode f2 = new MockFileNode("2");
        ArrayList<Leaf> a1 = new ArrayList<>(1);
        a1.add(f1);
        DirNode daa = new DirNode("a", a1, new ArrayList<DirNode>(0));
        ArrayList<Leaf> af = new ArrayList<>(2);
        ArrayList<DirNode> ad = new ArrayList<>(1);
        af.add(f1);
        af.add(f2);
        ad.add(daa);
        DirNode da = new DirNode("a", af, ad);
        
        ArrayList<Leaf> ba12 = new ArrayList<>(2);
        ba12.add(f1);
        ba12.add(f2);
        DirNode dba = new DirNode("a", ba12, new ArrayList<DirNode>(0));
        
        ArrayList<Leaf> bf = new ArrayList<>(1);
        ArrayList<DirNode> bd = new ArrayList<>(1);
        bf.add(f1);
        bd.add(dba);
        DirNode db = new DirNode("b", bf, bd);
        
        ArrayList<DirNode> d = new ArrayList<>(2);
        d.add(da);
        d.add(db);
        return new DirNode("d4", new  ArrayList<Leaf>(0), d);

    }
    private DirNode createD5() {
        // c/{4}
        MockFileNode f1 = new MockFileNode("4");
        ArrayList<Leaf> a = new ArrayList<>(2);
        a.add(f1);
        DirNode da = new DirNode("c", a, new ArrayList<DirNode>(0));
        ArrayList<DirNode> d = new ArrayList<>(1);
        d.add(da);
        return new DirNode("d5", new ArrayList<Leaf>(0), d );
    }

    @Test
    public void testMatchFactor() {
        TreeAlignmentAnalyser taa = new TreeAlignmentAnalyser(false);
        // a/{1,2}
        // {1,2}
        // a/{2, a/{1,2}, b/{2, a/{1,2}}}
        // a/{1,2, a/{1}}, b/{1, a/{1,2}}}
        DirNode d1 = createD1();
        DirNode d2 = createD2();
        DirNode d3 = createD3();
        DirNode d4 = createD4();
        DirNode d5 = createD5();
        assertEquals(0.0, taa.matchFactor(d1, d2), 0.0001);
        assertEquals( 4.0/15.0 , taa.matchFactor(d1, d3), 0.0001);
        assertEquals( 6.0/15.0 , taa.matchFactor(d1, d4), 0.0001);
        assertEquals( 0.0 , taa.matchFactor(d1, d5), 0.0001);
        assertEquals( 6.0/15.0 , taa.matchFactor(d4, d1), 0.0001);
        assertEquals( 0.0 , taa.matchFactor(d2, d3), 0.0001);
        assertEquals( 10.0/22.0 , taa.matchFactor(d3, d4), 0.0001);
        
    }
    
    private DirNode immediateSubdirWithName(DirNode d, String name) {
        for(Iterator<DirNode> iter =  d.getDirs().iterator(); iter.hasNext(); ) {
            DirNode sd = (DirNode) iter.next();
            if (sd.getName().equals(name))
                    return sd;
        }
        return null;
    }
    @Test
    public void testBestSubTree() {
        TreeAlignmentAnalyser taa = new TreeAlignmentAnalyser(false);
        // a/{1,2}
        // {1,2}
        // a/{2, a/{1,2}, b/{2, a/{1,2}}}
        // a/{1,2, a/{1}}, b/{1, a/{1,2}}}
        DirNode d1 = createD1();
        DirNode d2 = createD2();
        DirNode d3 = createD3();
        DirNode d4 = createD4();

        try {
            taa.bestSubTree(-1, d1, d2);
           fail("bestSubTree should throw exception if passed negative depth"); 
        } catch (Exception e) {            
        }
        assertEquals(1, taa.bestSubTree(0, d1, d2).size() );
        assertEquals(d1, taa.bestSubTree(0, d1, d2).get(0) );

        DirNode d1a = (DirNode)d1.getDirs().get(0);
        assertEquals(2, taa.bestSubTree(1, d1, d2).size() );
        assertEquals(d1, taa.bestSubTree(1, d1, d2).get(0) );
        assertEquals(d1a, taa.bestSubTree(1, d1, d2).get(1) );
        
        DirNode d3a = immediateSubdirWithName(d3, "a");
        assertEquals( 2, taa.bestSubTree(1, d3, d1).size());
        assertEquals( d3, taa.bestSubTree(1, d3, d1).get(0));
        assertEquals( d3a, taa.bestSubTree(1, d3, d1).get(1));

        assertEquals( 2, taa.bestSubTree(1, d3, d2).size());
        assertEquals( d3, taa.bestSubTree(1, d3, d2).get(0));
        assertEquals( d3a, taa.bestSubTree(1, d3, d2).get(1));
        DirNode d3aa = immediateSubdirWithName(d3a, "a");
        assertEquals( 3, taa.bestSubTree(2, d3, d2).size());
        assertEquals( d3, taa.bestSubTree(2, d3, d2).get(0));
        assertEquals( d3a, taa.bestSubTree(2, d3, d2).get(1));
        assertEquals( d3aa, taa.bestSubTree(2, d3, d2).get(2));
        DirNode d3ab = immediateSubdirWithName(d3a, "b");
        DirNode d3aba = immediateSubdirWithName(d3ab, "a");
        assertEquals( 4, taa.bestSubTree(3, d3, d2).size());
        assertEquals( d3, taa.bestSubTree(3, d3, d2).get(0));
        assertEquals( d3a, taa.bestSubTree(3, d3, d2).get(1));
        assertEquals( d3ab, taa.bestSubTree(3, d3, d2).get(2));
        assertEquals( d3aba, taa.bestSubTree(3, d3, d2).get(3));

        
        DirNode d4b = immediateSubdirWithName(d4, "b");
        assertEquals( 2, taa.bestSubTree(1, d4, d1).size());
        assertEquals( d4, taa.bestSubTree(1, d4, d1).get(0));
        assertEquals( d4b, taa.bestSubTree(1, d4, d1).get(1));
        
        DirNode d4ba = immediateSubdirWithName(d4b, "a");
        assertEquals( 3, taa.bestSubTree(2, d4, d2).size());
        assertEquals( d4, taa.bestSubTree(2, d4, d2).get(0));
        assertEquals( d4b, taa.bestSubTree(2, d4, d2).get(1));
        assertEquals( d4ba, taa.bestSubTree(2, d4, d2).get(2));
        
    }
}