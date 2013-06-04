/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 * SolvereFileFilter.java is part of Solvere4D.

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
import javax.swing.filechooser.*;
/**
 * This is a file filter for the meagre gui that will launch if Solvere4D
 * is called without an argument.
 * 
 * @author Matthew Millard
 */
public class SolvereFileFilter extends FileFilter {
    
    /** Creates a new instance of SolvereFileFilter */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
    
        String extension = "";
        String path = f.getName();
        int i = path.lastIndexOf('.');
        int j = path.length();
        
        if(i == -1){
            return false;
        }
        
        extension = path.substring(i+1, j);
        
        if(extension.compareTo("s4d") == 0){
            return true;
        }else{
            return false;
        }
        
        
    }
    
    public String getDescription() {
        return "Solvo4D File";
    }
}




