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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemTestHelper {

    private File tempDir;
    
    public FileSystemTestHelper() throws IOException {   
        setupTempDir();
    }
    
    private void setupTempDir() throws IOException {
        tempDir = File.createTempFile("dir", "tmp");
        if ( tempDir.exists() ) {
            if ( ! tempDir.isFile() ) 
                throw new IOException("tempDir " + tempDir.getAbsolutePath() + " unexpected state");
            tempDir.delete();
        }
        if ( ! tempDir.mkdir() )
            throw new IOException("could not create tempDir " + tempDir.getAbsolutePath());
        tempDir.deleteOnExit();
    }

    public File createTestFile(String name, String content) throws IOException {
        return createTestFile(name, content, tempDir);
    }

    public File createTestFile(String name, byte[] content) throws IOException {
        File testFile = new File(tempDir, name);
        testFile.deleteOnExit();
        try ( FileOutputStream out = new FileOutputStream(testFile) ){
            out.write(content);
        }
        return testFile;
    }

    public File createTestFile(String name, String content, File dir) throws IOException {       
        File testFile = new File(dir, name);
        testFile.deleteOnExit();
        FileWriter out = null;
        try {
            out = new FileWriter(testFile);
            out.write(content);
            out.close();
            out = null;
        } finally {
            if ( out != null) try { out.close(); } catch (Exception e) {}
        }
        return testFile;
    }
    public File createTestDir(String name) throws IOException {       
        return createTestDir(name, tempDir );
    }
    public File createTestDir(String name, File dir) throws IOException {       
        File testDir = new File(dir, name);
        testDir.deleteOnExit();
        testDir.mkdir();
        return testDir;
    }

    public File createTestSymlink(File original, String name) throws IOException  {
        return createTestSymlink( original, name, tempDir);
    }
    
    public File createTestSymlink(File original, String name, File dir) throws IOException  {
        Path pSym = Paths.get(dir.getPath(), name);
        Path pTrg = Paths.get(original.getPath());
        Files.createSymbolicLink(pSym, pTrg);
        pSym.toFile().deleteOnExit();
        return pSym.toFile();
    }

    public File createTestSymlink(String original, String name, File dir) throws IOException  {
        Path pSym = Paths.get(dir.getPath(), name);
        Path pTrg = Paths.get(original);
        Files.createSymbolicLink(pSym, pTrg);
        pSym.toFile().deleteOnExit();
        return pSym.toFile();
    }
    
    public File createTestBadSymlink(String name) throws IOException  {
        Path pSym = Paths.get(tempDir.getPath(), name);
        Path pTrg = Paths.get(tempDir.getPath(), "noexist");
        Files.createSymbolicLink(pSym, pTrg);
        pSym.toFile().deleteOnExit();
        return pSym.toFile();
    }
    
    public File createTestFifo(String name) throws Exception  {
        return createTestFifo(name, tempDir);
    }
    
    public File createTestFifo(String name, File dir) throws Exception  {

        Path pFifo = Paths.get(dir.getPath(), name);
        Runtime runtime = Runtime.getRuntime();
        String[] cmd = { "mkfifo", pFifo.toString() };
        Process process = runtime.exec(cmd);
        int exitCode = process.waitFor();
        
        if ( exitCode != 0 ) {
            throw new IOException("could not create named pipe " + pFifo + " exit code " + exitCode);
        }
        pFifo.toFile().deleteOnExit();
        return pFifo.toFile();
        
    }
    
    
}
