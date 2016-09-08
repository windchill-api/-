package cn.elead.tool.wc;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

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
import cn.elead.tool.common.CommonUtil;
public class FolderUtil implements RemoteAccess, Serializable {

	/**
	 * @author WangY
	 */
	private static final long serialVersionUID = 1L;
	private static String CLASSNAME = FolderUtil.class.getName();
	private static Logger logger = LogR.getLogger(CLASSNAME);
	
	
    /**
     * get folder by containerRef and folderPath
     * 
     * @param WTContainerRef
     * @param String
     *            folder
     * @return 		if containerRef and path is exist in windChill,return folder
     * 				else if path is not exist in windChill , path is empty or null,return null
     * 				else if containerRef is not exist in windChill ,return null
     */
    public static Folder getFolder(WTContainerRef containerRef, String path){
    	Folder f = null;
    	boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Folder) RemoteMethodServer.getDefault().invoke("getFolder", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTContainerRef.class,String.class},
	                		new Object[] { containerRef,path});
	        } else {
		        String folder = path;
		        if(containerRef!=null && folder!=null){
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
			        }
		        }
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
        return f;
    }
	
    /**
     * get folder by container and folderPath
     * @param container
     * @param path
     * @return		if container and path is exist in windChill,return folder
     * 				else if path is not exist in windChill , path is empty or null,return null
     * 				else if container is not exist in windChill ,return null
     */
	public static Folder getFolder(WTContainer container, String path) {
		Folder folder = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Folder) RemoteMethodServer.getDefault().invoke("getFolder", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTContainer.class,String.class},
	                		new Object[] { container,path});
	        } else {
				try {
					if(container!=null && path!=null){
						WTContainerRef containerRef = CommonUtil.getWTContainerref(container);
						folder = getFolder(containerRef, path);
					}
				} catch (WTException e) {
					logger.error(">>>>>"+e);
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return folder;
	}
    
	/**
	 * create folder by container and folderPath
	 * @param container
	 * @param path
	 * @return		if container is exist in windChill,path is not exist,create folder and return this folder
	 * 				else if container and path are exist or container is not exist in windChill,there is nothing to do,return null
	 * 				else if container is exist and path is empty,there is nothing to do, return null
	 * 				else if container and path all null,return null
	 */
	public static Folder createFolderByContainer(WTContainer container, String path){
		Folder folder = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Folder) RemoteMethodServer.getDefault().invoke("createFolderByContainer", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTContainer.class,String.class},
	                		new Object[] { container,path});
	        } else {
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
					}
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return folder;
	}
	
	/**
	 * create folder by container and folderPath
	 * @param container
	 * @param path
	 * @return		if containerRef is exist in windChill,path is not exist,create folder and return this folder
	 * 				else if containerRef and path are exist or containerRef is not exist in windChill,there is nothing to do,return null
	 * 				else if containerRef is exist and path is empty,there is nothing to do, return null
	 * 				else if containerRef and path all null,return null
	 */
	public static Folder createFolderByContainerRef(WTContainerRef containerRef, String path){
		Folder folder = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Folder) RemoteMethodServer.getDefault().invoke("createFolderByContainerRef", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTContainerRef.class,String.class},
	                		new Object[] { containerRef,path});
	        } else {
				if (containerRef != null && path!=null) {
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
					}
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return folder;
	}
	
	/**
	 * create folder by part and folderPath
	 * @param container
	 * @param path
	 * @return		if part is exist in windChill,path is not exist,create folder and return this folder
	 * 				else if part and path are exist or part is not exist in windChill,there is nothing to do,return null
	 * 				else if part is exist and path is empty,there is nothing to do, return null
	 * 				else if part and path all null,return null
	 */
	public static Folder createFolderByPart(WTPart part, String path){
		Folder folder = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (Folder) RemoteMethodServer.getDefault().invoke("createFolderByPart", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTPart.class,String.class},
	                		new Object[] { part,path});
	        } else {
				if (part != null && PartUtil.isPartExist(part.getNumber())) {
					try {
						if (!(path.startsWith("/"))) {
							path = "/" + path;
				        }
				        if (path.indexOf("Default") == -1) {
				        	path = "/Default" + path;
				        }
				        WTContainerRef containerRef = CommonUtil.getWTContainerrefByPart(part);
				        Folder oldFolder = getFolder(containerRef,path);
				        if(oldFolder==null){
							folder=FolderHelper.service.saveFolderPath(path,containerRef);
				        }
					} catch (WTException e) {
						e.printStackTrace();
					}
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return folder;
	}
	
	/**
	 * get Folder path by part
	 * @param part
	 * @return		if part is exist,
	 */
	public static String getFolderPathByPart(WTPart part){
		String folderPath = null;
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                return (String) RemoteMethodServer.getDefault().invoke("getFolderPathByPart", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTPart.class},
	                		new Object[] { part});
	        } else {
				if(part!=null && PartUtil.isPartExist(part.getNumber())){
					folderPath = part.getFolderPath();
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
		return folderPath;
	}
	
	/**
	 * change part folder path
	 * @param part
	 * @param path
	 * 			if part is exist,path is exist,change the part folder
	 * 			else if part is exist,path is not exist,create the path folder and change the part folder
	 * 			else if part is not exist,there is nothing to do
	 * 			else if part is exist,path is empty,change the part folder is Default folder
	 * 			else if part and path are all null,there is nothing to do
	 */
	public static void changePartFolderPath(WTPart part,String path){
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
        try{
	        if (!RemoteMethodServer.ServerFlag) {
	                RemoteMethodServer.getDefault().invoke("changePartFolderPath", 
	                		ChangeUtil.class.getName(), null, new Class[] {WTPart.class,String.class},
	                		new Object[] { part,path});
	        } else {
				try {
					if(part!=null && PartUtil.isPartExist(part.getNumber()) && path!=null){
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
				}
	        }
        } catch (RemoteException e) {
            logger.error(e.getMessage(),e);
        } catch (InvocationTargetException e) {
        	logger.error(e.getMessage(),e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(enforce);
        }
	}
	
	
    public static void test() throws RemoteException, InvocationTargetException, WTException{
//    	System.out.println("/*********************getFolder********************/");
//    	System.out.println(getFolder(CommonUtil.getWTContainerref(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022"))), "Design"));
//    	System.out.println(getFolder(CommonUtil.getWTContainerref(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022"))), "asd"));
//    	System.out.println(getFolder(CommonUtil.getWTContainerref(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber(""))), "asd"));
//    	System.out.println(getFolder(CommonUtil.getWTContainerref(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022"))), null));
//    	System.out.println(getFolder(CommonUtil.getWTContainerref(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022"))), ""));
//    	System.out.println("/*********************getFolder********************/");
//    	System.out.println(getFolder(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022")), "Design"));
//    	System.out.println(getFolder(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022")), "asd"));
//    	System.out.println(getFolder(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("")), "asd"));
//    	System.out.println(getFolder(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022")), null));
//    	System.out.println(getFolder(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("0000000022")), ""));
//    	System.out.println("/*********************createFolderByContainerRef********************/");
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), "testFolder_1"));
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), "Design"));
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("asd")), "testFolder_3"));
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), ""));
//    	System.out.println(crntln("/*********************createFolderByContainerRef********************/");
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), "testFolder_1"));
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), "Design"));
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("asd")), "testFolder_3"));
//    	System.out.println(createFolderByContainerRef(CommonUtil.getWTContainerrefByPart(PartUtil.getPartByNumber("GC000027")), ""));
//    	System.out.println(createeateFolderByContainerRef(null, null));
//    	System.out.println("/*********************createFolderByContainer********************/");
//    	System.out.println(createFolderByContainer(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("GC000027")), "testFolder-1"));
//    	System.out.println(createFolderByContainer(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("GC000027")), "Design"));
//    	System.out.println(createFolderByContainer(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("asd")), "testFolder-3"));
//    	System.out.println(createFolderByContainer(CommonUtil.getWTContainerByPart(PartUtil.getPartByNumber("GC000027")), ""));
//    	System.out.println(createFolderByContainer(null, null));
//    	System.out.println("/*********************createFolderByPart********************/");
//    	System.out.println(createFolderByPart(PartUtil.getPartByNumber("GC000027"), "test_part_folder1"));
//    	System.out.println(createFolderByPart(PartUtil.getPartByNumber("GC000027"), "Design"));
//    	System.out.println(createFolderByPart(PartUtil.getPartByNumber("asd"), "test_part_folder2"));
//    	System.out.println(createFolderByPart(PartUtil.getPartByNumber("GC000027"), ""));
//    	System.out.println(createFolderByPart(null, null));
//    	System.out.println("/*********************getFolderPathByPart********************/");
//    	System.out.println(getFolderPathByPart(PartUtil.getPartByNumber("GC000027")));
//    	System.out.println(getFolderPathByPart(PartUtil.getPartByNumber("asd")));
//    	System.out.println(getFolderPathByPart(null));
//    	System.out.println("/*********************changePartFolderPath********************/");
//    	changePartFolderPath(PartUtil.getPartByNumber("GC000027"), "test_part_folder1");
//    	changePartFolderPath(PartUtil.getPartByNumber("asd"), "test_part_folder1");
//    	changePartFolderPath(PartUtil.getPartByNumber("GC000027"), "change_part_folder_1");
//    	changePartFolderPath(PartUtil.getPartByNumber("GC000027"), "");
//    	changePartFolderPath(null, null);
    	
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
