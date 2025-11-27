package vuecontroleur; //signifie que cette classe fait partie du paquet vuecontroleur qui est un dossier
                    //package == nom de dossier logique/namespace
                    //parce que le fichier est dans un dossier

import java.awt.*; //importe toutes les classes du package java window toolkit
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable; //class that represent an observable object, "data" in the mv paradigm
                        //an observable object can have one or more observers
import java.util.Observer;

import javax.lang.model.type.NullType;
import javax.swing.*;


import modele.jeu.Elfes;
import modele.jeu.Humain;
import modele.jeu.Nain;
import modele.jeu.Gobelin;
import modele.jeu.Joueur;
import modele.jeu.Coup;
import modele.jeu.Jeu;
import modele.jeu.Unites;
import modele.plateau.Case;
import modele.plateau.Plateau;


/** Cette classe a deux fonctions :
 *  (1) Vue : proposer une représentation graphique de l'application (cases graphiques, etc.)
 *  (2) Controleur : écouter les évènements clavier et déclencher le traitement adapté sur le modèle (clic position départ -> position arrivée pièce))
 *
 */
public class VueControleur extends JFrame implements Observer {
    private Plateau plateau; // référence sur une classe de modèle : permet d'accéder aux données du modèle pour le rafraichissement, permet de communiquer les actions clavier (ou souris)
    private Jeu jeu;
    private final int sizeX; // taille de la grille affichée
    private final int sizeY;
    private static final int pxCase = 100; // nombre de pixel par case
    // icones affichées dans la grille
    private Image icoElfes;
    private Image icoDesert;

    private Image icoGreen;
    private Image icoPlaine;
    private Image icoMontagne;
    private Image icoHumain;
    private Image icoNain;
    private Image icoGobelin;

    private JLabel lblJ1;
    private JLabel lblJ2;
    private JLabel lblTour;
    private JLabel lblWinner;
    private JButton btnEndTurn;
    private JButton btnRestart;
    private boolean gameOverDialogShown = false;

     

    private JComponent grilleIP;
    private Case caseClic1; // mémorisation des cases cliquées
    private Case caseClic2;


    private ImagePanel[][] tabIP; // cases graphique (au moment du rafraichissement, chaque case va être associée à une icône background et front, suivant ce qui est présent dans le modèle)


    public VueControleur(Jeu _jeu) {
        jeu = _jeu;
        plateau = jeu.getPlateau();
        sizeX = Plateau.SIZE_X;
        sizeY = Plateau.SIZE_Y;


    
        chargerLesIcones();
        placerLesComposantsGraphiques();
        plateau.addObserver(this);
        mettreAJourAffichage();

    }


    private void chargerLesIcones() {
        //icoElfes = new ImageIcon("./data/res/cat.png").getImage();
        //icoDesert = new ImageIcon("./data/res/desert.png").getImage();

        icoHumain = new ImageIcon("./data/units/unit_yellow.png").getImage(); 
        icoElfes = new ImageIcon("./data/units/unit_red.png").getImage(); 
        icoDesert = new ImageIcon("./data/terrain/desert.png").getImage();
        icoGreen = new ImageIcon("./data/terrain/forest.png").getImage();
        icoPlaine = new ImageIcon("./data/terrain/plain.png").getImage();
        icoMontagne = new ImageIcon("./data/terrain/moutain.png").getImage();
        icoNain = new ImageIcon("./data/units/unit_blue.png").getImage();
        icoGobelin = new ImageIcon("./data/units/unit_green.png").getImage();
        
    }

    // chargerBiomes() unused for now





    private void placerLesComposantsGraphiques() {
        setTitle("Smallworld");
        setResizable(true);
        setSize(sizeX * pxCase, sizeY * pxCase);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // permet de terminer l'application à la fermeture de la fenêtre

        setLayout(new BorderLayout());

        // panneau supérieur affichant les joueurs et contrôles
        JPanel topPanel = new JPanel(new GridLayout(1,5));
        lblJ1 = new JLabel("J1");
        lblTour = new JLabel("Tour: 1", SwingConstants.CENTER);
        lblWinner = new JLabel("", SwingConstants.CENTER);
        lblJ2 = new JLabel("J2");
        lblJ1.setOpaque(true);
        lblJ2.setOpaque(true);
        lblJ1.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        lblJ2.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
        topPanel.add(lblJ1);
        topPanel.add(lblTour);
        // (maxTours is configured in code; spinner removed)
        // bouton pour terminer manuellement le tour
        btnEndTurn = new JButton("End Turn");
        btnEndTurn.setFocusable(false);
        btnEndTurn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    jeu.endTurn();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        topPanel.add(btnEndTurn, BorderLayout.CENTER);
        // bouton pour recommencer la partie
        btnRestart = new JButton("Restart");
        btnRestart.setFocusable(false);
        btnRestart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // reset dialog flag so modal can show again later
                    gameOverDialogShown = false;
                    jeu.resetGame();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        topPanel.add(btnRestart);
        topPanel.add(lblJ2);
        // label winner displayed under the controls (reuse center cell)
        // we don't add lblWinner to topPanel grid to keep layout simple; it will be shown via title when game ends
        add(topPanel, BorderLayout.NORTH);

        grilleIP = new JPanel(new GridLayout(sizeY, sizeX)); // grilleJLabels va contenir les cases graphiques et les positionner sous la forme d'une grille


        tabIP = new ImagePanel[sizeX][sizeY];

        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                ImagePanel iP = new ImagePanel();

                tabIP[x][y] = iP; // on conserve les cases graphiques dans tabJLabel pour avoir un accès pratique à celles-ci (voir mettreAJourAffichage() )

                final int xx = x; // permet de compiler la classe anonyme ci-dessous
                final int yy = y;
                // écouteur de clics
                iP.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {

                        Case clicked = plateau.getCases()[xx][yy];

                        // si pas de sélection en cours : sélectionner seulement si il y a une unité
                        if (caseClic1 == null) {
                            if (clicked != null && clicked.getUnites() != null) {
                                if (jeu.isGameOver()) return;
                                // n'autoriser la sélection que si l'unité appartient au joueur courant
                                if (clicked.getUnites().getOwner() != jeu.getCurrentJoueur()) {
                                    return;
                                }
                                // et si l'unité n'a pas déjà bougé ce tour
                                if (clicked.getUnites().hasMoved()) {
                                    return;
                                }
                                caseClic1 = clicked;
                                // calculer et surligner les cases accessibles
                                // n'autoriser la sélection que si le joueur a encore
                                // au moins 1 point d'endurance (endurance == nombre de déplacements)
                                if (jeu.getCurrentJoueur().getEndurance() <= 0) return;
                                int unitMv = clicked.getUnites().getMouvement();
                                // permettre la portée complète de l'unité pour ce déplacement
                                java.util.List<Case> accessibles = plateau.casesAccessibles(caseClic1, unitMv);
                                highlightCases(accessibles);
                            }
                        } else {
                            if (jeu.isGameOver()) return;
                            // si on reclique sur la case source -> annuler
                            if (clicked == caseClic1) {
                                clearHighlights();
                                caseClic1 = null;
                                return;
                            }

                            // si la case cliquée est surlignée, on envoie le coup
                            if (tabContainsCaseHighlighted(clicked)) {
                                caseClic2 = clicked;
                                jeu.envoyerCoup(new Coup(caseClic1, caseClic2));
                            }

                            // nettoyage quoi qu'il arrive
                            clearHighlights();
                            caseClic1 = null;
                            caseClic2 = null;
                        }

                    }
                });



                grilleIP.add(iP);
            }
        }
        add(grilleIP, BorderLayout.CENTER);
    }

    
    /**
     * Il y a une grille du côté du modèle ( jeu.getGrille() ) et une grille du côté de la vue (tabIP)
     */
    private void mettreAJourAffichage() {
        // Mettre à jour l'indicateur de joueur actif et afficher l'endurance
        try {
            // afficher endurance à côté de J1 / J2
            if (jeu.getJ1() != null) {
                lblJ1.setText("J1 (E:" + jeu.getJ1().getEndurance() + " P:" + jeu.getJ1().getPoints() + ")");
            } else {
                lblJ1.setText("J1");
            }
            if (jeu.getJ2() != null) {
                lblJ2.setText("J2 (E:" + jeu.getJ2().getEndurance() + " P:" + jeu.getJ2().getPoints() + ")");
            } else {
                lblJ2.setText("J2");
            }

            boolean isJ1 = jeu.isPlayer1Turn();
            if (isJ1) {
                lblJ1.setBackground(Color.YELLOW);
                lblJ1.setFont(lblJ1.getFont().deriveFont(Font.BOLD));
                lblJ2.setBackground(Color.LIGHT_GRAY);
                lblJ2.setFont(lblJ2.getFont().deriveFont(Font.PLAIN));
            } else {
                lblJ2.setBackground(Color.YELLOW);
                lblJ2.setFont(lblJ2.getFont().deriveFont(Font.BOLD));
                lblJ1.setBackground(Color.LIGHT_GRAY);
                lblJ1.setFont(lblJ1.getFont().deriveFont(Font.PLAIN));
            }
        } catch (Exception ex) {
            // jeu peut être null en cas de construction très précoce; ignorer
        }

        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                tabIP[x][y].setFront(null);

                Case c = plateau.getCases()[x][y];
                if (c != null) {
                    // choisir le fond suivant le biome (utilise le name() pour éviter d'accéder au type enum package-private)
                    String biomeName = c.getBiome().name();
                    switch (biomeName) {
                        case "DESERT":
                            tabIP[x][y].setBackground(icoDesert);
                            break;
                        case "FORET":
                            tabIP[x][y].setBackground(icoGreen);
                            break;
                        case "MONTAGNE":
                            tabIP[x][y].setBackground(icoMontagne);
                            break;
                        case "PLAINE":
                        default:
                            tabIP[x][y].setBackground(icoPlaine);
                            break;
                    }

                    Unites u = c.getUnites();
                    if (u instanceof Elfes) {
                        tabIP[x][y].setFront(icoElfes);
                    } else if (u instanceof Humain) {
                        tabIP[x][y].setFront(icoHumain);
                    } else if (u instanceof Nain) {
                        tabIP[x][y].setFront(icoNain);
                    } else if (u instanceof Gobelin) {
                        tabIP[x][y].setFront(icoGobelin);
                    }
                    // afficher le nombre d'unités sur la case
                    tabIP[x][y].setUnitCount(c.getNbUnites());
                } else {
                    tabIP[x][y].setBackground(icoPlaine);
                    tabIP[x][y].setUnitCount(0);
                }
            }
        }

        // update title with tour and winner if any
            try {
                setTitle("Smallworld - Tour: " + jeu.getTourNumber() + "/" + jeu.getMaxTours());
                if (jeu.isGameOver()) {
                    Joueur w = jeu.getWinner();
                    String winnerText;
                    if (w == jeu.getJ1()) {
                        winnerText = "Winner: J1";
                    } else if (w == jeu.getJ2()) {
                        winnerText = "Winner: J2";
                    } else {
                        winnerText = "Winner: Tie";
                    }
                    lblWinner.setText(winnerText);
                    // show modal dialog once
                    if (!gameOverDialogShown) {
                        gameOverDialogShown = true;
                        JOptionPane.showMessageDialog(VueControleur.this, winnerText + "\nPoints - J1: " + jeu.getJ1().getPoints() + "  J2: " + jeu.getJ2().getPoints(), "Game Over", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    lblWinner.setText("");
                }
            } catch (Exception ex) {
                // ignore
            }

        grilleIP.repaint();
    }

    // Efface tous les surlignages
    private void clearHighlights() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                tabIP[x][y].setHighlighted(false);
            }
        }
        grilleIP.repaint();
    }

    // Met en surbrillance les cases fournies
    private void highlightCases(java.util.List<Case> lst) {
        clearHighlights();
        if (lst == null) return;
        for (Case c : lst) {
            // chercher la case dans la grille pour retrouver ses coordonnées
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    if (plateau.getCases()[x][y] == c) {
                        tabIP[x][y].setHighlighted(true);
                    }
                }
            }
        }
        grilleIP.repaint();
    }

    // Retourne true si la case donnée est actuellement surlignée
    private boolean tabContainsCaseHighlighted(Case c) {
        if (c == null) return false;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                if (plateau.getCases()[x][y] == c && tabIP[x][y].isHighlighted()) return true;
            }
        }
        return false;
    }

    @Override
    public void update(Observable o, Object arg) {

        SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // nettoyer les sélections actives puis rafraîchir l'affichage
                        clearHighlights();
                        caseClic1 = null;
                        caseClic2 = null;
                        mettreAJourAffichage();
                    }
                }); 

    }
}
