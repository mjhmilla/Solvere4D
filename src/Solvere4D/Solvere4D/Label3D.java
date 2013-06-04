/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 *  Label3D.java is part of Solvere4D.

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
 * This class functions like a data structure to contain all of the data required
 * to make a 3D moving label in VRML, or X3D for that matter.
 *
 * @author mjhmilla
 */
public class Label3D {
    
    private String nametag; 
    private String labeltext;
    private double labelScaling;
    private String labelrgbStr;
    private String[] timeStr;
    private String[] posStr;
    
    /**
     * This constructor will populate the fields required to make a label that
     * sits on a moving billboard. The text, text size, text colour and position 
     * as a function of time are all set by the user.
     * 
     * @param tag : This is the unique identifier that is used within the 
     *              3D script to distinguish this part from another. This is usually
     *              set to the be name of the data file that is associated with this 
     *              part. 
     * @param text : This is the text that will be displayed in the script to 
     *               the user
     * @param scaling : This is the scale that should be applied to the label 
     *                  to make it the proper size
     * @param rgb : This is the RGB colour of the label, with values for R,G 
     *              and B ranging between 0 and 1 
     * @param data : This is the 2 dimensional array containing the position of 
     *                  the label at every time step. Column 1 contains the time
     *                  Column 2,3 and 4 the X, Y and Z locations of the label.
     */
    
    public Label3D(String tag, String text, double scaling, double[] rgb, double[][] data){
        nametag = tag;
        labeltext = text;
        StringBuffer tempStrBuf = new StringBuffer();
        labelScaling = scaling; 
        
        for(int k=0; k<3; k++){
            tempStrBuf.append(rgb[k]);
            if(k<2) tempStrBuf.append(" ");
        }
        labelrgbStr = tempStrBuf.toString();
        
        timeStr = new String[data.length];
        posStr = new String[data.length];
        
        for(int i=0; i<data.length; i++){
            timeStr[i] = Double.toString(data[i][0]/data[data.length-1][0]);
            
            tempStrBuf.replace(0, tempStrBuf.length(), "");
            for(int k=1; k<4; k++){
                tempStrBuf.append(data[i][k]);
                if(k<3) tempStrBuf.append(" ");
            }
            posStr[i] = tempStrBuf.toString();
        }
    }
    
    public String getTag(){
        return nametag;
    }
    
    public String getLabelText(){
        return labeltext;
    }
    
    public String getLabelColour(){
        return labelrgbStr;
    }
    
    public double getLabelScaling(){
        return labelScaling;
    }
    
    public String[] getLabelTime(){
        return timeStr;
    }
    
    public String[] getLabelPos(){
        return posStr;
    }
    

}
