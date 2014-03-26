package com.att.aro.pcap.windows;

import com.att.aro.commonui.MessageDialogFactory;
import com.att.aro.util.Util;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: AROUser
 * Date: 10/8/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class WinPacketCapture extends Thread {

    private static final Logger logger = Logger.getLogger(WinPacketCapture.class.getName());
    public boolean  setupFlag;

    private boolean cancelFlag;
    JFrame f;
    private Date pcapStartTime;

    public void initiate()
    {
        f=new JFrame("PacketCapture");
    }

    /**
     *
     * @return
     */
    public String getResourceHome()
    {

        //String resourcePath = Util.getCurrentRunningDir() + Util.FILE_SEPARATOR + "bin\\";
        String resourcePath = Util.getCurrentRunningDir();

        File workspace = new File(resourcePath);
        //resourcePath = workspace.getParent() + Util.FILE_SEPARATOR + "AROLib\\bin" + Util.FILE_SEPARATOR;
        resourcePath = workspace.getParent() + Util.FILE_SEPARATOR + "bin" + Util.FILE_SEPARATOR;
        try{
            resourcePath = URLDecoder.decode(resourcePath, "UTF-8"); //To avoid the %20(space) problem in windows relative path
        } catch (UnsupportedEncodingException uex){
            uex.printStackTrace();
        }
        //String currentDir = System.getProperty("user.dir");
        //String resourcePath = currentDir + Util.FILE_SEPARATOR + "AROResources\\bin\\";
      return resourcePath;

    }

    /**
     * Setup command will create a new virtual wifi.
     * wait until device is connected to virtual wifi
     * @return
     */
    private int preStartSetup()
    {
        String setupcmd = getResourceHome().concat("setup.cmd");
        logger.info("Resource Path " + setupcmd);

        execute(setupcmd);
        setupFlag = true;

        Object[] options = {"OK", "CANCEL"};
        String message = "Please connect your mobile device to Virtual Wifi network AROROCKS with password arorocks. Once connected, Click OK to start packet capture";
        int dialogResult = JOptionPane.showOptionDialog(null, message, "Connect your device", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,null,options,options[0]);
        return dialogResult;

    }

    /**
     * Start captureing the trace file
     * @param fileName
     */
    public void startPacketCapture(String fileName)
    {
        int dialogResult =0;

        // execute windump -D and load the devices into
        //String windumpCmd = "windump -i 5 -w  "  + fileName + "  & exit"   ; // for start the packet capture

        Map<String, String> preStartDevices = getListOfDevices(); // getting the devices info before virtual wifi starts

        if(preStartDevices.isEmpty()){
            /**
             * In case device's are not recognized by windump, run windump as administarator to fix
             * possible registry issues.
             */
            String windumpasAdmin = "windump -D &exit";
            execute(windumpasAdmin);
            preStartDevices = getListOfDevices();

            if(preStartDevices.isEmpty()){
                this.cancelFlag = true;
                logger.info("No Devices are recognized by windump ");
                MessageDialogFactory.showMessageDialog(null, " NO DEVICES FOUND, PLEASE VERIFY YOUR WINDUMP INSTALLATION AND RUN WINDUMP  ");
                return;
            }
        }

        if(!setupFlag)
        {
            dialogResult = preStartSetup();
        }

         Map<String, String> postStartDevices = getListOfDevices(); // getting the devices info after virtual wifi starts

         if(dialogResult==JOptionPane.YES_OPTION)
         {
             String wirelessid =  getWirelessDeviceId(preStartDevices,postStartDevices);

             logger.info("Device ID " + wirelessid);
             if(wirelessid == null){
                 logger.info("User cancel " + wirelessid);
                 this.cancelFlag = true;
                 return;
             }


             String windumpCmd = "windump -i "+wirelessid +" -s 0 -w "  + fileName + "  & exit"   ;
             logger.info("Windump Cmd " + windumpCmd);

         // String startCmd = getResourceHome().concat("start_pcap.cmd");
        //  execute(startCmd);

             this.pcapStartTime = new Date();
             execute(windumpCmd);

             File traceFile = new File(fileName);
             int waitTime=0;
             while(!traceFile.exists()){

                 //Add the delay for file to create
                 try {
                     Thread.sleep(500);
                 } catch (InterruptedException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     logger.info("Thread inturrepted while delaying process ");
                 }
                 if(waitTime == 10){
                     this.cancelFlag = true;
                     logger.info(" Trace file is not created ");
                     logger.info(" Waited for a minute for windows prompt set ");
                     break;
                 }
                 waitTime++;
             }
         }
        else
          {
              this.cancelFlag = true;
              return;
          }


    }

    /**
     * Execute the command for stopping the packet capture
     */
    public void stopPacketCapture() {
      String stopCmd = getResourceHome().concat("stop_pcap.cmd");
      execute(stopCmd);
        setupFlag= false;
    }

    /**
     * Execute the windows commands
     * @param cmd
     */
    public  void execute(String cmd)
    {
        try {

            logger.info("-- Setting up WLAN --");
            logger.info("Resource Home " + getResourceHome());
            String osVersion = System.getProperty("os.arch");
            logger.info("OS :" +osVersion );
            logger.info("OS ARCH info :"+System.getenv("ProgramFiles(X86)"));
            logger.info("OS ARCH info :"+System.getenv("ProgramFiles"));
            logger.info("OS :" + System.getProperty("sun.arch.data.model"));

            String netshCommand =  cmd;
            //Elevate the command line as administrator
            String[] elevateCommand = null;
            elevateCommand = new String[]{getResourceHome().concat("Elevate.exe"), "-wait", "-k", netshCommand};

            ProcessBuilder pb1 = new ProcessBuilder(elevateCommand);
            Process p1 = pb1.start();
            p1.waitFor();

            BufferedReader reader=new BufferedReader(
                    new InputStreamReader(p1.getInputStream())
            );
            String output;
            while((output = reader.readLine()) != null){
                logger.info(output);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * executing separately with windows cmd.exe for getting the device info. If we run with elevate.exe
     * new command prompt is creating, not able to capture the command line output for finding the device number
     *
     * @return
     * @deprecated
     */
    @Deprecated
    public String getWirelessDeviceId(){
        String deviceId= null;
        try{
            //String windumpCmd = "windump -D"; for getting the resource info
            String[] windumpCommand = new String[]{"cmd","/c","windump -D &exit"};
            ProcessBuilder pb2 = new ProcessBuilder(windumpCommand);
            Process p2 = pb2.start();
            //p1.waitFor();
            BufferedReader windumpReader=new BufferedReader(
                    new InputStreamReader(p2.getInputStream())
            );
            String line;
            while((line = windumpReader.readLine()) != null){
                //System.out.println(output);
                int i = line.toUpperCase().indexOf(".");
                if(i>0){
                    deviceId = line.substring(0, i);
                }

            }

        } catch(Exception ex){
            ex.printStackTrace();
        }
        return deviceId;
    }

    /**
     * Compare the devices before and after start the virtual wifi to find the virtual wifi
     * device id. New device id which created is the virtual wifi device id.
     *
     * @param beforeVwifiStart
     * @param afterVwifiStart
     * @return
     */
    private String getWirelessDeviceId(Map<String, String> beforeVwifiStart, Map<String, String> afterVwifiStart){
        String deviceId= null;

         if(!beforeVwifiStart.isEmpty() && !afterVwifiStart.isEmpty()){

            for (String device: afterVwifiStart.keySet()){

                if(!beforeVwifiStart.containsKey(device)){

                    deviceId = afterVwifiStart.get(device);
                    break;
                }

            }

        }

        logger.info(" Device ID " + deviceId);
        return deviceId;
    }

    /**
     * executing separately with windows cmd.exe for getting the device info. If we run with elevate.exe
     * new command prompt is creating, not able to capture the command line output for finding the device number
     * Getting the current list of devices
     *
     * @return
     */
    private Map<String, String> getListOfDevices(){
        Map<String, String> devices = new HashMap<String, String>();
        try{
            //String windumpCmd = "windump -D"; for getting the resource info
            String[] windumpCommand = new String[]{"cmd","/c","windump -D &exit"};
            ProcessBuilder pb2 = new ProcessBuilder(windumpCommand);
            Process p2 = pb2.start();
            //p1.waitFor();
            BufferedReader windumpReader=new BufferedReader(
                    new InputStreamReader(p2.getInputStream())
            );
            String line;
            while((line = windumpReader.readLine()) != null){
                //System.out.println(output);
                if(line.contains("windump")){
                    break;
                }

                int i = line.indexOf(".");
                if(i>0){
                    devices.put(line.substring(i+1),line.substring(0, i));
                }
            }

        } catch(Exception ex){
            ex.printStackTrace();
        }
        logger.info(" Size of the list " + devices.size());
        return devices;
    }

    /**
     * get the start time of trace
     * @return
     */
    public Date getPcapStartTime(){
        return this.pcapStartTime;
    }

    /**
     * When not conected to wifi
     * @return
     */
    public boolean isCancelFlag() {
        return cancelFlag;
    }

}
