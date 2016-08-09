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
package org.wtdiff.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import javax.xml.stream.XMLStreamException;

import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.NoHandleErrorHandler;
import org.wtdiff.util.NodeTreeBuilder;

public class XMLTreeBuilder implements NodeTreeBuilder{

    private Path root;
    public XMLTreeBuilder(String path) {
        root = Paths.get(path);
    }
    public DirNode buildTree(ErrorHandler handler) throws IOException {
        return buildTree(root, handler);
    }

    
    private DirNode buildTree(Path file, ErrorHandler handler) throws IOException {
        try {
            if ( ! file.toFile().exists() ) { // nio Files.exists(path) returns false in Windows7 if missing read permission
                throw new IOException(
                  MessageFormat.format(
                      Messages.getString("XMLTreeBuilder.file_noexist"),  //$NON-NLS-1$
                      file
                  )
                 );
            }
            if ( ! Files.isRegularFile(file) ) {
                throw new IOException(
                    MessageFormat.format(
                        Messages.getString("XMLTreeBuilder.file_notreg"),  //$NON-NLS-1$
                        file
                    )
                 );
            }
            if ( ! Files.isReadable(file) ) {
                throw new IOException(
                    MessageFormat.format(
                        Messages.getString("XMLTreeBuilder.file_noread"),  //$NON-NLS-1$
                        file
                    )
                 );
            }
        } catch (IOException e) {
            handler.logError(e);
            throw e;
        }
        return buildTree( new FileInputStream(file.toFile()), handler);
    }


    
    private DirNode buildTree(InputStream input, ErrorHandler handler) throws IOException {
        DirNode d = null;
        // note at top level follow symbolic links

        DirNodeXMLStreamReader snapshotReader = new DirNodeXMLStreamReader();
        
        try {
            d = snapshotReader.readSnapshot(input);
            //String subRoot = snapshotReader.getSnapshotInfo().get(DirNodeXMLStreamConstants.ELEMENT_CAPTURE_ROOT);
            if ( "".equals( d.getName() ) ) {
                //File rootFile = new File(root, d.getName());
                d.setName(root.toFile().getName());
                d.setRoot(root.toString());
            } else {
                //File rootFile = new File(root, subRoot);
                d = new DirNode(d);
                d.setName(root.toFile().getName());
                d.setRoot(root.toString());
                //d.setRoot(rootFile.getPath());
            }
        } catch (XMLStreamException e) {
            handler.logError(e); 
            throw new IOException(e);
        } catch (IOException e) {
            handler.logError(e);
            throw e;
        }
        return d;
    }

    public static boolean isXMLSnapshot(String file) throws IOException {
        Path f = Paths.get(file);
        if ( ! f.toFile().exists() ) { // nio Files.exists(path) returns false in Windows7 if missing read permission
            throw new IOException(
                MessageFormat.format(
                    Messages.getString("XMLTreeBuilder.file_noexist"),  //$NON-NLS-1$
                    file
                )
            );
        }
        if ( ! Files.isRegularFile(f) ) {
            return false;
        }
        try ( InputStream input = new FileInputStream(file) ){
            DirNodeXMLStreamReader reader = new DirNodeXMLStreamReader();
            return reader.isSnapshot(input);
        }
    }
    
    public static void main(String argv[]) throws Exception {
        XMLTreeBuilder builder = new XMLTreeBuilder(argv[0]);
        builder.buildTree(new NoHandleErrorHandler()).dump("", "");
        
    }
}
