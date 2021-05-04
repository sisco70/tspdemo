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

import java.awt.Component;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;

class ListEntry {
	Object object;
	int level;
	boolean isNode;

	public ListEntry(Object anObject, int aLevel, boolean isNode) {
		object = anObject;
		level = aLevel;
		this.isNode = isNode;
	}

	public Object object() {
		return object;
	}

	public int level() {
		return level;
	}

	public boolean isNode() {
		return isNode;
	}

	public String toString() {
		return object.toString();
	}
}


/**
 * JCombobox custom per mostrare un albero di scelte
 */
public class TreeCombo extends JComboBox<ListEntry> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Numero di pixel di avanzamento tra un livello ed un altro dell'albero.
	 */
	static final int OFFSET = 10;

	/**
	 * Immagine della foglia dell'albero.
	 */
	private static ImageIcon leafIcon;

	/**
	 * Immagine del nodo dell'albero.
	 */
	private static ImageIcon nodeIcon;

	/**
	* Il bordo vuoto.
	*/
	static Border emptyBorder = new EmptyBorder(1, 1, 1, 1);
		
	/**
	 * Riferimento al frame principale.
	 */
	private TspFrame tspFrame;

	/**
	 * Lista delle descrizioni.
	 */
	public String[] descList;

	/**
	 * Costruttore della classe.
	 */
	public TreeCombo(TreeModel aTreeModel, TspFrame tFrame) {
		super();
		tspFrame = tFrame;
		setModel(new TreeToListModel(aTreeModel));
		leafIcon = tspFrame.loadImageIcon("/images/eurist.gif");
		nodeIcon = tspFrame.loadImageIcon("/images/cartella.gif");
		setRenderer(new ListEntryRenderer());
	}

	class TreeToListModel extends AbstractListModel<ListEntry> implements ComboBoxModel<ListEntry>, TreeModelListener {
		/**
		 *
		 */
		private static final long serialVersionUID = 8340497774135183591L;
		TreeModel source;
		boolean invalid = true;
		Object currentValue;
		Vector<ListEntry> cache = new Vector<ListEntry>();
	
		/**
		 * Costruttore della classe.
		 */
		public TreeToListModel(TreeModel aTreeModel) {
			source = aTreeModel;
			aTreeModel.addTreeModelListener(this);
			setRenderer(new ListEntryRenderer());
		}
	
		public void setSelectedItem(Object anObject) {
			currentValue = anObject;
			//
			// Se l'elemento selezionato nella comboBox e' un nodo
			// lo scarta, e prende la prima foglia subito dopo.
			//
			if (((ListEntry) currentValue).isNode()) {
				int selInd = getSelectedIndex();
				if (selInd < getItemCount() - 1)
					setSelectedItem(getElementAt(selInd + 1));
			}
			fireContentsChanged(this, -1, -1);
		}
	
		public Object getSelectedItem() {
			return currentValue;
		}
	
		public int getSize() {
			validate();
			return cache.size();
		}
	
		public ListEntry getElementAt(int index) {
			return cache.elementAt(index);
		}
	
		public void treeNodesChanged(TreeModelEvent e) {
			invalid = true;
		}
	
		public void treeNodesInserted(TreeModelEvent e) {
			invalid = true;
		}
	
		public void treeNodesRemoved(TreeModelEvent e) {
			invalid = true;
		}
	
		public void treeStructureChanged(TreeModelEvent e) {
			invalid = true;
		}
	
		void validate() {
			if (invalid) {
				cache = new Vector<ListEntry>();
				cacheTree(source.getRoot(), 0);
				if (cache.size() > 0)
					currentValue = cache.elementAt(0);
				invalid = false;
				fireContentsChanged(this, 0, 0);
			}
		}
	
		void cacheTree(Object anObject, int level) {
			if (source.isLeaf(anObject))
				addListEntry(anObject, level, false);
			else {
				int c = source.getChildCount(anObject);
				int i;
				Object child;
				addListEntry(anObject, level, true);
				level++;
				for (i = 0; i < c; i++) {
					child = source.getChild(anObject, i);
					cacheTree(child, level);
				}
				level--;
			}
		}
	
		void addListEntry(Object anObject, int level, boolean isNode) {
			cache.addElement(new ListEntry(anObject, level, isNode));
		}
	}
	
	/**
 	* Ridefinisco il renderer degli elementi della jlist.
 	*/
	class ListEntryRenderer extends JLabel implements ListCellRenderer<ListEntry> {
		/**
		 *
		 */
		private static final long serialVersionUID = 274495154754867654L;

		/**
		 * Costruttore della classe.
		 */
		public ListEntryRenderer() {
			setOpaque(true);
		}

		/**
		 * Il metodo che costruisce la label che rappresenta l'elemento corrente
		 * della jlist.
		 */
		@Override
		public Component getListCellRendererComponent(JList<? extends ListEntry> listbox, ListEntry value, int index, boolean isSelected, boolean cellHasFocus) {
			if (value != null) {
				Border border;
				setText(value.toString());
				setIcon(value.isNode() ? nodeIcon : leafIcon);
				
				if (index != -1)
					border = new EmptyBorder(1,	(OFFSET * value.level()) + 1, 1, 1);
				else
					border = emptyBorder;
				
				setBorder(border);
				
				if (isSelected) {
					setBackground(UIManager.getColor("Tree.selectionBackground"));
					setForeground(UIManager.getColor("Tree.selectionForeground"));
					//
					// Inserisce la descrizione nella status area.
					//
					if (index != -1)
						tspFrame.statusArea.setStatus(descList[index]);
				} else {
					setBackground(null);
					setForeground(null);
				}
			} else
				setText("");
			return this;
		}
	}

}