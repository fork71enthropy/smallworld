package vuecontroleur;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image imgBackground;
    private Image imgFront;
    private int unitCount = 0;
    private boolean highlighted = false;

    public void setBackground(Image _imgBackground) {
        imgBackground = _imgBackground;
    }

    public void setFront(Image _imgFront) {
        imgFront = _imgFront;
    }

    public void setUnitCount(int c) {
        unitCount = c;
    }

    public void setHighlighted(boolean h) {
        highlighted = h;
    }

    public boolean isHighlighted() {
        return highlighted;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // cadre
        g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 1, 1);

        if (imgBackground != null) {

            g.drawImage(imgBackground, 2, 2, getWidth()-4, getHeight()-4, this);
        }

        if (imgFront != null) {
            g.drawImage(imgFront, 10, 10, (int) (getWidth()*0.5), (int) (getHeight()*0.5), this);
        }

        // dessiner le compteur d'unités en haut à droite uniquement si > 0
        if (unitCount > 0) {
            String s = Integer.toString(unitCount);
            FontMetrics fm = g.getFontMetrics();
            int pad = 6;
            int w = fm.stringWidth(s) + pad * 2;
            int h = fm.getHeight() + pad;
            int x = getWidth() - w - 6;
            int y = 6;
            // fond semi-opaque
            Color prev = g.getColor();
            g.setColor(new Color(255, 255, 255, 200));
            g.fillRoundRect(x, y, w, h, 6, 6);
            g.setColor(Color.BLACK);
            g.drawRoundRect(x, y, w, h, 6, 6);
            // dessiner le texte centré
            int tx = x + pad;
            int ty = y + fm.getAscent() + (h - fm.getHeight())/2;
            g.drawString(s, tx, ty);
            g.setColor(prev);
        }

        // overlay de surlignage si demandé
        if (highlighted) {
            Color prev = g.getColor();
            g.setColor(new Color(255, 255, 0, 80)); // jaune translucide
            g.fillRoundRect(2, 2, getWidth()-4, getHeight()-4, 4, 4);
            g.setColor(new Color(255, 200, 0));
            ((Graphics2D) g).setStroke(new BasicStroke(3f));
            g.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 4, 4);
            ((Graphics2D) g).setStroke(new BasicStroke(1f));
            g.setColor(prev);
        }


    }
}
