/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: HooksMemberBean.java,v 1.3 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for member low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksMemberBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: member */
  public static final String FIELD_MEMBER = "member";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_MEMBER);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Member member = null;
  
  /**
   * 
   */
  public HooksMemberBean() {
    super();
  }

  /**
   * @param theMember
   */
  public HooksMemberBean(Member theMember) {
    this.member = theMember;
  }
  
  /**
   * object being inserted
   * @return the Member
   */
  public Member getMember() {
    return this.member;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksMemberBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}
