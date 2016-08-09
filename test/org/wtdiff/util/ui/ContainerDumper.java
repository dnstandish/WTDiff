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

import java.awt.Component;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.MenuElement;
import javax.swing.text.Document;

public class ContainerDumper {

    /**
     * Utility method to assist creation of component find/match using Abbot
     *  
     * @param prefix cumulative prefix for nested output (normally empty string to start)  
     * @param cont
     */
    public static void dump(String prefix, Container cont) throws Exception {
        Component[] comps = cont.getComponents();
        for( Component comp: comps ) {
            System.out.println(prefix + comp.getClass().toString());
            if ( comp instanceof JButton ) {
                System.out.println(prefix + "button getText: " + ((JButton)comp).getText());
            }
            else if ( comp instanceof JLabel ) {
                System.out.println(prefix + "label getText: " + ((JLabel)comp).getText());
            }
            else if ( comp instanceof JTextField ) {
                System.out.println(prefix + "JTextField getText: " + ((JTextField)comp).getText());
            }
            else if ( comp instanceof JTextPane ) {
                Document doc = ((JTextPane)comp).getDocument();
                System.out.println(prefix + "JTextPane getText: " + doc.getText(0,doc.getLength()));
            }
            else if ( comp instanceof JMenuItem ) {
                System.out.println(prefix + "JMenuItem getText: " + ((JMenuItem)comp).getText());
            }
            else if ( comp instanceof JComboBox ) {
                dump(prefix + " ", (JComboBox)comp);
            } 
            else if ( comp instanceof Container ) {
                dump(prefix + " ", (Container)comp);
            } 
            else {
                System.out.println(prefix + comp.getClass());
            }
        }
    }
    
    public static void dump(String prefix, JPopupMenu menu) {
        for( Component child = menu; child != null; child = child.getParent() ) { 
            System.out.println(child.getClass());
        }
        for(MenuElement elem :  menu.getSubElements() ) {
            if ( elem instanceof JMenuItem ) {
                System.out.println( prefix + elem.getClass() + " " + ((JMenuItem)elem).getText() );
            }
            else {
                System.out.println( prefix + elem.getClass() );                
            }
                
        }
    }

    public static void dump(String prefix, JComboBox<?> combo) {
        for( int i = 0 ; i < combo.getItemCount(); i++) {
            System.out.println(combo.getItemAt(i));
        }
    }

}
