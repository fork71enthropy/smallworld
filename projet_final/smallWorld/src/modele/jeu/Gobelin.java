package modele.jeu;

import modele.plateau.Plateau;
import modele.plateau.Case;

public class Gobelin extends Unites {
    public Gobelin(Plateau _plateau) {
        super(_plateau);
    }

    @Override
    public Case.Biome getBiomePreference() {
        return Case.Biome.DESERT;
    }

}
