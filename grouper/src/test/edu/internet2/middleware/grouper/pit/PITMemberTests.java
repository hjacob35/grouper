package edu.internet2.middleware.grouper.pit;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
public class PITMemberTests extends GrouperTest {

  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /**
   * @param name
   */
  public PITMemberTests(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  private Timestamp getTimestampWithSleep() {
    GrouperUtil.sleep(100);
    Date date = new Date();
    GrouperUtil.sleep(100);
    return new Timestamp(date.getTime());
  }
  
  /**
   * 
   */
  public void testGetGroupsAtPointInTime() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group4 = edu.addChildGroup("test4", "test4");

    Timestamp beforeAll = getTimestampWithSleep();
    group1.addMember(member0.getSubject());
    group2.addMember(member0.getSubject());
    Timestamp afterGroup2AddMember = getTimestampWithSleep();
    group3.addMember(member0.getSubject());
    group4.addMember(member1.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(member0.getSubject());
    Timestamp afterGroup1DeleteMember = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();

    group2.delete();
    Timestamp afterGroup2Delete = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findById(group1.getId());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findById(group3.getId());
    
    Set<PITGroup> results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, beforeAll, beforeAll, null);
    assertEquals(0, results.size());
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, afterGroup2AddMember, afterGroup2AddMember, null);
    assertEquals(2, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, afterGroup1DeleteMember, afterGroup1DeleteMember, null);
    assertEquals(2, results.size());
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, afterGroup2Delete, afterGroup2Delete, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup3));
  }
  
  /**
   * 
   */
  public void testGetGroupsWithFromDate() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group4 = edu.addChildGroup("test4", "test4");

    Timestamp beforeAll = getTimestampWithSleep();
    group1.addMember(member0.getSubject());
    group2.addMember(member0.getSubject());
    Timestamp afterGroup2AddMember = getTimestampWithSleep();
    group3.addMember(member0.getSubject());
    group4.addMember(member1.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(member0.getSubject());
    Timestamp afterGroup1DeleteMember = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();

    group2.delete();
    Timestamp afterGroup2Delete = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findById(group1.getId());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findById(group3.getId());
    
    Set<PITGroup> results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, beforeAll, null, null);
    assertEquals(3, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, afterGroup2AddMember, null, null);
    assertEquals(3, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, afterGroup1DeleteMember, null, null);
    assertEquals(2, results.size());
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, afterGroup2Delete, null, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup3));
  }
  
  /**
   * 
   */
  public void testGetGroupsWithToDate() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group4 = edu.addChildGroup("test4", "test4");

    Timestamp beforeAll = getTimestampWithSleep();
    group1.addMember(member0.getSubject());
    group2.addMember(member0.getSubject());
    Timestamp afterGroup2AddMember = getTimestampWithSleep();
    group3.addMember(member0.getSubject());
    group4.addMember(member1.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    group1.deleteMember(member0.getSubject());
    Timestamp afterGroup1DeleteMember = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();

    group2.delete();
    Timestamp afterGroup2Delete = getTimestampWithSleep();
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findById(group1.getId());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findById(group3.getId());
    
    Set<PITGroup> results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, beforeAll, null);
    assertEquals(0, results.size());
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, afterGroup2AddMember, null);
    assertEquals(2, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, afterGroup1DeleteMember, null);
    assertEquals(3, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
    
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, afterGroup2Delete, null);
    assertEquals(3, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
  }
  
  /**
   * 
   */
  public void testGetGroupsWithScope() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Stem child = edu.addChildStem("child", "child");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = child.addChildGroup("test2", "test2");

    group1.addMember(member0.getSubject());
    group2.addMember(member0.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    
    Set<PITGroup> results = pitMember0.getGroups(Group.getDefaultList().getUuid(), "edu:child:", null, null, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup2));
  }
  
  /**
   * 
   */
  public void testGetGroupsSort() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group2 = edu.addChildGroup("test2", "test2");

    group1.addMember(member0.getSubject());
    group3.addMember(member0.getSubject());
    group2.addMember(member0.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findById(group1.getId());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findById(group3.getId());
    
    Set<PITGroup> results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, null, null);
    assertEquals(3, results.size());

    // should have sorted by name...
    Iterator<PITGroup> iter = results.iterator();
    assertEquals(pitGroup1, iter.next());
    assertEquals(pitGroup2, iter.next());
    assertEquals(pitGroup3, iter.next());
    
    // actually specify sort by name this time...
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sort(new QuerySort("name", true));
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, null, queryOptions);
    assertEquals(3, results.size());

    // should have sorted by name...
    iter = results.iterator();
    assertEquals(pitGroup1, iter.next());
    assertEquals(pitGroup2, iter.next());
    assertEquals(pitGroup3, iter.next());
    
    // sort by name desc this time
    queryOptions = new QueryOptions();
    queryOptions.sort(new QuerySort("name", false));
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, null, queryOptions);
    assertEquals(3, results.size());

    // should have sorted by name desc...
    iter = results.iterator();
    assertEquals(pitGroup3, iter.next());
    assertEquals(pitGroup2, iter.next());
    assertEquals(pitGroup1, iter.next());
  }
  
  /**
   * 
   */
  public void testGetGroupsPaging() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group2 = edu.addChildGroup("test2", "test2");

    group1.addMember(member0.getSubject());
    group3.addMember(member0.getSubject());
    group2.addMember(member0.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findById(group1.getId());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findById(group3.getId());
    
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.paging(2, 1, false);
    Set<PITGroup> results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, null, queryOptions);
    assertEquals(2, results.size());

    // should have sorted by name...
    Iterator<PITGroup> iter = results.iterator();
    assertEquals(pitGroup1, iter.next());
    assertEquals(pitGroup2, iter.next());

    // check second page now...
    queryOptions = new QueryOptions();
    queryOptions.paging(2, 2, false);
    results = pitMember0.getGroups(Group.getDefaultList().getUuid(), null, null, null, queryOptions);
    assertEquals(1, results.size());

    iter = results.iterator();
    assertEquals(pitGroup3, iter.next());
  }
  
  /**
   * 
   */
  public void testGetGroupsReadersField() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");

    group1.addMember(member0.getSubject());
    group2.grantPriv(member0.getSubject(), AccessPrivilege.READ);
    ChangeLogTempToEntity.convertRecords();
    
    PITMember pitMember0 = GrouperDAOFactory.getFactory().getPITMember().findById(member0.getUuid());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    
    Set<PITGroup> results = pitMember0.getGroups(FieldFinder.find("readers", true).getUuid(), null, null, null, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup2));
  }
  
  /**
   * 
   */
  public void testGetGroupsPrivs() {
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    Group group1 = edu.addChildGroup("test1", "test1");
    Group group2 = edu.addChildGroup("test2", "test2");
    Group group3 = edu.addChildGroup("test3", "test3");
    Group group4 = edu.addChildGroup("test4", "test4");

    group1.addMember(member1.getSubject());
    group2.addMember(member1.getSubject());
    group3.addMember(member1.getSubject());
    group4.addMember(member2.getSubject());
    group1.grantPriv(member0.getSubject(), AccessPrivilege.READ);
    group3.grantPriv(member0.getSubject(), AccessPrivilege.READ);
    group4.grantPriv(member0.getSubject(), AccessPrivilege.READ);
    
    ChangeLogTempToEntity.convertRecords();
    
    group3.delete();
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp first = getTimestampWithSleep();
    
    PITMember pitMember1 = GrouperDAOFactory.getFactory().getPITMember().findById(member1.getUuid());
    PITMember pitMember2 = GrouperDAOFactory.getFactory().getPITMember().findById(member2.getUuid());
    PITGroup pitGroup1 = GrouperDAOFactory.getFactory().getPITGroup().findById(group1.getId());
    PITGroup pitGroup2 = GrouperDAOFactory.getFactory().getPITGroup().findById(group2.getId());
    PITGroup pitGroup3 = GrouperDAOFactory.getFactory().getPITGroup().findById(group3.getId());
    PITGroup pitGroup4 = GrouperDAOFactory.getFactory().getPITGroup().findById(group4.getId());
        
    // root should be able to get all groups
    Set<PITGroup> results = pitMember1.getGroups(Group.getDefaultList().getUuid(), null, null, null, null);
    assertEquals(3, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    assertTrue(results.contains(pitGroup3));
    assertFalse(results.contains(pitGroup4));
    results = pitMember2.getGroups(Group.getDefaultList().getUuid(), null, null, null, null);
    assertEquals(1, results.size());
    assertFalse(results.contains(pitGroup1));
    assertFalse(results.contains(pitGroup2));
    assertFalse(results.contains(pitGroup3));
    assertTrue(results.contains(pitGroup4));
    
    // subj0 should be able to read members of group1 and group4 only
    GrouperSession s = GrouperSession.start(member0.getSubject());
    results = pitMember1.getGroups(Group.getDefaultList().getUuid(), null, null, null, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup1));
    results = pitMember2.getGroups(Group.getDefaultList().getUuid(), null, null, null, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup4));
    s.stop();
    
    // end memberships and verify again
    s = GrouperSession.startRootSession();
    group1.deleteMember(member1.getSubject());
    group2.deleteMember(member1.getSubject());
    group4.deleteMember(member2.getSubject());
    ChangeLogTempToEntity.convertRecords();
    
    Timestamp second = getTimestampWithSleep();
    
    // root should be able to get all groups
    results = pitMember1.getGroups(Group.getDefaultList().getUuid(), null, first, second, null);
    assertEquals(2, results.size());
    assertTrue(results.contains(pitGroup1));
    assertTrue(results.contains(pitGroup2));
    assertFalse(results.contains(pitGroup4));
    results = pitMember2.getGroups(Group.getDefaultList().getUuid(), null, first, second, null);
    assertEquals(1, results.size());
    assertFalse(results.contains(pitGroup1));
    assertFalse(results.contains(pitGroup2));
    assertFalse(results.contains(pitGroup3));
    assertTrue(results.contains(pitGroup4));

    // subj0 should be able to read members of group1 and group4 only
    s = GrouperSession.start(member0.getSubject());
    results = pitMember1.getGroups(Group.getDefaultList().getUuid(), null, first, second, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup1));
    results = pitMember2.getGroups(Group.getDefaultList().getUuid(), null, first, second, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup4));
    s.stop();

    // delete group1 and try again
    s = GrouperSession.startRootSession();
    group1.delete();
    ChangeLogTempToEntity.convertRecords();
    
    // subj0 should be able to read members of group4 only
    s = GrouperSession.start(member0.getSubject());
    results = pitMember1.getGroups(Group.getDefaultList().getUuid(), null, first, second, null);
    assertEquals(0, results.size());
    results = pitMember2.getGroups(Group.getDefaultList().getUuid(), null, first, second, null);
    assertEquals(1, results.size());
    assertTrue(results.contains(pitGroup4));
    s.stop();
  }
}