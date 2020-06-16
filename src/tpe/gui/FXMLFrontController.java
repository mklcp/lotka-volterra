/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tpe.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.util.Callback;
import tpe.model.Position;
import tpe.model.Simulation;

/**
 * FXML Controller class
 *
 * @author fred
 */
public class FXMLFrontController implements Initializable {

    // Map size (x,y)
    @FXML
    private TextField txtMapSizeX;
    @FXML
    private TextField txtMapSizeY;

    // Predators
    @FXML
    private TextField txtPredInitNumber;
    @FXML
    private TextField txtPredDeathRate;

    // Preys
    @FXML
    private TextField txtPreyInitNumber;
    @FXML
    private TextField txtPreyDeathRate;

    // Steps
    @FXML
    private TextField txtPredNumber;
    @FXML
    private TextField txtPreyNumber;
    @FXML
    private TextField txtLabel;
    @FXML
    private TextField txtStepNumber;

    @FXML
    private Button btProcess;

    @FXML
    private TableColumn<StatusIter, SimpleIntegerProperty> colStep;

    @FXML
    private TableColumn<StatusIter, SimpleIntegerProperty> colPreys;

    @FXML
    private TableColumn<StatusIter, SimpleIntegerProperty> colPreds;

    @FXML
    private LineChart<Integer, Integer> chart;

    @FXML
    private Canvas canvas;

    @FXML
    private Canvas preyCanvas;

    @FXML
    private Canvas predCanvas;

    @FXML
    private TableView<StatusIter> table;

    private double offset = 1.0;
    private double mapSize = 600 + 2. * offset;
    private int pitch;
    private Simulation simulation;

    private List<Integer> nbPredators;
    private List<Integer> nbPreys;

    private int kStep = 0;
    private XYChart.Series preysSeries;
    private XYChart.Series predSeries;

    final ObservableList<StatusIter> data = FXCollections.observableArrayList();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtMapSizeX.setText("40");
        txtMapSizeY.setText("40");
        txtStepNumber.setText("20");
        txtPredInitNumber.setText("2");
        txtPredDeathRate.setText("80");
        txtPreyInitNumber.setText("10");
        txtPreyDeathRate.setText("130");
        nbPredators = Collections.synchronizedList(new ArrayList<>());
        nbPreys = Collections.synchronizedList(new ArrayList<>());
        initTable();
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
                .addListener(new ChangeListener<StatusIter>() {
                    @Override
                    public void changed(ObservableValue<? extends StatusIter> observable, StatusIter oldValue, StatusIter newValue) {
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                if (newValue != null) {
                                    nbPreys.add(newValue.getNbPreysValue());
                                    nbPredators.add(newValue.getNbPredsValue());
                                    drawOnChart(newValue);
                                    data.add(newValue);
                                }
                            }
                        });
                    }
                });
    }

    public void onProcessAction(ActionEvent ev) {
        init();
        calculateService.restart();

    }

    private void init() {
        data.clear();
        nbPreys.clear();
        nbPredators.clear();
        int sizeX = Integer.parseInt(txtMapSizeX.getText().trim());
        int sizeY = Integer.parseInt(txtMapSizeY.getText().trim());
        int initPred = Integer.parseInt(txtPredInitNumber.getText());
        int initPrey = Integer.parseInt(txtPreyInitNumber.getText());
        int stepNumber = Integer.parseInt(txtStepNumber.getText());
        double deathRate = Double.parseDouble(txtPredDeathRate.getText()) / 100.;
        double increaseRatio = Double.parseDouble(txtPreyDeathRate.getText()) / 100.;
        drawMesh(sizeX, sizeY);
        simulation = new Simulation(stepNumber, sizeX, sizeY, initPred, initPrey, deathRate, increaseRatio);
        List<Position> preys = simulation.getPreys();
        drawPreys(preys);

        List<Position> predators = simulation.getPredators();
        drawPredators(predators);
        nbPreys.add(preys.size());
        nbPredators.add(predators.size());
        StatusIter iter = new StatusIter(0, simulation.getNbPreys(), simulation.getNbPredators());
        data.add(iter);
        preysSeries = new XYChart.Series();
        predSeries = new XYChart.Series();
        preysSeries.setName("Preys");
        predSeries.setName("Predators");
        chart.getData().addAll(predSeries);
        chart.getData().addAll(preysSeries);
        drawOnChart(iter);

    }

    public void drawOnChart(StatusIter iter) {

        preysSeries.getData().add(new XYChart.Data(iter.getNbIterValue(), iter.getNbPreysValue()));
        predSeries.getData().add(new XYChart.Data(iter.getNbIterValue(), iter.getNbPredsValue()));

    }

    public void drawMesh(int nbX, int nbY) {

        canvas.setHeight(mapSize);
        canvas.setWidth(mapSize);

        predCanvas.setHeight(mapSize);
        predCanvas.setWidth(mapSize);

        preyCanvas.setHeight(mapSize);
        preyCanvas.setWidth(mapSize);

        int nbMax = Math.max(nbX, nbY);
        pitch = ((int) canvas.getHeight()) / nbMax;

        double sizeX = nbX * pitch + 2. * offset;
        double sizeY = nbY * pitch + 2. * offset;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0., 0., mapSize, mapSize);
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

    public void drawPreys(List<Position> positions) {
        GraphicsContext g2c = preyCanvas.getGraphicsContext2D();
        g2c.clearRect(0., 0., mapSize, mapSize);

        g2c.setStroke(Color.GREEN);

        g2c.setLineWidth(2.);
        for (Position pos : positions) {
            int x = pos.getX() - 1;
            int y = pos.getY() - 1;
            RadialGradient radial = new RadialGradient(0, 0., offset + (x + 0.5) * pitch, offset + (y + 0.5) * pitch, pitch / 2, false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.BLACK));
            g2c.setFill(radial);
            g2c.fillOval(offset + 2 + x * pitch, offset + 2 + y * pitch, pitch - 4, pitch - 4);
        }
    }

    public void drawPredators(List<Position> positions) {
        GraphicsContext g2c = predCanvas.getGraphicsContext2D();
        g2c.clearRect(0., 0., mapSize, mapSize);
        g2c.setStroke(Color.RED);
        Color alphaRed = new Color(Color.RED.getRed(), 0., 0., 0.3f);
        g2c.setFill(alphaRed);
        g2c.setLineWidth(2.);
        for (Position pos : positions) {
            int x = pos.getX() - 1;
            int y = pos.getY() - 1;
            g2c.fillRect(offset + (x - 1) * pitch, offset + (y - 1) * pitch, 3 * pitch, 3 * pitch);
            g2c.strokeLine(offset + 2 + x * pitch, offset + 2 + y * pitch, offset + (x + 1) * pitch - 2, offset - 2 + (y + 1) * pitch);
            g2c.strokeLine(offset + 2 + x * pitch, offset - 2 + (y + 1) * pitch, offset + (x + 1) * pitch - 2, offset + 2 + y * pitch);
        }
    }

    public void initTable() {
        Callback<TableColumn<StatusIter, SimpleIntegerProperty>, TableCell<StatusIter, SimpleIntegerProperty>> renderer = new Callback<TableColumn<StatusIter, SimpleIntegerProperty>, TableCell<StatusIter, SimpleIntegerProperty>>() {
            @Override
            public TableCell<StatusIter, SimpleIntegerProperty> call(TableColumn<StatusIter, SimpleIntegerProperty> param) {
                return new TableCell<StatusIter, SimpleIntegerProperty>() {
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

            }
        };
        colStep.setCellValueFactory(new PropertyValueFactory<>("nbIter"));
        colStep.setCellFactory(renderer);
        colPreys.setCellValueFactory(new PropertyValueFactory<>("nbPreys"));
        colPreys.setCellFactory(renderer);
        colPreds.setCellValueFactory(new PropertyValueFactory<>("nbPreds"));
        colPreds.setCellFactory(renderer);

        table.setItems(data);

    }

    final Service<StatusIter> calculateService = new Service<StatusIter>() {

        @Override
        protected Task<StatusIter> createTask() {
            return new Task<StatusIter>() {

                @Override
                protected StatusIter call() throws Exception {
                    Integer nbLoop = simulation.getNbLoop();
                    StatusIter iter = null;
                    for (int j = 0; j < nbLoop; j++) {
                        simulation.next();
                        drawPreys(simulation.getPreys());
                        drawPredators(simulation.getPredators());
                        iter = new StatusIter(j + 1, simulation.getNbPreys(), simulation.getNbPredators());
                        updateValue(iter);
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ie) {

                        }
                    }
                    return iter;
                }
            };
        }

    };

}
