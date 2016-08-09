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

import junit.framework.*;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.NameDepth;

public class TestNameDepth  {
    @Test
    public void testComparator() {
        NameDepth nda1 = new NameDepth();
        nda1.name = "a";
        nda1.depth = 1;
        NameDepth nda2 = new NameDepth();
        nda2.name = "a";
        nda2.depth = 2;
        NameDepth ndb1 = new NameDepth();
        ndb1.name = "b";
        ndb1.depth = 1;
        NameDepth ndb2 = new NameDepth();
        ndb2.name = "b";
        ndb2.depth = 2;
        NameDepth nda1b = new NameDepth();
        nda1b.name = "a";
        nda1b.depth = 1;
        assertEquals("compare with self should return 0",
                0,
                NameDepth.nameDepthComparator.compare(nda1, nda1)
                );
        assertEquals("compare a 1 with a 1  should return 0",
                0,
                NameDepth.nameDepthComparator.compare(nda1, nda1b)
                );
        assertTrue("a 1 cmp a 2 not < 0", 
                NameDepth.nameDepthComparator.compare(nda1, nda2) < 0);
        assertTrue("a 2 cmp a 1 not > 0", 
                NameDepth.nameDepthComparator.compare(nda2, nda1) > 0);
        assertTrue("a 1 cmp b 1 not < 0", 
                NameDepth.nameDepthComparator.compare(nda1, ndb1) < 0);
        assertTrue("a 2 cmp b 1 not < 0", 
                NameDepth.nameDepthComparator.compare(nda2, ndb1) < 0);
        assertTrue("a 2 cmp b 2 not < 0", 
                NameDepth.nameDepthComparator.compare(nda2, ndb2) < 0);

    }
    

}
