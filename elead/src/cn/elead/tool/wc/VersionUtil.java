package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.QueryResult;
import wt.inf.container.WTContained;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.PersistenceException;
import wt.series.MultilevelSeries;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc._Versioned;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.ptc.windchill.enterprise.part.psb.client.ops.GetIterationForMasterOperation;

public class VersionUtil implements RemoteAccess{
		
		 public static RevisionControlled getLatestRevision(Master master){
		    RevisionControlled rc = null;
		    if (master != null) {
		      try {
		        QueryResult qr = VersionControlHelper.service.allVersionsOf(master);
		        while (qr.hasMoreElements()) {
		          RevisionControlled obj = (RevisionControlled)qr.nextElement();
		         MultilevelSeries c = obj.getVersionIdentifier().getSeries();
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
		 /**
	     *@author BaiJuanjuan
	     * eg：2  (the latest)
	     * @param RevisionControlled
	     * @throws WTException
	     */
		 public static String getIteration(RevisionControlled revisionControlled)throws WTException {
			    return revisionControlled.getIterationIdentifier().getValue();
		 }
		 
	    /**
	     *@author BaiJuanjuan
	     * eg：B  (the latest)
	     * @param RevisionControlled
	     * @throws WTException
	     */
		 public static String getVersion(RevisionControlled revisionControlled) throws WTException {
				//return getIteration(revisionControlled);
			    return revisionControlled.getVersionIdentifier().getValue();
		 }
		 
	    /**
	     *@author BaiJuanjuan
	     * eg：B.2  (the latest)
	     * @param RevisionControlled
	     * @throws WTException
	     */
		 public static String getLatestVersionIteration(RevisionControlled revisionControlled) throws WTException {
			    return getVersion(revisionControlled) + "." + getIteration(revisionControlled);
		 }
		 
		 /**
		 * @author BaiJuanjuan
		 * return a latest versionAndIteration object in a list.
		 * @param queryresult
		 * @param master
		 * @return RevisionControlled
		 * @throws WTException
		 */
		public static RevisionControlled getLatestObject(QueryResult queryresult,Master master) throws WTException {
		    RevisionControlled rc = null;
		    if(master != null ){
		    while (queryresult.hasMoreElements()) {
		        RevisionControlled obj = ((RevisionControlled) queryresult.nextElement());
		        if (rc == null || obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))
		            rc = obj;
		    }
		    if (rc != null)
		        return (RevisionControlled) VersionControlHelper.getLatestIteration(rc);
		    }
		        return rc;
		}
	 

		/**
		 * 通過master取得最新版本版序
		 *
		 * @function 通過Mastered來取得最新的版本版序對象
		 * @param master
		 * @return RevisionControlled
		 * @throws PersistenceException
		 * @throws WTException
		 */
		public static RevisionControlled getLatestObject(Master master) {
			RevisionControlled revidionControlled = null;
			if (master != null) {
				try {
					QueryResult queryResult = VersionControlHelper.service
							.allVersionsOf(master);
					return getLatestObject(queryResult);
				} catch (wt.util.WTException wte) {
					wte.printStackTrace();
				}
			}
			return revidionControlled;
		}
		
		private static RevisionControlled getLatestObject(QueryResult queryresult) {
			RevisionControlled rc = null;
			if (queryresult != null) {
				try {
					while (queryresult.hasMoreElements()) {
						RevisionControlled obj = (RevisionControlled) queryresult
								.nextElement();
						if (rc == null
								|| obj.getVersionIdentifier()
								.getSeries()
								.greaterThan(
										rc.getVersionIdentifier()
										.getSeries())) {
							rc = obj;
						}
					}
					if (rc != null) {
						return (RevisionControlled) wt.vc.VersionControlHelper
								.getLatestIteration(rc);
					} else {
						return rc;
					}
				} catch (wt.util.WTException wte) {
					wte.printStackTrace();
				}
			}
			return rc;
		}

		  /**
		   * 获取文档的最新已发布版本
		   * 
		   * @param docNumber
		   * @return
		   * @throws PersistenceException
		   * @throws WTException
		   */
		  public static RevisionControlled findRightVersionDoc(RevisionControlled revisionControlled)throws PersistenceException, WTException {
			  RevisionControlled revisContr = null;
			  if(revisContr instanceof WTDocument){
				  RevisionControlled doc = (RevisionControlled) DocUtil.getDocumentVersionList(_Versioned.VERSION_INFO);
					if (doc == null || revisionControlled == null) {
						return revisContr;
					}
					WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
					QueryResult qr = null;
					qr = VersionControlHelper.service.allVersionsOf(docMaster);
					while (qr.hasMoreElements()) {
						WTDocument temp = (WTDocument) qr.nextElement();
						String status = temp.getLifeCycleState().getDisplay(
								Locale.ENGLISH/*.SIMPLIFIED_CHINESE*/);
						if ("RELEASED".equals(status)) {
							revisContr = temp;
							break;
						}
					}
					return revisContr; 
			  }
			return revisContr;
		  }
		  
			/**
			 * this method is used to get latest object
			 * 
			 * @param master
			 * @return
			 * @throws WTException
			 */
			public static RevisionControlled getLatestObject(Mastered mastered)
					throws WTException {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				RevisionControlled controlled = null;
				try {
					QueryResult queryResult = VersionControlHelper.service
							.allVersionsOf(mastered);
					controlled = (RevisionControlled) queryResult.nextElement();
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}

				return controlled;
			}
		  
		 public static void test() throws RemoteException,InvocationTargetException, WTException {
			WTDocument a = DocUtil.getDocumentByNumber("1001");
			Master master = (Master) a.getMaster();
			if(master != null){
			//System.out.println("******"+getLatestRevision(master));
			RevisionControlled revisionControlled = getLatestRevision(master);
			if(revisionControlled != null){
			System.out.println("!!!!!"+getIteration(null));
			System.out.println(">>>>>>"+getVersion(null));
			System.out.println("<<<<<<<<<<"+getLatestVersionIteration(revisionControlled));
			/*QueryResult queryResult = VersionControlHelper.service.allVersionsOf(master);
			 while (queryResult.hasMoreElements()) {
				 QueryResult obj = ((QueryResult) queryResult.nextElement());
			     System.out.println(getLatestObject(obj, master));
			 }*/
			System.out.println(findRightVersionDoc(revisionControlled));
			}
			}
		
		 }

		public static void main(String[] args) throws RemoteException,
				InvocationTargetException, WTException {
			if (!RemoteMethodServer.ServerFlag) {
				try {
					RemoteMethodServer server = RemoteMethodServer.getDefault();
					server.setUserName("wcadmin");
					server.setPassword("wcadmin");
	
					RemoteMethodServer.getDefault().invoke("test",
							VersionUtil.class.getName(), null, new Class[] {},
							new Object[] {});
	
				} catch (java.rmi.RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		
	
}