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

import java.util.Random;

/**
 * Classe che contiene le strutture dati e che implementa le operazioni sui nodi
 * e sugli archi del nostro grafo.
 *   
 * Inoltre calcola anche l'"Held Karp lower bound".
 */
public class Heuristic extends Thread {

	/**
	 * Costante che indica la massima capacità del vettore che deve contenere le
	 * coordinate dei nodi (città).
	 */
	final static int CITIES_VECTOR_CAPACITY = 15000;

	/**
	 * Codici di riconoscimento per la generazione casuale, inserimento,
	 * spostamento, cancellazione di città (alcuni servono anche per l'undo
	 * delle rispettive operazioni).
	 */
	final static int RANDOM = 0;
	final static int ADDCITY = 1;
	final static int MOVCITY = 2;
	final static int DELCITY = 3;
	final static int LBOUND = 4;
	final static int NEW_TOUR = 5;

	/**
	 * Codici di riconoscimento per identificare il diverso tipo di
	 * rappresentazione delle distanze.
	 */
	final static int EXPLICIT = 0;
	final static int EUC_2D = 1;
	final static int GEO = 2;

	/**
	 * Periodo default (in millisecondi) tra una iterazione ed un'altra nell'animazione degli
	 * algoritmi.
	 */
	final static int DEF_STEP_TIME = 200;

	/**
	 * Numero di nodi facenti parte il circuito. Si puo' vedere anche come
	 * indice del prossimo nodo da inserire nel circuito.
	 */
	private static int totTourNodes;

	/**
	 * Ultimo nodo del circuito inserito.
	 */
	private static int lastNum;

	/**
	 * Variabile personalizzata di "stop".
	 */
	private boolean isStopped;

	/**
	 * Variabile personalizzata di "pause".
	 */
	private boolean isPaused;

	/**
	 * Le coordinate massime e minime in x e y dei nodi (citta') dell'istanza
	 * corrente.
	 */
	private static double maxX = Double.NEGATIVE_INFINITY;
	private static double maxY = Double.NEGATIVE_INFINITY;
	private static double minX = Double.POSITIVE_INFINITY;
	private static double minY = Double.POSITIVE_INFINITY;

	/**
	 * Indica se siamo in modalita' interattiva.
	 */
	private static boolean animated = false;

	/**
	 * Indice della citta' dalla quale iniziare la computazione di un algoritmo
	 * (in alcuni casi e' ignorata).
	 */
	protected static int startCity = 0;

	/**
	 * Tempo (in msec) tra una iterazione ed un'altra nella modalita'
	 * interattiva.
	 */
	private static int stepTime = DEF_STEP_TIME;

	/**
	 * Dimensione attuale del vettore dei nodi, 0 significa vettore vuoto. E'
	 * anche l'indice del prossimo nodo (citta') da inserire nel vettore.
	 */
	protected static int totCities = 0;

	/**
	 * Vettore che contiene tutte le coordinate dei nodi (citta') cosi' come
	 * sono stati inseriti.
	 */
	protected static City[] citiesVector = new City[CITIES_VECTOR_CAPACITY];

	/**
	 * Vettore che contiene il circuito che rappresenta la soluzione corrente al
	 * TSP per l'istanza.
	 */
	protected static City[] tourVector = new City[CITIES_VECTOR_CAPACITY];

	/**
	 * Vettore che contiene il circuito ottimo (se e' conosciuto) dell'istanza
	 * corrente.
	 */
	protected static City[] optVector = new City[CITIES_VECTOR_CAPACITY];

	/**
	 * Matrice di adiacenza del grafo.
	 */
	protected static double[][] adjMatrix = new double[CITIES_VECTOR_CAPACITY][CITIES_VECTOR_CAPACITY];

	/**
	 * Dimensione totale del circuito che rappresenta la soluzione attuale al
	 * TSP.
	 */
	protected static double tourSize = 0;

	/**
	 * Dimensione totale del circuito ottimo per il TSP (se ne siamo a
	 * conoscenza).
	 */
	protected static double optTourSize = 0;

	/**
	 * Lower bound per l'attuale circuito (se è stato calcolato)
	 */
	protected static double lowerBound = 0;

	/**
	 * Riferimento al frame principale dell'applicazione.
	 */
	protected TspFrame tspFrame;

	/**
	 * Codice che indica che tipo di operazione stiamo eseguendo.
	 */
	protected int execID;

	/**
	 * parametri per l'esecuzione dei thread legati alla gen. casuale
	 * all'inserimento, alla modifica, alla cancellazione di citta', e all'undo.
	 */
	private double xparam;
	private double yparam;
	private int param;
	private City cityparam;
	private boolean isUndoing = false;

	/**
	 * Abilita o meno l'undo delle operazioni di inserimento, modifica,
	 * cancellazione.
	 */
	public final void setUndoing(boolean enable) {
		isUndoing = enable;
	}

	/**
	 * Costruttore della classe.
	 */
	public Heuristic(TspFrame tFrame, int eID, double x, double y, int p, City city) {
		execID = eID;
		tspFrame = tFrame;
		xparam = x;
		yparam = y;
		param = p;
		cityparam = city;
	}

	/**
	 * Metodi per resituire MinX, MaxX, MinY, MaxY.
	 */
	public final static double getMinX() {
		return minX;
	}

	public final static double getMaxX() {
		return maxX;
	}

	public final static double getMinY() {
		return minY;
	}

	public final static double getMaxY() {
		return maxY;
	}

	/**
	 * Modifica MinX, MaxX, MinY, MaxY contemporaneamente.
	 */
	public final static void setMinMax(double mx, double mxx, double my, double mxy) {
		minX = mx;
		maxX = mxx;
		minY = my;
		maxY = mxy;
	}

	/**
	 * Verifica se e' abilitata l'animazione degli algoritmi.
	 */
	public final static synchronized boolean isAnimated() {
		return animated;
	}

	/**
	 * Abilita o meno l'animazione degli algoritmi.
	 */
	public final static synchronized void setAnimated(boolean b) {
		animated = b;
	}

	/**
	 * Restituisce il tempo (in ms) tra passo e un altro nell'animazione degli
	 * algoritmi.
	 */
	public final static synchronized int getStepTime() {
		return stepTime;
	}

	/**
	 * Imposta il tempo (in ms) tra passo e un altro nell'animazione degli
	 * algoritmi.
	 */
	public final static synchronized void setStepTime(int step) {
		stepTime = step;
	}

	/**
	 * Restituisce la citta' da cui iniziare la computazione di un algoritmo (in
	 * alcuni casi viene ignorata).
	 */
	public final static int getStartCity() {
		return startCity;
	}

	/**
	 * Imposta la citta' da cui iniziare la computazione di un algoritmo (in
	 * alcuni casi viene ignorata).
	 */
	public final static void setStartCity(int start) {
		startCity = start;
	}

	/**
	 * Aggiunge un nodo del vettore delle coordinate dei nodi.
	 */
	public final static void addCity(double x, double y) {
		euc2D(x, y, totCities++);
		lowerBound = 0;
		if (totTourNodes > 1) {
			City c = citiesVector[totCities - 1];
			int idx = c.neighbours[0].c.tourIndex;
			c.tourIndex = idx;
			System.arraycopy(tourVector, idx, tourVector, idx + 1, totTourNodes	- idx);
			totTourNodes++;
			tourVector[idx] = c;
			for (int count1 = idx + 1; count1 < totTourNodes; count1++)
				tourVector[count1].tourIndex++;
			int c1 = prevTourNode(c).num;
			int c2 = nextTourNode(c).num;
			tourSize += adjMatrix[c.num][c1] + adjMatrix[c.num][c2]	- adjMatrix[c1][c2];
		}
	}

	/**
	 * Modifica un nodo del vettore delle coordinate dei nodi.
	 */
	private static void movCity(double x, double y, int ind) {
		double dist;
		int count1, count2;
		double[] oldDist = new double[totCities];
		City c = citiesVector[ind];
		double dx, dy;
		checkBounds(x, y);
		lowerBound = 0;

		//
		// Aggiorna la dimensione del circuito (elimina nodo con la vecchia
		// posizione).
		//
		if (totTourNodes > 1)
			tourSize -= adjMatrix[c.num][nextTourNode(c).num] + adjMatrix[c.num][prevTourNode(c).num];

		//
		// Modifica le coordinate della citta'.
		//
		c.x = x;
		c.y = y;

		//
		// Memorizza le vecchie distanze tra il nodo da spostare e gli altri.
		//
		System.arraycopy(adjMatrix[ind], 0, oldDist, 0, totCities);

		//
		// Aggiorna la matrice di adiacenza con le nuove distanze.
		//
		for (count1 = 0; count1 < totCities; count1++) {
			c = citiesVector[count1];
			dx = x - c.x;
			dy = y - c.y;
			dist = Math.sqrt(dx * dx + dy * dy);
			adjMatrix[ind][count1] = dist;
			adjMatrix[count1][ind] = dist;
		}

		//
		// Ricalcola i neighbours.
		//
		for (count1 = 0; count1 < totCities; count1++) {
			c = citiesVector[count1];
			if ((c.farthestDist >= oldDist[count1])
					|| (c.farthestDist >= adjMatrix[count1][ind])) {
				c.clearNeighbours();
				for (count2 = 0; count2 < totCities; count2++)
					if (count1 != count2)
						c.addNeighbour(citiesVector[count2],
								adjMatrix[count1][count2]);
			}
		}

		//
		// Aggiorna la dimensione del circuito (inserisce nodo con la nuova
		// posizione).
		//
		c = citiesVector[ind];
		if (totTourNodes > 1)
			tourSize += adjMatrix[c.num][nextTourNode(c).num] + adjMatrix[c.num][prevTourNode(c).num];
	}

	/**
	 * Cancella un nodo del vettore delle coordinate dei nodi.
	 */
	private static void delCity(int ind) {
		int count1, count2;
		double[] oldDist = new double[totCities - 1];
		City c = citiesVector[ind];
		final int IDX = c.tourIndex;
		final int TOT = totCities - ind - 1;
		lowerBound = 0;

		//
		// Aggiorna la dimensione del circuito.
		//
		if (totTourNodes > 1) {
			count1 = nextTourNode(c).num;
			count2 = prevTourNode(c).num;
			tourSize += adjMatrix[count1][count2] - adjMatrix[c.num][count1] - adjMatrix[c.num][count2];
		}

		//
		// Memorizza le distanze tra il nodo da cancellare e gli altri.
		//
		System.arraycopy(adjMatrix[ind], 0, oldDist, 0, ind);
		System.arraycopy(adjMatrix[ind], ind + 1, oldDist, ind, TOT);

		//
		// Aggiorna la matrice di adiacenza.
		//
		for (count1 = 0; count1 < ind; count1++)
			System.arraycopy(adjMatrix[count1], ind + 1, adjMatrix[count1],
					ind, TOT);
		for (count1 = ind + 1; count1 < totCities; count1++) {
			System.arraycopy(adjMatrix[count1], 0, adjMatrix[count1 - 1], 0,
					ind);
			System.arraycopy(adjMatrix[count1], ind + 1, adjMatrix[count1 - 1],
					ind, TOT);
		}

		//
		// Aggiorna il CitiesVector.
		//
		System.arraycopy(citiesVector, ind + 1, citiesVector, ind, TOT);
		totCities--;
		for (count1 = ind; count1 < totCities; count1++)
			citiesVector[count1].num--;

		//
		// Aggiorna il TourVector.
		//
		if (totTourNodes > 0) {
			System.arraycopy(tourVector, IDX + 1, tourVector, IDX, totTourNodes
					- IDX - 1);
			totTourNodes--;
			for (count1 = IDX; count1 < totTourNodes; count1++)
				tourVector[count1].tourIndex--;
		}

		//
		// Ricalcola i neighbours.
		//
		for (count1 = 0; count1 < totCities; count1++) {
			c = citiesVector[count1];
			if (c.farthestDist >= oldDist[count1]) {
				c.clearNeighbours();
				for (count2 = 0; count2 < totCities; count2++)
					if (count1 != count2)
						c.addNeighbour(citiesVector[count2],
								adjMatrix[count1][count2]);
			}
		}

		//
		// Aggiorna l'indice della citta' di partenza per
		// l'esecuzione degli algoritmi.
		//
		if (startCity == ind)
			startCity = 0;
		else if (startCity > ind)
			startCity--;
	}

	/**
	 * Ripristina la memorizzazione di un nodo (serve per l'undo del
	 * "Delete city").
	 */
	private static void resumeCity(City node) {
		int count1, count2;
		int ind = node.num;
		final int IDX = node.tourIndex;
		final int TOT = totCities - ind;
		lowerBound = 0;

		//
		// Aggiorna la matrice di adiacenza.
		//
		for (count1 = 0; count1 < ind; count1++)
			System.arraycopy(adjMatrix[count1], ind, adjMatrix[count1],
					ind + 1, TOT);
		for (count1 = totCities; count1 > ind; count1--) {
			System.arraycopy(adjMatrix[count1 - 1], 0, adjMatrix[count1], 0, ind);
			System.arraycopy(adjMatrix[count1 - 1], ind, adjMatrix[count1],	ind + 1, TOT);
		}
		//
		// Aggiorna il CitiesVector.
		//
		System.arraycopy(citiesVector, ind, citiesVector, ind + 1, TOT);
		totCities++;
		for (count1 = ind + 1; count1 < totCities; count1++)
			citiesVector[count1].num++;
		citiesVector[ind] = node;

		//
		// Aggiorna le distanze e i neighbours.
		//
		double dist;
		double x = node.x;
		double y = node.y;
		double dx, dy;
		City c;
		for (count1 = 0; count1 < totCities; count1++) {
			c = citiesVector[count1];
			dx = x - c.x;
			dy = y - c.y;

			//
			// Rispetto le specifiche per il calcolo della
			// distanza della TSPLIB '95.
			//
			dist = Math.sqrt(dx * dx + dy * dy);
			if (count1 != ind)
				c.addNeighbour(node, dist);
			adjMatrix[ind][count1] = dist;
			adjMatrix[count1][ind] = dist;
		}

		//
		// Aggiorna il TourVector.
		//
		if (totTourNodes > 0) {
			System.arraycopy(tourVector, IDX, tourVector, IDX + 1, totTourNodes
					- IDX);
			totTourNodes++;
			tourVector[IDX] = node;
			for (count1 = IDX + 1; count1 < totTourNodes; count1++)
				tourVector[count1].tourIndex++;
		}

		//
		// Aggiorna la dimensione del circuito.
		//
		if (totTourNodes > 0) {
			count1 = nextTourNode(node).num;
			count2 = prevTourNode(node).num;
			tourSize += adjMatrix[node.num][count1]	+ adjMatrix[node.num][count2] - adjMatrix[count1][count2];
		}
	}

	/**
	 * Inizializza il vettore delle coordinate dei nodi (città) con valori casuali.
	 */
	private static void setRandomCitiesVector(int dimX, int dimY, int newTot, TspFrame tspFrame) {
		clearCities();
		minX = 0;
		minY = 0;
		maxX = dimX;
		maxY = dimY;
		Random randomCity = new Random();
		for (int count1 = 0; count1 < newTot; count1++)
		{
			tspFrame.statusArea.setProgress(Math.round(((float) count1 / newTot) * 100));
			euc2D(Math.abs(randomCity.nextInt() % dimX), Math.abs(randomCity.nextInt() % dimY), count1);
		}
		totCities = newTot;
	}

	/**
	 * Inizializza il vettore delle coordinate dei nodi (città) con i valori dei due
	 * vettori delle coordinate X e Y passati per parametro (se siamo nel caso
	 * EXPLICIT aggiorna la matrice delle distanze con quella passata per
	 * parametro).
	 */
	public final static void setCitiesVector(double[] xv, double[] yv, int newTot, double[][] tmpMatrix, int type, TspFrame tspFrame) {
		int count1, count2;
		double x, y;
		double dist;
		City c, newCity;
		clearCities();
		count1 = -1;
		switch (type) {
		case EXPLICIT:
			for (count2 = 0; count2 < newTot; count2++)
				System.arraycopy(tmpMatrix[count2], 0, adjMatrix[count2], 0, newTot);
			while (++count1 < newTot) {
				tspFrame.statusArea.setProgress(Math.round(((float) count1 / newTot) * 100));
				x = xv[count1];
				y = yv[count1];
				checkBounds(x, y);
				newCity = new City(x, y, count1);
				citiesVector[count1] = newCity;
				for (count2 = 0; count2 < count1; count2++) {
					c = citiesVector[count2];
					dist = adjMatrix[count1][count2];
					c.addNeighbour(newCity, dist);
					newCity.addNeighbour(c, dist);
				}
			}
			break;
		case GEO:
			//
			// NON E' SUPPORTATO DA QUESTO PROGRAMMA!
			//
			break;
		case EUC_2D:
			while (++count1 < newTot) {
				tspFrame.statusArea.setProgress(Math.round(((float) count1 / newTot) * 100));
				x = xv[count1];
				y = yv[count1];
				checkBounds(x, y);
				euc2D(x, y, count1);
			}
		}
		totCities = newTot;
	}

	/**
	 * Esegue l'inserimento di una citta' con le coordinate euclidee x e y.
	 * Quindi calcola tutti gli archi tra il nuovo nodo e i nodi preesistenti e
	 * li inserisce nella matrice di adiacenza. Inoltre memorizza per ogni nodo
	 * i K nodi piu' vicini.
	 */
	private static void euc2D(double x, double y, int count1) {
		double dx, dy;
		City c;
		double dist;
		City newCity = new City(x, y, count1);
		citiesVector[count1] = newCity;
		for (int count2 = 0; count2 < count1; count2++) {
			c = citiesVector[count2];
			dx = x - c.x;
			dy = y - c.y;

			//
			// Rispetto le specifiche per il calcolo della distanza
			// della TSPLIB '95.
			//
			dist = Math.sqrt(dx * dx + dy * dy);

			c.addNeighbour(newCity, dist);
			newCity.addNeighbour(c, dist);
			adjMatrix[count1][count2] = dist;
			adjMatrix[count2][count1] = dist;
		}
	}

	/**
	 * Memorizza le coord. minime e massime in X e Y.
	 */
	public final static void checkBounds(double x, double y) {
		if (x < minX)
			minX = x;
		if (y < minY)
			minY = y;
		if (x > maxX)
			maxX = x;
		if (y > maxY)
			maxY = y;
	}

	/**
	 * Restituisce un nodo contenuto ad un certo indice nel vettore dei nodi.
	 */
	public final static City getCityVector(int count) {
		return citiesVector[count];
	}

	/**
	 * Restituisce un nodo contenuto ad un certo indice nel vettore di citta'
	 * che rappresenta la soluzione al TSP (circuito corrente).
	 */
	public final static City getTourVector(int count) {
		return tourVector[count];
	}

	/**
	 * Restituisce un nodo contenuto ad un certo indice nel vettore di citta'
	 * che rappresenta il circuito ottimo (se ne siamo a conoscenza).
	 */
	public final static City getOptVector(int count) {
		return optVector[count];
	}

	/**
	 * Restituisce la dimensione attuale del vettore dei nodi.
	 */
	public final static int getTotCities() {
		return totCities;
	}

	/**
	 * Azzera il totale delle città.
	 */
	public final static void clearCities() {
		totCities = 0;
		optTourSize = 0;
		startCity = 0;
		lowerBound = 0;
		citiesVector = new City[CITIES_VECTOR_CAPACITY];
		optVector = new City[CITIES_VECTOR_CAPACITY];
		minX = Double.POSITIVE_INFINITY;
		minY = Double.POSITIVE_INFINITY;
		maxX = Double.NEGATIVE_INFINITY;
		maxY = Double.NEGATIVE_INFINITY;
		clearTour();
	}

	/**
	 * Inserisce una città nel vettore di città che rappresenta la soluzione al
	 * TSP.
	 */
	protected final static void addTourNode(City c) {
		if (totTourNodes < totCities) {
			if (totTourNodes > 0)
				tourSize += adjMatrix[lastNum][c.num];
			c.tourIndex = totTourNodes;
			tourVector[totTourNodes++] = c;
			lastNum = c.num;
			if (totTourNodes == totCities)
				tourSize += adjMatrix[lastNum][tourVector[0].num];
		}
	}

	/**
	 * Crea il vettore delle città che rappresenta la soluzione ottima.
	 */
	protected final static void makeOptTour(int[] index) {
		optTourSize = 0;
		int last = index.length - 1;
		int lastInd = index[last];
		int ind;
		optVector[last] = citiesVector[lastInd];
		for (int count = 0; count < last; count++) {
			ind = index[count];
			optVector[count] = citiesVector[ind];
			optTourSize += adjMatrix[ind][lastInd];
			lastInd = ind;
		}
		optTourSize += adjMatrix[index[last]][lastInd];
	}
	
	/**
	 * Restituisce il Lower Bound per il percorso corrente, altrimenti 0.
	 */
	public final static double getLowerBound() {
		return lowerBound;
	}

	/**
	 * Restituisce la dimensione del circuito corrente (la soluzione corrente
	 * del TSP).
	 */
	public final static double getTourSize() {
		return tourSize;
	}

	/**
	 * Restituisce la dimensione ottima del circuito corrente se ne siamo a
	 * conoscenza.
	 */
	public final static double getOptTourSize() {
		return optTourSize;
	}

	/**
	 * Imposta la dimensione ottima del circuito corrente. (Serve per
	 * ripristinare la dimensione del circuito ottimo dopo l'undo).
	 */
	public final static void setOptTourSize(double s) {
		optTourSize = s;
	}

	/**
	 * Restituisce il numero di nodi appartenenti attualmente al circuito.
	 */
	public final static int getTotTourNodes() {
		return totTourNodes;
	}

	/**
	 * Restituisce il nodo successivo a quello passato per parametro nel
	 * circuito corrente.
	 */
	public final static City nextTourNode(City c) {
		return tourVector[(c.tourIndex + 1) % totTourNodes];
	}

	/**
	 * Restituisce il nodo precedente a quello passato per parametro nel
	 * circuito corrente.
	 */
	public final static City prevTourNode(City c) {
		return tourVector[(c.tourIndex > 0) ? c.tourIndex - 1
				: totTourNodes - 1];
	}

	/**
	 * Cancella il circuito che rappresenta la soluzione attuale al TSP.
	 */
	public final static void clearTour() {
		tourSize = 0;
		totTourNodes = 0;
		tourVector = new City[CITIES_VECTOR_CAPACITY];
	}

	/**
	 * Imposta il circuito corrente.
	 */
	public final static void setTour(double size, City[] vect) {
		tourSize = size;
		totTourNodes = vect.length;
		City c;
		for (int count = 0; count < totTourNodes; count++) {
			c = vect[count];
			c.tourIndex = count;
			tourVector[count] = c;
		}
	}

	/**
	 * Restituisce true se il thread ha subito uno "stop" false altrimenti.
	 */
	protected final synchronized boolean isStopped() {
		return isStopped;
	}

	/**
	 * Imposta a true o false la variabile "mystop".
	 */
	protected final synchronized void setStopped(boolean stop) {
		isStopped = stop;
	}

	/**
	 * Restituisce true se il thread ha subito un "pause" false altrimenti.
	 */
	protected final synchronized boolean isPaused() {
		return isPaused;
	}

	/**
	 * Imposta a true o false la variabile "mypause".
	 */
	protected final synchronized void setPaused(boolean pause) {
		isPaused = pause;
		if (!pause)
			notify();
	}

	/**
	 * Controlla se e' a true o false la variabile "mypause", se e' a true
	 * attende.
	 */
	private synchronized void checkPause() {
		while (isPaused) {
			try {
				wait();
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * Si mette in pausa per un "stepTime" msec.
	 */
	protected final void pausing() {
		try {
			sleep(getStepTime());
		} catch (InterruptedException ie) {
		}
		checkPause();
	}

	/**
	 * Metodo necessario all'esecuzione di questo thread.
	 */
	public void run() {
		//
		// Setto la priorita' al minimo dato che questo thread
		// e' decisamente CPU-INTENSIVE.
		//
		setPriority(MIN_PRIORITY);
		switch (execID) {
			case RANDOM:
				setRandomCitiesVector((int) xparam, (int) yparam, param, tspFrame);
				UndoTSP.clear();
				tspFrame.setInstComm("");
				tspFrame.setTourComm("");
				tspFrame.setTourName("", "");
				tspFrame.setInstName("", "");
				tspFrame.tspArea.resetTreeComboBox();
				break;
			case ADDCITY:
				if (cityparam == null)
					addCity(xparam, yparam);
				else
					resumeCity(cityparam);
				break;
			case MOVCITY:
				movCity(xparam, yparam, param);
				break;
			case DELCITY:
				delCity(param);
				if (totCities == 0)
					tspFrame.workArea.resetMinMaxXY();
				break;
		}
		if (execID != MOVCITY)
			tspFrame.tspArea.setTotCities();
			
		if ((execID != RANDOM) && !isUndoing) {
			optTourSize = 0;
			tspFrame.chgInstance(true);
			tspFrame.chgTour(true);
		}

		tspFrame.tspArea.setTourLength();
		tspFrame.tspArea.setLowerBound();
		tspFrame.tspArea.setVisibleOpt();
		tspFrame.statusArea.setStatus("");
		tspFrame.workArea.repaint(true, true);
		tspFrame.setBusy(false);

		//
		// Cosi' il garbage collector puo' disporre della memoria
		// occupata da questo thread.
		//
		tspFrame.workArea.editThread = null;
	}
}
