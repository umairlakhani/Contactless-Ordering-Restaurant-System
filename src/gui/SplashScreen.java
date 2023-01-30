package gui;

import AppConstant.AppConstant;
import RestaurantAgents.BaseAgent;

import javax.swing.*;
import java.awt.Frame;
import java.awt.*;

public class SplashScreen extends Frame {

    // To call writeOnUiField method using interface
    public CommunicationGUI communicationGUI;

    // splash screen width and height
    int width = 600;
    int height = 600;

    // Singleton pattern because I need only one object ob GUI
    private static SplashScreen splashScreen = new SplashScreen();

    public static SplashScreen getInstance() {
        if (splashScreen == null)
            splashScreen = new SplashScreen();
        return splashScreen;
    }

    //Constructor
    private SplashScreen() {
        try {
            JWindow splashWindow = new JWindow();
            splashWindow.getContentPane().add(
                    new JLabel("", new ImageIcon(BaseAgent.class.getResource("/images/contactless_logo.jpg")), SwingConstants.CENTER)).setBackground(new Color(0, 0, 100, 0));

            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = 40;
            int y = (screen.height - height) / 2;
            splashWindow.setBounds(x, y, width, height);

            splashWindow.setVisible(true);
            try {
                Thread.sleep(AppConstant.SplashScreenStayTime); // to show the splash screen for 3 seconds
                splashWindow.setVisible(false); // hide splash
                communicationGUI = CommunicationGUI.getInstance(true); // show the main communication container
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {

        }
    }
}
