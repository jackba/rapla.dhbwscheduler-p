/*--------------------------------------------------------------------------*
 | Copyright (C) 2006 Christopher Kohlhaas                                  |
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
package org.rapla.plugin.dhbwscheduler;
import org.rapla.client.ClientServiceContainer;
import org.rapla.client.RaplaClientExtensionPoints;
import org.rapla.components.xmlbundle.I18nBundle;
import org.rapla.components.xmlbundle.impl.I18nBundleImpl;
import org.rapla.framework.Configuration;
import org.rapla.framework.PluginDescriptor;
import org.rapla.framework.TypedComponentRole;

public class DhbwschedulerPlugin   implements   PluginDescriptor<ClientServiceContainer>
{
	
	public static final boolean ENABLE_BY_DEFAULT = false;
	public static final TypedComponentRole<I18nBundle> RESOURCE_FILE = new TypedComponentRole<I18nBundle>(DhbwschedulerPlugin.class.getPackage().getName() + ".DhbwschedulerPluginResources");

    public void provideServices(ClientServiceContainer container, Configuration config) {
        container.addContainerProvidedComponent( RaplaClientExtensionPoints.PLUGIN_OPTION_PANEL_EXTENSION,DhbwschedulerAdminOption.class);

    	if ( !config.getAttributeAsBoolean("enabled", ENABLE_BY_DEFAULT) )
        	return;

        container.addContainerProvidedComponent( RESOURCE_FILE, I18nBundleImpl.class,I18nBundleImpl.createConfig( RESOURCE_FILE.getId() ) );
        container.addContainerProvidedComponent( RaplaClientExtensionPoints.HELP_MENU_EXTENSION_POINT, SchedulerHelpMenuExtension.class);
        container.addContainerProvidedComponent( RaplaClientExtensionPoints.OBJECT_MENU_EXTENSION, SchedulerReservationMenuFactory.class, config);
        container.addContainerProvidedComponent( RaplaClientExtensionPoints.VIEW_MENU_EXTENSION_POINT, DhbwSchedulerPlanningExtension.class, config);
    }

}

