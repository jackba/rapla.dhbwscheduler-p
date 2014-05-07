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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

//import org.rapla.entities.domain.Reservation;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.IdentifiableMenuEntry;
import org.rapla.gui.toolkit.RaplaWidget;


public class SchedulerHelpMenuExtension extends RaplaGUIComponent implements IdentifiableMenuEntry, ActionListener
{
	String id = "help";
	JMenuItem item;

	public SchedulerHelpMenuExtension(RaplaContext sm)  {
        super(sm);
		item = new JMenuItem( id );
        item.setIcon( getIcon("icon.help") );
        item.addActionListener(this);
    }
	
	public void actionPerformed(ActionEvent evt) {
          try {
              final MyDialog myDialog = new MyDialog(getContext());
              DialogUI dialog = DialogUI.create( getContext(),getMainComponent(),true, myDialog.getComponent(), new String[] {getString("ok")});
              dialog.setTitle("Help Usecase");
              dialog.setSize( 800, 600);
              dialog.startNoPack();
           } catch (Exception ex) {
              showException( ex, getMainComponent() );
          }
	}

	public String getId() {
		return id;
	}

	public JMenuItem getMenuElement() {
		return item;
	}
	
	class MyDialog extends RaplaGUIComponent implements  RaplaWidget
	{

	    JLabel label = new JLabel();

	    public MyDialog(RaplaContext sm) throws RaplaException {
	        super(sm);
	        setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	        getLogger().info("Help Dialog started");
//	        String helpText = "Hallo, ist da wer?";
//			label.setText( helpText);
	    }

	    public JComponent getComponent() {
	        return label;
	    }
	    	    
	}

}

