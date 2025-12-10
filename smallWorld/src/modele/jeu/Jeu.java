package modele.jeu;

import modele.plateau.Plateau;

public class Jeu extends Thread{
    private Plateau plateau;
    private Joueur j1;
    private Joueur j2;
    protected Coup coupRecu;
    private boolean isPlayer1Turn = true;
    // current tour (starts at 1)
    private int tourNumber = 1;
    // maximum number of tours to play before ending the match
    private int maxTours = 3;
    private boolean gameOver = false;
    private Joueur winner = null;


    //Jeu == Plateau + Joueur 1 + Joueur 2 + gestion du tour + gestion des coups

    public Jeu() {
        plateau = new Plateau();
        // créer les joueurs avant d'initialiser le plateau afin d'assigner
        // la propriété des unités (peuples)
        j1 = new Joueur(this);
        j2 = new Joueur(this);

        plateau.initialiser(j1, j2);

        start();

    }

    /**
     * Pour l'instant le joueur courant est `j1`. Plus tard on pourra étendre
     * la gestion de tours pour retourner le joueur actif.
     */
    public Joueur getCurrentJoueur() {
        return isPlayer1Turn ? j1 : j2;
    }

    public int getTourNumber() { return tourNumber; }

    public int getMaxTours() { return maxTours; }

    public void setMaxTours(int v) { if (v > 0) this.maxTours = v; }

    public boolean isGameOver() { return gameOver; }

    public Joueur getWinner() { return winner; }

    private void switchTurn() {
        // Check if both players have exhausted their endurance BEFORE switching
        // (this is when we know a round is complete)
        if (j1.getEndurance() == 0 && j2.getEndurance() == 0) {
            checkEndOfRound();
            if (gameOver) return;
            // If not game over, endurances were already reset in checkEndOfRound
            // and isPlayer1Turn is already set to true
            return;
        }
        
        // Switch to the other player
        isPlayer1Turn = !isPlayer1Turn;
        // reset l'endurance du joueur actuelle, et non du joueur précédent, sinon on ne saura pas calculer
        // la fin d'un tour
        getCurrentJoueur().resetEndurance();
        // notify observers (view) that turn changed so UI can update
        if (plateau != null) {
            plateau.notifyChange();
            // reset per-unit moved flags for the player who is now active
            plateau.resetMovedForPlayer(getCurrentJoueur());
        }
    }

    /** Ends the round if both players have no endurance left. Decides winner and stops the game. */
    private void checkEndOfRound() {
        if (gameOver) return;
        if (j1.getEndurance() == 0 && j2.getEndurance() == 0) {
            // Award final territory points to both players before deciding winner
            if (plateau != null) {
                int j1Territory = plateau.countPreferredTerrainCases(j1);
                if (j1Territory > 0) j1.addPoints(j1Territory);
                
                int j2Territory = plateau.countPreferredTerrainCases(j2);
                if (j2Territory > 0) j2.addPoints(j2Territory);
            }
            
            // If we've reached the maximum number of tours, decide the winner
            if (tourNumber >= maxTours) {
                if (j1.getPoints() > j2.getPoints()) {
                    winner = j1;
                } else if (j2.getPoints() > j1.getPoints()) {
                    winner = j2;
                } else {
                    winner = null; // tie
                }
                gameOver = true;
                if (plateau != null) plateau.notifyChange();
            } else {
                // advance to next tour: increment tour counter and reset endurances/hasMoved
                tourNumber++;
                isPlayer1Turn = true; // Always start new tour with J1
                j1.resetEndurance();
                j2.resetEndurance();
                if (plateau != null) {
                    plateau.resetMovedForPlayer(j1);
                    plateau.resetMovedForPlayer(j2);
                    plateau.notifyChange();
                }
            }
        }
    }

    public boolean isPlayer1Turn() {
        return isPlayer1Turn;
    }

    public Plateau getPlateau() {
        return plateau;
    }

    public Joueur getJ1() {
        return j1;
    }

    public Joueur getJ2() {
        return j2;
    }

    public void envoyerCoup(Coup c) {
        coupRecu = c;

        synchronized (this) {
            notify();
        }

    }


    public void appliquerCoup(Coup coup) {
        // calculer la portée effective : min(mouvement de l'unité, endurance du joueur courant)
        if (coup == null || coup.dep == null || coup.dep.getUnites() == null) return;
        // Ne permettre au joueur de déplacer qu'une seule unité (peuple) pendant son tour
        // et s'assurer que l'unité appartient bien au joueur courant.
        Joueur courant = getCurrentJoueur();
        modele.jeu.Unites unit = coup.dep.getUnites();
        // 1. L'unité appartient au joueur courant ?
        if (unit.getOwner() != courant) {
            // tentative de déplacer une unité qui n'appartient pas au joueur courant -> ignorer
            return;
        }
        // 2. L'unité n'a pas déjà bougé ?
        if (unit.hasMoved()) {
            return;
        }
        // 3. Le joueur joue le bon peuple ?
        if (courant.getActivePeupleClass() != null && courant.getActivePeupleClass() != unit.getClass()) {
            // tentative de déplacer une autre unité que celle déjà choisie -> ignorer le coup
            return;
        }

        int unitMv = unit.getMouvement();
        // L'endurance représente le nombre de déplacements possibles par tour,
        // pas les points de mouvement par déplacement. On doit donc permettre
        // à chaque déplacement d'utiliser la portée complète `unitMv` tant que
        // le joueur a au moins 1 endurance restante.
        if (courant.getEndurance() <= 0) {
            return; // plus d'endurance -> ne rien faire
        }
        int maxPortee = unitMv;

        // vérifier si la destination est atteignable (sinon ne rien faire)
        java.util.List<modele.plateau.Case> accessibles = plateau.casesAccessibles(coup.dep, maxPortee);
        boolean allowed = false;
        for (modele.plateau.Case c : accessibles) {
            if (c == coup.arr) { allowed = true; break; }
        }
        if (!allowed) return;

        boolean moved = plateau.deplacerUnite(coup.dep, coup.arr, maxPortee);

        // si c'était le premier déplacement du tour pour ce joueur et que le
        // déplacement a réussi, fixer l'unité active; si l'attaquant a été
        // éliminé (moved == false), effacer l'unité active pour permettre de
        // sélectionner un autre peuple.
        if (moved) {
            // Fixe le peuple actif (ne peut plus jouer d'autres peuples)
            if (courant.getActivePeupleClass() == null) {
                courant.setActivePeupleClass(unit.getClass());
                // Si le joueur n'a pas encore de peuple préféré, on le définit à partir de sa première unité jouée
                if (courant.getPreferredPeupleClass() == null) {
                    courant.setPreferredPeupleClass(unit.getClass());
                }
            }
            // marquer l'unité comme ayant bougé ce tour
            unit.setHasMoved(true);
        } else {
            // déplacement autorisé mais combattant éliminé -> déselectionner
            courant.clearActivePeuple();
        }

        // consommer l'endurance pour la tentative de déplacement
        courant.consumeEndurance(1);

        // Si le joueur courant n'a plus d'endurance, changer de joueur
        // (switchTurn() vérifiera automatiquement la fin du tour si les deux joueurs ont endurance == 0)
        if (courant.getEndurance() <= 0) {
            switchTurn();
        }
    }

    public void run() {
        jouerPartie();
    }

    /**
     * Allow external callers (UI) to force the end of the current player's turn.
     * This will switch the active player, reset endurance of the new player and
     * notify observers. It also wakes the game thread waiting for a coup.
     */
    public void endTurn() {
        synchronized (this) {
            if (!gameOver) {
                // Mettre l'endurance du joueur courant à 0 pour forcer le changement
                getCurrentJoueur().consumeEndurance(getCurrentJoueur().getEndurance());
                // switchTurn() vérifiera automatiquement la fin du tour si les deux joueurs ont endurance == 0
                switchTurn();
            }
            notify();
        }
    }

    /** Reset the game to initial state: clears board, resets points and endurance. */
    public void resetGame() {
        // clear game state
        this.gameOver = false;
        this.winner = null;
        this.tourNumber = 1;
        // reset players
        if (j1 != null) { j1.resetPoints(); j1.resetEndurance(); j1.clearActivePeuple(); }
        if (j2 != null) { j2.resetPoints(); j2.resetEndurance(); j2.clearActivePeuple(); }
        // clear cases and reinitialize placement
        if (plateau != null) {
            plateau.initialiser(j1, j2);
            plateau.notifyChange();
        }
    }

    public void jouerPartie() {

        while(true) {
            // attendre le coup du joueur courant
            Joueur courant = getCurrentJoueur();
            Coup c = courant.getCoup();
            appliquerCoup(c);

        }

    }


}
