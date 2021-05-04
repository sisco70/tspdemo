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
 * Classe che implementa delle euristiche di "costruzione", cioe' costruiscono
 * una soluzione al TSP a partire unicamente da un insieme di nodi (citta'):
 * 
 * - Greedy - Nearest Neighbor - Cheapest Insertion - Farthest Insertion -
 * Random Insertion - Minimum Spanning Tree (Pre-Order Visit)
 *
 */
public class ConstructionHeuristic extends Heuristic {

	/**
	 * Codici di riconoscimento per gli algoritmi di ricerca locale.
	 */
	 
	final static int GREEDY = 0;
	final static int NEAREST_NEIGHBOR = 1;
	final static int CHEAPEST_INSERTION = 2;
	final static int RANDOM_INSERTION = 3;
	final static int FARTHEST_INSERTION = 4;
	final static int MST_PREORDER = 5;

	final static int[] CONSTR_COD = { GREEDY, NEAREST_NEIGHBOR,
		CHEAPEST_INSERTION, RANDOM_INSERTION, FARTHEST_INSERTION,
		MST_PREORDER };

	/**
	 * Costruttore della classe.
	 */
	public ConstructionHeuristic(TspFrame tFrame, int eID) {
		super(tFrame, eID, -1, -1, -1, null);
	}

	/**
	 * [Greedy] Partendo da n catene di lunghezza zero si prende in
	 * considerazione via via l'arco di lunghezza minima (tra tutti quelli
	 * possibili), che mantiene la catena a cui si va a connettere ancora una
	 * catena e non un circuito o un albero.
	 */
	private final void greedy() {
		double min;
		int tmp1, tmp2, count1, count2;
		final int tot = totCities - 1;
		MFSet elem1, elem2;

		//
		// Array con le distanze minime per ogni nodo.
		//
		double[] d = new double[totCities];

		//
		// Array contenente per ogni nodo l'indice dell'ultimo nodo connesso
		// (al max se ne possono connettere 2).
		// E' inizializzato per default con tutti 0.
		//
		int[] c = new int[totCities];

		//
		// Array temporaneo.
		//
		double[] tmp;

		//
		// Costruisco l'array le distanze minime (in questo caso
		// ci sono proprio i minimi reali ottenuti dalle neigbours list,
		// successivamente ci saranno solo dei minimi di riferimento).
		//
		for (count1 = 0; count1 < tot; count1++)
			d[count1] = citiesVector[count1].neighbours[0].dist;
		tmp1 = 0;
		tmp2 = 0;
		int index = 0;

		//
		// Vettore contenente gli elementi del MFSet, servono per sapere
		// velocemente se aggiungendo un nuovo arco ad una catena
		// pre-esistente creo un circuito.
		//
		MFSet[] MFSetArray = new MFSet[totCities];
		for (count1 = 0; count1 < totCities; count1++)
			MFSetArray[count1] = new MFSet(citiesVector[count1]);
		while ((index < totCities) && !isStopped()) {
			min = Double.MAX_VALUE;

			//
			// Ricerca dell'arco di lunghezza minima (escludendo i nodi
			// che hanno gia' due archi e gli archi gia' estratti).
			//
			for (count1 = 0; count1 < tot; count1++) {
				if (min > d[count1]) {
					tmp = adjMatrix[count1];
					for (count2 = count1 + 1; count2 < totCities; count2++) {
						if ((min > tmp[count2])
								&& (MFSetArray[count2].adj2 == null)
								&& (c[count1] != count2)) {
							min = tmp[count2];
							tmp1 = count1;
							tmp2 = count2;
						}
					}

					//
					// Aggiorno il "limite inferiore" delle distanze riferite
					// a questo nodo.
					//
					d[count1] = min;
				}
			}

			//
			// Successivamente per il nodo tmp1 non dovra' piu' essere preso
			// in considerazione il nodo tmp2. Vale anche per il caso che il
			// nodo tmp2 non vada bene e generi un sottocircuito (anche se viene
			// perso il nodo che era memorizzato in precedenza in c[tmp1] non
			// ci sono problemi, se la connessione di tmp1 con tmp2 genera un
			// sottocircuito questo vuol dire che il precedente nodo contenuto
			// in c[tmp1] deve avere per forza 2 altri nodi connessi, percio'
			// verrebbe scartato lo stesso).
			//
			c[tmp1] = tmp2;
			elem1 = MFSetArray[tmp1];
			elem2 = MFSetArray[tmp2];

			//
			// Se non vado a creare un sotto-circuito (me ne accorgo grazie
			// al MFSet) allora connetto il nuovo arco ad una delle catene
			// attualmente in costruzione.
			//
			if (MFSet.merge(elem1, elem2) || (index == tot)) {
				index++;
				if (elem1.adj1 != null) {
					elem1.adj2 = elem2;

					//
					// Se il nodo ha gia' due elementi adiacenti, significa che
					// non deve piu' essere controllato.
					//
					d[tmp1] = Double.MAX_VALUE;
				} else
					elem1.adj1 = elem2;
				if (elem2.adj1 != null) {
					elem2.adj2 = elem1;

					//
					// Se il nodo ha gia' due elementi adiacenti, significa che
					// non deve piu' essere controllato.
					//
					d[tmp2] = Double.MAX_VALUE;
				} else
					elem2.adj1 = elem1;

				//
				// Se stiamo eseguendo l'animazione dell'algoritmo.
				//
				if (isAnimated()) {
					tspFrame.workArea.enableExtraEdges(true);
					tspFrame.workArea.addExtraEdge(elem1.c, elem2.c);
					tspFrame.workArea.repaintOpt(false);
					pausing();
					tspFrame.workArea.enableExtraEdges(false);
					tspFrame.workArea.addEdge(elem1.c, elem2.c);
				}
			}
		}
		if (!isStopped()) {

			//
			// Scorro la lista degli elementi del MFSet collegati tra di loro
			// con
			// una lista circolare "impropria", cioè ogni elemento è collegato
			// con il successivo e il precedente, ma non necessariamente il
			// successivo è adj1 oppure adj2. Si rende quindi necessario
			// controllare
			// ogni volta che ci spostiamo di nodo se al passo successivo
			// ritorniamo
			// al nodo da cui eravamo venuti.
			//
			elem1 = MFSetArray[0];
			elem2 = elem1;
			for (count1 = 0; count1 < totCities; count1++) {
				addTourNode(elem1.c);
				if (!elem2.equals(elem1.adj1)) {
					elem2 = elem1;
					elem1 = elem1.adj1;
				} else {
					elem2 = elem1;
					elem1 = elem1.adj2;
				}
			}
		}
	}

	/**
	 * Crea il "Convex Hull" (il piu' piccolo poligono convesso contenente tutti
	 * i nodi dell'istanza) utilizzando la "Jarvis march". Per l'algoritmo
	 * Jarvis march vedi: "Introduction to Algorithms", Cormen, Leiserson,
	 * Rivest. Pag. 905~907
	 */
	private final int convexHull() {
		int count;
		Node secondTour = null;
		Node curTour = null;
		Node.firstNode = new Node(citiesVector[0]);
		double min = Node.firstNode.y;
		Node minNode = Node.firstNode;
		Node curNode = Node.firstNode;

		//
		// Crea la lista dei nodi liberi mentre cerca
		// il nodo piu' in basso (minima y).
		//
		for (count = 1; count < totCities; count++) {
			curNode.next = new Node(citiesVector[count]);
			curNode.next.prev = curNode;
			curNode = curNode.next;
			if (curNode.y < min) {
				min = curNode.y;
				minNode = curNode;
			}
		}
		Node.firstNode.prev = curNode;
		curNode.next = Node.firstNode;
		curTour = null;
		int tot = totCities;

		//
		// Il nodo piu' in basso e' il primo nodo del "Convex Hull".
		//
		double x = minNode.x;
		double y = minNode.y;
		Node.firstTour = minNode;

		//
		// Produce il resto del "Convex Hull" (tutti gli altri nodi
		// che formano il poligono).
		//
		do {
			curNode = Node.firstNode;
			minNode = curNode.next;
			do {

				//
				// Controlla il coefficiente angolare.
				//
				if ((curNode.x - x) * (minNode.y - y) - (minNode.x - x)
						* (curNode.y - y) < 0)
					minNode = curNode;
				curNode = curNode.next;
			} while (curNode != Node.firstNode);

			//
			// Elimina il nodo trovato dalla lista dei nodi liberi.
			//
			Node.delete(minNode);

			//
			// Lo inserisce nella lista dei nodi formanti il tour.
			//
			minNode.prev = curTour;
			if (curTour != null)
				curTour.next = minNode;
			else
				secondTour = minNode;
			curTour = minNode;
			x = curTour.x;
			y = curTour.y;

			//
			// Se stiamo eseguendo l'animazione dell'algoritmo.
			//
			if (isAnimated()) {
				if (curTour.prev != null)
					tspFrame.workArea.addEdge(curTour.prev.c, curTour.c);
				else
					tspFrame.workArea.addEdge(Node.firstTour.c, curTour.c);
				pausing();
			}
			tot--;
		} while ((minNode != Node.firstTour) && !isStopped());
		Node.firstTour.next = secondTour;
		return tot;
	}

	/**
	 * [Cheapest Insertion] A partire dal "Convex Hull" via via si connettono
	 * nuovi nodi ai vertici del poligono prendendo sempre quelli che
	 * incrementano in misura minore la lunghezza complessiva del circuito.
	 */
	private final void cheapestInsertion() {
		int tot = convexHull();
		Node curTour = null;
		Node curNode = null;
		Node minTour = null;
		Node minNode = null;
		Node minNodeT = null;
		double[] tmpAdj1;
		double[] tmpAdj2;
		double minD, minE, tmpD, tmpE;

		//
		// Scorro gli archi del "Convex Hull", per ognuno trovo il nodo
		// (non ancora connesso) che aggiunto al tour (sostituendo il
		// vecchio arco con due nuovi che partono dal nodo in questione
		// verso i vertici del vecchio arco) ne incrementa meno la dimensione
		// complessiva. Fra tutti questi connetto solo il minimo. Viene
		// ripetuta questa operazione fino a che non ho connesso tutti i nodi.
		//
		while ((tot-- > 0) && !isStopped()) {
			curTour = Node.firstTour;
			minE = Double.MAX_VALUE;

			//
			// Trova il minimo fra i minimi di tutti gli archi.
			//
			do {
				minD = curTour.dist;

				//
				// Se il minimo per tale arco non è stato ancora trovato,
				// oppure deve essere ricalcolato.
				//
				if (minD == Double.MAX_VALUE) {
					tmpAdj1 = adjMatrix[curTour.index];
					tmpAdj2 = adjMatrix[curTour.next.index];
					tmpE = tmpAdj2[curTour.index];
					curNode = Node.firstNode;

					//
					// Trova il nodo che connesso ai due nodi alle estremità
					// dell'arco corrente da' il minimo incremento al circuito
					// corrente.
					//
					do {
						tmpD = tmpAdj1[curNode.index] + tmpAdj2[curNode.index] - tmpE;
						if (tmpD < minD) {
							minD = tmpD;
							minNodeT = curNode;
						}
						curNode = curNode.next;
					} while (curNode != Node.firstNode);
					curTour.dist = minD;
					curTour.node = minNodeT;
				}
				if (minD < minE) {
					minE = minD;
					minTour = curTour;
					minNode = curTour.node;
				}
				curTour = curTour.next;
			} while (curTour != Node.firstTour);

			//
			// Se stiamo eseguendo l'animazione dell'algoritmo.
			//
			if (isAnimated()) {
				tspFrame.workArea.addOptEdges(minNode.c, minTour.c, minNode.c, minTour.next.c);
				pausing();
				tspFrame.workArea.clearEdge(minTour.c, minTour.next.c);
				pausing();
				tspFrame.workArea.enableExtraEdges(false);
				tspFrame.workArea.addEdge(minNode.c, minTour.c);
				tspFrame.workArea.addEdge(minNode.c, minTour.next.c);
			}

			//
			// Resetta i minimi degli archi che hanno come nodo piu' vicino
			// quello che corrisponde al nodo appena estratto.
			//
			curNode = Node.firstTour;
			do {
				if (curNode.node.equals(minNode)) {
					curNode.dist = Double.MAX_VALUE;
					curNode.node = null;
				}
				curNode = curNode.next;
			} while (curNode != Node.firstTour);

			//
			// Esclude dalla lista dei nodi non connessi il nodo trovato.
			//
			Node.delete(minNode);

			//
			// Inserisce nella lista dei nodi formanti il tour il
			// nodo appena estratto.
			//
			minNode.prev = minTour;
			minNode.next = minTour.next;
			minTour.next = minNode;
			minTour.next.prev = minNode;
		}

		//
		// Scorre la lista dei nodi appartenenti al tour,
		// inserendoli nel vettore che contiene la soluzione
		// corrente al tsp.
		//
		curTour = Node.firstTour;
		if (!isStopped())
			do {
				addTourNode(curTour.c);
				curTour = curTour.next;
			} while (curTour != Node.firstTour);
	}

	/**
	 * [Farthest Insertion] A partire dal "Convex Hull" via via si connettono
	 * nuovi nodi ai vertici del poligono prendendo quelli la cui distanza
	 * minima dal poligono e' massima.
	 */
	private final void farthestInsertion() {
		int tot = convexHull();
		Node curTour = null;
		Node maxTour = null;
		Node curNode = null;
		Node maxNode = null;
		double[] tmpAdj1;
		double minD, maxE, tmpE;

		//
		// Trovo il nodo libero la cui minima distanza dai nodi dai nodi
		// circuito corrente e' massima, quindi lo connetto in modo da
		// incrementare il meno possibile la lunghezza complessiva del
		// circuito.
		//
		while ((tot-- > 0) && !isStopped()) {
			curNode = Node.firstNode;
			maxE = Integer.MIN_VALUE;

			//
			// Trova il nodo libero la cui minima distanza dai nodi
			// del circuito corrente e' massima.
			//
			do {
				minD = curNode.dist;

				//
				// Inizializza le distanze minime la prima volta.
				//
				if (minD == Double.MAX_VALUE) {
					tmpAdj1 = adjMatrix[curNode.index];
					curTour = Node.firstTour;

					//
					// Trova il nodo appartente al circuito il piu' vicino al
					// nodo corrente.
					//
					do {
						if (tmpAdj1[curTour.index] < minD)
							minD = tmpAdj1[curTour.index];
						curTour = curTour.next;
					} while (curTour != Node.firstTour);
					curNode.dist = minD;
				}
				if (minD > maxE) {
					maxE = minD;
					maxNode = curNode;
				}
				curNode = curNode.next;
			} while (curNode != Node.firstNode);
			curTour = Node.firstTour;
			minD = Double.MAX_VALUE;

			//
			// Trova i due nodi (estremita' di un arco gia' esistente) a cui
			// connettere il nodo trovato, in modo da incrementare il meno
			// possibile la lunghezza complessiva del circuito.
			//
			do {
				tmpE = adjMatrix[curTour.index][maxNode.index]
						+ adjMatrix[curTour.next.index][maxNode.index]
						- adjMatrix[curTour.index][curTour.next.index];
				if (minD > tmpE) {
					minD = tmpE;
					maxTour = curTour;
				}
				curTour = curTour.next;
			} while (curTour != Node.firstTour);

			//
			// Se stiamo eseguendo l'animazione dell'algoritmo.
			//
			if (isAnimated()) {
				tspFrame.workArea.addOptEdges(maxNode.c, maxTour.c, maxNode.c,
						maxTour.next.c);
				pausing();
				tspFrame.workArea.clearEdge(maxTour.c, maxTour.next.c);
				pausing();
				tspFrame.workArea.enableExtraEdges(false);
				tspFrame.workArea.addEdge(maxNode.c, maxTour.c);
				tspFrame.workArea.addEdge(maxNode.c, maxTour.next.c);
			}

			//
			// Aggiorna le distanze minime dei nodi liberi
			// in base al nodo trovato.
			//
			curNode = Node.firstNode;
			do {
				if (adjMatrix[curNode.index][maxNode.index] < curNode.dist)
					curNode.dist = adjMatrix[curNode.index][maxNode.index];
				curNode = curNode.next;
			} while (curNode != Node.firstNode);

			//
			// Estrae dalla lista dei nodi non connessi il nodo trovato.
			//
			Node.delete(maxNode);

			//
			// Inserisce nella lista dei nodi formanti il tour il
			// nodo appena estratto.
			//
			maxNode.prev = maxTour;
			maxNode.next = maxTour.next;
			maxTour.next = maxNode;
			maxTour.next.prev = maxNode;
		}

		//
		// Scorre la lista dei nodi appartenenti al tour,
		// inserendoli nel vettore che contiene la soluzione
		// corrente al tsp.
		//
		curTour = Node.firstTour;
		if (!isStopped())
			do {
				addTourNode(curTour.c);
				curTour = curTour.next;
			} while (curTour != Node.firstTour);
	}

	/**
	 * [Random Insertion] A partire dal "Convex Hull" via via si connettono
	 * nuovi nodi ai vertici del poligono prendendoli semplicemente a caso.
	 */
	private final void randomInsertion() {
		int tot = convexHull();
		Node curTour = null;
		Node maxTour = null;
		Node curNode = null;
		Node maxNode = null;
		double minD, tmpE;
		int pos;
		Random randomNode = new Random();

		//
		// Prendo un nodo a caso tra quelli liberi, quindi lo connetto
		// in modo da incrementare il meno possibile la lunghezza
		// complessiva del circuito.
		//
		while ((tot-- > 0) && !isStopped()) {
			curNode = Node.firstNode;
			if (tot > 0) {
				pos = randomNode.nextInt() % tot;

				//
				// Trova il nodo libero scelto a caso percorrendo
				// la lista dei nodi liberi.
				//
				do {
					curNode = curNode.next;
				} while ((pos-- > 0) && (curNode != Node.firstNode));
			}
			maxNode = curNode;
			curTour = Node.firstTour;
			minD = Double.MAX_VALUE;

			//
			// Trova i due nodi (estremita' di un arco gia' esistente) a cui
			// connettere il nodo trovato, in modo da incrementare il meno
			// possibile la lunghezza complessiva del circuito.
			//
			do {
				tmpE = adjMatrix[curTour.index][maxNode.index]
						+ adjMatrix[curTour.next.index][maxNode.index]
						- adjMatrix[curTour.index][curTour.next.index];
				if (minD > tmpE) {
					minD = tmpE;
					maxTour = curTour;
				}
				curTour = curTour.next;
			} while (curTour != Node.firstTour);

			//
			// Se stiamo eseguendo l'animazione dell'algoritmo.
			//
			if (isAnimated()) {
				tspFrame.workArea.addOptEdges(maxNode.c, maxTour.c, maxNode.c,
						maxTour.next.c);
				pausing();
				tspFrame.workArea.clearEdge(maxTour.c, maxTour.next.c);
				pausing();
				tspFrame.workArea.enableExtraEdges(false);
				tspFrame.workArea.addEdge(maxNode.c, maxTour.c);
				tspFrame.workArea.addEdge(maxNode.c, maxTour.next.c);
			}

			//
			// Aggiorna le distanze minime dei nodi liberi
			// in base al nodo trovato.
			//
			curNode = Node.firstNode;
			do {
				if (adjMatrix[curNode.index][maxNode.index] < curNode.dist)
					curNode.dist = adjMatrix[curNode.index][maxNode.index];
				curNode = curNode.next;
			} while (curNode != Node.firstNode);

			//
			// Estrae dalla lista dei nodi non connessi il nodo trovato.
			//
			Node.delete(maxNode);

			//
			// Inserisce nella lista dei nodi formanti il tour il
			// nodo appena estratto.
			//
			maxNode.prev = maxTour;
			maxNode.next = maxTour.next;
			maxTour.next = maxNode;
			maxTour.next.prev = maxNode;
		}

		//
		// Scorre la lista dei nodi appartenenti al tour,
		// inserendoli nel vettore che contiene la soluzione
		// corrente al tsp.
		//
		curTour = Node.firstTour;
		if (!isStopped())
			do {
				addTourNode(curTour.c);
				curTour = curTour.next;
			} while (curTour != Node.firstTour);
	}

	/**
	 * [Nearest Neighbor] Crea una soluzione al TSP partendo da un nodo
	 * iniziale, connettendo sempre il nodo piu' vicino all'ultimo nodo
	 * inserito.
	 */
	private final void nearestNeighbor() {
		int count;
		double dist, tmp;
		int tot = totCities;

		//
		// Utilizza come nodo di partenza quello impostato nel pannello
		// delle proprieta' di questa euristica (per default e' il primo
		// in ordine di inserimento).
		//
		int next = getStartCity();
		int nextc = next;
		City cur = null;
		City prev = null;
		int[] connected = new int[totCities];
		for (count = 0; count < tot; count++)
			connected[count] = count;

		//
		// Utilizza come nodo di partenza il nodo impostato dall'utente.
		//
		while ((tot > 0) && !isStopped()) {
			cur = citiesVector[next];
			addTourNode(cur);

			//
			// Se stiamo eseguendo l'animazione dell'algoritmo.
			//
			if (isAnimated()) {
				if (prev != null) {
					tspFrame.workArea.enableExtraEdges(true);
					tspFrame.workArea.addExtraEdge(prev, cur);
					tspFrame.workArea.repaintOpt(false);
					pausing();
					tspFrame.workArea.enableExtraEdges(false);
					tspFrame.workArea.addEdge(prev, cur);
				}
				prev = cur;
			}
			connected[nextc] = connected[--tot];
			dist = Double.MAX_VALUE;
			for (count = 0; count < tot; count++) {
				tmp = adjMatrix[next][connected[count]];
				if (tmp < dist) {
					dist = tmp;
					nextc = count;
				}
			}
			next = connected[nextc];
		}
	}

	/**
	 * [Pre-Order visit of Minimum Spanning Tree] Ricerca di una soluzione
	 * approssimata con l'ausilio del Minimum Spanning Tree. Si ottiene
	 * percorrendo in maniera anticipata (in tempo polinomiale) il MST, con uno
	 * scarto max dalla soluzione ottima del 100%. Per l'algoritmo di Prim vedi:
	 * "Introduction to Algorithms", Cormen, Leiserson, Rivest. Pag. 505~510
	 */
	private final void mstPreOrder() {
		int count, indexMin, indexTmp;
		double wid, min;
		int tot = totCities;
		nodeMST h1, h2, first;
		nodeMST[] h = new nodeMST[totCities];

		//
		// Inizializza l'elenco dei nodi da vistare.
		//
		for (count = 0; count < totCities; count++)
			h[count] = new nodeMST(citiesVector[count]);

		//
		// Utilizza come nodo di partenza quello impostato dall'utente.
		//
		indexTmp = getStartCity();
		first = h[indexTmp];

		//
		// Attribuisce al primo elemento priorita' massima
		// ed a tutti gli altri priorita' minima (massimo intero possibile).
		//
		first.key = 0;

		//
		// Calcola il Minimum Spanning Tree con l'algoritmo di Prim
		// (ottimizzato per un grafo completo) in O(n^2).
		//
		while (tot > 0) {
			h1 = h[indexTmp];

			//
			// Estrae il nodo piu' vicino al nodo corrente
			//
			h[indexTmp] = h[--tot];

			//
			// Indice (riferito all'elenco totale dei nodi) dell'ultimo
			// nodo estratto.
			//
			indexMin = h1.c.num;

			//
			// Cura il collegamento tra i nodi fratelli e con il nodo padre.
			//
			h2 = h1.father;
			if (h2 != null) {
				if (isAnimated())
					tspFrame.workArea.addExtraEdge(h1.c, h2.c);
				if (h2.lastSon != null)
					h2.lastSon.brother = h1;
				else
					h2.firstSon = h1;
				h2.lastSon = h1;
			}
			indexTmp = indexMin;
			min = Double.MAX_VALUE;
			for (count = 0; count < tot; count++) {
				h2 = h[count];
				wid = adjMatrix[h2.c.num][indexMin];
				if (wid < h2.key) {
					h2.father = h1;
					h2.key = wid;
				}
				if (h2.key < min) {
					indexTmp = count;
					min = h2.key;
				}
			}
		}

		//
		// Se siamo l'animazione è attiva disegna il MST.
		//
		if (isAnimated())
			tspFrame.workArea.repaint();

		//
		// Produce soluzione approssimata visitando il MST
		//
		preOrderVisitMST(first);
	}

	/**
	 * Visita (ricorsivamente) in maniera anticipata il Minum-Spanning-Tree
	 * producendo il vettore degli archi che rappresentano la soluzione
	 * (approssimata) del TSP.
	 */
	private final void preOrderVisitMST(nodeMST h1) {
		//
		// Aggiunge un nodo al circuito.
		//
		addTourNode(h1.c);
		if (isAnimated()) {
			tspFrame.workArea.addEdge(prevTourNode(h1.c), h1.c);
			pausing();
		}

		//
		// Se il nodo non è una foglia...
		//
		if ((h1.firstSon != null) && !isStopped()) {
			nodeMST h2 = h1.firstSon;

			//
			// Finche' non ho considerato tutti i fratelli...
			//
			while ((h2.brother != null) && !isStopped()) {
				preOrderVisitMST(h2);
				h2 = h2.brother;
			}
			if (!isStopped())
				preOrderVisitMST(h2);
		}
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
			case GREEDY:
				greedy();
				break;
			case CHEAPEST_INSERTION:
				cheapestInsertion();
				break;
			case RANDOM_INSERTION:
				randomInsertion();
				break;
			case FARTHEST_INSERTION:
				farthestInsertion();
				break;
			case MST_PREORDER:
				mstPreOrder();
				break;
			case NEAREST_NEIGHBOR:
				nearestNeighbor();
				break;
		}
		tspFrame.workArea.enableExtraEdges(false);
		if (isStopped()) {
			tspFrame.undo();
			if (isAnimated())
				tspFrame.workArea.repaint(false, true);
			setStopped(false);
		} else {
			tspFrame.chgTour(true);
			tspFrame.workArea.repaint(false, true);
		}
		tspFrame.tspArea.setTourLength();
		tspFrame.tspArea.setLowerBound();
		tspFrame.statusArea.setStatus("");
		tspFrame.enableEditing(true);
		tspFrame.tspArea.enableAnimationCheckbox(true);

		//
		// Cosi' il garbage collector puo' disporre della memoria
		// occupata da questo thread.
		//
		tspFrame.tspArea.computeThread = null;
	}

	/**
	 * Classe che contiene i nodi del MST utilizzato dal metodo mstPreOrder().
	 *   In modo da poter essere visitati in maniera anticipata sono stati 
	 *   strutturati cosi :
	 *    
	 *              null
	 *               ^
	 *               |(father)
	 *           [ROOT]------------------------------------.
	 *            |  ^                                     |
	 *  (firstSon)|  |(father)                             |(lastSon)
	 *            v  |                                     v
	 *           [LEAF] ---------> [..] ---------> ...... [..] ---------> null
	 *            |  |  (brother)       (brother)              (brother)
	 *            |  | 
	 *            |  '---.
	 *  (firstSon)|      |(lastSon)
	 *            v      v
	 *           null   null
	 * 
	 */
	class nodeMST {
		/**
		 * Citta'
		 */
		public City c;

		/**
		 * Nodo (citta') padre.
		 */
		public nodeMST father;

		/**
		 * Primo nodo figlio.
		 */
		public nodeMST firstSon;

		/**
		 * Ultimo nodo figlio.
		 */
		public nodeMST lastSon;

		/**
		 * Prossimo nodo fratello.
		 */
		public nodeMST brother;

		/**
		 * Peso del nodo.
		 */
		public double key;

		/**
		 * Costruttore classe.
		 */
		public nodeMST(City ci) {
			c = ci;
			father = null;
			firstSon = null;
			brother = null;
			key = Double.MAX_VALUE;
		}
	}

	
}

/**
 * Classe che contiene gli elementi del Merge-Find Set usati dal metodo
 * Greedy().
 */
class MFSet {
	/**
	 * Elemento dell'insieme (un nodo o citta').
	 */
	public City c;

	/**
	 * Elementi adiacenti.
	 */
	public MFSet adj1, adj2;

	/**
	 * Nodo padre
	 */
	private MFSet father;

	/**
	 * Dimensione dell'albero.
	 */
	private int dim;

	/**
	 * Costruttore della classe
	 */
	public MFSet(City ci) {
		c = ci;
		adj1 = null;
		adj2 = null;
		father = null;
		dim = 1;
	}

	/**
	 * Fonde due sottoinsiemi.
	 */
	public final static boolean merge(MFSet elem1, MFSet elem2) {
		//
		// Controlla se due elementi del Merge-Find Set appartengono
		// allo stesso sottoinsieme.
		//
		while (elem1.father != null)
			elem1 = elem1.father;
		while (elem2.father != null)
			elem2 = elem2.father;

		//
		// Se non è vero, li fonde.
		//
		if (elem1 != elem2) {

			//
			// Aggiorna le dimensioni dei sottoinsiemi.
			//
			if (elem1.dim < elem2.dim) {
				elem1.father = elem2;
				elem2.dim += elem1.dim;
			} else {
				elem2.father = elem1;
				elem1.dim += elem2.dim;
			}
			return true;
		} else
			return false;
	}
}

/**
 * Nodo utilizzato per l'euristica Convex-hull.
 */
class Node {
	/**
	 * Primo nodo della lista dei nodi connessi.
	 */
	public static Node firstTour;

	/*
	 * Primo nodo della lista dei nodi ancora liberi.
	 */
	public static Node firstNode;

	/**
	 * Nodo precedente.
	 */
	public Node prev;

	/**
	 * Nodo successivo.
	 */
	public Node next;

	/**
	 * Coordinata x.
	 */
	public final double x;

	/**
	 * Coordinata y.
	 */
	public final double y;

	/**
	 * Citta a cui si riferisce questo nodo.
	 */
	public final City c;

	/**
	 * Indice della citta a cui si riferisce questo nodo.
	 */
	public final int index;

	/**
	 * Nodo piu' vicino.
	 */
	public Node node;

	/**
	 * Distanza dall'arco uscente da questo nodo al nodo piu' vicino.
	 */
	public double dist;

	/**
	 * Costruttore classe.
	 */
	public Node(City ci) {
		c = ci;
		prev = null;
		next = null;
		index = ci.num;
		dist = Double.MAX_VALUE;
		node = null;
		x = ci.x;
		y = ci.y;
	}

	/*
	 * Esclude dalla lista dei nodi liberi il nodo passato per parametro.
	 */
	public static void delete(Node n) {
		if (firstNode == n)
			firstNode = firstNode.next;
		n.prev.next = n.next;
		n.next.prev = n.prev;
	}
}