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

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wtdiff.util.ComparisonDirNode;
import org.wtdiff.util.DirNode;
import org.wtdiff.util.ErrorHandler;
import org.wtdiff.util.Leaf;
import org.wtdiff.util.LeafComparisonResult;
import org.wtdiff.util.LoggingErrorHandler;
import org.wtdiff.util.CompareController;
import org.wtdiff.util.CompareController.NodeRole;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCompareController  {
    
    @Test
    public void testIsTextAndIgnoreNameCase() throws Exception {
        CompareController controller = new CompareController();
        assertFalse( "By default text compare should be off", controller.getTextCompare()) ;
        assertFalse( "By default ignore name case should be off", controller.getIgnoreNameCase() );
        controller.setTextCompare(true);
        assertTrue( "Text compare should now be on", controller.getTextCompare()) ;
        assertFalse( "Ignore name case should still be off", controller.getIgnoreNameCase() );
        controller.setIgnoreNameCase(true);
        assertTrue( "Text compare should still be on", controller.getTextCompare()) ;
        assertTrue( "Ignore name case now be on", controller.getIgnoreNameCase() );
        controller.setIgnoreNameCase(false);
        assertTrue( "Text compare should still be on", controller.getTextCompare()) ;
        assertFalse( "Ignore name case now be of", controller.getIgnoreNameCase() );
        controller.setTextCompare(false);
        assertFalse( "Text compare should now be off", controller.getTextCompare()) ;
        assertFalse( "Ignore name case now be of", controller.getIgnoreNameCase() );
        
        FileSystemTestHelper fileHelper = new FileSystemTestHelper();
        File simpleFile1 = fileHelper.createTestFile("simple1", "simple1-content");
        File simpleFile2 = fileHelper.createTestFile("simple2", "simple1-content");
        
        controller.setOldRoot(simpleFile1.getCanonicalPath());
        controller.setNewRoot(simpleFile2.getCanonicalPath());

        // changing ignore name case or text compare invalidate compare but only if a real change
        controller.compare();
        assertNotNull(controller.getCompareRootNode());
        controller.setTextCompare(controller.getTextCompare());
        assertNotNull(controller.getCompareRootNode());
        controller.setIgnoreNameCase(controller.getIgnoreNameCase());
        assertNotNull(controller.getCompareRootNode());
        
        controller.setTextCompare(! controller.getTextCompare());
        assertNull(controller.getCompareRootNode());
        
        controller.compare();
        assertNotNull(controller.getCompareRootNode());
        controller.setTextCompare(! controller.getTextCompare()); // test toggling both ways
        assertNull(controller.getCompareRootNode());

        
        controller.compare();
        assertNotNull(controller.getCompareRootNode());
        controller.setIgnoreNameCase(! controller.getIgnoreNameCase());
        assertNull(controller.getCompareRootNode());
        
        controller.compare();
        assertNotNull(controller.getCompareRootNode());
        controller.setIgnoreNameCase(! controller.getIgnoreNameCase()); // test toggling both ways
        assertNull(controller.getCompareRootNode());
        
    }
    
    @Test
    public void testSetRoot() throws Exception {
        CompareController controller = new CompareController();

        // root doesn't exist on file system
        try {
            controller.setNewRoot("noexist");
            fail("controller accepted non-existent file as new root");
        } catch (IOException e) {
            // should throw IO exception
        }
        try {
            controller.setOldRoot("noexist");
            fail("controller accepted non-existent file as old root");
        } catch (IOException e) {
            // should throw IO exception
        }

        FileSystemTestHelper fileHelper = new FileSystemTestHelper();
        File simpleFile1 = fileHelper.createTestFile("simple1", "simple1-content");
        File simpleFile2 = fileHelper.createTestFile("simple2", "simple1-content");
        controller.setOldRoot(simpleFile1.getCanonicalPath());
        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertEquals(controller.getOldRoot(),  simpleFile1.getPath());
        assertEquals(controller.getNewRoot(),  simpleFile2.getPath());
        assertEquals(controller.getOldRoot(), controller.getRoot(NodeRole.OLD_ROOT));
        assertEquals(controller.getNewRoot(), controller.getRoot(NodeRole.NEW_ROOT));

        assertEquals(controller.getOldRootNode().getRoot(),  simpleFile1.getParent());
        assertEquals(controller.getNewRootNode().getRoot(),  simpleFile2.getParent());
        controller.compare();
        assertEquals(controller.getCompareRoot(), controller.getRoot(NodeRole.CMP_ROOT));
        ComparisonDirNode compareNodeDifferent = controller.getCompareRootNode();
        assertFalse(compareNodeDifferent.areSame());
        controller.setNewRoot(simpleFile1.getCanonicalPath());
        controller.compare();
        ComparisonDirNode compareNodeSame= controller.getCompareRootNode();
        assertTrue(compareNodeSame.areSame());
        
        assertEquals(controller.getOldRootNode(), controller.getRootNode(NodeRole.OLD_ROOT));
        assertEquals(controller.getNewRootNode(), controller.getRootNode(NodeRole.NEW_ROOT));
        assertEquals(controller.getCompareRootNode(), controller.getRootNode(NodeRole.CMP_ROOT));
    }

    @Test
    public void testSetRootDir() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("testSetRootDir1");
        File testDir2 = helper.createTestDir("testSetRootDir2");
        
        CompareController controller = new CompareController();
        controller.setNewRoot(testDir1.getCanonicalPath());
        assertEquals(controller.getNewRoot(), testDir1.getCanonicalPath());
        controller.setOldRoot(testDir2.getCanonicalPath());
        assertEquals(controller.getOldRoot(), testDir2.getCanonicalPath());
        
    }

    @Test
    public void testRootOrder() throws Exception {
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("testSetRootDir1");
        helper.createTestFile("t2", "t2-content", testDir1);
        helper.createTestFile("t1", "t1-content", testDir1);
        
        CompareController controller = new CompareController();
        controller.setNewRoot(testDir1.getCanonicalPath());
        assertEquals("t1", controller.getNewRootNode().getLeaves().get(0).getName());
        assertEquals("t2", controller.getNewRootNode().getLeaves().get(1).getName());
        
        controller.setOldRoot(testDir1.getCanonicalPath());
        assertEquals("t1", controller.getOldRootNode().getLeaves().get(0).getName());
        assertEquals("t2", controller.getOldRootNode().getLeaves().get(1).getName());
        
    }

    @Test
    public void testRootNames() throws Exception {
        CompareController controller = new CompareController();
        
        assertNull(controller.getOldRoot());
        assertNull(controller.getNewRoot());
        assertEquals( "",controller.getCompareRoot());

        FileSystemTestHelper fileHelper = new FileSystemTestHelper();
        File simpleFile1 = fileHelper.createTestFile("simple1", "simple1-content");
        File simpleFile2 = fileHelper.createTestFile("simple2", "simple1-content");
        controller.setOldRoot(simpleFile1.getCanonicalPath());
        assertEquals(controller.getOldRoot(),  simpleFile1.getPath());
        assertNull(controller.getNewRoot());
        assertEquals( "",controller.getCompareRoot());
        
        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertEquals(controller.getOldRoot(),  simpleFile1.getPath());
        assertEquals(controller.getNewRoot(),  simpleFile2.getPath());
        assertEquals(controller.getCompareRoot(),
            simpleFile1.getPath() + " <> " + simpleFile2.getPath()
            );
        CompareController controller2 = new CompareController();
        controller2.setNewRoot(simpleFile2.getCanonicalPath());
        assertNull(controller2.getOldRoot());
        assertEquals(controller2.getNewRoot(),  simpleFile2.getPath());
        assertEquals( "",controller2.getCompareRoot());
    }
    
    @Test
    public void testSetRootZip() throws Exception {
        CompareController controller = new CompareController();

        ZipTestHelper helper = new ZipTestHelper();
        helper.addTestZipFile("simple1", "simple1-content", new Date() );
        File testZip1 = helper.createTestZipFile("simp1");
        File testZip2 = helper.createTestZipFile("simp2");
        
        controller.setOldRoot(testZip1.getCanonicalPath());
        controller.setNewRoot(testZip2.getCanonicalPath());
        assertEquals(controller.getOldRoot(),  testZip1.getCanonicalPath());
        assertEquals(controller.getNewRoot(),  testZip2.getCanonicalPath());
        assertEquals(controller.getOldRootNode().getRoot(),  testZip1.getCanonicalPath());
        assertEquals(controller.getNewRootNode().getRoot(),  testZip2.getCanonicalPath());
    }
    
    @Test
    public void testCompare() throws IOException {
        CompareController controller = new CompareController();
        
        assertNull(controller.getOldCompareRootNode());
        assertNull(controller.getNewCompareRootNode());
        assertNull(controller.getOldCompareRoot());
        assertNull(controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        controller.compare(); // should be harmless

        FileSystemTestHelper fileHelper = new FileSystemTestHelper();
        File simpleFile1 = fileHelper.createTestFile("simple1", "simple1-content");
        File simpleFile2 = fileHelper.createTestFile("simple2", "simple1-content");
        
        controller.setOldRoot(simpleFile1.getCanonicalPath());
        assertEquals(controller.getOldRootNode(), controller.getOldCompareRootNode());
        assertNull(controller.getNewCompareRootNode());
        assertEquals(controller.getOldRoot(), controller.getOldCompareRoot());
        assertNull(controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        controller.compare(); // should be harmless
        
        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertNull(controller.getCompareRootNode());
        assertEquals(controller.getNewRootNode(), controller.getNewCompareRootNode());
        assertEquals(controller.getNewRoot(), controller.getNewCompareRoot());

        controller.compare();
        assertNotNull(controller.getCompareRootNode());

        controller.setOldRoot(simpleFile1.getCanonicalPath());
        assertNull(controller.getCompareRootNode());
        controller.compare();
        assertNotNull(controller.getCompareRootNode());

        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertNull(controller.getCompareRootNode());
        controller.compare();
        assertNotNull(controller.getCompareRootNode());
        
        CompareController controller2 = new CompareController();
        controller2.setNewRoot(simpleFile2.getCanonicalPath());
        controller2.compare(); // should be harmless
    }

    @Test
    public void testCompareAlign() throws IOException {
        CompareController controller = new CompareController();
        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("testSetRootDir1");
        File testDir2 = helper.createTestDir("testSetRootDir2", testDir1);
        helper.createTestFile("simple1", "simple1-content", testDir2);
        
        // controller should realign old root down one level before real compare
        controller.setOldRoot(testDir1.getCanonicalPath());
        controller.setNewRoot(testDir2.getCanonicalPath());
        assertEquals(testDir1 + " <> " + testDir2, controller.getCompareRoot());
        controller.compare();
        ComparisonDirNode cmpNode1 = controller.getCompareRootNode();
        assertEquals(cmpNode1.getName(), "testSetRootDir2");
        assertEquals(cmpNode1.getDirs().size(), 0);
        assertEquals(cmpNode1.getLeaves().size(), 1);
        assertEquals(((Leaf)cmpNode1.getLeaves().get(0)).getName(), "simple1");
        assertEquals(testDir2 + " <> " + testDir2, controller.getCompareRoot());
        assertEquals(testDir2.getCanonicalPath(), controller.getOldCompareRoot());
        assertEquals(testDir2.getCanonicalPath(), controller.getNewCompareRoot());
        assertEquals(controller.getOldRootNode().getDirs().get(0), controller.getOldCompareRootNode());
        assertEquals(controller.getNewRootNode(), controller.getNewCompareRootNode());
        controller.setNewRoot(testDir1.getCanonicalPath());
        assertEquals(testDir1 + " <> " + testDir1, controller.getCompareRoot());
        controller.compare();
        assertEquals(testDir1 + " <> " + testDir1, controller.getCompareRoot());
        
        // controller should realign new root down one level before real compare
        controller.setOldRoot(testDir2.getCanonicalPath());
        controller.setNewRoot(testDir1.getCanonicalPath());
        controller.compare();
        ComparisonDirNode cmpNode2 = controller.getCompareRootNode();
        assertEquals(cmpNode2.getName(), "testSetRootDir2");
        assertEquals(cmpNode2.getDirs().size(), 0);
        assertEquals(cmpNode2.getLeaves().size(), 1);
        assertEquals(((Leaf)cmpNode2.getLeaves().get(0)).getName(), "simple1");
    }
    @Test
    public void testListening() throws IOException {
        StatefulListener listenerNew = new StatefulListener();
        StatefulListener listenerOld = new StatefulListener();
        StatefulListener listenerCompare = new StatefulListener();
        StatefulListener listenerAll = new StatefulListener();
        
        CompareController controller = new CompareController();
        
        controller.addRootNodeListener(NodeRole.OLD_ROOT, listenerOld);
        controller.addRootNodeListener(NodeRole.NEW_ROOT, listenerNew);
        controller.addRootNodeListener(NodeRole.CMP_ROOT, listenerCompare);
        controller.addRootNodeListener(NodeRole.OLD_ROOT, listenerAll);
        controller.addRootNodeListener(NodeRole.NEW_ROOT, listenerAll);
        controller.addRootNodeListener(NodeRole.CMP_ROOT, listenerAll);
        
        FileSystemTestHelper fileHelper = new FileSystemTestHelper();
        File simpleFile1 = fileHelper.createTestFile("simple1", "simple1-content");
        File simpleFile2 = fileHelper.createTestFile("simple2", "simple1-content");
        controller.setOldRoot(simpleFile1.getCanonicalPath());        
        assertTrue(listenerOld.notified);
        assertEquals(1, listenerOld.notifyCount(controller.getOldRootNode()));
        assertEquals(0, listenerOld.notifyCount(controller.getNewRootNode())); // null
        assertEquals(0, listenerOld.notifyCount(controller.getCompareRootNode())); // null
        assertFalse(listenerNew.notified);
        assertTrue(listenerCompare.notified);
        assertEquals(0, listenerCompare.notifyCount(controller.getOldRootNode())); 
        assertEquals(1, listenerCompare.notifyCount(controller.getNewRootNode())); // new is null at the moment
        assertEquals(1, listenerCompare.notifyCount(controller.getCompareRootNode())); // cmp is null at the moment
        assertTrue(listenerAll.notified);
        assertEquals(1, listenerAll.notifyCount(controller.getOldRootNode()));
        assertEquals(1, listenerAll.notifyCount(controller.getNewRootNode())); // new is null at the moment null
        assertEquals(1, listenerAll.notifyCount(controller.getCompareRootNode())); // cmp is null at the moment
        
        listenerOld.reset();
        listenerCompare.reset();
        listenerAll.reset();
        
        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertFalse(listenerOld.notified);
        assertTrue(listenerNew.notified);
        assertEquals(0, listenerNew.notifyCount(controller.getOldRootNode()));
        assertEquals(1, listenerNew.notifyCount(controller.getNewRootNode())); 
        assertEquals(0, listenerNew.notifyCount(controller.getCompareRootNode())); // cmp is null at the moment
        assertTrue(listenerCompare.notified);
        assertEquals(0, listenerCompare.notifyCount(controller.getOldRootNode())); 
        assertEquals(0, listenerCompare.notifyCount(controller.getNewRootNode()));
        assertEquals(1, listenerCompare.notifyCount(controller.getCompareRootNode())); // cmp is null at the moment

        assertTrue(listenerAll.notified);
        assertEquals(0, listenerAll.notifyCount(controller.getOldRootNode()));
        assertEquals(1, listenerAll.notifyCount(controller.getNewRootNode()));
        assertEquals(1, listenerAll.notifyCount(controller.getCompareRootNode())); // cmp is null at the moment

        listenerNew.reset();
        listenerCompare.reset();
        listenerAll.reset();
        
        controller.compare();
        assertFalse(listenerOld.notified);
        assertFalse(listenerNew.notified);
        assertTrue(listenerCompare.notified);
        assertEquals(0, listenerCompare.notifyCount(controller.getOldRootNode())); 
        assertEquals(0, listenerCompare.notifyCount(controller.getNewRootNode()));
        assertEquals(1, listenerCompare.notifyCount(controller.getCompareRootNode()));
        assertTrue(listenerAll.notified);
        assertEquals(0, listenerAll.notifyCount(controller.getOldRootNode()));
        assertEquals(0, listenerAll.notifyCount(controller.getNewRootNode()));
        assertEquals(1, listenerAll.notifyCount(controller.getCompareRootNode()));

        listenerCompare.reset();
        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertTrue(listenerCompare.notified);
        listenerCompare.reset();
        controller.setOldRoot(simpleFile2.getCanonicalPath());
        assertTrue(listenerCompare.notified);
        listenerCompare.reset();
        controller.compare();
        assertTrue(listenerCompare.notified);
        listenerCompare.reset();
        controller.setOldRoot(simpleFile2.getCanonicalPath());
        assertTrue(listenerCompare.notified);
        listenerCompare.reset();
        controller.setNewRoot(simpleFile2.getCanonicalPath());
        assertTrue(listenerCompare.notified);

    }

    @Test
    public void testErrorHandler() throws IOException {
        CompareController controller = new CompareController();
        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("testSetRootDir1");
        File testDir2 = helper.createTestDir("testSetRootDir2", testDir1);
        
        // default error handler should not all subdir perm error
        OperationSupportTester ost = new OperationSupportTester();
        assertTrue( ost.setReadable(testDir2, false) );
        try {
            // should we assume trees are built before compare?  
            controller.setOldRoot(testDir1.getCanonicalPath());
            controller.setNewRoot(testDir1.getCanonicalPath());
            controller.compare();
            fail("Default handler should not handle dir perm error");
        } catch (IOException ioe) {
            // this should happen
        }
        
        Logger logger = LogManager.getRootLogger();
        ErrorHandler h = new LoggingErrorHandler(logger,  true);
        controller.setErrorHandler(h);
        assertEquals( h, controller.getErrorHandler() );
        // should we assume trees are built before compare?  
        controller.setOldRoot(testDir1.getCanonicalPath());
        controller.setNewRoot(testDir1.getCanonicalPath());
        controller.compare();
        assertTrue(h.encounteredError());
    }
 
    /**
     * Test force root on tree compare alignment 
     * 
     * @throws IOException
     */
    @Test
    public void testForceRootNormal() throws IOException {
        /*
         * Test Trees
         *  d1/ { d12 { 2, sd/ { 1, 2 } } }
         *  and
         *  d2/ { 1, 2, d3/ { 3 } }
         *  
         *  normal realignment without forcing compares d1/d12/sd to d2
         */        
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("d1");
        File testDir12 = helper.createTestDir("d12", testDir1);
        File testFile121 =helper.createTestFile("1", "1", testDir12);
        File testDirSd = helper.createTestDir("sd", testDir12);
        File testFilesd1 = helper.createTestFile("1", "1", testDirSd);
        File testFilesd2 = helper.createTestFile("2", "2", testDirSd);
        
        File testDir2 = helper.createTestDir("d2");
        File testFile21 = helper.createTestFile("1", "1", testDir2);
        File testFile22 = helper.createTestFile("2", "2", testDir2);
        File testDir3 = helper.createTestDir("d3", testDir2);
        File testFile33 = helper.createTestFile("3", "3", testDir3);
        
        
        CompareController controller = new CompareController();
        
        // Listener notifications tests
        
        StatefulListener newListener = new StatefulListener();
        StatefulListener oldListener = new StatefulListener();
        StatefulListener cmpListener = new StatefulListener();
        controller.addRootNodeListener(NodeRole.NEW_ROOT, newListener);
        controller.addRootNodeListener(NodeRole.OLD_ROOT, oldListener);
        controller.addRootNodeListener(NodeRole.CMP_ROOT, cmpListener);
        
        // TEST unforce old/new compare roots when not loaded and not forced does not notify old/new listeners
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        //assertFalse(cmpListener.notified); // don't care yet
        controller.unforceCompareRoot(NodeRole.NEW_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        //assertFalse(cmpListener.notified); // don't care yet
        cmpListener.reset();
        
        // TEST load old root.  Old compare root and name initialized to old root.  Old listener and compare listener both notified 
        controller.setOldRoot(testDir1.getPath()); //------------------- OLD ROOT now set
        assertTrue(oldListener.notified);
        assertSame(controller.getOldRootNode(), oldListener.nodes.get(0));
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertNull(cmpListener.nodes.get(0));
        assertSame(controller.getOldRootNode(), controller.getOldCompareRootNode());
        assertEquals(controller.getOldRoot(), controller.getOldCompareRoot());
        assertNull(controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        assertEquals("", controller.getCompareRoot());
        
        oldListener.reset();
        newListener.reset();
        cmpListener.reset();

        // TEST unforce old/new compare root when old loaded and not forced, new not loaded, does not notify old/new listeners
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        //assertFalse(cmpListener.notified); // don't care yet
        controller.unforceCompareRoot(NodeRole.NEW_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        //assertFalse(cmpListener.notified); // don't care yet
        cmpListener.reset();

        DirNode d1Node = controller.getOldRootNode();
        DirNode d12Node = d1Node.childDirNodeByName("d12");
        DirNode sdNode = d12Node.childDirNodeByName("sd");
//        Leaf d12Leaf1 = d12Node.childLeafByName("1");
//        Leaf sdLeaf1 = sdNode.childLeafByName("1");
//        Leaf sdLeaf2 = sdNode.childLeafByName("2");

        // FORCE OLD ROOT
        // TEST: forcing old root does not notify old and new listeners.  Old compare root node and old compare root string reflect forced values
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1", "d12"));
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        //assertFalse(cmpListener.notified); // don't care yet
        assertSame(d1Node, controller.getOldRootNode());
        assertSame(d12Node, controller.getOldCompareRootNode());
        assertEquals(testDir12.getPath(), controller.getOldCompareRoot());
        assertNull(controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        assertEquals("", controller.getCompareRoot());
        cmpListener.reset();
        
        //UNFORCE OLD ROOT
        // TEST: unforcing old root does not notify old and new listeners.  Old compare root node and old compare rootstring reset to old root values        
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        //assertFalse(cmpListener.notified); // don't care yet
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        cmpListener.reset();

        // TEST: unforced pre-compare  old and new compare roots are same as old and new roots
        controller.setNewRoot(testDir2.getPath()); //------------------- OLD and NEW ROOT now set 
        assertFalse(oldListener.notified);
        assertTrue(newListener.notified);
        assertSame(controller.getNewRootNode(), newListener.nodes.get(0));
        assertTrue(cmpListener.notified);
        assertNull(cmpListener.nodes.get(0));
        assertSame(controller.getOldRootNode(), controller.getOldCompareRootNode());
        assertEquals(controller.getOldRoot(), controller.getOldCompareRoot());
        assertSame(controller.getNewRootNode(), controller.getNewCompareRootNode());
        assertEquals(controller.getNewRoot(), controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        assertEquals(controller.getOldRoot() + " <> " + controller.getNewRoot(), controller.getCompareRoot());
        
        oldListener.reset();
        newListener.reset();
        cmpListener.reset();

        DirNode d2Node = controller.getNewRootNode();
        DirNode d3Node = d2Node.childDirNodeByName("d3");
//        Leaf d2Leaf1 = d2Node.childLeafByName("1");
        Leaf d2Leaf2 = d2Node.childLeafByName("2");
//        Leaf d3Leaf3 = d3Node.childLeafByName("3");

        // FORCE NEW ROOT
        // TEST: forcing old root once old and new are loaded notifies compare listener but not old or new listeners.  
        //   Old compare root is old root and new compare root is forced value.
        //   Compare root string set based on old and new compare roots
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2", "d3"));
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldRootNode());
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertSame(d2Node, controller.getNewRootNode());
        assertSame(d3Node, controller.getNewCompareRootNode());
        assertEquals(testDir3.getPath(), controller.getNewCompareRoot());
        assertEquals(testDir1.getPath() + " <> " + testDir3.getPath(), controller.getCompareRoot());
        cmpListener.reset();
        
        // UNFORCE NEW ROOT
        // TEST: unforcing new root resets compare roots 
        controller.unforceCompareRoot(NodeRole.NEW_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldRootNode());
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertSame(d2Node, controller.getNewRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());
        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        assertEquals(testDir1.getPath() + " <> " + testDir2.getPath(), controller.getCompareRoot());
        cmpListener.reset();
        
        // TEST: compare with no forcing aligns old compare root to d1/d12/sd and new compare root to d2/ as expected  
        controller.compare();
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(sdNode, controller.getOldCompareRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());
        assertEquals(testDirSd.getPath(), controller.getOldCompareRoot());
        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        assertSame(controller.getCompareRootNode(), cmpListener.nodes.get(0));
        assertEquals("sd", controller.getCompareRootNode().getName());      
//        assertSame(sdNode, controller.getOldCompareRootNode());
//        assertEquals(testDirSd.getPath(), controller.getOldCompareRoot());
//        assertSame(d2Node, controller.getNewCompareRootNode());
//        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        assertEquals(testDirSd.getPath() + " <> " + testDir2.getPath(), controller.getCompareRoot());
        cmpListener.reset();
        
        // FORCE OLD ROOT
        // TEST: force old root to d1/d12 notifies compare listener and resets compare root node to NULL
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1", "d12"));
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldRootNode());
        assertSame(d12Node, controller.getOldCompareRootNode());
        assertEquals(testDir12.getPath(), controller.getOldCompareRoot());
        assertEquals(d2Node, controller.getNewCompareRootNode());
        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        assertEquals(testDir12.getPath() + " <> " + testDir2.getPath(), controller.getCompareRoot());
        cmpListener.reset();

        // TEST: compare with old compare root forced to d1/d12  new root not forced.
        //   no realignment since no shifting of new under d2/ results in more name matches.
        //   Compare tree top level is d12/ { 1, +2, -sd/, +d3 }
        controller.compare();
        assertTrue(cmpListener.notified);
        assertSame(d12Node, controller.getOldCompareRootNode());
        assertEquals(testDir12.getPath(), controller.getOldCompareRoot());
        assertEquals(d2Node, controller.getNewCompareRootNode());
        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        assertEquals("d12", controller.getCompareRootNode().getName());
        assertEquals(testDir12.getPath() + " <> " + testDir2.getPath(), controller.getCompareRoot());
        {
            DirNode compareNode = controller.getCompareRootNode();
            assertEquals(testDir12.getName(), compareNode.getName());
            assertEquals(2, compareNode.getDirs().size());
            ComparisonDirNode sdCompareNode =  (ComparisonDirNode)compareNode.childDirNodeByName("sd");
            assertTrue(sdCompareNode.isMissing2());
            ComparisonDirNode d3CompareNode =  (ComparisonDirNode)compareNode.childDirNodeByName("d3");
            assertTrue(d3CompareNode.isMissing1());
            assertEquals(2, compareNode.getLeaves().size());
            LeafComparisonResult result1 = (LeafComparisonResult)compareNode.childLeafByName("1");
            assertTrue( result1.haveBoth() );
            LeafComparisonResult result2 = (LeafComparisonResult)compareNode.childLeafByName("2");
            assertTrue(result2.isMissing1());
        }
        cmpListener.reset();

        // FORCE NEW ROOT
        // TEST: compare with old compare root forced to d1/d12  new root forced to d2/d3.
        //   no realignment both forced
        //   Compare tree top level is d12/ { -1, +3, -sd/ }
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2", "d3"));
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(d12Node, controller.getOldCompareRootNode());
        assertEquals(testDir12.getPath(), controller.getOldCompareRoot());
        assertSame(d3Node, controller.getNewCompareRootNode());
        assertEquals(testDir3.getPath(), controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        assertEquals(testDir12.getPath() + " <> " + testDir3.getPath(), controller.getCompareRoot());
        
        controller.compare();
        
        assertTrue(cmpListener.notified);
        assertSame(d12Node, controller.getOldCompareRootNode());
        assertEquals(testDir12.getPath(), controller.getOldCompareRoot());
        assertEquals(d3Node, controller.getNewCompareRootNode());
        assertEquals(testDir3.getPath(), controller.getNewCompareRoot());
        assertEquals("d12", controller.getCompareRootNode().getName());
        assertEquals(testDir12.getPath() + " <> " + testDir3.getPath(), controller.getCompareRoot());
        {
            DirNode compareNode = controller.getCompareRootNode();
            assertEquals(testDir12.getName(), compareNode.getName());
            assertEquals(1, compareNode.getDirs().size());
            ComparisonDirNode sdCompareNode =  (ComparisonDirNode)compareNode.childDirNodeByName("sd");
            assertTrue(sdCompareNode.isMissing2());
            assertEquals(2, compareNode.getLeaves().size());
            LeafComparisonResult result1 = (LeafComparisonResult)compareNode.childLeafByName("1");
            assertTrue( result1.isMissing2() );
            LeafComparisonResult result3 = (LeafComparisonResult)compareNode.childLeafByName("3");
            assertTrue(result3.isMissing1());
        }
        cmpListener.reset();

        //UNFORCE OLD ROOT
        // TEST: unforce old compareroot. New compare root stays forced at d2/d3. Compare listener notified.
        //   Old compare root is d1/  
        //   Compare root node is null.
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertSame(d3Node, controller.getNewCompareRootNode());
        assertEquals(testDir3.getPath(), controller.getNewCompareRoot());
        assertNull(controller.getCompareRootNode());
        assertEquals(testDir1.getPath() + " <> " + testDir3.getPath(), controller.getCompareRoot());
        cmpListener.reset();

        // TEST: unforced old compare root. New compare still forced at d2/d3. Compare.
        //   Compare listener notifed
        //   Old compare root is d1/ since no better alignment exists 
        //   Compare tree top level is d1/ { -d12/ +3 }
        controller.compare();
        
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertEquals(d3Node, controller.getNewCompareRootNode());
        assertEquals(testDir3.getPath(), controller.getNewCompareRoot());
        assertEquals("d1", controller.getCompareRootNode().getName());
        assertEquals(testDir1.getPath() + " <> " + testDir3.getPath(), controller.getCompareRoot());
        {
            DirNode compareNode = controller.getCompareRootNode();
            assertEquals(testDir1.getName(), compareNode.getName());
            assertEquals(1, compareNode.getDirs().size());
            ComparisonDirNode d12CompareNode =  (ComparisonDirNode)compareNode.childDirNodeByName("d12");
            assertTrue(d12CompareNode.isMissing2());
            assertEquals(1, compareNode.getLeaves().size());
            LeafComparisonResult result3 = (LeafComparisonResult)compareNode.childLeafByName("3");
            assertTrue(result3.isMissing1());
        }
        cmpListener.reset();


        //FORCE NEW ROOT to d2/2 leaf
        // TEST: Force new compare root to a leaf ( d2/2 ), old comapre root still unforced
        //   new compare root now uses artificial root "" with existing d2/2 leaf
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2", "2"));
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertNotSame(d2Node, controller.getNewCompareRootNode());
        assertEquals(0, controller.getNewCompareRootNode().getDirs().size());
        assertEquals(1, controller.getNewCompareRootNode().getLeaves().size());
        assertEquals("", controller.getNewCompareRootNode().getName());
        assertEquals(d2Leaf2, controller.getNewCompareRootNode().getLeaves().get(0));         
        assertEquals(testFile22.getPath(), controller.getNewCompareRoot());
        assertNull( controller.getCompareRootNode() );
        assertEquals(testDir1.getPath() + " <> " + testFile22.getPath(), controller.getCompareRoot());
        cmpListener.reset();

        // TEST: Compare with old comapre root not forced new compare root forced to leaf d2/2
        //  Compare listener notified
        //   old d1/ { d12/ { 1, sd/ { 1, 2 } } }
        //   new d2(fake)/ { 2 }
        // Compare will shift old compare root to d1/d12/sd since yields most name matches (one), the leaf 2
        controller.compare();
        
        assertTrue(cmpListener.notified);
        assertSame(sdNode, controller.getOldCompareRootNode());
        assertEquals(testDirSd.getPath(), controller.getOldCompareRoot());
        assertNotSame(d2Node, controller.getNewCompareRootNode());
        assertEquals(0, controller.getNewCompareRootNode().getDirs().size());
        assertEquals(1, controller.getNewCompareRootNode().getLeaves().size());
        assertEquals("", controller.getNewCompareRootNode().getName());
        assertEquals(d2Leaf2, controller.getNewCompareRootNode().getLeaves().get(0));         
        assertEquals(testFile22.getPath(), controller.getNewCompareRoot());
        assertEquals(testDirSd.getPath() + " <> " + testFile22.getPath(), controller.getCompareRoot());
        {
            DirNode compareNode = controller.getCompareRootNode();
            assertEquals(testDirSd.getName(), compareNode.getName());
            assertEquals(0, compareNode.getDirs().size());
            assertEquals(2, compareNode.getLeaves().size());
            LeafComparisonResult result1 = (LeafComparisonResult)compareNode.childLeafByName("1");
            assertTrue(result1.isMissing2());
            LeafComparisonResult result2 = (LeafComparisonResult)compareNode.childLeafByName("2");
            assertTrue(result2.haveBoth());
        }
        cmpListener.reset();
        
        // FORCE NEW ROOT d2
        // FORCE OLD ROOT d1
        
        // TEST: Old root forced to d1/, new root forced to d2/
        //  Pre-compare verfiy as expected
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2"));
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1"));
        assertFalse(oldListener.notified);
        assertFalse(newListener.notified);
        assertTrue(cmpListener.notified);
        assertSame(d1Node, controller.getOldRootNode());
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertSame(d2Node, controller.getNewRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());
        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        cmpListener.reset();

        // TEST: (cont'd) Old root forced to d1/, new root forced to d2/
        //  Compare does not realign.
        //  Compare tree top level is d1/ { +1, +2, -d12/, + d3 }
        controller.compare();
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertEquals(testDir1.getPath(), controller.getOldCompareRoot());
        assertSame(d2Node, controller.getNewCompareRootNode());
        assertEquals(testDir2.getPath(), controller.getNewCompareRoot());
        assertEquals(testDir1.getPath() + " <> " + testDir2.getPath(), controller.getCompareRoot());
        {
            DirNode compareNode = controller.getCompareRootNode();
            assertEquals(testDir1.getName(), compareNode.getName());
            assertEquals(2, compareNode.getDirs().size());
            assertEquals(2, compareNode.getLeaves().size());
            ComparisonDirNode d12CompareNode =  (ComparisonDirNode)compareNode.childDirNodeByName("d12");
            assertTrue(d12CompareNode.isMissing2());
            ComparisonDirNode d3CompareNode =  (ComparisonDirNode)compareNode.childDirNodeByName("d3");
            assertTrue(d3CompareNode.isMissing1());

            LeafComparisonResult result1 = (LeafComparisonResult)compareNode.childLeafByName("1");
            assertTrue(result1.isMissing1());
            LeafComparisonResult result2 = (LeafComparisonResult)compareNode.childLeafByName("2");
            assertTrue(result2.isMissing1());
        }
    }
    
    /**
     * Test compare tree alignment effects with forced compare roots
     * @throws Exception
     */
    @Test
    public void testAlignmentReset() throws Exception {
        /*
         * Test Trees
         *  d1/ { d12 { 2, sd/ { 1, 2 } } }
         *  and
         *  d2/ { 1, 2, d3/ { 3 } }
         *  
         *  normal realignment without forcing compares d1/d12/sd to d2 
         */
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("d1");
        File testDir12 = helper.createTestDir("d12", testDir1);
        helper.createTestFile("1", "1", testDir12);
        File testDirSd = helper.createTestDir("sd", testDir12);
        helper.createTestFile("1", "1", testDirSd);
        helper.createTestFile("2", "2", testDirSd);
        
        File testDir2 = helper.createTestDir("d2");
        helper.createTestFile("1", "1", testDir2);
        helper.createTestFile("2", "2", testDir2);
        File testDir3 = helper.createTestDir("d3", testDir2);
        helper.createTestFile("3", "3", testDir3);
        
        CompareController controller = new CompareController();
        
        controller.setOldRoot(testDir1.getPath());
        controller.setNewRoot(testDir2.getPath());
        
        DirNode d1Node = controller.getOldRootNode();
        DirNode d12Node = d1Node.childDirNodeByName("d12");
        DirNode sdNode = d12Node.childDirNodeByName("sd");
        DirNode d2Node = controller.getNewRootNode();
        DirNode d3Node = d2Node.childDirNodeByName("d3");

        // TEST: compare d1 tree forced to root at d1 results in no realignment of either.
        //  no subdirectory of d2 provides any improvement of name matches
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1"));
        controller.compare();
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());

        // return to nothing forced state
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);

        // TEST: normal realignment compare d1/d12/sd to d2 since this provides 2 matching file names
        controller.compare();
        assertSame(sdNode, controller.getOldCompareRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());
        
        // TEST: setting old root resets old and new compare roots if not forced
        //  Compare above set old compare root to sd
        controller.setOldRoot(testDir1.getPath());
        d1Node = controller.getOldRootNode(); // needs fresh since old tree rebuild
        d12Node = d1Node.childDirNodeByName("d12"); // needs fresh since old tree rebuild
        sdNode = d12Node.childDirNodeByName("sd"); // needs fresh since old tree rebuild
        assertSame(d1Node, controller.getOldCompareRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());
        
        
        // TEST: setting new root does not reset old forced compare root
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1", "d12", "sd"));
        controller.setNewRoot(testDir2.getPath());
        d2Node = controller.getNewRootNode(); // needs fresh since new tree rebuild
        d3Node = d2Node.childDirNodeByName("d3"); // needs fresh since new tree rebuild
        assertSame(sdNode, controller.getOldCompareRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());

        // TEST: forcing new compare root does not reset old compare root when old compare root is forced
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2", "d3"));
        assertSame(sdNode, controller.getOldCompareRootNode());
        assertSame(d3Node, controller.getNewCompareRootNode());

        // TEST: unforcing new compare root does not reset old compare root when old compare root is forced        
        controller.unforceCompareRoot(NodeRole.NEW_ROOT);
        assertSame(sdNode, controller.getOldCompareRootNode());
        assertSame(d2Node, controller.getNewCompareRootNode());
        
        
        /* -----------------------------------------
         * tests below use the trees in swapped around
         * old is d2/ {1, 2, d3/ { 3 } }
         * new is d11/ { d12/ { 1, sd/ { 1, 2 } } }
         */
        controller.setOldRoot(testDir2.getPath());
        controller.setNewRoot(testDir1.getPath());
        
        d2Node = controller.getOldRootNode();
        d1Node = controller.getNewRootNode();
        d12Node = d1Node.childDirNodeByName("d12");
        sdNode = d12Node.childDirNodeByName("sd");
        d3Node = d2Node.childDirNodeByName("d3");

        // TEST: (sanity) normal realignment works as expected when new is the one that needs to shift around
        controller.compare();
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(sdNode, controller.getNewCompareRootNode());

        // TEST: (sanity) new does not realign after compare if forced 
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d1"));
        controller.compare();
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(d1Node, controller.getNewCompareRootNode());
        controller.unforceCompareRoot(NodeRole.NEW_ROOT);
        
        // TEST: unforce old resets to root 
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d2", "d3"));
        assertSame(d3Node, controller.getOldCompareRootNode());
        assertSame(d1Node, controller.getNewCompareRootNode());
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(d1Node, controller.getNewCompareRootNode());
        
        // TEST: unforce old does not affect forced new
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d1", "d12", "sd"));
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(sdNode, controller.getNewCompareRootNode());
        controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d2", "d3"));
        assertSame(d3Node, controller.getOldCompareRootNode());
        assertSame(sdNode, controller.getNewCompareRootNode());
        controller.unforceCompareRoot(NodeRole.OLD_ROOT);
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(sdNode, controller.getNewCompareRootNode());
        controller.unforceCompareRoot(NodeRole.NEW_ROOT);
        
        // rebuild old tree
        controller.setOldRoot(testDir2.getPath());
        d2Node = controller.getOldRootNode(); // needs fresh since old tree rebuild
        d1Node = controller.getNewRootNode(); // needs fresh since old tree rebuild
        d12Node = d1Node.childDirNodeByName("d12");
        sdNode = d12Node.childDirNodeByName("sd");
        d3Node = d2Node.childDirNodeByName("d3");
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(d1Node, controller.getNewCompareRootNode());
        
        // TEST: rebuild old tree does not affect forced new compare root
        controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d1", "d12", "sd"));
        controller.setOldRoot(testDir2.getPath());
        d2Node = controller.getOldRootNode();
        d1Node = controller.getNewRootNode();
        d12Node = d1Node.childDirNodeByName("d12");
        sdNode = d12Node.childDirNodeByName("sd");
        d3Node = d2Node.childDirNodeByName("d3");
        assertSame(d2Node, controller.getOldCompareRootNode());
        assertSame(sdNode, controller.getNewCompareRootNode());        
        
        
    }

    /**
     * Test abnormal invocations of controller force / unforce compare root
     * 
     * @throws IOException
     */
    @Test
    public void testForceRootAbnormal() throws IOException {
        /* Trees
         * d1/ { d21/ {sd}}
         * and
         * d2/ { d3}
         */
        FileSystemTestHelper helper = new FileSystemTestHelper();
        File testDir1 = helper.createTestDir("d1");
        File testDir12 = helper.createTestDir("d12", testDir1);
        helper.createTestDir("sd", testDir12);
        
        File testDir2 = helper.createTestDir("d2");
        helper.createTestDir("d3", testDir2);

        {
            // TEST: force old compare root when old root not set 
            CompareController controller = new CompareController();
            try { 
                controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("a"));
                fail("force compare root when nase root not set should throw IllegalStateException");
            } catch(IllegalStateException ise) {
                //this should happen
            }
            // TEST: force new compare root when new root not set 
            try { 
                controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("a"));
                fail("force compare root when base root not set should throw IllegalStateException");
            } catch(IllegalStateException ise) {
                //this should happen
            }
            // TEST: force new compare root when new root not set, though old root is set 
            controller.setOldRoot(testDir1.getPath());
            try { 
                controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("a"));
                fail("force compare root when base root not set should throw IllegalStateException");
            } catch(IllegalStateException ise) {
                //this should happen
            }
        }
        {
            // TEST: force old compare root when old root not set, though new root is set 
            CompareController controller = new CompareController();
            controller.setNewRoot(testDir1.getPath());
            try { 
                controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("a"));
                fail("force compare root when base root not set should throw IllegalStateException");
            } catch(IllegalStateException ise) {
                //this should happen
            }
        }
        
        {
            CompareController controller = new CompareController();
            controller.setOldRoot(testDir1.getPath());
            controller.setNewRoot(testDir2.getPath());
            
            // TEST: unforcing when not forced should be benign (i.e. no exception)
            controller.unforceCompareRoot(NodeRole.OLD_ROOT);
            controller.unforceCompareRoot(NodeRole.NEW_ROOT);
            
            // TEST: attempt unforce for CMP_ROOT role  
            try { 
                controller.unforceCompareRoot(NodeRole.CMP_ROOT);
                fail("unforce compare root for role CMP_ROOT should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            
            // in tests below we also check that aborted forcings do not affect existing good forcings
            DirNode d12Node = controller.getOldRootNode().childDirNodeByName("d12");
            DirNode d3Node = controller.getNewRootNode().childDirNodeByName("d3");
            controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1", "d12"));
            controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2", "d3"));
            assertSame(d12Node, controller.getOldCompareRootNode());
            assertSame(d3Node, controller.getNewCompareRootNode());
            
            // TEST: attempt unforce for CMP_ROOT role when OLD and NEW roots are set  
            try { 
                controller.forceCompareRoot(NodeRole.CMP_ROOT, Arrays.asList("d1"));
                fail("force compare root for role CMP_ROOT should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            
            // TEST: attempt force old compare root passing null list
            try { 
                controller.forceCompareRoot(NodeRole.OLD_ROOT, null);
                fail("force compare root with null name list should throw NullPointerException");
            } catch(NullPointerException ise) {
                //this should happen
            }
            assertSame(d12Node, controller.getOldCompareRootNode());
            
            // TEST: attempt force new compare root passing null list
            try { 
                controller.forceCompareRoot(NodeRole.NEW_ROOT, null);
                fail("force compare root with null name list should throw NullPointerException");
            } catch(NullPointerException ise) {
                //this should happen
            }
            assertSame(d3Node, controller.getNewCompareRootNode());
            
            // TEST: attempt force old compare root passing empty list
            try { 
                controller.forceCompareRoot(NodeRole.OLD_ROOT, new ArrayList<String>(0));
                fail("force compare root with empty name list should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            assertSame(d12Node, controller.getOldCompareRootNode());

            // TEST: attempt force new compare root passing empty list
            try { 
                controller.forceCompareRoot(NodeRole.NEW_ROOT, new ArrayList<String>(0));
                fail("force compare root with empty name list should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            assertSame(d3Node, controller.getNewCompareRootNode());
            
            // TEST: attempt force old compare root passing invalid list (one item, doesn't match name of root)
            try { 
                controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d2"));
                fail("force root with nonmatching 1st name should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            assertSame(d12Node, controller.getOldCompareRootNode());
            
            // TEST: attempt force new compare root passing invalid list (two items, 1st doesn't match name of root)
            try { 
                controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d1", "d3"));
                fail("force root with nonmatching 1st name should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            assertSame(d3Node, controller.getNewCompareRootNode());
            
            // TEST: attempt force new compare root passing invalid list (two items, 2nd not in tree)
            try { 
                controller.forceCompareRoot(NodeRole.NEW_ROOT, Arrays.asList("d2", "dx"));
                fail("force root with nonmatching 2nd name should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            assertSame(d3Node, controller.getNewCompareRootNode());
            
            // TEST: attempt force old compare root passing invalid list (several items, last not in tree)
            try { 
                controller.forceCompareRoot(NodeRole.OLD_ROOT, Arrays.asList("d1", "d12", "dx"));
                fail("force root with nonmatching 1st name should throw IllegalArgumentException");
            } catch(IllegalArgumentException ise) {
                //this should happen
            }
            assertSame(d12Node, controller.getOldCompareRootNode());
        }
        
    }

}
