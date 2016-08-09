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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.LeafComparisonResult;
import org.wtdiff.util.Node;

public class TestComparisonResult  {

    @Test
    public void testComparison() {
        Node o1 = new Node();
        Node o2 = new Node();
        {
            LeafComparisonResult tcr1 = new LeafComparisonResult(o1, o2, true , true, true);
            assertFalse("Expected not missing 1", tcr1.isMissing1());
            assertFalse("Expected not missing 2", tcr1.isMissing2());
            assertTrue("Expected have both", tcr1.haveBoth());
            assertTrue("Expect same", tcr1.areSame());
            tcr1.setAreSame(false);
            assertFalse("Expect not same", tcr1.areSame());
            tcr1.setAreSame(true);
            assertTrue("Expect same", tcr1.areSame());
        } 
        {
            LeafComparisonResult tcr2 = new LeafComparisonResult(o1, o2, true , true, false);
            assertFalse("Expect not same", tcr2.areSame());
            assertFalse("Expected not missing 1", tcr2.isMissing1());
            assertFalse("Expected not missing 2", tcr2.isMissing2());
            assertTrue("Expected have both", tcr2.haveBoth());
        }
        {
            LeafComparisonResult tcr3 = new LeafComparisonResult(null, o2, false , true, false);
            assertTrue("Expected missing 1", tcr3.isMissing1());
            assertFalse("Expected not missing 2", tcr3.isMissing2());
            assertFalse("Expected not have both", tcr3.haveBoth());
        }
        {
            LeafComparisonResult tcr4 = new LeafComparisonResult(null, null, false , false, true);
            assertTrue("Expected missing 1", tcr4.isMissing1());
            assertTrue("Expected missing 2", tcr4.isMissing2());
            assertFalse("Expected not have both", tcr4.haveBoth());
        }
    }
    @Test
    public void testToString() {
        Node o1 = new Node();
        o1.setName("o1");
        Node o2 = new Node();
        o2.setName("o2");
        {
            LeafComparisonResult tcr1 = new LeafComparisonResult(o1, o2, true , true, true);
            assertEquals("o1", tcr1.toString());
            assertEquals("o1", tcr1.getName());
            assertEquals("o1", tcr1.getName1());
            assertEquals("o2", tcr1.getName2());
        }
        {
            LeafComparisonResult tcr1 = new LeafComparisonResult(o1, o2, true , true, false);
            assertEquals("*o1", tcr1.toString());
        }
        {
            LeafComparisonResult tcr1 = new LeafComparisonResult(o1, null, true , false, false);
            assertEquals("-o1", tcr1.toString());
            assertEquals("o1", tcr1.getName());
            assertEquals("o1", tcr1.getName1());
            assertEquals("", tcr1.getName2());
        }
        {
            LeafComparisonResult tcr1 = new LeafComparisonResult(null, o2, false , true, false);
            assertEquals("+o2", tcr1.toString());
            assertEquals("o2", tcr1.getName());
            assertEquals("", tcr1.getName1());
            assertEquals("o2", tcr1.getName2());

        }
    }
    
    @Test
    public void testDump() {
        Node o1 = new Node();
        o1.setName("o1");
        Node o2 = new Node();
        o2.setName("o2");
        {
            LeafComparisonResult tcr1 = new LeafComparisonResult(o1, o2, true , true, true);
            tcr1.dump("."); // ony tests that it doesn't blow up
        }
        
    }
}
