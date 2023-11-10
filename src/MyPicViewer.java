import javafx.geometry.Point2D;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MyFileChooser extends JFileChooser {
    private final MyFilterWrapper filter;

    public MyFileChooser(MyFilterWrapper filter) {
        this.filter = filter;
        // 扩展名过滤
        setFileFilter(filter);

        // 文件选择属性设置
        setMultiSelectionEnabled(true);
        setAcceptAllFileFilterUsed(false);
        setFileSelectionMode(FILES_AND_DIRECTORIES);
    }

    public String [] getAbsolutePathsRecursively() {
        ArrayList<String> paths = new ArrayList<String>();
        File [] files = getSelectedFiles();
        traverse(files, paths);
        return paths.toArray(new String [] {});
    }

    private void traverse(File [] files, ArrayList<String> paths) {
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                traverse(f.listFiles(this.filter), paths);
            } else if (f.isFile() && this.filter.accept(f)) {
                paths.add(f.getAbsolutePath());
            }
        }
    }
}

final class MyFilterWrapper extends javax.swing.filechooser.FileFilter implements java.io.FileFilter {
    private final FileNameExtensionFilter filter;

    public MyFilterWrapper(String description, String... extensions) {
        this.filter = new FileNameExtensionFilter(description, extensions);
    }

    public boolean accept(File f) {
        return this.filter.accept(f);
    }

    public String getDescription() {
        return this.filter.getDescription();
    }
}

class ZoomablePicture extends JComponent {
    //记录当前窗体框架
    MyPicViewer picViewer;

    //记录当前地图的地理信息
    private float max_lat = 0;
    private float max_long = 0;
    private float min_lat = Float.MAX_VALUE;
    private float min_long = Float.MAX_VALUE;

    //记录当前视区范围的地理信息
    private double CSMinLat = 0;
    private double CSMaxLat = 0;
    private double CSMinLong = 0;
    private double CSMaxlong = 0;

    //标识是否开始自动切图
    private Boolean isAutoTrans;
    private Image image;

    //记录当前窗口的宽和高
    private int width, height;
    private float zoomFactor;
    private Boolean InitType;
    private float DefaultWidth;
    private float DefaultHeight;
    private float CurrentWidth;
    private float CurrentHeight;
    private float SumZoomFactor;

    //记录当前地图的四点坐标信息
    private PointF[] pts;

    //电子地图集全部地图
    private List<Map> maps;

    public String CurrentMapTitle;

    private static JFrame UndecoratedFrame = new UndecoratedFrame( "空间信息提示框");

    private boolean isThematicMap = false;

    public boolean isThematicMap() {
        return isThematicMap;
    }

    public void setThematicMap(boolean thematicMap) {
        isThematicMap = thematicMap;
    }

    public void setUndecoratedFrame(Boolean visible){
        UndecoratedFrame.setVisible(visible);
    }

    public Boolean getAutoTrans() {
        return isAutoTrans;
    }

    public void setAutoTrans(Boolean autoTrans) {
        isAutoTrans = autoTrans;
    }

    public void load(String filename, PointF[] pts, List<Map> maps, MyPicViewer picViewer, String title) {
        unload();

        this.picViewer = picViewer;
        if (pts==null) {
            this.picViewer.btn_autoTrans.setVisible(false);
        }
        else {
            this.picViewer.btn_autoTrans.setVisible(true);
            this.picViewer.btn_autoTrans.setText("开始自动切图");
            this.picViewer.btn_autoTrans.setIcon(new ImageIcon(this.picViewer.getClass().getClassLoader().getResource("icons/readytrans透明.png")));
        }
        //this.picViewer.btn_autoTrans.setIcon(new ImageIcon("icons/readytrans透明.png"));
        this.picViewer.setTitle(title);

        this.maps = maps;

        CurrentMapTitle = title;
        CurrentMessureType = MESSURE_TYPE.NONE;
        isAutoTrans = false;
        image = Toolkit.getDefaultToolkit().getImage(filename);
        if (pts != null)
        {
            this.pts = pts;
            isThematicMap = false;
            this.picViewer.btn_AreaMessure.setVisible(true);
            this.picViewer.btn_DistanceMessure.setVisible(true);
        }
        else
        {
            this.pts = null;
            isThematicMap = true;
            this.picViewer.btn_AreaMessure.setVisible(false);
            this.picViewer.btn_DistanceMessure.setVisible(false);
        }
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        width = image.getWidth(null);
        height = image.getHeight(null);
        zoomFactor = 1.0f;
        SumZoomFactor = 1.0f;
        InitType = true;

        if (!isThematicMap)
            GetCurrentMapGeoInfo();
        //setPreferredSize(new Dimension(width, height));
        //setPreferredSize(new Dimension(1627, 1006));
        revalidate();
        repaint();


        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isThematicMap) {
                    MouseClickFunction(e);
                }
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

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point oldMousePt = e.getPoint();
                double xRate = oldMousePt.getX() / getWidth();
                double yRate = oldMousePt.getY() / getHeight();
                int xOffset = (int)(Math.round(oldMousePt.getX() - getVisibleRect().getLocation().getX()));
                int yOffset = (int)(Math.round(oldMousePt.getY() - getVisibleRect().getLocation().getY()));

                boolean isZoomIn = false;
                //System.out.println(getWidth() + ", " + getHeight());
                if (e.getWheelRotation() == 1)
                {
                    isZoomIn = true;
                    SumZoomFactor*=1.05f;
                    setZoomFactor(zoomFactor*1.05f);
                }
                else
                {
                    isZoomIn = false;
                    float newZoomFactor = zoomFactor*0.95f;
                    /*setZoomFactor(zoomFactor*0.9f);
                    System.out.println("当前图片放缩比例为： " + zoomFactor*0.9f);*/
                    if (newZoomFactor < 1)
                    {
                        setZoomFactor(1);
                        SumZoomFactor = 1;
                    }
                    else
                    {
                        setZoomFactor(newZoomFactor);
                        SumZoomFactor*=0.95f;
                    }
                }
                ZoomForMouseCenter(xRate, yRate, xOffset, yOffset, isZoomIn);
            }
        });

        this.setZoomFactor(1);
        this.setVisible(true);
        this.RefreshDimension();

        /*VisibleRect = getVisibleRect();
        System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
        System.out.println("可视矩形框的宽长为： " + VisibleRect.width + ", " + VisibleRect.height);
        if (width >= height)
        {
            setPreferredSize(new Dimension(VisibleRect.width, (int)(height*(1.0)*VisibleRect.width/width)));
            System.out.println("新长度为： " + (int)(height*Double.sum(VisibleRect.width, 0)*(1.0)/Double.sum(width, 0)*(1.0)) + "\n" + width + ", " + height);
        }
        else
        {
            setPreferredSize(new Dimension((int)(width*(1.0)*VisibleRect.height/height), VisibleRect.height));
            System.out.println("新宽度为： " + (int)(width*(1.0)*Double.sum(VisibleRect.height, 0)*(1.0)/Double.sum(height, 0)*(1.0)) + "\n" + width + ", " + height);
        }*/
    }

    private void MouseClickFunction(MouseEvent e){
        System.out.println("看我的点击事件！" + e.getButton());
        if (e.getButton()==MouseEvent.BUTTON1) {
            if (!isThematicMap) {
                Point ClickPt = e.getPoint();
                double CurrentMinX = 0.5 * getWidth() - 0.5 * CurrentWidth;
                double CurrentMaxX = 0.5 * getWidth() + 0.5 * CurrentWidth;
                double CurrentMinY = 0.5 * getHeight() - 0.5 * CurrentHeight;
                double CurrentMaxY = 0.5 * getHeight() + 0.5 * CurrentHeight;

                if (ClickPt.getX() >= CurrentMinX && ClickPt.getX() <= CurrentMaxX && ClickPt.getY() >= CurrentMinY && ClickPt.getY() <= CurrentMaxY) {
                    //(ClickPt.x-CurrentMinX)*1.0/CurrentWidth*(max_long-min_long)+
                    //(ClickPt.y-CurrentMinY)*1.0/CurrentHeight*(max_lat-min_lat)
                    double Longitude = (min_long + (ClickPt.x - CurrentMinX) * 1.0 / CurrentWidth * (max_long - min_long));
                    double Latitude = (max_lat - (ClickPt.y - CurrentMinY) * 1.0 / CurrentHeight * (max_lat - min_lat));
                    //GetScreenLocFromGeoLoc(new PointF(Float.valueOf(Double.toString(Latitude)), Float.valueOf(Double.toString(Longitude))));
                    AddPtToPointCollection(new PointF(Float.valueOf(Double.toString(Latitude)), Float.valueOf(Double.toString(Longitude))));
                    System.out.println("当前点击位置为： " + ClickPt + "\n" + getWidth() + ", " + getHeight() + "\n" + CurrentWidth + ", " + CurrentHeight + "\n" + "相对位置是： " + (ClickPt.x - CurrentMinX) + ", " + (ClickPt.y - CurrentMinY) + "\n" + "坐标值是： " + Longitude + ", " + Latitude);

                    repaint();

                    if (CurrentMessureType == MESSURE_TYPE.NONE) {
                        UndecoratedFrame.getContentPane().removeAll();
                        DecimalFormat df = new DecimalFormat("#0.00000");
                        //System.out.println(df.format(num));
                        UndecoratedFrame.getContentPane().add(new JLabel("当前点击位置坐标值为： " + df.format(Longitude) + ", " + df.format(Latitude), JLabel.CENTER), BorderLayout.CENTER);
                        UndecoratedFrame.setSize(300, 50);
                        UndecoratedFrame.setLocation(e.getLocationOnScreen());
                        //UndecoratedFrame.setLocation(new Point(Integer.valueOf(Double.toString(ClickPt.x-CurrentMinX)), Integer.valueOf(Double.toString(ClickPt.y- CurrentMinY))));
                        UndecoratedFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                        setUndecoratedFrame(true);
                    }
                } else {
                    System.out.println("当前点击位置在区域外");
                    setUndecoratedFrame(false);
                }
            }
        }
        else if (e.getButton()==MouseEvent.BUTTON3)
        {
            RemoveLastPtForPointCollection(e.getPoint().getX());
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        switch (CurrentMessureType){
            case AREA:
                DrawPolygon(g);
                break;
            case DISTANCE:
                DrawLines(g);
                break;
            case NONE:
                System.out.println("paint尚未进行测量");
                break;
        }
    }

    private void DrawPtsForPolygon(Graphics g){
        for (int i = 0; i < DrawedPts.size(); i++) {
            System.out.println(DrawedPts.size());
            MyIntPoint ScreenLoc = GetScreenLocFromGeoLoc(DrawedPts.get(i));
            g.setColor(Color.RED);
            g.drawOval(ScreenLoc.getX()-10, ScreenLoc.getY()-10, 20, 20);
            if (i == 0)
                g.drawString("起点： " , ScreenLoc.getX()-10, ScreenLoc.getY()-10);
            else
            {
                g.drawString("第" + (i+1) + "个点" , ScreenLoc.getX()-10, ScreenLoc.getY()-10);
            }
        }
    }

    private void DrawPtsForLines(Graphics g){
        double sum = 0;
        for (int i = 0; i < DrawedPts.size(); i++) {
            MyIntPoint ScreenLoc = GetScreenLocFromGeoLoc(DrawedPts.get(i));
            g.setColor(Color.RED);
            g.drawOval(ScreenLoc.getX()-10, ScreenLoc.getY()-10, 20, 20);
            if (i == 0)
                g.drawString("起点： " , ScreenLoc.getX()-10, ScreenLoc.getY()-10);
            else
            {
                sum += DataUtil.algorithm(DrawedPts.get(i-1).getLong(), DrawedPts.get(i-1).getLat(), DrawedPts.get(i).getLong(), DrawedPts.get(i).getLat());
                //g.drawString("第" + (i+1) + "个点" , ScreenLoc.getX()-10, ScreenLoc.getY()-10);
                if (sum > 1000)
                    g.drawString(String.valueOf(Math.round(sum/1000)) + "公里" , ScreenLoc.getX()-10, ScreenLoc.getY()-10);
                else
                    g.drawString(String.valueOf(Math.round(sum)) + "米" , ScreenLoc.getX()-10, ScreenLoc.getY()-10);
            }
        }
    }

    private void DrawLines(Graphics g){
        switch (DrawedPts.size()){
            case 0:
                break;
            case 1:
                DrawPtsForLines(g);
                break;
            default:
                DrawPtsForLines(g);
                DrawGeometry drawGeometry1 = GetLines();
                g.setColor(Color.GREEN);
                g.drawPolyline(drawGeometry1.getxPoints(), drawGeometry1.getyPoints(), drawGeometry1.getnPoints());
                //g.drawString("距离测量：" , xps[2], yps[2]);
                System.out.println("paint距离测量， 当前有：" + DrawedPts.size() + "个点");
                break;
        }
    }

    private void DrawPolygon(Graphics g){
        switch (DrawedPts.size()){
            case 0:
                break;
            case 1:
                DrawPtsForPolygon(g);
                break;
            case 2:
                DrawPtsForPolygon(g);
                DrawGeometry drawLineGeometry = GetLines();
                Color LineColor = new Color(255,0,255, 50);
                g.setColor(LineColor);
                g.drawPolyline(drawLineGeometry.getxPoints(), drawLineGeometry.getyPoints(), drawLineGeometry.getnPoints());
                //g.drawString("面积测量：" , xps1[3], yps1[3]);
                System.out.println("paint面积测量， 当前有：" + DrawedPts.size() + "个点");
                break;
            default:
                DrawPolygonTip(g);
                DrawPtsForPolygon(g);
                DrawGeometry drawGeometry = GetPolygons();
                Color c = new Color(255,0,255, 50);
                g.setColor(c);
                g.fillPolygon(drawGeometry.getxPoints(), drawGeometry.getyPoints(), drawGeometry.getnPoints());
                //g.drawString("面积测量：" , xps1[3], yps1[3]);
                System.out.println("paint面积测量， 当前有：" + DrawedPts.size() + "个点");
                break;
        }
    }

    private void DrawPolygonTip(Graphics g){
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = 0; i < DrawedPts.size(); i++) {
            MyIntPoint myIntPoint = GetScreenLocFromGeoLoc(DrawedPts.get(i));
            if (myIntPoint.getX() > maxX) maxX = myIntPoint.getX();
            if (myIntPoint.getX() < minX) minX = myIntPoint.getX();
            if (myIntPoint.getY() > maxY) maxY = myIntPoint.getY();
            if (myIntPoint.getY() < minY) minY = myIntPoint.getY();
        }
        MyIntPoint TipLoc = new MyIntPoint((minX+maxX)/2, (minY+maxY)/2);

        double[][] temp = new double[DrawedPts.size()][2];
        for (int i = 0; i < DrawedPts.size(); i++) {
            temp[i][0] = DrawedPts.get(i).getLong();
            temp[i][1] = DrawedPts.get(i).getLat();
        }

        double s = temp[0][1] * (temp[DrawedPts.size()-1][0] - temp[1][0]);

        for (int i = 1; i < DrawedPts.size(); i++) {
            s += temp[i][1] * (temp[i-1][0] - temp[(i+1)%DrawedPts.size()][0]);
        }
        double a = Math.round(Math.abs(s/2)*9101160000.085981);

        g.setColor(Color.RED);
        if (a > 1000000)
            g.drawString(String.valueOf(a/1000000) + "平方公里", TipLoc.getX()-20, TipLoc.getY()-5);
        else
            g.drawString(String.valueOf(a) + "平方米", TipLoc.getX()-20, TipLoc.getY()-5);
    }

    private DrawGeometry GetLines(){
        int[] xPoints = new int[DrawedPts.size()];
        int[] yPoints = new int[DrawedPts.size()];
        int nPoints = DrawedPts.size();
        for (int i = 0; i < DrawedPts.size(); i++) {
            MyIntPoint ScreenLoc = GetScreenLocFromGeoLoc(DrawedPts.get(i));
            xPoints[i] = ScreenLoc.getX();
            yPoints[i] = ScreenLoc.getY();
        }
        /*xPoints[DrawedPts.size()] = GetScreenLocFromGeoLoc(DrawedPts.get(0)).getX();
        yPoints[DrawedPts.size()] = GetScreenLocFromGeoLoc(DrawedPts.get(0)).getY();*/
        DrawGeometry drawGeometry = new DrawGeometry(xPoints, yPoints, nPoints);
        return drawGeometry;
    }

    private DrawGeometry GetPolygons(){
        int[] xPoints = new int[DrawedPts.size()];
        int[] yPoints = new int[DrawedPts.size()];
        int nPoints = DrawedPts.size();
        for (int i = 0; i < DrawedPts.size(); i++) {
            MyIntPoint ScreenLoc = GetScreenLocFromGeoLoc(DrawedPts.get(i));
            xPoints[i] = ScreenLoc.getX();
            yPoints[i] = ScreenLoc.getY();
        }
        DrawGeometry drawGeometry = new DrawGeometry(xPoints, yPoints, nPoints);
        return drawGeometry;
    }

    private MyIntPoint GetScreenLocFromGeoLoc(PointF pt){
        double CurrentMinX = 0.5 * getWidth() - 0.5 * CurrentWidth;
        double CurrentMinY = 0.5 * getHeight() - 0.5 * CurrentHeight;

        int x = (int)Math.round(CurrentMinX + (pt.getLong() - min_long) / (max_long - min_long) * CurrentWidth);
        int y = (int)Math.round(CurrentMinY + (max_lat - pt.getLat()) / (max_lat - min_lat) * CurrentHeight);

        System.out.println("latitude: " + pt.getLat() + ", longitude: " + pt.getLong());

        System.out.println("真实屏幕坐标为 x: " + x + ", y: " + y);

        return new MyIntPoint(x, y);
    }

    private double lastPtx;
    private void RemoveLastPtForPointCollection(double Ptx){
        if (DrawedPts.size() >= 1 && lastPtx != Ptx){
            DrawedPts.remove(DrawedPts.size()-1);
            lastPtx = Ptx;
            repaint();
        }
    }

    private void AddPtToPointCollection(PointF pt){
        for (int i = 0; i < DrawedPts.size(); i++) {
            if (DrawedPts.get(i).getLat() == pt.getLat() && DrawedPts.get(i).getLong() == pt.getLong())
                return;
        }
        DrawedPts.add(pt);
        System.out.println("AddPtToPointCollection" + DrawedPts.size());
    }

    private void RefreshPointCollection(){
        DrawedPts = new ArrayList<>();
    }

    private List<PointF> DrawedPts = new ArrayList<>();

    public enum MESSURE_TYPE {
        DISTANCE, AREA, NONE
    }

    public MESSURE_TYPE CurrentMessureType = MESSURE_TYPE.NONE;

    public MESSURE_TYPE getCurrentMessureType() {
        return CurrentMessureType;
    }

    public void setCurrentMessureType(MESSURE_TYPE currentMessureType) {
        if (currentMessureType != this.CurrentMessureType)
            RefreshPointCollection();
        CurrentMessureType = currentMessureType;
    }

    public void load(String filename, PointF[] pts, String title) {
        unload();

        CurrentMapTitle = title;
        CurrentMessureType = MESSURE_TYPE.NONE;

        if (pts==null) {
            this.picViewer.btn_autoTrans.setVisible(false);
        }
        else {
            this.picViewer.btn_autoTrans.setVisible(true);
            this.picViewer.btn_autoTrans.setText("开始自动切图");
            this.picViewer.btn_autoTrans.setIcon(new ImageIcon(this.picViewer.getClass().getClassLoader().getResource("icons/readytrans透明.png")));
        }
        //this.picViewer.btn_autoTrans.setIcon(new ImageIcon("icons/readytrans透明.png"));
        picViewer.setTitle(title);
        isAutoTrans = false;
        image = Toolkit.getDefaultToolkit().getImage(filename);
        if (pts != null)
        {
            this.pts = pts;
            isThematicMap = false;
            this.picViewer.btn_AreaMessure.setVisible(true);
            this.picViewer.btn_DistanceMessure.setVisible(true);
        }
        else
        {
            this.pts = null;
            isThematicMap = true;
            this.picViewer.btn_AreaMessure.setVisible(false);
            this.picViewer.btn_DistanceMessure.setVisible(false);
        }
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        try {
            mt.waitForAll();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        width = image.getWidth(null);
        height = image.getHeight(null);
        zoomFactor = 1.0f;
        SumZoomFactor = 1.0f;
        InitType = true;

        if (!isThematicMap)
            GetCurrentMapGeoInfo();
        //setPreferredSize(new Dimension(width, height));
        //setPreferredSize(new Dimension(1627, 1006));
        revalidate();
        repaint();


        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isThematicMap) {
                    MouseClickFunction(e);
                }
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

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point oldMousePt = e.getPoint();
                double xRate = oldMousePt.getX() / getWidth();
                double yRate = oldMousePt.getY() / getHeight();
                int xOffset = (int)(Math.round(oldMousePt.getX() - getVisibleRect().getLocation().getX()));
                int yOffset = (int)(Math.round(oldMousePt.getY() - getVisibleRect().getLocation().getY()));

                boolean isZoomIn = false;
                //System.out.println(getWidth() + ", " + getHeight());
                if (e.getWheelRotation() == 1)
                {
                    isZoomIn = true;
                    SumZoomFactor*=1.05f;
                    setZoomFactor(zoomFactor*1.05f);
                }
                else
                {
                    isZoomIn = false;
                    float newZoomFactor = zoomFactor*0.95f;
                    /*setZoomFactor(zoomFactor*0.9f);
                    System.out.println("当前图片放缩比例为： " + zoomFactor*0.9f);*/
                    if (newZoomFactor < 1)
                    {
                        setZoomFactor(1);
                        SumZoomFactor = 1;
                    }
                    else
                    {
                        setZoomFactor(newZoomFactor);
                        SumZoomFactor*=0.95f;
                    }
                }
                ZoomForMouseCenter(xRate, yRate, xOffset, yOffset, isZoomIn);
            }
        });

        /*VisibleRect = getVisibleRect();
        System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
        System.out.println("可视矩形框的宽长为： " + VisibleRect.width + ", " + VisibleRect.height);
        if (width >= height)
        {
            setPreferredSize(new Dimension(VisibleRect.width, (int)(height*(1.0)*VisibleRect.width/width)));
            System.out.println("新长度为： " + (int)(height*Double.sum(VisibleRect.width, 0)*(1.0)/Double.sum(width, 0)*(1.0)) + "\n" + width + ", " + height);
        }
        else
        {
            setPreferredSize(new Dimension((int)(width*(1.0)*VisibleRect.height/height), VisibleRect.height));
            System.out.println("新宽度为： " + (int)(width*(1.0)*Double.sum(VisibleRect.height, 0)*(1.0)/Double.sum(height, 0)*(1.0)) + "\n" + width + ", " + height);
        }*/
    }

    private void GetCurrentMapGeoInfo(){
        max_lat = 0;
        max_long = 0;
        min_lat = Float.MAX_VALUE;
        min_long = Float.MAX_VALUE;
        for (int i = 0; i < pts.length; i++) {
            float lat = pts[i].getLat();
            float longi = pts[i].getLong();
            if (lat > max_lat)
                max_lat = lat;
            if (longi > max_long)
                max_long = longi;
            if (lat < min_lat)
                min_lat = lat;
            if (longi < min_long)
                min_long = longi;
        }
    }

    private void ZoomForMouseCenter(double xRate, double yRate, int xOffset, int yOffset, Boolean isZoomIn){
        Rectangle rect = getVisibleRect();
        rect.setLocation(new Point((int)Math.round(getPreferredSize().width * xRate-(xOffset)), (int)Math.round(getPreferredSize().height * yRate-(yOffset))));
        //System.out.println("当前可视区域为： " + (rect.getLocation().getX()-rect.getWidth()/2) + ", " + (rect.getLocation().getY()-rect.getHeight()/2));
        System.out.println("（放缩）当前可视矩形框中心点为： " + (rect.getLocation().getX()) + ", " + (rect.getLocation().getY()) + "\n" + rect.getWidth() + ", " + rect.getHeight() + "\n" + getPreferredSize().width + ", " + getPreferredSize().height);


        scrollRectToVisible(rect);

        if (!isThematicMap) {
            double[] extent = GetCurrentScreenExtent(rect);

            System.out.println("当前可视区域坐标为： " + extent[0] + ", " + extent[1] + ", " + extent[2] + ", " + extent[3] + ", 当前正在放大： " + isZoomIn);

            if (isAutoTrans) {
                double thedelta = 0;
                int thenum = -1;
                for (int i = 0; i < maps.size(); i++) {
                    Map CurrentMap = maps.get(i);
                    if (CurrentMap.getMapType() >= 5 && CurrentMap.getMapGeoInfo() != null) {
                        PointF[] pts = CurrentMap.getMapGeoInfo();
                        float max_lat = 0;
                        float max_long = 0;
                        float min_lat = Float.MAX_VALUE;
                        float min_long = Float.MAX_VALUE;
                        for (int j = 0; j < pts.length; j++) {
                            float lat = pts[j].getLat();
                            float longi = pts[j].getLong();
                            if (lat > max_lat)
                                max_lat = lat;
                            if (longi > max_long)
                                max_long = longi;
                            if (lat < min_lat)
                                min_lat = lat;
                            if (longi < min_long)
                                min_long = longi;
                        }
                        if (isZoomIn) {
                            if (verifyAreaForZoomIn(max_lat, min_lat, max_long, min_long)) {
                                double thedelta1 = Math.abs(CSMaxLat - max_lat) + Math.abs(CSMinLat - min_lat) + Math.abs(CSMaxlong - max_long) + Math.abs(CSMinLong - min_long);
                                if (!isThisMap(max_lat, min_lat, max_long, min_long)) {
                                    if (thedelta == 0) {
                                        thedelta = thedelta1;
                                        thenum = i;
                                    }
                                    if (thedelta1 < thedelta) {
                                        thedelta = thedelta1;
                                        thenum = i;
                                    }
                                }
                            }
                        } else {
                            if (verifyAreaForZoomOut(max_lat, min_lat, max_long, min_long)) {
                                double thedelta1 = Math.abs(CSMaxLat - max_lat) + Math.abs(CSMinLat - min_lat) + Math.abs(CSMaxlong - max_long) + Math.abs(CSMinLong - min_long);
                                if (!isThisMap(max_lat, min_lat, max_long, min_long)) {
                                    if (thedelta == 0) {
                                        thedelta = thedelta1;
                                        thenum = i;
                                    }
                                    if (thedelta1 < thedelta) {
                                        thedelta = thedelta1;
                                        thenum = i;
                                    }
                                }
                            }
                        }
                    }
                }
                if (thenum != -1) {
                    load(maps.get(thenum).getPath(), maps.get(thenum).getMapGeoInfo(), maps.get(thenum).getName());
                    RefreshDimension();
                    setZoomFactor(1);
                    System.out.println(maps.get(thenum).getName());
                }
            }
        }
    }

    private boolean verifyAreaForZoomOut(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long){
        return mmax_lat > max_lat && mmin_lat < min_lat && mmax_long > max_long && mmin_long < min_long;
    }

    private boolean isThisMap(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long) {
        return this.max_lat==mmax_lat&&this.min_lat==mmin_lat&&this.max_long==mmax_long&&this.min_long==mmin_long;
    }

    private boolean verifyAreaForZoomIn(double mmax_lat, double mmin_lat, double mmax_long, double mmin_long) {
        double deltaLatK, deltaLongK;
        deltaLatK = (max_lat - min_lat) * 0.014;
        deltaLongK = (max_long - min_long) * 0.014;
        if ((mmin_lat - deltaLatK) < CSMinLat && (mmax_long + deltaLongK) > CSMaxlong && (mmin_long - deltaLongK) < CSMinLong)
            return true;
        else if ((mmax_lat + deltaLatK) > CSMaxLat && (mmax_long + deltaLongK) > CSMaxlong && (mmin_long - deltaLongK) < CSMinLong)
            return true;
        else if ((mmax_lat + deltaLatK) > CSMaxLat && (mmin_lat - deltaLatK) < CSMinLat && (mmin_long - deltaLongK) < CSMinLong)
            return true;
        else if ((mmax_lat + deltaLatK) > CSMaxLat && (mmin_lat - deltaLatK) < CSMinLat && (mmax_long + deltaLongK) > CSMaxlong)
            return true;
        else if ((mmax_lat + deltaLatK) > CSMaxLat && (mmin_lat - deltaLatK) < CSMinLat && (mmax_long + deltaLongK) > CSMaxlong && (mmin_long - deltaLongK) < CSMinLong)
            return true;
        else return false;
    }

    private double[] GetCurrentScreenExtent(Rectangle rect){
        double[] Extent = new double[4];
        Point TopLeftPt = rect.getLocation();
        double CurrentMinX = TopLeftPt.x;
        double CurrentMaxX = TopLeftPt.x+rect.getWidth();
        double CurrentMinY = TopLeftPt.y;
        double CurrentMaxY = TopLeftPt.y+rect.getHeight();

        System.out.println("当前可视区域范围为： " + CurrentMinX + ", " + CurrentMaxX + ", " + CurrentMinY + ", " + CurrentMaxY);
        float max_lat = 0;
        float max_long = 0;
        float min_lat = Float.MAX_VALUE;
        float min_long = Float.MAX_VALUE;
        for (int i = 0; i < pts.length; i++) {
            float lat = pts[i].getLat();
            float longi = pts[i].getLong();
            if (lat > max_lat)
                max_lat = lat;
            if (longi > max_long)
                max_long = longi;
            if (lat < min_lat)
                min_lat = lat;
            if (longi < min_long)
                min_long = longi;
        }

        CSMinLat = 0;
        CSMaxLat = 0;
        CSMinLong = 0;
        CSMaxlong = 0;

        if (CurrentMinX <= 0){
            CSMinLong = min_long;
            CSMaxlong = min_long + CurrentMaxX/getPreferredSize().width*(max_long-min_long);
        }
        else
        {
            CSMinLong = min_long + CurrentMinX/getPreferredSize().width*(max_long-min_long);
            CSMaxlong = min_long + CurrentMaxX/getPreferredSize().width*(max_long-min_long);
        }
        if (CurrentMinY <= 0){
            CSMaxLat = max_lat;
            CSMinLat = min_lat + (getPreferredSize().height-CurrentMaxY)/getPreferredSize().height*(max_lat-min_lat);
        }
        else
        {
            CSMaxLat = min_lat + (getPreferredSize().height-CurrentMinY)/getPreferredSize().height*(max_lat-min_lat);
            CSMinLat = min_lat + (getPreferredSize().height-CurrentMaxY)/getPreferredSize().height*(max_lat-min_lat);
        }

        Extent[0] = CSMinLong;
        Extent[1] = CSMaxlong;
        Extent[2] = CSMinLat;
        Extent[3] = CSMaxLat;

        return Extent;
    }

    public void unload() {
        if (image != null) {
            setUndecoratedFrame(false);
            image = null;
            setPreferredSize(new Dimension(1, 1));
            revalidate();
            repaint();
        }
    }

    public void setZoomFactor(float factor) {
        zoomFactor = factor;
        setPreferredSize(new Dimension((int) (DefaultWidth * zoomFactor), (int) (DefaultHeight * zoomFactor)));
        CurrentHeight = (int) (DefaultHeight * zoomFactor);
        CurrentWidth = (int) (DefaultWidth * zoomFactor);




        revalidate();
        repaint();
    }

    public void SetDefaultMapRect(){
        setPreferredSize(new Dimension((int) (width), (int) (height)));
        revalidate();
        repaint();
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public void RefreshDimension(){
        Dimension NewDimension = Toolkit.getDefaultToolkit().getScreenSize();
        NewDimension.width = NewDimension.width - 18;
        NewDimension.height = NewDimension.height - 106;
        if (width >= height) {
            int NewHeight = (int) (height * (1.0) * (NewDimension.width) / width);
            if (NewHeight >= NewDimension.height)
            {
                setPreferredSize(new Dimension((int) (width * (1.0) * (NewDimension.height) / height), NewDimension.height));
                DefaultHeight = NewDimension.height;
                DefaultWidth = (int) (width * (1.0) * (NewDimension.height) / height);
            }
            else
            {
                setPreferredSize(new Dimension(NewDimension.width, NewHeight));
                DefaultHeight = NewHeight;
                DefaultWidth = NewDimension.width;
            }
            //System.out.println("新长度为： " + (int) (height * Double.sum(VisibleRect.width, 0) * (1.0) / Double.sum(width, 0) * (1.0)) + "\n" + width + ", " + height);
        } else {
            int NewWidth = (int) (width * (1.0) * (NewDimension.height) / height);
            if (NewWidth >= NewDimension.width)
            {
                setPreferredSize(new Dimension(NewWidth, (int) (height * (1.0) * (NewDimension.width) / width)));
                DefaultHeight = (int) (height * (1.0) * (NewDimension.width) / width);
                DefaultWidth = NewWidth;
            }
            else
            {
                setPreferredSize(new Dimension(NewWidth, NewDimension.height));
                DefaultHeight = NewDimension.height;
                DefaultWidth = NewWidth;
            }
            //System.out.println("新宽度为： " + (int) (width * (1.0) * Double.sum(VisibleRect.height, 0) * (1.0) / Double.sum(height, 0) * (1.0)) + "\n" + width + ", " + height);
        }
        CurrentHeight = DefaultHeight;
        CurrentWidth = DefaultWidth;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            int ws = getWidth();
            int hs = getHeight();
            int wp = getPreferredSize().width;
            int hp = getPreferredSize().height;
            g.drawImage(image, (ws - wp) / 2, (hs - hp) / 2, wp, hp, null);

            if (InitType) {
                RefreshDimension();
                InitType = false;
            }
        }
    }
}

class ScrollablePicture extends ZoomablePicture {
    private Point oldCursorPos;
    //private PointF[] pts;

    public ScrollablePicture() {
        //this.pts = pts;
        addMouseMotionListener(new MouseMotionAdapter () {
            public void mouseDragged(MouseEvent e) {
                dragTo(e.getLocationOnScreen());
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                startDragging(e.getLocationOnScreen());
            }
            public void mouseReleased(MouseEvent e) {
                stopDragging();
            }
        });
    }


    private void startDragging(Point cursorPos) {
        oldCursorPos = cursorPos;
        setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }

    private void stopDragging() {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private void dragTo(Point newCursorPos) {
        int dx = newCursorPos.x - oldCursorPos.x;
        int dy = newCursorPos.y - oldCursorPos.y;
        Rectangle visibleRect = getVisibleRect();
        System.out.println("（拖拽）当前可视矩形框中心点为： " + (visibleRect.getLocation().getX()) + ", " + (visibleRect.getLocation().getY()) + "\n" + visibleRect.getWidth() + ", " + visibleRect.getHeight() + "\n" + getPreferredSize().width + ", " + getPreferredSize().height);
        visibleRect.translate(-dx, -dy);
        scrollRectToVisible(visibleRect);
        oldCursorPos = newCursorPos;
    }
}

class ToolBarStatusFrame extends JFrame {
    private final JToolBar toolbar = new JToolBar();
    private final JLabel status = new JLabel();

    public ToolBarStatusFrame() {
        Container cp = getContentPane();
        cp.add(toolbar, BorderLayout.NORTH);
        cp.add(status, BorderLayout.SOUTH);
    }

    public void setToolBarComponentsEnabled(boolean... enabled) {
        for (int i = 0; i < enabled.length; i++) {
            toolbar.getComponent(i).setEnabled(enabled[i]);
        }
    }

    public void addToolBarComponents(JComponent... comp) {
        for (int i = 0; i < comp.length; i++) {
            toolbar.add(comp[i]);
        }
    }

    public void setStatus(String statusText) {
        status.setText(statusText);
    }
}

final class MyPicViewer extends ToolBarStatusFrame {
    private JButton btn_LastPage;
    private JButton btn_NextPage;
    private JButton btn_winTopButton;
    public JButton btn_autoTrans;
    public JButton btn_XZQTree;
    public JButton btn_OtherTypeMap;

    public JButton btn_DistanceMessure;
    public JButton btn_AreaMessure;
    public JButton btn_Search;

    public static ZoomablePicture view = new ScrollablePicture();
    private float max_lat, min_lat, max_long, min_long;
    public static List<XZQTree> xzqTrees;
    public static List<Map> maps;
    public static JFrame SearchWindow=null;
    private static MyPicViewer myPicViewer = null;

    public static void main(String [] args) {

        /*mapList.add(new Map("", "序图组", 0, ""));
        mapList.add(new Map("", "资源与环境", 0, ""));
        mapList.add(new Map("", "社会经济图组", 0, ""));
        mapList.add(new Map("", "区域地理图组", 0, ""));
        mapList.add(new Map("序图组", "世界地图", 1, ""));
        mapList.add(new Map("序图组", "中国政区", 1, ""));
        mapList.add(new Map("资源与环境图组", "人口劳动力", 2, ""));
        mapList.add(new Map("社会经济图组", "综合经济", 3, ""));
        mapList.add(new Map("区域地理图组", "县图", 4, ""));
        mapList.add(new Map("区域地理图组", "各县城区图", 4, ""));
        mapList.add(new Map("区域地理图组", "各县影像图", 4, ""));
        mapList.add(new Map("区域地理图组", "乡镇图", 4, ""));
        mapList.add(new Map("世界地图", "test", 5, "D:\\\\test.png"));*/

        //tring [] nameStd = {"序图组","资源与环境图组","社会经济图组","区域地理图组"};

        //List<Map> mapList = GetMapData();


        //基础窗口
        /*JFrame BaseFrame = new JFrame();

        BaseFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);*/

        //OpenOldVersionElectricAtlas();
        //OpenMidVersionElectricAtlas();
        OpenNewVersionElectricAtlas();
        //LoadNextMapTypeOneWindow(main_frame, mapList, FindNextMapList(mapList, ""));
        //new MyPicViewer("D:\\test.png");
    }

    private static void OpenNewVersionElectricAtlas(){
        maps = GetMapData();

        /*for (int i = 0; i < maps.size(); i++) {
            System.out.println(maps.get(i).getParentNode() + ", " + maps.get(i).getName());
        }*/

        List<XZQTree> xzqTrees = GetXZQTree();
        //获取屏幕的尺寸
        Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println(scrSize.getWidth() + ", " + scrSize.getHeight());


        BackgroundImageJFrame BaseFrame = new BackgroundImageJFrame();


        //置顶小窗
        JFrame main_frame = new JFrame();

        main_frame.setIconImage(Toolkit.getDefaultToolkit().getImage("C:/lzyFile/白底LOGO1.png"));

        main_frame.setAlwaysOnTop(true);

        main_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        main_frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                //
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (myPicViewer==null || !myPicViewer.isVisible()){
                    int MinPage = Integer.MAX_VALUE;
                    int MinPageIndex = Integer.MAX_VALUE;
                    for (int i = 0; i < maps.size(); i++) {
                        System.out.println(i);
                        System.out.println(GetPage(maps.get(i).getName()));
                        System.out.println((maps.get(i).getName()));
                        if (!maps.get(i).getPath().trim().equals(""))
                        {
                            if (maps.get(i).getPage() < MinPage)
                            {
                                MinPage = maps.get(i).getPage();
                                MinPageIndex = i;
                                System.out.println("lzy: " + maps.get(i).getPath());
                            }
                        }
                    }
                    System.out.println("lzy: " + maps.get(MinPageIndex).getPath());
                    /*try {
                        mcopy(new File(maps.get(MinPageIndex).getPath()), new File(maps.get(MinPageIndex).getPath().replace("xml", "png")));
                    }
                    catch (Exception ee){
                        System.out.println(ee.toString());
                    }*/
                    myPicViewer = new MyPicViewer(maps.get(MinPageIndex).getPath(), maps.get(MinPageIndex).getName(), maps.get(MinPageIndex).getMapGeoInfo(), maps, true);
                    myPicViewer.setDefaultCloseOperation(EXIT_ON_CLOSE);
                    myPicViewer.setVisible(true);
                    view.setZoomFactor(1);
                    System.out.println("Windows Closed");
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        LoadNextMapTypeForNewVersion(main_frame, FindNextMapList(""), BaseFrame);
        //初始化窗体内容，此时parentNodeName为空，表示当前为根目录窗体
        //LoadNextMapTypeForNewVersion(main_frame, FindNextMapList(""), BaseFrame);
    }

    private static void mcopy(File inputFile, File outputFile) throws Exception{
        FileInputStream  fis = new FileInputStream(inputFile);
        FileOutputStream fos = new FileOutputStream(outputFile);
        copy(fis,fos);
        fis.close();
        fos.close();
    }

    private static void copy(InputStream ips,OutputStream ops) throws Exception{
        int len = 0;
        byte[] buf = new byte[1024];
        while((len = ips.read(buf)) != -1){
            ops.write(buf,0,len);
        }
    }

    private static void OpenMidVersionElectricAtlas(){
        maps = GetMapData();
        List<XZQTree> xzqTrees = GetXZQTree();
        BackgroundImageJFrame BaseFrame = new BackgroundImageJFrame();
        BaseFrame.setVisible(true);
        int MinPage = Integer.MAX_VALUE;
        int MinPageIndex = Integer.MAX_VALUE;
        for (int i = 0; i < maps.size(); i++) {
            System.out.println(i);
            System.out.println(GetPage(maps.get(i).getName()));
            System.out.println((maps.get(i).getName()));
            if (!maps.get(i).getPath().trim().equals(""))
            {
                if (maps.get(i).getPage() < MinPage)
                {
                    MinPage = maps.get(i).getPage();
                    MinPageIndex = i;
                    System.out.println("lzy: " + maps.get(i).getPath());
                }
            }
        }
        System.out.println("lzy: " + maps.get(MinPageIndex).getPath());
        JFrame j = new MyPicViewer(maps.get(MinPageIndex).getPath(), maps.get(MinPageIndex).getName(), maps.get(MinPageIndex).getMapGeoInfo(), maps, true);
        j.setDefaultCloseOperation(EXIT_ON_CLOSE);
        j.setVisible(true);
        view.setZoomFactor(1);
    }

    private static void OpenOldVersionElectricAtlasForMidVersion(MyPicViewer myPicViewer) {
        //maps = GetMapData();

        /*for (int i = 0; i < maps.size(); i++) {
            System.out.println(maps.get(i).getParentNode() + ", " + maps.get(i).getName());
        }*/

        //List<XZQTree> xzqTrees = GetXZQTree();
        //获取屏幕的尺寸


        if (SearchWindow == null){
            //置顶小窗
            SearchWindow = new JFrame();

            SearchWindow.setIconImage(Toolkit.getDefaultToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
            SearchWindow.setAlwaysOnTop(true);
        }
        else{
            SearchWindow.setAlwaysOnTop(true);
        }

        //初始化窗体内容，此时parentNodeName为空，表示当前为根目录窗体
        LoadNextMapTypeForMidVersion(SearchWindow, FindNextMapList(""), myPicViewer);
    }

    private static void OpenOldVersionElectricAtlas() {
        maps = GetMapData();

        /*for (int i = 0; i < maps.size(); i++) {
            System.out.println(maps.get(i).getParentNode() + ", " + maps.get(i).getName());
        }*/

        List<XZQTree> xzqTrees = GetXZQTree();
        //获取屏幕的尺寸
        Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println(scrSize.getWidth() + ", " + scrSize.getHeight());


        BackgroundImageJFrame BaseFrame = new BackgroundImageJFrame();


        //置顶小窗
        JFrame main_frame = new JFrame();

        main_frame.setIconImage(Toolkit.getDefaultToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
        main_frame.setAlwaysOnTop(true);

        //初始化窗体内容，此时parentNodeName为空，表示当前为根目录窗体
        LoadNextMapType(main_frame, FindNextMapList(""), BaseFrame);
    }

    public static String readtxt(String path) {
        //按行读取，不能保留换行等格式，所以需要手动添加每行换行符。
        //String result = "";
        StringBuffer txtContent = new StringBuffer();
        File file = new File(path);
        try {
            int len = 0;
            FileInputStream in = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(in, "utf-8");
            BufferedReader br = new BufferedReader(reader);
            String s = null;
            while ((s = br.readLine()) != null) {
                if (len != 0) {// 处理换行符的问题，第一行不换行
                    txtContent.append(new String(("\r\n" + s).getBytes(), "utf-8"));
                } else {
                    txtContent.append(new String(s.getBytes(), "utf-8"));
                }
                len++;
            }
            reader.close();
            in.close();
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    /*try {
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        String s = null;
        while((s=br.readLine())!=null){
            result = result + "\n" + s;
        }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }*/
        return txtContent.toString();
    }

    private static List<XZQTree> GetXZQTree(){
        try {
            //List<XZQTree> xzqTrees = new ArrayList<>();
            xzqTrees = new ArrayList<>();
            String DataFilePath = "C:\\lzyFile\\行政区划树.txt";
            String Data = readtxt(DataFilePath);
            String[] mData = Data.split("\n");
            String LastXZQNameForLevel0 = "";
            String LastXZQNameForLevel1 = "";
            String LastXZQNameForLevel2 = "";
            for (int i = 0; i < mData.length; i++) {
                String line = mData[i];
                String[] strings = line.split(",");
                String XZQName = strings[0];
                int XZQNum = Integer.valueOf(strings[1].trim());
                XZQTree xzqTree;
                switch (XZQNum){
                    case 0:
                        xzqTree = new XZQTree(XZQName, XZQNum, "");
                        LastXZQNameForLevel0 = XZQName;
                        xzqTrees.add(xzqTree);
                        break;
                    case 1:
                        xzqTree = new XZQTree(XZQName, XZQNum, LastXZQNameForLevel0);
                        LastXZQNameForLevel1 = XZQName;
                        xzqTrees.add(xzqTree);
                        break;
                    case 2:
                        xzqTree = new XZQTree(XZQName, XZQNum, LastXZQNameForLevel1);
                        LastXZQNameForLevel2 = XZQName;
                        xzqTrees.add(xzqTree);
                        break;
                    case 3:
                        xzqTree = new XZQTree(XZQName, XZQNum, LastXZQNameForLevel2);
                        xzqTrees.add(xzqTree);
                        break;
                }
            }
            return xzqTrees;
        }catch (Exception e){
            System.out.println(e.toString());
        }
        return null;
    }

    private static List<Map> GetMapData(){
        List<Map> mapList = new ArrayList<>();
        try {
            //String DataFilePath = "C:\\Users\\54286\\Desktop\\临沧市地图集\\data.txt";// 要读取以上路径的data.txt文件
            String DataFilePath = "C:\\lzyFile\\临沧市地图集\\data.txt";// 要读取以上路径的data.txt文件
            File DataFile = new File(DataFilePath);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(DataFile)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
            line = br.readLine();
            while (line != null) {
                System.out.println(line);
                String[] strings = line.split(",");
                //List<Point2D> FramePts = GetFramePts(strings[4]);
                int Page = GetPage(strings[1]);
                if (strings.length <= 5)
                {
                    mapList.add(new Map(strings[0], strings[1], Integer.parseInt(strings[2]), strings[3], null, Page));
                }
                else {
                    PointF[] pts = GetMapGeoInfo(strings[4], strings[5], strings[6], strings[7]);
                    mapList.add(new Map(strings[0], strings[1], Integer.parseInt(strings[2]), strings[3], pts, Page));
                }
                line = br.readLine(); // 一次读入一行数据
            }
            System.out.println(mapList.size());
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
        mapList = ResetPageNum(mapList);
        return mapList;
    }

    private static List<Map> ResetPageNum(List<Map> mapList){
        int maxPage = 0;
        for (int i = 0; i < mapList.size(); i++) {
            int page = mapList.get(i).getPage();
            if (page > maxPage)
                maxPage = page;
        }

        for (int i = 0; i < mapList.size(); i++) {
            int page = mapList.get(i).getPage();
            if (page < 0)
                mapList.get(i).setPage(Math.abs(page) + maxPage);
        }

        Collections.sort(mapList);

        return mapList;
    }

    private static int GetPage(String FileName){
        String PageNumPart = "";
        char[] chars = FileName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i])){
                PageNumPart+=chars[i];
            }
            else
                break;
        }
        if (PageNumPart.length() > 0) {
            System.out.println("当前pdf文件页码为" + PageNumPart);
            return Integer.valueOf(PageNumPart);
        }
        else
            return 0;
    }

    private static int GetPage1(String FileName){
        Boolean isSpecialPage = false;
        if (FileName.contains("00-"))
        {
            FileName = FileName.replace("00-", "");
            isSpecialPage = true;
        }
        String PageNumPart = "";
        char[] chars = FileName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isDigit(chars[i])){
                PageNumPart+=chars[i];
            }
            else
                break;
        }
        if (PageNumPart.length() > 0) {
            System.out.println("当前pdf文件页码为" + PageNumPart);
            if (isSpecialPage)
                return Integer.valueOf(PageNumPart);
            else
                return 0 - Integer.valueOf(PageNumPart);
        }
        else
            return 0;
    }

    private static PointF[] GetMapGeoInfo(String GeoInfo, String MediaBoxInfo, String BBoxInfo, String LptsInfo){
        String MapGeoInfo = "";
        System.out.println("原始坐标： " + GeoInfo);
        MapGeoInfo = DataUtil.getGPTS(GeoInfo, LptsInfo);
        System.out.println("坐标纠偏后： " + MapGeoInfo);
        MapGeoInfo = DataUtil.rubberCoordinate(MediaBoxInfo, BBoxInfo, MapGeoInfo);
        String[] MapGeoInfos = MapGeoInfo.split(" ");
        PointF[] pts = new PointF[4];
        for (int i = 0; i < MapGeoInfos.length; i+=2) {
            pts[i/2] = new PointF(Float.valueOf(MapGeoInfos[i]), Float.valueOf(MapGeoInfos[i+1]));
        }
        return pts;
    }

    private static List<Point2D> GetFramePts(String str){
        List<Point2D> FramePts = new ArrayList<>();
        String FramePtStrs = str;
        String[] ptStrs = FramePtStrs.split(";");
        for (int i = 0; i < ptStrs.length; i++) {
            String[] pt = ptStrs[i].split("&");
            FramePts.add(new Point2D(Float.parseFloat(pt[0]), Float.parseFloat(pt[1])));
        }
        return FramePts;
    }

    private void ss(){
        final List<Map> mapList = new ArrayList<>();
        String [] nameStd = {"序图组","资源与环境","社会与经济","区域地理图组"};
        Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println(scrSize.getWidth() + ", " + scrSize.getHeight());
        JFrame main_frame = new JFrame();
        main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main_frame.setTitle("临沧市地图集");
        main_frame.setLayout(new FlowLayout());
        main_frame.setSize(260,280);
        main_frame.setLocation(scrSize.width/2 - 130,scrSize.height/2 - 140);
        for(int i=0;i<4;i++)
        {
            final String friend_name=nameStd[i];
            JButton now = new JButton(friend_name);
            now.setPreferredSize(new Dimension(200,50));
            main_frame.add(now);
            // chose someone to talk
            ActionListener listener = new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    main_frame.getContentPane().removeAll();
                    main_frame.repaint();
                    main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    main_frame.setTitle("临沧市地图集");
                    main_frame.setLayout(new FlowLayout());
                    main_frame.setSize(260,280);
                    main_frame.setLocation(scrSize.width/2 - 130,scrSize.height/2 - 140);
                    for (int j = 0; j < mapList.size(); j++) {
                        Map m = mapList.get(j);
                        if (m.getParentNode().equals(friend_name)){
                            System.out.println(friend_name);
                            JButton now = new JButton(m.getName());
                            now.setPreferredSize(new Dimension(200,50));
                            main_frame.add(now);
                            // chose someone to talk
                            ActionListener listener = new ActionListener()
                            {
                                public void actionPerformed(ActionEvent event)
                                {
                                    /*main_frame.removeAll();
                                    for (int j = 0; j < mapList.size(); j++) {
                                        Map m = mapList.get(j);
                                        if (m.getParentNode().equals(friend_name)){

                                        }
                                    }*/
                                }
                            };
                            now.addActionListener(listener);
                        }
                    }
                    main_frame.repaint();
                    main_frame.setVisible(true);
                }
            };
            now.addActionListener(listener);
        }

        main_frame.setVisible(true);
    }

    static private void LoadNextMapTypeOneWindow(JFrame main_frame, List<Map> maps, List<Map> currentMaps){
        int size = currentMaps.size();
        System.out.println(currentMaps.get(0).getPath());
        if (size == 1 && !currentMaps.get(0).getPath().isEmpty()) {
            main_frame.setEnabled(false);
            new MyPicViewer(currentMaps.get(0).getPath(), main_frame, currentMaps.get(0).getName(), currentMaps.get(0).getMapGeoInfo(), maps);
        }
        else if (size == 0) {

        }
        else {
            JPanel jPanel = new JPanel();
            int currentMapType = currentMaps.get(0).getMapType();
            main_frame.getContentPane().removeAll();
            Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
            main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main_frame.setLayout(new FlowLayout(FlowLayout.CENTER));
            //main_frame.setLayout(new BorderLayout());

            // 将同层级的地图添加到JPanel中
            for (int i = 0; i < size; i++) {
                final String btName = currentMaps.get(i).getName();
                JButton now = new JButton(btName);
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        List<Map> newMaps = FindNextMapList(btName);
                        LoadNextMapTypeOneWindow(main_frame, maps, newMaps);
                    }
                };
                now.addActionListener(listener);
            }

            // 在除了主索引层外的其他层中添加返回按钮
            int currentHeight = size * 50 + 25;
            main_frame.setTitle("临沧市地图集");
            if (currentMapType > 0) {
                main_frame.setTitle(currentMaps.get(0).getParentNode());
                currentHeight = currentHeight + 50;
                JButton now = new JButton("返回");
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        List<Map> newMaps = FindParentMapList(maps, currentMaps.get(0).getParentNode());
                        System.out.println(newMaps.size() + ", " + currentMaps.get(0).getParentNode());
                        main_frame.getContentPane().removeAll();
                        LoadNextMapTypeOneWindow(main_frame, maps, newMaps);
                    }
                };
                now.addActionListener(listener);
            }
            jPanel.setPreferredSize(new Dimension(260, currentHeight));
            jPanel.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            jPanel.setBackground(Color.GRAY);
            //jPanel.setBounds(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2, 260 , currentHeight);
            //main_frame.add(jPanel, BorderLayout.CENTER);
            JPanel HoldPositionPanel = new JPanel();
            HoldPositionPanel.setPreferredSize(new Dimension(260, (scrSize.height - currentHeight)/2-30));
            JPanel CenterPanel = new JPanel();
            CenterPanel.setPreferredSize(new Dimension(260, scrSize.height));
            /*JPanel QueryPanel = GetQueryField();
            CenterPanel.add(QueryPanel);*/
            CenterPanel.add(HoldPositionPanel);
            CenterPanel.add(jPanel);
            /*main_frame.add(jPanel1);
            main_frame.add(jPanel);*/
            main_frame.add(CenterPanel);
            //main_frame.setSize(260, currentHeight);
            //main_frame.setSize(800, 600);
            main_frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            //jPanel.setPreferredSize(new Dimension());
            //main_frame.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            main_frame.repaint();
            main_frame.setVisible(true);
        }
    }

    private static JPanel GetQueryFieldForNewVersion(JFrame frame){
        JLabel label = new JLabel("查询：");

        //创建JTextField，16表示16列，用于JTextField的宽度显示而不是限制字符个数
        JTextField textField = new JTextField(8);
        JButton button = new JButton("确定");

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());
        //添加控件
        jPanel.add(label);
        jPanel.add(textField);
        jPanel.add(button);
        JComboBox comboBox = new JComboBox();

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                QueryStringFunction(textField, comboBox, jPanel, frame);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                QueryStringFunction(textField, comboBox, jPanel, frame);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                System.out.println(textField.getText());
            }
        });



        //按钮点击处理 lambda表达式
        button.addActionListener((e) -> {
            String QuriedMapName = comboBox.getSelectedItem().toString();
            for (int i = 0; i < maps.size(); i++) {
                Map map = maps.get(i);
                if (map.getName().equals(QuriedMapName))
                {
                    frame.setAlwaysOnTop(false);
                    if (myPicViewer==null || !myPicViewer.isVisible()) {
                        frame.setVisible(false);
                        /*try {
                            mcopy(new File(map.getPath()), new File(map.getPath().replace("xml", "png")));
                        }
                        catch (Exception ee){
                            System.out.println(ee.toString());
                        }*/
                        myPicViewer = new MyPicViewer(map.getPath(), map.getName(), map.getMapGeoInfo(), maps, true);
                        myPicViewer.setDefaultCloseOperation(EXIT_ON_CLOSE);
                        myPicViewer.setVisible(true);
                        view.setZoomFactor(1);
                    }
                    else
                    {
                        /*try {
                            mcopy(new File(map.getPath()), new File(map.getPath().replace("xml", "png")));
                        }
                        catch (Exception ee){
                            System.out.println(ee.toString());
                        }*/
                        view.load(map.getPath(), map.getMapGeoInfo(), maps, myPicViewer, map.getName());
                    }
                    break;
                }
            }
        });
        return jPanel;
    }

    private static JPanel GetQueryField(JFrame frame){
        JLabel label = new JLabel("查询：");

        //创建JTextField，16表示16列，用于JTextField的宽度显示而不是限制字符个数
        JTextField textField = new JTextField(8);
        JButton button = new JButton("确定");

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout());
        //添加控件
        jPanel.add(label);
        jPanel.add(textField);
        jPanel.add(button);
        JComboBox comboBox = new JComboBox();

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                QueryStringFunction(textField, comboBox, jPanel, frame);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                QueryStringFunction(textField, comboBox, jPanel, frame);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                System.out.println(textField.getText());
            }
        });



        //按钮点击处理 lambda表达式
        button.addActionListener((e) -> {
            String QuriedMapName = comboBox.getSelectedItem().toString();
            for (int i = 0; i < maps.size(); i++) {
                Map map = maps.get(i);
                if (map.getName().equals(QuriedMapName))
                {
                    frame.setAlwaysOnTop(false);
                    frame.setEnabled(false);
                    /*try {
                        mcopy(new File(map.getPath()), new File(map.getPath().replace("xml", "png")));
                    }
                    catch (Exception ee){
                        System.out.println(ee.toString());
                    }*/
                    new MyPicViewer(map.getPath(), frame, map.getName(), map.getMapGeoInfo(), maps);
                    break;
                }
            }
        });
        return jPanel;
    }

    private static void QueryStringFunction(JTextField textField, JComboBox comboBox, JPanel jPanel, JFrame frame){
        System.out.println(textField.getText());
        try {
            int textNum = textField.getText().trim().length();
            Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();

            if (textNum > 0){
                List<String> QueriedMaps = new ArrayList<>();
                int MaxFileNameLength = 0;
                for (int i = 0; i < maps.size(); i++) {
                    Map map = maps.get(i);
                    if (map.getPath().trim().length() > 0 && map.getName().contains(textField.getText().trim())){
                        QueriedMaps.add(map.getName());
                        if (map.getName().length() > MaxFileNameLength)
                            MaxFileNameLength = map.getName().length();
                    }
                }
                comboBox.setBounds(15, 15, (int)(MaxFileNameLength / 6.0 * 100), 25);
                // TODO

                frame.setLocation((scrSize.width / 2 - 130)-(int)(MaxFileNameLength / 6.0 * 100)/2, frame.getY());
                frame.setSize(260, frame.getHeight());

                if (jPanel.getComponentCount() == 3)
                {
                    jPanel.add(comboBox);
                    frame.setSize(frame.getWidth() + comboBox.getWidth(), frame.getHeight());
                }
                else
                {
                    frame.setLocation((scrSize.width / 2 - 130)-(int)(MaxFileNameLength / 6.0 * 100)/2, frame.getY());
                    //frame.setLocation(scrSize.width / 2 - 130, frame.getY());
                    frame.setSize(frame.getWidth() + comboBox.getWidth(), frame.getHeight());
                }
                String[] strings = new String[QueriedMaps.size()];

                for (int i = 0; i < strings.length; i++) {
                    strings[i] = QueriedMaps.get(i);
                }

                // 添加选项值
                comboBox.setModel(new DefaultComboBoxModel(strings));
            }
            else{
                frame.setLocation(scrSize.width / 2 - 130, frame.getY());
                frame.setSize(260, frame.getHeight());
                jPanel.remove(comboBox);
            }
        }
        catch (Exception ee){
            if (jPanel.getComponentCount() == 4)
            {
                Dimension scrSize=Toolkit.getDefaultToolkit().getScreenSize();
                frame.setLocation(scrSize.width / 2 - 130, frame.getY());
                frame.setSize(260, frame.getHeight());
                jPanel.remove(comboBox);
            }
            System.out.println(ee.toString());
        }

        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {

            }
        });
    }

    //事件处理
    private static void QueryButtonFunction(String QueryText, JFrame frame)
    {
        String str = QueryText;//获取输入内容
        //判断是否输入了
        if(str.equals(""))
        {
            Object[] options = { "OK ", "CANCEL " };
            JOptionPane.showOptionDialog(frame, "您还没有输入 ", "提示", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE,null, options, options[0]);
        }
        else
        {
            JOptionPane.showMessageDialog(frame,"您输入了：" + str);
        }

    }

    private static void LoadNextMapTypeForNewVersion(JFrame main_frame, List<Map> currentMaps, JFrame BaseFrame){
        int size = currentMaps.size();
        System.out.println(size);
        if (size == 1 && !currentMaps.get(0).getPath().isEmpty()) {
            //main_frame.setEnabled(false);
            main_frame.setAlwaysOnTop(false);
            main_frame.setVisible(false);

            /*try {
                mcopy(new File(maps.get(0).getPath()), new File(maps.get(0).getPath().replace("xml", "png")));
            }
            catch (Exception ee){
                System.out.println(ee.toString());
            }*/
            myPicViewer = new MyPicViewer(maps.get(0).getPath(), maps.get(0).getName(), maps.get(0).getMapGeoInfo(), maps, true);
            myPicViewer.setDefaultCloseOperation(EXIT_ON_CLOSE);
            myPicViewer.setVisible(true);
            view.setZoomFactor(1);
        }
        else if (size == 0) {

        }
        else {
            System.out.println("lzy");
            JPanel jPanel = new JPanel();
            int currentMapType = currentMaps.get(0).getMapType();
            main_frame.getContentPane().removeAll();
            Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
            main_frame.setLayout(new FlowLayout(FlowLayout.CENTER));
            //main_frame.setLayout(new BorderLayout());

            // 将同层级的地图添加到JPanel中
            for (int i = 0; i < size; i++) {
                final String btName = currentMaps.get(i).getName();
                JButton now = new JButton(btName);
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                final String path = currentMaps.get(i).getPath();
                final String Name = currentMaps.get(i).getName();
                final PointF[] pts = currentMaps.get(i).getMapGeoInfo();
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (path.trim().length()!=0)
                        {
                            //main_frame.setEnabled(false);
                            main_frame.setAlwaysOnTop(false);
                            main_frame.setVisible(false);
                            //myPicViewer = new MyPicViewer(path, main_frame, Name, pts, maps);
                            /*try {
                                mcopy(new File(path), new File(path.replace("xml", "png")));
                            }
                            catch (Exception ee){
                                System.out.println(ee.toString());
                            }*/
                            myPicViewer = new MyPicViewer(path, Name, pts, maps, true);
                            myPicViewer.setDefaultCloseOperation(EXIT_ON_CLOSE);
                            myPicViewer.setVisible(true);
                            view.setZoomFactor(1);
                        }
                        else {
                            List<Map> newMaps = FindNextMapList(btName);
                            LoadNextMapTypeForNewVersion(main_frame, newMaps, BaseFrame);
                        }
                    }
                };
                now.addActionListener(listener);
            }

            // 在除了主索引层外的其他层中添加返回按钮
            int currentHeight = size * 50 + size * 6;
            main_frame.setTitle("临沧市地图集");
            if (currentMapType > 0) {
                main_frame.setTitle(currentMaps.get(0).getParentNode());
                currentHeight = currentHeight + 50;
                JButton now = new JButton("返回");
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        List<Map> newMaps = FindParentMapList(maps, currentMaps.get(0).getParentNode());
                        System.out.println(newMaps.size() + ", " + currentMaps.get(0).getParentNode());
                        main_frame.getContentPane().removeAll();
                        LoadNextMapTypeForNewVersion(main_frame, newMaps, BaseFrame);
                    }
                };
                now.addActionListener(listener);
            }
            jPanel.setPreferredSize(new Dimension(260, currentHeight+10));
            //jPanel.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            JPanel QueryPanel = GetQueryFieldForNewVersion(main_frame);
            main_frame.add(QueryPanel);
            main_frame.add(jPanel);
            System.out.println(jPanel.getPreferredSize().getHeight());
            main_frame.setSize(260, (int)jPanel.getPreferredSize().getHeight() + 50 + 50);
            main_frame.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            main_frame.repaint();
            BaseFrame.setVisible(true);
            main_frame.setVisible(true);
        }
    }

    private static void LoadNextMapTypeForMidVersion(JFrame main_frame, List<Map> currentMaps, MyPicViewer myPicViewer){
        int size = currentMaps.size();
        System.out.println(size);
        if (size == 1 && !currentMaps.get(0).getPath().isEmpty()) {
            main_frame.setEnabled(false);
            main_frame.setAlwaysOnTop(true);
            //new MyPicViewer(currentMaps.get(0).getPath(), main_frame, currentMaps.get(0).getName(), currentMaps.get(0).getMapGeoInfo(), maps);
            view.load(currentMaps.get(0).getPath(), currentMaps.get(0).getMapGeoInfo(), currentMaps, myPicViewer, currentMaps.get(0).getName());
        }
        else if (size == 0) {

        }
        else {
            JPanel jPanel = new JPanel();
            int currentMapType = currentMaps.get(0).getMapType();
            main_frame.getContentPane().removeAll();
            Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
            main_frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            main_frame.setLayout(new FlowLayout(FlowLayout.CENTER));
            //main_frame.setLayout(new BorderLayout());

            // 将同层级的地图添加到JPanel中
            for (int i = 0; i < size; i++) {
                final String btName = currentMaps.get(i).getName();
                JButton now = new JButton(btName);
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                final String path = currentMaps.get(i).getPath();
                final String Name = currentMaps.get(i).getName();
                final PointF[] pts = currentMaps.get(i).getMapGeoInfo();
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (path.trim().length()!=0)
                        {
                            //main_frame.setEnabled(false);
                            main_frame.setAlwaysOnTop(true);
                            //new MyPicViewer(path, main_frame, Name, pts, maps);
                            view.load(path, pts, maps, myPicViewer, Name);
                        }
                        else {
                            List<Map> newMaps = FindNextMapList(btName);
                            LoadNextMapTypeForMidVersion(main_frame, newMaps, myPicViewer);
                        }
                    }
                };
                now.addActionListener(listener);
            }

            // 在除了主索引层外的其他层中添加返回按钮
            int currentHeight = size * 50 + size * 6;
            main_frame.setTitle("临沧市地图集");
            if (currentMapType > 0) {
                main_frame.setTitle(currentMaps.get(0).getParentNode());
                currentHeight = currentHeight + 50;
                JButton now = new JButton("返回");
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        List<Map> newMaps = FindParentMapList(maps, currentMaps.get(0).getParentNode());
                        System.out.println(newMaps.size() + ", " + currentMaps.get(0).getParentNode());
                        main_frame.getContentPane().removeAll();
                        LoadNextMapTypeForMidVersion(main_frame, newMaps, myPicViewer);
                    }
                };
                now.addActionListener(listener);
            }
            jPanel.setPreferredSize(new Dimension(260, currentHeight+10));
            //jPanel.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            JPanel QueryPanel = GetQueryFieldForNewVersion(main_frame);
            main_frame.add(QueryPanel);
            main_frame.add(jPanel);
            System.out.println(jPanel.getPreferredSize().getHeight());
            main_frame.setSize(260, (int)jPanel.getPreferredSize().getHeight() + 50 + 50);
            main_frame.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            main_frame.repaint();
            //BaseFrame.setVisible(true);
            main_frame.setVisible(true);
        }
    }

    private static void LoadNextMapType(JFrame main_frame, List<Map> currentMaps, JFrame BaseFrame){
        int size = currentMaps.size();
        System.out.println(size);
        if (size == 1 && !currentMaps.get(0).getPath().isEmpty()) {
            main_frame.setEnabled(false);
            main_frame.setAlwaysOnTop(false);
            new MyPicViewer(currentMaps.get(0).getPath(), main_frame, currentMaps.get(0).getName(), currentMaps.get(0).getMapGeoInfo(), maps);
        }
        else if (size == 0) {

        }
        else {
            JPanel jPanel = new JPanel();
            int currentMapType = currentMaps.get(0).getMapType();
            main_frame.getContentPane().removeAll();
            Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
            main_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            main_frame.setLayout(new FlowLayout(FlowLayout.CENTER));
            //main_frame.setLayout(new BorderLayout());

            // 将同层级的地图添加到JPanel中
            for (int i = 0; i < size; i++) {
                final String btName = currentMaps.get(i).getName();
                JButton now = new JButton(btName);
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                final String path = currentMaps.get(i).getPath();
                final String Name = currentMaps.get(i).getName();
                final PointF[] pts = currentMaps.get(i).getMapGeoInfo();
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (path.trim().length()!=0)
                        {
                            main_frame.setEnabled(false);
                            main_frame.setAlwaysOnTop(false);
                            new MyPicViewer(path, main_frame, Name, pts, maps);
                        }
                        else {
                            List<Map> newMaps = FindNextMapList(btName);
                            LoadNextMapType(main_frame, newMaps, BaseFrame);
                        }
                    }
                };
                now.addActionListener(listener);
            }

            // 在除了主索引层外的其他层中添加返回按钮
            int currentHeight = size * 50 + size * 6;
            main_frame.setTitle("临沧市地图集");
            if (currentMapType > 0) {
                main_frame.setTitle(currentMaps.get(0).getParentNode());
                currentHeight = currentHeight + 50;
                JButton now = new JButton("返回");
                now.setPreferredSize(new Dimension(200, 50));
                //main_frame.add(now);
                jPanel.add(now);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        List<Map> newMaps = FindParentMapList(maps, currentMaps.get(0).getParentNode());
                        System.out.println(newMaps.size() + ", " + currentMaps.get(0).getParentNode());
                        main_frame.getContentPane().removeAll();
                        LoadNextMapType(main_frame, newMaps, BaseFrame);
                    }
                };
                now.addActionListener(listener);
            }
            jPanel.setPreferredSize(new Dimension(260, currentHeight+10));
            //jPanel.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            JPanel QueryPanel = GetQueryField(main_frame);
            main_frame.add(QueryPanel);
            main_frame.add(jPanel);
            System.out.println(jPanel.getPreferredSize().getHeight());
            main_frame.setSize(260, (int)jPanel.getPreferredSize().getHeight() + 50 + 50);
            main_frame.setLocation(scrSize.width / 2 - 130, scrSize.height / 2 - currentHeight / 2);
            main_frame.repaint();
            BaseFrame.setVisible(true);
            main_frame.setVisible(true);
        }
    }

    static private List<Map> FindParentMapList(List<Map> maps, String parentNodeName){
        List<Map> newMaps = new ArrayList<>();
        int MapType = -1;
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getName().equals(parentNodeName))
                MapType = maps.get(i).getMapType();
        }

        System.out.println(MapType);

        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getMapType() == MapType)
                newMaps.add(maps.get(i));
        }
        return newMaps;
    }

    static private List<Map> FindNextMapList(String parentNodeName){
        List<Map> newMaps = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getParentNode().equals(parentNodeName))
                newMaps.add(maps.get(i));
        }
        return newMaps;
    }

    public MyPicViewer(String path, String name, PointF[] pts, List<Map> maps, boolean ff) {
        setTitle(name);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));

        //System.out.println(pts==null);
        createToolBarButtonsForNewVersion(this);
        //setToolBarComponentsEnabled(true, false, false, false, false, true);

        JScrollPane jsp = new JScrollPane(view);
        getContentPane().add(jsp);

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                //
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                //frame.setAlwaysOnTop(true);
                //frame.setEnabled(true);
                //frame.setVisible(true);
                view.setUndecoratedFrame(false);
                RemovePopWindow();
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        /*this.pictureList = fc.getAbsolutePathsRecursively();
=        System.out.println("最大经度： " + max_long + "\n" + "最大纬度： " + max_lat + "\n" + "  最小经度： " + min_long + "\n" + "最小纬度： " + min_lat);
        this.pictureIndex = (this.pictureList.length > 0) ? 0 : -1;*/

        showCurrentPicture(path, pts, maps, name);
        Image image = Toolkit.getDefaultToolkit().getImage(path);
        setSize(image.getWidth(null), image.getHeight(null));

        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        if((scrSize.height / 2 - image.getHeight(null) / 2) < 0) {
            setLocation(scrSize.width / 2 - image.getWidth(null)/2, 0);
        }
        else {
            setLocation(scrSize.width / 2 - image.getWidth(null)/2, scrSize.height / 2 - image.getHeight(null) / 2);
        }

        /*JButton LeftButton = new JButton();
        LeftButton.setBounds(0, scrSize.height/2 - 50, 100, 100);
        JButton RightButton = new JButton();
        RightButton.setBounds(scrSize.width-100, scrSize.height/2 - 50, 100, 100);

        getContentPane().add(LeftButton);
        getContentPane().add(RightButton);*/

        setVisible(true);
        view.setZoomFactor(1);
        ShowXZQTreeWindow();
        PopWindow.setVisible(true);
    }

    public MyPicViewer(String path, String name, PointF[] pts, List<Map> maps) {
        setTitle(name);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));

        createToolBarButtons();
        //setToolBarComponentsEnabled(true, false, false, false, false, true);

        JScrollPane jsp = new JScrollPane(view);
        getContentPane().add(jsp);

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                //
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                //frame.setAlwaysOnTop(true);
                //frame.setEnabled(true);
                //frame.setVisible(true);
                view.setUndecoratedFrame(false);
                RemovePopWindow();
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        /*this.pictureList = fc.getAbsolutePathsRecursively();
=        System.out.println("最大经度： " + max_long + "\n" + "最大纬度： " + max_lat + "\n" + "  最小经度： " + min_long + "\n" + "最小纬度： " + min_lat);
        this.pictureIndex = (this.pictureList.length > 0) ? 0 : -1;*/

        showCurrentPicture(path, pts, maps, name);
        Image image = Toolkit.getDefaultToolkit().getImage(path);
        setSize(image.getWidth(null), image.getHeight(null));

        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        if((scrSize.height / 2 - image.getHeight(null) / 2) < 0) {
            setLocation(scrSize.width / 2 - image.getWidth(null)/2, 0);
        }
        else {
            setLocation(scrSize.width / 2 - image.getWidth(null)/2, scrSize.height / 2 - image.getHeight(null) / 2);
        }

        /*JButton LeftButton = new JButton();
        LeftButton.setBounds(0, scrSize.height/2 - 50, 100, 100);
        JButton RightButton = new JButton();
        RightButton.setBounds(scrSize.width-100, scrSize.height/2 - 50, 100, 100);

        getContentPane().add(LeftButton);
        getContentPane().add(RightButton);*/

        setVisible(true);
        view.setZoomFactor(1);
        PopWindow.setVisible(true);
    }

    public MyPicViewer(String path, JFrame frame, String name, PointF[] pts, List<Map> maps) {
        setTitle(name);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));

        createToolBarButtons();
        //setToolBarComponentsEnabled(true, false, false, false, false, true);

        JScrollPane jsp = new JScrollPane(view);
        getContentPane().add(jsp);

        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
                //
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {
                //frame.setAlwaysOnTop(true);
                frame.setEnabled(true);
                frame.setVisible(true);
                view.setUndecoratedFrame(false);
                RemovePopWindow();
            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });
        /*this.pictureList = fc.getAbsolutePathsRecursively();
=        System.out.println("最大经度： " + max_long + "\n" + "最大纬度： " + max_lat + "\n" + "  最小经度： " + min_long + "\n" + "最小纬度： " + min_lat);
        this.pictureIndex = (this.pictureList.length > 0) ? 0 : -1;*/

        showCurrentPicture(path, pts, maps, name);
        Image image = Toolkit.getDefaultToolkit().getImage(path);
        setSize(image.getWidth(null), image.getHeight(null));

        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        if((scrSize.height / 2 - image.getHeight(null) / 2) < 0) {
            setLocation(scrSize.width / 2 - image.getWidth(null)/2, 0);
        }
        else {
            setLocation(scrSize.width / 2 - image.getWidth(null)/2, scrSize.height / 2 - image.getHeight(null) / 2);
        }

        /*JButton LeftButton = new JButton();
        LeftButton.setBounds(0, scrSize.height/2 - 50, 100, 100);
        JButton RightButton = new JButton();
        RightButton.setBounds(scrSize.width-100, scrSize.height/2 - 50, 100, 100);

        getContentPane().add(LeftButton);
        getContentPane().add(RightButton);*/

        setVisible(true);
        view.setZoomFactor(1);
        ShowXZQTreeWindow();
        PopWindow.setVisible(true);
    }

    private class ToolbarButton extends JButton {
        public ToolbarButton(String text, String icon, ActionListener l) {
            super(text, new ImageIcon(MyPicViewer.this. getClass().getClassLoader().getResource(icon)));
            addActionListener(l);
            setPreferredSize(new Dimension(100, 21));
        }
    }

    //目录弹出窗体
    private JFrame PopWindow = new JFrame();
    //行政区划树Panel
    private JPanel XZQTreePanel = new JPanel();
    //其它类型地图Panel
    private JPanel OtherTypeMapPanel = new JPanel();
    //记录当前目录弹出窗体的内容类型
    private enum MapCatalogueType{
        XZQTREE, OTHERTYPEMAP, NONE
    }
    private MapCatalogueType CurrentCatalogueType = MapCatalogueType.NONE;

    /*
    显示目录弹出窗体
     */
    private void ShowPopWindow(){
        PopWindow.setVisible(true);
    }

    /*
    关闭目录弹出窗体
     */
    private void RemovePopWindow(){
        PopWindow.setVisible(false);
    }

    /*
    初始化行政区划树窗体
     */
    private void InitXZQTreeWindow(){
        PopWindow.setTitle("行政区划树");
        PopWindow.setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
        PopWindow.getContentPane().removeAll();
        XZQTreePanel.removeAll();
    }

    /*
    初始化其它类型地图窗体
     */
    private void InitOtherTypeMapWindow(){
        PopWindow.setTitle("其它类型地图");
        PopWindow.setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
        PopWindow.getContentPane().removeAll();
        OtherTypeMapPanel.removeAll();
    }

    /*
    调整和显示行政区划树窗体
     */
    private void AdjustAndShowXZQTreeWindow(int size){
        int currentHeight = size * 50 + size * 6;
        XZQTreePanel.setPreferredSize(new Dimension(260, currentHeight));

        //加入垂直滑块
        JScrollPane scrollPane = new JScrollPane(XZQTreePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        //PopWindow.add(jPanel);
        ShowPopWindow(scrollPane);
    }

    /*
    调整和显示其他类型地图目录窗体
     */
    private void AdjustAndShowOtherTypeMapWindow(int size){
        int currentHeight = size * 50 + size * 6;
        OtherTypeMapPanel.setPreferredSize(new Dimension(260, currentHeight));

        //加入垂直滑块
        JScrollPane scrollPane = new JScrollPane(OtherTypeMapPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //PopWindow.add(jPanel);
        ShowPopWindow(scrollPane);
    }

    /*
    显示目录弹出框
     */
    private void ShowPopWindow(JScrollPane scrollPane){
        //PopWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        PopWindow.add(scrollPane);
        //PopWindow.setSize(260, (int)scrollPane.getPreferredSize().getHeight() + 50);
        PopWindow.setSize(260, 600);
        PopWindow.setIconImage(getToolkit().getImage("C:/lzyFile/白底LOGO1.png"));
        PopWindow.repaint();
        PopWindow.setVisible(true);
        PopWindow.setLocation(0,50);
        PopWindow.setAlwaysOnTop(true);
    }

    /*
    乡镇级以上行政区划单元的点击事件
     */
    private List<XZQTree> ExtendXZQForLevel012(String XZQName, int XZQNum){
        List<XZQTree> xzqTreeList = new ArrayList<>();
        for (int i = 0; i < xzqTrees.size(); i++) {
            XZQTree xzqTree = xzqTrees.get(i);
            if (xzqTree.getXZQNum() <= XZQNum)
                xzqTreeList.add(xzqTree);
            else if (xzqTree.getXZQNum() == XZQNum+1 && xzqTree.getLastXZQName().equals(XZQName))
                xzqTreeList.add(xzqTree);
            if (xzqTree.getXZQName().equals(XZQName))
            {
                for (int j = 0; j < maps.size(); j++) {
                    Map map = maps.get(j);
                    // TODO 2/18 行政区划树
                    if (map.getMapGeoInfo() != null && map.getName().contains(XZQName))
                    {
                        XZQTree xzqTree1 = new XZQTree(map.getName(), 5, XZQName);
                        xzqTreeList.add(xzqTree1);
                    }
                }
            }
        }
        return xzqTreeList;
    }

    /*
    乡镇级行政区划单元的点击事件
     */
    private List<XZQTree> ExtendXZQForLevel3(String XZQName, String LastXZQName, int XZQNum){
        List<XZQTree> xzqTreeList = new ArrayList<>();
        for (int i = 0; i < xzqTrees.size(); i++) {
            XZQTree xzqTree = xzqTrees.get(i);
            if (xzqTree.getXZQNum() <= XZQNum-1)
                xzqTreeList.add(xzqTree);
            else if (xzqTree.getXZQNum() == XZQNum && xzqTree.getLastXZQName().equals(LastXZQName))
                xzqTreeList.add(xzqTree);
            else if (xzqTree.getXZQNum() == XZQNum+1 && xzqTree.getLastXZQName().equals(XZQName))
                xzqTreeList.add(xzqTree);
            if (xzqTree.getXZQName().equals(XZQName))
            {
                for (int j = 0; j < maps.size(); j++) {
                    Map map = maps.get(j);
                    if (map.getMapGeoInfo() != null && map.getName().contains(XZQName))
                    {
                        XZQTree xzqTree1 = new XZQTree(map.getName(), 5, XZQName);
                        xzqTreeList.add(xzqTree1);
                    }
                }
            }
        }
        return xzqTreeList;
    }

    /*
    获取上一级行政区名称
     */
    private String GetLastXZQName(List<XZQTree> xzqTrees, String XZQName){
        for (int i = 0; i < xzqTrees.size(); i++) {
            if (xzqTrees.get(i).getXZQName().equals(XZQName))
                return xzqTrees.get(i).getLastXZQName();
        }
        return "";
    }

    /*
    显示行政区划树窗体，该方法不用于初次显示
     */
    private void ShowXZQTreeWindow(String XZQName, int XZQNum){
        InitXZQTreeWindow();
        List<XZQTree> xzqTreeList = null;

        if (XZQNum <= 2)
            xzqTreeList = ExtendXZQForLevel012(XZQName, XZQNum);
        else
        {
            String LastXZQName = GetLastXZQName(xzqTrees, XZQName);
            xzqTreeList = ExtendXZQForLevel3(XZQName, LastXZQName, XZQNum);
        }
        int size = 0;
        for (int i = 0; i < xzqTreeList.size(); i++) {
            XZQTree xzqTree = xzqTreeList.get(i);
        //if (xzqTrees.get(i).getXZQNum() == 0)
            {
                final String mXZQName = xzqTree.getXZQName();
                final int mXZQNum = xzqTreeList.get(i).getXZQNum();
                JButton now = new JButton(mXZQName);
                switch (mXZQNum){
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        now.setBackground(Color.GRAY);
                        break;
                }
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        switch (mXZQNum){
                            case 0:
                                ShowXZQTreeWindow();
                                break;
                            case 5:
                                btn_AreaMessure.setVisible(true);
                                btn_DistanceMessure.setVisible(true);
                                btn_autoTrans.setVisible(true);
                                LoadMap(mXZQName);
                                break;
                            default:
                                if (mXZQName.equals(XZQName)) {
                                    for (int i = 0; i < xzqTrees.size(); i++) {
                                        if (xzqTrees.get(i).getXZQName().equals(mXZQName))
                                            ShowXZQTreeWindow(xzqTrees.get(i).getLastXZQName(), mXZQNum-1);
                                    }
                                }
                                else
                                    ShowXZQTreeWindow(mXZQName, mXZQNum);
                                break;
                        }
                    }
                };
                now.addActionListener(listener);
                now.setPreferredSize(new Dimension(200, 50));
                XZQTreePanel.add(now);
                size++;
            }
        }
        AdjustAndShowXZQTreeWindow(size);
    }

    public void LoadMap(String MapName){
        for (int j = 0; j < maps.size(); j++) {
            Map map = maps.get(j);
            if (map.getName().equals(MapName))
            {
                view.load(map.getPath(), map.getMapGeoInfo(), map.getName());
                view.RefreshDimension();
                view.setZoomFactor(1);
                System.out.println(view.getZoomFactor());
                break;
            }
        }
    }

    /*
    显示行政区划树窗体，用于初次显示
     */
    private void ShowXZQTreeWindow(){
        InitXZQTreeWindow();
        List<XZQTree> xzqTreeList = new ArrayList<>();

        for (int i = 0; i < xzqTrees.size(); i++) {
            if (xzqTrees.get(i).getXZQNum() == 0)
                xzqTreeList.add(xzqTrees.get(i));
        }
        int size = 0;
        for (int i = 0; i < xzqTreeList.size(); i++) {
            //if (xzqTrees.get(i).getXZQNum() == 0)
            {
                final String mXZQName = xzqTreeList.get(i).getXZQName();
                final int XZQNum = xzqTreeList.get(i).getXZQNum();
                JButton now = new JButton(mXZQName);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        ShowXZQTreeWindow(mXZQName, XZQNum);
                    }
                };
                now.addActionListener(listener);
                now.setPreferredSize(new Dimension(200, 50));
                XZQTreePanel.add(now);
                size++;
            }
        }
        AdjustAndShowXZQTreeWindow(size);
    }

    /*
    显示其他类型地图的窗体，用于初次显示
     */
    private void ShowOtherTypeMapWindowForLC(){
        InitOtherTypeMapWindow();
        int size = 0;

        List<String> list = new ArrayList<>();

        list.add("临沧概览");
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getParentNode().equals("临沧概览"))
            {
                list.add(maps.get(i).getName());
            }
        }
        list.add("社会与经济");
        list.add("资源与环境");

        for (int i = 0; i < list.size(); i++) {
            final String btName = list.get(i);
            JButton now = new JButton(btName);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (btName.equals("社会与经济"))
                        ShowOtherTypeMapWindowForSociety();
                    else if (btName.equals("资源与环境"))
                        ShowOtherTypeMapWindowForEarth();
                    else if (btName.equals("临沧概览"))
                        ShowOtherTypeMapWindow();
                    else {
                        if (btName.contains("行政区划") || btName.contains("旅游交通")){
                            btn_AreaMessure.setVisible(true);
                            btn_DistanceMessure.setVisible(true);
                            btn_autoTrans.setVisible(true);
                            LoadMap(btName);
                        }else {
                            btn_AreaMessure.setVisible(false);
                            btn_DistanceMessure.setVisible(false);
                            btn_autoTrans.setVisible(false);
                            LoadMap(btName);
                            view.setThematicMap(true);
                        }
                    }
                }
            };
            if (!btName.equals("社会与经济") && !btName.equals("资源与环境") && !btName.equals("临沧概览"))
                now.setBackground(Color.GRAY);
            now.addActionListener(listener);
            now.setPreferredSize(new Dimension(200, 50));
            OtherTypeMapPanel.add(now);
            size++;
        }
        AdjustAndShowOtherTypeMapWindow(size);
    }

    /*
    显示其他类型地图的窗体，用于初次显示
     */
    private void ShowOtherTypeMapWindowForEarth(){
        InitOtherTypeMapWindow();
        int size = 0;

        List<String> list = new ArrayList<>();

        list.add("临沧概览");
        list.add("社会与经济");
        list.add("资源与环境");
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getParentNode().equals("资源与环境"))
            {
                list.add(maps.get(i).getName());
            }
        }

        for (int i = 0; i < list.size(); i++) {
            final String btName = list.get(i);
            JButton now = new JButton(btName);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (btName.equals("社会与经济"))
                        ShowOtherTypeMapWindowForSociety();
                    else if (btName.equals("资源与环境"))
                        ShowOtherTypeMapWindow();
                    else if (btName.equals("临沧概览"))
                        ShowOtherTypeMapWindowForLC();
                    else {
                        btn_AreaMessure.setVisible(false);
                        btn_DistanceMessure.setVisible(false);
                        btn_autoTrans.setVisible(false);
                        LoadMap(btName);
                        view.setThematicMap(true);
                    }
                }
            };
            if (!btName.equals("社会与经济") && !btName.equals("资源与环境") && !btName.equals("临沧概览"))
                now.setBackground(Color.GRAY);
            now.addActionListener(listener);
            now.setPreferredSize(new Dimension(200, 50));
            OtherTypeMapPanel.add(now);
            size++;
        }
        AdjustAndShowOtherTypeMapWindow(size);
    }

    /*
    显示其他类型地图的窗体，用于初次显示
     */
    private void ShowOtherTypeMapWindowForSociety(){
        InitOtherTypeMapWindow();
        int size = 0;

        List<String> list = new ArrayList<>();

        list.add("临沧概览");
        list.add("社会与经济");
        for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getParentNode().equals("社会与经济"))
            {
                list.add(maps.get(i).getName());
            }
        }
        list.add("资源与环境");

        for (int i = 0; i < list.size(); i++) {
            final String btName = list.get(i);
            JButton now = new JButton(btName);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (btName.equals("社会与经济"))
                        ShowOtherTypeMapWindow();
                    else if (btName.equals("资源与环境"))
                        ShowOtherTypeMapWindowForEarth();
                    else if (btName.equals("临沧概览"))
                        ShowOtherTypeMapWindowForLC();
                    else {
                        btn_AreaMessure.setVisible(false);
                        btn_DistanceMessure.setVisible(false);
                        btn_autoTrans.setVisible(false);
                        LoadMap(btName);
                        view.setThematicMap(true);
                    }
                }
            };
            if (!btName.equals("社会与经济") && !btName.equals("资源与环境") && !btName.equals("临沧概览"))
                now.setBackground(Color.GRAY);
            now.addActionListener(listener);
            now.setPreferredSize(new Dimension(200, 50));
            OtherTypeMapPanel.add(now);
            size++;
        }
        AdjustAndShowOtherTypeMapWindow(size);
    }

    /*
    显示其他类型地图的窗体，用于初次显示
     */
    private void ShowOtherTypeMapWindow(){
        InitOtherTypeMapWindow();
        int size = 0;

        List<String> list = new ArrayList<>();

        list.add("临沧概览");
        list.add("社会与经济");
        list.add("资源与环境");

        /*for (int i = 0; i < maps.size(); i++) {
            if (maps.get(i).getMapGeoInfo() != null)
            {
                final String btName = maps.get(i).getName();
                JButton now = new JButton(btName);
                ActionListener listener = new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                    }
                };
                now.addActionListener(listener);
                now.setPreferredSize(new Dimension(200, 50));
                OtherTypeMapPanel.add(now);
                size++;
            }
        }*/
        for (int i = 0; i < list.size(); i++) {
            final String btName = list.get(i);
            JButton now = new JButton(btName);
            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (btName.equals("社会与经济"))
                        ShowOtherTypeMapWindowForSociety();
                    else if (btName.equals("资源与环境"))
                        ShowOtherTypeMapWindowForEarth();
                    else if (btName.equals("临沧概览"))
                        ShowOtherTypeMapWindowForLC();
                }
            };
            now.addActionListener(listener);
            now.setPreferredSize(new Dimension(200, 50));
            OtherTypeMapPanel.add(now);
            size++;
        }
        AdjustAndShowOtherTypeMapWindow(size);
    }

    private void createToolBarButtonsForNewVersion(MyPicViewer mypdfviewer) {
        btn_winTopButton = new ToolbarButton("窗口置顶", "icons/icon_top.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(!isAlwaysOnTop());
                if(isAlwaysOnTop()){
                    btn_winTopButton.setText("取消置顶");
                }
                else{
                    btn_winTopButton .setText("窗口置顶");
                }
            }
        });
        btn_autoTrans = new ToolbarButton("开始自动切图", "icons/readytrans透明.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(view.getAutoTrans()){
                    btn_autoTrans.setText("开始自动切图");
                    btn_autoTrans.setIcon(new ImageIcon(MyPicViewer.this. getClass().getClassLoader().getResource("icons/readytrans透明.png")));
                    view.setAutoTrans(false);
                }
                else{
                    btn_autoTrans.setText("停止自动切图");
                    btn_autoTrans.setIcon(new ImageIcon(MyPicViewer.this. getClass().getClassLoader().getResource("icons/intrans透明.png")));
                    view.setAutoTrans(true);
                }
            }
        });
        btn_XZQTree = new ToolbarButton("行政区划树", "icons/document-open.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (PopWindow.isVisible())
                {
                    RemovePopWindow();
                }
                if (CurrentCatalogueType == MapCatalogueType.XZQTREE)
                    ShowPopWindow();
                else
                    ShowXZQTreeWindow();
                CurrentCatalogueType = MapCatalogueType.XZQTREE;
            }
        });
        btn_OtherTypeMap = new ToolbarButton("其他地图", "icons/document-open.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (PopWindow.isVisible())
                {
                    RemovePopWindow();
                }
                if (CurrentCatalogueType == MapCatalogueType.OTHERTYPEMAP)
                    ShowPopWindow();
                else
                    ShowOtherTypeMapWindow();
                CurrentCatalogueType = MapCatalogueType.OTHERTYPEMAP;
            }
        });
        btn_DistanceMessure = new ToolbarButton("距离量测", "icons/量测.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view.repaint();
                if (view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.NONE || view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.AREA)
                {
                    view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.DISTANCE);
                }
                else view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.NONE);
            }
        });
        btn_AreaMessure = new ToolbarButton("面积量测", "icons/量测.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view.repaint();
                if (view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.NONE || view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.DISTANCE)
                {
                    view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.AREA);
                }
                else view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.NONE);
            }
        });
        btn_LastPage = new ToolbarButton("上一页", "icons/go-previous.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < maps.size(); i++) {
                    Map m = maps.get(i);
                    if (view.CurrentMapTitle.equals(m.getName()))
                    {
                        i--;
                        while(i >= 0){
                            if (maps.get(i).getPage() != 0)
                            {
                                LoadMap(maps.get(i).getName());
                                break;
                            }
                            i--;
                        }
                        break;
                    }
                }
            }
        });
        btn_NextPage = new ToolbarButton("下一页", "icons/go-next.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < maps.size(); i++) {
                    Map m = maps.get(i);
                    if (view.CurrentMapTitle.equals(m.getName()))
                    {
                        i++;
                        while(i < maps.size()){
                            if (maps.get(i).getPage() != 0)
                            {
                                LoadMap(maps.get(i).getName());
                                break;
                            }
                            i++;
                        }
                        break;
                    }
                }
            }
        });

        btn_Search = new ToolbarButton("搜索", "icons/search透明.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                OpenOldVersionElectricAtlasForMidVersion(mypdfviewer);

            }
        });

        addToolBarComponents(btn_winTopButton);
        addToolBarComponents(btn_LastPage);
        addToolBarComponents(btn_NextPage);
        addToolBarComponents(btn_autoTrans);
        addToolBarComponents(btn_XZQTree);
        addToolBarComponents(btn_OtherTypeMap);
        addToolBarComponents(btn_DistanceMessure);
        addToolBarComponents(btn_AreaMessure);
        addToolBarComponents(btn_Search);
    }

    private void createToolBarButtons() {
        btn_winTopButton = new ToolbarButton("窗口置顶", "icons/icon_top.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(!isAlwaysOnTop());
                if(isAlwaysOnTop()){
                    btn_winTopButton.setText("取消置顶");
                }
                else{
                    btn_winTopButton .setText("窗口置顶");
                }
            }
        });
        btn_autoTrans = new ToolbarButton("开始自动切图", "icons/readytrans透明.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(view.getAutoTrans()){
                    btn_autoTrans.setText("开始自动切图");
                    btn_autoTrans.setIcon(new ImageIcon(MyPicViewer.this. getClass().getClassLoader().getResource("icons/readytrans透明.png")));
                    view.setAutoTrans(false);
                }
                else{
                    btn_autoTrans.setText("停止自动切图");
                    btn_autoTrans.setIcon(new ImageIcon(MyPicViewer.this. getClass().getClassLoader().getResource("icons/intrans透明.png")));
                    view.setAutoTrans(true);
                }
            }
        });
        btn_XZQTree = new ToolbarButton("行政区划树", "icons/document-open.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (PopWindow.isVisible())
                {
                    RemovePopWindow();
                }
                if (CurrentCatalogueType == MapCatalogueType.XZQTREE)
                    ShowPopWindow();
                else
                    ShowXZQTreeWindow();
                CurrentCatalogueType = MapCatalogueType.XZQTREE;
            }
        });
        btn_OtherTypeMap = new ToolbarButton("其他地图", "icons/document-open.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (PopWindow.isVisible())
                {
                    RemovePopWindow();
                }
                if (CurrentCatalogueType == MapCatalogueType.OTHERTYPEMAP)
                    ShowPopWindow();
                else
                    ShowOtherTypeMapWindow();
                CurrentCatalogueType = MapCatalogueType.OTHERTYPEMAP;
            }
        });
        btn_DistanceMessure = new ToolbarButton("距离量测", "icons/量测.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view.repaint();
                if (view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.NONE || view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.AREA)
                {
                    view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.DISTANCE);
                }
                else view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.NONE);
            }
        });
        btn_AreaMessure = new ToolbarButton("面积量测", "icons/量测.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                view.repaint();
                if (view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.NONE || view.getCurrentMessureType() == ZoomablePicture.MESSURE_TYPE.DISTANCE)
                {
                    view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.AREA);
                }
                else view.setCurrentMessureType(ZoomablePicture.MESSURE_TYPE.NONE);
            }
        });
        btn_LastPage = new ToolbarButton("上一页", "icons/go-previous.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < maps.size(); i++) {
                    Map m = maps.get(i);
                    if (view.CurrentMapTitle.equals(m.getName()))
                    {
                        i--;
                        while(i >= 0){
                            if (maps.get(i).getPage() != 0)
                            {
                                LoadMap(maps.get(i).getName());
                                break;
                            }
                            i--;
                        }
                        break;
                    }
                }
            }
        });
        btn_NextPage = new ToolbarButton("下一页", "icons/go-next.png", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < maps.size(); i++) {
                    Map m = maps.get(i);
                    if (view.CurrentMapTitle.equals(m.getName()))
                    {
                        i++;
                        while(i < maps.size()){
                            if (maps.get(i).getPage() != 0)
                            {
                                LoadMap(maps.get(i).getName());
                                break;
                            }
                            i++;
                        }
                        break;
                    }
                }
            }
        });

        addToolBarComponents(btn_winTopButton);
        addToolBarComponents(btn_LastPage);
        addToolBarComponents(btn_NextPage);
        addToolBarComponents(btn_autoTrans);
        addToolBarComponents(btn_XZQTree);
        addToolBarComponents(btn_OtherTypeMap);
        addToolBarComponents(btn_DistanceMessure);
        addToolBarComponents(btn_AreaMessure);
    }

    private void showCurrentPicture(String path, PointF[] pts, List<Map> maps, String title) {
        String filename = path;
        System.out.println(filename);
        /*max_lat = 0;
        max_long = 0;
        min_lat = Float.MAX_VALUE;
        min_long = Float.MAX_VALUE;
        for (int i = 0; i < pts.length; i++) {
            float lat = pts[i].getLat();
            float longi = pts[i].getLong();
            if (lat > max_lat)
            max_lat = lat;
            if (longi > max_long)
            max_long = longi;
            if (lat < min_lat)
            min_lat = lat;
            if (longi < min_long)
            min_long = longi;
            System.out.println(i + ": " + pts[i].ShowPt());
        }
        ShowMapRect(max_lat, max_long, min_lat, min_long);*/
        this.view.load(filename, pts, maps, this, title);
        //max_lat, min_lat, max_long, min_long
        //setStatus(String.format("[%d/%d] %s", i + 1, this.pictureList.length, filename));
        //setToolBarComponentsEnabled(true, i >= 0, i >= 0, i > 0, i + 1 < this.pictureList.length, true);
    }

    private void ShowMapRect(double max_lat, double max_long, double min_lat, double min_long){
        System.out.println("最大经度为： " + max_long + "最大纬度为： " + max_lat + "最小经度为： " + min_long + "最小纬度为： " + min_lat);
    }

    private void ShowCurrentRect() {

    }
}

