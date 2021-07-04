import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class ImageViewerFrame extends JFrame {
    public ImageViewerFrame(File file, int Width, int Height) {
        setTitle("ImageViewer");
        setSize(Width, Height);
        label = new JLabel();
        add(label);
        String name = file.getPath();
        label.setIcon(new ImageIcon(name));
        /*
        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);
        JMenu menu = new JMenu("File");
        menubar.add(menu);
        JMenuItem openItem = new JMenuItem("Open");
        menu.add(openItem);
        JMenuItem exitItem = new JMenuItem("Close");
        menu.add(exitItem);
        openItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                int result = chooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().getPath();
                    label.setIcon(new ImageIcon(name));
                }
            }
        });
        exitItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                System.exit(0);
            }
        });*/
    }

    private JLabel label;
    private JFileChooser chooser;
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 400;
}
