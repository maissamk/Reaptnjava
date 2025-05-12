package controllers.BackOffice.Offer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import services.EmployeService;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class Statistiques implements Initializable {

    @FXML
    private Label totalEmployesLabel;

    @FXML
    private Label totalOffresLabel;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private PieChart pieChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EmployeService service = new EmployeService();

        try {
            // Set totals
            totalEmployesLabel.setText(String.valueOf(service.getTotalEmployes()));
            totalOffresLabel.setText(String.valueOf(service.getTotalOffres()));

            // BarChart data
            Map<Integer, Integer> confirmedStats = service.getConfirmedEmployesPerOffre();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Confirmés");

            for (Map.Entry<Integer, Integer> entry : confirmedStats.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue());
                series.getData().add(data);
            }

            barChart.getData().add(series);

            // Tooltips for bar chart (added after rendering)
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Tooltip tooltip = new Tooltip("Offre ID: " + data.getXValue() + "\nConfirmés: " + data.getYValue());
                    Tooltip.install(data.getNode(), tooltip);
                }
            });

            // PieChart data
            int confirmed = service.getConfirmedCount();
            int unconfirmed = service.getUnconfirmedCount();

            pieChart.getData().addAll(
                    new PieChart.Data("Confirmés", confirmed),
                    new PieChart.Data("Non Confirmés", unconfirmed)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}