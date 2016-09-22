package cn.elead.tool.wc;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.Persistable;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;

	/**
	 * 
	 * @author zhangxj
	 * @version
	 *
	 */
	public class ConstraintUtil implements RemoteAccess, Serializable{
		private static String CLASSNAME = BaseLineUtil.class.getName();
		private static Logger logger = LogR.getLogger(CLASSNAME);
	    /**
	     * 判断是否为大写
	     * @param p
	     * @param key
	     * @return
	     * @throws WTException
	     */
	    public static  boolean isUppercaseAttributesValue(Persistable p, String key) throws WTException{
	    	try {
				if (!RemoteMethodServer.ServerFlag) {
					return (boolean) RemoteMethodServer.getDefault().invoke("isUppercaseAttributesValue",ConstraintUtil.class.getName(),null,new Class[] { Persistable.class,String.class},
							new Object[] { p,key });
				}else {
					boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
					boolean falg = true;
						try {
							if(!StringUtils.isEmpty(key)){
								String ibaValue =(String) WCUtil.getMBAValue(p,key);
								for(int i = 0;i<ibaValue.length();i++){
									char c = ibaValue.charAt(i);
									if(c<'A' || c>'Z'){
										falg= false;
										break;
									}
								}
								
							}
						} catch (Exception e) {
							logger.error(CLASSNAME+".isUppercaseAttributesValue:"+e);
						}finally{
							SessionServerHelper.manager.setAccessEnforced(foronce);
						}
					return falg;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return true;
	    }
	    /**
	     * 判断是否为小写
	     * @param p
	     * @param key
	     * @return
	     * @throws WTException
	     */
	    public static  boolean isLowerCaseAttributesValue(Persistable p, String key) throws WTException{
	    	try {
				if (!RemoteMethodServer.ServerFlag) {
					return (boolean) RemoteMethodServer.getDefault().invoke("isUppercaseAttributesValue",ConstraintUtil.class.getName(),null,new Class[] { Persistable.class,String.class},
							new Object[] { p,key });
				}else {
					boolean foronce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
					boolean falg = true;
					try {
						if(!StringUtils.isEmpty(key)
								){
							String ibaValue =(String)WCUtil. getMBAValue(p,key);
							for(int i = 0;i<ibaValue.length();i++){
								char c = ibaValue.charAt(i);
								if(c<'a' || c>'z'){
									falg= false;
									break;
								}
							}
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+".isUppercaseAttributesValue:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(foronce);
					}
					return falg;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return true;
	    }
	    
	    /**
	     * 判断属性值是否有值。
	     * @param p
	     * @param key
	     * @return
	     * @throws WTException
	     */
	    public static  boolean isNullAttributesValue(Persistable p, String key) throws WTException{
	    	try {
				if (!RemoteMethodServer.ServerFlag) {
				return	(boolean) RemoteMethodServer.getDefault().invoke("isUppercaseAttributesValue",ConstraintUtil.class.getName(),null,new Class[] { Persistable.class,String.class},
							new Object[] { p,key });
				}else {
					boolean enforonce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
					boolean falg = false;
					try {
						if(!StringUtils.isEmpty(key)){
							String ibaValue =(String)WCUtil. getMBAValue(p,key);
							if(ibaValue==null||"".equals(ibaValue)){
								falg=true;
							}
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+".isNullAttributesValue:"+e);
					}finally{
						SessionServerHelper.manager.setAccessEnforced(enforonce);
					}
					return falg;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return false;
	    }
	    
	    /**
	     * 判断是否为枚举值。
	     * @param systemEnumName
	     * @param value
	     * @return
	     */
	    public static boolean isEnumValue(String systemEnumName, String value){
	    	try {
				if (!RemoteMethodServer.ServerFlag) {
				return	(boolean) RemoteMethodServer.getDefault().invoke("isEnumValue",ConstraintUtil.class.getName(),null,new Class[] { String.class,String.class},
							new Object[] { systemEnumName,value });
				} else {
					boolean enforonce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
					boolean falg = false;
					try {
						if(!StringUtils.isEmpty(systemEnumName) && !StringUtils.isEmpty(value)){
							List<String >list = EnumUtil.getEnumerationItemDisValue(systemEnumName);
							if(list.contains(value)){
								falg= true;
							}
						}
					} catch (Exception e) {
						logger.error(CLASSNAME+".isNullAttributesValue:"+e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforonce);
					}
					return falg;
				}
			} catch (RemoteException e) {
				logger.error(e.getMessage(),e);
			} catch (InvocationTargetException e) {
				logger.error(e.getMessage(),e);
			}
			return false;
	    }
	    
		public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
			RemoteMethodServer r = RemoteMethodServer.getDefault();
			r.setUserName("wcadmin");
			r.setPassword("wcadmin");
			if (!RemoteMethodServer.ServerFlag) {
				RemoteMethodServer.getDefault().invoke("test", ConstraintUtil.class.getName(), null,new Class[] {},new Object[] {});
			}
		}
		
		public static void test() throws WTException{
			WTPart wtPart= PartUtil.getPartByNumber("HQ11100253000");
//			getMBAValue(wtPart,"HQ_SPECIAL_MARK");
//			System.out.println("getMBAValue>>>"+getMBAValue(wtPart,"name"));
//			boolean falg= 	isUppercaseAttributesValue(wtPart,"name");
//			System.out.println(falg);
//			boolean falg1=isLowerCaseAttributesValue(wtPart,"name");
//			System.out.println(falg1);
//			String ibaValue =(String) WCUtil.getMBAValue(wtPart,"HQ_PRODUCT_TYPE");
//			System.out.println(ibaValue);
//			boolean falg = isEnumValue("HQ_PRODUCT_TYPE",ibaValue);
//			System.out.println(falg);
		}
	}
