package ru.spb.lanton.soft.exchange.exchangesecurityanalysis;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author antoxa
 */
public class ApplicationFX extends Application {
    
    @Override
    public void start(Stage stage) throws IOException {
        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/WindowMain.fxml"));
        
        Scene scene = new Scene(root);
        
        stage.setTitle("Exchange security analiysis");
        URL url = getClass().getResource("/img/icon.png");
        Image icon = new Image(url.toString());
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
