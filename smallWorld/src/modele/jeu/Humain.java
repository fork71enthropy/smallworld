package modele.jeu;

import modele.plateau.Plateau;
import modele.plateau.Case;

public class Humain extends Unites {
    public Humain(Plateau _plateau) {
        super(_plateau);
    }

    @Override
    public Case.Biome getBiomePreference() {
        return Case.Biome.PLAINE;
    }

}
