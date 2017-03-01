package edu.pitt.battleshipgame.client;

import edu.pitt.battleshipgame.common.GameInterface;
import edu.pitt.battleshipgame.common.TooManyPlayersException;
import edu.pitt.battleshipgame.common.board.Board;
import edu.pitt.battleshipgame.common.board.Coordinate;
import edu.pitt.battleshipgame.common.ships.Ship;
import edu.pitt.battleshipgame.common.ships.ShipFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
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
import javax.xml.ws.WebServiceException;

public  class GraphicalClient extends Application
{
    private Scene scene;
    private Stage primaryStage;
    private GridPane grid;
    private GraphicalBoard ourBoard;
    private GraphicalBoard theirBoard;
    private StringProperty prompt;
    private GamePhase phase = GamePhase.CONNECTING;
    private GameInterface gameInterface;
    private int playerID;
    private ArrayList<Board> gameBoards;
    private ArrayList<Board> oldGameBoards = null;
    private Pane[][] ourCells;
    private Pane[][] theirCells;
    private HashMap<Ship.ShipType, Button> shipButtons;
    private Button doneButton;
    private MenuItem surrender;
    private HashMap<Ship.ShipType, Ship> ships;
    private Ship.ShipType currentlyPlacing = null;
    private Coordinate initialPlacementCoordinate = null;
    private boolean[][] occupied;
    
    @Override
    public void start(Stage primaryStage)
    {   
        this.primaryStage = primaryStage;
        initialize();
        this.primaryStage.show();
        String serverAddress = GetServerAddress();
        RunGetServerTask(serverAddress);
    }
    
    private void RunGetServerTask(String address)
    {
        Task task = new Task<Void>() {
            @Override public Void call() {
                ConnectToServer(address);
                WaitForMatch();
                CheckOpponentConnection();
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void initialize()
    {
        this.occupied = new boolean[10][10];
        this.shipButtons = new HashMap<>();
        this.ships = new HashMap<>();
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
        UpdateGamePhase(this.phase);
        this.primaryStage.setTitle("Battleship");
        this.primaryStage.setScene(this.scene);
        this.primaryStage.setMinHeight(400);
        this.primaryStage.setMinWidth(500);
        this.primaryStage.setOnCloseRequest(this::quit);
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
        prompt.setFont(new Font("Regular",15));
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
                this.prompt.set("Please place your ships by clicking a " +
                        "ship button then the coordinates of the ends of the ship");
                break;
            case FIRING:
                this.prompt.set("Please choose a cell to fire on");
                break;
            case WAITING:
                this.prompt.set("Please wait for your opponent");
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
    
    private void AddDoneButtonListener()
    {
        this.doneButton.setOnAction((ActionEvent event) ->
        {
            try
            {
                UpdateGamePhase(GamePhase.WAITING);
                this.gameBoards = this.gameInterface.getBoards();
                this.ships.values().forEach((ship) ->
                {
                    this.gameBoards.get(this.playerID).addShip(ship);
                    
                });

                this.gameInterface.setBoards(this.gameBoards);
                this.gameInterface.beatHeart(this.playerID);
                
                Wait();
            }
            catch (WebServiceException ex)
            {
                ServerLost();
            }
        });
    }
    
    private void Wait()
    {
        try
        {
            if (!this.gameInterface.isGameOver())
            {
                Task task = new Task<Void>() {
                    @Override public Void call() {
                        WaitForOpponent();
                        return null;
                    }
                };
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
            else
            {
                //TODO game is over
            }
        }
        catch (WebServiceException ex)
        {
            ServerLost();
        }
    }
    
    private void WaitForOpponent()
    {
        try
        {
            this.gameInterface.wait(this.playerID);
            this.gameBoards = this.gameInterface.getBoards();
            if (this.gameInterface.isGameOver())
            {
                Platform.runLater( () ->
                {
                    CheckIfShipsSunk();
                    GameOver();
                });
            }
            else
            {
                Platform.runLater( () ->
                {
                    CheckIfShipsSunk();
                    UpdateBoards();
                    UpdateGamePhase(GamePhase.FIRING);
                });
            }
        }
        catch (WebServiceException e)
        {
            ServerLost();
        }
    }
    
    private void UpdateBoards()
    {
        Coordinate theirMove = this.gameBoards.get(this.playerID).getLastMove();
        if (theirMove != null)
        {
            int y = theirMove.getRow();
            int x = theirMove.getCol();
            if (this.occupied[y][x])
            {
                this.ourBoard.setCellType(GraphicalBoard.CellType.HIT, y, x);
            }
            else
            {
                this.ourBoard.setCellType(GraphicalBoard.CellType.MISS, y, x);
            }
        }
    }
    
    private void RemoveDoneButtonListener()
    {
        this.doneButton.setOnAction(null);
    }
    
    private void AddShipButtonListeners()
    {
        this.shipButtons.entrySet().forEach((entry) ->
        {
            ((Button)entry.getValue()).setOnAction((ActionEvent event) ->
            {
                ShipButtonClicked((Ship.ShipType)entry.getKey());
            });
        });
    }
    
    private void RemoveShipButtonListeners()
    {
        this.shipButtons.entrySet().forEach((entry) ->
        {
            ((Button)entry.getValue()).setOnAction(null);
        });
    }
    
    private void ShipButtonClicked(Ship.ShipType type)
    {
        if (this.ships.containsKey(type))
        {
            RemoveShip(type);
        }
        this.currentlyPlacing = type;
        this.initialPlacementCoordinate = null;
    }
    
    private void PlacementCoordinatesEntered(int row, int col)
    {
        if (this.initialPlacementCoordinate == null)
        {
            this.initialPlacementCoordinate = new Coordinate(row, col);
        }
        else if (this.currentlyPlacing != null)
        {
            Coordinate finalPlacementCoordinate = new Coordinate(row, col);
            Ship ship = ShipFactory.newShipFromType(this.currentlyPlacing, this.initialPlacementCoordinate, finalPlacementCoordinate);
            
            if (ship.isValid() && PlacementValid(this.initialPlacementCoordinate, finalPlacementCoordinate))
            {
                this.ships.put(this.currentlyPlacing, ship);
                this.ourBoard.setCellType(GraphicalBoard.CellType.SHIP, this.initialPlacementCoordinate, finalPlacementCoordinate);
                MarkOccupied(this.initialPlacementCoordinate, finalPlacementCoordinate, true);
                this.doneButton.setDisable(!(this.ships.size() == 5));
                this.currentlyPlacing = null;
            }
            else if (!ship.isValid())
            {
                String message = "Hey!!! What are you doing!? The "
                        + ship.getName() + " is " + ship.getLength() + " cells long. Come on, you know that.";
                Alert wrongLength = new Alert(AlertType.ERROR, message, ButtonType.OK);
                wrongLength.show();
            }
            else if (!PlacementValid(this.initialPlacementCoordinate, finalPlacementCoordinate))
            {
                String message = "Hey!!! What are you doing!? There is already a ship there. Are you blind?";
                Alert overlap = new Alert(AlertType.ERROR, message, ButtonType.OK);
                overlap.show();
            }
            this.initialPlacementCoordinate = null;
        }
    }
    
    private void MarkOccupied(Coordinate c1, Coordinate c2, boolean occupied)
    {
        int x1 = c1.getCol();
        int x2 = c2.getCol();
        int y1 = c1.getRow();
        int y2 = c2.getRow();
        if (x1 > x2)
        {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2)
        {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        for (int x = x1; x <= x2; x++)
        {
            for (int y = y1; y <= y2; y++)
            {
                this.occupied[y][x] = occupied;
            }
        }
    }
    
    private boolean PlacementValid(Coordinate c1, Coordinate c2)
    {
        boolean valid = true;
        int x1 = c1.getCol();
        int x2 = c2.getCol();
        int y1 = c1.getRow();
        int y2 = c2.getRow();
        if (x1 > x2)
        {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2)
        {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        for (int x = x1; x <= x2; x++)
        {
            for (int y = y1; y <= y2; y++)
            {
                valid &= !this.occupied[y][x];
            }
        }
        return valid;
    }
    
    private void RemoveShip(Ship.ShipType ship)
    {
        Ship shipToRemove = this.ships.remove(ship);
        this.ourBoard.setCellType(GraphicalBoard.CellType.WATER, shipToRemove.getStart(), shipToRemove.getEnd());
        MarkOccupied(shipToRemove.getStart(), shipToRemove.getEnd(), false);
        this.doneButton.setDisable(!(this.ships.size() == 5));
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
        this.theirCells = this.theirBoard.getCells();
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
                final int x = col;
                final int y = row;
                cells[row][col].setOnMouseClicked((MouseEvent event) ->
                {
                    CellClicked(y, x);
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
        this.shipButtons.entrySet().forEach((entry) ->
        {
            ((Button)entry.getValue()).setDisable(!enable);
        });
        this.doneButton.setDisable(!(enable && this.ships.size() == 5));
        if (enable)
        {
            AddShipButtonListeners();
            AddDoneButtonListener();
        }
        else
        {
            RemoveShipButtonListeners();
            RemoveDoneButtonListener();
        }
    }
    
    private void CellClicked(int row, int col)
    {
        if (this.phase == GamePhase.PLACEMENT)
        {
            PlacementCoordinatesEntered(row, col);
        }
        else if (this.phase == GamePhase.FIRING)
        {
            
            Fire(row, col);
        }
    }
    
    private void Fire(int row, int col)
    {
        Board theirGameBoard = this.gameBoards.get((this.playerID + 1) % 2);
        if (!theirGameBoard.getMoves()[col][row]) //Board class uses column then row
        {
            Ship shipHit = theirGameBoard.makeMove(new Coordinate(row, col));
            try
            {
                this.gameInterface.setBoards(this.gameBoards);
                this.gameInterface.beatHeart(this.playerID);
            }
            catch (WebServiceException e)
            {
                ServerLost();
            }
            if (shipHit != null)
            {
                this.theirBoard.setCellType(GraphicalBoard.CellType.HIT, row, col);
                CheckIfShipsSunk();
            }
            else
            {
                this.theirBoard.setCellType(GraphicalBoard.CellType.MISS, row, col);
            }
            try
            {
                if (this.gameInterface.isGameOver())
                {
                    GameOver();
                }
                else
                {
                    UpdateGamePhase(GamePhase.WAITING);
                    Wait();
                }
            }
            catch (WebServiceException e)
            {
                ServerLost();
            }
        }
        else
        {
            String message = "Hey!!! What are you doing!? You already shot this cell!";
            Alert alreadyShot = new Alert(AlertType.ERROR, message, ButtonType.OK);
            alreadyShot.show();
        }
    }
    
    private MenuBar GenerateMenuBar()
    {
        this.surrender = new MenuItem("Surrender");
        this.surrender.setOnAction(this::surrender);
        
        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(this::quit);
        
        Menu file = new Menu("File");
        file.getItems().addAll(surrender, quit);
        
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        menuBar.getMenus().add(file);
        return menuBar;
    }
    
    private void CheckIfShipsSunk()
    {
        if (this.oldGameBoards != null)
        {
            for (int player = 0; player < 2; player++)
            {
                for (int ship = 0; ship < this.gameBoards.get(0).getShipList().size(); ship++)
                {
                    if (this.gameBoards.get(player).getShipList().get(ship).isSunk() != 
                            this.oldGameBoards.get(player).getShipList().get(ship).isSunk())
                    {
                        final int id = player;
                        final String shipName = this.gameBoards.get(player).getShipList().get(ship).getName();
                        SinkAlert(id, shipName);
                    }
                }
            }
        }
        try
        {
            this.oldGameBoards = this.gameInterface.getBoards();
        }
        catch (WebServiceException ex)
        {
            ServerLost();
        }
    }
    
    private void SinkAlert(int player, String shipName)
    {
        String message = "Your " + (player == this.playerID ? "" : "opponent's ") + shipName + " has been sunk.";
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }
    
    private void surrender(ActionEvent e)
    {
        final String confirmText = "Are you sure that you want to surrender?";
        Alert confirm = new Alert(AlertType.CONFIRMATION, confirmText, ButtonType.CANCEL, ButtonType.OK);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.CANCEL)
        {
            e.consume();
        }
        else if (confirm.getResult() == ButtonType.OK)
        {
            //alert the other user that the opponent has surrendered
            try
            {
                gameInterface.player_leave();
            }
            catch (WebServiceException ex)
            {
                ServerLost();
            }
            Platform.exit();//exit 
        }
    }
    
    private String GetServerAddress()
    {
        String address = "";
        TextInputDialog dialog = new TextInputDialog("0.0.0.0");
        dialog.setTitle("Enter server address");
        dialog.setContentText("Please enter the address of the server");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
        {
            address = result.get();
        }
        return address;
    }
    
    private void OpponentConnectionLost()
    {
        final String message = "The other player has taken too long to respond to the server, you win.";
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
        try
        {
            this.gameInterface.player_leave();
        }
        catch (WebServiceException ex)
        {
            ServerLost();
        }
        Platform.exit();
    }
    
    private void Timeout()
    {
        final String message = "You took too long and the game timed out. You lose.";
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
        try
        {
            this.gameInterface.player_leave();
        }
        catch (WebServiceException ex)
        {
            ServerLost();
        }
        Platform.exit();
    }
    
    private void ServerFull()
    {
        final String message = "The server is at capacity. You cannot play.";
        Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
        Platform.exit();
    }
    
    private void ServerLost()
    {
        Platform.runLater( () ->
        {
            final String message = "We seem to have misplaced the server. We'll call the match a draw.";
            Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
            alert.showAndWait();
            Platform.exit();
        });
    }
    
    private void surrender_event()
    {
        final String surrenderText = "The other user has surrendered. You win.";
        Alert confirm = new Alert(AlertType.INFORMATION, surrenderText, ButtonType.OK);
        confirm.showAndWait();
        try
        {
            //alert the other user that the opponent has surrendered
            gameInterface.player_leave();
        }
        catch (WebServiceException ex)
        {
            ServerLost();
        }
        Platform.exit();//exit 
    }
    
    private void quit(Event e)
    {
        final String confirmText = "Are you sure that you want to quit?";
        Alert confirm = new Alert(AlertType.CONFIRMATION, confirmText, ButtonType.CANCEL, ButtonType.OK);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.CANCEL)
        {
            e.consume();
        }
        else if (confirm.getResult() == ButtonType.OK)
        {
            Platform.exit();
        }
    }
    
    private void GameOver()
    {
        String message = "";
        if (this.gameBoards.get(this.playerID).areAllShipsSunk())
        {
            message = "Your opponent has sunk your fleet. You lose.";
        }
        else if (this.gameBoards.get((this.playerID + 1) % 2).areAllShipsSunk())
        {
            message = "Congratulations, you have sunk your opponent's fleet. You win!";
        }
        Alert alert = new Alert(AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
        Platform.exit();
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
        this.phase = phase;
        UpdatePrompt();
        switch (phase)
        {
            case CONNECTING:
                RemoveCellListeners(this.theirCells);
                RemoveCellListeners(this.ourCells);
                EnableButtons(false);
                this.surrender.setDisable(true);
                break;
            case MATCHMAKING:
                RemoveCellListeners(this.theirCells);
                RemoveCellListeners(this.ourCells);
                EnableButtons(false);
                this.surrender.setDisable(true);
                break;
            case PLACEMENT:
                RemoveCellListeners(this.theirCells);
                AddCellListeners(this.ourCells);
                EnableButtons(true);
                this.surrender.setDisable(false);
                break;
            case FIRING:
                RemoveCellListeners(this.ourCells);
                AddCellListeners(this.theirCells);
                EnableButtons(false);
                this.surrender.setDisable(false);
                break;
            case WAITING:
                RemoveCellListeners(this.theirCells);
                RemoveCellListeners(this.ourCells);
                EnableButtons(false);
                this.surrender.setDisable(false);
                break;
        }
    }
  
    private void ConnectToServer(String address)
    {
        while (true)
        {
            try
            {
                this.gameInterface = new ClientWrapper(address);
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
        try
        {
            this.playerID = this.gameInterface.registerPlayer();
        }
        catch (TooManyPlayersException e)
        {
            Platform.runLater( () ->
            {
                ServerFull();
            });
        }
        catch (WebServiceException e)
        {
            ServerLost();
        }
        Platform.runLater( () ->
        {
            UpdateGamePhase(GamePhase.MATCHMAKING);
        });
    }
    
    private void WaitForMatch()
    {
        try
        {
            this.gameInterface.wait(this.playerID);
            this.gameInterface.beatHeart(this.playerID);
        }
        catch (WebServiceException e)
        {
            ServerLost();
        }
        Platform.runLater( () ->
        {
            UpdateGamePhase(GamePhase.PLACEMENT);
        });
    }
    
    private void CheckOpponentConnection()
    {
        while (true)
        {
            try
            {
                if (this.gameInterface.bothUsersConnected())
                {
                    while (true)
                    {
                        if (!this.gameInterface.bothUsersConnected())
                        {
                            Platform.runLater( () ->
                            {
                                surrender_event();
                            });
                            return;
                        }
                        if (!this.gameInterface.hasBeatingHeart((this.playerID + 1) % 2))
                        {
                            Platform.runLater( () ->
                            {
                                OpponentConnectionLost();
                            });
                            return;
                        }
                        if (!this.gameInterface.hasBeatingHeart(this.playerID))
                        {
                            Platform.runLater( () ->
                            {
                                Timeout();
                            });
                            return;
                        }
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
            catch (WebServiceException ex)
            {
                ServerLost();
                break;
            }
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
                board.add(square, col, row);
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
    
    public void setCellType(CellType type, Coordinate c1, Coordinate c2)
    {
        int x1 = c1.getCol();
        int x2 = c2.getCol();
        int y1 = c1.getRow();
        int y2 = c2.getRow();
        if (x1 > x2)
        {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 > y2)
        {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        for (int x = x1; x <= x2; x++)
        {
            for (int y = y1; y <= y2; y++)
            {
                setCellType(type, y, x);
            }
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