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
 * Node is the base class for our tree.  A nodes has a name and possibly a root.
 * 
 * @author davidst
 *
 */
public class Node {

    private String name;
    private String root;
    /**
     * Get the name of this node
     * @return
     */
    public String getName() {
        return name;
    }
    /**
     * Set the name of this node.  The name is intended to only be set once.
     *   
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * Get the root where this node starts, if set, otherwise return null
     * 
     * @return the root if set, otherwise null
     */
    public String getRoot() {
        return root;
    }
    /**
     * Set the root where this node starts.
     * 
     * @param root
     */
    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * The string representation of a node is its name
     */
    public String toString() {
        return getName();
    }
    
    /**
     * Utility class to compare nodes by their names.  Always case sensitive.
     * 
     * @author davidst
     */
    static public class NameComparator implements Comparator<Node> {
        public int compare(Node o1, Node o2) {
            return o1.name.compareTo(o2.name);
        }        
    }
    /**
     * A ready made node name comparator,
     */
    static public NameComparator nameComparator = new NameComparator();

    
    
}
