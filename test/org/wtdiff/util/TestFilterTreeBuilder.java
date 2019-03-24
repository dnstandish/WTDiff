/*
Copyright 2018 David Standish

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

import java.io.IOException;
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
import org.wtdiff.util.filter.CompositeNodeFilter;
import org.wtdiff.util.filter.GlobNameFilter;
import org.wtdiff.util.filter.NodeFilter;

public class TestFilterTreeBuilder  {

    @Test
    public void testOneLevel()
    {
        {
            MockFileNode f = new MockFileNode("f");
            MockFileNode a = new MockFileNode("a");
            DirNode d = new DirNode( "d", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            d.setRoot("dd");
            d.addLeaf(a);
            d.addLeaf(f);
            GlobNameFilter filter = new GlobNameFilter("f");
            NoHandleErrorHandler handler = new NoHandleErrorHandler();
            FilterTreeBuilder builder = new FilterTreeBuilder(d, filter);
            try {
                DirNode filtered = builder.buildTree(handler);
                assertEquals( 2, d.getLeaves().size() );
                assertEquals( 1, filtered.getLeaves().size() );
                assertEquals( "a", filtered.getLeaves().get(0).getName() );
                assertEquals( "dd", filtered.getRoot() );
            }
            catch (IOException ioe) {
                fail("unexpected exception");
            }
        }
        {
            MockFileNode f = new MockFileNode("f");
            MockFileNode a = new MockFileNode("a");
            DirNode d = new DirNode( "d", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            d.setRoot("dd");
            d.addLeaf(a);
            d.addLeaf(f);
            GlobNameFilter filter = new GlobNameFilter("d");
            NoHandleErrorHandler handler = new NoHandleErrorHandler();
            FilterTreeBuilder builder = new FilterTreeBuilder(d, filter);
            try {
                DirNode filtered = builder.buildTree(handler);
                assertEquals( 2, d.getLeaves().size() );
                assertEquals( 0, filtered.getLeaves().size() );
                assertEquals( "dd", filtered.getRoot() );
            }
            catch (IOException ioe) {
                fail("unexpected exception");
            }
        }
    }

    @Test
    public void testTwoLevel()
    {
        {
            MockFileNode a = new MockFileNode("a");
            MockFileNode a_bak = new MockFileNode("a.bak");
            MockFileNode b = new MockFileNode("b");
            MockFileNode b_bak = new MockFileNode("b.bak");
            DirNode d = new DirNode( "d", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            d.setRoot("dd");
            d.addLeaf(a);
            d.addLeaf(a_bak);
            DirNode git = new DirNode( ".git", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            git.addLeaf(a);
            git.addLeaf(a_bak);
            
            DirNode sd = new DirNode( "sd", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
            sd.addLeaf(b);
            sd.addLeaf(b_bak);
            
            d.addDir(git);
            d.addDir(sd);
            
            GlobNameFilter bakFilter = new GlobNameFilter("*.bak");
            GlobNameFilter gitFilter = new GlobNameFilter(".git");
            CompositeNodeFilter filter = new CompositeNodeFilter();
            filter.add(bakFilter);
            filter.add(gitFilter);
            
            NoHandleErrorHandler handler = new NoHandleErrorHandler();
            FilterTreeBuilder builder = new FilterTreeBuilder(d, filter);
            try {
                DirNode filtered = builder.buildTree(handler);
                assertEquals( 2, d.getLeaves().size() );
                assertEquals( 1, filtered.getLeaves().size() );
                assertEquals( "a", filtered.getLeaves().get(0).getName() );
                assertEquals( "dd", filtered.getRoot() );
                
                assertEquals( 2, d.getDirs().size() );
                assertEquals( 1, filtered.getDirs().size() );
                DirNode subDir = filtered.getDirs().get(0);
                assertEquals( "sd", subDir.getName() );
                assertEquals( 1, subDir.getLeaves().size() );
                assertEquals( "b", subDir.getLeaves().get(0).getName() );
                
                
            }
            catch (IOException ioe) {
                fail("unexpected exception");
            }
        }
    }

}
