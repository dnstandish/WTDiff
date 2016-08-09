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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wtdiff.util.ui.DirCmpFrame;
/**
 * Main application class.  This application compares one directory structure
 * to another, one or both of which might be a zip file.  In future this application
 * may be expanded to to support other sources of directory structures.
 * 
 * This class parses any command line options, then initiates the comparison.
 *  
 * @author davidst
 *
 */
public class DirCmp {
    
    public enum Result { 
        SAME(0),
        SAME_WITH_ERRORS(4),
        DIFFERENT(8),
        DIFFERENT_WITH_ERRORS(12),
        HELP(9),
        FAILED(16);
        
        private final int exitCode;
        
        private Result(int exitCode) {
            this.exitCode = exitCode;
        }
        
        public int getExitCode() {
            return exitCode;
        }
    }

    private static final Logger logger = LogManager.getLogger(DirCmp.class.getName());

    static Option helpOption;
    static Option aboutOption;
    static Option ignoreCaseOption;
    static Option ignorePermErrorOption;
    static Option textCompareOption;
    static Option guiOption;
    static {
        aboutOption = new Option("a", "about", false, Messages.getString("DirCmp.opt.about.msg")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        helpOption = new Option("h", "help", false, Messages.getString("DirCmp.opt.help.msg")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ignoreCaseOption = new Option("i", "ignorecase", false, Messages.getString("DirCmp.opt.ignore_case.msg")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ignorePermErrorOption = new Option("p", "ignorepermerror", false, Messages.getString("DirCmp.opt.ignore_perm_error.msg")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        textCompareOption = new Option("t", "textcompare", false, Messages.getString("DirCmp.opt.textcompare.msg"));         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        guiOption = new Option("g", "gui", false, Messages.getString("DirCmp.opt.gui.msg"));         //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    public static Options buildOptions() {
    	Options opts = new Options();
        opts.addOption(aboutOption);
        opts.addOption(helpOption);
        opts.addOption(ignoreCaseOption);
        opts.addOption(ignorePermErrorOption);
        opts.addOption(textCompareOption);
        opts.addOption(guiOption);
        return opts;
    }
    
    /**
     * Run the GUI and wait for it to close
     * 
     * @param controller
     */
    private static void runGui( CompareController controller ) {
        
        final DirCmpFrame appFrame = new DirCmpFrame( controller );
        
        /*
         * the main thread doesn't wait for the frame to close
         * we tap into the frame window closing event using a lock
         * and wait for notification
         */
        final Object lock = new Object();
        appFrame.addWindowListener(
            new WindowAdapter() { 
                public void windowClosing(WindowEvent e) {
                    synchronized (lock) {
                        appFrame.setVisible(false);
                        lock.notify();
                    }
                }
           }
        );

        appFrame.setVisible(true);
        
        synchronized(lock) {
            while (appFrame.isVisible()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static Result process(String[] args) throws Exception {
        Options opts = buildOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cl = null;
        boolean help = false;
        boolean about = false;
        boolean isIgnoreCase = false;
        boolean isIgnorePermErrors = false;
        boolean isTextCompare = false;
        boolean isGui = false;
        try {
            cl = parser.parse(opts,args);
        } catch (ParseException pe) {
            System.err.println( pe.getMessage() );
            help = true;
        }
        if ( help || cl == null || cl.hasOption(helpOption.getOpt()) ) {
            HelpFormatter h = new HelpFormatter();
            h.printHelp(Messages.getString("DirCmp.usage"), opts); //$NON-NLS-1$
            return Result.HELP;
        }
        if ( cl.hasOption(aboutOption.getOpt()) ) {
            System.out.print(About.aboutText());
            return Result.HELP;
        }

        for (Iterator iter = cl.iterator(); iter.hasNext(); ) {
            Option o = (Option)iter.next();
            if ( o.equals(ignoreCaseOption) ) {
                isIgnoreCase = true;
            }
            else if ( o.equals(ignorePermErrorOption) ) {
                isIgnorePermErrors = true;
            }
            else if ( o.equals(textCompareOption) ) {
                isTextCompare = true;
            }
            else if ( o.equals(guiOption) ) {
                isGui = true;
            }
            else {
                throw new Exception(
                    MessageFormat.format(Messages.getString("DirCmp.opt.bug"), o)
                );
            }
        }

        List<String> what = cl.getArgList();
        if ( isGui ) {
            if ( what.size() > 2 ) {
                System.err.println( Messages.getString("DirCmp.required_max.msg")); //$NON-NLS-1$
                return Result.HELP;
            }
        } else if ( what.size() != 2 ) {
            System.err.println( Messages.getString("DirCmp.required.msg")); //$NON-NLS-1$
            return Result.HELP;
        }

        CompareController controller = new CompareController();
        ErrorHandler handler = new LoggingErrorHandler( logger, isIgnorePermErrors );
        controller.setErrorHandler(handler);
        
        controller.setTextCompare(isTextCompare);
        controller.setIgnoreNameCase(isIgnoreCase);
        Result result = Result.FAILED;
        try {
            if ( what.size() > 0 ) {
                controller.setOldRoot( (String)what.get(0) );
            }
            if ( what.size() > 1 ) {            
                controller.setNewRoot( (String)what.get(1) );
                controller.compare();
            }
            if ( isGui ) {
                runGui( controller );
                result =  Result.SAME;
            } else { 
                ComparisonDirNode r = controller.getCompareRootNode();
                r.dump(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                if ( r.areSame() ) {
                    if ( controller.getErrorHandler().encounteredError() )
                        result = Result.SAME_WITH_ERRORS;
                    else
                        result = Result.SAME;
                }
                else {
                    if ( controller.getErrorHandler().encounteredError() )
                        result = Result.DIFFERENT_WITH_ERRORS;
                    else
                        result =  Result.DIFFERENT;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) throws Exception {
        Result returnCode;
        try {
            returnCode = process(args);
        } catch (Exception e) {
            e.printStackTrace();
            returnCode = Result.FAILED;
        }
        System.exit(returnCode.getExitCode());
    }
}
