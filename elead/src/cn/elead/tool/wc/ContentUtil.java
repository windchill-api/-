package cn.elead.tool.wc;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import wt.content.Streamed;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.fc.ObjectNoLongerExistsException;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
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
import wt.representation.Representable;
import wt.representation.Representation;
import wt.representation.RepresentationHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;

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
    

	/**
	 * Add the contentHolder attenchMent  and can save the same name files.
	 * @author BaiJuanjuan
	 * @param holder
	 * @param filePath
	 * @param fileName
	 * @param contentType
	 * @throws WTException
	 */
	public static void addContent(ContentHolder holder, String filePath,
			String fileName, ContentRoleType contentType) throws WTException {
		boolean enforce = wt.session.SessionServerHelper.manager
				.setAccessEnforced(false);
		ApplicationData applicationData = ApplicationData
				.newApplicationData(holder);
		try {
			applicationData.setRole(contentType);
			applicationData = (ApplicationData) PersistenceHelper.manager
					.save(applicationData);
			applicationData = ContentServerHelper.service.updateContent(holder,
					applicationData, filePath);
			applicationData.setFileName(fileName);
			applicationData = (ApplicationData) PersistenceHelper.manager
					.modify(applicationData);
		} catch (IOException e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		} catch (PropertyVetoException e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}

	/**
	 * Add attachments and update the same name file. 
	 *  @author BaiJuanjuan
	 * @param doc
	 *            wt document instance
	 * @param filePath
	 *            file absolute server path
	 * @param fileName
	 *            file name
	 * @throws WTException
	 *             Windchill exception
	 */
	public static void addAttachments(ContentHolder holder, String filePath,
			String fileName) throws WTException {
		boolean enforce = wt.session.SessionServerHelper.manager
				.setAccessEnforced(false);
		try {
			QueryResult qr = ContentHelper.service.getContentsByRole(holder,
					ContentRoleType.SECONDARY);
			while (qr.hasMoreElements()) {
				ApplicationData oAppData = (ApplicationData) qr.nextElement();
				String strFileName = oAppData.getFileName();
				if (strFileName != null
						&& strFileName.trim().equalsIgnoreCase(fileName)) {
					ContentServerHelper.service.deleteContent(holder, oAppData);
				}
			}
			addContent(holder, filePath, fileName, ContentRoleType.SECONDARY);
		} catch (Exception e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
	}

	/**
	 * Add primary content for a document.
	 *  @author BaiJuanjuan
	 * @param doc
	 *            document instance
	 * @param filePath
	 *            file absolute path
	 * @param fileName
	 *            file name
	 * @throws WTException
	 *             Windchill exception
	 */
	public static void addPrimaryContent(WTDocument doc, String filePath,
			String fileName) throws WTException {
		boolean enforce = wt.session.SessionServerHelper.manager
				.setAccessEnforced(false);
		try {
			ContentHolder contentHolder = ContentHelper.service
					.getContents(doc);
			ApplicationData applicationData = (ApplicationData) ContentHelper
					.getPrimary((FormatContentHolder) contentHolder);
			if (applicationData != null) {
				try {
					ContentServerHelper.service.deleteContent(doc,
							applicationData);
				} catch (WTPropertyVetoException e) {
					LOGGER.error(e.getLocalizedMessage());
					throw new WTException(e);
				}
			}
		} catch (PropertyVetoException e) {
			LOGGER.error(e.getLocalizedMessage());
			throw new WTException(e);
		}
		addContent(doc, filePath, fileName, ContentRoleType.PRIMARY);
		SessionServerHelper.manager.setAccessEnforced(enforce);
		
	}
	

	
	/**
	 * deleteContentByFileName
	 *  @author BaiJuanjuan
	 * @param ContentHolder
	 * @param String
	 * @return
	 * @throws WTException
	 */
	public static ContentHolder deleteContentByFileName(ContentHolder holder1,
			String fileName) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ContentHolder) RemoteMethodServer.getDefault().invoke(
						"deleteContentByFileName", ContentUtil.class.getName(), null,
						new Class[] { ContentHolder.class, String.class },
						new Object[] { holder1,fileName });
			} catch (java.rmi.RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			} else {
					boolean enforce = wt.session.SessionServerHelper.manager
								.setAccessEnforced(false);
					ContentHolder holder = holder1;
					if (fileName == null) {
						return holder;
					}
					try {
						holder = ContentHelper.service.getContents(holder);
						List<?> vContentList = ContentHelper.getContentList(holder);
						int iSize = vContentList.size();
						for (int j = 0; j < iSize; j++) {
							ContentItem contentitem = (ContentItem) vContentList.get(j);
							String strFileName = ((ApplicationData) contentitem)
									.getFileName();
							if (fileName.equals(strFileName)) {
								ContentServerHelper.service.deleteContent(holder,
										contentitem);
							}
						}
						holder = (FormatContentHolder) PersistenceHelper.manager
								.refresh(holder);
					} catch (PropertyVetoException e) {
						LOGGER.error(e.getLocalizedMessage());
						throw new WTException(e);
					} finally {
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
			}
			return holder1;
	}

	/**
	 * deleteContents
	 * delete all contents about the doc include PrimaryContent
	 * @author BaiJuanjuan
	 * @param WTDocument
	 * @return WTDocument
	 * @throws WTException
	 */
	public static WTDocument deleteContents(WTDocument doc) throws WTException {
		FormatContentHolder holder = null;
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"deleteContents", DocUtil.class.getName(), null,
						new Class[] { WTDocument.class }, new Object[] {doc});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			try {
				holder = (FormatContentHolder) ContentHelper.service
						.getContents(doc);
				List<?> items = ContentHelper.getContentListAll(holder);
				for (int i = 0; i < items.size(); i++) {
					ContentItem item = (ContentItem) items.get(i);
					ContentServerHelper.service.deleteContent(holder, item);
				}
				holder = (FormatContentHolder) PersistenceHelper.manager
						.refresh(holder);
			} catch (PropertyVetoException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return (WTDocument) holder;
		}
	}

	/**
	 * @author BaiJuanjuan
	 * @param doc
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws WTException
	 * @throws PropertyVetoException
	 * @throws IOException
	 */
	public static WTDocument updatePrimaryContent(WTDocument doc,
			String filePath, String fileName) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"updatePrimaryContent", DocUtil.class.getName(), null,
						new Class[] {WTDocument.class, String.class, String.class }, new Object[] {doc,filePath,fileName});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ApplicationData applicationdata = ApplicationData
					.newApplicationData(doc);
			ContentHolder ch = null;
			try {
				applicationdata.setFileName(fileName);
				applicationdata.setUploadedFromPath(filePath);
				applicationdata.setRole(ContentRoleType.PRIMARY);
				ch = ContentHelper.service.getContents(doc);
				ContentServerHelper.service.deleteContent(doc,
						ContentHelper.getPrimary((FormatContentHolder) ch));
				ch = (FormatContentHolder) PersistenceHelper.manager
						.refresh(ch);
				ContentServerHelper.service.updateContent(ch, applicationdata,
						filePath);
			} catch (WTPropertyVetoException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} catch (PropertyVetoException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			} catch (IOException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
			}finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return (WTDocument) ch;
		}
	}
	
	public static ApplicationData updateApplicationDataToDoc(
			WTDocument document, String fileName, String filePath,
			boolean isPrimary) throws WTException, WTPropertyVetoException,
			java.beans.PropertyVetoException, IOException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("updateApplicationDataToDoc",
								DocUtil.class.getName(), null,
								new Class[] {WTDocument.class,String.class,String.class,boolean.class}, 
								new Object[] {document,fileName,filePath,isPrimary});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ApplicationData applicationdata = ApplicationData
					.newApplicationData(document);
			applicationdata.setFileName(fileName);
			applicationdata.setUploadedFromPath(filePath);
			if (isPrimary) {
				applicationdata.setRole(ContentRoleType.PRIMARY);
			}
			wt.content.ContentHolder ch = wt.content.ContentHelper.service
					.getContents(document);
			// delete original ApplicationData
			if (!isPrimary) {// Attachment
				Vector apps = ContentHelper.getApplicationData(ch);
				for (int i = 0; i < apps.size(); i++) {
					ApplicationData app = (ApplicationData) apps.elementAt(i);
					if (app.getFileName().equals(fileName)) {
						ContentServerHelper.service.deleteContent(ch, app);
					}
				}
			} else {// Primary
				ContentItem item = (ContentItem) ContentHelper.service
						.getPrimary(document);
				ContentServerHelper.service.deleteContent(ch, item);
			}
			if (!RemoteMethodServer.ServerFlag) {//
				try {
					Class aclass[] = { ContentHolder.class,
							ApplicationData.class, String.class };
					Object aobj[] = { ch, applicationdata, filePath + fileName };
					RemoteMethodServer.getDefault().invoke(
							"addApplicationDataToDoc", DocUtil.class.getName(),
							null, aclass, aobj);
				} catch (Exception e) {
					LOGGER.error("-------[addApplicationDataToDoc]------------"
							+ e.getLocalizedMessage());
				}
			} else {//
				ContentServerHelper.service.updateContent(ch, applicationdata,
						filePath);
				try {
					ContentServerHelper.service
							.updateHolderFormat((FormatContentHolder) document);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			SessionServerHelper.manager.setAccessEnforced(enforce);
			return applicationdata;
		}
	}

	/**
	 * only get attachment Get Attachment by pre-fix name
	 * @author BaiJuanjuan
	 * @param doc
	 * @param preFix
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getAttachement(WTDocument doc, String preFix)
			throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getAttachement", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class,String.class }, new Object[] {doc,preFix});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			QueryResult qr;
			try {
				qr = ContentHelper.service.getContentsByRole(doc,
						ContentRoleType.SECONDARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr
							.nextElement();
					if (appData != null) {
						String fileName = appData.getFileName();
						if (fileName.startsWith(preFix)) {
							return appData;
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			} finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return null;
		}

	}

	/**
	 * only get Primary Get Attachment by pre-fix name
	 * @author BaiJuanjuan
	 * @param doc
	 * @param preFix
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getPrimaryContent(WTDocument doc,
			String preFix) throws WTException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getPrimaryContent", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class,String.class }, new Object[] {doc,preFix});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			QueryResult qr;
			try {
				qr = ContentHelper.service.getContentsByRole(doc,
						ContentRoleType.PRIMARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr
							.nextElement();
					if (appData != null) {
						String fileName = appData.getFileName();
						if (fileName.startsWith(preFix)) {
							return appData;
						}
					}
				}
			} catch (WTException e) {
				e.printStackTrace();
			}finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return null;
		}
	}
	
	/**
	 * getExcelFromAttachContent
	 * @author BaiJuanjuan
	 * @param WTDocument
	 * @param String
	 * @return
	 * @throws WTException
	 */
	public static List<ApplicationData> getAttachContentList(
			WTDocument wtdocument, String fileName) throws WTException,
			PropertyVetoException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (List<ApplicationData>) RemoteMethodServer.getDefault()
						.invoke("getAttachContentList", DocUtil.class.getName(), null,
								new Class[] { WTDocument.class,String.class }, new Object[] {wtdocument,fileName});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ApplicationData applicationData = null;
			List<ApplicationData> applist = new ArrayList<ApplicationData>();
			try {
				ContentHolder contentHolder = ContentHelper.service
						.getContents((ContentHolder) wtdocument);
				List<?> vContentList = ContentHelper.getContentList(contentHolder);
				int iSize = vContentList.size();
				for (int j = 0; j < iSize; j++) {
					ContentItem contentitem = (ContentItem) vContentList.get(j);
					if (contentitem instanceof ApplicationData) {
						applicationData = ((ApplicationData) contentitem);
						applist.add(applicationData);
					}
				}
			} catch (PropertyVetoException e) {
				LOGGER.error(e.getLocalizedMessage());
				throw new WTException(e);
           } finally {
					SessionServerHelper.manager.setAccessEnforced(enforce);
				}
			return applist;
		}
	}
	
	/**
	 * @author BaiJuanjuan
	 * @param ContentHolder
	 * @param String
	 * @throws WTException
	 */
	public static void saveFiletoAttachment(ContentHolder contentholder, String filename) throws WTException, WTPropertyVetoException, PropertyVetoException, IOException {
		
		boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
		ApplicationData appData = ApplicationData.newApplicationData(contentholder);
		appData.setRole(ContentRoleType.SECONDARY);
		ContentServerHelper.service.updateContent(contentholder, appData, filename);
		SessionServerHelper.manager.setAccessEnforced(enforce);
	}

	public static Streamed getStreamed(ApplicationData appdata) throws WTException {
		if (appdata == null)
			return null;
		Streamed streamed = null;
		try {
			streamed = (Streamed) PersistenceHelper.manager.refresh(appdata.getStreamData().getObject());
		} catch (ObjectNoLongerExistsException objectnolongerexistsexception) {
			appdata = (ApplicationData) PersistenceHelper.manager.refresh(appdata);
			streamed = (Streamed) PersistenceHelper.manager.refresh(appdata.getStreamData().getObject());
		} catch (WTRuntimeException e) {
			e.printStackTrace();
		}
		return streamed;
	}
	
	/**
	 * @author BaiJuanjuan
	 * update the primary content by WTDocument.
	 * @param String
	 * @param WTDocument
	 * @return
	 * @throws WTException
	 */
	public static WTDocument updatePrimaryContent(String path, WTDocument wtdoc) throws WTException, FileNotFoundException, PropertyVetoException, IOException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (WTDocument) RemoteMethodServer.getDefault()
						.invoke("updatePrimaryContent", DocUtil.class.getName(), null,
								new Class[] {String.class, WTDocument.class}, new Object[] {path,wtdoc});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			if (wtdoc == null || path == null || path.trim().length() == 0)
				return wtdoc;
			ContentHolder contentHolder = ContentHelper.service.getContents(wtdoc);
			ContentItem contentItem = ContentHelper.getPrimary((FormatContentHolder) contentHolder);
			if (contentItem instanceof ApplicationData) {
				ApplicationData appData = (ApplicationData) contentItem;
				File file = new File(path);
				if (file != null && file.exists()) {
					FileInputStream fis = new FileInputStream(file);
					Transaction tran = new Transaction(); 
					try {
						tran.start();
						Streamed streamed = getStreamed(appData);
						PersistenceHelper.manager.lockAndRefresh(streamed);
						Streamed streamed_ = streamed.storeStream(wtdoc, appData, fis);
						long filesize = streamed_.getFileSize();
						appData.setFileSize(filesize);
						PersistenceHelper.manager.modify(appData);
						tran.commit();
						tran = null;
					} catch (WTPropertyVetoException e) {
						e.printStackTrace();
					} finally {
						if (tran != null)
							tran.rollback();
						try {
							fis.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						SessionServerHelper.manager.setAccessEnforced(enforce);
					}
			}
		}
		}
		return wtdoc;
	}

	public static Vector getPrimaryContent2(WTDocument wtdocument) throws WTException, PropertyVetoException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (Vector) RemoteMethodServer.getDefault()
						.invoke("getPrimaryContent2", DocUtil.class.getName(), null,
								new Class[] {WTDocument.class}, new Object[] {wtdocument});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ContentHolder contentHolder = null;
			Vector contentitem= null;
			try {
				contentHolder = ContentHelper.service.getContents((ContentHolder) wtdocument);
				contentitem = ContentHelper.getApplicationData(contentHolder);
			
			} catch (WTException e1) {
				throw new WTException(e1);
			}finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return contentitem;
	    }
	}

	/**
	 * @author BaiJuanjuan
	 * get attachment by file name.
	 * @param fileName
	 * @param WTDocument
	 * @return
	 * @throws WTException
	 */
	public static ApplicationData getAttachmentByName(WTDocument wtdocument, String fileName) throws WTException, PropertyVetoException {
		if (!RemoteMethodServer.ServerFlag) {
			try {
				return (ApplicationData) RemoteMethodServer.getDefault()
						.invoke("getAttachmentByName", DocUtil.class.getName(), null,
								new Class[] {WTDocument.class,String.class}, new Object[] {wtdocument,fileName});
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
				throw new WTException(e);
			}
		} else {
			boolean enforce = wt.session.SessionServerHelper.manager
					.setAccessEnforced(false);
			ContentHolder contentHolder = null;
			ApplicationData applicationdata = null;
			try {
				contentHolder = ContentHelper.service.getContents((ContentHolder) wtdocument);
				QueryResult qr = ContentHelper.service.getContentsByRole(contentHolder, ContentRoleType.SECONDARY);
				while (qr.hasMoreElements()) {
					ApplicationData appData = (ApplicationData) qr.nextElement();
					String appDataName = appData.getFileName();
					System.out.println("appDataName: " + appDataName);
					if (appDataName.indexOf(fileName) >= 0) {
						applicationdata = appData;
						break;
					}
				}
			} catch (WTException e1) {
				throw new WTException(e1);
			}finally {
				SessionServerHelper.manager.setAccessEnforced(enforce);
			}
			return applicationdata;
		}
     }
	
	/**
	 * Replace the document primary. (can not update version)
	 * @param doc
	 *            The document which document we need replace.
	 * @param content
	 *           Primary Content(String FilePath or InputStream)
	 * @return   The document which we would been replaced.
	 */
	public static WTDocument replaceDocPrimaryContent(WTDocument doc,
			Object content) {
		try {
			if (!RemoteMethodServer.ServerFlag) {
				return (WTDocument) RemoteMethodServer.getDefault().invoke(
						"replaceDocPrimaryContent", DocUtil.class.getName(),
						null, new Class[] { WTDocument.class, Object.class },
						new Object[] { doc, content });
			} else {
				try {
					wt.content.ContentItem docContentItem = ContentHelper.service
							.getPrimary((FormatContentHolder) doc);
					ContentServerHelper.service.deleteContent(doc,
							docContentItem);

					ApplicationData applicationdata = ApplicationData
							.newApplicationData(doc);
					applicationdata.setRole(ContentRoleType.PRIMARY);

					if (content instanceof String) {
						String filePath = (String) content;
						applicationdata = ContentServerHelper.service
								.updateContent(doc, applicationdata, filePath);
						PersistenceServerHelper.manager.restore(doc);
					} else if (content instanceof InputStream) {
						InputStream ins = (InputStream) content;
						applicationdata = ContentServerHelper.service
								.updateContent(doc, applicationdata, ins);
						PersistenceServerHelper.manager.restore(doc);
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				return doc;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	
	/**
	 * @author BaiJuanjuan
	 * get AttachmentName in WTDocument or EPMDocument.
	 * @param obj
	 * @return
	 * @throws WTException
	 */
	public static String getAttachmentName(WTObject obj) throws WTException {
		// TODO Auto-generated method stub
		String name ="";
		if (obj instanceof WTDocument) {
			WTDocument doc = (WTDocument) obj;
			QueryResult qr = ContentHelper.service.getContentsByRole(doc,ContentRoleType.SECONDARY);
			while (qr.hasMoreElements()) {
				ApplicationData applicationdata = (ApplicationData) qr.nextElement();
				String filename = applicationdata.getFileName();
				name += filename;
			}
		}
		if (obj instanceof EPMDocument) {
			EPMDocument epm = (EPMDocument) obj;
			QueryResult qr = ContentHelper.service.getContentsByRole(epm,ContentRoleType.SECONDARY);
			while (qr.hasMoreElements()) {
				ApplicationData applicationdata = (ApplicationData) qr.nextElement();
				String filename = applicationdata.getFileName();
				name += filename;
			}
			
		}
		return name;
	}
	
	 
	 

		/**
		 * Author: Freedom Rain
		 * Description:  获取可视化pdf文件路径（先从文档对象中，将主内容的文件格式转化为pdf格式；再将转档后的pdf文件，写入到指定路径（下载到指定路径
		 * 2015-8-12上午12:56:26
		 * @param doc
		 * @return
		 * @throws WTException
		 * @throws PropertyVetoException
		 * @throws IOException
		 */
		public static String getVisualPath(Representable doc) throws WTException,
				PropertyVetoException, IOException {
			/*
			 * HashSet set = new HashSet(); QueryResult qr01 =
			 * ContentHelper.service.getContentsByRole(doc,ContentRoleType.PRIMARY);
			 * while (qr01.hasMoreElements()) { ApplicationData applicationdata =
			 * (ApplicationData) qr01.nextElement(); String filename =
			 * applicationdata.getFileName();
			 * System.out.println("+++++++++++filename.........."+filename); String
			 * suffix=filename.substring(filename.indexOf(".")+1); String
			 * prefix=filename.substring(0,filename.indexOf("."));
			 * filename=prefix.trim()+"__"+suffix+".pdf";
			 * System.out.println("=======filename...="+filename);
			 * set.add(filename); }
			 */
			String wtHome = WTProperties.getLocalProperties()
					.getProperty("wt.home");
			Representation representation = RepresentationHelper.service
					.getDefaultRepresentation(doc);// 获得表示法
			System.out.println("doc:" + doc.getClass().getName()
					+ "==================");
			System.out.println("representation:" + representation
					+ "==================");
			if (representation != null) {
				wt.content.ContentHolder ch = wt.content.ContentHelper.service
						.getContents(doc);// 得到文档所在的容器
				representation = (Representation) ContentHelper.service
						.getContents(representation); // 得到表示法的ContentHolder（内容持有者）
				System.out.println("representation2:" + representation
						+ "==================");
				Vector vector1 = ContentHelper.getContentList(representation);// 得到内容列表
				System.out.println("vector1.size:" + vector1.size()
						+ "========================");
				for (int l = 0; l < vector1.size(); l++) {
					ContentItem contentitem = (ContentItem) vector1.elementAt(l);
					System.out.println("contentitem:" + contentitem
							+ "=================");
					if (contentitem instanceof ApplicationData) {
						ApplicationData applicationdata = (ApplicationData) contentitem; // 得到表示法对象的数据
						System.out.println("=============applicationdata:"
								+ applicationdata);

						// applicationdata对象接收转档后的pdf文件，获取输入流
						InputStream in = ContentServerHelper.service
								.findContentStream(applicationdata);
						String filename = applicationdata.getFileName();// 得到表示法对象的文件名
						// 将路径庄花为utf-8编码
						filename = PublicUtil.unescape(filename);
						if (filename.indexOf(".pdf") != -1) {
							String absoluteFileName = wtHome + File.separator
									+ "temp" + File.separator + filename;

							// 根据输入流对象以及pdf指定路径，写入到指定路径，生成pdf文件，实现下载功能
							downloadFile(in, absoluteFileName);// 下载文档
							return absoluteFileName;
						}
					}
				}
			}
			return null;
		}

		/**
		 * Author: Freedom Rain    根据输入流以及文档指定路径，将文件写入指定路径（下载到指定路径）
		 * Description:
		 * 2015-8-12上午1:07:56
		 */
		public static void downloadFile(InputStream in, String path)
				throws FileNotFoundException {
			FileOutputStream outputStream1 = new FileOutputStream(path);
			InputStream inputStream = in;
			byte[] buffer = new byte[1024];
			int len;
			try {
				while ((len = inputStream.read(buffer)) > 0) {
					outputStream1.write(buffer, 0, len);
				}
				inputStream.close();
				outputStream1.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	
  }
