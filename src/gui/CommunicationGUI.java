package gui;

import AppConstant.AppConstant;
import RestaurantAgents.BaseAgent;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.html.*;


public class CommunicationGUI extends Thread implements BaseAgent.UiMessageListener {

    final JTextPane agentCommunicationTextField = new JTextPane();
    final JTextPane onlineChefListTextField = new JTextPane();
    final JTextPane onlineWaiterListTextField = new JTextPane();
    final JTextPane onlineCustomersListTextField = new JTextPane();

    // communcation screen width and height
    int width = 721;
    int height = 510;

    //singleton pattern
    private static CommunicationGUI communicationGUI = null;

    public static CommunicationGUI getInstance(boolean isCreateInstance) {
        if (isCreateInstance) {
            if (communicationGUI == null)
                communicationGUI = new CommunicationGUI();
            return communicationGUI;
        }
        return null;
    }

    private CommunicationGUI() {
        String fontFamilyName = "Arial, sans-serif";
        Font font = new Font(fontFamilyName, Font.PLAIN, 14);

        final JFrame jframeGUI = new JFrame(AppConstant.RestaurantName);
        jframeGUI.getContentPane().setLayout(null);
        jframeGUI.setSize(width, height);
        jframeGUI.setResizable(false);
        Dimension dimen = Toolkit.getDefaultToolkit().getScreenSize();
        jframeGUI.setLocation(40, dimen.height / 2 - jframeGUI.getSize().height / 2);
        jframeGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // communication list box field
        agentCommunicationTextField.setBounds(25, 25, 490, 420);
        agentCommunicationTextField.setFont(font);
        agentCommunicationTextField.setMargin(new Insets(0, 5, 0, 3));
        agentCommunicationTextField.setEditable(false);
        agentCommunicationTextField.setContentType("text/html");
        agentCommunicationTextField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        JScrollPane agentComScrool = new JScrollPane(agentCommunicationTextField);
        agentComScrool.setBounds(25, 25, 490, 420);

        // Chef users list
        onlineChefListTextField.setBounds(520, 25, 156, 140);
        onlineChefListTextField.setEditable(false);
        onlineChefListTextField.setFont(font);
        onlineChefListTextField.setMargin(new Insets(6, 3, 6, 2));
        onlineChefListTextField.setEditable(false);
        onlineChefListTextField.setContentType("text/html");
        onlineChefListTextField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        JScrollPane scrollPane = new JScrollPane(onlineChefListTextField);
        scrollPane.setBounds(520, 25, 156, 140);

        // waiter user list
        onlineWaiterListTextField.setBounds(520, 165, 156, 140);
        onlineWaiterListTextField.setEditable(false);
        onlineWaiterListTextField.setFont(font);
        onlineWaiterListTextField.setMargin(new Insets(6, 3, 6, 2));
        onlineWaiterListTextField.setEditable(false);
        onlineWaiterListTextField.setContentType("text/html");
        onlineWaiterListTextField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        // scroll for waiter
        JScrollPane scrollPane1 = new JScrollPane(onlineWaiterListTextField);
        scrollPane1.setBounds(520, 165, 156, 140);

        // customer user list
        onlineCustomersListTextField.setBounds(520, 305, 156, 140);
        onlineCustomersListTextField.setEditable(false);
        onlineCustomersListTextField.setFont(font);
        onlineCustomersListTextField.setMargin(new Insets(6, 3, 6, 2));
        onlineCustomersListTextField.setEditable(false);
        onlineCustomersListTextField.setContentType("text/html");
        onlineCustomersListTextField.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

        JScrollPane scrollPane3 = new JScrollPane(onlineCustomersListTextField);
        scrollPane3.setBounds(520, 305, 156, 140);

        agentCommunicationTextField.setBackground(Color.LIGHT_GRAY);
        onlineChefListTextField.setBackground(Color.LIGHT_GRAY);
        onlineWaiterListTextField.setBackground(Color.LIGHT_GRAY);
        onlineCustomersListTextField.setBackground(Color.LIGHT_GRAY);

        // add the ui components into the Jframe
        jframeGUI.add(agentComScrool);
        jframeGUI.add(scrollPane1);
        jframeGUI.add(scrollPane);
        jframeGUI.add(scrollPane3);
        jframeGUI.setVisible(true);

        appendedTextToThePane(agentCommunicationTextField, "<h1>ContactLess Order Placing System</h1>"
                + "<h3>Made by Umair</h3>");
        onlineChefListTextField.setText("<h3>Online Kitchen Chef</h3>");
        onlineWaiterListTextField.setText("<h3>Online Waiters</h3>");
        onlineCustomersListTextField.setText("<h3>Online Customers</h3>");
    }

    // format the text into HTML tags
    private void appendedTextToThePane(JTextPane tp, String msg) {
        HTMLDocument document = (HTMLDocument) tp.getDocument();
        HTMLEditorKit htmlEditor = (HTMLEditorKit) tp.getEditorKit();
        try {
            htmlEditor.insertHTML(document, document.getLength(), msg, 0, 0, null);
            tp.setCaretPosition(document.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeMessagesOntheGUI(String msg) {
        // info sur le Chat
        appendedTextToThePane(agentCommunicationTextField, "<p>* " + msg + "</p>");
    }

    @Override
    public void writeChefAgentNameOnTheGUI(List<String> msg) {
        // info sur le Chat
        onlineChefListTextField.setText("");
        StringBuilder text = new StringBuilder("<h3>Online Kitchen Chef</h3><p>");
        for (String a : msg) {
            text.append("* ").append(a).append("<br>");
        }
        text.append("</p>");
        onlineChefListTextField.setText(text.toString());
    }

    @Override
    public void writeWaiterAgentNameOnTheGUI(List<String> msg) {
        // info sur le Chat
        onlineWaiterListTextField.setText("");
        StringBuilder text = new StringBuilder("<h3>Online Waiters</h3><p>");
        for (String a : msg) {
            text.append("* ").append(a).append("<br>");
        }
        text.append("</p>");
        onlineWaiterListTextField.setText(text.toString());
    }

    @Override
    public void writeCustomerAgentNameOnTheGUI(List<String> msg) {
        // info sur le Chat
        onlineCustomersListTextField.setText("");
        StringBuilder text = new StringBuilder("<h3>Online Customer</h3><p>");
        for (String a : msg) {
            text.append("* ").append(a).append("<br>");
        }
        text.append("</p>");
        onlineCustomersListTextField.setText(text.toString());
    }

}