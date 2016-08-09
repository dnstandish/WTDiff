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
 * Class to assist in analysing 2 trees of nodes. Possibly the trees may match best if 
 * one tree is compared to a subtree of the other.  This class doesn't do the best possible
 * job, but it should work in simple case where one tree is from a zip file that zipped up a directory
 * including the path to the directory, while the other tree starts at the directory itself.
 * <pre>
 *    zip:
 *       a/b/c/x.txt
 *    file system:
 *       c/x.txt
 * </pre>
 * The analysis is solely based on the names of things and their depth. 
 * 
 * @author davidst
 *
 */
public class TreeAlignmentAnalyser {
    // is case significant when comparing names?
    private boolean isIgnoreFileNameCase;
    
    /**
     * Construct an analyser with given sensitivity to case of names 
     * 
     * @param ignoreCase if true then not sensitive to upper/lower case of names
     */
    public TreeAlignmentAnalyser(boolean ignoreCase) {
        isIgnoreFileNameCase = ignoreCase;
    }
    
    /**
     * Without looking at detailed tree structure, what is the degree of matches between the
     * two trees based on names of nodes at the same depth in both trees.
     * 
     * @param d1 first tree
     * @param d2 second tree
     * @return match factor (inclusive range 0.0 to 1.0 with 0.0 being no matches, and 1.0 being all matches
     */
    public double matchFactor(DirNode d1, DirNode d2) {
        int n1;
        int n2;
        int nmatch;
        NameDepthListBuilder ndc = new NameDepthListBuilder(isIgnoreFileNameCase);
        List <NameDepth> nameDepths1 = ndc.buildNameDepthList(d1);
        n1 = nameDepths1.size();
        List <NameDepth> nameDepths2 = ndc.buildNameDepthList(d2);
        n2 = nameDepths2.size();
        Collections.sort(nameDepths1, NameDepth.nameDepthComparator);
        Collections.sort(nameDepths2, NameDepth.nameDepthComparator);
        IntBinCounter ibc = depthCompareOrderedLists(nameDepths1, nameDepths2);
        if ( ibc.getTotCount() == 0 ) { // tot count is number of name matches  
            return 0.0;
        }
        nmatch = ibc.getCount(0); // number of matches at exactly same depth
        return 2*nmatch / (double)(n1 + n2);  // match factor at same depth
    } 
    /**
     * Without looking at detailed tree structure, what is the degree of matches between the
     * two trees based on names of nodes at the same depth in both trees.
     * 
     * @param d1 first tree
     * @param d2 second tree
     * @return match factor (inclusive range 0.0 to 1.0 with 0.0 being no matches, and 1.0 being all matches
     */
    public int matchCount(DirNode d1, DirNode d2) {
        NameDepthListBuilder ndc = new NameDepthListBuilder(isIgnoreFileNameCase);
        List <NameDepth> nameDepths1 = ndc.buildNameDepthList(d1);
        List <NameDepth> nameDepths2 = ndc.buildNameDepthList(d2);
        Collections.sort(nameDepths1, NameDepth.nameDepthComparator);
        Collections.sort(nameDepths2, NameDepth.nameDepthComparator);
        IntBinCounter ibc = depthCompareOrderedLists(nameDepths1, nameDepths2);
        return ibc.getCount(0); // number of matches at exactly same depth
    } 
    
    /**
     * Without looking at child parent details.  Guesstimate whether one tree should be
     * considered as roughly equivalent to a subtree of the other.
     * 
     * @param d1 tree one
     * @param d2 tree two
     * @return  0 if trees best aligned at same depth. n &lt; 0 if d1 will align best n deep into d2; n &gt; 0 if d2 will best align n deep into d21
     */
    public int findBestDepthAlignment(DirNode d1, DirNode d2) {
        NameDepthListBuilder ndc = new NameDepthListBuilder(isIgnoreFileNameCase);
        List <NameDepth> nameDepths1 = ndc.buildNameDepthList(d1);
        List <NameDepth> nameDepths2 = ndc.buildNameDepthList(d2);
        Collections.sort(nameDepths1, NameDepth.nameDepthComparator);
        Collections.sort(nameDepths2, NameDepth.nameDepthComparator);
        IntBinCounter ibc = depthCompareOrderedLists(nameDepths1, nameDepths2);
        if ( ibc.getTotCount() == 0 ) {
            return 0;
        }
        int maxCount = 0;
        int maxCountDepth = 0;
        for (int depth = ibc.getMin(); depth <= ibc.getMax(); depth++ ) {
            int count = ibc.getCount(depth);
            if ( count > maxCount ) {
                maxCount = count;
                maxCountDepth = depth;
            }
        }
        return maxCountDepth;
        
    }
    
    
    /**
     * Compare 2 ordered lists of name/depth values and return an IntBinCounter with
     * number of name matches at various relative depths.  Note that this method does not
     * try to optimize the comparison by depth  
     * <pre>
     * example:
     *  l1 = { (a,1) (c,2) (d,3) (d,4) (e,2) }
     *  l2 = { (c,1) (d,4) (e,2) }
     *    a 1   -
     *    c 2   c 1   = +1
     *    d 3   d 4   = -1
     *    d 4   -     =  0
     *    e 3     e 2 =  0
     *  count of matched is 4
     *  max depth difference is +1
     *  min depth difference is -1
     *  count at depth difference -1 is 1
     *  count at depth difference  0 is 2
     *  count at depth difference +1 is 1
     * </pre>
     *  
     * @param l1
     * @param l2
     * @return
     */
    private IntBinCounter depthCompareOrderedLists(List <NameDepth> l1, List <NameDepth> l2) {
        IntBinCounter ibc = new IntBinCounter();
        ListIterator <NameDepth> iter1 = l1.listIterator();
        ListIterator <NameDepth> iter2 = l2.listIterator();
        // start each list.  handle case with a list is empty
        boolean isEnd1 = false; // have we reached the end of list 1?
        boolean isEnd2 = false; // have we reached the end of list 2?
        if (! iter1.hasNext() || ! iter2.hasNext() ) { // list should not be empty, but be defensive
            return ibc; // if one list is empty then no matches are possible;
        }
        NameDepth nd1 = iter1.next();
        NameDepth nd2 = iter2.next();
        
        // go through the lists counting the number of possible name matches depending on depth difference
        while( !isEnd1 && !isEnd2 ) {
            boolean isGet1 = true;  // start by assuming we will need to get the next value from list 1
            boolean isGet2 = true;  // similar for list 2
            // 
            int cmp = nd1.name.compareTo(nd2.name);
            if ( cmp < 0 ) { // name 1 < name 2 so name 1 is missing from list 2
                isGet2 = false; // it turns out that we won't need to get the next item from list 2
            } else if ( cmp > 0 ) {
                isGet1 = false; // it turns out that we won't need to get the next item from list 1
            } else {
                // the names are the same. add to count for relative depth difference
                // handle case of duplicate names in lists
                // warning - iter1 may be modified if duplicates pulled from list
                List<DepthDegeneracy> dupList1 = buildDuplicatesList( nd1, iter1 );
                List<DepthDegeneracy> dupList2 = buildDuplicatesList( nd2, iter2 );
                updateNameDepthCompareCounts( ibc, dupList1, dupList2);
            }
            // get the next item from list one if needed and if we haven't reached the end
            if ( isGet1 ) {
                if ( !iter1.hasNext() ) {
                    isEnd1 = true;
                } else {
                   nd1 = (NameDepth) iter1.next();
                }
            }
            // get the next item from list two if needed and if we haven't reached the end
            if ( isGet2 ) {
                if ( !iter2.hasNext() ) {
                    isEnd2 = true;
                } else {
                   nd2 = (NameDepth) iter2.next();
                }
            }
            
        }
        return ibc;
        
    }
    
    /** When comparing depths of names between two lists the situation becomes complicated
      if a name occurs more than once in a list.  If it occurs more than once at the same depth
      in a list (termed degeneracy here), we need even more care.  We are trying to 
      determine the theoretical maximum number of matches between the two lists.
      
      DepthDegeneracy helps capture the duplicates at a given depth
    **/ 
    private class DepthDegeneracy {
        public int depth;
        public int degeneracy = 1;
        public DepthDegeneracy(int depth) {
            this.depth = depth;
        }
    }

    /**
     * Build a list of depth degeneracies for the remaining elements in iter that
     * have the same name as theNd.  
     * 
     * @param theNd the name depth object currently pulled from iter 
     * @param iter the iterator from which to pull objects with the same name.  
     *     iter is assumed to be positioned directly after theNd.
     *     Any objects with the same name as theNd should be at the head of iter and
     *     should be in order of increasing depth
     *     On return iter will be positioned at the first object with a different name
     *     than theNd. 
     * @return a list of DepthDegeneracy objects
     */
    private List <DepthDegeneracy> buildDuplicatesList(NameDepth theNd, ListIterator <NameDepth> iter) {
        
        List <DepthDegeneracy> dupList = new ArrayList <> ();
        DepthDegeneracy current = new DepthDegeneracy(theNd.depth);
        dupList.add(current);
        
        while ( iter.hasNext() ) {
            NameDepth nextNd = iter.next();
            if ( ! nextNd.name.equals(theNd.name) ) {
                iter.previous();
                break;
            }
            if ( nextNd.depth == current.depth ) {
                current.degeneracy++;
            } else {
                current = new DepthDegeneracy(nextNd.depth);
                dupList.add(current);
            }
        }
        return dupList;
    }
    
    /**
     * This method increments counter to account for the possible matches at different 
     * depth offsets.
     * 
     * Simple case
     * l1  l2
     * 0   1
     * 1 match for offset -1
     * 
     * Simple duplicates in one list
     * l1  l2
     * 0   0
     * 1
     * 1 match for offset 0
     * 1 match for offset 1
     *
     * Simple duplicates in both lists
     * l1  l2
     * 0   1
     * 1   2
     * 2 matches for offset -1
     * 1 match for offset 0
     * 1 match for offset -2
     * 
     * Degenerate duplicates in one list
     * l1  l2
     * 0   0
     * 0
     * 1 match for offset 0
     * 
     * Degenerate duplicates in one list, simple duplicates in other
     * l1  l2
     * 0   0
     * 0   1
     * 1 match for offset -1
     * 1 match for offset 0
     * 
     * Degenerate duplicates in both lists
     * l1  l2
     * 0   0
     * 1   0
     * 1
     * 1 match for offset 0
     * 2 matches for offset 1
     * 
     * @param counter
     * @param depthDegeneracyList1
     * @param depthDegeneracyList2
     */
    private void updateNameDepthCompareCounts(IntBinCounter counter, List <DepthDegeneracy> depthDegeneracyList1,
      List <DepthDegeneracy> depthDegeneracyList2) {
        Iterator <DepthDegeneracy> iter1 = depthDegeneracyList1.iterator();
        while ( iter1.hasNext() ) {
            DepthDegeneracy dd1 = iter1.next();
            Iterator <DepthDegeneracy> iter2 = depthDegeneracyList2.iterator();
            while ( iter2.hasNext() ) {
                DepthDegeneracy dd2 = iter2.next();
                int minDegen = ( dd1.degeneracy < dd2.degeneracy ? dd1.degeneracy : dd2.degeneracy );
                for(int i = 0 ; i < minDegen; i++) {
                    counter.incr( dd1.depth - dd2.depth );
                }
            }
        }
        return;    
    }
    /**
     * Find best matching subtree in d1 for d2 at given depth
     *  
     * @param depth
     * @param d1
     * @param d2
     * @return dirnode in d1 at given depth that best matches d2 tree
     */
    public List<DirNode> bestSubTree(int depth, DirNode d1, DirNode d2) {
        if (depth < 0) {
            throw new IllegalArgumentException(Messages.getString("TreeAlignmentAnalyser.depth_negative")); //$NON-NLS-1$
        }
        IntRef bestCount = new IntRef();
        List <DirNode> bestPath = bestSubTree(depth, d1, d2, bestCount);
        
        System.out.println("best count " + bestCount.val); //$NON-NLS-1$
        return bestPath;
    }
        
    private class IntRef {
        public int val;
    }

    /**
     * Find best matching subtree in d1 for d2 at given depth, keeping track of 
     * best number of name "matches"  
     *  
     * @param depth
     * @param d1
     * @param d2
     * @param iRef
     * @return dirnode in d1 at given depth that best matches d2 tree
     */
    private List<DirNode> bestSubTree(int depth, DirNode d1, DirNode d2, IntRef iRef ) {
        if (depth == 0 ) {
            iRef.val = matchCount(d1, d2);
            List <DirNode> bestPath = new ArrayList<DirNode>();
            bestPath.add(d1);
            return bestPath;
        }
        List <DirNode> subDirs1 = d1.getDirs();
        List <DirNode> bestSubPath = new ArrayList<DirNode>(1);
        int bestMatchCount =  -1;
        for(Iterator <DirNode> iter = subDirs1.iterator(); iter.hasNext(); ) {
            DirNode sd1 = iter.next();
            IntRef nMatches = new IntRef(); 
            List <DirNode> subPath = bestSubTree(depth - 1, sd1, d2, nMatches);
            if (nMatches.val > bestMatchCount ) {
                bestMatchCount = nMatches.val;
                bestSubPath = subPath;
            }
        }
        iRef.val = bestMatchCount;
        bestSubPath.add(0, d1);
        return bestSubPath;
    }
}
