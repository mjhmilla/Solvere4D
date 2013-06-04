/*
 * Copyright Matthew J.H. Millard 2008
 * 
 * StickFigure.java is part of Solvere4D.

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
import java.lang.Integer;
/**
 * This class acts as a data structure to hold all of the deta required to 
 * create a stick figure map in VRML or X3D. It will also put the data into
 * text format so that it can be easily written to file.
 * 
 * @author mjhmilla
 */
public class StickFigure {

    private String stickTag;
    private String[] stickTime;
    private String[] stickDataStr;
    private String[] stickVertexCount;
    private String stickRGB;
    
    private int pts;
    /**
     * @param tag : the String that acts as a unique identifier in 
     *              the VRML/X3D script
     * @param data : An array of time and position data. Time is always in
     *              column 1, and after that you have 3 columns of X,Y,Z points
     *              for every vertex in the stick figure map. <br>
     *      Column 1: Time (in seconds)<br>
     *      Columns 2,3,4: X,Y,Z coordinates for the first vertex in the stick figure<br>
     *      Columns 5,6,7: " " for the second vertex in the stick figure <br>
     *      .<br>
     *      .<br>
     *      .<br>
     *      Columns n,n+1,n+2: "" for the nth vertex in the stick figure <br>
     * 
     * @param rgb : The colour of the sticks specified in R,G and B between 0 and 1 
     */
public StickFigure(String tag, double[][] data, double[] rgb){
    stickTag = tag;
    StringBuffer temp = new StringBuffer();
    pts = (int)Math.floor( (data[0].length-1)/3);
    stickDataStr = new String[data.length*pts];
    stickVertexCount = new String[data.length];
    stickTime = new String[data.length];
    
    temp.replace(0, temp.length(), "");
    for(int i=0; i<3; i++){
        temp.append(rgb[i]);
        if(i<2) temp.append(" ");
    }
    
    stickRGB = temp.toString();
        
    
    for(int i=0; i<data.length; i++){
        temp.replace(0, temp.length(), "");
        stickTime[i] = Double.toString(data[i][0]);
        
        for(int j=0; j<pts; j++){
            temp.replace(0, temp.length(), "");
            for(int k=0; k<3; k++){
                temp.append(data[i][j*3 + 1 + k]);
                if(k < 2) temp.append(" ");
            }
            stickDataStr[i*pts + j] = temp.toString();
        }
        stickVertexCount[i] = Integer.toString(pts);
    }
   
}
/**@return the unique string name that distinguises this element from others in the VRML/X3D file */
public String getTag(){
    return stickTag;
}

/**@return the XYZ coordinates of the stick figure. This 1D array has an "X Y Z"
 coordinate in every indicie. The length of the array is #timeSteps * #points, and
 they are in the following order <br>
 * <br>
 * [1] Point 1, of timeStep 1 of the stick figure<br>
 * [2] Point 2, timeStep 1<br>
 * .<br>
 * .<br>
 * .<br>
 * [n] Point n, timeStep 1<br>
 * [n+1] Point 1, or timeStep2 ... etc<br>
 */
public String[] getDataText(){
    return stickDataStr;
}

/**@return An array that has the same number of indicies as timeSteps, and tells
 * the VRML script interpreter how many indicies should be grouped together to
 * form a stick figure.
 */
public String[] getVertexText(){
    return stickVertexCount;
}
/**@return A String with the r,g,b coordinates of the users desired colour of the 
 * stick figure
 */
public String getColourText(){
    return stickRGB;
}


}
