//Program:      McKinney2.java
//Course:       COSC470
//Description:  This program evaluates the game trees of the "Letters Squared" puzzle using
//              a breadth-first (brute force) search, and a  best-first search which implements
//              a "sum of tiles out of place" heuristic. Best-first search as has the option
//              to be run with or without a depth penalty when evaluating the "score" of the boards.
//Author:       Joshua McKinney
//Revised:      3/6/2017
//Language:     Java
//IDE:          NetBeans 8.0.2
//Notes:        None.
//*******************************************************************************
//*******************************************************************************

//Class:        McKinney2
//Description:  Main class
//Globals:      boolean             useDepthPenalty         keeps track of when user wants depth penalty to be applied
//              boolean             bestFirst               if true, runs program using best-first search, if false, uses breadth-first
//              char[][]            goalstate               The two dimentional array of characters that represents the target board
public class McKinney2 {

    static boolean useDepthPenalty;
    static boolean bestFirst = false;
    static char[][] goalstate;

    //***************************************************************************
    //Method:       main
    //Description:  main method of the program
    //Parameters:   String[] args       (default main method parameters)
    //Returns:      void
    //Calls:        KeyboardInputClass
    //              PuzzleBoard
    //              PriorityQueue
    //              StringToBoard
    //              ScrambleBoard
    //              GetHeuristicScore
    //              GenerateChildren
    //              StepThroughSolution
    //Globals:      boolean             useDepthPenalty
    //              boolean             bestFirst
    //              char[][]            goalstate
    
    public static void main(String[] args) {

        System.out.println("           McKinney \"Letters Squared\" puzzle solver");
        while (true) {
            PuzzleBoard currentBoard;
            PriorityQueue open = new PriorityQueue();
            int openCount = 0;
            PriorityQueue closed = new PriorityQueue();
            int closedCount = 0;
            KeyboardInputClass keyboardInput = new KeyboardInputClass();
            int boardSize = keyboardInput.getInteger(false, 0, 0, 0, "Enter size of the puzzle (ie: \'3\' for 3x3, \'4\' for 4x4, exc...) or \'0\' to exit:");
            if (boardSize == 0) {
                System.exit(0);
            } else {
                String targetString = keyboardInput.getString(null, "Enter the TARGET String and use spaces for blanks: ");
                if (targetString == null) {
                    System.exit(0);
                }
                char[][] targetBoard = StringToBoard(targetString, boardSize);
                goalstate = StringToBoard(targetString, boardSize);

                int shuffles = keyboardInput.getInteger(false, -1, 0, 0, "Enter the number of shuffle moves desired:"
                        + "\nPress ENTER to manually construct board:");
                if (shuffles == -1) {
                    currentBoard = new PuzzleBoard();
                    String startString = keyboardInput.getString(null, "Enter the START string and use spaces for blanks:");
                    char[][] start = StringToBoard(startString, boardSize);
                    currentBoard.ConstructBoard(boardSize, start);
                    currentBoard.depth = 0;
                }//End of ManualConstructBoard if statement
                else {
                    currentBoard = new PuzzleBoard();
                    currentBoard.ConstructBoard(boardSize, targetBoard);
                    currentBoard = (ScrambleBoard(currentBoard, shuffles, open, closed));
                    currentBoard.depth = 0;
                    while (!closed.isEmpty()) {
                        closed.dequeue();
                    }
                    while (!open.isEmpty()) {
                        open.dequeue();
                    }
                }//End else ManualConstructBoard

                System.out.println("Board to be solved:");
                PrintBoard(currentBoard);

                int useBestFirst = keyboardInput.getInteger(false, 1, 0, 0, "Search mode (enter \'1\' for breadth-first or \n"
                        + "\'2\' for best-first (sum of tiles out of place heuristic):");

                if (useBestFirst == 2) {
                    bestFirst = true;
                    char depthPenaty = keyboardInput.getCharacter(false, 'Y', null, 1, "Include depth in heuristic evaluation? \'Y\' or \'N\' (Default \'Y\'):");
                    if (depthPenaty == 'N') {
                        useDepthPenalty = false;
                        currentBoard.boardScore = GetHeuristicScore(currentBoard);
                    }//End of no depth penalty if statement
                    else {
                        useDepthPenalty = true;
                        currentBoard.boardScore = GetHeuristicScore(currentBoard) + currentBoard.depth;
                    }//End of depth penalty used if statement
                }//End of best-first search setup
                else {
                    bestFirst = false;
                }//End of breadth-first search setup
                System.out.println("Finding solution...");
                while (GetHeuristicScore(currentBoard) != 0) {
                    GenerateChildren(currentBoard, open, closed);
                    currentBoard = (PuzzleBoard) open.dequeue();
                }//End while unsolved loop
                System.out.println("Solution found at depth " + currentBoard.depth);
                keyboardInput.getKeyboardInput("Press Enter to show all boards");

                StepThroughSolution(currentBoard);
                while (!open.isEmpty()) {
                    open.dequeue();
                    openCount++;
                }//End of dequeue open and count
                while (!closed.isEmpty()) {
                    closed.dequeue();
                    closedCount++;
                }//End of dequeue closed and count
                System.out.println("Nodes in OPEN: " + openCount + "; CLOSED: " + closedCount);
            }//End of continueProgram if statement
        }//End of continueProgram while loop        
    }//End of main method
    //******************************************************************************

    //Method:       StringToBoard
    //Description:  passes in a string and converts it into a two-dimentional char array
    //              that is N by N characters if N = board size.
    //Parameters:   Sting                   input                   the String to be converted into a char[][]
    //              int                     boardSize               the length and width od the board
    //Returns:      char[][]                board                   the N by N board of chars
    //Calls:        Nothing.
    //Globals:      None.
    public static char[][] StringToBoard(String input, int boardSize) {

        char[] charArray = input.toCharArray();
        char[][] board = new char[boardSize][boardSize];
        int count = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++, count++) {
                if (charArray[count] == ' ') {
                    board[i][j] = '.';
                } else {
                    board[i][j] = charArray[count];
                }
            }
        }
        return board;
    }
    //*******************************************************************************

    //Method:       GenerateChildren
    //Description:  Generates all possible children of a given board and add all children to PriorityQueue open
    //Parameters:   PuzzleBoard             currentBoard            The current puzzle to have children generated
    //              PriorityQueue           open                    queue holding generated boards that have not been tried yet
    //              PriorityQueue           close                   queue holding all boards that have been tried
    //Returns:      void
    //Calls:        PuzzleBoard
    //              MoveIsValid
    //              MoveTile
    //Globals:      boolean                 bestFirst
    public static void GenerateChildren(PuzzleBoard currentPuzzle, PriorityQueue open, PriorityQueue closed) {

        PuzzleBoard childPuzzle = null;
        for (int i = 0; i < currentPuzzle.board.length; i++) {
            for (int j = 0; j < currentPuzzle.board.length; j++) {
                if (currentPuzzle.board[i][j] != '.') {
                    if (MoveIsValid(currentPuzzle, i, j, 'N')) {
                        childPuzzle = MoveTile(currentPuzzle, i, j, 'N', open, closed);
                    }
                    if (childPuzzle != null) {
                        if (bestFirst) {
                            open.priorityEnqueue(childPuzzle);
                        } else {
                            open.enqueue(childPuzzle);
                        }
                    }
                    //check north
                    if (MoveIsValid(currentPuzzle, i, j, 'S')) {
                        childPuzzle = MoveTile(currentPuzzle, i, j, 'S', open, closed);
                    }
                    if (childPuzzle != null) {
                        if (bestFirst) {
                            open.priorityEnqueue(childPuzzle);
                        } else {
                            open.enqueue(childPuzzle);
                        }
                    }
                    //check south
                    if (MoveIsValid(currentPuzzle, i, j, 'E')) {
                        childPuzzle = MoveTile(currentPuzzle, i, j, 'E', open, closed);
                    }
                    if (childPuzzle != null) {
                        if (bestFirst) {
                            open.priorityEnqueue(childPuzzle);
                        } else {
                            open.enqueue(childPuzzle);
                        }
                    }
                    //check east
                    if (MoveIsValid(currentPuzzle, i, j, 'W')) {
                        childPuzzle = MoveTile(currentPuzzle, i, j, 'W', open, closed);
                    }
                    if (childPuzzle != null) {
                        if (bestFirst) {
                            open.priorityEnqueue(childPuzzle);
                        } else {
                            open.enqueue(childPuzzle);
                        }
                    }
                    //check west
                }
            }
        }
        closed.enqueue(currentPuzzle);
    }//End of GenerateChildren method
    //***************************************************************************

    //Method:       MoveIsValid
    //Description:  passes in a board, tile and direction and determines whether or not moving that tile in the
    //              specified direction is a valid move or not.
    //Parameters:   PuzzleBoard             board
    //              int                     row
    //              int                     col
    //              char                    direction
    //Returns       boolean                 true: there is a free space and the direction is not out of bounds.
    //                                      false: there is a tile in the way or the direction is out of bounds.
    //Calls:        Nothing
    //Globals:      None
    public static boolean MoveIsValid(PuzzleBoard board, int row, int col, char direction) {
        switch (direction) {
            case 'N':
                if (row - 1 >= 0) {
                    if (board.board[row - 1][col] == '.') {
                        return true;
                    }
                }
                return false;
            case 'S':
                if (row + 1 < board.boardSize) {
                    if (board.board[row + 1][col] == '.') {
                        return true;
                    }
                }
                return false;
            case 'E':
                if (col + 1 < board.boardSize) {
                    if (board.board[row][col + 1] == '.') {
                        return true;
                    }
                }
                return false;
            case 'W':
                if (col - 1 >= 0) {
                    if (board.board[row][col - 1] == '.') {
                        return true;
                    }
                }
                return false;
        }//End of direction Switch
        return true;
    }//End of MoveIsValid method
    //***********************************************************************************

    //Method:       MoveTile
    //Description:  moves the blank tile in a specified direction and returns the resulting board as a new board.
    //              If the new board is already in Open or Close, the method returns null.
    //Parameters:   PuzzleBoard             parent
    //              int                     row                 the row of the tile to be moved
    //              int                     col                 the column of the tile to be moved
    //              char                    direction           the direction in which to move the blank tile
    //              PriorityQueue           open                unvisited children
    //              PriorityQueue           close               visited children
    //Returns:      PuzzleBoard             childBoard          the new board with the blank tile moved into new position
    //              null                                        if the childboard has already been generated.
    //Calls:        PuzzleBoard
    //              GetHeuristicScore
    //              BoardIsInQueue
    //Globals:      useDepthPenalty
    public static PuzzleBoard MoveTile(PuzzleBoard parent, int row, int col, char direction, PriorityQueue open, PriorityQueue closed) {

        PuzzleBoard childBoard = CopyBoard(parent);
        switch (direction) {
            case 'N':
                childBoard.board[row - 1][col] = childBoard.board[row][col];
                childBoard.board[row][col] = '.';
                break;
            //move north

            case 'S':
                childBoard.board[row + 1][col] = childBoard.board[row][col];
                childBoard.board[row][col] = '.';
                break;
            //move south

            case 'E':
                childBoard.board[row][col + 1] = childBoard.board[row][col];
                childBoard.board[row][col] = '.';
                break;
            //move east

            case 'W':
                childBoard.board[row][col - 1] = childBoard.board[row][col];
                childBoard.board[row][col] = '.';
                break;
            //move west
        }//End direction switch
        if (!useDepthPenalty) {
            childBoard.boardScore = GetHeuristicScore(childBoard);
        } else {
            childBoard.boardScore = GetHeuristicScore(childBoard) + childBoard.depth;
        }
        if (!BoardIsInQueue(childBoard, open) && !BoardIsInQueue(childBoard, closed)) {
            return childBoard;
        } else {
            return null;
        }
    }//End of MoveTile method
    //***************************************************************************

    //Method:       CopyBoard
    //Description:  takes in a puzzle and deep copies it, creating an identical verion of the passed in puzzle
    //Parameters:   PuzzleBoard             oldPuzzle           Puzzle to be copied
    //Returns:      PuzzleBoard             newPuzzle           Puzzle after copy
    //Calls:        PuzzleBoard
    //Globals:      None
    public static PuzzleBoard CopyBoard(PuzzleBoard oldPuzzle) {

        PuzzleBoard newPuzzle = new PuzzleBoard();
        newPuzzle.board = new char[oldPuzzle.board.length][oldPuzzle.board.length];
        for (int i = 0; i < newPuzzle.board.length; i++) {
            for (int j = 0; j < newPuzzle.board.length; j++) {
                newPuzzle.board[i][j] = oldPuzzle.board[i][j];
            }//End of inner CopyBoard for loop            
        }//End of outer CopyBoard for loop
        newPuzzle.boardSize = oldPuzzle.boardSize;
        newPuzzle.depth = oldPuzzle.depth + 1;
        newPuzzle.parentBoard = oldPuzzle;
        return newPuzzle;
    }//End of CopyBoard method
    //***************************************************************************

    //Method:       PrintBoard
    //Description:  displays the board of the passed in puzzle in a formatted and visually pleasing way.
    //              Also displays the raw score, depth, and total score of the puzzle passed in.
    //Parameters:   PuzzleBoard             puzzle
    //Returns:      void
    //Calls:        GetHeuristicScore
    //Globals:      boolean                 bestFirst
    public static void PrintBoard(PuzzleBoard puzzle) {

        for (int i = 0; i < puzzle.board.length; i++) {
            for (int j = 0; j < puzzle.board.length; j++) {
                System.out.printf("%3c", puzzle.board[i][j]);
            }//End of inner PrintBoard for loop
            System.out.println("");
        }//End of outer PrintBoard for loop
        if (bestFirst) {
            System.out.println("Raw score = " + GetHeuristicScore(puzzle)
                    + "   Depth = " + puzzle.depth
                    + "   Total score = " + (GetHeuristicScore(puzzle) + puzzle.depth));
        }
            System.out.println("");
    }//End of PrintBoard method
    //***************************************************************************

    //Method:       GetHeuristicScore
    //Description:  evaluates the position of tiles on the board by summing the distances of each of the tiles from
    //              the position it would be in for a goal state. and produces a score from that sum.
    //Parameters:   PuzzleBoard     current                 the current board being scored
    //Returns:      int             sumOfTilesOutOfPlace
    //Calls:        Nothing
    //Globals:      char[][]        goalstate
    public static int GetHeuristicScore(PuzzleBoard current) {

        int sumOfTilesOutOfPlace = 0;
        for (int i = 0; i < current.board.length; i++) {
            for (int j = 0; j < current.board.length; j++) {
                if (current.board[i][j] != goalstate[i][j]) {
                    sumOfTilesOutOfPlace++;
                }
            }//End inner GetHeuristicScore for loop
        }//End outer GetHeuristicScore for loop
        return sumOfTilesOutOfPlace;
    }//End GetHeuristicScore method
    //***************************************************************************

    //Method:       ScrambleBoard
    //Description:  Passes in a puzzle with a board to be scrambled and a number of moves
    //              specified by the user and moves the blank tile that many times, making sure
    //              to never move back to the place it just came from.
    //Parameters:   PuzzleBoard             puzzle
    //              int                     moves           number of times the blank tile is moved
    //              PriorityQueue           open            unvisited nodes
    //              PriorityQueue           closed          visited nodes
    //Returns:      PuzzleBoard             puzzle          the scrambled board
    //Calls:        PuzzleBoard
    //              GenerateChildren
    //Globals:      None
    public static PuzzleBoard ScrambleBoard(PuzzleBoard puzzle, int moves, PriorityQueue open, PriorityQueue closed) {

        System.out.println("Shuffling...");
        for (int i = 0; i < moves; i++) {
            GenerateChildren(puzzle, open, closed);
            int childCount = open.length;
            int randomChild = (int) (Math.random() * childCount);
            while (randomChild >= 0) {
                randomChild--;
                puzzle = (PuzzleBoard) open.dequeue();
            }
            closed.enqueue(puzzle);
        }//End scramble for loop
        puzzle.parentBoard = null;

        return puzzle;
    }//End of ScrambleBoard method
    //***************************************************************************

    //Method:       StepThroughSolution
    //Description:  Passes in a PuzzleBoard and recurses, passing in the parent of the board previously 
    //              passed in. Then prints out the boards using the program stack. Effectively showing 
    //              a step by step solution to solve the puzzle originally passed in.
    //Parameters:   PuzzleBoard             board       
    //Returns:      void
    //Calls:        StepThroughSolution     (recursively)
    //Globals:      None
    public static void StepThroughSolution(PuzzleBoard board) {

        if (board.parentBoard != null) {
            StepThroughSolution(board.parentBoard);
        }//End of board.parentBoard != null if statement
        PrintBoard(board);
        board = null;
    }//End of StepThroughSolution method
    //***************************************************************************

    //Method:       BoardIsInQueue
    //Description:  Passes in a puzzle and a queue and dequeues each item chescking to see if any boards
    //              match the puzzle passed in. If there is a puzzle that has the same board, this method
    //              returns true. Otherwise, it returns false.
    //Parameters:   PuzzleBoard             puzzle
    //              PriorityQueue           originalQueue
    //Returns:      boolean                 boardFound
    //Calls:        PuzzleBoard
    //Globals:      None
    public static boolean BoardIsInQueue(PuzzleBoard puzzle, PriorityQueue originalQueue) {
        if (originalQueue.isEmpty()) {
            return false;
        }
        PuzzleBoard boardInQueue;
        boolean boardFound = false;
        PriorityQueue tempQueue = new PriorityQueue();
        while (!originalQueue.isEmpty()) {
            boardInQueue = (PuzzleBoard) originalQueue.dequeue();
            if (puzzle.Equals(boardInQueue)) {
                boardFound = true;
            }
            tempQueue.enqueue(boardInQueue);
        }//End of check for board in queue while loop
        while (!tempQueue.isEmpty()) {
            boardInQueue = (PuzzleBoard) tempQueue.dequeue();
            originalQueue.enqueue(boardInQueue);
        }//End of repopulating oldQueue from newQueue (put everything back where I found it)
        return boardFound;
    }//End of BoardIsInQueue method
    //***************************************************************************

}//End of class
//*******************************************************************************
//*******************************************************************************

//Class:        PuzzleBoard
//Description:  Constructs a square puzzle of a specified size and tracks where a black tile
//              is, the parent of the puzzle (if it has one), depth within the search tree,
//              and score of the board (determined by the sum of the distances of tiles out of place.
//Globals:      int             boardSize
//              int             depth
//              char[][]        board
//              PuzzleBoard     parentBoard
class PuzzleBoard implements Comparable<PuzzleBoard> {

    int boardSize;
    int depth;
    int boardScore;
    char[][] board;
    PuzzleBoard parentBoard;

    //***************************************************************************
    //Method:       ManualConstructBoard
    //Description:  Prompt the user to manually enter the numbers of the tiles in the rows
    //              and columns they desire to have them in, or ENTER for the blank tile.
    //              WARNING!! User may enter an unsolvable puzzle.
    //Parameters:   int             size            number of tiles on the board minus the blank tile
    //Returns:      void
    //Calls:        KeyboardInputClass
    //Globals:      int             boardSize
    //              char[][]        board
    public void ManualConstructBoard(int size) {

        KeyboardInputClass keyboardInput = new KeyboardInputClass();
        boardSize = size;
        String startString = keyboardInput.getString(null, "Enter the START String and use spaces for blanks: ");
        if (startString == null) {
            System.exit(0);
        }//End if null START String
        char[] startArray = startString.toCharArray();
        char[][] startBoard = new char[boardSize][boardSize];
        int count = 0;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++, count++) {
                if (startArray[count] == ' ') {
                    startBoard[i][j] = '.';
                } else {
                    startBoard[i][j] = startArray[count];
                }
            }
            System.out.println("");
        }
    }//End of ManualConstructBoard method
    //***************************************************************************

    //Method:       ConstructBoard
    //Description:  passes in the size of the board and generate the board and all of its
    //              attributes from that.
    //Parameters:   int             size            number of tiles in the board minus the blank tile
    //Returns:      void
    //Calls:        Nothing
    //Globals:      int             boardSize
    //              int             depth
    //              char[][]        board
    //              PuzzleBoard     parentBoard
    public void ConstructBoard(int size, char[][] input) {

        boardSize = size;
        board = new char[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = input[i][j];
            }//End of ConstructBoard inner for loop
        }//End of ConstructBoard outer for loop
        depth = 0;
        parentBoard = null;
    }//End of ConstructBoard method
    //***************************************************************************

    //Method:       compareTo
    //Description   Overrides the Comparable object type's compareTo method and evaluates them
    //              on the basis of their score.
    //Parameters:   PuzzleBoard         puzzle              the puzzle <this> is compared to
    //Returns:      int                 -1/1/0              returns -1 if <this> has a better score than
    //                                                      puzzle, 1 if <this>'s score is worse, and 0 otherwise
    //Calls:        Nothing
    //Globals:      int                 boardScore
    @Override
    public int compareTo(PuzzleBoard puzzle) {

        if (boardScore < puzzle.boardScore) {
            return -1;
        } else if (boardScore > puzzle.boardScore) {
            return 1;
        } else {
            return 0;
        }
    }//End of overridden compareTo method
    //***************************************************************************

    //Method:       Equals
    //Description:  acts as a suppliment to Comparable.equals(<Object>), comparing the puzzles
    //              on the basis of the values within board[][]. If the arrays are the same
    //              board, this returns true, and if any difference is found, this returns false
    //Parameters:   PuzzleBoard             puzzle          the puzzle <this> is compared to
    //Returns:      boolean
    //Calls:        Nothing
    //Globals:      char[][]                board
    public boolean Equals(PuzzleBoard puzzle) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] != puzzle.board[i][j]) {
                    return false;
                }
            }//End of inner for loop
        }//End of outer for loop
        return true;
    }//End of Equals method
    //***************************************************************************

}//End of PuzzleBoard class
//*******************************************************************************
//*******************************************************************************

class PriorityQueue implements QueueInterface, java.io.Serializable {

    private Node firstNode;
    private Node lastNode;
    int length = 0;

    //********************************************************************
    public PriorityQueue() {
        firstNode = null;
        lastNode = null;
    }

    //********************************************************************
    public void enqueue(Object newEntry) {
        Node newNode = new Node(newEntry, null);
        if (isEmpty()) {
            firstNode = newNode;
        } else {
            lastNode.setNextNode(newNode);
        }
        lastNode = newNode;
        length++;
    }

    //********************************************************************
    public boolean priorityEnqueue(Comparable newEntry) {
        Node newNode = new Node(newEntry);
        Node nodeBefore = getNodeBefore(newEntry);

        if (isEmpty() || (nodeBefore == null)) {
            newNode.setNextNode(firstNode);
            firstNode = newNode;
        } else {
            Node nodeAfter = nodeBefore.getNextNode();
            newNode.setNextNode(nodeAfter);
            nodeBefore.setNextNode(newNode);
        }
        length++;
        return true;
    }

    //********************************************************************
    private Node getNodeBefore(Comparable anEntry) {
        Node currentNode = firstNode;
        Node nodeBefore = null;

        while ((currentNode != null) && (anEntry.compareTo(currentNode.getData()) > 0)) {
            nodeBefore = currentNode;
            currentNode = currentNode.getNextNode();
        }
        return nodeBefore;
    }

    //********************************************************************
    public Object dequeue() {
        Object front = null;
        if (!isEmpty()) {
            front = firstNode.getData();
            firstNode = firstNode.getNextNode();
            if (firstNode == null) {
                lastNode = null;
            }
        }
        length--;
        return front;
    }

    //********************************************************************
    public Object getFront() {
        Object front = null;
        if (!isEmpty()) {
            front = firstNode.getData();
        }
        return front;
    }

    //********************************************************************
    public boolean isEmpty() {
        return firstNode == null;
    }

    //********************************************************************
    public void clear() {
        firstNode = null;
        lastNode = null;
        length = 0;
    }

    //********************************************************************
    //********************************************************************
    private class Node {

        private Object data;
        private Node next;

        //***************************************************************************
        private Node(Object dataPortion) {
            data = dataPortion;
            next = null;
        }
        //***************************************************************************

        private Node(Object dataPortion, Node nextNode) {
            data = dataPortion;
            next = nextNode;
        }
        //***************************************************************************

        private Object getData() {
            return data;
        }
        //***************************************************************************

        private void setData(Object newData) {
            data = newData;
        }
        //***************************************************************************

        private Node getNextNode() {
            return next;
        }
        //***************************************************************************

        private void setNextNode(Node nextNode) {
            next = nextNode;
        }
        //***************************************************************************
    }
	//*******************************************************************************
    //*******************************************************************************
}
//************************************************************************
//************************************************************************

