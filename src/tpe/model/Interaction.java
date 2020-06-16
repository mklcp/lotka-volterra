/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * classe permettant de décrire une interaction
 * @author fred
 */
public class Interaction {
      /**
       * liste des proprietes de la classe
       * probability: probabilité d'interaction en pourcentage(entier) 
       * type: type d'interaction
       */
      private SimpleIntegerProperty probability;
      private SimpleObjectProperty<InterType> type;
      /**
       * constructeur(créateur) de la classe Interaction
       * @param proba : probabilite d'interaction en pourcentage(entier)
       * @param type  : type d'interaction
       */
    public Interaction(int proba, InterType type) {
        this.probability =new SimpleIntegerProperty(proba);
        this.type = new SimpleObjectProperty<>(type);
    }

    /**
     * @return the probability
     */
    public SimpleIntegerProperty getProbability() {
        return probability;
    }

    /**
     * @param probability the probability to set
     */
    public void setProbability(SimpleIntegerProperty probability) {
        this.probability = probability;
    }

    /**
     * permet d'affecter la valeur de probabilité d'interaction de l'objet
     * @param probability : valeur de la probabilité (entier)
     */
    public void setProbability(Integer probability) {
        this.probability.set(probability);
    }
    /**
     * @return the type
     */
    public SimpleObjectProperty<InterType> getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(SimpleObjectProperty<InterType> type) {
        this.type = type;
    }
    
        /**
     * permet d'affecter le type d'interaction de l'objet
     * @param type : type d'interaction
     */
    public void setType(InterType type) {
        this.type.set(type);
    }

    
      
      
      
}
