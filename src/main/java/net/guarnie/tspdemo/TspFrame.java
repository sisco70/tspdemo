package net.guarnie.tspdemo;

/**
 *
 *===========================================
 *
 *  "Animazione del T.S.P. Euclideo in Java"
 *
 *  @version 3.0
 *  @author Francesco Guarnieri
 *
 *===========================================
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import static net.guarnie.tspdemo.Heuristic.CITIES_VECTOR_CAPACITY;

/**
 * Frame principale del progetto.
 */
public class TspFrame extends JFrame implements ActionListener, ItemListener,
		MenuListener, MouseListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3418025341471280659L;

	/**
	 * Sono abilitati gli identificatori dei nodi?
	 */
	final static boolean CITY_ID_FLAG = false;

	/**
	 * E' abilitata la visualizzazione delle coordinate?
	 */
	final static boolean COORD_FLAG = false;

	/**
	 * La dimensione del percorso è arrotondata?
	 */
	final static boolean ROUNDED_TOUR_LEN = true;

	/**
	 * Vengono mantenute le medesime proporzioni iniziali?
	 */
	final static boolean ASPECT_RATIO_FLAG = false;

	/**
	 * Dimensione di default in orizzontale e verticale del frame principale.
	 */
	final static int FRAME_SIZE_X = 800;
	final static int FRAME_SIZE_Y = 800;

	/**
	 * Nome del file indice delle istanze predefinite.
	 */
	final static String INSTANCES_INDEX = "index.zip";

	/**
	 * Area di lavoro.
	 */
	public WorkArea workArea;

	/**
	 * Area per lo status.
	 */
	public StatusArea statusArea;

	/**
	 * Area specializzata per selezionare le euristiche per il Tsp.
	 */
	public TspArea tspArea;

	/**
	 * Area di lavoro "Scorrevole".
	 */
	public JScrollPane scrollWorkArea;

	/**
	 * Viewport sull'area di lavoro.
	 */
	public JViewport viewportArea;

	/**
	 * Commenti dell'istanza e del tour.
	 */
	private String instComm = "";
	private String tourComm = "";

	/**
	 * Nomi dell'istanza e del tour.
	 */
	private String instName = "";
	private String tourName = "";

	/**
	 * Pathname dell'istanza e del tour.
	 */
	private String instPathname = "";
	private String tourPathname = "";

	/**
	 * Se l'istanza è stata modificata.
	 */
	private boolean instChg;

	/**
	 * Se il tour è stato modificato.
	 */
	private boolean tourChg;

	/**
	 * Pannello trasparente.
	 */
	private JPanel glass_pane;

	/**
	 * Dialog-box per le operazioni di I/O.
	 */
	private JDialog ioDialog;

	/**
	 * FileDialog.
	 */
	private FileDialog fileDialog;

	/**
	 * Dialog-box per la scelta delle istanze e dei tour predefiniti.
	 */
	private TSPLIbDialog tsplibDialog;

	/**
	 * Dialog-box per la generazione casuale delle istanze.
	 */
	private RandomDialog randomDialog;
	/**
	 * Thread di I/O.
	 */
	public IOThread ioThread;

	/**
	 * Contiene il percorso dove trovare le istanze
	 */
	private String instancesPath;

	/**
	 * Elementi del menu'.
	 */
	private JMenuItem quit, loadRemote, about, random, undo, clrInstance, clrTour;
	private JMenuItem normalView, table, loadLocal, saveLocalInst;
	private JMenuItem saveLocalTour, saveAsLocalInst, saveAsLocalTour;
	private JMenu fileMenu, editMenu, saveLocalMenu, saveAsLocalMenu;
	private JCheckBoxMenuItem cityIDCheckB, coordCheckB, aspRatioCheckB, roundedlenCheckB;

	/**
	 * Logo dell'applicazione.
	 */
	private static ImageIcon tspIcon;

	/**
	 * Cursore di default e di attesa.
	 */
	private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
	private static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

	/*
	 * Costruttore della classe.
	 */
	TspFrame(String title) {
		Container contentPane = getContentPane();
		
		instancesPath = "/instances"; 

		//
		// Carica il logo dell'applicazione.
		//
		tspIcon = loadImageIcon("/images/tsplogo.gif");

		//
		// - MENU BAR -
		//
		JMenuBar princMenu = new JMenuBar();

		//
		// Menu File
		//
		fileMenu = new JMenu("File");
		loadRemote = new JMenuItem("TSPLIB95..");
		loadRemote.setActionCommand("tsplib95");
		loadRemote.addActionListener(this);
		fileMenu.add(loadRemote);
		fileMenu.addSeparator();
		loadLocal = new JMenuItem("Load instance/tour..");
		loadLocal.setActionCommand("openlocal");
		loadLocal.addActionListener(this);
		fileMenu.add(loadLocal);
		saveLocalMenu = new JMenu("Save");
		saveLocalInst = new JMenuItem("current instance");
		saveLocalInst.setActionCommand("savelocalinst");
		saveLocalInst.addActionListener(this);
		saveLocalMenu.add(saveLocalInst);
		saveLocalTour = new JMenuItem("current tour");
		saveLocalTour.setActionCommand("savelocaltour");
		saveLocalTour.addActionListener(this);
		saveLocalMenu.add(saveLocalTour);
		fileMenu.add(saveLocalMenu);
		saveAsLocalMenu = new JMenu("Save as");
		saveAsLocalInst = new JMenuItem("current instance..");
		saveAsLocalInst.setActionCommand("saveaslocalinst");
		saveAsLocalInst.addActionListener(this);
		saveAsLocalMenu.add(saveAsLocalInst);
		saveAsLocalTour = new JMenuItem("current tour..");
		saveAsLocalTour.setActionCommand("saveaslocaltour");
		saveAsLocalTour.addActionListener(this);
		saveAsLocalMenu.add(saveAsLocalTour);
		fileMenu.add(saveAsLocalMenu);
		fileMenu.addSeparator();
		random = new JMenuItem("Random generation..");
		random.setActionCommand("random");
		random.addActionListener(this);
		fileMenu.add(random);
		fileMenu.addSeparator();
		quit = new JMenuItem("Exit");
		quit.setActionCommand("quit");
		quit.addActionListener(this);
		fileMenu.add(quit);
		princMenu.add(fileMenu);
		fileMenu.addMenuListener(this);

		//
		// Menu Edit
		//
		editMenu = new JMenu("Edit");
		editMenu.addMenuListener(this);
		undo = new JMenuItem("Undo");
		undo.setActionCommand("undo");
		undo.addActionListener(this);
		editMenu.add(undo);
		editMenu.addSeparator();
		clrInstance = new JMenuItem("Clear instance");
		clrInstance.setActionCommand("clearinst");
		clrInstance.addActionListener(this);
		editMenu.add(clrInstance);
		clrTour = new JMenuItem("Clear tour");
		clrTour.setActionCommand("cleartour");
		clrTour.addActionListener(this);
		editMenu.add(clrTour);
		editMenu.addSeparator();
		table = new JMenuItem("Instance info...");
		table.setActionCommand("table");
		table.addActionListener(this);
		editMenu.add(table);
		princMenu.add(editMenu);

		//
		// Menu View
		//
		JMenu viewMenu = new JMenu("View");
		cityIDCheckB = new JCheckBoxMenuItem("City ID");
		cityIDCheckB.setState(CITY_ID_FLAG);
		cityIDCheckB.addItemListener(this);
		viewMenu.add(cityIDCheckB);
		coordCheckB = new JCheckBoxMenuItem("Coordinates");
		coordCheckB.setState(COORD_FLAG);
		coordCheckB.addItemListener(this);
		viewMenu.add(coordCheckB);
		viewMenu.addSeparator();
		roundedlenCheckB = new JCheckBoxMenuItem("Rounded lengths");
		roundedlenCheckB.setState(ROUNDED_TOUR_LEN);
		roundedlenCheckB.addItemListener(this);
		viewMenu.add(roundedlenCheckB);
		viewMenu.addSeparator();
		normalView = new JMenuItem("Revert zoom");
		normalView.setActionCommand("resetzoom");
		normalView.addActionListener(this);
		viewMenu.add(normalView);
		aspRatioCheckB = new JCheckBoxMenuItem("Fixed aspect ratio");
		aspRatioCheckB.setState(ASPECT_RATIO_FLAG);
		aspRatioCheckB.addItemListener(this);
		viewMenu.add(aspRatioCheckB);
		princMenu.add(viewMenu);

		//
		// Menu Help
		//
		JMenu helpMenu = new JMenu("Help");
		about = new JMenuItem("About...");
		about.setActionCommand("about");
		about.addActionListener(this);
		helpMenu.add(about);
		princMenu.add(helpMenu);
		setJMenuBar(princMenu);
		contentPane.setLayout(new BorderLayout());

		//
		// Tsp Area
		//
		tspArea = new TspArea(this);
		JPanel tArea = new JPanel();
		tArea.setLayout(new BorderLayout());
		tArea.add(tspArea, BorderLayout.NORTH);
		tArea.add(new JPanel(), BorderLayout.CENTER);
		contentPane.add(tArea, BorderLayout.EAST);

		//
		// Work Area, scroll Work Area, viewport Area.
		//
		scrollWorkArea = new JScrollPane();
		scrollWorkArea.setViewportBorder(BorderFactory.createLoweredBevelBorder());
		viewportArea = new JViewport();
		scrollWorkArea.setViewport(viewportArea);

		//
		// E' importante che viewportArea sia creato prima
		// di workarea.
		//
		workArea = new WorkArea(this);
		workArea.setToolTipText("");
		viewportArea.setView(workArea);
		contentPane.add(scrollWorkArea, BorderLayout.CENTER);

		//
		// Status Area
		//
		statusArea = new StatusArea();
		contentPane.add(statusArea, BorderLayout.SOUTH);

		//
		// Glass Pane
		//
		glass_pane = new JPanel();
		glass_pane.addMouseListener(this);
		glass_pane.setOpaque(false);
		setGlassPane(glass_pane);

		// Centra nello schermo
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(title);
		setSize(FRAME_SIZE_X, FRAME_SIZE_Y);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Gestisce la chiusura del frame principale (tramite pressione del pulsante
	 * di chiusura della finestra).
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			clearWorkArea(true);
			setVisible(false);
			dispose();
		} else
			super.processWindowEvent(e);
	}

	/**
	 * Restituisce l'immagine memorizzata con il nome passato per parametro
	 * 
	 */
	public ImageIcon loadImageIcon(String name) {
		URL url = getClass().getResource(name);
		return new ImageIcon(url);
	}

	/**
	 * Abilita o meno la possibilità di modificare l'istanza (o il tour)
	 * corrente.
	 */
	public void enableEditing(boolean b) {
		workArea.enableEdit(b);
		tspArea.treeComboBox.setEnabled(b);
		tspArea.buttonRun.setEnabled(b);
		tspArea.buttonOpt.setEnabled(b);
		fileMenu.setEnabled(b);
		editMenu.setEnabled(b);
	}

	/**
	 * Effettua il vero e proprio undo.
	 */
	public final void undo() {
		if (!UndoTSP.isEmpty()) {
			Heuristic undoThread = null;
			UndoTSP undoTsp = UndoTSP.pop();
			tourChg = undoTsp.oldIsTourChg;
			if (undoTsp.undoType == Heuristic.NEW_TOUR) {
				tourComm = undoTsp.oldTourComm;
				tourName = undoTsp.oldTourName;
				tourPathname = undoTsp.oldTourPathname;
				Heuristic.setTour(undoTsp.oldTourSize, undoTsp.undoTour);
			} else {
				instChg = undoTsp.oldIsInstChg;
				//tspArea.setLowerBound(undoTsp.lowerBound);
				Heuristic.setOptTourSize(undoTsp.optTourSize);
				switch (undoTsp.undoType) {
					case Heuristic.MOVCITY:
						undoThread = new Heuristic(this, undoTsp.undoType, undoTsp.x, undoTsp.y, undoTsp.ind, null);
						break;
					case Heuristic.DELCITY:
						undoThread = new Heuristic(this, undoTsp.undoType, -1, -1, undoTsp.ind, null);
						break;
					case Heuristic.ADDCITY:
						// In questo caso e' corretto riutilizzare una città,
						// perche' essendo stata cancellata dal vettore delle
						// città il suo contenuto e' preservato da eventuali
						// modifiche.
						undoThread = new Heuristic(this, undoTsp.undoType, -1, -1, -1, undoTsp.city);
				}
				undoThread.setUndoing(true);
				undoThread.start();
			}
		}
	}

	/**
	 * Mostra il titolo del frame.
	 */
	private void showTitle() {
		String st = "";
		if (instName != null && instName.length() > 0)
			st += instName + ((instChg) ? "* " : " ");
		if (tourName != null && tourName.length() > 0)
			st += "(" + tourName + ((tourChg) ? "*" : "") + ")";
		setTitle(st);
	}

	/**
	 * Modifica il nome dell'istanza.
	 */
	public void setInstName(String st, String pn) {
		instName = st;
		instPathname = pn;
		showTitle();
	}

	/**
	 * Modifica il nome del tour.
	 */
	public void setTourName(String st, String pn) {
		tourName = st;
		tourPathname = pn;
		showTitle();
	}

	/**
	 * Restituisce il pathname del tour
	 * 
	 * @return
	*/
	public String getTourPathname()
	{
		return tourPathname;
	}

	/**
	 * Se il tour è stata cambiato
	 * 
	 * @return
	*/
	public boolean isTourChg()
	{
		return tourChg;
	}

	/**
	 * Se l'istannza è stata cambiata
	 * 
	 * @return
	*/
	public boolean isInstChg()
	{
		return instChg;
	}

	/**
	 * Indica che e' stato modificato il tour.
	 */
	public void chgTour(boolean b) {
		if (tourName != null && tourName.length() > 0)
			tourChg = b;
		else
			tourChg = false;
		showTitle();
	}

	/**
	 * Indica che e' stata modificata l'istanza.
	 */
	public void chgInstance(boolean b) {
		if (instName != null && instName.length() > 0)
			instChg = b;
		else
			instChg = false;
		showTitle();
	}

	/**
	 * Modifica il commento dell'istanza.
	 */
	public void setInstComm(String st) {
		instComm = st;
	}

	/**
	 * Modifica il commento del tour.
	 */
	public void setTourComm(String st) {
		tourComm = st;
	}

	/**
	 * Restituisce il commento dell'istanza.
	 */
	public String getInstComm() {
		return instComm;
	}

	/**
	 * Restituisce il commento del tour.
	 */
	public String getTourComm() {
		return tourComm;
	}

	/**
	 * Restituisce il nome dell'istanza.
	 */
	public String getInstName() {
		return instName;
	}

	/**
	 * Restituisce il nome del tour.
	 */
	public String getTourName() {
		return tourName;
	}

	/**
	 * Restituisce il valore della checkboxmenuitem CityID.
	 */
	public boolean getCityID() {
		return cityIDCheckB.getState();
	}

	/**
	 * Crea per la prima volta la dialog-box per la scelta delle istanze/tour
	 * predefiniti.
	 */
	public void createRemoteDialog(Object instNames[][]) {
		tsplibDialog = new TSPLIbDialog(this, "Instances and tours from TSPLIB95", true, instNames);
		tsplibDialog.setVisible(true);
	}

	/**
	 * "Blocca" il frame e imposta il cursore di wait.
	 */
	public void setBusy(boolean busy) {
		if (busy) {
			// Si deve impostare il cursore del frame E POI (l'ordine conta!) il
			// cursore del glass pane.
			this.setCursor(WAIT_CURSOR);
			glass_pane.setVisible(true);
			glass_pane.setCursor(WAIT_CURSOR);
		} else {
			glass_pane.setCursor(DEFAULT_CURSOR);
			glass_pane.setVisible(false);
			this.setCursor(DEFAULT_CURSOR);
		}
	}

	/**
	 * Necessari per implementare l'interfaccia MouseListener. (Non deve fare
	 * niente)
	 */
	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Necessari per implementare l'interfaccia MenuListener.
	 */
	@Override
	public void menuSelected(MenuEvent e) {
		if (e.getSource().equals(fileMenu)) {
			boolean tour = (Heuristic.getTotTourNodes() != 0);
			boolean cities = (Heuristic.getTotCities() != 0);
			boolean ipname = (instPathname != null && instPathname.length() > 0);
			boolean tpname = (tourPathname != null && tourPathname.length() > 0);
			saveAsLocalMenu.setEnabled(tour || cities);
			saveAsLocalInst.setEnabled(cities);
			saveAsLocalTour.setEnabled(tour);
			saveLocalMenu.setEnabled((ipname || tpname) && (tour || cities)	&& (instChg || tourChg));
			saveLocalInst.setEnabled(ipname && cities && instChg);
			saveLocalTour.setEnabled(tpname && tour && tourChg);
		} else {
			if (UndoTSP.isEmpty()) {
				undo.setText("Undo");
				undo.setEnabled(false);
			} else {
				UndoTSP undoTsp = UndoTSP.peek();
				undo.setText("Undo \"" + undoTsp.descr + "\"");
				undo.setEnabled(true);
			}
		}
	}

	public void menuDeselected(MenuEvent e) {
	}

	public void menuCanceled(MenuEvent e) {
	}

	/**
	 * Cancella la dialog box per le operazioni di I/O.
	 */
	public void closeIODialog() {
		ioDialog.dispose();
	}

	/**
	 * Cancella l'area di lavoro, in base al parametro, se è true cancella
	 * tutto, se è false cancella solo il circuito.
	 */
	private boolean clearWorkArea(boolean clearInstance) {
		if ((!clearInstance && (Heuristic.getTotTourNodes() > 0))
				|| (clearInstance && (Heuristic.getTotCities() > 0))) {
			if (clearInstance) {
				Object[] options = { "Yes", "No" };
				if (JOptionPane.showOptionDialog(this,
						"You are attempting to delete this instance definitively.\n"
								+ "Are you really sure?", "Warning!",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[1]) == JOptionPane.NO_OPTION)
					return false;
				UndoTSP.clear();
				Heuristic.clearCities();
				instPathname = "";
				instComm = "";
				instName = "";
				instChg = false;
				tspArea.setTotCities();
				tspArea.setLowerBound();
				tspArea.setVisibleOpt();
				workArea.resetZoom();
				workArea.resetMinMaxXY();
			} else {
				UndoTSP.push(this, "Clear tour", Heuristic.NEW_TOUR);
				Heuristic.clearTour();
			}
			tspArea.setTourLength();
			tourPathname = "";
			tourComm = "";
			tourName = "";
			tourChg = false;
			showTitle();
			tspArea.resetTreeComboBox();
			workArea.repaint(true, true);
		}
		return true;
	}

	/**
	 * Inizia una sessione di I/O.
	 */
	public void ioSession(int ioType, String pathFileName, String fileName, Object param) {
		String str = "";
		switch (ioType) {
			case IOThread.LOAD_TSPLIB_DIR:
				pathFileName = instancesPath + "/" + INSTANCES_INDEX;
				str = "Connecting to " + pathFileName;
				break;
			case IOThread.LOAD_LOCAL:
			case IOThread.LOAD_TSPLIB_INST:
				if (ioType == IOThread.LOAD_TSPLIB_INST)
					pathFileName = instancesPath + "/";
				str = "Loading instance file " + pathFileName + fileName;
				break;
			case IOThread.SAVE_LOCAL_TOUR:
				str = "Saving current tour on file " + fileName;
				break;
			case IOThread.SAVE_LOCAL_INST:
				str = "Saving current instance on file " + fileName;
		}
		ioThread = new IOThread(this, ioType, pathFileName, fileName, param);
		Object[] options = { "Cancel" };
		final JOptionPane optionPane = new JOptionPane(str,	JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null, options, options[0]);
		ioDialog = optionPane.createDialog(this, "Wait please...");
		ioThread.start();
		ioDialog.setVisible(true);
		if ((optionPane.getValue() == null) || optionPane.getValue().equals(options[0])) {
			// Se il Thread di I/O e' in esecuzione viene fermato e la
			// message-box in ogni caso viene chiusa.
			if ((ioThread != null) && ioThread.isAlive()) {
				ioThread.interrupt();
				try {
					ioThread.join();
				} catch (InterruptedException ie) {
				}
				ioThread = null;
			}
		}
	}

	/**
	 * Gestisce la selezione dei seguenti elementi dei menu View e Edit: - City
	 * ID - Coordinates - Fixed Aspect Ratio - Rounded Tour Lenght
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == cityIDCheckB)
			workArea.repaint(true, false);
		else if (e.getSource() == coordCheckB)
			workArea.enableCoord(coordCheckB.isSelected());
		else if (e.getSource() == aspRatioCheckB)
			workArea.setAspRatio(aspRatioCheckB.isSelected());
		else if (e.getSource() == roundedlenCheckB)
			tspArea.setRoundedTLen(roundedlenCheckB.isSelected());
	}

	/**
	 * Gestisce la selezione dei seguenti elementi del menu File: - Remote open
	 * - Local open - Local save instance/tour - Local save as instance/tour -
	 * Random generation - Exit
	 * 
	 * Gestisce la selezione dei seguenti elementi del menu Edit: - Undo - Clear
	 * instance - Clear tour - Instance info
	 * 
	 * Gestisce la selezione dei seguenti elementi del menu View: - Normal view
	 * 
	 * Gestisce la selezione dei seguenti elementi del menu Help: - About
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		String fileName, pathName;
		//
		// Effettua la lettura di una istanza (o circuito) remota.
		//
		switch (ac) {
            case "tsplib95":
				if (tsplibDialog == null)
					ioSession(IOThread.LOAD_TSPLIB_DIR, "", "", this);
				else {
					tsplibDialog.pack();
					tsplibDialog.setLocationRelativeTo(this);
					tsplibDialog.setVisible(true);
				}
				break;
			//
			// Effettua la lettura di una istanza (o circuito) locale.
			//
			case "openlocal":
				if (fileDialog == null)
					fileDialog = new FileDialog(this);
				fileDialog.setTitle("Open instance/tour from local disk");
				fileDialog.setMode(FileDialog.LOAD);
				fileDialog.setVisible(true);
				fileName = fileDialog.getFile();
				pathName = fileDialog.getDirectory();
				fileDialog.dispose();
				if ((fileName != null) && (fileName.length() > 0))
					ioSession(IOThread.LOAD_LOCAL, pathName, fileName, null);
				break;
			//
			// Effettua i salvataggi locali (istanze e circuiti).
			//
			case "saveaslocalinst": 
			case "saveaslocaltour":
			case "savelocalinst": 
			case "savelocaltour":
				int ioType;		
				String str, comment;
				if (ac.equals("savelocalinst") || ac.equals("saveaslocalinst")) {
					ioType = IOThread.SAVE_LOCAL_INST;
					str = "instance";
					comment = instComm;
				} else {
					ioType = IOThread.SAVE_LOCAL_TOUR;
					str = "tour";
					comment = tourComm;
				}
				if (ac.equals("saveaslocalinst") || ac.equals("saveaslocaltour")) {
					if (fileDialog == null)
						fileDialog = new FileDialog(this);
					fileDialog.setTitle("Save as current " + str + " on local disk");
					fileDialog.setFile("");
					fileDialog.setMode(FileDialog.SAVE);
					fileDialog.setVisible(true);
					fileName = fileDialog.getFile();
					pathName = fileDialog.getDirectory();
					fileDialog.dispose();
				} else {
					fileName = (ioType == IOThread.SAVE_LOCAL_INST) ? instName : tourName;
					pathName = (ioType == IOThread.SAVE_LOCAL_INST) ? instPathname : tourPathname;
				}
				if ((fileName != null) && (fileName.length() > 0))
					ioSession(ioType, pathName, fileName, comment);
				break;
			//
			// Cancella il circuito corrente o l'istanza corrente.
			//
			case "cleartour": 
			case "clearinst":
				clearWorkArea(ac.equals("clearinst"));
				break;
			//
			// Effettua l'undo.
			//
			case "undo":
				undo();
				showTitle();
				workArea.resetZoom();
				tspArea.setTourLength();
				tspArea.setVisibleOpt();
				workArea.repaint(true, true);
				break;
			//
			// Mostra la Dialog-box per la generazione di una serie di città
			// casuali.
			//
			case "random":
				if (randomDialog == null)
					randomDialog = new RandomDialog(this, "Generate random instance", true);
				randomDialog.pack();
				randomDialog.setLocationRelativeTo(this);
				randomDialog.setVisible(true);
				break;
			//
			// Mostra la Dialog-box con una tabella per l'inserimento manuale delle
			// città.
			//
			case "table":
				InstanceDialog tableDialog = new InstanceDialog(this, "Instance info", true);
				tableDialog.pack();
				tableDialog.setLocationRelativeTo(this);
				tableDialog.setVisible(true);
				break;
			//
			// Ripristina la visualizzazione normale (revert zoom).
			//
			case "resetzoom":
				workArea.resetZoom();
				break;
			//
			// Dialog box con le generalita' dell'autore.
			//
			case "about":
				{
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					JLabel label1 = new JLabel("Animazione del T.S.P. in Java", JLabel.LEFT);
					label1.setFont(label1.getFont().deriveFont(20.0f));
					JLabel label2 = new JLabel("Francesco Guarnieri 1998/99", JLabel.LEFT);
					label2.setFont(label2.getFont().deriveFont(16.0f));
					JLabel label3 = new JLabel("2020 - Aggiornata a JDK 8", JLabel.LEFT);
					label3.setFont(label3.getFont().deriveFont(12.0f));
					panel.add(label1);
					panel.add(label2);
					panel.add(new JLabel(" "));
					panel.add(label3);
					JOptionPane.showMessageDialog(this, panel, "About", JOptionPane.INFORMATION_MESSAGE, tspIcon);
				}
				break;
			//
			// Chiude il frame principale.
			//
			case "quit":
				if (clearWorkArea(true)) {
					dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
				}
				break;
		}
	}
}

/**
 * Implementa l'area di lavoro, la sua visualizzazione e la gestione dei suoi
 * eventi.
 */
class WorkArea extends JComponent implements ComponentListener,	ActionListener, MouseListener, MouseMotionListener, MouseWheelListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 1994229209745317615L;

	/**
	 * Dimensione del lato del quadrato rappresentante la citta'.
	 */
	final static int CITY_SIZE = 2;

	/**
	 * Distanza X e Y dell'identificatore dall'angolo superiore sinistro del
	 * quadrato rappresentante la citta'.
	 */
	final static int CITY_ID_X = CITY_SIZE + 1;
	final static int CITY_ID_Y = 0;

	/**
	 * Dimensione del "raggio" dell'area quadrata all'interno del quale si cerca
	 * la citta'.
	 */
	final static int RADIUS_CITY = 2;

	/**
	 * Dimensione del "diametro" dell'area quadrata all'interno del quale si
	 * cerca citta'.
	 */
	final static int DIAMETER_CITY = (RADIUS_CITY << 1) + CITY_SIZE;

	/**
	 * Percentuale di ingrandimento e di riduzione per ogni operazione di Zoom
	 * in e Zoom out.
	 */
	final static double ZOOM_RATIO = 0.5;

	/**
	 * Colore archi extra (usati per la MST Visit).
	 */
	final static Color EXTRA_MST_COLOR = Color.LIGHT_GRAY;

	/**
	 * Colore archi extra (usati per 2-opt, 3-Opt, etc...).
	 */
	final static Color EXTRA_OPT_COLOR = Color.YELLOW;

	/**
	 * Colore archi.
	 */
	final static Color EDGE_COLOR = Color.MAGENTA;

	/**
	 * Colore nodi.
	 */
	final static Color CITY_COLOR = Color.BLACK;

	/**
	 * Colore identificatori.
	 */
	final static Color CITY_ID_COLOR = Color.GRAY;

	/**
	 * Dimensione del font dell'identificatore della citta'.
	 */
	final static int CITY_ID_FONT_SIZE = 9;

	/**
	 * Nome del font dell'identificatore della citta'.
	 */
	final static String CITY_ID_FONT_TYPE = "Helvetica";


	/**
	 * Costanti che contengono alcuni dei cursori predefiniti utilizzati dai
	 * metodi di questa classe.
	 */
	private static final Cursor SELECT_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	private static final Cursor CROSS_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	private static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();
	private Cursor workAreaCursor = DEFAULT_CURSOR;

	/** 
	 * Costante della riga usata per gli archi extra
	*/
	private final static Stroke EXTRA_OPT_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f, new float[] {5.0f, 5.0f}, 0.0f);


	/**
	 * Tabella Hash che contiene per ogni indice di città
	 */
	private Hashtable<Point, City> pointHashTable = new Hashtable<Point, City>(CITIES_VECTOR_CAPACITY);

	/**
	 * Array delle etichette delle città dell'istanza corrente.
	 */
	private String[] idArray = new String[CITIES_VECTOR_CAPACITY];

	/**
	 * Array dei punti rappresentanti le città dell'istanza corrente.
	 */
	private Point[] pointArray = new Point[CITIES_VECTOR_CAPACITY];

	/**
	 * Array dei punti rappresentanti rispettivamente la coda e la testa degli
	 * archi del circuito corrente.
	 */
	private Point[] edgeArray1 = new Point[CITIES_VECTOR_CAPACITY];
	private Point[] edgeArray2 = new Point[CITIES_VECTOR_CAPACITY];

	/**
	 * Array dei punti rappresentanti rispettivamente la coda e la testa degli
	 * archi "extra" (possono essere quelli del MST, del circuito ottimo o del
	 * 2/3 Opt).
	 */
	private Point[] extraArray1 = new Point[CITIES_VECTOR_CAPACITY];
	private Point[] extraArray2 = new Point[CITIES_VECTOR_CAPACITY];

	public Heuristic editThread;
	private TspFrame tspFrame;
	private double minX, minY, parX, parY;
	private int rightBorder, bottomBorder, hsize, wsize, totC, totE, totEX;
	private int new_CITY_ID_Y, new_bottomBorder, curWidth, curHeight;
	private int maxAdv = -1;
	private int movingCityID = -1;
	private int selectedCity = -1;
	private Point optMinX, optMaxX, optMinY, optMaxY;
	private Point clickPoint, lastPoint;
	private boolean resetMinMax = true;
	private boolean isWorkArea = true;
	private boolean isZooming = false;
	private boolean editEnabled = true;
	private boolean aspRatio = TspFrame.ASPECT_RATIO_FLAG;
	private boolean coordEnabled = TspFrame.COORD_FLAG;
	private boolean optEdgesEnabled = false;
	private Font f;
	private NumberFormat coordFormat;
	private JViewport vp;
	public JPopupMenu popup;
	private JMenuItem moveCity, delCity, insertCity, startCity, zoomIn, zoomOut;

	/**
	 * Costruttore della classe.
	 */
	public WorkArea(TspFrame tFrame) {
		tspFrame = tFrame;
		f = new Font(CITY_ID_FONT_TYPE, Font.PLAIN, CITY_ID_FONT_SIZE);
		FontMetrics fm = getFontMetrics(f);
		coordFormat = NumberFormat.getNumberInstance();
		coordFormat.setMaximumFractionDigits(2);
		coordFormat.setGroupingUsed(false);

		//
		// Calcola la massima larghezza di una cifra.
		//
		for (int count = '0'; count <= '9'; count++)
			if (maxAdv < fm.charWidth(count))
				maxAdv = fm.charWidth(count);

		new_CITY_ID_Y = CITY_ID_Y + fm.getAscent();
		new_bottomBorder = new_CITY_ID_Y + fm.getDescent();
		for (int count = 0; count < CITIES_VECTOR_CAPACITY; count++)
			pointArray[count] = new Point();
		addMouseListener(this);
		addMouseMotionListener(this);
		vp = tspFrame.viewportArea;
		vp.addComponentListener(this);

		//
		// Menu Pop-Up
		//
		popup = new JPopupMenu();
		zoomIn = new JMenuItem("Zoom In");
		zoomIn.setActionCommand("zoomin");
		zoomIn.addActionListener(this);
		popup.add(zoomIn);
		zoomOut = new JMenuItem("Zoom Out");
		zoomOut.setActionCommand("zoomout");
		zoomOut.addActionListener(this);
		popup.add(zoomOut);
		popup.addSeparator();
		insertCity = new JMenuItem("Insert city");
		insertCity.setActionCommand("insert");
		insertCity.addActionListener(this);
		popup.add(insertCity);
		moveCity = new JMenuItem("Move this city");
		moveCity.setActionCommand("move");
		moveCity.addActionListener(this);
		popup.add(moveCity);
		delCity = new JMenuItem("Delete this city");
		delCity.setActionCommand("delete");
		delCity.addActionListener(this);
		popup.add(delCity);
		popup.addSeparator();
		startCity = new JMenuItem("Select as start city");
		startCity.setActionCommand("select");
		startCity.addActionListener(this);
		popup.add(startCity);

		// Gestione mouse wheel
		addMouseWheelListener(this);
	}

	/**
	 *   Gestisce gli eventi generati dalla rotella del mouse,
	 *   solo se il tasto CTRL è premuto.
	 *  @param e
 	*/
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if (e.isControlDown())
        {
			// Memorizza il punto dove è avvenuta l'operazione "wheel up" o "wheel down"
			clickPoint = e.getPoint();

			// Zoom Out/In (negative wheel up, positive wheel down)
			zoom(e.getWheelRotation() < 0);
        }
		else
			getParent().dispatchEvent(e);

    }

	/**
	 * Gestisce l'evento "resize" della viewport.
	 */
	@Override
	public final void componentResized(ComponentEvent e) {
		computeNodes();
		if (isZooming && (getSize().equals(vp.getSize()))) {
			setPreferredSize(null);
			isZooming = false;
		}
		repaint();
	}

	/**
	 * Necessari per implementare la ComponentListener.
	 */
	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	/**
	 * Disegna tutto il componente WorkArea.
	 */
	@Override
	public final synchronized void paintComponent(Graphics gr) {
		Graphics2D g = (Graphics2D) gr;

		//
		// Passa al disegno vero e proprio (se ci sono città).
		//
		if (totC > 0) {

			//
			// Disegna i limiti verticali oppure orizzontali nel caso
			// che "Same Aspect Ratio" sia true.
			//
			if (aspRatio) {
				g.setColor(Color.gray);
				g.drawLine(0, curHeight, curWidth, curHeight);
				g.drawLine(curWidth, 0, curWidth, curHeight);
				g.setColor(Color.black);
				g.drawLine(0, curHeight + 1, curWidth, curHeight + 1);
				g.drawLine(curWidth + 1, 0, curWidth + 1, curHeight);
				g.setColor(Color.white);
				g.fillRect(curWidth + 2, 0, getWidth() - curWidth - 2,
						getHeight());
				g.fillRect(0, curHeight + 2, getWidth(), getHeight()
						- curHeight);
			}
			int count;
			Point p1, p2;

			//
			// Disegna archi extra per la MST Visit animation
			// (se ve ne sono).
			//
			if ((totEX > 0) && !optEdgesEnabled) {
				g.setColor(EXTRA_MST_COLOR);
				for (count = 0; count < totEX; count++) {
					p1 = extraArray1[count];
					p2 = extraArray2[count];
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}

			//
			// Disegna il circuito (se non e' vuoto).
			//
			if (totE > 0) {
				g.setColor(EDGE_COLOR);
				for (count = 0; count < totE; count++) {
					p1 = edgeArray1[count];
					p2 = edgeArray2[count];
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}

			//
			// Disegna archi extra nel caso della 2-Opt,
			// 3-Opt animation etc.. (se ve ne sono).
			//
			if ((totEX > 0) && optEdgesEnabled) {
				g.setColor(EXTRA_OPT_COLOR); 
				g.setStroke(EXTRA_OPT_STROKE);
				for (count = 0; count < totEX; count++) {
					p1 = extraArray1[count];
					p2 = extraArray2[count];
					g.drawLine(p1.x, p1.y, p2.x, p2.y);
				}
			}

			//
			// Disegna le città.
			//
			g.setColor(CITY_COLOR);
			for (count = 0; count < totC; count++) {
				p1 = pointArray[count];
				g.fillRect(p1.x, p1.y, CITY_SIZE, CITY_SIZE);
			}

			//
			// Disegna gli identificatori delle città (se la loro
			// visualizzazione e' abilitata).
			//
			if (tspFrame.getCityID()) {
				g.setFont(f);
				g.setColor(CITY_ID_COLOR);
				for (count = 0; count < totC; count++) {
					p1 = pointArray[count];
					g.drawString(idArray[count], p1.x + CITY_ID_X, p1.y	+ new_CITY_ID_Y);
				}
			}
		}
	}

	/**
	 * Restituisce il numero di cifre intere di un numero.
	 */
	private final int totDigits(int n) {
		int size = 1;
		while ((n /= 10) > 0)
			size++;
		return size;
	}

	/**
	 * Impone che vengano ricalcolate le coord. min e max in X e Y in base alle
	 * dimensioni correnti della workarea.
	 */
	public final void resetMinMaxXY() {
		resetMinMax = true;
	}

	/**
	 * Ricalcola le coordinate dei nodi.
	 */
	public final synchronized void computeNodes() {
		int count;
		Point p;
		totC = Heuristic.getTotCities();
		if (tspFrame.getCityID()) {
			rightBorder = (maxAdv * totDigits(totC - 1)) + CITY_ID_X;
			bottomBorder = new_bottomBorder;
		} else {
			rightBorder = CITY_SIZE;
			bottomBorder = CITY_SIZE;
		}
		curHeight = getHeight();
		curWidth = getWidth();

		//
		// Imposta le coordinate minime e massime in X e Y
		// in base alle dimensioni correnti della workarea.
		//
		if (resetMinMax) {
			Heuristic.setMinMax(0, getWidth() - rightBorder, 0, getHeight()
					- bottomBorder);
			resetMinMax = false;
		}
		minX = Heuristic.getMinX();
		minY = Heuristic.getMinY();
		if (aspRatio) {
			double ar1 = (Heuristic.getMaxX() - minX)
					/ (Heuristic.getMaxY() - minY);
			double ar2 = (double) curWidth / (double) curHeight;
			if (ar1 > ar2)
				curHeight = (int) Math.round(curWidth / ar1);
			else
				curWidth = (int) Math.round(curHeight * ar1);
		}
		hsize = curHeight - bottomBorder;
		wsize = curWidth - rightBorder;
		parX = wsize / (Heuristic.getMaxX() - minX);
		parY = hsize / (Heuristic.getMaxY() - minY);
		City c;
		pointHashTable.clear();
		for (count = 0; count < totC; count++) {
			c = Heuristic.getCityVector(count);
			p = pointArray[count];
			idArray[count] = String.valueOf(count);
			p.setLocation((int) Math.round((c.x - minX) * parX), hsize
					- (int) Math.round((c.y - minY) * parY));
			pointHashTable.put(p, c);
		}
	}

	/**
	 * Ricalcola le coordinate degli archi.
	 */
	public final synchronized void computeEdges() {
		totE = Heuristic.getTotTourNodes();
		if (totE > 0) {
			int t = totE - 1;
			for (int count = 0; count < t;) {
				edgeArray1[count] = pointArray[Heuristic.getTourVector(count).num];
				edgeArray2[count] = pointArray[Heuristic.getTourVector(++count).num];
			}
			edgeArray1[t] = pointArray[Heuristic.getTourVector(t).num];
			edgeArray2[t] = pointArray[Heuristic.getTourVector(0).num];
		}
	}

	/**
	 * Aggiunge un arco all'array degli archi.
	 */
	public final synchronized int addEdge(City c1, City c2) {
		Point p1 = pointArray[c1.num];
		Point p2 = pointArray[c2.num];
		edgeArray1[totE] = p1;
		edgeArray2[totE++] = p2;
		repaint((int) Math.min(p1.x, p2.x), (int) Math.min(p1.y, p2.y),
				rightBorder + (int) Math.abs(p1.x - p2.x), bottomBorder
						+ (int) Math.abs(p1.y - p2.y));
		return totE - 1;
	}

	/**
	 * Cancella un arco nell'array degli archi.
	 */
	public final synchronized void clearEdge(City c1, City c2) {
		Point p1 = pointArray[c1.num];
		Point p2 = pointArray[c2.num];
		int ind = 0;
		while ((ind < totE)
				&& ((p1 != edgeArray1[ind]) || (p2 != edgeArray2[ind]))
				&& ((p1 != edgeArray2[ind]) || (p2 != edgeArray1[ind])))
			ind++;
		if (ind < totE) {
			System.arraycopy(edgeArray1, ind + 1, edgeArray1, ind, --totE - ind);
			System.arraycopy(edgeArray2, ind + 1, edgeArray2, ind, totE - ind);
			repaint((int) Math.min(p1.x, p2.x), (int) Math.min(p1.y, p2.y),
					rightBorder + (int) Math.abs(p1.x - p2.x), bottomBorder
							+ (int) Math.abs(p1.y - p2.y));
		}
	}

	/**
	 * Abilita o meno gli archi extra, resettando il rettangolo minimo 
	 * che li contiene.
	 */
	public final synchronized void enableExtraEdges(boolean opt) {
		totEX = 0;
		optEdgesEnabled = opt;
		if (optEdgesEnabled) {
			//
			// Inizializza le coordinate del rettangolo
			// piu' piccolo contenente i tre archi.
			// Serve per effettuare un repaint limitato solo
			// alle aree necessarie.
			//
			optMinX = new Point(Integer.MAX_VALUE, 0);
			optMinY = new Point(0, Integer.MAX_VALUE);
			optMaxX = new Point(Integer.MIN_VALUE, 0);
			optMaxY = new Point(0, Integer.MIN_VALUE);
		}
	}

	/**
	 * Ridisegna solo l'area occupata dagli archi extra (2-Opt e 3-Opt
	 * animation).
	 */
	public final void repaintOpt(boolean compEdges) {
		//
		// Aggiorna l'array degli archi.
		//
		if (compEdges)
			computeEdges();

		//
		// Quindi ridisegna solo l'area interessata.
		//
		repaint(optMinX.x, optMinY.y, rightBorder + (optMaxX.x - optMinX.x),
				bottomBorder + (optMaxY.y - optMinY.y));
	}

	/**
	 * Aggiunge due archi all'array degli archi extra (2-Opt animation, construction heuristics).
	 */
	public final synchronized void addOptEdges(City c1, City c2, City c3,
			City c4) {
		enableExtraEdges(true);
		addExtraEdge(c1, c2);
		addExtraEdge(c3, c4);
		repaint(optMinX.x, optMinY.y, rightBorder + (optMaxX.x - optMinX.x),
				bottomBorder + (optMaxY.y - optMinY.y));
	}

	/**
	 * Aggiunge tre archi all'array degli archi extra (3-Opt animation).
	 */
	public final synchronized void addOptEdges(City c1, City c2, City c3,
			City c4, City c5, City c6) {
		enableExtraEdges(true);
		addExtraEdge(c1, c2);
		addExtraEdge(c3, c4);
		addExtraEdge(c5, c6);
		repaint(optMinX.x, optMinY.y, rightBorder + (optMaxX.x - optMinX.x),
				bottomBorder + (optMaxY.y - optMinY.y));
	}

	/**
	 * Aggiunge un arco all'array degli archi extra.
	 */
	public final synchronized void addExtraEdge(City c1, City c2) {
		if (optEdgesEnabled) {
			Point p1 = pointArray[c1.num];
			Point p2 = pointArray[c2.num];
			if (p1.x < optMinX.x)
				optMinX = p1;
			if (p2.x < optMinX.x)
				optMinX = p2;
			if (p1.x > optMaxX.x)
				optMaxX = p1;
			if (p2.x > optMaxX.x)
				optMaxX = p2;
			if (p1.y < optMinY.y)
				optMinY = p1;
			if (p2.y < optMinY.y)
				optMinY = p2;
			if (p1.y > optMaxY.y)
				optMaxY = p1;
			if (p2.y > optMaxY.y)
				optMaxY = p2;
			extraArray1[totEX] = p1;
			extraArray2[totEX++] = p2;
		} else {
			extraArray1[totEX] = pointArray[c1.num];
			extraArray2[totEX++] = pointArray[c2.num];
		}
	}

	/**
	 * Mostra il circuito ottimo (se esiste).
	 */
	public final void showOptimumTour(boolean show) {
		if (show) {
			if (Heuristic.getOptTourSize() > 0) {
				totEX = Heuristic.getTotCities();
				optEdgesEnabled = true;
				int t = totEX - 1;
				for (int count = 0; count < t;) {
					extraArray1[count] = pointArray[Heuristic.getOptVector(count).num];
					extraArray2[count] = pointArray[Heuristic.getOptVector(++count).num];
				}
				extraArray1[t] = pointArray[Heuristic.getOptVector(t).num];
				extraArray2[t] = pointArray[Heuristic.getOptVector(0).num];
			}
		} else
			enableExtraEdges(false);
		repaint();
	}

	/**
	 * Ridisegna tutto aggiornando l'array dei nodi e/o degli archi .
	 */
	public final void repaint(boolean nodes, boolean edges) {
		//
		// Calcola i parametri di visualizzazione, aggiorna
		// l'array dei nodi.
		//
		if (nodes)
			computeNodes();
		//
		// Aggiorna l'array degli archi.
		//
		if (edges)
			computeEdges();
		//
		// Quindi ridisegna la workArea.
		//
		repaint();
	}

	/**
	 * Cambia l'Aspect-ratio.
	 */
	public final void setAspRatio(boolean b) {
		aspRatio = b;
		repaint(true, false);
	}

	/**
	 * Abilita o meno l'editing manuale del componente WorkArea.
	 */
	public final void enableEdit(boolean b) {
		editEnabled = b;
	}

	/**
	 * Cerca in una hashtable il punto passato per parametro.
	 */
	private void checkOverPoint(Point p) {
		if (movingCityID == -1) {
			p.translate(-RADIUS_CITY, -RADIUS_CITY);
			int x = p.x;
			int y = p.y;
			int count1 = 0;
			int count2;
			boolean find = pointHashTable.containsKey(p);
			while ((count1++ < DIAMETER_CITY) && !find) {
				count2 = 0;
				while ((count2 < DIAMETER_CITY) && !find) {
					p.setLocation(x + count1, y + count2++);
					find = pointHashTable.containsKey(p);
				}
			}
			if (find) {
				lastPoint = p;
				setCursor(SELECT_CURSOR);
			} else {
				setCursor(workAreaCursor);
				lastPoint = null;
			}
		}
	}

	/**
	 * Data una coordinata video x la trasforma in base ai parametri di
	 * visualizzazione in una coordinata relativa all'istanza corrente.
	 */
	private double computeX(int x) {
		return minX + (x / parX);
	}

	/**
	 * Data una coordinata video y la trasforma in base ai parametri di
	 * visualizzazione in una coordinata relativa all'istanza corrente.
	 */
	private double computeY(int y) {
		return minY - ((y - hsize) / parY);
	}

	/**
	 * Riporta ad una visualizzazione normale (no zoom).
	 */
	public final void resetZoom() {
		if (isZooming) {
			setPreferredSize(null);
			revalidate();
			isZooming = false;
		}
	}

	/**
	 * Zoom in e zoom out.
	 */
	private final void zoom(boolean in) {
		if (isZooming || in) {
			isZooming = true;
			Dimension d = getSize();
			Point n = vp.getViewPosition();
			if (in) {
				d.height = (int) Math.round(d.height * (1 + ZOOM_RATIO));
				d.width = (int) Math.round(d.width * (1 + ZOOM_RATIO));
				n.translate((int) Math.round(clickPoint.x * ZOOM_RATIO),
						(int) Math.round(clickPoint.y * ZOOM_RATIO));
			} else {
				d.height = (int) Math.round(d.height * (1 / (1 + ZOOM_RATIO)));
				d.width = (int) Math.round(d.width * (1 / (1 + ZOOM_RATIO)));
				n.translate(
						(int) -Math.round(clickPoint.x * (ZOOM_RATIO / (1 + ZOOM_RATIO))),
						(int) -Math.round(clickPoint.y * (ZOOM_RATIO / (1 + ZOOM_RATIO))));
			}
			setSize(d);
			computeNodes();
			vp.setViewPosition(n);
			setPreferredSize(d);
			revalidate();
		}
	}

	/**
	 * Fa partire un thread per il calcolo di un'insieme di città random.
	 */
	public final void startRandomThread(int tot) {
		tspFrame.workArea.resetZoom();
		tspFrame.statusArea.setStatus("Generating random cities...");
		tspFrame.setBusy(true);
		editThread = new Heuristic(tspFrame, Heuristic.RANDOM, wsize, hsize, tot, null);
		editThread.start();
	}

	/**
	 * Gestisce la selezione degli elementi del menu Pop-up:
	 */
	public final void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand()) {
			//
			// Inserimento città
			//
			case "insert":
				UndoTSP.push(tspFrame, "Add city", Heuristic.DELCITY, -1, -1, Heuristic.getTotCities());
				tspFrame.statusArea.setStatus("Adding new city...");
				tspFrame.setBusy(true);
				editThread = new Heuristic(tspFrame, Heuristic.ADDCITY, computeX(clickPoint.x), computeY(clickPoint.y), -1, null);
				editThread.start();
				break;
			//
			// Zoom in
			//
			case "zoomin":
				zoom(true);
				break;
			//
			// Zoom out
			//
			case "zoomout":
				zoom(false);
				break;
			//
			// Spostamento città
			//
			case "move":
				workAreaCursor = CROSS_CURSOR;
				setCursor(workAreaCursor);
				movingCityID = selectedCity;
				break;
			//
			// Cancellazione città
			//
			case "delete":
				UndoTSP.push(tspFrame, "Delete city", Heuristic.ADDCITY, -1, -1, selectedCity);
				tspFrame.statusArea.setStatus("Deleting city...");
				tspFrame.setBusy(true);
				editThread = new Heuristic(tspFrame, Heuristic.DELCITY, -1, -1, selectedCity, null);
				editThread.start();
				break;
			//
			// Selezione città di partenza
			//
			case "select":
				tspFrame.statusArea.setStartCityField(selectedCity);
				break;
		}
	}

	/**
	 * Gestisce lo spostamento del mouse con il pulsante premuto nell'area di
	 * disegno.
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Abilita o meno la visualizzazione delle coordinate.
	 */
	public final void enableCoord(boolean b) {
		coordEnabled = b;
		if (!coordEnabled)
			tspFrame.statusArea.clearCoord();
	}

	/**
	 * Gestisce il movimento del mouse nell'area di disegno, e cambia la forma
	 * del cursore nel caso di passaggio sopra ad una città.
	 */
	public final void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		checkWorkArea(p.x, p.y);
		if (isWorkArea) {
			if (coordEnabled)
				tspFrame.statusArea.setCoord(computeX(p.x), computeY(p.y));
			checkOverPoint(p);
		}
	}

	/**
	 * Fa si che appaia un tooltip per ogni punto dell'istanza.
	 */
	public final String getToolTipText(MouseEvent event) {
		String result = null;
		if (lastPoint != null) {
			City c = (City) pointHashTable.get(lastPoint);
			if (c != null)
				result = "City id: " + String.valueOf(c.num) + "  (x: "	+ coordFormat.format(c.x) + "  y: "	+ coordFormat.format(c.y) + ")";
		}
		return result;
	}

	/**
	 * Gestisce il click del mouse nell'area di disegno.
	 */
	public final void mouseClicked(MouseEvent e) {
	}

	/**
	 * Gestisce l'entrata del mouse nell'area di disegno.
	 */
	public final void mouseEntered(MouseEvent e) {
		if (isWorkArea)
			setCursor(workAreaCursor);
		else
			setCursor(DEFAULT_CURSOR);
	}

	/**
	 * Gestisce l'uscita del mouse nell'area di disegno.
	 */
	public final void mouseExited(MouseEvent e) {
		setCursor(DEFAULT_CURSOR);
		tspFrame.statusArea.clearCoord();
	}

	/**
	 * Controlla se siamo nell'area editabile.
	 */
	public final void checkWorkArea(int x, int y) {
		boolean checkWorkArea = ((x >= 0) && (x <= wsize) && (y >= 0) && (y <= hsize));
		if (isWorkArea && !checkWorkArea) {
			if (coordEnabled)
				tspFrame.statusArea.clearCoord();
			setCursor(DEFAULT_CURSOR);
			isWorkArea = false;
		} else if (!isWorkArea && checkWorkArea) {
			setCursor(workAreaCursor);
			isWorkArea = true;
		}
	}

	/**
	 * Controlla se si e' verificata la pressione del pulsante del mouse
	 * specifico per il menu Pop-up.
	 */
	private void checkPopUp(MouseEvent e) {
		if (e.isPopupTrigger()) {

			// Si deve memorizzare il punto dove si e' cliccato per
			// gestire le operazioni di delete, move, insert etc...
			clickPoint = e.getPoint();

			//
			// Se siamo nell'area editabile allora mostra il
			// menu Pop-up.
			//
			if (isWorkArea) {
				zoomOut.setEnabled(isZooming);
				boolean isCitySelected = editEnabled && (lastPoint != null);

				//
				// Calcola l'indice della città che e' stata selezionata.
				//
				selectedCity = (isCitySelected) ? ((City) pointHashTable.get(lastPoint)).num : -1;
				insertCity.setEnabled(editEnabled && !isCitySelected && (totC < CITIES_VECTOR_CAPACITY));
				moveCity.setEnabled(isCitySelected);
				delCity.setEnabled(isCitySelected);
				startCity.setEnabled(isCitySelected);
				popup.show(this, clickPoint.x, clickPoint.y);
				lastPoint = null;
			}
		}
	}

	/**
	 * Gestisce la pressione del pulsante del mouse nell'area di disegno
	 */
	public final void mousePressed(MouseEvent e) {
		//
		// Se non stiamo spostando una città.
		//
		if (movingCityID == -1)
			checkPopUp(e);
	}

	/**
	 * Gestisce il rilascio del pulsante del mouse nell'area di disegno.
	 */
	public final void mouseReleased(MouseEvent e) {
		//
		// Se non stiamo spostando una città.
		//
		if (movingCityID == -1)
			checkPopUp(e);
		else {
			int x = e.getX();
			int y = e.getY();
			checkWorkArea(x, y);
			if (isWorkArea) {
				workAreaCursor = DEFAULT_CURSOR;
				setCursor(workAreaCursor);
				//
				// Se si e' premuto il tasto per il pop-up, questo
				// equivale ad annullare lo spostamento.
				//
				if (!e.isPopupTrigger()) {
					tspFrame.setBusy(true);
					City c = Heuristic.getCityVector(movingCityID);
					UndoTSP.push(tspFrame, "Move city", Heuristic.MOVCITY, c.x, c.y, movingCityID);
					tspFrame.statusArea.setStatus("Moving city...");
					editThread = new Heuristic(tspFrame, Heuristic.MOVCITY, computeX(x), computeY(y), movingCityID, null);
					editThread.start();
				}
				movingCityID = -1;
			}
		}
	}
}

/**
 * Implementa l'area che fornisce le informazioni sullo status.
 */
class StatusArea extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = -4954430347631132908L;
	private JTextField coord, start;
	private JProgressBar progress;
	private NumberFormat coordFormat;

	/**
	 * Costruttore della classe.
	 */
	public StatusArea() {
		coordFormat = NumberFormat.getNumberInstance();
		coordFormat.setMaximumFractionDigits(2);
		coordFormat.setGroupingUsed(false);
		GridBagConstraints constraints = new GridBagConstraints();
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createEtchedBorder());

		//
		// ProgressBar "progress"
		//
		constraints.gridx = 0;
		constraints.weightx = 50;
		constraints.fill = GridBagConstraints.BOTH;
		progress = new JProgressBar(0, 100);
		progress.setIndeterminate(false);
		progress.setStringPainted(true);
		progress.setString("");
		progress.setFocusable(false);
		progress.setToolTipText("Progress");
		add(progress, constraints);

		//
		// Label "Start city"
		//
		constraints.gridx = 1;
		constraints.weightx = 10;
		start = new JTextField("0");
		start.setEditable(false);
		start.setFocusable(false);
		start.setHorizontalAlignment(JTextField.RIGHT);
		start.setToolTipText("Start city");
		add(start, constraints);

		//
		// Label "coord"
		//
		constraints.gridx = 2;
		constraints.weightx = 40;
		coord = new JTextField("");
		coord.setToolTipText("Coordinates (x,y)");
		coord.setEditable(false);
		coord.setFocusable(false);
		add(coord, constraints);
	}

	
	/**
	 * Modifica il campo string della ProgressBar "Progress".
	 */
	public synchronized void setStatus(String st) {
		progress.setValue(0);
		progress.setString(st);
	}

	/**
	 * Modifica il valore della ProgressBar "Progress".
	 *
	 * @param value da 0 a 100
	 */
	public synchronized void setProgress(int value) {
		progress.setValue(value);
	}

	/**
	 * Modifica il campo numerico "Start City".
	 */
	public void setStartCityField(int c) {
		start.setText(String.valueOf(c));
		Heuristic.setStartCity(c);
	}

	/**
	 * Imposta il valore delle coordinate.
	 */
	public void setCoord(double x, double y) {
		coord.setText("x: " + coordFormat.format(x) + "  y: " + coordFormat.format(y));
	}

	/**
	 * Cancella le coordinate.
	 */
	public void clearCoord() {
		coord.setText("");
	}

	/**
	 * Restituisce il valore del campo string della progressBar "Progress".
	 */
	public String getStatus() {
		return progress.getString();
	}
}

/**
 * Implementa i comandi per eseguire e scegliere le euristiche per il Tsp, e la
 * visualizzazione delle dimensioni del circuito e dell'istanza.
 */
class TspArea extends JPanel implements ActionListener, ItemListener, ChangeListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 3034763881597441653L;
	
	/**
	 * Periodo massimo (in millisecondi) tra una iterazione ed un'altra
	 * nell'animazione degli algoritmi.
	 */
	final static int MAX_STEP_TIME = Heuristic.DEF_STEP_TIME * 2;

	/**
	 * Stringhe per la descrizione degli algoritmi.
	 */
	final static String[] HEUR_STR = { "Construction", "Improvement", "Lower bounds" };
	final static String[] DESCR_STR = { "Greedy", "Nearest neighbor",
			"Cheapest insertion", "Random insertion", "Farthest insertion",
			"MST visit", "2-Opt", "3-Opt", "Held & Karp" };
	final static String[] HEUR_DESC_STR = { "Algorithms for TSP",
			"Construction heuristics", "Greedy (the shortest edge)",
			"Nearest neighbor", "Cheapest insertion (convex hull)",
			"Random insertion (convex hull)",
			"Farthest insertion (convex hull)",
			"Pre-order visit of the Minimum Spanning Tree",
			"Improvement heuristics (local search)", "2-Opt", "3-Opt",
			"Lower bounds", "Held & Karp lower bound" };

	/**
	 * Thread per l'elaborazione degli algoritmi di ricerca locale.
	 */
	public Heuristic computeThread;

	/**
	 * Pulsanti "Run" "Stop" "Pause".
	 */
	public JButton buttonRun, buttonOpt;
	private JButton buttonStop, buttonPause;

	/**
	 * Checkbox che abilita l'animazione degli algoritmi.
	 */
	private JCheckBox interact;

	/**
	 * JLabel per il campo Optimum.
	 */
	private JLabel labelOpt;

	/**
	 * Aree contenenti valori numerici come il numero di città, la lunghezza del
	 * circuito e il lower bound di Held-Karp.
	 */
	private JFormattedTextField totCities, tourLen, lowerBound, optimum;

	/**
	 * Rappresenta il frame principale.
	 */
	private TspFrame tspFrame;

	/**
	 * Lo slider con il quale si modifica il periodo di tempo che intercorre tra
	 * un'iterazione ed un'altra nell'animazione degli algoritmi.
	 */
	private JSlider slidStep;

	/**
	 * Combo box per la scelta degli algoritmi da eseguire.
	 */
	public TreeCombo treeComboBox;

	private String tmpStatus;
	private int count;
	private boolean visibleOptButton = true;
	private boolean isPressed = false;
	private boolean roundedTLen = TspFrame.ROUNDED_TOUR_LEN;
	private ButtonModel optModel;

	/**
	 * Costruttore della classe.
	 */
	public TspArea(TspFrame tFrame) {
		tspFrame = tFrame;
		GridBagConstraints constraints = new GridBagConstraints();
		GridBagLayout gridBag = new GridBagLayout();
		setLayout(gridBag);

		//
		// Etichetta "Cities".
		//
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 100;
		constraints.gridwidth = 2;
		constraints.insets = new Insets(4, 4, 4, 4);
		JLabel label1 = new JLabel("Tot. Cities:");
		add(label1, constraints);

		//
		// Campo numerico "Cities".
		//
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.EAST;
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		totCities = new JFormattedTextField(NumberFormat.getInstance());
		totCities.setHorizontalAlignment(JTextField.RIGHT);
		totCities.setEditable(false);
		totCities.setColumns(6);
		totCities.setValue(Heuristic.getTotCities());
		totCities.setFocusable(false);
		add(totCities, constraints);

		//
		// Etichetta "Tour Length"
		//
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 100;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 1;
		JLabel label2 = new JLabel("Tour length:", JLabel.LEFT);
		add(label2, constraints);

		//
		// Campo numerico "Tour Length".
		//
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		tourLen = new JFormattedTextField(NumberFormat.getInstance());
		tourLen.setHorizontalAlignment(JTextField.RIGHT);
		tourLen.setEditable(false);
		tourLen.setColumns(10);
		tourLen.setValue(Heuristic.getTourSize());
		tourLen.setFocusable(false);
		add(tourLen, constraints);

		//
		// Pulsante "Show optimum tour"
		//
		constraints.insets = new Insets(4, 4, 4, 0);
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.CENTER;
		constraints.weightx = 0;
		constraints.gridy = 2;
		constraints.gridx = 0;
		buttonOpt = new JButton(null, tspFrame.loadImageIcon("/images/lente.gif"));
		buttonOpt.setMargin(new Insets(0, 1, 0, 1));
		buttonOpt.setToolTipText("Show optimum tour");
		buttonOpt.addChangeListener(this);
		optModel = buttonOpt.getModel();
		add(buttonOpt, constraints);

		//
		// Etichetta "Optimum"
		//
		constraints.insets = new Insets(4, 2, 4, 4);
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 1;
		constraints.weightx = 100;
		labelOpt = new JLabel("Optimum:", JLabel.LEFT);
		add(labelOpt, constraints);

		//
		// Campo di testo numerico "Optimum"
		//
		constraints.insets = new Insets(4, 4, 4, 4);
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.gridx = 2;
		optimum = new JFormattedTextField(NumberFormat.getInstance());
		optimum.setHorizontalAlignment(JTextField.RIGHT);
		optimum.setEditable(false);
		optimum.setColumns(10);
		optimum.setValue(0);
		optimum.setFocusable(false);
		add(optimum, constraints);

		//
		// Etichetta "Lower bound"
		//
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 100;
		constraints.gridwidth = 2;
		constraints.gridx = 0;
		constraints.gridy = 3;
		JLabel label3 = new JLabel("Lower bound:", JLabel.LEFT);
		add(label3, constraints);

		//
		// Campo di testo numerico "Lower Bound"
		//
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0;
		constraints.gridwidth = 1;
		constraints.gridx = 2;
		lowerBound = new JFormattedTextField(NumberFormat.getInstance());
		lowerBound.setHorizontalAlignment(JTextField.RIGHT);
		lowerBound.setEditable(false);
		lowerBound.setFocusable(false);
		lowerBound.setColumns(10);
		lowerBound.setValue(0);
		add(lowerBound, constraints);

		//
		// Pannello contenente la treecombobox e i pulsanti.
		//
		JPanel aPanel = new JPanel();
		aPanel.setLayout(gridBag);
		aPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Algorithms "));
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 100;
		constraints.gridwidth = 3;
		constraints.gridy = 4;
		constraints.gridx = 0;
		add(aPanel, constraints);

		//
		// TreeComboBox "Heuristic"
		//
		DefaultMutableTreeNode heuristics = new DefaultMutableTreeNode(
				"Algorithms for TSP");
		DefaultMutableTreeNode constr = new DefaultMutableTreeNode(HEUR_STR[0]);
		heuristics.add(constr);
		DefaultMutableTreeNode local = new DefaultMutableTreeNode(HEUR_STR[1]);
		heuristics.add(local);
		DefaultMutableTreeNode lbound = new DefaultMutableTreeNode(HEUR_STR[2]);
		heuristics.add(lbound);
		for (count = 0; count < ConstructionHeuristic.CONSTR_COD.length; count++)
			constr.add(new DefaultMutableTreeNode(DESCR_STR[ConstructionHeuristic.CONSTR_COD[count]]));
		for (count = 0; count <  ImprovementHeuristic.LOCAL_COD.length; count++)
			local.add(new DefaultMutableTreeNode(DESCR_STR[ImprovementHeuristic.LOCAL_COD[count]]));
		for (count = 0; count <  ImprovementHeuristic.LBOUND_COD.length; count++)
			lbound.add(new DefaultMutableTreeNode(DESCR_STR[ImprovementHeuristic.LBOUND_COD[count]]));
		constraints.gridy = 0;
		constraints.gridwidth = 3;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.CENTER;
		treeComboBox = new TreeCombo(new DefaultTreeModel(heuristics), tspFrame);
		treeComboBox.descList = HEUR_DESC_STR;
		aPanel.add(treeComboBox, constraints);
		resetTreeComboBox();

		//
		// Pulsanti "Run", "Stop", "Pause"
		//
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.gridy = 1;
		constraints.gridwidth = 1;
		constraints.weightx = 33;
		buttonRun = new JButton(null, tspFrame.loadImageIcon("/images/play.gif"));
		buttonRun.setActionCommand("run");
		buttonRun.addActionListener(this);
		buttonRun.setToolTipText("Start computation");
		aPanel.add(buttonRun, constraints);
		constraints.gridx = 1;
		buttonPause = new JButton(null,	tspFrame.loadImageIcon("/images/pause.gif"));
		buttonPause.setActionCommand("pause");
		buttonPause.addActionListener(this);
		aPanel.add(buttonPause, constraints);
		buttonPause.setToolTipText("Pause computation");
		constraints.gridx = 2;
		buttonStop = new JButton(null, tspFrame.loadImageIcon("/images/stop.gif"));
		buttonStop.setActionCommand("stop");
		buttonStop.addActionListener(this);
		buttonStop.setToolTipText("Stop computation");
		aPanel.add(buttonStop, constraints);

		//
		// Pannello per l'animazione.
		//
		JPanel setPanel = new JPanel();
		setPanel.setLayout(gridBag);
		setPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), " Animation "));
		constraints.gridx = 0;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(4, 4, 4, 4);
		constraints.gridwidth = 3;
		constraints.gridy = 5;
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add(setPanel, constraints);

		//
		// CheckBox "Enable Animation"
		//
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.NONE;
		constraints.insets = new Insets(0, 4, 4, 4);
		interact = new JCheckBox("Enable animation", Heuristic.isAnimated());
		interact.addItemListener(this);
		setPanel.add(interact, constraints);

		//
		// Slider per incrementare/diminuire lo "Step time".
		//
		constraints.insets = new Insets(4, 0, 4, 0);
		constraints.gridy = 1;
		slidStep = new JSlider(JSlider.HORIZONTAL, 0, MAX_STEP_TIME, Heuristic.getStepTime());
		slidStep.addChangeListener(this);
		slidStep.setToolTipText("Step time (ms)");
		int step = MAX_STEP_TIME / 5;
		slidStep.setMajorTickSpacing(step);
		slidStep.setMinorTickSpacing(step / 5);
		slidStep.setPaintTicks(true);

		//
		// Creo la tabella delle label per lo slider.
		//
		Dictionary<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(Integer.valueOf(0), new JLabel("0ms"));
		labelTable.put(Integer.valueOf(MAX_STEP_TIME), new JLabel(MAX_STEP_TIME + "ms"));
		slidStep.setLabelTable(labelTable);
		slidStep.setPaintLabels(true);
		slidStep.setPreferredSize(new Dimension(170, slidStep.getPreferredSize().height));
		slidStep.setEnabled(interact.isSelected());
		setPanel.add(slidStep, constraints);

		// Invoco la procedura per la visualizzazione del pulsante
		// dell'ottimo e della sua lunghezza (in questo caso per default
		// non sono visualizzati).
		setVisibleOpt();
	}

	/**
	 * Gestisce la pressione della check-box "Animation".
	 */
	public void itemStateChanged(ItemEvent e) {
		boolean b = interact.isSelected();
		Heuristic.setAnimated(b);
		slidStep.setEnabled(b);
		slidStep.repaint();
	}

	/**
	 * Rende visibili i campi relativi al circuito ottimo se il parametro e'
	 * maggiore di 0, altrimenti li nasconde.
	 */
	public void setVisibleOpt() {
		double s = Heuristic.getOptTourSize();
		optimum.setValue(roundedTLen ? Math.round(s) : s);
		if ((s > 0) != visibleOptButton) {
			visibleOptButton = !visibleOptButton;
			labelOpt.setVisible(visibleOptButton);
			optimum.setVisible(visibleOptButton);
			buttonOpt.setVisible(visibleOptButton);
			revalidate();
			repaint();
		}
	}

	/**
	 * Aggiorna il campo numerico "Cities" e "Start city" nella status-bar.
	 */
	public synchronized void setTotCities() {
		totCities.setValue(Heuristic.getTotCities());
		tspFrame.statusArea.setStartCityField(Heuristic.getStartCity());
	}

	/**
	 * Modifica il campo numerico "Length".
	 */
	public synchronized void setTourLength() {
		double tlen = Heuristic.getTourSize();
		tourLen.setValue(roundedTLen ? Math.round(tlen) : tlen);
	}

	/**
	 * Abilita/disabilita l'arrotondamento delle lunghezze dei percorsi.
	 */
	public void setRoundedTLen(boolean b)
	{
		roundedTLen = b;
		setTourLength();
		setLowerBound();
		setVisibleOpt();
	}

	/**
	 * Abilita/disabilita la checkbox "Animation".
	 */
	public void enableAnimationCheckbox(boolean b) {
		interact.setEnabled(b);
	}

	/**
	 * Modifica il campo numerico "Lower Bound".
	 */
	public synchronized void setLowerBound() {
		double lb = Heuristic.getLowerBound();
		lowerBound.setValue(roundedTLen ? Math.round(lb) : lb);
	}

	/**
	 * Restituisce il valore del campo numerico "Lower Bound".
	 */
	public double getLowerBound() {
		return (double) lowerBound.getValue();
	}

	/**
	 * Gestisce il movimento dello slider, e la pressione del pulsante
	 * "Show optimum".
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(buttonOpt)
				&& (isPressed != optModel.isPressed())) {
			isPressed = optModel.isPressed();
			tspFrame.workArea.showOptimumTour(isPressed);
		} else {
			if (!slidStep.getValueIsAdjusting()) {
				int v = (int) slidStep.getValue();
				Heuristic.setStepTime(v);
			}
		}
	}

	/**
	 * Resetta la TreeComboBox.
	 */
	public void resetTreeComboBox() {
		treeComboBox.setSelectedIndex(0);
		treeComboBox.repaint();
	}

	/**
	 * Restituisce il tipo di Euristica scelta.
	 */
	private int getHeuristicType() {
		int choice = -1;
		String st = (String) ((DefaultMutableTreeNode) ((ListEntry) treeComboBox.getSelectedItem()).object).getUserObject();
		for (count = 0; count < DESCR_STR.length; count++)
			if (st.equals(DESCR_STR[count]))
				choice = count;
		return choice;
	}

	/**
	 * Gestisce la pressione dei Pulsanti "Run", "Stop", "Pause".
	 */
	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		//
		// Inizia l'esecuzione del thread per la computazione degli algoritmi.
		//
		switch (ac) {
			case "run":
				if (computeThread == null || (computeThread != null && !computeThread.isAlive())) {
					String err = null;
					int compID = getHeuristicType();
					if (compID < 0)
						err = "Please select a a valid heuristic...";
					else if (Heuristic.getTotCities() == 0)
							err = "There isn't a city.";
						else {
							switch (compID) {
								case ImprovementHeuristic.HK_LBOUND:
									if (Heuristic.getTotTourNodes() < 3)
										err = "A starting tour is needed.";
									else {
										computeThread = new ImprovementHeuristic(tspFrame, compID);
										tspFrame.statusArea.setStatus("Computing Held & Karp lower bound...");
									}
									break;
								case ImprovementHeuristic.TWO_CHANGE:
								case ImprovementHeuristic.THREE_CHANGE:
									if (Heuristic.getTotTourNodes() == 0)
										err = "A starting tour is needed.";
									else if (Heuristic.getTotTourNodes() < 4)
										err = "Nothing to improve.";
									else {
										computeThread = new ImprovementHeuristic(tspFrame, compID);
										UndoTSP.push(tspFrame, DESCR_STR[compID], Heuristic.NEW_TOUR);
										tspFrame.statusArea.setStatus("Improving this tour...");
									}
									break;
								default:
									if (Heuristic.getTotCities() < 2)
										err = "Nothing to construct.";
									else {
										computeThread = new ConstructionHeuristic(tspFrame, compID);
										UndoTSP.push(tspFrame, DESCR_STR[compID], Heuristic.NEW_TOUR);
										tspFrame.statusArea.setStatus("Construct a tour...");
										Heuristic.clearTour();
										tspFrame.workArea.repaint(false, true);
										tspFrame.tspArea.setTourLength();
									}
							}
						}
					if (err == null) {
						tspFrame.enableEditing(false);
						enableAnimationCheckbox(false);
						computeThread.start();
					} else
						JOptionPane.showMessageDialog(tspFrame, err, "Warning!", JOptionPane.WARNING_MESSAGE);
				}
				break;
			//
			// Interrompe il thread per la computazione degli algoritmi.
			//
			case "stop":
				if ((computeThread != null) && computeThread.isAlive()) {
					computeThread.setStopped(true);
					if (computeThread.isPaused())
						computeThread.setPaused(false);
					computeThread.interrupt();
					computeThread = null;
				}
				break;
			//
			// Mette il thread per la computazione degli algoritmi in pausa.
			//
			case "pause":
				if ((computeThread != null) && computeThread.isAlive()) {
					if (!computeThread.isPaused()) {
						tmpStatus = tspFrame.statusArea.getStatus();
						tspFrame.statusArea.setStatus("Paused...");
					} else
						tspFrame.statusArea.setStatus(tmpStatus);
					computeThread.setPaused(!computeThread.isPaused());
				}
		}
	}
}

/**
 * Dialog-box contenente una tabella per l'editing delle coordinate dei nodi
 * dell'istanza.
 */
class InstanceDialog extends JDialog implements ActionListener, TableModelListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 886223772323108547L;

	/**
	 * Etichette delle colonne della tabella, e flag che indicano se queste sono
	 * editabili.
	 */
	private static final String[] NAME_TABLE_COL = { "City ID", "X", "Y" };
	private static final boolean[] EDITABLE_COL = { false, true, true };

	private TspFrame tspFrame;
	private JButton ins, del, ok, cancel;
	private JTextField instName, tourName;
	private MyTableModel myModel;
	private JTable table;
	private JScrollPane scrollPane;
	private boolean tableIsChanged;

	/**
	 * Costruttore della classe.
	 */
	public InstanceDialog(TspFrame tFrame, String title, boolean modal) {
		super(tFrame, title, modal);
		tspFrame = tFrame;
		Container contentPane = getContentPane();
		tableIsChanged = false;
		GridBagConstraints constraints = new GridBagConstraints();
		contentPane.setLayout(new BorderLayout());
		JPanel buttPanel = new JPanel();
		buttPanel.setLayout(new BorderLayout());
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridBagLayout());
		buttPanel.add(buttonsPanel, BorderLayout.NORTH);
		JPanel commPanel = new JPanel();
		commPanel.setLayout(new GridBagLayout());
		commPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), " Comments: "));
		int tot = Heuristic.getTotCities();
		myModel = new MyTableModel(NAME_TABLE_COL, tot, EDITABLE_COL);
		table = new JTable(myModel);
		table.setPreferredScrollableViewportSize(new Dimension(400, 350));
		scrollPane = new JScrollPane(table);
		//
		// Aggiorna il contenuto della tabella.
		//
		City c;
		for (int count = 0; count < tot; count++) {
			c = Heuristic.getCityVector(count);
			myModel.setValueAt(c.num, count, 0);
			myModel.setValueAt(c.x, count, 1);
			myModel.setValueAt(c.y, count, 2);
		}
		myModel.addTableModelListener(this);
	
		// Stabilisce la grandezza della prima colonna,
		// le restanti ottengono lo spazio disponibile
		table.getColumnModel().getColumn(0).setMinWidth(50);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		//
		// Button "Insert".
		//
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(4, 4, 4, 4);
		constraints.weightx = 0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		ins = new JButton("Insert");
		ins.setActionCommand("ins");
		ins.addActionListener(this);
		buttonsPanel.add(ins, constraints);

		//
		// Button "Delete city".
		//
		constraints.gridy = 1;
		del = new JButton("Delete");
		del.setActionCommand("del");
		del.addActionListener(this);
		buttonsPanel.add(del, constraints);

		//
		// Button "OK".
		//
		constraints.gridy = 2;
		ok = new JButton("OK");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		buttonsPanel.add(ok, constraints);

		//
		// Button "Cancel".
		//
		constraints.gridy = 3;
		cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		buttonsPanel.add(cancel, constraints);

		//
		// Etichetta "Instance name".
		//
		constraints.fill = GridBagConstraints.NONE;
		constraints.gridx = 0;
		constraints.gridy = 0;
		commPanel.add(new JLabel("Instance comment:", JLabel.LEFT), constraints);

		//
		// TextField "Instance comment".
		//
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 100;
		constraints.gridx = 1;
		instName = new JTextField(tspFrame.getInstComm());
		commPanel.add(instName, constraints);
		if (Heuristic.getTotTourNodes() > 0) {
			//
			// Etichetta "Tour comment".
			//
			constraints.fill = GridBagConstraints.NONE;
			constraints.weightx = 0;
			constraints.gridx = 0;
			constraints.gridy = 1;
			commPanel.add(new JLabel("Tour comment:", JLabel.LEFT), constraints);

			//
			// TextField "Tour comment".
			//
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.gridx = 1;
			constraints.weightx = 100;
			tourName = new JTextField(tspFrame.getTourComm());
			commPanel.add(tourName, constraints);
		}

		//
		// Dispone i componenti del InstanceDialog.
		//
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(buttPanel, BorderLayout.EAST);
		contentPane.add(commPanel, BorderLayout.SOUTH);
	}

	/**
	 * Gestisce gli eventi provenienti dal TableModel.
	 */
	public void tableChanged(TableModelEvent e) {
		tableIsChanged = true;
	}

	/** 
	 * 
	*/
	private void insRow() {
		int count = 0;
		int tot = table.getRowCount();
		int[] sel = table.getSelectedRows();
		if ((tot >= CITIES_VECTOR_CAPACITY)	|| ((tot > 0) && (sel.length == 0)))
			JOptionPane.showMessageDialog(this,	"There isn't a selected row.", "Warning!", JOptionPane.WARNING_MESSAGE);
		else {
			int ind;
			if (tot++ > 0)
				ind = sel[0];
			else
				ind = 0;
			myModel.insertRow(ind);
			for (count = ind; count < tot; count++)
				myModel.setValueAt(Integer.valueOf(count), count, 0);
			myModel.setValueAt(Double.valueOf(0), ind, 1);
			myModel.setValueAt(Double.valueOf(0), ind, 2);
			table.repaint();
			table.revalidate();
			table.clearSelection();
		}
	}

	/** 
	 * 
	*/
	private void delRow() {
		int count = 0;
		int[] sel = table.getSelectedRows();
		if (sel.length > 0) {
			myModel.deleteRows(sel);
			int tot = table.getRowCount();
			for (count = sel[0]; count < tot; count++)
				myModel.setValueAt(Integer.valueOf(count), count, 0);
			table.repaint();
			table.revalidate();
			table.clearSelection();
		} else {
			JOptionPane.showMessageDialog(this,	"There isn't a selected row.", "Warning!", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * 
	 */
	private void loadTable() {
		int count = 0;
		tspFrame.setInstComm(instName.getText());
		if (Heuristic.getTotTourNodes() > 0)
			tspFrame.setTourComm(tourName.getText());
		if (tableIsChanged) {
			int tot = table.getRowCount();
			double[] xArray = new double[tot];
			double[] yArray = new double[tot];
			for (count = 0; count < tot; count++) {
				xArray[count] = ((Double) myModel.getValueAt(count, 1)).doubleValue();
				yArray[count] = ((Double) myModel.getValueAt(count, 2)).doubleValue();
			}
			dispose();
			tspFrame.setBusy(true);
			tspFrame.statusArea.setStatus("Compute instance");
			tspFrame.chgTour(false);
			tspFrame.setTourName("", "");
			tspFrame.setTourComm("");
			tspFrame.chgInstance(false);
			tspFrame.setInstName("", "");
			Heuristic.setCitiesVector(xArray, yArray, tot, null, Heuristic.EUC_2D, tspFrame);
			tspFrame.tspArea.setTotCities();
			tspFrame.tspArea.setTourLength();
			tspFrame.tspArea.setLowerBound();
			tspFrame.tspArea.setVisibleOpt();
			tspFrame.workArea.resetZoom();
			tspFrame.workArea.repaint(true, true);
			tspFrame.statusArea.setStatus("");
			tspFrame.setBusy(false);
		} else
			dispose();
	}

	/**
	 * Gestisce la pressione dei pulsanti "New", "Delete", "Ok", "Cancel".
	 */
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand()) {
			//
			// Inserimento di una riga vuota (tranne nel caso delle tabella vuota,
			// dobbiamo selezionare la riga dopo la quale vogliamo inserirla).
			//
			case "ins":
				insRow();
				break;
			//
			// Cancellazione di una riga.
			//
			case "del":
				delRow();
				break;
			//
			// Aggiorna il contenuto dell'istanza corrente, con i valori
			// della tabella.
			//
			case "ok":
				loadTable();
				break;
			default:
				dispose();

		}	
	}
}

/*
 * Modello astratto per le tabelle usate in questa applicazione.
 */
class MyTableModel extends AbstractTableModel {
	/**
	 *
	 */
	private static final long serialVersionUID = -933150263363940432L;
	private boolean[] editable;
	private String[] columnNames;
	private Object[][] data;
	private Object[][] tmpData = null;

	/**
	 * Costruttore classe.
	 */
	public MyTableModel(String[] cNames, int size, boolean[] edit) {
		columnNames = cNames;
		data = new Object[size][columnNames.length];
		editable = edit;
	}

	/**
	 * Ritorna il numero delle colonne della tabella.
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Ritorna il numero delle righe della tabella.
	 */
	@Override
	public int getRowCount() {
		return data.length;
	}

	/*
	 * Ritorna il nome della colonna.
	 */
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/*
	 * Ritorna l'oggetto memorizzato nella posizione della tabella indicata
	 * dalla riga e colonna.
	 */
	@Override
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	/*
	 * La JTable usa questo metodo per determinare il renderer di
	 * default per ogni cella. Se non implementassimo questo metodo, verrebbe
	 * usato il generico toString().
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * Aggiungi una riga.
	 */
	public void addRow() {
		tmpData = data;
		int tot1 = data.length;
		int tot2 = columnNames.length;
		data = new Object[tot1 + 1][tot2];
		for (int count1 = 0; count1 < tot1; count1++)
			for (int count2 = 0; count2 < tot2; count2++)
				data[count1][count2] = tmpData[count1][count2];
		tmpData = null;
		fireTableRowsInserted(tot1, tot1);
	}

	/*
	 * Inserisci una riga.
	 */
	public void insertRow(int index) {
		tmpData = data;
		int tot1 = data.length;
		int tot2 = columnNames.length;
		data = new Object[tot1 + 1][tot2];
		for (int count1 = 0; count1 < tot1; count1++) {
			if (count1 < index)
				for (int count2 = 0; count2 < tot2; count2++)
					data[count1][count2] = tmpData[count1][count2];
			else
				for (int count2 = 0; count2 < tot2; count2++)
					data[count1 + 1][count2] = tmpData[count1][count2];
		}
		tmpData = null;
		fireTableRowsInserted(index, index);
	}

	/*
	 * Cancella le righe selezionate.
	 */
	public void deleteRows(int[] rowsToDel) {
		int totRows = data.length;
		int totRowsToDel = rowsToDel.length;
		if ((totRows > 0) && (totRowsToDel > 0)) {
			tmpData = data;
			int delcount = 0;
			int curRowToDel = rowsToDel[0];
			int count2;
			int count3 = 0;
			int totColumn = columnNames.length;
			data = new Object[totRows - totRowsToDel][totColumn];
			for (int count1 = 0; count1 < totRows; count1++) {
				if (count1 != curRowToDel) {
					for (count2 = 0; count2 < totColumn; count2++)
						data[count3][count2] = tmpData[count1][count2];
					count3++;
				} else if (++delcount < totRowsToDel)
					curRowToDel = rowsToDel[delcount];
			}
			tmpData = null;
			fireTableRowsDeleted(rowsToDel[0], rowsToDel[totRowsToDel - 1]);
		}
	}

	/*
	 * Non c'e' bisogno di implementare questo metodo se tutte le celle sono
	 * editabili.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return editable[col];
	}

	/*
	 * Memorizza il valore passato per parametro nella posizione della tabella
	 * indicate dalla riga e colonna.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}
}




/**
 * Implementa la Dialog Box per la lettura delle
 * istanze predefinite della TSPLIB'95.
 */
class TSPLIbDialog extends JDialog implements ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -8433435713535919613L;
	/**
	 * Etichette delle colonne della tabella, e flag che indicano se queste sono
	 * editabili.
	 */
	private static final String[] NAME_TABLE_COL = { "Name", "Type",
			"Description" };
	private static final boolean[] EDITABLE_COL = { false, false, false };

	private TspFrame tspFrame;
	private JButton ok, cancel;
	private MyTableModel myModel;
	private JTable table;
	private String[] optimum;
	private JScrollPane scrollPane;

	/**
	 * Costruttore della classe.
	 */
	public TSPLIbDialog(TspFrame tFrame, String title, boolean modal, Object data[][]) {
		super(tFrame, title, modal);
		tspFrame = tFrame;
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		JPanel buttonsPanel = new JPanel();
		int tot = data.length;
		myModel = new MyTableModel(NAME_TABLE_COL, tot, EDITABLE_COL);
		table = new JTable(myModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(new Dimension(500, 400));
		scrollPane = new JScrollPane(table);
		table.getColumnModel().getColumn(0).setMinWidth(130);
		table.getColumnModel().getColumn(0).setMaxWidth(150);
		table.getColumnModel().getColumn(1).setMinWidth(50);
		table.getColumnModel().getColumn(1).setMaxWidth(60);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		//
		// Aggiorna il contenuto della tabella.
		//
		optimum = new String[tot];
		for (int count = 0; count < tot; count++) {
			myModel.setValueAt(data[count][0], count, 0);
			myModel.setValueAt(data[count][1], count, 1);
			myModel.setValueAt(data[count][2], count, 2);
			optimum[count] = (String) data[count][3];
		}

		//
		// Button "Open".
		//
		ok = new JButton("Open");
		ok.setActionCommand("open");
		ok.addActionListener(this);
		buttonsPanel.add(ok);

		//
		// Button "Cancel".
		//
		cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		buttonsPanel.add(cancel);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(buttonsPanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(tspFrame);
	}

	/**
	 * Gestisce la pressione dei pulsanti "Ok", "Cancel".
	 */
	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		if (ac.equals("open")) {
			int sel = table.getSelectedRow();
			if (sel == -1)
				JOptionPane.showMessageDialog(this,
						"There isn't a selected file.", "Warning!",
						JOptionPane.WARNING_MESSAGE);
			else {
				tspFrame.ioSession(IOThread.LOAD_TSPLIB_INST, "", (String) myModel.getValueAt(sel, 0), optimum[sel].trim());
				table.clearSelection();
				dispose();
			}
		}
		if (ac.equals("cancel"))
			dispose();
	}
}

/**
 * Implementa la dialog box per la generazione casuale delle città.
 */
class RandomDialog extends JDialog implements AdjustmentListener, ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -265656932765869534L;
	private TspFrame tspFrame;
	private JScrollBar sbMaxCities;
	private JButton buttonGenerate, buttonCancel;
	private JFormattedTextField maxCities;
	private int tot = 1;

	public RandomDialog(TspFrame tFrame, String title, boolean modal) {
		super(tFrame, title, modal);
		Container contentPane = getContentPane();
		tspFrame = tFrame;
		GridBagConstraints constraints = new GridBagConstraints();
		contentPane.setLayout(new GridBagLayout());
		constraints.insets = new Insets(4, 5, 4, 5);
		JPanel frPanel = new JPanel();
		frPanel.setLayout(new GridBagLayout());
		frPanel.setBorder(BorderFactory.createRaisedSoftBevelBorder());

		//
		// Pulsante "Generate"
		//
		constraints.weightx = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.EAST;
		buttonGenerate = new JButton("Generate");
		buttonGenerate.setActionCommand("generate");
		buttonGenerate.addActionListener(this);
		contentPane.add(buttonGenerate, constraints);

		//
		// Pulsante "Cancel"
		//
		constraints.gridy = 1;
		buttonCancel = new JButton("Cancel");
		buttonCancel.setActionCommand("cancel");
		buttonCancel.addActionListener(this);
		contentPane.add(buttonCancel, constraints);

		//
		// Etichetta "Cities"
		//
		constraints.gridy = 0;
		JLabel label = new JLabel("Random cities:", JLabel.RIGHT);
		frPanel.add(label, constraints);

		//
		// Campo di testo numerico "Cities"
		//
		constraints.gridx = 1;
		maxCities = new JFormattedTextField(NumberFormat.getInstance());
		maxCities.setHorizontalAlignment(JTextField.RIGHT);
		maxCities.setEditable(false);
		maxCities.setColumns(6);
		maxCities.setValue(tot);
		frPanel.add(maxCities, constraints);

		//
		// Scrollbar (Cursore) per incrementare/diminuire il
		// numero max di città
		//
		constraints.gridy = 1;
		constraints.gridx = 0;
		constraints.gridwidth = 2;
		constraints.weightx = 100;
		sbMaxCities = new JScrollBar(JScrollBar.HORIZONTAL, tot, 0, 1, CITIES_VECTOR_CAPACITY);
		sbMaxCities.addAdjustmentListener(this);
		frPanel.add(sbMaxCities, constraints);
		
		constraints.gridwidth = 1;
		constraints.gridheight = 2;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridy = 0;
		constraints.gridx = 1;
		constraints.weightx = 100;
		contentPane.add(frPanel, constraints);
	}

	/**
	 * Gestisce il movimento della Scrollbar (Cursore) "Max cities"
	 */
	public void adjustmentValueChanged(AdjustmentEvent e) {
		maxCities.setValue(sbMaxCities.getValue());
	}

	/**
	 * Gestisce la pressione dei pulsanti: - Generate - Cancel
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("generate")) {
			tot = sbMaxCities.getValue();
			tspFrame.workArea.startRandomThread(tot);
		}
		dispose();
	}
}


/**
 * Classe che serve a gestire le operazioni di undo.
 */
class UndoTSP {
	/*
	* Stack utilizzato per gestire l'undo.
	*/
	private final static Stack<UndoTSP> undoStack = new Stack<UndoTSP>();

	String descr, oldTourName, oldTourPathname, oldTourComm;
	boolean oldIsTourChg, oldIsInstChg;
	int undoType;
	City[] undoTour;
	City city;
	double x, y;
	int ind; 
	double oldTourSize, optTourSize, lowerBound;
	
	/**
	 * Costruttore undo su operazioni su percorsi (tour).
	 * 
	 * @param tspFrame
	 * @param desc
	 * @param type
	 */
	public UndoTSP(TspFrame tspFrame, String desc, int type) {
		undoType = type;
		descr = desc;
		oldTourComm = tspFrame.getTourComm();
		oldTourName = tspFrame.getTourName();
		oldTourPathname = tspFrame.getTourPathname();
		oldIsTourChg = tspFrame.isTourChg();
		int dim = Heuristic.getTotTourNodes();
		undoTour = new City[dim];
		oldTourSize = Heuristic.getTourSize();
		for (int count = 0; count < dim; count++)
			undoTour[count] = Heuristic.getTourVector(count);
	}

	/**
	 *  Costruttore undo su operazioni su nodi (città).
	 * 
	 * @param tspFrame
	 * @param desc
	 * @param type
	 * @param x
	 * @param y
	 * @param ind
	 */
	public UndoTSP(TspFrame tspFrame, String desc, int type, double x, double y, int ind) {
		undoType = type;
		descr = desc;
		optTourSize = Heuristic.getOptTourSize();
		lowerBound = Heuristic.getLowerBound();
		oldIsTourChg = tspFrame.isTourChg();
		oldIsInstChg = tspFrame.isInstChg();
		this.ind = ind;
		this.x = x;
		this.y = y;
		city = (undoType == Heuristic.ADDCITY) ? Heuristic.getCityVector(ind) : null;
	}

	/**
	 * Inserimento di un'operazione di undo (vari tipi).
	 */
	public final static void push(TspFrame tspFrame, String desc, int type) {
		undoStack.push(new UndoTSP(tspFrame, desc, type));
	}

	public final static void push(TspFrame tspFrame, String desc, int type, double x, double y, int ind) {
		undoStack.push(new UndoTSP(tspFrame, desc, type, x, y, ind));
	}

	/**
	 * Se lo stack di undo è vuoto.
	 * 
	 * @return
	 */
	public final static boolean isEmpty() {
		return undoStack.isEmpty();
	}

	/**
	 * Resetta l'undo.
	 */
	public final static void clear() {
		undoStack.clear();
	}

	/**
	 * Estrae il primo elemento dello stack di undo
	 * @return
	 */
	public final static UndoTSP pop() {
		return undoStack.pop();
	}
	
	/**
	 * Ritorna (senza rimuoverlo) il primo elemento dello stack di undo
	 * @return
	 */
	public final static UndoTSP peek() {
		return undoStack.peek();
	}
}