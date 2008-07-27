/*
 * @author mchyzer $Id: GrouperDdlUtils.java,v 1.3 2008-07-27 07:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ddl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.ForeignKey;
import org.apache.ddlutils.model.Index;
import org.apache.ddlutils.model.IndexColumn;
import org.apache.ddlutils.model.NonUniqueIndex;
import org.apache.ddlutils.model.Reference;
import org.apache.ddlutils.model.Table;
import org.apache.ddlutils.model.UniqueIndex;
import org.apache.ddlutils.platform.SqlBuilder;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperDdl;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.LifecycleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksLifecycleDdlInitBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.registry.RegistryInitializeSchema;
import edu.internet2.middleware.grouper.registry.RegistryInstall;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class GrouperDdlUtils {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperDdlUtils.class);

  /** if inside bootstrap, ok to use hibernate */
  private static boolean insideBootstrap = false;

  /**
   * if we are inside the bootstrap, or if everything is ok, we are good to go
   * @return true if ok
   */
  public static boolean okToUseHibernate() {
    return insideBootstrap || everythingRightVersion || RegistryInitializeSchema.inInitSchema;
  }
  
  /**
   * retrieve the ddl utils platform
   * @return the platform object
   */
  public static Platform retrievePlatform() {
    String ddlUtilsDbnameOverride = GrouperConfig.getProperty("ddlutils.dbname.override");
    Platform platform = null;

    //convenience to get the url, user, etc of the grouper db
    GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");

    if (StringUtils.isBlank(ddlUtilsDbnameOverride)) {
      platform = PlatformFactory.createNewPlatformInstance(grouperDb.getDriver(),
          grouperDb.getUrl());
    } else {
      platform = PlatformFactory.createNewPlatformInstance(ddlUtilsDbnameOverride);
    }

    return platform;
  }

  /**
   * kick off bootstrap
   * @param args
   */
  public static void main(String[] args) {
    GrouperStartup.startup();
  }
  
  
  /**
   * true to compare ddl from db version to the current java version, 
   * false to start over and find all diffs (without deleting existing) 
   */
  public static boolean compareFromDbDllVersion = true;

  /**
   * only run this once
   */
  private static boolean bootstrapDone = false;
  
  /** if everything is the right version */
  private static boolean everythingRightVersion = true;
  
  /**
   * startup the process, if the version table is not there, print out that ddl
   * @param callFromCommandLine
   * @param installDefaultGrouperData 
   */
  @SuppressWarnings("unchecked")
  public static void bootstrap(boolean callFromCommandLine, boolean installDefaultGrouperData) {
    if (bootstrapDone) {
      if (callFromCommandLine) {
        throw new RuntimeException("DDL bootstrap is already done, something is wrong...");
      }
      return;
    }

    try {
      //do here so we arent re-entrant
      bootstrapDone = true;
  
      bootstrapHelper(callFromCommandLine, false, !callFromCommandLine || compareFromDbDllVersion, 
          callFromCommandLine && RegistryInitializeSchema.isDropBeforeCreate(), 
          callFromCommandLine && RegistryInitializeSchema.isWriteAndRunScript(), false, installDefaultGrouperData);
    } catch (RuntimeException re) {
      everythingRightVersion = false;
      throw re;
    }
  }

  /** keep track if we have already inserted a record here, then subsequent ones are updates */
  private static Set<String> alreadyInsertedForObjectName = new HashSet<String>();
  
  /**
   * helper method which is more easily testable
   * @param callFromCommandLine
   * @param fromUnitTest true if just testing this method
   * @param theCompareFromDbVersion 
   * @param theDropBeforeCreate
   * @param theWriteAndRunScript
   * @param dropOnly just drop stuff, e.g. for unit test
   * @param installDefaultGrouperData if registry install should be called afterwards
   */
  @SuppressWarnings("unchecked")
  static void bootstrapHelper(boolean callFromCommandLine, boolean fromUnitTest,
      boolean theCompareFromDbVersion, boolean theDropBeforeCreate, boolean theWriteAndRunScript,
      boolean dropOnly, boolean installDefaultGrouperData) {
        
    try {
      insideBootstrap = true;

      //clear out for this run (in case testing, might call this multiple times)
      alreadyInsertedForObjectName.clear();
      
      //clear out cache of object versions since if multiple calls from unit tests, can get bad data
      cachedDdls = null;
      
      //if we are messing with ddl, lets clear caches
      FieldFinder.clearCache();
      GroupTypeFinder.clearCache();
      
      Platform platform = retrievePlatform();
      
      //this is in the config or just in the driver
      String dbname = platform.getName();
      
      LOG.info("Ddl db name is: '" + dbname + "'");
      
      //convenience to get the url, user, etc of the grouper db, helps get db connection
      GrouperLoaderDb grouperDb = GrouperLoaderConfig.retrieveDbProfile("grouper");
      
      Connection connection = null;
  
      StringBuilder result = new StringBuilder();
      
      try {
        connection = grouperDb.connection();
  
        List<String> objectNames = retrieveObjectNames();
        
        for (String objectName : objectNames) {
  
          Class<Enum> objectEnumClass = null;
          
          try {
            objectEnumClass = retrieveDdlEnum(objectName);
          } catch (RuntimeException e) {
            //if this is grouper or subject, we have problems
            if (StringUtils.equals(objectName, "Grouper") || StringUtils.equals(objectName, "Subject")) {
              //kill the app
              everythingRightVersion = false;
              throw e;
            }
            //this is probably ok I guess, since the UI tables might not have logic in ws or whatever...
            LOG.warn("This might be ok, since the DDL isnt managed from this app, but here is the issue for ddl app '" + objectName + "' " + e.getMessage());
          }
          
          //this is the version in java
          int javaVersion = retrieveDdlJavaVersion(objectName); 
          
          DdlVersionable ddlVersionable = retieveVersion(objectName, javaVersion);
          
          StringBuilder historyBuilder = retrieveHistory(objectName);
          
          //this is the version in the db
          int realDbVersion = retrieveDdlDbVersion(objectName);
          
          String versionStatus = "Ddl object type '" + objectName + "' has dbVersion: " 
            + realDbVersion + " and java version: " + javaVersion;
          
          boolean versionMismatch = javaVersion != realDbVersion;
  
          if (callFromCommandLine) {
            System.err.println(versionStatus);
          } else {
            if (versionMismatch) {
              if (!LOG.isErrorEnabled()) {
                System.err.println(versionStatus);
              } else {
                LOG.error(versionStatus);
              }
            } else {
              LOG.info(versionStatus);
            }
            
          }
  
          //one originally in the DB
          @SuppressWarnings("unused")
          int originalDbVersion = realDbVersion;
          
          //this is the logic version in the objects
          int dbVersion = realDbVersion;
  
          if (!theCompareFromDbVersion || theDropBeforeCreate) {
            //if going from nothing, then go from nothing
            dbVersion = 0;
          }
          
          //reset to take into account if starting from scratch
          versionMismatch = javaVersion != dbVersion;
          
          //see if same version, just continue, all good
          if (!versionMismatch) {
            continue;
          }
          
          //shut down hibernate if not just testing
          if (!fromUnitTest) {
            everythingRightVersion = false;
          }
          
          //if the java is less than db, then grouper was rolled back... that might not be good
          if (javaVersion < dbVersion) {
            LOG.warn("Java version of db object name: " + objectName + " is " 
                + javaVersion + " which is less than the dbVersion " + dbVersion
                + ".  This means grouper was upgraded and rolled back?  Check in the enum "
                + objectEnumClass.getName() + " for details on if things are compatible.");
            //not much we can do here... good luck!
            continue;
          }
  
          //pattern to get only certain objects (e.g. GROUPERLOADER% )
          String defaultTablePattern = ddlVersionable.getDefaultTablePattern(); 
          //to be safe lets only deal with tables related to this object
          platform.getModelReader().setDefaultTablePattern(defaultTablePattern);
          //platform.getModelReader().setDefaultTableTypes(new String[]{"TABLES"});
  
          SqlBuilder sqlBuilder = platform.getSqlBuilder();
          
          // if deleting all, lets delete all:
          if (theDropBeforeCreate) {
            //it needs a name, just use "grouper"
            Database oldDatabase = platform.readModelFromDatabase(connection, "grouper", null,
                grouperDb.getUser().toUpperCase(), null);
            Database newDatabase = platform.readModelFromDatabase(connection, "grouper", null,
                grouperDb.getUser().toUpperCase(), null);
            
            removeAllTables(newDatabase);
            
            String script = convertChangesToString(objectName, sqlBuilder, oldDatabase, newDatabase);
            
            if (!StringUtils.isBlank(script)) {
              result.append(script);
            }
            
          }
          
          if (!dropOnly) {
            //the db version is less than the java version
            //lets go up one version at a time until we are current
            for (int version = dbVersion+1; version<=javaVersion;version++) {
    
              ddlVersionable = retieveVersion(objectName, version);
              //we just want a script, see if one exists for this version
              String script = findScriptOverride(ddlVersionable, dbname);
              
              //if there was no override
              if (StringUtils.isBlank(script)) {
                
                //it needs a name, just use "grouper"
                Database oldDatabase = platform.readModelFromDatabase(connection, "grouper", null,
                    grouperDb.getUser().toUpperCase(), null);
                Database newDatabase = platform.readModelFromDatabase(connection, "grouper", null,
                    grouperDb.getUser().toUpperCase(), null);
                
                if (theDropBeforeCreate) {
                  removeAllTables(oldDatabase);
                  removeAllTables(newDatabase);
                }
                
                //get this to the previous version
                upgradeDatabaseVersion(oldDatabase, dbVersion, objectName, version-1);
                //get this to the current version
                upgradeDatabaseVersion(newDatabase, dbVersion, objectName, version);
                
                script = convertChangesToString(objectName, sqlBuilder, oldDatabase,
                    newDatabase);
                
                //String ddl = platform.getAlterTablesSql(connection, database);
              }
              //make sure no single quotes in any of these...
              String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
              //is this db independent?  if not, figure out what the issues are and fix so we can have comments
              String summary = timestamp + ": upgrade " + objectName + " from V" + (version-1) + " to V" + version;
              
              boolean scriptNotBlank = !StringUtils.isBlank(script);
              //dont do this if shouldnt
              boolean upgradeDdlTable = realDbVersion < version || theDropBeforeCreate;
    
              if (scriptNotBlank || upgradeDdlTable) {
                result.append("/* " + summary + " */\n");
              }
              
              if (scriptNotBlank) {
                result.append(script).append("\n\n");
              }
    
              if (upgradeDdlTable) {
                realDbVersion = version;
                historyBuilder.insert(0, summary + ", ");
                
                String historyString = StringUtils.abbreviate(historyBuilder.toString(), 4000);
      
                //see if already in db
                if ((!containsDbRecord(objectName) || (version == 1 && theDropBeforeCreate)) 
                    && !alreadyInsertedForObjectName.contains(objectName)) {
                
                  result.append("insert into grouper_ddl (id, object_name, db_version, " +
                  		"last_updated, history) values ('" + GrouperUuid.getUuid() 
                      +  "', '" + objectName + "', 1, '" + timestamp + "', \n'" + historyString + "');\n");
                  //dont insert again for this object
                  alreadyInsertedForObjectName.add(objectName);
  
                } else {
                  
                  result.append("update grouper_ddl set db_version = " + version 
                      + ", last_updated = '" + timestamp + "', \nhistory = '" + historyString 
                      + "' where object_name = '" + objectName + "';\n");
  
                }
                result.append("commit;\n\n");
              }
            }
          }
        }
  
      } finally {
        GrouperUtil.closeQuietly(connection);
      }
  
      String resultString = result.toString();
      
      if (StringUtils.isNotBlank(resultString)) {
  
        String scriptDirName = GrouperConfig.getProperty("ddlutils.directory.for.scripts");
        
        File scriptFile = GrouperUtil.newFileUniqueName(scriptDirName, "grouperDdl", ".sql", true);
        GrouperUtil.saveStringIntoFile(scriptFile, resultString);
  
        String logMessage = "Grouper database schema DDL requires updates, script file is:\n" + scriptFile.getAbsolutePath();
        if (LOG.isErrorEnabled()) {
          LOG.error(logMessage);
          if (callFromCommandLine) {
            System.err.println(logMessage);
          }
        } else {
          System.err.println(logMessage);
        }

        if (theWriteAndRunScript) {
  
          PrintStream err = System.err;
          PrintStream out = System.out;
          InputStream in = System.in;
  
          //dont let ant mess up or close the streams
          ByteArrayOutputStream baosOutErr = new ByteArrayOutputStream();
          PrintStream newOutErr = new PrintStream(baosOutErr);
  
          System.setErr(newOutErr);
          System.setOut(newOutErr);
          
          SQLExec sqlExec = new SQLExec();
          
          sqlExec.setSrc(scriptFile);
          
          sqlExec.setDriver(grouperDb.getDriver());
  
          sqlExec.setUrl(grouperDb.getUrl());
          sqlExec.setUserid(grouperDb.getUser());
          sqlExec.setPassword(grouperDb.getPass());

          Project project = new GrouperAntProject();
  
          //tell output where to go
          DefaultLogger defaultLogger = new DefaultLogger();
          defaultLogger.setErrorPrintStream(newOutErr);
          defaultLogger.setOutputPrintStream(newOutErr);
          project.addBuildListener(defaultLogger);
          
          try {
            sqlExec.setProject(project);
  
            sqlExec.execute();

            logMessage = "Script was executed successfully";
          } catch (Exception e) {
            logMessage = "Error running script: " + ExceptionUtils.getFullStackTrace(e) + "\n";
          } finally {
          
            newOutErr.flush();
            newOutErr.close();
            
            System.setErr(err);
            System.setOut(out);
            System.setIn(in);
          }
          
          String antOutput = StringUtils.trimToEmpty(baosOutErr.toString());
  
          if (!StringUtils.isBlank(antOutput)) {
            logMessage = antOutput + "\n";
          }
          //if call from command line, print to screen
          if (LOG.isErrorEnabled() && !callFromCommandLine) {
            LOG.error(logMessage);
          } else {
            System.out.println(logMessage);
          }
        } else {
          if (callFromCommandLine) {
            System.err.println("Note: this script was not executed per the grouper.properties: ddlutils.schemaexport.writeAndRunScript");
            System.err.println("To run script via ant, carefully review it, then run this:\nant -Dname=" + scriptFile.getAbsolutePath() + " sql");
          }
        }
      } else {
        boolean printed = false;
        String note = "NOTE: database table/object structure (ddl) is up to date";
        if (!theCompareFromDbVersion) {
          //no script to update
          if (LOG.isErrorEnabled()) {
            LOG.error(note);
          } else {
            printed = true;
            System.err.println(note);
          }
        }
        if (!printed && callFromCommandLine) {
          System.err.println(note);
        }
      }
      if (installDefaultGrouperData) {
        try {
          RegistryInstall.main(new String[]{"internal"});
        } catch (RuntimeException e) {
          
          GrouperDdlUtils.everythingRightVersion = false;
          throw e;
        }
      }
    } finally {
      insideBootstrap = false;
    }
  }

  /**
   * @param objectName
   * @param sqlBuilder
   * @param oldDatabase
   * @param newDatabase
   * @return string
   */
  private static String convertChangesToString(String objectName, SqlBuilder sqlBuilder,
      Database oldDatabase, Database newDatabase) {
    String script;
    //upgrade to version: version
    //we need to upgrade from one version to another, but dont want to get the version from the DB, so 
    //call protected method via reflection
    StringWriter buffer = new StringWriter();
    sqlBuilder.setWriter(buffer);
    try {
      
      sqlBuilder.alterDatabase(oldDatabase, newDatabase, null);
    } catch (Exception e) {
      throw new RuntimeException("Problem with object name: " + objectName, e);
    }
    
    //GrouperUtil.callMethod(sqlBuilder.getClass(), sqlBuilder, "processTableStructureChanges",
    //  new Class[]{Database.class, Database.class, Table.class, Table.class, Map.class, List.class},
    //  new Object[]{oldDatabase, newDatabase, oldDatabase.findTable("grouper_ext_loader_log"), table, null, GrouperUtil.toList(addColumnChange)});

    script = buffer.toString();
    return script;
  }

  /**
   * remove all objects, the foreign keys, then the tables
   * @param database
   */
  public static void removeAllTables(Database database) {
    
    //delete all foreign keys
    for (Table table : GrouperUtil.nonNull(database.getTables())) {
      database.removeTable(table);
    }

  }
  
  /**
   * find history for a certain object name
   * @param objectName
   * @return the history or new stringbuilder if none available
   */
  public static StringBuilder retrieveHistory(String objectName) {
    retrieveDdlsFromCache();
    for (Hib3GrouperDdl hib3GrouperDdl : GrouperUtil.nonNull(cachedDdls)) {
      if (StringUtils.equals(objectName, hib3GrouperDdl.getObjectName())) {
        return hib3GrouperDdl.getHistory() == null ? new StringBuilder() 
          : new StringBuilder(hib3GrouperDdl.getHistory());
      }
    }
    return new StringBuilder();
  }
  
  /**
   * retrieve a version of a ddl object versionable
   * @param objectName
   * @param version
   * @return the ddl versionable
   */
  public static DdlVersionable retieveVersion(String objectName, int version) {
    
    String enumName = "V" + version;
    Class<Enum> enumClass = retrieveDdlEnum(objectName);
    
    try {
      return (DdlVersionable)Enum.valueOf(enumClass, enumName);
    } catch (Exception e) {
      throw new RuntimeException("Cant find version " + version + "(" + enumName 
          + ")  in objectName: " + objectName + ", " + enumClass.getName(), e);
    }
  }
  
  /** cache the ddls */
  private static List<Hib3GrouperDdl> cachedDdls = null;

  /**
   * get the version of a ddl object in the DB
   * @param objectName
   * @return the version or -1 if not in the DB
   */
  public static int retrieveDdlJavaVersion(String objectName) {
    
    Class<Enum> objectEnum = retrieveDdlEnum(objectName);
    
    //call currentVersion with reflection
    Integer currentVersion = (Integer)GrouperUtil.callMethod(objectEnum, "currentVersion");
    
    return currentVersion;
    
  }
  
  /**
   * the relationship between object name and the enum is as follows.  If the object name
   * is "Grouper", then the enum is edu.internet2.middleware.grouper.ddl.GrouperDdl
   * @param objectName
   * @return the enum
   */
  @SuppressWarnings("unchecked")
  public static Class<Enum> retrieveDdlEnum(String objectName) {
    return GrouperUtil.forName("edu.internet2.middleware.grouper.ddl." + objectName + "Ddl");
  }
  
  /**
   * get the cached ddls from the db and update the hibernate version if they are there
   */
  private static void retrieveDdlsFromCache() {
    //lazy load the cached ddls
    if (cachedDdls == null) {
      try {
        cachedDdls = retrieveDdlsFromDb();
        
        for (Hib3GrouperDdl hib3GrouperDdl : GrouperUtil.nonNull(cachedDdls)) {
          
          String objectName = hib3GrouperDdl.getObjectName();
          
          int ddlJavaVersion = retrieveDdlJavaVersion(objectName);
          
          LOG.info("Current java version for ddl '" + objectName + "' is " + ddlJavaVersion);
        }
      } catch (Exception e) {
        //just log, maybe the table isnt there
        LOG.error("maybe the grouper_ddl table isnt there... if that is the reason its ok.", e);
      }
    
      cachedDdls = GrouperUtil.defaultIfNull(cachedDdls, new ArrayList<Hib3GrouperDdl>());
      
      //call hook so they can be removed or added
      GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.LIFECYCLE, 
          LifecycleHooks.METHOD_DDL_INIT, HooksLifecycleDdlInitBean.class, 
          (Object)cachedDdls, List.class, null);

      
    }
  }

  /**
   * 
   * @param ddlName
   * @return true if record exists
   */
  private static boolean containsDbRecord(String ddlName) {
    retrieveDdlsFromCache();
    for (Hib3GrouperDdl hib3GrouperDdl  : cachedDdls) {
      if (StringUtils.equals(hib3GrouperDdl.getObjectName(), ddlName)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * get the object names from the 
   * @return the list of object names
   */
  public static List<String> retrieveObjectNames() {
    //init stuff
    retrieveDdlsFromCache();
    
    List<String> objectNames = new ArrayList<String>();
    for (Hib3GrouperDdl hib3GrouperDdl : cachedDdls) {
      objectNames.add(hib3GrouperDdl.getObjectName());
    }
    
    //make sure Grouper is in there
    if (!objectNames.contains("Grouper")) {
      objectNames.add("Grouper");
    }
    if (!objectNames.contains("Subject")) {
      objectNames.add("Subject");
    }
    if (GrouperConfig.getPropertyBoolean("ddlutils.exclude.subject.tables", false)) {
      objectNames.remove("Subject");
    }
    
    
    return objectNames;
  }
  
  /**
   * get the version of a ddl object in the DB
   * @param objectName
   * @return the version or -1 if not in the DB
   */
  public static int retrieveDdlDbVersion(String objectName) {

    //init stuff
    retrieveDdlsFromCache();
    
    //find the ddl in the list
    Hib3GrouperDdl hib3GrouperDdl = Hib3GrouperDdl.findInList(cachedDdls, objectName);
    if (hib3GrouperDdl != null) {
      return hib3GrouperDdl.getDbVersion();
    }
    return 0;
    
  }
  
  /**
   * get all the ddls, put grouper at the front
   * @return the ddls
   */
  @SuppressWarnings("unchecked")
  public static List<Hib3GrouperDdl> retrieveDdlsFromDb() {

    List<Hib3GrouperDdl> grouperDdls = HibernateSession.byCriteriaStatic().list(Hib3GrouperDdl.class, null); 

    //move the grouper one to the front
    if (grouperDdls != null) {
      for (int i = 0; i < GrouperUtil.length(grouperDdls); i++) {
        Hib3GrouperDdl hib3GrouperDdl = grouperDdls.get(i);
        if (StringUtils.equals(hib3GrouperDdl.getObjectName(), "Grouper")) {
          grouperDdls.remove(i);
          grouperDdls.add(0, hib3GrouperDdl);
          break;
        }
      }
    }
    return grouperDdls;

  }

  /**
   * <pre>
   * File name must be objectName.V#.dbname.sql
   * e.g. Grouper.5.oracle10.sql
   * 
   * The dbname must be a valid ddlutils dbname:
   * axion, cloudscape, db2, db2v8, derby, firebird, hsqldb, interbase, maxdb, mckoi, 
   * mssql, mysql, mysql5, oracle, oracle10, oracle9, postgresql, sapdb, sybase, sybasease15
   *
   * Also the following catchalls are acceptable: oracleall, mysqlall, db2all, sybaseall
   * </pre>
   * @param dbObjectVersion e.g. Grouper or GrouperLoader
   * @param dbname e.g. oracle10 or mysql5
   * @return the script or blank if it is not found
   */
  public static String findScriptOverride(DdlVersionable dbObjectVersion, String dbname) {
    String objectName = dbObjectVersion.getObjectName();
    int version = dbObjectVersion.getVersion();
    //lets see if there is a specific one:
    String script = findScriptOverride(objectName, version, dbname);
    if (StringUtils.isBlank(script)) {
      //now see if there is a general one...
      String generalName = null;
      //this is not an exact science...  but here is the algorithm
      if (dbname.startsWith("oracle")) {
        generalName = "oracleall";
      } else if (dbname.startsWith("mysql")) {
        generalName = "mysqlall";
      } else if (dbname.startsWith("db2")) {
        generalName = "db2all";
      } else if (dbname.startsWith("sybase")) {
        generalName = "sybaseall";
      }
      if (StringUtils.isNotBlank(generalName)) {
        script = findScriptOverride(objectName, version, generalName);
      }
    }
    return script;
  }

  /**
   * <pre>
   * get an override file (exact, dont look for the all ones like oracleall)
   * File name must be objectName.V#.dbname.sql
   * e.g. Grouper.5.oracle10.sql
   * 
   * </pre>
   * @param objectName
   * @param version
   * @param dbNameExact
   * @return the script or null if none found
   */
  public static String findScriptOverride(String objectName, int version, String dbNameExact) {
    String resourceName = "/ddl/" + objectName + "." + version + "." + dbNameExact + ".sql";
    String script = null;
    script = GrouperUtil.readResourceIntoString(resourceName, true);
    return script;
  }
  
  /**
   * find a version from an enum version int
   * @param ddlVersion
   * @return the version
   */
  public static int versionIntFromEnum(Enum ddlVersion) {
    String name = ddlVersion.name();
    if (!name.startsWith("V")) {
      throw new RuntimeException("Version enums must start with V: " + name);
    }
    String version = name.substring(1);
    return GrouperUtil.intValue(version);

  }
  
  
  /**
   * find the object name from the db object version
   * @param dbObjectVersion
   * @return the object name
   */
  public static String objectName(Enum dbObjectVersion) {
    String className = dbObjectVersion.getDeclaringClass().getSimpleName();
    
    //now we have GrouperEnum, strip off the Enum part
    if (!className.endsWith("Ddl")) {
      throw new RuntimeException("Db object version classes MUST end in Ddl! '" + className + "'");
    }
    String objectName = className.substring(0, className.length()-3);
    return objectName;
  }
  
  /**
   * get a database object of a certain version based on the existing database, and tack on
   * all the enums up to the version we want (if any)
   * @param baseVersion
   * @param baseDatabaseVersion
   * @param objectName
   * @param requestedVersion
   */
  public static void upgradeDatabaseVersion(Database baseVersion, int baseDatabaseVersion, 
      String objectName, int requestedVersion) {
    if (baseDatabaseVersion == requestedVersion) {
      return;
    }
    //loop up to the version we need
    for (int version = baseDatabaseVersion+1; version<=requestedVersion; version++) {
      //get the enum
      DdlVersionable ddlVersionable = retieveVersion(objectName, version);
      //do an incremental update
      ddlVersionable.updateVersionFromPrevious(baseVersion);
    }
  }
  
  /**
   * find or create table
   * @param database
   * @param tableName
   * @param description currently not used, but eventually can be the data dictionary comment
   * @return the table
   */
  public static Table ddlutilsFindOrCreateTable(Database database, String tableName, String description) {
    Table table = database.findTable(tableName);
    if (table == null) {
      table = new Table();
      table.setName(tableName);
      table.setDescription(description);
      database.addTable(table);
    }
    return table;
  }

  /**
   * add an index on a table.  drop a misnamed or a misuniqued index which is existing
   * @param database
   * @param tableName
   * @param indexName 
   * @param unique
   * @param columnNames
   * @return the index which is the new one, or existing one if it already exists
   */
  public static Index ddlutilsFindOrCreateIndex(Database database, String tableName, String indexName, 
      boolean unique, String... columnNames) {
    Table table = GrouperDdlUtils.ddlutilsFindTable(database,tableName);

    //search for the index
    OUTERLOOP:
    for (Index existingIndex : table.getIndices()) {
      if (existingIndex.getColumnCount() == columnNames.length) {
        
        //no need to check if unique.  you dont want two of the same index, one unique, the other not
        //look through existing columns (order is important)
        //see if this is not a match
        for (int i=0;i<columnNames.length;i++) {
          if (!StringUtils.equalsIgnoreCase(existingIndex.getColumn(i).getName(), columnNames[i])) {
            continue OUTERLOOP;
          }
        }
        
        //if we made it this far, it is the same index!
        
        //if exactly the same, leave it be
        if (unique == existingIndex.isUnique() && StringUtils.equalsIgnoreCase(indexName, existingIndex.getName())) {
          return existingIndex;
        }
        
        table.removeIndex(existingIndex);
      }
    }
    
    Index index = unique ? new UniqueIndex() : new NonUniqueIndex();
    index.setName(indexName);

    for (String columnName : columnNames) {
      
      Column column = GrouperDdlUtils.ddlutilsFindColumn(table, columnName);
      IndexColumn nameColumn = new IndexColumn(column);
      index.addColumn(nameColumn);
      
    }
    
    table.addIndex(index);
    return index;
  }

  /**
   * add a foreign key on a table.  drop a misnamed foreign key which is existing
   * @param database
   * @param tableName
   * @param foreignKeyName 
   * @param foreignTableName 
   * @param localColumnName
   * @param foreignColumnName 
   * @return the foreign key which is the new one, or existing one if it already exists
   */
  public static ForeignKey ddlutilsFindOrCreateForeignKey(Database database, String tableName, String foreignKeyName, 
      String foreignTableName, String localColumnName, String foreignColumnName) {
    return ddlutilsFindOrCreateForeignKey(database, tableName, foreignKeyName, foreignTableName, 
        GrouperUtil.toList(localColumnName), GrouperUtil.toList(foreignColumnName));
  }

  /**
   * add a foreign key on a table.  drop a misnamed foreign key which is existing
   * @param database
   * @param tableName
   * @param foreignKeyName 
   * @param foreignTableName 
   * @param localColumnNames 
   * @param foreignColumnNames 
   * @return the foreign key which is the new one, or existing one if it already exists
   */
  public static ForeignKey ddlutilsFindOrCreateForeignKey(Database database, String tableName, String foreignKeyName, 
      String foreignTableName, List<String> localColumnNames, List<String> foreignColumnNames) {
    
    //validate inputs
    if (localColumnNames.size() != foreignColumnNames.size()) {
      throw new RuntimeException("Local col size must equal foreign col size: " 
          + localColumnNames.size() + " != " + foreignColumnNames.size());
    }
    
    Table table = GrouperDdlUtils.ddlutilsFindTable(database,tableName);
    Table foreignTable = GrouperDdlUtils.ddlutilsFindTable(database,foreignTableName);
    
    //search for the foreign key
    OUTERLOOP:
    for (ForeignKey foreignKey : table.getForeignKeys()) {
      if (foreignKey.getReferences().length == localColumnNames.size()) {
        for (int i=0;i<localColumnNames.size();i++) {

          Reference reference = foreignKey.getReferences()[i];
          
          //if this isnt a match
          if (!StringUtils.equalsIgnoreCase(reference.getForeignColumnName(), foreignColumnNames.get(i))
              || !StringUtils.equalsIgnoreCase(reference.getLocalColumnName(), localColumnNames.get(i))) {
            continue OUTERLOOP;
          }
        }
        
        //if we made it this far, it is the same foreign key!
        
        //if exactly the same, leave it be
        if (StringUtils.equalsIgnoreCase(foreignKeyName, foreignKey.getName())) {
          return foreignKey;
        }
        
        table.removeForeignKey(foreignKey);
      }
    }
    
    ForeignKey foreignKey = new ForeignKey(foreignKeyName);
    foreignKey.setForeignTableName(foreignTableName);
    
    for (int i=0;i<localColumnNames.size();i++) {
      
      Column localColumn = GrouperDdlUtils.ddlutilsFindColumn(table, localColumnNames.get(i));
      Column foreignColumn = GrouperDdlUtils.ddlutilsFindColumn(foreignTable, foreignColumnNames.get(i));
      
      Reference reference = new Reference(localColumn, foreignColumn);
      
      foreignKey.addReference(reference);
    }
    
    table.addForeignKey(foreignKey);
    return foreignKey;
  }
  
  /**
   * find table, if not exist, throw exception
   * @param database
   * @param tableName
   * @return the table
   */
  public static Table ddlutilsFindTable(Database database, String tableName) {
    Table table = database.findTable(tableName);
    if (table == null) {
      throw new RuntimeException("Cant find table: '" + tableName 
          + "', perhaps you need to rollback your ddl version in the DB and sync up");
    }
    return table;
  }
  
  /**
   * find column, if not exist, throw exception
   * @param table table to get column from
   * @param columnName column name of column (case insensitive)
   * @return the column
   */
  public static Column ddlutilsFindColumn(Table table, String columnName) {
    Column[] columns = table.getColumns();
    
    for (Column column : GrouperUtil.nonNull(columns)) {
      
      if (StringUtils.equalsIgnoreCase(columnName, column.getName())) {
        return column;
      }
    }
    
    throw new RuntimeException("Cant find table: '" + table.getName() 
        + "' columns: '" + columnName + "', perhaps you need to rollback your ddl version in the DB and sync up");
  }
  
  /**
   * find or create column with various properties
   * @param table 
   * @param columnName 
   * @param description not used, but eventually can be used for data dictionary
   * @param typeCode from java.sql.Types
   * @param size string, can be a simple int, or comma separated, see ddlutils docs
   * @param primaryKey this should only be true for new tables
   * @param required this should probably only be true for new tables (since ddlutils will copy to temp table)
   * @return the column
   */
  public static Column ddlutilsFindOrCreateColumn(Table table, String columnName, String description, 
      int typeCode, String size, boolean primaryKey, boolean required) {

    Column column = table.findColumn(columnName);
    
    if (column == null) {
      column = new Column();
      column.setName(columnName);
      //just add to end of columns
      table.addColumn(column);
    }

    column.setPrimaryKey(primaryKey);
    column.setRequired(required);
    column.setDescription(description);
    column.setTypeCode(typeCode);
    column.setSize(size);
    return column;
  }
  
}