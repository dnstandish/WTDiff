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
import java.util.zip.*;
import java.io.*;
import java.util.*;

/**
 * Node Tree builder to build a tree based on a Zip file
 * @author davidst
 */
public class ZipTreeBuilder implements NodeTreeBuilder {

    // name of Zip file from which to build tree
    private String zipFileName;

    /**
     * Constructor
     * 
     * @param zipFile Zip file from shich to build tree
     */
    public ZipTreeBuilder(String zipFile) {   
        zipFileName = zipFile;
    }
    
    /**
     * Build the Node tree from our Zip file
     */
    public DirNode buildTree(ErrorHandler handler) throws IOException {
        return load(handler);
    } 
    
    /**
     * Inner class that provides FileNode behaviour for a regular file 
     * in a Zip file
     * @author davidst
     */
    private class ZipFileNode extends FileNode {
        // Zip entry for this file
        private ZipEntry zipEntry;
        
        /**
         * MD5 sum of file content. Lazily initiated, NULL if we haven't computed it yet.
         */
        private byte[] md5;

        /**
         * Construct from a ZipEntry
         * 
         * @param ze Zip file etnry
         */
        public ZipFileNode(ZipEntry ze) {
            zipEntry = ze;
            // the name of the file is whatever follows the last slash
            String path = ze.getName();
            int lastSlash = path.lastIndexOf('/');
            if ( lastSlash < 0) {
                setName(path);  // no slash, simply the names
            } else if ( lastSlash < path.length()-1 ) {
                setName( path.substring(lastSlash+1)); // slash is not the last character
            } else {
                throw new IllegalArgumentException(
                    Messages.getString("ZipTreeBuilder.bug.must_be_file") + ze.getName() //$NON-NLS-1$
                );
            }
            
        }
        /**
         * Type of file (usually regular file)
         */
        public FileType getFileType() {
            return FileType.REGFILE;
        }
        /**
         * timestamp of file
         */
        public long getTime() {
            return zipEntry.getTime();
        }
        /**
         * CRC fof fiel content
         */
        public long getCrc() {
            return zipEntry.getCrc();
        }
        /**
         * Return MD5Sum of file content.  Note client will also need to check size.
         * The CRC32 isn't good enough for to guard against malicious file tampering.
         * For that should need a cryptographic hash.
         */
        public synchronized byte[] getMd5() throws IOException {
            if (md5 == null) {
                md5 = calculateMd5();
            }
            return md5;
        }

        /**
         * uncompressed size of file
         */
        public long getSize() {
            return zipEntry.getSize();
        }
        
        /** 
         * Class to wrap around input stream from a zip file that closes zip file when stream is closed.
         * 
         * @author davidst
         *
         */
        private class CloseZipWIthCloseInputStream extends InputStream {
            ZipFile zip = null;
            InputStream stream = null;
            public CloseZipWIthCloseInputStream(String zipFileName) throws IOException {
                zip =  new ZipFile(zipFileName);
                try {
                    stream = zip.getInputStream(zipEntry);
                } catch (Exception e) {
                    if (zip != null) {
                        try { zip.close(); } catch (Exception ignore) {}
                    }
                    throw(e);
                }
                
            }

            public int available()  throws IOException {
                return stream.available();
            }
            public void close()  throws IOException {
                try {
                    if ( stream != null )
                        stream.close();
                    if ( zip != null )
                        zip.close();
                } finally {
                    if (stream != null) {
                        try { stream.close(); } catch (Exception e) {}
                    }
                    if (zip != null) {
                        try { zip.close(); } catch (Exception e) {}
                    }
                }
            }
            public int read() throws IOException {
                return stream.read();
            }
            public int read(byte[] b) throws IOException {
                return stream.read(b);
            }
            public int read(byte[] b, int off, int len) throws IOException {
                return stream.read(b, off, len);
            }
            public void mark(int readlimit) {
                stream.mark(readlimit);
            }
            public void reset() throws IOException {
                stream.reset();
            }
            public boolean markSupported() {
                return stream.markSupported();
            }
            public long skip(long n) throws IOException {
                return stream.skip(n);
            }
        }
        /**
         * Input stream of file content.  It is clients responsibility to close the stream.
         */
        public InputStream getInputStream() throws IOException {
            return new CloseZipWIthCloseInputStream(zipFileName);
        }
        
        /**
         * Return "cost" of given content comparison method
         */
        public double getContentMethodCost(ContentMethod method) {
            if ( method == FileNode.CONTENT_METHOD_CONTENT )
                return FileNode.COST_HARD; // we need to cuncompress the content
            if ( method == FileNode.CONTENT_METHOD_CONTENT_TEXT )
                return FileNode.COST_HARD; // we need to cuncompress the content
            if ( method == FileNode.CONTENT_METHOD_CRC )
                return FileNode.COST_EASY; // CRC is easy, can get from the Zip entry
            if ( method == FileNode.CONTENT_METHOD_MD5 )
                return FileNode.COST_HARD; // CRC is easy, can get from the Zip entry
            return FileNode.COST_IMPOSSIBLE;  // Don't know this method.  Therefore impossible
        }
    }
    
    /**
     * Helper class for constructing a tree from path strings.  Since a Zip file 
     * does not have a recursive structure, we use this class to build a tree which 
     * we can subsequently convert into a DirNode structure,
     * 
     * @author davidst
     */
    private class DirTree {
        private String name;  // name of dir
        private Vector <DirTree> children = new Vector <> ();  // subdirs of this dir ordered by name
        private Vector <Leaf> leaves = new Vector <> (); // files in this dir
        private HashSet<String> leafNames = new HashSet<>();  // names of files in this dir
        
        /**
         * Construct from simple dir name
         * @param s
         */
        public DirTree(String s) {
            name = s;
        }
        /**
         * Retrun simple dir aname
         * @return
         */
        public String getName() {
            return name;
        }
        /**
         * Add subdir (child) under this dir if it does not already exist
         * 
         * @param child
         * @return Dirtree of child
         */
        public DirTree addChild(String child) {
            // go through dir's current children looking for child, inserting in correct place
            // if ordered list.  Note that possible performance enhancement would be to check 
            // last element before iterating though list.  Binary search would be better.
            // A hash might have been a better implementation choice.
            for(int i=0; i < children.size(); i++) {
                int cmp = ((DirTree)children.get(i)).getName().compareTo(child);
                if ( cmp == 0 ) {
                    // the child already exists, return it
                    return (DirTree)children.get(i);
                }
                if ( cmp > 0 ) {
                    // this entry's name is grater than the child.  Insert the child here 
                    DirTree childTree = new DirTree(child);
                    children.add(i, childTree);
                    return childTree;
                }
            }
            // This is a new child whose name is greater than any existing child.  append to list
            DirTree childTree = new DirTree(child);
            children.add(childTree);
            return childTree;
        }
        /**
         * Add a leaf to this dir unless there already is a leaf with this name.
         * 
         * @param leaf
         */
        public void addLeaf(FileNode leaf) {
            if ( ! haveLeafWithName(leaf.getName())) {
                leaves.add(leaf);
                leafNames.add(leaf.getName());
            }
        }
        
        /**
         * Do we already have a leaf with this name?
         * 
         * @param name
         * @return
         */
        public boolean haveLeafWithName(String name) {
            return leafNames.contains(name);
        }
        
        /**
         * Return subdirs of this dir
         * @return
         */
        public List <DirTree> getChildren() {
            return children;
        }
        /**
         * Return leaves of this dir
         * @return
         */
        public List<Leaf> getLeaves() {
            return leaves;
        }

        /**
         * Debugging method to print out tree recursively
         * 
         * @param prefix
         * @param totPrefix
         */
        public void dump(String prefix, String totPrefix ) {
            System.out.println(totPrefix + "name: '"+name+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            System.out.println(totPrefix + "leaves:"); //$NON-NLS-1$
            Iterator <Leaf> iterL = leaves.iterator();
            while (iterL.hasNext()) {
                System.out.println(totPrefix+prefix+"'"+iterL.next().toString()+"'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            System.out.println(totPrefix + "children:"); //$NON-NLS-1$
            Iterator <DirTree> iterC = children.iterator();
            while (iterC.hasNext()) {
                ((DirTree) iterC.next()).dump(prefix, totPrefix+prefix);
            }
        }
    }
    private DirNode load(ErrorHandler handler) throws IOException {
        // construct a DirTree from all the zip entries.  Later we sill convert
        // into final tree of nodes
        DirTree zipTree = new DirTree( (new File(zipFileName)).getName() );
        
        try ( ZipFile zip = new ZipFile(zipFileName) ) {

            java.util.Enumeration <? extends ZipEntry> entries = zip.entries();        
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                String path = ze.getName();
                // strip leading slash
                if (path.length() > 0 && path.charAt(0) == '/' ) {
                    path = path.substring(1);
                } 
                
                DirTree currTree = zipTree; // current directory starts at top
                while (path.length() > 0) {
                    int iNextSlash = path.indexOf('/');
                    if ( iNextSlash < 0 ) {
                        // note that directories should end with /. so this is not a directory
                        // add FileNode to current directory
                        ZipFileNode fileNode = new ZipFileNode(ze);
                        if ( currTree.haveLeafWithName(fileNode.getName())) {
                            // ZipFile seems to lookup zip entries by name, it is not clear
                            // which copy will be used if there are duplicates
                            ZipException e = new ZipException(
                                MessageFormat.format(
                                    Messages.getString("ZipTreeBuilder.duplicate_name_in_zip"),
                                    zipFileName,
                                    ze.getName()
                                )
                            );
                            if ( ! handler.handleError(e) ) {
                                throw e;
                            }
                        } else { 
                            currTree.addLeaf(fileNode);
                        }
                        break;
                    }
                    else if ( iNextSlash == 0 ) {
                        break;  // trailing slash of directory zip entry.  nothing more to do 
                    } else {
                        // use slash separated dir component to shift currTree into dir for path component 
                        String dir = path.substring(0, iNextSlash);                    
                        currTree = currTree.addChild(dir);
                        path = path.substring(iNextSlash+1);
                    }
                }
            }
        }
        // next step is to convert the dirTree into dirNodes
        DirNode dirNode = dirTree2DirNode(zipTree);
        dirNode.setRoot(zipFileName);
        return dirNode;
    }
    
    /**
     * Recursively DirTree structure into tree of DirNodes and FileNodes
     * 
     * @param tree
     * @return
     */
    private DirNode dirTree2DirNode(DirTree tree) {
        List <Leaf> fileList = tree.getLeaves();  // the leaves are File nodes 
        List <DirNode> dirList = new Vector <> (tree.children.size());  // Vector that will house DirNodes created from Dirtrees
        Iterator <DirTree> iter = tree.getChildren().iterator();
        while (iter.hasNext()) {
            DirTree childDirTree = iter.next();
            dirList.add( dirTree2DirNode(childDirTree) );  // convert this subtree into a DirNode
        }
        // finally create DirNode for this tree 
        DirNode thisDirNode = new DirNode(tree.getName(), fileList, dirList);
        return thisDirNode;
    }
    
    /**
     * static main for testing purposes
     * 
     * @param argv names of zipfiles given on command line
     */
    public static void main(String[] argv) {
        for(int i = 0 ; i < argv.length; i++) {
            try {
                ZipTreeBuilder builder = new ZipTreeBuilder(argv[i]);
                DirNode dir = builder.buildTree(new NoHandleErrorHandler());
                dir.dump(" ",""); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
}