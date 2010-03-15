/**
 * 
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueDelegate;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotAllowed;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportAttributeAssign;
import edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple;


/**
 * definition of an attribute
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class AttributeAssign extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, 
    XmlImportableMultiple<AttributeAssign>, AttributeAssignable {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssign.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_ASSIGN = "grouper_attribute_assign";

  /** actions col in db */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_ACTION_ID = "attribute_assign_action_id";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_NOTES = "notes";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_DEF_NAME_ID = "attribute_def_name_id";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_DELEGATABLE = "attribute_assign_delegatable";

  /** column */
  public static final String COLUMN_ATTRIBUTE_ASSIGN_TYPE = "attribute_assign_type";

  /** column */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";

  /** column */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";

  /** column */
  public static final String COLUMN_OWNER_MEMBER_ID = "owner_member_id";

  /** column */
  public static final String COLUMN_OWNER_MEMBERSHIP_ID = "owner_membership_id";

  /** column */
  public static final String COLUMN_OWNER_ATTRIBUTE_ASSIGN_ID = "owner_attribute_assign_id";

  /** column */
  public static final String COLUMN_OWNER_ATTRIBUTE_DEF_ID = "owner_attribute_def_id";

  /** column */
  public static final String COLUMN_ENABLED = "enabled";

  /** column */
  public static final String COLUMN_ENABLED_TIME = "enabled_time";

  /** column */
  public static final String COLUMN_DISABLED_TIME = "disabled_time";

  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeAssignActionId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ACTION_ID = "attributeAssignActionId";

  /** constant for field name for: attributeAssignDelegatable */
  public static final String FIELD_ATTRIBUTE_ASSIGN_DELEGATABLE = "attributeAssignDelegatable";

  /** constant for field name for: attributeAssignType */
  public static final String FIELD_ATTRIBUTE_ASSIGN_TYPE = "attributeAssignType";

  /** constant for field name for: attributeNameId */
  public static final String FIELD_ATTRIBUTE_NAME_ID = "attributeNameId";

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: disabledTimeDb */
  public static final String FIELD_DISABLED_TIME_DB = "disabledTimeDb";

  /** constant for field name for: enabled */
  public static final String FIELD_ENABLED = "enabled";

  /** constant for field name for: enabledTimeDb */
  public static final String FIELD_ENABLED_TIME_DB = "enabledTimeDb";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: notes */
  public static final String FIELD_NOTES = "notes";

  /** constant for field name for: ownerAttributeAssignId */
  public static final String FIELD_OWNER_ATTRIBUTE_ASSIGN_ID = "ownerAttributeAssignId";

  /** constant for field name for: ownerAttributeDefId */
  public static final String FIELD_OWNER_ATTRIBUTE_DEF_ID = "ownerAttributeDefId";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerMemberId */
  public static final String FIELD_OWNER_MEMBER_ID = "ownerMemberId";

  /** constant for field name for: ownerMembershipId */
  public static final String FIELD_OWNER_MEMBERSHIP_ID = "ownerMembershipId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_ASSIGN_DELEGATABLE, FIELD_ATTRIBUTE_NAME_ID, FIELD_CONTEXT_ID, 
      FIELD_CREATED_ON_DB, 
      FIELD_DISABLED_TIME_DB, FIELD_ENABLED, FIELD_ENABLED_TIME_DB, FIELD_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_NOTES, FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, FIELD_OWNER_ATTRIBUTE_DEF_ID, 
      FIELD_OWNER_GROUP_ID, FIELD_OWNER_MEMBER_ID, FIELD_OWNER_MEMBERSHIP_ID, FIELD_OWNER_STEM_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_ASSIGN_ACTION_ID, FIELD_ATTRIBUTE_NAME_ID, FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, 
      FIELD_DISABLED_TIME_DB, FIELD_ENABLED, FIELD_ENABLED_TIME_DB, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_ID, FIELD_LAST_UPDATED_DB, FIELD_NOTES, FIELD_OWNER_ATTRIBUTE_ASSIGN_ID, 
      FIELD_OWNER_ATTRIBUTE_DEF_ID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_MEMBER_ID, FIELD_OWNER_MEMBERSHIP_ID, 
      FIELD_OWNER_STEM_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * 
   */
  public AttributeAssign() {
    //default
    this.attributeAssignDelegatable = AttributeAssignDelegatable.FALSE;
  }
  
  /**
   * create an attribute assign, including a uuid
   * @param ownerStem
   * @param theAction
   * @param attributeDefName
   * @param uuid
   */
  public AttributeAssign(Stem ownerStem, String theAction, AttributeDefName attributeDefName, String uuid) {

    this();
    this.setAttributeAssignType(AttributeAssignType.stem);
    this.setOwnerStemId(ownerStem.getUuid());
    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(attributeAssignAction.getId());
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerAttributeDef
   * @param theAction
   * @param attributeDefName
   * @param uuid is uuid or null if generated
   */
  public AttributeAssign(AttributeDef ownerAttributeDef, String theAction, AttributeDefName attributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.attr_def);
    this.setOwnerAttributeDefId(ownerAttributeDef.getId());
    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(attributeAssignAction.getId());
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerGroup
   * @param theAction
   * @param attributeDefName
   * @param uuid is the uuid or null if generated
   */
  public AttributeAssign(Group ownerGroup, String theAction, AttributeDefName attributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.group);
    this.setOwnerGroupId(ownerGroup.getUuid());
    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(attributeAssignAction.getId());
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid.  This is for an immediate or effective membership
   * @param ownerGroup
   * @param ownerMember 
   * @param theAction
   * @param attributeDefName
   * @param uuid
   */
  public AttributeAssign(Group ownerGroup, Member ownerMember, String theAction, 
      AttributeDefName attributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.any_mem);

    //this can be any membership
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
      .findAllByGroupOwnerAndMemberAndField(ownerGroup.getId(), ownerMember.getUuid(), 
          Group.getDefaultList(), true);

    if (memberships.size() == 0) {
      throw new RuntimeException("'Any' memberships which have attributes must be immediate or effective on the members list: " 
          + ownerGroup + ", " + GrouperUtil.subjectToString(ownerMember.getSubject()));
    }
    
    this.setOwnerGroupId(ownerGroup.getUuid());
    this.setOwnerMemberId(ownerMember.getUuid());
    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(attributeAssignAction.getId());
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerAttributeAssign
   * @param theAction
   * @param attributeDefName
   * @param uuid to use or null for generated
   */
  public AttributeAssign(AttributeAssign ownerAttributeAssign, String theAction, 
      AttributeDefName attributeDefName, String uuid) {
    
    this();
    
    AttributeAssignType ownerType = ownerAttributeAssign.getAttributeAssignType();
    if (AttributeAssignType.group == ownerType) {
      this.attributeAssignType = AttributeAssignType.group_asgn;
    } else if (AttributeAssignType.stem == ownerType) {
      this.attributeAssignType = AttributeAssignType.stem_asgn;
    } else if (AttributeAssignType.member == ownerType) {
      this.attributeAssignType = AttributeAssignType.mem_asgn;
    } else if (AttributeAssignType.attr_def == ownerType) {
      this.attributeAssignType = AttributeAssignType.attr_def_asgn;
    } else if (AttributeAssignType.any_mem == ownerType) {
      this.attributeAssignType = AttributeAssignType.any_mem_asgn;
    } else if (AttributeAssignType.imm_mem == ownerType) {
      this.attributeAssignType = AttributeAssignType.imm_mem_asgn;
    } else {
      throw new RuntimeException("Not expecting attribute on ownerAttributeType: " + ownerType);
    }

    
    //cant assign to an assignment of an assignment.
    if (!StringUtils.isBlank(ownerAttributeAssign.getOwnerAttributeAssignId())) {
      throw new RuntimeException("You cants assign an attribute to " +
      		"an assignment of an assignment (only to an assignment of a non-assignment): " 
          + theAction + ", " + attributeDefName.getName());
    }
    
    this.setOwnerAttributeAssignId(ownerAttributeAssign.getId());
    
    theAction = StringUtils.defaultIfEmpty(theAction, AttributeDef.ACTION_DEFAULT);
    
    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(attributeAssignAction.getId());
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerMembership
   * @param theAction
   * @param attributeDefName
   * @param uuid
   */
  public AttributeAssign(Membership ownerMembership, String theAction, AttributeDefName attributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.imm_mem);

    //this must be an immediate, list membership
    if (!ownerMembership.isImmediate()) {
      throw new RuntimeException("Memberships which have attributes must be immediate: " 
          + ownerMembership.getType() + ", " + ownerMembership.getUuid());
    }
    
    if (!Group.getDefaultList().equals(ownerMembership.getList())) {
      throw new RuntimeException("Memberships which have attributes must be list type: " 
          + ownerMembership.getList() + ", " + ownerMembership.getImmediateMembershipId());
      
    }
    
    this.setOwnerMembershipId(ownerMembership.getImmediateMembershipId());

    AttributeAssignAction attributeAssignAction = attributeDefName.getAttributeDef()
      .getAttributeDefActionDelegate().allowedAction(theAction, true);

    this.setAttributeAssignActionId(attributeAssignAction.getId());
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * create an attribute assign, including a uuid
   * @param ownerMember
   * @param theActionId
   * @param attributeDefName
   * @param uuid is the uuid or null for generated
   */
  public AttributeAssign(Member ownerMember, String theActionId, AttributeDefName attributeDefName, String uuid) {
    
    this();
    this.setAttributeAssignType(AttributeAssignType.member);

    this.setOwnerMemberId(ownerMember.getUuid());
    this.setAttributeAssignActionId(theActionId);
    this.setAttributeDefNameId(attributeDefName.getId());
    this.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid);

  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    AttributeDef attributeDef = this.getAttributeDef();
    
    //validate if allowed
    if (StringUtils.isNotEmpty(this.ownerGroupId) && StringUtils.isEmpty(this.ownerMemberId) && !attributeDef.isAssignToGroup()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to group: " + attributeDef + ", " + this.ownerGroupId + ", to allow this, make sure the attributeDef has setAssignToGroup(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerStemId) && !attributeDef.isAssignToStem()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to stem: " + attributeDef + ", " + this.ownerStemId + ", to allow this, make sure the attributeDef has setAssignToStem(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerMemberId) && StringUtils.isEmpty(this.ownerGroupId) && !attributeDef.isAssignToMember()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to member: " + attributeDef + ", " + this.ownerMemberId + ", to allow this, make sure the attributeDef has setAssignToMember(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerMembershipId) && !attributeDef.isAssignToImmMembership()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to immediate membership: " + attributeDef + ", " + this.ownerMembershipId + ", to allow this, make sure the attributeDef has setAssignToImmMembership(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerAttributeDefId) && !attributeDef.isAssignToAttributeDef()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to attribute def: " + attributeDef + ", " + this.ownerAttributeDefId + ", to allow this, make sure the attributeDef has setAssignToAttributeDef(true)");
    }
    if (StringUtils.isNotEmpty(this.ownerMemberId) && StringUtils.isNotEmpty(this.ownerGroupId) && !attributeDef.isAssignToEffMembership()) {
      throw new AttributeAssignNotAllowed("Not allowed to assign to effective membership: " + attributeDef + ", " + this.ownerGroupId + ", " + this.ownerMemberId + ", to allow this, make sure the attributeDef has setAssignToEffMembership(true)");
    }
    //TODO check for assignments (6 cases?)
    GrouperDAOFactory.getFactory().getAttributeAssign().saveOrUpdate(this);
  }
  
  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttributeAssign().delete(this);
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public AttributeAssign clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
  
  /** attribute name in this assignment */
  private String attributeDefNameId;

  /** if the subjects assigned to the attribute can delegate to someone else, or delegate as delegatable */
  private AttributeAssignDelegatable attributeAssignDelegatable;
  
  /** type of assignment */
  private AttributeAssignType attributeAssignType;
  
  /**
   * get the enum for delegatable, do not return null
   * @return the attributeAssignDelegatable
   */
  public AttributeAssignDelegatable getAttributeAssignDelegatable() {
    return GrouperUtil.defaultIfNull(this.attributeAssignDelegatable, 
        AttributeAssignDelegatable.FALSE); 
  }

  /**
   * internal method for hibernate to persist this enum
   * @return the string value (enum name)
   */
  public String getAttributeAssignDelegatableDb() {
    return this.getAttributeAssignDelegatable().name();
  }

  /**
   * internal method for hibernate to set if delegatable
   * @param theAttributeAssignDelegatableDb
   */
  public void setAttributeAssignDelegatableDb(String theAttributeAssignDelegatableDb) {
    this.attributeAssignDelegatable = AttributeAssignDelegatable.valueOfIgnoreCase(
        theAttributeAssignDelegatableDb, false);
  }
  
  /**
   * @param attributeAssignDelegatable1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignDelegatable(
      AttributeAssignDelegatable attributeAssignDelegatable1) {
    this.attributeAssignDelegatable = attributeAssignDelegatable1;
  }

  /**
   * get the enum for delegatable, should not return null
   * @return the attributeAssignDelegatable
   */
  public AttributeAssignType getAttributeAssignType() {
    return this.attributeAssignType;
  }

  /**
   * internal method for hibernate to persist this enum
   * @return the string value (enum name)
   */
  public String getAttributeAssignTypeDb() {
    return this.getAttributeAssignType().name();
  }

  /**
   * internal method for hibernate to set if delegatable
   * @param theAttributeAssignTypeDb
   */
  public void setAttributeAssignTypeDb(String theAttributeAssignTypeDb) {
    this.attributeAssignType = AttributeAssignType.valueOfIgnoreCase(
        theAttributeAssignTypeDb, false);
  }
  
  /**
   * @param attributeAssignType1 the attributeAssignDelegatable to set
   */
  public void setAttributeAssignType(
      AttributeAssignType attributeAssignType1) {
    this.attributeAssignType = attributeAssignType1;
  }

  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String ownerAttributeAssignId;
  
  /** if this is an attribute def attribute, this is the foreign key */
  private String ownerAttributeDefId;
  
  /** if this is a group attribute, this is the foreign key */
  private String ownerGroupId;
  
  /** if this is a member attribute, this is the foreign key */
  private String ownerMemberId;
  
  /** if this is a membership attribute, this is the foreign key */
  private String ownerMembershipId;
  
  /** if this is a stem attribute, this is the foreign key */
  private String ownerStemId;
  
  /** id of this attribute def */
  private String id;

  /** context id of the transaction */
  private String contextId;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * notes about this assignment, free-form text
   */
  private String notes;

  /**
   * id of action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT
   */
  private String attributeAssignActionId;
  
  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   */
  private boolean enabled = true;
  
  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   */
  private Long enabledTimeDb;
  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   */
  private Long disabledTimeDb;
  
  /**
   * action for this assignment (e.g. assign).  Generally this will be AttributeDef.ACTION_DEFAULT.
   * action must exist in AttributeDef.actions
   * @return the action
   */
  public String getAttributeAssignActionId() {
    return this.attributeAssignActionId;
  }

  /** cache the attribute def name */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeDefName attributeDefName;
  
  /**
   * 
   * @return attributeDefName
   */
  public AttributeDefName getAttributeDefName() {
    if (this.attributeDefName == null ) {
      this.attributeDefName = AttributeDefNameFinder.findById(this.attributeDefNameId, true);
    }
    return this.attributeDefName;
  }
  
  /** cache the attribute assign action */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignAction attributeAssignAction;
  
  /**
   * 
   * @return attributeAssignAction
   */
  public AttributeAssignAction getAttributeAssignAction() {
    if (this.attributeAssignAction == null ) {
      this.attributeAssignAction = GrouperDAOFactory.getFactory()
        .getAttributeAssignAction().findById(this.attributeAssignActionId, true);
    }
    return this.attributeAssignAction;
  }
  
  /** cache the attribute def of this attribute def name */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeDef attributeDef;
  
  /**
   * 
   * @return attributeDef
   */
  public AttributeDef getAttributeDef() {
    if (this.attributeDef == null ) {
      this.attributeDef = AttributeDefFinder.findByAttributeDefNameId(this.attributeDefNameId, true);
    }
    return this.attributeDef;
  }
  
  /** */
  @GrouperIgnoreClone @GrouperIgnoreDbVersion @GrouperIgnoreFieldConstant
  private AttributeAssignAttrAssignDelegate attributeAssignAttrAssignDelegate;
  
  /**
   * 
   * @return the delegate
   */
  public AttributeAssignAttrAssignDelegate getAttributeDelegate() {
    if (this.attributeAssignAttrAssignDelegate == null) {
      this.attributeAssignAttrAssignDelegate = new AttributeAssignAttrAssignDelegate(this);
    }
    return this.attributeAssignAttrAssignDelegate;
  }

  
  /**
   * default is "assign" actions must contain only alphanumeric or underscore, case sensitive
   * e.g. id for read,write,admin
   * @param theActionId
   */
  public void setAttributeAssignActionId(String theActionId) {
    this.attributeAssignActionId = theActionId;
  }

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * id of this attribute def
   * @return id
   */
  public String getId() {
    return id;
  }

  /**
   * id of this attribute def
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }

  
  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }
  
  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * notes about this assignment, free-form text
   * @return the notes
   */
  public String getNotes() {
    return this.notes;
  }

  /**
   * notes about this assignment, free-form text
   * @param notes1
   */
  public void setNotes(String notes1) {
    this.notes = notes1;
  }

  
  /**
   * attribute name in this assignment
   * @return the attributeNameId
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  
  /**
   * attribute name in this assignment
   * @param attributeDefNameId1 the attributeNameId to set
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
    //reset cached object
    this.attributeDefName = null;
    this.attributeDef = null;
  }

  
  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @return the ownerAttributeAssignId
   */
  public String getOwnerAttributeAssignId() {
    return this.ownerAttributeAssignId;
  }

  
  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @param ownerAttributeAssignId1 the ownerAttributeAssignId to set
   */
  public void setOwnerAttributeAssignId(String ownerAttributeAssignId1) {
    this.ownerAttributeAssignId = ownerAttributeAssignId1;
  }

  
  /**
   * if this is an attribute def attribute, this is the foreign key
   * @return the ownerAttributeDefId
   */
  public String getOwnerAttributeDefId() {
    return this.ownerAttributeDefId;
  }

  
  /**
   * if this is an attribute def attribute, this is the foreign key
   * @param ownerAttributeDefId1 the ownerAttributeDefId to set
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId1) {
    this.ownerAttributeDefId = ownerAttributeDefId1;
  }

  
  /**
   * if this is a group attribute, this is the foreign key
   * @return the ownerAttributeGroupId
   */
  public String getOwnerGroupId() {
    return this.ownerGroupId;
  }

  
  /**
   * if this is a group attribute, this is the foreign key
   * @param ownerAttributeGroupId1 the ownerAttributeGroupId to set
   */
  public void setOwnerGroupId(String ownerAttributeGroupId1) {
    this.ownerGroupId = ownerAttributeGroupId1;
  }

  
  /**
   * if this is a member attribute, this is the foreign key
   * @return the ownerAttributeMemberId
   */
  public String getOwnerMemberId() {
    return this.ownerMemberId;
  }

  
  /**
   * if this is a member attribute, this is the foreign key
   * @param ownerAttributeMemberId1 the ownerAttributeMemberId to set
   */
  public void setOwnerMemberId(String ownerAttributeMemberId1) {
    this.ownerMemberId = ownerAttributeMemberId1;
  }

  
  /**
   * if this is a membership attribute, this is the foreign key
   * @return the ownerAttributeMembershipId
   */
  public String getOwnerMembershipId() {
    return this.ownerMembershipId;
  }

  
  /**
   * if this is a membership attribute, this is the foreign key
   * @param ownerAttributeMembershipId1 the ownerAttributeMembershipId to set
   */
  public void setOwnerMembershipId(String ownerAttributeMembershipId1) {
    this.ownerMembershipId = ownerAttributeMembershipId1;
  }

  
  /**
   * if this is a stem attribute, this is the foreign key
   * @return the ownerAttributeStemId
   */
  public String getOwnerStemId() {
    return this.ownerStemId;
  }

  
  /**
   * if this is a stem attribute, this is the foreign key
   * @param ownerAttributeStemId1 the ownerAttributeStemId to set
   */
  public void setOwnerStemId(String ownerAttributeStemId1) {
    this.ownerStemId = ownerAttributeStemId1;
  }

  
  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public boolean isEnabled() {
    //currently this is based on timestamp
    long now = System.currentTimeMillis();
    if (this.enabledTimeDb != null && this.enabledTimeDb > now) {
      return false;
    }
    if (this.disabledTimeDb != null && this.disabledTimeDb < now) {
      return false;
    }
    return true;
  }
  
  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1 the enabled to set
   */
  public void setEnabled(boolean enabled1) {
    this.enabled = enabled1;
  }

  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @return the enabled
   */
  public String getEnabledDb() {
    return this.enabled ? "T" : "F";
  }

  
  /**
   * true or false for if this assignment is enabled (e.g. might have expired) 
   * @param enabled1 the enabled to set
   */
  public void setEnabledDb(String enabled1) {
    this.enabled = GrouperUtil.booleanValue(enabled1);
  }

  
  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @return the enabledTimeDb
   */
  public Long getEnabledTimeDb() {
    return this.enabledTimeDb;
  }

  
  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTimeDb(Long enabledTimeDb1) {
    this.enabledTimeDb = enabledTimeDb1;
  }

  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Long getDisabledTimeDb() {
    return this.disabledTimeDb;
  }

  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTimeDb(Long disabledTimeDb1) {
    this.disabledTimeDb = disabledTimeDb1;
  }

  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @return the enabledTimeDb
   */
  public Timestamp getEnabledTime() {
    return this.enabledTimeDb == null ? null : new Timestamp(this.enabledTimeDb);
  }

  
  /**
   * if there is a date here, and it is in the future, this assignment is disabled
   * until that time
   * @param enabledTimeDb1 the enabledTimeDb to set
   */
  public void setEnabledTime(Timestamp enabledTimeDb1) {
    this.enabledTimeDb = enabledTimeDb1 == null ? null : enabledTimeDb1.getTime();
  }

  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @return the disabledTimeDb
   */
  public Timestamp getDisabledTime() {
    return this.disabledTimeDb == null ? null : new Timestamp(this.disabledTimeDb);
  }

  
  /**
   * if there is a date here, and it is in the past, this assignment is disabled
   * @param disabledTimeDb1 the disabledTimeDb to set
   */
  public void setDisabledTime(Timestamp disabledTimeDb1) {
    this.disabledTimeDb = disabledTimeDb1 == null ? null : disabledTimeDb1.getTime();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    long now = System.currentTimeMillis();
    this.setCreatedOnDb(now);
    this.setLastUpdatedDb(now);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    this.setLastUpdatedDb(System.currentTimeMillis());
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    ToStringBuilder toStringBuilder = new ToStringBuilder(this)
      .append( "id", this.id)
      .append( "action", this.getAttributeAssignAction().getName() )
      .append( "attributeDefName", this.getAttributeDefName().getName() );
    
    if (!StringUtils.isBlank(this.ownerStemId)) {
      toStringBuilder.append("stem", 
          StemFinder.findByUuid(GrouperSession.staticGrouperSession()
              .internal_getRootSession(), this.ownerStemId, true));
    }
    if (!StringUtils.isBlank(this.ownerGroupId)) {
      toStringBuilder.append("group", 
          GroupFinder.findByUuid(GrouperSession.staticGrouperSession()
              .internal_getRootSession(), this.ownerGroupId, true));
    }
    if (!StringUtils.isBlank(this.ownerMemberId)) {
      toStringBuilder.append("subjectId", 
          MemberFinder.findByUuid(GrouperSession.staticGrouperSession()
              .internal_getRootSession(), this.ownerMemberId, true));
    }
    if (!StringUtils.isBlank(this.ownerMembershipId)) {
      toStringBuilder.append("membershipId", 
              this.ownerMembershipId);
    }
    if (!StringUtils.isBlank(this.ownerAttributeDefId)) {
      toStringBuilder.append("attributeDef", 
          GrouperDAOFactory.getFactory().getAttributeDef().findById(
              this.ownerAttributeDefId, true));
    }
    if (!StringUtils.isBlank(this.ownerAttributeAssignId)) {
      toStringBuilder.append("ownerAttributeAssignId", 
          this.ownerAttributeAssignId);
    }
    
    return toStringBuilder.toString();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(AttributeAssign existingRecord) {
    
    existingRecord.setAttributeAssignActionId(existingRecord.getAttributeAssignActionId());
    existingRecord.setAttributeAssignDelegatable(existingRecord.getAttributeAssignDelegatable());
    existingRecord.setAttributeAssignType(existingRecord.getAttributeAssignType());
    existingRecord.setAttributeDefNameId(existingRecord.getAttributeDefNameId());
    existingRecord.setDisabledTimeDb(existingRecord.getDisabledTimeDb());
    existingRecord.setEnabled(existingRecord.isEnabled());
    existingRecord.setEnabledTimeDb(existingRecord.getEnabledTimeDb());
    existingRecord.setId(existingRecord.getId());
    existingRecord.setNotes(existingRecord.getNotes());
    existingRecord.setOwnerAttributeAssignId(existingRecord.getOwnerAttributeAssignId());
    existingRecord.setOwnerAttributeDefId(existingRecord.getOwnerAttributeDefId());
    existingRecord.setOwnerGroupId(existingRecord.getOwnerGroupId());
    existingRecord.setOwnerMemberId(existingRecord.getOwnerMemberId());
    existingRecord.setOwnerMembershipId(existingRecord.getOwnerMembershipId());
    existingRecord.setOwnerStemId(existingRecord.getOwnerStemId());

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(AttributeAssign other) {
    if (!StringUtils.equals(this.attributeAssignActionId, other.attributeAssignActionId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.attributeAssignDelegatable, other.attributeAssignDelegatable)) {
      return true;
    }
    if (!GrouperUtil.equals(this.attributeAssignType, other.attributeAssignType)) {
      return true;
    }
    if (!StringUtils.equals(this.attributeDefNameId, other.attributeDefNameId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.disabledTimeDb, other.disabledTimeDb)) {
      return true;
    }
    if (this.enabled != other.enabled) {
      return true;
    }
    if (!GrouperUtil.equals(this.enabledTimeDb, other.enabledTimeDb)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.notes, other.notes)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerAttributeAssignId, other.ownerAttributeAssignId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerAttributeDefId, other.ownerAttributeDefId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerGroupId, other.ownerGroupId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerMemberId, other.ownerMemberId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerMembershipId, other.ownerMembershipId)) {
      return true;
    }
    if (!StringUtils.equals(this.ownerStemId, other.ownerStemId)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(AttributeAssign other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.createdOnDb, other.createdOnDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.lastUpdatedDb, other.lastUpdatedDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveBusinessProperties(java.lang.Object)
   */
  public AttributeAssign xmlSaveBusinessProperties(AttributeAssign existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      
      AttributeAssignResult attributeAssignResult = null;
      AttributeAssignAction attributeAssignAction = this.getAttributeAssignAction();
      AttributeDefName attributeDefName = this.getAttributeDefName();
        
      if (!StringUtils.isBlank(this.ownerAttributeAssignId)) {
        
        AttributeAssign ownerAttributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
          .findById(this.ownerAttributeAssignId, true);
        attributeAssignResult = ownerAttributeAssign.getAttributeDelegate()
          .internal_assignAttributeHelper(attributeAssignAction.getName(), attributeDefName, true, this.id);
        
      } else if (!StringUtils.isBlank(this.ownerAttributeDefId)) {

        AttributeDef ownerAttributeDef = GrouperDAOFactory.getFactory().getAttributeDef()
          .findById(this.ownerAttributeDefId, true);
        attributeAssignResult = ownerAttributeDef.getAttributeDelegate()
          .internal_assignAttributeHelper(attributeAssignAction.getName(), attributeDefName, true, this.id);
      
      } else if (!StringUtils.isBlank(this.ownerGroupId)) {

        Group ownerGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerGroupId, true);
        attributeAssignResult = ownerGroup.getAttributeDelegate()
          .internal_assignAttributeHelper(attributeAssignAction.getName(), attributeDefName, true, this.id);
        
      } else if (!StringUtils.isBlank(this.ownerMemberId)) {
        
        Member ownerMember = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerMemberId, true);
        attributeAssignResult = ownerMember.getAttributeDelegate()
          .internal_assignAttributeHelper(attributeAssignAction.getName(), attributeDefName, true, this.id);

      } else if (!StringUtils.isBlank(this.ownerMembershipId)) {
        
        Membership ownerMembership = GrouperDAOFactory.getFactory().getMembership().findByUuid(this.ownerMembershipId, true, false);
        attributeAssignResult = ownerMembership.getAttributeDelegate()
          .internal_assignAttributeHelper(attributeAssignAction.getName(), attributeDefName, true, this.id);

      } else if (!StringUtils.isBlank(this.ownerStemId)) {
        
        Stem ownerStem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.id, true);
        attributeAssignResult = ownerStem.getAttributeDelegate()
          .internal_assignAttributeHelper(attributeAssignAction.getName(), attributeDefName, true, this.id);

      } else {
        throw new RuntimeException("Cant find owner: " + this);
      }
      
      existingRecord = attributeAssignResult.getAttributeAssign();
      
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.saveOrUpdate();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getAttributeAssign().saveUpdateProperties(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableMultiple#xmlRetrieveByIdOrKey(java.util.Collection)
   */
  public AttributeAssign xmlRetrieveByIdOrKey(Collection<String> idsToIgnore) {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findByUuidOrKey(idsToIgnore,
        this.id, this.attributeDefNameId, this.attributeAssignActionId, this.ownerAttributeAssignId, this.ownerAttributeDefId, this.ownerGroupId,
        this.ownerMemberId, this.ownerMembershipId, this.ownerStemId,  
        false, this.disabledTimeDb, this.enabledTimeDb, this.notes);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportAttributeAssign xmlToExportAttributeAssign(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    XmlExportAttributeAssign xmlExportAttributeAssign = new XmlExportAttributeAssign();
    xmlExportAttributeAssign.setAttributeAssignActionId(this.getAttributeAssignActionId());
    xmlExportAttributeAssign.setAttributeAssignDelegatable(this.getAttributeAssignDelegatableDb());
    xmlExportAttributeAssign.setAttributeAssignType(this.getAttributeAssignTypeDb());
    xmlExportAttributeAssign.setAttributeDefNameId(this.getAttributeDefNameId());
    xmlExportAttributeAssign.setContextId(this.getContextId());
    xmlExportAttributeAssign.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportAttributeAssign.setDisabledTime(GrouperUtil.dateStringValue(this.getDisabledTimeDb()));
    xmlExportAttributeAssign.setEnabled(this.getEnabledDb());
    xmlExportAttributeAssign.setEnabledTime(GrouperUtil.dateStringValue(this.getEnabledTimeDb()));
    xmlExportAttributeAssign.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportAttributeAssign.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportAttributeAssign.setNotes(this.getNotes());
    xmlExportAttributeAssign.setOwnerAttributeAssignId(this.getOwnerAttributeAssignId());
    xmlExportAttributeAssign.setOwnerAttributeDefId(this.getOwnerAttributeDefId());
    xmlExportAttributeAssign.setOwnerGroupId(this.getOwnerGroupId());
    xmlExportAttributeAssign.setOwnerMemberId(this.getOwnerMemberId());
    xmlExportAttributeAssign.setOwnerMembershipId(this.getOwnerMembershipId());
    xmlExportAttributeAssign.setOwnerStemId(this.getOwnerStemId());
    xmlExportAttributeAssign.setUuid(this.getId());
     
    return xmlExportAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("AttributeAssign: " + this.getId());

//    XmlExportUtils.toStringAttributeAssign(null, stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

  /**
   * get the delegate that relates the object with the the assignment
   * @return the delegate
   */
  public AttributeAssignable retrieveAttributeAssignable() {
    AttributeDef attributeDef = this.getAttributeDef();
    if (attributeDef.isAssignToGroup()) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerGroupId, true);
      return group;
    }
    if (attributeDef.isAssignToStem()) {
      Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerStemId, true);
      return stem;
    }
    if (attributeDef.isAssignToMember()) {
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerMemberId, true);
      return member;
    }
    if (attributeDef.isAssignToAttributeDef()) {
      AttributeDef attributeDefOwner = AttributeDefFinder.findById(this.ownerAttributeDefId, true);
      return attributeDefOwner;
    }
    if (attributeDef.isAssignToEffMembership()) {
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerGroupId, true);
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), this.ownerMemberId, true);
      return new EffectiveMembershipWrapper(group, member);
    }
    if (attributeDef.isAssignToImmMembership()) {
      Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(this.ownerMembershipId, true, false);
      return membership;
    }
    if (attributeDef.isAssignToAttributeDefAssn() || attributeDef.isAssignToEffMembershipAssn() 
        || attributeDef.isAssignToGroupAssn() || attributeDef.isAssignToImmMembershipAssn()
        || attributeDef.isAssignToMemberAssn() || attributeDef.isAssignToStemAssn()) {
      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign()
        .findById(this.ownerAttributeAssignId, true);
      return attributeAssign;
    }
    throw new RuntimeException("Cannot find assign delegate for assignment and attributeDef: " 
        + this.id + ", " + attributeDef.getName());
    
  }
  
  /**
   * get the delegate that relates the object with the the assignment
   * @return the delegate
   */
  public AttributeAssignBaseDelegate retrieveAttributeAssignDelegate() {
    return this.retrieveAttributeAssignable().getAttributeDelegate();
  }
  
  /** delegate to manage values on this assignment */
  private AttributeAssignValueDelegate valueDelegate;
  
  /**
   * 
   * @return the value delegate
   */
  public AttributeAssignValueDelegate getValueDelegate() {
    if  (this.valueDelegate == null) {
      this.valueDelegate = new AttributeAssignValueDelegate(this);
    }
    return this.valueDelegate;
  }
  
}
