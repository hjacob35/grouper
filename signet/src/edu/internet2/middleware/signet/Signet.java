/*--
$Id: Signet.java,v 1.36 2005-09-13 17:16:07 acohen Exp $
$Date: 2005-09-13 17:16:07 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.collections.set.UnmodifiableSet;

import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNotFoundException;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.signet.tree.TreeAdapter;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.Source;

import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
import edu.internet2.middleware.subject.provider.SourceManager;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

/**
* This is the factory class for all Signet entities.
*  
*/
public final class Signet
{
 /**
  * This constant denotes the default subject-type ID, as it is
  * defined and used by Signet.
  */
 public static final String DEFAULT_SUBJECT_TYPE_ID
  = "person";

 // This constant should probably end up in some sort of
 // Tree-specific presentation class adapter, and should probably
 // be private when it moves there.
 static final String           SCOPE_PART_DELIMITER
  = ":";

 private static final String   DEFAULT_TREE_ADAPTER_NAME
  = "edu.internet2.middleware.signet.TreeAdapterImpl";

 private static final String    DEFAULT_CHOICE_SET_ADAPTER_NAME
  = "edu.internet2.middleware.signet.ChoiceSetAdapterImpl";

 /**
  * This constant denotes the "first name" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
 public static final String    ATTR_FIRSTNAME                 = "~firstname";

 /**
  * This constant denotes the "middle name" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
 public static final String    ATTR_MIDDLENAME                = "~middlename";

 /**
  * This constant denotes the "last name" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
 public static final String    ATTR_LASTNAME                  = "~lastname";

 /**
  * This constant denotes the "display ID" attribute of a Subject, as it is
  * defined and used by Signet.
  * <p />
  * Perhaps this constant should be moved to the PrivilegedSubject interface,
  * or even hidden within the implementation of that interface.
  */
 public static final String    ATTR_DISPLAYID                 = "~displayid";

 // This is the metadata that describes the pre-defined Signet
 // super-subject.
 
 private static final String   SUPERSUBJECT_ID                = "SignetSuperSubject";

 private static SessionFactory sessionFactory;
 
 private static HousekeepingInterceptor interceptor;

 private Session               session;

 private Transaction           tx;

 private PrivilegedSubject     superPSubject;

 private int                   xactNestingLevel               = 0;

 // If I could, I would, by default, set Signet's log to be the same as
 // whatever log was configured for Signet's underlying Hibernate instance.
 // Unfortunately, I can't find a way to get Hibernate to cough up its
 // Log instance. I could conceivably read Hibernate's config file to
 // determine its logging configuration, but I don't want to add that
 // dependency.
 //
 // Likewise, if I could, I would re-set Hibernate's log to be the same as
 // Signet's log whenever Signet.setLog() was called. Unfortunately, I
 // can't find a way to change Hibernate's Log instance at runtime.
 //
 // So, what to do, what to do?
 //
 // By default, Signet will log to stdout. If Signet.setLog() is called,
 // Signet will use that supplied Log instance. This arrangement has 3
 // benefits:
 //
 //   1) This allows simple, naive, command-line-oriented Signet test
 //      programs and other applications to see logging output without
 //      doing anything special, beyond the configuration setup that
 //      Hibernate requires.
 //
 //   2) This allows more sophisticated Signet applications to set up
 //      whatever logging they want.
 //
 //   3) This avoids (so far, at least) the necessity for a Signet-specific
 //      configuration file, by fobbing that responsibility off onto the
 //      enclosing application.

 private Logger                logger;

 private SourceManager sourceManager;
 
 /**
  * This constructor builds the fundamental Signet factory object. It opens a
  * Hibernate session, and stores some Signet-specific metadata in that
  * database if it is not already present.
  *  
  */
 public Signet()
 {
   super();

   try
   {
     session = sessionFactory.openSession();
     interceptor.setConnection(session.connection());
   }
   catch (HibernateException he)
   {
     throw new SignetRuntimeException(he);
   }

   this.logger = Logger.getLogger(this.toString());

   try {
    this.sourceManager = SourceManager.getInstance();
   }
   catch (Exception ex) {
    throw new RuntimeException("Error getting SourceManager", ex);
   }
   
 }

 /**
  * Creates a new Category.
  * 
  * @param subsystem
  *          The {@link Subsystem}which contains this {@link Category}.
  * @param id
  *          A short mnemonic code which will appear in XML documents and other
  *          documents used by analysts.
  * @param name
  *          A descriptive name which will appear in UIs and documents exposed
  *          to users.
  * @param status
  *          The {@link Status}that should be initially assigned to this
  *          {@link Category}.
  */
 public final Category newCategory(Subsystem subsystem, String id,
     String name, Status status)
 {
   Category category = new CategoryImpl((SubsystemImpl) subsystem, id, name,
       status);
   subsystem.add(category);

   return category;
 }

 /**
  * Sets the Log associated with this Signet instance.
  * 
  * @param log
  */
 public final void setLogger(Logger logger)
 {
   this.logger = logger;
 }

 /**
  * Gets the Log associated with this Signet instance.
  * 
  * @return the Log.
  */
 public final Logger getLogger()
 {
   return this.logger;
 }

 /**
  * Creates a new Function.
  * 
  * @param category
  *          The {@link Category}which contains this {@link Function}.
  * @param id
  *          A short mnemonic code which will appear in XML documents and other
  *          documents used by analysts.
  * @param name
  *          A descriptive name which will appear in UIs and documents exposed
  *          to users.
  * @param status
  *          The {@link Status}that should be initially assigned to this
  *          {@link Category}.
  * @param helpText
  *          A prose description which will appear in help-text and other
  *          explanatory materials.
  * @param permissions
  *          The {@link Permission}s which should be associated with this
  *          {@link Function}.
  */
 public Function newFunction(Category category, String id, String name,
     Status status, String helpText)
 {
   Function newFunction = new FunctionImpl(this, category, id, name, helpText,
       status);

   ((SubsystemImpl) (category.getSubsystem())).add(newFunction);

   ((CategoryImpl) category).add(newFunction);

   return newFunction;
 }

 /**
  * Creates a new Subsystem.
  * 
  * @param code
  *          A short mnemonic code which will appear in XML documents and other
  *          documents used by analysts.
  * @param name
  *          A descriptive name which will appear in UIs and documents exposed
  *          to users.
  * @param helpText
  *          A prose description which will appear in help-text and other
  *          explanatory materials.
  * @param status
  *          The {@link Status}that should be initially assigned to this
  *          {@link Subsystem}.
  */

 public final Subsystem newSubsystem(String id, String name, String helpText,
     Status status)
 {
   return new SubsystemImpl(this, id, name, helpText, status);
 }

 /**
  *  
  */
 static
 /* runs at class load time */
 {
   Configuration cfg = new Configuration();

   try
   {
     // Read the "hibernate.cfg.xml" file. It is expected to be in a root
     // directory of the classpath.
     cfg.configure();
     String dbAccount = cfg.getProperty("hibernate.connection.username");
     interceptor = new HousekeepingInterceptor(dbAccount);
     cfg.setInterceptor(interceptor);

     sessionFactory = cfg.buildSessionFactory();
     interceptor.setSessionFactory(sessionFactory);
   }
   catch (HibernateException he)
   {
     throw new SignetRuntimeException(he);
   }

 }
 

 /**
  * Begins a Signet transaction.
  *  
  */
 public final void beginTransaction()
 {
   if (xactNestingLevel == 0)
   {
     try
     {
       tx = session.beginTransaction();
     }
     catch (HibernateException e)
     {
       throw new SignetRuntimeException(e);
     }
   }

   xactNestingLevel++;
 }

 /**
  * Saves a new Signet object, and any Signet objects it refers to.
  * 
  * @param o
  */
 final void save(Object o)
 {
   try
   {
     session.save(o);
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }
 }

 /**
  * Creates a new Tree, using the default Signet TreeAdapter.
  * 
  * @param treeId
  * @param treeName
  * @return the new Tree
  */
 public final Tree newTree(String treeId, String treeName)
 {
   TreeAdapter defaultTreeAdapter
    = getTreeAdapter(DEFAULT_TREE_ADAPTER_NAME);
   Tree newTree
    = new TreeImpl(this, defaultTreeAdapter, treeId, treeName);
   return newTree;
 }

 /**
  * Creates a new Tree.
  * 
  * @param adapter
  * @param treeId
  * @param treeName
  * @return the new Tree
  */
 public final Tree newTree(TreeAdapter adapter, String treeId,
     String treeName)
 {
   Tree newTree = new TreeImpl(this, adapter, treeId, treeName);
   return newTree;
 }

 /**
  * Creates a new TreeNode.
  * 
  * @param tree
  * @param id
  * @param name
  * @return
  */
 public final TreeNode newTreeNode(Tree tree, String id, String name)
 {
   TreeNode newTreeNode = tree.getAdapter().newTreeNode(tree, id, name);

   return newTreeNode;
 }

 /**
  * Gets a single Tree by ID.
  * 
  * @param id
  * @return
  * @throws ObjectNotFoundException
  */
 public final Tree getTree(String id)
 throws ObjectNotFoundException
 {
   try
   {
     TreeImpl tree = (TreeImpl)(session.load(TreeImpl.class, id));
     tree.setSignet(this);
     return tree;
   }
   catch (net.sf.hibernate.ObjectNotFoundException onfe)
   {
     throw new
      edu.internet2.middleware.signet.ObjectNotFoundException(onfe);
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }
 }

 /**
  * Gets a single ChoiceSet by ID.
  * 
  * @param id
  * @return
  * @throws ObjectNotFoundException
  */
 public final ChoiceSet getChoiceSet(String id)
 throws ObjectNotFoundException
 {
   try
   {
     return (ChoiceSet) (session.load(ChoiceSetImpl.class, id));
   }
   catch (net.sf.hibernate.ObjectNotFoundException onfe)
   {
     throw new
      edu.internet2.middleware.signet.ObjectNotFoundException(onfe);
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }
 }

 /**
  * Gets all of the Subsystems in the Signet database.
  * 
  * @return an unmodifiable Set of all of the {@link Subsystem}s in the Signet
  *         database. Never returns null: in the case of zero {@link Subsystem}
  *         s, this method will return an empty Set.
  */
 public Set getSubsystems()
 {
   List resultList;
   Set resultSet = new HashSet();

   try
   {
     resultList = session
         .find("from edu.internet2.middleware.signet.SubsystemImpl as subsystem");
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   resultSet.addAll(resultList);
   setSignetForAll(resultSet);
   return UnmodifiableSet.decorate(resultSet);
 }

 private void setSignetForAll(Collection collection)
 {
   Iterator collectionIterator = collection.iterator();
   while (collectionIterator.hasNext())
   {
     EntityImpl entityImpl = (EntityImpl) (collectionIterator.next());
     entityImpl.setSignet(this);
   }
 }

 /**
  * Gets all of the Trees in the Signet database. Should probably be changed to
  * return a type-safe Collection.
  * 
  * @return an array of all of the {@link Tree}s in the Signet database. Never
  *         returns null: in the case of zero {@link Tree}s, this method will
  *         return a zero-length array.
  */
 public Tree[] getTrees()
 {
   List resultList;

   try
   {
     resultList = session
         .find("from edu.internet2.middleware.signet.TreeImpl as tree");
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Object[] objectArray = resultList.toArray();

   TreeImpl[] treeImplArray = new TreeImpl[objectArray.length];

   for (int i = 0; i < objectArray.length; i++)
   {
     treeImplArray[i] = (TreeImpl) (objectArray[i]);
   }

   return treeImplArray;
 }


 /**
  * Gets all PrivilegedSubjects. Should probably be changed to return a
  * type-safe Collection.
  * 
  * @return a List of all of the {@link PrivilegedSubjects}s accessible to
  *         Signet, including those who have no privileges. Never returns null:
  *         in the case of zero {@link PrivilegedSubject}s, this method will
  *         return an empty List.
  */
 public List getPrivilegedSubjects()
 {
   try
   {
    List privilegedSubjects =
      session.find("from edu.internet2.middleware.signet.PrivilegedSubject");
    return privilegedSubjects;
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(
         "Error while attempting to retrieve all PrivilegedSubject from"
             + " the database", e);
   }

 }


 // I really want to do away with this method, having the PrivilegedSubject
 // pick up its granted Assignments via Hibernate object-mapping. I just
 // haven't figured out how to do that yet. This method will be greatly
 // simplified when we re-introduce the PrivilegedSubject table, which will
 // use a simple synthetic key.
 //
 // I do, however, like this notion of returning an UnmodifiableSet instead of
 // an Array.
 Set getAssignmentsByGrantor(SubjectKey grantor)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.AssignmentImpl"
             + " as assignment" + " where grantorID = :id"
             + " and grantorTypeID = :type");

     query.setString("id", grantor.getSubjectId());
     query.setString("type", grantor.getSubjectTypeId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     Assignment assignment = (Assignment) (resultSetIterator.next());
     ((AssignmentImpl) assignment).setSignet(this);
   }

   return resultSet;
 }


 // This method will be greatly simplified when we re-introduce the
 // PrivilegedSubject table, which will use a simple synthetic key.
 Set getProxiesByGrantor(SubjectKey grantor)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.ProxyImpl"
             + " as proxy" + " where grantorID = :id"
             + " and grantorTypeID = :type");

     query.setString("id", grantor.getSubjectId());
     query.setString("type", grantor.getSubjectTypeId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     Proxy proxy = (Proxy) (resultSetIterator.next());
     ((ProxyImpl) proxy).setSignet(this);
   }

   return resultSet;
 }


 // This method will be greatly simplified when we re-introduce the
 // PrivilegedSubject table, which will use a simple synthetic key.
 Set getProxiesByGrantee(SubjectKey grantee)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.ProxyImpl"
             + " as proxy" + " where granteeID = :id"
             + " and granteeTypeID = :type");

     query.setString("id", grantee.getSubjectId());
     query.setString("type", grantee.getSubjectTypeId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     Proxy proxy = (Proxy) (resultSetIterator.next());
     ((ProxyImpl) proxy).setSignet(this);
   }

   return resultSet;
 }
 
 
 Set findDuplicates(Assignment assignment)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery
           ("from edu.internet2.middleware.signet.AssignmentImpl"
            + " as assignment"
            + " where granteeID = :granteeId"
            + " and granteeTypeID = :granteeTypeId"
            + " and functionID = :functionId"
            + " and subsystemID = :subsystemId"
            + " and scopeID = :scopeId"
            + " and scopeNodeID = :scopeNodeId"
            + " and assignmentID != :assignmentId");

     query.setString
       ("granteeId", assignment.getGrantee().getSubjectId());
     query.setString
       ("granteeTypeId", assignment.getGrantee().getSubjectTypeId());
     query.setString
       ("functionId", assignment.getFunction().getId());
     query.setString
       ("subsystemId", assignment.getFunction().getSubsystem().getId());
     query.setString
       ("scopeId", assignment.getScope().getTree().getId());
     query.setString
       ("scopeNodeId", assignment.getScope().getId());
     query.setInteger
       ("assignmentId",
        (assignment.getId() == null ? -1 : assignment.getId().intValue()));

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     Assignment matchedAssignment = (Assignment) (resultSetIterator.next());
     ((AssignmentImpl) matchedAssignment).setSignet(this);
     
     // Now, let's trim this set of Assignments down further, keeping only
     // those Assignments whose LimitValues actually match.
     
     if (!(assignment.getLimitValues()
           .equals(matchedAssignment.getLimitValues())))
     {
       resultSet.remove(matchedAssignment);
     }
   }

   return resultSet;
 }
 
 
  Set findDuplicates(Proxy proxy)
  {
    Query query;
    List resultList;

    try
    {
      query
        = session.createQuery
            ("from edu.internet2.middleware.signet.ProxyImpl"
             + " as proxy"
             + " where granteeID = :granteeId"
             + " and granteeTypeID = :granteeTypeId"
             + " and subsystemID = :subsystemId"
             + " and proxyID != :proxyId");

      query.setString
        ("granteeId", proxy.getGrantee().getSubjectId());
      query.setString
        ("granteeTypeId", proxy.getGrantee().getSubjectTypeId());
      query.setString
        ("subsystemId", proxy.getSubsystem().getId());
     
      query.setParameter("proxyId", proxy.getId(), Hibernate.INTEGER);

      resultList = query.list();
    }
    catch (HibernateException e)
    {
      throw new SignetRuntimeException(e);
    }

    Set resultSet = new HashSet(resultList);

    Iterator resultSetIterator = resultSet.iterator();
    while (resultSetIterator.hasNext())
    {
      Proxy matchedProxy = (Proxy) (resultSetIterator.next());
      ((ProxyImpl) matchedProxy).setSignet(this);
    }

    return resultSet;
  }

 // I really want to do away with this method, having the
 // Tree pick up its parent-child relationships via Hibernate
 // object-mapping. I just haven't figured out how to do that yet.
 Set getParents(TreeNode childNode)
 {
   Query query;
   List resultList;
   Tree tree = childNode.getTree();

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.TreeNodeRelationship"
             + " as treeNodeRelationship"
             + " where treeID = :treeId"
             + " and nodeID = :childNodeId");

     query.setString("treeId", tree.getId());
     query.setString("childNodeId", childNode.getId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);
   Set parents = new HashSet();

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     TreeNodeRelationship tnr = (TreeNodeRelationship) (resultSetIterator
         .next());
     parents.add(tree.getNode(tnr.getParentNodeId()));
   }

   return parents;
 }

 // I really want to do away with this method, having the
 // Tree pick up its parent-child relationships via Hibernate
 // object-mapping. I just haven't figured out how to do that yet.
 Set getChildren(TreeNode parentNode)
 {
   Query query;
   List resultList;
   Tree tree = parentNode.getTree();

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.TreeNodeRelationship"
             + " as treeNodeRelationship"
             + " where treeID = :treeId"
             + " and parentNodeID = :parentNodeId");

     query.setString("treeId", tree.getId());
     query.setString("parentNodeId", parentNode.getId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);
   Set children = new HashSet();

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     TreeNodeRelationship tnr = (TreeNodeRelationship) (resultSetIterator
         .next());
     children.add(tree.getNode(tnr.getChildNodeId()));
   }

   return children;
 }

 // I really want to do away with this method, having the PrivilegedSubject
 // pick up its granted Assignments via Hibernate object-mapping. I just
 // haven't figured out how to do that yet.
 //
 // I do, however, like this notion of returning an UnmodifiableSet instead of
 // an Array.
 Set getAssignmentsByGrantee(SubjectKey grantee)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.AssignmentImpl"
             + " as assignment" + " where granteeID = :id"
             + " and granteeTypeID = :type");

     query.setString("id", grantee.getSubjectId());
     query.setString("type", grantee.getSubjectTypeId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);
   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     AssignmentImpl assignment = (AssignmentImpl) (resultSetIterator.next());
     assignment.setSignet(this);
   }

   return resultSet;
 }

 // I really want to do away with this method, having the Subsystem
 // pick up its associated Functions via Hibernate object-mapping. I just
 // haven't figured out how to do that yet.
 Set getFunctionsBySubsystem(Subsystem subsystem)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery("from edu.internet2.middleware.signet.FunctionImpl"
             + " as function" + " where subsystemID = :id");

     query.setString("id", subsystem.getId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set resultSet = new HashSet(resultList);

   Iterator resultSetIterator = resultSet.iterator();
   while (resultSetIterator.hasNext())
   {
     Function function = (Function) (resultSetIterator.next());
     ((FunctionImpl) function).setSignet(this);
   }

   return resultSet;
 }

 // I really want to do away with this method, having the Subsystem
 // pick up its associated ChoiceSets via Hibernate object-mapping.
 // I just haven't figured out how to do that yet.
 Set getChoiceSetsBySubsystem(Subsystem subsystem)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery
          ("from edu.internet2.middleware.signet.ChoiceSetImpl"
            + " as choiceSet where subsystemID = :id");

     query.setString("id", subsystem.getId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Set choiceSets = new HashSet(resultList.size());

   Iterator resultListIterator = resultList.iterator();
   while (resultListIterator.hasNext())
   {
     ChoiceSet choiceSet = (ChoiceSet)(resultListIterator.next());
     ((ChoiceSetImpl)choiceSet).setSignet(this);
     choiceSets.add(choiceSet);
   }

   return choiceSets;
 }

 // I really want to do away with this method, having the Subsystem
 // pick up its associated Limits via Hibernate object-mapping.
 // I just haven't figured out how to do that yet.
 Map getLimitsBySubsystem(Subsystem subsystem)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery
          ("from edu.internet2.middleware.signet.LimitImpl"
            + " as limit where subsystemID = :id");

     query.setString("id", subsystem.getId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Map limits = new HashMap(resultList.size());

   Iterator resultListIterator = resultList.iterator();
   while (resultListIterator.hasNext())
   {
     Limit limit = (Limit)(resultListIterator.next());
     ((LimitImpl)limit).setSignet(this);
     limits.put(limit.getId(), limit);
   }

   return limits;
 }

 // I really want to do away with this method, having the Subsystem
 // pick up its associated Permissions via Hibernate object-mapping.
 // I just haven't figured out how to do that yet.
 Map getPermissionsBySubsystem(Subsystem subsystem)
 {
   Query query;
   List resultList;

   try
   {
     query = session
         .createQuery
          ("from edu.internet2.middleware.signet.PermissionImpl"
            + " as limit where subsystemID = :id");

     query.setString("id", subsystem.getId());

     resultList = query.list();
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Map permissions = new HashMap(resultList.size());

   Iterator resultListIterator = resultList.iterator();
   while (resultListIterator.hasNext())
   {
     Permission permission = (Permission)(resultListIterator.next());
     ((PermissionImpl)permission).setSignet(this);
     permissions.put(permission.getId(), permission);
   }

   return permissions;
 }

// // I really want to do away with this method, having the Function
// // pick up its associated Permissions via Hibernate object-mapping.
// // I just haven't figured out how to do that yet.
// Set getPermissionsByFunction(Function function)
// {
//   Query query;
//   List resultList;
//
//   try
//   {
//     query = session
//         .createQuery
//          ("from edu.internet2.middleware.signet.PermissionImpl"
//            + " as limit"
//            + " where edu.internet2.middleware.signet.Permission_Limit.functionID = :functionID"
//            + " and edu.internet2.middleware.signet.Permission_Limit.subsystemID = :subsystemID"
//            + " and edu.internet2.middleware.signet.Permission_Limit.permissionID = permissionID"
//            + " and edu.internet2.middleware.signet.Permission_Limit.subsystemID = subsystemID");
//
//     try
//     {
//       query.setString("functionID", function.getId());
//       query.setString("subsystemID", function.getSubsystem().getId());
//     }
//     catch (ObjectNotFoundException onfe)
//     {
//       throw new SignetRuntimeException(onfe);
//     }
//
//     resultList = query.list();
//   }
//   catch (HibernateException e)
//   {
//     throw new SignetRuntimeException(e);
//   }
//
//   Set permissions = new HashSet(resultList.size());
//
//   Iterator resultListIterator = resultList.iterator();
//   while (resultListIterator.hasNext())
//   {
//     Permission permission = (Permission)(resultListIterator.next());
//     ((PermissionImpl)permission).setSignet(this);
//     permissions.add(permission);
//   }
//
//   return permissions;
// }

 /**
  * Gets all Assignments in the Signet database. Should probably be changed to
  * return a type-safe Collection.
  * 
  * @return an array of all of the {@link Assignment}s in the Signet database.
  *         Never returns null: in the case of zero {@link Assignment}s, this
  *         method will return a zero-length array.
  */
 public Assignment[] getAssignments()
 {
   List resultList;

   try
   {
     resultList = session
         .find("from edu.internet2.middleware.signet.AssignmentImpl"
             + " as assignment");
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }

   Iterator assignmentsIterator = resultList.iterator();
   while (assignmentsIterator.hasNext())
   {
     AssignmentImpl assignmentImpl = (AssignmentImpl) (assignmentsIterator
         .next());
     assignmentImpl.setSignet(this);
   }

   AssignmentImpl[] assignmentImplArray = new AssignmentImpl[resultList.size()];

   collection2array(resultList, assignmentImplArray);

   return assignmentImplArray;
 }


 /**
  * Creates a new Subsystem.
  * 
  * @return
  */
 public Subsystem newSubsystem(String id, String name, String helpText)
 {
   return new SubsystemImpl(this, id, name, helpText, Status.PENDING);
 }

 /**
  * commit a Signet database transaction.
  */
 public void commit()
 {
   if (tx == null)
   {
     throw new IllegalStateException(
         "It is illegal to attempt to commit a Signet transaction that has"
             + " not yet begun.");
   }

   if (xactNestingLevel < 1)
   {
     throw new SignetRuntimeException(
         "A Signet transaction is open, but Signet's transaction-nesting"
             + " level is not greater than zero. This is an internal error.");
   }

   xactNestingLevel--;

   if (xactNestingLevel == 0)
   {
     try
     {
       tx.commit();
       tx = null;
     }
     catch (HibernateException e)
     {
       throw new SignetRuntimeException(e);
     }
   }
 }

 /**
  * Closes a Signet session.
  */
 public void close()
 {
   try
   {
     session.close();
     session = null;

     // We leave the SessionFactory open, in case the application
     // wants to create a subsequent Signet object.
   }
   catch (HibernateException e)
   {
     throw new SignetRuntimeException(e);
   }
 }

 /**
  * Creates a new Permission.
  * 
  * @param subsystem the Subsystem which will contain the new Permission.
  * @param id the ID of the new Permission.
  * @param status the Status of the new Permission.
  * @return
  */
 public Permission newPermission
  (Subsystem subsystem, String id, Status status)
 {
   Permission newPermission
    = new PermissionImpl
        ((SubsystemImpl) subsystem, id, status);
   
   ((SubsystemImpl)subsystem).add(newPermission);

   return newPermission;
 }

 /**
  * @deprecated This function is no longer needed, and should never have been
  *             public in the first place.
  * 
  * @param set
  * @param parentsArray
  */
 public Object[] collection2array(Collection srcCollection, Object[] destArray)
 {
   Iterator srcIterator = srcCollection.iterator();
   int i = 0;
   while (srcIterator.hasNext())
   {
     destArray[i] = srcIterator.next();
     i++;
   }

   return destArray;
 }

 /**
  * Gets a single PrivilegedSubject by its underlying Subject.
  * 
  * @param subject
  * @return
  * @throws ObjectNotFoundException
  */
 public PrivilegedSubject getPrivilegedSubject(Subject subject)
 {
   return new PrivilegedSubjectImpl(this, subject);
 }


 /**
  * Gets a single PrivilegedSubject by type and ID.
  * 
  * @param subjectTypeId
  * @param subjectId
  * @return
  * @throws ObjectNotFoundException
  */
 public PrivilegedSubject getPrivilegedSubject(String subjectTypeId,
     String subjectId) throws ObjectNotFoundException
 {
   Subject subject = getSubject(subjectTypeId, subjectId);
   PrivilegedSubject pSubject = getPrivilegedSubject(subject);

   return pSubject;
 }


 /**
  * Gets a single PrivilegedSubject by its type and displayID.
  * 
  * @param subjectTypeId
  * @param displayId
  * @return
  * @throws ObjectNotFoundException
  *           if the PrivilegedSubject is not found.
  */
 public PrivilegedSubject getPrivilegedSubjectByDisplayId(
     String subjectTypeId, String displayId) throws ObjectNotFoundException
 {
  Subject subject = getSubjectByDisplayId(subjectTypeId, displayId);
  if (subject == null) {
    throw new ObjectNotFoundException("Unable to get PrivilegedSubject by display ID.");
  }
  return getPrivilegedSubject(subject);
 }


  /**
   * Gets PrivilegedSubjects by type and display ID.
   * 
   * @param subjectTypeId
   * @param displayId
   * @return Set of PrivilegedSubjects
   */
  public Set getPrivilegedSubjectsByDisplayId
    (String subjectTypeId,
     String displayId)
  {
    Set pSubjects = new HashSet();
    for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext();)
    {
      try
      {
    	  Subject result
          = ((Source)iter.next()).getSubjectByIdentifier(displayId);
    	  PrivilegedSubject pSubject = getPrivilegedSubject(result);
    	  pSubjects.add(pSubject);
      }
      catch (SubjectNotFoundException snfe)
      {
      }
    }
    return UnmodifiableSet.decorate(pSubjects);
 }


 /**
  * This method is only for use by the native Signet TreeAdapter.
  * 
  * @return null if the Tree is not found.
  * @throws SignetRuntimeException
  *           if more than one Tree is found.
  */
 Tree getNativeSignetTree(String id) throws ObjectNotFoundException
 {
   TreeImpl treeImpl;

   try
   {
     treeImpl = (TreeImpl) (session.load(TreeImpl.class, id));
   }
   catch (net.sf.hibernate.ObjectNotFoundException onfe)
   {
     throw new edu.internet2.middleware.signet.ObjectNotFoundException(onfe);
   }
   catch (HibernateException he)
   {
     throw new SignetRuntimeException(he);
   }

   treeImpl.setAdapter(new TreeAdapterImpl(this));
   treeImpl.setSignet(this);

   return treeImpl;
 }


 /**
  * Gets a single TreeNode by treeID and nodeID, using the default Signet
  * TreeAdapter.
  * 
  * @param treeId
  * @param treeNodeId
  * @return the specified TreeNode
  * @throws ObjectNotFoundException
  */

 public TreeNode getTreeNode(String treeId, String treeNodeId)
     throws ObjectNotFoundException
 {
   TreeAdapter adapter = this
       .getTreeAdapter(DEFAULT_TREE_ADAPTER_NAME);
   return getTreeNode(adapter, treeId, treeNodeId);
 }

 /**
  * Gets a single TreeNode by adapter, treeID and nodeID.
  * 
  * @param adapter
  * @param treeId
  * @param treeNodeId
  * @return the specified TreeNode
  * @throws ObjectNotFoundException
  */
 public TreeNode getTreeNode(TreeAdapter adapter, String treeId,
     String treeNodeId) throws ObjectNotFoundException
 {
   Tree tree = null;

   try
   {
     tree = adapter.getTree(treeId);
   }
   catch (TreeNotFoundException tnfe)
   {
     throw new ObjectNotFoundException(tnfe);
   }

   TreeNode treeNode = tree.getNode(treeNodeId);

   return treeNode;
 }

 /**
  * Gets a single TreeNode identified by a scope-string. The format of that
  * scopeString is currently subject to change. and will be documented after it
  * is finalized.
  * 
  * @param scopeString
  * @return
  * @throws ObjectNotFoundException
  */
 public TreeNode getTreeNode(String scopeString)
     throws ObjectNotFoundException
 {
   int firstDelimIndex = scopeString.indexOf(SCOPE_PART_DELIMITER);
   int secondDelimIndex = scopeString.indexOf(SCOPE_PART_DELIMITER,
       firstDelimIndex + SCOPE_PART_DELIMITER.length());

   String treeAdapterName = scopeString.substring(0, firstDelimIndex);
   String treeId = scopeString.substring(firstDelimIndex
       + SCOPE_PART_DELIMITER.length(), secondDelimIndex);
   String treeNodeId = (scopeString.substring(secondDelimIndex
       + SCOPE_PART_DELIMITER.length()));

   TreeAdapter adapter = getTreeAdapter(treeAdapterName);
   return getTreeNode(adapter, treeId, treeNodeId);
 }

 /**
  * This method loads the named ChoiceSetAdapter class, instantiates
  * it using its parameterless constructor, and passes back the new
  * instance.
  * 
  * @param adapterName The fully-qualified class-name of the
  *     ChoiceSetAdapter.
  * @return the new ChoiceSetAdapter.
  */
 public ChoiceSetAdapter getChoiceSetAdapter(String adapterName)
 {
   ChoiceSetAdapter adapter
    = (ChoiceSetAdapter)
        (loadAndCheckAdapter
            (adapterName, ChoiceSetAdapter.class, "ChoiceSet"));
  
   if (adapter instanceof ChoiceSetAdapterImpl)
   {
     ((ChoiceSetAdapterImpl) (adapter)).setSignet(this);
   }
   
   return adapter;
 }
 
 /**
  * @param className
  * @param requiredInterface
  * @param adapterTargetName e,g. "Tree" or "Limit"
  * @return
  */
 private Object loadAndCheckAdapter
  (String className,
   Class  requiredInterface,
   String adapterTargetName)
 {
   Object adapter;
   Class  actualClass = null;

   try
   {
     actualClass = Class.forName(className);
   }
   catch (ClassNotFoundException cnfe)
   {
     throw new SignetRuntimeException
        ("A "
         + adapterTargetName
         + " referenced by Signet relies upon an adapter which"
         + " is implemented by the class named '"
         + className
         + "'. This class cannot be found in Signet's classpath.",
         cnfe);
   }
   
   if (!classImplementsInterface(actualClass, requiredInterface))
   {
     throw new SignetRuntimeException
      ("A "
        + adapterTargetName
        + " referenced by Signet relies upon an adapter which"
        + " is implemented by the class named '"
        + className
        + "'. This class is in Signet's classpath, but it does not"
        + " implement the required interface '"
        + requiredInterface.getName()
        + "'.");
   }
   
   try
   {
     adapter = actualClass.newInstance();
   }
   catch (Exception e)
   {
     throw new SignetRuntimeException
         ("A "
          + adapterTargetName
          + " in the Signet database relies upon an adapter which"
          + " is implemented by the class named '"
          + className
          + "'. This class is in Signet's classpath, but Signet did not"
          + " succeed in invoking its default constructor.", e);
   }
   
   return adapter;
 }

 /**
  * @param actualClass
  * @param requiredInterface
  * @return
  */
 private boolean classImplementsInterface
  (Class actualClass,
   Class requiredInterface)
 {
   Class[] implementedInterfaces = actualClass.getInterfaces();
   for (int i = 0; i < implementedInterfaces.length; i++)
   {
     if (implementedInterfaces[i].equals(requiredInterface))
     {
       return true;
     }
   }

   // If we've gotten this far, the actualClass does not
   // implement the requiredInterface.
   return false;
 }

 /**
  * This method loads the named TreeAdapter class, instantiates it
  * using its parameterless constructor, and passes back the new
  * instance.
  * 
  * @param adapterName The fully-qualified class-name of the
  *     TreeAdapter.
  * @return the new TreeAdapter.
  */
 public TreeAdapter getTreeAdapter(String adapterName)
 {
   TreeAdapter adapter
    = (TreeAdapter)
        (loadAndCheckAdapter
          (adapterName, TreeAdapter.class, "Tree"));

   if (adapter instanceof TreeAdapterImpl)
   {
     ((TreeAdapterImpl) (adapter)).setSignet(this);
   }

   return adapter;
 }

 /**
  * This method loads the named HTMLLimitRenderer class, instantiates
  * it using its parameterless constructor, and passes back the new
  * instance.
  * 
  * @param rendererName The fully-qualified class-name of the
  *     HTMLLimitRenderer.
  * @return the new HTMLLimitRenderer.
  */
 HTMLLimitRenderer getHTMLLimitRenderer(String rendererName)
 {
   HTMLLimitRenderer renderer
    = (HTMLLimitRenderer)
        (loadAndCheckAdapter
          (rendererName,
           HTMLLimitRenderer.class,
           "HTMLLimitRenderer"));

   if (renderer instanceof HTMLLimitRendererImpl)
   {
     ((HTMLLimitRendererImpl) (renderer)).setSignet(this);
   }

   return renderer;
 }

 /**
  * Formats a scope-tree for display. This method should probably be
  * moved to some new, display-oriented class.
  * 
  * @param treeNode
  *     The TreeNode whose ancestry is to be displayed.
  * @param childSeparatorPrefix
  *     A String which is to appear before every instance of a
  *     child-node (that is, before every TreeNode except the root).
  * @param levelPrefix
  *     A String which is to appear before every instance of TreeNode
  *    which is a child of its immediate predecessor (that is,
  *    between a parent-node and its first child-node).
  * @param levelSuffix
  *    A String which is to appear after every instance of TreeNode
  *    which is a parent of its immediate successor (that is,
  *    between a parent-node and its first child-node).
  * @param childSeparatorSuffix
  *    A String which is to appear after every instance of a
  *    child-node (that is, after every TreeNode except the root).
  * 
  * @return
  *   A String representation of the specified node and its
  *  ancestors.
  * 
  * @throws ObjectNotFoundException
  */
 public String displayAncestry
  (TreeNode treeNode,
   String   childSeparatorPrefix,
    String  levelPrefix,
    String  levelSuffix,
    String  childSeparatorSuffix)
 {
   StringBuffer display = new StringBuffer();
   Set roots = new HashSet();

   buildAncestry
    (display,
     treeNode,
     childSeparatorPrefix + levelPrefix,
     levelSuffix + childSeparatorSuffix,
     roots);
   
   Iterator rootsIterator = roots.iterator();
   while (rootsIterator.hasNext())
   {
     TreeNode root = (TreeNode)(rootsIterator.next());
     display.insert(0, root.getName());  
   }  
   
   display.insert(0, levelPrefix);
   display.append(levelSuffix);
   
   return display.toString();
 }

 /**
  * 
  * @param display
  * @param node
  * @param prefix
  * @param suffix
  * @return a Set of roots of the specified TreeNode.
  */
 private void buildAncestry
   (StringBuffer  display,
    TreeNode      node,
    String        prefix,
    String        suffix,
    Set         roots)
 {
   if (node.getParents().size() == 0)
   {
     // This method does not display the roots of the Tree.
     // That is handled by this method's caller.
     roots.add(node);
     return;
   }

   display.insert(0, node.getName());
   display.insert(0, prefix);
   display.append(suffix);

   Set parents = node.getParents();
   Iterator parentsIterator = parents.iterator();
   while (parentsIterator.hasNext())
   {
     buildAncestry
      (display,
       (TreeNode)(parentsIterator.next()),
       prefix,
        suffix,
        roots);
   }
 }

 /**
  * Gets a single Subsystem by ID.
  * 
  * @param string
  * @return
  */
 public Subsystem getSubsystem(String id)
 throws ObjectNotFoundException
 {
   SubsystemImpl subsystemImpl;
   
   if (id == null)
   {
     throw new IllegalArgumentException
       ("Subsystem IDs must be non-null.");
   }
   
   if (id.length() == 0)
   {
     throw new IllegalArgumentException
       ("Zero-length Subsystem IDs are not allowed.");
   }

   try
   {
     subsystemImpl = (SubsystemImpl) (session.load(SubsystemImpl.class, id));
   }
   catch (net.sf.hibernate.ObjectNotFoundException onfe)
   {
     throw new edu.internet2.middleware.signet.ObjectNotFoundException
       ("Unable to retrieve Subsystem with ID '" + id + "'.",
        onfe);
   }
   catch (HibernateException he)
   {
     throw new SignetRuntimeException(he);
   }

   subsystemImpl.setSignet(this);
   return subsystemImpl;
 }

 /**
  * Gets the Signet super-privileged Subject, creating it if it does not
  * already exist in the database.
  * 
  * @return
  * @throws ObjectNotFoundException
  */
 public PrivilegedSubject getSuperPrivilegedSubject()
     throws ObjectNotFoundException
 {
   // If we've already fetched the SuperPrivilegedSubject from the
   // database, we'll just return it and be done.
   if (superPSubject != null)
   {
     return superPSubject;
   }

   // Let's fetch the SuperPrivilegedSubject from the database, starting
   // with its underlying Subject.

   Subject superSubject
    = this.getSubject
        (Signet.DEFAULT_SUBJECT_TYPE_ID,
          Signet.SUPERSUBJECT_ID);

   superPSubject = new PrivilegedSubjectImpl(this, superSubject);

   return superPSubject;
 }

  /**
   * Gets a single Subject by type and ID.
   * 
   * @param subjectTypeId
   * @param subjectId
   * @return
   * @throws ObjectNotFoundException
   */
  public Subject getSubject(String subjectTypeId, String subjectId)
  throws ObjectNotFoundException
  {
    // Here's a special case: A null subjectTypeId and subjectId will yield
    // a null Subject.
    if ((subjectTypeId == null) && (subjectId == null))
    {
      return null;
    }
    
    Subject subject = null;
    for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext(); )
    {
      try
      {
        subject = ((Source)iter.next()).getSubject(subjectId);
      }
      catch (SubjectNotFoundException snfe)
      {
        // Don't do anything since we may find the subject
        // in other sources.
      }
    }
    if (subject == null)
    {
      throw new ObjectNotFoundException
        ("Unable to find Subject by subject ID '"
         + subjectId
         + "' and subject type ID '"
         + subjectTypeId
         + "'.");
    }

   return subject;
 }

 /**
  * Gets a single Subject by type and display ID.
  * 
  * @param subjectTypeId
  * @param displayId
  * @return
  * @throws ObjectNotFoundException
  */
 public Subject getSubjectByDisplayId(String subjectTypeId, String displayId)
     throws ObjectNotFoundException
 {
   Subject subject = null;
   for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext(); ) {
   	try {
    	subject = ((Source)iter.next()).getSubjectByIdentifier(displayId);
    }
    catch (SubjectNotFoundException snfe) {
        // Don't do anything since we may find the subject
        // in other sources.
    }
   }
  if (subject == null) {
    throw new ObjectNotFoundException("Unable to find Subject by display ID.");
  }
   return subject;
 }

 /**
  * Finds a set of Subjects which matches the argument search value.
  * 
  * @param searchValue
  * @return
  */
 public Set findPrivilegedSubjects(String searchValue)
 {
  Set pSubjects = new HashSet();
   for (Iterator iter = sourceManager.getSources().iterator(); iter.hasNext(); ) {
    Set result = ((Source)iter.next()).search(searchValue);
    for (Iterator iter2 = result.iterator(); iter2.hasNext();) {
      PrivilegedSubject pSubject =
        getPrivilegedSubject((Subject)iter2.next());
      pSubjects.add(pSubject);
    }
   }
   return pSubjects;
 }
 
 /**
  * Finds a set of Subjects by type and search value.
  * 
  * @param subjectTypeId
  * @param searchValue
  * @return
  */
 public Set findPrivilegedSubjects(String subjectTypeId, String searchValue)
 {
  Set pSubjects = new HashSet();
   for (Iterator iter = getSources(subjectTypeId).iterator(); iter.hasNext(); ) {
    Set result = ((Source)iter.next()).search(searchValue);
    for (Iterator iter2 = result.iterator(); iter2.hasNext();) {
      PrivilegedSubject pSubject =
        getPrivilegedSubject((Subject)iter2.next());
      pSubjects.add(pSubject);
    }
   }
   return pSubjects;
 }


 /**
  * Normalizes a Subject attribute-value. Should this method be public?
  * 
  * @param value
  *          The Value of a Signet Subject attribute.
  * 
  * @return the normalized version of the attribute value. This is the original
  *         value shifted to all lower-case, and with all punctuation marks
  *         removed.
  */
 public String normalizeSubjectAttributeValue(String value)
 {
   int valueLen = value.length();
   StringBuffer normalized = new StringBuffer(valueLen);

   for (int i = 0; i < valueLen; i++)
   {
     char currentChar = value.charAt(i);
     if (Character.isLetterOrDigit(currentChar))
     {
       normalized.append(currentChar);
     }
     else
       if (Character.isWhitespace(currentChar))
       {
         normalized.append(currentChar);
       }
   }

   return new String(normalized);
 }

 private Set getRootsOfContainingTrees(Set treeNodes)
 {
   Set roots = new HashSet();

   Iterator treeNodesIterator = treeNodes.iterator();
   while (treeNodesIterator.hasNext())
   {
     TreeNode treeNode = (TreeNode) (treeNodesIterator.next());
     Tree tree = treeNode.getTree();
     ((TreeImpl)tree).setSignet(this);
     roots.addAll(tree.getRoots());
   }

   return roots;
 }

 /**
  * Formats a Tree for display, with special handling of specified nodes, the
  * ancestors of those noes, and the descendants of those nodes.
  * 
  * @param ancestorPrefix
  * @param selfPrefix
  * @param descendantPrefix
  * @param prefixIncrement
  * @param infix
  * @param infixIncrement
  * @param suffix
  * @param treeNodesOfInterest
  * @return
  * @throws TreeNotFoundException
  */
 public String printTreeNodesInContext
   (String ancestorPrefix,
    String selfPrefix,
    String descendantPrefix,
    String prefixIncrement,
    String infix,
    String infixIncrement,
    String suffix,
    Set    treeNodesOfInterest)
 {
   StringBuffer scopesDisplay = new StringBuffer();
   Set roots = getRootsOfContainingTrees(treeNodesOfInterest);

   Iterator rootsIterator = roots.iterator();
   while (rootsIterator.hasNext())
   {
     TreeNode root = (TreeNode) (rootsIterator.next());
     printTreeNode(scopesDisplay, ancestorPrefix, selfPrefix,
         descendantPrefix, prefixIncrement, infix, infixIncrement, suffix,
         treeNodesOfInterest, root);
   }

   return scopesDisplay.toString();
 }

 /**
  * Formats a Tree for display. This method should probably be moved to some
  * new, display-oriented class.
  * 
  * @param ancestorPrefix
  * @param selfPrefix
  * @param descendantPrefix
  * @param prefixIncrement
  * @param infix
  * @param infixIncrement
  * @param followingLine
  * @param tree
  * @return
  * @throws TreeNotFoundException
  */
 public String printTree(String ancestorPrefix, String selfPrefix,
     String descendantPrefix, String prefixIncrement, // gets PREpended to
     // prefix
     String infix, String infixIncrement, // gets APpended to infix
     String followingLine, Tree tree)
 {
   StringBuffer treeDisplay = new StringBuffer();

   if (tree != null)
   {
     Set roots = tree.getRoots();
     Iterator rootsIterator = roots.iterator();
     while (rootsIterator.hasNext())
     {
       TreeNode root = (TreeNode) (rootsIterator.next());
       Set allTreeNodes = root.getTree().getTreeNodes();

       printTreeNode(treeDisplay, ancestorPrefix, selfPrefix,
           descendantPrefix, prefixIncrement, infix, infixIncrement,
           followingLine, allTreeNodes, root);
     }
   }

   return treeDisplay.toString();
 }

 private void printTreeNode(StringBuffer scopesDisplay, String ancestorPrefix,
     String selfPrefix, String descendantPrefix, String prefixIncrement,
     String infix, String infixIncrement, String suffix,
     Set allGrantableScopes, TreeNode treeNode)
 {
   if (treeNode == null)
   {
     return;
   }
   if (allGrantableScopes.contains(treeNode))
   {
     scopesDisplay.append(selfPrefix);
   }
   else
     if (treeNode.isAncestorOfAll(allGrantableScopes))
     {
       scopesDisplay.append(ancestorPrefix);
     }
     else
     {
       scopesDisplay.append(descendantPrefix);
     }

   scopesDisplay.append(treeNode);
   scopesDisplay.append(infix);
   scopesDisplay.append(treeNode.getName());
   scopesDisplay.append(suffix);

   Set children = treeNode.getChildren();
   SortedSet sortedChildren = new TreeSet(children);
   Iterator sortedChildrenIterator = sortedChildren.iterator();
   while (sortedChildrenIterator.hasNext())
   {
     printTreeNode(scopesDisplay, prefixIncrement + ancestorPrefix,
         prefixIncrement + selfPrefix, prefixIncrement + descendantPrefix,
         prefixIncrement, infix + infixIncrement, infixIncrement, suffix,
         allGrantableScopes, (TreeNode) (sortedChildrenIterator.next()));
   }
 }

 /**
  * Gets a single Assignment by ID.
  * 
  * @param assignmentId
  * @return the fetched Assignment object
  * @throws ObjectNotFoundException
  */
 public Assignment getAssignment(int id)
 throws ObjectNotFoundException
 {
   Assignment assignment;

   try
   {
     assignment = (Assignment) (session.load(AssignmentImpl.class,
         new Integer(id)));
   }
   catch (net.sf.hibernate.ObjectNotFoundException onfe)
   {
     throw new edu.internet2.middleware.signet.ObjectNotFoundException(onfe);
   }
   catch (HibernateException he)
   {
     throw new SignetRuntimeException(he);
   }

   return assignment;
 }
 
  /**
   * Gets a single Proxy by ID.
   * 
   * @param proxyId
   * @return the fetched Proxy object
   * @throws ObjectNotFoundException
   */
  public Proxy getProxy(int id)
  throws ObjectNotFoundException
  {
    Proxy proxy;

    try
    {
      proxy = (Proxy) (session.load(ProxyImpl.class, new Integer(id)));
    }
    catch (net.sf.hibernate.ObjectNotFoundException onfe)
    {
      throw new edu.internet2.middleware.signet.ObjectNotFoundException(onfe);
    }
    catch (HibernateException he)
    {
      throw new SignetRuntimeException(he);
    }

    return proxy;
  }



 /**
  * @param helptext
  * @return
  */
 public Limit newLimit
  (Subsystem  subsystem,
   String     id,
   DataType   dataType,
   ChoiceSet  choiceSet,
   String     name,
   int        displayOrder,
   String     helpText,
   Status     status,
   String     renderer)
 {
   Limit limit
    = new LimitImpl
        (this,
         subsystem,
         id,
         dataType,
         choiceSet,
         name,
         displayOrder,
         helpText,
         status,
         renderer);
   
   subsystem.add(limit);
   
   return limit;
 }

 /**
  * Creates a new ChoiceSet, using the default Signet
  * ChoiceSetAdapter.
  * 
  * @param subsystem
  * @param id
  * @return the new ChoiceSet
  */
 public final ChoiceSet newChoiceSet
  (Subsystem  subsystem,
   String     id)
 {
   ChoiceSetAdapter defaultChoiceSetAdapter
    = getChoiceSetAdapter(DEFAULT_CHOICE_SET_ADAPTER_NAME);
   ChoiceSet newChoiceSet
    = new ChoiceSetImpl
        (this, subsystem, defaultChoiceSetAdapter, id);
   
   return newChoiceSet;
 }

 /**
  * @return
  */
 public ChoiceSet newChoiceSet
  (Subsystem        subsystem,
   ChoiceSetAdapter choiceSetAdapter,
   String           id)
 {
   ChoiceSet choiceSet
    = new ChoiceSetImpl(this, subsystem, choiceSetAdapter, id);
   
   return choiceSet;
 }
 
 /**
  * Returns Source adapters which supports the argument SubjectType.
  * @return Collection of Source adapters
  */
 public Collection getSources(String subjectTypeId)
 {
  SubjectType type = SubjectTypeEnum.valueOf(subjectTypeId);
  Collection sources = this.sourceManager.getSources(type);
  
  // Sometimes, SourceManaget.getSources() returns null. We want to avoid
  // doing that.
  if (sources == null)
  {
    sources = new HashSet();
  }
  
  return sources;
 }
}