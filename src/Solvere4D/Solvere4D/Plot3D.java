/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 * Plot3D.java is part of Solvere4D.

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

import java.lang.Double;
/**
 * This class acts like a data structure to hold all of the information required
 * to create a 3D plot in VRML/X3D and convert it into a form that is condusive
 * to writing it in a text file
 * 
 * @author mjhmilla
 */
public class Plot3D {

    private SolvereUtilities SolUtil;
    private String rgbColour;
    private String transparency;
    private String nameTag;
    
    private double[] time;
    private double[][] axis1;
    private double[][] axis2;
    private String[] markerPos;
    
    private double textScale;
    private String[] triDataStr;
    private String[] timeStr;
    
    private String displayName;
    private double[] labelRGB;
    private String[] labelSRGBStr;
    
    private boolean plotMarker;
    private String[] markerSRGBStr;
    
    private boolean plotWire;
    private double[] plotOption;
    
/**
 * This constructor will create an object that acts a stucture to contain all
 * of the data required to render a 3D plot.
 * 
 * @param tag   : The unique name that is used to identify this element from others 
 *              in the VRML file
 * @param data  : The plot data to display. Here is the format <br>
 *                  Column 1: Time <br>
 *                  Column 2,3,4: Axis 1: The X,Y,Z coordinates of the first axis. <br>
 *                  Column 5,6,7: Axis 2: The X,Y,Z coordinates of the second axis. <br>
 *              N.B.: Every row must be full.
 * @param wireFrame     : If this option is set true a wire frame version of the
 *                        plot will be displayed
 * @param plotOptions   : A double array that contains R,G,B, and T data <br>
 *                      Index 0: R (0-1), red <br>
 *                      Index 1: G (0-1), green <br>
 *                      Index 2: B (0-1), blue <br>
 *                      Index 3: T (0-1), transparency: 0=solid, 1=completely transparent <br>
 * @param scale :   A double value that sets the size of the label text and the
 *                  size of the marker that follows the data (if the user selected it)
 * @param label :   The text of a label that will be displayed at the first point location
 * @param labelColourRGB  : The colour the text of the label should be printed in
 * @param marker   : If this is marked true a sphereical marker will be animated to follow
 *                   Axis2 as a function of time.
 * @param markerColourRGB : The colour of the marker
 */    
public Plot3D(String tag, double[][] data, boolean wireFrame, double[] plotOptions, double scale, String label, double[] labelColourRGB, boolean marker, double[] markerColourRGB){
    
    nameTag = tag;
    displayName = label;
    plotMarker = marker;
    
    labelSRGBStr = new String[2];
    markerSRGBStr = new String[2];
    time = new double[data.length];
    timeStr = new String[data.length];
    axis1 = new double[data.length][3];
    axis2 = new double[data.length][3];
    markerPos = new String[data.length];
    labelRGB = new double[3];
    
    labelSRGBStr[0] = Double.toString(scale);
    markerSRGBStr[0] = Double.toString(scale/4);
    StringBuffer tempStrBuf = new StringBuffer();
    
    for(int i=0; i<3; i++)
        labelRGB[i] = labelColourRGB[i];
    
    
    
    for(int i=0; i<data.length; i++){
        time[i] = data[i][0]/data[data.length-1][0];
        timeStr[i] = Double.toString(time[i]);
        int k=0;
        for(int j=1; j<4; j++){
            axis1[i][k] = data[i][j];
            k++;
            if(k>2) k=0;
        }
        
        k=0;
        tempStrBuf.replace(0, tempStrBuf.length(), "");
        for(int j=4; j<7; j++){
            axis2[i][k] = data[i][j];
            tempStrBuf.append(axis2[i][k]); 
            if(k<2) tempStrBuf.append(" ");
            
            k++;
            if(k>2) k=0;
        }
        markerPos[i]=tempStrBuf.toString();
        
    }
    plotWire = wireFrame;
    plotOption = new double[4];
    
    //for(int i=0; i<plotFlags.length; i++)
    //    plotFlag[i]=plotFlags[i];
    
    for(int i=0; i<plotOptions.length; i++)
        plotOption[i]=plotOptions[i];
    
    transparency = new String(Double.toString(plotOptions[3]));
    
    tempStrBuf.replace(0, tempStrBuf.length(), "");
    for(int i=0; i<3; i++){
        tempStrBuf.append(plotOptions[i]);
        if(i<2) tempStrBuf.append(" ");
    }
    rgbColour = tempStrBuf.toString();
    tempStrBuf.replace(0, tempStrBuf.length(), "");
    
    for(int i=0; i<3; i++){
        tempStrBuf.append(labelColourRGB[i]);
        if(i<2) tempStrBuf.append(" ");
    }
    labelSRGBStr[1] = tempStrBuf.toString();
    
    tempStrBuf.replace(0, tempStrBuf.length(), "");
    for(int i=0; i<3; i++){
        tempStrBuf.append(markerColourRGB[i]);
        if(i<2) tempStrBuf.append(" ");
    }
    markerSRGBStr[1] = tempStrBuf.toString();
    
    axis1 = new double[data.length][3]; 
    axis2 = new double[data.length][3]; 
   
    for(int i=0; i<data.length; i++){
        for(int j=0; j< 3; j++){
            axis1[i][j] = data[i][j+1];
            axis2[i][j] = data[i][j+4];
        }
    }
    
    double[][][] tempData = SolUtil.getTriangularArray(axis1, axis2);
    triDataStr = new String[6*data.length];
    
    for(int i = 0; i<data.length; i++){
        for(int j = 0; j<6; j++){
            tempStrBuf.replace(0,tempStrBuf.length(),"");
            for(int k = 0; k<3; k++){
                tempStrBuf.append(tempData[i][j][k]);
                if(k < 2) tempStrBuf.append(" ");
                    
            }
            triDataStr[i*6 + j] = tempStrBuf.toString();
            
        }    
    }
    
    
    
    
}


/**@return An string array containing all of the verticies required to render
 * the plot as a series of triangles. If you have n data points, then this will
 * return n*3 points to render n triangles. The triangles are in order, and if you
 * have to pick a direction for the normal, you would use ccw. 
 */
public String[] getDataText(){
    return triDataStr;
}

/**@return A string of the RGB colour used for the entire array*/
public String getRGBText(){
    return rgbColour;
}
/**@return A string of the transparency of the data plot*/
public String getTransparencyText(){
    return transparency;
}

/**@return The text of the label that will be placed at the first data point of the plot*/
public String getLabelText(){
    return displayName;
}

/**@return The string that represents the unique tag that will distinguish this
* element from other elements in the VRML/X3D file. 
*/
public String getTag(){
    return nameTag;
}

/**@return The state of the flag that indicates whether this plot should be 
 * rendered as a wire frame or a solid.
 */
public boolean isWireFrame(){
    return plotWire;
}

/**@return The scaling factor that will be applied to the label to size it appropriately*/
public double getLabelScale(){
    return textScale;
}

/**@return The key values (normalized time) required to animate the plot data marker*/
public String[] getKeyValues(){
    return timeStr;
}

/**@return Index 1 contains the scale of the label, and index 2 contains the RGB value
 * of the colour that the label should be rendered in.
 */
public String[] getLabelColour(){
    return labelSRGBStr;
}

/**@return A value of TRUE if a marker should be rendered to follow the data plot*/
public boolean isMarkerEnabled(){
    return plotMarker;
}

/**@return Index 1 contains the scale of the marker, and index 2 contains the RGB value
 * of the colour that the marker should be rendered in.
 */
public String[] getMarkerOptions(){
    return markerSRGBStr;
}
/**@return An array containing the X,Y,Z positions of Axis2, which the marker will 
 * follow. Each index contains "X Y Z" data.
 */
public String[] getMarkerPositions(){
    return markerPos;
}

}