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
 * Classe che contiene le città "vicine".
 */
public class Neighbour {
	/**
	 * Città vicina.
	 */
	public final City c;
	/**
	 * Distanza della città.
	 */
	public final double dist;

	/**
	 * Costruttore della classe.
	 */
	public Neighbour(City city, double d) {
		c = city;
		dist = d;
	}
}