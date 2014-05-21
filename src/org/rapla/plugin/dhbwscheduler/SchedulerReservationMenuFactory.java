package org.rapla.plugin.dhbwscheduler;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Reservation;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.framework.StartupEnvironment;
import org.rapla.gui.MenuContext;
import org.rapla.gui.ObjectMenuFactory;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.RaplaButton;
import org.rapla.gui.toolkit.RaplaMenuItem;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;
import org.rapla.plugin.urlencryption.UrlEncryption;

public class SchedulerReservationMenuFactory extends RaplaGUIComponent implements ObjectMenuFactory
{
	DhbwschedulerService service;
	DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
	UrlEncryption urlEncryption;
	
	public SchedulerReservationMenuFactory( RaplaContext context, Configuration config, DhbwschedulerService service, UrlEncryption urlEncryption) throws RaplaException
	{
		super( context );
		this.service = service;
		this.urlEncryption = urlEncryption;
		setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RaplaMenuItem[] create( final MenuContext menuContext, final RaplaObject focusedObject )
	{
		Collection selectedObjects = menuContext.getSelectedObjects();
		Collection selectionList = new HashSet();

		if ( selectedObjects != null )
		{
			selectionList.addAll( selectedObjects);
		}

		if ( focusedObject != null)
		{
			selectionList.add( focusedObject);
		}
		final List<Reservation> selectedReservations = new ArrayList<Reservation>();
		for ( Object obj: selectionList)
		{
			if ( obj instanceof Reservation)
			{
				selectedReservations.add( (Reservation) obj);
			}
			if ( obj instanceof Appointment)
			{
				selectedReservations.add( ((Appointment) obj).getReservation());
			}
			if ( obj instanceof AppointmentBlock)
			{
				selectedReservations.add( ((AppointmentBlock) obj).getAppointment().getReservation());
			}

		}
		if ( selectedReservations.size() == 0)
		{
			return RaplaMenuItem.EMPTY_ARRAY;
		}
		List<RaplaMenuItem> menus = new ArrayList<RaplaMenuItem>();
		
		{
			final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_ABSCHLIESSEN");
			menu.setText(getString("Planung_abschliessen"));
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					
					for (Reservation editableEvent : selectedReservations){
						HelperClass.changeReservationAttribute(editableEvent , HelperClass.PLANUNGSSTATUS, getString("closed"));
						}
					createMessage(getString("planning_status"), getString("planningGui_status_changed") + " " + getString("closed"), 200, 200, menuContext, false);
				}
			});
			menus.add( menu );
		}
		{
			final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_OEFFNEN");
			menu.setText(getString("Planung_oeffnen"));
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					for (Reservation editableEvent : selectedReservations){
						HelperClass.changeReservationAttribute(editableEvent , HelperClass.PLANUNGSSTATUS, getString("planning_open"));
					}
					createMessage(getString("planning_status"), getString("planningGui_status_changed") + " " + getString("planning_open"), 200, 200, menuContext, false);
				}
			});
			menus.add( menu );
		}
		{
			final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_SCHLIESSEN");
			menu.setText( getString("Planung_schliessen"));
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					String veranstaltungen = "";
					for (Reservation editableEvent : selectedReservations){
						String statusConstraint = (String) editableEvent.getClassification().getValue("planungsconstraints");
						int status = ConstraintService.getReservationStatus(statusConstraint);
						if (status != ConstraintService.STATUS_RECORDED){
							veranstaltungen += "\n" + editableEvent.getClassification().getName(getLocale());
						}
						HelperClass.changeReservationAttribute(editableEvent , HelperClass.PLANUNGSSTATUS, getString("planning_closed"));
					}
					createMessage(getString("planning_status"), getString("planningGui_status_changed") + " " + getString("planning_closed"), 200, 200, menuContext, false);
					if (veranstaltungen != ""){
						createMessage(getString("planning_status"), getString("planningGui_status_changed") + " " +  getString("planning_closed") + getString("missing_planing_constraints") + veranstaltungen, 200, 100, menuContext, false);
					}
				}
			});
			menus.add( menu );
		}
		{
			final RaplaMenuItem menu = new RaplaMenuItem("SCHEDULING_ANSTOSSEN");
			menu.setText( getString("Scheduling_anstossen"));
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					try 
					{


						List<String> reservationIds = new ArrayList<String>();

						for ( Reservation obj: selectedReservations)
						{
							String id = obj.getId();
							reservationIds.add(id);
						}

						String[] ids = reservationIds.toArray( new String[] {});
						String result = service.schedule(ids);
						getClientFacade().refresh();
						JTextArea content = new JTextArea();
						content.setText( result);
						DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content,new String[] {"OK"});
						dialogUI.setSize( 500, 300);
						dialogUI.setTitle(getString("Scheduling_results"));
						if (menuContext.getPoint() != null)
						{    
							dialogUI.setLocation( menuContext.getPoint() );
						}
						dialogUI.startNoPack();
					}
					catch (RaplaException ex )
					{
						showException( ex, menuContext.getComponent());
					}
				}
			});
			menus.add( menu );
		}
		{
			final RaplaMenuItem menu = new RaplaMenuItem("ERFASSUNG");
			menu.setText( getString("Erfassungslink") );
			menu.addActionListener( new ActionListener()
			{

				public void actionPerformed( ActionEvent e )
				{
					try 
					{	
						boolean isValidated = false;
						//Anzahl benötigter Felder ermitteln
						isValidated = checkCompleteReservation(selectedReservations);
						if(isValidated){
							int felder = 0;
							for (Reservation r : selectedReservations)
							{
								for (int t = 0; t < r.getPersons().length; t++)
								{
									felder++;
								}
							}

							JLabel[] label = new JLabel[felder];
							JLabel labelHead = new JLabel();
							JPanel panel = new JPanel();
							JPanel[] p1 = new JPanel[felder];
							JPanel[] p2 = new JPanel[felder];
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

							//JTextField[] urlField = new JTextField[felder];
							JTextArea[] urlField = new JTextArea[felder];
							RaplaButton[] copyButton = new RaplaButton[felder];
							RaplaButton[] bt = new RaplaButton[felder];

							try {
								int i = 0;
								for (Reservation r : selectedReservations)
								{
									for (int t = 0; t < r.getPersons().length; t++)
									{

										String pTest =  r.getPersons()[t].getId();
										String pID =  pTest;

										String rSI =  r.getId();
										String resID =  rSI;

										String studiengang = HelperClass.getStudiengang(r);

										p1[i] = new JPanel();
										p2[i] = new JPanel();
										p1[i].setLayout(new BoxLayout(p1[i], BoxLayout.Y_AXIS));
										p2[i].setLayout(new BoxLayout(p2[i], BoxLayout.X_AXIS));

										label[i] = new JLabel();
										labelHead.setFont(new Font("Arial",Font.BOLD,16));
										label[i].setFont(new Font("Arial",Font.CENTER_BASELINE,14));
										labelHead.setText(getString("Studiengang") + ": " + studiengang
												+ ", " + getString("Veranstaltung") + ": " + r.getName(getLocale()));
										label[i].setText(getString("Dozent_in") + ": " 
												+ r.getPersons()[t].getClassification().getValue("firstname").toString()
												+ " " + r.getPersons()[t].getClassification().getValue("surname").toString());

										bt[i] = new RaplaButton();
										bt[i].setText(getString("Link_oeffnen2"));
										bt[i].setToolTipText(getString("Link_oeffnen"));
										urlField[i] = new JTextArea();
										urlField[i].setWrapStyleWord(true);
										urlField[i].setLineWrap(true);
										urlField[i].setText(getUrl(resID,pID));
										urlField[i].setSize(100, 20);
										urlField[i].setEditable(false);
										copyButton[i] = new RaplaButton();
										copyButton[i].setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
										copyButton[i].setFocusable(false);
										copyButton[i].setRolloverEnabled(false);
										copyButton[i].setIcon(getIcon("icon.copy"));
										copyButton[i].setToolTipText(getString("copy_to_clipboard"));
										addCopyPaste(urlField[i]);
										final String help = urlField[i].getText();
										final URI uri = new URI(urlField[i].getText());
										bt[i].addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												if (Desktop.isDesktopSupported())
												{
													try {
														Desktop.getDesktop().browse(uri);
													}
													catch (IOException ex)
													{
														;
													}
												}
											}
										});
										copyButton[i].addActionListener(new ActionListener() {
											public void actionPerformed(ActionEvent e) {
												StringSelection stringSelection = new StringSelection(help);
												Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
												clipBoard.setContents(stringSelection, stringSelection);
											}

										});
										urlField[i].setAlignmentX(Component.LEFT_ALIGNMENT);
										p1[i].setAlignmentX(Component.LEFT_ALIGNMENT);
										p2[i].setAlignmentX(Component.LEFT_ALIGNMENT);
										Component placeHolderVb = Box.createVerticalStrut(20);
										Component placeHolderVl = Box.createVerticalStrut(5);
										Component placeHolderHl = Box.createHorizontalStrut(5);
										if (i>0)
										{
											p1[i].add(placeHolderVb);
										}
										if (i==0)
										{
											p1[i].add(labelHead);
											p1[i].add(placeHolderVb);
										}
										p1[i].add(label[i]);
										p1[i].add(placeHolderVl);
										p1[i].add(urlField[i]);
										p1[i].add(placeHolderVl);
										p2[i].add(bt[i]);
										p2[i].add(placeHolderHl);
										p2[i].add(copyButton[i]);
										panel.add(p1[i]);
										panel.add(p2[i]);
										i++;
									}
								}

							} catch (UnsupportedEncodingException e1) {
								getLogger().info("ERROR:" + e1.toString());
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								getLogger().info("ERROR:" + e1.toString());
								e1.printStackTrace();
							}

							DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,panel,new String[] {"OK"});
							dialogUI.setSize( 600, 300);
							dialogUI.setTitle(getString("Erfassungslink"));
							if (menuContext.getPoint() != null)
							{    
								dialogUI.setLocation( menuContext.getPoint() );
							}
							dialogUI.startNoPack();

						}
					
					}
					catch (RaplaException ex )
					{
						showException( ex, menuContext.getComponent());
					} 
				}
			});
			menus.add( menu );
		}
		{

			final RaplaMenuItem menu = new RaplaMenuItem("EMAIL_SENDEN");
			menu.setText(getString("E-Mail_senden"));
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					int result = -1;
					//DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
					try {
						getClientFacade().refresh();
					} catch (RaplaException e2) {
						e2.printStackTrace();
					}
					String strTitel = getString("Email_Title");
					String strQuestion = getString("Email_senden_frage");
					
					if (checkCompleteReservation(selectedReservations)){
						result = JOptionPane.showConfirmDialog(null, strQuestion, strTitel, JOptionPane.YES_NO_CANCEL_OPTION);
					}
					
					if (result == JOptionPane.YES_OPTION) {
						
						
						getLogger().info("E-Mail wird gesendet");
						String message = "";

						//Für jede ausgewählt Reservierung wird eine E-Mail versendet.
						for (Reservation r : selectedReservations)
						{

							String veranstaltungsTitel = (String) r.getClassification().getValue("title");						
							//Constraints initialisieren oder ändern. (Neuer Dozent = neuer Constraint.)
							r = initConstraint(r);
							message += veranstaltungsTitel + ": \n"; 
							//Jeder Dozent bekommt eine E-Mail
							for (int t = 0; t < r.getPersons().length; t++)
							{

								String name		= (String) r.getPersons()[t].getClassification().getValue("surname");
								String vorname	= (String) r.getPersons()[t].getClassification().getValue("firstname");
								String titel 	= (String) r.getPersons()[t].getClassification().getValue("title");
								String email 	= (String) r.getPersons()[t].getClassification().getValue("email");

								boolean isSend = false;

								String dozentID =  r.getPersons()[t].getId();
								
								String pTest = r.getId();
								String reservationID = pTest;

								//Überprüfung ob es nötig ist eine E-Mail zu versenden.
								if(EmailVersendeBerechtigung(r,dozentID)){

									try {
										String url = getUrl(reservationID,dozentID);
										isSend = service.sendMail(reservationID, dozentID,getUser().getName(),url);
									} catch (RaplaException | UnsupportedEncodingException e1) {
										//e1.printStackTrace();
										getLogger().error(veranstaltungsTitel + ": Unable to sent e-mail to " + email);
										isSend = false;
									}
										if (isSend){
											getLogger().info( veranstaltungsTitel + ": e-mail sent to " + email);
											//createMessage(getString("planning_open"), 200, 100, message, menuContext);

											//ändere nun den Constraint!
											String strConstraint = (String) r.getClassification().getValue("planungsconstraints");

											//nur Ändern wenn der Status nich schon 1 ist 1 = eingeladen
											if(ConstraintService.getStatus(strConstraint, dozentID) != ConstraintService.STATUS_INVITED){

												String newConstraint = ConstraintService.changeDozConstraint(strConstraint, dozentID, ConstraintService.CHANGE_SINGLESTATUS, ConstraintService.STATUS_INVITED);
												//Status auf eingeladen setzen;
												if (newConstraint == null){
													//Fehler beim ändern des Constraints
												}else{
													r = HelperClass.changeReservationAttribute(r,"planungsconstraints",newConstraint );
													
													getLogger().info("Change for " + veranstaltungsTitel + " sucessfull");
													
												}
											}

										}

									
								}

								//Message zusammenbauen

								message += "   " + titel + " " + vorname + " " + name + ": ";

								//email
								if(isSend){
									message += "\t " + getString("E-Mail") + ": " + getString("gesendet") + " ";
								}else{
									//message += "\t email: not sent ";
									message += "\t " + getString("E-Mail") + ": " + getString("nichtgesendet") + " ";
								}

								//status
								message += "\t status: " + getErfassungsstatusDoz(r,dozentID) + "\n";
								String statusConstraint = (String) r.getClassification().getValue("planungsconstraints");
								r = HelperClass.changeReservationAttribute(r,"erfassungsstatus",HelperClass.getStringStatus(ConstraintService.getReservationStatus(statusConstraint)));
							}

							message += "\n";
							

						}		
						// Veranstaltung, Dozent, Senden, speichern /n
						createMessage(getString("planning_open"), message, 400,200 , menuContext, true);
					} else if (result == JOptionPane.NO_OPTION) {
						getLogger().info("E-Mail senden abgebrochen");
					}


				}

				

				
			});
			menus.add( menu );
		}

		return menus.toArray(new RaplaMenuItem[] {});
	}
	/**
	 * Check the Reservation if they are validate for send to the Clients
	 * @param selectedReservations
	 * @return
	 */
	private boolean checkCompleteReservation(List<Reservation> selectedReservations) {
		String Message = getI18n().getString("Folgende_Veranstaltung") + "\n";
		String Messagezusatz = "";
		boolean returnValue = true;
		boolean hasCourse = false;
		boolean reservationValid = true;
		try{
			for (Reservation r : selectedReservations)
			{
				reservationValid = true;
				hasCourse = false;
				Messagezusatz = getI18n().getString("Veranstaltung") + ": " + (String) r.getClassification().getValue("title") + "\n";
				for (Allocatable kurs : r.getResources()){
					if(kurs.getClassification().getType().getKey().equals("kurs")){
						hasCourse = true;
					}
				}
				
				if (!hasCourse){
					reservationValid = false;
					Messagezusatz += "    " + getI18n().getString("kein_Kurs") + " \n";
				}
				
				if(r.getPersons() != null && r.getPersons().length > 0){
					for (Allocatable persons : r.getPersons()){
						
						if (persons.getClassification().getValue("email") == null || persons.getClassification().getValue("email").equals("")){
							Messagezusatz += "    " + getI18n().getString("Email_von") + " " +persons.getClassification().getValue("surname")+ " " + getI18n().getString("fehlt") + " \n";
							reservationValid = false;
						}
					}
				}else{
					Messagezusatz += "    " + getI18n().getString("kein_Dozent") + " \n";
				}

				if(r.getClassification().getValue("planungszyklus") != null){
					
					Allocatable planzykl = (Allocatable) r.getClassification().getValue("planungszyklus");
					
					if(planzykl.getClassification().getValue("semester") == null || planzykl.getClassification().getValue("semester").equals("")){
						reservationValid = false;
						Messagezusatz += "    " + getI18n().getString("semester_Planungszyklus") + " \n";
					}
					
					if(planzykl.getClassification().getValue("startdate") == null || planzykl.getClassification().getValue("startdate").equals("")){
						reservationValid = false;
						Messagezusatz += "    " + getI18n().getString("startDatum_Planungszyklus") + " \n";
					}
					
					if(planzykl.getClassification().getValue("enddate") == null || planzykl.getClassification().getValue("enddate").equals("")){
						reservationValid = false;
						Messagezusatz += "    " + getI18n().getString("endDatum_Planungszyklus") + " \n";
					}
					
				}else{
					reservationValid = false;
					Messagezusatz += "    " + getI18n().getString("kein_Planungszyklus") + " \n";
				}
				
				if(r.getClassification().getValue("studiengang") == null) {
					reservationValid = false;
					Messagezusatz += "    " + getI18n().getString("kein_Studiengang") + " \n";
				}
				
				if(!reservationValid){
					returnValue = false;
					Message += Messagezusatz;
				}
			}
		}catch(Exception e){
			getLogger().error("Fehler bei der Überprüfung der Veranstaltung");
			returnValue = false;
			Message = "Fehler bei der der Überprüfung der Veranstaltungen";
			e.printStackTrace();
		}
		
		if(!returnValue){
			Message += "\n " + getI18n().getString("Programm_abgebrochen");
			JOptionPane.showMessageDialog(null, Message);
		}
		
		return returnValue;
	}
	/**
	 * Generate a Message with the Parameters
	 * @param title
	 * @param message
	 * @param x
	 * @param y
	 * @param menuContext
	 * @param pack
	 */
	private void createMessage(String title, String message, int x, int y, final MenuContext menuContext, boolean pack){
		JTextArea content = new JTextArea();
		content.setText(message);
		DialogUI dialogUI;
		try {
			dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content, new String[] {"OK"});

			dialogUI.setSize(x,y);
			dialogUI.setTitle(title);
			if (menuContext.getPoint() != null)
			{    
				dialogUI.setLocation( menuContext.getPoint() );
			}

			if (pack){
				dialogUI.start();
			}else{
				dialogUI.startNoPack();
			}

		}catch (RaplaException e) {
			e.printStackTrace();
			getLogger().info("ERROR:" + e.toString());
		}
	}
	
	/**
	 * Generate a URL for the Reservation and Person
	 * @param reservationID
	 * @param dozentId
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws RaplaException
	 * @throws EntityNotFoundException
	 */
	public String getUrl(String reservationID, String dozentId) throws UnsupportedEncodingException,RaplaException,EntityNotFoundException
	{
//		String result = "";

		//Dynamische Generierung "Servername:Port"
		StartupEnvironment env = getService( StartupEnvironment.class );
		URL codeBase = env.getDownloadURL();

		String key;
		try {
			String encryptedParamters = urlEncryption.encrypt("page=scheduler-constraints&id=" + reservationID + "&dozent=" + dozentId);
			key = UrlEncryption.ENCRYPTED_PARAMETER_NAME+"="+encryptedParamters;
		} catch( UnsupportedOperationException e) {
			//URLEncryption funktioniert nicht
			key = "page=scheduler-constraints&id=" + reservationID + "&dozent=" + dozentId;
		}

		try{
			return new URL( codeBase,"rapla?" + key).toExternalForm();
		}catch(MalformedURLException ex){
			getLogger().info("ERROR:" + ex.toString());
			return "error";
		}


	}
	
	/**
	 * Generate a new Constraint for this Reservation (r)
	 * @param r
	 * @return
	 */
	private Reservation initConstraint(Reservation r) {
		DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");

		String[] dozids = new String[r.getPersons().length];

		for (int x = 0; x < r.getPersons().length; x++){
			dozids[x] = r.getPersons()[x].getId();
		}

		String newConstraint = ConstraintService.initDozConstraint(strConstraint,dozids);

		return HelperClass.changeReservationAttribute(r,"planungsconstraints",newConstraint);

	}



	/**
	 * Check Status value of the Reservation if an E-Mail can be send
	 * @param r
	 * @param dozentenID
	 * @return
	 */
	private boolean EmailVersendeBerechtigung(Reservation r, String dozentenID) {
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");
		//ConstraintService.buildDozConstraint(dozentenID, null, null, status);
		int erfassungsstatus = ConstraintService.getStatus(strConstraint, dozentenID);

		boolean returnvalue = false;

		if(r.getClassification().getValue("planungsstatus").equals(HelperClass.PLANNING_CLOSED) ||
				r.getClassification().getValue("planungsstatus").equals(HelperClass.CLOSED)){
			returnvalue = false;
		}else{
			switch(erfassungsstatus){
			case ConstraintService.STATUS_UNINVITED:
				returnvalue = true;
				break;
			case ConstraintService.STATUS_INVITED:
				returnvalue = true;
				break;
			case ConstraintService.STATUS_RECORDED:
				returnvalue = false;
				break;
			default:
				break;
			}
		}


		return returnvalue;
	}

	/**
	 * returns the Status from this Reservation
	 * @param r
	 * @param key
	 * @return
	 */
	private String getErfassungsstatusDoz(Reservation r, String key) {
		
		DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");
		
		if (strConstraint ==null){
			return null;
		}
		
		return HelperClass.getStringStatus(ConstraintService.getStatus(strConstraint, key));
	}
	
}
