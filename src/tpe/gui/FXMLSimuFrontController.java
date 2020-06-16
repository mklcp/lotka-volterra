/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.gui;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.converter.IntegerStringConverter;
import tpe.Utils;
import tpe.model.GeoMap;
import tpe.model.InterType;
import tpe.model.Interaction;
import tpe.model.Position;
import tpe.model.Simulation3;
import tpe.model.TypeEspece;

/**
 * FXML Controller class
 *
 * @author fred
 */
public class FXMLSimuFrontController implements Initializable {

    @FXML
    private TableView<TypeEspece> tableSpecs;

    //Species Edition
    @FXML
    private TextField txtName;

    @FXML
    private TextField txtEvolRate;

    @FXML
    private TextField txtInit;

    @FXML
    private ColorPicker colorPick;

    @FXML
    private TableView<Map.Entry<TypeEspece, Interaction>> tableInter;
    @FXML
    private TableColumn<Map.Entry<TypeEspece, Interaction>, InterType> colInterType;
    @FXML
    private TableColumn<Map.Entry<TypeEspece, Interaction>, Integer> colInterProba;

    @FXML
    private TableColumn<Map.Entry<TypeEspece, Interaction>, TypeEspece> colInterEsp;

    @FXML
    private TableColumn<TypeEspece, SimpleStringProperty> colName;
    @FXML
    private TableColumn<TypeEspece, SimpleDoubleProperty> colRate;
    @FXML
    private TableColumn<TypeEspece, SimpleIntegerProperty> colInit;
    @FXML
    private TableColumn<TypeEspece, SimpleObjectProperty<Color>> colColor;

    @FXML
    private TextField txtSizeX;

    @FXML
    private TextField txtSizeY;

    @FXML
    private TextField txtNbLoop;

    @FXML
    private Button btInitMap;

    @FXML
    private Button btInitSimu;

    @FXML
    private Button btWriteOnFile;

    @FXML
    private Button btClear;

    @FXML
    private LineChart<Integer, Integer> chart;

    @FXML
    private AnchorPane pane;

    @FXML
    private Canvas canvas;

    @FXML
    private TableView<IterStatus> table;

    private TableColumn<IterStatus, Integer> colStep;

    private Map<TypeEspece, TableColumn<IterStatus, Integer>> columns;

    private Map<TypeEspece, Canvas> specCanvas;

    private final ObservableList<TypeEspece> species = FXCollections.observableArrayList();

    private final ObservableList<IterStatus> data = FXCollections.observableArrayList();

    private DecimalFormat format = new DecimalFormat("0.000");

    //Map size
    private GeoMap map;
    private final double offset = 1.0;
    private double mapSizeX = 2. * offset;
    private double mapSizeY = 2. * offset;
    private int pitch = 15;

    private int nbLoop = 1;
    private boolean RUN = false;
    
    private TypeEspece selectedSpecies;

    private final Service<IterStatus> calculateService = new Service<IterStatus>() {

        @Override
        protected Task<IterStatus> createTask() {
            return new Task<IterStatus>() {

                @Override
                protected IterStatus call() throws Exception {
                    IterStatus status = null;
                    System.out.println("number of loops " + nbLoop);
                    int j =0;
                    while (j<nbLoop && RUN) {
                        simu.next();
                        drawPosition(simu.getPositions());
                        status = new IterStatus(j + 1, simu.getNumbers());
                        //iter = new StatusIter(j + 1, simulation.getNbPreys(), simulation.getNbPredators());
                        updateValue(status);
                        j++;
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ie) {

                        }
                    }
                    return status;
                }
            };
        }

    };
    private Simulation3 simu;
    private Map<TypeEspece, XYChart.Series> series;
    private ObservableList<Entry<TypeEspece, Interaction>> interItems;

    Callback<TableColumn<IterStatus, Integer>, TableCell<IterStatus, Integer>> intRenderer = (TableColumn<IterStatus, Integer> param) -> new TableCell<IterStatus, Integer>() {
        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
            if (!empty) {
                setText(item + "");
            } else {
                setText("");
            }
        }

    };

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initSpeciesTable();
        columns = new HashMap<>();
        tableInter.setEditable(true);
        initSpeciesData();
        initTable();

        txtSizeX.setText("40");
        txtSizeY.setText("40");
        initMap();
        calculateService.stateProperty()
                .addListener((ObservableValue<? extends Worker.State> observableValue, Worker.State oldValue, Worker.State newValue) -> {
                    switch (newValue) {
                        case FAILED:
                        case CANCELLED:
                        case SUCCEEDED:
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {

                                }
                            });
                            break;
                    }

                }
                );
        calculateService.valueProperty()
                .addListener(new ChangeListener<IterStatus>() {
                    @Override
                    public void changed(ObservableValue<? extends IterStatus> observable, IterStatus oldValue, IterStatus newValue) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                if (newValue != null) {
                                    drawOnChart(newValue);
                                    data.add(newValue);
                                    //species.add(newValue);
                                }
                            }
                        });
                    }
                });

        // TODO
    }

    @FXML
    public void onMapInit(ActionEvent ev) {
        initMap();
    }

    @FXML
    public void onSimuInit(ActionEvent ev) {
        series = new HashMap<>();
        for (TypeEspece tpe : species) {
            XYChart.Series serie = new XYChart.Series();
            serie.setName(tpe.getNameValue());

            chart.getData().addAll(serie);
            series.put(tpe, serie);
        }
        initSimulation();
        RUN=true;
        calculateService.restart();

    }

    @FXML
    public void onWriteOnFile(ActionEvent ev) {
        FileChooser chooser = new FileChooser();
        File file = chooser.showSaveDialog(null);
        Utils.storeDataInFile(data, file, species);
    }

    @FXML
    public void addNewSpecies(ActionEvent ev) {
        TypeEspece type = new TypeEspece("New name", 2, TypeEspece.LINEAR, 1.0, Color.BISQUE);
        species.add(type);
        editSpecies(type);
        addColumn(type);

    }

    @FXML
    public void deleteSpecies(ActionEvent ev) {
        TypeEspece tpe = tableSpecs.getSelectionModel().getSelectedItem();
        if (tpe != null) {
            species.remove(tpe);
            TableColumn<IterStatus, Integer> col = columns.get(tpe);
            table.getColumns().removeAll(col);
        }
    }

    @FXML
    public void editSelectedSpecies(ActionEvent ev) {
        TypeEspece tpe = tableSpecs.getSelectionModel().getSelectedItem();
        if (tpe != null) {
            editSpecies(tpe);
        }
    }

    @FXML
    public void onSaveAction(ActionEvent ev) {
        if (selectedSpecies != null) {
            selectedSpecies.setName(txtName.getText());
            selectedSpecies.setInitNumber(Integer.parseInt(txtInit.getText()));
            selectedSpecies.setEvolutionRate(Double.parseDouble(txtEvolRate.getText()));
            selectedSpecies.setColor(colorPick.getValue());
            selectedSpecies.clearInteractions();
            for (Entry<TypeEspece, Interaction> inter : interItems) {
                selectedSpecies.addInteraction(inter.getKey(), inter.getValue());
            }

            columns.get(selectedSpecies).setText(selectedSpecies.getNameValue());
            tableSpecs.refresh();
        }
    }

    @FXML
    public void onAddInteraction(ActionEvent ev) {
        if (interItems != null) {

            ChoiceDialog<TypeEspece> dialog = new ChoiceDialog<>(selectedSpecies, species);
            dialog.setTitle("New Interaction");
            dialog.setHeaderText("Interaction");
            dialog.setContentText("Choose the interacting species :");

            // Traditional way to get the response value.
            Optional<TypeEspece> result = dialog.showAndWait();
            if (result.isPresent()) {
                SimpleEntry entry = new SimpleEntry(result.get(), new Interaction(100, InterType.MANGE));
                interItems.add(entry);
            }
        }
    }

    @FXML
    public void deleteInteraction(ActionEvent ev) {
        Entry<TypeEspece, Interaction> tpe = tableInter.getSelectionModel().getSelectedItem();
        if (tpe != null) {
            interItems.remove(tpe);
        }
    }

    public void initSimulation() {
        nbLoop = Integer.parseInt(txtNbLoop.getText());
        simu = new Simulation3(map, species);
        drawPosition(simu.getPositions());

    }

    public void initSpeciesTable() {
        Callback<TableColumn<TypeEspece, SimpleStringProperty>, TableCell<TypeEspece, SimpleStringProperty>> strRenderer = (TableColumn<TypeEspece, SimpleStringProperty> param) -> new TableCell<TypeEspece, SimpleStringProperty>() {
            @Override
            protected void updateItem(SimpleStringProperty item, boolean empty) {
                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                if (!empty) {
                    setText(item.get());
                } else {
                    setText("");
                }
            }

        };

        Callback<TableColumn<TypeEspece, SimpleIntegerProperty>, TableCell<TypeEspece, SimpleIntegerProperty>> intRenderer = (TableColumn<TypeEspece, SimpleIntegerProperty> param) -> new TableCell<TypeEspece, SimpleIntegerProperty>() {
            @Override
            protected void updateItem(SimpleIntegerProperty item, boolean empty) {
                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                if (!empty) {
                    setText("" + item.get());
                } else {
                    setText("");
                }
            }

        };
        Callback<TableColumn<TypeEspece, SimpleDoubleProperty>, TableCell<TypeEspece, SimpleDoubleProperty>> doubleRenderer = (TableColumn<TypeEspece, SimpleDoubleProperty> param) -> new TableCell<TypeEspece, SimpleDoubleProperty>() {
            @Override
            protected void updateItem(SimpleDoubleProperty item, boolean empty) {
                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                if (!empty) {
                    setText("" + format.format(item.get()));
                } else {
                    setText("");
                }
            }

        };
        Callback<TableColumn<TypeEspece, SimpleObjectProperty<Color>>, TableCell<TypeEspece, SimpleObjectProperty<Color>>> colorRenderer = (TableColumn<TypeEspece, SimpleObjectProperty<Color>> param) -> new TableCell<TypeEspece, SimpleObjectProperty<Color>>() {
            @Override
            protected void updateItem(SimpleObjectProperty<Color> item, boolean empty) {
                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                if (!empty) {
                    setText(item.get().toString());
                    setBackground(new Background(new BackgroundFill(item.get(), CornerRadii.EMPTY, Insets.EMPTY)));
                } else {
                    setText("");
                    setBackground(Background.EMPTY);
                }
            }

        };

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setCellFactory(strRenderer);
        colInit.setCellValueFactory(new PropertyValueFactory<>("initNumber"));
        colInit.setCellFactory(intRenderer);
        colRate.setCellValueFactory(new PropertyValueFactory<>("evolutionRate"));
        colRate.setCellFactory(doubleRenderer);
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colColor.setCellFactory(colorRenderer);
        tableSpecs.setItems(species);
        tableSpecs.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                tableSpecs.getSelectionModel().getSelectedItem();
            }
        });
    }

    public void initSpeciesData() {
        TypeEspece a = new TypeEspece("A", 40, 0, 1.2, Color.SALMON);
        TypeEspece b = new TypeEspece("B", 40, 0, 1.1, Color.THISTLE);
        TypeEspece c = new TypeEspece("C", 30, 0, 0.9, Color.TAN);
        TypeEspece d = new TypeEspece("D", 40, 0, 1.2, Color.MINTCREAM);
        TypeEspece e = new TypeEspece("E", 40, 0, 1.2, Color.DARKKHAKI);
        TypeEspece f = new TypeEspece("F", 40, 0, 1.1, Color.FIREBRICK);
        TypeEspece g = new TypeEspece("G", 30, 0, 0.9, Color.GAINSBORO);
        TypeEspece h = new TypeEspece("H", 40, 0, 1.2, Color.ANTIQUEWHITE);
        TypeEspece i = new TypeEspece("I", 40, 0, 1.2, Color.LIGHTGOLDENRODYELLOW);
        TypeEspece j = new TypeEspece("J", 40, 0, 1.1, Color.ALICEBLUE);
        TypeEspece k = new TypeEspece("K", 30, 0, 0.9, Color.BLANCHEDALMOND);
        TypeEspece l = new TypeEspece("L", 40, 0, 1.2, Color.BURLYWOOD);
        TypeEspece m = new TypeEspece("M", 40, 0, 1.2, Color.BISQUE);
        TypeEspece n = new TypeEspece("N", 40, 0, 1.1, Color.CRIMSON);
        TypeEspece o = new TypeEspece("O", 30, 0, 0.9, Color.CHARTREUSE);
        TypeEspece p = new TypeEspece("P", 40, 0, 1.2, Color.DARKGOLDENROD);

       	a.addInteraction(p, new Interaction(100, InterType.MANGE));

        b.addInteraction(a, new Interaction(80, InterType.MANGE));
        b.addInteraction(p, new Interaction(70, InterType.MANGE));

        c.addInteraction(a, new Interaction(60, InterType.MANGE));
        c.addInteraction(b, new Interaction(50, InterType.MANGE));
        c.addInteraction(p, new Interaction(40, InterType.MANGE));

        d.addInteraction(p, new Interaction(50, InterType.MANGE));
        d.addInteraction(a, new Interaction(60, InterType.MANGE));
        d.addInteraction(b, new Interaction(70, InterType.MANGE));
        d.addInteraction(c, new Interaction(80, InterType.MANGE));

        e.addInteraction(d, new Interaction(100, InterType.MANGE));

        f.addInteraction(d, new Interaction(80, InterType.MANGE));
        f.addInteraction(e, new Interaction(70, InterType.MANGE));

        g.addInteraction(d, new Interaction(60, InterType.MANGE));
        g.addInteraction(e, new Interaction(50, InterType.MANGE));
        g.addInteraction(f, new Interaction(40, InterType.MANGE));

        h.addInteraction(d, new Interaction(50, InterType.MANGE));
        h.addInteraction(e, new Interaction(60, InterType.MANGE));
        h.addInteraction(f, new Interaction(70, InterType.MANGE));
        h.addInteraction(g, new Interaction(80, InterType.MANGE));

       	i.addInteraction(h, new Interaction(100, InterType.MANGE));

        j.addInteraction(h, new Interaction(80, InterType.MANGE));
        j.addInteraction(i, new Interaction(70, InterType.MANGE));

        k.addInteraction(h, new Interaction(60, InterType.MANGE));
        k.addInteraction(i, new Interaction(50, InterType.MANGE));
        k.addInteraction(j, new Interaction(40, InterType.MANGE));

        l.addInteraction(h, new Interaction(50, InterType.MANGE));
        l.addInteraction(i, new Interaction(60, InterType.MANGE));
        l.addInteraction(j, new Interaction(70, InterType.MANGE));
        l.addInteraction(k, new Interaction(80, InterType.MANGE));

        m.addInteraction(l, new Interaction(100, InterType.MANGE));

        n.addInteraction(l, new Interaction(80, InterType.MANGE));
        n.addInteraction(m, new Interaction(70, InterType.MANGE));

        o.addInteraction(l, new Interaction(60, InterType.MANGE));
        o.addInteraction(m, new Interaction(50, InterType.MANGE));
        o.addInteraction(n, new Interaction(40, InterType.MANGE));

        p.addInteraction(l, new Interaction(50, InterType.MANGE));
        p.addInteraction(m, new Interaction(60, InterType.MANGE));
        p.addInteraction(n, new Interaction(70, InterType.MANGE));
        p.addInteraction(o, new Interaction(80, InterType.MANGE));

        species.add(a);
        species.add(b);
        species.add(c);
        species.add(d);
        species.add(e);
        species.add(f);
        species.add(g);
        species.add(h);
        species.add(i);
        species.add(j);
        species.add(k);
        species.add(l);
        species.add(m);
        species.add(n);
        species.add(o);
        species.add(p);


        
        colInterEsp.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<TypeEspece, Interaction>, TypeEspece>, ObservableValue<TypeEspece>>() {
            @Override
            public ObservableValue<TypeEspece> call(TableColumn.CellDataFeatures<Entry<TypeEspece, Interaction>, TypeEspece> p) {
                return new SimpleObjectProperty<TypeEspece>(p.getValue().getKey());
            }
        });
        colInterEsp.setCellFactory(new Callback<TableColumn<Entry<TypeEspece, Interaction>, TypeEspece>, TableCell<Entry<TypeEspece, Interaction>, TypeEspece>>() {
            @Override
            public TableCell<Entry<TypeEspece, Interaction>, TypeEspece> call(TableColumn<Entry<TypeEspece, Interaction>, TypeEspece> param) {
                return new TableCell<Entry<TypeEspece, Interaction>, TypeEspece>() {
                    @Override
                    protected void updateItem(TypeEspece item, boolean empty) {
                        if (!empty) {
                            setText(item.getNameValue());
                        } else {
                            setText("");
                        }
                    }

                };
            }
        });

        colInterType.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<TypeEspece, Interaction>, InterType>, ObservableValue<InterType>>() {
            @Override
            public ObservableValue<InterType> call(TableColumn.CellDataFeatures<Entry<TypeEspece, Interaction>, InterType> p) {
                return p.getValue().getValue().getType();
            }
        });
        colInterType.setCellFactory(ComboBoxTableCell.forTableColumn(InterType.values()));
        colInterType.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Entry<TypeEspece, Interaction>, InterType>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Entry<TypeEspece, Interaction>, InterType> event) {
                Entry<TypeEspece, Interaction> tmp = event.getRowValue();
                tmp.getValue().setType(event.getNewValue());
            }
        });

        colInterProba.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Entry<TypeEspece, Interaction>, Integer>, ObservableValue<Integer>>() {
            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<Entry<TypeEspece, Interaction>, Integer> p) {
                return p.getValue().getValue().getProbability().asObject();
            }
        });
        colInterProba.setCellFactory(
                TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colInterProba.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Entry<TypeEspece, Interaction>, Integer>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Entry<TypeEspece, Interaction>, Integer> event) {
                Entry<TypeEspece, Interaction> tmp = event.getRowValue();
                tmp.getValue().setProbability(event.getNewValue().intValue());
            }
        });

        TableColumn<Map.Entry<String, String>, String> column2 = new TableColumn<>("Value");
        column2.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                // for second column we use value
                return new SimpleStringProperty(p.getValue().getValue());
            }
        });

    }

    public void initMap() {

        int nbX = Integer.parseInt(txtSizeX.getText().trim());
        int nbY = Integer.parseInt(txtSizeY.getText().trim());
        mapSizeX += nbX * pitch;
        mapSizeY += nbX * pitch;
        //Init maps   
        canvas.setHeight(mapSizeY);
        canvas.setWidth(mapSizeX);

        //
        specCanvas = new HashMap<>();
        for (TypeEspece tpe : species) {
            Canvas cnv = new Canvas();
            cnv.setHeight(mapSizeY);
            cnv.setWidth(mapSizeX);
            pane.getChildren().add(cnv);
            specCanvas.put(tpe, cnv);
        }

        map = new GeoMap(nbX, nbY);

        double sizeX = nbX * pitch + 2. * offset;
        double sizeY = nbY * pitch + 2. * offset;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0., 0., mapSizeX, mapSizeY);
        gc.setFill(new Color(0.2, 1.0, 0.2, 0.6));
        gc.fillRect(0., 0., sizeX, sizeY);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1.);
        gc.strokeRect(0., 0., sizeX, sizeY);

        for (int i = 0; i < nbX; i++) {
            for (int j = 0; j < nbY; j++) {
                gc.strokeRect(offset + i * pitch, offset + j * pitch, pitch, pitch);
            }
        }

    }

    public void initTable() {

        colStep = new TableColumn<>("Step");
        colStep.setCellValueFactory(new PropertyValueFactory<>("Number"));
        colStep.setCellFactory(intRenderer);
        table.getColumns().add(colStep);
        for (TypeEspece type : species) {
            addColumn(type);
        }
        table.setItems(data);

    }

    public void clearMaps() {
        for (Canvas map : specCanvas.values()) {
            GraphicsContext g2c = map.getGraphicsContext2D();
            g2c.clearRect(0, 0, mapSizeX, mapSizeY);
        }
    }

    public void drawOnChart(IterStatus iter) {
        ObservableMap<TypeEspece, Integer> mprop = iter.getData();
        for (TypeEspece tpe : mprop.keySet()) {
            series.get(tpe).getData().add(new XYChart.Data(iter.getNumber(), iter.getData().get(tpe)));
        }

    }

    public void drawPosition(ObservableMap<Position, TypeEspece> positions) {
        clearMaps();
        for (Entry<Position, TypeEspece> entry : positions.entrySet()) {
            drawPosition(entry.getKey(), entry.getValue());
        }
    }

    public void drawPosition(Position pos, TypeEspece tpe) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        GraphicsContext g2c = specCanvas.get(tpe).getGraphicsContext2D();
        if (tpe.getPredator().get()) {
            Color color = tpe.getColor().get();
            g2c.setStroke(color);
            Color alphaRed = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0.3f);
            g2c.setFill(alphaRed);
            g2c.setLineWidth(2.);
            g2c.fillRect(offset + (x - 1) * pitch, offset + (y - 1) * pitch, 3 * pitch, 3 * pitch);
            g2c.strokeLine(offset + 2 + x * pitch, offset + 2 + y * pitch, offset + (x + 1) * pitch - 2, offset - 2 + (y + 1) * pitch);
            g2c.strokeLine(offset + 2 + x * pitch, offset - 2 + (y + 1) * pitch, offset + (x + 1) * pitch - 2, offset + 2 + y * pitch);

        } else {
            RadialGradient radial = new RadialGradient(0, 0., offset + (x + 0.5) * pitch, offset + (y + 0.5) * pitch, pitch / 2, false, CycleMethod.NO_CYCLE, new Stop(0, tpe.getColor().get()), new Stop(1, Color.BLACK));
            g2c.setFill(radial);
            g2c.fillOval(offset + 2 + x * pitch, offset + 2 + y * pitch, pitch - 4, pitch - 4);
        }
    }

    public void removePosition(Position pos, TypeEspece tpe) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        GraphicsContext g2c = specCanvas.get(tpe).getGraphicsContext2D();
        if (tpe.getPredator().get()) {
            g2c.clearRect(offset + (x - 1) * pitch, offset + (y - 1) * pitch, 3 * pitch, 3 * pitch);
        } else {
            g2c.clearRect(offset + x * pitch, offset + y * pitch, pitch, pitch);
        }
    }

    @FXML
    public void clear(ActionEvent ev) {
        RUN=false;
        data.clear();
        chart.getData().clear();
        initSimulation();
        clearMaps();
    }

    @FXML
    public void stop(ActionEvent ev) {
        RUN=false;
    }
    
    public void editSpecies(TypeEspece type) {

        selectedSpecies = type;
        interItems = FXCollections.observableArrayList(selectedSpecies.getInteractions().entrySet());
        tableInter.setItems(interItems);
        txtName.setText(type.getNameValue());
        txtEvolRate.setText(type.getEvolutionRateValue() + "");
        txtInit.setText(type.getInitNumberValue() + "");
        colorPick.setValue(type.getColor().getValue());
    }

    private void addColumn(TypeEspece tpe) {
        TableColumn<IterStatus, Integer> col = new TableColumn<IterStatus, Integer>(tpe.getNameValue());
        col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<IterStatus, Integer>, ObservableValue<Integer>>() {

            @Override
            public ObservableValue<Integer> call(TableColumn.CellDataFeatures<IterStatus, Integer> p) {
                if (p.getValue() != null) {
                    return p.getValue().getData().valueAt(tpe);
                } else {
                    return new SimpleIntegerProperty(0).asObject();
                }
            }
        });
        col.setCellFactory(intRenderer);
        columns.put(tpe, col);
        table.getColumns().add(col);
    }
}
