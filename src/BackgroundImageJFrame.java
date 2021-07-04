import javax.swing.*;
import java.awt.*;

public class BackgroundImageJFrame extends JFrame {
    public BackgroundImageJFrame() throws HeadlessException {
        setTitle("临沧市电子地图集");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setContentPane(new JLabel(new ImageIcon("C:/lzyFile/临沧站.JPG")));
        setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
    }
}
