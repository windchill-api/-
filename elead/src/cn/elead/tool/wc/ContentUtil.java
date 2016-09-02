package cn.elead.tool.wc;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;



import java.util.Vector;

import org.apache.log4j.Logger;

import com.google.gwt.rpc.client.impl.RemoteException;





import com.ptc.arbortext.windchill.techpubs.doc.docResource;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.DataFormat;
import wt.content.DataFormatReference;
import wt.content.FormatContentHolder;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.inf.team.ContainerTeamManaged;
import wt.log4j.LogR;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.WTPart;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;

	public class ContentUtil implements RemoteAccess, Serializable {
		 private static String TEMP_PATH = null;
		 private static final Logger LOGGER = LogR.getLogger(ContentUtil.class.getName());
		  private static final Logger logger = LogR.getLogger(ContentUtil.class.getName());
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
						server.invoke("test", ContentUtil.class.getName(), null,
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
			public static void test () throws WTException, PropertyVetoException{
			//	WTDocument wtDocument = DocUtil.getDocumentByNumber("CPKF00000139");
				ContentHolder holder =(ContentHolder) PartUtil.getPartByNumber("测试099");
				//deleteDoc("CPKF00000139");
		//	System.out.println(getPrimaryAndSencondaryAttachments(wtPart));	
			//	deletePrimary(wtDocument);
		//	System.out.println(resolveFileFormatName("sdfsdfa"));	
			//	System.out.println(getContainerByName("A1032"));
				System.out.println(retrievePrimary(holder));
				
			}
			/**
			 * @author zhangxj
			 * 根据名称找到上下文
			 * @param name
			 * @return
			 */
			public static WTContainerRef getContainerByName(String name) {
		        WTContainerRef result = null;
		        ContainerTeamManaged containerTeamManaged = null;
		        try {
		            QuerySpec select = new QuerySpec(WTContainer.class);
		            select.appendWhere(new SearchCondition(WTContainer.class, WTContainer.NAME, SearchCondition.EQUAL, name));
		            QueryResult queryResult = PersistenceHelper.manager.find(select);
		            if (queryResult.hasMoreElements()) {
		                WTContainer container = (WTContainer) queryResult.nextElement();
		                result = WTContainerRef.newWTContainerRef(container);
		             /*  System.out.println("$"+result+"$");
		                if (container instanceof PDMLinkProduct) {
		     			   containerTeamManaged = (PDMLinkProduct) container;
		     		   } else if (container instanceof WTLibrary) {
		     			   containerTeamManaged = (WTLibrary) container;
		     		   }
		                List<WTPrincipal> zjgygcsprincipals =     getMembersOfContainerRole((ContainerTeamManaged) container,Role.toRole("PQA"));
		                for (WTPrincipal principal : zjgygcsprincipals) {
							System.out.println("******"+principal+"**");
					   }*/
		            }
		        } catch (WTException ex) {
		        }
		        return result;
		    }




	
	/**
	 * 根据文件路径获取文件类型
	 * @param filePath
	 * @author zhangxj
	 * 
	 */
    public static String resolveFileFormatName(String filePath) {
        String format = "";

        if (filePath != null && filePath.trim().length() > 0) {
            filePath = filePath.toLowerCase();

            if (filePath.endsWith(".xls"))
                format = "Microsoft Excel";
            else if (filePath.endsWith(".doc"))
                format = "Microsoft Word";
            else if (filePath.endsWith(".txt"))
                format = "Text File";
            else if (filePath.endsWith(".pdf"))
                format = "PDF";
            else
                format = "Unknown";
        }else{
        	format="目录为空";
        	return format;
        }
		return format;

        
    }
    
    
    /**
     * @author zhangxj
     * 获取业务对象的主要内容
     * @param holder 业务对象，比如WTDocument
     * @return 主要对象ApplicationData
     * @throws WTException
     * @throws PropertyVetoException
     */
    public static ApplicationData retrievePrimary(ContentHolder holder) throws WTException, PropertyVetoException {
        holder = ContentHelper.service.getContents(holder);
        return (ApplicationData) ContentHelper.getPrimary((FormatContentHolder) holder);
    }

    public static File createTempDirectory() throws IOException {
        String dirPath = "tmp_" + (new java.util.Date()).getTime();
        File tmpDir = new File(TEMP_PATH + File.separator + dirPath);
        tmpDir.mkdir();

        return tmpDir;
    }
    
    /**
     * @author zhangxj
     * 获取所有主文档和附件
     * @param contentHolder Object which includes attachement.
     * @return List<ApplicationData> which contains all the attachments(primary and secondary).
     */
    public static List<ApplicationData> getPrimaryAndSencondaryAttachments(ContentHolder contentHolder) throws WTException, PropertyVetoException {
        List<ApplicationData> attachments = new ArrayList<ApplicationData>();

        ContentHolder holder = ContentHelper.service.getContents(contentHolder);
        ContentRoleType[] roleTypes = new ContentRoleType[]{ContentRoleType.PRIMARY, ContentRoleType.SECONDARY};

        for (ContentRoleType roleType : roleTypes) {
            QueryResult qurAttached = ContentHelper.service.getContentsByRole(holder, roleType);
            while (qurAttached.hasMoreElements()) {
                Object objQr = qurAttached.nextElement();
                if (objQr instanceof ApplicationData && !attachments.contains(objQr))
                    attachments.add((ApplicationData) objQr);
            }
        }

        return attachments;
    }
    
    /**
     * @author zhangxj
     * 获取业务对象的所有附件
     * @param holder 业务对象，比如WTDocument
     * @return ApplicationData列表
     * @throws WTException
     * @throws PropertyVetoException
     */
    public static ArrayList<ApplicationData> retrieveSecondary(ContentHolder holder) throws WTException, PropertyVetoException {
        ArrayList<ApplicationData> contents = new ArrayList<ApplicationData>();
        holder = ContentHelper.service.getContents(holder);
        Vector vector = ContentHelper.getContentList(holder);
        for (int i = 0; i < vector.size(); i++) {
            contents.add((ApplicationData) vector.get(i));
        }
        return contents;
    }
 
	}
