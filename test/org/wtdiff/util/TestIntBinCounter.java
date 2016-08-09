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
import org.wtdiff.util.IntBinCounter;

public class TestIntBinCounter  {
    @Test
    public void testEmpty() {
        IntBinCounter ibc = new IntBinCounter();
        assertEquals("initial count is not zero", 0, ibc.getTotCount() );
        assertEquals("initial count of 0 is not zero", 0, ibc.getCount(0) );
        assertEquals("initial count of 1 is not zero", 0, ibc.getCount(1) );
        assertEquals("initial count of -1 is not zero", 0, ibc.getCount(-1) );
        try {
            ibc.getMax();
            fail("getMax() did not throw execption with no items");
        } catch (IllegalArgumentException iae) { }
        try {
            ibc.getMin();
            fail("getMin() did not throw execption with no items");
        } catch (IllegalArgumentException iae) { }
        
        
    }
    @Test
    public void testCounter() {
        IntBinCounter ibc = new IntBinCounter();
        ibc.incr(0);
        assertEquals("expected count 0 not 1", 1, ibc.getCount(0));
        assertEquals("expected count 1 not 0", 0, ibc.getCount(1));
        assertEquals("expected tot count not 1", 1, ibc.getTotCount());
        assertEquals("min not 0", 0, ibc.getMin());
        assertEquals("max not 0", 0, ibc.getMax());
        ibc.incr(0);
        ibc.incr(2);
        ibc.incr(-2);
        ibc.incr(1);
        ibc.incr(-1);
        
        assertEquals("expected tot count not 6", 6, ibc.getTotCount());
        assertEquals("expected count 2 not 1", 1, ibc.getCount(2));
        assertEquals("expected count -2 not 1", 1, ibc.getCount(-2));
        assertEquals("expected count 1 not 1", 1, ibc.getCount(1));
        assertEquals("expected count -1 not 1", 1, ibc.getCount(-1));
        assertEquals("expected count 3 not 0", 0, ibc.getCount(3));
        assertEquals("min not -2", -2, ibc.getMin());
        assertEquals("max not 2", 2, ibc.getMax());
        
    }
}
