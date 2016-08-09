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

import java.util.HashMap;

/**
 * Class to collect simiple stats into buckets (bins) based on 
 * integer value.  Also keeps track of min/max integer values
 * <pre>
 *    binCounter = new IntBinCounter();
 *    binCounter.incr(2);
 *    binCounter.incr(4);
 *    binCounter.incr(5);
 *    binCounter.incr(2);
 *    binCounter.incr(5);
 *    binCounter.getMin(); // 2  was lowest value passed
 *    binCounter.getMax(); // 5  was biggest value paseed
 *    binCounter.getTotCount(); // 5 was total number of things counted
 *    binCounter.getCount(-999); // 0 counted no -999 values
 *    binCounter.getCount(4); // 1 counted one value 4
 *    binCounter.getCount(5); // 2 counted two value 5
 * @author davidst
 *
 */
public class IntBinCounter {
    private int min; // min value seen
    private int max; // max value seen
    private int count = 0; // total count of values seen
    private HashMap <Integer, Integer> counts = new HashMap <> (); //counts per value
    
    /**
     * Constructor
     */
    public IntBinCounter() {            
    }

    /**
     * increment count for given value
     * @param i the value
     */
    public void incr(int i) {
        // min/max value of i seen
        if ( count == 0 ) {
            min = max = i;
        } else if (i < min) {
            min = i;
        } else if ( i > max) {
            max = i;
        }
        count++; // total count
        
        // convert int to Integer object so can use as a key
        Integer key = new Integer(i);
        Integer count = counts.get(key);
        if (count == null) {
            count = new Integer(1);   // we don't have a bucket for this value, so count is 1            
        } else {
            // we already have a bucket, so increment count for this value
            count = new Integer(count.intValue()+1);
        }
        //      note that count is a new object.  We have to replace the old object in hash map.
        counts.put(key, count); 
    }
    
    /**
     * @return total number of things counted
     */
    public int getTotCount() {
        return count;
    }
    /**
     * Number of things value 'i' counted.  If nothing value 'i' seen then count is 0
     * 
     * @param i
     * @return count for value i
     */
    public int getCount(int i) {
        Integer key = new Integer(i);
        Integer count = counts.get(key);
        return count == null ? 0 : count.intValue();
    }
    /**
     * Minimum thing seen.
     * 
     * @return
     */
    public int getMin() {
        if ( count == 0) {
            throw new IllegalArgumentException(Messages.getString("IntBinCounter.min_undefined")); //$NON-NLS-1$
        }
        return min;
    }
    /**
     * maximum thing seen
     * 
     * @return
     */
    public int getMax() {
        if ( count == 0) {
            throw new IllegalArgumentException(Messages.getString("IntBinCounter.max_undefined")); ///$NON-NLS-1$
        }
        return max;
    }
    

}
