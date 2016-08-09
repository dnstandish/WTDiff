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
package org.wtdiff.util.text;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * Utility class for dealing with text
 * 
 * @author davidst
 *
 */
public final class TextUtil {

    // unicode character vertical box
    private static char WHITE_VERTICAL_RECTANGALE = 0x25af;
    

    /**
     * number of possible lines to read before guessing line separator
     */
    private static final int LINE_SEPARATOR_THRESHHOLD= 5;

    /**
     * carriage return only line separator
     */
    private static final char[] LINE_SEPARATOR_CR= { '\r' }; 
    /**
     * carriage return plus linefeed line separator
     */
    private static final char[] LINE_SEPARATOR_CRLF= { '\r', '\n' }; 
    /**
     * linefeed return only line separator
     */
    private static final char[] LINE_SEPARATOR_LF= { '\n' };
    /**
     * system default line separator
     */
    private static final char[] DEFAULT_LINE_SEPARATOR_CHARS = System.getProperty("line.separator").toCharArray();
    
    
    public enum LineSeparator {
        CR { char[] chars() { return  LINE_SEPARATOR_CR; } },
        LF { char[] chars() { return  LINE_SEPARATOR_LF; } },
        CRLF { char[] chars() { return  LINE_SEPARATOR_CRLF; } };
        
        abstract char[] chars();
    }

    public static LineSeparator DEFAULT_SEP;
    static {
        LineSeparator defSep = charsToLineSeparator(DEFAULT_LINE_SEPARATOR_CHARS);
        DEFAULT_SEP = defSep == null ? LineSeparator.LF: defSep;
    }

    /**
     * Utility class should not be instantiated
     */
    private TextUtil() {
    }

    public static LineSeparator charsToLineSeparator(char ... chars) {
        if ( chars == null)
            return null;
        if ( Arrays.equals(chars, LineSeparator.CR.chars() )) {
            return LineSeparator.CR;
        } else if ( Arrays.equals(chars, LineSeparator.LF.chars() )) {
            return LineSeparator.LF;
        } else if ( Arrays.equals(chars, LineSeparator.CRLF.chars() )) {
            return LineSeparator.CRLF;
        } else {
            return null;
        }
    }

    /**
     * read all lines from input into a list using system default line separator.  The
     * lines retain the trailing line separation characters. The last line will be included even
     * if not terminated with a line separator, but in that case the string returned for that
     * line will also be missing the line separator.
     * 
     * @param ir reader to read from
     * @return list of lines including line separation characters 
     * @throws IOException
     */
    public static List<String> readerToLines(Reader ir) throws IOException {
        return readerToLines(ir, DEFAULT_SEP);
    }

    /**
     * read all lines from input into a list using given line separator.  The
     * lines retain the trailing line separation characters.  The last line will be included even
     * if not terminated with a line separator, but in that case the string returned for that
     * line will also be missing the line separator.
     * 
     * @param ir ir reader to read from
     * @param lineSeparator
     * @return list of lines including line separation characters 
     * @throws IOException
     */
    public static  List<String> readerToLines(Reader ir, LineSeparator lineSeparator) throws IOException {
        Vector<String> lines = new Vector<>();
        for(String line = readLine(ir, lineSeparator) ; line.length() > 0  ; line = readLine(ir, lineSeparator) ) {
            lines.add(line);
        }
        return lines;
    }
    
    
    /**
     * read next line from input using given line separator.  The
     * lines retain the trailing line separation characters.  If the
     * last line is not terminated with a line separator, it will be returned, but
     * will also be missing the line separator.
     * 
     * @param ir ir reader to read from
     * @param lineSeparator
     * @return next line including line separation characters 
     * @throws IOException
     */
    public static String readLine(Reader ir, LineSeparator lineSeparator) throws IOException {
        char[] lineSepChars = lineSeparator.chars();
        int iSepMatch = 0;
        StringBuilder sb = new StringBuilder();        
        char c;
        while( true ) {
            int b = ir.read();
            if ( b < 0 )
                break;
            c = (char)b;
            sb.append(c);
            if ( c == lineSepChars[iSepMatch] ) {
                iSepMatch++;
                if ( iSepMatch >= lineSepChars.length ) {
                    break;
                }
            } else if ( iSepMatch > 0 && c == lineSepChars[0] ) { // This will work for CRLF but not for arbitrarily long sequence of line sep chars
                iSepMatch = 1;                
            }
            else {
                iSepMatch = 0;
            }
        }
        return sb.toString();    
    }
    
    /**
     * Return line with trailing line separator removed if present
     * 
     * @param line
     * @param lineSeparator
     * @return
     */
    public static String removeTrailingLineSeparator(String line, LineSeparator lineSeparator) {
        
        String ls = new String(lineSeparator.chars());
        if ( line.endsWith( ls ))
            return line.substring(0, line.length() - ls.length() );
     
        return line;
    }
    
    /**
     * Examine input to guess line separation convention.  May examine only a portion
     * of the input.  Can detect \r, \n, and \r\n conventions.
     * 
     * @param ir
     * @return
     * @throws IOException
     */
    public static LineSeparator guessLineSeparator(Reader ir) throws IOException {
        // look for \r\n; \n not preceeded by \r; \r not followed by \n
        int crlfCount = 0;
        int lfCount = 0;
        int crCount = 0;
        int lineCount = 0;
        boolean previousIsCr = false;
        while ( lineCount < LINE_SEPARATOR_THRESHHOLD ) {
            int ic = ir.read();
            if ( ic < 0  )
                break;
            
            char c = (char)ic;
            if ( c == '\n') {
                if ( previousIsCr ) {
                    crlfCount++;
                    previousIsCr = false;
                } else {
                    lfCount++;
                }
                lineCount++;
            }
            else {
                if ( previousIsCr ) {
                    previousIsCr = false;
                    crCount++;
                    lineCount++;
                }
                if ( c == '\r') {
                    previousIsCr = true;
                }
            }
        }
        if ( previousIsCr ) {
            crCount++;
            lineCount++;
        }
        
        if ( lineCount == 0 ) 
            return DEFAULT_SEP;
            
        if ( crlfCount >= lfCount &&  crlfCount >= crCount ) {
            return LineSeparator.CRLF; 
        }
        else if ( lfCount >= crCount ) {
            return LineSeparator.LF;
        } 
        else {
            return LineSeparator.CR;
        }
    }

    //TODO would following methods be better if descendants of a TextFilter class?
    
    /**
     * Return escape sequences (like \n), or 
     * control sequences (like ^@), or &lt;XX&gt; hex sequence
     * 
     * @param c
     * @return
     */
    public static String controlRepresentation(char c) {
        switch ( c ) {
            case '\n': return "\\n"; // Another possibility is to use the unicode control character symbols
            case '\r': return "\\r";
            case '\t': return "\\t";
            case '\f': return "\\f";
            default: break;  // not one of the above then continue below
        }
        if ( c < ' ') {
            return "^" + (char)('@' + c);
        }

        return "<" + Integer.toHexString(c).toUpperCase() + ">";  
    }

    /**
     * Substitute ISO control characters with escape sequences (like \n), 
     * or control sequences (like ^@), or &lt;XX&gt; hex sequences
     * 
     * @param s
     * @return
     */
    public static String visibleContolChars(String s) {
        StringBuilder sb = new StringBuilder();
        boolean foundControl = false;
        for( char c : s.toCharArray() ) {
            if ( Character.isISOControl(c) ) {
                sb.append( controlRepresentation(c) );
                foundControl = true;
            }
            else {
                sb.append(c);
            }
        }   
        return foundControl ? sb.toString(): s;
    }
    
    /**
     * Substitute ISO control characters with boxes
     * 
     * @param s
     * @return
     */
    public static String boxContolChars(String s) {
        StringBuilder sb = new StringBuilder();
        boolean foundControl = false;
        for( char c : s.toCharArray() ) {
            if ( Character.isISOControl(c) ) {
                sb.append( WHITE_VERTICAL_RECTANGALE );
                foundControl = true;
            }
            else {
                sb.append(c);
            }
        }   
        return foundControl ? sb.toString(): s;
    }

    /**
     * Remove ISO control characters
     * 
     * @param s
     * @return
     */
    public static String removeContolChars(String s) {
        StringBuilder sb = new StringBuilder();
        boolean foundControl = false;
        for( char c : s.toCharArray() ) {
            if ( Character.isISOControl(c) ) {
                foundControl = true;
            }
            else {
                sb.append(c);
            }
        }   
        return foundControl ? sb.toString(): s;
    }

    /**
     * Return string with all whitespace characters removed
     * 
     * @param s
     * @return
     */
    public static String removeWhiteSpace(String s) {
        boolean foundWS = false;
        StringBuilder sb = new StringBuilder();
        for( char c : s.toCharArray() ) {
            if ( Character.isWhitespace(c) ) {
                foundWS = true;
            }
            else {
                sb.append(c);
            }
        }
        return foundWS ? sb.toString() : s;
    }

    /**
     * Return string with whitespace characters normalized.
     * All white space characters normalized to single space character.  
     * 
     * @param s
     * @return
     */
    public static String normalizeWhiteSpace(String s) {
        boolean foundWS = false;
        boolean inWS = false;
        StringBuilder sb = new StringBuilder();
        for( char c : s.toCharArray() ) {
            if ( Character.isWhitespace(c) ) {
                if ( ! inWS ) {
                    sb.append(' ');
                    inWS = true;
                }
                foundWS = true;
            }
            else {
                sb.append(c);
                inWS = false;
            }
        }        
        return foundWS ? sb.toString() : s;
    }
    /**
     * Return string with whitespace characters normalized.
     * Leading and trailing whitespace removed.  All white space
     * characters normalized to space character.  
     * 
     * @param s
     * @return
     */
    public static String normalizeTrimWhiteSpace(String s) {
        return normalizeWhiteSpace( s.trim() );
    }

    public static String expandTabs(String s, int tabWidth) {
        if ( tabWidth < 1 ) {
            return s;
        }
        boolean foundTab = false;
        int curPos = 0;
        int nextTab = tabWidth;
        StringBuilder sb = new StringBuilder();
        for( char c : s.toCharArray() ) {
            if ( curPos == nextTab ) {
                nextTab += tabWidth;
            }
            if ( c == '\t' ) {
                foundTab = true;
                while ( curPos < nextTab ) {
                    sb.append(' ');
                    curPos++;
                }
            }
            else {
                sb.append(c);
                curPos++;
            }
        }        
        return foundTab ? sb.toString() : s;
        
    }
}
