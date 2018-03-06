package ru.spb.lanton.soft.exchange.exchangesecurityanalysis.view;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model.Output;
import ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model.Parser;
import ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model.TypeTask;
import static ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model.TypeTask.GetIpCountry;
import static ru.spb.lanton.soft.exchange.exchangesecurityanalysis.model.TypeTask.PrepareData;

/**
 * FXML Controller class
 *
 * @author antoxa
 */
public class WindowMainController implements Initializable {

    @FXML
    private DatePicker date;
    
    @FXML
    private ComboBox choice;
    
    @FXML
    private Button buttonPrepareData;
    
    @FXML
    private Button buttonGetIpCountry;
    
    @FXML
    private Button buttonPrintToScreen;
    
    @FXML
    private Button buttonPrintToFile;
    
    @FXML
    private Button buttonAbout;
    
    @FXML
    private TextArea terminal;
    
    @FXML
    private Label status;
    
    private Parser parser;
    
    public WindowMainController() {
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        terminal.clear();
        date.setValue(LocalDate.now());
        choice.getItems().addAll("Почта - ip", "Список ip и адресов", "Полный отчет", "Максимум информации");        
        choice.getSelectionModel().selectFirst();
        choice.setDisable(true);
        buttonPrintToScreen.setDisable(true);
        buttonPrintToFile.setDisable(true);
        buttonGetIpCountry.setDisable(true);
    }    
    
    @FXML
    private void pressButtonPrepareData() {
        parser = new Parser(this);
        if (choice.getSelectionModel().getSelectedIndex() == 3) {
            parser.setFullLog(true);
        }
        else {
            parser.setFullLog(false);
        }
        parser.setDate(date.getValue());
        terminal.clear();
        buttonPrepareData.setDisable(true);
        parser.execute(PrepareData);   
    }
    
    @FXML
    private void pressButtonGetIpCountry() {
        parser.execute(GetIpCountry);
        buttonGetIpCountry.setDisable(true);
    }
    
    @FXML
    private void pressButtonPrintToScreen() {
        terminal.clear();
        parser.printReport(choice.getSelectionModel().getSelectedIndex(), Output.CONSOLE);
        buttonPrintToScreen.setDisable(true);
    }
    
    @FXML
    private void pressButtonPrintToFile() {
        terminal.appendText("Экспорт данных в файл report.log\n");
        parser.printReport(choice.getSelectionModel().getSelectedIndex(), Output.FILE);
        buttonPrintToFile.setDisable(true);
    }
    
    @FXML
    private void pressButtonAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("О программе");
        about.setHeaderText(null);
        StringBuilder msg = new StringBuilder();
        msg.append("Анализатор логов Exchange server\n");
        msg.append("Версии 1.3\n");
        msg.append("Автор: LAnton\n");
        msg.append("Релиз от 06.02.2018\n");
        msg.append("email: LAntonDev@gmail.com");
        about.setContentText(msg.toString());
        about.showAndWait();
    }
    
    @FXML
    private void onChangeDate() {        
        buttonPrepareData.setDisable(false);
        buttonGetIpCountry.setDisable(true);
        buttonPrintToFile.setDisable(true);
        buttonPrintToScreen.setDisable(true);
        choice.setDisable(true);
    }
    
    @FXML
    private void onChangeReport() {
        if (choice.getSelectionModel().getSelectedIndex() == 3) {
            buttonPrintToScreen.setDisable(true);
            buttonPrintToFile.setDisable(true);
        }
        else {
            buttonPrintToScreen.setDisable(false);
            buttonPrintToFile.setDisable(false);
        }
    }
    
    public void enabledButtonGetIpCountry() {
        buttonGetIpCountry.setDisable(false);
    }
    
    public void enabledButtonPrintToScree() {
        buttonPrintToScreen.setDisable(false);
    }
    
    public void enabledButtonPrintToFile() {
        buttonPrintToFile.setDisable(false);
    }
    
    public void enabledChoicer() {
        choice.setDisable(false);
    }
    
    public void setText(String msg) {
        terminal.setText(msg);
    }
    
    public void appendText(String msg) {
        terminal.appendText(msg);
    }
    
    public void setStatus(String status) {
        this.status.setText(status);
    }
}
