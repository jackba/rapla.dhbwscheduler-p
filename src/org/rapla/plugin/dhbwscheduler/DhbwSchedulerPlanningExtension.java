package org.rapla.plugin.dhbwscheduler;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.table.DefaultTableModel;

import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
//import org.rapla.gui.internal.common.InternMenus;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.IdentifiableMenuEntry;
//import org.rapla.gui.toolkit.RaplaMenu;
import org.rapla.gui.toolkit.RaplaWidget;
//import org.rapla.plugin.dhbwscheduler.SchedulerHelpMenuExtension.MyDialog;

public class DhbwSchedulerPlanningExtension extends RaplaGUIComponent implements ActionListener, IdentifiableMenuEntry 
{

	String id;
	JMenuItem item;
	private static DefaultTableModel veranstaltungen_model;
	
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
    		//JPanel planungsgui = getPlanungsGui();
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

	    JPanel planungsgui = new JPanel();

	    public MyDialog(RaplaContext sm) throws RaplaException {
	        super(sm);
	        setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
//	        getLogger().info("Help Dialog started");
//	        String helpText = "Bla Bsasdasdla Bla";
//			label.setText( helpText);
	        planungsgui = getPlanungsGui();
	    }

	    public JComponent getComponent() {
	        return planungsgui;
	    }
	    	    
	}
	private static JPanel getPlanungsGui() {

		JPanel planungsgui = new JPanel();
		planungsgui.setLayout(new BoxLayout(planungsgui, BoxLayout.Y_AXIS));

		JPanel ueberschrift_panel = new JPanel();
		JLabel ueberschrift = new JLabel("Semesterplanung");
		ueberschrift.setFont(new Font("Sans_Serif", Font.ITALIC, 38));
		ueberschrift_panel.add(ueberschrift);
		planungsgui.add(ueberschrift_panel);

		JPanel planungszyklus_panel = new JPanel();
		planungszyklus_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		planungszyklus_panel.add(new JLabel(
				"Wählen Sie bitte einen Planungszyklus aus:   "));
		String[] planungszyklen = getPlanungszyklen();
		JComboBox comboBox = new JComboBox(planungszyklen);
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				refreshData(itemEvent.getItem().toString());
			}
		};
		comboBox.addItemListener(itemListener);
		planungszyklus_panel.add(comboBox);
		planungsgui.add(planungszyklus_panel);

		Vector columnNames = getColumnNames();
		Vector data = getData(comboBox.getSelectedItem().toString());
		veranstaltungen_model = new DefaultTableModel();
		JTable veranstaltungen = new JTable(veranstaltungen_model) {

			@Override
			public Class getColumnClass(int column) {
				if (column == 2 || column == 3)
					return Boolean.class;
				return super.getColumnClass(column);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		veranstaltungen_model.setDataVector(data, columnNames);
		JScrollPane veranstaltungen_scroll = new JScrollPane(veranstaltungen);
		planungsgui.add(veranstaltungen_scroll);
		
		
		return planungsgui;
	}

	private static Vector getData(String planungszyklus) {
		Vector data = new Vector();
		Vector row1 = new Vector();
		Vector row2 = new Vector();
		Vector row3 = new Vector();

		switch (planungszyklus) {
		case "Test1":
			row1.add("Test1");
			row1.add("Smith");
			row1.add(true);
			row1.add(false);
			row1.add("test.html");

			row2.add("");
			row2.add("Schulmeister");
			row2.add(true);
			row2.add(true);
			row2.add("doz_constrains.html");

			row3.add("FiBu");
			row3.add("Brown");
			row3.add(true);
			row3.add(false);
			row3.add("versuch.html");

			data.add(row1);
			data.add(row2);
			data.add(row3);
			break;

		case "Test2":
			row1.add("Test2");
			row1.add("Smith");
			row1.add(true);
			row1.add(false);
			row1.add("test.html");

			row2.add("");
			row2.add("Schulmeister");
			row2.add(true);
			row2.add(true);
			row2.add("doz_constrains.html");

			row3.add("FiBu");
			row3.add("Brown");
			row3.add(true);
			row3.add(false);
			row3.add("versuch.html");

			data.add(row1);
			data.add(row2);
			data.add(row3);
			break;

		case "Test3":
			row1.add("Test3");
			row1.add("Smith");
			row1.add(true);
			row1.add(false);
			row1.add("test.html");

			row2.add("");
			row2.add("Schulmeister");
			row2.add(true);
			row2.add(true);
			row2.add("doz_constrains.html");

			row3.add("FiBu");
			row3.add("Brown");
			row3.add(true);
			row3.add(false);
			row3.add("versuch.html");

			data.add(row1);
			data.add(row2);
			data.add(row3);
			break;

		default:
			row1.add("Test0");
			row1.add("Smith");
			row1.add(true);
			row1.add(false);
			row1.add("test.html");

			row2.add("");
			row2.add("Schulmeister");
			row2.add(true);
			row2.add(true);
			row2.add("doz_constrains.html");

			row3.add("FiBu");
			row3.add("Brown");
			row3.add("true");
			row3.add("false");
			row3.add("versuch.html");

			data.add(row1);
			data.add(row2);
			data.add(row3);
			break;
		}

		return data;
	}

	private static Vector getColumnNames() {
		Vector columnNames = new Vector();
		columnNames.add("Lehrveranstaltung");
		columnNames.add("Dozent");
		columnNames.add("Mail");
		columnNames.add("Rückantwort");
		columnNames.add("URL");

		return columnNames;
	}

	private static void refreshData(String planungszyklus) {
		Vector data = getData(planungszyklus);
		Vector columnNames = getColumnNames();
		veranstaltungen_model.setDataVector(data, columnNames);
	}

	private static String[] getPlanungszyklen() {
		String[] planungszyklen = { "Test1", "Test2", "Test3" };
		return planungszyklen;
	}

}
