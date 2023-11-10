import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class BackgroundImageJFrame extends JFrame {

    public BackgroundImageJFrame() throws HeadlessException {
        setTitle("临沧市电子地图集");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setUndecorated(false);
        Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();
        double ScreenBz = scrSize.height*1.0 / scrSize.width;

        ImageIcon img = new ImageIcon("C:/lzyFile/临沧电子地图首页1.png");
        int width = (int)(img.getIconWidth());
        int height = (int)(img.getIconHeight());
        //double bz = height * 1.0 / width;
        double bz = 0.5625;
        System.out.println(ScreenBz + ", " + bz);
        /*int width1 = scrSize.width;
        int height1 = (int)((bz) * width1);*/
        /*int height1 = scrSize.height - 10;
        int width1 = (int)(height1/ScreenBz);*/
        int height1 = scrSize.height - 65;
        int width1 = scrSize.width;
        Image i = img.getImage().getScaledInstance(width1, height1, Image.SCALE_AREA_AVERAGING);
        JLabel jLabel = new JLabel(new ImageIcon(i));
        setContentPane(jLabel);
        setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("JLabel的位置为：" + jLabel.getLocation().getX() + ", " + jLabel.getLocation().getY());
                System.out.println("鼠标点击位置为：" + e.getX() + ", " + e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

}
