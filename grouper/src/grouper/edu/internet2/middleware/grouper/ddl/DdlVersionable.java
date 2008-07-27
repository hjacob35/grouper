/*
 * @author mchyzer
 * $Id: DdlVersionable.java,v 1.1 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;
 
import org.apache.ddlutils.model.Database;


/**
 * enums which are ddl version need to implement this interface
 */
public interface DdlVersionable {

  /**
   * get the version of this enum
   * @return the version
   */
  public int getVersion();

  /**
   * get the object name of this enum, e.g. if GrouperEnum, the object name is Grouper
   * @return the object name
   */
  public String getObjectName();

  /**
   * <pre>
   * get the table pattern for this dbname (would be nice if there were no overlap,
   * so ext's should not start with grouper, e.g. grouploader_
   * note that underscore is a wildcard which is unfortunate
   * @return the table patter, e.g. "GROUPER%"
   * </pre>
   */
  public String getDefaultTablePattern();
  
  /**
   * check to see if the changes are already made, and then add the changes
   * to the database object
   * that should be used to update from the previous version
   * @param database ddlutils database object
   */
  public void updateVersionFromPrevious(Database database);
}