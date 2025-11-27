package modele.jeu;

public class Joueur {
    private Jeu jeu;
    private int endurance;
    private int points = 0;
    private Class<? extends Unites> activePeupleClass = null;

    public Joueur(Jeu _jeu) {
        jeu = _jeu;
        this.endurance = 2;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int v) {
        this.points += v;
        if (this.points < 0) this.points = 0;
    }

    public void resetPoints() {
        this.points = 0;
    }

    public int getEndurance() {
        return endurance;
    }

    public Class<? extends Unites> getActivePeupleClass() {
        return activePeupleClass;
    }

    public void setActivePeupleClass(Class<? extends Unites> c) {
        this.activePeupleClass = c;
    }

    public void clearActivePeuple() {
        this.activePeupleClass = null;
    }

    public Coup getCoup() {

        synchronized (jeu) {
            try {
                jeu.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return jeu.coupRecu;
    }

    public void consumeEndurance(int amount) {
        this.endurance -= amount;
        if (this.endurance < 0) this.endurance = 0;
        if (this.endurance == 0) this.clearActivePeuple();
    }

    public void resetEndurance() {
        this.endurance = 2;
        this.clearActivePeuple();
    }
}
