package cn.elead.tool.wc;

import java.util.HashMap;
import java.util.Map;

import wt.inf.container.WTContainer;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.project.Role;

public class Constant
{
	
	public static final long serialVersionUID = 592924258829951579L;
	public static final String role = "供应商";
	public static final String SEP_MULTIUSERNAME = ";";
	public static final String IRIS_SEP_MULTIUSERNAME = ",";
	public static String DESCRIPTION = "DESCRIPTION"; // discription 
	public static String TYPE = "TYPE"; 
	public static String FOLDER = "FOLDER"; // create folder
    
	public static Map<WTContainer, Boolean> containerMap = new HashMap<>();
	public static final Role roleMember = Role.toRole("MEMBERS"); 
	public static final WTPrincipal principal = null;
	public static final WTPrincipalReference principalReference = null;
	
}