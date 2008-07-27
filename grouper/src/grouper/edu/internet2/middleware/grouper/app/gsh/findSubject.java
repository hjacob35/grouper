/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;

/**
 * Find a {@link Subject}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: findSubject.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 * @since   0.0.1
 */
public class findSubject {

  // PUBLIC CLASS METHODS //

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @return  Found {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    catch (SubjectNotUniqueException eSNU) {
      GrouperShell.error(i, eSNU);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name)

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @return  Found {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id, type);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    catch (SubjectNotUniqueException eSNU) {
      GrouperShell.error(i, eSNU);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name, type)

  /**
   * Find a {@link Subject}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   id          Subject <i>id</i>.
   * @param   type        Subject <i>type</i>.
   * @param   source      Subject <i>source</i>.
   * @return  Found {@link RegistrySubject}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Subject invoke(
    Interpreter i, CallStack stack, String id, String type, String source
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      return SubjectFinder.findById(id, type, source);
    }
    catch (SourceUnavailableException eSNA) {
      GrouperShell.error(i, eSNA);
    }
    catch (SubjectNotFoundException eSNF) {
      GrouperShell.error(i, eSNF);
    }
    catch (SubjectNotUniqueException eSNU) {
      GrouperShell.error(i, eSNU);
    }
    return null;
  } // public static Subject invoke(i, stack, parent, name, type, source)

} // public class findSubject
