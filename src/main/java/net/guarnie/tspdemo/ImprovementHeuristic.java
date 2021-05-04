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

/**
*    Classe che contiene i metodi per implementare i seguenti
*    algoritmi che partendo da un circuito cercano di "migliorarlo"
*    fin quanto e' possibile:
*
*    - 2-Change (2-Opt)
*    - 3-change (3-Opt)
*/
public class ImprovementHeuristic extends Heuristic {

  /**
	 * Codici di riconoscimento per gli algoritmi di ricerca locale.
	 */
	final static int TWO_CHANGE = 6;
	final static int THREE_CHANGE = 7;
  final static int HK_LBOUND = 8;

	final static int[] LBOUND_COD = { HK_LBOUND };
  final static int[] LOCAL_COD = { TWO_CHANGE, THREE_CHANGE };

  /**
  *    Costruttore della classe.
  */
  public ImprovementHeuristic(TspFrame tFrame, int eID) {
    super(tFrame, eID, -1, -1, -1, null);
  }


  /**
  *    Algoritmo "2-Opt" dedicato alla ricerca di una soluzione più
  *    "raffinata" rispetto alla soluzione approssimata di partenza.
  *    Dati 2 archi (non adiacenti) e i rispettivi 4 nodi alle estremità
  *    verifica se è possibile riconnettere questi ultimi in modo da far
  *    si che il circuito di partenza rimanga tale (cioé che tutti i nodi
  *    continuino ad essere connessi) e diminuire la lunghezza totale del
  *    circuito.

              (t1)-X->(t2)
              ^  \    /  :  ^
              :   \  /   :  :
              :    \/    X  :
              :    /\    :  : (Ordine invertito)
              :   /  \   :  :
              :  v    v  v  :
              (t4)<-X-(t3)

  */
  private void twoChange() {
    double dt1t2;
    int ind, count;
    City t1, t2, t3, t4;
    Neighbour n;
    final int NSIZE = tourVector[0].neighboursSize;
    boolean tourChanged, tourNotEnd;
    boolean last = false;
    do {
      count = startCity;
      tourChanged = false;
      tourNotEnd = false;
      do {
      	if (isStopped()) return;
        t1 = tourVector[count];
        t2 = nextTourNode(t1);
        dt1t2 = adjMatrix[t1.num][t2.num];
        ind = 0;
        do {
          n = t2.neighbours[ind++];
          if (last) {
            t3 = n.c;
            t4 = nextTourNode(t3);
          }
          else {
            t4 = n.c;
            t3 = prevTourNode(t4);
          }
        } while ((ind < NSIZE) && (n.dist < dt1t2) && (t4.equals(t1) || 
            t3.equals(t2) || 
            (adjMatrix[t2.num][t4.num] + adjMatrix[t1.num][t3.num] >= 
            dt1t2 + adjMatrix[t3.num][t4.num])
          ));
        if ((n.dist >= dt1t2) || (ind >= NSIZE)) {
          count = (count + 1) % totCities;
          tourNotEnd = (count != startCity);
        }
        else {
          reverseOrder(t2.tourIndex, t3.tourIndex);
          tourSize += adjMatrix[t2.num][t4.num] + adjMatrix[t1.num][t3.num] - 
            dt1t2 - adjMatrix[t3.num][t4.num];
          tourChanged = true;
          
          //
          //  Animazione.
          //
          if (isAnimated()) {
            tspFrame.workArea.addOptEdges(t1, t3, t2, t4);
            pausing();
            if (isStopped()) return;
            tspFrame.workArea.repaintOpt(true);
            pausing();
            if (isStopped()) return;
            tspFrame.tspArea.setTourLength();
            tspFrame.workArea.enableExtraEdges(false);
            tspFrame.workArea.repaintOpt(false);
          }
        }
      } while (tourNotEnd);
      if (!last && !tourChanged) {
        last = true;
        tourChanged = true;
      }
    } while (tourChanged);
  }


  /**
  *    Algoritmo "3-Opt" dedicato alla ricerca di una soluzione piu'
  *    "raffinata" rispetto alla soluzione approssimata di partenza.
  *    Dati 3 archi (non adiacenti) e i rispettivi 6 nodi alle estremita'
  *    verifica se e' possibile riconnettere questi ultimi in modo da far
  *    si che il circuito di partenza rimanga tale (cioe' che tutti i nodi
  *    continuino ad essere connessi) e diminuire la lunghezza totale
  *    del circuito.

            [CASO 1]                        [CASO 2]

                                                    <......
      ..>(t1)--X-->(t2)....          ..>(t1)--X-->(t2).... :
      :    |         ^    :          :    |         |    X : (rev. order)
      :    |         |    v        (t4)<--|----------    : :
    (t6)<--|---------|--(t3)         ^    |              v :
      ^    |         |    |          |    |         ---->(t5)
      |    |         |    X          X    |         |    |
      X    |         |    |          |    |         |    X
      |    |         |    v        (t3)---|----------    |
    (t5)<---         ---(t4)         ^    |              v
     : ^                 : ^         :    -------------->(t6)
     : :.........X.......: :         :...................:
     :.....................:
            (rev. order)

  */
  private void threeChange() {
    double dt1t2, dt3t4, dt5t6, dt2t4, edge3dst;
    int ind1, ind2;
    int count;
    City t1, t2, t3, t4, t5, t6;
    Neighbour n;
    final int NSIZE = tourVector[0].neighboursSize;
    boolean tourChanged, searchNotEnd, between, edgeAdj, tourNotEnd;
    do {
      count = startCity;
      tourChanged = false;
      tourNotEnd = false;
      do {
        t1 = tourVector[count];
        t2 = nextTourNode(t1);
        dt1t2 = adjMatrix[t1.num][t2.num];
        ind1 = 0;
        do {
          if (isStopped()) return;
          do {
            n = t2.neighbours[ind1++];
            t3 = prevTourNode(n.c);
            searchNotEnd = (ind1 < NSIZE) && (n.dist < dt1t2);
          } while (searchNotEnd && t3.equals(t2));
          if (searchNotEnd) {
            t4 = n.c;
            dt3t4 = adjMatrix[t3.num][t4.num];
            dt2t4 = adjMatrix[t2.num][t4.num];
            ind2 = 0;
            do {
              n = t3.neighbours[ind2++];
              if (t2.tourIndex < t3.tourIndex) between = 
                (n.c.tourIndex < t3.tourIndex) && (n.c.tourIndex > t2.tourIndex);
              else between = 
                (n.c.tourIndex < t3.tourIndex) || (n.c.tourIndex > t2.tourIndex);
              if (between) {
                t5 = n.c;
                t6 = nextTourNode(t5);
                edge3dst = adjMatrix[t6.num][t1.num];
                
                //
                //  t5 non puo' essere t2 perche' altrimenti non starebbe 
                //  strettamente tra t2 e t3.
                //
                edgeAdj = t6.equals(t3); 
              }
              else {
                t6 = n.c;
                t5 = prevTourNode(t6);
                edge3dst = adjMatrix[t5.num][t1.num];
                edgeAdj = t5.equals(t4) || t6.equals(t1) || 
                  t6.equals(t4) || t6.equals(t2);
              }
              dt5t6 = adjMatrix[t5.num][t6.num];
              searchNotEnd = (ind2 < NSIZE) && (n.dist + dt2t4 < dt1t2 + dt3t4);
            } while (searchNotEnd && 
                (edgeAdj || (dt2t4 + n.dist + edge3dst >= dt1t2 + dt3t4 + dt5t6))
              );
            if (searchNotEnd) {
              
              //
              //  Caso 2.
              //
              if (between) {
                reverseOrder(t2.tourIndex, t3.tourIndex);
                reverseOrder(t3.tourIndex, t6.tourIndex);
              }
              
              //
              //  Caso 1
              //
              else {
                reverseOrder(t2.tourIndex, t5.tourIndex);
                reverseOrder(t3.tourIndex, t2.tourIndex);
              }
              
              tourSize += dt2t4 + n.dist + edge3dst - (dt1t2 + dt3t4 + dt5t6);
              tourChanged = true;
              
              //
              //  Animazione
              //
              if (isAnimated()) {
                if (between) tspFrame.workArea.addOptEdges(t1, t6, t2, t4, t3, t5);
                else tspFrame.workArea.addOptEdges(t1, t5, t4, t2, t3, t6);
                pausing();
                if (isStopped()) return;
                tspFrame.workArea.repaintOpt(true);
                pausing();
                if (isStopped()) return;
                tspFrame.tspArea.setTourLength();
                tspFrame.workArea.enableExtraEdges(false);
                tspFrame.workArea.repaintOpt(false);
              }
            }
            searchNotEnd = !searchNotEnd;
          }
          else {
            count = (count + 1) % totCities;
            tourNotEnd = (count != startCity);
          }
        } while (searchNotEnd);
      } while (tourNotEnd);
    } while (tourChanged);
  }


  /**
  *  Scambia i due archi che hanno i nodi alle estremita' 
  *  indicati da: ind1..ind4 ed inoltre inverte il senso del circuito 
  *  nel percorso tra ind2 e ind3.
  */
  private void reverseOrder(int ind1, int ind2) {
    City tmp;
    int dim1 = (ind2 - ind1 + 1 + ((ind1 < ind2)? 0 : totCities)) >> 1;
    int index1 = ind1;
    int index2 = ind2;
    while (dim1-- > 0) {
      tmp = tourVector[index1];
      tourVector[index1] = tourVector[index2];
      tourVector[index2] = tmp;
      tmp.tourIndex = index2;
      tourVector[index1].tourIndex = index1++;
      index1 %= totCities;
      if (index2 > 0) index2--;
      else index2 = totCities - 1;
    }
  }

  /**
  *   Metodo necessario all'esecuzione di questo thread.
  */
  public void run() {
        
    //  Setto la priorita' al minimo dato che questo thread
    //  e' decisamente CPU-INTENSIVE.
    setPriority(MIN_PRIORITY);
    
    switch (execID) {
      case HK_LBOUND:
				lowerBound = heldKarpLowerBound();
				break;
      case TWO_CHANGE:
        twoChange();
        break;
      case THREE_CHANGE:
        threeChange();
        break;
    }
    tspFrame.workArea.enableExtraEdges(false);
    tspFrame.chgTour(true);
    tspFrame.tspArea.setTourLength();
    tspFrame.tspArea.setLowerBound();
    tspFrame.statusArea.setStatus("");
    tspFrame.workArea.repaint(false, true);
    tspFrame.enableEditing(true);
    tspFrame.tspArea.enableAnimationCheckbox(true);

    //  Cosi' il garbage collector puo' disporre della memoria
    //  occupata da questo thread.
    tspFrame.tspArea.computeThread = null;
  }


  /**
	 * Classe che contiene i nodi del MST utilizzato dal metodo
	 * heldKarpLowerBound().
	 */
	class nodeLB {
		/**
		 * Nodo padre.
		 */
		public nodeLB father;

		/**
		 * Grado del nodo.
		 */
		public int degree;

		/**
		 * Grado precedente del nodo.
		 */
		public int oldDegree;

		/**
		 * Indice della citta' a cui ci si riferisce.
		 */
		public final int index;

		/**
		 * Citta' a cui ci si riferisce.
		 */
		public final City c;

		/**
		 * Peso del nodo.
		 */
		public double key;

		/**
		 * Costruttore classe.
		 */
		public nodeLB(City ci) {
			c = ci;
			index = ci.num;
			father = null;
			degree = 0;
			oldDegree = 0;
			key = Double.POSITIVE_INFINITY;
		}
	}

	/**
	 * [Held-Karp lower-bound] Vedi: "The Traveling Salesman, computational
	 * solutions for TSP Applications", Gerhard Reinelt, Lecture Notes in
	 * Computer Science 840. Pag. 176~179.
	 */
	private final double heldKarpLowerBound() {
		final double LAMBDA = 0.98;
		final int MAX_ITERATIONS = 300;
		int count, indexTmp, indexMin, tot, count2;
		int k = 0;
		double min, dimMST, piTot;
		double dimMax1Tree = 0;
		double lowBound = 0;
		double t = 0;
		double wid = 0;
		nodeLB h1;
		nodeLB h2 = null;
		nodeLB h3 = null;
		nodeLB node1 = null;
		nodeLB node2 = null;
		nodeLB[] h = new nodeLB[totCities];
		double[] pi = new double[totCities];
		double piIndexMin;
		double[] adjMatrixIndexMin;
		for (count = 0; count < totCities; count++)
			h[count] = new nodeLB(citiesVector[count]);

		//
		// Svolge un certo numero (fissato) di iterazioni per arrivare
		// ad un lower-bound il più vicino possibile al valore dell'ottimo.
		//
		do {
			tot = totCities;
			dimMST = 0;
			indexTmp = 0;

			//
			// Attribuisce al primo elemento priorità massima
			// ed a tutti gli altri priorità minima (massimo intero possibile).
			//
			h1 = h[0];
			h1.key = 0;
			h1.degree = 0;

			//
			// Calcola il Minimum Spanning Tree con l'algoritmo di Prim
			// (ottimizzato per un grafo completo) in O(n^2), basandosi
			// anche sulle distanze aggiuntive.
			//
			while (tot > 0) {

				//
				// Estrae il nodo dall'array dei nodi e lo pone in testa (dove
				// non verrà più esaminato fino al prossimo calcolo del MST).
				//
				h[indexTmp] = h[--tot];
				h[tot] = h1;
				h2 = h1.father;
				if (h2 != null) {

					//
					// Fa in modo che anche la radice del MST abbia un padre.
					// In pratica è uno dei suoi figli (serve per calcolare
					// l'1-tree).
					//
					if (h2.father == null)
						h2.father = h1;
					h1.degree = 1;
					h2.degree++;
				}
				indexMin = h1.index;
				piIndexMin = pi[indexMin];
				adjMatrixIndexMin = adjMatrix[indexMin];
				if (tot > 0) {
					min = Double.POSITIVE_INFINITY;
					for (count = 0; count < tot; count++) {
						h2 = h[count];
						wid = adjMatrixIndexMin[h2.index] + pi[h2.index] + piIndexMin;
						if (wid < h2.key) {
							h2.key = wid;
							h2.father = h1;
						}
						if (min > h2.key) {
							indexTmp = count;
							min = h2.key;
						}
					}
					h1 = h[indexTmp];
					dimMST += min;
				}
			}
			piTot = 0;
			dimMax1Tree = Double.NEGATIVE_INFINITY;

			//
			// Calcola il Max 1-Tree ottenibile (con le distanze
			// aggiuntive). Per rendere l'elaborazione più veloce si
			// approssima prendendo in esame come nodo "1" solo le foglie
			// non tutti i nodi come si dovrebbe.
			//
			for (count = 0; count < totCities; count++) {
				h1 = h[count];

				//
				// Se il nodo e' una foglia.
				//
				if (h1.degree == 1) {
					min = Double.POSITIVE_INFINITY;
					indexMin = h1.index;
					adjMatrixIndexMin = adjMatrix[indexMin];

					//
					// Cerca il nodo che ha la seconda distanza minima
					// dal nodo corrente (il primo e' gia' connesso
					// tramite il MST) tenendo conto delle distanze aggiuntive.
					//
					for (count2 = 0; count2 < totCities; count2++) {
						h2 = h[count2];
						wid = adjMatrixIndexMin[h2.index] + pi[h2.index];
						if ((min > wid) && (count2 != count)
								&& (h1.father != h2)) {
							min = wid;
							h3 = h2;
						}
					}
					min += pi[indexMin];
					if (min > dimMax1Tree) {
						dimMax1Tree = min;
						node1 = h1;
						node2 = h3;
					}
				}
				piTot += pi[count];

				//
				// Resetto i seguenti valori per il prossimo calcolo del MST.
				//
				h1.key = Double.POSITIVE_INFINITY;
				h1.father = null;

				//
				// Si decrementa di due il grado di ogni nodo.
				//
				h1.degree -= 2;
			}
			dimMax1Tree += dimMST - (2 * piTot);
			if (dimMax1Tree > lowBound)
				lowBound = dimMax1Tree;

			tspFrame.statusArea.setProgress(Math.round(((float) k / MAX_ITERATIONS) * 100));

			if ((k++ == MAX_ITERATIONS) || isStopped())
				break;

			//
			// Aumento di uno il grado del nodo "1" prescelto e
			// del nodo a cui viene connesso. L'altro nodo era
			// gia' stato connesso nel MST.
			//
			node1.degree++;
			node2.degree++;

			//
			// Inizializza la lunghezza del passo la prima volta.
			//
			if (k == 1) {
				t = (10 * (tourSize - dimMax1Tree)) / totCities;

				//
				// Memorizza i gradi dei nodi dell'istanza.
				//
				for (count = 0; count < totCities; count++)
					h[count].oldDegree = h[count].degree;
			} else {

				//
				// Calcola il nuovo array delle distanze aggiuntive.
				//
				for (count = 0; count < totCities; count++) {
					h1 = h[count];
					pi[h1.index] += t * (0.7 * h1.degree + 0.3 * h1.oldDegree);

					//
					// Memorizza i gradi dei nodi dell'istanza.
					//
					h1.oldDegree = h1.degree;
				}

				//
				// Aggiorna la lunghezza del passo t tramite il fattore
				// di decremento lambda.
				//
				t *= LAMBDA;
			}
		} while (true);
		return lowBound;
	}
}  