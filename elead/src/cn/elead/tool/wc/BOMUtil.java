package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.log4j.Logger;

import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.part.WTPartAlternateLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartSubstituteLink;
import wt.part.WTPartUsageLink;
import wt.session.SessionServerHelper;
import wt.util.WTException;



	/**
	 *  建立Part 之间的关系， 全局替代， 特定替代，
	 * @author zhangxj
	 * @version
	 *
	 */

	public class BOMUtil implements RemoteAccess{
		private static  String CLASSNAME = PartUtil.class.getName();
		private static Logger logger = LogR.getLogger(CLASSNAME);
		
		/**
		 * 取得part 的替代
		 * @author zhangxj
		 * @param partMaster
		 * @return
		 */

		public static Vector getAlternatesLink(WTPartMaster partMaster){
			try {
					if (!RemoteMethodServer.ServerFlag) {
						return (Vector) RemoteMethodServer.getDefault().invoke("getAlternatesLink", BOMUtil.class.getName(), null,
								new Class[] { WTPartMaster.class }, new Object[] { partMaster });
			        
				}else {
					boolean  enforce = SessionServerHelper.manager.setAccessEnforced(false);
					Vector deVE = new Vector();
					try {
						QueryResult queryresult = WTPartHelper.service.getAlternateForWTPartMasters(partMaster);//全局替代用于的零件
						if(queryresult != null && queryresult.size() > 0){
							while(queryresult.hasMoreElements()){
								WTPartAlternateLink alertnateLink=(WTPartAlternateLink)queryresult.nextElement();
//				 WTPartMaster master = alertnateLink.getAlternates();
								WTPartMaster master = alertnateLink.getAlternateFor();
								deVE.add(master);
							}
						}
					} catch (WTException e) {
						logger.error(CLASSNAME+".getAlternatesLink："+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return deVE;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
		}
		
		/**
		 * 建立Party 全局的替代
		 * @author zhangxj
		 * @param part
		 * @param alertPart
		 * @return
		 */

		public static WTPartAlternateLink createAlternatePart(WTPart part,WTPart alertPart){
			try {
				if(!RemoteMethodServer.ServerFlag){
					 return (WTPartAlternateLink) RemoteMethodServer.getDefault().invoke("createAlternatePart", BOMUtil.class.getName(), null, new  Class[]{WTPart.class ,WTPart.class}, new Object[]{part,alertPart});
				}else {
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					WTPartAlternateLink alertnateLink = null;
					try {
						if(part!=null&&alertPart!=null){
							WTPartMaster partMaster = (WTPartMaster) part.getMaster();
							WTPartMaster alertPartMaster = (WTPartMaster) alertPart.getMaster();
							Vector alertVE = getAlternatesLink(partMaster);
							if(!alertVE.contains(alertPartMaster)){
								alertnateLink =WTPartAlternateLink.newWTPartAlternateLink(partMaster,alertPartMaster);
								PersistenceServerHelper.manager.insert(alertnateLink);  
								return alertnateLink;
							}
						}
					} catch (WTException e) {
					logger.error(CLASSNAME+".createAlternatePart:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return null;
			}
		/**
		 * getSubstituteLinkby usageLink
		 * @author zhangxj
		 * @param usageLink
		 * @return
		 * @throws WTException
		 */

		public static Vector getSubstituteLink(WTPartUsageLink usageLink) throws WTException{
			try {
				if(RemoteMethodServer.ServerFlag){
					return (Vector) RemoteMethodServer.getDefault().invoke("getSubstituteLink", BOMUtil.class.getName(), null, new Class []{WTPartSubstituteLink.class},new Object []{usageLink});
				}else{
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
							Vector deVE = new Vector();
					try {
					QueryResult queryresult=WTPartHelper.service.getSubstitutesWTPartMasters(usageLink);
					if(queryresult != null && queryresult.size() > 0){
						while(queryresult.hasMoreElements()){
							WTPartSubstituteLink WTPartSubstituteLink = 
									(WTPartSubstituteLink)queryresult.nextElement();
							WTPartMaster master = WTPartSubstituteLink.getSubstitutes();
							deVE.add(master);
						}
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".getSubstituteLink:"+e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
					return deVE;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
				e.printStackTrace();
			}
			return null;
			}

		/**
		 * 创建特定替代
		 * @author zhangxj
		 * @param usageLink
		 * @param substitutePart
		 * @return
		 * @throws WTException
		 */
		public static WTPartSubstituteLink createSubstitutePart(WTPartUsageLink usageLink,WTPart substitutePart) throws WTException{
			try {
				if(RemoteMethodServer.ServerFlag){
					return (WTPartSubstituteLink) RemoteMethodServer.getDefault().invoke("createSubstitutePart", BOMUtil.class.getName(), null, new Class[]{WTPartSubstituteLink.class,WTPart.class}, 
							new Object[]{usageLink,substitutePart});
				}else {
					boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
					WTPartSubstituteLink substituteLink = null;
					try {
						if(usageLink!=null&&substitutePart!=null){
							WTPartMaster substitutePartMaster = (WTPartMaster) substitutePart.getMaster();
							Vector alertVE = getSubstituteLink(usageLink);
							if(!alertVE.contains(substitutePartMaster)){
								substituteLink = 
										WTPartSubstituteLink.newWTPartSubstituteLink(usageLink,substitutePartMaster);
								PersistenceServerHelper.manager.insert(substituteLink);  
							}
						}
					} catch (Exception e) {
					logger.error(CLASSNAME+".createSubstitutePart:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return substituteLink;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
					return null;
				}
		public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
			RemoteMethodServer r = RemoteMethodServer.getDefault();
			r.setUserName("wcadmin");
			r.setPassword("wcadmin");
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("test", BOMUtil.class.getName(), null,
						new Class[] {},
						new Object[] {});
			}
		}
		public static void test() throws WTException{
			WTPart wtPart1=PartUtil.getPartByNumber("DFGHJ");
			WTPart wtPart2=PartUtil.getPartByNumber("HQ11110018000");
			WTPartMaster wtPartMaster= PartUtil.getPartMasterByNumber("HQ11100038000");
			WTPartUsageLink usageLink=PartUtil.getPartUsageLink(wtPart1, wtPartMaster);
//			WTPartMaster wtPart2=PartUtil.getPartMasterByNumber("HQ11120046000");
//			PartUtil.createUsageLink(wtPart1,wtPart2);
//			getAlternatesLink(wtPartMaster);
//			System.out.println("getAlternatesLink(wtPartMaster)>>>>>>>"+getAlternatesLink(wtPartMaster));
//			createAlternatePart(wtPart1,wtPart2);
//			System.out.println("getAlternatesLink(wtPartMaster)>>>>>>>"+getAlternatesLink(wtPartMaster));
			getSubstituteLink(usageLink);
			System.out.println("getSubstituteLink(usageLink);>>>"+getSubstituteLink(usageLink));
			createSubstitutePart(usageLink,wtPart2);
			
		}
	}
