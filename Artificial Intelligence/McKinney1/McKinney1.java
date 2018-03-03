//Program:	McKinney1.java
//Course:	COSC470
//Description:	This program automatically finds the shortest way out of a two-dimensional 
//              M row by N column maze from any specified starting point within the maze
//              using a depth-first search algorith. The maze is represented as an array 
//              of 1's and 0's; 1's being walls and 0's being paths.
//Author:	Joshua McKinney
//Revised:	2/8/2016
//Language:	Java
//IDE:		NetBeans 8.0.2
//Notes:        None.
//*******************************************************************************
//*******************************************************************************

import java.io.*;

public class McKinney1 {
    
//Class:        McKinney1
//Description:  Main class
//Globals:      MazeCoordinate[][]  shortestMaze    the resting place for the  maze with current shortest path
//              int                 cellsVisited
//              int                 shortestPathLength
//              boolean             continueProgram
//              boolean             showSteps
//              boolean             autoStepThrough
    
     static MazeCoordinate[][] shortestMaze = null;
     static int shortestPathLength = Integer.MAX_VALUE;
     static int cellsVisited = 1;
     static boolean continueProgram = true;
     static boolean showSteps = true;
     static boolean autoStepThrough = true;
     static char mazeRunner = 'X';
     static char wallChar = '█';
     static char pathChar = ' ';
     static char traversedPath = '*';     
     
 public static void main(String[] args) {
     
    //Method:       main
    //Description:  main method that runs the program and sets up options and formating
    //Parameters:   String[] args           (default main method parameters)
    //Returns:      void
    //Calls:        TextFileClass
    //              Integer.parseInt
    //              PopulateMaze
    //              KeyboardInputClass
    //              MazeCoordinate
    //              LookForPaths
    //              PrintMaze
    //Globals:      MazeCoordinate[][]      shortestMaze
    //              int                     cellsVisited
    //              int                     shortestPathLength
    //              boolean                 continueProgram
    //              boolean                 showSteps
    //              boolean                 autoStepThrough
    //              char                    wallChar
    //              char                    pathChar
    
    int numOfRows;
    int numOfColumns;
    int startingRow;
    int startingColumn;
    int pathLength = 1;
    while (continueProgram == true){
        TextFileClass textFile = new TextFileClass();
        textFile.getFileName("Specify the maze text file to be read in:");
        textFile.getFileContents();
        String[] fileContents = textFile.text;
        
        numOfRows = Integer.parseInt(fileContents[0]);
        numOfColumns = Integer.parseInt(fileContents[1]);
        startingRow = Integer.parseInt(fileContents[2]);
        startingColumn = Integer.parseInt(fileContents[3]);
        MazeCoordinate [][] maze = PopulateMaze(numOfRows, numOfColumns, fileContents, wallChar, pathChar);
        MazeCoordinate currentCell = maze[startingRow][startingColumn];
        MazeCoordinate parentCell = null;
        PrintMaze(maze);
        
        KeyboardInputClass keyboardInput = new KeyboardInputClass();
        char userInput=keyboardInput.getCharacter(false, 'Y', null, 1, "Show all steps? Y/N (Default \'Y\'):");
        if(userInput == 'N') showSteps = false;
        userInput = keyboardInput.getCharacter(false, 'N', null, 1, "Pause after each step? Y/N (Default \'N\'):");
        if (userInput == 'Y') autoStepThrough = false;
        
        shortestMaze = TraverseMaze(pathLength, maze, currentCell, parentCell);
        if(shortestMaze != null){
            System.out.println("Maze with shortest path to exit:");
            PrintMaze(shortestMaze);
            System.out.println(cellsVisited + " cells visited.");
            System.out.println("The shortest Path is " + shortestPathLength + " steps.");
        }//this only runs as long as an exit was found out of the maze.
        else System.out.println("There were no available paths out of the maze.");
        userInput = keyboardInput.getCharacter(false, 'N', null, 1, "Run program again? Y/N (Default \'N\'):");
        if(userInput == 'N') continueProgram = false;
        else{shortestMaze = null;
        shortestPathLength = Integer.MAX_VALUE;
        cellsVisited = 1;}//reset maze, shortest path length and cellsVisited
    }//End of continueProgram while loop        
}// End of main method
 //******************************************************************************
 
 public static void PrintMaze(MazeCoordinate[][] maze){
     
     //Method:      PrintMaze
     //Description: Prints the maze[][] passed in
     //Parameters:  MazeCoordinate[][]      maze                        the maze to be printed
     //Returns:     void
     //Calls:       Nothing
     //Globals:     boolean             autoStepThrough
     
     for (int i = 0; i < maze.length; i++) {
         System.out.println("");
         for (int j = 0; j < maze[i].length; j++) {
             System.out.print(maze[i][j].symbol);             
         }//End of inner PrintMaze for loop 
     }//End of outer PrintMaze for loop
     System.out.println("");
 }//End of PrintMaze method
 //******************************************************************************

 public static MazeCoordinate[][] CopyMaze(MazeCoordinate[][] oldMaze){
     
     //Method:          CopyMaze
     //Description:     Makes a deep copy of the maze passed in and returns the copy
     //Parameters:      MazeCoordinate[][]      oldMaze                 the maze to be copied
     //Returns:         MazeCoordinate[][]      newMaze                 the copied maze
     //Calls:           Nothing
     //Globals:         None
     
    int rows = oldMaze.length;
    int columns = oldMaze[0].length;
    MazeCoordinate[][] newMaze = new MazeCoordinate[rows][columns];
    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < columns; j++) {
            MazeCoordinate newCell = new MazeCoordinate(i, j, oldMaze[i][j].symbol);
            newMaze[i][j] = newCell;
        }//End inner for loop
    }//End outer for loop
    return newMaze;
 }//End CopyMaze method
 //******************************************************************************
 
 public static MazeCoordinate[][] PopulateMaze(int numOfRows, int numOfColmns, String[] fileContentArray, char wallChar, char pathChar){
    
     //Method:          PopulateMaze
     //Description:     Generates a 2 dementional array of type MazeCoordinate with
     //                 each cell of the maze containing either a wall or path character
     //Parameters:      int                     numOfRows               
     //                 int                     numOfColumns            
     //                 String[]                fileContentsArray[]     array containing the rows of the maze to be generated
     //                 char                    wallChar                
     //                 char                    pathChar                
     //Returns:         MazeCoordinate[][]      maze                    the generated maze
     //Calls:           MazeCoordinate          the object for each cell of the maze
     //Globals:         None
     
     MazeCoordinate[][] maze = new MazeCoordinate [numOfRows][numOfColmns];
     for (int i = 0; i < numOfRows; i++) {              
         for (int j = 0; j < numOfColmns; j++) {
             if (fileContentArray[i+4].charAt(j) == '0')
             maze[i][j] = new MazeCoordinate(i, j, pathChar);
             else if (fileContentArray[i+4].charAt(j) == '1')
             maze[i][j] = new MazeCoordinate(i, j, wallChar);
             else System.out.println("A character within the text file was something other than a 1 or 0.");
         }//End of inner for loop         
     }//End of outer for loop
 return maze;
 }//End of PopulateMaze method
 //******************************************************************************
 
 public static MazeCoordinate[][] TraverseMaze(int pathLength, MazeCoordinate[][] maze, MazeCoordinate currentCell, MazeCoordinate parentCell){
    
     //Method:          TraverseMaze
     //Description:     traverses the maze looking in all possible (N, S, E, W) directions for
     //                 exits to the maze. Once an exit is found it is checked against the current
     //                 shortest maze path to an exit, and if it is shorter, it is stored out as the
     //                 new current shortest path out of the maze.
     //Parameters:      int                     pathLength
     //                 MazeCoordinate[][]      maze
     //                 MazeCoordinate          currentCell
     //                 MazeCoordinate          parentCell
     //Returns:         MazeCoordinate[][]      shortestMaze
     //Calls:           PrintMaze
     //                 CopyMaze
     //                 TraverseMaze            calls itself
     //Globals:         char                    mazeRunner
     //                 int                     shortestPathLength
     //                 MazeCoordinate[][]      shortestMaze
     //                 char                    pathChar
     //                 char                    traversedPath
    cellsVisited++;
    int row = currentCell.row;
    int column = currentCell.column;
    currentCell.symbol = mazeRunner;
    
     if(!autoStepThrough){
         KeyboardInputClass keyboardInput = new KeyboardInputClass();
         keyboardInput.getString(null, "Press ENTER to continue:");
     }//End manual step through If Statement
     
    if(showSteps){
        PrintMaze(maze);}
    if(row == 0 || column == 0 || row == maze.length-1 || column == maze[row].length-1){
        if (pathLength < shortestPathLength){
            shortestPathLength = pathLength;
            shortestMaze = CopyMaze(maze);
            }//End 'found shortest path to exit' If Statement
        currentCell.symbol = pathChar;
    if(parentCell != null) parentCell.symbol = mazeRunner;
        }//End 'found exit' If Statement
    else{
        pathLength++;
    if (maze[row][column-1].symbol == pathChar){
        maze[row][column].symbol = traversedPath;
        TraverseMaze(pathLength, maze, maze[row][column-1], currentCell);}//Add West child if possible
    if (maze[row+1][column].symbol == pathChar){
        maze[row][column].symbol = traversedPath;
        TraverseMaze(pathLength, maze, maze[row+1][column], currentCell);}//Add South child if possible
    if (maze[row][column+1].symbol == pathChar){
        maze[row][column].symbol = traversedPath;
        TraverseMaze(pathLength, maze, maze[row][column+1], currentCell);}//Add East child if possible
    if (maze[row-1][column].symbol == pathChar){
        maze[row][column].symbol = traversedPath;
        TraverseMaze(pathLength, maze, maze[row-1][column], currentCell);}//Add North child if possible
    currentCell.symbol = pathChar;
    if(parentCell != null) parentCell.symbol = mazeRunner;
        }
    pathLength--;
    if (showSteps){
        PrintMaze(maze);
    }      //If maze is still unsolved
    
 return shortestMaze;
 }//End of LookForPaths method
//*******************************************************************************
 
}//End of main class
//*******************************************************************************
//*******************************************************************************

class MazeCoordinate{
    
    //Class:            MazeCoordinate
    //Description:      this class is a custom object data type to help keep up with each
    //                  cell of the maze.
    //Globals:          int                     row                     
    //                  int                     column                  
    //                  char                    symbol                  the symbol to be printed for the cell
    
    int row;
    int column;
    char symbol;
    
    public MazeCoordinate(int y, int x, char printedChar){
        
        //Method:           MazeCoordinate
        //Description:      this is the constructor method of the MazeCoordinate class
        //Parameters:       int                 y
        //                  int                 x
        //                  char                printedChar
        //Returns:          Nothing
        //Calls:            Nothing
        //Globals:          int                 row
        //                  int                 column
        //                  char                symbol
        
        row = y;
        column = x;
        symbol = printedChar;
    }//End of MazeCoordinate constructor
    //***************************************************************************
    
}//End of MazeCoordinates node class
//*******************************************************************************
//*******************************************************************************


//**************************************************************************************************************************
//**************************************************************************************************************************
//Class:		KeyboardInputClass
//Description:	Provides multiple methods for entering information from the keyboard for console based programs.
//Author:		Steve Donaldson
//Revised:		August 6, 2013
class KeyboardInputClass {
	//**********************************************************************************************************************
	//Method:		getKeyboardInput
	//Description:	Permits keyboard input for strings
	//Parameters:	prompt - descriptive text telling the user what to enter
	//Returns:		inputString	- the entered text (i.e., the user's response). Note that even though this is a string,
	//								it can be converted to an integer, double, etc. if necessary in the client routine.
	//Throws:		Exception (but doesn't do anything with it!)
	//Calls:		nothing
	public String getKeyboardInput(String prompt) 
	{
		String inputString="";
		System.out.println(prompt);
		try {
			InputStreamReader reader=new InputStreamReader(System.in);
			BufferedReader buffer=new BufferedReader(reader);
			inputString=buffer.readLine();
		}
		catch (Exception e) {}
		return inputString;
	}
	//**********************************************************************************************************************
	//Method:		getCharacter
	//Description:	Gets a character (char) from the keyboard. If validateInput=true, the routine loops until the user entry
	//				matches defaultResult (which may be obtained just by pressing the ENTER key without entering anything)
	//				or is one of the validEntries.
	//Parameters:	validateInput		- true=make sure the entered character is in validEntries; false=accept any
	//										character that is entered
	//				defaultResult		- character to be returned if the user enters no character (i.e., just presses
	//										ENTER). If validateInput=true, the method assumes that this is a valid entry
	//										even if it is not explicitly included in validEntries (i.e., the method will
	//										add it to validEntries).
	//				validEntries		- acceptable characters if validateInput = true. Note: if validation is to be
	//										performed, then unless one of the case conversion modes is specified, the user
	//										entry must match one of the validEntries characters exactly in order to be
	//										accepted.
	//				caseConversionMode	- 0=no conversion occurs; 1=the entered character is converted to uppercase before
	//										being checked against validEntries and before being returned; 2= the entered
	//										character is converted to lowercase before being checked against validEntries
	//										and before being returned. Note: both case conversion modes 1 and 2 also convert
	//										validEntries to the specified case prior to checking the validity of the entry.
	//										If validateInput=false, this parameter is ignored.
	//				prompt				- descriptive text prompting the user for an entry
	//Returns:		result				- the character entered by the user or defaultResult if no character was entered
	//Calls:		getKeyboardInput
	public char getCharacter(boolean validateInput, char defaultResult, String validEntries, int caseConversionMode, String prompt) {
		if (validateInput) {
			if (caseConversionMode == 1) {
				validEntries = validEntries.toUpperCase();
				defaultResult = Character.toUpperCase(defaultResult);
			}
			else if (caseConversionMode == 2) {
				validEntries = validEntries.toLowerCase();
				defaultResult = Character.toLowerCase(defaultResult);
			}
			if ((validEntries.indexOf(defaultResult) < 0))								//if default not in validEntries
				validEntries = (new Character(defaultResult)).toString() + validEntries;//then add it
		}
		String inputString="";
		char result = defaultResult;
		boolean entryAccepted = false;
		while (!entryAccepted) {
			result = defaultResult;
			entryAccepted = true;
			inputString = getKeyboardInput(prompt);
			if (inputString.length() > 0) {
				result = (inputString.charAt(0));
				if (caseConversionMode == 1)
					result = Character.toUpperCase(result);
				else if (caseConversionMode == 2)
					result = Character.toLowerCase(result);
			}
			if (validateInput)
				if (validEntries.indexOf(result) < 0) {
					entryAccepted = false;
					System.out.println("Invalid entry. Select an entry from the characters shown in brackets: [" + validEntries + "]");
				}
		}
		return result;
	}
	//**********************************************************************************************************************
	//Method:		getInteger
	//Description:	Gets an integer (int) from the keyboard. If validateInput=true, the routine loops until the user entry
	//				matches defaultResult (which may be obtained just by pressing the ENTER key without entering anything)
	//				or falls within the range specified by minAllowableResult and maxAllowableResult.
	//Parameters:	validateInput		- true=make sure the entered integer equals the default or is in in the allowable
	//										range specified by minAllowableResult and maxAllowableResult; false=accept any
	//										integer that is entered
	//				defaultResult		- integer to be returned if the user enters nothing (i.e., just presses (ENTER).
	//										If validateInput=true, the method assumes that this is a valid entry
	//										even if it is not explicitly included in the specified range.
	//				minAllowableResult	- the minimum allowable value for the user entry (if validateEntries=true)
	//				maxAllowableResult	- the maximum allowable value for the user entry (if validateEntries=true)
	//										Note: if validateInput=false,these values are ignored
	//				prompt				- descriptive text prompting the user for an entry
	//Returns:		result				- the integer entered by the user or defaultResult if no integer was entered
	//Calls:		getKeyboardInput
	public int getInteger(boolean validateInput, int defaultResult, int minAllowableResult, int maxAllowableResult, String prompt) {
		String inputString = "";
		int result = defaultResult;
		boolean entryAccepted = false;
		while (!entryAccepted) {
			result = defaultResult;
			entryAccepted = true;
			inputString = getKeyboardInput(prompt);
			if (inputString.length() > 0) {
				try {
					result = Integer.parseInt(inputString);
				}
				catch (Exception e) {
					entryAccepted = false;
					System.out.println("Invalid entry...");
				}
			}
			if (entryAccepted && validateInput) {
				if ((result != defaultResult) && ((result < minAllowableResult) || (result > maxAllowableResult))) {
					entryAccepted = false;
					System.out.println("Invalid entry. Allowable range is " + minAllowableResult + "..." + maxAllowableResult + " (default = " + defaultResult + ").");
				}
			}
		}
		return result;
	}
	//**********************************************************************************************************************
	//Method:		getLong
	//Description:	Gets a long integer (long) from the keyboard. If validateInput=true, the routine loops until the user entry
	//				matches defaultResult (which may be obtained just by pressing the ENTER key without entering anything)
	//				or falls within the range specified by minAllowableResult and maxAllowableResult.
	//Parameters:	validateInput		- true=make sure the entered long integer equals the default or is in in the allowable
	//										range specified by minAllowableResult and maxAllowableResult; false=accept any
	//										integer that is entered
	//				defaultResult		- long integer to be returned if the user enters nothing (i.e., just presses (ENTER).
	//										If validateInput=true, the method assumes that this is a valid entry
	//										even if it is not explicitly included in the specified range.
	//				minAllowableResult	- the minimum allowable value for the user entry (if validateEntries=true)
	//				maxAllowableResult	- the maximum allowable value for the user entry (if validateEntries=true)
	//										Note: if validateInput=false,these values are ignored
	//				prompt				- descriptive text prompting the user for an entry
	//Returns:		result				- the long integer entered by the user or defaultResult if nothing was entered
	//Calls:		getKeyboardInput
	public long getLong(boolean validateInput, long defaultResult, long minAllowableResult, long maxAllowableResult, String prompt) {
		String inputString = "";
		long result = defaultResult;
		boolean entryAccepted = false;
		while (!entryAccepted) {
			result = defaultResult;
			entryAccepted = true;
			inputString = getKeyboardInput(prompt);
			if (inputString.length() > 0) {
				try {
					result = Long.parseLong(inputString);
				}
				catch (Exception e) {
					entryAccepted = false;
					System.out.println("Invalid entry...");
				}
			}
			if (entryAccepted && validateInput) {
				if ((result != defaultResult) && ((result < minAllowableResult) || (result > maxAllowableResult))) {
					entryAccepted = false;
					System.out.println("Invalid entry. Allowable range is " + minAllowableResult + "..." + maxAllowableResult + " (default = " + defaultResult + ").");
				}
			}
		}
		return result;
	}
	//**********************************************************************************************************************
	//Method:		getDouble
	//Description:	Gets a double from the keyboard. If validateInput=true, the routine loops until the user entry
	//				matches defaultResult (which may be obtained just by pressing the ENTER key without entering anything)
	//				or falls within the range specified by minAllowableResult and maxAllowableResult.
	//Parameters:	validateInput		- true=make sure the entered double equals the default or is in in the allowable
	//										range specified by minAllowableResult and maxAllowableResult; false=accept any
	//										double that is entered
	//				defaultResult		- double to be returned if the user enters nothing (i.e., just presses (ENTER).
	//										If validateInput=true, the method assumes that this is a valid entry
	//										even if it is not explicitly included in the specified range.
	//				minAllowableResult	- the minimum allowable value for the user entry (if validateEntries=true)
	//				maxAllowableResult	- the maximum allowable value for the user entry (if validateEntries=true)
	//										Note: if validateInput=false,these values are ignored
	//				prompt				- descriptive text prompting the user for an entry
	//Returns:		result				- the double entered by the user or defaultResult if no double was entered
	//Calls:		getKeyboardInput
	public double getDouble(boolean validateInput, double defaultResult, double minAllowableResult, double maxAllowableResult, String prompt) {
		String inputString = "";
		double result = defaultResult;
		boolean entryAccepted = false;
		while (!entryAccepted) {
			result = defaultResult;
			entryAccepted = true;
			inputString = getKeyboardInput(prompt);
			if (inputString.length() > 0) {
				try {
					result = Double.parseDouble(inputString);
				}
				catch (Exception e) {
					entryAccepted = false;
					System.out.println("Invalid entry...");
				}
			}
			if (entryAccepted && validateInput) {
				if ((result != defaultResult) && ((result < minAllowableResult) || (result > maxAllowableResult))) {
					entryAccepted = false;
					System.out.println("Invalid entry. Allowable range is " + minAllowableResult + "..." + maxAllowableResult + " (default = " + defaultResult + ").");
				}
			}
		}
		return result;
	}
	//**********************************************************************************************************************
	//Method:		getString
	//Description:	Gets a string of alphanumeric text from the keyboard. If the ENTER key is pressed without anything else
	//				being entered, returns defaultResult.
	//Parameters:	defaultResult	- the string to be returned if the user enters nothing (i.e., just presses (ENTER).
	//				prompt			- descriptive text prompting the user for an entry
	//Returns:		result			- the string entered by the user or defaultResult if no string was entered
	//Calls:		getKeyboardInput
	public String getString(String defaultResult, String prompt) {
		String result = getKeyboardInput(prompt);
		if (result.length() == 0)
			result = defaultResult;
		return result;
	}
	//**********************************************************************************************************************
}
//**************************************************************************************************************************
//**************************************************************************************************************************

//Here is how to use it... (remove the comments!)
//KeyboardInputClass keyboardInput = new KeyboardInputClass();
//String userInput="";
//userInput=keyboardInput.getKeyboardInput("Specify the string to be processed");

//***************************************************************************
//Class:		TextFileClass
//Description:	Provides for reading the contents of a text file
//Uses:			KeyboardInputClass
//Author:       Steve Donaldson
//Revised:      1/23/2014

class TextFileClass {
	String fileName;
	static final int MAX_TEXT_ROWS = 2000;	//if the first constructor is used this is the maximum number of rows of text
											//that can be stored in the text[] array
	String text[];							//the text read from the file (one line per row)
	int lineCount;							//the actual number of rows of text in the text[] array
	KeyboardInputClass keyboardInput = new KeyboardInputClass();
	//************************************************************************
	//Method:		TextFileClass
	//Description:	Constructor - used when it is known that the max rows of text is < MAX_TEXT_ROWS
	//Parameters:	none
	//Returns:		nothing
	//Throws		nothing
	//Calls:		nothing
	TextFileClass() {
		text = new String[MAX_TEXT_ROWS];
		lineCount = 0;
	}
	//************************************************************************
	//Method:		TextFileClass
	//Description:	Constructor - used when a specific # of rows of text are to be read or when the # of rows is unknown but
    //                              is likely larger than MAX_TEXT_ROWS. Will result in two passes on the file in order to get
    //                              the text (one to count the # of lines and allocate space in the array and one to load it).
	//Parameters:	maxTextRows - maximum number of rows that can be read from the file and stored in the text[] array. If a
	//				value of zero is passed, space for the array is not assigned until the getFileContents method is called.
	//Returns:		nothing
	//Throws		nothing
	//Calls:		nothing
	TextFileClass(int maxTextRows) {
		if (maxTextRows > 0)
			text = new String[maxTextRows];
		lineCount = 0;
	}
	//************************************************************************
	//Method:		getFileName
	//Description:	Gets the name of a text file from the user
	//Parameters:	prompt - descriptive text telling the user what to enter
	//Returns:		nothing
	//Throws		nothing
	//Calls:		getKeyboardInput from class KeyboardInputClass
	public void getFileName(String prompt) {
		fileName=keyboardInput.getKeyboardInput(prompt);
		return;
	}
	//************************************************************************
	//Method:		getFileContents
	//Description:	Reads the contents of a specified text file. If space has not yet been allocated for the text[] array,
	//				this routine makes two passes on the file--one to determine how many rows of text it contains (so space
	//				for the text[] array can be allocated) and the second to actually load the array.
	//Parameters:	none
	//Returns:		lineCount - the number of lines read. A value of 0 can be interpreted to mean that nothing was read,
	//				perhaps due to a problem with the file contents, inability to locate it, etc.
	//Throws		Exception (and displays a generic error message)
	//Calls:		no user defined methods
	public int getFileContents() {
		String s;
        int maxRows = MAX_TEXT_ROWS;
		int passStart = 2;
		if (text == null) {
			passStart = 1;
            maxRows = 50000000;
        }
		for (int pass = passStart; pass <= 2; pass++) {
			lineCount = 0;
			try {
				FileReader reader = new FileReader(fileName);
				BufferedReader buffer = new BufferedReader(reader);
				s = buffer.readLine();
				while ((s != null) && (lineCount < maxRows)) {
					if (pass == 2)
						text[lineCount] = s;
					lineCount++;
					s = buffer.readLine();
				}
				reader.close();
			}
			catch (Exception e) {
				keyboardInput.getKeyboardInput("Problem trying to access or read file. Press ENTER to continue...");
				pass = 3;
			}
			if(pass==1)
				text = new String[lineCount];
		}
		return lineCount;
	}
	//************************************************************************
}
//***************************************************************************
//***************************************************************************
//Here is how to begin using it:
//TextFileClass textFile = new TextFileClass();
//textFile.getFileName("Specify the text file to be read");
//if (textFile.fileName.length()>0) {/*do something here!*/}