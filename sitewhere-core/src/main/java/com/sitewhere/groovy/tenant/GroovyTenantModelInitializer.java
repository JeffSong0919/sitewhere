/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.groovy.tenant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import com.sitewhere.SiteWhere;
import com.sitewhere.rest.model.tenant.request.scripting.TenantManagementRequestBuilder;
import com.sitewhere.server.SiteWhereServer;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.tenant.ITenantModelInitializer;
import com.sitewhere.spi.tenant.ITenantManagement;

import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * Implementation of {@link ITenantModelInitializer} that delegates creation
 * logic to a Groovy script.
 * 
 * @author Derek
 */
public class GroovyTenantModelInitializer implements ITenantModelInitializer {

    /** Static logger instance */
    private static Logger LOGGER = LogManager.getLogger();

    /** Relative path to Groovy script */
    private String scriptPath;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.spi.server.tenant.ITenantModelInitializer#initialize(com.
     * sitewhere. spi.tenant.ITenantManagement)
     */
    @Override
    public void initialize(ITenantManagement tenantManagement) throws SiteWhereException {
	Binding binding = new Binding();
	binding.setVariable("logger", LOGGER);
	binding.setVariable("tenantBuilder", new TenantManagementRequestBuilder(tenantManagement));

	try {
	    // Use system account for logging "created by" on created elements.
	    SecurityContextHolder.getContext().setAuthentication(SiteWhereServer.getSystemAuthentication());
	    SiteWhere.getServer().getGroovyConfiguration().getGroovyScriptEngine().run(getScriptPath(), binding);
	} catch (ResourceException e) {
	    throw new SiteWhereException("Unable to access Groovy script. " + e.getMessage(), e);
	} catch (ScriptException e) {
	    throw new SiteWhereException("Unable to run Groovy script.", e);
	} finally {
	    SecurityContextHolder.getContext().setAuthentication(null);
	}
    }

    public String getScriptPath() {
	return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
	this.scriptPath = scriptPath;
    }
}