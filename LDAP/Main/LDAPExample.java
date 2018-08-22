
package Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;


public class LDAPExample {
	private static Properties props=null;
	private static BufferedReader br;
	static ArrayList<String> list=new ArrayList<String>();
	static String empID=null;
	static ArrayList<String> sendUser=new ArrayList<String>();
	static FileWriter fw;
//	private static Scanner scanPath;
	 
	
	public LDAPExample(){
		
	}

	
	public static DirContext getConnection() throws NamingException{
		PropertyFileHelper propertyFileHelper = PropertyFileHelper.getInstance();
		props=propertyFileHelper.getProperties();
		Hashtable<String,String> env=new Hashtable<String,String>(11);
		env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		System.out.println("Getting the LDAP Connection\n");

	    env.put("java.naming.provider.url", props.getProperty("LDAPURL"));
	    env.put("java.naming.security.authentication", "simple");
	    env.put("java.naming.security.principal", props.getProperty("UserName"));
	    env.put("java.naming.security.credentials", props.getProperty("UserPassword"));	    
		
		return new InitialDirContext(env);
	}

	
	
	public static void main(String[] s) {
		try {
			getActiveEIDMSUser();
		} catch (NamingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void getActiveEIDMSUser() throws NamingException, IOException { 
		DirContext dctx = getConnection();
		String base = "dc=users,dc=com";
	    String filter = new String();
	    String line="";
//	    scanPath = new Scanner(System.in);
//	    
//	    System.out.println("Please Provide the input path (default /Validation/TermID.txt): ");
//		String getInPath=scanPath.next();
//		
//		System.out.println("Please Provide the output path: (default /Validation/TermOut.txt)");
//		String getOutPath=scanPath.next();
//	    String inputFile="\\\\C:\\users\\group\\data\\Team_Docs\\TermID.txt";
	    
	    String inputFile="\\\\C:\\TermID.txt";
	    
	    fw=new FileWriter("\\\\C:\\TermIDOutput.txt"); 
//		String inputFile=(getInPath);
//	    fw=new FileWriter(getOutPath); 
	    try {
			br = new BufferedReader(new FileReader(inputFile));
			while((line=br.readLine())!=null){
				 empID=line;
	
				 list.add(empID);	
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    for(String l:list){
	    SearchControls sc=new SearchControls();
	   	    
	    filter ="uid="+l;
    
	    sc.setReturningAttributes(new String[]{"entryDN","employeenumber","nsAccountLock" ,"absjobaction","absjobterminatedate"});
	    sc.setSearchScope(2);

	    NamingEnumeration<SearchResult> results=dctx.search(base,filter, sc);
	    
	    if(results.hasMoreElements()==false){
	    	String output="uid="+l+" : "+"ID not exist"+"\r\n";
	    	sendMail(output);
	    	fw.write(output);
	    	System.out.println(output);
	    }
	    while (results.hasMoreElements()) {
	      SearchResult sr = (SearchResult)results.next();
	      Attributes entryAttrs = sr.getAttributes();			
    	  String absjobaction = entryAttrs.get("absjobaction")!=null?entryAttrs.get("absjobaction").get().toString():"";
    	  String absjobterminatedate = entryAttrs.get("absjobterminatedate")!=null?entryAttrs.get("absjobterminatedate").get().toString():"";
//    	  String nsAccountLock = entryAttrs.get("nsAccountLock")!=null?entryAttrs.get("nsAccountLock").get().toString():"";
    	  String employeeNumber = entryAttrs.get("employeenumber")!=null?entryAttrs.get("employeenumber").get().toString():"";
    	  		      
	      if(absjobaction.contains("TERM")){
	    	  String output=sr.getName()+" - "+absjobaction+" on "+absjobterminatedate+"\r\n";
	    	  sendMail(output);
	    	  fw.write(output);
	  	      System.out.println(output);

	      }else if(!sr.getAttributes().toString().contains("TERM")){
	    	  String output=sr.getName()+"["+employeeNumber+"]"+" - "+absjobaction+"\r\n";
	    	  sendMail(output);
	    	  fw.write(output);
//	    	  System.out.println(output);
	    	  	    }
	      }	
	    }
	    
	    
	    ArrayList<String> dl=new ArrayList<String>();
	    dl.add("sagar.v.hande@gmail.com");	   
	   
	    Mailer.send("sagar.v.hande@gmail.com",dl,"Term Status  : ",sendUser);
	    fw.close();
	    
	}
	public static void sendMail(String s){
		
		sendUser.add(s);		   	
    }
	
	
	
		
}
