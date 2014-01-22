package org.rapla.plugin.dhbwscheduler;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.internal.common.InternMenus;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.IdentifiableMenuEntry;
import org.rapla.gui.toolkit.RaplaMenu;
import org.rapla.gui.toolkit.RaplaWidget;
import org.rapla.plugin.dhbwscheduler.SchedulerHelpMenuExtension.MyDialog;

public class DhbwSchedulerPlanningExtension extends RaplaGUIComponent implements ActionListener, IdentifiableMenuEntry 
{

	String id;
	JMenuItem item;
	
	public DhbwSchedulerPlanningExtension(RaplaContext context) {
		super(context);
		setChildBundleName(DhbwschedulerPlugin.RESOURCE_FILE);
		id = getString("planning_gui");
		item = new JMenuItem( id );
        item.setIcon( getIcon("icon.planning") );
        item.addActionListener(this);
	}

	public void actionPerformed(ActionEvent evt) {
        try {
            final MyDialog myDialog = new MyDialog(getContext());
            DialogUI dialog = DialogUI.create( getContext(),getMainComponent(),true, myDialog.getComponent(), new String[] {getString("ok")});
            dialog.setTitle(getString("planning_gui"));
            dialog.setSize( 900, 700);
            dialog.startNoPack();
         } catch (Exception ex) {
            showException( ex, getMainComponent());
        }
	}

	public String getId() {
		return id;
	}

	public MenuElement getMenuElement() {
		return item;
	}
	class MyDialog extends RaplaGUIComponent implements  RaplaWidget
	{

	    JLabel label = new JLabel();

	    public MyDialog(RaplaContext sm) throws RaplaException {
	        super(sm);
	        setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	        getLogger().info("Help Dialog started");
	        String helpText = "Bla Bla Bla";
			label.setText( helpText);
	    }

	    public JComponent getComponent() {
	        return label;
	    }
	    	    
	}

}
