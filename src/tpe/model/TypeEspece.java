/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.model;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

/**
 *
 * @author fred
 */
public class TypeEspece implements Comparable<TypeEspece> {

    public static final int LINEAR = 0;
    public static final int EXPONENTIAL = 1;
    
    /**
     * liste des propriétés de la classe
     * name: nom de l'espece
     * initnumber: nombre initial d'individus de l'espece 
     * evolType: non utilisé, différence linéaire, exponential
     * color: couleur associé à l'espece dans partie graph
     * predator: return True si interaction de prédation présente
     */
    private SimpleStringProperty name;
    private SimpleIntegerProperty initNumber;
    private SimpleIntegerProperty evolType;
    private SimpleDoubleProperty evolutionRate;
    private SimpleObjectProperty<Color> color;
    private SimpleBooleanProperty predator;
    
        /**
     * dictionnaire interactions lié à l'espece
     */
    private Map<TypeEspece,Interaction> interactions;
    
    /**
     * @return the evolType
     */
    public SimpleIntegerProperty getEvolType() {
        return evolType;
    }

    /**
     * @param evolType the evolType to set
     */
    public void setEvolType(SimpleIntegerProperty evolType) {
        this.evolType = evolType;
    }



    /**
     * @return the interactions
     */
    public Map<TypeEspece,Interaction> getInteractions() {
        return interactions;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * @param initNumber the initNumber to set
     */
    public void setInitNumber(int initNumber) {
        this.initNumber.set(initNumber);
    }

    /**
     * @param evolutionRate the evolutionRate to set
     */
    public void setEvolutionRate(double evolutionRate) {
        this.evolutionRate.set(evolutionRate);
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color.set(color);
    }

    /**
     * @return the predator
     */
    public SimpleBooleanProperty getPredator() {
        return predator;
    }
    /**
     * constructeur de l'espece
     * permet d'instancier, créer une espece
     * @param name 
     * @param initNb
     * @param type
     * @param evolutionRate
     * @param color
     * initialisation dictionnaire des interactions
     */
    public TypeEspece(String name, int  initNb, int type, double evolutionRate, Color color) {
        this.name = new SimpleStringProperty(name);
        this.initNumber = new SimpleIntegerProperty(initNb);
        this.evolType = new SimpleIntegerProperty(type);
        this.evolutionRate = new SimpleDoubleProperty(evolutionRate);
        this.color = new SimpleObjectProperty<Color>(color);
        predator = new SimpleBooleanProperty(false);
        interactions = new HashMap<>();
    }

    /**
     * @return the name
     */
    public SimpleStringProperty getName() {
        return name;
    }

    public String getNameValue() {
        return name.get();
    }
    
    /**
     * @return the evolutionRate
     */
    public SimpleDoubleProperty getEvolutionRate() {
        return evolutionRate;
    }
    
    public Double getEvolutionRateValue() {
        return evolutionRate.doubleValue();
    }    
    /**
     * ajout d'une interaction
     * @param esp espece en interaction
     * @param inter  type d'interaction
     * en cas de creation d'une interaction de predation -> predator=True
     */
    public void addInteraction(TypeEspece esp,Interaction inter) {
        getInteractions().put(esp, inter);
        if (inter.getType().get().equals(InterType.MANGE)) {
            predator.set(true);
        }
    } 
    
    public Interaction getInteraction(TypeEspece esp) {
        return getInteractions().get(esp);
    }

    @Override
    /**
     * redefinition de la fonction comparaison pour classer les especes: 
     * le classement se fait selon le taux d'acroissement
     */
    public int compareTo(TypeEspece o) {
        return getEvolutionRateValue().compareTo(o.getEvolutionRateValue());       
    }

    /**
     * @return the initNumber
     */
    public SimpleIntegerProperty getInitNumber() {
        return initNumber;
    }

    public Integer getInitNumberValue() {
        return initNumber.intValue();
    }

    /**
     * @return the color
     */
    public SimpleObjectProperty<Color> getColor() {
        return color;
    }
    
    public void clearInteractions() {
        interactions.clear();
    }

    @Override
    /**
     * redefinition de la méthode toSTring lors de son appel celle ci renvoie mtnt le nom de l'espece
     */
    public String toString() {
        return name.get();
    }
    
    
    
}
