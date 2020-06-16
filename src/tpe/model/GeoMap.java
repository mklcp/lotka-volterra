/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import tpe.Utils;

/**
 * classe permettant de définir une map
 * @author fred
 */
public class GeoMap {

    private int sizeX;
    private int sizeY;

    private int originX = 1;
    private int originY = 1;

    private List<Position> positions;
 
    /**
     * constructeur de la map
     * permet d'instancier, créer un objet map dont les dimensions sont sizeX et sizeY
     * @param sizeX: dimension longueur
     * @param sizeY: dimension largeur
     */
    public GeoMap(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        /*
        boucle permettant de répertorier toutes les positions possibles de la map: stockage liste
        */
        positions = new ArrayList<>();
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                positions.add(new Position(i, j));
            }
        }
    }

    /**
     * fonction permettant de tester si une position est dans les limites de la map
     * @param pos : position a tester
     * @return True si in map  , False si out map
     */
    public boolean isInMap(Position pos) {
        int x = pos.getX();
        int y = pos.getY();
        return (y < (originY + sizeY) && x >= originX && x < (originX + sizeX) && originY <= y);
    }
    /**
     * génère une position aeleatoire
     * @return position genere
     */
    public Position generateRandomPosition() {
        int x = Utils.draw(sizeX) + 1;
        int y = Utils.draw(sizeY) + 1;
        return new Position(x, y);
    }
    /**
     * fonction obsolete utilise avec Simulation1[premiere version prog]
     * @param l1 : première liste de coord proies
     * @param l2 : deuxième liste de coord prédateurs
     * @return si somme des tailles des deux liste < nbmax positions alors
     * on tire des positions tant que les positions tirés appartiennent à une des deux listes
     */
    public Position generateRandomPosition(List<Position> l1, List<Position> l2) {
        Position pos;
        if (l1.size() + l2.size() < maxNbOfPosition()) {

            do {
                pos = generateRandomPosition();
            } while (l1.contains(pos) || l2.contains(pos));
            return pos;
        } else {
            return null;
        }
    }

    /**
     * modifs : on tire une position et on vérifie qu'elle n'appartient pas à la liste des positions déjà occupé.
     * on vérifie que la map peut encore supporter cette position(nombre max de case)
     * @param positions : dictionnaire des positions occupés(map)
     * @return si dans nombre case map < nbmax positions alors
     * on tire des positions tant que les positions tirés appartiennent au dictionnaire
     */
    public Position generateRandomPosition(Map<Position, TypeEspece> positions) {
        Position pos;
        if (positions.size() < maxNbOfPosition()) {
            do {
                pos = generateRandomPosition();
            } while (positions.get(pos) != null);
            return pos;
        } else {
            return null;
        }
    }

    /**
     * récupère liste des positions
     * @return the positions
     */
    public List<Position> getPositions() {
        return positions;
    }
    /**
     * récupère nombre maximum de case de la map
     * @return 
     */
    public int maxNbOfPosition() {
        return sizeX * sizeY;
    }
/**
 * tirage aléatoire d'une position dans les espaces libres, 
 * @param type : espèce concerné
 * @param positions : dictionnaire des positions occupés
 * @param free : espace libre
 * return True si il y a place libre alors on génère la position 
 */
    public boolean generateRandomPosition(TypeEspece type, ObservableMap<Position, TypeEspece> positions, ObservableList<Position> free) {
        if (!free.isEmpty()) {
            int sizefree = free.size();
            int ipos = Utils.draw(sizefree);
            Position pos = free.get(ipos);
            positions.put(pos, type);
            free.remove(pos);
            return true;
        } 
        return false;
    }
}
