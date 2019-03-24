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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.MockFileNode;
import org.wtdiff.util.filter.NodeFilter.Result;

public class TestGlobNameFilter {

    @Test
    public void testSimple() {
        
        MockFileNode emptyNameNode = new MockFileNode("");
        MockFileNode aNameNode = new MockFileNode("a");
        MockFileNode aDotNameNode = new MockFileNode("a.");
        MockFileNode aBNameNode = new MockFileNode("ab");
        MockFileNode dotANameNode = new MockFileNode(".a");
        
        GlobNameFilter emptyFilter = new GlobNameFilter("");
        assertEquals( "", emptyFilter.getGlob() );
        assertEquals( Result.EXCLUDE,  emptyFilter.filterNode(emptyNameNode));
        assertEquals( Result.NONE, emptyFilter.filterNode(aNameNode));
        assertEquals( Result.NONE, emptyFilter.filterNode(aDotNameNode));
        assertEquals( Result.NONE, emptyFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, emptyFilter.filterNode(dotANameNode));

        GlobNameFilter aFilter = new GlobNameFilter("a");
        assertEquals( "a", aFilter.getGlob() );
        assertEquals( Result.NONE,  aFilter.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, aFilter.filterNode(aNameNode));
        assertEquals( Result.NONE, aFilter.filterNode(aDotNameNode));
        assertEquals( Result.NONE, aFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, aFilter.filterNode(dotANameNode));
        
        GlobNameFilter aDotFilter = new GlobNameFilter("a.");
        assertEquals( "a.", aDotFilter.getGlob() );
        assertEquals( Result.NONE,  aDotFilter.filterNode(emptyNameNode));
        assertEquals( Result.NONE, aDotFilter.filterNode(aNameNode));
        assertEquals( Result.EXCLUDE, aDotFilter.filterNode(aDotNameNode));
        assertEquals( Result.NONE, aDotFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, aDotFilter.filterNode(dotANameNode));
        
    }
    
    @Test
    public void testWild() {
        
        MockFileNode emptyNameNode = new MockFileNode("");
        MockFileNode aNameNode = new MockFileNode("a");
        MockFileNode aDotNameNode = new MockFileNode("a.");
        MockFileNode aBNameNode = new MockFileNode("ab");
        MockFileNode dotANameNode = new MockFileNode(".a");
        
        GlobNameFilter starFilter = new GlobNameFilter("*");
        assertEquals( Result.EXCLUDE,  starFilter.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, starFilter.filterNode(aNameNode));
        assertEquals( Result.EXCLUDE, starFilter.filterNode(aDotNameNode));
        assertEquals( Result.EXCLUDE, starFilter.filterNode(aBNameNode));
        assertEquals( Result.EXCLUDE, starFilter.filterNode(dotANameNode));
        
        GlobNameFilter aStarFilter = new GlobNameFilter("a*");
        assertEquals( Result.NONE,  aStarFilter.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, aStarFilter.filterNode(aNameNode));
        assertEquals( Result.EXCLUDE, aStarFilter.filterNode(aDotNameNode));
        assertEquals( Result.EXCLUDE, aStarFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, aStarFilter.filterNode(dotANameNode));
        
        GlobNameFilter qmFilter = new GlobNameFilter("?");
        assertEquals( Result.NONE,  qmFilter.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, qmFilter.filterNode(aNameNode));
        assertEquals( Result.NONE, qmFilter.filterNode(aDotNameNode));
        assertEquals( Result.NONE, qmFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, qmFilter.filterNode(dotANameNode));

        GlobNameFilter acFilter = new GlobNameFilter("[a]");
        assertEquals( Result.NONE,  acFilter.filterNode(emptyNameNode));
        assertEquals( Result.EXCLUDE, acFilter.filterNode(aNameNode));
        assertEquals( Result.NONE, acFilter.filterNode(aDotNameNode));
        assertEquals( Result.NONE, acFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, acFilter.filterNode(dotANameNode));
        
        GlobNameFilter starBFilter = new GlobNameFilter("*b");
        assertEquals( Result.NONE,  starBFilter.filterNode(emptyNameNode));
        assertEquals( Result.NONE, starBFilter.filterNode(aNameNode));
        assertEquals( Result.NONE, starBFilter.filterNode(aDotNameNode));
        assertEquals( Result.EXCLUDE, starBFilter.filterNode(aBNameNode));
        assertEquals( Result.NONE, starBFilter.filterNode(dotANameNode));
    }
        
}
