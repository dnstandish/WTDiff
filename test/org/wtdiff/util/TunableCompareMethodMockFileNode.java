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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import org.wtdiff.util.FileNode;

public class TunableCompareMethodMockFileNode extends MockFileNode {
    
    HashMap<ContentMethod, Double> contentMethodCosts = new HashMap<>();
    ContentMethod usedMethod = null;
    //CONTENT_METHOD_CRC, CONTENT_METHOD_CONTENT, CONTENT_METHOD_CONTENT_TEXT
    
    public TunableCompareMethodMockFileNode(String name, String content, Date time) {
        super( name, content, time);
        for ( ContentMethod method :CONTENT_METHODS )
            contentMethodCosts.put(method, COST_IMPOSSIBLE);
    }
    
    public ContentMethod getUsedMethod() {
        return usedMethod;
    }
    
    public void resetUsedMethod() {
        usedMethod = null;
    }
    
    public void setContentMethodCost(ContentMethod method, double cost) {
        contentMethodCosts.put(method, cost);
    }
    public double getContentMethodCost(ContentMethod method) {
        return contentMethodCosts.get(method).doubleValue();
    }
    
    public long getCrc() throws IOException { 
        if ( getContentMethodCost(CONTENT_METHOD_CRC) >= COST_IMPOSSIBLE ) {
            throw new IOException("BAD getCrc() invoked even though cost impossible");
        }
        return super.getCrc();
    }

    public boolean compareDetails(FileNode f2, ContentMethod method) throws IOException {
        if ( getContentMethodCost(method) >= COST_IMPOSSIBLE ) {
            throw new IOException("BAD compareDetails invoked with method" + method +  "even though cost impossible");
        }
        if ( method == CONTENT_METHOD_CONTENT_TEXT && ! isText() ) {
            throw new IOException("BAD compareDetails invoked with compare tetxt method even though not text");
        }
        usedMethod = method;
        return super.compareDetails( f2, method);
    }
    
}
