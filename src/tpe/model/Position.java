/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.model;

import java.util.ArrayList;
import java.util.List;
import tpe.Utils;

/**
 * classe permettant de définir une position sur la map
 * @author fred
 */
public class Position {
/**
 * liste des propriétés de la classe
 * 
 */
    private int x;
    private int y;
/**
 * constructeur de la classe 
 * @param x : coordonnée abcisse(int)
 * @param y : coordonnée ordonné(int)
 */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
/**
 * liste l'ensemble des positions autour d'une position
 * @param range : portée exprimé en nombre de case (même valeur en x et y)
 * @return liste des positions voisines
 */
    public List<Position> findNeighbours(int range) {
        List<Position> out = new ArrayList<>();
        for (int i = -range; i <= range; i++) {
            for (int j = -range; j <= range; j++) {
                if (i == 0 && j==0) {

                } else {
                    out.add(new Position(x + i, y + j));
                    
                }
            }
        }
        return out;
    }
/**
 * effectue le déplacement de position initiale a position suivante
 * @param dx : valeur de la translation en x
 * @param dy : valeur de la translation en y 
 */
    public void move(int dx, int dy) {
        setX(getX() + dx);
        setY(getY() + dy);
    }
/**
 * permet de tirer aléatoirement un deplacement dans un intervalle defini en X et en Y
 * @param dxmin : deplac min X
 * @param dxmax : deplac max X
 * @param dymin : deplac min Y
 * @param dymax : deplac max Y
 * @return deplacement en X et Y [dx, dy]
 */
    public int[] randomMove(int dxmin, int dxmax, int dymin, int dymax) {
        int dx = randomMove(dxmin, dxmax);
        int dy = randomMove(dymin, dymax);
        return new int[]{dx, dy};
    }
/**
 * tirage du deplacement aleatoir selon axe quelconque entre valeur min et valeur max (version récente)
 * @param dmin : valeur minimum de deplacement (peut être negative)
 * @param dmax : valeur maximum de deplacement
 * @return valeur du deplacement selon axe quelconque
 */
    public int randomMove(int dmin, int dmax) {
        int range = (dmax - dmin) + 1;
        return dmin + Utils.draw(range) ;
    }

    
    /**
     * les methodes de comparaison de l'objet qui se base sur propriété de l'objet(coordonnée) uniquement et non pas sur l'instance de la classe
     * @return clé d'indexation
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.getX();
        hash = 67 * hash + this.getY();
        return hash;
    }
/**
 * 
 * @param obj
 * @return 
 */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Position other = (Position) obj;
        if (this.getX() != other.getX()) {
            return false;
        }
        if (this.getY() != other.getY()) {
            return false;
        }
        return true;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(int y) {
        this.y = y;
    }
/**
 * test de proximité
 * @param pos : position a tester
 * @param range : porté de la zone de test(en nombre de cases, identique pour x et y)
 * @return True si positions dans limite zone de test sinon false
 */
    public boolean isNearFrom(Position pos, int range) {
        int xmin = x - range;
        int xmax = x + range;
        int ymin = y - range;
        int ymax = y + range;

        return !(pos.x < xmin || pos.x > xmax || pos.y < ymin || pos.y > ymax);
    }
    /**
     * permet d'afficher coordonnées dans une chaine (module de vérification)
     * @return string
     */
    @Override
    public String toString() {
        return "x=" + x + " ; y=" + y;
    }

}
