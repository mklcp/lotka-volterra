/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.gui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import tpe.model.InterType;
import tpe.model.Interaction;
import tpe.model.TypeEspece;

/**
 * FXML Controller class
 *
 * @author fred
 */
public class FXMLInteractionController implements Initializable {

    
    @FXML
    private ComboBox<TypeEspece> cmbSpecies;
    
    @FXML
    private ComboBox<InterType> cmbInter;
    
    @FXML
    public void onDeleteInter(ActionEvent ev) {
        
    }
    /**
     * @return the interaction
     */
    public Interaction getInteraction() {
        return interaction;
    }

    /**
     * @param interaction the interaction to set
     */
    public void setInteraction(Interaction interaction) {
        this.interaction = interaction;
    }
    
    private Interaction interaction;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {        
        cmbInter.getItems().addAll(InterType.values());
    }    

    public void setSpecies(ObservableList<TypeEspece> species) {
        cmbSpecies.setItems(species);
    }
    
    
}
