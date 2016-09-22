package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
@SuppressWarnings("serial")
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
	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes", "unused" })
	public static ArrayList getAllBaselines(WTPart part) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ArrayList) RemoteMethodServer.getDefault().invoke("getAllBaselines",BaseLineUtil.class.getName(),null,new Class[] { WTPart.class},
						new Object[] { part });
			} else {
				ArrayList baselinelist =new ArrayList();
				boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
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
	@SuppressWarnings("deprecation")
	public static ManagedBaseline getBaseline(String baselineName ,String containerName) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ManagedBaseline) RemoteMethodServer.getDefault().invoke("getBaseline",BaseLineUtil.class.getName(),null,new Class[] { String.class,String.class},
						new Object[] { baselineName ,containerName});
			} else {
				boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				ManagedBaseline baseline =null;
				try {
					if(!StringUtil.isEmpty(baselineName)&& !StringUtil.isEmpty(containerName)){
						WTContainer wtContainer=WCUtil.getWTContainerByName(containerName);
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
	 * @param part
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public static ManagedBaseline createBaseLine(WTContainer container ,String baseLineName) throws WTException, WTPropertyVetoException{
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (ManagedBaseline) RemoteMethodServer.getDefault().invoke("getBaseline",BaseLineUtil.class.getName(),null,new Class[] { WTContainer.class,String.class},
						new Object[] { container ,baseLineName});
			}else {
				boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				ManagedBaseline baseline = ManagedBaseline.newManagedBaseline();
				try {
						Folder folder= null;
						//logger.debug("part=" + part);
						logger.debug("baseLineName=" + baseLineName);
						if ( baseLineName == null) {
							return null;
						}
						baseline.setName(baseLineName);
						//WTContainer container = part.getContainer();
						//文件夹获取失败，直接创建基线
						folder = FolderHelper.service.getFolder(BASELINE_LOCATION, WTContainerRef.newWTContainerRef(container));
						if( folder == null ) {
							folder = FolderHelper.service.createSubFolder(BASELINE_LOCATION, WTContainerRef.newWTContainerRef(container));
						}
						WTHashSet hashSet = new WTHashSet();
						hashSet.add(baseline);
						FolderHelper.assignLocation(hashSet, folder);
						PersistenceHelper.manager.save(baseline);
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
				RemoteMethodServer.getDefault().invoke("getBaseline",BaseLineUtil.class.getName(),null,new Class[] { WTObject.class,Baseline.class},
						new Object[] { object ,baseline});
			}else {
				boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					Baselineable baselineable=(Baselineable) object;
					BaselineHelper.service.addToBaseline(baselineable, baseline);
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
					RemoteMethodServer.getDefault().invoke("getBaseline",BaseLineUtil.class.getName(),null,new Class[] { WTObject.class,Baseline.class},
							new Object[] { object ,baseline});
				}else {
					boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
					try {
						Baselineable baselineable = (Baselineable) object;
						BaselineHelper.service.removeFromBaseline(baselineable, baseline);
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
	   
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", BaseLineUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
	
	public static void test() throws WTException, WTPropertyVetoException{
		WTPart wtPart= PartUtil.getPartByNumber("HQ11120019000");
//		WTPart wtPart= PartUtil.getPartByNumber("HQ11100114000");
//		getAllBaselines(wtPart);
//		System.out.println("getAllBaselines>>>"+getAllBaselines(wtPart));
//		WTContainer wtContainer=WCUtil.getWTContainerByName("产品信息库");
//		System.out.println("	getAllBaselines(wtPart);>>>"+	getAllBaselines(wtPart));
//		getBaseline("测试基线01","A1511");
//		System.out.println("getBaseline>>>>>>;"+getBaseline("测试基线01","A1511"));
//		ManagedBaseline baseline =createBaseLine(wtPart,"测试基线06");
//		System.out.println("baseline>>>>>"+baseline);
//		BaselineHelper.service.addToBaseline(wtPart, getPartByNumber("HQ11120019000"));
//		BaselineHelper.service.removeFromBaseline(wtPart, getBaseline("测试基线01","A1511"));
//		baseline.setName("");
//		baseline.setNumber("");
//		WTDocument wtDocument= DocUtil.getDocumentByNumber("CPKF00000062");
//		addObjectForBaseline(wtDocument,getBaseline("测试基线05","A1511"));
//		removeObjectfromBaseline(wtDocument,getBaseline("测试基线05","A1511"));
	}
}
