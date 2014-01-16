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
	DhbwschedulerService service;
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
			menu.setText( "Planung abschliessen" );
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					try 
					{
						Entity event = selectedReservations.get( 0);
						Reservation editableEvent = getClientFacade().edit( event);

						setDesignStatus(editableEvent, getString("closed"));
						createMessage(getString("design_status"), getString("closed"), 200, 100, menuContext, false);
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
			final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_OEFFNEN");
			menu.setText( "Planung oeffnen" );
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
						setDesignStatus(editableEvent, getString("planning_open"));
						createMessage(getString("design_status"), getString("planning_open"), 200, 100, menuContext, false);

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
			final RaplaMenuItem menu = new RaplaMenuItem("PLANUNG_SCHLIESSEN");
			menu.setText( "Planung schliessen" );
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
						setDesignStatus(editableEvent, getString("planning_closed"));
						createMessage(getString("design_status"), getString("planning_closed"), 200, 100, menuContext, false);

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
			final RaplaMenuItem menu = new RaplaMenuItem("SCHEDULING_ANSTOSSEN");
			menu.setText( "Scheduling anstossen" );
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
						String result = service.schedule( ids);
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
			menu.setText( "Erfassungslink öffnen" );
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
									
									String studiengang = "";
									if (r.getClassification().getValue("studiengang")!=null)
									{
										studiengang = r.getClassification().getValue("studiengang").toString();
										if (studiengang.contains(" "))
										{
											int pos = studiengang.indexOf(" ");
											studiengang = studiengang.substring(0, pos);
										}
									}

									p1[i] = new JPanel();
									p2[i] = new JPanel();
									p1[i].setLayout(new BoxLayout(p1[i], BoxLayout.Y_AXIS));
									p2[i].setLayout(new BoxLayout(p2[i], BoxLayout.X_AXIS));

									label[i] = new JLabel();
									labelHead.setFont(new Font("Arial",Font.BOLD,16));
									label[i].setFont(new Font("Arial",Font.CENTER_BASELINE,14));
									//TODO Übersetzung?
									labelHead.setText("Studiengang " + studiengang
											+ ", Veranstaltung: " + r.getName(getLocale()));
									label[i].setText("Dozent: " 
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
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (URISyntaxException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,panel,new String[] {"OK"});
						dialogUI.setSize( 600, 300);
						dialogUI.setTitle("Erfassungslink");
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
			
			//TODO ÜBersetzungen und Message bearbeiten. Logger infos richtig setzen.
			final RaplaMenuItem menu = new RaplaMenuItem("EMAIL_SENDEN");
			menu.setText( "E-Mail senden" );
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					
					//createMessage("email", "möchten Sie die email an die ausgewählten Veranstaltungen senden?", 100, 100, menuContext, true);
					//TODO Wollen Sie wirklich senden
					
					String message = "";
					
					//Für jede ausgewählt Reservierung wird eine E-Mail versendet.
					for (Reservation r : selectedReservations)
					{
						
						String veranstaltungsTitel = (String) r.getClassification().getValue("title");						
						//Constraints initialisieren oder ändern. (Neuer Dozent = neuer Constraint.)
						r = initConstraint(r);
						int dozCount = 0;
						
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
									
									if (isSend){
										dozCount++;
										getLogger().info( veranstaltungsTitel + ": e-mail sent to " + email);
										message += "\t email: check ";
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
												r = changeReservationAttribute(r,"planungsconstraints",newConstraint );
												getLogger().info("Change for " + veranstaltungsTitel + " sucessfull");
												
											}
										}
										
									}
									
								} catch (RaplaException | UnsupportedEncodingException e1) {
									e1.printStackTrace();
									getLogger().error(veranstaltungsTitel + ": Unable to sent e-mail to " + email);
									
									
								}
							}
							
							//Message zusammenbauen
							message += veranstaltungsTitel + ": \n"; 
							message += "   " + titel + " " + vorname + " " + name + ": ";
							
							//email
							if(isSend){
								message += "\t email: sent ";
							}else{
								message += "\t email: not sent ";
							}
							
							//status
							message += "\t status: " + getErfassungsstatus(r,dozentID.getKey()) + "\n";
							
						}
						
						message += "\n";
				
					}		
						// Veranstaltung, Dozent, Senden, speichern /n
						createMessage(getString("planning_open"), message, 400,200 , menuContext, true);
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
			// TODO Sinvolle Loggerinfo einbauen
			e.printStackTrace();
		}
	}

	private void setDesignStatus(Reservation editableEvent, String zielStatus) throws RaplaException{
		String istStatus = (String) editableEvent.getClassification().getValue("planungsstatus");
		if (istStatus != zielStatus) {
			editableEvent.getClassification().setValue("planungsstatus", zielStatus);
		}
		getClientFacade().store( editableEvent ); 
	}
	
	private Reservation changeReservationAttribute(Reservation r ,String Attribute, String value){
		try {
			Reservation editableEvent = getClientFacade().edit( r);
			editableEvent = getClientFacade().edit( r);
			editableEvent.getClassification().setValue(Attribute, value);
			getClientFacade().store( editableEvent );
			getClientFacade().refresh();
			return editableEvent;
		} catch (RaplaException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return r;
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
			return "error";
		}


	}
	
	private Reservation initConstraint(Reservation r) {
		
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");

		if (strConstraint == null){
			int[] dozentid = new int[r.getPersons().length];
			int[] newStatus = new int[r.getPersons().length];

			for (int x = 0; x < r.getPersons().length; x++){

				Comparable pDozi = ((RefEntity<?>) r.getPersons()[x]).getId();
				SimpleIdentifier pID = (SimpleIdentifier) pDozi;

				dozentid[x] 	= pID.getKey();
				newStatus[x] 	= 0;

			}

			strConstraint = ConstraintService.buildDozConstraint(dozentid, new String[r.getPersons().length], new Date[r.getPersons().length][], newStatus);
			return changeReservationAttribute(r,"planungsconstraints",strConstraint);

		}else{
			
			String newConstraint ="";
			
			for (int x = 0; x < r.getPersons().length; x++){
				
				Comparable pDozi = ((RefEntity<?>) r.getPersons()[x]).getId();
				SimpleIdentifier pID = (SimpleIdentifier) pDozi;
				boolean hit = false;
				
				// alte rausschmeisen
				
				//ist der Dozent schon vorhanden, wird der Constraint beibehalten, bei einem neuen Dozenten wird dieser hinzugefügt. 
				for(int i = 0; i< ConstraintService.getDozIDs(strConstraint).length ; i++){
					
					int key = ConstraintService.getDozIDs(strConstraint)[i];
					
					if (key == pID.getKey()){
						
						newConstraint += ConstraintService.buildDozConstraint(key, 
								ConstraintService.getDozStringConstraint(strConstraint, key), 
								ConstraintService.getExceptionDatesDoz(strConstraint, key),								
								ConstraintService.getStatus(strConstraint, key)); 
						
						hit = true;		
					}
				}
				//neuer Dozent
				if(!hit){
					newConstraint += ConstraintService.buildDozConstraint(pID.getKey(),	null, null ,0);
				}
				
				if(x < r.getPersons().length-1) {
					newConstraint += "\n";
				}
				
			}
			return changeReservationAttribute(r,"planungsconstraints",newConstraint);
		}
	}
	

	/*
	 *Überprüfung, ob bei dieser Veranstalltung eine E-Mail versendet wird.
	 */
	private boolean EmailVersendeBerechtigung(Reservation r, int dozentenID) {
		
		
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");
		//ConstraintService.buildDozConstraint(dozentenID, null, null, status);
		int erfassungsstatus = ConstraintService.getStatus(strConstraint, dozentenID);
		
		boolean returnvalue = false;

		if(r.getClassification().getValue("planungsstatus").equals(planning_closed) ||
				r.getClassification().getValue("planungsstatus").equals(closed)){
			returnvalue = false;
		}else{
			//TODO Konstanten definieren?
			switch(erfassungsstatus){
			//uneingeladen
			case 0:
				returnvalue = true;
				break;
			//eingeladen
			case 1:
				returnvalue = true;
				break;
			//erfasst
			case 2:
				returnvalue = false;
				break;
			default:
				break;
			}
		}


		return returnvalue;
	}

	private String getErfassungsstatus(Reservation r, int key) {
		// TODO Übersetzungen
		String strConstraint = (String) r.getClassification().getValue("planungsconstraints");
		String returnvalue = "";
		if (strConstraint ==null){
			return null;
		}
		
		switch(ConstraintService.getStatus(strConstraint, key)){
		case 0:
			returnvalue = "uneingeladen";
			break;
		case 1:
			returnvalue = "eingeladen";
			break;
		case 2:
			returnvalue = "erfasst";
			break;
		default:
			returnvalue = "error";							
		};
		
		return returnvalue;
	}
	
}
