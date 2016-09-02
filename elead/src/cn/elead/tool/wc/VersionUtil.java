package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;

import wt.doc.WTDocument;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.QueryResult;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pom.PersistenceException;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;

public class VersionUtil implements RemoteAccess
{
  public static String getIteration(RevisionControlled revisionControlled)
    throws WTException
  {
    return revisionControlled.getIterationIdentifier().getValue();
  }

  public static String getVersion(RevisionControlled revisionControlled)
    throws WTException
  {
    return getIteration(revisionControlled);
  }

  public static String getVersionIteration(RevisionControlled revisionControlled)
    throws WTException
  {
    return getVersion(revisionControlled) + "." + 
      getIteration(revisionControlled);
  }

  public static RevisionControlled getLatestRevision(Master master)
  {
    RevisionControlled rc = null;
    if (master != null) {
      try {
        QueryResult qr = VersionControlHelper.service.allVersionsOf(master);
        while (qr.hasMoreElements()) {
          RevisionControlled obj = (RevisionControlled)qr.nextElement();
          if ((rc == null) || (obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))) {
            rc = obj;
          }
        }
        if (rc != null)
          rc = (RevisionControlled)VersionControlHelper.getLatestIteration(rc, false);
      }
      catch (PersistenceException e) {
        e.printStackTrace();
      } catch (VersionControlException e) {
        e.printStackTrace();
      } catch (WTException e) {
        e.printStackTrace();
      }
    }
    return rc;
  }

  public static RevisionControlled getLatestRevision(RevisionControlled rc)
    throws PersistenceException, WTException
  {
    if (rc != null) {
      Mastered master = rc.getMaster();
      QueryResult qr = VersionControlHelper.service.allVersionsOf(master);
      while (qr.hasMoreElements()) {
        RevisionControlled obj = (RevisionControlled)qr.nextElement();
        if (!obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))
          continue;
        rc = obj;
      }

      rc = (RevisionControlled)VersionControlHelper.getLatestIteration(rc, false);
    }
    return rc;
  }

  public static boolean isLatestRevision(RevisionControlled rc)
    throws PersistenceException, WTException
  {
    boolean flag = false;

    if (rc != null) {
      RevisionControlled newRc = getLatestRevision(rc);
      String newRc_V_I = getVersionIteration(newRc);
      String rc_V_I = getVersionIteration(rc);
      if (newRc_V_I.equals(rc_V_I)) {
        flag = true;
      }
    }
    return flag;
  }

  public static void test()
    throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
  {
    WTDocument doc = DocUtil.getDocumentByName("test01");
    if (doc != null) {
      Master master = (Master)doc.getMaster();
      RevisionControlled rc1 = getLatestRevision(master);
      System.out.println(rc1);
    }
    RevisionControlled rc = DocUtil.getDocumentByName("test01");
    System.out.println(getLatestRevision(rc));
    System.out.println(isLatestRevision(rc));
  }

  public static void main(String[] args)
    throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
  {
    if (!RemoteMethodServer.ServerFlag)
      try {
        RemoteMethodServer.getDefault().invoke("test", 
          VersionUtil.class.getName(), null, new Class[0], 
          new Object[0]);
      }
      catch (java.rmi.RemoteException e)
      {
        e.printStackTrace();
      }
  }
}