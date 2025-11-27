package modele.jeu;

import modele.plateau.Plateau;
import modele.plateau.Case;

public class Elfes extends Unites {
    public Elfes(Plateau _plateau) {
        super(_plateau);
    }

    @Override
    public Case.Biome getBiomePreference() {
        return Case.Biome.FORET;
    }

}
