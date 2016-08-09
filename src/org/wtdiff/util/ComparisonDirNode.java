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

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * result of comparing 2 DirNodes
 * @author davidst
 */
public class ComparisonDirNode extends DirNode implements ComparisonResult {

    private boolean missing1;
    private boolean missing2;
    private boolean areSame12;
    private String name1;
    private String name2;

    
    /**
     * Constructor
     * @param 1st DirNode that was compared
     * @param 2nd DirNode that was compared
     * @param have1 true if exists in tree 1
     * @param have2 true if exists in tree 2
     * @param areSame true if exists in both and they are the same
     */
    public ComparisonDirNode(DirNode  n1, DirNode n2, boolean have1, boolean have2, boolean areSame) {
        this(n1, n2, have1, have2, areSame, new ArrayList <Leaf> (0), new ArrayList <DirNode> (0));
    }

    /**
     * Constructor for DirNode with leaves
     * @param n1 name of 1st DirNode that was compared
     * @param n2 name of 1st DirNode that was compared
     * @param have1 true if exists in tree 1
     * @param have2 true if exists in tree 2
     * @param areSame true if exists in both and they are the same
     * @param fileComparisonList List of child Leaf Comparison results 
     * @param dirComparisonList List of child DirNode Comparison results
     */
    public ComparisonDirNode(DirNode n1, DirNode n2, boolean have1, boolean have2, boolean areSame, 
      List <Leaf> fileComparisonList, List <DirNode> dirComparisonList) {
        //TODO add n2 null check?
        super((n1 != null ? n1.getName(): (n2 != null? n2.getName(): "")), fileComparisonList, dirComparisonList);
        missing1 = !have1;
        missing2 = !have2;
        areSame12 = areSame; 
        name1 = n1 != null ? n1.getName(): "";
        name2 = n2 != null ? n2.getName(): "";
    }
    
    public String getName1() {
        return name1;
    }
    
    public String getName2() {
        return name2;
    }
    
    public boolean areSame() {
        return areSame12;
    }
    public void setAreSame(boolean areSame) {
        areSame12 = areSame;
    }
    
    public boolean isMissing1 () {
        return missing1;
    }
    
    public boolean isMissing2() {
        return missing2;
    }
    public boolean haveBoth() {
        return ! missing1 && ! missing2;
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
    public void dump(String prefix, String totPrefix ) {
        System.out.println( prefix + (missing1 ? "+" : missing2 ? "-" : areSame12 ? " ": "*" ) + getName()  );
        Iterator <Leaf> iterF = getLeaves().iterator();
        if ( iterF.hasNext() ) 
            System.out.println(prefix + "Files:");
        while ( iterF.hasNext() ) {
            LeafComparisonResult r = (LeafComparisonResult) iterF.next();
            r.dump(" "+prefix);
        }
        Iterator <DirNode> iterD = getDirs().iterator();
        if ( iterD.hasNext() ) 
            System.out.println(prefix + "Dir:");
        while ( iterD.hasNext() ) {
            ComparisonDirNode r = (ComparisonDirNode) iterD.next();
            r.dump(prefix, totPrefix+prefix);
        }
    }
}
