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

/**
 *
 * @author fred
 */
public class Simulation2 {

    /**
     * @return the numbers
     */
    public ObservableMap<TypeEspece, Integer> getNumbers() {
        return numbers;
    }

    // reference to the map
    private final GeoMap map;
    // Give the species for each occupied position in the map
    private final ObservableMap<Position, TypeEspece> positions;
    // Give the overall number of each species on the map
    private final ObservableMap<TypeEspece, Integer> numbers;

    public Simulation2(GeoMap map, ObservableList<TypeEspece> types) {

        Map<Position, TypeEspece> positionMap = Collections.synchronizedMap(new HashMap<>());
        positions = FXCollections.observableMap(positionMap);
        Map<TypeEspece, Integer> nbs = Collections.synchronizedMap(new HashMap<>());
        numbers = FXCollections.observableMap(nbs);
        this.map = map;
        Position tmp;
        for (TypeEspece type : types) {
            int nb = type.getInitNumberValue();
            for (int i = 0; i < nb; i++) {
                tmp = map.generateRandomPosition(positions);
                positions.put(tmp, type);

            }
            numbers.put(type, nb);
        }
        System.out.println("initialization step");
        System.out.println(printNumbers());
    }

    /**
     * defines the process for each step 1 - interactions 2 - move all positions
     * 3 - modify population considering the evolution rate
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
                                removed.add(pn);
                                added.add(tpe);
                                break;
                        }
                    }
                }

            }

        }
        System.out.println("numbers to remove " + removed.size());
        for (Position pos : removed) {

            TypeEspece type = positions.get(pos);
            if (type != null) {
                int value = getNumbers().get(type) - 1;
                
                System.out.println("position  " + pos);
                positions.remove(pos);
                getNumbers().put(type, value);
            }

        }

        for (TypeEspece tpe : getNumbers().keySet()) {
            Integer nb = Collections.frequency(added, tpe);
            System.out.println("numbers before " + getNumbers().get(tpe));
            System.out.println("numbers to add " + nb);
            if (nb != null && nb > 0) {
                addSpecs(tpe, nb);
            }
            System.out.println("numbers final     " + getNumbers().get(tpe));
        }

    }

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

        }
        for (Entry<Position, Position> entry : newPos.entrySet()) {
            Position pos = entry.getValue();
            TypeEspece tpe = positions.get(pos);
            positions.remove(pos);
            positions.put(entry.getKey(), tpe);
        }
    }

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

    private synchronized void changePopulation() {
        Map<TypeEspece, Integer> plus = new HashMap<>();
        Map<TypeEspece, Integer> minus = new HashMap<>();
        Stream<TypeEspece> stream = getNumbers().keySet().stream().sorted();
        Iterator<TypeEspece> iter = stream.iterator();
        while (iter.hasNext()) {
            TypeEspece tpe = iter.next();
            int nb = getNumbers().get(tpe);
            int delta = (int) Math.round((tpe.getEvolutionRateValue() - 1.) * nb);
            if (delta < 0) {
                minus.put(tpe, delta);
                getNumbers().put(tpe, nb + delta);
            } else {
                plus.put(tpe, delta);
            }
        }
        List<Position> removed = new ArrayList<>();
        for (Entry<Position, TypeEspece> entry : getPositions().entrySet()) {
            Integer number = minus.get(entry.getValue());
            if (number != null && number < 0) {
                removed.add(entry.getKey());
                minus.put(entry.getValue(), ++number);
            }
        }

        for (Entry<TypeEspece, Integer> entry : plus.entrySet()) {
            addSpecs(entry.getKey(), entry.getValue());
        }
        
        removed.forEach((pos) -> {
            positions.remove(pos);
        });


    }

    private void addSpecs(TypeEspece key, int nb) {
        int value = getNumbers().get(key);
        int k = 0;
        for (int i = 0; i < nb; i++) {
            Position tmp = this.map.generateRandomPosition(getPositions());
            if (tmp != null) {
                getPositions().put(tmp, key);
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

    public static void main(String[] args) {
        ObservableList<TypeEspece> data = FXCollections.observableArrayList();
        GeoMap map = new GeoMap(40, 40);
        TypeEspece rabbit = new TypeEspece("Rabbit", 10,0, 1.3, Color.WHITE);
        data.add(rabbit);
        TypeEspece bobcat = new TypeEspece("Bobcat", 4,0, 0.6, Color.RED);
        bobcat.addInteraction(rabbit, new Interaction(100, InterType.MANGE));
        data.add(bobcat);
        Simulation2 simu = new Simulation2(map, data);
        for (int i = 0; i < 50; i++) {
            System.out.println("Step " + i);
            simu.next();
            System.out.println("*************");
        }
    }
}
