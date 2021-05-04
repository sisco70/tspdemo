package net.guarnie.tspdemo;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

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
public class Main {
    public static void main( String[] args )
    {
        // Schedula un job nell'event-dispatching thread.
        // Crea e mostra il frame principale dell'applicazione.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if ("Nimbus".equals(info.getName())) {
                            UIManager.setLookAndFeel(info.getClassName());
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Nimbus Look & Feel not found.");
                }
                new TspFrame("TspDemo");
            }
        });
    }
}
