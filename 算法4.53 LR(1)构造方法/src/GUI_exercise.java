import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class GUI_exercise extends JFrame{
    private static JFrame frame;
    TextArea inputTextArea = new TextArea();
    TextArea outputTextArea = new TextArea();

    ImageIcon icon = new ImageIcon("ls.jpg");
    JButton button = new JButton(icon);

    GUI_exercise(){
        frame = new JFrame();
    }
    void setframe(){
        frame.setTitle("C语言编译器_Made by:232");
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    void setComponent(){
        Container c = frame.getContentPane();

        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        frame.setSize(screenWidth,screenHeight/2);
        frame.setLocation(100,100);

        JLabel lable1 = new JLabel("C_Input:");
        JLabel label2 = new JLabel("Output:");
        inputTextArea.setSize(screenWidth/200,screenHeight/40);
        outputTextArea.setSize(screenWidth/200,screenHeight/40);
        c.setLayout(new FlowLayout());
        //c.setLayout(new GridLayout(2,1));
        c.add(lable1);
        c.add(inputTextArea);
        c.add(label2);
        c.add(outputTextArea);
        c.add(button);
    }
    void listener(){
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = inputTextArea.getText();//获取文本框中内容
                FileWriter out;
                try {
                    out = new FileWriter("in1.txt");
                    out.write(s);//将文本内容保存到文件中
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                String str_out="";
                File file=new File("result.txt");
                try {
                    FileInputStream in=new FileInputStream(file);
                    // size 为字串的长度 ，这里一次性读完
                    int size=in.available();
                    byte[] buffer=new byte[size];
                    in.read(buffer);
                    in.close();
                    str_out=new String(buffer,"GB2312");
                } catch (IOException ee) {
                    // TODO Auto-generated catch block
                    ee.printStackTrace();
                }
                outputTextArea.setText(str_out);
            }
        });
    }
    public static void main(String[] args){
        GUI_exercise g = new GUI_exercise();
        g.setframe();
        g.setComponent();
        g.listener();

    }
}