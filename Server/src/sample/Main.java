package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

public class Main extends Application {
    private static Server_database server_database = new Server_database("mohannaddb","1234512345","DBServer");

    @Override
    public void start(Stage primaryStage) throws Exception{
        server_database.Connect();
        primaryStage.setTitle("Sever");
        Button Add_Client = new Button("Add Client");
        Button Add_Amount = new Button("Add Amount");
        Button Request_Certificate = new Button("Request Certificate");
        VBox vBox = new VBox();
        vBox.getChildren().addAll(Add_Client,Add_Amount,Request_Certificate);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        Scene scene = new Scene(vBox, 300, 275);


        ////Add_Client
        Label Client_Name = new Label("Client Name");
        Label Password = new Label("Password");
        Label Client_Num = new Label("Client Number");
        Label Amount1 = new Label("Amount");
        Label Client_Key = new Label("Key");
        VBox vBoxL1 = new VBox();
        HBox hBox1 = new HBox();
        VBox vBox1 = new VBox();
        VBox vBoxT1 = new VBox();
        vBoxL1.getChildren().addAll(Client_Name,Password,Client_Num,Amount1,Client_Key);
        TextField Client_Name_txt = new TextField();
        TextField Password_txt = new TextField();
        TextField Client_Num_txt = new TextField();
        TextField Amount_txt1 = new TextField();
        TextField Client_Key_txt = new TextField();
        vBoxT1.getChildren().addAll(Client_Name_txt,Password_txt,Client_Num_txt,Amount_txt1,Client_Key_txt);
        Button send1 = new Button("send");
        hBox1.getChildren().addAll(vBoxL1,vBoxT1);
        vBox1.getChildren().addAll(hBox1,send1);
        vBox1.setAlignment(Pos.CENTER);
        hBox1.setAlignment(Pos.CENTER);
        vBoxL1.setSpacing(30);
        vBoxT1.setSpacing(20);
        hBox1.setSpacing(20);
        vBox1.setSpacing(20);




        ////Add_Amount
        Button send2 = new Button("send");
        Label Client_id = new Label("Client id");
        Label Amount2 = new Label("Amount");
        VBox vBoxL2 = new VBox();
        VBox vBoxT2 = new VBox();
        HBox hBox2 = new HBox();
        VBox vBox2 = new VBox();
        vBoxL2.getChildren().addAll(Client_id,Amount2);
        TextField Client_id_txt = new TextField();
        TextField Amount_txt2 = new TextField();
        vBoxT2.getChildren().addAll(Client_id_txt,Amount_txt2);
        hBox2.getChildren().addAll(vBoxL2,vBoxT2);
        vBox2.getChildren().addAll(hBox2,send2);
        vBox2.setAlignment(Pos.CENTER);
        hBox2.setAlignment(Pos.CENTER);
        vBoxL2.setSpacing(30);
        vBoxT2.setSpacing(20);
        hBox2.setSpacing(20);
        vBox2.setSpacing(20);

        Scene scene1 = new Scene(vBox1, 300, 275);
        Add_Client.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(scene1);
                primaryStage.show();
            }
        });
        Scene scene2 = new Scene(vBox2, 300, 275);
        Add_Amount.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                primaryStage.setScene(scene2);
                primaryStage.show();
            }
        });
        send1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                if(server_database.add_Client(Integer.parseInt(Client_Num_txt.getText()),Client_Name_txt.getText(),Password_txt.getText(),Integer.parseInt(Amount_txt1.getText()),Client_Key_txt.getText())){
                    alert.setContentText("Success");
                }
                else{
                    alert.setContentText("Client number is exist already");
                }
                alert.show();
                primaryStage.setScene(scene);
                primaryStage.show();
            }
        });
        send2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                Random randomGenerator = new Random();
                if(server_database.transfer(-1,Integer.parseInt(Client_id_txt.getText()),Float.parseFloat(Amount_txt2.getText()),"",randomGenerator.nextInt(10000))){
                    alert.setContentText("Success");
                }
                else{
                    alert.setContentText("Client number isn't exist");
                }
                alert.show();
                primaryStage.setScene(scene);
                primaryStage.show();
            }
        });

        /*Request_Certificate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                if(capitalizeS.Request_Certificate(Client_Name_txt.getText(),Password_txt.getText(),Client_Num_txt.getText()))
                {
                    alert.setContentText("Success");
                    alert.show();
                }
                else
                {
                    alert.setContentText("Failed");
                    alert.show();
                }
            }
        });*/

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}