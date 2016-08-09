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

import java.util.Comparator;

/**
 * simple class that captures a name and its Depth (in node tree)
 * @author davidst
 *
 */
public class NameDepth {
    /**
     * Comparator for 2 name depth objects, based on name, then depth
     * @author davidst
     */
    static public class NameDepthComparator implements Comparator<NameDepth> {
        public int compare(NameDepth o1, NameDepth o2) {
            int nc = o1.name.compareTo(o2.name);
            if (nc != 0 ) return nc;
            return o1.depth - o2.depth;
        }        
    }
    /**
     * Preconstructed Comparator
     */
    static public NameDepthComparator nameDepthComparator = new NameDepthComparator();

    /**
     * Publicly accessible name
     */
    public String name;
    /**
     * Publicly accessible depth
     */
    public int depth;
}
