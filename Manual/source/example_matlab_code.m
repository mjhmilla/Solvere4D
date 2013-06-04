%%
% Write the VRML data files
%%

if flag_genVRMLFiles == 1

 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
 %
 % 1. Copy over the files in the support directory
 % 
 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


    tempDir = pwd;

    aniDir = ['C:\mjhmilla_TEMP\FPE\Animation'];
    cd(aniDir);
    supDir = [aniDir,'\suppfiles']; 

    subDir = ['S',num2str(i)];
    expDir = expName;
    trialDir = ['T',num2str(k)];

    %%
    %Get into the proper directory
    %%

    if isdir(subDir)
        cd(subDir);
    else
        mkdir(subDir);
        cd(subDir);
    end

    if isdir(expDir)
        cd(expDir);
    else
        mkdir(expDir);
        cd(expDir);
    end

    if isdir(trialDir)
        cd(trialDir);
    else
        mkdir(trialDir);
        cd(trialDir);
    end

    fileDir = pwd;

 %Now we are in the directory we would like to 
 %have the support files in. Time to get DOS to
 %copy them over for us

    dos(['xcopy ',supDir, ' /e /Y']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% 2. Generate your data and write it to the appropriate 
%    *.dat files using the following command for each 
%    matrix of data:
%
%      dlmwrite(['filename.dat'], matrixName, '\t');
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% 3. Create the *.bat file required to run Solvere4D
%    then call it from DOS to create our new animation
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


    goToS4D = 'cd C%s\\Solvere4D\\build\\classes';

    runRunSolver4D = ['java Solvere4D.Solvere4D', ...
                       ' C:/exampleFolder/exampleFile.s4d'];
    goToHomeDir = pwd;
    goToHomeDir = 'cd ' + curDir;

 %Write the *.bat file
    fid = fopen('createFPE.bat', 'w');
    fprintf(fid, goToS4D,':');
    fprintf(fid, '\n');
    fwrite(fid, runSolvere4D);
    fwrite(fid, goToHomeDir);
    fclose(fid);

 %Run the *.bat file to get Solvere4D to create the animation
    dos('createFPE.bat');

 %Rename the animation file and copy it to another directory
    dos(['copy exampleFile.wrl ', 'C:\AnotherFolder\']);
end

%%
% Done Writing the VRML data files
%%