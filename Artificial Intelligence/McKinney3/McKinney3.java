package McKinney3;

//Program:	McKinney3.java
//Course:	COSC470
//Description:	This program prompts the user for a file containing a grey-scale image,
//              then filters out all regions of the image that are not squares, rectangles
//              or circles. Then it color codes those shapes and displays them over top
//              the original image.
//Author:	Joshua McKinney
//Revised:	3/30/2016
//Language:	Java
//IDE:		NetBeans 8.0.2
//Notes:        DisplayImage() authored by Steve Donaldson.
//              EasyImageDisplay slightly modified to keep backgroud from being overwritten.
//              ^^ included at the bottom of class

//*******************************************************************************
//*******************************************************************************

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.RandomAccessFile;
import java.util.ArrayList;

//Class:        McKinney3
//Description:  Main class
//Globals:      int colorTolerance          tolerance of color value between pixels in a region
//              int minShapeSize            minimum size of regions to be checked
//              int misshapeTolerance       tolerance of pixels out of place that determine shapes
//              int numOfSquares            number of squares found in the image
//              int numOfRect               number of rectangles found in the i
//              int numOfCircles            number of circles found in the imagemage
//              int imageWidth
//              int imageHeight
//              int[][] squares             array containing positions of all squares
//              int[][] rectangles          array containing positions of all rectangles
//              int[][] circles             array containing positions of all circles

public class McKinney3 {
    
    
    static int colorTolerance = 0;
    static int minShapeSize = 30;
    static int misshapeTolerance = 6;
    static int numOfSquares = 0, numOfRect = 0, numOfCircles = 0;
    static int imageWidth, imageHeight;
    static int[][] squares;
    static int[][] rectangles;
    static int[][] circles;
    
    //***************************************************************************
    
    //Method:       main
    //Description:  main method of the program
    //Parameters:   String[] args       (default main method parameters)
    //Returns:      void
    //Calls:        EasyImageDisplay
    //              KeyboardInputClass
    //              ArrayList
    //              Regions
    //              PopulateListOfRegions
    //              SingleRegion
    //              CopyShapeIntoArray
    //Globals:      int minShapeSize
    //              int misshapeTolerance
    //              int imageWidth
    //              int imageHeight
    //              int colorTolerance
    //              int[][] squares
    //              int[][] rectangles
    //              int[][] circles
    //              int numOfSquares
    //              int numOfRect
    //              int numOfCircles
    
 public static void main(String[] args) {
     EasyImageDisplay imageObject = DisplayImage();
     
     KeyboardInputClass keyboardInput = new KeyboardInputClass();
     minShapeSize = keyboardInput.getInteger(false, minShapeSize, 0, 0, 
             "Enter minimum pixel count for shapes to be found (default " + minShapeSize+ "):");
     misshapeTolerance = keyboardInput.getInteger(false, misshapeTolerance, 0, 0,
             "Enter max tolerance for pixel misplacement (Default " + misshapeTolerance + "):");
     
     System.out.println("Working...");
     imageWidth = imageObject.imageWidth;
     imageHeight = imageObject.imageHeight;
     ArrayList listOfRegions = new ArrayList();
     ArrayList listOfRegionLabels = new ArrayList();
     Regions regionObject = new Regions(2, imageWidth, imageHeight, imageObject.pixels, 4, true, colorTolerance);
     regionObject.findRegions();
     regionObject.computeRegionProperties();
     int[][] filteredArray = regionObject.filterRegions(minShapeSize, Integer.MAX_VALUE, false, 0);
     squares = new int[imageHeight][imageWidth];
     rectangles = new int[imageHeight][imageWidth];
     circles = new int[imageHeight][imageWidth];
     
     
     PopulateListOfRegions(listOfRegions, listOfRegionLabels, filteredArray, regionObject);
     for (int i = 0; i < listOfRegions.size(); i++) {
         isCircle((SingleRegion) listOfRegions.get(i), misshapeTolerance);
         isQuadrilateral((SingleRegion) listOfRegions.get(i), misshapeTolerance);
     }//End of check filtered regions for shapes loop
     
     for (int i = 0; i < listOfRegions.size(); i++) {
         SingleRegion shape =(SingleRegion) listOfRegions.get(i);
         switch (shape.shape){
             case "square":
                 CopyShapeIntoArray(shape.regionInArray, squares);
                 numOfSquares++;
                 break;
             case "rectangle":
                 rectangles = CopyShapeIntoArray(shape.regionInArray, rectangles);
                 numOfRect++;
                 break;
             case "circle":
                 CopyShapeIntoArray(shape.regionInArray, circles);
                 numOfCircles++;
                 break;
         }//End of switch
     }//End of loop
     
     EasyImageDisplay newImage = new EasyImageDisplay(1, imageWidth, imageHeight, squares, rectangles, circles, imageObject.pixels);
     newImage.paint(imageObject.getGraphics());
     System.out.println("There are "+ numOfSquares + " squares, " + numOfRect + " rectangles, and " + numOfCircles + " circles.");
     keyboardInput.getString(null, "Press ENTER to close the image.");
     System.exit(0);
     
 }//End of main method
 //******************************************************************************
 
//Method:           CopyShapeIntoArray
//Discription:      integrates the pixel values of one array into another
//Parameters:       int[][] shapeIn
//                  int[][] arrayOut
//Returns:          int[][] arrayOut
//Calls:            Nothing
//Globals:          None
 
public static int[][] CopyShapeIntoArray(int[][] shapeIn, int[][] arrayOut){
     
    for (int i = 0; i < arrayOut.length; i++) {
        for (int j = 0; j < arrayOut[0].length; j++) {
            if(shapeIn[i][j] !=0 && arrayOut[i][j] ==0)
                arrayOut[i][j] = 255;
        }//End inner for loop
    }//End outer for loop
    return arrayOut;
     
}//End CopyShapeIntoArray method
//*******************************************************************************
 
//Method:           PopulateListOfRegions
//Discription:      populates a list of regions after they have been filtered out by size.
//Parameters:       ArrayList listOfRegions
//                  ArrayList listofLabels
//                  int[][] array
//                  Regions regionObject
//Returns:          void
//Calls:            Nothing
//Globals:          None

public static void PopulateListOfRegions(ArrayList listOfRegions, ArrayList listOfLabels, int[][] array, Regions regionObject){
    
    for (int i = 0; i < array.length; i++) {
        for (int j = 0; j < array[0].length; j++) {
            if(array[i][j] != 0){
                if(!listOfLabels.contains(array[i][j])){
                    int label = array[i][j];
                    int[][] singleRegionArray = regionObject.getSingleRegion(label);
                    SingleRegion shape = new SingleRegion(label, singleRegionArray, regionObject);
                    listOfRegions.add(shape);
                    listOfLabels.add(array[i][j]);
                }//End of inner if
            }//End of outer if
        }//End of inner for loop
    }//End of outer for loop
}//End of PopulateListOfRegions method
//*******************************************************************************

//Method:           isCircle
//Discription:      sets region.shape to circle if that region is a circle
//Parameters:       SingleRegion region
//                  int tolerance
//Returns:          void
//Calls:            Math
//Globals:          None

public static void isCircle(SingleRegion region, int tolerance){
    
    int startRow = 0, pixels = 0, radius = 0;
    int[][] array = region.regionInArray;
    boolean foundRegion = false;
    for (int row = 0; row < array.length; row++) {
        for (int column = 0; column < array[0].length; column++) {
            if(array[row][column] != 0){
                if (!foundRegion){
                    startRow=row;
                    foundRegion = true;
                }//End of inner if
                pixels++;
            }//End of outer if
        }//End of inner for loop
    }//End of outer for loop
    radius = region.centriodRow-startRow;
    if(Math.abs(Math.sqrt(pixels/Math.PI)-radius) <= tolerance)
        region.shape = "circle";
}//End of isCircle method
//*******************************************************************************

//Method:           isQuadrilateral
//Discription:      sets region.shape to rectangle or square if that region is a 
//                  retangle or square. if region.shape is still null, sets it to "blob"
//Parameters:       SingleRegion region
//                  int tolerance
//Returns:          void
//Calls:            Math
//Globals:          None

public static void isQuadrilateral(SingleRegion region, int tolerance){
    
    int height = 0, width = 0, pixels = 0;
    int[][] array = region.regionInArray;
    boolean isQuad = false;
    for (int row = 0; row < array.length; row++) {
        int widthCount = 0;
        for (int column = 0; column < array[0].length; column++) {
            if(array[row][column] != 0)
                widthCount++;
        }//End inner for loop
        if(widthCount != 0){
            pixels+=widthCount;
            height++;
            if(width == 0)
                width = widthCount;
            else if(Math.abs(width-widthCount) <= tolerance && (pixels/height)-width <= tolerance)
                isQuad = true;
            else isQuad = false;
        }//End of outer if
        else if (width != 0)        //break out of loop
            row=array.length+1;
    }//End of outer for loop
    if(isQuad)
        region.shape = "rectangle";
    if(isQuad && Math.abs(width-height) <= tolerance)
        region.shape = "square";
    if(region.shape == null)
        region.shape = "blob";
}//End of isQuadrilateral method
//*******************************************************************************

//Method:           DisplayImage
//Discription:      propmts the user for a file containing an image to be read in
//                  and displays that image until the user closes image.
//                  ***Author: Steve Donaldson
//Parameters:       None
//Returns:          EasyImageDisplay sampleDisplayObject
//Calls:            KeyboardInputClass
//                  EasyImageDisplay
//Globals:          None
 
 public static EasyImageDisplay DisplayImage() {
		int imageType = 0, width = 0, height = 0, row, column, start;
		int red[][] = null, green[][] = null, blue[][] = null, gray[][] = null;
		char c1,c2,c3,c4;
		KeyboardInputClass keyboardInput = new KeyboardInputClass();
		String userInput;
		String fileName;
		EasyImageDisplay sampleDisplayObject;

		System.out.println("Image Display Routine");
		System.out.println("---------------------\n");
		fileName=keyboardInput.getKeyboardInput("Specify the name of the file containing the image data: ");
		if (fileName.length()>0) {
			try {
				RandomAccessFile imageFile=new RandomAccessFile(fileName,"r");
				start=0;
				imageFile.seek(start);					//move pointer to beginning of file
				//Read the file type, rows, and columns (stored as integers). This requires reading
				//four bytes per value. These bytes represent an integer stored by C++ or Basic
				//(i.e., in low byte to high byte order (not reversed bit order!)). The routine
				//converts to a Java integer representation (i.e., high byte to low byte order).
				c1=(char)imageFile.read();
				c2=(char)imageFile.read();
				c3=(char)imageFile.read();
				c4=(char)imageFile.read();
				imageType=(c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
				if ((imageType!=1)&&(imageType!=2)&&(imageType!=3)) {
					userInput=keyboardInput.getKeyboardInput("Bad file type. Press ENTER to continue...");
					System.exit(0);
				}
				c1=(char)imageFile.read();
				c2=(char)imageFile.read();
				c3=(char)imageFile.read();
				c4=(char)imageFile.read();
				width=(c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
				c1=(char)imageFile.read();
				c2=(char)imageFile.read();
				c3=(char)imageFile.read();
				c4=(char)imageFile.read();
				height=(c4 << 24) | (c3 << 16) | (c2 << 8) | c1;
				//set up color or grayscale array(s)
				if (imageType == 1) {
					red = new int[height][width];
					green = new int[height][width];
					blue = new int[height][width];
				}
				else
					gray = new int[height][width];

				for(row=0;row<height;row++) {
					for(column=0;column<width;column++) {
						if (imageType==1) {			//color
							blue[row][column] = (char)imageFile.read();
							green[row][column] = (char)imageFile.read();
							red[row][column] = (char)imageFile.read();
						}
						else if (imageType==2)		//grayscale
							gray[row][column]=(char)imageFile.read();
					}
				}
				imageFile.close();
			}
			catch (Exception e) {
				userInput=keyboardInput.getKeyboardInput("Error trying to read file. Press ENTER to continue...");
				System.exit(0);
			}

			sampleDisplayObject = new EasyImageDisplay(imageType, width, height, red, green, blue, gray);
			sampleDisplayObject.showImage("Image Display Routine", true);
			userInput = keyboardInput.getKeyboardInput("Press ENTER to exit...");
			//sampleDisplayObject.closeImageDisplay();
                        return sampleDisplayObject;
                       }
                return null;
	}
	//****************************************************************************************
	//end ImageDisplayDriver

}//End of class
//****************************************************************************************
//****************************************************************************************
	
//Class:            SingleRegion
//Discription:      generates all the information needed about a single filtered region.
//Globals:          int regionNumber        this regions label given by the Regions Class
//                  int centriodRow         the row on which the center of the region lies
//                  int centriodColumn      the column on which the center of the region lies
//                  int[][] regionArray     the array containing the region and all the
//                                          other blank pixels.
//                  Regions parent          the Regions object that this region came from
//                  String shape

class SingleRegion{
    
    int regionNumber;
    int centriodRow;
    int centriodColumn;
    int[][] regionInArray;
    Regions parent;
    String shape;
    
    //***************************************************************************
    
    //Method:           SingleRegion
    //Discription:      constructor method for SingleRegion
    //Parameters:       int region              -region number
    //                  int[][] array           -image of region
    //                  Regions parentRegion
    //Returns:          N/A
    //Calls:            Nothing
    //Globals           Regions parent
    //                  int regionNumber
    //                  int centriodRow
    //                  int centriodColumn
    //                  int[][] regionInArray
    
    public SingleRegion(int region, int[][] array, Regions parentRegion){
        
        parent = parentRegion;
        regionNumber = region;
        centriodRow = parentRegion.centroids[regionNumber][0];
        centriodColumn = parentRegion.centroids[regionNumber][1];
        regionInArray = array;
    }//End of SingleRegion constructor method
    //***************************************************************************    
}//End of SingleRegion Class
//*******************************************************************************
//*******************************************************************************

//******************************************************************************************
//******************************************************************************************
//Class:		EasyImageDisplay
//Description:	Implements the paint() routine using one or more 2D arrays containing the
//				image (one for grayscale or binary images, 3 for color images). Assumes
//				that the image is oriented correctly (i.e,. paint() does not flip it).
//				Allows for showing an image one time or continuously (for example, if it
//				is being constantly updated by another routine).
//				Provides routines for referencing or copying image data. The referencing
//				options provide for potentially faster processing but at the expense of
//				creating aliases. The copy options are slower but safer.
//				Also includes the capability to show text on the image.
//Author:		Steve Donaldson
//Date:			4/2/09
class EasyImageDisplay extends Frame {
	public int imageType;				//1=24 bit color;2=256 level gray scale (for binary, use color values 0 and 255)
	public int imageWidth;				//image width in pixels
	public int imageHeight;				//image height in pixels
	public int imageSize;				//height*width (*3 for images of type 1)
	public int pixels[][];				//pixel values in 2D for type 1 or type 2 images
	public int redPixels[][];			//red pixel values for type 1 images
	public int greenPixels[][];			//green pixel values for type 1 images
	public int bluePixels[][];			//blue pixel values for type 1 images
	public boolean showOnce;			//true=show the image one time; false=continuously
										//redraw the image. The "false" option is useful for
										//situations such as those when the image pixels are
										//being changed in another routine.
	public static int windowHeaderOffset = 30;		//space for window title bar
	public static int windowSideOffset = 4;			//space for window side bar(s)
	public static int windowBotttomOffset = 4;		//space for window bottom bar

	public int textLineCount;			//the number of lines of text in array text[]
	public String text[];				//each row contains a line of text to be displayed
	public int textPosition[][];		//the row and column positions in the image at which
										//to display the text
	//**************************************************************************************
	EasyImageDisplay(int type, int width, int height, int redValues[][], int greenValues[][], int blueValues[][], int values[][]) {
		imageType = type;
		imageWidth = width;
		imageHeight = height;
		imageSize = height*width;
		if (imageType == 1)
			imageSize *= 3;
		redPixels=redValues;
		greenPixels = greenValues;
		bluePixels = blueValues;
		pixels = values;
		showOnce = true;
		textLineCount = 0;
		text = null;
		textPosition = null;
	}
	//**************************************************************************************
	EasyImageDisplay(EasyImageDisplay displayObject) {
		imageType = displayObject.imageType;
		imageWidth = displayObject.imageWidth;
		imageHeight = displayObject.imageHeight;
		imageSize = displayObject.imageSize;
		redPixels = displayObject.redPixels;
		greenPixels = displayObject.greenPixels;
		bluePixels = displayObject.bluePixels;
		pixels = displayObject.pixels;
		showOnce = true;
}
	//**************************************************************************************
	//Method:		referenceColorArrayData
	//Description:	Makes the color arrays for this image reference the RGB pixel values
	//				from a specified set of color arrays. Note that a change to the source
	//				arrays will be reflected in this image if subsequently displayed.
	//Parameters:	redValues[][]	- source red pixels
	//				greenValues[][]	- source green pixels
	//				blueValues[][]	- source blue pixels
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean referenceColorArrayData(int redValues[][], int greenValues[][], int blueValues[][]) {
		if ((redValues != null) && (greenValues != null) && (blueValues != null)) {
			redPixels=redValues;
			greenPixels = greenValues;
			bluePixels = blueValues;
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		referenceGrayArrayData
	//Description:	Makes the grayscale array for this image reference the gray pixel values
	//				from a specified grayscale array. Note that a change to the source
	//				array will be reflected in this image if subsequently displayed.
	//Parameters:	values[][]	- source gray pixels
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean referenceGrayArrayData(int values[][]) {
		if (values != null) {
			pixels = values;
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		copyColorArrayData
	//Description:	Copies RGB pixel values from a specified set of arrays into the arrays
	//				for this image. Making a copy prevents a change to the source arrays
	//				from subsequently affecting this image.
	//Parameters:	redValues[][]	- source red pixels
	//				greenValues[][]	- source green pixels
	//				blueValues[][]	- source blue pixels
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean copyColorArrayData(int redValues[][], int greenValues[][], int blueValues[][]) {
		if ((redValues != null) && (greenValues != null) && (blueValues != null)) {
			redPixels = new int[imageHeight][imageWidth];
			greenPixels = new int[imageHeight][imageWidth];
			bluePixels = new int[imageHeight][imageWidth];
			for (int r = 0; r < imageHeight; r++)
				for (int c = 0; c < imageWidth; c++) {
					redPixels[r][c] = redValues[r][c];
					greenPixels[r][c] = greenValues[r][c];
					bluePixels[r][c] = blueValues[r][c];
				}
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		copyGrayArrayData
	//Description:	Copies grayscale pixel values from a specified array into the array
	//				for this image. Making a copy prevents a change to the source array
	//				from subsequently affecting this image.
	//Parameters:	values[][]	- source gray pixels
	//Returns:		true if successful; false otherwise
	//Calls:		nothing
	public boolean copyGrayArrayData(int values[][]) {
		if (values != null) {
			pixels = new int[imageHeight][imageWidth];
			for(int r=0;r<imageHeight;r++)
				for(int c=0;c<imageWidth;c++)
					pixels[r][c] = values[r][c];
			return true;
		}
		return false;
	}
	//**************************************************************************************
	//Method:		showImage
	//Description:	Initializes graphics window parameters and displays its contents.
	//Parameters:	title		- title of the graphics window
	//				displayOnce	- show the image a single time or display it continuously
	//								(i.e., as for continuous digital camera input)
	//Returns:		nothing
	//Calls:		various Java graphics routines
	public void showImage(String title, boolean displayOnce) {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		showOnce = displayOnce;
		setTitle(title);
		setSize(imageWidth + 2 * windowSideOffset, imageHeight + windowHeaderOffset + windowBotttomOffset);
		setVisible(true);
	}
	//**************************************************************************************
	//Method:		closeImageDisplay
	//Description:	Terminates continuous display (if applicable) and closes the graphics
	//				window.
	//Parameters:	none
	//Returns:		nothing
	//Calls:		Java setVisible
	public void closeImageDisplay() {
		showOnce = true;				//exit endless loop in paint()
		setVisible(false);
	}
	//**************************************************************************************
	//Method:		paint
	//Description:	Display an image stored in a 2D array. Overrides the paint method
	//				inherited from Frame (via Container). Allows for showing an image
	//				one time or continuously (for example, if it is being constantly
	//				updated by another routine).
	//Parameters:	g	- the graphics object
	//Returns:		nothing
	//Calls:		setColorValues for an ImageClass object
	//				setGrayValues for an ImageClass object
	//				referenceColorArrayData
	//				referenceGrayArrayData
	//				plus various Java graphics routines
	public void paint(Graphics g) {
		int row, column, pixel;
		Color color = new Color(0);
		int i, a = 0, b = 0;

		while (a == b) {
			if (imageType == 1) {
				for (row = 0; row < imageHeight; row++) {
					for (column = 0; column < imageWidth; column++) {
                                            if(redPixels[row][column] != 0 || greenPixels[row][column] != 0 || bluePixels[row][column] != 0 ){
						color = new Color(redPixels[row][column], greenPixels[row][column], bluePixels[row][column]);
						g.setColor(color);
						g.drawLine(column + windowSideOffset, row + windowHeaderOffset, column + windowSideOffset, row + windowHeaderOffset);
                                            }
                                        }
				}
			}
			else if ((imageType == 2) || (imageType == 3)) {
				for (row = 0; row < imageHeight; row++) {
					for (column = 0; column < imageWidth; column++) {
						pixel = pixels[row][column];
						color = new Color(pixel, pixel, pixel);
						g.setColor(color);
						g.drawLine(column + windowSideOffset, row + windowHeaderOffset, column + windowSideOffset, row + windowHeaderOffset);
					}
				}
			}

			g.setColor(Color.white);
			Font f = new Font("sansserif", Font.BOLD, 12);
			g.setFont(f);
			for (i = 0; i < textLineCount; i++)
				g.drawString(text[i], textPosition[i][1] + windowSideOffset, textPosition[i][0] + windowHeaderOffset);
			
			if (showOnce)
				b++;			//exit
		}
	}
	//end paint()
	//**************************************************************************************
}	//end EasyImageDisplay class
//******************************************************************************************
//******************************************************************************************
//To use this class:
//Create a new instance and load the arrays with the pixel data to be displayed...
