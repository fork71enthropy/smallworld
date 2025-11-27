package modele.jeu;

import modele.plateau.Case;
import modele.plateau.Plateau;

/**
 * Entités amenées à bouger
 */
public abstract class Unites {

    protected Case c;
    protected Plateau plateau;
    protected Joueur owner = null;
    // nombre de cases que l'unité peut parcourir par déplacement (par défaut 1)
    protected int mouvement = 2;
    // indique si l'unité a déjà effectué un déplacement pendant le tour courant
    protected boolean hasMoved = false;

    public Unites(Plateau _plateau) {
        plateau = _plateau;

    }

    public int getMouvement() {
        return mouvement;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean v) {
        this.hasMoved = v;
    }

    public Joueur getOwner() {
        return owner;
    }

    public void setOwner(Joueur j) {
        this.owner = j;
    }

    public void quitterCase() {
        if (c != null) {
            c.quitterLaCase(this);
        }
    }
    public boolean allerSurCase(Case _c) {
        if (c != null) {
            quitterCase();
        }
        c = _c;
        return plateau.arriverCase(c, this);
    }

    /**
     * Combat naïf : l'unité actuelle attaque les unités présentes sur la case cible.
     * - si la case est vide ou contient des unités du même type, on ajoute simplement
     *   l'unité sur la case (empilement).
     * - si la case contient des unités d'un autre type, on résout un duel aléatoire
     *   : 50% de chances pour l'attaquant de gagner. Si l'attaquant gagne, on
     *   retire une unité défenderesse (appel à `quitterLaCase` sur la case) puis
     *   on ajoute l'attaquant sur la case. Si l'attaquant perd, il est considéré
     *   comme éliminé (rappel : `allerSurCase` a déjà enlevé l'attaquant de sa case
     *   d'origine via `quitterCase`).
     */
    public boolean combattreSurCase(Case cible) {
        if (cible == null) return false;
        Unites def = cible.getUnites();
        // si case vide
        if (def == null) {
            cible.ajouterUnite(this);
            return true;
        }
        // si même type -> empilement
        if (def.getClass().equals(this.getClass())) {
            cible.ajouterUnite(this);
            return true;
        }

        // combat aléatoire simple
        boolean attaquantGagne = new java.util.Random().nextBoolean();
        if (attaquantGagne) {
            // retirer une unité défenderesse (retire l'instance choisie)
            if (def != null) {
                cible.quitterLaCase(def);
            }
            // ajouter l'attaquant (empilement/prise de case)
            cible.ajouterUnite(this);
            return true;
        } else {
            // l'attaquant perd : il a déjà été retiré de sa case source
            return false;
        }
    }

    public Case getCase() {
        return c;
    }
}

/**
 * clic x,y
 * Case c = plateau.getCase(x,y);
 * ArrayList<Case> lst = new ArrayList<Case>();
 * cases_accessibles(c,c.getU().getEndurance(),lst);
 * marquer(lst);
 * lancer_raffraichissement();
 * 
 * 
 * 
 * 
 * cases_accessibles(Case c, int enduranceRestante, ArrayList<Case> lst){
 *  if(enduranceRestante > 0 ){
 *     lst.add(c);
 *     enduranceRestante -= 1; 
 *     cases_accessibles (c.voisines(), enduranceRestante, lst);
 *  }
 * 
 * }
 * 
 * 
 * 
 * 
 * 
 */

































