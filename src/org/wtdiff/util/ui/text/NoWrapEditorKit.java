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
package org.wtdiff.util.ui.text;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class NoWrapEditorKit extends StyledEditorKit {
    // based on http://java-sl.com/wrap.html
    static class NoWrapViewFactory implements ViewFactory {

        @Override
        public View create(Element elem) {
            String type = elem.getName();
            if ( type != null) {
                if ( type.equals(AbstractDocument.ContentElementName) ) {
                    return new LabelView(elem);
                }
                if ( type.equals(AbstractDocument.ParagraphElementName) ) {
                    return new NoWrapParagraphView(elem);
//                    return new ParagraphView(elem);
                }
                if ( type.equals(AbstractDocument.SectionElementName) ) {
                    return new BoxView(elem, View.Y_AXIS);
                }
                if ( type.equals(StyleConstants.ComponentElementName) ) {
                    return new ComponentView(elem);
                }
                if ( type.equals(StyleConstants.IconElementName) ) {
                    return new IconView(elem);
                }
            }
                
            return new LabelView(elem);
        }
        
    }
    
    ViewFactory defaultFactory=new NoWrapViewFactory();
    public ViewFactory getViewFactory() {
        return defaultFactory;
    }


}
