/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 * ForceTorqueData.java is part of Solvere4D.

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
 * This class will take arrays of time, X, Y, Z and Fx, Fy, Fz and Tx, Ty, Tz 
 * along with normalizing parameters to generate animated force and torque 
 * vectors that change magnitude with the magnitude of F and T, and also change 
 * colour with the orientation of the vectors.
 * 
 * @author mjhmilla
 */
public class ForceTorqueData {
    
    private SolvereUtilities SolUtil;
    private String keyForceTag;
    private String keyTorqueTag;
    
    private String[] geoForceFileStr;
    private String[] geoTorqueFileStr;
    
    private String[] transFileStr;
    private String[] fOrienFileStr;
    private String[] tOrienFileStr;
    private String[] fScaleFileStr;
    private String[] tScaleFileStr;
    private String[] fColourFileStr;
    private String[] tColourFileStr;
    private String fTransparency;
    private String tTransparency;
    
    private String[] keyVal;
    private double[] keyValDbl;
    private StringBuffer tempStrBuf;
    
    private int defaultRowNum = 1000;
    private int rows;
    private int cols;
    
    //x,y,z
    private double[][] posXYZ_m;
    private double[] forceXYZ_m;
    private double[] torqueXYZ_m;
    
    private double maxTorque;
    private double maxForce;
    
    private double[][] forceOrien;
    private double[][] torqueOrien;
    
    private double[][] fColour;
    private double[][] tColour;
    
    private double[][] fVector_d;
    private double[][] tVector_d;
    
    //plotting
    private boolean genPlots;
    private boolean genForcePlots;
    private boolean genTorquePlots;
    
    private boolean[] plotFlags;
    /*Index
     * 0 - generate force plot
     * 1 - - wireframe True, solid False
     * 2 - - setColour True, use default False
     * 3 - generate torque plot
     * 4 - - wireframe True, solid False
     * 5 - - setColour True, use default False
     */
    private double[] plotOptions;
    /*Index
     *
     *  Force Plots
     * 0 - R
     * 1 - G
     * 2 - B
     * 3 - t (transparency)
     *
     *  Torque Plots
     * 4 - R
     * 5 - G
     * 6 - B
     * 7 - t (transparency)
     */
    
    private double[][][] pFTri;
    private String[] pFTriStr;
    private String[] cFTriStr;
    private double[][][] pTTri;
    private String[] pTTriStr;
    private String[] cTTriStr;

    private int downSampling;
    
    /**
     * This constructor will take in all of the data from the user required to
     * represent forces and torques as animated 3D vectors of arrows, and arrows
     * with disks that are applied the point the force is, whose magnitude is scaled
     * with the the size of the force and whose colour changes with orientation.
     * The force and torque vectors must be sampled at the same times as the rest
     * of the animation data.
     * 
     * 
     * @param fTTagName     : The unique string name that will be used to indentify this element
     * @param geoForceFile  : The *.wrl file that should be used to represent the 
     *                      force vector. This is usually "../../SolvereLibs/WRL_SYNTAX\lib_force.wrl"
     * @param geoTorqueFile : The *.wrl file that should be used to represent the 
     *                      torque vector. This is usually "../../SolvereLibs/WRL_SYNTAX\lib_torque.wrl" 
     * @param fTTransXYZ    : The time series of X,Y,Z positions
     * @param fVector       : An n x 3 array of FX, FY, FZ values  
     * @param tVector       : An n x 3 array of TX, TY, TZ values
     * @param fNorm         : The magnitude of F will be divided by fNorm
     * @param tNorm         : The magnitude of T will be divided by dNorm
     * @param dNorm         : (distance Norm) The magnidue of F, and T will be multipled by dNorm
     * @param plotFlag      : A series of flags that will plot a history of the F and T
     *                        values as desired by the user:
     *                      <li> Index: 0. True = Plot Force History,   False = No plot
     *                      <li> Index: 1. True = Wire frame,           False = Render as solid
     *                      <li> Index: 2. True = Use user set colour,  False = use directional colours
     *                      <li> Index: 3. True = Plot Torque History,  False = No plot
     *                      <li> Index: 4. True = Wire frame,           False = Render as solid
     *                      <li> Index: 5. True = Use user set colour,  False = use directional colours
     * @param plotOpt       :The colour and transparency values to apply to the historical plots if
     *                       they are going to be rendered in one single colour (R(0,1), G(0,1), B(0,1) and T(0,1)). 
     * @param downSampleFactor : The factor by which to downsample the data
     * 
     */
    public ForceTorqueData(String fTTagName, File geoForceFile,File geoTorqueFile,double[][] fTTransXYZ, double[][] fVector, double [][] tVector,  
            double fNorm, double tNorm, double dNorm, boolean[] plotFlag, double[] plotOpt, int downSampleFactor) {
    
       
        keyForceTag = fTTagName.concat("_F");
        keyTorqueTag = fTTagName.concat("_T");

        downSampling = downSampleFactor;
        
        int rows = (int)Math.floor(fTTransXYZ.length/downSampling);
        posXYZ_m = new double[rows][3];
        forceXYZ_m = new double[rows];
        torqueXYZ_m = new double[rows];
        forceOrien = new double[rows][4];
        torqueOrien = new double[rows][4];
        fColour = new double[rows][3];
        tColour = new double[rows][3];
        fVector_d = new double[rows][3];
        tVector_d = new double[rows][3];
        
        double fCScaling = 1.0;
        double tCScaling = 1.0;
        maxForce = 0;
        maxTorque = 0;
        
        plotFlags = new boolean[plotFlag.length];
        plotOptions = new double[plotOpt.length];
        
        for(int i=0; i<plotFlag.length; i++)
            plotFlags[i]=plotFlag[i];
        
        for(int i=0; i<plotOpt.length; i++)
            plotOptions[i]=plotOpt[i];
                
        if(plotFlags[0] || plotFlags[3]){
            genPlots = true;
            genForcePlots = plotFlags[0];
            genTorquePlots = plotFlags[3];
        }
        else{genPlots = false;}
        
        if(genPlots){ 
               pFTri = new double[rows][6][3];
               pTTri = new double[rows][6][3];
        }
        
        
        double[] rVec = new double[3];
        rVec[0] = 0;
        rVec[1] = 1;
        rVec[2] = 0;

        
        for(int i=0; i < rows; i++){
            
            forceXYZ_m[i] = 0;
            torqueXYZ_m[i] = 0;
            
            for(int j=0; j < 3; j++){
                posXYZ_m[i][j] = fTTransXYZ[i*downSampling][j];
                forceXYZ_m[i] = forceXYZ_m[i] + fVector[i*downSampling][j]*fVector[i*downSampling][j];
                torqueXYZ_m[i] = torqueXYZ_m[i] + tVector[i*downSampling][j]*tVector[i*downSampling][j];
                fVector_d[i][j] = fVector[i*downSampling][j];
                tVector_d[i][j] = tVector[i*downSampling][j];
            }
            
            forceXYZ_m[i] = Math.sqrt(forceXYZ_m[i]);
            torqueXYZ_m[i] = Math.sqrt(torqueXYZ_m[i]);
            
            if(forceXYZ_m[i] == Double.NaN) forceXYZ_m[i] = 0;
            if(torqueXYZ_m[i] == Double.NaN) torqueXYZ_m[i] = 0;
            
            if(Math.abs(forceXYZ_m[i]) > fCScaling)
                fCScaling = Math.abs(forceXYZ_m[i]);
            if(Math.abs(torqueXYZ_m[i]) > tCScaling)
                tCScaling = Math.abs(torqueXYZ_m[i]);
            
            if(Math.abs(forceXYZ_m[i]) > maxForce)
                maxForce = Math.abs(forceXYZ_m[i]);
            if(Math.abs(torqueXYZ_m[i]) > maxTorque)
                maxTorque = Math.abs(torqueXYZ_m[i]);
            
            
            forceXYZ_m[i] = forceXYZ_m[i]*dNorm/fNorm;
            torqueXYZ_m[i] = torqueXYZ_m[i]*dNorm/tNorm;
            
        }
        if(genPlots){
            pFTri = SolUtil.getTriangularArray(posXYZ_m, fVector_d, forceXYZ_m);
            pTTri = SolUtil.getTriangularArray(posXYZ_m, tVector_d, torqueXYZ_m);
        }
        
        forceOrien = SolUtil.getAxisAngle(fVector_d,rVec);
        torqueOrien = SolUtil.getAxisAngle(tVector_d,rVec);
        
        fColour = SolUtil.getColourMapping(fVector_d);
        tColour = SolUtil.getColourMapping(tVector_d);
        
        //Now make the text versions of these matricies
        keyVal = new String[rows];
        keyValDbl = new double[rows];
        transFileStr = new String[rows];
        fOrienFileStr = new String[rows];
        tOrienFileStr = new String[rows];
        fColourFileStr = new String[rows];
        tColourFileStr = new String[rows];
        fScaleFileStr = new String[rows];
        tScaleFileStr = new String[rows];
    
        StringBuffer transBufStr = new StringBuffer();
        StringBuffer fOrienBufStr = new StringBuffer();
        StringBuffer tOrienBufStr = new StringBuffer();
        StringBuffer tScaleBufStr = new StringBuffer();
        StringBuffer fScaleBufStr = new StringBuffer();
        StringBuffer fColourBufStr = new StringBuffer();
        StringBuffer tColourBufStr = new StringBuffer();
        
        StringBuffer pFTriBufStr = new StringBuffer();
        StringBuffer pTTriBufStr = new StringBuffer();
        StringBuffer cFTriBufStr = new StringBuffer();
        StringBuffer cTTriBufStr = new StringBuffer();
        
        if(genPlots){
            pFTriStr = new String[rows*6];
            pTTriStr = new String[rows*6];    
            
            if(plotFlags[2]){ 
                cFTriStr = new String[1];
                cFTriStr[0] = new String();
                cFTriStr[0] =   Double.toString(plotOptions[0]) + " " +
                                Double.toString(plotOptions[1]) + " " +
                                Double.toString(plotOptions[2]);
                fTransparency = Double.toString(plotOptions[3]);
                
            }else{ cFTriStr = new String[rows*6];}
            
            if(plotFlags[5]){ 
                cTTriStr = new String[1];
                cTTriStr[0] = new String();
                cTTriStr[0] =   Double.toString(plotOptions[4]) + " " +
                                Double.toString(plotOptions[5]) + " " +
                                Double.toString(plotOptions[6]);
                tTransparency = Double.toString(plotOptions[7]);
                
            }else{ cTTriStr = new String[rows*6];}
        }
        
        double[] temp = new double[3];
        for(int i = 0; i < rows; i++){      
            transBufStr.replace(0,transBufStr.length(),"");
            fOrienBufStr.replace(0,fOrienBufStr.length(),"");
            tOrienBufStr.replace(0,tOrienBufStr.length(),"");
            tScaleBufStr.replace(0,tScaleBufStr.length(),"");
            fScaleBufStr.replace(0,fScaleBufStr.length(),"");
            fColourBufStr.replace(0,fColourBufStr.length(),"");
            tColourBufStr.replace(0,tColourBufStr.length(),"");

            for(int j = 0; j < 3; j++){
                transBufStr.append(posXYZ_m[i][j]);
                fColourBufStr.append(fColour[i][j]);
                tColourBufStr.append(tColour[i][j]);
                fScaleBufStr.append(forceXYZ_m[i]);
                tScaleBufStr.append(torqueXYZ_m[i]);
                if(j < 2){
                    transBufStr.append(" ");
                    fColourBufStr.append(" ");
                    tColourBufStr.append(" ");
                    fScaleBufStr.append(" ");
                    tScaleBufStr.append(" ");
                }
            }
            
             for(int j = 0; j < 4; j++){
                fOrienBufStr.append(forceOrien[i][j]);
                tOrienBufStr.append(torqueOrien[i][j]);
                if(j < 3){
                    fOrienBufStr.append(" ");
                    tOrienBufStr.append(" ");
                }
            }
            
            if(genPlots){
                
                for(int j=0; j<6; j++){   
                    for(int k=0; k<3; k++){
                        pFTriBufStr.append(pFTri[i][j][k]);
                        pTTriBufStr.append(pTTri[i][j][k]);
                        
                        cFTriBufStr.append(fColour[i][k]);
                        cTTriBufStr.append(tColour[i][k]);
                        
                        if(k < 2){
                            pFTriBufStr.append(" ");
                            pTTriBufStr.append(" ");
                            cFTriBufStr.append(" ");
                            cTTriBufStr.append(" ");
                        }
                    }
                    pFTriStr[i*6+j] = pFTriBufStr.toString();
                    pTTriStr[i*6+j] = pTTriBufStr.toString();
                    
                    if(plotFlags[2] == false) cFTriStr[i*6+j] = cFTriBufStr.toString();
                    if(plotFlags[5] == false) cTTriStr[i*6+j] = cTTriBufStr.toString();
                    
                    pFTriBufStr.replace(0,pFTriBufStr.length(),"");
                    pTTriBufStr.replace(0,pTTriBufStr.length(),"");
                    cFTriBufStr.replace(0,cFTriBufStr.length(),"");
                    cTTriBufStr.replace(0,cTTriBufStr.length(),"");
                }
                
                
            }
                
            
            transFileStr[i] = transBufStr.toString();
            keyValDbl[i] = (double)(i+1)/(double)rows;
            keyVal[i] = Double.toString(keyValDbl[i]); 
            fOrienFileStr[i] = fOrienBufStr.toString();
            tOrienFileStr[i] = tOrienBufStr.toString();
            fColourFileStr[i]= fColourBufStr.toString();
            tColourFileStr[i]= tColourBufStr.toString();
            fScaleFileStr[i] = fScaleBufStr.toString();
            tScaleFileStr[i] = tScaleBufStr.toString();
        }
        geoForceFileStr = getGeoText(geoForceFile, "force", fTTagName.concat("_F"));
        geoTorqueFileStr= getGeoText(geoTorqueFile,"torque", fTTagName.concat("_T"));
        //Parse force and torque 3D files.


    }
    
/**
 * This function will open a text file and replace the beginning up to the end 
 * of "oldTag" with "newTag", the rest of the file will be read in as is and passed
 * out the the user as a string array with one line per index.
 * 
 * @param geoFile   : The *.wrl file
 * @param oldTag    : The keyword at which everything preceding is replaced by newtag
 * @param newTag    : The replacement for the header of the file.
 * @return  : A string array of the text file with one line per index.
 */
    private String[] getGeoText(File geoFile, String oldTag, String newTag){
       
        String[] geoFileStr = null;
        
        if(geoFile != null){
            
            try{
                    FileReader geoFileFRdr = new FileReader(geoFile.getAbsolutePath());
                    BufferedReader geoFileBufRdr = new BufferedReader(geoFileFRdr);
                    
                    
                    geoFileStr = new String[defaultRowNum];
                    rows = 0;
                    String line = new String(geoFileBufRdr.readLine());
                    
                    while(line != null && line.indexOf("Transform") == -1){
                        line = geoFileBufRdr.readLine();
                    }
                    line = line.replaceAll(oldTag, newTag);
                    
                    while(line != null){
                        
                        if(line != null){
                            geoFileStr[rows] = line;
                            rows++;
                        }

                        if( rows%(defaultRowNum-1) == 0){
                            //resize dataString without losing data;
                            int scaleFactor = 1 + (int)Math.floor((double)rows/(double)(defaultRowNum-2));
                            String[] tempDataHolder = new String[scaleFactor*rows];
                            for(int i = 0; i < rows; i++)
                                    tempDataHolder[i]= geoFileStr[i];

                             geoFileStr = tempDataHolder;
                        }
                        line = geoFileBufRdr.readLine();
                        if(line != null)
                            line = line.replaceAll(oldTag, newTag);
                    }

                    String[] tempDataHolder = new String[rows];
                    for(int i = 0; i < rows; i++)
                            tempDataHolder[i] = geoFileStr[i];

                    geoFileStr = tempDataHolder;

                    geoFileFRdr.close();
                    geoFileBufRdr.close();
                    
            }catch(FileNotFoundException fnfe){
                System.out.println("VRML/X3D file not found exception thrown in ForceTorqueData");
                fnfe.printStackTrace();
            }catch(IOException ioe){
                System.out.println("IO exception thrown while trying to read VRML/X3D file in ForceTorqueData");
                ioe.printStackTrace();
            }   
        }
        return geoFileStr;
    }
    

    public String getForceTag(){
        return keyForceTag;
    }
    
    public String getTorqueTag(){
        return keyTorqueTag;
    }
    
    public String[] getKeyFrameText(){
        return keyVal;
    }
    
    public String[] getForcePosText(){
        if( maxForce > 0){ return transFileStr;
        }else{ return null;}
        
    }
    
    public String[] getForceOrienText(){
        if( maxForce > 0){ return fOrienFileStr;
        }else{ return null;}

    }
    
    public String[] getForceScalingText(){
        if(maxForce > 0){ return fScaleFileStr;
        }else{ return null;}
        
    }
    
    public String[] getForceColouringText(){
        if(maxForce > 0){ return fColourFileStr;
        }else{ return null;}
    }
     
    
    
    
    public String[] getTorquePosText(){
        if(maxTorque > 0){ return transFileStr;
        }else{ return null;}
        
    }
    
    public String[] getTorqueOrienText(){
        if(maxTorque > 0){ return tOrienFileStr;
        }else{ return null;}
        
    }
    
    public String[] getTorqueScalingText(){
        if(maxTorque > 0){ return tScaleFileStr;
        }else{ return null;}
        
    }
    
    public String[] getTorqueColouringText(){
        if(maxTorque > 0){ return tColourFileStr;
        }else{ return null;}
        
    }
    
    public String[] getForceGeometryText(){
        if(maxForce > 0){ return geoForceFileStr;
        }else{ return null;}
        
    }
    
    public String[] getTorqueGeometryText(){
        if(maxTorque > 0){ return geoTorqueFileStr;
        }else{ return null;}
    }
    
    public String[] getForcePlotText(){
        if(genForcePlots && maxForce > 0){ return pFTriStr;
        }else{ return null;}
    }
    
    public String[] getTorquePlotText(){
        if(genTorquePlots && maxTorque > 0){ return pTTriStr;
        }else{ return null;}
    }
    
    public String[] getForcePlotColourText(){
        if(genForcePlots && maxForce > 0){ return cFTriStr;
        }else{return null;}
    }
    
    public String[] getTorquePlotColourText(){
        if(genTorquePlots && maxTorque > 0){ return cTTriStr;
        }else{ return null; }
    }
    
    public boolean plotForceHist(){
        return genForcePlots;
    }
    
    public boolean plotTorqueHist(){
        return genTorquePlots;
    }
    
    public boolean plotForceInWireFrame(){
        return plotFlags[1];
    }
    
    public boolean plotTorqueInWireFrame(){
        return plotFlags[4];
    }
    
    public double getForcePlotTransparency(){
        return plotOptions[3];
    }
    
    public double getTorquePlotTransparency(){
        return plotOptions[3];
    }
    
    public String getForceTransparencyText(){
        return fTransparency;
    }
    
    public String getTorqueTransparencyText(){
        return tTransparency;
    }
  
}
