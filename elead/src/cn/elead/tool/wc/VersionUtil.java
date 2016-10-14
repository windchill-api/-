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
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pom.PersistenceException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.vc.Mastered;
import wt.vc.VersionControlHelper;
import wt.vc._Versioned;

import com.google.gwt.rpc.client.impl.RemoteException;

public class VersionUtil implements RemoteAccess{
	private final static Logger LOGGER = LogR.getLogger(VersionUtil.class.getName());
	private static String CLASSNAME = VersionUtil.class.getName();
		/**
		 * 没有方法说明 这个方法是做啥的
		 * <一句话功能简述>
		 * <功能详细描述>
		 * @author  zhangxj
		 * @see [类、类#方法、类#成员]
		 */
	public static RevisionControlled getLatestRevision(Master master){
        try {
			if (!RemoteMethodServer.ServerFlag) {
						return (RevisionControlled) RemoteMethodServer.getDefault().invoke("getLatestRevision", 
							VersionUtil.class.getName(), null, new Class[] { Master.class},
							new Object[] { master });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
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
			      } catch (WTException e) {
					 LOGGER.error(CLASSNAME+".getLatestRevision:" + e);
				  } finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				 }
			   }
			   return rc;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
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
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getIteration", 
						VersionUtil.class.getName(), null, new Class[] { RevisionControlled.class},
					new Object[] { revisionControlled });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				String gii = revisionControlled.getIterationIdentifier().getValue();
				SessionServerHelper.manager.setAccessEnforced(enforce);
			    return gii;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	LOGGER.error(e.getMessage(),e); 
		}
		return null;
	}
		 
	/**
	 *@author BaiJuanjuan
	 * eg：B  (the latest)
	 * @param RevisionControlled
	 * @throws WTException
	 */
	public static String getVersion(RevisionControlled revisionControlled) throws WTException {
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
	@SuppressWarnings("deprecation")
	public static RevisionControlled getLatestObject(QueryResult queryresult,Master master) throws WTException {
        try {
			if (!RemoteMethodServer.ServerFlag) {
						return (RevisionControlled) RemoteMethodServer.getDefault().invoke("getLatestObject", 
								VersionUtil.class.getName(), null, new Class[] { QueryResult.class,Master.class},
								new Object[] { queryresult,master });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				RevisionControlled rc = null;
				try {
					
				    if(master != null ){
					    while (queryresult.hasMoreElements()) {
					        RevisionControlled obj = ((RevisionControlled) queryresult.nextElement());
					        if (rc == null || obj.getVersionIdentifier().getSeries().greaterThan(rc.getVersionIdentifier().getSeries()))
					            rc = obj;
					    }
					    if (rc != null){
					        return (RevisionControlled) VersionControlHelper.getLatestIteration(rc);
					    }
				    }	
				} catch (Exception e) {
					 LOGGER.error(CLASSNAME+".getLatestObject:" + e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
		        return rc;
		    }
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
        	LOGGER.error(e.getMessage(),e); 
		}
        return null;
	}
 

	/**
	 * 获取什么状态可以写称参数。 就可以想获取什么状态的就获取什么状态的。
	 * 获取文档的最新已发布版本
	 * 
	 * @param revisionControlled
	 * @return
	 * @throws PersistenceException
	 * @throws WTException
	 */
	public static RevisionControlled findLatestVersion(RevisionControlled revisionControlled)throws PersistenceException, WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
						return (RevisionControlled) RemoteMethodServer.getDefault().invoke("findLatestVersion", 
								OrganizationUtil.class.getName(), null, new Class[] { RevisionControlled.class},
								new Object[] { revisionControlled });
			} else {
				  boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				  RevisionControlled revisContr = null;
				  try {
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
				   } catch (Exception e) {
					 LOGGER.error(CLASSNAME+".findLatestVersion:" + e);
				   } finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				  }
				  return revisContr;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
	    	LOGGER.error(e.getMessage(),e); 
		}
	    return null;
					
	}
		  
	/**
	 * 这个个方法和上一个getLatestObject 重复
	 * this method is used to get latest object
	 * 
	 * @param master
	 * @return
	 * @throws WTException
	 */
	public static RevisionControlled getLatestObject(Mastered mastered) throws WTException {
	    try {
			if (!RemoteMethodServer.ServerFlag) {
					return (RevisionControlled) RemoteMethodServer.getDefault().invoke("getLatestObject", 
							VersionUtil.class.getName(), null, new Class[] { String.class},
							new Object[] { mastered });
			} else {
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				RevisionControlled controlled = null;
				try {
					QueryResult queryResult = VersionControlHelper.service.allVersionsOf(mastered);
					controlled = (RevisionControlled) queryResult.nextElement();
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return controlled;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
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
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static List<String> getDocumentVersionList(String docNum) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (List<String>) RemoteMethodServer.getDefault().invoke(
							"getDocumentVersionList", VersionUtil.class.getName(), null, new Class[] { String.class }, new Object[] { docNum });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				List<String> list = new ArrayList<String>();
				QuerySpec spec = new QuerySpec(WTDocument.class);
				spec.appendSearchCondition(new SearchCondition(WTDocument.class, WTDocument.NUMBER, SearchCondition.EQUAL, docNum, true));
	
				QueryResult results = PersistenceHelper.manager.find(spec);
				try {
					TreeMap<String, String> sortMap = new TreeMap<String, String>();
					NumberFormat nf = NumberFormat.getInstance();
					nf.setMinimumIntegerDigits(5);
					nf.setMaximumIntegerDigits(5);
					nf.setGroupingUsed(false);
		
					while (results.hasMoreElements()) {
						WTDocument doc = (WTDocument) results.nextElement();
						sortMap.put(
								doc.getVersionIdentifier().getValue() + "." + nf.format(Integer.parseInt(doc.getIterationIdentifier().getValue())), 
								doc.getVersionIdentifier().getValue() + "." + doc.getIterationIdentifier().getValue());
					}
					for (String key : sortMap.keySet()) {
						list.add(sortMap.get(key));
					}
				} catch (Exception e) {
					 LOGGER.error(CLASSNAME+".findLatestVersion:" + e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return list;
			}
		} catch (NumberFormatException | java.rmi.RemoteException | InvocationTargetException e) {
	    	LOGGER.error(e.getMessage(),e);
		}
		return null;
		
	}
		/**
		 *这个方法就多余了。 可以不用。 一般的对象里面都有了。 
		 * <一句话功能简述>
		 * <功能详细描述>
		 * @author  zhangxj
		 * @see [类、类#方法、类#成员]
		 */
	public static Persistable getLatestByMaster(Mastered master) throws WTException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (Persistable) RemoteMethodServer.getDefault().invoke(
						"getLatestByMaster", VersionUtil.class.getName(), null, new Class[] { Mastered.class }, new Object[] { master });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				Persistable latest = null;
				QueryResult qr = VersionControlHelper.service.allIterationsOf(master);
				try {
					if (qr.hasMoreElements()){
						latest = (Persistable) qr.nextElement();
					}
					
				} catch (Exception e) {
					 LOGGER.error(CLASSNAME+".getLatestByMaster:" + e);
				} finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return latest;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
	    	LOGGER.error(e.getMessage(),e);
		}
		return null;
	}
		  
	public static void test() throws RemoteException,InvocationTargetException, WTException {
		WTPartMaster part = PartUtil.getPartMasterByNumber("HQ11100576000");
		RevisionControlled revisionControlled=getLatestRevision(part);
//		
//		System.out.println(getIteration(getLatestRevision(part)));
		String a = getVersion(revisionControlled);
		System.out.println(a);
		System.out.println(getLatestVersionIteration(revisionControlled));
		List<String> list =getDocumentVersionList("CPJY00000542");
		System.out.println(list);
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
				e.printStackTrace();
			}
		}
	}

}