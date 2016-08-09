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
import org.wtdiff.util.NameDepth;
import org.wtdiff.util.NameDepthListBuilder;

public class TestNameDepthListBuilder  {
    
    @Test
    public void testEmpty() {
        DirNode d = new DirNode("dir", new ArrayList<Leaf>(), new ArrayList<DirNode>());
        NameDepthListBuilder builder= new NameDepthListBuilder(false);
        List<NameDepth> list = builder.buildNameDepthList(d);
        assertEquals("expected one element in list", 1, list.size());
        NameDepth nd = (NameDepth)list.get(0);
        assertEquals("expected name dir", "dir", nd.name);
        assertEquals("expected depth 0", 0, nd.depth);

        DirNode dUpper = new DirNode("DIR", new ArrayList<Leaf>(), new ArrayList<DirNode>());

        NameDepthListBuilder ignoreCaseBuilder= new NameDepthListBuilder(true);
        List<NameDepth> ignoreCaseList1 = ignoreCaseBuilder.buildNameDepthList(d);        
        List<NameDepth> ignoreCaseList2 = ignoreCaseBuilder.buildNameDepthList(dUpper);        
        NameDepth ind1 = (NameDepth)ignoreCaseList1.get(0);
        NameDepth ind2 = (NameDepth)ignoreCaseList2.get(0);
        assertEquals("expected name dir DIR the same", ind1.name, ind2.name);
    }
    
    @Test
    public void testFileNameIgnoreCase() {
        MockFileNode mfnLower = new MockFileNode("mfn");
        MockFileNode mfnUpper = new MockFileNode("MFN");
        
        ArrayList<Leaf> lLower = new ArrayList<>(1);
        lLower.add(mfnLower);
        ArrayList<Leaf> lUpper = new ArrayList<>(1);
        lUpper.add(mfnUpper);
        
        DirNode dLower = new DirNode( "lowerDir", lLower, new ArrayList<DirNode>() );
        DirNode dUpper = new DirNode( "upperDir", lUpper, new ArrayList<DirNode>() );
        
        NameDepthListBuilder builder = new NameDepthListBuilder(true);
        List<NameDepth> listLower = builder.buildNameDepthList(dLower);
        List<NameDepth> listUpper = builder.buildNameDepthList(dUpper);
        
        NameDepth ndLower = findByName("mfn", listLower);
        NameDepth ndUpper = findByName("MFN", listUpper);
        
        assertEquals("name depth for file node mfnLower does not match mfnUpper", ndLower.name, ndUpper.name );
        
    }
    
    @Test
    public void testDirs() {
        DirNode ssd = new DirNode("ssdir", new ArrayList<Leaf>(), new ArrayList<DirNode>());
        ArrayList<DirNode> al1 = new ArrayList<>(1);
        al1.add(ssd);
        DirNode sd = new DirNode("sdir", new ArrayList<Leaf>(), al1);
        ArrayList<DirNode> al = new ArrayList<>(1);
        al.add(sd);
        DirNode d = new DirNode("dir", new ArrayList<Leaf>(), al);
        
        NameDepthListBuilder builder= new NameDepthListBuilder(false);
        List <NameDepth> list = builder.buildNameDepthList(d);
        
        assertEquals("expected lsit size 3", 3, list.size());
        
        NameDepth dNd = findByName("dir", list);
        assertEquals("dir depth not 0", 0, dNd.depth);
       
        NameDepth dNsd = findByName("sdir", list);
        assertEquals("sdir depth not 1", 1, dNsd.depth );        
        NameDepth dNssd = findByName("ssdir", list);
        assertEquals("ssdir depth not 2", 2, dNssd.depth );                
    }
    @Test
    public void testDirsAndFiles() {
        MockFileNode ssf = new MockFileNode("ssf");
        MockFileNode sf = new MockFileNode("sf");
        MockFileNode f1 = new MockFileNode("f1");
        MockFileNode f2 = new MockFileNode("f2");
        ArrayList<Leaf> af = new ArrayList<>(2);
        af.add(f1);
        af.add(f2);
        ArrayList<Leaf> asf = new ArrayList<>(1);
        asf.add(sf);
        ArrayList<Leaf> assf = new ArrayList<>(1);
        assf.add(ssf);
        
        DirNode ssd = new DirNode("ssdir", assf, new ArrayList<DirNode>(0));
        ArrayList<DirNode> al1 = new ArrayList<>(1);
        al1.add(ssd);
        DirNode sd = new DirNode("sdir", asf, al1);
        ArrayList<DirNode> al = new ArrayList<>(1);
        al.add(sd);
        DirNode d = new DirNode("dir", af, al);
        
        NameDepthListBuilder builder= new NameDepthListBuilder(false);
        List<NameDepth> list = builder.buildNameDepthList(d);
        
        assertEquals("expected list size 7", 7, list.size());
        {
            NameDepth ndf1 = findByName("f1", list);
            assertEquals("f1 depth not 1", 1, ndf1.depth);
        }
        {
            NameDepth  ndf2 = findByName("f2", list);
            assertEquals("f2 depth not 1", 1, ndf2.depth );
        }
        {
            NameDepth  ndsf = findByName("sf", list);
            assertEquals("sf depth not 2", 2, ndsf.depth );
        }
        {
            NameDepth  ndssf = findByName("ssf", list);
            assertEquals("ssf depth not 3", 3, ndssf.depth );
        }
    }
    
    private  NameDepth findByName(String name, List<NameDepth> list) {
        Iterator<NameDepth> iter = list.iterator();
        while (iter.hasNext()) {
            NameDepth nd = (NameDepth)iter.next();
            if ( name.equalsIgnoreCase(nd.name) ) 
                return nd;
        }
        return null;
    }
}
