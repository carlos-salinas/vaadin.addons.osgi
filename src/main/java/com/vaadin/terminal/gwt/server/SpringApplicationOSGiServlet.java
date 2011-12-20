/*
 * Copyright 2011 Thingtrack, S.L.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.terminal.gwt.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.springframework.context.ApplicationContext;

import com.vaadin.Application;

/**
 * This Servlet retrieves the Spring Application Context registered by Spring DM.
 * You have to define new init param with the name of the with the Application subclass bean.
 * @author carlos
 * 
 */
public class SpringApplicationOSGiServlet extends AbstractApplicationServlet {

	// Private fields
	private Class<? extends Application> applicationClass;

	private String applicationParam;
	private String versionParam;
	private String beanParam;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.terminal.gwt.server.AbstractApplicationServlet#init(javax.
	 * servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		// TODO Auto-generated method stub
		super.init(servletConfig);

		applicationParam = servletConfig.getInitParameter("application");

		if (applicationParam == null) {
			throw new ServletException(
					"Application not specified in servlet parameters");
		}

		versionParam = getInitParameter("version");

		if (versionParam == null) {
			throw new ServletException(
					"Bundle Version not specified in servlet parameters");
		}
		
		beanParam = getInitParameter("bean");
		
		if (beanParam == null) {
			throw new ServletException(
					"Bean Name not specified in servlet parameters");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.terminal.gwt.server.AbstractApplicationServlet#getNewApplication
	 * (javax.servlet.http.HttpServletRequest)
	 */
	@Override
	protected Application getNewApplication(HttpServletRequest request)
			throws ServletException {

		// Creates a new application instance
        try {
        	// Retrieve the Spring ApplicationContext registered as a service
    		ApplicationContext springContext = getApplicationContext();

    		if (!springContext.containsBean(beanParam)) {

    		    throw new ClassNotFoundException("No application bean found under name " + beanParam);
    		}
    		
    		final Application application = (Application) springContext.getBean(beanParam);

            return application;
            
        } catch (ClassNotFoundException e) {
            throw new ServletException("getNewApplication failed", e);
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.vaadin.terminal.gwt.server.AbstractApplicationServlet#getApplicationClass
	 * ()
	 */
	@Override
	protected Class<? extends Application> getApplicationClass()
			throws ClassNotFoundException {
		ApplicationContext springContext = getApplicationContext();

		if (!springContext.containsBean(beanParam)) {

		    throw new ClassNotFoundException("No application bean found under name " + beanParam);
		}
		
		
		
		return (Class<? extends Application>) springContext.getBean(beanParam).getClass();
	}

	/**
	 * @return
	 * @throws ClassNotFoundException
	 */
	private ApplicationContext getApplicationContext()
			throws ClassNotFoundException {
		BundleContext context = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();

		ServiceReference[] refs = null;
		try {
			refs = context.getServiceReferences(
					ApplicationContext.class.getName(),"(org.springframework.context.service.name="+ getSymbolicName(this.applicationParam,this.versionParam)+")");
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		ApplicationContext springContext = null;
		if (refs != null && refs.length > 0) {
			springContext = (ApplicationContext) context.getService(refs[0]);
		}

		if (springContext == null) {
			throw new ClassNotFoundException(
					"Spring ApplicationContext has not been registered");
		}
		return springContext;
	}
	
	@SuppressWarnings("unchecked")
	private String getSymbolicName(String className, String version)
			throws ClassNotFoundException {
		Version versionParam = version != null ? Version.parseVersion(version)
				: null;

		String packageName = className;
		String[] splittedString = packageName.split("\\.");

		if (splittedString.length > 0)
			packageName = packageName.substring(0, packageName.length()
					- splittedString[splittedString.length - 1].length() - 1);

		BundleContext context = FrameworkUtil.getBundle(this.getClass())
				.getBundleContext();
		ServiceReference ref = context.getServiceReference(PackageAdmin.class
				.getName());
		PackageAdmin packageAdmin = (PackageAdmin) context.getService(ref);

		ExportedPackage[] packages = packageAdmin
				.getExportedPackages(packageName);
		if (packages == null) {
			return null;
		}
		for (ExportedPackage packageImported : packages) {
			Bundle bundle = packageImported.getExportingBundle();

			if (bundle == null) {
				return null;
			}
			if (versionParam == null
					|| versionParam.equals(bundle.getVersion())) {

				return bundle.getSymbolicName();
			}

		}

		return null;

	}

}
