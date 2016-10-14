package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTHashSet;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.baseline.Baseline;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.Baselineable;
import wt.vc.baseline.ManagedBaseline;

/**
 * 关于基线的创建， 更新。等操作
 * @author zhangxj
 * @version
 *
 */

public class BaseLineUtil implements RemoteAccess, Serializable{
	private static String CLASSNAME = BaseLineUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	private static final String BASELINE_LOCATION = "/Default/Baseline"; //基线路径
	/**
	 * 获取基线
	 * @author zhangxj
	 * @param part
	 * @return
	 * @throws WTException
	 */

	public static ArrayList getAllBaselines(WTPart part) throws WTException {
		try {



			if (!RemoteMethodServer.ServerFlag) {
				return (ArrayList) RemoteMethodServer.getDefault().invoke("getAllBaselines",BaseLineUtil.class.getName(),null,new Class[] { WTPart.class},
						new Object[] { part });
			} else {
				ArrayList baselinelist =new ArrayList();
				boolean foronce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if(part != null ){
						QuerySpec qs =new QuerySpec(ManagedBaseline.class);
						qs.appendWhere(WTContainerHelper.getWhereContainerIs(part.getContainer()));
						QueryResult qr = PersistenceHelper.manager.find(qs);
						ManagedBaseline baseline =null;
						while (qr.hasMoreElements()) {
							WTObject obj = (WTObject) qr.nextElement();
							if (obj instanceof ManagedBaseline) {
								baselinelist.add(((ManagedBaseline) obj).getName());
							}
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".getAllBaselines:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(foronce);
				}
				return baselinelist;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * 获取基线
	 * @author zhangxj
	 * @param baselineName
	 * @param containerName
	 * @return
	 * @throws WTException
	 */
	public static ManagedBaseline getBaseline(String baselineName ,String containerName) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ManagedBaseline) RemoteMethodServer.getDefault().invoke("getBaseline",BaseLineUtil.class.getName(),null,new Class[] { String.class,String.class},
						new Object[] { baselineName ,containerName});
			} else {
				boolean foronce = SessionServerHelper.manager.setAccessEnforced(false);
				ManagedBaseline baseline =null;
				try {
					if(!StringUtils.isEmpty(baselineName)&& !StringUtils.isEmpty(containerName)){
						WTContainer wtContainer=WCUtil.getWtContainerByName(containerName);
						QuerySpec qs =new QuerySpec(ManagedBaseline.class);
						qs.appendWhere(WTContainerHelper.getWhereContainerIs(wtContainer));
						qs.appendAnd();
						qs.appendWhere(new SearchCondition(ManagedBaseline.class,ManagedBaseline.NAME, SearchCondition.EQUAL, baselineName,false));
						QueryResult qr = PersistenceHelper.manager.find(qs);
						while (qr.hasMoreElements()) {
							WTObject obj = (WTObject) qr.nextElement();
							if (obj instanceof ManagedBaseline) {
								baseline = (ManagedBaseline) obj;
							}
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".getBaseline:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(foronce);
				}
				return baseline;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 创建基线
	 * @author zhangxj
	 * @param container
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static ManagedBaseline createBaseLine(WTContainer container ,String baseLineName) throws WTException, WTPropertyVetoException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ManagedBaseline) RemoteMethodServer.getDefault().invoke("createBaseLine",BaseLineUtil.class.getName(),null,new Class[] { WTContainer.class,String.class},
						new Object[] { container ,baseLineName});
			}else {
				boolean foronce = SessionServerHelper.manager.setAccessEnforced(false);
				ManagedBaseline baseline = ManagedBaseline.newManagedBaseline();
				try {
					if(container != null && !StringUtils.isEmpty(baseLineName)){
						Folder folder= null;
						baseline.setName(baseLineName);
						//文件夹获取失败，直接创建基线
						folder = FolderHelper.service.getFolder(BASELINE_LOCATION, WTContainerRef.newWTContainerRef(container));
						if( folder == null ) {
							folder = FolderHelper.service.createSubFolder(BASELINE_LOCATION, WTContainerRef.newWTContainerRef(container));
						}
						WTHashSet hashSet = new WTHashSet();
						hashSet.add(baseline);
						FolderHelper.assignLocation(hashSet, folder);
						PersistenceHelper.manager.save(baseline);
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".createBaseLine:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(foronce);
				}
				return baseline;
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 基线中添加对象
	 * @param object
	 * @param baseline
	 */
	public static void  addObjectForBaseline(WTObject object,Baseline baseline) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("addObjectForBaseline",BaseLineUtil.class.getName(),null,new Class[] { WTObject.class,Baseline.class},
						new Object[] { object ,baseline});
			}else {
				boolean foronce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if(object != null && baseline != null){
						Baselineable baselineable=(Baselineable) object;
						BaselineHelper.service.addToBaseline(baselineable, baseline);
					}
				} catch (WTException e) {
					logger.error(CLASSNAME+".addObjectForBaseline:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(foronce);
				}
			}
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		} 
	}
	
        /**
         * 移除基线中的对象
         * @param object
         * @param baseline
         */
	   public static void removeObjectfromBaseline(WTObject object, Baseline baseline){
		   try {
				if (!RemoteMethodServer.ServerFlag) {
						RemoteMethodServer.getDefault().invoke("removeObjectfromBaseline",BaseLineUtil.class.getName(),null,new Class[] { WTObject.class,Baseline.class },
								new Object[] { object ,baseline});
					}else {
						boolean foronce = SessionServerHelper.manager.setAccessEnforced(false);
						try {
							if(object != null && baseline != null){
								Baselineable baselineable = (Baselineable) object;
								BaselineHelper.service.removeFromBaseline(baselineable, baseline);
							}
						} catch (WTException e) {
							logger.error(CLASSNAME+".removeObjectfromBaseline:"+e);
						}finally{
							SessionServerHelper.manager.setAccessEnforced(foronce);
						}
					}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
	   }
}
