package com.att.aro.pcap.windows;

import com.att.aro.util.Util;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
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

        if(!setupFlag)
        {
            dialogResult = preStartSetup();
        }


          if(dialogResult==JOptionPane.YES_OPTION)
         {
             String wirelessid =  getWirelessDeviceId();
             logger.info("Device ID " + wirelessid);

             String windumpCmd = "";
             if(wirelessid != null){
                 windumpCmd = "windump -i "+wirelessid +" -w  "  + fileName + "  & exit"   ;
                 logger.info("Windump Cmd " + windumpCmd);
             }else{
                 windumpCmd = "windump -i 5 -w  "  + fileName + "  & exit"   ;
             }
         // String startCmd = getResourceHome().concat("start_pcap.cmd");
        //  execute(startCmd);

             this.pcapStartTime = new Date();
             execute(windumpCmd);
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

            String netshCommand =  cmd;
            //Elevate the command line as administrator
            String[] elevateCommand = new String[]{getResourceHome().concat("elevate.exe"), "-wait", "-k", netshCommand};
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
     */
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
                if(line.toUpperCase().contains("MICROSOFT")){
                    int i = line.toUpperCase().indexOf(".");
                    if(i>0){
                        deviceId = line.substring(0, i);
                    }
                }
            }

        } catch(Exception ex){
            ex.printStackTrace();
        }
        return deviceId;
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
