package McKinney2;



//Program:	McKinney2.java
//Course:	COSC470
//Description:	This program evaluates the game trees of the "Eights" puzzle using
//              using a sum of distance out of place heuristic.
//Author:	Joshua McKinney
//Revised:	2/11/2016
//Language:	Java
//IDE:		NetBeans 8.0.2
//Notes:        None.

//*******************************************************************************
//*******************************************************************************

//Class:        McKinney2
//Description:  Main class
//Globals:      boolean             continueProgram         when true, program stays within while loop,
//                                                          otherwise program stops looping
//              boolean             useDepthPenalty         keeps track of when user wants depth penalty to be applied
//              boolean             bestFirst               if true, runs program using best-first search, if false, uses breadth-first
public class McKinney2 {
    
    static boolean continueProgram = true;
    static boolean useDepthPenalty;
    static boolean bestFirst;
    
    //***************************************************************************
    
    //Method:       main
    //Description:  main method of the program
    //Parameters:   String[] args       (default main method parameters)
    //Returns:      void
    //Calls:        KeyboardInputClass
    //              PuzzleBoard
    //              PriorityQueue
    //              ScrambleBoard
    //              GenerateChildrenHeuristicly
    //              GenerateChildren
    //              StepThroughSolution
    //Globals:      boolean             continueProgram
    //              boolean             useDepthPenalty
    //              boolean             bestFirst
        
 public static void main(String[] args) {

     System.out.println("           McKinney \"Eights\" puzzle solver");
    while(continueProgram){
        PuzzleBoard currentBoard;
        KeyboardInputClass keyboardInput = new KeyboardInputClass();
        int boardSize = keyboardInput.getInteger(false, 0, 0, 0, "Enter size of the puzzle (ie: 8, 15, 24, 35...) or \'0\' to exit:");
        if (boardSize == 0)
            continueProgram = false;
        else{
            int shuffles = keyboardInput.getInteger(false, -1, 0, 0, "Number of shuffle moves desired?"
                    + "\nPress ENTER to manually construct board:");
            if(shuffles == -1){
                currentBoard = new PuzzleBoard();
                currentBoard.ManualConstructBoard(boardSize);
            }//End of ManualConstructBoard if statement
            else{
                currentBoard = new PuzzleBoard();
                currentBoard.ConstructBoard(boardSize);
                currentBoard = (ScrambleBoard(currentBoard, shuffles));
            }//End else ManualConstructBoard
            PriorityQueue open = new PriorityQueue();
            int openCount = 0;
            PriorityQueue closed = new PriorityQueue();
            int closedCount = 0;
            int useBestFirst = keyboardInput.getInteger(false, 1, 0, 0, "Search mode (enter \'1\' for breadth-first or \'2\' for best-first):");
            
            if(useBestFirst == 2){
                bestFirst = true;
                char depthPenaty = keyboardInput.getCharacter(false, 'Y', null, 1, "Include depth in heuristic evaluation? \'Y\' or \'N\' (Default \'Y\'):");
                if (depthPenaty == 'N'){
                    useDepthPenalty = false;
                    currentBoard.boardScore = currentBoard.GetHeuristicScore();
                }//End of no depth penalty if statement
                else{
                    useDepthPenalty = true;
                    currentBoard.boardScore = currentBoard.GetHeuristicScore()+currentBoard.depth;
                }//End of depth penalty used if statement
                System.out.println("Board to be solved:");
                PrintBoard(currentBoard);
            }//End of best-first search setup
            else{
                bestFirst = false;
            }//End of breadth-first search setup
            
            System.out.println("Finding solution...");
            while(currentBoard.GetHeuristicScore() != 0){
                GenerateChildren(currentBoard, open, closed);
                currentBoard = (PuzzleBoard) open.dequeue();
            }//End while unsolved loop
            
            StepThroughSolution(currentBoard);
                while(!open.isEmpty()){
                    open.dequeue();
                    openCount++;
                }//End of dequeue open and count
                while(!closed.isEmpty()){
                    closed.dequeue();
                    closedCount++;
                }//End of dequeue closed and count
                System.out.println("Nodes in OPEN: " + openCount + "; CLOSED: " + closedCount);
        }//End of continueProgram if statement
    }//End of continueProgram while loop        
 }//End of main method
 //******************************************************************************

    //Method:       GenerateChildren
    //Description:  Generates all possible children of a given board and add all children to PriorityQueue open
    //Parameters:   PuzzleBoard             currentBoard            The current puzzle to have children generated
    //              PriorityQueue           open                    queue holding generated boards that have not been tried yet
    //              PriorityQueue           close                   queue holding all boards that have been tried
    //Returns:      void
    //Calls:        PuzzleBoard
    //              MoveTile
    //Globals:      boolean                 bestFirst
 
    public static void GenerateChildren(PuzzleBoard currentPuzzle, PriorityQueue open, PriorityQueue closed){
        
        PuzzleBoard childPuzzle;
        
        if(currentPuzzle.rowOfBlankTile < currentPuzzle.board.length-1){
            childPuzzle = MoveTile(currentPuzzle, 'S');
            if(!BoardIsInQueue(childPuzzle, open) && !BoardIsInQueue(childPuzzle, closed)){
                if(bestFirst)
                    open.priorityEnqueue(childPuzzle);
                else
                    open.enqueue(childPuzzle);}
        }//End of try moving null tile south
        
        if(currentPuzzle.rowOfBlankTile > 0){
            childPuzzle = MoveTile(currentPuzzle, 'N');
            if(!BoardIsInQueue(childPuzzle, open) && !BoardIsInQueue(childPuzzle, closed)){
                if(bestFirst)
                    open.priorityEnqueue(childPuzzle);
                else
                    open.enqueue(childPuzzle);}
        }//End of try moving null tile north
        
        if(currentPuzzle.columnOfBlankTile < currentPuzzle.board.length-1){
            childPuzzle = MoveTile(currentPuzzle, 'E');
            if(!BoardIsInQueue(childPuzzle, open) && !BoardIsInQueue(childPuzzle, closed)){
                if(bestFirst)
                    open.priorityEnqueue(childPuzzle);
                else
                    open.enqueue(childPuzzle);}
        }//End of try moving null tile east
        
        if(currentPuzzle.columnOfBlankTile > 0){
            childPuzzle = MoveTile(currentPuzzle, 'W');
            if(!BoardIsInQueue(childPuzzle, open) && !BoardIsInQueue(childPuzzle, closed)){
                if(bestFirst)
                    open.priorityEnqueue(childPuzzle);
                else
                    open.enqueue(childPuzzle);}
        }//End of try moving null tile west
        
        closed.enqueue(currentPuzzle);
    }//End of GenerateChildren method
    //***************************************************************************
    
    //Method:       MoveTile
    //Description:  moves the blank tile in a specified direction and returns the resulting board as a new board
    //Parameters:   PuzzleBoard             parent
    //              char                    direction           the direction in which to move the blank tile
    //Returns:      PuzzleBoard             childBoard          the new board with the blank tile moved into new position
    //Calls:        PuzzleBoard
    //Globals:      useDepthPenalty
    
    public static PuzzleBoard MoveTile(PuzzleBoard parent, char direction){
        
        PuzzleBoard childBoard = CopyBoard(parent);
        int nullRow = childBoard.rowOfBlankTile;
        int nullCol = childBoard.columnOfBlankTile;
        int[][] board = childBoard.board;
        int nullNumber = childBoard.boardSize*childBoard.boardSize;
        switch(direction){
            
            case 'S':
                board[nullRow][nullCol] = board[nullRow+1][nullCol];
                board[nullRow+1][nullCol] = nullNumber;
                childBoard.rowOfBlankTile++;
                childBoard.depth++;
                childBoard.parentBoard = parent;
                break;
                
            case 'N':
                board[nullRow][nullCol] = board[nullRow-1][nullCol];
                board[nullRow-1][nullCol] = nullNumber;
                childBoard.rowOfBlankTile--;
                childBoard.depth++;
                childBoard.parentBoard = parent;
                break;
                
            case 'E':
                board[nullRow][nullCol] = board[nullRow][nullCol+1];
                board[nullRow][nullCol+1] = nullNumber;
                childBoard.columnOfBlankTile++;
                childBoard.depth++;
                childBoard.parentBoard = parent;
                break;
                
            case 'W':
                board[nullRow][nullCol] = board[nullRow][nullCol-1];
                board[nullRow][nullCol-1] = nullNumber;
                childBoard.columnOfBlankTile--;
                childBoard.depth++;
                childBoard.parentBoard = parent;
                break;
        }//End direction switch
        if(!useDepthPenalty)
            childBoard.boardScore = childBoard.GetHeuristicScore();
        else
            childBoard.boardScore = childBoard.GetHeuristicScore()+childBoard.depth;
        return childBoard;
    }//End of MoveTile method
    //***************************************************************************
    
    //Method:       CopyBoard
    //Description:  takes in a puzzle and deep copies it, creating an identical verion of the passed in puzzle
    //Parameters:   PuzzleBoard             oldPuzzle           Puzzle to be copied
    //Returns:      PuzzleBoard             newPuzzle           Puzzle after copy
    //Calls:        PuzzleBoard
    //Globals:      None
 
    public static PuzzleBoard CopyBoard(PuzzleBoard oldPuzzle){
        
        PuzzleBoard newPuzzle = new PuzzleBoard();
        newPuzzle.board = new int[oldPuzzle.board.length][oldPuzzle.board.length];
        for (int i = 0; i < newPuzzle.board.length; i++) {
            for (int j = 0; j < newPuzzle.board.length; j++) {
                newPuzzle.board[i][j] = oldPuzzle.board[i][j];                
            }//End of inner CopyBoard for loop            
        }//End of outer CopyBoard for loop
        newPuzzle.rowOfBlankTile = oldPuzzle.rowOfBlankTile;
        newPuzzle.columnOfBlankTile = oldPuzzle.columnOfBlankTile;
        newPuzzle.boardSize = oldPuzzle.boardSize;
        newPuzzle.depth = oldPuzzle.depth;
        return newPuzzle;
    }//End of CopyBoard method
    //***************************************************************************
    
    //Method:       PrintBoard
    //Description:  displays the board of the passed in puzzle in a formatted and visually pleasing way.
    //              Also displays the raw score, depth, and total score of the puzzle passed in.
    //Parameters:   PuzzleBoard             puzzle
    //Returns:      void
    //Calls:        PuzzleBoard
    //Globals:      None
    
    public static void PrintBoard(PuzzleBoard puzzle){
        
        for (int i = 0; i < puzzle.board.length; i++) {
            for (int j = 0; j < puzzle.board.length; j++) {
                if(puzzle.board[i][j] != (puzzle.boardSize*puzzle.boardSize))
                        System.out.printf("%3d", puzzle.board[i][j]);
                else System.out.print("   ");
            }//End of inner PrintBoard for loop
            System.out.println("");
        }//End of outer PrintBoard for loop
        System.out.println("Raw score = " + puzzle.GetHeuristicScore() +
                "   Depth = " + puzzle.depth +
                "   Total score = " + (puzzle.GetHeuristicScore() + puzzle.depth));
        System.out.println("");
    }//End of PrintBoard method
    //***************************************************************************
    
    //Method:       ScrambleBoard
    //Description:  Passes in a puzzle with a board to be scrambled and a number of moves
    //              specified by the user and moves the blank tile that many times, making sure
    //              to never move back to the place it just came from.
    //Parameters:   PuzzleBoard             puzzle
    //              int                     moves           number of times the blank tile is moved
    //Returns:      PuzzleBoard             puzzle
    //Calls:        PuzzleBoard
    //Globals:      None
    
    public static PuzzleBoard ScrambleBoard(PuzzleBoard puzzle, int moves){
        
        int lastCase = 5;
        for (int i = 0; i < moves; i++) {            
            int randomNum = (int) (Math.random()*100)%4;
            switch(randomNum){
                case 0:
                    if(puzzle.rowOfBlankTile < puzzle.board.length-1 && lastCase != 1){
                        puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile] = puzzle.board[puzzle.rowOfBlankTile+1][puzzle.columnOfBlankTile];
                        puzzle.board[puzzle.rowOfBlankTile+1][puzzle.columnOfBlankTile] = puzzle.boardSize*puzzle.boardSize;
                        puzzle.rowOfBlankTile++;
                        lastCase = 0;
                        break;}//End of case 0 if statement (check tile below)
                    else{
                        i--;
                        break;}//End of case 0 else statement
                case 1:
                    if(puzzle.rowOfBlankTile > 0 && lastCase != 0){
                        puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile] = puzzle.board[puzzle.rowOfBlankTile-1][puzzle.columnOfBlankTile];
                        puzzle.board[puzzle.rowOfBlankTile-1][puzzle.columnOfBlankTile] = puzzle.boardSize*puzzle.boardSize;
                        puzzle.rowOfBlankTile--;
                        lastCase = 1;
                        break;}//End of case 1 if statement (check tile above)
                    else{
                        i--;
                        break;}//End of case 1 else statement
                case 2:
                    if(puzzle.columnOfBlankTile < puzzle.board.length-1 && lastCase !=3){
                        puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile] = puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile+1];
                        puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile+1] = puzzle.boardSize*puzzle.boardSize;
                        puzzle.columnOfBlankTile++;
                        lastCase = 2;
                        break;}//End of case 2 if statement
                    else{
                        i--;
                        break;}//End of case 2 else statement (check tile to the right)
                case 3:
                    if(puzzle.columnOfBlankTile > 0 && lastCase != 2){
                        puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile] = puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile-1];
                        puzzle.board[puzzle.rowOfBlankTile][puzzle.columnOfBlankTile-1] = puzzle.boardSize*puzzle.boardSize;
                        puzzle.columnOfBlankTile--;
                        lastCase = 3;
                        break;}//End of case 3 if statement
                    else{
                        i--;
                        break;}//End of case 3 else statement (check tile to the left)
            }//End switch cases
        }//End scramble for loop
        
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
    
    public static void StepThroughSolution (PuzzleBoard board){
     
        if(board.parentBoard != null){
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
    public static boolean BoardIsInQueue(PuzzleBoard puzzle, PriorityQueue originalQueue){
        if(originalQueue.isEmpty())
            return false;
        PuzzleBoard boardInQueue;
        boolean boardFound = false;
        PriorityQueue tempQueue = new PriorityQueue();
        while(!originalQueue.isEmpty()){
            boardInQueue = (PuzzleBoard) originalQueue.dequeue();
            if (puzzle.Equals(boardInQueue))
                boardFound = true;
            tempQueue.enqueue(boardInQueue);
        }//End of check for board in queue while loop
        while(!tempQueue.isEmpty()){
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
//              int[][]         board
//              int             rowofBlankTile
//              int             columnOfBlankTile
//              PuzzleBoard     parentBoard
class PuzzleBoard implements Comparable<PuzzleBoard>{
    
    int boardSize;
    int depth;
    int boardScore;
    int[][] board;
    int rowOfBlankTile;
    int columnOfBlankTile;
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
    //              int             rowOfBlankTile
    //              int             columnOfBlankTile
    //              int[][]         board
        
    public void ManualConstructBoard(int size){
        
        boardSize = (int) Math.sqrt(size+1);
        board = new int[boardSize][boardSize];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                KeyboardInputClass keyboardInput = new KeyboardInputClass();
                int userInput = keyboardInput.getInteger(false, (boardSize*boardSize), 0, 0,
                        "Enter the tile number in row " + (i+1) + ", column " + (j+1) +
                        ": \n(press ENTER for the blank tile)");
                board[i][j] = userInput;
                if(userInput == (boardSize*boardSize)){
                    rowOfBlankTile = i;
                    columnOfBlankTile = j;
                }//End of null tile if statement
            }//End of ManualConstructBoard inner for loop            
        }//End of ManualConstructBoard outer for loop
    }//End of ManualConstructBoard method
    //***************************************************************************
    
    //Method:       ConstructBoard
    //Description:  passes in the size of the board and generate the board and all of its
    //              attributes from that.
    //Parameters:   int             size            number of tiles in the board minus the blank tile
    //Returns:      void
    //Calls:        Math
    //Globals:      int             boardSize
    //              int             depth
    //              int             rowOfBlankTile
    //              int             columnOfBlankTile
    //              int[][]         board
    //              PuzzleBoard     parentBoard
    
    public void ConstructBoard(int size){
        
        boardSize = (int) Math.sqrt(size+1);
        board = new int[boardSize][boardSize];
        int tileCount = 1;
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize && tileCount <= (boardSize*boardSize); j++, tileCount++) {
                board[i][j] = tileCount;                
            }//End of ConstructBoard inner for loop
        }//End of ConstructBoard outer for loop
        rowOfBlankTile = boardSize-1;
        columnOfBlankTile = boardSize-1;
        depth = 0;
        parentBoard = null;
    }//End of ConstructBoard method
    //***************************************************************************
    
    //Method:       GetHeuristicScore
    //Description:  evaluates the position of tiles on the board by summing the distances of each of the tiles from
    //              the position it would be in for a goal state. and produces a score from that sum.
    //Parameters:   None
    //Returns:      int             sumOfDistanceOutOfPlace
    //Calls:        Nothing
    //Globals:      int[][]         board
    
    public int GetHeuristicScore(){
        
        int sumOfDistanceOutOfPlace = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                sumOfDistanceOutOfPlace += Math.abs(((board[i][j] - 1)%board.length) - j);//difference in column
                sumOfDistanceOutOfPlace += Math.abs(((board[i][j] - 1)/board.length) - i);//difference in row
            }//End inner GetHeuristicScore for loop
        }//End outer GetHeuristicScore for loop
        return sumOfDistanceOutOfPlace;
    }//End GetHeuristicScore method
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
        
        if (boardScore < puzzle.boardScore)
            return -1;
        else if (boardScore > puzzle.boardScore)
            return 1;
        else
            return 0;
    }//End of overridden compareTo method
    //***************************************************************************
    
    //Method:       Equals
    //Description:  acts as a suppliment to Comparable.equals(<Object>), comparing the puzzles
    //              on the basis of the values within board[][]. If the arrays are the same
    //              board, this returns true, and if any difference is found, this returns false
    //Parameters:   PuzzleBoard             puzzle          the puzzle <this> is compared to
    //Returns:      boolean
    //Calls:        Nothing
    //Globals:      board[][]
    
    public boolean Equals(PuzzleBoard puzzle){
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] != puzzle.board[i][j])
                    return false;
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
    //********************************************************************
    public PriorityQueue() {
            firstNode = null;
            lastNode = null;
    }
    //********************************************************************
    public void enqueue(Object newEntry) {
            Node newNode = new Node(newEntry, null);
            if (isEmpty())
                    firstNode = newNode;
            else
                    lastNode.setNextNode(newNode);
            lastNode = newNode;
    }
    //********************************************************************
    public boolean priorityEnqueue(Comparable newEntry) {
            Node newNode = new Node(newEntry);
            Node nodeBefore = getNodeBefore(newEntry);

            if (isEmpty() || (nodeBefore == null)) {
                    newNode.setNextNode(firstNode);
                    firstNode = newNode;
            }
            else {
                    Node nodeAfter = nodeBefore.getNextNode();
                    newNode.setNextNode(nodeAfter);
                    nodeBefore.setNextNode(newNode);
            }
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
                    if (firstNode == null)
                            lastNode = null;
            }
            return front;
    }
    //********************************************************************
    public Object getFront() {
            Object front = null;
            if (!isEmpty())
                    front = firstNode.getData();
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
    }
    //********************************************************************
    //********************************************************************
    private class Node {
            private Object data;
            private Node next;

            private Node(Object dataPortion) {
                    data = dataPortion;
                    next = null;	
            }

            private Node(Object dataPortion, Node nextNode) {
                    data = dataPortion;
                    next = nextNode;	
            }

            private Object getData() {
                    return data;
            }

            private void setData(Object newData) {
                    data = newData;
            }

            private Node getNextNode() {
                    return next;
            }

            private void setNextNode(Node nextNode) {
                    next = nextNode;
            }
    }
    //********************************************************************
    //********************************************************************
}
//************************************************************************
//************************************************************************

interface QueueInterface {
	public void enqueue(Object newEntry);
	public Object dequeue();
	public Object getFront();
	public boolean isEmpty();
	public void clear();
}
//**************************************************************************************************************************
//**************************************************************************************************************************
