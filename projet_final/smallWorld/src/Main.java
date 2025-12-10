import vuecontroleur.VueControleur;
import modele.jeu.Jeu;

import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Runnable r = new Runnable() {
            @Override
            public void run() {

                VueControleur vc = new VueControleur(new Jeu());
                vc.setVisible(true);
                
                
                
            }
        };

        SwingUtilities.invokeLater(r);


    }
}