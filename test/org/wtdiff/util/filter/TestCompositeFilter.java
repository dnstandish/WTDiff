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
package org.wtdiff.util.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.filter.NodeFilter.Result;

public class TestCompositeFilter {

    @Test
    public void testEmpty() {
        
        MockFileNode emptyNameNode = new MockFileNode("");
        MockFileNode aNameNode = new MockFileNode("a");
        MockFileNode aDotNameNode = new MockFileNode("a.");
        MockFileNode aBNameNode = new MockFileNode("ab");
        MockFileNode dotANameNode = new MockFileNode(".a");
        
        CompositeNodeFilter empty = new CompositeNodeFilter();
        assertEquals(0, empty.size());
        assertEquals( Result.NONE,  empty.filterNode(emptyNameNode));
        assertEquals( Result.NONE, empty.filterNode(aNameNode));
        assertEquals( Result.NONE, empty.filterNode(aDotNameNode));
        assertEquals( Result.NONE, empty.filterNode(aBNameNode));
        assertEquals( Result.NONE, empty.filterNode(dotANameNode));

        assertEquals( 0, empty.filters().size() );
    }

    
    @Test
    public void testSimple() {
        
        MockFileNode emptyNameNode = new MockFileNode("");
        MockFileNode aNameNode = new MockFileNode("a");
        MockFileNode aDotNameNode = new MockFileNode("a.");
        MockFileNode aBNameNode = new MockFileNode("ab");
        MockFileNode dotANameNode = new MockFileNode(".a");
        
        GlobNameFilter emptyFilter = new GlobNameFilter("");
        GlobNameFilter aFilter = new GlobNameFilter("a*");
        CompositeNodeFilter aOrNothing = new CompositeNodeFilter();
        aOrNothing.add(emptyFilter);
        aOrNothing.add(aFilter);

        assertEquals( 2, aOrNothing.filters().size() );
        assertEquals( 2, aOrNothing.size());

        assertEquals( Result.EXCLUDE,  aOrNothing.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, aOrNothing.filterNode(aNameNode));
        assertEquals( Result.EXCLUDE, aOrNothing.filterNode(aDotNameNode));
        assertEquals( Result.EXCLUDE, aOrNothing.filterNode(aBNameNode));
        assertEquals( Result.NONE, aOrNothing.filterNode(dotANameNode));

    }
    
    @Test
    public void testMulti() {
        
        MockFileNode emptyNameNode = new MockFileNode("");
        MockFileNode aNameNode = new MockFileNode("a");
        MockFileNode aDotNameNode = new MockFileNode("a.");
        MockFileNode aBNameNode = new MockFileNode("ab");
        MockFileNode dotANameNode = new MockFileNode(".a");
        
        GlobNameFilter starAFilter = new GlobNameFilter("*a");
        GlobNameFilter starBFilter = new GlobNameFilter("*b");
        {
            CompositeNodeFilter filter = new CompositeNodeFilter();
            filter.add(starAFilter).add(starBFilter);
            
            assertEquals( Result.NONE,  filter.filterNode(emptyNameNode));
            assertEquals( Result.EXCLUDE, filter.filterNode(aNameNode));
            assertEquals( Result.NONE, filter.filterNode(aDotNameNode));
            assertEquals( Result.EXCLUDE, filter.filterNode(aBNameNode));
            assertEquals( Result.EXCLUDE, filter.filterNode(dotANameNode));
        }
        {
        ArrayList<NodeFilter> list = new ArrayList<NodeFilter>();
        CompositeNodeFilter filter = new CompositeNodeFilter();
        list.add(starAFilter);
        list.add(starBFilter);
        filter.add(list);
        
        assertEquals( Result.NONE,  filter.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, filter.filterNode(aNameNode));
        assertEquals( Result.NONE, filter.filterNode(aDotNameNode));
        assertEquals( Result.EXCLUDE, filter.filterNode(aBNameNode));
        assertEquals( Result.EXCLUDE, filter.filterNode(dotANameNode));
        }
    }
        
}
