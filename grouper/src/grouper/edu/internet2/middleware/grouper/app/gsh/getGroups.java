/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.filter.GroupNameFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;

import  java.util.*;

/**
 * Query for groups by name.
 * <p/>
 * @author  blair christensen.
 * @version $Id: getGroups.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.0.1
 */
public class getGroups {

  // PUBLIC CLASS METHODS //

  /**
   * Query for groups by name.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Find groups with <i>name</i> as part of their name.
   * @return  {@link Set} of {@link Group}s.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Set invoke(Interpreter i, CallStack stack, String name) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Stem            root  = StemFinder.findRootStem(s);
      GrouperQuery    gq    = GrouperQuery.createQuery(
        s, 
        new GroupNameFilter(name, root)
      );
      return gq.getGroups();
    }
    catch (QueryException eQ) {
      GrouperShell.error(i, eQ);
    }
    return null;
  } // public static Set invoke(i, stack, name)

} // public class getGroups
