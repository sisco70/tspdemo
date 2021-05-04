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
 * Classe che contiene le città (i nodi del nostro grafo).
 */
public class City  {
	/**
	 * Costante che indica la massima capacita' dei vettori che contenengono i
	 * vicini di ogni nodo (città).
	 */
	final static int NEIGHBOURS_LIST_SIZE = 30;

	/**
	 * Coordinata x della città.
	 */
	public double x;

	/**
	 * Coordinata y della città.
	 */
	public double y;

	/**
	 * Indice della città nel vettore delle città.
	 */
	public int num;

	/**
	 * Indice della città nel vettore contenente il circuito corrente.
	 */
	public int tourIndex = -1;

	/**
	 * Vettore contenente le prime "NEIGHBOURS_LIST_SIZE" città vicine.
	 */
	public Neighbour[] neighbours = new Neighbour[NEIGHBOURS_LIST_SIZE];

	/**
	 * Distanza della città più lontana tra quelle contenute nel vettore delle
	 * città vicine.
	 */
	public double farthestDist;

	/**
	 * Numero di città presenti nel vettore contenente le città vicine.
	 */
	public int neighboursSize;

	/**
	 * Costruttore della classe.
	 */
	public City(double coordX, double coordY, int n) {
		x = coordX;
		y = coordY;
		num = n;
	}

	/**
	 * Svuota la neighbours list.
	 */
	public final void clearNeighbours() {
		farthestDist = 0;
		neighboursSize = 0;
	}

	/**
	 * Inserisce un vicino nel vettore delle citta' vicine. (Anche se il metodo
	 * arraycopy e' molto piu' veloce di una copia elemento per elemento,
	 * l'inserimento di un nuovo neighbour potrebbe essere rivisto e reso piu'
	 * efficiente, ad esempio usando una lista ordinata, ma date le ridotte
	 * dimensioni dell'array dei neighbours, non credo che il guadagno di
	 * prestazioni valga la pena).
	 */
	public final void addNeighbour(City c, double d) {
		if ((farthestDist > d) || (neighboursSize < NEIGHBOURS_LIST_SIZE)) {
			int count = 0;
			while ((count < neighboursSize) && (neighbours[count].dist < d))
				count++;
			if (neighboursSize < NEIGHBOURS_LIST_SIZE) {
				System.arraycopy(neighbours, count, neighbours, count + 1,
						neighboursSize - count);
				neighboursSize++;
			} else
				System.arraycopy(neighbours, count, neighbours, count + 1,
						neighboursSize - count - 1);
			neighbours[count] = new Neighbour(c, d);
			farthestDist = neighbours[neighboursSize - 1].dist;
		}
	}
}
