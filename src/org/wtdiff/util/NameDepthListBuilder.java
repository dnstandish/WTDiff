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

import java.util.*;

/**
 * Class for traversing a node tree to build a list of the names of the nodes
 * and their depth in the tree.  Names are collected in normallized case if constructed
 * with IGNORE_CASE
 * <pre>
 *  //given node tree d
 *  //  asdf/
 *  //     1
 *  //     2
 *  //     xx/
 *  //        Ab
 *  //        cD
 *             
 *     ndlb = new NameDepthListBuilder(NameDepthListBuilder.NO_IGNORE_CASE);
 *     List l = ndl.bbuildNameDepthList( d );
 *  // l will contain 
 *  //  { (asdf 0), (1 1) (2 1) (xx 1) (Ab 2) (cD 2) }
 *  </pre>
 * @author davidst
 *
 */
public class NameDepthListBuilder {
    
    /**
     * Typesafe enum for specifying normalize (ignore) case of namse or
     * preserve case (no ignore)
     */
    public static final class IgnoreCase {
    }
    public static final IgnoreCase IGNORE_CASE = new IgnoreCase();
    public static final IgnoreCase NO_IGNORE_CASE = new IgnoreCase();
    
    // do we ignore (normallize) case of node names
    private boolean ignoreCase;
    
    /**
     * Construct instance with specified behaviour with respect to 
     * case (upper/lower) of node names
     * 
     * @param ignoreCase if IGNORE_CASE names will be normallized to single case
     */       
    public NameDepthListBuilder(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
    
    /** 
     * return name of node depending on case sensitivity setting
     * 
     * @param n
     * @return
     */
    private String getName(Node n) {
        return ignoreCase ? n.getName().toLowerCase(): n.getName();
    }
    
    /**
     * Build a list of the names in a node tree and their depth in that tree.
     * The top dirnode (i.e. the one passed) is depth zero. Caller should make
     * no assumptions as to the ordering of the returned list
     * 
     * @param d  top DirNode of tree
     * @return list of name depth values in tree including leafs and subdirs
     */
    public List <NameDepth> buildNameDepthList(DirNode d) {
        // the list we will populate
        ArrayList <NameDepth> al = new ArrayList <> ();
        
        // Name depth structure of the top node
        NameDepth nd = new NameDepth();
        nd.name = getName(d);
        nd.depth = 0;
        al.add(nd); // add it to the list
        // recursively traverse the tree and add the names
        buildNameDepthList(d, al, 1);
        return al;
    }
    
    /**
     * Recurse the tree under the given node and collect name/depth values of subnodes
     * 
     * @param d node under which to collect name/depth data
     * @param l list to append to
     * @param depth depth of nodes under current node
     */
    private void buildNameDepthList(DirNode d, List <NameDepth> l, int depth) {
        // collect nanme/depth values of immediate leaf nodes
        List <Leaf> files = d.getLeaves();
        for (Iterator <Leaf> iter1 = files.iterator(); iter1.hasNext(); ) {
            Node n = iter1.next();
            NameDepth nd = new NameDepth();
            nd.name = getName(n);
            nd.depth = depth;
            l.add(nd);
        }
        // collect name/depth values of dir nodes recursively 
        for (Iterator <DirNode> iter2 = d.getDirs().iterator(); iter2.hasNext(); ) {
            // name depth of this sub dirnode
            DirNode d2 = iter2.next();
            NameDepth nd = new NameDepth();
            nd.name = getName(d2);
            nd.depth = depth;
            l.add(nd);
            // now do subnodes of this sub dirnode increasing depth as we descend
            buildNameDepthList(d2, l, depth+1);
        }
    }
}
