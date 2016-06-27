package im;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.OfficeBlue2007Skin;
import org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel;  
  
/**
 * java 即时通讯工具
 * @author xuguanglv
 *
 */
public class ChatGUI extends JFrame  
{  
    private static final long serialVersionUID = 1L;  
    private JTextField newNickName = new JTextField(6);  
    private JTextField friendAddress = new JTextField(9);  
    private JTextArea dialog = new JTextArea(15, 30);  
    private JTextArea input = new JTextArea(5, 40);  
    private JButton sendButton = new JButton("send");  
    private JButton changeNickName = new JButton("change");  
    private JButton requestChat = new JButton("invite");  
    private String nickName = null;  
    private LinkedBlockingQueue<String> sendMsgQueue = new LinkedBlockingQueue<String>();  
    private String inputContent;  
    private SendChatRequest smsg;  
  
    public ChatGUI()  
    {  
        super();  
          
        Container pane = this.getContentPane();  
          
        Font f = new Font(null, Font.PLAIN, 12);  
        dialog.setFont(f);  
        dialog.setEditable(false);  
        input.setFont(f);  
        friendAddress.setFont(f);  
        newNickName.setFont(f);  
          
        JPanel northPanel = new JPanel();  
        northPanel.setLayout(new FlowLayout(FlowLayout.LEFT));  
        northPanel.add(new JLabel("new name: "));  
        northPanel.add(newNickName);  
        northPanel.add(changeNickName);  
        northPanel.add(new JLabel("input his/her IP:"));  
        northPanel.add(friendAddress);  
        northPanel.add(requestChat);  
          
        pane.add("North", northPanel);  
          
        ActionListener cn = new ChangeName();  
        changeNickName.addActionListener(cn);  
        ActionListener rc = new RequestChat();  
        requestChat.addActionListener(rc);  
          
        JPanel centerPanel = new JPanel();  
        centerPanel.setLayout(new GridLayout(1, 1, 10, 10));  
        JScrollPane jsp = new JScrollPane(dialog);  
        centerPanel.add(jsp);  
        pane.add("West", centerPanel);  
          
        JPanel southPanel = new JPanel();  
        JScrollPane inputJSP = new JScrollPane(input);  
        southPanel.add(inputJSP);  
        southPanel.add(sendButton);  
          
        pane.add("South", southPanel);  
          
        ActionListener al = new SendMessage();    
        sendButton.addActionListener(al);  
        sendButton.setEnabled(false);  
          
        ServerListener sl = new ServerListener();  
        Thread srvl = new Thread(sl);  
        srvl.start();  
    }  
      
    class SendMessage implements ActionListener   
    {  
  
        public void actionPerformed(ActionEvent evt)  
        {  
            inputContent = input.getText();  
            if(inputContent.equals(""))  
            {  
                JOptionPane.showMessageDialog(ChatGUI.this,   
                          "Please do not send empty message", "", JOptionPane.ERROR_MESSAGE);  
                return;  
            }  
            input.setText("");  
            try   
            {  
                String displayName = null;  
                if(nickName == null || nickName.equals(""))  
                {  
                    displayName = InetAddress.getLocalHost().getHostAddress();  
                }  
                else  
                {  
                    displayName = nickName;  
                }  
                //synchronized(dialog)  
                {  
                    //dialog.wait();  
                    dialog.setText(dialog.getText() + "你：\n" + inputContent + "\n");  
                    //dialog.notify();  
                }  
                //将inputContent放入发送队列  
                sendMsgQueue.add(inputContent);  
            }  
            catch (UnknownHostException e)  
            {  
                e.printStackTrace();  
            }  
            /*catch (InterruptedException e) 
            { 
                e.printStackTrace(); 
            }*/  
            
        }  
    }  
      
    class ChangeName implements ActionListener   
    {  
        public void actionPerformed(ActionEvent evt)  
        {  
            if(newNickName.getText().trim().equals(""))  
            {  
                JOptionPane.showMessageDialog(ChatGUI.this,   
                          "Name cannot be empty", "", JOptionPane.ERROR_MESSAGE);  
            }  
            else  
            {  
                nickName = newNickName.getText();  
                newNickName.setText("");  
                //发送向对方  
                JOptionPane.showMessageDialog(ChatGUI.this,   
                          "You have changed your name to " + nickName, "", JOptionPane.INFORMATION_MESSAGE);  
            }  
        }  
    }  
      
    class RequestChat implements ActionListener  
    {  
        public void actionPerformed(ActionEvent evt)  
        {  
            String requestIP = ChatGUI.this.friendAddress.getText();  
            try   
            {  
                String localIP = InetAddress.getLocalHost().getHostAddress();  
                if(requestIP.equals(localIP))  
                {  
                    JOptionPane.showMessageDialog(ChatGUI.this,   
                              "You cannot send invitation to yourself", "", JOptionPane.ERROR_MESSAGE);  
                    return;  
                }  
            }  
            catch (UnknownHostException e)  
            {  
                e.printStackTrace();  
            }  
            if(requestIP.equals(""))  
            {  
                JOptionPane.showMessageDialog(ChatGUI.this,   
                          "Please input valid IP", "", JOptionPane.ERROR_MESSAGE);  
            }  
            else  
            {  
                smsg = new SendChatRequest();  
                Thread sm = new Thread(smsg);  
                sm.start();  
            }  
        }  
    }  
      
    public static void main(String[] args)  
    {  
        SwingUtilities.invokeLater(new Runnable(){  
  
            public void run()   
            {  
                //JFrame.setDefaultLookAndFeelDecorated(true);  
                //JDialog.setDefaultLookAndFeelDecorated(true);  
                try  
                {    
//                    SubstanceImageWatermark watermark  =   new  SubstanceImageWatermark(ChatGUI.class.getResourceAsStream("../image/skyani.jpeg"));    
//                    watermark.setKind(ImageWatermarkKind.APP_CENTER);    
                    SubstanceSkin skin  =   new  OfficeBlue2007Skin();
           
                    UIManager.setLookAndFeel(new SubstanceOfficeBlue2007LookAndFeel());    
                    SubstanceLookAndFeel.setSkin(skin);  
                       
                }   
                catch  (UnsupportedLookAndFeelException ex)  
                {   
                    ex.printStackTrace();  
                }   
                  
                ChatGUI c = new ChatGUI();  
                c.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);  
                c.pack();  
                  
                EventQueue.invokeLater(new FrameShower(c));  
            }});  
    }  
      
    private static class FrameShower implements Runnable   
    {  
            
        private final Frame frame;  
          
        FrameShower(Frame frame)   
        {  
          this.frame = frame;  
        }  
          
        public void run()   
        {  
         frame.setVisible(true);  
        }  
          
    }     
      
    private class SendChatRequest implements Runnable  
    {  
        public void run()  
        {  
            //连接服务器  
            String requestIP = ChatGUI.this.friendAddress.getText();  
            try   
            {  
                InetAddress ia = InetAddress.getByName(requestIP);  
                Socket socket = new Socket(ia, 5776);  
                ObjectOutputStream  out = new ObjectOutputStream(socket.getOutputStream());  
                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));  
                StringBuffer sb = new StringBuffer();  
                String c;  
                try   
                {  
                    c = (String)in.readObject();  
                    sb.append(c);  
                }   
                catch (ClassNotFoundException e)  
                {  
                    e.printStackTrace();  
                }  
                String rstCode = sb.toString();  
                System.out.println(rstCode);  
                if(rstCode.equals("0"))  
                {  
                    JOptionPane.showMessageDialog(ChatGUI.this,   
                            requestIP + " accepted your invitation", "", JOptionPane.INFORMATION_MESSAGE);  
                    sendButton.setEnabled(true);  
                }  
                else if(rstCode.equals("1"))  
                {  
                    JOptionPane.showMessageDialog(ChatGUI.this,   
                            requestIP + " refused your invitation", "", JOptionPane.ERROR_MESSAGE);  
                    in.close();  
                    socket.close();  
                    return;  
                }  
              
                SendMsgThruSocket smts = new SendMsgThruSocket(out);  
                Thread ss = new Thread(smts);  
                ss.start();  
                  
                ReceiveMsgFromSocket rmfs = new ReceiveMsgFromSocket(in);  
                Thread rs = new Thread(rmfs);  
                rs.start();  
            }   
            catch (UnknownHostException e1)   
            {  
                JOptionPane.showMessageDialog(ChatGUI.this,   
                          "UnknownHostAddress", "", JOptionPane.ERROR_MESSAGE);  
                return;  
            }   
            catch (IOException e1)  
            {  
                JOptionPane.showMessageDialog(ChatGUI.this,   
                          "I/O exception", "", JOptionPane.ERROR_MESSAGE);  
            }  
        }  
    }  
      
    private class ServerListener implements Runnable  
    {  
        public void run()  
        {  
            try  
            {  
                ServerSocket server = new ServerSocket(5776);  
              
                while(true)  
                {  
                    Socket connection = server.accept();  
                    ObjectOutputStream  out = new ObjectOutputStream(connection.getOutputStream());  
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(connection.getInputStream()));  
                    String requesterIP = connection.getInetAddress().getHostAddress();  
                    String[] options = {"accept", "refuse"};  
                    int response = JOptionPane.showOptionDialog(ChatGUI.this, requesterIP + " sent an invitation to you...", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);  
                    if(response == 0)  
                    {  
                        out.writeObject("0");  
                        out.flush();  
                        sendButton.setEnabled(true);  
                          
                        SendMsgThruSocket smts = new SendMsgThruSocket(out);  
                        Thread ss = new Thread(smts);  
                        ss.start();  
                          
                        ReceiveMsgFromSocket rmfs = new ReceiveMsgFromSocket(in);  
                        Thread rs = new Thread(rmfs);  
                        rs.start();  
                    }  
                    else if(response == 1)  
                    {  
                        out.writeObject("1");  
                        out.flush();  
                        connection.close();  
                    }  
                }  
            }   
            catch (IOException e)   
            {  
                e.printStackTrace();  
            }  
        }  
    }  
      
    private class SendMsgThruSocket implements Runnable  
    {  
        ObjectOutputStream out;  
          
        SendMsgThruSocket(ObjectOutputStream out2)  
        {  
            this.out = out2;  
        }  
        public void run()  
        {  
            //等待发送消息  
            while (true)  
            {  
                String msgFromQueue;  
                try  
                {  
                    msgFromQueue = (String) sendMsgQueue.take();  
                    out.writeObject(msgFromQueue);  
                }   
                catch (InterruptedException e)  
                {  
                    e.printStackTrace();  
                }   
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
      
    private class ReceiveMsgFromSocket implements Runnable  
    {  
        ObjectInputStream in;  
        ReceiveMsgFromSocket(ObjectInputStream in)  
        {  
            this.in = in;  
        }  
        public void run()  
        {  
            while(true)  
            {  
                try   
                {  
                    String rm;  
                    while((rm = (String)in.readObject()) != null)  
                    {  
                        //synchronized(dialog)  
                        {  
                            //dialog.wait();  
                            dialog.setText(dialog.getText() + "对方：\n" + rm + "\n");  
                            //dialog.notify();  
                        }  
                    }  
                }   
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }   
                catch (ClassNotFoundException e)  
                {  
                    e.printStackTrace();  
                }  
                /*catch (InterruptedException e) { 
                    // TODO Auto-generated catch block 
                    e.printStackTrace(); 
                }*/  
            }  
        }  
    }  
}  
