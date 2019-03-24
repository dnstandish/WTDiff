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
package org.wtdiff.util.filter;

import java.util.List;
import java.util.Vector;

import org.wtdiff.util.Node;

public class CompositeNodeFilter implements NodeFilter {

    private Vector<NodeFilter> filters = new Vector<NodeFilter>(); 
    public CompositeNodeFilter() {
    }
    
    public CompositeNodeFilter add(NodeFilter filter) {
        filters.add(filter);
        return this;
    }

    public CompositeNodeFilter add(List<NodeFilter> filterList) {
        filters.addAll(filterList);
        return this;
    }

    public List<NodeFilter> filters() {
        return filters;
    }
    
    public int size() {
        return filters.size();
    }
    
    @Override
    public Result filterNode(Node node) {
        // TODO Auto-generated method stub
        for( NodeFilter n: filters) {
            Result r = n.filterNode(node);
            if ( r != Result.NONE ) {
                return r;
            }
        }
        return Result.NONE;
    }

}
