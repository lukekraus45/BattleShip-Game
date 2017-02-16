/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.battleshipgame.client;

import edu.pitt.battleshipgame.common.ships.Ship;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GraphicalClient extends Application
{
    private Scene scene;
    private Stage primaryStage;
    private GridPane grid;
    private BorderPane ourBoard;
    private BorderPane theirBoard;
    
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
        this.grid = GenerateGrid();
        this.grid.add(GeneratePlacementButtonGrid(), 0, 2);
        GridPane boardPane = GenerateBoardPane();
        this.grid.add(boardPane, 0, 1);
        BorderPane masterPane = GenerateMasterPane();
        masterPane.setTop(GenerateMenuBar());
        masterPane.setCenter(this.grid);
        this.scene = GenerateScene(masterPane);
    }
    
    private Scene GenerateScene(Parent parent)
    {
        Scene scene = new Scene(parent, 800, 450);
        return scene;
    }
    
    private BorderPane GenerateMasterPane()
    {
        BorderPane pane = new BorderPane();
        return pane;
    }
    
    private GridPane GenerateGrid()
    {
        GridPane grid = new GridPane();
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(10);
        grid.getRowConstraints().add(rowConstraints);
        rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(80);
        grid.getRowConstraints().add(rowConstraints);
        rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(10);
        grid.getRowConstraints().add(rowConstraints);
        ColumnConstraints columnConstraint = new ColumnConstraints();
        columnConstraint.setPercentWidth(100);
        grid.getColumnConstraints().add(columnConstraint);
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
        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setPercentHeight(100);
        grid.getRowConstraints().add(rowConstraint);
        
        int i = 0;
        for (Ship.ShipType ship : Ship.ShipType.values())
        {
            if (ship != Ship.ShipType.NONE)
            {
                Button shipButton = GenerateButton(ship.name());
                grid.add(shipButton, i, 0);
                i++;
            }
        }
        
        return grid;
    }
    
    private Button GenerateButton(String text)
    {
        Button button = new Button();
        button.setText(text);
        GridPane.setFillWidth(button, true);
        GridPane.setFillHeight(button, true);
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return button;
    }
    
    private GridPane GenerateBoardPane()
    {
        GridPane grid = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(50);
        for (int i = 0; i < 2; i++)
        {
            grid.getColumnConstraints().add(columnConstraints);
        }
        RowConstraints rowConstraint = new RowConstraints();
        rowConstraint.setPercentHeight(100);
        grid.getRowConstraints().add(rowConstraint);
        
        this.ourBoard = GenerateBoard("Your Board");
        this.theirBoard = GenerateBoard("Opponent's Board");
        grid.add(this.ourBoard, 0, 0);
        grid.add(this.theirBoard, 1, 0);
        
        return grid;
    }
    
    private BorderPane GenerateBoard(String title)
    {
        BorderPane pane = new BorderPane();
        
        Label titleLabel = new Label(title);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setContentDisplay(ContentDisplay.CENTER);
        pane.setTop(titleLabel);
        
        GridPane board = new GridPane();
        ConstrainBoard(board);
        LabelBoard(board);
        ColorBoard(board);
        pane.setCenter(board);
        
        return pane;
    }
    
    private void ColorBoard(GridPane board)
    {
        for (int row = 1; row <= 10; row++)
        {
            for (int col = 1; col <= 10; col++)
            {
                Pane square = new Pane();
                square.setBackground(new Background(new BackgroundFill(Color.web("#1f00bc"), CornerRadii.EMPTY, Insets.EMPTY)));
                square.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                board.add(square, row, col);
            }
        }
    }
    
    private void LabelBoard(GridPane board)
    {
        char column = 'A';
        for (int i = 1; i <= 10; i++, column++)
        {
            Label rowLabel = new Label(String.valueOf(i));
            Label columnLabel = new Label(String.valueOf(column));
            rowLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            columnLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            rowLabel.setAlignment(Pos.CENTER_RIGHT);
            columnLabel.setAlignment(Pos.BOTTOM_CENTER);
            board.add(rowLabel, 0, i);
            board.add(columnLabel, i, 0);
        }
    }
    
    private void ConstrainBoard(GridPane board)
    {
        RowConstraints rowConstraints = new RowConstraints();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        rowConstraints.setPercentHeight(10);
        columnConstraints.setPercentWidth(10);
        for (int i = 0; i < 11; i++)
        {
            board.getRowConstraints().add(rowConstraints);
            board.getColumnConstraints().add(columnConstraints);
        }
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
