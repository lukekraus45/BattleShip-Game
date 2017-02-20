/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.pitt.battleshipgame.client;

import edu.pitt.battleshipgame.common.GameInterface;
import edu.pitt.battleshipgame.common.board.Board;
import edu.pitt.battleshipgame.common.board.Coordinate;
import edu.pitt.battleshipgame.common.ships.Ship;
import edu.pitt.battleshipgame.common.ships.ShipFactory;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
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
import javafx.scene.input.MouseEvent;
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
import javafx.scene.text.Font;
import javafx.stage.Stage;

public  class GraphicalClient extends Application
{
    private Scene scene;
    private Stage primaryStage;
    private GridPane grid;
    private GraphicalBoard ourBoard;
    private GraphicalBoard theirBoard;
    private StringProperty prompt;
    private GamePhase phase = GamePhase.CONNECTING;
    public static GameInterface gi;
    public static int myPlayerID;
    public static ArrayList<Board> gameBoards;
    private Pane[][] ourCells;
    private Pane[][] theirCells;
    static boolean connection_made = false;
    static Ship.ShipType current_ship;
    static Coordinate  global_start = null, global_end = null;
    static boolean start_or_end = false; //if false then the next value is start and if true then the next cell is the end. Used for placing ships
    private HashMap<Ship.ShipType, Button> shipButtons;
    private Button doneButton;
    
    @Override
    public void start(Stage primaryStage)
    {   
        this.primaryStage = primaryStage;
        initialize();
        primaryStage.setTitle("Battleship");
        primaryStage.setScene(this.scene);
        primaryStage.setMinHeight(400);
        primaryStage.setMinWidth(500);
        primaryStage.show();
        RunGetServerTask();
    }
    
    private void RunGetServerTask()
    {
        Task task = new Task<Void>() {
            @Override public Void call() {
                ConnectToServer();
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void initialize()
    {
        this.shipButtons = new HashMap<>();
        this.prompt = new SimpleStringProperty();
        this.grid = GenerateGrid();
        this.grid.add(GeneratePlacementButtonGrid(), 0, 2);
        this.grid.add(GenerateBoardPane(), 0, 1);
        this.grid.add(GeneratePrompt(), 0, 0);
        UpdatePrompt();
        BorderPane masterPane = GenerateMasterPane();
        masterPane.setTop(GenerateMenuBar());
        masterPane.setCenter(this.grid);
        this.scene = GenerateScene(masterPane);
        this.myPlayerID = -1;
        UpdateGamePhase(this.phase);
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
    
    private Label GeneratePrompt()
    {
        Label prompt = new Label();
        prompt.textProperty().bind(this.prompt);
        prompt.setMaxWidth(Double.MAX_VALUE);
        prompt.setAlignment(Pos.CENTER);
        prompt.setContentDisplay(ContentDisplay.CENTER);
        prompt.setFont(new Font("Regular",20));
        return prompt;
    }
        
    public void UpdatePrompt()
    {
        switch (this.phase)
        {
            case CONNECTING:
                this.prompt.set("Please wait while we connect to the server");
                break;
            case MATCHMAKING:
                this.prompt.set("Please wait while a match is made");
                break;
            case PLACEMENT:
                this.prompt.set("Please place your ships");
                break;
            case FIRING:
                this.prompt.set("Please choose a cell to fire on");
                break;
            case WAITING:
                this.prompt.set("Please wait while your opponent chooses a cell to fire on");
                break;
        }
    }
    
    private GridPane GeneratePlacementButtonGrid()
    {
        GridPane grid = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setPercentWidth(20);
        for (int i = 0; i < 6; i++)
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
                this.shipButtons.put(ship, shipButton);
                i++;
            }
        }
        this.doneButton = GenerateButton("DONE");
        grid.add(this.doneButton, 5, 0);
        
        return grid;
    }
    
    private void AddDoneButtonListener(Button button)
    {
        button.setOnAction((ActionEvent event) ->
        {
           //TODO
           //check that all ships are placed
           //then, move the the firing phase
        });
    }
    
    private void AddShipButtonListeners()
    {
        for (HashMap.Entry entry : this.shipButtons.entrySet())
        {
            ((Button)entry.getValue()).setOnAction((ActionEvent event) ->
            {
                ShipButtonClicked((Ship.ShipType)entry.getKey());
            });
        }
    }
    
    private void RemoveShipButtonListeners()
    {
        for (HashMap.Entry entry : this.shipButtons.entrySet())
        {
            ((Button)entry.getValue()).setOnAction(null);
        }
    }
    
    private void ShipButtonClicked(Ship.ShipType type)
    {
        //TODO
        //should only be activated during placement phase, as buttons should be disabled during all other phases
        //if the ship is not placed, click should set the corresponding ship as the one being placed
        //if the ship is placed, click should remove the ship from the board.
        current_ship = type;        
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
        
        this.ourBoard = new GraphicalBoard("Your Board");
        this.theirBoard = new GraphicalBoard("Opponent's Board");
        this.ourCells = this.ourBoard.getCells();
        this.theirCells = this.ourBoard.getCells();
        grid.add(this.ourBoard.getBoard(), 0, 0);
        grid.add(this.theirBoard.getBoard(), 1, 0);
        
        return grid;
    }
    
    private void AddCellListeners(Pane [][] cells)
    {
        for (int row = 0; row < 10; row++)
        {
            for (int col = 0; col < 10; col++)
            {
                final int x = row;
                final int y = col;
                cells[row][col].setOnMouseClicked((MouseEvent event) ->
                {
                    CellClicked(x, y);
                });
            }
        }
    }
    
    private void RemoveCellListeners(Pane[][] cells)
    {
        for (int row = 0; row < 10; row++)
        {
            for (int col = 0; col < 10; col++)
            {
                cells[row][col].setOnMouseClicked(null);
            }
        }
    }
    
    private void EnableButtons(boolean enable)
    {
        for (HashMap.Entry entry : this.shipButtons.entrySet())
        {
            ((Button)entry.getValue()).setDisable(!enable);
        }
        this.doneButton.setDisable(!enable);
    }
    
    private void CellClicked(int row, int col)
    {
        //TODO
        //Action should depend upon game state
        //if the cell is clicked during the placement phase, the click originates from ourBoard
        //if the sell is clicked during the firing phase, the cell originates from theirBoard
       //The above logic should be worked out by the caller function. For example if the method that is calling
        //it is supposed to place ships than it will be our cells and if it is to guess a coordinate it would be theircells
        //In otherwords we don't need to worry about gamestate here, but rather in some other method 
        
        if(start_or_end){
        //if true then the coordinate that is being placed is the end coordintate. This means that the start coordinate would have already been clicked
        start_or_end = false;
        global_end = new Coordinate(col,row);
        }else{
        start_or_end = true;
        global_start = new Coordinate(col,row);
        }
        
        if(this.phase == GamePhase.PLACEMENT){
        
            while(global_start != null && global_end != null){
            placeShips(gameBoards.get(myPlayerID) , current_ship);
            }
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
    
    enum GamePhase
    {
        MATCHMAKING,
        PLACEMENT,
        FIRING,
        WAITING,
        CONNECTING
    }
    
    private void UpdateGamePhase(GamePhase phase)
    {
        //TODO
        //update game based on phase
        //disable/enable buttons, listners, etc.
        this.phase = phase;
        UpdatePrompt();
        UpdateCellListenersAndButtons(phase);
    }
    
    private void UpdateCellListenersAndButtons(GamePhase phase)
    {
        switch (phase)
        {
            case CONNECTING:
                RemoveCellListeners(this.theirCells);
                RemoveCellListeners(this.ourCells);
                EnableButtons(false);
                break;
            case MATCHMAKING:
                RemoveCellListeners(this.theirCells);
                RemoveCellListeners(this.ourCells);
                EnableButtons(false);
                break;
            case PLACEMENT:
                RemoveCellListeners(this.theirCells);
                AddCellListeners(this.ourCells);
                EnableButtons(true);
                break;
            case FIRING:
                RemoveCellListeners(this.ourCells);
                AddCellListeners(this.theirCells);
                EnableButtons(false);
                break;
            case WAITING:
                RemoveCellListeners(this.theirCells);
                RemoveCellListeners(this.ourCells);
                EnableButtons(false);
                break;
        }
    }
    
    private void placeShips(Board board,Ship.ShipType type) {
        
        /*
      
      
        Should wait to see what button is pressed (ship type). Whatever ship type is pressed it should set the start coordinate to the cell that is clicked into
        The end point should be the cell that is clicked next. After these two values are computed the ship should be placed on the board. (Not sure how we want to go about this
        We could make an actual ship graphic (not sure how difficult that will be) or an easier way would be to just color the cells different colors (ex. red for desroyer yellow
        for battleship etc.) 
      
      */
      
          
            if(type != Ship.ShipType.NONE) {
                
                
               
                
                Coordinate start = global_start;
                System.out.println("Start " + start.toString());
               
                Coordinate end = global_end;
                System.out.println("End " + end.toString());
                global_start = null;
                global_end = null;
                
                int length;
                if(start.getRow() == end.getRow()){
                //if the start and end row are the same then they will be horizontal
                length = Math.abs(end.getCol() - start.getCol());
                for(int i = 0; i <= length; i++){
                ourBoard.setCellType(GraphicalBoard.CellType.SHIP, start.getRow()+i, start.getCol());
                }
                
                }
                else if(start.getCol() == end.getCol()){
                //if the start and end col are the same they will be vertical
                length = Math.abs(end.getRow() - start.getRow());
                for(int i = 0; i <= length; i++){
                ourBoard.setCellType(GraphicalBoard.CellType.SHIP, start.getRow(), start.getCol()+i);
                }
                }
                // We don't need to track a reference to the ship since it will be
                // on the board.
                ShipFactory.newShipFromType(type, start, end, board);
                
          
      }
    }
  
    private void ConnectToServer()
    {
        while (true)
        {
            try
            {
                gi = new ClientWrapper();
                break;
            }
            catch (Throwable causeGot)
            {
                Throwable cause = causeGot;
                Throwable rootCause;
                while (null != (rootCause = cause.getCause())  && (rootCause != cause))
                {
                    cause = rootCause;
                }
                if (!(cause instanceof java.net.ConnectException))
                {
                    throw causeGot;
                }
                else
                {
                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                        //do nothing
                    }
                }
            }
        }
        Platform.runLater( () ->
        {
            myPlayerID = gi.registerPlayer();
            gameBoards = gi.getBoards();
            UpdateGamePhase(GamePhase.MATCHMAKING);
        });
    }
  
    public static void main(String[] args)
    {   
        launch(args); 
    }
}

class GraphicalBoard
{
    private final BorderPane board;
    private final Pane[][] cells;
    private final String blue = "#1f00bc";
    private final String grey = "#8e8e8e";
    private final String red = "#c60b0b";
    private final String black = "#2b2525";
    
    GraphicalBoard(String title)
    {
        cells = new Pane[10][10];
        board = GenerateBoard(title);
    }
    
    public BorderPane getBoard()
    {
        return this.board;
    }
    
    public Pane[][] getCells()
    {
        return this.cells;
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
                SetCellColor(square, this.blue);
                square.setBorder(new Border(new BorderStroke(Color.BLACK,
                        BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                this.cells[row-1][col-1] = square;
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
    
    private void SetCellColor(Pane cell, String color)
    {
        cell.setBackground(new Background(new BackgroundFill(Color.web(color), CornerRadii.EMPTY, Insets.EMPTY)));
    }
    
    /**
     * @param row 0 indexed row
     * @param col 0 indexed column
     */
    public void setCellType(CellType type, int row, int col)
    {
        Pane cell = this.cells[row][col];
        switch (type)
        {
            case WATER:
                SetCellColor(cell, this.blue);
                break;
            case HIT:
                SetCellColor(cell, this.red);
                break;
            case MISS:
                SetCellColor(cell, this.black);
                break;
            case SHIP:
                SetCellColor(cell, this.grey);
                break;
        }
    }
    
    public enum CellType
    {
        WATER,
        HIT,
        MISS,
        SHIP
    }
}