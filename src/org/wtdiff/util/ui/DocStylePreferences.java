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
package org.wtdiff.util.ui;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

public class DocStylePreferences {
    
    private Preferences prefs;
    private String versionedRoot;
    
    public DocStylePreferences(Class aClass, String version) throws BackingStoreException {
        prefs = Preferences.userNodeForPackage(aClass);
        versionedRoot = aClass.getSimpleName() + "/" + version;
        if ( ! prefs.nodeExists(versionedRoot) ) {
            loadPreferences(aClass, version);
            prefs = Preferences.userNodeForPackage(aClass);
        }
    }
    private void loadPreferences(Class aClass, String version) {
        String pkg = aClass.getPackage().getName();
        String resourceName = pkg.replace('.', '/') + "/" + aClass.getSimpleName() + "." + version + ".prefs";
        try ( InputStream is =   this.getClass().getClassLoader().getResourceAsStream(resourceName) ) {
            
            if ( is == null ) // TODO should log this problem
                return;
            
            Preferences.importPreferences(is); // TODO should we log this activity?
        } catch (IOException ioe) {
            ioe.printStackTrace(); // TODO do something more
        } catch (InvalidPreferencesFormatException e) {
            e.printStackTrace();// TODO do something more
        }
    }
    
    public void applyPreferences(Style style, String ... path) throws BackingStoreException {
        // BUG in Java LabelView class, does look to style hierarchy in case of background
        // see https://bugs.openjdk.java.net/browse/JDK-4530474
        if ( ! style.isDefined(StyleConstants.Background) ) {            
            StyleConstants.setBackground(style,StyleConstants.getBackground(style) );
        }
        
        if (  ! prefs.nodeExists(versionedRoot) ) {
            return;
        }
        
        Preferences subPrefs = prefs.node(versionedRoot);
        
        StringBuilder sb = new StringBuilder();
        boolean haveFirst = false;
        for ( String part : path ) {
            if ( haveFirst ) {
                sb.append('/');
            } else {
                haveFirst = true;
            }
            sb.append(part);
        }
        String pathName = sb.toString();
        if ( ! subPrefs.nodeExists(pathName) ) {
            return;
        }
        Preferences specificPrefs = subPrefs.node(pathName);
        int defForeground = StyleConstants.getForeground(style).getRGB();
        int defBackground = StyleConstants.getBackground(style).getRGB();
        for( String key : specificPrefs.keys() ) {
            if ( Font.MONOSPACED.equals(key) && specificPrefs.getBoolean(key, false)) {
                StyleConstants.setFontFamily(style, Font.MONOSPACED);
            } else if ( "bold".equals(key) ) {
                StyleConstants.setBold(style, specificPrefs.getBoolean(key, false));
            } else if ( "italic".equals(key) ) {
                StyleConstants.setItalic(style, specificPrefs.getBoolean(key, false));
            } else if ( "fgcolor".equals(key) ) {
                Color fg = new Color(specificPrefs.getInt(key, defForeground));
                StyleConstants.setForeground(style, fg);
            } else if ( "bgcolor".equals(key) ) {
                Color bg = new Color(specificPrefs.getInt(key, defBackground));
                StyleConstants.setBackground(style, bg);
            }
        }
    }
}
