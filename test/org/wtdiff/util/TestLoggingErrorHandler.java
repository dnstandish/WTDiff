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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.LoggingErrorHandler;

public class TestLoggingErrorHandler   {
    
    private static final Logger logger = LogManager.getRootLogger();

    @Test
    public void testHandlerLog() {
        
        ErrorHandler h = new LoggingErrorHandler(logger,  false);
        assertFalse(h.encounteredError());
        h.logError(new Exception("exception"));
        assertTrue(h.encounteredError());
    }
    
    @Test
    public void testHandlerNoIgnore() {
        
        ErrorHandler h = new LoggingErrorHandler(logger,  false);
        assertFalse(h.encounteredError());
        h.reset();
        assertFalse(h.encounteredError());
        assertFalse(h.handleError(new Exception("exception")));
        assertTrue(h.encounteredError());
        h.reset();
        assertFalse(h.encounteredError());
        
        
    }

    @Test
    public void testHandlerIgnore() {
        
        ErrorHandler h = new LoggingErrorHandler(logger,  true);
        assertFalse(h.encounteredError());
        h.reset();
        assertFalse(h.encounteredError());
        assertTrue(h.handleError(new Exception("exception")));
        assertTrue(h.encounteredError());
        h.reset();
        assertFalse(h.encounteredError());
        
        
    }

}
