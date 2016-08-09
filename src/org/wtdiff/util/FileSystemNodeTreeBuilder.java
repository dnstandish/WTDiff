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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import org.wtdiff.util.FileNode.FileType;

import java.nio.file.*;

/**
 * Node Tree builder to build a tree based on some starting point in a file system
 *  
 * @author davidst
 *
 */
public class FileSystemNodeTreeBuilder implements NodeTreeBuilder {

    /**
     * Starting point in filesystem from which to build tree
     */
    private Path rootFile;
    /**
     * Constructor
     * 
     * @param path starting place in file system
     */
    public FileSystemNodeTreeBuilder(String path) {
        rootFile = Paths.get(path);
    }

    /**
     * Build the tree.  Note that if starting place in file system is a regular file,
     * will create an artificial DirNode to contain the file,
     * 
     * @return the constructed Node tree
     */
    public DirNode buildTree(ErrorHandler handler) throws IOException  {
        DirNode d;
        // note at top level follow symbolic links
        if ( ! rootFile.toFile().exists() ) { // nio Files.exists(path) returns false in Windows7 if missing read permission
            throw new IOException(
                MessageFormat.format(
                    Messages.getString("FileSystemNodeTreeBuilder.root_file_noexist"), //$NON-NLS-1$
                    rootFile
                )
            );
        }
        String root = rootFile.toString();
        if ( Files.isDirectory(rootFile) ) {
            // root is a directory. recursively build Node tree from files and dirs under it
            d = buildTree(rootFile, handler);
        } else {
            // root is not a directory. create an artificial DirNode to hold it.
            // Construction is trivial
            FileNode f = new FileSystemFileNode(
                    rootFile, 
                    Files.isRegularFile(rootFile) ? FileType.REGFILE : FileType.SPECIAL
            );
            d = new DirNode(f);
            if ( rootFile.getParent() == null ) {
                root = "";
            } else {
                root = rootFile.getParent().toString();
            }
        }
        // The top DirNode knows where is is rooted.  It is rooted at the filesystem
        // directory which holds our rootFile
        d.setRoot(root);    
        return d;
    }
    
    /**
     * Recursively build tree of files and dirs
     * 
     * @param dir directory to expand into node tree
     * @return DirNode representing dir and its children
     */
    private DirNode buildTree(Path dir, ErrorHandler handler)  throws IOException {
        // Make two passes through dir, one for dirs and one for files.
        // Would be more efficient to do this in one pass.
        
        ArrayList <Leaf> fileNodeList = new ArrayList <Leaf> ();
        ArrayList <DirNode> dirNodeList = new ArrayList <DirNode>();

        if ( ! Files.isReadable(dir) || ! Files.isExecutable(dir) ) {
            IOException e = new IOException(
                MessageFormat.format(
                    Messages.getString("FileSystemNodeTreeBuilder.dir_perm_denied"), //$NON-NLS-1$
                    dir.toFile().getAbsolutePath()
                )
            );
            if ( ! handler.handleError(e) )
                throw e;
            return new DirNode(dir.toFile().getName(), fileNodeList, dirNodeList);
        }

//        Path dirPath = Paths.get(dir.getPath());
        try ( DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir) ) {
            for ( Path p : dirStream ) {
                if ( Files.isSymbolicLink(p)) {
                    fileNodeList.add( new FileSystemFileNode(p) );
                } else if ( Files.isDirectory(p) ) {
                    DirNode aDir = buildTree( p, handler );
                    dirNodeList.add(aDir);
                } else {
                    fileNodeList.add( new FileSystemFileNode(p) );   
                }
            }
        }
        // now construct the DirNode for this dir
        return new DirNode(dir.toFile().getName(), fileNodeList, dirNodeList);
    }
    
    /**
     * staic main for testing purposes
     * 
     * @param argv
     */
    static public void main(String[] argv) {
        for(int i = 0 ; i < argv.length; i++) {
            try {
                FileSystemNodeTreeBuilder builder = new FileSystemNodeTreeBuilder(argv[i]);
                DirNode dir = builder.buildTree(new NoHandleErrorHandler());
                System.out.println("root: "+dir.getRoot()); //$NON-NLS-1$
                dir.dump(" ",""); //$NON-NLS-1$ //$NON-NLS-2$
            } catch (Exception e) {
                e.printStackTrace(); 
            }
        }

    }

}
