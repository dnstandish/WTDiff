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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.LoggingErrorHandler;
import org.wtdiff.util.CompareController;

public class Snapshotter {

    private static final Logger logger = LogManager.getLogger(Snapshotter.class.getName());

    public Snapshotter() {
        
    }
    
    public int createSnapshot(String root, String outputFile) {
        Path rootFile = Paths.get(root);
        if ( ! rootFile.toFile().exists() ) { // nio Files.exists(path) returns false in Windows7 if missing read permission
            logger.error(
                MessageFormat.format(
                    Messages.getString("Snapshotter.file_noexist"),
                    root
                )
            );
            return 8;
        }
        if ( ! Files.isReadable(rootFile) ) {
            logger.error(
                MessageFormat.format(
                    Messages.getString("Snapshotter.file_noread"),
                    root
                )
            );
            return 8;
        }
        
        CompareController controller = new CompareController();
        ErrorHandler handler = new LoggingErrorHandler( logger, false );
        controller.setErrorHandler(handler);
        
        try ( FileOutputStream out = new FileOutputStream(outputFile) ) {
            controller.setOldRoot(root);  // TODO should really move core node building out of controller
            createSnapshot(outputFile, controller.getOldRootNode() );
        } catch ( IOException ioe ) {
            logger.error(Messages.getString("Snapshotter.ioexception"), ioe);
            return 16;
        } catch ( Throwable t ) {
            logger.error(Messages.getString("Snapshotter.throwable"), t);
            return 16; // TODO hardcode
        }
        return 0;
    }
    
    public void createSnapshot(String outputFile, DirNode rootDir ) throws IOException, XMLStreamException {
        try ( FileOutputStream out = new FileOutputStream(outputFile) ) {
            DirNodeXMLStreamWriter writer = new DirNodeXMLStreamWriter(out, Arrays.asList("CRC32", "MD5"));
            writer.writeDirNodeSnapShot(rootDir);
        }
    }

    static String usage() {
        return "usage: " + Snapshotter.class.getSimpleName() + " root snapshot"; // TODO usage
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        if ( args.length != 2 ) {
            System.err.println(usage());
            System.exit(9); // TODO hard code
        }
        else {
            Snapshotter snapshotter = new Snapshotter();
            int result = snapshotter.createSnapshot(args[0], args[1]);
            System.exit(result);
        }
    }

}
