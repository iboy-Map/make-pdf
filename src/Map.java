import javafx.geometry.Point2D;

import java.awt.*;
import java.util.List;

public class Map implements Comparable<Map>{
    private String parentNode;
    private String name;
    private int mapType;
    private String path;
    private PointF[] MapGeoInfo = new PointF[4];
    private int page;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public int compareTo(Map o) {
        return this.page - o.getPage();
    }
//private List<Point2D> FramePts;

    /*public Map(String parentNode, String name, int mapType, String path) {
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
    }*/

    /*public List<Point2D> getFramePts() {
        return FramePts;
    }

    public void setFramePts(List<Point2D> framePts) {
        FramePts = framePts;
    }*/

    /*public Map(String parentNode, String name, int mapType, String path, List<Point2D> framePts) {
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
        FramePts = framePts;
    }*/

    public Map(String parentNode, String name, int mapType, String path, PointF[] MapGeoInfo, int page){
        this.parentNode = parentNode;
        this.name = name;
        this.mapType = mapType;
        this.path = path;
        this.MapGeoInfo = MapGeoInfo;
        this.page = page;
    }

    public PointF[] getMapGeoInfo() {
        return MapGeoInfo;
    }

    public void ShowMapRect(){
        float max_lat = 0;
        float max_long = 0;
        float min_lat = Float.MAX_VALUE;
        float min_long = Float.MAX_VALUE;
        for (int i = 0; i < MapGeoInfo.length; i++) {
            float lat = MapGeoInfo[i].getLat();
            float longi = MapGeoInfo[i].getLong();
            if (lat > max_lat)
                max_lat = lat;
            if (longi > max_long)
                max_long = longi;
            if (lat < min_lat)
                min_lat = lat;
            if (longi < min_long)
                min_long = longi;
        }
        System.out.println("最大经度： " + max_long + "\n" + "最大纬度： " + max_lat + "\n" + "最小经度： " + min_long + "\n" + "最小纬度： " + min_lat);
    }

    public void setMapGeoInfo(PointF[] mapGeoInfo) {
        MapGeoInfo = mapGeoInfo;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getParentNode() {
        return parentNode;
    }

    public void setParentNode(String parentNode) {
        this.parentNode = parentNode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }
}
