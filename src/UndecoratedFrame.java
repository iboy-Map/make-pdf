import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.MouseEvent;

public class UndecoratedFrame extends JFrame{
    private   static   final   int   TITLE_HEIGHT   =   20;

    public   UndecoratedFrame()   throws   HeadlessException
    {
        super();
        setUndecorated(true);

        MouseHandler   ml   =   new   MouseHandler();
        addMouseListener(ml);
        addMouseMotionListener(ml);
    }

    public   UndecoratedFrame(String   title)   throws   HeadlessException
    {
        super(title);
        setUndecorated(true);

        MouseHandler ml   =   new MouseHandler();
        addMouseListener(ml);
        addMouseMotionListener(ml);
    }

    public Insets getInsets()
    {
        return   new   Insets(TITLE_HEIGHT,   1,   1,   1);
    }

    public   void   paint(Graphics   g)
    {
        super.paint(g);
        g.setColor(new   Color(0,   0,   128));
        g.drawRect(0,   0,   getWidth()-1,   getHeight()-1);
        g.fillRect(0,   0,   getWidth(),   TITLE_HEIGHT);

        FontMetrics   fm   =   g.getFontMetrics();
        g.setColor(Color.white);
        g.drawString(getTitle(),   2,   (TITLE_HEIGHT   -   fm.getHeight())   /   2   +   fm.getAscent());
    }

    private   class   MouseHandler   extends MouseInputAdapter
    {
        private   Point   point;

        public   void   mousePressed(MouseEvent   e)
        {
            if   (e.getY()   <=   TITLE_HEIGHT)   {
                this.point   =   e.getPoint();
            }
        }

        public   void   mouseDragged(MouseEvent   e)
        {
            if   (point   !=   null)   {
                setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

                Point   p   =   e.getPoint();
                int   dx   =   p.x   -   point.x;
                int   dy   =   p.y   -   point.y;

                int   x   =   getX();
                int   y   =   getY();
                setLocation(x   +   dx,   y   +   dy);
            }
        }

        public   void   mouseReleased(MouseEvent e)
        {
            point   =   null;
            setCursor(Cursor.getDefaultCursor());
        }
    }

    public   static   void   main(String[]   args)
    {
        JFrame   f   =   new   UndecoratedFrame( "Undecorated   Frame ");
        f.getContentPane().add(new   JLabel( "Hello   World! ",   JLabel.CENTER),   BorderLayout.CENTER);
        f.setSize(400,   400);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
