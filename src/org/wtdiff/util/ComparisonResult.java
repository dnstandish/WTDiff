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
 * Interface detailing the result of comparing 2 nodes
 * 
 * @author davidst
 */
public interface ComparisonResult {

    //TODO might be better to have the original nodes instead of their names!
    
    /**
     * Note that name1 and name2 can be different in the case where we
     * are ignoring differences in file name case 
     *
     * @return 1st node name 
     */
    public String getName1();

    /**
     * Note that name1 and name2 can be different in the case where we
     * are ignoring differences in file name case 
     *
     * @return 2nd node name 
     */
    public String getName2();
    
    /**
     * Set that the 2 nodes are the same
     * 
     * @param b true if same
     */
    public  void setAreSame(boolean b);
    
    /**
     * @return true if the result is that the 2 nodes are the same
     */
    public boolean areSame();
    
    /**
     * @return true if the corresponding first node does not exist
     */
    public boolean isMissing1();
    
    
    /**
     * @return true if the corresponding second node does not exist
     */
    public boolean isMissing2();
    
    /**
     * @return true if a both exist.  Does not imply they are the same
     */
    public boolean haveBoth();
    
}
