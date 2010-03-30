/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.privs;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/** 
 * Privilege helper class.
 * <p>TODO 20070823 Relocate these methods once I figure out the best home for them.</p>
 * @author  blair christensen.
 * @version $Id: PrivilegeHelper.java,v 1.12 2009-09-28 05:06:46 mchyzer Exp $
 * @since   1.2.1
 */
public class PrivilegeHelper {


  /**
   * @param s 
   * @param g 
   * @param subj 
   * @return admin
   * @since   1.2.1
   */
  public static boolean canAdmin(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    AccessResolver accessResolver = s.getAccessResolver();
    //System.out.println(accessResolver.getClass().getName());
    //validatingAccessResolver
    return accessResolver.hasPrivilege(g, subj, AccessPrivilege.ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrAdmin(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrRead(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_READ)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrView(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_VIEW)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_READ)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTIN)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTOUT);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrUpdate(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_UPDATE)
      || attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_ADMIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrOptin(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTIN);
  } 

  /**
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @return admin
   */
  public static boolean canAttrOptout(GrouperSession s, AttributeDef attributeDef, Subject subj) {
    AttributeDefResolver attributeDefResolver = s.getAttributeDefResolver();
    return attributeDefResolver.hasPrivilege(attributeDef, subj, AttributeDefPrivilege.ATTR_OPTOUT);
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param ns 
   * @param subj 
   * @return can create
   * @since   1.2.1
   */
  public static boolean canCreate(GrouperSession s, Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return s.getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.CREATE);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return can
   * @since   1.2.1
   */
  public static boolean canOptin(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false;
  } 

  /**
   * 
   * @param s
   * @param stem
   * @param subj
   * @param privInSet
   * @return if has privilege
   */
  public static boolean hasPrivilege(GrouperSession s, Stem stem, Subject subj, Set<Privilege> privInSet) {
    
    for (Privilege privilege : privInSet) {
      if (s.getNamingResolver().hasPrivilege(stem, subj, privilege)) {
        return true;
      }
    }
    return false;
  } 

  /**
   * 
   * @param s
   * @param g
   * @param subj
   * @param privInSet
   * @return if has privilege
   */
  public static boolean hasPrivilege(GrouperSession s, Group g, Subject subj, Set<Privilege> privInSet) {
    
    for (Privilege privilege : privInSet) {
      if (s.getAccessResolver().hasPrivilege(g, subj, privilege)) {
      return true;
    }
    }
    return false;
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return  can optout
   * @since   1.2.1
   */
  public static boolean canOptout(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTOUT)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
    )
    {
      return true;
    }
    return false; 
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return  can read
   * @since   1.2.1
   */
  public static boolean canRead(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
   }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param ns 
   * @param subj 
   * @return can stem
   * @since   1.2.1
   */
  public static boolean canStem(Stem ns, Subject subj) {
    // TODO 20070820 deprecate
    // TODO 20070820 perform query for all privs and compare internally
    return GrouperSession.staticGrouperSession().getNamingResolver().hasPrivilege(ns, subj, NamingPrivilege.STEM);
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return can update
   * @since   1.2.1
   */
  public static boolean canUpdate(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
    )
    {
      return true;
    }
    return false; 
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @return can view
   * @since   1.2.1
   */
  public static boolean canView(GrouperSession s, Group g, Subject subj) {
    // TODO 20070816 deprecate
    // TODO 20070816 perform query for all privs and compare internally
    //note, no need for GrouperSession inverse of control
    if (
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.VIEW)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.READ)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.ADMIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.UPDATE)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTIN)
      ||
      s.getAccessResolver().hasPrivilege(g, subj, AccessPrivilege.OPTOUT)
    )
    {
      return true;
    }
    return false; 
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param candidates 
   * @return can view
   * @since   1.2.1
   */
  public static Set canViewGroups(GrouperSession s, Set candidates) {
    //note, no need for GrouperSession inverse of control
    Set       groups  = new LinkedHashSet();
    Group     g;
    Iterator  it      = candidates.iterator();
    while (it.hasNext()) {
      Object obj = it.next();
      g = (Group)obj;
      if ( canView( s, g, s.getSubject() ) ) {
        groups.add(g);
      }
    }
    return groups;
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param inputMemberships 
   * @return filtered memberships
   * @SINCE   1.2.1
   */
  public static Set<Membership> canViewMemberships(GrouperSession s, Collection<Membership> inputMemberships) {
    
    if (inputMemberships == null) {
      return null;
    }
    
    //make sure all groups are prepopulated
    Membership.retrieveGroups(inputMemberships);
    
    //note, no need for GrouperSession inverse of control
    Set<Membership>         mships  = new LinkedHashSet<Membership>();
    Membership  ms;
    Iterator    it      = inputMemberships.iterator();
    while ( it.hasNext() ) {
      ms = (Membership)it.next() ;
      try {
    	//2007-10-17: Gary Brown
    	//https://bugs.internet2.edu/jira/browse/GRP-38
        //Ah! Memberships for stem privileges are passed through here also
    	//The conditional makes sense - except it was wrong  -and didn't cope with stem privileges
        if ( FieldType.NAMING.equals( ms.getList().getType() ) ) {
          dispatch( s, ms.getStem(), s.getSubject(), ms.getList().getReadPriv() );
          mships.add(ms);
        } else if ( FieldType.ACCESS.equals( ms.getList().getType() ) ) {
        	dispatch( s, ms.getGroup(), s.getSubject(), ms.getList().getReadPriv() );
            mships.add(ms);
        } else if (FieldType.NAMING.equals( ms.getList().getType() ) ) {
          
          dispatch( s, ms.getAttributeDef(), s.getSubject(), ms.getList().getReadPriv() );
            mships.add(ms);

        } else if (FieldType.LIST.equals( ms.getList().getType() ) ) {
          
          //am I supposed to see what the read privilege is for the field, or just look at read???
          if (!canRead(s, ms.getGroup(), s.getSubject())) {
            continue;
          }
          
        } else {
          throw new RuntimeException("Invalid field type: " + ms.getList().getType());
        }
        mships.add(ms);
      } catch (InsufficientPrivilegeException e) {
        //ignore, not allowed, dont add
        continue;
      } catch (Exception e) {
        LOG.error("canViewMemberships: " + e.getMessage(), e );
      }
    }
    return mships;
  } 


  /**
   * @param grouperSession 
   * @param group 
   * @param field 
   * @return true or false
   */
  public static boolean canViewMembers(GrouperSession grouperSession, Group group, Field field) {
    try {
      dispatch( grouperSession, group, grouperSession.getSubject(), field.getReadPriv() );
      return true;
    } catch (InsufficientPrivilegeException e) {
      return false;
    } catch (SchemaException e) {
      throw new RuntimeException("Problem viewing members: " 
          + (grouperSession == null ? null : GrouperUtil.subjectToString(grouperSession.getSubject())) 
          + ", " + (group == null ? null : group.getName())
          + ", " + (field == null ? null : field.getName()), e);
  } 
  } 

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(PrivilegeHelper.class);

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param g 
   * @param subj 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws SchemaException 
   * @SINCE   1.2.1
   */
  public static void dispatch(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if ( !Privilege.isAccess(priv) ) {
      throw new SchemaException("access privileges only apply to groups");
    }

    if      (priv.equals(AccessPrivilege.ADMIN))  {
      rv = PrivilegeHelper.canAdmin(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_ADMIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTIN))  {
      rv = PrivilegeHelper.canOptin(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_OPTIN;
      }
    }
    else if (priv.equals(AccessPrivilege.OPTOUT)) {
      rv = PrivilegeHelper.canOptout(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_OPTOUT;
      }
    }
    else if (priv.equals(AccessPrivilege.READ))   {
      rv = PrivilegeHelper.canRead(s, g, subj);
      if (!rv) {
        msg = "subject " + subj.getId() + " cannot READ group: " + g.getName();
      }
    }
    else if (priv.equals(AccessPrivilege.VIEW))   {
      rv = PrivilegeHelper.canView( s, g, subj );
      if (!rv) {
        msg = E.CANNOT_VIEW;
      }
    }
    else if (priv.equals(AccessPrivilege.UPDATE)) {
      rv = PrivilegeHelper.canUpdate(s, g, subj);
      if (!rv) {
        msg = E.CANNOT_UPDATE;
      }
    }
    else if (priv.equals(AccessPrivilege.SYSTEM))  {
      msg = E.SYSTEM_MAINTAINED + priv;
    }
    else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param ns 
   * @param subj 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws SchemaException 
   * @SINCE   1.2.1
   */
  public static void dispatch(GrouperSession s, Stem ns, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if      ( !Privilege.isNaming(priv) ) {
      throw new SchemaException("naming privileges only apply to stems");
    }

    if      (priv.equals(NamingPrivilege.CREATE)) { 
      rv = PrivilegeHelper.canCreate(s, ns,  subj);
      if (!rv) {
        msg = E.CANNOT_CREATE;
      }
    }
    else if (priv.equals(NamingPrivilege.STEM))   {
      rv = PrivilegeHelper.canStem(ns, subj);
      if (!rv) {
        msg = E.CANNOT_STEM;
      }
    }
    else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } 

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param attributeDef 
   * @param subj 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws SchemaException 
   */
  public static void dispatch(GrouperSession s, AttributeDef attributeDef, Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException, SchemaException  {
    // TODO 20070823 this is ugly
    boolean rv  = false;
    String  msg = GrouperConfig.EMPTY_STRING; 

    if ( !Privilege.isAttributeDef(priv) ) {
      throw new SchemaException("attributeDef privileges only apply to attributeDefs");
    }

    if      (priv.equals(AttributeDefPrivilege.ATTR_ADMIN)) { 
      rv = PrivilegeHelper.canAttrAdmin(s, attributeDef,  subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_ADMIN;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_OPTIN))   {
      rv = PrivilegeHelper.canAttrOptin(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_OPTIN;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_OPTOUT))   {
      rv = PrivilegeHelper.canAttrOptout(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_OPTOUT;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_READ))   {
      rv = PrivilegeHelper.canAttrRead(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_READ;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_UPDATE))   {
      rv = PrivilegeHelper.canAttrUpdate(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_UPDATE;
      }
    } else if (priv.equals(AttributeDefPrivilege.ATTR_VIEW))   {
      rv = PrivilegeHelper.canAttrView(s, attributeDef, subj);
      if (!rv) {
        msg = E.CANNOT_ATTR_VIEW;
      }
    } else {
      throw new SchemaException(E.UNKNOWN_PRIVILEGE + priv);
    }
    if (!rv) {
      throw new InsufficientPrivilegeException(msg);
    }
  } 

  /**
   * TODO 20070824 add tests
   * @param privileges 
   * @return  Given an array of privileges return an array of access privileges.
   * @since   1.2.1
   */
  public static Privilege[] getAccessPrivileges(Privilege[] privileges) {
    Set<Privilege> accessPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isAccess(priv) ) {
        accessPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return accessPrivs.toArray(template);
  }

  /**
   * TODO 20070824 add tests
   * @param privileges 
   * @return  Given an array of privileges return an array of access privileges.
   * @since   1.2.1
   */
  public static Privilege[] getAttributeDefPrivileges(Privilege[] privileges) {
    Set<Privilege> attributeDefPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isAttributeDef(priv) ) {
        attributeDefPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return attributeDefPrivs.toArray(template);
  }

  /**
   * TODO 20070824 add tests
   * @param privileges 
   * @return  Given an array of privileges return an array of naming privileges.
   * @since   1.2.1
   */
  public static Privilege[] getNamingPrivileges(Privilege[] privileges) {
    Set<Privilege> namingPrivs = new LinkedHashSet();
    for ( Privilege priv : privileges ) {
      if ( Privilege.isNaming(priv) ) {
        namingPrivs.add(priv);
      }
    } 
    Privilege[] template = {};
    return namingPrivs.toArray(template);
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @return is root
   * @SINCE   1.2.1
   */
  public static boolean isRoot(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    if ( SubjectHelper.eq( s.getSubject(), SubjectFinder.findRootSubject() ) ) {
      rv = true;
    }
    else {
      rv = isWheel(s);
    }
    return rv;
  }

  /**
   * see if system subject
   * @param subject 
   * @return true if grouper system
   */
  public static boolean isSystemSubject(Subject subject) {
    return SubjectHelper.eq( subject, SubjectFinder.findRootSubject() );
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @return  is wheel
   * @SINCE   1.2.1
   */
  public static boolean isWheel(GrouperSession s) {
    // TODO 20070823 this is ugly
    boolean rv = false;
    
    if (s.isConsiderIfWheelMember() == false) {
      return false;
    }
    
    if ( Boolean.valueOf( GrouperConfig.getProperty( GrouperConfig.PROP_USE_WHEEL_GROUP ) ).booleanValue() ) {
      String name = GrouperConfig.getProperty( GrouperConfig.PROP_WHEEL_GROUP );
      try {
        // goodbye, performance
        Group wheel = GroupFinder.findByName( s.internal_getRootSession(), name, true );
        rv          = wheel.hasMember( s.getSubject() );
      }
      catch (GroupNotFoundException eGNF) {
        // wheel group not found. oh well!
        LOG.error( E.NO_WHEEL_GROUP + name );
      }
    } 
    return rv;
  }

  /**
   * see if a subject is wheel or root
   * @param subject 
   * @return true or false
   */
  public static boolean isWheelOrRoot(Subject subject) {
    if (SubjectHelper.eq( subject, SubjectFinder.findRootSubject() )) {
      return true;
    }
    
    if (GrouperSession.staticGrouperSession().isConsiderIfWheelMember() == false) {
      return false;
    }
    
    if (GrouperConfig.getPropertyBoolean(GrouperConfig.PROP_USE_WHEEL_GROUP, false)) {
      String name = GrouperConfig.getProperty( GrouperConfig.PROP_WHEEL_GROUP );
      try {
        Group wheel = GroupFinder.findByName( GrouperSession.staticGrouperSession().internal_getRootSession(), name, true );
        return wheel.hasMember(subject);
      } catch (GroupNotFoundException gnfe) {
        throw new GrouperException("Cant find wheel group: " + name, gnfe);
      }
    }
    return false;
  } 
  
  /**
   * Is this user allowed to move stems?
   * @param subject 
   * @return boolean
   */
  public static boolean canMoveStems(Subject subject) {
    String allowedGroupName = GrouperConfig
        .getProperty("security.stem.groupAllowedToMoveStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Is this user allowed to copy stems?
   * @param subject 
   * @return boolean
   */
  public static boolean canCopyStems(Subject subject) {
    String allowedGroupName = GrouperConfig
        .getProperty("security.stem.groupAllowedToCopyStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Is this user allowed to rename stems?
   * @param subject 
   * @return boolean
   */
  public static boolean canRenameStems(Subject subject) {
    String allowedGroupName = GrouperConfig
        .getProperty("security.stem.groupAllowedToRenameStem");
    if (StringUtils.isNotBlank(allowedGroupName) && !isWheelOrRoot(subject)) {

      Group allowedGroup = GroupFinder.findByName(GrouperSession.staticGrouperSession()
          .internal_getRootSession(), allowedGroupName, false);
      if (allowedGroup == null || !allowedGroup.hasMember(subject)) {
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param s
   * @param attributeDef
   * @param subj
   * @param privInSet
   * @return if has privilege
   */
  public static boolean hasPrivilege(GrouperSession s, AttributeDef attributeDef, Subject subj, Set<Privilege> privInSet) {
    
    for (Privilege privilege : privInSet) {
      if (s.getAttributeDefResolver().hasPrivilege(attributeDef, subj, privilege)) {
        return true;
      }
    }
    return false;
  }

  /**
   * TODO 20070823 find a real home for this and/or add tests
   * @param s 
   * @param inputAttributeDefs 
   * @return filtered attributeDefs
   * @SINCE   1.2.1
   */
  public static Set<AttributeDef> canViewAttributeDefs(GrouperSession s, Collection<AttributeDef> inputAttributeDefs) {
    
    if (inputAttributeDefs == null) {
      return null;
    }
    
    //note, no need for GrouperSession inverse of control
    Set<AttributeDef>         attrDefs  = new LinkedHashSet<AttributeDef>();
    AttributeDef  attributeDef;
    Iterator<AttributeDef>    it      = inputAttributeDefs.iterator();
    while ( it.hasNext() ) {
      attributeDef = it.next() ;
      try {
      	//2007-10-17: Gary Brown
      	//https://bugs.internet2.edu/jira/browse/GRP-38
          //Ah! Memberships for stem privileges are passed through here also
      	//The conditional makes sense - except it was wrong  -and didn't cope with stem privileges
        dispatch( s, attributeDef, s.getSubject(), AttributeDefPrivilege.ATTR_VIEW );
        attrDefs.add(attributeDef);
        
      } catch (InsufficientPrivilegeException e) {
        //ignore, not allowed, dont add
        continue;
      }
    }
    return attrDefs;
  }

  /**
   * TODO 20100327 find a real home for this and/or add tests
   * @param s 
   * @param inputAttributeAssigns 
   * @return filtered memberships
   * @SINCE   1.2.1
   */
  public static Set<AttributeAssign> canViewAttributeAssigns(GrouperSession s, Collection<AttributeAssign> inputAttributeAssigns) {
    
    if (inputAttributeAssigns == null) {
      return null;
    }
    
    Set<AttributeAssign> attributeAssigns  = new LinkedHashSet<AttributeAssign>();
    
    for (AttributeAssign attributeAssign : inputAttributeAssigns) {
      try {
        
        //first try the attributeDefs
        AttributeDef attributeDef = attributeAssign.getAttributeDef();
        
        dispatch(s, attributeDef, s.getSubject(), AttributeDefPrivilege.ATTR_READ);
        
        //now, depending on the assignment, check it out
        AttributeAssignType attributeAssignType = attributeAssign.getAttributeAssignType();
        
        switch (attributeAssignType) {
          case group:
            dispatch(s, attributeAssign.getOwnerGroup(), s.getSubject(), AccessPrivilege.VIEW);
            break;

          case stem:
            //no need to check stem, everyone can view all stems
            break;
            
          case member:
            //no need to check member, everyone can edit all members
            break;
            
          case attr_def:
            dispatch(s, attributeAssign.getOwnerAttributeDef(), s.getSubject(), AttributeDefPrivilege.ATTR_VIEW);
            break;
            
          case imm_mem:
            dispatch(s, attributeAssign.getOwnerImmediateMembership().getGroup(), s.getSubject(), AccessPrivilege.READ);
            break;

          default: 
            throw new RuntimeException("Not expecting attributeAssignType: " + attributeAssignType);
          
          
        }
        
        //ok, add to list
        attributeAssigns.add(attributeAssign);

      } catch (InsufficientPrivilegeException e) {
        //ignore, not allowed, dont add
        continue;
      }
      
    }
    return attributeAssigns;
  }

}

