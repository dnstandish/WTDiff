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

import javax.swing.text.Style;
import javax.swing.text.StyleContext;

import junit.extensions.abbot.ComponentTestFixture;

import org.junit.Test;

import abbot.tester.ComponentTester;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.wtdiff.util.ui.LimitedDigitsTextField;

public class TestLimitedDigitsTextField extends ComponentTestFixture {


    @Test
    public void testConstructor() { 

        try {
            new LimitedDigitsTextField(0);
            fail( "maxDigits 0 should throw exception");            
        } catch (IllegalArgumentException iae) {
            // this should happen
        }

        try {
            new LimitedDigitsTextField(-1);
            fail( "maxDigits -1 should throw exception");            
        } catch (IllegalArgumentException iae) {
            // this should happen
        }

        try {
            new LimitedDigitsTextField(1);
        } catch (IllegalArgumentException iae) {
            fail( "maxDigits -1 should not throw exception");            
        }
    }
    
    @Test
    public void testNonDigitEntry() {
        LimitedDigitsTextField tf = new LimitedDigitsTextField(10);
        showFrame(tf);
        ComponentTester tester = ComponentTester.getTester(tf);
        tester.actionKeyString(tf, " -1,a3dfhgh890");
        assertEquals("13890", tf.getText());
        assertEquals(13890, tf.getValue());
        
        tf.setText("ab2c3");
        assertEquals("23", tf.getText());
        assertEquals(23, tf.getValue());
    }
    
    @Test
    public void testMaxDigitsEntry() {
        LimitedDigitsTextField tf1 = new LimitedDigitsTextField(1);
        showFrame(tf1);
        assertEquals(0, tf1.getValue());
        ComponentTester tester1 = ComponentTester.getTester(tf1);
        tester1.actionKeyString(tf1, "1234567890");
        assertEquals("1", tf1.getText());
        assertEquals(1, tf1.getValue());
        LimitedDigitsTextField tf3 = new LimitedDigitsTextField(3);
        showFrame(tf3);
        ComponentTester tester3 = ComponentTester.getTester(tf3);
        tester3.actionKeyString(tf3, "1234567890");
        assertEquals("123", tf3.getText());
        assertEquals(123, tf3.getValue());
    }
    
    

}
