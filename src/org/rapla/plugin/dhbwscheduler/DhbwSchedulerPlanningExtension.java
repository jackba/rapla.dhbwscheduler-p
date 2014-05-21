package org.rapla.plugin.dhbwscheduler;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
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

//TODO: Buttons einbauen
//TODO: Button für URL


public class DhbwSchedulerPlanningExtension extends RaplaGUIComponent implements
		ActionListener, IdentifiableMenuEntry {


	DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());

	DhbwschedulerService service;

	String id;
	JMenuItem item;
	private static DefaultTableModel veranstaltungen_model;
	private JTable veranstaltungen_table;
	HashMap<String, Allocatable> planungszyklen_allocatables = new HashMap<String, Allocatable>();
	HashSet<Reservation> reservationList = new HashSet<Reservation>();
	
	public DhbwSchedulerPlanningExtension(RaplaContext context) {
		super(context);
		setChildBundleName(DhbwschedulerPlugin.RESOURCE_FILE);
		id = getString("planning_gui");
		item = new JMenuItem(id);
		item.setIcon(getIcon("icon.planning"));
		item.addActionListener(this);
		try {
			service = new DhbwschedulerServiceImpl(getContext(), getClientFacade().getUser());
		} catch (RaplaException e) {
			getLogger().debug(e.getMessage());
		}
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
			planungsgui = getPlanungsGui();
		}

		public JComponent getComponent() {
			return planungsgui;
		}
	}

	/**
	 * Baut das JPanel der PlanungsGUI mit allen notwendigen Daten zusammen
	 * 
	 * @return JPanel
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JPanel getPlanungsGui() {

		JPanel planungsgui = new JPanel();
		planungsgui.setLayout(new BoxLayout(planungsgui, BoxLayout.Y_AXIS));

		JPanel ueberschrift_panel = new JPanel();
		JLabel ueberschrift = new JLabel(getString("Semesterplanung"));
		ueberschrift.setFont(new Font("Sans_Serif", Font.ITALIC, 38));
		ueberschrift_panel.add(ueberschrift);
		planungsgui.add(ueberschrift_panel);

		JPanel planungszyklus_panel = new JPanel();
		planungszyklus_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		planungszyklus_panel.add(new JLabel(getString("planninggui_choose")));

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
		veranstaltungen_table = new JTable(veranstaltungen_model) {

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
		JScrollPane veranstaltungen_scroll = new JScrollPane(veranstaltungen_table);
		setColumnWidth();
		
		planungsgui.add(veranstaltungen_scroll);
		
		JPanel buttonPanel = new JPanel();
		JButton buttonPlanningOpen = new JButton();
		buttonPlanningOpen.setText(getString("Planung_oeffnen"));
		buttonPlanningOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Reservation editableEvent : reservationList){
					HelperClass.changeReservationAttribute(editableEvent , HelperClass.PLANUNGSSTATUS, getString("planning_open"));
				}
				
				JOptionPane.showMessageDialog(null, getString("planningGui_status_changed") + getString("planning_open"), getString("planning_status"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		JButton buttonPlanningClose = new JButton();
		buttonPlanningClose.setText(getString("Planung_schliessen"));
		buttonPlanningClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Reservation editableEvent : reservationList){
					HelperClass.changeReservationAttribute(editableEvent , HelperClass.PLANUNGSSTATUS, getString("planning_closed"));
				}
				
				JOptionPane.showMessageDialog(null, getString("planningGui_status_changed") + getString("planning_closed"), getString("planning_status"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		JButton buttonSchedule = new JButton();
		buttonSchedule.setText(getString("Scheduling_anstossen"));
		buttonSchedule.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				List<String> reservationIds = new ArrayList<String>();
				try 
				{
					for (Reservation editableEvent : reservationList){
						String id = editableEvent.getId();
						reservationIds.add(id);
					}

					String[] ids = reservationIds.toArray( new String[] {});
					String result = service.schedule(ids);
					getClientFacade().refresh();
					JOptionPane.showMessageDialog(null, result, getString("Scheduling_results"), JOptionPane.INFORMATION_MESSAGE);
				}
				catch (RaplaException ex )
				{
					showException( ex, null);
				}
				
			}
		});
		
		JButton buttonPlanningClosed = new JButton();
		buttonPlanningClosed.setText(getString("Planung_abschliessen"));
		buttonPlanningClosed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for (Reservation editableEvent : reservationList){
					HelperClass.changeReservationAttribute(editableEvent , HelperClass.PLANUNGSSTATUS, getString("closed"));
				}
				
				JOptionPane.showMessageDialog(null, getString("planningGui_status_changed") + getString("closed"), getString("planning_status"), JOptionPane.INFORMATION_MESSAGE);
			}
		});

		buttonPanel.add(buttonPlanningOpen);
		buttonPanel.add(buttonPlanningClose);
		buttonPanel.add(buttonSchedule);
		buttonPanel.add(buttonPlanningClosed);
		
		planungsgui.add(buttonPanel);
		return planungsgui;
	}
	
	/**
	 * Passt die Spaltenbreite der JTable an
	 */
	private void setColumnWidth() {
		veranstaltungen_table.getColumnModel().getColumn(0).setPreferredWidth(200);
		veranstaltungen_table.getColumnModel().getColumn(1).setPreferredWidth(200);
		veranstaltungen_table.getColumnModel().getColumn(2).setPreferredWidth(70);
		veranstaltungen_table.getColumnModel().getColumn(3).setPreferredWidth(70);
	}
	
	/**
	 * Ließt Informationen zu den Lehrveranstaltungen eines Planungszykluses aus
	 * 
	 * @param planungszyklus - ausgewählter Planungsstatus als String
	 * @return - Vector mit den Daten für die JTable
	 */
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
			reservations = getClientFacade().getReservations(null, start, end);
			reservationList = new HashSet<Reservation>();
			
			for (Reservation reservation : reservations){
				Allocatable planning = (Allocatable) reservation.getClassification().getValue("planungszyklus");
				if(planning.equals(zyklus)) {
					String veranstaltung = reservation.getClassification().getName(getLocale());
					String dozent = "";
					String dozent_id = "";
					Vector data_line = new Vector();
					
					reservationList.add(reservation);
					
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
					
					RaplaContext context = getContext();
					DefaultConfiguration config = new DefaultConfiguration();
					UrlEncryption urlcrypt = new UrlEncryptionService(context, config);
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
			}
		} catch (Exception e) {
			getLogger().debug(e.getMessage());
		}
		
		return data;
	}

	/**
	 * Definiert die Bezeichnungen der einzelnen Spalten der Tabelle
	 * 
	 * @return - Liefert einen Vector mit den TableHeadern
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector getColumnNames() {
		Vector columnNames = new Vector();
		columnNames.add(getString("Lehrveranstaltung"));
		columnNames.add(getString("Dozent_in"));
		columnNames.add(getString("eingeladen"));
		columnNames.add(getString("erfasst"));
		columnNames.add("URL");

		return columnNames;
	}
	
	
	/**
	 * aktualisiert die Daten der JTable mit einem neuen Planungszyklus
	 * 
	 * @param planungszyklus
	 */
	@SuppressWarnings("rawtypes")
	private void refreshData(String planungszyklus) {
		Vector data = getData(planungszyklus);
		Vector columnNames = getColumnNames();
		veranstaltungen_model.setDataVector(data, columnNames);
		setColumnWidth();
	}

	/**
	 * Ließt alle Planungszyklen aus der zugrundeliegenden Datenbank aus
	 * 
	 * @return - Vector vom Typ String mit allen gefundenen Planungszyklen
	 */
	private Vector<String> getPlanungszyklen() {
		Vector<String> planungszyklen = new Vector<String>();
		Allocatable[] ressourcen;
		try {
			ressourcen = getClientFacade().getAllocatables();
			for (Allocatable a : ressourcen) {
				DynamicType allocatableType = a.getClassification().getType();
				if (allocatableType.getKey().equals("planungszyklus")) {
					String planungszyklus = a.getClassification().getValue("name") + " / " + getString("Semester") + a.getClassification().getValue("semester");
					planungszyklen.add(planungszyklus);
					planungszyklen_allocatables.put(planungszyklus, a);
				}
			}
		} catch (RaplaException e) {
			e.printStackTrace();
		}
		
		return planungszyklen;
	}

}
