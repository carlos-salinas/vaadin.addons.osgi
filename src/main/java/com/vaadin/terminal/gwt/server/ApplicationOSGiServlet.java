package com.vaadin.terminal.gwt.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.AdminPermission;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import com.vaadin.Application;

@SuppressWarnings("serial")
public class ApplicationOSGiServlet  extends AbstractApplicationServlet {
	
		//Private fields
    	private Class<? extends Application> applicationClass;
	   
		@Override
	    public void init(javax.servlet.ServletConfig servletConfig)
	            throws javax.servlet.ServletException {
	        super.init(servletConfig);

	        // Loads the application class using the same class loader
	        // as the servlet itself

	        // Gets the application class name
	        final String applicationClassName = servletConfig
	                .getInitParameter("application");
	        if (applicationClassName == null) {
	            throw new ServletException(
	                    "Application not specified in servlet parameters");
	        }
	        
	        final String version = getInitParameter("version");
			
			if (version == null) {
	            throw new ServletException(
	                    "Bundle Version not specified in servlet parameters");
	        }

	        try {
	        	applicationClass = 
	        		(Class<? extends Application>)getApplication(applicationClassName, version);
	        	
	        } catch (final ClassNotFoundException e) {
	            throw new ServletException("Failed to load application class: "
	                    + applicationClassName);
	        }
	    }
	   
	   
	   @SuppressWarnings("unchecked")
		private Class<? extends Application> getApplication(String className, String version) throws ClassNotFoundException 
		{
	    	Version versionParam = version != null ? Version.parseVersion(version) : null;
		   	
		   String packageName = className; 
	    	String[] splittedString = packageName.split("\\.");
	    		
	    	if(splittedString.length > 0)
	    		packageName = packageName.substring(0, packageName.length() - splittedString[splittedString.length-1].length() - 1);
	    		
	    	BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	    	ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
	    	PackageAdmin packageAdmin = (PackageAdmin)context.getService(ref);
			 
	    	ExportedPackage[] packages = packageAdmin.getExportedPackages(packageName);
			  if (packages == null) {
			    return null;
			  }
			  for (ExportedPackage packageImported : packages) {
			    Bundle bundle = packageImported.getExportingBundle(); 
			    if (bundle == null) {
			      return null;
			    }
			    if(versionParam == null || versionParam.equals(bundle.getVersion())){
			    		
			    	return (Class<? extends Application>) bundle.loadClass(className);
			    }
			    
			      
		      }
			  
			  return null;

		  }


	@Override
	protected Application getNewApplication(HttpServletRequest request)
			throws ServletException {
		
		// Creates a new application instance
        try {
            final Application application = getApplicationClass().newInstance();

            return application;
        } catch (final IllegalAccessException e) {
            throw new ServletException("getNewApplication failed", e);
        } catch (final InstantiationException e) {
            throw new ServletException("getNewApplication failed", e);
        } catch (ClassNotFoundException e) {
            throw new ServletException("getNewApplication failed", e);
        }

	}


	@Override
	protected Class<? extends Application> getApplicationClass()
			throws ClassNotFoundException {
		
		return applicationClass;
	}


}
