/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.battleshipgame.client;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

public class GraphicalClient extends Application
{
    private Scene scene;
    private Stage primaryStage;
    
    @Override
    public void start(Stage primaryStage)
    {   
        this.primaryStage = primaryStage;
        initialize();
        primaryStage.setTitle("Battleship");
        primaryStage.setScene(this.scene);
        primaryStage.setMinHeight(250);
        primaryStage.setMinWidth(400);
        primaryStage.show();
    }
    
    private void initialize()
    {
        GridPane masterGrid = GenerateMasterGrid();
        masterGrid.getChildren().add(GenerateMenuBar());
        masterGrid.getChildren().add(GenerateGrid());
        this.scene = GenerateScene(masterGrid);
    }
    
    private Scene GenerateScene(Parent parent)
    {
        Scene scene = new Scene(parent, 800, 450);
        return scene;
    }
    
    private GridPane GenerateMasterGrid()
    {
        GridPane grid = new GridPane();
        return grid;
    }
    
    private GridPane GenerateGrid()
    {
        GridPane grid = new GridPane();
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(10);
        grid.getRowConstraints().add(rowConstraints);
        rowConstraints.setPercentHeight(80);
        grid.getRowConstraints().add(rowConstraints);
        rowConstraints.setPercentHeight(10);
        grid.getRowConstraints().add(rowConstraints);
        return grid;
    }
    
    private GridPane GeneratePlacementButtonGrid()
    {
        GridPane grid = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(20);
        for (int i = 0; i < 5; i++)
        {
            grid.getColumnConstraints().add(columnConstraints);
        }
        return grid;
    }
    
    private Button GenerateButton(String text)
    {
        Button button = new Button();
        button.setText(text);
        return button;
    }
    
    private GridPane GenerateBoardPanel()
    {
        GridPane grid = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        for (int i = 0; i < 2; i++)
        {
            grid.getColumnConstraints().add(columnConstraints);
        }
        return grid;
    }
    
    private MenuBar GenerateMenuBar()
    {
        MenuItem surrender = new MenuItem("Surrender");
        surrender.setOnAction(this::surrender);
        
        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(this::quit);
        
        Menu file = new Menu("File");
        file.getItems().addAll(surrender, quit);
        
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        menuBar.getMenus().add(file);
        return menuBar;
    }
    
    private void surrender(ActionEvent e)
    {
        //TODO
    }
    
    private void quit(ActionEvent e)
    {
        //TODO
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
