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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.*;

import org.wtdiff.util.xml.XMLTreeBuilder;

/**
 * Controller class to decouple user interface from logic of loading and comparing trees.
 * @author davidst
 *
 * <PRE>
 *      CompareController controller = new CompareController();
 *      controller.setTextCompare(true);
 *      controller.setIgnoreNameCase(false);
 *       
 *      controller.setOldRoot("a/b/xx");
 *      controller.setNewRoot("a.zip");
 *      controller.compare();
 *       
 *      DirNode r = controller.getCompareRootNode();
 * </PRE>
 */  
public class CompareController { //TODO change class name

    public enum NodeRole { OLD_ROOT, NEW_ROOT, CMP_ROOT }
    
    private ErrorHandler errorHandler;
    
    private String oldRoot;
    private String newRoot;
    
    /* root used of comparison may be different from true root 
     * due to realignment for comparison 
     */
    private String oldCompareRoot;
    private String newCompareRoot;
    
    private DirNode oldRootNode;
    private DirNode newRootNode;
    /* node used of comparison may be different from true node 
     * due to realignment for comparison 
     */
    private DirNode oldCompareNode;
    private DirNode newCompareNode;
    private ComparisonDirNode compareRootNode;
    
    /**
     * Comparison option to compare files as text files.  
     * This ignores variants in line endings for text files.
     * {@link TreeComparor#TreeComparor(boolean, boolean)}
     */
    private boolean isTextCompare = false;
    /**
     * Comparison option to ignore case in file names
     * {@link TreeComparor#TreeComparor(boolean, boolean)}
     */
    private boolean isIgnoreNameCase = false;
    
    /**
     * Listeners to be notified when the "Old" root node has been built into a tree 
     */
    private List <RootNodeListener> oldRootListenerList = new ArrayList <>(); 
    /**
     * Listeners to be notified when the "New" root node has been built into a tree 
     */
    private List <RootNodeListener> newRootListenerList  = new ArrayList <>();
    /**
     * Listeners to be notified when the "Comparison" root node has been built into a tree 
     */
    private List <RootNodeListener> compareRootListenerList = new ArrayList <>();

    /**
     * Have we currently forced the root of the old tree?
     */
    private boolean isForcedOldRoot;

    /**
     * Have we currently forced the root of the new tree?
     */
    private boolean isForcedNewRoot;
    
    /**
     * Constructor
     */
    public CompareController() {
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
     * Get error handler
     * 
     * @return
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }
    /**
     * Set option to perform "text" comparison for text files {@link TreeComparor}
     * 
     * @param compareAsText if true then use text comparison rules to text files, otherwise use binary comparison
     */
    public void setTextCompare(boolean compareAsText) {
        if ( isTextCompare != compareAsText ) {
            isTextCompare = compareAsText;
            setCompareRootNode(null); // old comparison is no longer valid
        }            
    }
    
    /**
     * Get current "text" comparison stting {@link #setTextCompare}
     * 
     * @return current setting
     */
    public boolean getTextCompare() {
        return isTextCompare;
    }
    
    /**
     * Set comparison option as to whether name comparison should be case insensitive.
     * {@link TreeComparor}
     * 
     * @param ingoreNameCase if true then ignore case in names
     */
    public void setIgnoreNameCase(boolean ingoreNameCase) {
        if ( isIgnoreNameCase != ingoreNameCase ) {
            isIgnoreNameCase = ingoreNameCase;
            setCompareRootNode(null); // old comparison is no longer valid
        }

    }
    
    /**
     * Get name case sensitive option {@link #setIgnoreNameCase(boolean)}
     * 
     * @return
     */
    public boolean getIgnoreNameCase() {
        return isIgnoreNameCase;
    }
    
    /**
     * String representing root of old tree
     * 
     * @return
     */
    public String getOldRoot() {
        return oldRoot;
    }

    /**
     * String representing root of new tree
     * 
     * @return
     */
    public String getNewRoot() {
        return newRoot;
    }
    
    /**
     * Construct string representing compare root based
     * on old and new roots.
     * 
     * @return compare root based on old and new roots
     */
    private String constructCompareRoot() {
        if ( oldCompareRoot == null || newCompareRoot == null)
            return ""; //$NON-NLS-1$
        return oldCompareRoot + " <> " + newCompareRoot; //$NON-NLS-1$
        
    }
    /**
     * String representing root of comparison tree
     * 
     * @return
     */
    public String getCompareRoot() {
        //if ( compareRootNode == null ) {
            return constructCompareRoot();
        //}
        //else {
        //    return compareRootNode.getRoot();
        //}
    }
    
    /**
     * Get string representing specified root
     * @param selector  old root; new root; comparison root
     * @return string representing specified root.  may be null {@link #getCompareRoot()}
     */
    public String getRoot(NodeRole selector) {
        switch (selector) {
            case OLD_ROOT: return getOldRoot(); 
            case NEW_ROOT: return getNewRoot();
            case CMP_ROOT: return getCompareRoot();
            default: throw new IllegalArgumentException(Messages.getString("CompareController.bug.unexpected_root_selector") + selector); //$NON-NLS-1$
        }        
    }
    
    /**
     * Set old root to specified "path" and type and build its tree.  Registered 
     * old root node listeners will be notified ({@link #addRootNodeListener(NodeRole, RootNodeListener)} 
     * Note that change in root will not trigger comparison, but will clear and existing comparison
     * 
     * @param root path of root
     * @throws IOException
     */
    public void setOldRoot(String root) throws IOException {
        oldRootNode = buildRoot(root);
        oldRootNode.sort();
        oldCompareNode = oldRootNode;
        oldRoot = root;        
        oldCompareRoot = root;
//        oldRoot = oldRootNode.getRoot();        
//        oldCompareRoot = oldRoot;
        isForcedOldRoot = false;
        if ( ! isForcedNewRoot ) {
            newCompareRoot = newRoot; // old may have been realigned, that realignment would no longer be valid
            newCompareNode = newRootNode;
        }
        notifyRootNodeListeners(oldRootNode, oldRootListenerList);
        setCompareRootNode(null); // old comparison is no longer valid
    }
    
    /**
     * Set new root to specified "path" and type and build its tree.  Registered 
     * new root node listeners will be notified ({@link #addRootNodeListener(NodeRole, RootNodeListener)}.
     * Note that change in root will not trigger comparison, but will clear any existing comparison
     * 
     * @param root path of root
     * @throws IOException
     */
    public void setNewRoot(String root) throws IOException {
        newRootNode = buildRoot(root);
        newRootNode.sort();
        newCompareNode = newRootNode;
        newRoot = root;
        newCompareRoot = root;
//        newRoot = newRootNode.getRoot();
//        newCompareRoot = newRoot;
        isForcedNewRoot = false;
        if ( ! isForcedOldRoot ) {
            oldCompareRoot = oldRoot; // old may have been realigned, that realignment would no longer be valid
            oldCompareNode = oldRootNode;
        }
        notifyRootNodeListeners(newRootNode, newRootListenerList);
        setCompareRootNode(null); // old comparison is no longer valid
    }
    
    /**
     * Force dirnode used as root for tree comparison to that given by path of names from 
     * real root for given role (old or new).  Forcing the compare root prevents realignment 
     * of this roles root for tree comparison. If the node given by namesPath is a FileNode, then 
     * a dummy dirNode containing only this file will be used.
     *  
     * @param role OLD_ROOT or NEW_ROOT
     * @param namesPath names of nodes including that of the root to the desired node.
     */
    public void forceCompareRoot(NodeRole role, List<String> namesPath) {
        List<Node> nodePath = new ArrayList<Node>(namesPath.size());
        
        DirNode rootNode;
        DirNode compareNode = null;
        String compareRoot = null;
        if ( role == NodeRole.OLD_ROOT )  {
            rootNode = oldRootNode;
        }  else if ( role == NodeRole.NEW_ROOT )  {
            rootNode = newRootNode;
        } else {
            throw new IllegalArgumentException(
                Messages.getString("CompareController.bug.unexpected_force_root_selector")
            );      
        }
        
        if ( rootNode == null ) {
            throw new IllegalStateException(
                Messages.getString("CompareController.bug.root_not_set_force")
            );      
        }
        if (namesPath.size() == 0 ) {
            throw new IllegalArgumentException(
                Messages.getString("CompareController.bug.force_root_bad_path")
            );     
        }
        if ( ! rootNode.getName().equals(namesPath.get(0)) ) {
            throw new IllegalArgumentException(      
                Messages.getString("CompareController.bug.force_root_bad_path")
            );     
        }
        if (namesPath.size() == 1 ) {
            compareNode = rootNode;
            compareRoot = buildRootString(rootNode.getRoot(),nodePath );
        } else {
            nodePath.add(rootNode);
            if ( ! rootNode.populatePathByNames(namesPath.subList(1,  namesPath.size()), nodePath) ) {
                throw new IllegalArgumentException(
                    Messages.getString("CompareController.bug.force_root_bad_path")
                );     
            }
            Node lastNode = nodePath.get(nodePath.size() - 1);
            if ( ! ( lastNode instanceof DirNode ) ) {                
                lastNode = new DirNode( (Leaf)lastNode );
                compareRoot = buildRootString(rootNode.getRoot(),nodePath.subList(0, nodePath.size()) );
            } else {
                compareRoot = buildRootString(rootNode.getRoot(),nodePath );
            }
            compareNode = (DirNode)(lastNode);
        }
            
        
        if ( role == NodeRole.OLD_ROOT )  {
            isForcedOldRoot = true;
            oldCompareNode = compareNode;
            oldCompareRoot = compareRoot;
            if ( ! isForcedNewRoot ) {
                newCompareNode = newRootNode;
                newCompareRoot = newRoot;
            }
            //notifyRootNodeListeners(oldRootNode, oldRootListenerList);
        } else if ( role == NodeRole.NEW_ROOT )  {            
            isForcedNewRoot = true;
            newCompareNode = compareNode;
            newCompareRoot = compareRoot;
            if ( ! isForcedOldRoot ) {
                oldCompareNode = oldRootNode;
                oldCompareRoot = oldRoot;
            }
            //notifyRootNodeListeners(newRootNode, newRootListenerList);
        }
        setCompareRootNode(null);
    }

    /**
     * If the root with given role is currently forced, change back to unforced root.
     * 
     * @param role OLD_ROOT or NEW_ROOT
     */
    public void unforceCompareRoot(NodeRole role) {
         if ( role == NodeRole.OLD_ROOT) {
            oldCompareNode = oldRootNode;
            oldCompareRoot = oldRoot;
            isForcedOldRoot = false;
            if ( ! isForcedNewRoot ) {
                newCompareNode = newRootNode;
                newCompareRoot = newRoot; // old may have been realigned, that realignment would no longer be valid
            }
        } else if ( role == NodeRole.NEW_ROOT) {
            newCompareNode = newRootNode;
            newCompareRoot = newRoot;
            isForcedNewRoot = false;
            if ( ! isForcedOldRoot ) {
                oldCompareNode = oldRootNode;
                oldCompareRoot = oldRoot; // old may have been realigned, that realignment would no longer be valid
            }
        } else { 
            throw new IllegalArgumentException(
                Messages.getString("CompareController.bug.unexpected_unforce_root_selector")
            );
        }
        setCompareRootNode(null);
    }

    /**
     * Set or clear compare root node notifying any listeners 
     * @param node
     */
    private void setCompareRootNode(ComparisonDirNode node) {
        compareRootNode = node;
        notifyRootNodeListeners(node, compareRootListenerList);
    }
    /**
     * Build tree for specified root path and type
     * 
     * @param root path of root
     * @return root node of constructed tree
     * @throws IOException
     */
    private DirNode buildRoot(String root) throws IOException {

        NodeTreeBuilder builder;
        
        if ( isZip(root) ) {
            builder = new ZipTreeBuilder(root);
        } else {
            if ( isXMLSnapshot(root) ) {
                builder = new XMLTreeBuilder(root);
            } else {
                builder = new FileSystemNodeTreeBuilder(root);
            }
        }
        return builder.buildTree(errorHandler);
    }
    
    /**
     * Register listener for build events for specified root
     * 
     * @param selector old root; new root; or comparison root
     * @param listener listener to notify
     */
    public void addRootNodeListener( NodeRole selector, RootNodeListener listener ) {
        switch ( selector ) {   
        case OLD_ROOT: oldRootListenerList.add(listener);
                break;
        case NEW_ROOT: newRootListenerList.add(listener);
                break;
        case CMP_ROOT: compareRootListenerList.add(listener);
                break;
        default: throw new IllegalArgumentException(Messages.getString("CompareController.bug.unexpected_root_listener") + selector ); //$NON-NLS-1$
        }
    }
    
    /**
     * Notify listeners that root node has changed to n
     * 
     * @param n new root node
     * @param listeners list of listeners to notify
     */
    private void notifyRootNodeListeners(DirNode n, List <RootNodeListener> listeners) {
        Iterator <RootNodeListener> iter = listeners.iterator();
        while ( iter.hasNext() ) {
            RootNodeListener listener = (RootNodeListener)iter.next();
            listener.rootNodeChanged(n);
        }
    }
    
    /**
     * Build root path string based on original root by appending directory path
     * represented by list of directories.
     * 
     * @param root base root
     * @param subpath list of nodes under root 
     * @return string representing root path of subpath 
     */
    private String buildRootString(String root, List<? extends Node> subpath) {
        if (subpath.size() == 0)
            return root;
        File newRootFile = new File(root);
        boolean isFirst = true;
        for( Node d: subpath) {
            // note skip first name since already reflected in root
            if ( isFirst ) {
                isFirst = false;
            }
            else {
                newRootFile = new File( newRootFile, d.getName());
            }
        }
        return newRootFile.getPath();
    }
    /**
     * Compare old an new trees.  If either old or new root hasn't been set. will do nothing.
     * Builds comparison tree and notifies registered compare root node listeners.
     * 
     * @throws IOException
     */
    public void compare() throws IOException {
        if ( oldRootNode == null || newRootNode == null )
            return;
        
//        if ( isForcedOldRoot || isForcedNewRoot ) {
//            if ( ! isForcedOldRoot ) {
//                oldCompareNode = oldRootNode; 
//            } else if ( ! isForcedNewRoot ) {
//                newCompareNode = newRootNode; 
//            }
//        } else {
        if ( !isForcedOldRoot || !isForcedNewRoot ) {
            DirNode oldNode =  isForcedOldRoot ? oldCompareNode : oldRootNode;
            DirNode newNode =  isForcedNewRoot ? newCompareNode : newRootNode;
            TreeAlignmentAnalyser taa = new TreeAlignmentAnalyser( isIgnoreNameCase ); 
            double matchFactor = taa.matchFactor(oldNode, newNode);
            int bestDepth = taa.findBestDepthAlignment(oldNode, newNode);
            System.out.println("match factor "+ matchFactor); //$NON-NLS-1$
            System.out.println("best depth "+ bestDepth); //$NON-NLS-1$            
            System.out.println("isForcedOldRoot " + isForcedOldRoot);
            System.out.println("isForcedNewRoot " + isForcedNewRoot);
            List<DirNode> pathToBest;
            if ( bestDepth < 0 && !isForcedNewRoot ) {
                pathToBest = taa.bestSubTree(-bestDepth, newNode, oldNode);
                newCompareRoot  = buildRootString(newRoot, pathToBest);
                newCompareNode = pathToBest.get( pathToBest.size() - 1 );
            } else if ( bestDepth > 0 && !isForcedOldRoot ) {
                pathToBest = taa.bestSubTree(bestDepth, oldNode, newNode);
                oldCompareRoot  = buildRootString(oldRoot, pathToBest);
                oldCompareNode = pathToBest.get( pathToBest.size() - 1 );
            } else {
                if ( !isForcedOldRoot )
                    oldCompareRoot = oldRoot;
                if ( !isForcedNewRoot )
                    newCompareRoot  = newRoot;
            }
        }
        TreeComparor cmp = new TreeComparor(isIgnoreNameCase, isTextCompare);
        cmp.setErrorHandler(errorHandler);
        ComparisonDirNode r = cmp.compare(oldCompareNode, newCompareNode);
        //r.setRoot(getCompareRoot());
        setCompareRootNode(r);
    }
    
    /**
     * Get comparison root node.  This does not invoke {@link #compare()}
     *  
     * @return current comparison root node
     */
    public ComparisonDirNode getCompareRootNode() {
        return compareRootNode;
    }
    
    /**
     * Get current new root node.  
     * @return
     */
    public DirNode getNewRootNode() {
        return newRootNode;
    }
    
    /**
     * Get current old root node.  
     * @return
     */
    public DirNode getOldRootNode() {
        return oldRootNode;
    }
    
    /**
     * Get current node per selector.  Note that asking for comparison root does not trigger comparison.
     *  
     * @param selector 1 - old root; 2 - new root; 3 comparison root
     * @return selected node if set, otherwise null
     */
    public DirNode getRootNode(NodeRole role) {
        switch (role) {
            case OLD_ROOT: return getOldRootNode(); 
            case NEW_ROOT: return getNewRootNode();
            case CMP_ROOT: return getCompareRootNode();
            default: throw new IllegalArgumentException(Messages.getString("CompareController.bug.unexpected_root_role") + role); //$NON-NLS-1$
        }        
    }

    /**
     * Return the node from the old tree from which comparison is based.
     * Note that this may differ from the root of the true old tree since
     * realignment may have taken place before comparison.
     * 
     * @return
     */
    public DirNode getOldCompareRootNode() {
        return oldCompareNode;
    }

    /**
     * Return the node from the new tree from which comparison is based.
     * Note that this may differ from the root of the true new tree since
     * realignment may have taken place before comparison.
     * 
     * @return
     */
    public DirNode getNewCompareRootNode() {
        return newCompareNode;
    }
    
    /**
     * Return the "path" string representing the old node from the comparison 
     * is based.  Note that this may differ from the root of the true new tree since
     * realignment may have taken place before comparison.
     * 
     * @return
     */
    public String getOldCompareRoot() {
        return oldCompareRoot;
    }

    /**
     * Return the "path" string representing the new node from the comparison 
     * is based.  Note that this may differ from the root of the true new tree since
     * realignment may have taken place before comparison.
     * 
     * @return
     */
    public String getNewCompareRoot() {
        return newCompareRoot;
    }
    
    /**
     * Try to guess if specified path is a zip file.  Will look at contents not name.
     * 
     * @param path path to file
     * @return true if it is a zip file
     * @throws IOException
     */
    private boolean isZip(String path) throws IOException {
        
        Path f = Paths.get(path);
        if ( Files.isDirectory(f) ) {
            return false;
        }

        try (ZipFile z = new ZipFile(path)) {
            z.size();
        } catch (ZipException ze) {
            return false;
        }
        return true;
    }
    
    private boolean isXMLSnapshot(String path) throws IOException {
        return XMLTreeBuilder.isXMLSnapshot(path);
    }
}
