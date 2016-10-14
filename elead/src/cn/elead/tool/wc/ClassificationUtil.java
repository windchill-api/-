package cn.elead.tool.wc;

import com.google.gwt.rpc.client.impl.RemoteException;
import com.ptc.core.lwc.common.AttributeTemplateFlavor;
import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCAttributeDefaultValue;
import com.ptc.core.lwc.server.LWCIBAAttDefinition;
import com.ptc.core.lwc.server.LWCStructEnumAttTemplate;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class ClassificationUtil  implements RemoteAccess {
    private static String CLASSNAME = ClassificationUtil.class.getName();
    private static final String DEFAULT_NAMESPACE = "com.ptc.csm.default_clf_namespace"; //默认namespace
	
	private static final String DEFAULT_ATTR_NAME = "HQ_CLASSIFICATION";//分类属性默认的分类名称
	
	private static String NAMESPACE = ""; //获取Properties中配置的namespace
	
	private static String ATTR_NAME = ""; //获取Properties中配置的属性名称
	
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	static {
		try {
			WTProperties PROPERTIES = WTProperties.getLocalProperties();
			   System.out.println(("~~~~1~~~~~"+PROPERTIES));
			NAMESPACE = PROPERTIES.getProperty("com.ptc.csm.namespace", DEFAULT_NAMESPACE);
			   //System.out.println(("~~~~2~~~~~"+NAMESPACE));
			ATTR_NAME = PROPERTIES.getProperty("part.classification.ref.att", DEFAULT_ATTR_NAME);
			   //System.out.println(("~~~~3~~~~~"+ATTR_NAME));
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	/**
	 * cath  的捕捉方法名 获取的什么
	 * <一句话功能简述>
	 * <功能详细描述>
	 * @author  zhangxj
	 * @see [类、类#方法、类#成员]
	 */
	public static TypeDefinitionReadView getClsNodeByName(String name) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
						return (TypeDefinitionReadView) RemoteMethodServer.getDefault().invoke(
								"getClsNodeByName", CLASSNAME, null,
								new Class[] { String.class }, new Object[] {name});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if(!StringUtils.isEmpty(name)){
					  TypeDefinitionReadView nodeView = TypeDefinitionServiceHelper.service.getTypeDefView(AttributeTemplateFlavor.LWCSTRUCT, NAMESPACE, name);
					  return nodeView;
					}
				} catch (Exception e) {
					logger.error(CLASSNAME+".getPromotionNoticeByNumber:" + e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			}
		} catch (java.rmi.RemoteException e) {
		       logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
		       logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @Author: bjj
	 * @Date: 2016年9月20日 上午10:46:14
	 * @Description: 获取零部件对应的分类节点
	 * @param part
	 * @return
	 * @throws WTException 
	 */
	public static TypeDefinitionReadView getClsNodeByPart(WTPart part) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (TypeDefinitionReadView) RemoteMethodServer.getDefault().invoke("getClsNodeByPart", CLASSNAME, null,
						new Class[] { WTPart.class }, new Object[] {part});
			} else {
				TypeDefinitionReadView nodeView = null;
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					if(part != null && PartUtil.isPartExist(part.getNumber())){	
					    String clsString = part.getName();
						nodeView = getClsNodeByName(clsString);
					}
				}catch (Exception e) {
					logger.error(CLASSNAME+".getClsNodeByPart:" + e);
				}finally{
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return nodeView;
			}
		} catch (java.rmi.RemoteException e) {
		       logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
		       logger.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * @Author: bjj
	 * @Date: 2016年9月20日 上午10:50:55
	 * @Description:获取零部件对应分类节点的内部名称
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static String getClsInternalNameByPart(WTPart part) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getClsInternalNameByPart", CLASSNAME, null,
						new Class[] { WTPart.class }, new Object[] {part});
			} else {
				TypeDefinitionReadView nodeView = null;
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				String str = "";
				if(part != null && PartUtil.isPartExist(part.getNumber())){	
					try {
						nodeView = getClsNodeByPart(part);
						if(nodeView!=null) {
							str = PropertyHolderHelper.getName(nodeView);
						}	
					} catch (Exception e) {
						logger.error(CLASSNAME+".getClsInternalNameByPart:" + e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return str;
				}
			}
		} catch (java.rmi.RemoteException e) {
		       logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
		       logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @Author: bjj
	 * @Date: 2016年9月20日 上午10:50:55
	 * @Description:获取零部件对应分类节点的显示名称
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static String getClsDisplayNameByPart(WTPart part) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {return (String) RemoteMethodServer.getDefault().invoke("getClsInternalNameByPart", CLASSNAME, null,
						new Class[] { WTPart.class }, new Object[] {part});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				if(part != null && PartUtil.isPartExist(part.getNumber())){	
					TypeDefinitionReadView nodeView = getClsNodeByPart(part);
					String str = "";
					try {
						if(nodeView!=null) {
							str = PropertyHolderHelper.getDisplayName(nodeView,Locale.CHINA);
						}
					}  catch (Exception e) {
						logger.error(CLASSNAME+".getClsDisplayNameByPart:" + e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
					return str;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	
	/**
	 * @Author: bjj
	 * @Date: 2016年9月20日 上午11:01:36
	 * @Description: 根据分类节点获取分类路径
	 * @param nodeView 分类节点
	 * @param output 当前分类节点对应的显示名称
	 * @return
	 * @throws WTException
	 */
	public static String getClfNodeDislayPath(TypeDefinitionReadView nodeView,String output) throws WTException {

		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getClfNodeDislayPath", CLASSNAME, null,
						new Class[] { TypeDefinitionReadView.class, String.class }, new Object[] {nodeView, output});
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				try {
					nodeView = TypeDefinitionServiceHelper.service.getParentTypeDefView(nodeView);
					if (nodeView != null) {
						String nodeName = "";
						// WTContext.getContext().setLocale(Locale.CHINA);
						nodeName = PropertyHolderHelper.getDisplayName(nodeView,
								Locale.CHINA);
						output = nodeName + "/" + output;
						output = getClfNodeDislayPath(nodeView, output);
					}
				} catch (Exception e) {
				logger.error(CLASSNAME+".getClfNodeDislayPath:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
				return output;
			}
		} catch (java.rmi.RemoteException e) {
			logger.error(e.getMessage(),e);
		} catch (InvocationTargetException e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}
	
	/**
	 * @Author: bjj
	 * @Date: 2016年9月20日 上午11:08:13
	 * @Description: 获取当前零部件的分类路径
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static String getClfNodeDislayPathByPart(WTPart part) throws WTException {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (String) RemoteMethodServer.getDefault().invoke("getClfNodeDislayPathByPart", CLASSNAME, null,
						new Class[] { WTPart.class }, new Object[] { part });
			} else {
				boolean enforce = wt.session.SessionServerHelper.manager.setAccessEnforced(false);
				String displayPath = "";
				try {
					TypeDefinitionReadView nodeView = getClsNodeByPart(part);
					if (nodeView != null) {
						// WTContext.getContext().setLocale(Locale.CHINA);
						String displayName = PropertyHolderHelper.getDisplayName(nodeView,
								Locale.CHINA);
						displayPath = getClfNodeDislayPath(nodeView, displayName);
					}	
				} catch (Exception e) {
					logger.error(CLASSNAME+".getClfNodeDislayPathByPart:" + e);
				} finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			   return displayPath;
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
	 * @descrip 根据部件分类节点Id获取其分类节点的某一属性值
	 * @param noteReadView >> getClsNodeByName("分类属性内部名称");
	 * @param internetName 属性内部名称
	 * @return
	 */
	public static LWCAttributeDefaultValue getSimpleAttrValueByNode(TypeDefinitionReadView noteReadView,String internetName) {
		LWCAttributeDefaultValue defaultValue = null;
		boolean isForce = SessionServerHelper.manager.isAccessEnforced();
		try {
			SessionServerHelper.manager.setAccessEnforced(false);
			QuerySpec qs = new QuerySpec();
			qs.setAdvancedQueryEnabled(true);
			int ldv = qs.appendClassList(LWCAttributeDefaultValue.class, true);
			int ld = qs.appendClassList(LWCIBAAttDefinition.class, false);
			int lt = qs.appendClassList(LWCStructEnumAttTemplate.class, false);
			/** ldv.ida3a4=ld.ida2a2 */
			SearchCondition _ldvldId = new SearchCondition(
					LWCAttributeDefaultValue.class,"attributeReference.key.id",
					LWCIBAAttDefinition.class,"thePersistInfo.theObjectIdentifier.id");
			
			/** ldv.ida3c4=lt.ida2a2 */
			SearchCondition _ldvltId = new SearchCondition(
					LWCAttributeDefaultValue.class,"contextReference.key.id",
					LWCStructEnumAttTemplate.class,"thePersistInfo.theObjectIdentifier.id");
			
			/** lt.id=part分类节点Id */
			SearchCondition eq_clsNode = new SearchCondition(
					LWCStructEnumAttTemplate.class,"thePersistInfo.theObjectIdentifier.id",
					SearchCondition.EQUAL,noteReadView.getId());
			
			/** ld.name=具体某个属性的内部名称 */
			SearchCondition eq_attrName = new SearchCondition(
					LWCIBAAttDefinition.class,LWCIBAAttDefinition.NAME,
					SearchCondition.EQUAL,internetName);

			qs.appendWhere(_ldvldId, new int[]{ldv,ld});
            qs.appendAnd();
            qs.appendWhere(_ldvltId, new int[]{ldv,lt});
            qs.appendAnd();
			qs.appendWhere(eq_clsNode, new int[]{lt});
			qs.appendAnd();
			qs.appendWhere(eq_attrName, new int[]{ld});
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
			while(qr.hasMoreElements()){
                Object[] object=  (Object[])qr.nextElement();
                if(object != null && object instanceof Persistable[]){
                	Persistable[] persistable = (Persistable[])object;
                	defaultValue = (LWCAttributeDefaultValue)persistable[0];
                }
            }
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		} finally{
        	SessionServerHelper.manager.setAccessEnforced(isForce);
        }
		return defaultValue ;
	}

	
	public static <T> void test() throws RemoteException,InvocationTargetException, WTException {  
		
		/*String oid="OR%3Acom.ptc.core.lwc.server.LWCStructEnumAttTemplate%3A4043068".replace("%3A", ":");
		ReferenceFactory referenceFactory = new ReferenceFactory();
		Persistable cls = referenceFactory.getReference(oid).getObject();
		System.out.println(">>>1>>>>"+cls);*/
		/*String name = "bjj_test01";
		System.out.println(getClsNodeByName(name));
		
		String oid="OR%3Acom.ptc.core.lwc.server.LWCStructEnumAttTemplate%3A4043068".replace("%3A", ":");
		ReferenceFactory referenceFactory = new ReferenceFactory();
		WTPart part = (WTPart) referenceFactory.getReference(oid).getObject();
		getClsValue(part);*/
//		WTPart part = PartUtil.getPartByNumber("0000000023");
//		TypeDefinitionReadView gcnbp = getClsNodeByPart(part);
//		System.out.println("2---------"+gcnbp);
//		String gcin = getClsInternalNameByPart(part);
//		System.out.println("3-------"+gcin);
//		String gcdn = getClsDisplayNameByPart(part);
//		System.out.println("4------------"+gcdn);
//		System.out.println("---------------------------------------");
//		TypeDefinitionReadView gcnbp1 = getClsNodeByPart(null);
//		System.out.println("5---------"+gcnbp1);
//		String gcin1 = getClsInternalNameByPart(null);
//		System.out.println("6-------"+gcin1);
//		String gcdn1 = getClsDisplayNameByPart(null);
//		System.out.println("7------------"+gcdn1);
//		
//		String dicppath = getClfNodeDislayPath(gcnbp, gcdn);
//		System.out.println("8-----------------"+dicppath);
//		String disbypart = getClfNodeDislayPathByPart(part);
//		System.out.println("9---------------"+disbypart);
//		
//		LWCAttributeDefaultValue attrv = getSimpleAttrValueByNode(gcnbp, gcdn);
//		System.out.println("10---------------"+attrv);
		WTPart part = PartUtil.getPartByNumber("HQ11100005000");
		TypeDefinitionReadView nodeView =getClsNodeByPart(part);
//		System.out.println(nodeView);
//		System.out.println("1111111");
//		System.out.println(getClsNodeByName("华贝物料"));
		System.out.println(getClsInternalNameByPart(part));
//		System.out.println(getSimpleAttrValueByNode(nodeView,"HQ_MOISTURE_SENTIVITY"));
//		System.out.println(getClfNodeDislayPathByPart(part)+"222");
//		System.out.println(getClfNodeDislayPath(nodeView,"焊接材料")+"1");
//		System.out.println(getClsDisplayNameByPart(part));
//		
		
	
	}
	
	public static void main(String[] args)throws com.google.gwt.rpc.client.impl.RemoteException, InvocationTargetException, WTException{
	    if (!RemoteMethodServer.ServerFlag)
	      try {
	    	    RemoteMethodServer server = RemoteMethodServer.getDefault();
				server.setUserName("wcadmin");
				server.setPassword("wcadmin");
	            RemoteMethodServer.getDefault().invoke("test", CLASSNAME, null, new Class[0], new Object[0]);
	      }
	      catch (java.rmi.RemoteException e)
	      {
	        e.printStackTrace();
	      }
	}

}
