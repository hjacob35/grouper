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
import  junit.framework.*;

public class TestStemsDelete extends TestCase {

  public TestStemsDelete(String name) {
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
  

  // Confirm non-existence of stem
  public void testNS0DoesNotExist() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    Assert.assertNotNull("subj !null", subj);
    GrouperSession s = GrouperSession.start(subj);
    Assert.assertNotNull("session !null", s);

    // Confirm that NS0 doesn't exist
    GrouperStem ns0 = GrouperStem.load(
                         s, Constants.ns0s, Constants.ns0e
                       );
    Assert.assertNull("ns0 null", ns0);

    // We're done
    s.stop();
  }

  // TODO I thought stems couldn't be deleted?

  // NS at root-level
  public void testDeleteNS0() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );

    // Delete ns0
    try {
      GrouperStem.delete(s, ns0);
      Assert.assertTrue("delete ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("delete ns0");
    }

    s.stop();
  }

  // NS at root-level
  public void testFetchNS0() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Delete ns0
    try {
      GrouperStem.delete(s, ns0);
      Assert.assertTrue("delete ns0", true);
    } catch (RuntimeException e) {
      Assert.fail("delete ns0");
    }

    // Fetch ns0
    GrouperStem ns = GrouperStem.load(
                        s, Constants.ns0s, Constants.ns0e
                      );
    Assert.assertNull("ns0 !null", ns);

    s.stop();
  }

  // NS with child stem
  public void testDeleteNS0WithChildStem() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create ns1
    GrouperStem ns1 = GrouperStem.create(
                         s, Constants.ns1s, Constants.ns1e
                       );

    // Delete ns0
    try {
      GrouperStem.delete(s, ns0);
      Assert.fail("delete ns0 with child stem");
    } catch (RuntimeException e) {
      Assert.assertTrue("delete ns with child stem0", true);
    }

    s.stop();
  }

  // NS with child group
  public void testDeleteNS0WithChildGroup() {
    Subject subj = SubjectFactory.getSubject(Constants.rootI, Constants.rootT);
    GrouperSession s = GrouperSession.start(subj);

    // Create ns0
    GrouperStem ns0 = GrouperStem.create(
                         s, Constants.ns0s, Constants.ns0e
                       );
    // Create g0
    GrouperGroup g0 = GrouperGroup.create(
                         s, Constants.g0s, Constants.g0e
                       );

    // Delete ns0
    try {
      GrouperStem.delete(s, ns0);
      Assert.fail("delete ns0 with child group");
    } catch (RuntimeException e) {
      Assert.assertTrue("delete ns with child group", true);
    }

    s.stop();
  }

}

