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
import java.nio.file.*;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Set;
import java.io.IOException;

/**
 * Utility class for checking is OS dependent file system operations are supported.
 * 
 * @author davidst
 *
 */
public class OperationSupportTester {

    /**
     * temp directory where we try to create OS dependent files
     */
    private File testDir;
    private FileSystemTestHelper fsHelper;
    
    /**
     * Constructor
     * 
     * @throws IOException if problem setting up for testing
     */
    public OperationSupportTester() throws IOException {
        fsHelper = new FileSystemTestHelper();
        testDir = fsHelper.createTestDir("opsupporttest");
    }
    
    /**
     * Attempt to create a symbolic link using java.nio.file.createSymbolicLink()
     * 
     * @return true if symbolic links are supported
     * @throws IOException
     */
    public boolean isSymlinkSupported() throws IOException {
        Path pSym = Paths.get(testDir.getPath(), "symlink");
        Path pTrg = Paths.get(testDir.getPath(), "dummy");
        try {
            Files.createSymbolicLink(pSym, pTrg);
        } catch (UnsupportedOperationException ignore) {
            return false;
        } catch (SecurityException ignore) {
            return false;
        } catch (IOException ignore) {
            return false;
        } finally {
            Files.deleteIfExists(pSym);
        }
        return true;
    }
    
    /**
     * Does OS allow files whose names differ only by case to exist in same
     * directory?
     * 
     * @return
     * @throws IOException 
     */
    public boolean allowsDegenerateFileNames() throws IOException {
        File lower = fsHelper.createTestFile("casetest", "lower", testDir);
        File upper = fsHelper.createTestFile("CASETEST", "UPPER", testDir);
        int count = 0;
        for( String name: testDir.list()) {
            if("casetest".equalsIgnoreCase(name))
                count++;
        }
        lower.delete();
        upper.delete();
        return count >= 2;        
    }
    /**
     * Attempt to create a special file (a unix named pipe) using OS external program mkfifo.
     * 
     * @return true if special files are supported
     * @throws IOException
     */
    public boolean isSpecialFileSupported() throws IOException, InterruptedException {
        Path pFifo = Paths.get(testDir.getPath(), "fifo");
        int exitCode = -1;
        try {
            Runtime runtime = Runtime.getRuntime();
            String[] cmd = { "mkfifo", pFifo.toString() };
            Process process = runtime.exec(cmd);
            exitCode = process.waitFor();
        } catch (SecurityException ignore) {
            return false;
        } catch (IOException ignore) {
            return false;
        } finally {
            Files.deleteIfExists(pFifo);
        }
        return exitCode == 0;
    }
    
    private void aclSetPerm(Path f, AclEntryPermission perm,  boolean allow) {
        AclFileAttributeView view = Files.getFileAttributeView(f, AclFileAttributeView.class);
        if ( view == null )
            return;
        try {
            UserPrincipal owner = view.getOwner();
            List<AclEntry> acl = view.getAcl();
            boolean isDeniedPerm = false;
            boolean isAllowedPerm = false;
            for(AclEntry ent: acl) {
                if (ent.principal().equals(owner) ) {
                    if ( ent.permissions().contains(perm) ) {
                        if ( ent.type() == AclEntryType.DENY ) {
                            isDeniedPerm = true;                        
                        }
                        else if ( ent.type() == AclEntryType.ALLOW ) {
                            isAllowedPerm = true;
                        }
                    }
                }
            }
            if ( allow && isDeniedPerm ) {
                AclEntry entry = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(view.getOwner())
                    .setPermissions(perm)
                    .build();
                acl.add(0, entry);
                view.setAcl(acl);
            }
            if ( !allow && isAllowedPerm ) {
                AclEntry entry = AclEntry.newBuilder()
                    .setType(AclEntryType.DENY)
                    .setPrincipal(view.getOwner())
                    .setPermissions(perm)
                    .build();
                acl.add(0, entry);
                view.setAcl(acl);
            }
        } catch (IOException ioe) {
            ioe. printStackTrace();
        }
    }

    private void posixSetPerm(Path f, PosixFilePermission perm, boolean allow) {
        try {
            Set<PosixFilePermission> s = Files.getPosixFilePermissions(f);
            if ( s.contains(perm) ) {
                if ( ! allow ) {
                    s.remove(perm);
                }
            } else if ( allow ) {
                s.add(perm);
            }
            Files.setPosixFilePermissions(f, s);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException uoe) {
            // ignore, posix file perms nort supported
        }
    }

    public boolean setReadable(File f, boolean b) {
        Path path = f.toPath();
        if ( Files.isReadable(path) == b) 
            return true; // nothing to do
        
        f.setReadable(b);
        if ( Files.isReadable(path) == b) 
            return true; // done
        
        posixSetPerm(path, PosixFilePermission.OWNER_READ, b);
        if ( Files.isReadable(path) == b) 
            return true; // done
        
        aclSetPerm(path, AclEntryPermission.READ_DATA, b);            
        if ( Files.isReadable(path) == b) 
            return true; // done
        
        return false; 
    }

    public boolean setWritable(File f, boolean b) {
        Path path = f.toPath();
        if ( Files.isWritable(path) == b) 
            return true; // nothing to do
        
        f.setWritable(b);
        if ( Files.isWritable(path) == b) 
            return true; // done
        
        posixSetPerm(path,  PosixFilePermission.OWNER_READ, b);
        if ( Files.isWritable(path) == b) 
            return true; // done
        
        aclSetPerm(path, AclEntryPermission.WRITE_DATA, b);            
        if ( Files.isWritable(path) == b) 
            return true; // done
        
        return false; 
    }

    public boolean setExecutable(File f, boolean b) {
        Path path = f.toPath();
        if ( Files.isExecutable(path) == b) 
            return true; // nothing to do
        
        f.setExecutable(b);
        if ( Files.isExecutable(path) == b) 
            return true; // done
        
        posixSetPerm(path,  PosixFilePermission.OWNER_EXECUTE, b);
        if ( Files.isExecutable(path) == b) 
            return true; // done
        
        aclSetPerm(path, AclEntryPermission.EXECUTE, b);            
        if ( Files.isExecutable(path) == b) 
            return true; // done
        
        return false; 
    }

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws Exception {
        OperationSupportTester tester = new OperationSupportTester();
        System.out.println( "symlink supported " +  tester.isSymlinkSupported() );
        System.out.println( "special file supported " +  tester.isSpecialFileSupported() );
    }

}
