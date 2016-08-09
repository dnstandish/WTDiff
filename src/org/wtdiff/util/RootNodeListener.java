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
 * Listener Interface for root node changes  
 * @author davidst
 *
 */
public interface RootNodeListener {
    // should method inlcude old rrot node?  If so will it result in 
    // significantly increased memory use?
    
    /**
     * root node changed to d
     */
    public void  rootNodeChanged(DirNode d);

}
