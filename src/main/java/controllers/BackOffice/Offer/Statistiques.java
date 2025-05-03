package controllers.BackOffice.Offer;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import services.EmployeService;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class Statistiques implements Initializable {

    @FXML
    private BarChart<String, Number> barChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EmployeService service = new EmployeService();
        Map<Integer, Integer> stats = service.getEmployeCountPerOffre();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Employés par Offre");

        for (Map.Entry<Integer, Integer> entry : stats.entrySet()) {
            series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue()));
        }

        barChart.getData().add(series);
    }
}