package org.rapla.plugin.dhbwscheduler;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.rapla.entities.Entity;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.RaplaObject;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.domain.Appointment;
import org.rapla.entities.domain.AppointmentBlock;
import org.rapla.entities.domain.Reservation;
import org.rapla.entities.storage.RefEntity;
import org.rapla.entities.storage.internal.SimpleIdentifier;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.framework.StartupEnvironment;
import org.rapla.framework.TypedComponentRole;
import org.rapla.gui.MenuContext;
import org.rapla.gui.ObjectMenuFactory;
import org.rapla.gui.PublishExtension;
import org.rapla.gui.PublishExtensionFactory;
import org.rapla.gui.RaplaGUIComponent;
import org.rapla.gui.toolkit.DialogUI;
import org.rapla.gui.toolkit.RaplaButton;
import org.rapla.gui.toolkit.RaplaMenuItem;
import org.rapla.plugin.dhbwscheduler.server.ConstraintService;
import org.rapla.plugin.dhbwscheduler.server.DhbwschedulerServiceImpl;
import org.rapla.servletpages.RaplaPageGenerator;
import org.rapla.storage.StorageOperator;
import org.rapla.plugin.mail.MailException;
import org.rapla.plugin.mail.MailToUserInterface;
import org.rapla.plugin.mail.server.MailapiClient;
import org.rapla.plugin.urlencryption.*;
import org.rapla.plugin.urlencryption.server.UrlEncryptionService;

import com.sun.xml.internal.ws.api.server.Container;

public class SchedulerReservationMenuFactory extends RaplaGUIComponent implements ObjectMenuFactory
{
	public static final String closed = new String("geplant");
	public static final String planning_open = new String("in Planung");
	public static final String planning_closed = new String("in Planung geschlossen");
	public static final String planungsstatus = new String("planungsstatus");
	DhbwschedulerService service;
	DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
	
	public SchedulerReservationMenuFactory( RaplaContext context, Configuration config, DhbwschedulerService service) throws RaplaException
	{
		super( context );
		this.service = service;
		setChildBundleName( DhbwschedulerPlugin.RESOURCE_FILE);
	}

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
		// create the menu entry
		/*        {
		    final RaplaMenuItem menu = new RaplaMenuItem("MY_ACTION_1");
		    menu.setText( "My Action1" );
		    // Last the action for the marked menu 
		    menu.addActionListener( new ActionListener()
		    {
		        public void actionPerformed( ActionEvent e )
		        {
		            try 
		            {
		                Entity event = selectedReservations.get( 0);
						Reservation editableEvent = getClientFacade().edit( event);
		                // do something with the reservation
		                getClientFacade().store( editableEvent ); 
		            }
		            catch (RaplaException ex )
		            {
		                showException( ex, menuContext.getComponent());
		            }
		        }
		     });
		    menus.add( menu );
        }
		 */
		{
			final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_ABSCHLIESSEN");
			menu.setText(getString("Planung_abschliessen"));
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{

					for (Reservation editableEvent : selectedReservations){
						HelperClass.changeReservationAttribute(editableEvent , planungsstatus, getString("closed"));
						}
					createMessage("planungsstatus", getString("closed"), 200, 100, menuContext, false);
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
						HelperClass.changeReservationAttribute(editableEvent , planungsstatus, getString("planning_open"));
					}
					createMessage("planungsstatus", getString("planning_open"), 200, 100, menuContext, false);
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
					for (Reservation editableEvent : selectedReservations){
						HelperClass.changeReservationAttribute(editableEvent , planungsstatus, getString("planning_closed"));
					}
					createMessage("planungsstatus", getString("planning_closed"), 200, 100, menuContext, false);
				}
			});
			menus.add( menu );
		}
		{
			final RaplaMenuItem menu = new RaplaMenuItem("SCHEDULING_ANSTOSSEN");
			menu.setText( getString("Scheduling_anstossen"));
			//menu.setIcon( getIcon("icon.help"));
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					try 
					{


						List<SimpleIdentifier> reservationIds = new ArrayList<SimpleIdentifier>();

						for ( Reservation obj: selectedReservations)
						{
							Comparable id = ((RefEntity<?>) obj).getId();
							reservationIds.add((SimpleIdentifier)id);
						}

						SimpleIdentifier[] ids = reservationIds.toArray( new SimpleIdentifier[] {});
						String result = service.schedule(ids);
						getClientFacade().refresh();
						JTextArea content = new JTextArea();
						content.setText( result);
						DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content,new String[] {"OK"});
						dialogUI.setSize( 300, 300);
						dialogUI.setTitle("Example Dialog");
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
						//Anzahl benötigter Felder ermitteln
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

									Comparable pTest = ((RefEntity<?>) r.getPersons()[t]).getId();
									SimpleIdentifier pID = (SimpleIdentifier) pTest;

									Comparable rSI = ((RefEntity<?>) r).getId();
									SimpleIdentifier resID = (SimpleIdentifier) rSI;

									String studiengang = HelperClass.getStudiengang(r);
//									if (r.getClassification().getValue("studiengang")!=null)
//									{
//										studiengang = r.getClassification().getValue("studiengang").toString();
//										if (studiengang.contains(" "))
//										{
//											int pos = studiengang.indexOf(" ");
//											studiengang = studiengang.substring(0, pos);
//										}
//									}

									p1[i] = new JPanel();
									p2[i] = new JPanel();
									p1[i].setLayout(new BoxLayout(p1[i], BoxLayout.Y_AXIS));
									p2[i].setLayout(new BoxLayout(p2[i], BoxLayout.X_AXIS));

									label[i] = new JLabel();
									labelHead.setFont(new Font("Arial",Font.BOLD,16));
									label[i].setFont(new Font("Arial",Font.CENTER_BASELINE,14));
									labelHead.setText(getString("Studiengang") + ": " + studiengang
											+ ", " + getString("Veranstaltung") + ": " + r.getName(getLocale()));
									label[i].setText(getString("Dozent") + ": " 
											+ r.getPersons()[t].getClassification().getValue("firstname").toString()
											+ " " + r.getPersons()[t].getClassification().getValue("surname").toString());

									bt[i] = new RaplaButton();
									bt[i].setText(getString("Link_oeffnen2"));
									bt[i].setToolTipText(getString("Link_oeffnen"));
									//urlField[i] = new JTextField();
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
					
					//DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
					try {
						getClientFacade().refresh();
					} catch (RaplaException e2) {
						// TODO mit Kohlhaas abklären. Refresh funktioniert nicht, sodass die Daten vom Server geladen werden.
						e2.printStackTrace();
					}
					String strTitel = getString("Email_Title");
					String strQuestion = getString("Email_senden_frage");

					int result = JOptionPane.showConfirmDialog(null, strQuestion, strTitel, JOptionPane.YES_NO_CANCEL_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						getLogger().info("E-Mail wird rausgeschickt");
						String message = "";

						//Für jede ausgewählt Reservierung wird eine E-Mail versendet.
						for (Reservation r : selectedReservations)
						{

							String veranstaltungsTitel = (String) r.getClassification().getValue("title");						
							//Constraints initialisieren oder ändern. (Neuer Dozent = neuer Constraint.)
							r = initConstraint(r);
							int dozCount = 0;
							message += veranstaltungsTitel + ": \n"; 
							//Jeder Dozent bekommt eine E-Mail
							for (int t = 0; t < r.getPersons().length; t++)
							{

								String name		= (String) r.getPersons()[t].getClassification().getValue("surname");
								String vorname	= (String) r.getPersons()[t].getClassification().getValue("firstname");
								String titel 	= (String) r.getPersons()[t].getClassification().getValue("title");
								String email 	= (String) r.getPersons()[t].getClassification().getValue("email");

								boolean isSend = false;

								Comparable compDoz = ((RefEntity<?>) r.getPersons()[t]).getId();
								SimpleIdentifier dozentID = (SimpleIdentifier) compDoz;

								Comparable pTest = ((RefEntity<?>) r).getId();
								SimpleIdentifier reservationID = (SimpleIdentifier) pTest;

								//Überprüfung ob es nötig ist eine E-Mail zu versenden.
								if(EmailVersendeBerechtigung(r,dozentID.getKey())){

									try {
										String url = getUrl(reservationID,dozentID);
										isSend = service.sendMail(reservationID, dozentID,getUser().getName(),url);
									} catch (RaplaException | UnsupportedEncodingException e1) {
										//e1.printStackTrace();
										getLogger().error(veranstaltungsTitel + ": Unable to sent e-mail to " + email);
										isSend = false;
									}
										if (isSend){
											dozCount++;
											getLogger().info( veranstaltungsTitel + ": e-mail sent to " + email);
											//message += "\t email: check ";
											//createMessage(getString("planning_open"), 200, 100, message, menuContext);

											//ändere nun den Constraint!
											String strConstraint = (String) r.getClassification().getValue("planungsconstraints");

											//nur Ändern wenn der Status nich schon 1 ist 1 = eingeladen
											if(ConstraintService.getStatus(strConstraint, dozentID.getKey()) != 1){

												String newConstraint = ConstraintService.changeDozConstraint(strConstraint, dozentID.getKey(), ConstraintService.CHANGE_SINGLESTATUS, 1);
												//Status auf eingeladen setzen;
												if (newConstraint == null){
													//Fehler beim ändern des Constraints
													//	createMessage(getString("planning_open"), 200, 100, message, menuContext);
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
									//message += "\t email: sent ";
									message += "\t " + getString("E-Mail") + ": " + getString("gesendet") + " ";
								}else{
									//message += "\t email: not sent ";
									message += "\t " + getString("E-Mail") + ": " + getString("nichtgesendet") + " ";
								}

								//status
								message += "\t status: " + getErfassungsstatusDoz(r,dozentID.getKey()) + "\n";
								String statusConstraint = (String) r.getClassification().getValue("planungsconstraints");
								r = HelperClass.changeReservationAttribute(r,"erfassungsstatus",HelperClass.getStringStatus(ConstraintService.getReservationStatus(statusConstraint)));
							}

							message += "\n";
							

						}		
						// Veranstaltung, Dozent, Senden, speichern /n
						createMessage(getString("planning_open"), message, 400,200 , menuContext, true);
						//System.exit(0);
					} else if (result == JOptionPane.NO_OPTION) {
						getLogger().info("E-Mail senden abgebrochen");
						//System.exit(0);
					}


				}

				
			});
			menus.add( menu );
		}

		return menus.toArray(new RaplaMenuItem[] {});
	}

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
	
	public String getUrl(SimpleIdentifier reservationID, SimpleIdentifier dozentId) throws UnsupportedEncodingException,RaplaException,EntityNotFoundException
	{

		StorageOperator lookup;
		Reservation veranstaltung;			

		String veranstaltungsId = String.valueOf(reservationID.getKey());

		String result;


		//Dynamische Generierung "Servername:Port"
		StartupEnvironment env = getService( StartupEnvironment.class );
		URL codeBase = env.getDownloadURL();

		UrlEncryption webservice;
		String key;


		result = codeBase + "rapla?page=scheduler-constraints&id=" + veranstaltungsId + "&dozent=" + String.valueOf(dozentId.getKey());
		webservice = getService(UrlEncryption.class);
		String encryptedParamters = webservice.encrypt("page=scheduler-constraints&id=" + veranstaltungsId + "&dozent=" + String.valueOf(dozentId.getKey()));
		key = UrlEncryption.ENCRYPTED_PARAMETER_NAME+"="+encryptedParamters;

		try{
			return new URL( codeBase,"rapla?" + key).toExternalForm();
		}catch(MalformedURLException ex){
			getLogger().info("ERROR:" + ex.toString());
			return "error";
		}


	}

	private Reservation initConstraint(Reservation r) {
		DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");

		int[] dozids = new int[r.getPersons().length];

		for (int x = 0; x < r.getPersons().length; x++){
			Comparable pDozi = ((RefEntity<?>) r.getPersons()[x]).getId();
			SimpleIdentifier pID = (SimpleIdentifier) pDozi;

			dozids[x] = pID.getKey();
		}

		String newConstraint = ConstraintService.initDozConstraint(strConstraint,dozids);

		return HelperClass.changeReservationAttribute(r,"planungsconstraints",newConstraint);

	}


	/*
	 *Überprüfung, ob bei dieser Veranstalltung eine E-Mail versendet wird.
	 */
	private boolean EmailVersendeBerechtigung(Reservation r, int dozentenID) {

		final int intUneingeladen = 0;
		final int intEingeladen = 1;
		final int intErfasst = 2;
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");
		//ConstraintService.buildDozConstraint(dozentenID, null, null, status);
		int erfassungsstatus = ConstraintService.getStatus(strConstraint, dozentenID);

		boolean returnvalue = false;

		if(r.getClassification().getValue("planungsstatus").equals(planning_closed) ||
				r.getClassification().getValue("planungsstatus").equals(closed)){
			returnvalue = false;
		}else{
			switch(erfassungsstatus){
			case intUneingeladen:
				returnvalue = true;
				break;
			case intEingeladen:
				returnvalue = true;
				break;
			case intErfasst:
				returnvalue = false;
				break;
			default:
				break;
			}
		}


		return returnvalue;
	}

	
	private String getErfassungsstatusDoz(Reservation r, int key) {
		
		DhbwschedulerReservationHelper HelperClass = new DhbwschedulerReservationHelper(getContext());
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");
		
		if (strConstraint ==null){
			return null;
		}
		
		return HelperClass.getStringStatus(ConstraintService.getStatus(strConstraint, key));
	}
	
}
