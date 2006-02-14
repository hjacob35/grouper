/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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

package edu.internet2.middleware.grouper;

import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/**
 * Perform low-level operations on the Groups Registry.
 * <p>
 * <strong>WARNING:</strong> Do <strong>not</strong> run the methods
 * expose by this class against your Grouper installation unless you
 * know what you are doing.  It <strong>will</strong> delete data.
 * </p>
 * @author  blair christensen.
 * @version $Id: RegistryReset.java,v 1.11 2006-02-14 19:58:41 blair Exp $
 */
public class RegistryReset {

  /*
   * TODO
   * * Use HQL
   * * Make SQL and table names class constants
   * * Preserve root stem based upon query for stem id
   * * Preserve grouper stem based upon query for stem id
   * * Preserve wheel group (including attributes and types) based upon 
   *   query for group id
   * * Preserve creator-of-root-stem, create-of-grouper-stem and
   *   creator-of-wheel-group
   * * Move subject adding to a different class?
   */

  // Private Class Constants
  private static final Log    LOG         = LogFactory.getLog(RegistryReset.class);
  private static final String SUBJ_TYPE   = "person"; 


  // Private Instance Variables
  private Connection  conn;
  private Session     hs;
  private Transaction tx;


  // Constructors
  private RegistryReset() {
    super();
  } // private RegistryReset()


  // Public Class methods
 
  /**
   * Add JDBC test subjects to the Groups Registry.
   */ 
  public static void addTestSubjects() {
    RegistryReset rr = new RegistryReset();
    try {
      rr._setUp();
      rr._addSubjects();
    }
    catch (Exception e) {
      rr._abort(e.getMessage());
    }
    finally {
      rr._tearDown();
    } 
  } // public static void addTestSubjects()

  /**
   * Reset the Groups Registry.
   * <p>
   * <strong>WARNING:</strong> This is a destructive act and will
   * delete all groups, stems, members, memberships and subjects from
   * your Groups Registry.  Do <strong>not</strong> run this unless
   * that is what you want.
   * </p>
   * <pre class="eg">
   * % java edu.internet2.middleware.grouper.RegistryReset
   * </pre>
   */
  public static void main(String[] args) {
    RegistryReset.reset();
    System.exit(0);
  } // public static void main(args)

  /**
   * Attempt to reset the Groups Registry to a pristine state.
   */
  public static void reset() {
    RegistryReset rr = new RegistryReset();
    try {
      rr._setUp();
      rr._emptyTables();
    }
    catch (Exception e) {
      rr._abort(e.getMessage());
    }
    finally {
      rr._tearDown();
    } 
    CacheMgr.resetAllCaches();
  } // public static void reset()

  /**
   * Attempt to reset the Groups Registry to a pristine state and then
   * add JDBC test subjects.
   */
  public static void resetRegistryAndAddTestSubjects() {
    RegistryReset rr = new RegistryReset();
    try {
      rr._setUp();
      rr._emptyTables();
      rr._addSubjects();
    }
    catch (Exception e) {
      rr._abort(e.getMessage());
    }
    finally {
      rr._tearDown();
    } 
    CacheMgr.resetAllCaches();
  } // public static void resetRegistryAndAddTestSubjects()


  // Private Instance Methods

  private void _addSubject(
    PreparedStatement add, String id, String type, String name
  ) 
  {
    try {
      add.setString(1, id);
      add.setString(2, type);
      add.setString(3, name);
      add.executeUpdate();
    }
    catch (SQLException eSQL) {
      String err = "unable to add subject " + id + ": " + eSQL.getMessage();
      this._abort(err);
    }
  } // private void _addSubject(id, type, name)

  private void _addSubjects() {
    try {
      PreparedStatement add = this.conn.prepareStatement(
        "INSERT INTO Subject (subjectID, SubjectTypeID, name) "
        + "VALUES (?, ?, ?)"
      );
      for (int i=0; i<100; i++) {
        String  id    = "test.subject." + i;
        String  name  = "my name is " + id;
        this._addSubject(add, id, SUBJ_TYPE, name);
      }
    }
    catch (SQLException eSQL) {
      String err = "unable to prepare statement: " + eSQL.getMessage();
      this._abort(err);
    }
  } // private void _addSubjects()

  private void _abort(String err) {
    LOG.fatal(err);
    throw new RuntimeException(err);
  } // private void _abort(err)

  private void _emptyTable(String table) {
    this._emptyTable(table, "delete from " + table);
  } // private void _emptyTable(table)

  private void _emptyTable(String table, String sql) {
    try {
      PreparedStatement update = this.conn.prepareStatement(sql);
      try {
        update.executeUpdate();
      } 
      catch (SQLException eSQL) {
        String err = "unable to update table " + table + "(" 
          + sql + "): " + eSQL.getMessage();
        this._abort(err);
      }
    } catch (SQLException eSQL) {
      String err = "unable to prepare statement for table " 
        + table + "(" + sql + "): " + eSQL.getMessage();
      this._abort(err);
    }
  } // private void _emptyTable(table, sql)

  private void _emptyTableGrouperGroups() {
    String  table = "grouper_groups";
    String  sqlNoModifier = "update " + table + " set "
      + "modifier_id = null, modify_source = null, modify_time = 0.0";
    String  sqlNoParent   = "update " + table + " set "
      + "parent_stem = null";
    this._emptyTable(table, sqlNoModifier);
    this._emptyTable(table, sqlNoParent);
    this._emptyTable(table);
  } // private void _emptyTableGrouperGroups()

  private void _emptyTableGrouperMembers() {
    String  table = "grouper_members";
    String  sqlSaveRoot = "delete from " + table + " where "
      + "subject_id != 'GrouperSystem'";
    this._emptyTable(table, sqlSaveRoot);
  } // private void _emptyTableGrouperMembers()

  private void _emptyTableGrouperMemberships() {
    String  table       = "grouper_memberships";
    String  sqlNoParent = "update " + table + " set "
      + "parent_membership = null";
    this._emptyTable(table, sqlNoParent);
    this._emptyTable(table);
  } // private void _emptyTableGrouperGroups()

  private void _emptyTableGrouperStems() {
    String  table = "grouper_stems";
    String  sqlNoModifier = "update " + table + " set "
      + "modifier_id = null, modify_source = null, modify_time = 0.0";
    String  sqlNoParent   = "update " + table + " set "
      + " parent_stem = null";
    String sqlSaveRoot    = "delete from " + table + " where "
      + "name != '" + Stem.ROOT_INT + "'";
    this._emptyTable(table, sqlNoModifier);
    this._emptyTable(table, sqlNoParent);
    this._emptyTable(table, sqlSaveRoot);
  } // private void _emptyTableGrouperStems()

  private void _emptyTableGrouperTypes() {
    // TODO This should be part of a larger, all HQL transaction
    try {
      Transaction tx  = this.hs.beginTransaction();
      hs.delete(
        "from GroupType as t where "
        + "(t.name != 'base' and t.name != 'naming')"
      );
      tx.commit();
    }
    catch (Exception e) {
      this._abort(e.getMessage());    
    }
  } // private void _emptyTableGrouperTypes()

  private void _emptyTables() {
    this._emptyTable("grouper_groups_types");
    this._emptyTableGrouperMemberships();
    this._emptyTable("grouper_sessions");
    this._emptyTable("grouper_attributes");
    this._emptyTableGrouperGroups();
    this._emptyTableGrouperStems();
    this._emptyTable("grouper_factors");
    this._emptyTableGrouperMembers();
    this._emptyTableGrouperTypes();
    this._emptyTable("SubjectAttribute");
    this._emptyTable("Subject");
  } // private void _emptyTables()

  private void _setUp() 
    throws  HibernateException
  {
    this.hs   = HibernateHelper.getSession();
    this.conn = hs.connection();
    if (this.conn == null) {
      String msg = "null connection";
      LOG.fatal(msg);
      throw new HibernateException(msg);
    }
    this.tx   = this.hs.beginTransaction();
  } // private void _setUp()

  private void _tearDown() {
    try {
      if (this.hs != null) {
        if (this.tx != null) {
          this.tx.commit();
        }
        this.hs.close();
      }
    }
    catch (HibernateException eH) {
      String err = "unable to tearDown: " + eH.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // private void _tearDown()

}

