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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.net.URL;
import java.lang.IllegalArgumentException;
import java.util.StringTokenizer;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;

import static net.guarnie.tspdemo.Heuristic.CITIES_VECTOR_CAPACITY;

/**
 * Classe specializzata per l'I/O locale e remoto delle istanze.
 */
public class IOThread extends Thread {
	/**
	 * Codici di riconoscimento per il tipo di operazione di I/O su file remoto
	 * o locale.
	 */
	final static int LOAD_TSPLIB_INST = 0;
	final static int LOAD_LOCAL = 1;
	final static int SAVE_LOCAL_TOUR = 2;
	final static int SAVE_LOCAL_INST = 3;
	final static int LOAD_TSPLIB_DIR = 4;

	private TspFrame tspFrame;
	private int ioType = -1;
	private Object param;
	private String fileName, pathName, pathFileName, optimum;
	private double[] xArray;
	private double[] yArray;
	private int[] indexTour;
	private double[][] weightsMatrix;

	/**
	 * Costruttore della classe.
	 */
	IOThread(TspFrame tFrame, int ioType, String pathName, String fileName,	Object param) {
		tspFrame = tFrame;
		weightsMatrix = null;
		this.ioType = ioType;
		this.param = param;
		this.fileName = fileName;
		this.pathName = pathName;
		pathFileName = pathName + fileName;
	}

	/**
	 * Esecuzione dei thread di lettura/scrittura di una istanza.
	 */
	public void run() {
		setPriority(MIN_PRIORITY);
		switch (ioType) {
			case SAVE_LOCAL_TOUR:
			case SAVE_LOCAL_INST:
				int tot = Heuristic.getTotCities();
				try 
				(
					BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathFileName)));
				)
				{
					String str;
					str = "NAME : " + fileName;
					outWriter.write(str, 0, str.length());
					outWriter.newLine();
					if (param != null) {
						str = "COMMENT : " + (String) param;
						outWriter.write(str, 0, str.length());
						outWriter.newLine();
					}
					str = "TYPE : "	+ ((ioType == SAVE_LOCAL_INST) ? "TSP" : "TOUR");
					outWriter.write(str, 0, str.length());
					outWriter.newLine();
					str = "DIMENSION : " + String.valueOf(tot);
					outWriter.write(str, 0, str.length());
					outWriter.newLine();
					if (ioType == SAVE_LOCAL_INST) {
						str = "EDGE_WEIGHT_TYPE : EUC_2D";
						outWriter.write(str, 0, str.length());
						outWriter.newLine();
					}
					str = (ioType == SAVE_LOCAL_INST) ? "NODE_COORD_SECTION" : "TOUR_SECTION";
					outWriter.write(str, 0, str.length());
					outWriter.newLine();
					if (ioType == SAVE_LOCAL_INST)
						saveInstance(outWriter, tot);
					else
						saveTour(outWriter);
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(tspFrame, "Can't create file " + fileName, "I/O Error", JOptionPane.ERROR_MESSAGE);
				}
				finally
				{
					tspFrame.closeIODialog();
				}
				break;
			case LOAD_TSPLIB_DIR:
				int dim = 0;
				int count;
				boolean error = false;
				Object[][] instNames = null;
				URL indexURL = getClass().getResource(pathFileName);
				//
				// Lettura dei nomi delle istanze predefinite memorizzate
				// nella URL di default.
				//
				try
				(
					//
					// Il file indice e' in formato Zip.
					//
					ZipInputStream zipStream = new ZipInputStream(indexURL.openStream());
					BufferedReader zipBufReader = new BufferedReader(new InputStreamReader(zipStream));
				)
				{
					zipStream.getNextEntry();
					StreamTokenizer inToken = new StreamTokenizer(zipBufReader);
					inToken.wordChars(58, 58);
					inToken.nextToken();

					//
					// Inizializzazione della matrice
					// in base al numero di files.
					//
					if ("FILES:".equals(inToken.sval)) {
						if (StreamTokenizer.TT_NUMBER == inToken.nextToken()) {
							dim = (int) inToken.nval;
							instNames = new Object[dim][4];
							//
							// Si sposta sulla prima linea utile.
							//
							inToken.nextToken();
						} else
							error = true;
					} else
						error = true;

					//
					// Abilita il riconoscimento degli EOL (end of line).
					//
					inToken.eolIsSignificant(true);
					count = 0;
					//
					// Ciclo di riempimento della matrice.
					//
					while ((StreamTokenizer.TT_EOF != inToken.ttype) && !error) {
						if (StreamTokenizer.TT_WORD == inToken.ttype) {
							switch(inToken.sval) {
								case "NAME:":
									instNames[count][0] = zipBufReader.readLine();
									break;
								case "TYPE:":
									instNames[count][1] = zipBufReader.readLine();
									break;
								case "DESCRIPTION:":
									instNames[count][2] = zipBufReader.readLine();
									break;
								case "OPTIMUM:":
									instNames[count][3] = zipBufReader.readLine();
									break;
								default:
									error = true;
							}
						} else if (StreamTokenizer.TT_EOL == inToken.ttype)
							count++;
						inToken.nextToken();
					}
					tspFrame.closeIODialog();
				} catch (IOException ioe) {
					tspFrame.closeIODialog();
					JOptionPane.showMessageDialog(tspFrame, "Can't connect to "	+ pathFileName, "I/O Error", JOptionPane.ERROR_MESSAGE);
				}
				
				if (!error)
						tspFrame.createRemoteDialog(instNames);
					else
						JOptionPane.showMessageDialog(tspFrame, pathFileName + " isn't a valid index file.", "I/O Error", JOptionPane.ERROR_MESSAGE);
				break;
			case LOAD_TSPLIB_INST:
			case LOAD_LOCAL:
				if (ioType == LOAD_LOCAL) {
					loadInstance(pathFileName, LOAD_LOCAL);
				} else {
					if (loadInstance(pathFileName, LOAD_TSPLIB_INST)) {
						optimum = (String) param;
						if ((optimum != null) && (optimum.length() != 0)) {
							fileName = optimum;
							tspFrame.statusArea.setStatus(" Loading optimum tour...");
							loadInstance(pathName + fileName, LOAD_TSPLIB_INST);
						}
					}
				}
				tspFrame.tspArea.setVisibleOpt();
		}

		//
		// Cosi' il garbage collector puo' disporre della memoria
		// occupata da questo thread.
		//
		tspFrame.ioThread = null;
	}

	/**
	 * Salva l'istanza corrente (Si basa sulle specifiche della TSPLIB 95).
	 */
	private void saveInstance(BufferedWriter outputWriter, int tot) throws IOException {
		int index1;
		String str;
		City c;
		for (index1 = 0; index1 < tot; index1++) {
			c = Heuristic.getCityVector(index1);
			str = String.valueOf(index1 + 1) + " " + String.valueOf(c.x) + " " + String.valueOf(c.y);
			outputWriter.write(str, 0, str.length());
			outputWriter.newLine();
		}
		str = "EOF";
		outputWriter.write(str, 0, str.length());
		outputWriter.close();
		tspFrame.setInstName(fileName, pathName);
		tspFrame.chgInstance(false);
	}

	/**
	 * Salva il circuito corrente (Si basa sulle specifiche della TSPLIB 95).
	 */
	private void saveTour(BufferedWriter outputWriter) throws IOException {
		String str;
		int tot = Heuristic.getTotCities();
		
		for (int count = 0; count < tot;) {
			str = String.valueOf(Heuristic.getTourVector(count++).num + 1);
			outputWriter.write(str, 0, str.length());
			outputWriter.newLine();
		}
		str = "-1";
		outputWriter.write(str, 0, str.length());
		outputWriter.newLine();
		str = "EOF";
		outputWriter.write(str, 0, str.length());
		outputWriter.close();
		tspFrame.setTourName(fileName, pathName);
		tspFrame.chgTour(false);
	}

	/**
	 * Legge un'istanza o un circuito (Si basa sulle specifiche della TSPLIP95)
	 * 
	 */
	private boolean loadInstance(String pathFileName, int ioType) {
		final String badFormatErrorStr = "Bad instance's data format.";
		boolean tourFlag = false;
		int edgeWeightType = Heuristic.EUC_2D;
		boolean needNodeSection = true;
		boolean needTourSection = false;
		boolean needEdgeSection = false;
		boolean needDisplaySection = false;
		StringTokenizer inNumToken;
		String comment = "";
		String tokenStr, tmpStr;
		String edgeWeightFormat = null;
		int instanceDim = -1;
		int index1, index2;
		boolean result = true;
		BufferedReader inputReader = null;

		try {
			if (ioType == LOAD_LOCAL) {
				inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(pathFileName)));
				
			} else {
				URL instanceURL = getClass().getResource(pathFileName);
				inputReader = new BufferedReader(new InputStreamReader(instanceURL.openStream()));
			}
			StreamTokenizer inToken = new StreamTokenizer(inputReader);
			inToken.wordChars('!', '*');
			inToken.wordChars('/', '/');
			inToken.whitespaceChars(':', ':');
			inToken.wordChars(';', '@');
			inToken.wordChars('[', '`');
			inToken.wordChars('{', '~');

			do {
				inToken.nextToken();
				switch (inToken.ttype) {
					case StreamTokenizer.TT_EOF:
						break;
					case StreamTokenizer.TT_WORD:
						tokenStr = inToken.sval;
						switch (tokenStr)
						{
							//
							// - THE SPECIFICATION PART -
							//
							case "NAME":
								inputReader.readLine();
								break;
							case "TYPE":
								inToken.nextToken();
								tmpStr = inToken.sval;
								if (!tmpStr.equals("TSP") && !tmpStr.equals("TOUR"))
									throw new IllegalArgumentException("Only Symmetric TSP instances and tours are allowed.");
								else if (tmpStr.equals("TOUR")) {
									tourFlag = true;
									needNodeSection = false;
									needTourSection = true;
									if (Heuristic.getTotCities() == 0)
										throw new IllegalArgumentException("Can't apply tour to an empty node set.");
								}
								break;
							case "COMMENT":
								comment = inputReader.readLine().trim();
								comment = comment.startsWith(":")? comment.substring(1).trim() : comment.trim();
								break;
							case "DIMENSION":
								inToken.nextToken();
								instanceDim = (int) inToken.nval;
								if (instanceDim > CITIES_VECTOR_CAPACITY)
									throw new IllegalArgumentException(((tourFlag) ? "Tour" : "Instance") + " too large.");
								break;
							case "EDGE_WEIGHT_TYPE":
								inToken.nextToken();
								tmpStr = inToken.sval;
								if (tmpStr.equals("EXPLICIT")) {
									needEdgeSection = true;
									edgeWeightType = Heuristic.EXPLICIT;
								}
								//
								// IL TIPO "GEO" NON E' SUPPORTATO!
								//
								else if (!tmpStr.equals("EUC_2D"))
									throw new IllegalArgumentException("Edge weight type \"" + inToken.sval + "\" not supported.");
								break;
							case "EDGE_WEIGHT_FORMAT":
								inToken.nextToken();
								edgeWeightFormat = inToken.sval;
								if (edgeWeightFormat.equals("FUNCTION"))
									throw new IllegalArgumentException("Weights given by function are not supported.");
								break;
							case "EDGE_DATA_FORMAT":
								throw new IllegalArgumentException("Not complete graphs are not supported.");
							case "NODE_COORD_TYPE":
								inToken.nextToken();
								break;
							case "DISPLAY_DATA_TYPE":
								inToken.nextToken();
								if (inToken.sval.equals("NO_DISPLAY"))
									throw new IllegalArgumentException("No graphical display is possible.");
								else if (inToken.sval.equals("TWOD_DISPLAY")) {
									needDisplaySection = true;
									needNodeSection = false;
								}
								break;
							//
							// - THE DATA PART -
							//
							case "NODE_COORD_SECTION":
							case "DISPLAY_DATA_SECTION":
							{
								if ("NODE_COORD_SECTION".equals(tokenStr)) {
									if (!needNodeSection)
										throw new IllegalArgumentException(badFormatErrorStr);
									else
										needNodeSection = false;
								} else {
									if (!needDisplaySection)
										throw new IllegalArgumentException(badFormatErrorStr);
									else
										needDisplaySection = false;
								}

								if (instanceDim == -1)
									throw new IllegalArgumentException("Instance dimension not found.");
								index1 = 0;
								try {
									xArray = new double[instanceDim];
									yArray = new double[instanceDim];
								} catch (OutOfMemoryError om) {
									tspFrame.ioThread = null;
									throw new IllegalArgumentException("Out of memory.");
								}
								while (index1 < instanceDim) {
									inToken.nextToken();
									if (inToken.ttype != StreamTokenizer.TT_NUMBER)
										throw new IllegalArgumentException(badFormatErrorStr);
									else {
										if ((index1 + 1) != ((int) inToken.nval))
											throw new IllegalArgumentException(badFormatErrorStr);
										//
										// Lo StreamTokenizer non riconosce i numeri
										// floating-point nel formato [-]m.ddddE^xx, 
										// quindi li devo interpretare "manualmente".
										//
										inNumToken = new StringTokenizer(inputReader.readLine());
										try {
											xArray[index1] = Double.valueOf(inNumToken.nextToken()).floatValue();
											yArray[index1++] = Double.valueOf(inNumToken.nextToken()).floatValue();
										} catch (NumberFormatException nfe) {
											throw new IllegalArgumentException(badFormatErrorStr);
										}
									}
								}
								break;
							}
							case "EDGE_DATA_SECTION":
								throw new IllegalArgumentException("Not complete graphs are not supported.");
							case "FIXED_EDGES_SECTION":
								throw new IllegalArgumentException("Fixed edges are not supported.");
							case "EDGE_WEIGHT_SECTION":
							{
								if (!needEdgeSection)
									throw new IllegalArgumentException(badFormatErrorStr);
								else
									needEdgeSection = false;
								try {
									weightsMatrix = new double[instanceDim][instanceDim];
								} catch (OutOfMemoryError om) {
									tspFrame.ioThread = null;
									throw new IllegalArgumentException("Out of memory.");
								}
								int startX = 0;
								int endX = instanceDim;
								int startY = 0;
								int endY = instanceDim;
								int modStart = 0;
								int modEnd = 0;
								if (edgeWeightFormat == null)
									throw new IllegalArgumentException(badFormatErrorStr);
								else switch (edgeWeightFormat)
								{
									case "UPPER_ROW":
									case "LOWER_COL":
										startX = 1;
										endY = instanceDim - 1;
										modStart = 1;
										break;
									case "UPPER_DIAG_ROW":
									case "LOWER_DIAG_COL":
										modStart = 1;
										break;
									case "UPPER_COL":
									case "LOWER_ROW":
										endX = 1;
										startY = 1;
										modEnd = 1;
										break;
									case "UPPER_DIAG_COL":
									case "LOWER_DIAG_ROW":
										endX = 1;
										modEnd = 1;
										break;
								}
								index1 = startY;
								while (index1 < endY) {
									index2 = startX;
									while (index2 < endX) {
										inToken.nextToken();
										if (inToken.ttype == StreamTokenizer.TT_NUMBER) {
											weightsMatrix[index1][index2] = (double) inToken.nval;
											weightsMatrix[index2++][index1] = (double) inToken.nval;
										} else
											throw new IllegalArgumentException(badFormatErrorStr);
									}
									index1++;
									startX += modStart;
									endX += modEnd;
								}
								break;
							}
							case "TOUR_SECTION":
							{
								if (!needTourSection)
									throw new IllegalArgumentException(badFormatErrorStr);
								else
									needTourSection = false;
								
								int tot = Heuristic.getTotCities();

								if ((tot != instanceDim) && (instanceDim != -1))
									throw new IllegalArgumentException("The number of tour's nodes doesn't equals to current node set.");
									
								try {
									indexTour = new int[tot];
								} catch (OutOfMemoryError om) {
									tspFrame.ioThread = null;
									throw new IllegalArgumentException("Out of memory.");
								}

								int count = 0;
								index1 = -1;
								do {
									inToken.nextToken();
									if (inToken.ttype != StreamTokenizer.TT_NUMBER)
										throw new IllegalArgumentException(badFormatErrorStr);
									else {
										index1 = (int) inToken.nval;
										if ((count > tot) || (index1 > tot)	|| (index1 < -1) || (index1 == 0))
											throw new IllegalArgumentException(badFormatErrorStr);
										else if (index1 != -1)
											indexTour[count++] = index1 - 1;
									}
								} while (index1 != -1);

								if (tot != count)
									throw new IllegalArgumentException("The number of tour's nodes doesn't equals to current node set.");
								else
									instanceDim = tot;
								
								break;
							}
							case "EOF":
								inToken.nextToken();
								break;
						}
						break;
				default:
					throw new IllegalArgumentException(badFormatErrorStr);
				}
			} while (inToken.ttype != StreamTokenizer.TT_EOF);
			
			if (needTourSection)
				throw new IllegalArgumentException("TOUR_SECTION not found.");
			if (needEdgeSection)
				throw new IllegalArgumentException("EDGE_WEIGHT_SECTION not found.");
			if (needNodeSection)
				throw new IllegalArgumentException("NODE_COORD_SECTION not found, no graphical display is possible.");
			if (needDisplaySection)
				throw new IllegalArgumentException("DISPLAY_DATA_SECTION not found, no graphical display is possible.");
			tspFrame.closeIODialog();
		} catch (IllegalArgumentException iae) {
			result = false;
			tspFrame.closeIODialog();
			JOptionPane.showMessageDialog(tspFrame, iae.getMessage(), "Format error", JOptionPane.ERROR_MESSAGE);
		}
		catch (IOException ioe) {
			result = false;
			tspFrame.closeIODialog();
			JOptionPane.showMessageDialog(tspFrame, "Can't load " + fileName + " data.", "I/O Error", JOptionPane.ERROR_MESSAGE);
		}
		finally
		{
			if (inputReader != null) try { inputReader.close(); } catch (Exception e) {} 
		}

		if (result) {
			tspFrame.setBusy(true);
			//
			// Leggiamo il circuito ottimo.
			//
			if (optimum != null) {
				tspFrame.statusArea.setStatus("Compute optimum tour...");
				Heuristic.makeOptTour(indexTour);
			}
			// Altrimenti leggiamo una istanza o un tour
			else {
				if (tourFlag) {
					tspFrame.statusArea.setStatus("Computing tour...");
					UndoTSP.push(tspFrame, "Load tour", Heuristic.NEW_TOUR);
					Heuristic.clearTour();
					tspFrame.setTourComm(comment);
					tspFrame.chgTour(false);
					tspFrame.setTourName(fileName, (ioType == LOAD_TSPLIB_INST) ? "" : pathName);
					for (index1 = 0; index1 < instanceDim; index1++)
						Heuristic.addTourNode(Heuristic.getCityVector(indexTour[index1]));
				} 
				else {
					tspFrame.statusArea.setStatus("Computing instance...");
					tspFrame.chgTour(false);
					tspFrame.setTourName("", "");
					tspFrame.setTourComm("");
					tspFrame.chgInstance(false);
					tspFrame.setInstName(fileName, (ioType == LOAD_TSPLIB_INST) ? "" : pathName);
					tspFrame.setInstComm(comment);
					Heuristic.setCitiesVector(xArray, yArray, instanceDim, weightsMatrix, edgeWeightType, tspFrame);
					UndoTSP.clear();
					tspFrame.tspArea.setTotCities();
					tspFrame.tspArea.setLowerBound();
					tspFrame.tspArea.resetTreeComboBox();
				}
				tspFrame.tspArea.setTourLength();
				tspFrame.workArea.resetZoom();
				tspFrame.workArea.repaint(true, true);
			}
			tspFrame.statusArea.setStatus("");
			tspFrame.setBusy(false);
		}
			
		return result;
	}
}