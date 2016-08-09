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
import org.wtdiff.util.Node;

public class TestNode  {

    @Test
    public void testSettersGetters() {
            Node n = new Node();
            n.setName("n");
            n.setRoot("r");
            assertEquals( "n", n.getName() );
            assertEquals( "r", n.getRoot() );
            assertEquals( "n", n.toString() );
    }
    
    public void TestCompare() {
        Node.NameComparator c = Node.nameComparator;

        Node na = new Node();
        na.setName("a");
        assertTrue("node doesn't compare to self", c.compare(na, na) == 0 );
        
        Node nb = new Node();
        nb.setName("b");
        assertTrue("node a not less than b", c.compare(na, nb) < 0 );
        assertTrue("node b not greated than a", c.compare(nb, na) < 0 );

        
        Node nA = new Node();
        nA.setName("A");
        assertTrue("node a not less than A", c.compare(nA, na) < 0 );
        
        Node na2 = new Node();
        na2.setName("a");
        assertTrue("node a doesn't compare to other a", c.compare(na, na2) == 0 );
    }               
}

