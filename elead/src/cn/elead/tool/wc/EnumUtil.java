package cn.elead.tool.wc;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import com.ptc.core.lwc.server.LWCEnumerationEntry;
import com.ptc.core.lwc.server.LWCEnumerationEntryLink;
import com.ptc.core.lwc.server.LWCLocalizablePropertyValue;
import com.ptc.core.lwc.server.LWCMasterEnumerationDefinition;
import com.ptc.core.lwc.server.LWCOrganizer;
import com.ptc.core.lwc.server.LWCPropertyDefinition;
import com.ptc.core.lwc.server.LWCPropertyValue;

public class EnumUtil implements RemoteAccess {
	private static String CLASSNAME = EnumUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	public static final String INNERNAME = "innerName";
	public static final String DISPLYNAME = "displyName";
	
	/**
	 * judge whether exist library in windChill
	 * @param name
	 * @return		if name is exist,return true
	 * 				else if name is not exist,name is empty or name is null,return false
	 */
	@SuppressWarnings("deprecation")
	public static boolean isEnumerationExist(String name) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	return (boolean)RemoteMethodServer.getDefault().invoke("isEnumerationExist", EnumUtil.class.getName(), null,
	            			new Class[] { String.class }, new Object[] { name });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	if (!StringUtils.isEmpty(name)) {
		        	try {
		        		QuerySpec queryspec = new QuerySpec(LWCMasterEnumerationDefinition.class);
				        queryspec.appendWhere(new SearchCondition(LWCMasterEnumerationDefinition.class, LWCMasterEnumerationDefinition.NAME, SearchCondition.EQUAL, name));
				        QueryResult queryResult = PersistenceHelper.manager.find(queryspec);
				        if (queryResult.hasMoreElements()) {
				        	flag = true;
				        }
		        	} catch(WTException e) {
		        		logger.error(CLASSNAME+".isEnumerationExist:"+e);
		        	} finally {
		        		SessionServerHelper.manager.setAccessEnforced(enforce);
		        	}
	        	}
	        	return flag;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	
	/**
	 * get Enumeration by name
	 * @param name
	 * @return		if name is exist in windChill,return LWCMasterEnumerationDefinition object
	 * 				else if name is not exist,return null
	 * 				else if name is empty or name is null,return null
	 */
	@SuppressWarnings("deprecation")
	public static LWCMasterEnumerationDefinition getEnumerationByName(String name) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (LWCMasterEnumerationDefinition) RemoteMethodServer.getDefault().invoke("getEnumerationByName", EnumUtil.class.getName(), null, 
						new Class[] { String.class}, new Object[] { name });
	        } else {
	        	LWCMasterEnumerationDefinition enumeration = null;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (!StringUtils.isEmpty(name) && isEnumerationExist(name)) {
		        		QuerySpec criteria = new QuerySpec(LWCMasterEnumerationDefinition.class);
				        criteria.appendWhere(new SearchCondition(LWCMasterEnumerationDefinition.class, LWCMasterEnumerationDefinition.NAME, SearchCondition.LIKE, name, false));
				        QueryResult results = PersistenceHelper.manager.find(criteria);
				        if (results.hasMoreElements()) {
				        	enumeration = (LWCMasterEnumerationDefinition) results.nextElement();
				        }
		        	}
				} catch(WTException e) {
					logger.error(CLASSNAME+".getEnumerationByName:"+e);
				} finally {
	                SessionServerHelper.manager.setAccessEnforced(enforce);
	            }
		        return enumeration;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
        return null;
    }
	
	/**
	 * create enumeration
	 * @param newEnumName
	 * @param containerRef
	 * @param org
	 * @return 		if newEnumName is not exist in windChill and containerRef,org all are not null,create enumeration
	 * 				else if newEnumName is exist in windChill,return this enumeration
	 * 				else if newEnumName is empty or null and containerRef is null,org is null,return null
	 */
	public static LWCMasterEnumerationDefinition createEnumeration(String newEnumName, WTContainerRef containerRef,LWCOrganizer org) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (LWCMasterEnumerationDefinition) RemoteMethodServer.getDefault().invoke("createEnumeration", EnumUtil.class.getName(), null, 
						new Class[] { String.class, WTContainerRef.class, LWCOrganizer.class }, new Object[] { newEnumName, containerRef, org });
	        } else {
	        	LWCMasterEnumerationDefinition enumeration = getEnumerationByName(newEnumName);
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if (StringUtils.isEmpty(newEnumName) || containerRef == null || org == null) {
						return null;
					}
					if (enumeration != null) {
						return enumeration;
					}
					enumeration = LWCMasterEnumerationDefinition.newLWCMasterEnumerationDefinition();
					enumeration.setName(newEnumName);//设置名字
					enumeration.setOrganizationScope(containerRef);
					enumeration.setOrganizer(org);
					PersistenceHelper.manager.save(enumeration);
				} catch(WTException e) {
					logger.error(CLASSNAME+".createEnumeration:"+e);
	        	} catch(WTPropertyVetoException e) {
	        		logger.error(CLASSNAME+".createEnumeration:"+e);
	        	} finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
				return enumeration;
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        }
		return null;
	}
	
	/**
	 * judge whether enum has item in windChill by enumName
	 * @param name
	 * @return		if name is exist,return true
	 * 				else if name is not exist,name is empty or name is null,return false
	 */
	@SuppressWarnings("deprecation")
	public static boolean isEnumHasItem(String name) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	return (boolean)RemoteMethodServer.getDefault().invoke("isEnumHasItem", EnumUtil.class.getName(), null,
	            			new Class[] { String.class }, new Object[] { name });
	        } else {
	        	boolean flag = false;
	        	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
	        	if (StringUtils.isBlank(name) && !isEnumerationExist(name)) {
	            	return false;
	            }
	        	try {
	        		QuerySpec qs = new QuerySpec();
	                int enumerentry = qs.appendClassList(LWCEnumerationEntry.class, true);
	                int enumvalue = qs.appendClassList(LWCLocalizablePropertyValue.class, true);
	                int enumlink = qs.appendClassList(LWCEnumerationEntryLink.class, false);
	                int enumpv = qs.appendClassList(LWCPropertyValue.class, false);
	                int enummaster = qs.appendClassList(LWCMasterEnumerationDefinition.class, false);
	                int pd = qs.appendClassList(LWCPropertyDefinition.class, false);
	                SearchCondition eq_link1 = new SearchCondition(LWCMasterEnumerationDefinition.class, WTAttributeNameIfc.ID_NAME,
	                		LWCEnumerationEntryLink.class, "enumerationReference.key.id");
	                SearchCondition ev_link1 = new SearchCondition(LWCPropertyValue.class,"holderReference.key.id",
	                        LWCEnumerationEntryLink.class,"thePersistInfo.theObjectIdentifier.id");
	                SearchCondition eq_link2 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
	                        LWCEnumerationEntryLink.class,"entryReference.key.id");
	                SearchCondition eq_value3 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
	                        LWCLocalizablePropertyValue.class,"holderReference.key.id");
	                SearchCondition eq_pd = new SearchCondition(LWCLocalizablePropertyValue.class,"propertyReference.key.id",
	                        LWCPropertyDefinition.class,WTAttributeNameIfc.ID_NAME);            
	                SearchCondition eq_enumername = new SearchCondition(LWCMasterEnumerationDefinition.class,LWCMasterEnumerationDefinition.NAME,
	                        SearchCondition.EQUAL,name);
	                qs.appendWhere(eq_link1, new int[]{enummaster,enumlink});
	                qs.appendAnd();
	                qs.appendWhere(ev_link1, new int[]{enumpv,enumlink});
	                qs.appendAnd();
	                qs.appendWhere(eq_link2,new int[]{enumerentry,enumlink});
	                qs.appendAnd();
	                qs.appendWhere(eq_value3,new int[]{enumerentry,enumvalue});
	                qs.appendAnd();
	                qs.appendWhere(eq_pd,new int[]{enumvalue,pd});
	            	qs.appendAnd();
	                qs.appendWhere(eq_enumername,new int[]{enummaster});
	                qs.appendOrderBy(LWCPropertyValue.class, "value", false);
	                QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
	                if (qr.hasMoreElements()) {
	                    flag = true;
	                }
	        	} catch(WTException e) {
	        		logger.error(CLASSNAME+".isEnumHasItem:"+e);
	        	} finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
	        	return flag;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}
	
	/**
	 * Gets all the items of the enumeration by enumerName,contains innerName and displyName
	 * @param enumerName
	 * @return
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static List<Map<String,String>> getEnumerationItem(String enumerName) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	return (List<Map<String,String>>)RemoteMethodServer.getDefault().invoke("getEnumerationItem", EnumUtil.class.getName(), null,
	            			new Class[] { String.class }, new Object[] { enumerName });
	        } else {
				List<Map<String,String>> result = new ArrayList<Map<String,String>>();
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (StringUtils.isBlank(enumerName) || !isEnumerationExist(enumerName) || !isEnumHasItem(enumerName)) {
			        	return result;
			        }
			        QuerySpec qs = new QuerySpec();
			        int enumerentry = qs.appendClassList(LWCEnumerationEntry.class, true);
			        int enumvalue = qs.appendClassList(LWCLocalizablePropertyValue.class, true);
			        int enumlink = qs.appendClassList(LWCEnumerationEntryLink.class, false);
			        int enumpv = qs.appendClassList(LWCPropertyValue.class, false);
			        int enummaster = qs.appendClassList(LWCMasterEnumerationDefinition.class, false);
			        int pd = qs.appendClassList(LWCPropertyDefinition.class, false);
			        SearchCondition eq_link1 = new SearchCondition(LWCMasterEnumerationDefinition.class, WTAttributeNameIfc.ID_NAME,
			        		LWCEnumerationEntryLink.class, "enumerationReference.key.id");
			        SearchCondition ev_link1 = new SearchCondition(LWCPropertyValue.class,"holderReference.key.id",
			                LWCEnumerationEntryLink.class,"thePersistInfo.theObjectIdentifier.id");
			        SearchCondition eq_link2 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
			                LWCEnumerationEntryLink.class,"entryReference.key.id");
			        SearchCondition eq_value3 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
			                LWCLocalizablePropertyValue.class,"holderReference.key.id");
			        SearchCondition eq_pd = new SearchCondition(LWCLocalizablePropertyValue.class,"propertyReference.key.id",
			                LWCPropertyDefinition.class,WTAttributeNameIfc.ID_NAME);            
			        SearchCondition eq_enumername = new SearchCondition(LWCMasterEnumerationDefinition.class,LWCMasterEnumerationDefinition.NAME,
			                SearchCondition.EQUAL,enumerName);
			        qs.appendWhere(eq_link1, new int[]{enummaster,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(ev_link1, new int[]{enumpv,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(eq_link2,new int[]{enumerentry,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(eq_value3,new int[]{enumerentry,enumvalue});
			        qs.appendAnd();
			        qs.appendWhere(eq_pd,new int[]{enumvalue,pd});
			    	qs.appendAnd();
			        qs.appendWhere(eq_enumername,new int[]{enummaster});
			        qs.appendOrderBy(LWCPropertyValue.class, "value", false);
			        QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
			        Object[] o = null;
			        LWCEnumerationEntry entry = null;
			        LWCLocalizablePropertyValue entryValue = null;
			        while (qr.hasMoreElements()) {
			            Map<String,String> entryMap = new HashMap<String,String>();
			            o =  (Object[])qr.nextElement();
			            entry = (LWCEnumerationEntry)o[enumerentry];
			            entryValue = (LWCLocalizablePropertyValue)o[enumvalue];
			            entryMap.put(INNERNAME, entry.getName());
			            entryMap.put(DISPLYNAME, entryValue.getValue());
			            result.add(entryMap);
			        }
		        } catch(WTException e) {
		        	logger.error(CLASSNAME+".getEnumerationItem:"+e);
		        } finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
		        return result;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * get enum item innerValue(innerName)
	 * @param enumerName
	 * @return
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static List<String> getEnumerationItemInnerValue(String enumerName) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	return (List<String>)RemoteMethodServer.getDefault().invoke("getEnumerationItemInnerValue", EnumUtil.class.getName(), null,
	            			new Class[] { String.class }, new Object[] { enumerName });
	        } else {
				List<String> list = new ArrayList<String>();
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (StringUtils.isBlank(enumerName) || !isEnumerationExist(enumerName) || !isEnumHasItem(enumerName)) {
			        	return list;
			        }
			        QuerySpec qs = new QuerySpec();
			        int enumerentry = qs.appendClassList(LWCEnumerationEntry.class, true);
			        int enumvalue = qs.appendClassList(LWCLocalizablePropertyValue.class, true);
			        int enumlink = qs.appendClassList(LWCEnumerationEntryLink.class, false);
			        int enumpv = qs.appendClassList(LWCPropertyValue.class, false);
			        int enummaster = qs.appendClassList(LWCMasterEnumerationDefinition.class, false);
			        int pd = qs.appendClassList(LWCPropertyDefinition.class, false);
			        SearchCondition eq_link1 = new SearchCondition(LWCMasterEnumerationDefinition.class, WTAttributeNameIfc.ID_NAME,
			        		LWCEnumerationEntryLink.class, "enumerationReference.key.id");
			        SearchCondition ev_link1 = new SearchCondition(LWCPropertyValue.class,"holderReference.key.id",
			                LWCEnumerationEntryLink.class,"thePersistInfo.theObjectIdentifier.id");
			        SearchCondition eq_link2 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
			                LWCEnumerationEntryLink.class,"entryReference.key.id");
			        SearchCondition eq_value3 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
			                LWCLocalizablePropertyValue.class,"holderReference.key.id");
			        SearchCondition eq_pd = new SearchCondition(LWCLocalizablePropertyValue.class,"propertyReference.key.id",
			                LWCPropertyDefinition.class,WTAttributeNameIfc.ID_NAME);            
			        SearchCondition eq_enumername = new SearchCondition(LWCMasterEnumerationDefinition.class,LWCMasterEnumerationDefinition.NAME,
			                SearchCondition.EQUAL,enumerName);
			        qs.appendWhere(eq_link1, new int[]{enummaster,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(ev_link1, new int[]{enumpv,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(eq_link2,new int[]{enumerentry,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(eq_value3,new int[]{enumerentry,enumvalue});
			        qs.appendAnd();
			        qs.appendWhere(eq_pd,new int[]{enumvalue,pd});
			    	qs.appendAnd();
			        qs.appendWhere(eq_enumername,new int[]{enummaster});
			        qs.appendOrderBy(LWCPropertyValue.class, "value", false);
			        QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
			        Object[] o = null;
			        LWCEnumerationEntry entry = null;
			        while (qr.hasMoreElements()) {
			            o =  (Object[])qr.nextElement();
			            entry = (LWCEnumerationEntry)o[enumerentry];
			            list.add(entry.getName());
			        }
		        } catch(WTException e) {
		        	logger.error(CLASSNAME+".getEnumerationItemInnerValue:"+e);
		        } finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
		        return list;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * get enumeration display name
	 * @param enumerName
	 * @return
	 */
	@SuppressWarnings({ "deprecation", "unchecked" })
	public static List<String> getEnumerationItemDisValue(String enumerName) {
		try {
	        if (!RemoteMethodServer.ServerFlag) {
	            	return (List<String>)RemoteMethodServer.getDefault().invoke("getEnumerationItemDisValue", EnumUtil.class.getName(), null,
	            			new Class[] { String.class }, new Object[] { enumerName });
	        } else {
				List<String> list = new ArrayList<String>();
				boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		        try {
		        	if (StringUtils.isBlank(enumerName) || !isEnumerationExist(enumerName) || !isEnumHasItem(enumerName)) {
			        	return list;
			        }
			        QuerySpec qs = new QuerySpec();
			        int enumerentry = qs.appendClassList(LWCEnumerationEntry.class, true);
			        int enumvalue = qs.appendClassList(LWCLocalizablePropertyValue.class, true);
			        int enumlink = qs.appendClassList(LWCEnumerationEntryLink.class, false);
			        int enumpv = qs.appendClassList(LWCPropertyValue.class, false);
			        int enummaster = qs.appendClassList(LWCMasterEnumerationDefinition.class, false);
			        int pd = qs.appendClassList(LWCPropertyDefinition.class, false);
			        SearchCondition eq_link1 = new SearchCondition(LWCMasterEnumerationDefinition.class, WTAttributeNameIfc.ID_NAME,
			        		LWCEnumerationEntryLink.class, "enumerationReference.key.id");
			        SearchCondition ev_link1 = new SearchCondition(LWCPropertyValue.class,"holderReference.key.id",
			                LWCEnumerationEntryLink.class,"thePersistInfo.theObjectIdentifier.id");
			        SearchCondition eq_link2 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
			                LWCEnumerationEntryLink.class,"entryReference.key.id");
			        SearchCondition eq_value3 = new SearchCondition(LWCEnumerationEntry.class,WTAttributeNameIfc.ID_NAME,
			                LWCLocalizablePropertyValue.class,"holderReference.key.id");
			        SearchCondition eq_pd = new SearchCondition(LWCLocalizablePropertyValue.class,"propertyReference.key.id",
			                LWCPropertyDefinition.class,WTAttributeNameIfc.ID_NAME);            
			        SearchCondition eq_enumername = new SearchCondition(LWCMasterEnumerationDefinition.class,LWCMasterEnumerationDefinition.NAME,
			                SearchCondition.EQUAL,enumerName);
			        qs.appendWhere(eq_link1, new int[]{enummaster,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(ev_link1, new int[]{enumpv,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(eq_link2,new int[]{enumerentry,enumlink});
			        qs.appendAnd();
			        qs.appendWhere(eq_value3,new int[]{enumerentry,enumvalue});
			        qs.appendAnd();
			        qs.appendWhere(eq_pd,new int[]{enumvalue,pd});
			    	qs.appendAnd();
			        qs.appendWhere(eq_enumername,new int[]{enummaster});
			        qs.appendOrderBy(LWCPropertyValue.class, "value", false);
			        QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
			        Object[] o = null;
			        LWCLocalizablePropertyValue entryValue = null;
			        while (qr.hasMoreElements()) {
			            o =  (Object[])qr.nextElement();
			            entryValue = (LWCLocalizablePropertyValue)o[enumvalue];
			            list.add(entryValue.getValue());
			        }
		        } catch(WTException e) {
		        	logger.error(CLASSNAME+".getEnumerationItemDisValue:"+e);
		        } finally {
	        		SessionServerHelper.manager.setAccessEnforced(enforce);
	        	}
		        return list;
	        }
		} catch (RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	public static void test() throws RemoteException, InvocationTargetException, WTException {
//		System.out.println("/*********************isEnumerationExist********************/");
//		System.out.println(isEnumerationExist("cn.elead.enum1"));
//		System.out.println(isEnumerationExist("enum1"));
//		System.out.println(isEnumerationExist(""));
//		System.out.println(isEnumerationExist(null));
//		System.out.println("/*********************getEnumerationByName********************/");
//		System.out.println(getEnumerationByName("cn.elead.enum1"));
//		System.out.println(getEnumerationByName("enum1"));
//		System.out.println(getEnumerationByName(""));
//		System.out.println(getEnumerationByName(null));
//		System.out.println("/*********************createEnumeration********************/");
//		System.out.println(createEnumeration("cn.elead.Test2", getEnumerationByName("cn.elead.enumTest1").getOrganizationScope(),
//				WCUtil.getLWCOrganizer("cn.elead.Elead_enumeration1")));
//		System.out.println(createEnumeration("cn.elead.enum1", getEnumerationByName("cn.elead.enum1").getOrganizationScope(), 
//				WCUtil.getLWCOrganizer("cn.elead.Elead_enumerations")));
//		System.out.println(createEnumeration("cn.elead.enum12", null, null));
//		System.out.println(createEnumeration("", null, null));
//		System.out.println(createEnumeration(null, null, null));
//		System.out.println("/*********************isEnumHasItem********************/");
//		System.out.println(isEnumHasItem("cn.elead.enum6"));
//		System.out.println(isEnumHasItem("cn.elead.enum7"));
//		System.out.println(isEnumHasItem("asd"));
//		System.out.println(isEnumHasItem(""));
//		System.out.println(isEnumHasItem(null));
//		System.out.println("/*********************getEnumerationItem********************/");
//		List<Map<String,String>> result = getEnumerationItem("cn.elead.enum1");
//		for (int i = 0; i < result.size(); i++) {
//			System.out.println("name:"+result.get(i).get(INNERNAME)+"    value:"+result.get(i).get(DISPLYNAME));
//		}
//		System.out.println("---------------------------------------");
//		List<Map<String,String>> result1 = getEnumerationItem("enum");
//		for (int i = 0; i < result1.size(); i++) {
//			System.out.println("name:"+result1.get(i).get(INNERNAME)+"    value:"+result1.get(i).get(DISPLYNAME));
//		}
//		System.out.println("---------------------------------------");
//		List<Map<String,String>> result2 = getEnumerationItem("");
//		for (int i = 0; i < result2.size(); i++) {
//			System.out.println("name:"+result2.get(i).get(INNERNAME)+"    value:"+result2.get(i).get(DISPLYNAME));
//		}
//		System.out.println("---------------------------------------");
//		List<Map<String,String>> result3 = getEnumerationItem(null);
//		for (int i = 0; i < result3.size(); i++) {
//			System.out.println("name:"+result3.get(i).get(INNERNAME)+"    value:"+result3.get(i).get(DISPLYNAME));
//		}
//		System.out.println("/*********************getEnumerationItemInnerValue********************/");
//		List<String> list1 = getEnumerationItemInnerValue("cn.elead.enum6");
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println(list1.get(i));
//		}
//		List<String> list2 = getEnumerationItemInnerValue("enum6");
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println(list2.get(i));
//		}
//		List<String> list3 = getEnumerationItemInnerValue("");
//		for (int i = 0; i < list3.size(); i++) {
//			System.out.println(list3.get(i));
//		}
//		List<String> list4 = getEnumerationItemInnerValue(null);
//		for (int i = 0; i < list4.size(); i++) {
//			System.out.println(list4.get(i));
//		}
//		System.out.println("/*********************getEnumerationItemDisValue********************/");
//		List<String> list1 = getEnumerationItemDisValue("cn.elead.enum6");
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println(list1.get(i));
//		}
//		List<String> list2 = getEnumerationItemDisValue("enum6");
//		for (int i = 0; i < list2.size(); i++) {
//			System.out.println(list2.get(i));
//		}
//		List<String> list3 = getEnumerationItemDisValue("");
//		for (int i = 0; i < list3.size(); i++) {
//			System.out.println(list3.get(i));
//		}
//		List<String> list4 = getEnumerationItemDisValue(null);
//		for (int i = 0; i < list4.size(); i++) {
//			System.out.println(list4.get(i));
//		}
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException {
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", EnumUtil.class.getName(), null, new Class[] {}, new Object[] {});
		}
	}
}
