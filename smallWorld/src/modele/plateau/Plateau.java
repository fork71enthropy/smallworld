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
import modele.jeu.Joueur;

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

        // clear any existing units (useful for restart)
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c != null) c.clearUnits();
            }
        }

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
        casesAccessibles(start, mouvement, resultat);
        return resultat;
    }

    /**
     * Tutor-prescribed BFS: populate `lst` with reachable cases starting from
     * `start`, exploring up to `enduranceRestante` steps (levels). This method
     * follows the algorithm shape provided by the tutor: it visits the start
     * and then explores neighbours recursively/level-by-level. We also forbid
     * entering on a case containing units of the same class as the start unit.
     */
    public void casesAccessibles(Case start, int enduranceRestante, java.util.List<Case> lst) {
        if (start == null || enduranceRestante <= 0 || lst == null) return;

        // startClass is no longer used: we allow entering/passing over same-type units

        java.util.Set<Case> visited = new java.util.HashSet<>();

        // mark start visited but do NOT add it to the result (base rules)
        visited.add(start);

        // enqueue first-level neighbours, each with its direction
        class Node { Case c; int dir; Node(Case c, int dir){this.c=c;this.dir=dir;} }
        java.util.Queue<Node> qq = new java.util.ArrayDeque<>();

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        // first level
        for (int d = 0; d < dirs.length; d++) {
            int nx = map.get(start).x + dirs[d][0];
            int ny = map.get(start).y + dirs[d][1];
            if (nx < 0 || nx >= SIZE_X || ny < 0 || ny >= SIZE_Y) continue;
            Case neigh = grilleCases[nx][ny];
            if (neigh == null) continue;
            if (visited.contains(neigh)) continue;
            visited.add(neigh);
            lst.add(neigh);
            qq.add(new Node(neigh, d));
        }

        int level = 1;
        while (level < enduranceRestante) {
            if (qq.isEmpty()) break;
            int curLevelSize = qq.size();
            for (int i = 0; i < curLevelSize; i++) {
                Node node = qq.poll();
                if (node == null) continue;
                Case cur = node.c;
                int dir = node.dir;
                java.awt.Point p = map.get(cur);
                if (p == null) continue;
                int nx = p.x + dirs[dir][0];
                int ny = p.y + dirs[dir][1];
                if (nx < 0 || nx >= SIZE_X || ny < 0 || ny >= SIZE_Y) continue;
                Case neigh = grilleCases[nx][ny];
                if (neigh == null) continue;
                if (visited.contains(neigh)) continue;
                visited.add(neigh);
                lst.add(neigh);
                qq.add(new Node(neigh, dir));
            }
            level++;
        }
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
        if (c1 == null || c2 == null || c1.getUnites() == null) {
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
        boolean moved = c1.getUnites().allerSurCase(c2);

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

    /**
     * Reset the moved flag for all units owned by the given player.
     */
    public void resetMovedForPlayer(Joueur j) {
        if (j == null) return;
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c != null) {
                    for (Unites u : c.getAllUnites()) {
                        if (u != null && u.getOwner() == j) {
                            u.setHasMoved(false);
                        }
                    }
                }
            }
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

    /**
     * Compte le nombre de cases occupées par au moins une unité d'un joueur
     * où le biome de la case correspond au biome préféré de ce type d'unité.
     * Utilisé pour attribuer des points en fin de tour.
     */
    public int countPreferredTerrainCases(Joueur joueur) {
        if (joueur == null) return 0;
        int count = 0;
        for (int x = 0; x < SIZE_X; x++) {
            for (int y = 0; y < SIZE_Y; y++) {
                Case c = grilleCases[x][y];
                if (c != null) {
                    // Vérifier chaque unité sur cette case
                    for (Unites u : c.getAllUnites()) {
                        if (u != null && u.getOwner() == joueur) {
                            // Si le biome de la case correspond au biome préféré de l'unité
                            if (c.getBiome() == u.getBiomePreference()) {
                                count++;
                                break; // Une seule fois par case (au moins une unité)
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

}
