package cn.elead.tool.wc;



import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import wt.epm.EPMDocument;
import wt.epm.structure.EPMReferenceLink;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.PartDocHelper;
import wt.part.WTPart;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.WhereExpression;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;
import wt.vc.struct.IteratedDescribeLink;

import com.google.gwt.rpc.client.impl.RemoteException;


public class EPMUtil implements RemoteAccess{
	private static final int[] DEFAULT_CONDITION_ARRAY = new int[] { 0 };
	private final static String CLASSNAME = EPMUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	// Logger
	private static final Logger LOGGER = LogR.getLogger(CLASSNAME);
		
	public static void main(String[] args) {
		try {
			RemoteMethodServer server = RemoteMethodServer.getDefault();
			server.setUserName("wcadmin");
			server.setPassword("wcadmin");
			try {
				// server.invoke("setPartTenclosure",
				// Test.class.getName(), null, new Class[]
				// {String.class},
				// new Object[] {args[0]});
				server.invoke("test", EPMUtil.class.getName(), null,
						new Class[] {}, new Object[] {});
			} catch (java.rmi.RemoteException e) {
				e.printStackTrace();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
			
	public static void test() throws WTException{
	//	System.out.println("@@"+getObjectByNumber("测试098"));
		//getObjectByNumber("测试099");
	//	String oidString="VR:wt.epm.EPMDocument:7412137";
		String oidString="";
		String  EPMName	="测试099";
		String EPMNum="CAD098";
		//System.out.println("$$$$$$$$$"+geEPMDocumentByOid(oidString));
	//System.out.println("!!!!!!!!!!"+geEPMDocumentByName(EPMName));
		//System.out.println(getEPMDocByNumber(EPMNum));
		EPMDocument EPMDoc = getEPMDocByNumber(EPMNum);
		System.out.println("^^^^^"+getTypeByEPMDoc(EPMDoc));
	}
	
	
	
    public static boolean isEPMDoc(String strNumber){
    	EPMDocument EPMDoc = null;
        if(!StringUtils.isEmpty(strNumber)){
        	try {
        		EPMDoc = getEPMDocByNumber(strNumber);
			} catch (WTException e) {
				logger.error(">>>>>"+e);
			}
        }
        if (EPMDoc == null) {
            return false;
        } else {
            return true;
        }
    }


	/**
	 * getCADDocumentsByPart
	 * @author zhangxj
	 * @param part
	 * @return
	 * @throws Exception
	 */	
    public static List<EPMDocument> getCADDocumentsByPart(WTPart part) throws Exception{
		List<EPMDocument> epmDocs = new ArrayList<EPMDocument>();
		EPMDocument epmDoc = null;
		QueryResult result = PartDocHelper.service.getAssociatedDocuments(part);
		while (result.hasMoreElements())
		{
			WTObject obj = (WTObject) result.nextElement();
			if (obj instanceof EPMDocument)
			{
				epmDoc = (EPMDocument) obj;
				epmDocs.add(epmDoc);
				LOGGER.debug("编号为...." + part.getNumber()
						+ "的部件关联的三维图纸(EPMDocument)的编号是....."
						+ epmDoc.getNumber());
			} else if (obj instanceof IteratedDescribeLink)
			{
				obj = (WTObject) ((IteratedDescribeLink) obj).getDescribedBy();
				epmDoc = (EPMDocument) obj;
				epmDocs.add(epmDoc);
				LOGGER.debug("编号为...." + part.getNumber()
						+ "的部件关联的三维图纸(IteratedDescribeLink)的编号是....."
						+ epmDoc.getNumber());
			}
		
		}
		return epmDocs;
	}
    /**
     * 
     * @param EPMDoc
     * @return
     */
	public static String getTypeByEPMDoc(EPMDocument EPMDoc){
		String EPMDocType = null;
		try {
			if(EPMDoc!=null && isEPMDoc(EPMDoc.getNumber())){
				EPMDocType = TypedUtilityServiceHelper.service.getExternalTypeIdentifier(EPMDocType).toString();
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
		return EPMDocType;
	}
    
	/**
	 * 
	 * @Description: 更改图纸的存储位置
	 * @author zhangxj
	 * @param epm
	 * @param folder1
	 */
	public static void changeLocation(EPMDocument epm, String folder1) {
		try {

			String oldFolder = epm.getLocation();
			String oldContext = epm.getContainerName();
			String folder = folder1;
			WTContainer wtcontainer =getWTContainerByName(oldContext);
			WTContainerRef wtcRef = WTContainerRef
					.newWTContainerRef(wtcontainer);
			LOGGER.debug("图纸:" + epm.getNumber() + "原有的存储路径为...." + oldFolder);
			if (!oldFolder.equals(folder)) {
				Folder newfolder = null;
				try {
					newfolder = FolderHelper.service.getFolder(folder, wtcRef);
				} catch (Exception e) {
					newfolder = FolderHelper.service.createSubFolder(folder,
							wtcRef);
				}

				epm = (EPMDocument) FolderHelper.service.changeFolder(
						(FolderEntry) epm, newfolder);
				PersistenceHelper.manager.refresh(epm);
			}

		} catch (Exception e) {
			LOGGER.error("changeLocation() occur exception:", e);
		}
	}
	
	

	/**
	 * geEPMDocumentByOid
	 * @author zhangxj
	 * @param oid
	 * @return EPMDoc
	 */
    public static EPMDocument geEPMDocumentByOid(String oid){
    	EPMDocument EPMDoc = null;
		try {
			if(!StringUtils.isEmpty(oid)){
				ReferenceFactory rf = new ReferenceFactory();
				EPMDoc = (EPMDocument) rf.getReference(oid).getObject();
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return EPMDoc;
	}
   
    	/**
    	 * geEPMDocumentByName
    	 * @author zhangxj
    	 * @param EPMDocName
    	 * @return	EPMList
    	 * @throws WTException
    	 */
    public static List<EPMDocument> geEPMDocumentByName(String EPMDocName) throws WTException {
        List<EPMDocument> EPMList = new ArrayList<EPMDocument>();
        if(!StringUtils.isEmpty(EPMDocName)){
	        QuerySpec qs = new QuerySpec(EPMDocument.class);
	        SearchCondition sc = new SearchCondition(EPMDocument.class, EPMDocument.NAME,
	                SearchCondition.EQUAL, EPMDocName, false);
	        qs.appendWhere(sc, DEFAULT_CONDITION_ARRAY);
	        QueryResult qr = PersistenceServerHelper.manager
	                .query((StatementSpec) qs);
	        qr = new LatestConfigSpec().process(qr);
	        EPMDocument EPMDoc= null;
	        while (qr.hasMoreElements()) {
	        	EPMDoc = (EPMDocument) qr.nextElement();
	        	EPMList.add(EPMDoc);
	        }
    	}
        return EPMList;
    }
    
    /**
     * getEPMDocByNumber
     * @author zhangxj
     * @param EPMNum
     * @return	EPMDoc
     * @throws WTException
     */
    public static EPMDocument getEPMDocByNumber(String EPMNum) throws WTException {
        QuerySpec querySpec = new QuerySpec(EPMDocument.class);
        EPMNum = EPMNum.toUpperCase();
        WhereExpression searchCondition = new SearchCondition(EPMDocument.class, EPMDocument.NUMBER, SearchCondition.EQUAL, EPMNum, false);
        querySpec.appendWhere(searchCondition,DEFAULT_CONDITION_ARRAY);
        QueryResult queryResult = PersistenceHelper.manager.find((StatementSpec)querySpec);
        while (queryResult.hasMoreElements()) {
        	EPMDocument EPMDoc = (EPMDocument) queryResult.nextElement();
            return EPMDoc;
        }
        return null;
    }
	
	/**
     * 根据三维图纸获取相关的二维图纸
     *  @author zhangxj
     * @param epm
     * @return
     */
    public static List<EPMDocument> get2DEPM(EPMDocument epm) {
        QueryResult qr = null;
        List<EPMDocument> result = new ArrayList<EPMDocument>();
        if (epm == null) {
            return null;
        }
        try {
        	qr = PersistenceHelper.manager.navigate(epm.getMaster(), "referencedBy",
                    wt.epm.structure.EPMReferenceLink.class, false);
            while (qr.hasMoreElements()) {
                EPMReferenceLink epmreferencelink = (EPMReferenceLink) qr.nextElement();
                EPMDocument epm2d = (EPMDocument) epmreferencelink.getOtherObject(epm.getMaster());
                if(epm2d != null){
                	LOGGER.debug("编号为:"+epm.getNumber()+"的三维图纸关联的二维图纸编号为....."+epm2d.getNumber());
                	result.add(epm2d);
                }
            }
            return result;
        } catch (WTException e) {
            e.printStackTrace();
            return null;
        }
    }
	/**

	 *getWTContainerByName
	 * @author zhangxj
	 * @param name  
	 * @return WTContainer 
	 * @throws WTException  
	 */
	public static WTContainer getWTContainerByName(String name) throws WTException{
	
		QuerySpec qs = new QuerySpec(WTContainer.class);
		SearchCondition sc = new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, name);
		qs.appendSearchCondition(sc);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		
		if(qr!=null && qr.size() == 1) {
			return (WTContainer)qr.nextElement();
		} else {
			throw new WTException("WTContainer name:" + name  + " has more than one container or no container!");
		} 
	}
}
