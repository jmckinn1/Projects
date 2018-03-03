//Program:	McKinney1.java
//Course:	COSC470
//Description:	Treasure of various amounts is stored at multiple locations throughout
//              a landscape. Any two locations may be connected (or not) by a one-way
//              passage of a certain length. The objective is to move from a specified
//              starting location to a specified ending location along allowable passages,
//              maximizing the overall treasure collected while minimizing the distance
//              traveled. Locations cannot be repeated. The overall performance score is
//              determined by using a weighted calculation involving both distance and
//              treasure components, where the weights are specified by the user. The
//              calculation for the score along a particular path is score = treasure
//              amount * treasure weight factor - distance * distance weight factor.
//Author:	Joshua David McKinney
//Revised:	1/24/2017
//Language:	Java
//IDE:		NetBeans 8.0.2
//Notes:        Utilizes TextFileClass.java (Dr. Steve Donaldson) and KeyboardInputClass.java (Dr. Steve Donaldson)
//
//*******************************************************************************
//*******************************************************************************

//Class:        McKinney1
//Discription:  main class
//Globals:      treasureMap
//              allLocations
//              bestPath
//              bestScore
//              numOfLocations
//              startingLocationNum
//              endingLocationNum
//              treasureImportance
//              distanceImportance
public class McKinney1 {

    //********************************************************************************
    static double[][] treasureMap;
    static Location[] allLocations;
    static Location[] bestPath;
    static double bestScore;
    static int numOfLocations;
    static int startingLocationNum;
    static int endingLocationNum;
    static double treasureImportance;
    static double distanceImportance;
    static boolean tsp = false;

    //*******************************************************************************
    //Method:       main()
    //Discription:  sets program up and reads in data from text file
    //Returns:      void
    //Calls:        KeyboardInputClass
    //              TextFileClass
    //              Location
    //              ExplorePath();
    //Globals:      treasureMap
    //              allLocations
    //              bestPath
    //              bestScore
    //              numOfLocations
    //              startingLocationNum
    //              endingLocationNum
    //              treasureImportance
    //              distanceImportance
    public static void main(String[] args) {

        boolean continueProgram = true;
        int optionInput;
        System.out.println("Treasure Hunter: McKinney\n");

        while (continueProgram) {

            TextFileClass textFile = new TextFileClass();
            textFile.getFileName("Specify the text file to be read in:");
            textFile.getFileContents();
            String[] fileContents = textFile.text;
            int fileLength = fileContents.length;
            String[] fileContentsArray = new String[fileLength];
            for (int i = 0; i < fileLength; i++) {
                fileContentsArray[i] = fileContents[i];
            }//End of text file read-in For Loop
            numOfLocations = Integer.parseInt(fileContentsArray[0]);
            startingLocationNum = Integer.parseInt(fileContentsArray[1]);
            endingLocationNum = Integer.parseInt(fileContentsArray[2]);
            distanceImportance = Double.parseDouble(fileContentsArray[3]);
            treasureImportance = Double.parseDouble(fileContentsArray[4]);

            //populate treasureMap
            treasureMap = new double[numOfLocations][numOfLocations];
            allLocations = new Location[numOfLocations];
            bestPath = new Location[numOfLocations + 1];
            for (int i = 0; i < numOfLocations; i++) {
                String fileContentsRowOfText = fileContentsArray[5 + i];
                Location location = new Location();
                location.DiscoverLocation(i);
                for (int j = 0; j < numOfLocations; j++) {
                    treasureMap[i][j] = Double.parseDouble(fileContentsRowOfText.split(",")[j]);
                }//End of available path parsing
                location.RevealTreasure(Double.parseDouble(fileContentsArray[5 + numOfLocations + i]));
                allLocations[i] = location;
            }//End of populateTreasureMap for loop
            
            System.out.println("");
            
            //explore starting location
            Location start = allLocations[startingLocationNum];
            ExplorePath(start, 0);
            if(bestPath[0] == null)
                System.out.println("No paths found were solutions.");
            else{
                System.out.println("Best Score: " + bestScore);
                System.out.print("Path Taken: " + bestPath[0].name);
                for (int i = 1; i < bestPath.length; i++) {
                    if (bestPath[i] != null) {
                        System.out.print("-" + bestPath[i].name);
                    }
                }
                System.out.println("");
            }//End display final path
            //prompt user to play again
            KeyboardInputClass keyboardInput = new KeyboardInputClass();
            char userInput = keyboardInput.getCharacter(false, 'Y', null, 1, "Play again? Y/N (Default \'Y\'):");
            if (userInput == 'N') {
                continueProgram = false;
            }
            bestScore = 0;
        }//End of program loop
    }//End of main method
//***************************************************************************************

//Method:       ExploreLocation()
//Description:  This method uses a depth-first search using recursion to traverse all possible paths
//              and calls other methods to check for GOAL states.
//Returns:      void
//Calls:        GoalState();
//              ExplorePath();
//Globals:      treasureMap
//              allLocations
    public static void ExplorePath(Location currentLocation, double distanceTraveled) {
        if (GoalState(currentLocation)) {
        }//End if current_state is goal state
        else {
            currentLocation.visited = true;//add current_state to closed

            for (int i = 0; i < treasureMap.length; i++) {
                Location next = allLocations[i];
                if ((!next.visited) && (treasureMap[currentLocation.name][i] != 0)) {
                    //if child is not a member of closed or start/end of tsp
                    currentLocation.child = next;
                    ExplorePath(next, treasureMap[currentLocation.name][i]);
                }//then depthsearch(child)
            }//End "while" current_state has unexamined children
            currentLocation.visited = false;
        }
    }//End of ExplorePath
//***********************************************************************************

//Method:       GoalState()
//DescriptionL  Checks to see if current node is ending state and if the path taken
//              was a valid path. If node is GOAL state, the score is calculated and
//              the path taken is recorded.
//Returns:      boolean
//Calls:        CalculateScore();
//              CopyPathTaken();
//Globals:      bestScore
//              endingLocationNum
    public static boolean GoalState(Location current) {
        if (current.name == endingLocationNum) {
            boolean betterScore = CalculateScore();
            if (betterScore) {
                //CopyPathTaken();
            }//End of reset best score/path
            return true;
        }//End Treasure Hunt GOAL condition
        return false;
    }//End GoalState method
//**************************************************************************************

//Method:       CalculateScore()
//Description:  Calculates the total distance traveled and the total amount of treasure
//              aquired and displays them, then weighs them according to thier specified
//              importance and returns the difference in the values of weighted treasure
//              and weighted distance traveled.
//Returns:      currentScore
//Calls:        Nothing.
//Globals:      allLocations
//              startingLocationNum
//              treasureMap
//              treasureImportance
//              distanceImportance
    public static boolean CalculateScore() {

        Location current = allLocations[startingLocationNum];
        double distance = 0;
        double currentScore = 0;
        double treasure = 0;
        for (int i = 1; i < treasureMap.length; i++) {
            if (current.child == null) {
                i = treasureMap.length;
            } else {
                distance += (treasureMap[current.name][current.child.name]);
                current = current.child;
                treasure += (current.treasure);
            }
        }
        currentScore = (treasure * treasureImportance) - (distance * distanceImportance);
        if(currentScore >= bestScore){
            System.out.println("New best score found!");
            CopyPathTaken();
            System.out.println("Treasure gathered: " + treasure);
            System.out.println("Distance traveeled: " + distance);
            System.out.println("Score: " + currentScore + " = " +
                    treasure + " * " + treasureImportance + " - " +
                    distance + " * " + distanceImportance);
            System.out.println("");
            bestScore = currentScore;
            return true;
        }//End if new best score
        return false;
    }//End of CalculateScore method
//****************************************************************************************

//Method:       CopyPathTaken()
//Description:  Starts at the starting node and follows the children of that node
//              to the current node, recording all node along the path into bestPath
//Returns:      void
//Calls:        Nothing.
//Globals:      allLocations
//              startingLocationNum
//              bestPath
    public static void CopyPathTaken() {
        Location current = allLocations[startingLocationNum];
        System.out.print("Path: " + current.name);
        bestPath[0] = current;
        current = current.child;
        for (int i = 1; i < bestPath.length; i++) {
            if (current == null) {
                bestPath[i] = null;
            } else {
                System.out.print("-" + current.name);
                bestPath[i] = current;
                current = current.child;
            }
        }//End populate bestPath for loop
        System.out.println("");
    }//End CopyPathTaken method
//**************************************************************************************

}//End of class
//***************************************************************************************
//***************************************************************************************
//Class:        Location
//Description:  Location node object class for Treasure Hunt
//Globals:      visited
//              name
//              treasure
//              child

class Location {
    //************************************************************************************
    
    boolean     visited;
    int         name;
    double      treasure;
    Location    child;
    
    //Method:       DiscoverLocation{}
    //Description:  acts as somewhat of a constructor method setting up name and visited variables
    //Returns:      void
    //Calls:        nothing
    //Globals:      name
    //              visited
    
public void DiscoverLocation(int location){
    name = location;
    visited = false;
}//End DiscoverLocation method
//*******************************************************************************************

//Method:       RevealTreasure()
//Description:  sets the value of the treasure for this location node
//Returns:      void
//Calls:        nothing
//Globals:      treasure

public void RevealTreasure(double value){
    treasure = value;
}//End RevealTreasure method
//*******************************************************************************************

}//End of class
//*******************************************************************************************
//*******************************************************************************************