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

import java.awt.BorderLayout;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.rapla.components.layout.TableLayout;
import org.rapla.framework.Configuration;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.framework.PluginDescriptor;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.DefaultPluginOption;

/** Not used yet */
public class DhbwschedulerAdminOption extends DefaultPluginOption {
    JTextField attributeField = new JTextField();
    
    public DhbwschedulerAdminOption( RaplaContext sm ) 
    {
        super( sm );
    }

    protected JPanel createPanel() throws RaplaException {
        JPanel panel = super.createPanel();
        //Hier zusätzliche Einstellungsmöglichkeiten implemetieren
        //Implement new options here
        return panel;
    }

        
    protected void addChildren( DefaultConfiguration newConfig) {
        DefaultConfiguration markerLabelConf = new DefaultConfiguration("testattribute");
        markerLabelConf.setValue( attributeField.getText() );
        newConfig.addChild( markerLabelConf );
    }

    protected void readConfig( Configuration config)   {
        String markerLabelText = config.getChild("testattribute").getValue("defaultValue");
        attributeField.setText( markerLabelText );
    }

    public void show() throws RaplaException  {
        super.show();
    }
  
    public void commit() throws RaplaException {
        super.commit();
    }

    /**
     * @see org.rapla.gui.DefaultPluginOption#getPluginClass()
     */
    public Class<? extends PluginDescriptor<?>> getPluginClass() {
        return DhbwschedulerPlugin.class;
    }
    
    public String getName(Locale locale) {
        return "Dhbwscheduler Plugin";
    }

}
