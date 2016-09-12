package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.enterprise.Master;
import wt.enterprise.RevisionControlled;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContained;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.PersistenceException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
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
	private final static Logger LOGGER = LogR.getLogger(VersionUtil.class.getName());
		
		 public static RevisionControlled getLatestRevision(Master master){
			 try{
			        if (!RemoteMethodServer.ServerFlag) {
			                try {
								return (RevisionControlled) RemoteMethodServer.getDefault().invoke("getLatestRevision", 
										VersionUtil.class.getName(), null, new Class[] { Master.class},
										new Object[] { master });
							} catch (java.rmi.RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        } else {
			        	WTOrganization org = null;
			        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
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
					    SessionServerHelper.manager.setAccessEnforced(enforce);
					    return rc;
			        }
		        } catch (RemoteException e) {
		            LOGGER.error(e.getMessage(),e);
		        } catch (InvocationTargetException e) {
		        	LOGGER.error(e.getMessage(),e);
		        }
		        return null;
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
			 try{
			        if (!RemoteMethodServer.ServerFlag) {
			                try {
								return (RevisionControlled) RemoteMethodServer.getDefault().invoke("getLatestObject", 
										VersionUtil.class.getName(), null, new Class[] { QueryResult.class,Master.class},
										new Object[] { queryresult,master });
							} catch (java.rmi.RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        } else {
			        	WTOrganization org = null;
			        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
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
					    SessionServerHelper.manager.setAccessEnforced(enforce);
					        return rc;
				        }
				        } catch (RemoteException e) {
				            LOGGER.error(e.getMessage(),e);
				        } catch (InvocationTargetException e) {
				        	LOGGER.error(e.getMessage(),e);
				        }
	        return null;
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
			  try{
			        if (!RemoteMethodServer.ServerFlag) {
			                try {
								return (RevisionControlled) RemoteMethodServer.getDefault().invoke("findRightVersionDoc", 
										OrganizationUtil.class.getName(), null, new Class[] { RevisionControlled.class},
										new Object[] { revisionControlled });
							} catch (java.rmi.RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        } else {
			        	  boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
						  RevisionControlled revisContr = null;
						  if(revisContr instanceof WTDocument){
							  RevisionControlled doc = (RevisionControlled) VersionUtil.getDocumentVersionList(_Versioned.VERSION_INFO);
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
						  }
						return revisContr;
			        }
		        } catch (RemoteException e) {
		            LOGGER.error(e.getMessage(),e);
		        } catch (InvocationTargetException e) {
		        	LOGGER.error(e.getMessage(),e);
		        }
		        return null;
						
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
				 try{
				        if (!RemoteMethodServer.ServerFlag) {
				                try {
									return (RevisionControlled) RemoteMethodServer.getDefault().invoke("getLatestObject", 
											VersionUtil.class.getName(), null, new Class[] { String.class},
											new Object[] { mastered });
								} catch (java.rmi.RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				        } else {
							        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
							RevisionControlled controlled = null;
							try {
								QueryResult queryResult = VersionControlHelper.service
										.allVersionsOf(mastered);
								controlled = (RevisionControlled) queryResult.nextElement();
							} finally {
								SessionServerHelper.manager.setAccessEnforced(enforce);
							}
							SessionServerHelper.manager.setAccessEnforced(enforce);
							return controlled;
							
				        }
		        } catch (RemoteException e) {
		            LOGGER.error(e.getMessage(),e);
		        } catch (InvocationTargetException e) {
		        	LOGGER.error(e.getMessage(),e);
		        }
		        return null;
			}
			

			/**
			 * get document version list, just as A.1, A.2 and so on.
			 * 
			 * @param docNum
			 * @return
			 */
			public static List<String> getDocumentVersionList(String docNum)
					throws WTException {
				List<String> list = new ArrayList<String>();
				try{
				if (!RemoteMethodServer.ServerFlag) {
					try {
						return (List<String>) RemoteMethodServer.getDefault().invoke(
								"getDocumentVersionList", VersionUtil.class.getName(), null,
								new Class[] { String.class }, new Object[] { docNum });
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
						throw new WTException(e);
					}
				} else {
					boolean enforce = wt.session.SessionServerHelper.manager
							.setAccessEnforced(false);

					QuerySpec spec = new QuerySpec(WTDocument.class);
					spec.appendSearchCondition(new SearchCondition(WTDocument.class,
							WTDocument.NUMBER, SearchCondition.EQUAL, docNum, true));

					QueryResult results = PersistenceHelper.manager.find(spec);

					TreeMap<String, String> sortMap = new TreeMap<String, String>();

					NumberFormat nf = NumberFormat.getInstance();
					nf.setMinimumIntegerDigits(5);
					nf.setMaximumIntegerDigits(5);
					nf.setGroupingUsed(false);

					while (results.hasMoreElements()) {
						WTDocument doc = (WTDocument) results.nextElement();
						sortMap.put(
								doc.getVersionIdentifier().getValue()
										+ "."
										+ nf.format(Integer.parseInt(doc
												.getIterationIdentifier().getValue())),
								doc.getVersionIdentifier().getValue() + "."
										+ doc.getIterationIdentifier().getValue());
					}
					for (String key : sortMap.keySet()) {
						list.add(sortMap.get(key));
					}
					
					SessionServerHelper.manager.setAccessEnforced(enforce);
					return list;
				}
	        } catch (RemoteException e) {
	            LOGGER.error(e.getMessage(),e);
	        }
	        return null;
				
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