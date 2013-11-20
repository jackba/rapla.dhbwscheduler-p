/*--------------------------------------------------------------------------*
 | Copyright (C) 2013 Christopher Kohlhaas                                  |
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/
package org.rapla.plugin.dhbwscheduler.server;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.framework.Configuration;
import org.rapla.framework.PluginDescriptor;
import org.rapla.framework.RaplaContextException;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerPlugin;
import org.rapla.plugin.dhbwscheduler.DhbwschedulerService;
import org.rapla.server.ServerServiceContainer;

public class DhbwschedulerServerPlugin implements PluginDescriptor<ServerServiceContainer>
{
    public void provideServices(ServerServiceContainer container, Configuration config) throws RaplaContextException {
        if ( !config.getAttributeAsBoolean("enabled", false) )
        	return;
        
        container.addContainerProvidedComponent( DhbwschedulerPlugin.RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( DhbwschedulerPlugin.RESOURCE_FILE.getId() ) );
        
        container.addRemoteMethodFactory(DhbwschedulerService.class, DhbwschedulerServiceImpl.class);
    	container.addWebpage("scheduler-constraints",SchedulerConstraintsPageGenerator.class, config  );
    }

}

