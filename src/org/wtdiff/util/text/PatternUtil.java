package org.wtdiff.util.text;

import java.util.regex.Pattern;

public class PatternUtil {

    public static String SimpleGlobToRegexp(String glob) {
        
        char[] globChars = glob.toCharArray();
        StringBuffer regex = new StringBuffer();
        regex.append('^');
        for (char c : globChars) {
            switch (c) {
                case '.': 
                case '+': 
                case '\\': 
                case '{': 
                case '}': 
                case '(': 
                case ')': 
                    regex.append("\\");
                    regex.append(c);
                    break;
                case '?': 
                    regex.append(".");
                    break;
                case '*': 
                    regex.append(".*");
                    break;
                default:
                    regex.append(c);
            }
        }
        regex.append('$');
        return regex.toString();
    }
    
    public static Pattern SimpleGlobToPattern(String glob) {
        String regexp = SimpleGlobToRegexp(glob); 
        return Pattern.compile(regexp);
    }

            
}
