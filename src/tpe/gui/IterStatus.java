/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.gui;

import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import tpe.model.TypeEspece;

/**
 *
 * @author fred
 */
public class IterStatus {
    private Integer number;
    private SimpleMapProperty<TypeEspece,Integer> data;

    public IterStatus(Integer number, Map<TypeEspece, Integer> data) {
        this.number = number;
        Map<TypeEspece,Integer> map = new HashMap<>();
        data.entrySet().forEach((entry) -> {
            map.put(entry.getKey(), entry.getValue());
        });
        this.data = new SimpleMapProperty<>(FXCollections.observableMap(map));
    }
    
    

    /**
     * @return the number
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * @return the data
     */
    public SimpleMapProperty<TypeEspece,Integer> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(SimpleMapProperty<TypeEspece,Integer> data) {
        this.data = data;
    }
    
    
}
