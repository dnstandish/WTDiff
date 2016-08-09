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

import org.apache.logging.log4j.Logger;

public class LoggingErrorHandler implements ErrorHandler {

    private Logger logger;

    private boolean isIgnore = false;
    private boolean encounteredErrors = false;
    
    public LoggingErrorHandler(Logger logger,  boolean ignore) {
        this.logger = logger;
        isIgnore = ignore;
    }
    @Override
    public boolean handleError(Exception e) {
        logger.error(e.getMessage());
        encounteredErrors = true;
        return isIgnore;
    }

    @Override
    public void logError(Exception e) {
        logger.error(e.getMessage());
        encounteredErrors = true;
    }

    
    @Override
    public boolean encounteredError() {
        return encounteredErrors;
    }
    
    @Override
    public void reset() {
        encounteredErrors = false;
    }
    
}
