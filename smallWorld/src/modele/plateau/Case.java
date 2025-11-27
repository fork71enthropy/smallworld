/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele.plateau;

import modele.jeu.Unites;

public class Case {

    /** Types de terrain (biomes) disponibles sur une case */
    public static enum Biome {
        PLAINE,
        FORET,
        MONTAGNE,
        DESERT
    }

    // biome == type de terrain
    protected Biome biome;
    protected Unites u;
    protected Plateau plateau;
    // nombre d'unités présentes sur la case (du même type que `u`)
    protected int nbUnites = 0;

    public void quitterLaCase() {
        if (nbUnites > 0) nbUnites--;
        if (nbUnites == 0) u = null;
    }

    public Case(Plateau _plateau) {
        plateau = _plateau;
        biome = Biome.PLAINE; // valeur par défaut
    }


    public Case(Plateau _plateau, Biome _biome) {
        plateau = _plateau;
        biome = _biome; // valeur choisie
    }

    public Unites getUnites() {
        return u;
    }

    public int getNbUnites() {
        return nbUnites;
    }

    /** Ajoute une unité sur la case. Si c'est la première, on conserve une référence de type pour l'affichage. */
    public void ajouterUnite(Unites _u) {
        if (u == null) {
            u = _u;
            nbUnites = 1;
        } else {
            // si même type on incrémente, sinon on remplace la référence mais on incrémente aussi
            nbUnites++;
        }
    }

    public Biome getBiome() {
        return this.biome;
    }

    public void setBiome(Biome _biome) {
        this.biome = _biome;
    }

}
