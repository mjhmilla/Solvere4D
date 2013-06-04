/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 * MarkerData.java is part of Solvere4D.

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

/**
 * This class acts like a data structure to hold all of the information required
 * to create a static 3D marker that is either a sphere, cone, cylinder or a box.
 * Arrays of static markers are very useful for creating grids, and axis, sidewalks,
 * and other pieces of static geometry that would make a useful reference.
 * 
 * @author mjhmilla
 */
public class MarkerData {

    private float[][] markerXYZPos;
    private float[] markerSizeProp_m;
    private float[] markerRGB;
    private float markerTransparency;
    private int markerShape;
    
    
    public static int SHAPE_SPHERE = 0;
    public static int SHAPE_CYLINDER = 1;
    public static int SHAPE_BOX = 2;
    public static int SHAPE_CONE = 3;
    
    /**
     * This constructor will get all of the necessary data required to create
     * a static array of markers of a uniform size, shape (sphere, cylinder, cone
     * or box), colour and transparency.
     * 
     * @param shape : <li> 0=sphere 
     *                <li> 1=cylinder, 
     *                <li> 2=box 
     *                <li> 3=cone
     * @param xyz   : An array of x,y,z positions for each of the makers, as a [n][3] sized array
     * @param sizeProperties_m : The size properties for the shape of choice,
     *                           always an array with 3 indicies:
     *          <li> Sphere:    idx1=radius,    idx2=0,     idx3 = 0; 
     *          <li> Cylinder:  idx1=radius,    idx2=height,idx3 = 0;
     *          <li> Box:       idx1= X len,    idx2= Y len,idx3 = Z len;
     *          <li> Cone:      idx1=radius,    idx2=height,idx3 = 0;
     * @param RGB           :The double values for R, G and B. Each of these values range from 0-1
     * @param transparency  :The transparency of the maker from opaque (0) to totally clear (1)
     * 
     */
    public MarkerData(int shape, double[][] xyz, double[] sizeProperties_m,double[] RGB, double transparency) {
            
            markerTransparency = (float)transparency;
            
            switch (shape){
                case 0:
                    markerShape = SHAPE_SPHERE;
                    break;
                case 1:
                    markerShape = SHAPE_CYLINDER;
                    break;
                case 2:
                    markerShape = SHAPE_BOX;
                    break;
                case 3:
                    markerShape = SHAPE_CONE;
                    break;
                default:
                    markerShape = 0;
                    break;
            }
            
            markerSizeProp_m = new float[3];
            markerSizeProp_m[0] = (float)sizeProperties_m[0];
            markerSizeProp_m[1] = (float)sizeProperties_m[1];
            markerSizeProp_m[2] = (float)sizeProperties_m[2];
            
            markerRGB = new float[3];
            markerRGB[0] = (float)RGB[0];
            markerRGB[1] = (float)RGB[1];
            markerRGB[2] = (float)RGB[2];
             
            int rows = xyz.length;
            int cols = xyz[0].length;
            
            markerXYZPos = new float[rows][cols];
            
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++){
                    markerXYZPos[i][j] = (float)xyz[i][j];
                }
            }
            
    }
    
    public float[][] getMarkerPos(){
        return markerXYZPos;
    }
    
    public float[] getMarkerSizeProperties(){
        return markerSizeProp_m;
    }
    
    public int getMarkerShape(){
        return markerShape;
    }
    
    public float[] getMarkerRGB(){
        return markerRGB;
    }
    
    public float getMarkerTransparency(){
        return markerTransparency;
    }
    
}
