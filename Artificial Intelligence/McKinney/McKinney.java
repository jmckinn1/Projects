//Program:      Othello
//Course:       COSC470
//Description:  Permits two programs, each using this control structure (but each with additional
//              customized classes and/or methods)to play Othello (i.e, against each other).
//Author:       Dr. Steve Donaldson and Joshua David McKinney
//Revised:      05/03/2017
//Language:     Java
//IDE:          NetBeans 8.2
//Notes:        None.

import java.io.*;
//***************************************************************************************************
//***************************************************************************************************
//Class:        Othello
//Description:  Main class for the program. Allows set-up and plays one side.

public class OthelloShell {

    public static char myColor = '?';           //B (black) or W (white) - ? means not yet selected
    public static char opponentColor = '?';     //ditto but opposite

    //INSERT ANY ADDITIONAL GLOBAL VARIABLES HERE
    //===========================================
    //===========================================
    public static boolean randomGame = false;   //If true, this assigns a random score to each move at depth 1
    public static boolean alphaBetaPruning = false;
    //===========================================
    //===========================================
    //***************************************************************************************************
    //Method:		main
    //Description:	Calls routines to play Othello
    //Parameters:	none
    //Returns:		nothing
    //Calls:        loadBoard, saveBoard, showBoard, constructor in Board class
    //              getCharacter, getInteger, getKeyboardInput, constructor in KeyboardInputClass
    //              Manual, IsValidMove, CopyBoard, Score, Min, PlaceTile
    //Globals:      myColor
    //              opponentColor
    //              randomGame
    
    public static void main(String args[]) {
        //INSERT ANY ADDITIONAL CONTROL VARIABLES HERE
        //============================================
        //============================================
        int maxDepth;
        //^^The max depth at which the "smart search" will look

        //============================================
        //============================================
        KeyboardInputClass keyboardInput = new KeyboardInputClass();
        int pollDelay = 250;
        long moveStartTime, moveEndTime, moveGraceTime = 10000;     //times in milliseconds
        Board currentBoard = Board.loadBoard();
        String myMove = "", myColorText = "";
        System.out.println("--- Othello ---");
        System.out.println("Player: McKinney\n");
        if (currentBoard != null) {                                 //board found, make sure it can be used
            if (currentBoard.status == 1) {                          //is a game in progress?   
                if (keyboardInput.getCharacter(true, 'Y', "YN", 1, "A game appears to be in progress. Abort it? (Y/N (default = Y)") == 'Y') {
                    currentBoard = null;
                } else {
                    System.out.println("Exiting program. Try again later...");
                    System.exit(0);
                }
            }
        }
        if ((currentBoard == null) || (currentBoard.status == 2)) {   //create a board for a new game
            int rows = 8;
            int cols = 8;
            if (keyboardInput.getCharacter(true, 'Y', "YN", 1, "Use standard board? (Y/N: default = Y):") == 'N') {
                rows = keyboardInput.getInteger(true, rows, 4, 26, "Specify the number of rows for the board (default = " + rows + "):");
                cols = keyboardInput.getInteger(true, cols, 4, 26, "Specify the number of columns for the board (default = " + cols + "):");
            }
            int maxTime = 60;
            maxTime = keyboardInput.getInteger(true, maxTime, 10, 600, "Max time (seconds) allowed per move (Default = " + maxTime + "):");
            currentBoard = new Board(rows, cols, maxTime);
            while (currentBoard.saveBoard() == false) {
            }               //try until board is saved (necessary in case of access conflict)
        }

        //INSERT CODE HERE FOR ANY ADDITIONAL SET-UP OPTIONS
        //==================================================
        //==================================================
        char gameType = keyboardInput.getCharacter(true, 'S', "RMS", 1, "Enter game type:"
                + "\n\'R\' for random move selection"
                + "\n\'M\' for manual move selection"
                + "\n\'S\' for smart move selection (default)");
        if (gameType == 'S') {
            maxDepth = keyboardInput.getInteger(false, 8, 1, 20, "Enter the maximum depth at which to search the game tree "
                    + "\nEnter \'0\' to play a random game (default: 8):"
                    + "\nWARNING! Deeper searches become exponentially slower!");
//            char abPruning = keyboardInput.getCharacter(true, 'Y', "YN", 1, "Use alpha-beta pruning? (\'Y\' or \'N\')");
//            if(abPruning == 'Y'){
//                alphaBetaPruning = true;
//            }
        } else {
            randomGame  = true;
            maxDepth = 1;
        }

        //==================================================
        //==================================================
        //At this point set-up must be in progress so colors can be assigned
        if (currentBoard.colorSelected == '?') {                    //if no one has chosen a color yet, choose one (player #1)
            myColor = keyboardInput.getCharacter(true, 'B', "BW", 1, "Select color: B=Black; W=White (Default = Black):");
            currentBoard.colorSelected = myColor;

            while (currentBoard.saveBoard() == false) {
            }               //try until the board is saved
            System.out.println("You may now start the opponent's program...");
            while (currentBoard.status == 0) {                      //wait for other player to join in
                currentBoard = null;                                //get the updated board
                while (currentBoard == null) {
                    currentBoard = Board.loadBoard();
                }
            }
        } else {                                                      //otherwise take the other color (this is player #2)
            if (currentBoard.colorSelected == 'B') {
                myColor = 'W';
            } else {
                myColor = 'B';
            }
            currentBoard.status = 1;                                //by now, both players are engaged and play can begin
            while (currentBoard.saveBoard() == false) {
            }               //try until the board is saved
        }

        if (myColor == 'B') {
            myColorText = "Black";
            opponentColor = 'W';
        } else {
            myColorText = "White";
            opponentColor = 'B';
        }
        System.out.println("This player will be " + myColorText + "\n");

        //INSERT CODE HERE FOR ANY ADDITIONAL OUTPUT OPTIONS
        //==================================================
        //==================================================
        //==================================================
        //==================================================
        //Now play can begin. (At this point each player should have an identical copy of currentBoard.)
        while (currentBoard.status == 1) {
            if (currentBoard.whoseTurn == myColor) {
                if (currentBoard.whoseTurn == 'B') {
                    System.out.println("Black's turn to move...");
                } else {
                    System.out.println("White's turn to move");
                }
                currentBoard.showBoard();
                String previousMove = currentBoard.move;
                moveStartTime = System.currentTimeMillis();

                //CALL METHOD(S) HERE TO SELECT AND MAKE A VALID MOVE
                //===================================================
                //===================================================
                
                String currentBestPlacement;
                Move bestMove= new Move(Integer.MIN_VALUE, Integer.MAX_VALUE, -1, -1);
                Board cloneBoard = CopyBoard(currentBoard);
                switch(gameType){
                    case 'M':
                    //set move to the result of the prompt for row and column
                    myMove = Manual(currentBoard);
                    break;
                    case 'R':
                    case 'S':
                        bestMove = MiniMax(bestMove, cloneBoard, maxDepth, 0, myColor, opponentColor);
                        break;
                }
                if(bestMove.row == -1 || bestMove.col == -1 && gameType != 'M'){
                    //No move was made and bestRow/bestCol were never changed
                    myMove = "";
                }else{
                    PlaceTile(bestMove.row, bestMove.col, currentBoard, myColor, opponentColor);
                    //^^Finalization of the move selected
                    currentBestPlacement = Character.toString((char)(bestMove.row+65)) + Character.toString((char)(bestMove.col+65));
                    if(gameType != 'M')
                        myMove = currentBestPlacement;
                }
                //===================================================
                //===================================================
                //YOU MAY ADD NEW CLASSES AND/OR METHODS BUT DO NOT
                //CHANGE ANY EXISTING CODE BELOW THIS POINT
                moveEndTime = System.currentTimeMillis();
                if ((moveEndTime - moveStartTime) > (currentBoard.maxMoveTime * 1000 + moveGraceTime)) {
                    System.out.println("\nMaximum allotted move time exceeded--Opponent wins by default...\n");
                    keyboardInput.getKeyboardInput("\nPress ENTER to exit...");
                    currentBoard.status = 2;
                    while (currentBoard.saveBoard() == false) {
                    }       //try until the board is saved
                    System.exit(0);
                }

                if (myMove.length() != 0) {
                    System.out.println(myColorText + " chooses " + myMove + "\n");
                    currentBoard.showBoard();
                    System.out.println("Waiting for opponent's move...\n");
                } else {
                    if (previousMove.length() == 0) {               //neither player can move
                        currentBoard.status = 2;                    //game over...
                        System.out.println("\nGame over!");
                        int blackScore = 0;
                        int whiteScore = 0;
                        for (int r = 0; r < currentBoard.boardRows; r++) {
                            for (int c = 0; c < currentBoard.boardCols; c++) {
                                if (currentBoard.board[r][c] == 'B') {
                                    blackScore++;
                                } else if (currentBoard.board[r][c] == 'W') {
                                    whiteScore++;
                                }
                            }
                        }
                        if (blackScore > whiteScore) {
                            System.out.println("Blacks wins " + blackScore + " to " + whiteScore);
                        } else if (whiteScore > blackScore) {
                            System.out.println("White wins " + whiteScore + " to " + blackScore);
                        } else {
                            System.out.println("Black and White tie with scores of " + blackScore + " each");
                        }
                    } else {
                        System.out.println("No move available. Opponent gets to move again...");
                    }
                }
                currentBoard.move = myMove;
                currentBoard.whoseTurn = opponentColor;
                while (currentBoard.saveBoard() == false) {
                }           //try until the board is saved
            } else {                                                   //wait a moment then poll again
                try {
                    Thread.sleep(pollDelay);
                } catch (Exception e) {
                }
            }
            currentBoard = null;                                    //get the updated board
            while (currentBoard == null) {
                currentBoard = Board.loadBoard();
            }
        }
        keyboardInput.getKeyboardInput("\nPress ENTER to exit...");
    }
    //****************************************************************************************
    
    //Method:       Manual()
    //Description:  Calls KeyboardInputClass to get row and column values, checks for the validity of the entered
    //              location and places the tile in that location before returning a string to be used by the shell
    //              as "myMove".
    //Parameters:   Board           currentBoard            the board before tiles are flipped.
    //Returns:      String          contains the row and column values in their character format.
    //Calls:        keyboardInputClass
    //              IsValidMove()
    //              PlaceTile()
    //Globals:      char            myColor                 the color of the player who is manually placing the tile
    //              char            opponentColor           the color of the player's opponent
    static String Manual(Board currentBoard){
        KeyboardInputClass keyboardInputClass = new KeyboardInputClass();
        boolean validMove = false;
        while(!validMove){
            char rowChar = keyboardInputClass.getCharacter(false, 'A', null, 1, "Enter the row in which to place the tile:");
            char colChar = keyboardInputClass.getCharacter(false, 'A', null, 1, "Enter the column in which to place the tile:");
            int rowNum = ((int)rowChar)-65;
            int colNum = ((int)colChar)-65;
            if(IsValidMove(rowNum, colNum, currentBoard, myColor, opponentColor)){
                PlaceTile(rowNum, colNum, currentBoard, myColor, opponentColor);
                return Character.toString(rowChar) + Character.toString(colChar);
            }else{
                System.out.println("That was an invalid move.");
            }//End of IF valid move statement
        }//End while no valid move has been made loop
        return"";
    }//End Manual method
    //******************************************************************************************
    
    //Method:       Minimax()
    //Description:  Acts like a regular recusive mini-max method with the option of alpha-beta pruning.
    //              BASE CASE: if node is at the maximum depth to be searched or if the board is completely full of tiles.
    //              FOR BOTH MAX AND MIN: loops through all positions on the board checking the validity of a move,
    //              when a move is found, a new board is cloned from the original, the tile is placed and the search is called
    //              again, swapping whose turn it is to place a tile.
    //              FOR MAX ONLY: the values of it's children are calculated and the greatest of those is saved as a best move.
    //              FOR MIN ONLY: the values of it's children are claculated and the least of those is saved as a best move.
    //Parameters:   Move            parentMove              the move from which this current node came from
    //              Board           currentBoard            the board object class for this current node
    //              int             maxDepth                the maximum depth at wich to search the game tree
    //              int             currentDepth            the depth of the current node in the game tree
    //              char            player                  the player that is currently selecting a move
    //              char            opponent                the player that is not selecting a move
    //Returns:      Move            bestNextMove            the best option for the player at this depth
    //calls:        Move
    //              IsValidMove()
    //              PlaceTile()
    //              MiniMax()       (recursively)
    //Globals:      boolean         alphaBetaPruning        dictates when pruning is used or not
    static Move MiniMax(Move parentMove, Board parentBoard, int maxDepth, int currentDepth, char player, char opponent){
        int score;
        Board childBoard;
        Move childMove;
        Move bestChild = new Move(parentMove.alpha, parentMove.beta, -1, -1);
        if(currentDepth == maxDepth || BoardIsFull(parentBoard)){
            score = Score(parentBoard);
            parentMove.alpha = score;
            parentMove.beta = score;
            return parentMove;
        }else{
            for (int row = 0; row < parentBoard.boardRows; row++) {
                for (int col = 0; col < parentBoard.boardCols; col++) {
                    if(IsValidMove(row, col, parentBoard, player, opponent)){
                        childBoard = CopyBoard(parentBoard);
                        PlaceTile(row, col, childBoard, player, opponent);
                        childMove = new Move(parentMove.alpha, parentMove.beta, row, col);
                        if(player == myColor){
                            childMove = MiniMax(childMove, childBoard, maxDepth, currentDepth + 1, opponent, player);
                            score = childMove.beta;
                            if(score > parentMove.alpha){
                                parentMove.alpha = score;
                                bestChild = new Move(parentMove.alpha, parentMove.beta, row, col);
                            }//End update MAX's alpha value and best next move
                        }else{
                            childMove = MiniMax(childMove, childBoard, maxDepth, currentDepth + 1, opponent, player);
                            score = childMove.alpha;
                            if(score < parentMove.beta){
                                parentMove.beta = score;
                                bestChild = new Move(parentMove.alpha, parentMove.beta, row, col);
                            }//End update MIN's beta value and best next move
                        }//End IF my move ELSE opponent's move statement
                    }//End if move is valid statement
                    if(parentMove.alpha >= parentMove.beta && alphaBetaPruning){
                        return bestChild;
                    }//End prune condition
                }//End column for loop
            }//End row for loop
        }//End IF base case ELSE statement
        return bestChild;
    }//End MiniMax method
    //********************************************************************************************
    
    //Method:       BoardIsFull()
    //Description:  Checks if there are any empty spaces in the board, if there are not, then the game has ended
    //Parameters:   Board           currentBoard            the board that is being checked
    //Returns       boolean         TRUE - there are no empty spaces, thus the game is over.
    //                              FALSE - there is at least one empty space.
    //Calls:        Nothing.
    //Globals:      None.
    static boolean BoardIsFull(Board currentBoard){
        for (int row = 0; row < currentBoard.boardRows; row++) {
            for (int col = 0; col < currentBoard.boardCols; col++) {
                if(currentBoard.board[row][col] == ' '){
                    return false;
                }//End IF space is blank
            }//End column for loop
        }//End row for loop
        return true;
    }//End BoardIsFull method
    //***************************************************************************************

    //Method:       Score()
    //Description:  scores the board that is passed in according to the following rules:
    //              For each of my own tiles score is incremented by 2, if they are on the
    //              edges, that is another 4 points, and if it is in a corner, plus another 4 still.
    //              For each of the opponent's tiles, decrement the score by 1, if they are on an
    //              edge, subtract another 3, and for corners, another 3 still.
    //Parameters:   Board           currentBoard            the board being scored
    //Returns:      int             score                   the score of this board
    //Calls:        Nothing.
    //Globals:      boolean         randomGame              a random score is assigned to the board
    //              char            myColor                 the color of my tiles
    //              char            opponentColor           the color of my opponent's tiles
    static int Score(Board currentBoard) {
        int score = 0;
        if (randomGame) {
            score = (int) (Math.random() * 200);
        } else {
            for (int row = 0; row < currentBoard.boardRows; row++) {
                for (int col = 0; col < currentBoard.boardCols; col++) {
                    if (currentBoard.board[row][col] == myColor) {
                        score = score + 2;
                        if (row == 0 || row == currentBoard.boardRows - 1){
                            score = score + 4;
                        }//End IF on outter row statement
                        if(col == 0 || col == currentBoard.boardCols - 1) {
                            score = score + 4;
                        }//End IF on outter column statement
                    }//End IF tile is my own statement
                    if (currentBoard.board[row][col] == opponentColor) {
                        score = score - 1;
                        if (row == 0 || row == currentBoard.boardRows - 1){
                            score = score - 3;
                        }//End IF on outter row statement
                        if(col == 0 || col == currentBoard.boardCols - 1) {
                            score = score - 3;
                        }//End IF on outter column statement
                    }//End IF tile is opponent's statement
                }//End column for loop
            }//End row for loop
        }//End IF random game ELSE statement
        return score;
    }//End Score method
    //***************************************************************************************
    
    //Method:       IsValidMove()
    //Description:  checks the Moore (8) neighborhood for the given row and column on the board for
    //              any validation of the proposed move.
    //Parameters:   int             row                     the row of the proposed move
    //              int             col                     the column of the proposed move
    //              Board           currentBoard
    //              char            player                  the players tile character
    //              char            opponent                the opponent's tile character
    //Returns:      boolean         TRUE - the move is a valid one in at least one direction
    //                              FALSE - the move is not a valid move
    //Calls:        IsValidDirection()
    //Globals:      None.
    static boolean IsValidMove(int row, int col, Board currentBoard, char player, char opponent) {
        if (currentBoard.board[row][col] != ' ') {
            return false;
        }//End IF selected location is not empty statement
        //Look North
        if (IsValidDirection(currentBoard, row, -1, col, 0, player, opponent, false)) {
            return true;
        }//End IF North statement
        //Look NorthEast
        if (IsValidDirection(currentBoard, row, -1, col, 1, player, opponent, false)) {
            return true;
        }//End IF NorthEast statement
        //Look East
        if (IsValidDirection(currentBoard, row, 0, col, 1, player, opponent, false)) {
            return true;
        }//End IF East statement
        //Look SouthEast
        if (IsValidDirection(currentBoard, row, 1, col, 1, player, opponent, false)) {
            return true;
        }//End IF SouthEast statement
        //Look South
        if (IsValidDirection(currentBoard, row, 1, col, 0, player, opponent, false)) {
            return true;
        }//End IF South statement
        //Look SouthWest
        if (IsValidDirection(currentBoard, row, 1, col, -1, player, opponent, false)) {
            return true;
        }//End IF SouthWest statement
        //Look West
        if (IsValidDirection(currentBoard, row, 0, col, -1, player, opponent, false)) {
            return true;
        }//End IF West statement
        //Look NorthWest
        if (IsValidDirection(currentBoard, row, -1, col, -1, player, opponent, false)) {
            return true;
        }//End IF NorthWest statement
        return false;
    }//End IsValidMove method
    //****************************************************************************************
    
    //Method:       IsValidDirection()
    //Description:  given a row, column, and dirention (passed in via a row and column modifier),
    //              this method checks if there is an opponent tile imediately next to the current position,
    //              as well as one of the payers tiles following that opponent tile. There can be any number of opponent tiles
    //              betweent them.
    //Parameters:   Board           currentBoard
    //              int             row
    //              int             rowModifier
    //              int             col
    //              int             colModifier
    //              char            player
    //              char            opponent
    //              boolean         opponentFirst           TRUE - there was an opponent tile next to the origin
    //                                                      FALSE - no opponent tile has been found (yet)
    //Returns:      boolean         TRUE - the direction constitutes a valid move
    //                              FALSE - the direction fails to meet the criteria of a valid move
    //Calls:        IsValidDirection()  (recursively)
    //Globals:      None.
    static boolean IsValidDirection(Board currentBoard, int row, int rowModifier, int col, int colModifier, char player, char opponent, boolean opponentFirst) {

        //Shift to row and column
        row += rowModifier;
        col += colModifier;
        //Boundry check
        if (row >= 0 && row < currentBoard.boardRows
                && col >= 0 && col < currentBoard.boardCols) {

            if (currentBoard.board[row][col] == player && opponentFirst) {
                return true;
            }//End IF current space is player && opponentFirst statement
            if (currentBoard.board[row][col] == opponent) {
                return IsValidDirection(currentBoard, row, rowModifier, col, colModifier, player, opponent, true);
            }//End IF current space is opponent tile statement
        }//End boundry check
        return false;
    }//End IsValidDirection method
    //**********************************************************************************************
    
    //Method:       CopyBoard()
    //Description   clones the board passes in with a deep copy
    //Parameters:   Board           currentBoard
    //Returns:      Board           newBoard
    //Calls:        Board
    //Globals:      None.
    static Board CopyBoard(Board currentBoard) {
        Board newBoard = new Board(currentBoard.boardRows, currentBoard.boardCols, currentBoard.maxMoveTime);
        newBoard.status = currentBoard.status;
        newBoard.whoseTurn = currentBoard.whoseTurn;
        newBoard.move = currentBoard.move;
        newBoard.colorSelected = currentBoard.colorSelected;
        for (int row = 0; row < newBoard.board.length; row++) {
            for (int col = 0; col < newBoard.board[0].length; col++) {
                newBoard.board[row][col] = currentBoard.board[row][col];
            }//End column for loop
        }//End row for loop
        return newBoard;
    }//End CopyBoard method
    //**************************************************************************************************
    
    //Method:       PlaceTile()
    //Description:  takes in a Board, row and column then places the players tile, calling FlipTiles()
    //              for all the appropriate directions.
    //Parameters:   int             row
    //              int             col
    //              Board           currentBoard
    //              char            player
    //              char            opponent
    //Returns:      void
    //Calls:        IsValidDirection()
    //              FlipTiles()
    //Globals:      None.
    static void PlaceTile(int row, int col, Board currentBoard, char player, char opponent) {
        currentBoard.board[row][col] = player;
        //move all valid tiles to the North
        if (IsValidDirection(currentBoard, row, -1, col, 0, player, opponent, false)) {
            FlipTiles(currentBoard, row, -1, col, 0, player, opponent);
        }//End move tile to North
        //move all valid tiles to the NorthEast
        if (IsValidDirection(currentBoard, row, -1, col, 1, player, opponent, false)) {
            FlipTiles(currentBoard, row, -1, col, 1, player, opponent);
        }//End move tile to NorthEast
        //move all valid tiles to the East
        if (IsValidDirection(currentBoard, row, 0, col, 1, player, opponent, false)) {
            FlipTiles(currentBoard, row, 0, col, 1, player, opponent);
        }//End move tile to East
        //move all valid tiles to the SouthEast
        if (IsValidDirection(currentBoard, row, 1, col, 1, player, opponent, false)) {
            FlipTiles(currentBoard, row, 1, col, 1, player, opponent);
        }//End move tile to SouthEast
        //move all valid tiles to the South
        if (IsValidDirection(currentBoard, row, 1, col, 0, player, opponent, false)) {
            FlipTiles(currentBoard, row, 1, col, 0, player, opponent);
        }//End move tile to South
        //move all valid tiles to the SouthWest
        if (IsValidDirection(currentBoard, row, 1, col, -1, player, opponent, false)) {
            FlipTiles(currentBoard, row, 1, col, -1, player, opponent);
        }//End move tile to SouthWest
        //move all valid tiles to the West
        if (IsValidDirection(currentBoard, row, 0, col, -1, player, opponent, false)) {
            FlipTiles(currentBoard, row, 0, col, -1, player, opponent);
        }//End move tile to West
        //move all valid tiles to the NorthWest
        if (IsValidDirection(currentBoard, row, -1, col, -1, player, opponent, false)) {
            FlipTiles(currentBoard, row, -1, col, -1, player, opponent);
        }//End move tile to NorthWest
    }//End PlaceTile method
    //*************************************************************************************************
    
    //Method:       FlipTiles()
    //Description:  given a row/column location, and a direction, flips all the tiles in that direction until the players other
    //              tile is hit.
    //Parameters:   Board           currentBoard
    //              int             row
    //              int             rowModifier
    //              int             col
    //              int             colModifier
    //              char            player
    //              char            opponent
    //Returns:      void
    //Calls:        FlipTiles()     (recursively)
    //Globals:      None.
    static void FlipTiles(Board currentBoard, int row, int rowModifier, int col, int colModifier, char player, char opponent) {
        //Shift to row and column
        row = row + rowModifier;
        col = col + colModifier;
        if (currentBoard.board[row][col] == opponent) {
            currentBoard.board[row][col] = player;
            FlipTiles(currentBoard, row, rowModifier, col, colModifier, player, opponent);
        }//End IF tile is opponent's tile statement
    }//End FlipTiles method
    //*********************************************************************************************
    
    //Method:       CloneMove()
    //Description:  makes a deep copy of a Move object and returns the copy
    //Parameters:   Move            parent
    //Returns:      Move            clone
    //Calls:        Nothing
    //Globals:      None.
    static Move CloneMove(Move parent){
        Move clone = new Move(parent.alpha, parent.beta, parent.row, parent.col);
        return clone;
    }//End CloneMove method
    
}//End Othello shell class
//*******************************************************************************************************
//*******************************************************************************************************
//Class:        Board
//Description:  Othello board and related parms

class Board implements Serializable {

    char status;        //0=set-up for a new game is in progress; 1=a game is in progress; 2=game is over
    char whoseTurn;     //'?'=no one's turn yet--game has not begun; 'B'=black; 'W'=white
    String move;        //the move selected by the current player (as indicated by whoseTurn)
    char colorSelected; //'B' or 'W' indicating the color chosen by the first player to access the file
    //for a new game ('?' if neither player has yet chosen a color)
    //Note: this may or may not be the color for the player accessing the file
    int maxMoveTime;    //maximum time allotted for a move (in seconds)
    int boardRows;      //size of the board (allows for variations on the standard 8x8 board)
    int boardCols;
    char board[][];     //the board. Positions are filled with: blank = no piece; 'B'=black; 'W'=white
    //***************************************************************************************************
    //Method:       Board
    //Description:  Constructor to create a new board object
    //Parameters:	rows - size of the board
    //              cols
    //              time - maximum time (in seconds) allowed per move
    //Calls:		nothing
    //Returns:		nothing

    Board(int rows, int cols, int time) {
        int r, c;
        status = 0;
        whoseTurn = 'B';        //Black always makes the first move
        move = "*";
        colorSelected = '?';
        maxMoveTime = time;
        boardRows = rows;
        boardCols = cols;
        board = new char[boardRows][boardCols];
        for (r = 0; r < boardRows; r++) {
            for (c = 0; c < boardCols; c++) {
                board[r][c] = ' ';
            }
        }
        r = boardRows / 2 - 1;
        c = boardCols / 2 - 1;
        board[r][c] = 'W';
        board[r][c + 1] = 'B';
        board[r + 1][c] = 'B';
        board[r + 1][c + 1] = 'W';
    }

    //***************************************************************************************************
    //Method:       saveBoard
    //Description:  Saves the current board to disk as a binary file named "OthelloBoard"
    //Parameters:	none
    //Calls:		nothing
    //Returns:		true if successful; false otherwise
    public boolean saveBoard() {
        try {
            ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream("OthelloBoard"));
            outStream.writeObject(this);
            outStream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    //***************************************************************************************************
    //Method:       loadBoard
    //Description:  Loads the current Othello board and data from a binary file
    //Parameters:   none
    //Calls:        nothing
    //Returns:      a Board object (or null if routine is unsuccessful)
    public static Board loadBoard() {
        try {
            ObjectInputStream inStream = new ObjectInputStream(new FileInputStream("OthelloBoard"));
            Board boardObject = (Board) inStream.readObject();
            inStream.close();
            return boardObject;
        } catch (Exception e) {
        }
        return null;
    }

    //***************************************************************************************************
    //Method:       showBoard
    //Description:  Displays the current Othello board using extended Unicode characters. Looks fine
    //               in a command window but may not display well in the NetBeans IDE...
    //Parameters:   none
    //Calls:        nothing
    //Returns:      nothing
    public void showBoard() {
        int r, c;
        System.out.print("  ");                         //column identifiers
        for (c = 0; c < boardCols; c++) {
            System.out.print(" " + (char) (c + 65));
        }
        System.out.println();

        //top border
        System.out.print("  " + (char) 9484);                   //top left corner \u250C
        for (c = 0; c < boardCols - 1; c++) {
            System.out.print((char) 9472);               //horizontal \u2500
            System.out.print((char) 9516);               //vertical T \u252C
        }
        System.out.print((char) 9472);                   //horizontal \u2500
        System.out.println((char) 9488);                 //top right corner \u2510

        //board rows
        for (r = 0; r < boardRows; r++) {
            System.out.print(" " + (char) (r + 65));         //row identifier
            System.out.print((char) 9474);               //vertical \u2502
            for (c = 0; c < boardCols; c++) {
                System.out.print(board[r][c]);
                System.out.print((char) 9474);           //vertical \u2502
            }
            System.out.println();

            //insert row separators
            if (r < boardRows - 1) {
                System.out.print("  " + (char) 9500);           //left T \u251C
                for (c = 0; c < boardCols - 1; c++) {
                    System.out.print((char) 9472);       //horizontal \u2500
                    System.out.print((char) 9532);       //+ (cross) \u253C
                }
                System.out.print((char) 9472);           //horizontal \u2500
                System.out.println((char) 9508);         //right T \u2524
            }
        }

        //bottom border
        System.out.print("  " + (char) 9492);                   //lower left corner \u2514
        for (c = 0; c < boardCols - 1; c++) {
            System.out.print((char) 9472);               //horizontal \u2500
            System.out.print((char) 9524);               //upside down T \u2534
        }
        System.out.print((char) 9472);                   //horizontal \u2500
        System.out.println((char) 9496);                 //lower right corner \u2518

        return;
    }
    //***************************************************************************************************
}
//*******************************************************************************************************
//*******************************************************************************************************

class Move{
    int alpha;
    int beta;
    int row;
    int col;
    
    public Move(int a, int b, int r, int c){
        alpha = a;
        beta = b;
        row = r;
        col = c;
    }
}