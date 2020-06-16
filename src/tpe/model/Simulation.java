/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tpe.Utils;

/**
 *
 * @author fred
 */
public class Simulation {

    private List<Position> predators;
    private List<Position> preys;
    private GeoMap map;
    private double deathRate;
    private double increaseRate;
    private int nbLoop;

    
    
    public Simulation(int nbLoop, int sizeX, int sizeY, int nbPred, int nbPreys, double deathRate, double increaseRate) {
        this.nbLoop=nbLoop;
        map = new GeoMap(sizeX, sizeY);
        predators = new ArrayList<>();
        preys = new ArrayList<>();
        for (int i = 0; i < nbPred; i++) {
            predators.add(map.generateRandomPosition(predators, preys));
        }
        for (int i = 0; i < nbPreys; i++) {
            preys.add(map.generateRandomPosition(predators, preys));
        }
        this.deathRate = 100-deathRate;
        this.increaseRate = increaseRate;
    }
    


    public void next() {
        //Test des positions
        int nbPred = 0;
        List<Position> removed = new ArrayList<>();
        for (Position posPred : predators) {
            for (Position posPrey : preys) {
                if (posPred.isNearFrom(posPrey, 1)) {
                    removed.add(posPrey);
                    nbPred++;
                }
            }
        }
        System.out.println("generate nbPred " + nbPred);
        for (int i = 0; i < nbPred; i++) {
            Position pos = map.generateRandomPosition(predators, preys);
            if (pos != null) {
                predators.add(pos);
            }
        }
        preys.removeAll(removed);
        /**
         * ******************
         */
        move(predators);
        move(preys);

        changePop(predators, deathRate);
        changePop(preys, increaseRate);
    }

    public void changePop(List<Position> pop, double rate) {
        int size = (int) Math.rint(pop.size() * rate);
        int delta = size - pop.size();
        if (delta < 0) {
            for (int i = 0; i < -delta; i++) {
                pop.remove(0);
            }
        } else {
            for (int i = 0; i < delta; i++) {
                Position pos = map.generateRandomPosition(predators, preys);
                if (pos != null) {
                    pop.add(pos);
                }
            }
        }

    }

    public void move(List<Position> positions) {
        for (Position pos : positions) {
            Position tmp;
            List<int[]> possibleMoves = possibleMoves(pos);
            if (possibleMoves.isEmpty()) {
                // System.out.println("No move");
            } else {
                int index = Utils.draw(possibleMoves.size());
                int[] dpos = possibleMoves.get(index);
                pos.move(dpos[0], dpos[1]);
            }

        }
    }

    public List<int[]> possibleMoves(Position pos) {
        List<int[]> out = new ArrayList<>();
        Position tmp;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; i <= 1; i++) {
                tmp = new Position(pos.getX() + i, pos.getY() + j);
                if (!predators.contains(tmp) && !preys.contains(tmp) && map.isInMap(tmp)) {
                    out.add(new int[]{i, j});
                }
            }
        }
        return out;
    }
/**
 * 
 * @param args 
 */
    public static void main(String[] args) {
        Simulation process = new Simulation(20,40, 40, 4, 10, 0.8, 1.4);
        
    }

    public String toString(List<Position> positions) {
        StringBuilder sb = new StringBuilder();
        for (Position pos : positions) {
            sb.append(pos.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * @return the predators
     */
    public List<Position> getPredators() {
        return predators;
    }

    /**
     * @return the preys
     */
    public List<Position> getPreys() {
        return preys;
    }

    /**
     * @return the nbPredators
     */
    public int getNbPredators() {
        return predators.size();
    }

    /**
     * @return the nbPreys
     */
    public int getNbPreys() {
        return preys.size();
    }

    /**
     * @return the nbLoop
     */
    public int getNbLoop() {
        return nbLoop;
    }

}
