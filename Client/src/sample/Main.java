package sample;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class Main extends Application {

    private CapitalizeClient capitalizeClient = new CapitalizeClient();
    private CapitalizeClient_CA capitalizeClient_ca = new CapitalizeClient_CA();
    private Scene scene;
    private String Encrypt_Type = "";
    @Override
    public void stop() throws Exception {
        capitalizeClient.close_socket();
        super.stop();
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("Client");
        Button send2 = new Button("send");
        Label Client_id = new Label("Client id");
        Label Amount = new Label("Amount");
        Label Reason = new Label("Reason");
        VBox vBoxL2 = new VBox();
        VBox vBoxT2 = new VBox();
        HBox hBox2 = new HBox();
        VBox vBox2 = new VBox();
        vBoxL2.getChildren().addAll(Client_id,Amount,Reason);
        TextField Client_id_txt = new TextField();
        TextField Amount_txt = new TextField();
        TextField Reason_txt = new TextField();
        vBoxT2.getChildren().addAll(Client_id_txt,Amount_txt,Reason_txt);
        hBox2.getChildren().addAll(vBoxL2,vBoxT2);
        vBox2.getChildren().addAll(hBox2,send2);
        vBox2.setAlignment(Pos.CENTER);
        hBox2.setAlignment(Pos.CENTER);
        vBoxL2.setSpacing(30);
        vBoxT2.setSpacing(20);
        hBox2.setSpacing(20);
        vBox2.setSpacing(20);

        Button Accept_finger_print = new Button("Accept");
        Button Resend = new Button("Resend");
        Label Finger_print = new Label("Your finger print is   :   ");
        HBox hBox_send_finger_print = new HBox();
        hBox_send_finger_print.getChildren().addAll(Accept_finger_print,Resend);
        VBox vBox_finger_print = new VBox();
        vBox_finger_print.getChildren().addAll(Finger_print,hBox_send_finger_print);
        vBox_finger_print.setAlignment(Pos.CENTER);
        hBox_send_finger_print.setAlignment(Pos.CENTER);
        vBox_finger_print.setSpacing(30);
        hBox_send_finger_print.setSpacing(20);


        ToggleGroup tg = new ToggleGroup();
        TilePane Choices = new TilePane();
        RadioButton radioButton1 = new RadioButton("AES");
        RadioButton radioButton2 = new RadioButton("PGP");
        radioButton1.setToggleGroup(tg);
        radioButton2.setToggleGroup(tg);
        Choices.getChildren().addAll(radioButton1,radioButton2);
        VBox vBox = new VBox();
        vBox.getChildren().add(Choices);
        vBox.setSpacing(50);


        Label Client_Name = new Label("Client Name");
        Label Password = new Label("Password");
        Label Client_Num = new Label("Client Number");
        VBox vBoxL1 = new VBox();
        HBox hBox1 = new HBox();
        HBox hBoxB1 = new HBox();
        VBox vBox1 = new VBox();
        VBox vBoxT1 = new VBox();
        vBoxL1.getChildren().addAll(Client_Name,Password,Client_Num);
        TextField Client_Name_txt = new TextField();
        TextField Password_txt = new TextField();
        TextField Client_Num_txt = new TextField();
        vBoxT1.getChildren().addAll(Client_Name_txt,Password_txt,Client_Num_txt);
        Button send_login = new Button("send");
        Button Request_Certificate = new Button("Request Certificate");
        hBoxB1.getChildren().addAll(send_login,Request_Certificate);
        hBox1.getChildren().addAll(vBoxL1,vBoxT1);
        vBox1.getChildren().addAll(hBox1,hBoxB1);
        vBox1.setAlignment(Pos.CENTER);
        hBox1.setAlignment(Pos.CENTER);
        hBoxB1.setAlignment(Pos.CENTER);
        vBoxL1.setSpacing(30);
        vBoxT1.setSpacing(20);
        hBox1.setSpacing(20);
        hBoxB1.setSpacing(20);
        vBox1.setSpacing(20);
        //vBox.setStyle("-fx-background-color:#000000;");
        //vBox1.setStyle("-fx-background-color:#000000;");
        vBox.setBackground(new Background(new BackgroundFill(Color.DARKGRAY,CornerRadii.EMPTY, Insets.EMPTY)));
        vBox1.setBackground(new Background(new BackgroundFill(Color.DARKGRAY,CornerRadii.EMPTY, Insets.EMPTY)));
        Label private_key = new Label("Enter your private key");
        TextField private_key_txt = new TextField();
        Button send_key = new Button("Send");
        VBox vBox_key = new VBox();
        vBox_key.getChildren().addAll(private_key,private_key_txt,send_key);
        vBox_key.setSpacing(50);

        scene = new Scene(vBox1, 300, 275);

        Request_Certificate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                try {
                    String fingerprint = capitalizeClient_ca.send_CSR(Client_Name_txt.getText());
                    if(fingerprint.compareToIgnoreCase("")!=0)
                    {
                        Finger_print.setText(Finger_print.getText()+fingerprint);
                        primaryStage.setScene(new Scene(vBox_finger_print, 300, 275));
                        primaryStage.show();
                    }
                    else
                    {
                        alert.setContentText("Failed");
                        alert.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        send_login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if(capitalizeClient.send_info(Client_Name_txt.getText(),Password_txt.getText(),Client_Num_txt.getText())){
                        primaryStage.setScene(new Scene(vBox, 300, 275));
                        primaryStage.show();
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setContentText("Your name or password is false please try again");
                        alert.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        send2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                try {
                    if(capitalizeClient.send(Client_id_txt.getText(),Amount_txt.getText(),Reason_txt.getText())) {
                        alert.setContentText("Success");
                        alert.show();
                    }
                    else {
                        alert.setContentText("Failed");
                        alert.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                RadioButton rb = (RadioButton)tg.getSelectedToggle();
                if (rb != null) {
                    Encrypt_Type = rb.getText();
                    capitalizeClient.send_Encrypt_Type(Encrypt_Type);
                    if (Encrypt_Type.compareToIgnoreCase("AES")==0)
                    {
                        primaryStage.setScene(new Scene(vBox_key, 300, 275));
                        primaryStage.show();
                    }
                    else if (Encrypt_Type.compareToIgnoreCase("PGP")==0) {
                        primaryStage.setScene(new Scene(vBox2, 300, 275));
                        primaryStage.show();
                    }
                }
            }
        });
        send_key.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                capitalizeClient.setAes_Key(private_key_txt.getText());
                primaryStage.setScene(new Scene(vBox2, 300, 275));
                primaryStage.show();
            }
        });
        Accept_finger_print.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                capitalizeClient_ca.Receive_Cer();
                Request_Certificate.setVisible(!capitalizeClient_ca.isCertificate());
                primaryStage.setScene(scene);
                primaryStage.show();
            }
        });
        Resend.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                capitalizeClient_ca.Resend_Cer();
                primaryStage.setScene(scene);
                primaryStage.show();
            }
        });
        Request_Certificate.setVisible(!capitalizeClient_ca.isCertificate());
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
