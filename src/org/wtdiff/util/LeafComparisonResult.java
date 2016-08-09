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

/**
 * Result of comparing two leaves
 * 
 * @author davidst
 */
public class LeafComparisonResult extends Leaf implements ComparisonResult {
    private boolean missing1;
    private boolean missing2;
    private boolean areSame12;
    private String name1;
    private String name2;
    /**
     * Constructor
     * 
     * @param n the node that was compared
     * @param have1 true if the node exists in the first tree 
     * @param have2 true if the node exists in the second tree
     * @param areSame true if the nodes are the same in both trees
     */
    public LeafComparisonResult(Node n1, Node n2, boolean have1, boolean have2, boolean areSame) {
        missing1 = !have1;
        missing2 = !have2;
        areSame12 = areSame;
        name1 = ""; 
        name2 = "";
        if ( n1 == null  && n2 == null ) {
            setName("");
        } else if ( n1 != null ) {
            name1 = n1.getName();
            setName(name1);
            if ( n2 != null ) {
                name2 = n2.getName();
            }
        } else {
            name2 = n2.getName();
            setName(name2);
        }
    }
    
    public String getName1() {
        return name1;
    }
    
    public String getName2() {
        return name2;
    }
    
    public  void setAreSame(boolean b) {
        areSame12 = b;
    }
    public boolean areSame() {
        return areSame12;
    }
    public boolean isMissing1() {
        return missing1;
    }
    public boolean isMissing2() {
        return missing2;
    }
    public boolean haveBoth() {
        return !missing2 && !missing1;
    }

    /**
     * return a String representation of the comparison result of the node
     * node (node exists in both and they are the same)
     * +node (node exists in tree 2 only)
     * -node (node exists in tree 1 only)
     * *node (node exists in both but they differ)
     */
    public String toString() {
        if (areSame12)
            return super.toString();
        if ( missing1 )
            return "+" + super.toString();
        if ( missing2 )
            return "-" + super.toString();

        return "*" + super.toString();
    }

     /**
      * debugging method which prints the result
      * 
      * @param prefix
      */
    public void dump(String prefix) {
        System.out.println( prefix + (missing1 ? "+" : missing2 ? "-" : areSame12 ? " ": "*" ) + getName() );
    }
    
}
