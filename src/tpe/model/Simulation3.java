/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;
import tpe.Utils;
import static tpe.model.TypeEspece.EXPONENTIAL;
import static tpe.model.TypeEspece.LINEAR;

/**
 *
 * @author fred
 */
public class Simulation3 {

    /**
     * @return the numbers
     */
    public ObservableMap<TypeEspece, Integer> getNumbers() {
        return numbers;
    }
/**
 * 
 * variable de la classe
 * map : objet map
 * positions : dictionnaire des associations positions espece
 * numbers : dictionnaire du nombre total d'individus de chaque espece sur la map
 * free : positions libres sur la map
 */
    // reference to the map
    private final GeoMap map;
    // Give the species for each occupied position in the map
    private final ObservableMap<Position, TypeEspece> positions;
    // Give the overall number of each species on the map
    private final ObservableMap<TypeEspece, Integer> numbers;

    private final ObservableList<Position> free;
/**
 * constructeur de la classe
 * init de l'ensemble des dictionnaires
 * création des differents individus de chaque espece aleatoirement sur la map
 * @param map : objet map
 * @param types : liste des especes en presence
 * 
 */
    public Simulation3(GeoMap map, ObservableList<TypeEspece> types) {
        free = FXCollections.observableList(map.getPositions());
        Map<Position, TypeEspece> positionMap = Collections.synchronizedMap(new HashMap<>());
        positions = FXCollections.observableMap(positionMap);
        Map<TypeEspece, Integer> nbs = Collections.synchronizedMap(new HashMap<>());
        numbers = FXCollections.observableMap(nbs);
        this.map = map;
        Position tmp;
        for (TypeEspece type : types) {
            int nb = type.getInitNumberValue();
            for (int i = 0; i < nb; i++) {
                map.generateRandomPosition(type, positions, free);

            }
            numbers.put(type, nb);
        }
        
    }

    /**
     * synchronized utilise pour eviter conflit d'acces dans les listes 
     * manageInteraction : methode(fonction) 
     * moveAllposition: methode de deplacement des differents individus sur la map
     * changepopulation: methode de croissance et decroissance de la population selon les taux
     * next: effectue à chaque tour l'ensemble de ces methodes
     */
    public synchronized void next() {

        /**
         * detect neighnours and adapt behaviour
         */
        manageInteraction();
        System.out.println("Interaction step");
        System.out.println(printNumbers());
        /**
         * move all positions
         */
        moveAllPosition();
        System.out.println("Moving step");
//        System.out.println(printNumbers());
        /**
         * population evolution
         */
        changePopulation();
        System.out.println("change population step");
//        System.out.println(printNumbers());

    }
/**
 * pour chaque couple position(clé), espece(valeur) :
 * on test si il y a une interaction avec les elements voisins
 * pour chaque voisin on determine la position-> l'espece-> interaction
 * en fonction du type d'interaction on effectue un traitement particulier
 * dans le cas MANGE on test si il y a interaction(selon prob inter)
 * si oui on ajoute l'element voisin à la liste des elements à supprimer et on ajoute un elements à la liste des individus à generer
 */
    private synchronized void manageInteraction() {
        List<Position> removed = new ArrayList<>();
        List<TypeEspece> added = new ArrayList<>();
        for (Entry<Position, TypeEspece> entry : positions.entrySet()) {
            Position pos = entry.getKey();
            TypeEspece tpe = entry.getValue();
            if (tpe.getPredator().get()) {
                List<Position> neigh = pos.findNeighbours(1);
                for (Position pn : neigh) {

                    TypeEspece tesp = positions.get(pn);
                    Interaction inter = tpe.getInteraction(tesp);
                    if (inter != null) {
                        switch (inter.getType().get()) {
                            case MANGE:
                                int res = Utils.draw(100);
                                if (res < inter.getProbability().get()) {
                                    removed.add(pn);
                                    added.add(tpe);
                                }
                                break;
                        }
                    }
                }

            }

        }
/**
 * pour chaque individu dans la liste des individus à supprimer:
 * on test si l'espece existe
 * si oui on retire un individus du nombre total de l'espece et on marque comme libre la position qui lui est associe
 */
        

        for (Position pos : removed) {

            TypeEspece type = positions.get(pos);
            if (type != null) {
                int value = getNumbers().get(type) - 1;                
                positions.remove(pos);
                free.add(pos);
                getNumbers().put(type, value);
            }

        }
/**
 * pour chaque individu à ajouter
 * on verifie si l'espece existe et que son nombre d'individus n'est pas nul
 * et on applique la méthode addSpecs
 */
        for (TypeEspece tpe : getNumbers().keySet()) {
            Integer nb = Collections.frequency(added, tpe);
            if (nb != null && nb > 0) {
                addSpecs(tpe, nb);
            }
  
        }

    }
/**
 * pour chaque position occupé on test si il y a des deplacements possibles
 * si il y en a, on en tire un aléatoirement
 * puis on fait varier l'ancienne position en X et en Y, on enregistre les modifications
 */
    private synchronized void moveAllPosition() {
        Map<Position, Position> newPos = new HashMap<>();
        for (Position pos : getPositions().keySet()) {
            List<int[]> possibleMoves = possibleMoves(pos);
            if (possibleMoves.isEmpty()) {
                // System.out.println("No move");
            } else {
                int index = Utils.draw(possibleMoves.size());
                int[] dpos = possibleMoves.get(index);
                Position newKey = new Position(pos.getX() + dpos[0], pos.getY() + dpos[1]);
                newPos.put(newKey, pos);
            }
/**
 * On enlève la position precedente qu'on remplace par la nouvelle
 * on marque la position précedente comme un espace libre et on marque la nouvelle comme occupé
 */
        }
        for (Entry<Position, Position> entry : newPos.entrySet()) {
            Position pos = entry.getValue();
            TypeEspece tpe = positions.get(pos);
            positions.remove(pos);
            positions.put(entry.getKey(), tpe);
            free.remove(entry.getKey());
            free.add(pos);
        }
    }
/**
 * on marque l'ancienne position comme temporaire
 * on fait une boucle i de -1 à 1 avec indenté une boucle J de -1 à 1
 * On test si la position précédente + le couple I(abcisse) et J(ordonnée) renvoie à une place occupé ou libre
 * si elle elle est libre :
 * @param pos : position
 * @return on enregistre le couple i,j comme le deplacement effectué par l'individus
 */
    private synchronized List<int[]> possibleMoves(Position pos) {
        List<int[]> out = new ArrayList<>();
        Position tmp;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                tmp = new Position(pos.getX() + i, pos.getY() + j);
                if (getPositions().get(tmp) == null && map.isInMap(tmp)) {
                    out.add(new int[]{i, j});
                }
            }
        }
        return out;
    }
/**
 * pour chaque espece on calcul le pourcentage d'evolution que l'on exprime en nombre d'invidus via delta(tjrs lineaire)
 * si delta est inférieur à 0 on affecte à l'espece un nombre d'individu egal a son total+delta(delta etant négatif)
 * si delta est positif ou nulle on affecte la valeur de delta au nombre d'individu à ajouter
 */
    private synchronized void changePopulation() {
        Map<TypeEspece, Integer> plus = new HashMap<>();
        Map<TypeEspece, Integer> minus = new HashMap<>();
        Stream<TypeEspece> stream = getNumbers().keySet().stream().sorted();
        Iterator<TypeEspece> iter = stream.iterator();
        while (iter.hasNext()) {
            TypeEspece tpe = iter.next();
            int nb = getNumbers().get(tpe);
            int delta = 0;
            switch (tpe.getEvolType().get()) {
                case LINEAR:
                    delta = (int) Math.ceil((tpe.getEvolutionRateValue() - 1.) * nb);
                    break;
                case EXPONENTIAL:
                    delta = (int) Math.ceil(nb * Math.exp(tpe.getEvolutionRateValue()));
                    break;
            }
            if (delta < 0) {
                minus.put(tpe, delta);
                getNumbers().put(tpe, nb + delta);
            } else {
                plus.put(tpe, delta);
            }
        }

  /**
   * pour le nombre d'individus à ajouter on applique la méthode add specs
   */      
        for (Entry<TypeEspece, Integer> entry : plus.entrySet()) {
            addSpecs(entry.getKey(), entry.getValue());
        }
/**
 * on créer un nouveau dictionnaire de positions pour l'espece 
 * si la liste de position de l'espece est vide on créer une nouvelle liste auquel on affecte les positions a ajouter
 */
        Map<TypeEspece, List<Position>> espPos = new HashMap<>();
        for (Entry<Position, TypeEspece> pos : positions.entrySet()) {
            TypeEspece tpe = pos.getValue();
            List<Position> posList = espPos.get(tpe);
            if (posList == null) {
                posList = new ArrayList<>();
                espPos.put(tpe, posList);
            }
            posList.add(pos.getKey());
        }
/**
 *on fait le dictionnaire des positions à enlever
 *on ajoute ces positions tant qu'elle appartiennent au dictionnnaires des positions occupés
 *puis on enlève ces positions et on les rajoutes dans les espaces libres
 */
        for (Entry<TypeEspece, Integer> entry : minus.entrySet()) {
            List<Position> posList = espPos.get(entry.getKey());
            int size = posList.size();
            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < -entry.getValue(); i++) {
                int index = -1;
                do {
                    index = Utils.draw(size);
                } while (indexes.contains(index));
                indexes.add(index);
            }
            Collections.sort(indexes);
            for (int i = indexes.size() - 1; i >= 0; i--) {
                Position pos = posList.get(indexes.get(i));
                positions.remove(pos);
                free.add(pos);
            }

        }

    }
/**
 * genere un nombre nb d'individu aleatoirement
 * @param key : espece
 * @param nb  : nombre d'individus à generer
 * pour chaque individu on test si on peut ajouter l'individu(place) 
 * si oui on créer l'individu et on ajoute 1 à k
 * on ajoute k individu au total d'individu de l'espece en question
 */
    private void addSpecs(TypeEspece key, int nb) {
        int value = getNumbers().get(key);
        int k = 0;
        for (int i = 0; i < nb; i++) {
            boolean isOk = this.map.generateRandomPosition(key, positions, free);
            if (isOk) {
                k++;
            }
        }
        getNumbers().put(key, value + k);
    }

    /**
     * @return the positions
     */
    public ObservableMap<Position, TypeEspece> getPositions() {
        return positions;
    }

    public String printNumbers() {

        StringBuilder bd = new StringBuilder("Numbers summary \n");
        for (Entry<TypeEspece, Integer> entry : getNumbers().entrySet()) {
            bd.append(entry.getKey().getName()).append(" : ").append(entry.getValue()).append("\n");
        }
        return bd.toString();

    }
/**
 * methode de test
 * @param args 
 */
    public static void main(String[] args) {
        ObservableList<TypeEspece> data = FXCollections.observableArrayList();
        GeoMap map = new GeoMap(40, 40);
        TypeEspece rabbit = new TypeEspece("Rabbit", 10, 0, 1.3, Color.WHITE);
        data.add(rabbit);
        TypeEspece bobcat = new TypeEspece("Bobcat", 4, 0, 0.6, Color.RED);
        bobcat.addInteraction(rabbit,new Interaction(100,InterType.MANGE));
        data.add(bobcat);
        Simulation3 simu = new Simulation3(map, data);
        for (int i = 0; i < 50; i++) {
            System.out.println("Step " + i);
            simu.next();
            System.out.println("*************");
        }
    }
}
