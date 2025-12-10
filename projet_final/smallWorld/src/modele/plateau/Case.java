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
    protected java.util.List<Unites> units = new java.util.ArrayList<>();
    protected Plateau plateau;
    // nombre d'unités présentes sur la case (du même type que `units.get(0)`)
    protected int nbUnites = 0;

    public void quitterLaCase(Unites leaving) {
        if (leaving == null) return;
        // remove the specific unit instance from the stack if present
        boolean removed = units.remove(leaving);
        if (removed) {
            nbUnites = units.size();
        }
        if (nbUnites == 0) {
            // nothing left
        }
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
        if (units.isEmpty()) return null;
        return units.get(0);
    }

    public int getNbUnites() {
        return nbUnites;
    }

    /** Ajoute une unité sur la case. Si c'est la première, on conserve une référence de type pour l'affichage. */
    public void ajouterUnite(Unites _u) {
        if (_u == null) return;
        units.add(_u);
        nbUnites = units.size();
    }

    /** Retourne une copie de la liste des unités présentes sur la case. */
    public java.util.List<Unites> getAllUnites() {
        return new java.util.ArrayList<Unites>(units);
    }

    public Biome getBiome() {
        return this.biome;
    }

    public void setBiome(Biome _biome) {
        this.biome = _biome;
    }

    /** Supprime toutes les unités de la case (utilisé pour réinitialiser la grille). */
    public void clearUnits() {
        units.clear();
        nbUnites = 0;
    }

}




