/*
 *  Copyright Matthew J.H. Millard 2008
 * 
 Solvere4D.java is part of Solvere4D.

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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.io.IOException;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JCheckBox;

import java.util.prefs.Preferences;

/**
 * This is an open source software program that will take in VRML97 geometry,
 * motion files, force/torque files and user options in order to write a VRML97
 * file that presents all of this data as an animation. This package is currently
 * set up to make it easy to adapt it to write different 3D output files - such
 * as an X3D file. For a first release however, it will only output VRML97 files.
 * 
 * 
 * @author Matthew Millard
 * @version 1.0 - September 29, 2008
 */
public strictfp class Solvere4D extends javax.swing.JFrame implements ActionListener {
    
    
    private static String packageName = "Solvere4D";
    
    private SolvereUtilities SolUtil;
    
    private Preferences userPreferences;
    
    
    private File    rootFile; //path to the user's *.s4d file
    private File    dataFile; //a temporary File object that is used to read in the users data files
    
    
    private double[] lightDirection;    //The user's desired lighting direction vector
    private boolean headlight;          //The user's desired setting for the headlight
    private double[]          aniTime;  //A time vector read in from the users data files
    private double[]          backgroundColour; //RGB background colour
    private double timeScaling; //The desired time scaling for the default play speed
    private double sizeScaling; //Not used. When implemented will scale everything - this allows extreme close ups
    private int downSampleFactor; //The degree in which to downsample the video data
    
    
    private ForceTorqueData[] ft;           //Force & Torque object array
    private BodyData[]      bodies;         //Object array for storing geometry and animated paths
    private double[][]      partTransXYZ;   //Temporary arrays for storing XYZ(t) paths
    private double[][]      partRotMAT;     //Temporary array for storing a row-wise rotation matrices
    
    private BodyData        cameraPath;     //The body data object for the camera path
    private double[][]      cameraXYZ;      //Temporary array for the XYZ(t) pos of the camera
    private double[][]      cameraRotMAT;   //Temporary array for the array of rotation matricies 
    
    private MarkerData[]    markerData; //Array of marker data objects
    private double[][] markerPos;   //Temporary array of marker positions
    private double[] markerSize;    //Temporary array of marker geometry information
    private double[] markerRGB;     //Temporary array of marker colours
    private double markerTrans;     //Temporary array of marker transparencies
    
    private Plot3D[]        plot3Ddata;     //Plot3D object arrays
    private StickFigure[]   stickFigures;   //Stick figure object arrays
    private Label3D[]       labels3D;       //3D moving label object arrays
    
    
    /**Animation Panel Variables*/
    private JPanel controlPanel;
    private Dimension       screenSize;
    private JPanel aniOptionPanel;
    private JPanel spinnerPanel;
    private JPanel reloadAniPanel;
    private JPanel controlAniPanel;
    private JPanel fileControlPanel;
    
    private JButton openButton;
    private JButton writeButton;
    
    private JSpinner timeSpinner;
    private JSpinner sizeSpinner;
    private JSpinner downSampleSpinner;   
    
    private SpinnerNumberModel timeSpinnerNM;
    private SpinnerNumberModel sizeSpinnerNM;
    private SpinnerNumberModel downSampleSpinnerNM;
    private JCheckBox chkUseCameraPath;
    
    private JLabel timeLabel;
    private JLabel downSampleLabel;
    
    private volatile JPanel mainPanel;
    private volatile JPanel aniPanel;
    
    private Dimension aniSize;
    private Dimension controlSize;
    
    private static String KEY_PATH = "lastPath";

    
    
    
    /**
     * Solvere4D is a program that will take in VRML geometry files, motion
     * files (columns of time, X,Y,Z, m11, m12, m13, ... m33), force files
     * (time, X, Y, Z, FX, FY, FZ, TX, TY, TZ), and a number of display options
     * and create a VRML97 script that will show the data as a 3D animation when
     * viewed with the appropriate viewer. For a viewer I recommend BSContact
     * http://www.bitmanagement.de/index.en.html
     * 
     * All of these animation options, and file names are stored in a configuration
     * file that has the extension of *.s4d. Details on the syntax for this config
     * file can be found in the user manual for this program
     * 
     * If Solvere4D is called from the command line without arguments, a small
     * GUI will popup that will allow the user to choose the appropriate *.sd4
     * file. If Solvere4D is called with a path to a *.sd4 file, that config
     * file will be loaded, and a *.wrl file with the appropriate extention 
     * will be added in the same directory.
     */
    public Solvere4D(String[] args) {
        
        timeScaling = 1.0;
        sizeScaling = 1.0;
        downSampleFactor = 1;
        backgroundColour = new double[3];
        backgroundColour[0] = 0;
        backgroundColour[1] = 0;
        backgroundColour[2] = 0;
        
        if (args.length != 0){
            rootFile = new File(args[0]);
            if(rootFile != null){
                readAnimationData(rootFile.getAbsolutePath());
                
                StringBuffer targetFile = new StringBuffer(rootFile.getAbsolutePath());
                int extIdx = targetFile.indexOf(".");
                targetFile.delete(extIdx,targetFile.length());
                targetFile.append(".wrl");

                writeAnimationFile(targetFile.toString());
           }
            
        }else{
     
            userPreferences = Preferences.userRoot().node(packageName);

            screenSize =  java.awt.Toolkit.getDefaultToolkit().getScreenSize(); 
            screenSize.setSize(screenSize.getWidth()/1.5d,screenSize.getHeight()/5.0d);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(screenSize);
            setTitle("Solvere4D File Writer");
            setVisible(true);



            aniSize = new Dimension(screenSize.width,(int)(screenSize.getHeight()-150.0) );
            controlSize = new Dimension(screenSize.width,(int)(150) );

            mainPanel = new JPanel();
            mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
                mainPanel.setMaximumSize(screenSize);
                mainPanel.setPreferredSize(screenSize);

            //animationTime = new JLabel("Time: ");
            //animationFrame = new JLabel("Frame: ");

            Dimension buttonSize = new Dimension(100, 30);
            Dimension bigButtonSize = new Dimension(150,30); 



           getAnimationFile();

           if(rootFile != null){
                readAnimationData(rootFile.getAbsolutePath());
           }




            controlPanel = new JPanel();
            controlPanel.setLayout(new GridLayout(1,3));
            controlPanel.setAlignmentX(controlPanel.CENTER_ALIGNMENT);
                controlPanel.setMaximumSize(controlSize);
                controlPanel.setPreferredSize(controlSize);

            aniPanel = new JPanel();
                aniPanel.setPreferredSize(aniSize);
                aniPanel.setMaximumSize(aniSize);
            controlAniPanel = new JPanel();
            controlAniPanel.setLayout(new GridLayout(2,3));
            controlAniPanel.setAlignmentX(controlAniPanel.CENTER_ALIGNMENT);
                controlAniPanel.setMaximumSize(controlSize);
                controlAniPanel.setPreferredSize(controlSize);           

            fileControlPanel = new JPanel();


             openButton = new JButton("Open");
                openButton.setPreferredSize(buttonSize);
                openButton.setMaximumSize(buttonSize);
                openButton.addActionListener(this);

             writeButton = new JButton("Write File");
                writeButton.setPreferredSize(buttonSize);
                writeButton.setMaximumSize(buttonSize);
                writeButton.addActionListener(this);   

             reloadAniPanel = new JPanel();
             reloadAniPanel.setLayout(new BoxLayout(reloadAniPanel,BoxLayout.X_AXIS));

             aniOptionPanel = new JPanel();
             aniOptionPanel.setLayout(new BoxLayout(aniOptionPanel,BoxLayout.Y_AXIS));
             aniOptionPanel.setMaximumSize(new Dimension(300,100));   

             spinnerPanel = new JPanel();
             spinnerPanel.setLayout(new GridLayout(3,2));
             spinnerPanel.setMaximumSize(new Dimension(300,60));

             timeSpinnerNM = new SpinnerNumberModel(timeScaling, 0.5, 1000.0, 1.0);
             sizeSpinnerNM = new SpinnerNumberModel(sizeScaling, 0.5, 1000.0, 1.0);
             downSampleSpinnerNM = new SpinnerNumberModel((int)1, (int)1, (int)100, (int)1);   

             timeSpinner = new JSpinner(timeSpinnerNM);
                timeSpinner.setMaximumSize(buttonSize);
             sizeSpinner = new JSpinner(sizeSpinnerNM);
                sizeSpinner.setMaximumSize(buttonSize);
             downSampleSpinner = new JSpinner(downSampleSpinnerNM);
                downSampleSpinner.setMaximumSize(buttonSize);

             chkUseCameraPath = new JCheckBox("Use Camera Path");   
             chkUseCameraPath.setSelected(true);

             timeLabel = new JLabel("Time Scaling");
             downSampleLabel = new JLabel("Down Sample Factor");



             spinnerPanel.add(timeLabel);
             spinnerPanel.add(timeSpinner);
             //spinnerPanel.add(sizeLabel);
             //spinnerPanel.add(sizeSpinner);
             spinnerPanel.add(downSampleLabel);
             spinnerPanel.add(downSampleSpinner);

             aniOptionPanel.add(chkUseCameraPath);
             aniOptionPanel.add(spinnerPanel);


             reloadAniPanel.add(aniOptionPanel);


            fileControlPanel.add(openButton);    
            controlPanel.add(fileControlPanel);
            //controlAniPanel.add(backButton);
            fileControlPanel.add(writeButton);
            //controlAniPanel.add(animationTime);
            //controlAniPanel.add(animationFrame);

            controlPanel.add(controlAniPanel);
            controlPanel.add(reloadAniPanel);

            //mainPanel.add(aniPanel,0);
            mainPanel.add(controlPanel,0);


            setFocusable(true);

                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    showAnimation(mainPanel);
                }
            });

        }
    }
    
    /**
     * This function will show the gui to the user, if they have called Solvere4D
     * from the command line without specifying an appropriate configuration file
     */
    private void showAnimation(JPanel panel){
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(screenSize);
        setTitle("Solvere4D Animation Script Writer");
        setVisible(true);
        getContentPane().add(panel);   
        pack();
        //show();
        setVisible(true);
        repaint();

    }
    
    /**
     * @param args : A full path to the desired *.s4d configuration file, if no argument
     *               is passed in a simple gui is launched to help the user choose
     *               the configuration file that should be used to write the 
     *               animation script (VRML in this case).
     */
    public static void main(String[] args) {
        
        
        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
                    //UIManager.getSystemLookAndFeelClassName()
                    );
            } catch (Exception e) {
                //There should be a message or a pop up here!
            }
            System.out.println("Copyright Matthew J.H.Millard 2008");
            System.out.println("  Solvere4D is offered under the GPL V3");
            System.out.println("  for more information visit http://www.gnu.org/");
            
            Solvere4D testAnimation = new Solvere4D(args);
    }
    
    /**
     * This interrupt handler is called when ever buttons from the GUI trigger
     * an interrupt - like the "Open" buttons and the "Write" buttons. If the 
     * GUI is not called, then this interrupt handler will never be used.
     */
    public void actionPerformed(ActionEvent e){
    
        if(e.getSource().equals(openButton)){
            getAnimationFile();
            
            if(rootFile != null){
                readAnimationData(rootFile.getAbsolutePath());
            }
            
        }
        
        if(e.getSource().equals(writeButton)){
            boolean useCamera = chkUseCameraPath.isSelected();
            Double temp = (Double)timeSpinner.getValue();
            timeScaling = temp.doubleValue();
            temp = (Double)sizeSpinner.getValue();
            //sizeScaling = temp.doubleValue();
            Integer iTemp = (Integer)downSampleSpinner.getValue();
            downSampleFactor = iTemp.intValue();
            
            StringBuffer targetFile = new StringBuffer(rootFile.getAbsolutePath());
            int extIdx = targetFile.indexOf(".");
            targetFile.delete(extIdx,targetFile.length());
            targetFile.append(".wrl");
            
            writeAnimationFile(targetFile.toString());
            
        }
 
    }
    /**
     * This function supports the small GUI that is launched, and controls
     * the "Open File" menu dialog.
     */
    private void getAnimationFile(){
            int fileOpenReturn = -1;
            JFileChooser menuFileOpen = null;
                    
            String lastDir = userPreferences.get(KEY_PATH, null);
            if(lastDir == null){
                 menuFileOpen = new JFileChooser("C://"); 
            }else{
                menuFileOpen = new JFileChooser(lastDir);
            }
            
            
            menuFileOpen.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileFilter dfaFilter = new SolvereFileFilter();
            menuFileOpen.setFileFilter(dfaFilter);

            fileOpenReturn = menuFileOpen.showOpenDialog(openButton);

            rootFile = null;
            
            if(fileOpenReturn == menuFileOpen.APPROVE_OPTION){
                
                rootFile = menuFileOpen.getSelectedFile();
                userPreferences.put(KEY_PATH,rootFile.getParent());
            }
    }
    
    
    /**
     * This function will go through all of the data the user has decided to
     * animate and will call the appropriate functions in SolvereUtilities to
     * write the animation script to file and save it.
     * 
     * @param targetFile    : The full path of the target animation file (VRML file)
     */
    private void writeAnimationFile(String targetFile){ //, BodyData[] rb, MarkerData[] md, double timeScaling, int downSampling){ 
    
        StringBuffer aniStrBuf = new StringBuffer();
        
        int vrml_0_x3d_1 = -1;
        
        
        //All of the "append ..." type functions need to be updated along
        //with the "lib_ ..." files in the "build/class/ ..." need to be updated
        //such that there are equivalent X3D libraries. For now this can only
        //output a *.wrl file.
        int t1 = targetFile.indexOf(".wrl");
        int t2 = targetFile.indexOf(".x3dv");
        int t3 = targetFile.indexOf(".x3d");
        
        if(t1 != -1 || t2 != -1){
           vrml_0_x3d_1 = 0;
        }if(t3 != -1){
           vrml_0_x3d_1 = 1;}     
                
        double maxTime = aniTime[aniTime.length-1]*timeScaling;
        
        try{
            FileOutputStream fos = new FileOutputStream(targetFile); 
            OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8"); 
            
            if(vrml_0_x3d_1 == 0){
                
                aniStrBuf = SolUtil.appendHeader(aniStrBuf, maxTime, backgroundColour, lightDirection, headlight);
                
                String[] rbGeo = null;
                String[] rbTrans = null;
                String[] rbOrien = null;
                String[] rbKeyFrame = null;
                String rbTag = null;
                
                for(int i = 0; i < bodies.length; i++){
                    rbGeo = bodies[i].getGeometryText();
                    rbTrans = bodies[i].getTranslationText();
                    rbOrien = bodies[i].getOrientationText();
                    rbKeyFrame = bodies[i].getKeyFrameText();
                    rbTag = new String(bodies[i].getTagName());
                    
                    //Write the geometry file
                    for(int j=0; j < rbGeo.length; j++){
                        aniStrBuf.append(rbGeo[j]);
                        aniStrBuf.append('\n');
                    }
                    aniStrBuf.append('\n');
                    
                    if(rbTrans != null) aniStrBuf = SolUtil.appendTranslationTag(aniStrBuf, rbTag, rbKeyFrame, rbTrans, downSampleFactor);
                    if(rbTrans != null) aniStrBuf = SolUtil.appendRouteTranslation(aniStrBuf,rbTag);
                    
                    if(rbOrien != null) aniStrBuf = SolUtil.appendOrientationTag(aniStrBuf, rbTag, rbKeyFrame, rbOrien, downSampleFactor);
                    if(rbOrien != null) aniStrBuf = SolUtil.appendRouteRotation(aniStrBuf,rbTag);
                    
                    aniStrBuf.append('\n');
     
            }
                
                if(markerData != null){
                    for(int i=0; i < markerData.length; i++){
                        
                        float[][] mkrPos = markerData[i].getMarkerPos();
                        float[] mkrRGB = markerData[i].getMarkerRGB();
                        float[] mkrGeo = markerData[i].getMarkerSizeProperties();
                        float mkrTrans = markerData[i].getMarkerTransparency();
                        
                        for(int j = 0; j < mkrPos.length; j++){
                          aniStrBuf = SolUtil.appendStaticMarker(aniStrBuf, markerData[i].getMarkerShape(), mkrPos[j], mkrRGB, mkrGeo, mkrTrans);
                        }
                    
                        aniStrBuf.append('\n');
                        aniStrBuf.append('\n');
                    }
                }
                 
                if(ft != null){
                        String[] fGeo = null;
                        String[] tGeo = null;
                        String[] fTrans = null;
                        String[] tTrans = null;
                        String[] fOrien = null;
                        String[] tOrien = null;
                        String[] fScale = null;
                        String[] tScale = null;
                        String[] fColour = null;
                        String[] tColour = null;
                        String[] ftKeyFrame = null;
                       
                        String[] fPlotTri = null;
                        String[] fPlotColour = null;
                        String[] tPlotTri = null;
                        String[] tPlotColour = null;
                        String transparency = null;
                        
                        String fTag = null;
                        String tTag = null;
                        boolean wireFrame = false;
                        
                        for(int i = 0; i < ft.length; i++){
                            fGeo = ft[i].getForceGeometryText();
                            tGeo = ft[i].getTorqueGeometryText();
                            fTrans = ft[i].getForcePosText();
                            tTrans = ft[i].getTorquePosText();
                            fOrien = ft[i].getForceOrienText();
                            tOrien = ft[i].getTorqueOrienText();
                            fScale = ft[i].getForceScalingText();
                            tScale = ft[i].getTorqueScalingText();
                            fColour = ft[i].getForceColouringText();
                            tColour = ft[i].getTorqueColouringText();
                            ftKeyFrame = ft[i].getKeyFrameText();
                            fTag = ft[i].getForceTag();
                            tTag = ft[i].getTorqueTag();

                            //Write the geometry file
                            if(fGeo != null){
                                for(int j=0; j < fGeo.length; j++){
                                    aniStrBuf.append(fGeo[j]);
                                    aniStrBuf.append('\n');
                                }
                                aniStrBuf.append('\n');
                            }
                            
                            if(tGeo != null){
                                for(int j=0; j < tGeo.length; j++){
                                    aniStrBuf.append(tGeo[j]);
                                    aniStrBuf.append('\n');
                                }
                                aniStrBuf.append('\n');
                            }
                            
                            if(fTrans != null) aniStrBuf = SolUtil.appendTranslationTag(aniStrBuf, fTag, ftKeyFrame, fTrans, downSampleFactor);
                            if(fTrans != null) aniStrBuf = SolUtil.appendRouteTranslation(aniStrBuf,fTag);
                            if(fOrien != null) aniStrBuf = SolUtil.appendOrientationTag(aniStrBuf, fTag, ftKeyFrame, fOrien, downSampleFactor);    
                            if(fOrien != null) aniStrBuf = SolUtil.appendRouteRotation(aniStrBuf,fTag);
                            aniStrBuf.append('\n');
                            
                            if(tTrans != null) aniStrBuf = SolUtil.appendTranslationTag(aniStrBuf, tTag, ftKeyFrame, tTrans, downSampleFactor);
                            if(tTrans != null) aniStrBuf = SolUtil.appendRouteTranslation(aniStrBuf,tTag);
                            if(tOrien != null) aniStrBuf = SolUtil.appendOrientationTag(aniStrBuf, tTag, ftKeyFrame, tOrien, downSampleFactor);                         
                            if(tOrien != null) aniStrBuf = SolUtil.appendRouteRotation(aniStrBuf,tTag);
                            aniStrBuf.append('\n');
                            
                            if(fTrans != null) aniStrBuf = SolUtil.appendColourTag(aniStrBuf, fTag, ftKeyFrame, fColour, downSampleFactor);   
                            if(fTrans != null) aniStrBuf = SolUtil.appendRouteColour(aniStrBuf, fTag);
                            if(tTrans != null) aniStrBuf = SolUtil.appendColourTag(aniStrBuf, tTag, ftKeyFrame, tColour, downSampleFactor);
                            if(tTrans != null) aniStrBuf = SolUtil.appendRouteColour(aniStrBuf, tTag);
                            aniStrBuf.append('\n');
                            
                            if(fTrans != null) aniStrBuf = SolUtil.appendScaleTag(aniStrBuf, fTag, ftKeyFrame, fScale, downSampleFactor);    
                            if(fTrans != null) aniStrBuf = SolUtil.appendRouteScale(aniStrBuf, fTag);
                            if(tTrans != null) aniStrBuf = SolUtil.appendScaleTag(aniStrBuf, tTag, ftKeyFrame, tScale, downSampleFactor);
                            if(tTrans != null) aniStrBuf = SolUtil.appendRouteScale(aniStrBuf, tTag);
                            aniStrBuf.append('\n');
                            aniStrBuf.append('\n');
                            
                            //write plots if they have been enabled.
                            if(ft[i].plotForceHist()){
                                fPlotTri = ft[i].getForcePlotText();
                                fPlotColour = ft[i].getForcePlotColourText();
                                String fpTag = new String(fTag);
                                fpTag = fpTag + "_P";
                                transparency = ft[i].getForceTransparencyText();
                                wireFrame = ft[i].plotForceInWireFrame();
                                if(fPlotTri != null) aniStrBuf = SolUtil.appendPlot(aniStrBuf, fpTag ,null,null,false, null,null,null, downSampleFactor, fPlotTri, fPlotColour,transparency, wireFrame);
                            }
                            
                            if(ft[i].plotTorqueHist()){
                                tPlotTri = ft[i].getTorquePlotText();
                                tPlotColour = ft[i].getTorquePlotColourText();
                                String tpTag = new String(tTag);
                                tpTag = tpTag + "_P";
                                transparency = ft[i].getTorqueTransparencyText();
                                wireFrame = ft[i].plotTorqueInWireFrame();
                                if(tPlotTri != null) aniStrBuf = SolUtil.appendPlot(aniStrBuf,tpTag, null,null,false, null,null,null, downSampleFactor, tPlotTri, tPlotColour, transparency, wireFrame);
                            }
                    }       
                
                }
                //3D Plot data
                if(plot3Ddata != null){
                    String tag = null;
                    String[] data = null;
                    String[] rgb = new String[1];
                    String transparency = null;
                    boolean wireFrame = false;
                    String label = null;
                    String[] labelOpt = null;
                    boolean applyMkr;
                    String[] mkrOptions;
                    String[] mkrTime;
                    String[] mkrPos;
                    
                    for (int i = 0; i < plot3Ddata.length; i++){
                        
                        label   = plot3Ddata[i].getLabelText();
                        tag     = plot3Ddata[i].getTag();
                        data    = plot3Ddata[i].getDataText();
                        rgb[0]  = plot3Ddata[i].getRGBText();
                        transparency    = plot3Ddata[i].getTransparencyText();
                        wireFrame       = plot3Ddata[i].isWireFrame();
                        labelOpt        = plot3Ddata[i].getLabelColour();
                        applyMkr        = plot3Ddata[i].isMarkerEnabled();
                        mkrOptions      = plot3Ddata[i].getMarkerOptions();
                        mkrTime         = plot3Ddata[i].getKeyValues();
                        mkrPos          = plot3Ddata[i].getMarkerPositions();
                        //appendPlot(StringBuffer curBuf, String tag, String[] triPlotGeo, String[] vertexColour, String transparency, boolean wireFrame)
                        SolUtil.appendPlot(aniStrBuf, tag, label, labelOpt, applyMkr, mkrOptions, mkrTime, mkrPos, downSampleFactor,data, rgb, transparency, wireFrame);
                    
                    } 
                }
                
                if(stickFigures != null){
                        String tag = null;
                        String sfRGB = null;
                        String[] sfData = null;
                        String[] sfVertexCnt = null;
                
                    for(int i=0; i<stickFigures.length; i++){
                        tag = stickFigures[i].getTag();
                        sfRGB = stickFigures[i].getColourText();
                        sfData = stickFigures[i].getDataText();
                        sfVertexCnt = stickFigures[i].getVertexText();
                                                  
                        SolUtil.appendStickFigure(aniStrBuf, tag, sfRGB, sfData, sfVertexCnt);                        
                    }
                    
                }
                
                if(labels3D != null){
                    String tag = null;
                    String text = null;
                    double scaling = 1;
                    String rgb = null;
                    String[] labelTime = null;
                    String[] labelPos = null;
                    
                    for(int i=0; i< labels3D.length; i++){
                        tag     = labels3D[i].getTag();
                        text    = labels3D[i].getLabelText();
                        scaling = labels3D[i].getLabelScaling();
                        rgb     = labels3D[i].getLabelColour();
                        labelTime   = labels3D[i].getLabelTime();
                        labelPos    = labels3D[i].getLabelPos();
                        SolUtil.appendMovingLabel(aniStrBuf, tag, text, scaling, rgb, labelTime, labelPos, downSampleFactor);
                    }
                    
                    //appendMovingLabel(StringBuffer curBuf, String tag, String text,
                //        double scaling, String rgb, String[] labelTime, String[] labelPos, int downSample)
                }
                
                //Navigation Information
                if(cameraPath != null){
                    String camTag = cameraPath.getTagName();
                    String camTTag = camTag + "_T";
                    String[] camKeyFrame = cameraPath.getKeyFrameText();
                    String[] camTrans = cameraPath.getTranslationText();
                    String[] camOrien = cameraPath.getOrientationText();
                    
                    aniStrBuf.append("DEF ");
                    aniStrBuf.append(camTTag);
                    aniStrBuf.append(" Transform{");
                    aniStrBuf.append('\n');
                    aniStrBuf.append(" translation 0 0 0");
                    aniStrBuf.append('\n');
                    aniStrBuf.append(" children[");
                    aniStrBuf.append('\n');
                        aniStrBuf.append("DEF ");
                        aniStrBuf.append(camTag);
                        aniStrBuf.append(" Viewpoint {");
                        aniStrBuf.append('\n');
                            aniStrBuf.append("description \"Viewpoint\" ");
                            aniStrBuf.append('\n');
                            aniStrBuf.append("position 0 0 0");
                            aniStrBuf.append('\n');    
                            aniStrBuf.append("orientation 0 0 1 0");
                            aniStrBuf.append('\n');
                            aniStrBuf.append("centerOfRotation 0 0 0");
                            aniStrBuf.append('\n');
                        aniStrBuf.append("  }");
                        aniStrBuf.append('\n');
                        aniStrBuf.append(" ]");
                        aniStrBuf.append('\n');
                        aniStrBuf.append("}");
                        aniStrBuf.append('\n');
                        aniStrBuf.append('\n');
                    
                    
                    
                    aniStrBuf = SolUtil.appendTranslationTag(aniStrBuf, camTag, camKeyFrame, camTrans, downSampleFactor);
                    aniStrBuf = SolUtil.appendOrientationTag(aniStrBuf, camTag, camKeyFrame, camOrien, downSampleFactor);
                    
                    aniStrBuf.append('\n');
                    aniStrBuf.append('\n');    
                    
                    /*ROUTE TS.fraction_changed TO PI.set_fraction
                    ROUTE PI.value_changed TO KEY.translation*/
                    aniStrBuf.append("ROUTE TS.fraction_changed TO ");
                    aniStrBuf.append(camTag);
                    aniStrBuf.append("_OI.set_fraction");
                    aniStrBuf.append('\n');
                        aniStrBuf.append("ROUTE ");
                        aniStrBuf.append(camTag);
                        aniStrBuf.append("_OI.value_changed TO ");
                        aniStrBuf.append(camTTag);
                        aniStrBuf.append(".rotation");
                        aniStrBuf.append('\n');
                    aniStrBuf.append("ROUTE TS.fraction_changed TO ");
                    aniStrBuf.append(camTag);
                    aniStrBuf.append("_PI.set_fraction");
                    aniStrBuf.append('\n');
                        aniStrBuf.append("ROUTE ");
                        aniStrBuf.append(camTag);
                        aniStrBuf.append("_PI.value_changed TO ");
                        aniStrBuf.append(camTTag);
                        aniStrBuf.append(".translation");
                        aniStrBuf.append('\n');
                        aniStrBuf.append('\n');
                }
                
                aniStrBuf = SolUtil.appendTimeControls(aniStrBuf, aniTime, timeScaling);
                
            }
            
            
            
            out.write(aniStrBuf.toString());
            out.flush();
            
            out.close();
            fos.close();
            
            
        }catch(IOException ioe){
            System.out.println("IO exception thrown in DynaFlexAnimation class while writing text file");
            ioe.printStackTrace();
        }
           
                
        
        
    }
    
    
    /**
     * This function will use the users *.s4d configuration file to read in
     * all of the data required to write the final animation script, be it 
     * in what ever format the user has decided to use.
     * 
     * @param s4dFile : The full path of the *.s4d file that the user has 
     *                  written to configure the animator
     */
    private void readAnimationData(String s4dFile){
        
        //to be gotten by a pop up from the file menu in the future
        TextParser pRootFile = new TextParser(s4dFile, ",",",");
        
        
        String[][] aniFiles = pRootFile.getStringData();
        String leafTagName = null;
        int leafTagTempInt = 0;
        
        int numBodies = 0;
        int numForces = 0;
        boolean bodyFlag = false;
        boolean forceFlag = false;
        
        lightDirection = null;
        headlight = true;
        timeScaling = 1.0;
        downSampleFactor = 1;
        backgroundColour = new double[3];
        for(int z=0; z<3; z++)
            backgroundColour[z] = 0.95;
        
        for(int i = 0; i < aniFiles.length; i++){
            if(bodyFlag == true && aniFiles[i][0].compareTo("<\\bodyGEO>") != 0){
                numBodies = numBodies+1;
            }
            
            if(aniFiles[i][0].compareTo("<\\bodyGEO>") == 0){
                bodyFlag = false;
            }
            
            if(aniFiles[i][0].compareTo("<bodyGEO>")==0){
                bodyFlag = true;
            }
            
            if(forceFlag == true && aniFiles[i][0].compareTo("<\\forceTorque>") != 0){
                numForces = numForces+1;
            }
            
            if(aniFiles[i][0].compareTo("<\\forceTorque>") == 0){
                forceFlag = false;
            }
            
            if(aniFiles[i][0].compareTo("<forceTorque>")==0){
                forceFlag = true;
            }
        }
        
        
        dataFile = null;
        
        markerSize = new double[3];
        
        markerData = null;
        TextParser aniData = null;
        
        partTransXYZ = null;
        partRotMAT = null;
        
        double[][] fTTransXYZ = null;
        double[][] fVector = null;
        double[][] tVector = null;
        double fNorm = 1;
        double dNorm = 1;
        double tNorm = 1;
        
        cameraXYZ = null;
        cameraRotMAT = null;
        
        int rows = 0;
        int cols = 0;
        double[][] temp = null;
        
        int bodyCount = 0;
        int fTCount = 0;
        
        bodies = new BodyData[numBodies];
        
        if(numForces > 0)
            ft = new ForceTorqueData[numForces];
        else ft = null;
            
           
        cameraPath = null;
        
        boolean timeRead = false;
        boolean matricesSized = false;
        
        int i = 0;
        while(i < aniFiles.length){

            if(aniFiles[i][0].compareTo("<timeScaling>") == 0){
                    try{ 
                        timeScaling = Double.valueOf(aniFiles[i][1]);
                    
                    }catch(NumberFormatException nfe){
                        timeScaling = 1.0;
                    }
                        
            }
            
            if(aniFiles[i][0].compareTo("<sizeScaling>") == 0){
                    try{ 
                        sizeScaling = Double.valueOf(aniFiles[i][1]);
                    
                    }catch(NumberFormatException nfe){
                        sizeScaling = 1.0;
                    }
                    sizeScaling = 1.0; //I have not gotten this to work just yet.
                        
            }
            if(aniFiles[i][0].compareTo("<downSampling>") == 0){
                    try{ 
                        downSampleFactor = Integer.valueOf(aniFiles[i][1]);
                    
                    }catch(NumberFormatException nfe){
                        downSampleFactor = 1;
                    }
                        
            }
            
            if(aniFiles[i][0].compareTo("<backgroundColour>") == 0){
                    String temp2 = aniFiles[i][1];
                    int s = temp2.indexOf("<",0);
                    int e = temp2.indexOf(">",0);
                    temp2 = temp2.substring(s+1,e);
                    double[] rgbt = new double[4];
                    rgbt = SolUtil.parseRGBT(temp2, 0, temp2.length()-1,i+1);
                    for(int z=0; z<3;z++)
                        backgroundColour[z]=rgbt[z];
                        
            }
            
            if(aniFiles[i][0].compareTo("<lightDirection>") == 0){
                    String temp2 = aniFiles[i][1];
                    int s = temp2.indexOf("<",0);
                    int e = temp2.indexOf(">",0);
                    temp2 = temp2.substring(s+1,e);
                    lightDirection = new double[3];
                    lightDirection[0] = SolUtil.parseNumber(temp2, "X", " ", 0, temp2.length()-2, i+1);
                    lightDirection[1] = SolUtil.parseNumber(temp2, "Y", " ", 0, temp2.length()-2, i+1);
                    lightDirection[2] = SolUtil.parseNumber(temp2, "Z", " ", 0, temp2.length()-1, i+1);
                        
            }
            
            if(aniFiles[i][0].compareTo("<headlight>") == 0){
                    String temp2 = aniFiles[i][1];
                    double tempD = 0;
                    try{ 
                        tempD = Integer.valueOf(aniFiles[i][1]);
                    
                    }catch(NumberFormatException nfe){
                        tempD = 0;
                    }
                    
                    if(tempD > 0.5){
                        headlight = true;
                    }else{
                        headlight = false;
                    }
                        
            }
            
            if(aniFiles[i][0].compareTo("<bodyGEO>") == 0){
                i = i+1;
                while(aniFiles[i][0].compareTo("<\\bodyGEO>") != 0){
                    aniData = new TextParser(rootFile.getParent()  + "\\"+ aniFiles[i][1], "\t", "\t");
                    if(matricesSized == false){
                        rows = aniData.numRows();
                        cols = aniData.numCol();
                        partTransXYZ = new double[rows][3];
                        partRotMAT = new double[rows][9];
                        aniTime = new double[rows];
                        temp = new double[rows][cols];
                        matricesSized = true;
                    }

                    temp = aniData.getDoubleData();

                    for(int r = 0; r < rows; r++){
                        partTransXYZ[r][0] = temp[r][1];
                        partTransXYZ[r][1] = temp[r][2];
                        partTransXYZ[r][2] = temp[r][3];

                        partRotMAT[r][0] = temp[r][4];
                        partRotMAT[r][1] = temp[r][5];
                        partRotMAT[r][2] = temp[r][6];
                        partRotMAT[r][3] = temp[r][7];
                        partRotMAT[r][4] = temp[r][8];
                        partRotMAT[r][5] = temp[r][9];
                        partRotMAT[r][6] = temp[r][10];
                        partRotMAT[r][7] = temp[r][11];
                        partRotMAT[r][8] = temp[r][12];
                    }

                    if(timeRead == false){
                        for( int r = 0; r < rows; r++){
                            aniTime[r] = temp[r][0];
                        }
                        timeRead = true;
                    }
                    dataFile = new File(rootFile.getParent()  + "\\"+ aniFiles[i][0]);
                    
                    leafTagTempInt = aniFiles[i][1].indexOf(".");
                    leafTagName = new String(aniFiles[i][1].substring(0,leafTagTempInt));
                    bodies[bodyCount] = new BodyData(leafTagName,dataFile, partTransXYZ,partRotMAT, (int)downSampleFactor);
                    bodyCount++;
                    i++;
                }
            
           }
            
            
            
           if(aniFiles[i][0].compareTo("<camera>") == 0 ){
                
                i = i+1;
                cameraXYZ = new double[rows][3];
                cameraRotMAT = new double[rows][9];
                temp = new double[rows][cols];
                
                aniData = new TextParser(rootFile.getParent()  + "\\"+ aniFiles[i][0],"\t","\t");
                temp = aniData.getDoubleData();
                
                for(int r = 0; r < rows; r++){
                        cameraXYZ[r][0] = temp[r][1];
                        cameraXYZ[r][1] = temp[r][2];
                        cameraXYZ[r][2] = temp[r][3];

                        cameraRotMAT[r][0] = temp[r][4];
                        cameraRotMAT[r][1] = temp[r][5];
                        cameraRotMAT[r][2] = temp[r][6];
                        cameraRotMAT[r][3] = temp[r][7];
                        cameraRotMAT[r][4] = temp[r][8];
                        cameraRotMAT[r][5] = temp[r][9];
                        cameraRotMAT[r][6] = temp[r][10];
                        cameraRotMAT[r][7] = temp[r][11];
                        cameraRotMAT[r][8] = temp[r][12];
                    }
                
                leafTagTempInt = aniFiles[i][0].indexOf(".");
                leafTagName = new String(aniFiles[i][0].substring(0,leafTagTempInt));
                
                cameraPath = new BodyData(leafTagName,null, cameraXYZ, cameraRotMAT, downSampleFactor);
                
           }
            
            
            if(aniFiles[i][0].compareTo("<forceTorque>") == 0){
                String temp2;      
                temp2 = aniFiles[i][1];
                int p;
                int q;
                
                
                
                fNorm = SolUtil.parseNumber(temp2,"normF="," ",0,temp2.length()-1,i+1);
                    if(fNorm == Double.NaN) fNorm = 1;

                tNorm = SolUtil.parseNumber(temp2,"normT="," ",0,temp2.length()-1,i+1);
                    if(tNorm == Double.NaN) tNorm = 1;

                dNorm = SolUtil.parseNumber(temp2,"normD="," ",0,temp2.length(),i+1);
                    if(dNorm == Double.NaN) dNorm = 1;

                i = i+1;
                matricesSized = false;
                boolean[] plotFlags = new boolean[6];
                double[] plotOptions = new double[8];
                double[] rgbt = new double[3];
                int s = 0;
                int e = 0;
                
                while(aniFiles[i][0].compareTo("<\\forceTorque>") != 0){
                    aniData = new TextParser(rootFile.getParent()  + "\\"+ aniFiles[i][0], "\t", "\t");

                    for(int z = 0; z < plotFlags.length; z++)
                        plotFlags[z]=false;
                    for(int z = 0; z < plotOptions.length; z++)
                        plotOptions[z]=0;
                    
                    //Parse force history plot data
                    temp2 = aniFiles[i][1];

                    s = temp2.indexOf("<",0);
                    e = temp2.indexOf(">",0);
                    if(SolUtil.parseNumber(temp2,"f"," ",s,e,i+1) == 1){ 
                        plotFlags[0] = true;
                        if(SolUtil.parseNumber(temp2,"w"," ",s,e,i+1) == 1) 
                            plotFlags[1] = true;
                        if(SolUtil.parseNumber(temp2,"c"," ",s,e,i+1) == 1){ 
                            plotFlags[2] = true;
                            rgbt = SolUtil.parseRGBT(temp2,s,e,i+1);
                            for(int z = 0; z <4; z++)
                                plotOptions[z]=rgbt[z];
                        }
                     }
                    
                    s = temp2.indexOf("<",e);
                    e = temp2.indexOf(">",s);
                    if(SolUtil.parseNumber(temp2,"t"," ",s,e,i+1) == 1){ 
                        plotFlags[3] = true;
                        if(SolUtil.parseNumber(temp2,"w"," ",s,e,i+1) == 1) 
                            plotFlags[4] = true;
                        if(SolUtil.parseNumber(temp2,"c"," ",s,e,i+1) == 1){ 
                            plotFlags[5] = true;
                            rgbt = SolUtil.parseRGBT(temp2,s,e,i+1);
                            for(int z = 0; z <4; z++)
                                plotOptions[z+4]=rgbt[z];
                        }
                     }
                    
                    
                    if(matricesSized == false){
                        rows = aniData.numRows();
                        cols = aniData.numCol();
                        fTTransXYZ = new double[rows][3];
                        fVector = new double[rows][3];
                        tVector = new double[rows][3];
                        temp = new double[rows][cols];        
                        matricesSized = true;
                    }

                    temp = aniData.getDoubleData();

                    for(int r = 0; r < rows; r++){
                        fTTransXYZ[r][0] = temp[r][1];
                        fTTransXYZ[r][1] = temp[r][2];
                        fTTransXYZ[r][2] = temp[r][3];

                        fVector[r][0] = temp[r][4];
                        fVector[r][1] = temp[r][5];
                        fVector[r][2] = temp[r][6];
                        
                        tVector[r][0] = temp[r][7];
                        tVector[r][1] = temp[r][8];
                        tVector[r][2] = temp[r][9];
                    }

                    File forceFile = new File("../../SolvereLibs/WRL_SYNTAX/lib_force.wrl");
                    File torqueFile = new File("../../SolvereLibs/WRL_SYNTAX/lib_torque.wrl");
                    
                    leafTagTempInt = aniFiles[i][0].indexOf(".");
                    leafTagName = new String(aniFiles[i][0].substring(0,leafTagTempInt));
                    ft[fTCount] = new ForceTorqueData(leafTagName, forceFile,torqueFile,fTTransXYZ,fVector,tVector, fNorm, tNorm, dNorm, plotFlags,plotOptions, downSampleFactor);
                    fTCount++;
                    i++;
                }
            
           }
            
            if(aniFiles[i][0].compareTo("<markers>") == 0){
                int j = i+1;
                int numMarkerSets = 0;
                while(aniFiles[j][0].compareTo("<\\markers>") != 0){
                    numMarkerSets++;
                    j++;
                }
                
                markerData = new MarkerData[numMarkerSets];
                for(int k = 0; k < numMarkerSets; k++){
                        i = i+1;
                        temp = null;
                        String markerProp = aniFiles[i][1];
                        aniData = new TextParser(rootFile.getParent() + "\\" + aniFiles[i][0],"\t","\t");
                        markerPos = aniData.getDoubleData();
                        markerRGB = new double[3];

                        int s = markerProp.indexOf("<",0);
                        int e = markerProp.indexOf(">",0);
                        markerProp = markerProp.substring(s+1,e);
                        s=0;
                        e=markerProp.length();
                        
                        String tempProp = "";
                        Double tempPropD = null;
                        int markerShape = 0;

                        int p = markerProp.indexOf("sphere");
                        int q = markerProp.indexOf("cylinder");
                        int r = markerProp.indexOf("cone");
                        s = markerProp.indexOf("box");

                        if(p != -1) markerShape = MarkerData.SHAPE_SPHERE;
                        if(q != -1) markerShape = MarkerData.SHAPE_CYLINDER;
                        if(r != -1) markerShape = MarkerData.SHAPE_CONE;
                        if(s != -1) markerShape = MarkerData.SHAPE_BOX;

                        markerSize[0] = 0.1;
                        markerSize[1] = 0.1;
                        markerSize[2] = 0.1;

                        
                        s = 0;
                        e = markerProp.length();
                        if(markerShape != MarkerData.SHAPE_BOX){
                            markerSize[0] = SolUtil.parseNumber(markerProp," r"," ",s,e-1,i+1);
                            if(markerShape != MarkerData.SHAPE_SPHERE)
                                markerSize[1] = SolUtil.parseNumber(markerProp," h"," ",s,e-1,i+1);
                        }else{
                            markerSize[0] = SolUtil.parseNumber(markerProp," x"," ",s,e-1,i+1);
                            markerSize[1] = SolUtil.parseNumber(markerProp," y"," ",s,e-1,i+1);
                            markerSize[2] = SolUtil.parseNumber(markerProp," z"," ",s,e-1,i+1);
                        }
                        
                        String tempRGB = markerProp.substring(markerProp.indexOf("R"), e);
                        double[] tempRGBT = SolUtil.parseRGBT(tempRGB,0,tempRGB.length(),i+1);
                        
                        for(int z=0; z<3; z++)
                            markerRGB[z] = tempRGBT[z];

                        markerTrans = tempRGBT[3];

                        markerData[k] = new MarkerData(markerShape,markerPos,markerSize,markerRGB,markerTrans);
                }
            }
            
            if(aniFiles[i][0].compareTo("<plot3D>") == 0){
                int j = i+1;
                    int numPlotSets = 0;
                    while(aniFiles[j][0].compareTo("<\\plot3D>") != 0){
                        numPlotSets++;
                        j++;
                    }
                    plot3Ddata = new Plot3D[numPlotSets];
                    
                    double[][] plotData;
                    double[] plotOptions = new double[4];
                    double[] rgbt;
                    boolean plotFlags = false;
                    
                    String tag;
                    String plotLabel;
                    int s=0;
                    int e=0;
                    
                for(int k=0; k < numPlotSets; k++){
                    i=i+1;
                    String temp2  = aniFiles[i][1];
                    aniData = new TextParser(rootFile.getParent() + "\\" + aniFiles[i][0],"\t","\t");
                    plotData = aniData.getDoubleData();

                    s = temp2.indexOf("<",0);
                    e = temp2.indexOf(">",0);
                    temp2 = temp2.substring(s+1,e);
                    s=0;
                    e=temp2.length();
                   
                    if( SolUtil.parseNumber(temp2,"w"," ",s,e,i+1) == 1){ 
                        plotFlags = true;
                    }
                           
                    rgbt = SolUtil.parseRGBT(temp2,s,e-1,i+1);
                    for(int z = 0; z <4; z++)
                        plotOptions[z]=rgbt[z];
                   
                    double scale = SolUtil.parseNumber(temp2," s"," ",s,temp2.length(), i+1);
                    boolean addMarker=false;
                    double[] markerOptions = new double[3];
                    if(SolUtil.parseNumber(temp2," m"," ",s,temp2.length(), i+1) > 0.5){
                        addMarker=true;
                        double[] tempRGBT = SolUtil.parseRGBT(temp2,temp2.indexOf(" m"), temp2.indexOf("\""),i+1); 
                        for(int z=0; z<3; z++)
                            markerOptions[z] = tempRGBT[z];
                    }
                    
                    s = temp2.indexOf("\"",s);
                    e = temp2.indexOf("\"", s+1);
                    if(s != -1 && e != -1) plotLabel = temp2.substring(s+1, e);
                    else plotLabel = null;
                    
                    //(String tag, double[][] data, boolean wireFrame, double[] plotOptions, String label)
                    double[] labelOptions = new double[3];
                    
                    if(plotLabel != null){
                        double[] tempRGBT =  SolUtil.parseRGBT(temp2,e, temp2.length(), i+1); 
                        for(int z=0; z<3; z++)
                            labelOptions[z] = tempRGBT[z];
                    }
                    
                    
                    
                    tag = aniFiles[i][0].substring(0, aniFiles[i][0].indexOf("."));
                    plot3Ddata[k] = new Plot3D(tag, plotData, plotFlags, plotOptions, scale,plotLabel, labelOptions, addMarker, markerOptions);
                    
                }
                    
            }
            
            if(aniFiles[i][0].compareTo("<stickFigures>") == 0){
                int j = i+1;
                int numStickSets = 0;
                while(aniFiles[j][0].compareTo("<\\stickFigures>") != 0){
                    numStickSets++;
                    j++;
                }
                stickFigures = new StickFigure[numStickSets];

                double[][] stickData;
                double[] stickOptions = new double[3];
                
                
                for(int k=0; k < numStickSets; k++){
                    i=i+1;
                    String temp2  = aniFiles[i][1];
                    aniData = new TextParser(rootFile.getParent() + "\\" + aniFiles[i][0],"\t","\t");
                    stickData = aniData.getDoubleData();
                    temp2 = aniFiles[i][1];
                    int s = temp2.indexOf("<",0);
                    int e = temp2.indexOf(">",0);
                    temp2 = temp2.substring(s+1,e);
                    
                    double[] rgbtemp = SolUtil.parseRGBT(temp2,0,temp2.length(),i+1);
                    for(int z=0; z<3; z++) 
                        stickOptions[z] = rgbtemp[z]; 
                    String tag = aniFiles[i][0].substring(0, aniFiles[i][0].indexOf("."));
                    stickFigures[k] = new StickFigure(tag, stickData, stickOptions);
                    
                }     
            }
            
            if(aniFiles[i][0].compareTo("<movingLabels>") == 0){
                int j = i+1;
                int numLabels = 0;
                while(aniFiles[j][0].compareTo("<\\movingLabels>") != 0){
                    numLabels++;
                    j++;
                }
                labels3D = new Label3D[numLabels];
                
                String tag = null;
                String text = null;
                double scaling = 0;
                double[] rgb = new double[3];
                double[][] data = null;
                int s = 0;
                int e = 0;
                
                for(int k=0; k<numLabels; k++){
                    i=i+1;
                    
                    aniData = new TextParser(rootFile.getParent() + "\\" + aniFiles[i][0],"\t","\t");
                    data = aniData.getDoubleData();
                    String temp2 = aniFiles[i][1];
                    s = temp2.indexOf("<",0);
                    e = temp2.indexOf(">",0);
                    temp2 = temp2.substring(s+1,e);
                    
                    tag = aniFiles[i][0].substring(0, aniFiles[i][0].indexOf("."));
                    s = temp2.indexOf("\"");
                    e = temp2.indexOf("\"",s+1);
                    text = temp2.substring(s+1, e);
                    scaling = SolUtil.parseNumber(temp2," s"," ", e, temp2.length()-1, i+1);
                    double[] rgbtemp = SolUtil.parseRGBT(temp2,e,temp2.length()-1, i+1);
                    for(int z=0; z<3; z++) 
                        rgb[z] = rgbtemp[z];
                    
                    labels3D[k] = new Label3D(tag, text, scaling, rgb, data);
                }
            
            }
            
            i = i+1;
            
        }

     
        //Here we write the code to write the *.x3d or *.x3dv file
        //If geometry files are x3d write x3d? If they are vrml, write .x3dv?       
        
    }
    
   
}

