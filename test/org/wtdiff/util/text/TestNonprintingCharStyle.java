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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.wtdiff.util.text.NonprintingCharStyle;

public class TestNonprintingCharStyle {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        assertEquals("as is", NonprintingCharStyle.ASIS.localizedString() );
    }

}
