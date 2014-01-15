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
						createMessage(getString("closed"), 200, 100, getString("design_status"), menuContext);
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
						createMessage(getString("planning_open"), 200, 100, getString("design_status"), menuContext);

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
						createMessage(getString("planning_closed"), 200, 100, getString("design_status"), menuContext);

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
									urlField[i].setText(getUrl(r,pID.getKey()));
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
			final RaplaMenuItem menu = new RaplaMenuItem("EMAIL_SENDEN");
			menu.setText( "E-Mail senden" );
			// Last the action for the marked menu 
			menu.addActionListener( new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					//Für jede ausgewählt Reservierung wird eine E-Mail versendet.
					for (Reservation r : selectedReservations)
					{
						//Überprüfung ob es nötig ist eine E-Mail zu versenden.
						if(EmailVersendeBerechtigung(r)){

							//Jeder Dozent bekommt eine E-Mail
							for (int t = 0; t < r.getPersons().length; t++)
							{
								Comparable pTest = ((RefEntity<?>) r.getPersons()[t]).getId();
								SimpleIdentifier pID = (SimpleIdentifier) pTest;
								Sende_mail(r,pID);
							}
						}


					}
				}

				/*
				 *Überprüfung, ob bei dieser Veranstalltung eine E-Mail versendet wird.
				 */
				private boolean EmailVersendeBerechtigung(Reservation r) {

					String erfassungsstatus = (String) r.getClassification().getValue("erfassungsstatus");
					boolean returnvalue = false;

					if(r.getClassification().getValue("planungsstatus").equals(planning_closed) ||
							r.getClassification().getValue("planungsstatus").equals(closed)){
						returnvalue = false;
					}else{
						switch(erfassungsstatus){
						case "uneingeladen":
							returnvalue = true;
							break;
						case "eingeladen":
							returnvalue = true;
							break;
						case "erfasst":
							returnvalue = false;
							break;
						default:
							break;
						}
					}


					return returnvalue;
				}

				private void Sende_mail(Reservation r, SimpleIdentifier pID) {
					// TODO Auto-generated method stub
					try {
						boolean isPerson = false;
						boolean reminder = false;
						String email = "";
						String name = "";
						String vorname = "";
						String titel = "";

						for(int i = 0 ; i<r.getPersons().length; i++){
							Comparable pTest = ((RefEntity<?>) r.getPersons()[i]).getId();
							SimpleIdentifier dID = (SimpleIdentifier) pTest;

							if(dID.getKey() == pID.getKey()){
								isPerson 	= r.getPersons()[i].isPerson();
								email		= (String) r.getPersons()[i].getClassification().getValue("email");
								name		= (String) r.getPersons()[i].getClassification().getValue("surname");
								vorname		= (String) r.getPersons()[i].getClassification().getValue("firstname");
								titel 		= (String) r.getPersons()[i].getClassification().getValue("title");
								break;
							}

						}

						//Dozent.getClassification().getValue("");
						if(isPerson){	
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
							String veranstaltungstitel 	= (String) r.getClassification().getValue("title");
							String betreff;
							String url = getUrl(r,pID.getKey());
							
							String erfassungsstatus = (String) r.getClassification().getValue("erfassungsstatus");
							switch(erfassungsstatus){
							case "uneingeladen":
								reminder=false;
								break;
							case "eingeladen":
								reminder=true;
								break;
							case "erfasst":
								reminder=false;
								break;
							default:
								break;
							}
							
							if(reminder){
								betreff = getString("email_Betreff_Erinnerung");
							}else{
								betreff = getString("email_Betreff_Einladung");
							}
							betreff += veranstaltungstitel;

							String Inhalt = getString("email_anrede") + titel+ " " + vorname + " " + name + ",\n\n" + 
									getString("email_Inhalt") + "\n" + 
									veranstaltungstitel +  " (" + studiengang + ")" + "\n\n"  +
									getString("Link_Text") + "\n" +
									url + "\n\n" +
									getString("email_Signatur") + "\n" + 
									getUser().getName() + "\n";
							//getUser().getEmail();
								
							//Muss das so gemacht werden ?????????
							MailToUserInterface MailClient = getService(MailToUserInterface.class);
							MailClient.sendMail(getUser().getUsername(), betreff, Inhalt);

							//createMessage(Inhalt, 200, 100, "Planungsstatus", menuContext);
							//Link generieren
							// Text einfügen
							//Senden!

						}else{
							;
						}
					} catch ( RaplaException | UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
			menus.add( menu );
		}

		return menus.toArray(new RaplaMenuItem[] {});
	}

	private void createMessage(String message, int x, int y, String title, final MenuContext menuContext) throws RaplaException {
		JTextArea content = new JTextArea();
		content.setText(message);
		DialogUI dialogUI = DialogUI.create( getContext(), menuContext.getComponent(), false,content,new String[] {"OK"});
		dialogUI.setSize(x,y);
		dialogUI.setTitle(title);
		if (menuContext.getPoint() != null)
		{    
			dialogUI.setLocation( menuContext.getPoint() );
		}
		dialogUI.startNoPack();
	}

	private String getUrl(Reservation selectedReservation, int dozentId) throws UnsupportedEncodingException
	{
		Comparable test = ((RefEntity<?>) selectedReservation).getId();
		SimpleIdentifier id = (SimpleIdentifier) test;
		String strId = String.valueOf(id.getKey());
		//		String strName = selectedReservation.getName(getLocale());
		//		String strKurs = "";
		//		String studiengang = "";
		//		for (int i = 0; i < selectedReservation.getResources().length; i++)
		//		{
		//			if (selectedReservation.getResources()[i].getClassification().getType().getElementKey().equals("kurs"))
		//			{
		//				if (strKurs=="")
		//				{
		//					strKurs = selectedReservation.getResources()[i].getClassification().getValue("name").toString(); 
		//
		//				}
		//				else
		//				{
		//					strKurs = strKurs + "," + selectedReservation.getResources()[i].getClassification().getValue("name").toString();
		//				}
		//				//studiengang = selectedReservation.getResources()[i].getClassification().getValue("abteilung").toString();
		//			}
		//		}

		//		String strDozent = selectedReservation.getPersons()[0].getClassification().getValue("surname").toString();
		//		strDozent = strDozent + "," + selectedReservation.getPersons()[0].getClassification().getValue("firstname").toString();
		//		String strBegin = selectedReservation.getFirstDate().toString();
		//		String strEnd = selectedReservation.getMaxEnd().toString();

		String result;
		//		result = "http://localhost:8051/rapla?page=scheduler-constraints&id=" + strId
		//				+ "&name=" + strName + "&kurs=" + strKurs + "&dozent=" + strDozent
		//				+ "&begin=" + strBegin + "&end=" + strEnd;
		//		result = "http://localhost:8051/rapla?page=scheduler-constraints&id=" + strId
		//		+ "&dozent=" + String.valueOf(dozentId);

		//Dynamische Generierung "Servername:Port"
		StartupEnvironment env = getService( StartupEnvironment.class );
		//Dynamische Generierung "webpage"

		URL codeBase;
		UrlEncryption webservice;
		String key;
		try {
			codeBase = env.getDownloadURL();
			result = codeBase + "rapla?page=scheduler-constraints&id=" + strId + "&dozent=" + String.valueOf(dozentId);
			webservice = getWebservice(UrlEncryption.class);
			String encryptedParamters = webservice.encrypt("page=scheduler-constraints&id=" + strId + "&dozent=" + String.valueOf(dozentId));
			key = UrlEncryption.ENCRYPTED_PARAMETER_NAME+"="+encryptedParamters;
			return new URL( codeBase,"rapla?" + key).toExternalForm();
		} catch (RaplaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error";
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error";
		}
	}


	public void setDesignStatus(Reservation editableEvent, String zielStatus) throws RaplaException{
		String istStatus = (String) editableEvent.getClassification().getValue("planungsstatus");
		if (istStatus != zielStatus) {
			editableEvent.getClassification().setValue("planungsstatus", zielStatus);
		}
		getClientFacade().store( editableEvent ); 
	}


}
