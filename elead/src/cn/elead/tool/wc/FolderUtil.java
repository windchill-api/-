package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.fc.collections.WTValuedHashMap;
import wt.fc.collections.WTValuedMap;
import wt.folder.Folder;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.session.SessionServerHelper;
import wt.util.WTException;

public class FolderUtil implements RemoteAccess, Serializable {

	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = FolderUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	public static Persistable getPersistableByOid(String strOid)
			throws WTException {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		if (strOid != null && strOid.trim().length() > 0) {
			ReferenceFactory referencefactory = new ReferenceFactory();
			WTReference wtreference = referencefactory.getReference(strOid);
			Persistable persistable = wtreference != null ? wtreference
					.getObject() : null;
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			return persistable;
		}
		return null;
	}
	
	/**
	 * get WTContainer by Part
	 * @param wtPart
	 * @return
	 * @throws WTException
	 */
	public static WTContainer getWTContainerByPart(WTPart wtPart)
            throws WTException {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		WTContainer wtContainer = null;
		if(wtPart!=null && PartUtil.isPartExist(wtPart.getNumber())){
			wtContainer =  wtPart.getContainer();
		}
		SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		return wtContainer;
    }
	
	/**
	 * get WTContainer by document
	 * @param wtDoc
	 * @return
	 * @throws WTException
	 */
	public static WTContainer getWTContainerByDoc(WTDocument wtDoc)
            throws WTException {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		WTContainer wtContainer = null;
		if(wtDoc!=null && PartUtil.isPartExist(wtDoc.getNumber())){
			wtContainer =  wtDoc.getContainer();
		}
		SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		return wtContainer;
    }
	
	/**
	 * get wtcontainerRef by WTContainer
	 * @param wtcontainer
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerref(WTContainer wtcontainer)
            throws WTException {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		WTContainerRef wtContainerRef = null;
		if(wtcontainer!=null){
			wtContainerRef =  WTContainerRef.newWTContainerRef(wtcontainer);
		}
		SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		return wtContainerRef;
    }
    
	/**
	 * get WTContainerRef by part
	 * @param wtPart
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerrefByPart(WTPart wtPart)
            throws WTException {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		WTContainerRef wtContainerRef = null;
		if(wtPart!=null && PartUtil.isPartExist(wtPart.getNumber())){
			WTContainer wtcontainer = getWTContainerByPart(wtPart);
			wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
		}
		SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		return wtContainerRef;
    }
	
	/**
	 * get WTContainerRef by document
	 * @param wtDoc
	 * @return
	 * @throws WTException
	 */
	public static WTContainerRef getWTContainerrefByDoc(WTDocument wtDoc)
            throws WTException {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		WTContainerRef wtContainerRef = null;
		if(wtDoc!=null && PartUtil.isPartExist(wtDoc.getNumber())){
			WTContainer wtcontainer = getWTContainerByDoc(wtDoc);
			wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
		}
		SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		return wtContainerRef;
    }
	
    /**
     * get folder by containerRef and folderPath
     * 
     * @param WTContainerRef
     * @param String
     *            folder
     * @return Folder
     */
    public static Folder getFolder(WTContainerRef containerRef, String path){
    	boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
    	Folder f = null;
        String folder = path;
        if(containerRef!=null){
	        if (!(folder.startsWith("/"))) {
	            folder = "/" + folder;
	        }
	        if (folder.indexOf("Default") == -1) {
	            folder = "/Default" + folder;
	        }
	        try{
	        	f = FolderHelper.service.getFolder(folder, containerRef);
	        }catch(WTException e){
	        	f = null;
	        }finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
        }
        return f;
    }
	
    /**
	 * get folder by container and folderPath
	 * @directions �õ��ļ��ж���
	 * @param  ������·��
	 */
	public static Folder getFolder(WTContainer container, String path) {
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		Folder folder = null;
		WTContainerRef containerRef = null;
		if(container!=null){
			try {
				if (!(path.startsWith("/"))) {
					path = "/" + path;
		        }
		        if (path.indexOf("Default") == -1) {
		        	path = "/Default" + path;
		        }
				containerRef = getWTContainerref(container);
				folder = FolderHelper.service.getFolder(path, containerRef);
			} catch (WTException e) {
				folder = null;
			} finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
		}
		return folder;
	}
    
	/**
	 * create folder by container and folderPath
	 * @param container
	 * @param path
	 * @return
	 */
	public static Folder createFolderByContainer(WTContainer container, String path){
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		Folder folder = null;
		if (container != null) {
			try {
				if (!(path.startsWith("/"))) {
					path = "/" + path;
		        }
		        if (path.indexOf("Default") == -1) {
		        	path = "/Default" + path;
		        }
		        Folder oldFolder = getFolder(container,path);
		        if(oldFolder==null){
					WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
					folder=FolderHelper.service.saveFolderPath(path,containerRef);
		        }
			} catch (WTException e) {
				e.printStackTrace();
			} finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
		}
		return folder;
	}
	
	/**
	 * create folder by container and folderPath
	 * @param container
	 * @param path
	 * @return
	 */
	public static Folder createFolderByContainerRef(WTContainerRef containerRef, String path){
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		Folder folder = null;
		if (containerRef != null) {
			try {
				if (!(path.startsWith("/"))) {
					path = "/" + path;
		        }
		        if (path.indexOf("Default") == -1) {
		        	path = "/Default" + path;
		        }
		        Folder oldFolder = getFolder(containerRef,path);
		        if(oldFolder==null){
					folder=FolderHelper.service.saveFolderPath(path,containerRef);
		        }
			} catch (WTException e) {
				e.printStackTrace();
			} finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
		}
		return folder;
	}
	
	/**
	 * create folder by part and folderPath
	 * @param container
	 * @param path
	 * @return
	 */
	public static Folder createFolderByPart(WTPart part, String path){
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		Folder folder = null;
		if (part != null && PartUtil.isPartExist(part.getNumber())) {
			try {
				if (!(path.startsWith("/"))) {
					path = "/" + path;
		        }
		        if (path.indexOf("Default") == -1) {
		        	path = "/Default" + path;
		        }
		        WTContainerRef containerRef = getWTContainerrefByPart(part);
		        Folder oldFolder = getFolder(containerRef,path);
		        if(oldFolder==null){
					folder=FolderHelper.service.saveFolderPath(path,containerRef);
		        }
			} catch (WTException e) {
				e.printStackTrace();
			} finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
		}
		return folder;
	}
	
	/**
	 * create folder by part and folderPath
	 * @param container
	 * @param path
	 * @return
	 */
	public static Folder createFolderByDoc(WTDocument doc, String path){
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		Folder folder = null;
		if (doc != null) {
			try {
				if (!(path.startsWith("/"))) {
					path = "/" + path;
		        }
		        if (path.indexOf("Default") == -1) {
		        	path = "/Default" + path;
		        }
		        WTContainerRef containerRef = getWTContainerrefByDoc(doc);
		        Folder oldFolder = getFolder(containerRef,path);
		        if(oldFolder==null){
					folder=FolderHelper.service.saveFolderPath(path,containerRef);
		        }
			} catch (WTException e) {
				e.printStackTrace();
			} finally{
				SessionServerHelper.manager.setAccessEnforced(accessEnforced);
			}
		}
		return folder;
	}
	
	/**
	 * get Folder path by part
	 * @param part
	 * @return
	 */
	public static String getFolderPathByPart(WTPart part){
		String folderPath = null;
		if(part!=null && PartUtil.isPartExist(part.getNumber())){
			folderPath = part.getFolderPath();
		}
		return folderPath;
	}
	
	/**
	 * get Folder path by doc
	 * @param part
	 * @return
	 */
	public static String getFolderPathByDoc(WTDocument doc){
		String folderPath = null;
		if(doc!=null){
			folderPath = doc.getFolderPath();
		}
		return folderPath;
	}
	
	
	/**
	 * change part folder path
	 * 
	 */
	public static void changePartFolderPath(WTPart part,String path){
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			if(part!=null && PartUtil.isPartExist(part.getNumber())){
				Folder folder=createFolderByContainer(part.getContainer(), path);
				if(folder == null){
					folder = getFolder(part.getContainer(), path);
				}
				if(!getFolderPathByPart(part).equals(folder.getFolderPath())){
					WTValuedMap map = new WTValuedHashMap();
					map.put(part, folder);
					FolderHelper.service.changeFolder(map); 
				}
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		} finally{
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		}
	}
	
	/**
	 * change doc folder path
	 * 
	 */
	public static void changeDocFolderPath(WTDocument doc,String path){
		boolean accessEnforced = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			if(doc!=null){
				Folder folder=createFolderByContainer(doc.getContainer(), path);
				if(folder == null){
					folder = getFolder(doc.getContainer(), path);
				}
				if(!getFolderPathByDoc(doc).equals(folder.getFolderPath())){
					WTValuedMap map = new WTValuedHashMap();
					map.put(doc, folder);
					FolderHelper.service.changeFolder(map); 
				}
			}
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		} finally{
			SessionServerHelper.manager.setAccessEnforced(accessEnforced);
		}
	}
	
	/*public static void changeFolderPath(Foldered oldFolder,Folder newFolder) throws WTException, FileNotFoundException, IOException  {
		try {
			
			FolderHelper.service.changeFolder(oldFolder, newFolder);
			
		} catch (Exception e) {
			logger.error(">>>>>"+e);
		}
}*/
	
    public static void test() throws RemoteException, InvocationTargetException, WTException{
    	//System.out.println(createFolderByPart(PartUtil.getPartByNumber("0000000041"), "testFolder-001"));
    	//System.out.println(createFolderByDoc(DocUtil.getDocumentByNumber("0000000101"), "testFolder-002"));
    	//System.out.println(createFolderByContainer(getWTContainerByPart(PartUtil.getPartByNumber("0000000041")), "testFolder-003"));
    	System.out.println(createFolderByContainerRef(getWTContainerrefByPart(PartUtil.getPartByNumber("0000000041")), "testFolder-004"));
    	/*System.out.println(getWTContainerrefByDoc(DocUtil.getDocumentByNumber("0000000101")));
    	System.out.println(getWTContainerrefByDoc(DocUtil.getDocumentByNumber("asd")));*/
    	//System.out.println(getFolder(getWTContainerByPart(PartUtil.getPartByNumber("0000000062")), "/default/Manufacturing"));
    	/*System.out.println(getFolder(getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), "Design"));
    	System.out.println(getFolder(getWTContainerrefByPart(PartUtil.getPartByNumber("asdf")), "test"));
    	System.out.println(getFolder(getWTContainerByPart(PartUtil.getPartByNumber("GC000027")), "Design"));
    	System.out.println(getFolder(getWTContainerByDoc(DocUtil.getDocumentByNumber("0000000101")), "asd"));*/
    	/*changeFolderPath(PartUtil.getPartByNumber("0000000062"), "/Default/testDoc");
    	changeFolderPath(PartUtil.getPartByNumber("0000000062"), "/testDoc1");
    	changeFolderPath(PartUtil.getPartByNumber("0000000062"), "testDoc2");
    	changeFolderPath(PartUtil.getPartByNumber("0000000062"), "/Default/testDoc");
    	changeFolderPath(PartUtil.getPartByNumber("0000000062"), "/Default/Design/testDoc");*/
    	//System.out.println(getDocFolderPath(DocUtil.getDocumentByNumber("0000000083")));
    	//changeDocFolderPath(DocUtil.getDocumentByNumber("0000000083"), "changeFolder1");
    	//System.out.println(getFolder(PartUtil.getPartByNumber("0000000061").getContainer(),"changeFolder").getFolderPath());
    	//System.out.println(getFolder(getWTContainerByPart(PartUtil.getPartByNumber("0000000062")), folder1));
    	/*System.out.println(getFolderPathByPart(PartUtil.getPartByNumber("0000000041")));
    	System.out.println(getFolderPathByDoc(DocUtil.getDocumentByNumber("0000000101")));
    	changePartFolderPath(PartUtil.getPartByNumber("0000000041"), "/Default/Design");
    	changeDocFolderPath(DocUtil.getDocumentByNumber("0000000101"), "/Default/Design/doc1");*/
    	
	}
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException, WTException{
		RemoteMethodServer r = RemoteMethodServer.getDefault();
		r.setUserName("wcadmin");
		r.setPassword("wcadmin");
		if (!RemoteMethodServer.ServerFlag) {
			RemoteMethodServer.getDefault().invoke("test", FolderUtil.class.getName(), null,
					new Class[] {},
					new Object[] {});
		}
	}
	
}
