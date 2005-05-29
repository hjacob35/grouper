/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

import  java.util.*;
import  junit.framework.*;

public class TestGroupsMoFAdd3Reverse extends TestCase {

  public TestGroupsMoFAdd3Reverse(String name) {
    super(name);
  }

  protected void setUp () {
    DB db = new DB();
    db.emptyTables();
    db.stop();
  }

  protected void tearDown () {
    // Nothing -- Yet
  }


  /*
   * TESTS
   */
  

  //
  // Add gB to gC
  // Add gA to gB
  // Add m0 to gA
  //
  public void testMoF() {
    Subject subj = null;
    try {
      subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    } catch (SubjectNotFoundException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create gA
    GrouperGroup gA  = GrouperGroup.create(
                         s, Constants.gAs, Constants.gAe
                       );
    // Create gB
    GrouperGroup gB  = GrouperGroup.create(
                         s, Constants.gBs, Constants.gBe
                       );
    // Create gC
    GrouperGroup gC  = GrouperGroup.create(
                         s, Constants.gCs, Constants.gCe
                       );
    // Load m0
    GrouperMember m0 = Common.loadMember(
                         s, Constants.mem0I, Constants.mem0T
                       );

    // Add gB to gC's "members"
    try {
      gC.listAddVal(gB.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gB to gC");
    }  
    // Add gA to gB's "members"
    try {
      gB.listAddVal(gA.toMember());
    } catch (RuntimeException e) {
      Assert.fail("add gA to gB");
    }
    // Add m0 to gA's "members"
    try {
      gA.listAddVal(m0);
    } catch (RuntimeException e) {
      Assert.fail("add m0 to gA");
    }

    /*
     * m0 -> gA
     */
    // Now inspect gA's resulting list values
    Assert.assertTrue(
      "gA members == 1", gA.listVals("members").size() == 1
    );
    Assert.assertTrue(
      "gA imm members == 1", gA.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gA eff members == 0", gA.listEffVals("members").size() == 0
    );
    Iterator iterAI = gA.listImmVals().iterator();
    while (iterAI.hasNext()) {
      GrouperList lv = (GrouperList) iterAI.next();
      Assert.assertTrue("gA empty chain", lv.chain().size() == 0);
      Assert.assertNull("gA null via", lv.via());
    }

    /*  
     * gA -> gB
     * m0 -> gA -> gB
     *    => gA -> gB
     */
    // Now inspect gB's resulting list values
    Assert.assertTrue(
      "gB members == 2", gB.listVals("members").size() == 2
    );
    Assert.assertTrue(
      "gB imm members == 1", gB.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gB eff members == 1", gB.listEffVals("members").size() == 1
    );
    Iterator iterBI = gB.listImmVals().iterator();
    while (iterBI.hasNext()) {
      GrouperList lv = (GrouperList) iterBI.next();
      Assert.assertTrue("gB empty chain", lv.chain().size() == 0);
      Assert.assertNull("gB null via", lv.via());
    }
    Iterator iterBE = gB.listEffVals().iterator();
    while (iterBE.hasNext()) {
      GrouperList lv = (GrouperList) iterBE.next();
      Assert.assertTrue("gB chain == 1", lv.chain().size() == 1);
      Assert.assertNotNull("gB !null via", lv.via());
      Assert.assertEquals("gB member() == m0", m0, lv.member());
      Assert.assertEquals("gB via() == gA", gA, lv.via());
      Iterator iterVia = lv.chain().iterator();
      while (iterVia.hasNext()) {
        MemberVia   mv  = (MemberVia) iterVia.next();
        GrouperList lvv = mv.toList(s);
        Assert.assertTrue("gB via g == gB", lvv.group().equals(gB));
        Assert.assertTrue(
          "gB via m == gA", lvv.member().toGroup().equals(gA)
        );
      }
    }

    /*  
     * gB -> gC
     * gA -> gB -> gC
     *    => gB -> gC
     * m0 -> gA -> gB -> gC
     *    => gA -> gB
     *    => gB -> gC
     */
    // Now inspect gC's resulting list values
    Assert.assertTrue(
      "gC members == 3", gC.listVals("members").size() == 3
    );
    Assert.assertTrue(
      "gC imm members == 1", gC.listImmVals("members").size() == 1
    );
    Assert.assertTrue(
      "gC eff members == 2", gC.listEffVals("members").size() == 2
    );
    Iterator iterCI = gC.listImmVals().iterator();
    while (iterCI.hasNext()) {
      GrouperList lv = (GrouperList) iterCI.next();
      Assert.assertTrue("gC empty chain", lv.chain().size() == 0);
      Assert.assertNull("gC null via", lv.via());
    }
    Iterator iterCE = gC.listEffVals().iterator();
    while (iterCE.hasNext()) {
      GrouperList lv = (GrouperList) iterCE.next();
      if        (lv.chain().size() == 1) {
        Assert.assertTrue("gC chain == 1", true);
        Assert.assertNotNull("gC (1) !null via", lv.via());
        Assert.assertEquals("gC (1) member() == gA", gA.toMember(), lv.member());
        Assert.assertEquals("gC (1) via() == gB", gB, lv.via());
        Iterator iterVia = lv.chain().iterator();
        while (iterVia.hasNext()) {
          MemberVia   mv  = (MemberVia) iterVia.next();
          GrouperList lvv = mv.toList(s);
          Assert.assertTrue("gC (1) via g == gC", lvv.group().equals(gC));
          Assert.assertTrue(
            "gC (1) via m == gB", lvv.member().toGroup().equals(gB));
        }
      } else if (lv.chain().size() == 2) {
        Assert.assertTrue("gC chain == 2", true);
        Assert.assertNotNull("gC (2) !null via", lv.via());
        Assert.assertEquals("gC (2) member() == m0", m0, lv.member());
        Assert.assertEquals("gC (2) via() == gA", gA, lv.via());
        int idx = 0;
        Iterator iterVia = lv.chain().iterator();
        while (iterVia.hasNext()) {
          MemberVia   mv  = (MemberVia) iterVia.next();
          GrouperList lvv = mv.toList(s);
          if (idx == 0) {
            // gA -> gB 
            Assert.assertTrue("gC (2)[0] via g == gB", lvv.group().equals(gB));
            Assert.assertTrue(
              "gC (2)[0] via m == gA", lvv.member().toGroup().equals(gA)
            );
            Assert.assertNull("gC (2)[0] !via", lvv.via());
          } else {
            // gB -> gC
            Assert.assertTrue("gC (2)[1] via g == gC", lvv.group().equals(gC));
            Assert.assertTrue(
              "gC (2)[1] via m == gB", lvv.member().toGroup().equals(gB)
            );
            Assert.assertNull("gC (2)[1] !via", lvv.via());
          } 
          idx++;
        }
      } else {
        Assert.fail("gC chain (1,2) != " + lv.chain().size());
      }
    }

    s.stop();
  }

}

