/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xboxremotecontroller;

import ch.aplu.xboxcontroller.XboxController;
import ch.aplu.xboxcontroller.XboxControllerAdapter;
import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Moritz
 */
public class XBoxRemoteController implements Runnable {

    private static int PORT = 12001;
    private static int RESOLUTION = 100;

    private XboxController xc;

    private int leftThrottle;
    private int forwardLeft = 1;

    private int rightThrottle;
    private int forwardRight = 1;

    private boolean running = true;

    private String remoteHost;
    private int interval;

    public XBoxRemoteController(String _remoteHost, int _interval) {
        this.remoteHost = _remoteHost;
        this.interval = _interval;
    }

    @Override
    public void run() {
        // init controller
        xc = new XboxController(is64bit() ? "xboxcontroller64" : "xboxcontroller", 1, 50, 50);

        // check if controller is connected
        if (!xc.isConnected()) {
            JOptionPane.showMessageDialog(null,
                    "Xbox controller not connected.",
                    "Fatal error",
                    JOptionPane.ERROR_MESSAGE);
            xc.release();
            return;
        }

        xc.addXboxControllerListener(new XboxControllerAdapter() {
            public void leftTrigger(double value) {
                leftThrottle = (int) (value * RESOLUTION);
            }

            public void rightShoulder(boolean pressed) {
                if (pressed) {
                    forwardRight = -1;
                } else {
                    forwardRight = 1;
                }
            }

            public void rightTrigger(double value) {
                rightThrottle = (int) (value * RESOLUTION);
            }

            public void leftShoulder(boolean pressed) {
                if (pressed) {
                    forwardLeft = -1;
                } else {
                    forwardLeft = 1;
                }
            }
        });

        try {
            ServerSocket serverSock = new ServerSocket(PORT);
            while (running) {
                Socket incomingSocket = serverSock.accept();
                incomingSocket.setTcpNoDelay(true);
                PrintWriter out = new PrintWriter(incomingSocket.getOutputStream(), true);
                while (running) {
                    out.println(createPayload(leftThrottle, forwardLeft, rightThrottle, forwardRight));
                    TimeUnit.MILLISECONDS.sleep(interval);
                }
                incomingSocket.close();
            }
            serverSock.close();
        } catch (IOException ex) {
            Logger.getLogger(XBoxRemoteController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(XBoxRemoteController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        xc.release();

        // create datagramsocket, old way via udp
/*        
         byte[] buffer;
        
         while(running) {
         try {
         buffer = createPayload(leftThrottle, forwardLeft, rightThrottle, forwardRight).getBytes();
                
         try {
         DatagramSocket datagramSocket = new DatagramSocket();
         InetAddress receiverAddress = InetAddress.getByName(remoteHost);
         DatagramPacket packet = new DatagramPacket(buffer, buffer.length, receiverAddress, PORT);
         datagramSocket.send(packet);
                    
         } catch (UnknownHostException ex) {
         Logger.getLogger(XBoxRemoteController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SocketException ex) {
         Logger.getLogger(XBoxRemoteController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
         Logger.getLogger(XBoxRemoteController.class.getName()).log(Level.SEVERE, null, ex);
         }
                
         TimeUnit.MILLISECONDS.sleep(interval);
         } catch (InterruptedException ex) {
         Logger.getLogger(XBoxRemoteController.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
         */
    }

    public String createPayload(int leftThrottle, int forwardLeft, int rightThrottle, int forwardRight) {
        return leftThrottle + "||" + forwardLeft + "||" + rightThrottle + "||" + forwardRight;
    }

    public void stopServer() {
        this.running = false;
    }

    static boolean is64bit() {
        return System.getProperty("sun.arch.data.model").equals("64");
    }
}
