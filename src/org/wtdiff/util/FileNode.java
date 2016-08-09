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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * A FileNode is a leaf node that is a file of some sort.  FileNodes 
 * can compare themselves to other FileNodes.  They can use different comparison methods.  
 * They also have an idea of the "cost" of different methods.
 * 
 * @author davidst
 *
 */
public abstract class FileNode extends Leaf {
 
    public enum FileType { REGFILE, SYMLINK, SPECIAL }

    /**
     * Enumeration of content comparison methods for type safety.
     * 
     * @author davidst
     *
     */
    public static final class ContentMethod {
    }
    
    /**
     * Compare content by CRC. Note does not check size.
     */
    public static final ContentMethod CONTENT_METHOD_CRC = new ContentMethod();
    /**
     * Compare content by MD5. Note does not check size.
     */
    public static final ContentMethod CONTENT_METHOD_MD5 = new ContentMethod();
    /**
     * Compare content by actual content.
     */
    public static final ContentMethod CONTENT_METHOD_CONTENT = new ContentMethod();
    /**
     * Compare content by content allowing for small differences in different text file formats.
     * For example treat LF, CRLF, CR as equivalent line separators.
     */
    public static final ContentMethod CONTENT_METHOD_CONTENT_TEXT = new ContentMethod();
    
    /**
     * These are the known content comparison methods
     */
    public static final ContentMethod[] CONTENT_METHODS = {
        CONTENT_METHOD_CRC, CONTENT_METHOD_MD5,  CONTENT_METHOD_CONTENT, CONTENT_METHOD_CONTENT_TEXT};
    
    /**
     * Rough factors for quantifying cost of content comparison methods.
     */
    public static final double COST_NOT_SET = -10e6;
    public static final double COST_EASY = 0;
    public static final double COST_MODERATE = 1.0;
    public static final double COST_HARD = 3.0;
    public static final double COST_VERY_HARD = 9.0;
    public static final double COST_IMPOSSIBLE = 10e6;
    
    /**
     * Do we know if this file is a text file?
     */
    private boolean isKnownText = false;
    /**
     * Is this file a text file?
     */
    private boolean isText = false;
    
    /**
     * What type of file is this?
     */
    abstract public FileType getFileType();
    
    /**
     * Timestamp of this file
     * 
     * @return
     */
    abstract public long getTime();
    
    /**
     * "Raw" size of this file
     * @return
     */
    abstract public long getSize();
    
    /**
     * CRC of this file
     * @return
     * @throws IOException
     */
    abstract public long getCrc() throws IOException;

    /**
     * Return an inputstream of this file.
     * 
     * @return
     * @throws IOException
     */
    abstract public InputStream getInputStream() throws IOException;
    
    /**
     * How difficult is it to use this content comparison method with this File?
     * @param method
     * @return
     */
    abstract public double getContentMethodCost(ContentMethod method);
    
    /**
     * Does this file look like text?
     * Currently not aware of most encodings (UTF-8 ...). 
     * 
     * @return true if is text, false otherwise
     * @throws IOException if problem accessing contents
     */
    public synchronized boolean isText() throws IOException {
        // use cached result if we have it.
        if (isKnownText)
            return isText;
        isText = isTextGuess();
        isKnownText = true;
        return isText;
    }

    /**
     * Is the content of this file available?  Note does not check permissions.
     *  
     * @return
     */
    public boolean isContentAccessible() {
        return getContentMethodCost(CONTENT_METHOD_CONTENT) < COST_IMPOSSIBLE;
    }
    
    /**
     * Force read to fill buffer unless end of file 
     * 
     * @param is stream to read from
     * @param buf buffer to fill
     * @return number of bytes read, or -1 if end of file
     * @throws IOException
     */
    private int fullRead(InputStream is, byte[] buf) throws IOException {
        int ntot = 0;
        while ( ntot < buf.length ) {
            int nread = is.read(buf, ntot, buf.length - ntot);
            if ( nread < 0 )  { // EOF
                return ntot > 0 ? ntot : nread;
            }
            ntot += nread;
        }
        return ntot;
    }

    /**
     * Try to guess if this file is a text file (similar to perl -T file operator.
     * Currently not aware of most encodings (UTF-8 ...). 
     * 
     * @return true if is text, false otherwise
     * @throws IOException if problem accessing contents
     */
    private boolean isTextGuess() throws IOException {
        if ( getFileType() != FileType.REGFILE ) {
            return false;
        }
        // TODO reimplement with a reader???  A reader might deal with encodings other than ASCII. 
        // On the other hand a read might blow up due to bad encoding for a true binary file.
        int BUFF_SIZE = 512;  // will look at first 512 bytes
        byte[] buff = new byte[BUFF_SIZE];
        int nread; // nread has the total number of bytes read
        try ( InputStream is = getInputStream() )
        { 
              nread = fullRead( is, buff ); // nread has the total number of bytes
        }
        
        if (nread <= 0)
            return false; // is an empty file text?  We'll say no.
        
        int nOdd = 0; // count of non printable bytes
        for (int i = 0 ; i < nread; i++) {
            if (buff[i] == 0)
                return false; // We don't allow NULL bytes in text
            if ( buff[i] >=  (byte)' ' && buff[i] < 127  || buff[i] == (byte)'\t' || buff[i] == (byte)'\n' || buff[i] == (byte)'\r' )
                continue;  // this is a normal  character, go onto the next one.
            nOdd++; // This one was odd.
        }
        if ( (double)nOdd / nread > 0.333 )
            return false;  // more than 1/3 were odd characters. Declare this as non-text
        
        return true; // could be text.
                  
    }
    
    /**
     * Compare this file to another file using given content comparison method
     * 
     * @param f2
     * @param method
     * @return True if same, false otherwise.
     * 
     * @throws IOException
     */
    public boolean compareDetails(FileNode f2, ContentMethod method) throws IOException {
        
        if ( this.getFileType() != f2.getFileType() ) {
            return false;
        }
        if ( this.getFileType() == FileType.SPECIAL || f2.getFileType() == FileType.SPECIAL ) {
            return false;
        }
        // why not check size?
        if ( method == CONTENT_METHOD_CRC ) {
            return this.getSize() == f2.getSize() && this.getCrc() == f2.getCrc();
        }
        else if ( method == CONTENT_METHOD_MD5 ) {
            return this.getSize() == f2.getSize() && Arrays.equals( this.getMd5(), f2.getMd5() );
        }
        else if ( method == CONTENT_METHOD_CONTENT) {
            return this.getSize() == f2.getSize() && compareContent(f2);
        }
        else if ( method == CONTENT_METHOD_CONTENT_TEXT) {
            return compareContentText(f2);
        }
        throw new IllegalArgumentException(Messages.getString("FileNode.illegal_content_method") + method); //$NON-NLS-1$
    }
     
    /**
     * Perform a byte by byte comparison
     * 
     * @param f2
     * @return true if same
     * @throws IOException
     */
    private boolean compareContent(FileNode f2) throws IOException {
        
        try (InputStream thisStream = this.getInputStream();
            InputStream thatStream = f2.getInputStream() )   
        {
            int BUF_SIZE = 512;
            byte[] thisBuf = new byte[BUF_SIZE];
            byte[] thatBuf = new byte[BUF_SIZE];
            
            // Will ignore possibility of non-blocking IO
            int thisN;
            int thatN;
            do {
                thisN = fullRead( thisStream, thisBuf );
                thatN = fullRead( thatStream, thatBuf );
            
                if (thisN != thatN ) 
                    return false;
                for(int i = 0 ; i < thisN; i++)
                    if (thisBuf[i] != thatBuf[i])
                        return false; 
            } while ( thisN > 0 ); // NOTE eof would yeild -1; 0 could be result of non-blocking IO
        }
        return true;
    }
    
    /**
     * Utility class to normalize text file formats to ignore differences in
     * line separators.  Will also ignore old DOS convention of putting a ^Z
     * at the end of a file.  This is a poorman's intput stream.  It only has read().
     * 
     * @author davidst
     *
     */
    private class CRLFFilter {
        int oldCh = 0; // Keep track of previous character from a look-ahead
        InputStream is; // The stream to filter
        
        /**
         * Construct
         * @param stream
         */
        public CRLFFilter( InputStream stream ) {
            is = stream;
        }
        
        /**
         * Read a single character from out input stream.  Normalize 
         * possible line separators.
         *  
         * @return
         * @throws IOException
         */
        public int read() throws IOException {
            // if we'vea character from a previous look-ahead
            // then return that
            if (oldCh != 0) {
                int temp = oldCh;
                oldCh = 0;
                return temp;
            }
            
            int ch = is.read();
            /**
             * If this char is a CR then check if next char is LF.
             * CR, CRLF, and LF all return a single LF
             */
            if ( ch == '\r' ) {
                ch = '\n';
                int next = is.read();
                if ( next != '\n' ) {
                    oldCh = next; // CR without LF remember the look-ahead char
                }
            } else if ( ch == '\032' ) { // DOS ^Z at end of file
                int next = is.read();
                if ( next >= 0 ) {
                    oldCh = next; // ^Z but wasn't EOF remember look-ahead
                } else {
                    ch = next; // we reached EOF, so return EOF
                }
                
            }
            return ch;
        }
    }
    
    /**
     * Perform a byte by byte text comparison 
     * 
     * @param f2
     * @return true if same
     * @throws IOException
     */
    private boolean compareContentText(FileNode f2) throws IOException {
        
        try (InputStream thisStream = this.getInputStream();
            InputStream thatStream = f2.getInputStream() ) 
        {
            CRLFFilter thisFiltered = new CRLFFilter(thisStream);
            CRLFFilter thatFiltered = new CRLFFilter(thatStream);
            
            int thisB;
            int thatB;
            
            // Will ignore possibility of non-blocking IO
            do {
                thisB = thisFiltered.read();
                thatB = thatFiltered.read();
                if (thisB != thatB ) // note this will take care of EOF too
                    return false;
            } while ( thisB >= 0 ); // NOTE eof would yeild -1
            
        }        
        return true;
    }

    /**
     * Calculate md5 digest
     * 
     * @return
     * @throws IOException
     */
    protected byte[] calculateMd5() throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }  catch (NoSuchAlgorithmException e) {
            throw new IOException(
                MessageFormat.format(
                    Messages.getString("FileNode.digest_not_available"), //$NON-NLS-1$
                    "MD5"
                )
            );
        }
        int bufSize = 512;  // Define this elsewhere?
        byte[] b = new byte[bufSize];
        try ( InputStream in = getInputStream()) 
        {
            int n;
            while( (n = in.read(b)) > 0 ) {
                md.update(b, 0, n);
            }
            return md.digest();
        }
        
    }

    
    /**
     * Return MD5Sum of file content.  Note client will also need to check size.
     * The CRC32 isn't good enough for to guard against malicious file tampering.
     * For that should need a cryptographic hash.
     */
    abstract public byte[] getMd5() throws IOException;
    
    /**
     * Return symbolic link path if this is a symbolic link,  otherwise empty string.
     * 
     * @return symbolic link path
     */
    public String getLinkTo() {
        if ( getFileType() != FileType.SYMLINK ) {
            return "";
        }
        
        try ( InputStream is = getInputStream() ) {
            long size = getSize();
//            if ( size > 512 )  
//                size = 512;
            byte[] bytes = new byte[(int)size];
            is.read(bytes);
            return new String(bytes);
        } 
        catch (IOException ioe) {
            return "?"; // TODO better default ?
        }
    }
}
