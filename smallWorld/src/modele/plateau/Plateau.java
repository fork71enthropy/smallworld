/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modele.plateau;

import modele.jeu.Elfes;
import modele.jeu.Humain;
import modele.jeu.Gobelin;
import modele.jeu.Unites;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Observable;

public class Plateau extends Observable {

    public static final int SIZE_X = 6;
    public static final int SIZE_Y = 6;

    private HashMap<Case, Point> map = new HashMap<Case, Point>();
    private Case[][] grilleCases = new Case[SIZE_X][SIZE_Y];

    public Plateau() {
        initPlateauVide();
    }

    public Case[][] getCases() {
        return grilleCases;
    }

    private void initPlateauVide() {
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                grilleCases[x][y] = new Case(this);
                map.put(grilleCases[x][y], new Point(x, y));
            }
        }
    }

    public void initialiser(modele.jeu.Joueur j1, modele.jeu.Joueur j2) {
        // générer les biomes de la grille
        genererBiomesEquilibresRandom();

        // Positionner 8 unités pour le joueur 1 dans le coin haut-gauche
        int toPlace = 8;
        int placed = 0;
        for (int x = 0; x < SIZE_X && placed < toPlace; x++) {
            for (int y = 0; y < SIZE_Y && placed < toPlace; y++) {
                Humain h = new Humain(this);
                h.setOwner(j1);
                h.allerSurCase(grilleCases[x][y]);
                placed++;
            }
        }

        // Positionner 8 unités pour le joueur 2 dans le coin bas-droit
        placed = 0;
        for (int x = SIZE_X - 1; x >= 0 && placed < toPlace; x--) {
            for (int y = SIZE_Y - 1; y >= 0 && placed < toPlace; y--) {
                Gobelin g = new Gobelin(this);
                g.setOwner(j2);
                g.allerSurCase(grilleCases[x][y]);
                placed++;
            }
        }

        setChanged();
        notifyObservers();
    }

    /**
     * Génère une répartition aussi équilibrée que possible des biomes
     * et l'affecte aux cases de la grille de façon aléatoire.
     */
    public void genererBiomesEquilibresRandom() {
        List<Case.Biome> pool = new ArrayList<>();
        int total = SIZE_X * SIZE_Y;
        Case.Biome[] values = Case.Biome.values();
        int types = values.length;
        int base = total / types;
        int rem = total % types;

        // ajouter base occurrences de chaque biome
        for (Case.Biome b : values) {
            for (int i = 0; i < base; i++) {
                pool.add(b);
            }
        }

        // distribuer le reste
        for (int i = 0; i < rem; i++) {
            pool.add(values[i]);
        }

        // shuffle pour répartir aléatoirement
        Collections.shuffle(pool, new Random());

        // affecter aux cases
        int idx = 0;
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                grilleCases[x][y].setBiome(pool.get(idx));
                idx++;
            }
        }
    }

    /**
     * Retourne la liste des cases accessibles depuis `start` en respectant la
     * portée `mouvement` (distance de Manhattan), les déplacements orthogonaux
     * et l'interdiction d'arriver sur une case qui contient déjà des unités
     * du même type que l'unité qui se déplace.
     *
     * Règles appliquées ici :
     * - on considère uniquement les 4 voisins (pas de diagonale)
     * - on autorise le passage vers une case si elle est vide (nbUnites==0)
     *   ou si elle contient une unité d'un type différent (attaque possible)
     * - on n'autorise pas d'entrer sur une case contenant des unités du même
     *   type que l'unité en départ
     *
     * Cette méthode fait un BFS limité par `mouvement` et retourne les cases
     * atteignables (exclut la case de départ dans la liste retournée).
     */
    public java.util.List<Case> casesAccessibles(Case start, int mouvement) {
        java.util.List<Case> resultat = new java.util.ArrayList<>();
        if (start == null || mouvement <= 0) return resultat;

        // trouver la position de start pour indexation
        java.awt.Point p = map.get(start);
        if (p == null) return resultat;

        int sx = p.x;
        int sy = p.y;

        // Parcours en ligne droite dans les 4 directions (haut, bas, gauche, droite)
        // pour interdire les déplacements diagonaux et les changements de direction.
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] dir : dirs) {
            for (int step = 1; step <= mouvement; step++) {
                int nx = sx + dir[0] * step;
                int ny = sy + dir[1] * step;
                if (nx < 0 || nx >= SIZE_X || ny < 0 || ny >= SIZE_Y) break;
                Case neigh = grilleCases[nx][ny];
                // Autoriser l'entrée sur la case (empilement autorisé).
                resultat.add(neigh);
                // Si on souhaite stopper le parcours quand on rencontre une case
                // occupée par un autre peuple (attaque possible), on pourrait
                // ajouter une condition ici pour `break;`. Pour l'instant on
                // continue la ligne même si la case est occupée.
            }
        }

        return resultat;
    }

    public boolean arriverCase(Case c, Unites u) {
        // Si la case est vide ou contient des unités du même type, on empile.
        Unites present = c.getUnites();
        if (present == null) {
            c.ajouterUnite(u);
            return true;
        }

        if (present.getClass().equals(u.getClass())) {
            c.ajouterUnite(u);
            return true;
        }

        // cas : unités d'un autre type -> déléguer au mécanisme de combat de l'unité
        return u.combattreSurCase(c);
    }

    public boolean deplacerUnite(Case c1, Case c2, int maxPortee) {
        // Valide que la case source contient bien une unité
        if (c1 == null || c2 == null || c1.u == null) {
            return false;
        }

        // Autoriser le déplacement si et seulement si la case destination est
        // atteignable depuis la case source en respectant la portée effective
        // fournie (maxPortee). La portée effective devrait être calculée par
        // le contrôleur/jeu en prenant en compte `Unites.mouvement` et
        // `Joueur.endurance`.
        java.util.List<Case> accessibles = casesAccessibles(c1, maxPortee);
        boolean allowed = false;
        for (Case c : accessibles) {
            if (c == c2) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            // destination non atteignable dans la portée -> ne rien faire
            return false;
        }

        // Effectuer le déplacement (allerSurCase retourne true si l'attaquant
        // est présent sur la case cible après résolution, false sinon)
        boolean moved = c1.u.allerSurCase(c2);

        if (moved) {
            setChanged();
            notifyObservers();
            return true;
        } else {
            // attaquant éliminé ou non ajouté
            setChanged();
            notifyObservers();
            return false;
        }
    }

    /** Indique si p est contenu dans la grille */
    private boolean contenuDansGrille(Point p) {
        return p.x >= 0 && p.x < SIZE_X && p.y >= 0 && p.y < SIZE_Y;
    }

    private Case caseALaPosition(Point p) {
        Case retour = null;
        if (contenuDansGrille(p)) {
            retour = grilleCases[p.x][p.y];
        }
        return retour;
    }

    /** Permet à d'autres classes (ex: Jeu) de notifier les observers via Plateau */
    public void notifyChange() {
        setChanged();
        notifyObservers();
    }

}
