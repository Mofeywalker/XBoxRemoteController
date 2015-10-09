/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xboxremotecontroller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Moritz
 */
public class ManagementController {

    public static String START_CAMERA = "startCamera";
    public static String STOP_CAMERA = "stopCamera";
    
    private String host;
    private int port;
    
    public ManagementController(String host_, int port_) {
        this.host = host_;
        this.port = port_;
    }
    
    public void startCamera() {
        try {
            Socket sock = new Socket(host, port);
            DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
            outToServer.writeBytes(START_CAMERA +'\n');
            sock.close();
            System.out.println("startCamera");
        } catch (IOException ex) {
            Logger.getLogger(ManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void stopCamera() {
        try {
            Socket sock = new Socket(host, port);
            DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
            outToServer.writeBytes(STOP_CAMERA +'\n');
            sock.close();
            System.out.println("stopCamera");
        } catch (IOException ex) {
            Logger.getLogger(ManagementController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
