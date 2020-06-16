/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.ObservableList;
import tpe.gui.IterStatus;
import tpe.model.TypeEspece;

/**
 *
 * @author fred
 */
public class Utils {
    /**
     * tirage aleatoire d'un nombre entre 0 et nb-1
     * @param nb: nombre de possibilités du tirage
     * @return valeur aléatoirement tirée
     */
    public static int draw(int nb) {
        double value = Math.random();
        double res = Math.floor(value * nb);
        return (int) res;
    }
    
    public static void storeDataInFile(ObservableList<IterStatus> list, File file, List<TypeEspece> species) {
        try {
            FileWriter writer = new FileWriter(file);
            StringBuilder sb = new StringBuilder();
            sb.append("step");       
            for(TypeEspece tpe : species) {
                sb.append(";").append(tpe.getNameValue());
            }
            sb.append("\n");
            writer.write(sb.toString());
            for (IterStatus iter : list) {
                sb=new StringBuilder();
                sb.append(iter.getNumber()+"");
                SimpleMapProperty<TypeEspece, Integer> data = iter.getData();
                for ( TypeEspece tpe : species) {
                    sb.append(";"+data.get(tpe));
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
