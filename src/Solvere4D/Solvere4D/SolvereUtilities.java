/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 * SolvereUtilities.java is part of Solvere4D.

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
 * This class contains a number of very useful static functions that are employed
 * by the rest of the package to convert and format orientation, colour and triangular
 * meshes in a way that is expected in VRML/X3D. This class also contains all of
 * the "append ..." functions which are employed by the main function to write
 * the VMRL or X3D file. Currently this group of functions is implemented in a rather
 * un-elegant brute force manner. It would be far nicer to have say an external
 * library of the syntax of each of the tags such that the code would be more readable,
 * easier to update. There are probably other even more elegant ways of accomplishing
 * this. Alas my knowledge of programming languages is limited at this point, and
 * this is the best I could do with the time I had for the first version.
 * 
 * To Do's: 
 *  1) Update all append functions to also append in the X3D format
 *  2) Software architecture update? Move syntax for the different types of 
 *      VRML/X3D elements into text files?
 * 
 * @author mjhmilla, Versio 1.0, September 17,2008
 */
public class SolvereUtilities {

    
    /**
     * This function will parse the numbers for red, green, blue and transparency
     * out of text of the following format: "R0.5 G0.1 B0.8 T0.5", which would
     * result in a double array of (0.5, 0.1, 0.8, 0.5). If any of the R, G, B
     * or T strings are not found, values of 0.0 are returned in their places.
     * 
     * @param text      : The text that contains the RGBT values
     * @param minIdx    : Minimum character index in the line of text to begin searching for the RGBT tag
     * @param maxIdx     :Maximum character index in the line of text to begin searching for the RBGT tag
     * @param fileLineNo :The line number that text was taken from. Used to write useful error messages.
     * @return The double values for R, G, B and T in a double array
     */
     public static double[] parseRGBT(String text, int minIdx, int maxIdx, int fileLineNo){
        double[] rgbt = new double[4];
        String tempProp;
        Double tempPropD;
        int p = 0;
        int q = 0;
        
        text = " " + text + " ";
        maxIdx = maxIdx+2;
        
        try{
            p = text.indexOf(" R", minIdx);
            if(p==-1){
                p = text.indexOf("R", minIdx);
                if(p != 0) p=-1;
            }
            
            q = text.indexOf(" ", p+2);
            if(q>=maxIdx || q == -1)
                 q=maxIdx;
            
            if(p != -1 && q != -1 && p <= maxIdx){
                tempProp = text.substring(p+2,q);
                tempPropD = new Double(tempProp);
                rgbt[0] = tempPropD.doubleValue();
            }else{
                rgbt[0] = 0.0;
            }

            p = text.indexOf(" G", minIdx);
            q = text.indexOf(" ", p+1);
            if(q>=maxIdx || q == -1)
                 q=maxIdx;

            if(p != -1 && q != -1 && p <= maxIdx){
                tempProp = text.substring(p+2,q);
                tempPropD = new Double(tempProp);
                rgbt[1] = tempPropD.doubleValue();
            }else{
                rgbt[1] = 0.0;
            }

            p = text.indexOf(" B", minIdx);
            q = text.indexOf(" ", p+1);
            if(q>=maxIdx || q == -1)
                 q=maxIdx;
            
            if(p != -1 && p <= maxIdx){
                tempProp = text.substring(p+2,q);
                tempPropD = new Double(tempProp);
                rgbt[2] = tempPropD.doubleValue();
            }else{
                rgbt[2] = 0.0;
            }
            p = text.indexOf(" T", minIdx);
            q = text.indexOf(" ", p+1);
            if(q>=maxIdx || q == -1)
                 q=maxIdx;
                 
            
            if(p != -1 && p <= maxIdx){
                tempProp = text.substring(p+2,q);
                tempPropD = new Double(tempProp);
                rgbt[3] = tempPropD.doubleValue();
            }else{
                rgbt[3] = 0.0;
            }
            
        }catch(NumberFormatException nfe){
            System.out.println("Error: Parsing RGB information " + text + " from " + minIdx + " to " + maxIdx + " from Line " + fileLineNo + " of the *.s4d file"); 
            for(int i=0; i<4;i++)
                rgbt[i]=Double.NaN;
        }
        return rgbt;
    }
    /**
     *This function will parse a number out of a text that is preceded by sTag,
     * followed by eTag is after minIdx and before maxIdx and occurs at fileLineNo
     * in the text file. The fileLineNo parameter is just used to produce useful
     * error messages in case the function fails to extract a number out of the
     * text.
     * 
     * @param text      : The text which contains the number to be parsed
     * @param sTag      : The string that precedes the number
     * @param eTag      : The string that proceeds the number. If the number is at
     *                    the end of a line, put a dummy eTag in like " ". Do not
     *                    put nothing ("") in, else the function will not work properly
     * @param minIdx    : The minimum index of the string at which the sTag occurs
     * @param maxIdx    : The maximum index of the string at which the eTag can occur
     *                    if maxIdx == text.length, then the eTag can be ignored if
     *                    it cannot be found (because the number is at the end of the line)
     * @param fileLineNo    : This is the current line number of the file that is
     *                       being parsed - this allows this function to produce
     *                       useful error messages to the user when things don't
     *                       work.
     * @return The number from the text as a double
     */
    public static double parseNumber(String text, String sTag, String eTag, int minIdx, int maxIdx, int fileLineNo){
        double num = 0;
        String tempProp;
        Double tempPropD;
        int p = 0;
        int q = 0;
        
        text = " " + text + " ";
        maxIdx = maxIdx+2;
        
        try{    
            p = text.indexOf(sTag, minIdx);
            q = text.indexOf(eTag, p+sTag.length());
            
            if(maxIdx == text.length() && q == -1){
                q = maxIdx;
            }
            
            if( (p != -1 && q != -1) && (p >= minIdx && q <= maxIdx) ) {
                
                tempProp = text.substring(p+sTag.length(),q);
                tempPropD = new Double(tempProp);
                num = tempPropD.doubleValue();
            }else{
                num = Double.NaN;
            }
        }catch(NumberFormatException nfe){
            System.out.println("Error: Parsing Numerical field " + sTag + " in " + text + " from column " + minIdx + " to " +  maxIdx  + fileLineNo + " of the *.s4d file");  
            num = Double.NaN;
        }
        return num;
    }
    
    /**
     * This funciton will take an input array of vectors and use the directions 
     * of the vectors to calculate a colour for the vector based on its direction.
     * This is used for the force and torque displays, to make it possible to 
     * discern the direction of a vector when it is projected on a 2D screen.
     *  
     * @param a : An array of vectors. There is one row per vector, and it is
     *            expected that the columns are in X, Y and Z.
     * @return : The orientations of the vectors are mapped into the RBG colour 
     *           space such that the vector orientations map to:
     *              <li> X Pos : Red
     *              <li> X Neg : Green
     *              <li> Y Pos : Yellow
     *              <li> Y Neg : Magenta
     *              <li> Z Pos : Blue
     *              <li> Z Neg : Cyan
     *      I tried to place colours that people are typically colour blind to
     *      as far apart as possible, so that potentially confusing colours
     *      (eg Red and Green) would map to very different directions. Directions
     *      that are between these cardinal axis are interpolated.
     * 
     */
   public static double[][] getColourMapping(double[][] a){
        double[][] rgb = new double[a.length][3];
        double[] posBasis = new double[6];
        double mag = 0;
        double[][] rgbBasis = new double[6][3];
        
        //x pos : Red
        rgbBasis[0][0] = 1; 
        rgbBasis[0][1] = 0;
        rgbBasis[0][2] = 0;
        
        //y pos : yellow
        rgbBasis[1][0] = 1; 
        rgbBasis[1][1] = 1;
        rgbBasis[1][2] = 0;
        
        //z pos : Blue
        rgbBasis[2][0] = 0; 
        rgbBasis[2][1] = 0;
        rgbBasis[2][2] = 1;
        
        //x neg : green
        rgbBasis[3][0] = 0; 
        rgbBasis[3][1] = 1;
        rgbBasis[3][2] = 0;
        
        //y neg : magenta
        rgbBasis[4][0] = 1; 
        rgbBasis[4][1] = 0;
        rgbBasis[4][2] = 1;
        
        //z neg : cyan
        rgbBasis[5][0] = 0; 
        rgbBasis[5][1] = 1;
        rgbBasis[5][2] = 1;
        
        for(int i = 0; i < a.length; i++){
            mag = 0;
            for(int j = 0; j < 3; j++){
                if(a[i][j] > 0)
                        posBasis[j] = a[i][j];
                else posBasis[j+3] = Math.abs(a[i][j]);      
                mag = mag + posBasis[j]*posBasis[j] + posBasis[j+3]*posBasis[j+3];
            }
            if( mag > Double.MIN_VALUE*100){
                    mag = Math.sqrt(mag);
                    for(int j = 0; j < 6; j++)
                        posBasis[j] = posBasis[j]/mag;

                    for(int j = 0; j < 6; j++){
                        rgb[i][0] = rgb[i][0] + posBasis[j]*rgbBasis[j][0];
                        rgb[i][1] = rgb[i][1] + posBasis[j]*rgbBasis[j][1];
                        rgb[i][2] = rgb[i][2] + posBasis[j]*rgbBasis[j][2];
                    }

                    mag = 0;
                    for(int j=0; j<3; j++){
                        if(rgb[i][j] > mag)
                            mag = rgb[i][j];
                    }

                    for(int j=0; j<3; j++)
                        rgb[i][j] = rgb[i][j]/mag;                
            
            }else{
                rgb[i][0] = 0;
                rgb[i][1] = 0;
                rgb[i][2] = 0;
            }
        }
        
        return rgb;
    }     
    
/**
 * This function will take an array of rotation matricies and convert it into
 * an array of quaternions. There is one rotation matrix in R[][] per row, with 
 * 9 columns for each of the entries. The algorithm used is designed to work
 * even when the rotation matrices approach singularities. The algorithm was
 * taken from the following page 
 * <link>http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm<\link>
 * 
 * @param  R : An array of rotation matricies. There is one rotation matrix per
 *             row, and it is decomposed by rows:
 *              <li> R[i][0] = r0 c0
 *              <li> R[i][1] = r0 c1
 *              <li> R[i][2] = r0 c2
 *              <li> R[i][3] = r1 c0
 *              <li> R[i][4] = r1 c1
 *              <li> R[i][5] = r1 c2
 *              <li> R[i][6] = r2 c0
 *              <li> R[i][7] = r2 c1
 *              <li> R[i][8] = r2 c2
 * 
 * @return An array of quaternions, 1 per row, corresponding to the rotation
 *  matrix on the corresponding row. These quaternions are typicall converted into
 *  axis-angle format so they can be used to animate changing orientations in
 *  VRML/X3D.
 */   
public static double[][] convertToQuat(double[][] R){
        
        double t;

        double m00;
        double m01;
        double m02;

        double m10;
        double m11;
        double m12;

        double m20;
        double m21;
        double m22;

        
        double[][] q = new double[R.length][4];
        
        double S = 0;
        double qx = 0;
        double qy = 0;
        double qz = 0;
        double qw = 0;
        
        double scaling = 0;
        
        for(int i=0; i<R.length; i++){
        
         t = R[i][0]+R[i][4]+R[i][8]+1;

         m00 = R[i][0];
         m01 = R[i][1];
         m02 = R[i][2];

         m10 = R[i][3];
         m11 = R[i][4];
         m12 = R[i][5];

         m20 = R[i][6];
         m21 = R[i][7];
         m22 = R[i][8];    
            
        if(t > 0){
            S = 0.5 / Math.pow(t,0.5);
            qw = 0.25 / S;
            qx = ( m21 - m12 ) * S;
            qy = ( m02 - m20 ) * S;
            qz = ( m10 - m01 ) * S;
        }else if ((m00 > m11)&&(m00 > m22)){ 
           S = Math.pow(1.0 + m00 - m11 - m22, 0.5) * 2; 
           qw = (m12 - m21) / S;
           qx = 0.25 * S;
           qy = (m01 + m10) / S; 
           qz = (m02 + m20) / S; 
        } else if (m11 > m22){  
           S = Math.pow(1.0 + m11 - m00 - m22, 0.5) * 2; 
           qw = (m02 - m20) / S;
           qx = (m01 + m10) / S; 
           qy = 0.25 * S;
           qz = (m12 + m21) / S; 
        } else {  
           S = Math.pow(1.0 + m22 - m00 - m11, 0.5) * 2; 
           qw = (m01 - m10) / S;
           qx = (m02 + m20) / S; 
           qy = (m12 + m21) / S; 
           qz = 0.25 * S;
        }
        
         scaling = 1e-12 + (qx*qx + qy*qy + qz*qz);
         q[i][0] = qx/scaling;
         q[i][1] = qy/scaling;
         q[i][2] = qz/scaling;
         q[i][3] = 2.0d*Math.acos(qw);
        }
        return q;
        
    }
 /**
  * This function will calculate the angle between two vectors using the 
  * dot product property:
  * 
  * A dot B = |A||B|cos(AB)
  * 
  * This function is typically used to convert a quaternion into axis-angle
  * format to animate changes in orientation.
  * 
  * @param vArray   
  * @param vRef
  * @return double array of nx4 elements with the X,Y,Z axis unit vector in the first 3 elements and the angle in the 4th.
  */  
 public static double[][] getAxisAngle(double[][] vArray, double[] vRef){
        double[][] aa = new double[vArray.length][4];
        double[] temp = new double[3];
        double tempMag = 1;
        double tempAngle = 0;
        double vDotR = 0;
        double vMag = 0;
        double rMag = vRef[0]*vRef[0] + vRef[1]*vRef[1] + vRef[2]*vRef[2];
        rMag = Math.sqrt(rMag);
        
        for(int i = 0; i < vArray.length; i++){
            temp[0] = -(vArray[i][1]*vRef[2] - vArray[i][2]*vRef[1]);
            temp[1] = -(vArray[i][2]*vRef[0] - vArray[i][0]*vRef[2]);
            temp[2] = -(vArray[i][0]*vRef[1] - vArray[i][1]*vRef[0]);
            tempMag = temp[0]*temp[0] + temp[1]*temp[1] + temp[2]*temp[2];
            tempAngle = 0;
            
            vDotR = vArray[i][0]*vRef[0] + vArray[i][1]*vRef[1] + vArray[i][2]*vRef[2];
            
            vMag = vArray[i][0]*vArray[i][0] + vArray[i][1]*vArray[i][1] + vArray[i][2]*vArray[i][2];
            vMag = Math.sqrt(vMag);

            if(tempMag > Double.MIN_VALUE*100){
                tempMag = 1/Math.sqrt(tempMag);
                temp[0] = temp[0]*tempMag;
                temp[1] = temp[1]*tempMag;
                temp[2] = temp[2]*tempMag;
                tempAngle = Math.acos(vDotR/(vMag*rMag));
                
                aa[i][0] = temp[0];
                aa[i][1] = temp[1];
                aa[i][2] = temp[2];
                aa[i][3] = tempAngle;
                
            }else{
                aa[i][0] = 0;
                aa[i][1] = 0;
                aa[i][2] = 1;
                aa[i][3] = 0;
            }
            
        }
        
        return aa;
    
    }  
 
   /**
 * This function will take one array of x,y,z points, an array of 3D direction vectors
 * and an array containing vector magnitudes and calculate the 3 point
 * arrays needed to make a triangular mesh out of these points. Triangular meshes
 * are used quite often rather than quadralateral meshes because they are supported 
 * more widely in VRML/X3D viewers.
 * 
 * @param pos : The position of the first data axis through 3D space       
 * @param dir : The direction of the vector in X,Y,Z. This vector need not be normalized
   @param mag : The magnitudes of the vector in question.
 * @return double[][][] : The positions of the vertices of the triangles that 
    *                   will be used to render the 3D data plot. Here are the
 *                        dimensions:
 *                  <li>[1-n][][] : n = number of time steps
 *                  <li>[][1-6][] : the verticies of the 2 triangles that render 
 *                              the quadralateral between the point n in axis 1
 *                              and 2, and point n+1.
 *                  <li>[][][1-3] : The X,Y and Z coordinates of the data point
 */  
  public static double[][][] getTriangularArray(double[][] pos, double[][] dir, double[] mag){
        double[][][] triArray = new double[pos.length][6][3];
        double[][] prevPts = new double[2][3];
        double[][] currPts = new double[2][3];
        double[] currDir = new double[3];
        double currMag = 0;
        
        int i = 0; //timestep
        int j = 0; //vertices
        int k = 0; //x,y,z
        
            for(k=0; k<3; k++){
                prevPts[0][k] = pos[0][k];
                currMag = currMag + dir[0][k]*dir[0][k];
            }
            currMag = Math.sqrt(currMag);
            for(k=0; k<3; k++){
                prevPts[1][k] = prevPts[0][k] - dir[0][k]*mag[0]/(currMag+Double.MIN_VALUE);
            }
        
        for(i = 1; i < pos.length; i++){
            //calculate the current 2 points
            for(k=0; k<3; k++){
                currPts[0][k] = pos[i][k];
                currMag = currMag + dir[i][k]*dir[i][k];
            }
            currMag = Math.sqrt(currMag);
            if(currMag == Double.NaN)
                currMag = 0;
            
            for(k=0; k<3; k++){
                currPts[1][k] = currPts[0][k] - dir[i][k]*mag[i]/(currMag+Double.MIN_VALUE);
            }    
        
            //assign the 6 vertices, chosen such that the triangles will
            //all have the same normals.
            for(k=0; k<3; k++){
                if(i == 1){
                    triArray[0][0][k] = prevPts[0][k]; 
                    triArray[0][1][k] = prevPts[1][k];
                    triArray[0][2][k] = currPts[1][k];
                    triArray[0][3][k] = prevPts[0][k];
                    triArray[0][4][k] = currPts[1][k];
                    triArray[0][5][k] = currPts[0][k];
                }
                    triArray[i][0][k] = prevPts[0][k]; 
                    triArray[i][1][k] = prevPts[1][k];
                    triArray[i][2][k] = currPts[1][k];
                    triArray[i][3][k] = prevPts[0][k];
                    triArray[i][4][k] = currPts[1][k];
                    triArray[i][5][k] = currPts[0][k];
                
               
            }
            for(k=0; k<3; k++){
                prevPts[0][k]=currPts[0][k];
                prevPts[1][k]=currPts[1][k];
            }
                
            //set the prev point to be the current point
        }
            
        return triArray;    
        
    }
    
    
 /**
 * This function will take two arrays of x,y,z points and calculate the 3 point
 * arrays needed to make a triangular mesh out of these points. Triangular meshes
 * are used quite often rather than quadralateral meshes because they are supported 
 * more widely in VRML/X3D viewers.
 * 
 * @param axis1 : The position of the first data axis through 3D space
 *                  <li> Column 1,2,3: X, Y, Z
 *                  <li> Rows: 1-n (n= number of timesteps)
 * @param axis2 : The position of the second data axis
 * @return double[][][] : The positions of the vertices of the triangles that will
 *                        will be used to render the 3D data plot. Here are the
 *                        dimensions: 
 *                  <li>[1-n][][] : n = number of time steps
 *                  <li>[][1-6][] : the verticies of the 2 triangles that render 
 *                              the quadralateral between the point n in axis 
 *                              and 2, and point n+1.
 *                  <li>[][][1-3] : The X,Y and Z coordinates of the data point
 */
public static double[][][] getTriangularArray(double[][] axis1, double[][] axis2 ){
double[][][] triArray = new double[axis1.length][6][3];
double[][] prevPts = new double[2][3];
double[][] currPts = new double[2][3];

int i = 0; //timestep
int j = 0; //vertices
int k = 0; //x,y,z

    for(k=0; k<3; k++){
        prevPts[0][k] = axis1[0][k];
        prevPts[1][k] = axis2[0][k];
    }

for(i = 1; i < axis1.length; i++){
    //calculate the current 2 points
    for(k=0; k<3; k++){
        currPts[0][k] = axis1[i][k];
        currPts[1][k] = axis2[i][k];
    }    

    //assign the 6 vertices, chosen such that the triangles will
    //all have the same normals.
    for(k=0; k<3; k++){
            if(i==1){
                triArray[0][0][k] = prevPts[0][k]; 
                triArray[0][1][k] = prevPts[1][k];
                triArray[0][2][k] = currPts[1][k];
                triArray[0][3][k] = prevPts[0][k];
                triArray[0][4][k] = currPts[1][k];
                triArray[0][5][k] = currPts[0][k];            
            }
            
            triArray[i][0][k] = prevPts[0][k]; 
            triArray[i][1][k] = prevPts[1][k];
            triArray[i][2][k] = currPts[1][k];
            triArray[i][3][k] = prevPts[0][k];
            triArray[i][4][k] = currPts[1][k];
            triArray[i][5][k] = currPts[0][k];

    }
    for(k=0; k<3; k++){
        prevPts[0][k]=currPts[0][k];
        prevPts[1][k]=currPts[1][k];
    }

    //set the prev point to be the current point
}

return triArray;    

}

/* 
  * This is a function that will search through a read in text file
  * in the body[] array, and replace instances of the entries in tags[] with 
  * the corresponding string in the data[] array.
  * 
  * @param body     : The text body to search though 
  * @param tag      : tags to search for in the body text
  * @param data     : the data to switch for tags in the body text
  * @param multReplace  : If an a particular array element is in multReplace
  * @return         : The the modified body text.
  */   
    private static String[] replaceTags(String[] body, String[] tag, String[] data){
        int s=0;
        int e=0;
        int prevStart = 0;
        String temp = null;
        for(int i = 0; i<body.length; i++){
             for(int k = 0; k < tag.length; k++){
                 temp = body[i];
                if(temp.indexOf(tag[k]) != -1){
                    //replace all instance of the tag in this line with the corresponding data
                    prevStart = temp.indexOf(tag[k], 0);
                    s = -1;
                    while(prevStart  != -1){
                        s = body[i].indexOf(tag[k],s+1);
                        body[i] = body[i].substring(0, s) + data[k] + body[i].substring(s+tag[k].length(), body[i].length());
                        prevStart = temp.indexOf(tag[k], prevStart+1);
                        
                    }
                }
             }   
        }
        return body;
        
    } 
  
 /**
  * This function will append the file header, the time sensor and the background
  * colour tag for the animation. This function also has a prototype of how all
  * other append functions might be updated so that they do not write the syntax
  * of the VRML/X3D file directly, but rather load library functions that contain
  * key symbols and then replace the key symbols with the appropriate data. Working
  * in this manner will make it far easier to update this program so that it 
  * outputs the X3D file format as well.
  * 
  * @param curBuf   : The current file buffer to which the header should be applied
  * @param maxTime  : The maximum time of the simulation in seconds.
  * @param rgb      : The background colour of the animation
  * @return         : The file buffer with the header appended to it.
  */   
    public static StringBuffer appendHeader(StringBuffer curBuf, double maxTime,double[] rgb, double[] lightDir, boolean headlight){
    
        
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_header.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[5];
        tag[0] = "$TIME$";
        tag[1] = "$RGB$";
        tag[2] = "$DL_DIRECTION$";
        tag[3] = "$DL_ON$";
        tag[4] = "$HEADLIGHT$";
        
        
        String[] data = new String[5];
        data[0] = Double.toString(maxTime);
        data[1] = Double.toString(rgb[0]) + " "+ Double.toString(rgb[1]) + " " + Double.toString(rgb[2]);
        
        if(lightDir == null){
            data[2] = "-1 -1 -1";
            data[3] = "FALSE";
        }else{
            data[2] = Double.toString(lightDir[0]) + " "+ Double.toString(lightDir[1]) + " " + Double.toString(lightDir[2]);
            data[3] = "TRUE";
        }
        
        if(headlight){
            data[4] = "TRUE";
        }else{
            data[4] = "FALSE";
        }
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        
        return curBuf;
                
    }
    
 
    /**
     * This function will append the route statements required to "wire" an
     * orientation interpolator to set the rotation of a body. The body and the 
     * orientation interpolator must use the same, identical, tagName.
     * 
     * @param curBuf   : The current file buffer to which the header should be applied
     * @param tagName  : The nameTag for the element that the rotation is being applied to
     * @return         : The file buffer with the Route Rotation statement appended to it. 
     */
    public static StringBuffer appendRouteRotation(StringBuffer curBuf, String tagName){
        
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_routerotation.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[1];
        tag[0] = "$TAG$";
        String[] data = new String[1];
        data[0] = tagName;
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        
        return curBuf;
    }
    
    /**
     * This function will append the route statements required to "wire" a
     * translation interpolator to set the motion of a body. The body and the 
     * translation interpolator must use the same, identical, tagName.
     *  
     * @param curBuf   : The current file buffer to which the header should be applied
     * @param tagName  : The nameTag for the element that the translation is being applied to
     * @return         : The file buffer with the Route Translation statement appended to it. 
     */
    public static StringBuffer appendRouteTranslation(StringBuffer curBuf, String tagName){       
        
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_routetranslation.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[1];
        tag[0] = "$TAG$";
        String[] data = new String[1];
        data[0] = tagName;
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        
       

        return curBuf;
    }
    /**
     * This function will append the route statements required to "wire" a
     * scale interpolator to set the motion of a body. The body and the 
     * translation interpolator must use the same, identical, tagName.
     *  
     * @param curBuf   : The current file buffer to which the header should be applied
     * @param tagName  : The nameTag for the element that the translation is being applied to
     * @return         : The file buffer with the Route Scale statement appended to it. 
     */
    public static StringBuffer appendRouteScale(StringBuffer curBuf, String tagName){       
        
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_routescale.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[1];
        tag[0] = "$TAG$";
        String[] data = new String[1];
        data[0] = tagName;
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }

        return curBuf;
    }
    
    /**
     * This function will append the route statements required to "wire" a
     * colour interpolator to set the colour of a body. The body and the 
     * translation interpolator must use the same, identical, tagName.
     *  
     * @param curBuf   : The current file buffer to which the header should be applied
     * @param tagName  : The nameTag for the element that the translation is being applied to
     * @return         : The file buffer with the Route Colour statement appended to it. 
     */
    public static StringBuffer appendRouteColour(StringBuffer curBuf, String tagName){

        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_routecolour.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[1];
        tag[0] = "$TAG$";
        String[] data = new String[1];
        data[0] = tagName;
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }

         return curBuf;
    }
    
    /**
     * This function will append a colourInterpolator to the current StringBuffer. This 
     * interpolator should have the same "tagName" as that given to the function
     * routeColourInterpolator, and also the the material of the body in question.
     * 
     * @param curBuf        : The current file buffer to which the header should be applied
     * @param tagName       : The nameTag for the element that the colour change is being applied to
     * @param key           : Normalized time from 0 to 1
     * @param keyValue      : The R,G,B values corresponding to the times in key. One "R G B" entry per index.
     * @param downSample    : The amount of downsampling to perform.
     * @return              : The file buffer with the ColourTag appended to it. 
     */
    public static StringBuffer appendColourTag(StringBuffer curBuf, String tagName, String[] key, String[] keyValue, int downSample){
        

        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_colourinterpolator.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[3];
        tag[0] = "$TAG$";
        tag[1] = "$KEY$";
        tag[2] = "$KEYVALUE$";
        String[] data = new String[3];
        data[0] = tagName;
        data[1]= key[0];
        data[2]= keyValue[0];
        
        for(int i=1; i<key.length; i++){           
            if(i%downSample == 0){
                data[1] = data[1] + " " + key[i];
                data[2] = data[2] + ", " + keyValue[i];
            }
        }
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        
        return curBuf;
       
    }
    
    /**
     * This function will append a scale tag to the current StringBuffer. The
     * "tagName" should be indentical to that of the body which will be scaled.
     * 
     * 
     * @param curBuf        : The current file buffer to which the header should be applied
     * @param tagName       : The nameTag for the element that the colour change is being applied to
     * @param key           : Normalized time from 0 to 1
     * @param keyValue      : The X, Y, Z scale values corresponding to the times in key. One "X Y Z"  entry per index
     * @param downSample    : The amount of downsampling to perform.
     * @return              : The file buffer with the ScaleTag appended to it. 
     */ 
    public static StringBuffer appendScaleTag(StringBuffer curBuf, String tagName, String[] key, String[] keyValue, int downSample){
            
        
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_scaleinterpolator.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[3];
        tag[0] = "$TAG$";
        tag[1] = "$KEY$";
        tag[2] = "$KEYVALUE$";
        String[] data = new String[3];
        data[0] = tagName;
        data[1]= key[0];
        data[2]= keyValue[0];
        
        for(int i=1; i<key.length; i++){           
            if(i%downSample == 0){
                data[1] = data[1] + " " + key[i];
                data[2] = data[2] + ", " + keyValue[i];
            }
        }
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        
      
        return curBuf;
    }
    
    /** 
     * This function will append 1 static marker to the current StringBuffer of
     * one of 4 pre-defined shapes: sphere, cylinder, cone or a box. The user
     * can select the size, location, colour of the marker and its transparency.
     * The orientation of the marker cannot currently be set, though this would
     * be a simple upgrade if you're interested in doing it.
     * 
     * @param curBuf    : The current file buffer to which the header should be applied
     * @param mkrShape  : 0,1,2,3 : Sphere, Cylinder, Box, Cone
     * @param mkrPos    : The X,Y,Z position of the marker. 
     * @param mkrRGB    : The R,G,B value of the colour of the marker
     * @param mkrGeo    : The geometrical properties of the marker
     *      <li> Sphere     : 1 property: radius (m)
     *      <li> Cylinder   : 2 properties: radius (m), height (m)
     *      <li> Box        : 3 properties: X size, Y size, Z size all in {m)   
     *      <li> Cone       : 3 properties: radius (m), height (m)   
  
     * @param mkrTrans  : The transparency of the marker
     * @return  : The file buffer with the new static marker attached to it. 
     */ 
    public static StringBuffer appendStaticMarker(StringBuffer curBuf, int mkrShape, float[] mkrPos, float[] mkrRGB, float[] mkrGeo, float mkrTrans){
                   
       
        String pos = new String();
        String rgb = new String();
        String trans = new String();
        String geo[] = new String[mkrGeo.length];
        
        trans = Double.toString((double)mkrTrans);
        for(int i=0; i<3; i++){
            pos = pos + " " + Double.toString((double)mkrPos[i]);
            rgb = rgb + " " + Double.toString((double)mkrRGB[i]);
        }
        for(int i=0; i<geo.length; i++){
            geo[i] = Double.toString((double)mkrGeo[i]);
        }
        
        File libfile = null; //new File("../../SolvereLibs/WRL_SYNTAX/lib_scaleinterpolator.wrl");
        String[] tag = null;
        String[] data = null;


switch (mkrShape){
        case 0: //Sphere
            libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_sphere.wrl");
            tag = new String[4];
            data = new String[4];
            tag[0] = "$TRANSLATION$";
            tag[1] = "$RGB$";
            tag[2] = "$TRANSPARENCY$";
            tag[3] = "$RADIUS$";
            data[0] = pos;
            data[1] = rgb;
            data[2] = trans;
            data[3] = geo[0];
            
            break;
        case 1: //Cylinder
            libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_cylinder.wrl");
            tag = new String[5];
            data = new String[5];
            tag[0] = "$TRANSLATION$";
            tag[1] = "$RGB$";
            tag[2] = "$TRANSPARENCY$";
            tag[3] = "$RADIUS$";
            tag[4] = "$HEIGHT$";
            data[0] = pos;
            data[1] = rgb;
            data[2] = trans;
            data[3] = geo[0];
            data[4] = geo[1];
            
            break;
        case 2: //Box
            libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_box.wrl");
            tag = new String[4];
            data = new String[4];
            tag[0] = "$TRANSLATION$";
            tag[1] = "$RGB$";
            tag[2] = "$TRANSPARENCY$";
            tag[3] = "$SIZE$";
            data[0] = pos;
            data[1] = rgb;
            data[2] = trans;
            data[3] = geo[0] + " " + geo[1] + " " + geo[2];
            
            break;
        case 3: //Cone
            libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_cone.wrl");
            tag = new String[5];
            data = new String[5];
            tag[0] = "$TRANSLATION$";
            tag[1] = "$RGB$";
            tag[2] = "$TRANSPARENCY$";
            tag[3] = "$RADIUS$";
            tag[4] = "$HEIGHT$";
            data[0] = pos;
            data[1] = rgb;
            data[2] = trans;
            data[3] = geo[0];
            data[4] = geo[1];
            break;
        default: //Notta

            break;
            }  
        String[] libString = getTextFile(libfile);
         
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        
            return curBuf;
    }
    
    /**
     * This function will append a translation tag to the current StringBuffer. The
     * "tagName" should be indentical to that of the body which will be moved.
     * 
     * 
     * @param curBuf        : The current file buffer to which the header should be applied
     * @param tagName       : The nameTag for the element that the colour change is being applied to
     * @param key           : Normalized time from 0 to 1
     * @param keyValue      : The X,Y,Z values corresponding to the times in key. One "X Y Z" entry per index.
     * @param downSample    : The amount of downsampling to perform.
     * @return              : The file buffer with the Translation Tag appended to it. 
     */ 
    public static  StringBuffer appendTranslationTag(StringBuffer curBuf, String tagName, String[] key, String[] keyValue, int downSample){
                    
        //Write the translation interpolation file
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_translationinterpolator.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[3];
        tag[0] = "$TAG$";
        tag[1] = "$KEY$";
        tag[2] = "$KEYVALUE$";
        String[] data = new String[3];
        data[0] = tagName;
        data[1]= key[0];
        data[2]= keyValue[0];
        
        for(int i=1; i<key.length; i++){           
            if(i%downSample == 0){
                data[1] = data[1] + " " + key[i];
                data[2] = data[2] + ", " + keyValue[i];
            }
        }
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
        return curBuf;
    }

    
/**
 * This function will append a orientation tag to the current StringBuffer. The
 * "tagName" should be indentical to that of the body which will be moved.
 * 
 * 
 * @param curBuf        : The current file buffer to which the header should be applied
 * @param tagName       : The nameTag for the element that the colour change is being applied to
 * @param key           : Normalized time from 0 to 1
 * @param keyValue      : The orientation axis-angle values corresponding to the times in key. One "X Y Z A" entry per index.
 * @param downSample    : The amount of downsampling to perform.
 * @return              : The file buffer with the Translation Tag appended to it. 
 */ 
public static  StringBuffer appendOrientationTag(StringBuffer curBuf, String tagName, String[] key, String[] keyValue, int downSample){
    
        //Write the orientation interpolation file          
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_rotationinterpolator.wrl");
        String[] libString = getTextFile(libfile);
        
        String[] tag = new String[3];
        tag[0] = "$TAG$";
        tag[1] = "$KEY$";
        tag[2] = "$KEYVALUE$";
        String[] data = new String[3];
        data[0] = tagName;
        data[1]= key[0];
        data[2]= keyValue[0];
        
        for(int i=1; i<key.length; i++){           
            if(i%downSample == 0){
                data[1] = data[1] + " " + key[i];
                data[2] = data[2] + ", " + keyValue[i];
            }
        }
        
        String[] element = replaceTags(libString, tag, data);
        
        for(int i=0; i< element.length; i++){
            curBuf.append(element[i]);
            curBuf.append('\n');
        }
    
                    
                    return curBuf;
    }


/**
 * This function will display a plot specified by the user in the form of a triangular
 * array that form a datum axis and a data axis, along with a number of display
 * options. The user can render the plot in a solid colour, with a different colour
 * for every point, in wire frame, as a solid, with a user-settable transparency,
 * with a data label and with a marker that follows the current time in the plot.
 * 
 * 
 * @param  curBuf   :   The current string buffer
 * @param tagName   :   The tag name used to uniquely identify the plot in the script
 * @param label     :   The text of the label to display at the first data point of the datum axis
 * @param labelOptions  : The scale (index 0) and display colour (in "R G B" at index 1) of the label
 * @param addMarker :   If this is true, a marker will be added that follows the data with time
 * @param markerOptions : The colour of the marker (in "R G B" in index 0), and its radius (index 1)
 * @param markerTime    : A normalized time vector (0-1) with the same number of elements as the markerPos array
 * @param markerPos     : An array of "X Y Z" positions for the marker.
 * @param downSample    : The level of downsampling to perform on the marker animation data.
 * @param triPlotGeo    : The triangular array that describes the plotted surface 
 * @param vertexColour  : The colour to plot the array in. If one value "R G B" is 
 *                        passed in, the entire plot will be in this colour. If one
 *                        "R G B" value per vertex (same length as triPlotGeo) is
 *                        passed in, every index will have its own colour applied to it
 * @param transparency  : The level of transparency for this surface
 * @param wireFrame     : If this is true a wire frame of the plot is rendered, else a solid is rendered
 * @return      : The current string buffer with the necessary commands to display the 3D plot
 */
public static  StringBuffer appendPlot(StringBuffer curBuf, String tagName, String label, String[] labelOptions,
        boolean addMarker, String[] markerOptions, String[] markerTime, String[] markerPos, int downSample,
        String[] triPlotGeo, String[] vertexColour, String transparency, boolean wireFrame){
    
        if( addMarker == true){
                String mkrTag = tagName + "_MKR";
                File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_sphere.wrl");
                String[] libString = getTextFile(libfile);

                String[] tag = new String[5];
                tag[0] = "Transform {";
                tag[1] = "$TRANSLATION$";
                tag[2] = "$RGB$";
                tag[3] = "$TRANSPARENCY$";
                tag[4] = "$RADIUS$";

                String[] data = new String[5];
                data[0] = "DEF " + mkrTag + " Transform {";
                data[1] = "0 0 0";
                data[2] = markerOptions[1];
                data[3] = "0.75";
                data[4] = markerOptions[0];

                String[] element = replaceTags(libString, tag, data);

                for(int i=0; i< element.length; i++){
                    curBuf.append(element[i]);
                    curBuf.append('\n');
                }

                appendTranslationTag(curBuf, mkrTag, markerTime, markerPos, downSample);
                appendRouteTranslation(curBuf, mkrTag);    
        }
    
   
        if( label != null){
            
                File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_label.wrl");
                String[] libString = getTextFile(libfile);

                String[] tag = new String[7];
                tag[0] = "$TRANSLATION$";
                tag[1] = "$SCALE$";
                tag[2] = "$RGB$";
                tag[3] = "$TXT_LENGTH$";
                tag[4] = "$TXT_MAXEXTENT$";
                tag[5] = "$TXT_TEXT$";
                tag[6] = "DEF $TAG$";
                
                String[] data = new String[7];
                data[0] = triPlotGeo[0];
                data[1] = labelOptions[0] + " " + labelOptions[0] + " " + labelOptions[0];
                data[2] = labelOptions[1];
                data[3] = Integer.toString((int)Math.floor(label.length()/2));
                data[4] = Integer.toString((int)Math.floor(label.length()/2));
                data[5] = label;
                data[6] = "";
                
                String[] element = replaceTags(libString, tag, data);

                for(int i=0; i< element.length; i++){
                    curBuf.append(element[i]);
                    curBuf.append('\n');
                }
                    
        }
        
        String[] plotTag = new String[1];
        String[] tag = null;
        String[] data = null;
        StringBuffer tempStrBuf = new StringBuffer();
        
        if(vertexColour.length == 1){
                File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_shape.wrl");
                String[] libString = getTextFile(libfile);
                
                tag = new String[3];
                tag[0] = "$TAG$";
                tag[1] = "$RGB$";
                tag[2] = "$TRANSPARENCY$";

                data = new String[3];
                data[0] = tagName;
                data[1] = vertexColour[0];
                data[2] = transparency;
                
                plotTag = replaceTags(libString, tag, data);
                
                
            if(wireFrame == true){
                File linefile = new File("../../SolvereLibs/WRL_SYNTAX/lib_lineset.wrl");
                String[] lineString = getTextFile(linefile);
                plotTag = mergeStrings(plotTag,"$GEO$",lineString);
            }else{
                File trifile = new File("../../SolvereLibs/WRL_SYNTAX/lib_triangleset.wrl");
                String[] triString = getTextFile(trifile);
                plotTag = mergeStrings(plotTag,"$GEO$",triString);
            }
                
        }else{
            
            if(wireFrame == true){
                    File linefile = new File("../../SolvereLibs/WRL_SYNTAX/lib_lineset.wrl");
                    plotTag = getTextFile(linefile);

                }else{
                    File trifile = new File("../../SolvereLibs/WRL_SYNTAX/lib_triangleset.wrl");
                    plotTag = getTextFile(trifile);
                }
        }
      
        if(wireFrame == true){   
                tag = new String[3];
                data = new String[3];

                tag[0] = "$TAG$";
                tag[1] = "$VERTEXCOUNT$";
                tag[2] = "$COORDINATE$";

                data[0] = tagName;
                    for(int z = 0; z < triPlotGeo.length/3; z++)
                        tempStrBuf.append(" 3");
                data[1] = tempStrBuf.toString();
                    tempStrBuf.replace(0, tempStrBuf.length(), "");
                    for(int z = 0; z < triPlotGeo.length-1; z++){
                        tempStrBuf.append(triPlotGeo[z]);
                        tempStrBuf.append(",");
                    }
                    tempStrBuf.append(triPlotGeo[triPlotGeo.length-1]);
                data[2] = tempStrBuf.toString();
                plotTag = replaceTags(plotTag, tag, data);
                    
         }else{   
                tag = new String[4];
                data = new String[4];

                tag[0] = "$TAG$";
                tag[1] = "$CCW$";
                tag[2] = "$SOLID$";
                tag[3] = "$COORDINATE$";

                data[0] = tagName;    
                data[1] = "TRUE";
                data[2] = "FALSE";

                    tempStrBuf.replace(0, tempStrBuf.length(), "");
                    for(int z = 0; z < triPlotGeo.length-1; z++){
                        tempStrBuf.append(triPlotGeo[z]);
                        tempStrBuf.append(",");
                    }
                    tempStrBuf.append(triPlotGeo[triPlotGeo.length-1]);
                data[3] = tempStrBuf.toString();
                plotTag = replaceTags(plotTag, tag, data);
         }
        
         if(vertexColour.length > 1){
            tag = new String[2];
            data = new String[2];
            tag[0] = "#";
            tag[1] = "$COLOR$";
            
            data[0] = "";
            tempStrBuf.replace(0, tempStrBuf.length(), "");
            for(int i = 0; i < vertexColour.length-1; i++){
                tempStrBuf.append(vertexColour[i]);
                tempStrBuf.append(",");
            }
                tempStrBuf.append(vertexColour[vertexColour.length-1]);
            data[1] = tempStrBuf.toString();
            plotTag = replaceTags(plotTag, tag, data);
         }
        
        for(int z = 0; z < plotTag.length; z++){
            curBuf.append(plotTag[z]);
            curBuf.append('\n');
        }
       
        return curBuf;
}

/**
 * This function will render a series of static stick figures on the screen to
 * allow the viewer to see an entire kinematic motion history all at once.
 * 
 * @param curBuf        : The current file buffer
 * @param tagName       : The unique string name that identifies this element
 * @param sfRGB         : The single colour "R G B" used to render the stick figures
 * @param sfData        : The "X1 Y1 Z1, X2 Y2 Z2, ... ,XN YN ZN" data that specifies
 *                        the coordinates of each stick figure. One stick figure
 *                        per index.
 * @param sfVertexCnt   : The number of points in space for each stick figure
 */
public static  StringBuffer appendStickFigure(StringBuffer curBuf, String tagName, String sfRGB, String[] sfData, String[] sfVertexCnt){
    
        String[] sfTag = new String[1];
        String[] tag = null;
        String[] data = null;
        StringBuffer tempStrBuf = new StringBuffer();
    
        File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_shape.wrl");
        String[] libString = getTextFile(libfile);

        tag = new String[3];
        tag[0] = "$TAG$";
        tag[1] = "$RGB$";
        tag[2] = "$TRANSPARENCY$";

        data = new String[3];
        data[0] = tagName;
        data[1] = sfRGB;
        data[2] = "0";

        sfTag = replaceTags(libString, tag, data);
    
        File linefile = new File("../../SolvereLibs/WRL_SYNTAX/lib_lineset.wrl");
        String[] lineString = getTextFile(linefile);
        sfTag = mergeStrings(sfTag,"$GEO$",lineString);
        
        tag = new String[3];
                data = new String[3];

        tag[0] = "$TAG$";
        tag[1] = "$VERTEXCOUNT$";
        tag[2] = "$COORDINATE$";

        data[0] = tagName;
            for(int z = 0; z < sfVertexCnt.length; z++){
                tempStrBuf.append(sfVertexCnt[z]);
                tempStrBuf.append(" ");
            }
        data[1] = tempStrBuf.toString();
            tempStrBuf.replace(0, tempStrBuf.length(), "");
            for(int z = 0; z < sfData.length-1; z++){
                tempStrBuf.append(sfData[z]);
                tempStrBuf.append(",");
            }
            tempStrBuf.append(sfData[sfData.length-1]);
        data[2] = tempStrBuf.toString();
        sfTag = replaceTags(sfTag, tag, data);
        
        for(int z=0; z<sfTag.length; z++){
            curBuf.append(sfTag[z]);
            curBuf.append('\n');
        }
        
    return curBuf;
}
/**
 * This function will append the script necessary to create a label that displays
 * text, of colour, and of a size of the users choosing, through a path they command.
 * The text is placed on a billboard such that it will always face the user no
 * matter where they are. 
 * 
 * @param curBuf    : The current file buffer
 * @param tagName   : The unique string name that identifies this element
 * @param text      : The text to display to the user.
 * @param scaling   : The scale of the text - the default of 1 makes the letters 1m tall.
 * @param rgb       : The desired colour of the text
 * @param labelTime : The normalized time array required to animate the text
 * @param labelPos  : The an array of the labels X,Y,Z postion in meters
 * @param downSample : The level of downsampling to use; 1= no downsampling, 2 means take every 2nd pt.
 * @return The current buffer with the script appended to it to animate a moving label 
 */
public static  StringBuffer appendMovingLabel(StringBuffer curBuf, String tagName, String text,
        double scaling, String rgb, String[] labelTime, String[] labelPos, int downSample){
    
                File libfile = new File("../../SolvereLibs/WRL_SYNTAX/lib_label.wrl");
                String[] libString = getTextFile(libfile);

                String scaleStr = new String(Double.toString(scaling));
                
                String[] tag = new String[7];
                tag[0] = "$TRANSLATION$";
                tag[1] = "$SCALE$";
                tag[2] = "$RGB$";
                tag[3] = "$TXT_LENGTH$";
                tag[4] = "$TXT_MAXEXTENT$";
                tag[5] = "$TXT_TEXT$";
                tag[6] = "$TAG$";
                
                String[] data = new String[7];
                data[0] = "0 0 0";
                data[1] = scaleStr + " " + scaleStr + " " + scaleStr;
                data[2] = rgb;
                data[3] = Integer.toString((int)Math.floor(text.length()/2));
                data[4] = Integer.toString((int)Math.floor(text.length()/2));
                data[5] = text;
                data[6] = tagName;
                
                String[] element = replaceTags(libString, tag, data);
    
                for(int z=0; z < element.length; z++){
                    curBuf.append(element[z]);
                    curBuf.append('\n');
                }
                
                appendTranslationTag(curBuf, tagName, labelTime, labelPos, downSample);
                appendRouteTranslation(curBuf, tagName);
 
    return curBuf;
}

/**
 * This function will append the required shapes, text and custom scripts required
 * to create the time controls. For VRML (*.wrl) files this text is stored in 
 * lib_timecontrols.wrl.
 * 
 * @param curBuf    : The current StringBuffer
 * @param aniTime   : The time array for this animation
 * @param timeScaling : The amount the simulation time play time should be increased
 *                     or decreased. 
 */
public static  StringBuffer appendTimeControls(StringBuffer curBuf, double[] aniTime, double timeScaling){
    
    File timeControlFile = new File("../../SolvereLibs/WRL_SYNTAX/lib_timecontrol.wrl");
    String[] timeControls = getTextFile(timeControlFile);
    double maxTime = aniTime[aniTime.length-1];//*timeScaling;
    String cycleTime = new String("");
    cycleTime = Double.toString(maxTime);
    int t1 = 0;
    String tstr = new String(""); 
    String pstr = new String("");
    
    for(int i = 0; i< timeControls.length; i++){
        if( timeControls[i].indexOf("$1$") != -1){
            t1 = timeControls[i].indexOf("$1$");
            tstr = timeControls[i].substring(0,t1);
            pstr = timeControls[i].substring(t1+3,timeControls[i].length());
            tstr = tstr + cycleTime;
            curBuf.append(tstr);
            curBuf.append(pstr);
            curBuf.append('\n');
        }else{
            curBuf.append(timeControls[i]);
            curBuf.append('\n');
        }
    }
    
    return curBuf;
    //Replace default_cycleTime with the correct value
    
}
/**
 * This function will read in a text file into a String array with one line of
 * text for each array index.
 * 
 * @param textFile : The file associated with the text file to open
 */
  public static String[] getTextFile(File textFile){
       
        String[] fileStr = null;
        int defaultRowNum = 1000;
        
        
        if(textFile != null){
    
            try{
                    FileReader fileFRdr = new FileReader(textFile.getAbsolutePath());
                    BufferedReader fileBufRdr = new BufferedReader(fileFRdr);
                    
                    
                    fileStr = new String[defaultRowNum];
                    int rows = 0;
                    String line = new String(fileBufRdr.readLine());
                   
                    
                    while(line != null){
                        
                        if(line != null){
                            fileStr[rows] = line;
                            rows++;
                        }

                        if( rows%(defaultRowNum-1) == 0){
                            //resize dataString without losing data;
                            int scaleFactor = 2; //1 + (int)Math.floor((double)rows/(double)(defaultRowNum-2));
                            String[] tempDataHolder = new String[scaleFactor*rows];
                            for(int i = 0; i < rows; i++)
                                    tempDataHolder[i]= fileStr[i];

                             fileStr = tempDataHolder;
                        }
                        line = fileBufRdr.readLine();
                        
                    }

                    String[] tempDataHolder = new String[rows];
                    for(int i = 0; i < rows; i++)
                            tempDataHolder[i] = fileStr[i];

                    fileStr = tempDataHolder;

                    fileFRdr.close();
                    fileBufRdr.close();
                    
            }catch(FileNotFoundException fnfe){
                System.out.println(textFile.getAbsolutePath() + " File Not Found");
                fnfe.printStackTrace();
            }catch(IOException ioe){
                System.out.println("IO exception thrown while trying to read " + textFile.getAbsolutePath() );
                ioe.printStackTrace();
            }   
        }
        return fileStr;
    }

  /**
   * This function will take a string array in str1, and it will insert the 
   * string array in str2 into it, with the first line of str2 being inserted
   * where the str1Tag is located in str1.
   * 
   * @param str1    : String array 1
   * @param str1Tag : The tag in str1 that marks where str2 should be inserted
   * @param str2    : String array 2
   */
  private static String[] mergeStrings(String[] str1, String str1Tag, String[] str2){
        String[] finalStr = new String[str1.length + str2.length - 1];
        int fCtr = 0;
        int s = 0;
        for(int i = 0; i< str1.length; i++){
            s = str1[i].indexOf(str1Tag); 
            if(s != -1){
                finalStr[fCtr] = str1[i].substring(0, s) + " " + str2[0];
                fCtr++;
                for(int j=1; j<str2.length;j++){
                    finalStr[fCtr] = str2[j];
                    fCtr++;
                }
                
            }else{
                finalStr[fCtr] = str1[i];
                fCtr++;
            }
            
        }
        
        return finalStr;
  }
    
}
