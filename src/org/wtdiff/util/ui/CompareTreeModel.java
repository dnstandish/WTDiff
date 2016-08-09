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

package org.wtdiff.util.ui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.EventListenerList;

import org.wtdiff.util.*;

/**
 * Class implementing TreeModel interface for a DirNode based tree.
 * TreeNode text is via Node.toString().
 *    
 * @author davidst
 *
 */
public class CompareTreeModel extends Object implements TreeModel {

    private EventListenerList listenerList = new EventListenerList();
    private DirNode rootNode; 
    public CompareTreeModel(DirNode root) {        
        rootNode = root;
    }
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    public Object getChild(Object parent, int index) {
        DirNode d = (DirNode)parent;
        int nDirs = d.getDirs().size();
        if ( index < 0 )
            return null;
        else if (index < nDirs )
            return d.getDirs().get(index);
        else if (index - nDirs < d.getLeaves().size() )
            return d.getLeaves().get(index - nDirs);
        else
            return null;
    }

    public int getChildCount(Object parent) {
        // JTree may call getChildCount() on a leaf in response to keyboard events
        if ( isLeaf(parent) )  
            return 0;
        
        DirNode d = (DirNode)parent;        
        return d.getDirs().size()  + d.getLeaves().size();
    }

    public int getIndexOfChild(Object parent, Object child) {
        DirNode d = (DirNode)parent;
        int dirIndex = d.getDirs().indexOf(child);
        if ( dirIndex >= 0  )
            return dirIndex;
        int leafIndex = d.getLeaves().indexOf(child);
        if ( leafIndex >= 0 )
            return leafIndex + d.getDirs().size();
        return -1;
    }
    
    public Object getRoot() {
        return rootNode;
    }

    public boolean isLeaf(Object node) {
        return  node instanceof Leaf;
    }

    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);

    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // not editable
    }
    
    public void presentationChange(Object source, TreePath path) {
        TreeModelEvent e = new TreeModelEvent(source, path);
        for( TreeModelListener listener: listenerList.getListeners(TreeModelListener.class)) {
            listener.treeNodesChanged(e);
        }
    }

}
