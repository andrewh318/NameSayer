package app.controllers;

import app.Main;
import app.models.NamesModel;

import app.models.ShopModel;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSlider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Enum representing the different states of the application.
 */
enum Screen {
    LISTEN, PRACTICE, SHOP
}

public class FrameController {
    @FXML private BorderPane borderPane;

    private NamesModel _model;
    private ShopModel _shopModel;
    private ListenController _listenController;

    // reference to the stage used throughout the application
    private Stage _stage;

    @FXML private JFXProgressBar _progressBar;
    @FXML private JFXSlider _volumeSlider;

    @FXML private Label _currentNameLabel;
    @FXML private Label _moneyLabel;

    private Parent shopScreen;

    private Screen _currentScreen;



    public void initialize(){
        setUpNamesModel();
        setUpMoneyModel();
        initializeMoney();
        loadListen(_model);
        _currentScreen = Screen.LISTEN;
    }

    /**
     * Sets up the global application money state by binding the text label to the money field in Shop Model
     */
    private void initializeMoney(){
        SimpleIntegerProperty startingMoney = _shopModel.getMoneyBinding();
        _moneyLabel.textProperty().bind(startingMoney.asString());

    }


    private void setUpNamesModel(){
        _model = new NamesModel();
        _model.setUp();
    }

    private void setUpMoneyModel(){
        _shopModel = new ShopModel();
    }

    public void setStage(Stage stage){
        _stage = stage;
        setUpOnClose();
    }


    private void setUpOnClose(){
        _stage.setOnCloseRequest(e ->{
            e.consume();
            closeRequest();
        });
    }

    /**
     * On close request, application prompts user to save playlists created in the session
     * Application also saves the money user has accumulated and what themes they have unlocked
     */
    private void closeRequest(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to save all created playlists?");


        Optional<ButtonType> action = alert.showAndWait();

        if (action.get() == ButtonType.OK){
            _model.savePlaylists();
            _stage.close();
        } else {
            _stage.close();
        }
        // always save money on close
        _shopModel.saveStateToFile();
        _model.deleteFolder(new File(NamesModel.TRIMMED_NORMALISED_DIRECTORY));
    }

    @FXML
    private void onPracticeButtonClicked(){
        if (!_currentScreen.equals(Screen.PRACTICE)){
            loadPractice(_model, borderPane);
            _currentScreen = Screen.PRACTICE;
        }


    }

    @FXML
    private void onListenButtonClicked(){
        if (!_currentScreen.equals(Screen.LISTEN)) {
            loadListen(_model);
            _currentScreen = Screen.LISTEN;
        }
    }

    @FXML
    private void onShopButtonClicked(){
        if (!_currentScreen.equals(Screen.SHOP)){
            loadShop();
            _currentScreen = Screen.SHOP;
        }
    }

    @FXML
    private void onTestMicButtonClicked(){
        loadTestMic();
    }

    @FXML
    /**
     * Opens a file chooser to allow user to select a playlist to upload (only text files acepteD)
     */
    private void onUploadButtonClicked(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Playlist File");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File f = fc.showOpenDialog(_stage);


        if (f != null){
            // parse the txt file
            List<String> invalidNames = _model.readPlaylist(f);
            if (!(invalidNames.isEmpty())) {
                String fileName = f.getName().substring(0, f.getName().lastIndexOf("."));

                String invalidNamesString = "";
                for (String invalidName : invalidNames) {
                    invalidNamesString = invalidNamesString + invalidName.replaceAll("%"," ") + "\n";
                }

                showAlert("Error: the following names from playlist: " + fileName + " could not be found in the database", invalidNamesString);
            }
        }
    }


    private void loadListen(NamesModel model){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/Listen.fxml"));
            Parent root = loader.load();
            _listenController = loader.getController();
            _listenController.setModel(model, this);
            borderPane.setCenter(root);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // pass a reference of the border pane into the practice set up controller so it can change the scene to
    // practice mode

    /**
     * Loads the practice set up screen and injects the model and border pane into it
     * @param model Practice mode requires the application model so user can select what playlist they want to practice
     * @param borderPane Practice mode requires reference to the border pane so it can switch scenes to the practice mode
     */
    private void loadPractice(NamesModel model, BorderPane borderPane){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/PracticeSetup.fxml"));
            Parent root = loader.load();
            PracticeSetupController controller = loader.getController();

            // initializes practice set up by passing it the required objects it needs
            controller.setModels(model, _shopModel);
            controller.setPane(borderPane);
            controller.setUpComboBox();
            controller.setFrameController(this);
            borderPane.setCenter(root);
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * Loads the shop screen. We only want to instantiate the shop once so if it has already been loaded befoe we
     * simply get the old reference to it.
     */
    private void loadShop(){
        if (shopScreen == null){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/Shop.fxml"));
                Parent root = (Parent) loader.load();
                ShopController controller = loader.getController();
                controller.setUp(_model, _shopModel);
                shopScreen = root;
                borderPane.setCenter(root);
            } catch (IOException e){
                e.printStackTrace();
            }
        } else {
            borderPane.setCenter(shopScreen);
        }
    }

    private void loadTestMic(){
        // load new playlist FXML
        Stage stage = new Stage();
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/views/TestMic.fxml"));
            root = (Parent) loader.load();

            // need to set CSS for this node as its a new stage
            Main.setTheme(Main.currentTheme, root);

            stage.setTitle("Test Mic");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is called from the listen screen and practice screen when a user plays or makes a recording
     * @param duration Duration of the progress bar in seconds
     */
    public void startProgressBar(float duration){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < duration*1000; i++){
                    updateProgress(i+1, duration*1000);
                    Thread.sleep(1);
                }
                return null;
            }
        };
        _progressBar.progressProperty().unbind();
        _progressBar.progressProperty().bind(task.progressProperty());

        new Thread(task).start();
    }

    private void showAlert(String header, String content){
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setHeaderText(header);
        errorAlert.setContentText(content);
        errorAlert.showAndWait();
    }

    public double getVolume() {
        return _volumeSlider.getValue();
    }
    /*
    public void setUpVolumeSlider() {
        _volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                System.out.println(new_val.doubleValue());
                opacityValue.setText(String.format("%.2f", new_val));
            }
        });
    }
    */



}
