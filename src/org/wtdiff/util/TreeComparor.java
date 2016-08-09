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

import java.text.MessageFormat;
import java.util.*;
import java.io.IOException;

/**
 * Class to compare two trees of Nodes.  The comparison result is also a tree of Nodes, Comparison result nodes. 
 * 
 * @author davidst
 *
 */
public class TreeComparor {
    // comparison of node names is case insensitive
    private boolean isNameIgnoreCase;
    // text nodes should be compared as text  
    private boolean isTextCompare;
    
    // error handler
    private ErrorHandler errorHandler; 
    /**
     * Constructor
     * 
     * @param nameIgnoreCase comparison of node names is case insensitive
     * @param textCompare text nodes should be compared as text  
     * @param errorHandler error handler
     */
    public TreeComparor(boolean nameIgnoreCase, boolean textCompare) {
        isNameIgnoreCase = nameIgnoreCase;
        isTextCompare = textCompare;
        errorHandler = new NoHandleErrorHandler( );
    }
    
    /**
     * Set error handler
     * 
     * @param handler
     */
    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    /**
     * Check if any comparison results in list indicate a difference.
     * 
     * @param l list of Leaf comparison results
     * @return true if same, false otherwise
     */
    private boolean checkResults(List<Leaf> l) {
        for( Iterator<Leaf> rIter = l.iterator(); rIter.hasNext(); ) {
            LeafComparisonResult dRes = (LeafComparisonResult)rIter.next();;
            if (! dRes.areSame() ) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if any comparison results in list indicate a difference.
     * 
     * @param l list of DirNode comparison results
     * @return true if same, false otherwise
     */
    private boolean checkDirResults(List<DirNode> l) {
        for( Iterator<DirNode> rIter = l.iterator(); rIter.hasNext(); ) {
            ComparisonDirNode dRes = (ComparisonDirNode)rIter.next();;
            if (! dRes.areSame() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compare two trees of DirNodes.
     * 
     * @param d1 root DirNode of first tree
     * @param d2 root DirNode of first tree
     * @return root DirNode of comparison result tree
     *  
     * @throws IOException
     */
    public ComparisonDirNode compare(DirNode d1, DirNode d2)  throws IOException {
        List<Leaf> fileResults;
        List<DirNode> dirResults;
        boolean have1 = false;
        boolean have2 = false;
//        String name1 = "";
//        String name2 = "";
        if ( d1 != null && d2 != null ) {
//            name1 = d1.getName();
//            name2 = d2.getName();
            fileResults = CompareFileNodes(d1.getLeaves(), d2.getLeaves());
            dirResults = CompareDirNodes(d1.getDirs(), d2.getDirs());
            have1 = have2 = true;
        } else if ( d2 != null ) {
//            name2 = d2.getName();
            fileResults = CompareFileNodes(new ArrayList<Leaf>(0), d2.getLeaves());
            dirResults = CompareDirNodes(new ArrayList<DirNode>(0), d2.getDirs());       
            have2 = true;
            
        } else if (d1 != null) {
//            name1 = d1.getName();
            fileResults = CompareFileNodes(d1.getLeaves(), new ArrayList<Leaf>(0));
            dirResults = CompareDirNodes(d1.getDirs(), new ArrayList<DirNode>(0));
            have1 = true;
        } else {
            fileResults = new ArrayList<Leaf>(0);
            dirResults = new ArrayList<DirNode>(0);
        }
        boolean areSame = have1 && have2 && checkResults(fileResults) && checkDirResults(dirResults);
        return new ComparisonDirNode(d1, d2, have1, have2, areSame, fileResults, dirResults);
    }
    
    /**
     * Convert list of nodes into hash where key is node name and value is node
     * 
     * @param nodes
     * @return
     */
    private <T extends Node> HashMap<String,T> buildNameHash(List <T>nodes, boolean isIgnoreCase) {
        // HashMap will not work with ignoreCase as can has duplicate keys
        HashMap<String,T>  m = new HashMap<>();
        for(Iterator<T> iter = nodes.iterator(); iter.hasNext(); ) {
            T nd = iter.next();
            String key = isIgnoreCase ? nd.getName().toLowerCase(): nd.getName();
             if ( m.containsKey(key) ) {
                 // we have a collision of names then if this one's name
                 // is the same as its normalized form then this one should
                 // replace the previous one.  The previous one would be remapped
                 // to its unnormalized one.  If not then map this one under its
                 // unnormalized form
                 if ( nd.getName().equals(key) ) {
                     T oldNd = m.get(key);
                     if ( oldNd.getName().equals(key) ) {
                         // this shouldn't happen
                         throw new IllegalArgumentException(
                             MessageFormat.format(
                                 Messages.getString("TreeComparor.duplicate_name"),  //$NON-NLS-1$
                                 key
                             )
                          );
                     }
                     m.put( oldNd.getName(), oldNd );
                     m.put(key, nd);
                 } else {
                     m.put(nd.getName(), nd);
                 }
             }  else {
                 m.put(key, nd);
             }
        }
        return m;
    }
    
    private enum Membership {BOTH, ONLY1, ONLY2}
    private <T extends Node> HashMap<Membership,List<T>> namePartition(List<T> l1, List<T> l2, boolean isIgnoreCase) {
        List<T> intersection = new ArrayList<T>();
        List<T> l1Only = new ArrayList<T>();
        List<T> l2Only = new ArrayList<T>();
        
        HashMap<String,T> m1 = buildNameHash( l1, isIgnoreCase);
        HashMap<String,T> m2 = buildNameHash( l2, isIgnoreCase);
        for(String name: m1.keySet()) {
            if ( m2.containsKey(name) ) {
                intersection.add(m1.get(name));
                intersection.add(m2.get(name));
                m2.remove(name);
            }
            else {
                l1Only.add(m1.get(name));
            }
        }
        l2Only.addAll(m2.values());

        HashMap <Membership, List<T>> r = new HashMap <Membership, List<T>>();
        r.put(Membership.BOTH, intersection);
        r.put(Membership.ONLY1, l1Only);
        r.put(Membership.ONLY2, l2Only);
        return r;
    }
    /**
     * Compare two lists of DirNodes and package results into a list of ComparisonDirNodes
     * 
     * @param l1
     * @param l2
     * @return 
     * @throws IOException
     */
    private List<DirNode> CompareDirNodes(List<DirNode> l1, List<DirNode> l2) throws IOException  {
        HashMap<Membership, List<DirNode>> partition = namePartition(l1, l2, false);
        
        int startSize = l1.size();
        if ( l2.size() > startSize ) {
            startSize = l2.size();
        }        
        ArrayList<DirNode> resultList = new ArrayList<>(startSize);

        List<DirNode> lBoth = partition.get(Membership.BOTH);
        for(Iterator<DirNode> iter = lBoth.iterator(); iter.hasNext();) {
            DirNode dn1 = iter.next();
            DirNode dn2 = iter.next();
            ComparisonDirNode r;
            r = compare(dn1, dn2);
            resultList.add(r);
        }
        
        List<DirNode> lOnly1 = partition.get(Membership.ONLY1);
        List<DirNode> lOnly2 = partition.get(Membership.ONLY2);
        if ( isNameIgnoreCase ) {
            Collections.sort(lOnly1, Node.nameComparator);  
            Collections.sort(lOnly2, Node.nameComparator);  
            partition = namePartition(lOnly1, lOnly2, true);
            lBoth = partition.get(Membership.BOTH);
            lOnly1 = partition.get(Membership.ONLY1);
            lOnly2 = partition.get(Membership.ONLY2);
            
            for(Iterator<DirNode> iter = lBoth.iterator(); iter.hasNext();) {
                DirNode dn1 = iter.next();
                DirNode dn2 = iter.next();
                ComparisonDirNode r;
                r = compare(dn1, dn2);
                resultList.add(r);
            }
            
        }

        for(Iterator<DirNode> iter = lOnly1.iterator(); iter.hasNext();) {
            DirNode dn1 = iter.next();
            ComparisonDirNode r;
            r = compare(dn1, null);
            resultList.add(r);
        }
        for(Iterator<DirNode> iter = lOnly2.iterator(); iter.hasNext();) {
            DirNode dn2 = iter.next();
            ComparisonDirNode r;
            r = compare(null, dn2);
            resultList.add(r);
        }
        
        // sort result list by name?
        Collections.sort(resultList, Node.nameComparator);  
        return resultList;  
    }
    
    /**
     * Compare two lists of FileNodes and package result into a list of LeafComparisonResults
     * 
     * @param l1
     * @param l2
     * @return
     * @throws IOException
     */
    private List<Leaf> CompareFileNodes(List<Leaf> l1, List<Leaf> l2) throws IOException {

        HashMap<Membership, List<Leaf>> partition = namePartition(l1, l2, false);

        // presize our result list. Assume fairly close match of names
        int startSize = l1.size();
        if ( l2.size() > startSize ) {
            startSize = l2.size();
        }        
        ArrayList<Leaf> resultList = new ArrayList<>(startSize);

        List<Leaf> lBoth = partition.get(Membership.BOTH);
        for(Iterator<Leaf> iter = lBoth.iterator(); iter.hasNext();) {
            FileNode fn1 = (FileNode)iter.next();
            FileNode fn2 = (FileNode)iter.next();
            boolean areSame = compareDetails(fn1, fn2);
            LeafComparisonResult r = new LeafComparisonResult(fn1, fn2, true, true, areSame);
            resultList.add(r);
        }
        
        List<Leaf> lOnly1 = partition.get(Membership.ONLY1);
        List<Leaf> lOnly2 = partition.get(Membership.ONLY2);

        if ( isNameIgnoreCase ) {
            Collections.sort(lOnly1, Node.nameComparator);  
            Collections.sort(lOnly2, Node.nameComparator);  

            partition = namePartition(lOnly1, lOnly2, true);
            lBoth = partition.get(Membership.BOTH);
            lOnly1 = partition.get(Membership.ONLY1);
            lOnly2 = partition.get(Membership.ONLY2);
            for(Iterator<Leaf> iter = lBoth.iterator(); iter.hasNext();) {
                FileNode fn1 = (FileNode)iter.next();
                FileNode fn2 = (FileNode)iter.next();
                boolean areSame = compareDetails(fn1, fn2);
                LeafComparisonResult r = new LeafComparisonResult(fn1, fn2, true, true, areSame);
                resultList.add(r);
            }
        }
        
        for(Iterator<Leaf> iter = lOnly1.iterator(); iter.hasNext();) {
            FileNode fn1 = (FileNode)iter.next();
            LeafComparisonResult r = new LeafComparisonResult(fn1, null, true, false, false);
            resultList.add(r);
        }

        for(Iterator<Leaf> iter = lOnly2.iterator(); iter.hasNext();) {
            FileNode fn2 = (FileNode)iter.next();
            LeafComparisonResult r = new LeafComparisonResult(null, fn2, false, true, false);
            resultList.add(r);
        }

        // sort result list by name?
        Collections.sort(resultList, Node.nameComparator);  
        return resultList; 
    }
    
    /**
     * Compare details of two FileNodes
     * 
     * @param f1
     * @param f2
     * @return true if same, false otherwise
     * @throws IOException
     */
    private boolean compareDetails(FileNode f1, FileNode f2)throws IOException {
        // TODO use strategy, since least cost method is not always desirable.
        // might want to base decision on security.  For example, in case of a snapshot
        // may want to compare based on most secure hash.
        
        // Text comparison
        if ( isTextCompare ) {
            if ( f1.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT) < FileNode.COST_IMPOSSIBLE  
              && f2.getContentMethodCost(FileNode.CONTENT_METHOD_CONTENT_TEXT) < FileNode.COST_IMPOSSIBLE ) {
                // if isText determination fails do to IO problem but handler says it is ok
                // then eat exception but consider files to be different
                boolean isText1 = false;
                boolean isText2 = false;
                try {
                    isText1 = f1.isText();
                    isText2 = f2.isText();
                } catch ( IOException ioe ) {
                    if ( errorHandler.handleError(ioe) )
                        return false;
                    throw(ioe);
                }
                if ( isText1 && isText2 ) {
                    return compareDetails(f1, f2, FileNode.CONTENT_METHOD_CONTENT_TEXT);
                }
                if ( f1.isText() || f2.isText() ) {
                    return false; // one is text and the other isn't - thus not the same
                }
            }
        }
        
        // Binary comparison
        if ( f1.getSize() != f2.getSize() ) 
            return false;
        // determine cheapest way to compare files
        FileNode.ContentMethod bestMethod = null;
        double leastCost = FileNode.COST_IMPOSSIBLE;
        for(int i = 0 ; i < FileNode.CONTENT_METHODS.length; i++) {
            
            if ( FileNode.CONTENT_METHODS[i] == FileNode.CONTENT_METHOD_CONTENT_TEXT)
                continue;  // already did text logic above
                
            double c1 = f1.getContentMethodCost(FileNode.CONTENT_METHODS[i]);
            if ( c1 >= FileNode.COST_IMPOSSIBLE )
                continue;
            double c2 = f2.getContentMethodCost(FileNode.CONTENT_METHODS[i]);
            if ( c2 >= FileNode.COST_IMPOSSIBLE )
                continue;
            if ( c1+c2 < leastCost ) {
                leastCost = c1+c2;
                bestMethod = FileNode.CONTENT_METHODS[i];                
            }                
        }
        if ( leastCost >= FileNode.COST_IMPOSSIBLE ) {
            throw new IllegalArgumentException(
                MessageFormat.format(
                    Messages.getString("TreeComparor.compare_impossible"),  //$NON-NLS-1$
                    f1.getName(),
                    f2.getName()
                )
            );
        }
        // now compare with cheapest method
        return compareDetails(f1, f2, bestMethod);
    }

    /**
     * Compare details of two FileNodes
     * 
     * @param f1
     * @param f2
     * @return true if same, false otherwise
     * @throws IOException
     */
    private boolean compareDetails(FileNode f1, FileNode f2, FileNode.ContentMethod method)throws IOException {
        try {
            return f1.compareDetails(f2, method);
        } catch (IOException ioe) {
            if ( errorHandler.handleError(ioe) )
                return false;  // consider files to be different
            throw( ioe );
        }
    }

}
