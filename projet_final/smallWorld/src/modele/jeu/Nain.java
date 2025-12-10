package modele.jeu;

import modele.plateau.Plateau;
import modele.plateau.Case;

public class Nain extends Unites {
    public Nain(Plateau _plateau) {
        super(_plateau);
    }

    @Override
    public Case.Biome getBiomePreference() {
        return Case.Biome.MONTAGNE;
    }

}
