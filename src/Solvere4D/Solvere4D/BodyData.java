/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 * BodyData.java is part of Solvere4D.

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
import java.nio.CharBuffer;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.Math;
/**
 * This function will take a file name to a geometry file, XYX postion data and a
 * rotation matrix for every time step, downsample it and convert it into the
 * form required to for display.
 * 
 * @author mjhmilla
 */
public class BodyData {
  
    private SolvereUtilities SolUtil;
    private File geoFile;
    private File frFile;
    private String keyTag;
    
    private FileReader geoFileFRdr;
    private BufferedReader geoFileBufRdr;
    private String[] geoFileStr;
    private FileReader frFileFRdr;
    private String frFileName;
    private BufferedReader frFileBufRdr;
    private String[] frFileStr;
    private String[] frRepTxt;
    private String[] transFileStr;
    private String[] orienFileStr;
    private String[] keyVal;
    private double[] keyValDbl;

    
    private int defaultRowNum = 1000;
    private int rows;

    
    //x,y,z
    private double[][] translationXYZ_m;

    
    //rot x,y,z
   
    private int downSampling;
    
    
    /**
     * This constructor will take in all of the data required to animate a ridgid
     * body through a series of translations and orientation changes, convert
     * it as required (rotation matrix to axis-angle) and save it for later use.
     * 
     * @param tagName       : The unique string identifier for this body
     * @param vrml97File    : The vrml97File for the geometry of this file
     * @param transXYZ_m    : The n x 3 array of X,Y,Z positions in units of (m)
     * @param rotMat        : The n x 9 array of rotation matrices (taken row wise)
     * @param downSample    : The amount of downsampling to use
     */
    public BodyData(String tagName,File vrml97File,double[][] transXYZ_m, double[][] rotMat, int downSample ){
        
        if(transXYZ_m != null && rotMat != null){
            
        downSampling = downSample;
            
        double[] rotMatrix  = new double[9];
        rotMatrix[0] = 1;    
        rotMatrix[1] = 0;
        rotMatrix[2] = 0;
        
        rotMatrix[3] = 0;    
        rotMatrix[4] = 1;
        rotMatrix[5] = 0;
        
        rotMatrix[6] = 0;    
        rotMatrix[7] = 0;
        rotMatrix[8] = 1;
        
        rows = transXYZ_m.length/downSampling;
        
        translationXYZ_m = new double[rows][3];
        //rotationMAT = new double[rows][9];
        //rotationQUAT = new double[rows][4];
        
        keyVal = new String[rows];
        keyValDbl = new double[rows];
        transFileStr = new String[rows];
        orienFileStr = new String[rows];
        
        StringBuffer tempBufStr = new StringBuffer();

        double[] temp = new double[3];
        for(int i = 0; i < rows; i++){      
            tempBufStr.replace(0,tempBufStr.length(),"");
            temp = transXYZ_m[i*downSampling];//multiply3x3MatrixVec(rotMatrix, transXYZ_m[i]);
           
            for(int j = 0; j < transXYZ_m[0].length; j++){
                translationXYZ_m[i][j] = (float)temp[j];
                tempBufStr.append(translationXYZ_m[i][j]);
                
                if(j < transXYZ_m[0].length-1){
                    tempBufStr.append(" ");}
                   
            }
            transFileStr[i] = tempBufStr.toString();
            keyVal[i] = Double.toString( (float)(i+1)/(float)rows);
            keyValDbl[i] = (float)(i+1)/(float)rows;
            
            
        }
        
        
        double[][] rotQuat = SolUtil.convertToQuat(rotMat);
        for(int i = 0; i < rows; i++){
            
            tempBufStr.replace(0,tempBufStr.length(),"");
            for(int j = 0; j < 4; j++){
                   tempBufStr.append((float)rotQuat[i*downSampling][j]);
                  if(j < 3){
                   tempBufStr.append(" ");}              
            }
            orienFileStr[i] = tempBufStr.toString(); 
        }
        
        keyTag = tagName;
        
        if(vrml97File != null){
            
            try{
                    frFileName = vrml97File.getAbsolutePath();
                    int t1 = frFileName.indexOf(".");
                    frFileName = frFileName.substring(0, t1+1);
                    frFileName = frFileName.concat("fr");
                    
                    geoFile = new File(vrml97File.getAbsolutePath());
                    geoFileFRdr = new FileReader(vrml97File.getAbsolutePath());
                    geoFileBufRdr = new BufferedReader(geoFileFRdr);
                    geoFileStr = new String[defaultRowNum];
                    int fRows = 0;
                    String line = new String(geoFileBufRdr.readLine());
                    
                    while(line != null && line.indexOf("Transform") == -1){
                        line = geoFileBufRdr.readLine();
                    }
                    StringBuffer firstLine = new StringBuffer();
                    firstLine.append("DEF ");
                    firstLine.append(keyTag);
                    firstLine.append(" Transform {");
                    
                    line = firstLine.toString();

                    int f1 = 0; //Index of the 1st "found" tag
                    int f2 = 0; //Index of the 2nd "found" tag
                    int r1 = 0; //Index of the start of the replace text
                    int r2 = 0; //Index of the end of the replacement text
                    int idxCOMM = 0; //Comment index;
                    int rline = 0; //lines in the replacement text;
                    String rtxtLine = new String("");
                    String ftag = new String("");
                    String line1 = new String("");
                    String line2 = new String("");
                    Integer tagNo = new Integer("0");

                    while(line != null){

                        idxCOMM = line.indexOf("#");
                        f1 = line.indexOf("$");

                        if( (f1 != -1 && idxCOMM == -1) ||(f1 < idxCOMM && idxCOMM != -1)){
                            f2 = line.indexOf("$", f1+1);
                            ftag = line.substring(f1+1, f2);
                            tagNo = new Integer(ftag);


                            if (frFile == null){
                                frFile = new File(frFileName);
                                frFileFRdr = new FileReader(frFileName);
                                frFileBufRdr = new BufferedReader(frFileFRdr);
                                frFileStr = new String[defaultRowNum];
                                rtxtLine = frFileBufRdr.readLine();

                                while(rtxtLine != null){
                                    frFileStr[rline] = rtxtLine;
                                    rline++;
                                    rtxtLine = frFileBufRdr.readLine();
                                }


                            }

                            line1 = new String(line.substring(0, f1));
                            line2 = new String(line.substring(f2, line.length()-1));
                            
                            
                            line = line1.concat(frFileStr[tagNo.intValue()-1]);

                            if(line2 != null)
                                line = line.concat(line2);

                           
                            
                        }

                        geoFileStr[fRows] = line;
                        fRows++;

                        if( fRows%(defaultRowNum-1) == 0){
                            //resize dataString without losing data;
                            int scaleFactor = 1 + (int)Math.floor((double)fRows/(double)(defaultRowNum-2));
                            String[] tempDataHolder = new String[scaleFactor*fRows];
                            for(int i = 0; i < fRows; i++)
                                    tempDataHolder[i]= geoFileStr[i];

                             geoFileStr = tempDataHolder;
                        }
                        line = geoFileBufRdr.readLine();
                    }

                    String[] tempDataHolder = new String[fRows];
                    for(int i = 0; i < fRows; i++)
                            tempDataHolder[i] = geoFileStr[i];

                    geoFileStr = tempDataHolder;

                    geoFileFRdr.close();
                    geoFileBufRdr.close();
                    
            }catch(FileNotFoundException fnfe){
                System.out.println("VRML/X3D file not found exception thrown in LeafData");
                fnfe.printStackTrace();
            }catch(IOException ioe){
                System.out.println("IO exception thrown while trying to read VRML/X3D file in LeafData");
                ioe.printStackTrace();
            }   
                    
                    
            }else{

                geoFileFRdr = null;
                geoFileBufRdr = null;
                geoFileStr = null;
            }
        }
    }
    
    

    
        
    public File getGeometryFile(){
        return geoFile;
    }
    
    public double[][] getTranslationXYZ_m(){
        return translationXYZ_m;
    }
 
    public String[] getGeometryText(){
        return geoFileStr;
    }
    
    public String[] getTranslationText(){
        return transFileStr;
    }

    public String[] getOrientationText(){
        return orienFileStr;
    }
    
    public String getTagName(){
        return keyTag;
    }
    
    public String[] getKeyFrameText(){
        return keyVal;
    }
    public double[] getKeyFrame(){
        return keyValDbl;
    }
   
    
}