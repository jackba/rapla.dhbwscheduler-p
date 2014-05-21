package org.rapla.plugin.dhbwscheduler;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.MenuElement;
import javax.swing.table.DefaultTableModel;

import org.rapla.entities.User;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.dynamictype.DynamicType;
import org.rapla.framework.DefaultConfiguration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.IdentifiableMenuEntry;
import org.rapla.gui.toolkit.RaplaWidget;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;
import org.rapla.plugin.urlencryption.UrlEncryption;
import org.rapla.plugin.urlencryption.server.UrlEncryptionService;

public class DhbwSchedulerPlanningExtension extends RaplaGUIComponent implements
		ActionListener, IdentifiableMenuEntry {

	String id;
	JMenuItem item;
	private static DefaultTableModel veranstaltungen_model;
	HashMap<String, Allocatable> planungszyklen_allocatables = new HashMap<String, Allocatable>();
	
	public DhbwSchedulerPlanningExtension(RaplaContext context) {
		super(context);
		setChildBundleName(DhbwschedulerPlugin.RESOURCE_FILE);
		id = getString("planning_gui");
		item = new JMenuItem(id);
		item.setIcon(getIcon("icon.planning"));
		item.addActionListener(this);
	}

	public void actionPerformed(ActionEvent evt) {
		try {
			final MyDialog myDialog = new MyDialog(getContext());
			DialogUI dialog = DialogUI.create(getContext(), getMainComponent(),
					true, myDialog.getComponent(),
					new String[] { getString("ok") });
			dialog.setTitle(getString("planning_gui"));
			dialog.setSize(900, 700);
			dialog.startNoPack();
		} catch (Exception ex) {
			showException(ex, getMainComponent());
		}
	}

	public String getId() {
		return id;
	}

	public MenuElement getMenuElement() {
		return item;
	}

	class MyDialog extends RaplaGUIComponent implements RaplaWidget {

		JPanel planungsgui = new JPanel();

		public MyDialog(RaplaContext sm) throws RaplaException {
			super(sm);
			setChildBundleName(DhbwschedulerPlugin.RESOURCE_FILE);
			// getLogger().info("Help Dialog started");
			// String helpText = "Bla Bsasdasdla Bla";
			// label.setText( helpText);
			planungsgui = getPlanungsGui();
		}

		public JComponent getComponent() {
			return planungsgui;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JPanel getPlanungsGui() {

		JPanel planungsgui = new JPanel();
		planungsgui.setLayout(new BoxLayout(planungsgui, BoxLayout.Y_AXIS));

		JPanel ueberschrift_panel = new JPanel();
		JLabel ueberschrift = new JLabel("Semesterplanung");
		ueberschrift.setFont(new Font("Sans_Serif", Font.ITALIC, 38));
		ueberschrift_panel.add(ueberschrift);
		planungsgui.add(ueberschrift_panel);

		JPanel planungszyklus_panel = new JPanel();
		planungszyklus_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		planungszyklus_panel.add(new JLabel("Wählen Sie bitte einen Planungszyklus aus:   "));

		Vector<String> planungszyklen = getPlanungszyklen();
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

			private static final long serialVersionUID = 1L;

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
		veranstaltungen.setAutoResizeMode(veranstaltungen.AUTO_RESIZE_OFF);
		JScrollPane veranstaltungen_scroll = new JScrollPane(veranstaltungen);
		veranstaltungen.getColumnModel().getColumn(0).setPreferredWidth(200);
		veranstaltungen.getColumnModel().getColumn(1).setPreferredWidth(200);
		veranstaltungen.getColumnModel().getColumn(2).setPreferredWidth(70);
		veranstaltungen.getColumnModel().getColumn(3).setPreferredWidth(70);
//		veranstaltungen.getColumnModel().getColumn(4).setPreferredWidth(1000);
		planungsgui.add(veranstaltungen_scroll);

		return planungsgui;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector getData(String planungszyklus) {
		Vector data = new Vector();
		Reservation[] reservations;
		Allocatable zyklus;
		Allocatable[] zyklen = new Allocatable[1];
		Date start;
		Date end;
		
		zyklus = planungszyklen_allocatables.get(planungszyklus);
		start = (Date) zyklus.getClassification().getValue("startdate");
		end = (Date) zyklus.getClassification().getValue("enddate");
		zyklen[0] = zyklus;
		try {
			reservations = getClientFacade().getReservationsForAllocatable(zyklen, start, end, null);
			for (Reservation reservation : reservations){
				String veranstaltung = reservation.getClassification().getName(getLocale());
				String dozent = "";
				String dozent_id = "";
				Vector data_line = new Vector();

				Allocatable[] ressourcen = reservation.getAllocatables();
				for (Allocatable a : ressourcen) {
					DynamicType allocatableType = a.getClassification().getType();
					if (allocatableType.getKey().equals("professor")) {
						if (!dozent.equals("")) {
							dozent += "\n";
						}
						dozent += a.getClassification().getValue("title") + " " + a.getClassification().getValue("surname");
						dozent_id = a.getId();
					}
				}
				
				boolean mail = false;
				boolean recorded = false;
				
				int status = ConstraintService.getReservationStatus((String) reservation.getClassification().getValue("planungsconstraints"));
				switch(status) {
					case ConstraintService.STATUS_RECORDED:
						mail = true;
						recorded = true;
						break;
					case ConstraintService.STATUS_INVITED:
						mail = true;
						recorded = false;
						break;
					case ConstraintService.STATUS_PARTIAL_INVITED:
						mail = false;
						recorded = false;
						break;
					case ConstraintService.STATUS_PARTIAL_RECORDED:
						mail = true;
						recorded = false;
						break;
					case ConstraintService.STATUS_UNINVITED:
						mail = false;
						recorded = false;
						break;
				}
				
				User user = getClientFacade().getUser();
				RaplaContext context = getContext();
				DefaultConfiguration config = new DefaultConfiguration();
				UrlEncryption urlcrypt = new UrlEncryptionService(context, config);
				DhbwschedulerService service = new DhbwschedulerServiceImpl(context, user);
				SchedulerReservationMenuFactory menuFactory = new SchedulerReservationMenuFactory(getContext(), config, service, urlcrypt);
				
				//Lehrveranstaltung
				data_line.add(veranstaltung);
				//Dozent
				data_line.add(dozent);
				//Mail versandt?
				data_line.add(mail);
				//Rückantwort eingegeben?
				data_line.add(recorded);
				//URL
				data_line.add(menuFactory.getUrl(reservation.getId(), dozent_id));

				data.add(data_line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return data;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector getColumnNames() {
		Vector columnNames = new Vector();
		columnNames.add("Lehrveranstaltung");
		columnNames.add("Dozent");
		columnNames.add("eingeladen");
		columnNames.add("geantwortet");
		columnNames.add("URL");

		return columnNames;
	}

	@SuppressWarnings("rawtypes")
	private void refreshData(String planungszyklus) {
		Vector data = getData(planungszyklus);
		Vector columnNames = getColumnNames();
		veranstaltungen_model.setDataVector(data, columnNames);
	}

	private Vector<String> getPlanungszyklen() {
		Vector<String> planungszyklen = new Vector<String>();
		Allocatable[] ressourcen;
		try {
			ressourcen = getClientFacade().getAllocatables();
			for (Allocatable a : ressourcen) {
				DynamicType allocatableType = a.getClassification().getType();
				if (allocatableType.getKey().equals("planungszyklus")) {
					String planungszyklus = a.getClassification().getValue("name") + " / Semester " + a.getClassification().getValue("semester");
					planungszyklen.add(planungszyklus);
					planungszyklen_allocatables.put(planungszyklus, a);
				}
			}
		} catch (RaplaException e) {
			e.printStackTrace();
		}
		
//		Set set = new HashSet(planungszyklen);
//		planungszyklen = new Vector(set);
		return planungszyklen;
	}

}
