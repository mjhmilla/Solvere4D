/*
 * Copyright Matthew J.H. Millard 2008
 * 
 * TextParser.java is part of Solvere4D.

    Solvere4D is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Solvere4D is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Solvere4D.  If not, see <http://www.gnu.org/licenses/>.
 */

package Solvere4D;


import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
/**
 *This class is designed to take a text file that is filled with rows 
 * and columns of data delimeted by delimeter1 and delimeter2 and parse it into 
 * an array of doubles. 
 * 
 * If the 
 * entire file is delimeted by the same characters this class will parse
 * the entire file into an array of doubles. You can get at the double values 
 * using getDoubleData(). You can get a string representation using getStringData()
 * 
 * @author Matthew Millard
 */
public class TextParser {
    
    private FileReader dataFile;
    private BufferedReader dataBuf;
    private String[][] dataString;
    
    private int defaultRowNum = 1000;
    private int rows;
    private int cols;
    
    /**
     * @author Matthew Millard
     * 
     * This class is designed to take a text file that is filled with rows 
     * and columns of data delimeted by delimeter1 and delimeter2. If the 
     * entire file is delimeted by the same characters this class will parse
     * the entire file into an array of doubles. You can get at the double values 
     * using getDoubleData(). You can get a string representation using getStringData()
     * 
     * @param absoluteFilePath : The complete file path of the desired data file
     * @param delimeter1 : The delimeter to the left of a column
     * @param delimeter2 : The delimeter to the right of a column
     * 
     * @throws FileNotFoundException : if the file path is invalid
     * @throws  IOException : if an IO exception is made
     */
    public TextParser(String absoluteFilePath, String delimeter1, String delimeter2) {
        if(absoluteFilePath != null && delimeter1 != null && delimeter2 != null
                && absoluteFilePath != "" && delimeter1 != "" && delimeter2 != ""){
        
            
            try{
                dataFile = new FileReader(absoluteFilePath);
                dataBuf = new BufferedReader(dataFile);
            
                String line = dataBuf.readLine();
                String field = "";
                cols = 0;
                int index = 0;
                
                while(field != null){
                    
                    field = getNextField(line, delimeter1,delimeter2,index);
                    
                    if(field != null){
                        index += field.length() + delimeter2.length();
                        cols++;
                    }
                }
                
                dataString = new String[defaultRowNum][cols];
                rows = 0;
                String element = "";
                
                while(line != null && line.compareTo("") != 0){
                    index = 0;
                    
                    for(int i = 0; i < cols; i++){
                        element = getNextField(line, delimeter1, delimeter2, index);
                        
                        if(element != null){
                            dataString[rows][i] = element; 
                            index += dataString[rows][i].length() + delimeter2.length();
                        }else{
                            dataString[rows][i] = ""; 
                            index += dataString[rows][i].length() + delimeter2.length();  
                        }
                    }
                    rows++;
                    
                    if( rows%(defaultRowNum-1) == 0){
                        //resize dataString without losing data;
                        int scaleFactor = 1 + (int)Math.floor((double)rows/(double)(defaultRowNum-2));
                        String[][] tempDataHolder = new String[scaleFactor*rows][cols];
                        for(int i = 0; i < rows; i++)
                            for(int j = 0; j < cols; j++)
                                tempDataHolder[i][j] = dataString[i][j];
                    
                         dataString = tempDataHolder;
                    }
                    line = dataBuf.readLine();
                }
            
                String[][] tempDataHolder = new String[rows][cols];
                for(int i = 0; i < rows; i++)
                    for(int j = 0; j < cols; j++)
                        tempDataHolder[i][j] = dataString[i][j];
                
                dataString = tempDataHolder;
            
                dataFile.close();
                dataBuf.close();
                
            }catch(FileNotFoundException fnfe){
                System.out.println("File not found exception thrown in TextParser");
                fnfe.printStackTrace();
            }catch(IOException ioe){
                System.out.println("IO exception thrown in TextParser");
                ioe.printStackTrace();
            }
        
        }
    }
    
/**Grabs the next data field separated by delimeter1 and delimeter2, while igoring delimeters in
         * between that have no data between them. The search sarts at index1. If it finds nothing it returns
         * null if enableException is false and throws an InvalidFileFormatException if enableException is true
         *
         *@param LineContents: the string contents to be searched
         *@param delimeter1: string of the first delimeter
         *@param delimeter2: string of the second delimter
         *@param index1: the index within LineContents to begin the search
         *@param enableException: if you are expecting a next field set this to true. If you are checking if there is a next
         *                field set this to false - that way if there is nothing this function returns null.
         *@return the next non empty field in string form separated by delimeter1 and delimeter2 in LineCntents
         *@throws InvalidFileFormatException: if enableException is true and this routine does not find a non-empty next field
         *@throws                             delimeted by delimeter1 and delimeter2.
         */
        private String getNextField(String LineContents,String delimeter1, String delimeter2 ,int index1){

            int d1 = index1;
            int d2 = index1 + 1;
            String nextField = "";
                if(LineContents.charAt(0) != delimeter1.toCharArray()[0])
                    LineContents = delimeter1+LineContents;
            
                if(LineContents.charAt(LineContents.length()-1) != delimeter2.toCharArray()[0]);
                    LineContents = LineContents + delimeter2;
            
                if(LineContents == null) return null;
                do{
                    d1 = LineContents.indexOf(delimeter1,d1);
                    d2 = LineContents.indexOf(delimeter2,d1 + delimeter1.length());

                
                    if(d1 != -1 && d2 != -1){
                        nextField = LineContents.substring(d1 + delimeter1.length(), d2);
                        nextField = nextField.trim();
                    }else{
                       
                            return null;
                        }
                    
                    
                    d1 = d2;
                    d2 = d2 + delimeter1.length();
                    
                }while(nextField.compareTo("") == 0);
                    
            return nextField;

        }
        /**
         * @return  A 2D data table of doubles of the data that was in the text field
         */
        public double[][] getDoubleData(){
            double[][] doubleData = new double[rows][cols];
            
            if(dataString != null){
            
            Double tempField = new Double(0.0);
            for(int i = 0; i< rows; i++){
                for(int j = 0; j < cols; j++){
                    tempField = new Double(dataString[i][j]);
                    doubleData[i][j] = tempField.doubleValue(); 
                }
            }
            }
            return doubleData;
        }

        /**
         * @return  A 2D data table of Strings of the data that was in the text field
         */
        public String[][] getStringData(){
            String[][] temp = new String[dataString.length][dataString[0].length];
            for(int i = 0; i < dataString.length; i++){
                for(int j = 0; j < dataString[0].length; j++){
                    temp[i][j] = dataString[i][j];
                }
            }
            
            
            return temp;
        }
        
        /**
         * @return  the number of rows in the 2D data array
         */
        public int numRows(){
            return dataString.length;
        }
        
        /**
         * @return  the number of columns in the 2D data array
         */
        public int numCol(){
            return dataString[0].length;
        }
        
}