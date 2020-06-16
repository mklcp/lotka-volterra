/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.gui;

import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author fred
 */
public class StatusIter {
    
    private SimpleIntegerProperty nbIter;
    private SimpleIntegerProperty nbPreys;
    private SimpleIntegerProperty nbPreds;

    public StatusIter(int nbIter, int nbPreys, int nbPreds) {
        this.nbIter = new SimpleIntegerProperty(nbIter);
        this.nbPreys = new SimpleIntegerProperty(nbPreys);
        this.nbPreds = new SimpleIntegerProperty(nbPreds);
    }

    /**
     * @return the nbIter
     */
    public int getNbIterValue() {
        return getNbIter().get();
    }

    /**
     * @return the nbPreys
     */
    public int getNbPreysValue() {
        return getNbPreys().get();
    }

    /**
     * @return the nbPreds
     */
    public int getNbPredsValue() {
        return getNbPreds().get();
    }

    /**
     * @return the nbIter
     */
    public SimpleIntegerProperty getNbIter() {
        return nbIter;
    }

    /**
     * @param nbIter the nbIter to set
     */
    public void setNbIter(SimpleIntegerProperty nbIter) {
        this.nbIter = nbIter;
    }

    /**
     * @return the nbPreys
     */
    public SimpleIntegerProperty getNbPreys() {
        return nbPreys;
    }

    /**
     * @param nbPreys the nbPreys to set
     */
    public void setNbPreys(SimpleIntegerProperty nbPreys) {
        this.nbPreys = nbPreys;
    }

    /**
     * @return the nbPreds
     */
    public SimpleIntegerProperty getNbPreds() {
        return nbPreds;
    }

    /**
     * @param nbPreds the nbPreds to set
     */
    public void setNbPreds(SimpleIntegerProperty nbPreds) {
        this.nbPreds = nbPreds;
    }
    
}
