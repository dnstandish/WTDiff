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
 * DirNode is the class used for trees.  DirNodes can have leafs and DirNodes as children.
 * Ths implementation differentiates between leaf nodes and non-leafnodes via class instead of 
 * an attribute that can be tested by a method.
 *   
 * @author davidst
 *
 */
public class DirNode extends Node {

    /** 
     * Leaf children of this node
     */
    private List <Leaf> leaves;
    /**
     * DirNode children of this node
     */
    private List <DirNode >dirNodes;
    
    /**
     * Construct a DirNode with given name, leaves and sub-dir nodes.
     * 
     * @param name name of thie node
     * @param leafList List of leaf children (not null)
     * @param dirList List of DirNode children (not null)
     */
    public DirNode(String name, List <Leaf> leafList, List <DirNode> dirList) {
        setName(name);
        leaves = leafList;
        dirNodes = dirList;
    }
    /**
     * Construct a dummy DirNode with an empty name and one leaf child
     *  
     * @param leaf
     */
    public DirNode(Leaf leaf) {
        setName("");
        dirNodes = new ArrayList <DirNode> (0);
        leaves = new ArrayList <Leaf>(1);
        leaves.add(leaf);
    }
    /**
     * Construct a dummy node with an empty name and one DirNode child
     * 
     * @param dn
     */
    public DirNode(DirNode dn) {
        setName("");
        dirNodes = new ArrayList <DirNode> (1);
        leaves = new ArrayList <Leaf> (0);
        dirNodes.add(dn);
    }

    /**
     *  Get the leaf children of this node. Should return an empty List if no leaf children. 
     *   
     * @return
     */
    public List <Leaf> getLeaves() {
        return leaves;
    }
    
    /**
     * Add a leaf to the children of this node.  No consideration is made for ordering,
     * 
     * @param l
     */
    public void addLeaf(Leaf l) {
        leaves.add(l);
    }

    /**
     * Get the DirNode children of this node. Should return an empty List if no DirNode children. 
     * @return
     */
    public List <DirNode> getDirs() {
        return dirNodes;
    }

    /**
     * Add a DirNOde to the children of this node,  No consideration is made for ordering.
     * 
     * @param dir
     */
    public void addDir(DirNode dir) {
        dirNodes.add(dir);
    }
    
    /**
     * Recursively sort leaves and directories
     */
    public void sort() {
        Collections.sort(leaves, nameComparator);
        Collections.sort(dirNodes, nameComparator);
        for ( DirNode dir: dirNodes )
            dir.sort();
    }
    
    /**
     * Find immediate sub dir with given name
     * 
     * @param name name to search for
     * @return DirNode of child with name if found, otherwise null
     */
    public DirNode childDirNodeByName(String name) {
        for( DirNode d: dirNodes ) {
            if ( d.getName().equals(name) )
                return d;
        }
        return null;
    }
    
    /**
     * Find immediate leaf with given name
     * 
     * @param name name to search for
     * @return DirNode of child with name if found, otherwise null
     */
    public Leaf childLeafByName(String name) {
        for( Leaf leaf: leaves ) {
            if ( leaf.getName().equals(name) )
                return leaf;
        }
        return null;
    }
    
    /**
     * Given list of node names from top to bottom, populate path
     * list with nodes matching names.
     * 
     * @param names
     * @param path list to populate by appending if all nodes found
     * @return true if all nodes found, false otherwise
     */
    public boolean populatePathByNames(List <String> names, List<Node> path) {
        if ( names.size() == 0 )
            return false;
        
        if ( names.size() == 1 ) {
            Node node = childLeafByName(names.get(0));
            if ( node != null ) {
                path.add(node);
                return true;
            }
        }
        
        DirNode dirNode = childDirNodeByName( names.get(0));
        
        if ( dirNode == null) 
            return false;
        
        path.add(dirNode);
        
        if ( names.size() == 1)
            return true;
        
        return dirNode.populatePathByNames( names.subList(1, names.size() ), path);
    }
    
    /**
     * Debug method to print out the tree structure, using prefix for
     * "indentation"
     * 
     * @param prefix
     * @param totPrefix
     */
    public void dump(String prefix, String totPrefix ) {
        System.out.println(totPrefix + "name: '"+getName()+"'");
        System.out.println(totPrefix + "files:");
        Iterator <Leaf> iterL = leaves.iterator();
        while (iterL.hasNext()) {
            System.out.println(totPrefix+prefix+"'"+iterL.next().toString()+"'");
        }
        System.out.println(totPrefix + "dirs:");
        Iterator <DirNode> iterC = dirNodes.iterator();
        while (iterC.hasNext()) {
            ((DirNode) iterC.next()).dump(prefix, totPrefix+prefix);
        }
    }
}
