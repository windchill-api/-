package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.doc.WTDocumentMaster;
import wt.fc.Identified;
import wt.fc.IdentityHelper;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.ObjectVector;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.folder.Foldered;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.State;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartDescribeLink;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartMasterIdentity;
import wt.part.WTPartReferenceLink;
import wt.part.WTPartUsageLink;
import wt.pds.StatementSpec;
import wt.query.CompositeWhereExpression;
import wt.query.ConstantExpression;
import wt.query.LogicalOperator;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.query.WhereExpression;
import wt.session.SessionHelper;
import wt.team.TeamReference;
import wt.type.TypedUtility;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.Iterated;
import wt.vc.VersionControlHelper;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;
import wt.vc.views.View;
import wt.vc.views.ViewHelper;
import wt.vc.views.ViewManageable;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinition;
import com.ptc.core.meta.type.mgmt.server.impl.WTTypeDefinitionMaster;

public class PartUtil implements RemoteAccess, Serializable {
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = PartUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	private static final int[] DEFAULT_CONDITION_ARRAY = new int[] { 0 };
	
	/**
     * judge whether part exist by part number
     * @author WangY
     */
    public static boolean isPartExist(String strNumber){
    	WTPartMaster wtpartmaster = null;
        if(!StringUtils.isEmpty(strNumber)){
        	try {
				wtpartmaster = getPartMasterByNumber(strNumber);
			} catch (WTException e) {
				logger.error(">>>>>"+e);
			}
        }
        if (wtpartmaster == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get part master by part number
     * @author WangY
     */
    public static WTPartMaster getPartMasterByNumber(String partNo) throws WTException {
        QuerySpec querySpec = new QuerySpec(WTPartMaster.class);
        partNo = partNo.toUpperCase();
        WhereExpression searchCondition = new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER, SearchCondition.EQUAL, partNo, false);
        querySpec.appendWhere(searchCondition,DEFAULT_CONDITION_ARRAY);
        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
        while (queryResult.hasMoreElements()) {
            WTPartMaster partMaster = (WTPartMaster) queryResult.nextElement();
            return partMaster;
        }
        return null;
    }
    
    
	/**
	 * get part by oid
	 * @author WangY
	 */
    public static WTPart getPartByOid(String oid){
		WTPart wtpart = null;
		try {
			if(!StringUtils.isEmpty(oid)){
				ReferenceFactory rf = new ReferenceFactory();
				wtpart = (WTPart) rf.getReference(oid).getObject();
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return wtpart;
	}
	
	
	
    /**
	 * get part by type
	 * @author WangY
	 */
	public static List<WTPart> getPartByType(String partType){
		List<WTPart> listPart = new ArrayList<WTPart>();
		try {
			if(!StringUtils.isEmpty(partType)){
				QuerySpec qs = new QuerySpec();
				int in1 = qs.appendClassList(WTTypeDefinitionMaster.class, false);//管理对象类型表
				int in2 = qs.appendClassList(WTTypeDefinition.class, false);//WTPart与WTTypeDefinitionMaster中间表
				int in3 = qs.addClassList(WTPart.class, true);
				String str[] = new String[3];
				str[0] = qs.getFromClause().getAliasAt(in1);
				str[1] = qs.getFromClause().getAliasAt(in2);
				str[2] = qs.getFromClause().getAliasAt(in3);
				TableColumn tcMast = new TableColumn(str[0], "ida2a2");
				TableColumn tcMast1 = new TableColumn(str[0], "inthid");
				TableColumn tcDefin = new TableColumn(str[1], "ida2a2");
				TableColumn tcDefin1 = new TableColumn(str[1], "ida3masterreference");
				TableColumn tcWtpart = new TableColumn(str[2], "ida2typedefinitionreference");
				TableColumn tcWtpart1 = new TableColumn(str[2], "latestiterationinfo");
				CompositeWhereExpression cwe = new CompositeWhereExpression(LogicalOperator.AND);
				cwe.append(new SearchCondition(tcMast, "=", tcDefin1));
				cwe.append(new SearchCondition(tcDefin, "=", tcWtpart));
				cwe.append(new SearchCondition(tcWtpart1, "=", new ConstantExpression("1",true)));//最新小版本
				cwe.append(new SearchCondition(tcMast1, "=", new ConstantExpression(partType,true)));
				qs.appendWhere(cwe,DEFAULT_CONDITION_ARRAY);
				QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
				ObjectVector objv = new ObjectVector();//对查询结果处理
				while(qr.hasMoreElements()){
					Object obj[] = (Object[])qr.nextElement();
					objv.addElement(obj[0]);
				}
				qr = new QueryResult((wt.fc.ObjectVectorIfc)objv);
				LatestConfigSpec cs = new LatestConfigSpec();
				cs.process(qr);
				if(qr.hasMoreElements()){
					while(qr.hasMoreElements()){
						Object object = qr.nextElement();
						WTPart wp = (WTPart)object;
						listPart.add(wp);
					}
				}else{
					logger.error(">>>>> error: partType is not exist");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return listPart;
	}
	
	
	
	/**
	 * get part by number
	 * @author WangYu
	 */
	public static WTPart getPartByNumber(String partNumber) throws WTException{
		WTPart part = null;
		if (!StringUtils.isEmpty(partNumber)) {
			QuerySpec qs = new QuerySpec(WTPart.class);
			WhereExpression  we = new SearchCondition(WTPart.class,WTPart.NUMBER, SearchCondition.EQUAL, partNumber);
			qs.appendWhere(we,DEFAULT_CONDITION_ARRAY);
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
			LatestConfigSpec cfg = new LatestConfigSpec();  //构建过滤器
			QueryResult qr1 = cfg.process(qr); //按小版本排序
			if(qr1.hasMoreElements())
				part =(WTPart) qr1.nextElement(); //获取最新小版本的WTPart对象
		}
		return part;
	}
	
	/**
	 * get part by partNumber,state
	 * @param partName
     * @param states
     * @author WangY
     */
    public static List<WTPart> getPartByNumberState(String partNumber, String state) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partNumber) && !StringUtils.isEmpty(state)){
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
	                SearchCondition.EQUAL, partNumber, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            String partState = part.getState().toString();
	            logger.debug("partState = " + partState);
	            if (partState.indexOf(state) != -1) {
	                partList.add(part);
	            }
	        }
    	}
        return partList;
    }
	
	
    /**
	 * get part by partNumber,state,type
	 * @param partName
     * @param states
     * @param type
     * @author WangY
     */
    public static List<WTPart> getPartByNumberStateType(String partNumber, String state,     
            String type) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partNumber) && !StringUtils.isEmpty(state) && !StringUtils.isEmpty(type)){
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
	                SearchCondition.EQUAL, partNumber, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            String ibaType = TypedUtility.getTypeIdentifier(part).getTypename();
	            String partState = part.getState().toString();
	            logger.debug("ibaType=" + ibaType + ",partState = " + partState);
	            if (partState.indexOf(state) != -1 && ibaType.indexOf(type) != -1) {
	                partList.add(part);
	            }
	        }
    	}
        return partList;
    }
    
    /**
	 * get part by partNumber,state,type,view
	 * @param partName
     * @param states
     * @param type
     * @param view
     * @author WangY
     */
    public static List<WTPart> getPartByNumberStateTypeView(String partNumber, String state,     
            String type, String partView) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partNumber) && !StringUtils.isEmpty(state) && !StringUtils.isEmpty(type) 
        		&& !StringUtils.isEmpty(partView)){
	        View view = ViewHelper.service.getView(partView);
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER,
	                SearchCondition.EQUAL, partNumber, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        if (view != null) {
	            qs.appendAnd();
	            sc = new SearchCondition(WTPart.class, "view.key.id",
	                    SearchCondition.EQUAL, view.getPersistInfo()
	                            .getObjectIdentifier().getId());
	            qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        }
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            String ibaType = TypedUtility.getTypeIdentifier(part).getTypename();
	            String partState = part.getState().toString();
	            logger.debug("ibaType=" + ibaType + ",partState = " + partState);
	            if (partState.indexOf(state) != -1 && ibaType.indexOf(type) != -1) {
	                partList.add(part);
	            }
	        }
    	}
        return partList;
    }
    
    
    
	/**
	 * get part by partName
	 * @param partName
     * @author WangY
     */
    public static List<WTPart> getPartByName(String partName) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partName)){
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NAME,
	                SearchCondition.EQUAL, partName, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            partList.add(part);
	        }
    	}
        return partList;
    }
	
	/**
	 * get part by partName,state
	 * @param partName
     * @param states
     * @author WangY
     */
    public static List<WTPart> getPartByNameState(String partName, String state) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partName) && !StringUtils.isEmpty(partName)){
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NAME,
	                SearchCondition.EQUAL, partName, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            String partState = part.getState().toString();
	            logger.debug("partState = " + partState);
	            if (partState.indexOf(state) != -1) {
	                partList.add(part);
	            }
	        }
    	}
        return partList;
    }
	
    /**
	 * get part by partName,state,type
	 * @param partName
     * @param states
     * @param type
     * @author WangY
     */
    public static List<WTPart> getPartByNameStateType(String partName, String state,     
            String type) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partName) && !StringUtils.isEmpty(state) && !StringUtils.isEmpty(type)){
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NAME,
	                SearchCondition.EQUAL, partName, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            String ibaType = TypedUtility.getTypeIdentifier(part).getTypename();
	            String partState = part.getState().toString();
	            logger.debug("ibaType=" + ibaType + ",partState = " + partState);
	            if (partState.indexOf(state) != -1 && ibaType.indexOf(type) != -1) {
	                partList.add(part);
	            }
	        }
    	}
        return partList;
    }
    
    /**
	 * get part by partName,state,type,view
	 * @param partName
     * @param states
     * @param type
     * @param view
     * @author WangY
     */
    public static List<WTPart> getPartByNameStateTypeView(String partName, String state,     
            String type, String partView) throws WTException {
        List<WTPart> partList = new ArrayList<WTPart>();
        if(!StringUtils.isEmpty(partName) && !StringUtils.isEmpty(state) && !StringUtils.isEmpty(type) 
        		&& !StringUtils.isEmpty(partView)){
	        View view = ViewHelper.service.getView(partView);
	        QuerySpec qs = new QuerySpec(WTPart.class);
	        SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NAME,
	                SearchCondition.EQUAL, partName, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        if (view != null) {
	            qs.appendAnd();
	            sc = new SearchCondition(WTPart.class, "view.key.id",
	                    SearchCondition.EQUAL, view.getPersistInfo()
	                            .getObjectIdentifier().getId());
	            qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        }
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        WTPart part = null;
	        while (qr.hasMoreElements()) {
	            part = (WTPart) qr.nextElement();
	            String ibaType = TypedUtility.getTypeIdentifier(part).getTypename();
	            String partState = part.getState().toString();
	            logger.debug("ibaType=" + ibaType + ",partState = " + partState);
	            if (partState.indexOf(state) != -1 && ibaType.indexOf(type) != -1) {
	                partList.add(part);
	            }
	        }
    	}
        return partList;
    }
    
    /**
	 * get type by part
	 * @author WangY
	 */
	public static String getTypeByPart(WTPart part){
		String partType = null;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				partType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(part).toString();
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return partType;
	}
	
	/**
	 * get number by part
	 * @author WangY
	 */
	public static String getNumberByPart(WTPart part){
		String partNumber = null;
		try{
			if(part!=null && isPartExist(part.getNumber())){
				partNumber = part.getNumber();
			}
		}catch(Exception e){
			logger.error(">>>>>"+e);
		}
		return partNumber;
	}
	
	/**
	 * get name by part
	 * @author WangY
	 */
	public static String getNameByPart(WTPart part){
		String partName = null;
		try{
			if(part!=null && isPartExist(part.getNumber())){
				partName = part.getName();
			}
		}catch(Exception e){
			logger.error(">>>>>"+e);
		}
		return partName;
	}
	
	/**
	 * get latest part by part
	 * @author WangY
	 */
	public static WTPart getLatestPartByPart(WTPart part){
		WTPart wt = null;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				QueryResult qr = new QueryResult();
				QuerySpec qs = new QuerySpec(WTPart.class);
				WhereExpression we = new SearchCondition(WTPart.class, WTPart.NUMBER, "=", part.getNumber());
				qs.appendWhere(we,DEFAULT_CONDITION_ARRAY);
				qr = PersistenceHelper.manager.find((StatementSpec) qs);
				LatestConfigSpec cfg = new LatestConfigSpec();//构建过滤器
				QueryResult qr1 = cfg.process(qr); //按小版本排序
				while (qr1.hasMoreElements()) {
					wt = (WTPart) qr1.nextElement();//获取最新小版本的WTPart对象
				}
			}
		}  catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return wt;
	}
	
	/**
	 * get latest part by number
	 * @author WangY
	 */
	public static WTPart getLatestPartByPartNumber(String partNumber){
		WTPart wt = null;
		try {
			if(!StringUtils.isEmpty(partNumber)){
				QueryResult qr = new QueryResult();
				QuerySpec qs = new QuerySpec(WTPart.class);
				WhereExpression we = new SearchCondition(WTPart.class, WTPart.NUMBER, "=", partNumber);
				qs.appendWhere(we,DEFAULT_CONDITION_ARRAY);
				qr = PersistenceHelper.manager.find((StatementSpec) qs);
				LatestConfigSpec cfg = new LatestConfigSpec();//构建过滤器
				QueryResult qr1 = cfg.process(qr); //按小版本排序
				while (qr1.hasMoreElements()) {
					wt = (WTPart) qr1.nextElement();//获取最新小版本的WTPart对象
				}
			}
		}  catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return wt;
	}
	
	/**
	 * get bigVersion part by part
	 * @author WangY
	 */
	public static String getBigVersionPartByPart(WTPart part){
		String wt = null;
		try{
			if(part!=null && isPartExist(part.getNumber())){
				wt = part.getVersionIdentifier().getValue();
			}
		}catch(Exception e){
			logger.error(">>>>>"+e);
		}
		return wt;
	}
	
	/**
	 * get smallVersion part by part
	 * @author WangY
	 */
	public static String getSmallVersionPartByPart(WTPart part){
		String wt = null;
		try{
			if(part!=null && isPartExist(part.getNumber())){
				wt = part.getIterationIdentifier().getValue();
			}
		}catch(Exception e){
			logger.error(">>>>>"+e);
		}
		return wt;
	}
	
	/**
	 * get subPart by part number
	 * @author WangY
	 */
	public static List<WTPartMaster> getSubPartByPartNumber(String number) throws WTException{
		if(!StringUtils.isEmpty(number) && isPartExist(number)){
			List<WTPartMaster> wtpmList = new ArrayList<WTPartMaster>(); 
			QuerySpec qs = new QuerySpec();
			int wtp = qs.appendClassList(WTPart.class,false);
			int wtm = qs.appendClassList(WTPartMaster.class,false);
			int wtu = qs.appendClassList(WTPartUsageLink.class, true);
			String str[] = new String[3];
			str[0] = qs.getFromClause().getAliasAt(wtp);
			str[1] = qs.getFromClause().getAliasAt(wtm);
			str[2] = qs.getFromClause().getAliasAt(wtu);
			TableColumn wtpCol = new TableColumn(str[0], "ida3masterreference");
			TableColumn wtpICol = new TableColumn(str[0], "ida2a2");
			TableColumn wtmCol = new TableColumn(str[1], "ida2a2");
			TableColumn wtuCol = new TableColumn(str[2], "ida3a5");
			WhereExpression w1 = new SearchCondition(wtmCol, SearchCondition.EQUAL, wtpCol);
			WhereExpression w2 = new SearchCondition(wtpICol, SearchCondition.EQUAL, wtuCol); 
			WhereExpression w3 = new SearchCondition(WTPart.class, WTPart.NUMBER, "=", number); 
			qs.appendWhere(w1,DEFAULT_CONDITION_ARRAY);
			qs.appendAnd();
			qs.appendWhere(w2,DEFAULT_CONDITION_ARRAY);
			qs.appendAnd();
			qs.appendWhere(w3,DEFAULT_CONDITION_ARRAY);
			QueryResult qr = PersistenceHelper.manager.find((StatementSpec)qs);
			while(qr.hasMoreElements()){
				Object[] obj = (Object[]) qr.nextElement();
				WTPartUsageLink wu = (WTPartUsageLink) obj[0];
				WTPartMaster pa = (WTPartMaster)wu.getUses();
				//System.out.println("名称：" + pa.getName() + ",编号："+ pa.getNumber());
				wtpmList.add(pa);
			}
			int l = wtpmList.size();
			for (int i = 0; i < l-1; i++) {
				for (int j = i+1; j < l; j++) {
					if(wtpmList.get(i)==wtpmList.get(j)){
						wtpmList.remove(j);
						l = l-1;
					}
				}
			}
			return wtpmList;
		}
		return null;
	}
	
	/**
	 * get part discriptionDoc by part
	 * @author WangY
	 */
	public static List<WTDocument> getDisDocByPart(WTPart part){
		List<WTDocument> listDoc = new ArrayList<WTDocument>();
		try {
			if(part!=null && isPartExist(part.getNumber())){
				QueryResult qr = WTPartHelper.service.getDescribedByDocuments(part);
				while(qr.hasMoreElements()){
					Object object = qr.nextElement();
					if(object instanceof WTDocument){
					     WTDocument doc = (WTDocument)object;
					     listDoc.add(doc);
					}
				}
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return listDoc;
	}
	
	/**
	 * get referenceDoc by part
	 * @author WangY
	 */
	public static List<WTDocumentMaster> getRefDocByPart(WTPart part){
		List<WTDocumentMaster> listDocMaster = new ArrayList<WTDocumentMaster>();
		try {
			if(part!=null && isPartExist(part.getNumber())){
				@SuppressWarnings("deprecation")
				QueryResult qr = WTPartHelper.service.getReferencesWTDocumentMasters(part);
				while(qr.hasMoreElements()){
					Object object = qr.nextElement();
				if(object instanceof WTDocumentMaster){
					    WTDocumentMaster doct = (WTDocumentMaster)object;
					    listDocMaster.add(doct);
				        System.out.println("number="+doct.getNumber()+";name="+ doct.getName());
					}
				}
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return listDocMaster;
	}
	
	/**
	 * new WTPart by newPartName,newPartNumber,partNumber
	 * @author WangY
	 */
	public static WTPart newPart(String newPartName,String newPartNumber,String partNumber){
		WTPart part = null;
		try{
			if(!StringUtils.isEmpty(newPartName)&&!StringUtils.isEmpty(newPartNumber)&&
					!StringUtils.isEmpty(partNumber)&&isPartExist(partNumber)&&
					!isPartExist(newPartNumber)){
				part = WTPart.newWTPart();
				part.setName(newPartName);//设置名字
				part.setNumber(newPartNumber);//设置编号
				part.setContainer(getPartByNumber(partNumber).getContainer());//设置容器
				PersistenceHelper.manager.save(part);
			}
		}catch(Exception e){
			logger.error(">>>>>"+e);
		}
		return part;
	}
	
	/**
	 * delete part by part
	 * @author WangY
	 */
	public static void deletePart(WTPart part){
		try {
			if(part!=null && isPartExist(part.getNumber())){
				PersistenceHelper.manager.delete(part);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
	}
	
	/**
	 * update part number by part,number
	 * @author WangY
	 */
	public static void updatePartNumber(WTPart part,String newNumber){
		try {
			if(part!=null && isPartExist(part.getNumber()) && !StringUtils.isEmpty(newNumber) && !isPartExist(newNumber)){
				WTPartMaster partMaster = (WTPartMaster) part.getMaster();
				WTOrganization wtorganization = part.getOrganization();
				partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	part.getName(), newNumber, wtorganization);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
	}
	
	/**
	 * update part number by part,name
	 * @author WangY
	 */
	public static void updatePartName(WTPart part,String newName){
		try {
			if(part!=null && isPartExist(part.getNumber()) && !StringUtils.isEmpty(newName)){
				WTPartMaster partMaster = (WTPartMaster) part.getMaster();
				WTOrganization wtorganization = part.getOrganization();
				partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	newName, part.getNumber(), wtorganization);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
	}
	
	/**
	 * update part number by part,name,number
	 * @author WangY
	 */
	public static void updatePartNoAndName(WTPart part,String newName,String newNumber){
		try {
			if(part!=null && isPartExist(part.getNumber()) && !StringUtils.isEmpty(newName) && !StringUtils.isEmpty(newNumber) 
					&& !isPartExist(newNumber)){
				WTPartMaster partMaster = (WTPartMaster) part.getMaster();
				WTOrganization wtorganization = part.getOrganization();
				partMaster = WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) partMaster,	newName, newNumber, wtorganization);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
	}
	
	/**
	 * To determine whether part is checkout state
	 * @author WangY
	 */
	public static boolean isCheckOutState(WTPart part){
		boolean bo = false;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				bo = WorkInProgressHelper.isCheckedOut(part);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return bo;
	}
	
	/**
	 * To determine whether part is workingCopy
	 * @author WangY
	 */
	public static boolean isWorkingCopy(WTPart part){
		boolean bo = false;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				bo = WorkInProgressHelper.isWorkingCopy(part);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return bo;
	}
	
	/**
	 * To determine whether part can modify
	 * @author WangY
	 */
	public static boolean isModifiable(WTPart part){
		boolean bo = false;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				bo = WorkInProgressHelper.isModifiable(part);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return bo;
	}
	
	/**
	 * get part lifeCycle by part
	 * @author WangY
	 */
	public static State getLifeCycle(WTPart part){
		State state = null;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				state = part.getLifeCycleState();
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return state;
	}
	
	/**
	 * get part lifeCycleTemplate by part
	 * @author WangY
	 */
	@SuppressWarnings("deprecation")
	public static LifeCycleTemplate getLifeCycleTemplate(WTPart part){
		LifeCycleTemplate lt = null;
		try {
			if(part!=null && isPartExist(part.getNumber())){
				 lt = LifeCycleHelper.service.getLifeCycleTemplate((LifeCycleManaged) part);
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return lt;
	}
	
	/**
	 * reset part lifeCycle
	 * @author WangY
	 */
	public static void reSetLifeCycle(WTPart part,String state){
		try {
			if(part!=null && isPartExist(part.getNumber())){
				part = (WTPart) LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) part,State.toState(state));
				PersistenceHelper.manager.refresh(part);
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
	}
	
	/**
	 * checkout part and return wokingCopy
	 * @author WangY
	 */
	public static Workable doCheckOut(Workable part){
		Workable workable = null;
		try{
			if(part!=null){
				if(part instanceof Iterated){
					Iterated it = (Iterated) part;
					part=(Workable) VersionControlHelper.service.getLatestIteration(it, false);
					Boolean checkOutFlag=WorkInProgressHelper.isCheckedOut(part);
					if(checkOutFlag){ 
						if(!WorkInProgressHelper.isWorkingCopy(part)){
					         workable=WorkInProgressHelper.service.workingCopyOf(part);
						}else{
							workable = part;
						}
					}else{
						Folder myFolder= WorkInProgressHelper.service.getCheckoutFolder();
						CheckoutLink link = WorkInProgressHelper.service.checkout(part, myFolder, "AutoCheckOut");
						workable = link.getWorkingCopy();
					}
				}	
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return workable;
	}
	
	/**
	 * checkIn part by part
	 * @author WangY
	 */
	public static void doCheckIn(Workable part){
		if(part!=null){
			Workable workable = null;
			if(!WorkInProgressHelper.isWorkingCopy(part)){
				workable = doCheckOut(part);//从检出方法中得到已检出工作副本
			}else{
				workable = part;
			}
			try{
				workable = WorkInProgressHelper.service.checkin(workable, "AutoCheckIn");
			}catch(Exception e){
				logger.error(">>>>>"+e);
			}
		}
	}
	
	/**
     * revise WTPart by part
     * 
     * @param  WTPart
     * @return WTPart
     * @throws WTException
     */
    @SuppressWarnings("deprecation")
	public static WTPart reviseWTPart(WTPart part, String comment)
            throws WTException {
        WTPart wtpart = part;
        try {
            if (wtpart == null && !isPartExist(part.getNumber())) {
                return null;
            }
            WTContainer container = wtpart.getContainer();
            WTContainerRef containerRef = WTContainerRef
                    .newWTContainerRef(container);
            TeamReference teamReference = wtpart.getTeamId();
            Folder oldFoler = FolderHelper.getFolder(wtpart);// wtpart.getFolderingInfo().getFolder();
            if (oldFoler == null) {
                String strLocation = wtpart.getLocation();
                oldFoler = FolderHelper.service.getFolder(strLocation,
                        containerRef);
            }
            wtpart = (WTPart) VersionControlHelper.service
                    .newVersion((Versioned) wtpart);
            if (wtpart != null) {
                wtpart.setTeamId(teamReference);
            }
            VersionControlHelper.setNote(wtpart, comment);
            wtpart.setContainer(container);
            FolderHelper.assignFolder((Foldered) wtpart, oldFoler);
            wtpart = (WTPart) PersistenceHelper.manager.save(wtpart);
            wtpart = (WTPart) PersistenceHelper.manager.refresh(wtpart);
        } catch (WTPropertyVetoException e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new WTException(e, e.getLocalizedMessage());
        }
        return wtpart;
    }
	
    /**
     * Remove WTPartUsageLink between childPart and root
     * 
     * @param childPart
     * @param root
     * @throws WTException
     */
    public static void removeUseLink(WTPart childPart, WTPart root)
            throws WTException {
        if (childPart == null || root == null) {
            return;
        }
        WTPartMaster partMaster = (WTPartMaster) childPart.getMaster();
        QuerySpec queryspec = new QuerySpec(WTPartUsageLink.class);
        queryspec.appendWhere(
                new SearchCondition(WTPartUsageLink.class,
                        "roleAObjectRef.key", "=", PersistenceHelper
                                .getObjectIdentifier(root)), DEFAULT_CONDITION_ARRAY);
        queryspec.appendAnd();
        queryspec.appendWhere(
                new SearchCondition(WTPartUsageLink.class,
                        "roleBObjectRef.key", "=", PersistenceHelper
                                .getObjectIdentifier(partMaster)),
                                DEFAULT_CONDITION_ARRAY);
        QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
        while (qr.hasMoreElements()) {
            WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
            PersistenceServerHelper.manager.remove(link);
        }
    }

    /**
     * is Part Number Exist
     * 
     * @param strNumber
     *            String
     * @return boolean
     * @throws WTException
     */
    public static boolean isPartNumberExist(String partNo) throws WTException {
        boolean result = false;
        if (RemoteMethodServer.ServerFlag) {
            StatementSpec stmtSpec = new QuerySpec(WTPartMaster.class);
            WhereExpression where = new SearchCondition(WTPartMaster.class,
                    WTPartMaster.NUMBER, SearchCondition.EQUAL,
                    partNo.toUpperCase());
            QuerySpec querySpec = (QuerySpec) stmtSpec;
            querySpec.appendWhere(where, DEFAULT_CONDITION_ARRAY);
            QueryResult qr = PersistenceServerHelper.manager
                    .query(stmtSpec);
            if (qr.hasMoreElements()) {
                result = true;
            }
        } 
        return result;
    }
   
    /**
     * Get part usage link
     * 
     * @param parentPart
     *            parent part
     * @param childMaster
     *            child part master
     * @return part usage link
     * @throws WTException
     *             Windchill exception
     * @see wt.part.WTPart
     * @see wt.part.WTPartMaster
     * @see wt.part.WTPartUsageLink
     */
    public static WTPartUsageLink getPartUsageLink(WTPart parentPart,
            WTPartMaster childMaster) throws WTException {
        WTPartUsageLink usageLink = null;
        if (parentPart != null && childMaster != null && isPartExist(parentPart.getNumber())
        		&& isPartExist(childMaster.getNumber())) {
            QueryResult queryresult = PersistenceHelper.manager.find(
                    WTPartUsageLink.class, parentPart,
                    WTPartUsageLink.USED_BY_ROLE, childMaster);

            if (queryresult == null || queryresult.size() == 0) {
                usageLink = null;
            } else {
                usageLink = (WTPartUsageLink) queryresult.nextElement();
            }
        }
        return usageLink;
    }
    
    /**
     * Get reference link from a part and a doc
     * @author WangY
     */
    public static WTPartReferenceLink getPartReferenceLink(WTPart part,
            WTDocument doc) throws WTException {
        WTPartReferenceLink refLink = null;
        if (part != null && doc != null && isPartExist(part.getNumber())) {
            WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
            refLink = getPartReferenceLink(part, docMaster);
        }
        return refLink;
    }
    public static WTPartReferenceLink getPartReferenceLink(WTPart part,
            WTDocumentMaster docMaster) throws WTException {
        WTPartReferenceLink refLink = null;
        if (RemoteMethodServer.ServerFlag) {
            QueryResult queryresult = PersistenceServerHelper.manager
                    .query(WTPartReferenceLink.class, part,
                            WTPartReferenceLink.REFERENCED_BY_ROLE,
                            docMaster);
            if (queryresult.hasMoreElements()) {
                refLink = (WTPartReferenceLink) queryresult.nextElement();
            }
        }
        return refLink;
    }
    
    /**
     * Add reference doc for a part
     * 
     * @param part
     *            part instance
     * @param doc
     *            document instance
     * @throws WTException
     *             Windchill exception
     */
    public static void addReferenceDoc(WTPart part, WTDocument doc)
            throws WTException {
    	if(isPartExist(part.getNumber())){
	        WTPartReferenceLink refLink = getPartReferenceLink(part, doc);
	        if (refLink == null) {
	            WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
	            refLink = WTPartReferenceLink.newWTPartReferenceLink(part,
	                    docMaster);
	            PersistenceServerHelper.manager.insert(refLink);
	        }
    	}
	}
    
    /**
     * Get the Latest Part by PartMaster
     * 
     * @param partMaster
     * @return
     * @throws WTException
     */
    public static WTPart getLatestPartByMaster(WTPartMaster partMaster)
            throws WTException {
        WTPart latestPart = null;
        if(partMaster!=null && isPartExist(partMaster.getNumber())){
	        QueryResult qr = VersionControlHelper.service
	                .allIterationsOf(partMaster);
	        if (qr.hasMoreElements()) {
	            latestPart = (WTPart) qr.nextElement();
	        }
        }
        return latestPart;
    }
    
    /**
     * Create part usage link
     * 
     * @param parentPart
     *            parent part
     * @param childMaster
     *            child part master
     *
     * @return usage link
     * @throws WTException
     *             Windchill exception
     */
    public static WTPartUsageLink createUsageLink(WTPart parentPart,WTPartMaster childMaster)
            throws WTException {
    	WTPartUsageLink newLink = null;
    	if (parentPart != null && isPartExist(parentPart.getNumber()) && 
        		childMaster != null && isPartExist(childMaster.getNumber())) {
	    	if(getSubPartByPartNumber(parentPart.getNumber()).contains(childMaster)){
	    		newLink = getPartUsageLink(parentPart, childMaster);
	    	}else{
	            newLink = WTPartUsageLink.newWTPartUsageLink(parentPart,
	                    childMaster);
	            PersistenceServerHelper.manager.insert(newLink);
	    	}
        }
        return newLink;
    }
    
    /**
     * rename the  part name
     * 
     * @param wtpart
     * @param partName
     * @throws WTException
     */
    public static void resetPartName(WTPart wtpart, String partName)
            throws WTException {
    	if(wtpart!=null && isPartExist(wtpart.getNumber()) && !StringUtils.isEmpty(partName)){
	        WTPartMaster lPartMaster = (WTPartMaster) wtpart.getMaster();
	        WTPartMasterIdentity identity = WTPartMasterIdentity.newWTPartMasterIdentity(lPartMaster);
	        try {
	            identity.setName(partName);
	            identity.setNumber(lPartMaster.getNumber());
	        } catch (WTPropertyVetoException e) {
	            logger.error(e.getLocalizedMessage(), e);
	            throw new WTException(e, e.getLocalizedMessage());
	        }
	        wt.session.SessionHelper.manager.setAdministrator();
	        IdentityHelper.service.changeIdentity((Identified) lPartMaster,identity);
    	}
    }
    
    /**
     * reset the part number
     * 
     * @param wtpart
     * @param String
     *            newNumber
     * @throws WTException
     */
    public static void resetPartNumber(WTPart aPart, String newNumber){
    	try{
    	if(aPart!=null && isPartExist(aPart.getNumber()) && !StringUtils.isEmpty(newNumber)){
	        Identified aIdentified = (Identified) aPart.getMaster();
	        WTPartMasterIdentity aWTPartMasterIdentity;
	        aWTPartMasterIdentity = (WTPartMasterIdentity) aIdentified
	                .getIdentificationObject();
	        aWTPartMasterIdentity.setNumber(newNumber);
	        IdentityHelper.service.changeIdentity(aIdentified,
	                aWTPartMasterIdentity);
    	}
    	}catch(Exception e){
    		logger.error(">>>>>"+e);
    	}
    }
    
    /**
     * get the usageLink of part
     * 
     * @param part
     * @return
     * @throws WTException
     */
    public static QueryResult findUsageLink(WTPart part) {
        QueryResult qr = new QueryResult();
        try{
        	if(part!=null && isPartExist(part.getNumber())){
		        QuerySpec queryspec = new QuerySpec(WTPartUsageLink.class);
		        queryspec.appendWhere(
		                new SearchCondition(WTPartUsageLink.class,
		                        "roleAObjectRef.key", "=", PersistenceHelper
		                                .getObjectIdentifier(part)), DEFAULT_CONDITION_ARRAY);
		        qr = PersistenceServerHelper.manager.query(queryspec);
        	}
        }catch(WTException e){
        	logger.error(">>>>>"+e);
        }
        return qr;
    }
    
    /**
     * remove all useLink
     * 
     * @param root
     * @throws WTException
     */
    public static void removeAllUseLink(WTPart root) throws WTException {
    	if(root!=null && isPartExist(root.getNumber())){
	        QueryResult qr = WTPartHelper.service.getUsesWTParts(root,
	                WTPartHelper.service.findWTPartConfigSpec());
	        while (qr.hasMoreElements()) {
	            Object[] obj = (Object[]) qr.nextElement();
	            WTPartUsageLink link = (WTPartUsageLink) obj[0];
	            PersistenceServerHelper.manager.remove(link);
	        }
    	}
    }
    
    /**
     * get part's parents
     * 
     * @param child
     * @return List<WTPart>
     * @throws WTException
     */
    public static List<WTPart> getParents(WTPart child) throws WTException {
        List<WTPart> result = new ArrayList<WTPart>();
	    if(child!=null && isPartExist(child.getNumber())){
	        QuerySpec queryspec = new QuerySpec(WTPart.class, WTPartUsageLink.class);
	        queryspec.appendWhere(
	                VersionControlHelper.getSearchCondition(WTPart.class, true),
	                new int[] { 0, 1 });
	        QueryResult qr = PersistenceHelper.manager.navigate(child.getMaster(),
	                "usedBy", queryspec, true);
	        LatestConfigSpec lcs = new LatestConfigSpec();
	        qr = lcs.process(qr);
	        while (qr.hasMoreElements()) {
	            WTPart parent = (WTPart) qr.nextElement();
	            result.add(parent);
	        }
    	}
        return result;
    }
    
    public static List<WTPart> getParentsByView(WTPart child, String viewName)
            throws WTException {
        List<WTPart> result = new ArrayList<WTPart>();
        if(child!=null && isPartExist(child.getNumber())){
	        QuerySpec queryspec = new QuerySpec(WTPart.class, WTPartUsageLink.class);
	        queryspec.appendWhere(
	                VersionControlHelper.getSearchCondition(WTPart.class, true),
	                new int[] { 0, 1 });
	        if (StringUtils.isNotEmpty(viewName)) {
	            queryspec.appendAnd();
	            View view = ViewHelper.service.getView(viewName);
	            ObjectIdentifier objId = PersistenceHelper
	                    .getObjectIdentifier(view);
	            SearchCondition viewCondition = new SearchCondition(WTPart.class,
	                    ViewManageable.VIEW + "." + ObjectReference.KEY,
	                    SearchCondition.EQUAL, objId);
	            queryspec.appendWhere(viewCondition, DEFAULT_CONDITION_ARRAY);
	        }
	        QueryResult qr = PersistenceHelper.manager.navigate(child.getMaster(),
	                "usedBy", queryspec, true);
	        LatestConfigSpec lcs = new LatestConfigSpec();
	        qr = lcs.process(qr);
	        while (qr.hasMoreElements()) {
	            WTPart parent = (WTPart) qr.nextElement();
	            result.add(parent);
	        }
        }
        return result;
    }
    
    /**
     * get partList by number,state,view
     * @author WangY
     * @param number
     * @param state
     * @param view
     * @return
     * @throws WTException
     */
    public static List<WTPart> getPartListByNumberStateView(String number,
            String state,String view) throws WTException {
    	List<WTPart> list = new ArrayList<WTPart>();
    	if(!StringUtils.isEmpty(number) && !StringUtils.isEmpty(state)){
	        StatementSpec stmtSpec = new QuerySpec(WTPart.class);
	        QuerySpec querySpec = (QuerySpec) stmtSpec;
	        WhereExpression where = new SearchCondition(WTPart.class,
	                WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
	        querySpec.appendWhere(where, DEFAULT_CONDITION_ARRAY);
	        querySpec.appendAnd();
	        WhereExpression where3 = new SearchCondition(WTPart.class,
	                WTPart.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
	        querySpec.appendWhere(where3, DEFAULT_CONDITION_ARRAY);
	        if(StringUtils.isNotEmpty(view)){
		        querySpec.appendAnd();
		        WhereExpression where2 = new SearchCondition(WTPart.class,
		                "view.key.id", SearchCondition.EQUAL, ViewHelper.service
		                        .getView(view).getPersistInfo()
		                        .getObjectIdentifier().getId());
		        querySpec.appendWhere(where2, DEFAULT_CONDITION_ARRAY);
	        }
	        QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
	        while (qr.hasMoreElements()) {
	            WTPart part = (WTPart) qr.nextElement();
	            list.add(part);
	        }
    	}
        return list;
    }
    
    /**
     * get partList by number,state
     * @author WangY
     * @param number
     * @param state
     * @return
     * @throws WTException
     */
    public static List<WTPart> getPartListByNumberState(String number,
            String state) throws WTException {
    	List<WTPart> list = new ArrayList<WTPart>();
    	if(!StringUtils.isEmpty(number) && !StringUtils.isEmpty(state)){
	        StatementSpec stmtSpec = new QuerySpec(WTPart.class);
	        QuerySpec querySpec = (QuerySpec) stmtSpec;
	        WhereExpression where = new SearchCondition(WTPart.class,
	                WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
	        querySpec.appendWhere(where, DEFAULT_CONDITION_ARRAY);
	        querySpec.appendAnd();
	        WhereExpression where3 = new SearchCondition(WTPart.class,
	                WTPart.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
	        querySpec.appendWhere(where3, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
	        while (qr.hasMoreElements()) {
	            WTPart part = (WTPart) qr.nextElement();
	            list.add(part);
	        }
    	}
        return list;
    }
    
    /**
     * get partList by number,state,view
     * @author WangY
     * @param number
     * @param view
     * @return
     * @throws WTException
     */
    public static List<WTPart> getPartListByNumberView(String number,
            String view) throws WTException {
    	List<WTPart> list = new ArrayList<WTPart>();
    	if(!StringUtils.isEmpty(number)){
	        StatementSpec stmtSpec = new QuerySpec(WTPart.class);
	        QuerySpec querySpec = (QuerySpec) stmtSpec;
	        WhereExpression where = new SearchCondition(WTPart.class,
	                WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
	        querySpec.appendWhere(where, DEFAULT_CONDITION_ARRAY);
	        if(StringUtils.isNotEmpty(view)){
		        querySpec.appendAnd();
		        WhereExpression where2 = new SearchCondition(WTPart.class,
		                "view.key.id", SearchCondition.EQUAL, ViewHelper.service
		                        .getView(view).getPersistInfo()
		                        .getObjectIdentifier().getId());
		        querySpec.appendWhere(where2, DEFAULT_CONDITION_ARRAY);
	        }
	        QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
	        while (qr.hasMoreElements()) {
	            WTPart part = (WTPart) qr.nextElement();
	            list.add(part);
	        }
    	}
        return list;
    }
    
    /**
     * get partList by number,state
     * @author WangY
     * @param number
     * @return
     * @throws WTException
     */
    public static List<WTPart> getPartListByNumber(String number) throws WTException {
    	List<WTPart> list = new ArrayList<WTPart>();
    	if(!StringUtils.isEmpty(number)){
	        StatementSpec stmtSpec = new QuerySpec(WTPart.class);
	        QuerySpec querySpec = (QuerySpec) stmtSpec;
	        WhereExpression where = new SearchCondition(WTPart.class,
	                WTPart.NUMBER, SearchCondition.EQUAL, number.toUpperCase());
	        querySpec.appendWhere(where, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
	        while (qr.hasMoreElements()) {
	            WTPart part = (WTPart) qr.nextElement();
	            list.add(part);
	        }
    	}
        return list;
    }
    
    /**
     * get partList by number,state,view
     * @author WangY
     * @param number
     * @param state
     * @param view
     * @return
     * @throws WTException
     */
    public static List<WTPart> getPartListByStateView(String state,String view) throws WTException {
    	List<WTPart> list = new ArrayList<WTPart>();
    	if(!StringUtils.isEmpty(state)){
	        StatementSpec stmtSpec = new QuerySpec(WTPart.class);
	        QuerySpec querySpec = (QuerySpec) stmtSpec;
	        WhereExpression where3 = new SearchCondition(WTPart.class,
	                WTPart.LIFE_CYCLE_STATE, SearchCondition.EQUAL, state);
	        querySpec.appendWhere(where3, DEFAULT_CONDITION_ARRAY);
	        if(StringUtils.isNotEmpty(view)){
		        querySpec.appendAnd();
		        WhereExpression where2 = new SearchCondition(WTPart.class,
		                "view.key.id", SearchCondition.EQUAL, ViewHelper.service
		                        .getView(view).getPersistInfo()
		                        .getObjectIdentifier().getId());
		        querySpec.appendWhere(where2, DEFAULT_CONDITION_ARRAY);
	        }
	        QueryResult qr = PersistenceServerHelper.manager.query(stmtSpec);
	        while (qr.hasMoreElements()) {
	            WTPart part = (WTPart) qr.nextElement();
	            list.add(part);
	        }
    	}
        return list;
    }
    
    /**
     * 
     * this method is used to get working version of part.(check out part).
     * 
     * @param part
     *            : WTPart need check out WTPart object
     * @return WTPart : work copy of part
     * @throws WTException
     *             : exception handling
     * @throws WTPropertyVetoException
     *             : exception handling
     */
    public static WTPart getWorkingCopyOfPart(WTPart part){
        WTPart workingPart = null;
        try{
	        if(part!=null && isPartExist(part.getNumber())){
		        if (!WorkInProgressHelper.isCheckedOut(part)) {
		            Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
		            CheckoutLink checkoutlink = WorkInProgressHelper.service.checkout(
		                    part, folder, SessionHelper.manager.getPrincipal()
		                            .getName());
		            workingPart = (WTPart) checkoutlink.getWorkingCopy();
		        } else {
		            if (!WorkInProgressHelper.isWorkingCopy(part)) {
		                workingPart = (WTPart) WorkInProgressHelper.service
		                        .workingCopyOf(part);
		            } else {
		                workingPart = part;
		            }
		        }
	        }
        }catch(WTException e){
        	logger.error(">>>>>"+e);
        }catch(WTPropertyVetoException e){
        	logger.error(">>>>>"+e);
        }
        return workingPart;
    }
    
    /**
     * Judge if the two given parts has usage link between them
     * 
     * @param parentPart
     *            parent part
     * @param childMaster
     *            child part master
     * @return has usage link or not
     * @throws WTException
     *             windchill exxception
     * @see wt.part.WTPart
     * @see wt.part.WTPartMaster
     * @see wt.part.WTPartUsageLink
     */
    public static boolean hasUsageLink(WTPart parentPart,
            WTPartMaster childMaster) throws WTException {
        boolean hasUsageLink = false;
        WTPartUsageLink usageLink = getPartUsageLink(parentPart, childMaster);
        if (usageLink != null) {
            hasUsageLink = true;
        }
        return hasUsageLink;
    }
    
    /**
     * Remove WTPartUsageLink between part and referenceDoc
     * 
     * @param part
     * @param doc
     * @throws WTException
     */
    public static void removeUseLinkOfRef(WTPart part,WTDocument doc)
            throws WTException {
        if (doc == null || part == null) {
            return;
        }
        WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
        QuerySpec queryspec = new QuerySpec(WTPartReferenceLink.class);
        queryspec.appendWhere(
                new SearchCondition(WTPartReferenceLink.class,
                        "roleAObjectRef.key", "=", PersistenceHelper
                                .getObjectIdentifier(part)), DEFAULT_CONDITION_ARRAY);
        queryspec.appendAnd();
        queryspec.appendWhere(
                new SearchCondition(WTPartReferenceLink.class,
                        "roleBObjectRef.key", "=", PersistenceHelper 
                                .getObjectIdentifier(docMaster)),
                                DEFAULT_CONDITION_ARRAY);
        QueryResult qr = PersistenceServerHelper.manager.query(queryspec);
        while (qr.hasMoreElements()) {
        	WTPartReferenceLink link = (WTPartReferenceLink) qr.nextElement();
            PersistenceServerHelper.manager.remove(link);
        }
    }
    
    /**
     * Get describe link from a part and a doc
     * @author WangY
     */
    /*public static WTPartDescribeLink getPartDescribeLink(WTPart part,
            WTDocument doc) throws WTException {
    	WTPartDescribeLink desLink = null;
        if (part != null && doc != null && isPartExist(part.getNumber())) {
        	QueryResult queryresult = PersistenceServerHelper.manager
                    .query(WTPartDescribeLink.class, part,
                    		WTPartDescribeLink.DESCRIBED_BY_ROLE,
                    		doc);
            if (queryresult.hasMoreElements()) {
            	desLink = (WTPartDescribeLink) queryresult.nextElement();
            }
        }
        return desLink;
    }
    public static WTPartDescribeLink getPartDescribeLink(WTPart part,
            WTDocumentMaster docMaster) throws WTException {
    	WTPartDescribeLink desLink = null;
        QueryResult queryresult = PersistenceServerHelper.manager
                .query(WTPartDescribeLink.class, part,
                		WTPartDescribeLink.DESCRIBED_BY_ROLE,
                        docMaster);
        if (queryresult.hasMoreElements()) {
        	desLink = (WTPartDescribeLink) queryresult.nextElement();
        }
        return desLink;
    }*/
    
    
    /**
     * Add describe doc for a part
     * 
     * @param part
     *            part instance
     * @param doc
     *            document instance
     * @throws WTException
     *             windChill exception
     */
    public static void addDescribeDoc(WTPart part, WTDocument doc)
            throws WTException {
    	if(isPartExist(part.getNumber())){
	        /*WTPartDescribeLink refLink = getPartDescribeLink(part, doc);
	        if (refLink == null) {*/
	            //WTDocumentMaster docMaster = (WTDocumentMaster) doc.getMaster();
    		    WTPartDescribeLink refLink = WTPartDescribeLink.newWTPartDescribeLink(part,doc);
	            PersistenceServerHelper.manager.insert(refLink);
	        //}
    	}
	}
    
	public static void test() throws RemoteException, InvocationTargetException, WTException{
		/*List<WTPart> list = getPartByNameStateTypeView("GOLF_CART","INWORK","wt.part.WTPart","");
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}*/
		getNameByPart(getPartByNumber("GC000032"));
		System.out.println(getPartMasterByNumber("GC000032"));
		addReferenceDoc(getPartByNumber("GC000032"), DocUtil.getDocumentByNumber("0000000101"));
	
		//System.out.println("1----------------------"+getWorkingCopyOfPart(getPartByNumber("GC000001")));
		//System.out.println("2----------------------"+getWorkingCopyOfPart(getPartByNumber("asdfd")));
		//createUsageLink(getPartByNumber("GC000001"),getPartMasterByNumber("1234"));
		/*System.out.println(getPartDescribeLink(getPartByNumber("GC000032"), DocUtil.getDocumentByNumber("0000000001")));
		System.out.println(getPartDescribeLink(getPartByNumber("GC000032"), DocUtil.getDocumentByNumber("0000000101")));
		System.out.println(getPartDescribeLink(getPartByNumber("GC000032"), DocUtil.getDocumentByNumber("qewrq")));
		addDescribeDoc(getPartByNumber("GC000032"), DocUtil.getDocumentByNumber("0000000101"));
		System.out.println(getPartDescribeLink(getPartByNumber("GC000032"), DocUtil.getDocumentByNumber("0000000101")));*/
		//System.out.println(hasUsageLink(getPartByNumber("GC000032"),getPartMasterByNumber("GC000030")));
		/*List<WTPart> list = getChildrenByParent(getPartByNumber("GC000001"));
		for (int i = 0; i < list.size(); i++) {
			System.out.println("1-----------------------"+list.get(i));
		}*/
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", PartUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
}
