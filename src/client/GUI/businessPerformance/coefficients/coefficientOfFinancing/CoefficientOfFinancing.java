package client.GUI.businessPerformance.coefficients.coefficientOfFinancing;

import client.Objects.ActiveAndPassiveSaver;
import client.Objects.Record;
import client.Objects.Result;
import client.Objects.Transfer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class CoefficientOfFinancing implements Initializable {

    @FXML // fx:id="lineChart"
    private LineChart<Integer, Double> lineChart; // Value injected by FXMLLoader

    @FXML // fx:id="activeAndPassiveTable"
    private TableView<ActiveAndPassiveSaver> activeAndPassiveTable; // Value injected by FXMLLoader

    @FXML // fx:id="dateBegin"
    private ListView<Date> dateBegin; // Value injected by FXMLLoader

    @FXML // fx:id="dateEnd"
    private ListView<Date> dateEnd; // Value injected by FXMLLoader

    @FXML // fx:id="activeCol"
    private TableColumn<ActiveAndPassiveSaver, Double> activeCol; // Value injected by FXMLLoader

    @FXML // fx:id="passiveCol"
    private TableColumn<ActiveAndPassiveSaver, Double> passiveCol; // Value injected by FXMLLoader

    @FXML // fx:id="relativeCol"
    private TableColumn<ActiveAndPassiveSaver, Double> relativeCol; // Value injected by FXMLLoader

    @FXML // fx:id="backButton"
    private Button backButton; // Value injected by FXMLLoader

    @FXML // fx:id="prognosisButton"
    private Button prognosisButton; // Value injected by FXMLLoader

    @FXML // fx:id="periodField"
    private TextField periodField; // Value injected by FXMLLoader

    @FXML // fx:id="resultViewList"
    private ListView<Double> resultViewList; // Value injected by FXMLLoader

    @FXML // fx:id="infoLabel"
    private Label infoLabel; // Value injected by FXMLLoader

    @FXML
    void back(ActionEvent event) {
        backButton.getScene().getWindow().hide();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("../../BusinessPerformance.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Прогнозирование устойчивости предприятия");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<XYChart.Series> seriesList = FXCollections.observableArrayList();

    @FXML
    void prognosisAction(ActionEvent event) {

        Date startDate = dateBegin.getSelectionModel().getSelectedItem();
        Date finishDate = dateEnd.getSelectionModel().getSelectedItem();

        if (startDate == null) {
            infoLabel.setText("Дата начала выборки не выбрана!");
        } else {
            infoLabel.setText("");
            if (finishDate == null) {
                infoLabel.setText("Дата окончания выборки не выбрана!");
            } else {
                infoLabel.setText("");

                if (finishDate.getTime() < startDate.getTime()) {
                    infoLabel.setText("Дата окночания выбрана не верно!");
                } else {
                    infoLabel.setText("");

                    if (!periodField.getText().equals("")) {
                        infoLabel.setText("");

                        Result result = new Result();
                        result.setBeginDate(startDate);
                        result.setEndDate(finishDate);
                        result.setCoefficient("Коэффициент финансирования");
                        result.setTerm(Integer.parseInt(periodField.getText()));
                        try {

                            Transfer.getBw().write("получение данных по выборке");
                            Transfer.getBw().newLine();
                            Transfer.getBw().flush();

                            Transfer.getBw().write(Transfer.getGson().toJson(result));
                            Transfer.getBw().newLine();
                            Transfer.getBw().flush();

                            String gsonResponse = Transfer.getBr().readLine();
                            result = Transfer.getGson().fromJson(gsonResponse, Result.class);

                            XYChart.Series<Integer, Double> series = new XYChart.Series<>();
                            for (int i = 0; i < result.getTerm(); i++)
                                series.getData().add(new XYChart.Data(Integer.toString(i +1), result.getFunctionValues()[i]));
                            lineChart.getData().addAll(series);

                            for (int i = 0; i < result.getRelation().length; i++)
                                activeAndPassiveTable.getItems().add(new ActiveAndPassiveSaver(result.getAssets()[i],
                                        result.getLiabilities()[i],
                                        result.getRelation()[i]));

                            resultList = FXCollections.observableArrayList();
                            for (Double value : result.getFunctionValues())
                                resultList.add(value);

                            resultViewList.setItems(resultList);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        infoLabel.setText("Не выбран срок прогнозирования!");
                    }
                }
            }
        }
    }

    ObservableList<Date> startOfSamplingList;
    ObservableList<Date> endOfSamplingList;
    ObservableList<Double> resultList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        activeCol.setCellValueFactory(new PropertyValueFactory<ActiveAndPassiveSaver, Double>("active"));
        passiveCol.setCellValueFactory(new PropertyValueFactory<ActiveAndPassiveSaver, Double>("passive"));
        relativeCol.setCellValueFactory(new PropertyValueFactory<ActiveAndPassiveSaver, Double>("relation"));

        try {
            Transfer.getBw().write("получение дат");
            Transfer.getBw().newLine();
            Transfer.getBw().flush();

            String gsonResponse = Transfer.getBr().readLine();
            Record[] records = Transfer.getGson().fromJson(gsonResponse, Record[].class);

            if (records.length == 0) {
                infoLabel.setText("Отсутствуют данные!");
            } else {
                startOfSamplingList = FXCollections.observableArrayList();
                endOfSamplingList = FXCollections.observableArrayList();

                for (Record record : records) {
                    startOfSamplingList.add(record.getDate());
                    endOfSamplingList.add(record.getDate());
                }
                dateBegin.setItems(startOfSamplingList);
                dateEnd.setItems(endOfSamplingList);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
