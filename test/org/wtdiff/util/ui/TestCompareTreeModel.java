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
package org.wtdiff.util.ui;

import java.util.ArrayList;

import org.wtdiff.util.DirNode;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.ui.CompareTreeModel;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCompareTreeModel  {
     
    @Test
    public void testEmptyDir() {
        DirNode d = new DirNode( "d", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
        CompareTreeModel model = new CompareTreeModel(d);
        assertEquals(d, model.getRoot());
        assertEquals( 0, model.getChildCount(model.getRoot()));
        assertNull( model.getChild(model.getRoot(), -1));
        assertNull( model.getChild(model.getRoot(), 0));
        assertNull( model.getChild(model.getRoot(), 1));

        assertEquals( -1, model.getIndexOfChild(model.getRoot(), d) );
        assertFalse( model.isLeaf(model.getRoot()) );
    }

    @Test
    public void testOneFile() {
        MockFileNode f1 = new MockFileNode("f1");
        DirNode d = new DirNode( f1);
        CompareTreeModel model = new CompareTreeModel(d);
        assertEquals(d, model.getRoot());
        assertEquals( 1, model.getChildCount(model.getRoot()));
        assertNull( model.getChild(model.getRoot(), -1));
        assertEquals( f1,  model.getChild(model.getRoot(), 0) );
        assertNull( model.getChild(model.getRoot(), 1));

        assertEquals( 0, model.getIndexOfChild(model.getRoot(), f1) );
        assertTrue( model.isLeaf( model.getChild(model.getRoot(), 0) ) );
    }

    @Test
    public void testOneDir() {

        DirNode d1 = new DirNode( "d1", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
        DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
        d1.addDir(d2);

        CompareTreeModel model = new CompareTreeModel( d1 );
        assertEquals(d1, model.getRoot());
        assertEquals( 1, model.getChildCount(model.getRoot()));
        assertNull( model.getChild(model.getRoot(), -1));
        assertEquals( d2,  model.getChild(model.getRoot(), 0) );
        assertNull( model.getChild(model.getRoot(), 1));

        assertEquals( 0, model.getIndexOfChild(model.getRoot(), d2) );
        assertFalse( model.isLeaf( model.getChild(model.getRoot(), 0) ) );
    }

    @Test
    public void testOneDirOneFile() {

        DirNode d1 = new DirNode( "d1", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
        DirNode d2 = new DirNode( "d2", new ArrayList<Leaf>(), new ArrayList<DirNode>()  );
        d1.addDir(d2);
        MockFileNode f1 = new MockFileNode("f1");
        d1.addLeaf(f1);
        
        CompareTreeModel model = new CompareTreeModel( d1 );
        assertEquals(d1, model.getRoot());
        assertEquals( 2, model.getChildCount(model.getRoot()));
        assertNull( model.getChild(model.getRoot(), -1));
        assertEquals( d2,  model.getChild(model.getRoot(), 0) );
        assertEquals( f1, model.getChild(model.getRoot(), 1));
        assertNull( model.getChild(model.getRoot(), 2));

        assertEquals( 0, model.getIndexOfChild(model.getRoot(), d2) );
        assertEquals( 1, model.getIndexOfChild(model.getRoot(), f1) );
    }

}
