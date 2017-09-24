/*
Copyright 2017 David Standish

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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.TransferHandler;

public class FileDropHandler extends TransferHandler {

    private FileDropListener listener;
    
    public FileDropHandler( FileDropListener fdl) {
    
        listener = fdl;
        
    }

    public boolean canImport(TransferHandler.TransferSupport support) {
        for(DataFlavor flavour : support.getDataFlavors()) {
            if (flavour.isFlavorJavaFileListType()) {
                return true;
            }
        }        
        return false;
    }
    
    public boolean importData(TransferHandler.TransferSupport support) {
        if ( ! this.canImport(support) )
            return false;
        
        List<File> files;
        try {
            files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if ( files.isEmpty() )
                return false;
        }
        catch (UnsupportedFlavorException ufe) {
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        for(File file : files) {
            System.out.println(file.getPath());
        }
        return listener.filesDropped(files);
    }

}
