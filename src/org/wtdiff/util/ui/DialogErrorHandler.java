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

import javax.swing.JOptionPane;

import org.wtdiff.util.ErrorHandler;

public class DialogErrorHandler implements ErrorHandler {
    private final static String IGNORE_OPTION = Messages.getString("DialogErrorHandler.ingnore_option"); //$NON-NLS-1$
    private final static String IGNORE_ALL_OPTION = Messages.getString("DialogErrorHandler.ignore_all_option"); //$NON-NLS-1$
    private final static String CANCEL_OPTION = Messages.getString("DialogErrorHandler.cancel_option"); //$NON-NLS-1$
    private String[] options = { IGNORE_OPTION, IGNORE_ALL_OPTION, CANCEL_OPTION};
    
    private boolean ignoreAll = false;
    private boolean encounteredErrors = false;
    
    @Override
    public boolean handleError(Exception e) {
        
        encounteredErrors = true;
        if ( ignoreAll )
            return true;
        
        int response = JOptionPane.showOptionDialog(null,
            e.getMessage(),
            Messages.getString("DialogErrorHandler.error_title"), //$NON-NLS-1$
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            CANCEL_OPTION);
        if ( response == JOptionPane.CLOSED_OPTION || options[response].equals(CANCEL_OPTION))
            return false;
        if ( options[response].equals(IGNORE_ALL_OPTION) )
            ignoreAll = true;
        return true;
    }
    
    @Override
    public void logError(Exception e) {
        
        encounteredErrors = true;
        if ( ignoreAll )
            return;
        
        JOptionPane.showMessageDialog(null,
            e.getMessage(),
            Messages.getString("DialogErrorHandler.error_title"), //$NON-NLS-1$
            JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean encounteredError() {
        return encounteredErrors;
    }
    
    public void reset() {
        ignoreAll = false;
        encounteredErrors = false;
    }
}