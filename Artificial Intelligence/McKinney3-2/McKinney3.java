//Program:      McKinney3.java
//Course:       COSC470
//Description:
//
//
//
//Author:       Joshua McKinney
//Revised:      3/7/2017
//Language:     Java
//IDE:          NetBeans 8.2
//Notes:        None.
//*******************************************************************************
//*******************************************************************************

//Class:        McKinney3
//Description:  Main class
//Globals:      Picture[]               inputImages                 the Picture array of input images
//              Picture[]               targetImages                the Picture array of images that serve
//                                                                  as the desired output for the supervised learning
//              Neuron[][]              neuronLayer                 the 2D Neuron array that serves as the single layer of neurons
public class McKinney3 {

    static Picture[] inputImages;
    static Picture[] targetImages;
    static Neuron[][] neuronLayer;

//***************************************************************************
    //Method:       main
    //Description:  main method of the program
    //Parameters:   String[] args       (default main method parameters)
    //Returns:      void
    //Calls:        TextFileClass       to read in the text files that the patterns are stored in
    //              Picture             object class that stores the image pixel values
    //              Neuron              object class that stores the information about each neuron
    //              KeyboardInputClass  to get keyboard input from the user
    //              Iterate()           to iterate through the training of neurons
    //Globals:      Picture[]           inputImages
    //              Picture[]           targetImages
    //              Neuron[][]          neuronLayer    
    public static void main(String[] args) {

        System.out.println("McKinney3: Artificial Neural Training and Recall");

        double threshold = 0;

        neuronLayer = new Neuron[11][11];
        for (int row = 0; row < neuronLayer.length; row++) {
            for (int col = 0; col < neuronLayer[0].length; col++) {
                double[][] weights = new double[11][11];
                neuronLayer[row][col] = new Neuron(threshold, weights);
            }//End set weights column for loop
        }//End set weights row for loop

        //read in the text file that specifies the number of pattern sets and the name of each pattern
        TextFileClass indexFile = new TextFileClass();
        indexFile.getFileName("Specify the text file to be read:");
        if (indexFile.fileName.length() <= 0) {
            System.exit(0);
        } else {
            //parse index file
            indexFile.getFileContents();
            int imageSize = 11;
            int imageCount = 0;
            imageCount = (int) Integer.parseInt(indexFile.text[0]);
            String inputImageFileName;
            String targetImageFileName;
            TextFileClass inputImageFile = new TextFileClass();
            TextFileClass targetImageFile = new TextFileClass();
            inputImages = new Picture[imageCount];
            targetImages = new Picture[imageCount];
            for (int image = 0; image < imageCount; image++) {
                inputImageFileName = indexFile.text[image * 2 + 1];
                targetImageFileName = indexFile.text[image * 2 + 2];
                inputImageFile.fileName = inputImageFileName;
                targetImageFile.fileName = targetImageFileName;
                inputImageFile.getFileContents();
                targetImageFile.getFileContents();
                Picture inputImage = new Picture(inputImageFileName, inputImageFile.text[0], imageSize);
                Picture targetImage = new Picture(targetImageFileName, targetImageFile.text[0], imageSize);
                inputImages[image] = inputImage;
                targetImages[image] = targetImage;
            }//End construct/format images for loop
        }//End file length if/else statement
        while (true) {
            //Keep looping through the options until the user decides to exit.
            KeyboardInputClass keyboardInput = new KeyboardInputClass();
            int options = keyboardInput.getInteger(false, 1, 0, 0, "Enter an option:"
                    + "\n1: Train (Default)"
                    + "\n2: Recall"
                    + "\n3: Sever connection(s) (simulate a stroke)"
                    + "\n4: Reset all neuron weights"
                    + "\n5: Exit");
            switch (options) {
                case 1:

                    int iterations = keyboardInput.getInteger(false, 10, 0, 0, "Number of training iterations? (Default: 10):");
                    double learningRate = keyboardInput.getDouble(true, 0.01, 0, 1, "Learning Rate? (Default: 0.01)");
                    Train(iterations, learningRate);
                    break;
                case 2:
                    TextFileClass recallFile = new TextFileClass();
                    recallFile.getFileName("Enter the file namme of an image:");
                    recallFile.getFileContents();
                    Picture recallImage = new Picture("recall", recallFile.text[0], 11);

                    //Recall
                    Recall(recallImage);
                    break;

                case 3:
                    int numOfSevers = keyboardInput.getInteger(false, 100, 0, 0, "Enter the number of neural connections to sever (Default 100):");
                    for (int connection = 0; connection < numOfSevers; connection++) {

                        int randomRowN = (int) (Math.random() * neuronLayer.length);
                        int randomColN = (int) (Math.random() * neuronLayer[0].length);
                        int randomRowW = (int) (Math.random() * neuronLayer[randomRowN][randomColN].weights.length);
                        int randomColW = (int) (Math.random() * neuronLayer[randomRowN][randomColN].weights[0].length);
                        neuronLayer[randomRowN][randomColN].weights[randomRowW][randomColW] = Math.random() - 0.5;
                    }//End of severConnections for loop
                    break;

                case 4:
                    for (int row = 0; row < neuronLayer.length; row++) {
                        for (int col = 0; col < neuronLayer[0].length; col++) {
                            double[][] weights = new double[11][11];
                            neuronLayer[row][col] = new Neuron(threshold, weights);
                        }//End reset weights column for loop
                    }//End reset weights row for loop
                    System.out.println("All neural weights have been reset.");
                    break;

                case 5:
                    System.exit(0);

            }
        }//End continue program while loop
    }//End of main method
    //******************************************************************************************************

    //Method:       PrintImageSet()
    //Description:  This method is used by the train option from the main method to display the 3 images
    //              in Iterate in a nice format. -1 values are displayed as 'X' and +1 values are displayed
    //              as the solid block character Unicode 9608 or \u2588.
    //Parameters:   int[][]         in                  the input image
    //              int[][]         target              the image of the desired output
    //              int[][]         actual              the image created from the current weights and the input image
    //Returns:      void
    //Calls:        Nothing
    //Globals:      None
    public static void PrintImageSet(int[][] in, int[][] target, int[][] actual) {

        for (int row = 0; row < in.length; row++) {
            for (int colIn = 0; colIn < in.length; colIn++) {
                if (in[row][colIn] == 1) {
                    System.out.print("X");//\u2588
                } else {
                    System.out.print(" ");
                }
            }//End input column for loop

            System.out.print("       ");
            for (int targetOut = 0; targetOut < in.length; targetOut++) {
                if (target[row][targetOut] == 1) {
                    System.out.print("X");//\u2588
                } else {
                    System.out.print(" ");
                }
            }//End target column for loop
            System.out.print("       ");
            for (int colActual = 0; colActual < in.length; colActual++) {
                if (actual[row][colActual] == 1) {
                    System.out.print("X");//\u2588
                } else {
                    System.out.print(" ");
                }
            }//End actual column for loop
            System.out.println("");
        }//End all row for loop
    }//End of PrintImage method
    //******************************************************************************************************

    //Method:       Recall()
    //Description:  This method is used by the main method to produce the values of an output image
    //              given the current state of the neural weights and the values of an input image,
    //              then display them both in a visually pleasing format.
    //Parameters:   Picture             pictureIn               the image being passed in
    //Returns:      void
    //Calls:        nothing
    //Globals:      Neuron[][]          neuronLayer
    public static void Recall(Picture pictureIn) {

        int[][] inputImage = pictureIn.image;
        int[][] actualImage = new int[inputImage.length][inputImage[0].length];

        //Attempt to produce target image below.
        for (int row = 0; row < inputImage.length; row++) {
            for (int col = 0; col < inputImage[0].length; col++) {
                actualImage[row][col] = neuronLayer[row][col].Fire(inputImage);
            }//End Fire column for loop
        }//End Fire row for loop

        for (int row = 0; row < inputImage.length; row++) {
            for (int colIn = 0; colIn < inputImage.length; colIn++) {
                if (inputImage[row][colIn] == 1) {
                    System.out.print("X");//\u2588
                } else {
                    System.out.print(" ");
                }
            }//End print input column for loop

            System.out.print("       ");
            for (int colActual = 0; colActual < actualImage.length; colActual++) {
                if (actualImage[row][colActual] == 1) {
                    System.out.print("X");//\u2588
                } else {
                    System.out.print(" ");
                }
            }//End print actual column for loop
            System.out.println("");
        }//End print row for loop

    }//End Recall method
    //************************************************************************************
    
    //Method:       Train()
    //Description:  This method takes in a number of training iterations and a learning rate. It then calculates
    //              the values for the output image given the input values and weights. Next, it calls PrintImageSet
    //              to display the input, target image and output image. Lastly, it calls Learn and  adjusts all of
    //              the weights according to the Widrow-Hoff delta rule.
    //Parameters:   int                 iterations                  the number of training iterations
    //              double              learningRate                a very small learning rate between 0 and 1
    //Returns:      void
    //Calls:        Fire()              See the Neuron class for more on the Fire method
    //              PrintImageSet()     Prints the input image, target image and output image
    //              Learn()             Trains the weights accoring to the Widrow-Hoff delta rule
    //Globals:      Neuron[][]          neuronLayer
    public static void Train(int iterations, double learningRate) {
        for (int iteration = 0; iteration < iterations; iteration++) {
            for (int image = 0; image < targetImages.length; image++) {
                int[][] inputImage = inputImages[image].image;
                int[][] actualImage = new int[inputImage.length][inputImage[0].length];
                int[][] targetImage = targetImages[image].image;

                //Attempt to produce target image below.
                for (int row = 0; row < inputImage.length; row++) {
                    for (int col = 0; col < inputImage[0].length; col++) {
                        actualImage[row][col] = neuronLayer[row][col].Fire(inputImage);
                    }//End column for loop
                }//End row for loop

                //Display input, target, and actual images below
                PrintImageSet(inputImage, targetImage, actualImage);
                System.out.println("");

                //Learning process below
                    Learn(learningRate, inputImage, targetImage);
                
            }//End image for loop
        }//End iterations for loop
    }//End Train method
    //******************************************************************************************************

    //Method:       Learn()
    //Description:  This method takes in a learning rate, and input image and a target image
    //              and trains the weights of the artificial neurons according to the Widrow-Hoff
    //              delta rule. ~~ the new weight (W.i.j.new) equals the old weight(W.i.j.old) times
    //              the difference in the target value and actual value (delta) times a very small learning rate (nu)
    //              ~~W.i.j.new = (W.i.j.old)*delta*nu~~
    //Parameters:   double                  learningRate                    a very small learning rate (nu according to the delta rule)
    //              int[][]                 inputImage                      the input image from which the activation was derived
    //              int[][]                 targetImage                     the image that serves as the desired output (supervised learning)
    //Returns:      void
    //Calls:        Activation()            See the Neuron class for more on the Activation method
    //Globals:      Neuron[][]              neuronLayer
    public static void Learn(double learningRate, int[][] inputImage, int[][] targetImage) {

        for (int row = 0; row < targetImage.length; row++) {
            for (int col = 0; col < targetImage.length; col++) {
                //Delta is target MINUS activation
                neuronLayer[row][col].delta = targetImage[row][col] - neuronLayer[row][col].Activation(inputImage);
                //Widrow-Hoff delta rule: NewWeight = OldWeight PLUS (Eta*Delta*a(sub)i)
                for (int row2 = 0; row2 < targetImage.length; row2++) {
                    for (int col2 = 0; col2 < targetImage.length; col2++) {
                        neuronLayer[row][col].weights[row2][col2] += learningRate * neuronLayer[row][col].delta * inputImage[row2][col2];
                    }//End learning column for loop
                }//End learning row for loop
            }//End train neuron column for loop
        }//End train neuron row for loop
    }//End of Learn method
}//End of class
//******************************************************************************************************
//******************************************************************************************************

//Class:        Picture
//Description:  This class serves as a custom object class to keep track of the image for the
//              McKinney3 Class
//Globals:      int[][]                 image                   this is the 2D array for 1s and -1s that
//                                                              serve as the pixel values of the picture
//              String                  name                    This String serves no funtion in the McKinney3 class
//                                                              currently but could be of some use in the future
class Picture {

    int[][] image;
    String name;
    
//********************************************************************************************************
    
    //Method:       Picture()
    //Description:  Serves as the constructor method for the Picture class\
    //Parameters:   String                  picName                 the name of the picture
    //              String                  imageInput              the pixel values of the image in a single String array format
    //              int                     imageSize               the width/height of the image
    //Returns:      Nothing
    //Calls:        Integer.parseInt()
    //Globals:      String                  name
    //              int[][]                 image
    public Picture(String picName, String imageInput, int imageSize) {
        name = picName;
        image = new int[imageSize][imageSize];
        String[] imageValues = new String[imageSize * imageSize];
        for (int pixel = 0; pixel < imageValues.length; pixel++) {
            imageValues[pixel] = imageInput.split(" ")[pixel];
        }//End of split pixel values
        for (int row = 0; row < imageSize; row++) {
            for (int column = 0; column < imageSize; column++) {
                image[row][column] = (int) Integer.parseInt(imageValues[(row * imageSize) + column]);
            }//End of column for loop
        }//End of row for loop
    }//End of Picture Constructor method
    //******************************************************************************************************
}//End if Picture class
//******************************************************************************************************
//******************************************************************************************************

//Class:        Picture
//Description:  This class serves as a custom object class to keep track of the artificial neurons for the
//              McKinney3 Class
//Globals:      double[][]                  weights                 the weights from each input pixel to this neuron
//              double                      threshold               the firing threshold at which the neuron either fires or does not fire
//              double                      delta                   the differece in the target output and the actual activation of
//                                                                  this neuron given an input image
class Neuron {

    double[][] weights;
    double threshold;
    double delta;

    //*******************************************************************************************************
    
    //Method:       Neuron()
    //Description:  this method serves as the constructor method for the Neuron class
    //Parameters:   double                  thresh                  the firing threshold of this neuron
    //              double[][]              weight                  the 2D double weight array that has already been declaired
    //                                                              with specified dementions
    //Returns:      Nothing
    //Calls:        Math.random()
    //Globals:      double                  threshold
    //              double[][]              weights
    public Neuron(double thresh, double[][] weight) {
        for (int row = 0; row < weight.length; row++) {
            for (int col = 0; col < weight[0].length; col++) {
                weight[row][col] = (Math.random() - 0.5);
            }//End set weights row for loop
        }//End set weights column for loop
        threshold = thresh;
        weights = weight;
    }//End Neuron constructor method
    //******************************************************************************************************

    //Method:       Activation()
    //Description:  this method calculates the activation of this neuron. The activation is the dot product
    //              of the inputs and the weights.
    //Parameters:   int[][]                 inputs                  the pixel values that act as inputs for the activation
    //Returns:      double                  activation              the value for the dot product of the input and weights
    //Calls:        Nothing
    //Globals:      double[][]              weights
    public double Activation(int[][] inputs) {
        double activation = 0;
        for (int row = 0; row < inputs.length; row++) {
            for (int col = 0; col < inputs[0].length; col++) {
                activation += inputs[row][col] * weights[row][col];
            }//End of calculate activation column for loop
        }//End of calculate activation row for loop
        return activation;
    }//End of Activation method
    //******************************************************************************************************

    //Method:       Fire()
    //Description:  this method calculates whether this neuron would fire or not given the inputs and current weights
    //Parameters:   int[][]                 inputs                  the pixel values that serve as the inputs for this neuron
    //Returns:      int                     +1/-1                   if +1 then this neuron should fire*
    //                                                              if -1 then this neuron should not fire*
    //                                                              *given the input values and current weights
    //Calls:        Activation()            see the Activation method for more information
    //Globals:      double                  threshold
    public int Fire(int[][] inputs) {
        double activation = Activation(inputs);
        if (activation < threshold) {
            return -1;
        } else {
            return 1;
        }//End if/else Fire statement
    }//End of Fire method
    //******************************************************************************************************
}//End Neruon class
//******************************************************************************************************
//******************************************************************************************************
