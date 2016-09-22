package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.enterprise.RevisionControlled;
import wt.fc.IdentityHelper;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.fc.collections.WTArrayList;
import wt.fc.collections.WTCollection;
import wt.fc.collections.WTHashSet;
import wt.fc.collections.WTSet;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.maturity.MaturityBaseline;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.Promotable;
import wt.maturity.PromotionNotice;
import wt.maturity.PromotionNoticeIdentity;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.Iterated;
import wt.vc.baseline.BaselineHelper;

import com.google.gwt.rpc.client.impl.RemoteException;



public class PromotionUtil  implements RemoteAccess{
	private static String CLASSNAME = PromotionUtil.class.getName();
	private final static Logger logger = LogR.getLogger(PromotionUtil.class.getName());
	public static WTContainerRef getWTContainerref(WTContainer wtcontainer) throws WTException{
	    return WTContainerRef.newWTContainerRef(wtcontainer);
	}
	
   /**
     * @author bjj
	 * get PromotionNotice By Number
	 * 
	 * @param number
	 * @return
	 * 20160913
	 */
	public static PromotionNotice getPromotionNoticeByNumber(String number) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
					return (PromotionNotice) RemoteMethodServer.getDefault().invoke(
							"getPromotionNoticeByNumber", PromotionUtil.class.getName(), null,
							new Class[] { String.class }, new Object[] {number});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				PromotionNotice result = null;
				try {
					QuerySpec querySpec = new QuerySpec(PromotionNotice.class);       
					
					if (!StringUtils.isEmpty(number)) {                                           
						SearchCondition searchCondi = new SearchCondition(PromotionNotice.class,"number", SearchCondition.EQUAL, number);
						querySpec.appendWhere(searchCondi, new int[] { 0 });
					}
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
					if (qr.hasMoreElements()) {
						result = (PromotionNotice) qr.nextElement();
					} 
					
				} catch (WTException e) {
					logger.error(CLASSNAME+".getPromotionNoticeByNumber:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			    return result;
			}
		} catch (java.rmi.RemoteException | InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}	
	  return null;
	}
		
	@SuppressWarnings("deprecation")
	public static PromotionNotice createPromotionRequest(String number, String name, String objType, String desciption, WTContainer container,
		  Folder folder, String targetState, Map<String, Object> mbaMap)throws WTException{
	   try {  
            if (!RemoteMethodServer.ServerFlag) {   
                return (PromotionNotice) RemoteMethodServer.getDefault().invoke("createPromotionRequest", UserUtil.class.getName(), null,   
                        new Class[] {String.class,String.class,String.class,String.class,WTContainer.class,Folder.class,String.class,Map.class},   
                        new Object[] {number,name,objType,desciption,container,folder,targetState,mbaMap});   
            } else {   
                boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
			    PromotionNotice promotion = null;
			    try {
			      promotion = PromotionNotice.newPromotionNotice(name);
			
			      if (StringUtils.isNotBlank(number)) {
			        promotion.setNumber(number);
			      }
			
			      promotion.setName(name);
			
			      if (StringUtils.isNotBlank(objType)) {
			        TypeDefinitionReference typeDefinitionRef = TypedUtility.getTypeDefinitionReference(objType);
			        promotion.setTypeDefinitionReference(typeDefinitionRef);
			      }
			
			      promotion.setContainer(container);
			
			      if (StringUtils.isNotBlank(desciption)) {
			        promotion.setDescription(desciption);
			      }
			
			      String folderpath = "";
			      if (folder == null) {
			        folderpath = "/Default";
			        folder = FolderUtil.getFolder(getWTContainerref(container), folderpath);
			      }
			      if (folder != null) {
			        FolderHelper.assignLocation(promotion, folder);
			      }
			
			      if (StringUtils.isNotBlank(targetState)) {
			        promotion.setMaturityState(State.toState(targetState));
			      }
			
			      MaturityBaseline maturityBaseline = MaturityBaseline.newMaturityBaseline();
			      maturityBaseline.setContainer(container);
			      maturityBaseline = (MaturityBaseline)PersistenceHelper.manager.save(maturityBaseline);
			      promotion.setConfiguration(maturityBaseline);
			      promotion = MaturityHelper.service.savePromotionNotice(promotion);
			      promotion = (PromotionNotice)PersistenceHelper.manager.refresh(promotion);
			      if (mbaMap != null) {
			        MBAUtil.setObjectValue(promotion, mbaMap);
			      }
			    } catch (Exception e) {
					logger.error(CLASSNAME+".createPromotionRequest:" + e);
			    } finally {
				    SessionServerHelper.manager.setAccessEnforced(enforce);
			    }
			    return promotion;
            }
            } catch (Exception e) {   
        	   	logger.error(e.getMessage(),e);   
	        }   
		return null;  
	  }
			  
	public static PromotionNotice addPromotable(PromotionNotice promotion, WTArrayList promotableList) throws WTException{
	  try {
			if (!RemoteMethodServer.ServerFlag) {   
			      return (PromotionNotice) RemoteMethodServer.getDefault().invoke("addPromotable", PromotionUtil.class.getName(), null,   
			              new Class[] {PromotionNotice.class,WTArrayList.class}, new Object[] {promotion,  promotableList });   
			 } else {   
				    boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				    try {
				    	if(promotion != null && isPromotionNoticeExist(promotion.getNumber())&& promotableList != null){
							MaturityBaseline maturityBaseline = null;
							maturityBaseline = promotion.getConfiguration();
							System.out.println("~~~~~1~~~"+maturityBaseline);
							maturityBaseline = (MaturityBaseline)BaselineHelper.service.addToBaseline(promotableList, maturityBaseline);
							promotion.setConfiguration(maturityBaseline);
							PersistenceHelper.manager.save(promotion);
							
							WTSet promotableSet = new WTHashSet();
							promotableSet.addAll(promotableList);
							MaturityHelper.service.savePromotionTargets(promotion, promotableSet);
							promotion = (PromotionNotice)PersistenceHelper.manager.refresh(promotion);
				    	}
				    } catch (WTPropertyVetoException e) {
				    	logger.error(CLASSNAME+"."+"addPromotable"+":"+e);
				    } catch (WTException e) {
				    	logger.error(CLASSNAME+"."+"addPromotable"+":"+e);
				    } finally {
				        SessionServerHelper.manager.setAccessEnforced(enforce);
				    }
				    return promotion;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}


	
	 /** 
	  * @author bjj
	  * Find the associated propulsion process to find related promotion
	  * @param epm 
	  * return 
	  */ 
	 @SuppressWarnings("unchecked")
	public static Vector<PromotionNotice> findRelatedPromotion(RevisionControlled epm) {  
		 try {
			if (!RemoteMethodServer.ServerFlag) {   
			      return (Vector<PromotionNotice>) RemoteMethodServer.getDefault().invoke("findRelatedPromotion", PromotionUtil.class.getName(), null,   
			              new Class[] {RevisionControlled.class}, new Object[] { epm });   
			 } else {
				 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				 Vector<PromotionNotice> vector = new Vector<PromotionNotice>();
				 if(epm != null){
					  WTHashSet set = new WTHashSet(); 
					  set.add(epm); 
					  WTCollection collection;   
					  try {    
						  collection = MaturityHelper.service.getPromotionNotices(set);    
						  Iterator<?> iterator = collection.iterator();    
						  while (iterator.hasNext()) {     
							  ObjectReference objRef = (ObjectReference) iterator.next();     
							  PromotionNotice pn = (PromotionNotice) objRef.getObject();     
							  vector.add(pn); 
						    System.out.println("==2==" + pn); 
						   
						 } 
					 } catch (WTException e) {    
						 logger.error(CLASSNAME+"."+"findRelatedPromotion"+":"+e);  
					 }finally{
						 SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					 } 
				 }
				 return vector; 
			 }
		} catch (java.rmi.RemoteException e) {
			  logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			  logger.error(e.getMessage(),e);
		} catch (WTRuntimeException e) {
			  logger.error(e.getMessage(),e);
		}
		return null;
	 } 
	

		
	 public static void renamePromotion (String newName, PromotionNotice promotion){
		 try {
			 if (!RemoteMethodServer.ServerFlag) {
				  RemoteMethodServer.getDefault().invoke("renamePromotion", PromotionUtil.class.getName(), null,   
				        new Class[] {String.class, PromotionNotice.class}, new Object[] { newName, promotion });
			 } else {
				 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				 if( promotion != null){
					 String proNumber = promotion.getNumber();
					 if(!StringUtils.isEmpty(newName) && isPromotionNoticeExist(proNumber)){
						try {
							 PromotionNoticeIdentity promotionidentity = (PromotionNoticeIdentity) promotion.getIdentificationObject();
							 promotionidentity.setName(newName);
							 PromotionNotice idw = (PromotionNotice)IdentityHelper.service.changeIdentity(promotion, promotionidentity);
							 refreshPromotion(idw);
							 
						} catch (WTException e) {
							logger.error(CLASSNAME+"."+"renamePromotion"+":"+e);  
						}finally{
							SessionServerHelper.manager.setAccessEnforced(accessEnforced);
						}
				     }
				 }
		  }
		} catch (java.rmi.RemoteException e) {
			  logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			  logger.error(e.getMessage(),e);
		} catch (WTPropertyVetoException e) {
			  logger.error(e.getMessage(),e);
		}
		 
	 }
	 
	public static boolean isPromotionNoticeExist (String proNumber){
		
		try {
			if (!RemoteMethodServer.ServerFlag) {
				  RemoteMethodServer.getDefault().invoke("isPromotionNoticeExist", PromotionUtil.class.getName(), null,   
				        new Class[] {String.class}, new Object[] { proNumber });
			 } else {
				boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					QuerySpec querySpec = new QuerySpec(PromotionNotice.class);                  
					if (!StringUtils.isEmpty(proNumber)) {                                           
						SearchCondition searchCondi = new SearchCondition(PromotionNotice.class,"number", SearchCondition.EQUAL, proNumber);
						querySpec.appendWhere(searchCondi, new int[] { 0 });
					}
					QueryResult qr = PersistenceHelper.manager.find((StatementSpec) querySpec);
					if (qr.hasMoreElements()) {
						PromotionNotice result = (PromotionNotice) qr.nextElement();
						return true;
					} 
				} catch (Exception e) {
					logger.error(CLASSNAME+".isPromotionNoticeExist:" + e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
			}
		} catch (java.rmi.RemoteException e) {
            logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
            logger.error(e.getMessage(),e);
		}
		return false;
		
	}
	 
	public static void delectPromotionNotice(PromotionNotice promotion){

		try {
			if (!RemoteMethodServer.ServerFlag) {
				  RemoteMethodServer.getDefault().invoke("delectPromotionNotice", PromotionUtil.class.getName(), null,   
				        new Class[] {PromotionNotice.class}, new Object[] { promotion });
			 } else {
				 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
				 try {
					 if(promotion != null){
						if(isPromotionNoticeExist(promotion.getNumber())){
						    PersistenceHelper.manager.delete(promotion);
						}
					 }
				} catch (Exception e) {
					logger.error(CLASSNAME+".delectPromotionNotice:" + e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(accessEnforced);
				}
			 }
		} catch (java.rmi.RemoteException e) {
            logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
            logger.error(e.getMessage(),e);
		}
	}
	
	 @SuppressWarnings("deprecation")
	 public static void refreshPromotion(WTObject pbo) throws MaturityException, WTException, WTPropertyVetoException{
		 try {
			if (!RemoteMethodServer.ServerFlag) {
				     RemoteMethodServer.getDefault().invoke("refreshPromotion", PromotionUtil.class.getName(), null,   
				        new Class[] {WTObject.class}, new Object[] { pbo });
			 } else {
				 boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);	
				 try {
				
				 if (pbo instanceof PromotionNotice) {
						PromotionNotice promotion = (PromotionNotice) pbo;
						
						List<Promotable> needToAdd = new ArrayList<Promotable>();
						List<Promotable> needToRemove = new ArrayList<Promotable>();
						QueryResult targets = MaturityHelper.service.getPromotionTargets(promotion);
						while (targets.hasMoreElements()) {
							Object object = (Object) targets.nextElement();
							if(object instanceof Iterated){
								Iterated iter = (Iterated) object;
								boolean flag = iter.isLatestIteration();
								if(!flag){
									needToRemove.add((Promotable)iter);
									Persistable latestIter = VersionUtil.getLatestByMaster(iter.getMaster());
									needToAdd.add((Promotable)latestIter);
								}
							}
						}
						
						WTSet promotableSet = new WTHashSet(1);
			           Vector<Promotable> seedVec = new Vector<Promotable>();
			           MaturityBaseline maturityBaseline = null;
			           if(needToAdd.size() > 0){
			           	seedVec.addAll(needToAdd);
			           	maturityBaseline = promotion.getConfiguration();
			           	maturityBaseline = (MaturityBaseline) BaselineHelper.service.addToBaseline(seedVec, maturityBaseline);
			           	promotion.setConfiguration(maturityBaseline);
			           	PersistenceHelper.manager.save(promotion);
			           	promotableSet.addAll(needToAdd);
			           	MaturityHelper.service.savePromotionTargets(promotion, promotableSet);
			           	promotion = (PromotionNotice) PersistenceHelper.manager.refresh(promotion);
			           }
			           
			           if(needToRemove.size() > 0){
			           	promotableSet = new WTHashSet(1);
			           	seedVec = new Vector<Promotable>();
			           	maturityBaseline = null;
			           	seedVec.addAll(needToRemove);
			           	maturityBaseline = promotion.getConfiguration();
			           	maturityBaseline = (MaturityBaseline) BaselineHelper.service.removeFromBaseline(seedVec,maturityBaseline);
			           	promotion.setConfiguration(maturityBaseline);
			           	PersistenceHelper.manager.save(promotion);
			           	promotableSet.addAll(needToRemove);
			           	MaturityHelper.service.deletePromotionTargets(promotion, promotableSet);
			           	promotion = (PromotionNotice) PersistenceHelper.manager.refresh(promotion);
			           }
					}
					} catch (Exception e) {
						logger.error(CLASSNAME+".refreshPromotion:" + e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(accessEnforced);
					}
			 }
		} catch (java.rmi.RemoteException e) {
			 logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			 logger.error(e.getMessage(),e);
		}
	 }
	
/*	public static PromotionNotice updatePromotable(PromotionNotice promotion, WTArrayList promotableList) throws WTException{
		
		return promotion;
	}
	
	public static PromotionNotice ReassignLifecyleByRevisionControlled(PromotionNotice promotion, String lifecyleName){
		
		try {
			if(promotion != null && lifecyleName != null){
				LifeCycleTemplateReference lifecyleTemRef = LifeCycleHelper.service.getLifeCycleTemplateReference(lifecyleName);
				System.out.println(lifecyleTemRef);
				promotion = (PromotionNotice) LifeCycleHelper.service.reassign(promotion, lifecyleTemRef);
				if(promotion != null){
					Log.info("Reassign lifecyle successed!");
				}
			}
			try {
				ReferenceFactory rf = new ReferenceFactory();
			    Persistable paramLifeCycleTemplate = rf.getReference(lifecyleName).getObject();
				LifeCycleHelper.setLifeCycle(promotion, (LifeCycleTemplate) paramLifeCycleTemplate );
			} catch (WTPropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		} catch (LifeCycleException e) {
			logger.error(CLASSNAME+".delectPromotionNotice:" + e);
		} catch (WTException e) {
			logger.error(CLASSNAME+".delectPromotionNotice:" + e);
		}
		
		return promotion;
		
	}*/
	
	 
	 public static <T> void test() throws RemoteException,InvocationTargetException, WTException  {  
	
	  }
	
	  public static void main(String[] args)throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException
	  {
	    if (!RemoteMethodServer.ServerFlag)
	      try {
	    	    RemoteMethodServer server = RemoteMethodServer.getDefault();
				server.setUserName("wcadmin");
				server.setPassword("wcadmin");
	            RemoteMethodServer.getDefault().invoke("test", PromotionUtil.class.getName(), null, new Class[0], new Object[0]);
	      }
	      catch (java.rmi.RemoteException e)
	      {
	        e.printStackTrace();
	      }
	  }
	  
}