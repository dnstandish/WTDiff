/*
Copyright 2018 David Standish

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.wtdiff.util.filter.NodeFilter;
import org.wtdiff.util.filter.NodeFilter.Result;

public class FilterTreeBuilder implements NodeTreeBuilder {

    private DirNode root;
    private NodeFilter filter;
    
    public FilterTreeBuilder(DirNode dirNode, NodeFilter nodeFilter) {
        root = dirNode;
        filter = nodeFilter;
    }
    @Override
    public DirNode buildTree(ErrorHandler handler) throws IOException {

        DirNode filteredDir;
        Result filterResult = filter.filterNode(root);
        if ( filterResult == Result.EXCLUDE ) {
            filteredDir = new DirNode( root.getName(), new ArrayList <Leaf>(0), new ArrayList <DirNode>(0) );
        }
        else {
            filteredDir = buildTree( handler, root );
        }
        filteredDir.setRoot(root.getRoot());
        return filteredDir;
    }

    private DirNode buildTree(ErrorHandler handler, DirNode dirNode) throws IOException {
        List<Leaf> leaves = new ArrayList <Leaf>();
        for ( Leaf n: dirNode.getLeaves()) {
            Result filterResult = filter.filterNode(n);
            if ( filterResult != Result.EXCLUDE ) {
                leaves.add(n);
            }
        }

        List<DirNode> dirs = new ArrayList <DirNode>();
        for ( DirNode dn: dirNode.getDirs()) {
            Result filterResult = filter.filterNode(dn);
            if ( filterResult == Result.EXCLUDE ) {
                continue;
            }
            DirNode filteredDir = buildTree(handler, dn);
            if ( filteredDir != null ) {
                dirs.add( filteredDir );
            }
        }
        
        return new DirNode(dirNode.getName(), leaves, dirs);
    }


}
