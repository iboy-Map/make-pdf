import java.text.DecimalFormat;

public class DataUtil {
    //坐标拉伸算法
    public static String rubberCoordinate(String MediaBox, String BBox, String GPTS) {
        String[] MediaBoxString = MediaBox.split(" ");
        String[] BBoxString = BBox.split(" ");
        String[] GPTSString = GPTS.split(" ");
        //将String 数组转换为 Float 数组
        float[] MediaBoxs = new float[MediaBoxString.length];
        float[] BBoxs = new float[BBoxString.length];
        float[] GPTSs = new float[GPTSString.length];
        for (int i = 0; i < MediaBoxString.length; ++i) {
            MediaBoxs[i] = Float.valueOf(MediaBoxString[i]);
            //locError("MediaBoxs : " + MediaBoxs[i]);
        }
        for (int i = 0; i < BBoxString.length; ++i) {
            BBoxs[i] = Float.valueOf(BBoxString[i]);
        }
        //优化BBOX拉伸算法
        float del;
        if (BBoxs[1] < BBoxs[3]) {
            del = BBoxs[1];
            BBoxs[1] = BBoxs[3];
            BBoxs[3] = del;
        }
        //
        for (int i = 0; i < GPTSString.length; ++i) {
            GPTSs[i] = Float.valueOf(GPTSString[i]);
        }

        if (Math.floor(MediaBoxs[2]) != Math.floor(BBoxs[2]) && Math.floor(MediaBoxs[3]) != Math.floor(BBoxs[3])) {
            PointF pt1_lb = new PointF(), pt1_lt = new PointF(), pt1_rt = new PointF(), pt1_rb = new PointF();
            PointF pt_lb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF(), pt_rb = new PointF();
            pt1_lb.x = BBoxs[0] / MediaBoxs[2];
            pt1_lb.y = BBoxs[3] / MediaBoxs[3];
            pt1_lt.x = BBoxs[0] / MediaBoxs[2];
            pt1_lt.y = BBoxs[1] / MediaBoxs[3];
            pt1_rt.x = BBoxs[2] / MediaBoxs[2];
            pt1_rt.y = BBoxs[1] / MediaBoxs[3];
            pt1_rb.x = BBoxs[2] / MediaBoxs[2];
            pt1_rb.y = BBoxs[3] / MediaBoxs[3];
            float lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            float long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
            for (int i = 0; i < GPTSs.length; i = i + 2) {
                if (GPTSs[i] < lat_axis) {
                    if (GPTSs[i + 1] < long_axis) {
                        pt_lb.x = GPTSs[i];
                        pt_lb.y = GPTSs[i + 1];
                    } else {
                        pt_rb.x = GPTSs[i];
                        pt_rb.y = GPTSs[i + 1];
                    }
                } else {
                    if (GPTSs[i + 1] < long_axis) {
                        pt_lt.x = GPTSs[i];
                        pt_lt.y = GPTSs[i + 1];
                    } else {
                        pt_rt.x = GPTSs[i];
                        pt_rt.y = GPTSs[i + 1];
                    }
                }
            }
            GPTS = Float.toString(pt1_lb.x) + " " + Float.toString(pt1_lb.y) + " " + Float.toString(pt1_lt.x) + " " + Float.toString(pt1_lt.y) + " " + Float.toString(pt1_rt.x) + " " + Float.toString(pt1_rt.y) + " " + Float.toString(pt1_rb.x) + " " + Float.toString(pt1_rb.y);
            float delta_lat = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2, delta_long = ((pt_rb.y - pt_lb.y) + (pt_rt.y - pt_lt.y)) / 2;
            float delta_width = pt1_rb.x - pt1_lb.x, delta_height = pt1_lt.y - pt1_lb.y;
            pt_lb.x = pt_lb.x - (delta_lat / delta_height * (pt1_lb.y - 0));
            pt_lb.y = pt_lb.y - (delta_long / delta_width * (pt1_lb.x - 0));
            pt_lt.x = pt_lt.x - (delta_lat / delta_height * (pt1_lt.y - 1));
            pt_lt.y = pt_lt.y - (delta_long / delta_width * (pt1_lt.x - 0));
            pt_rb.x = pt_rb.x - (delta_lat / delta_height * (pt1_rb.y - 0));
            pt_rb.y = pt_rb.y - (delta_long / delta_width * (pt1_rb.x - 1));
            pt_rt.x = pt_rt.x - (delta_lat / delta_height * (pt1_rt.y - 1));
            pt_rt.y = pt_rt.y - (delta_long / delta_width * (pt1_rt.x - 1));

            GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
        } else {
        }
        return GPTS;

    }

    //坐标飘移判断
    public static boolean isDrift(String LPTS){
        boolean isDrift = false;
        String[] LPTSStrings = LPTS.split(" ");
        //locError(Integer.toString(LPTSStrings.length));
        for (int i = 0; i < LPTSStrings.length; ++i){
            //locError(LPTSStrings[i]);
            if (Float.valueOf(LPTSStrings[i]) != 0 && Float.valueOf(LPTSStrings[i]) != 1){
                isDrift = true;
                break;
            }
        }
        return isDrift;
    }

    //获取GPTS值
    public static String getGPTS(String GPTS, String LPTS){
        //locError("看这里: " + " & LPTS " + LPTS);
        if (isDrift(LPTS) == true) {
            //locError("看这里: " + GPTS + " & LPTS " + LPTS);
            float lat_axis, long_axis;
            float lat_axis1, long_axis1;
            DecimalFormat df = new DecimalFormat("0.0");
            String[] GPTSStrings = GPTS.split(" ");
            String[] LPTSStrings = LPTS.split(" ");
            //将String 数组转换为 Float 数组
            float[] GPTSs = new float[GPTSStrings.length];
            float[] LPTSs = new float[LPTSStrings.length];
            for (int i = 0; i < LPTSStrings.length; ++i) {
                LPTSs[i] = Float.valueOf(LPTSStrings[i]);
            }
            for (int i = 0; i < GPTSStrings.length; ++i) {
                GPTSs[i] = Float.valueOf(GPTSStrings[i]);
            }
            //
            //构建两个矩形
            //构建经纬度矩形
            PointF pt_lb = new PointF(), pt_rb = new PointF(), pt_lt = new PointF(), pt_rt = new PointF();
            //PointF pt_lb1 = new PointF(), pt_rb1 = new PointF(), pt_lt1 = new PointF(), pt_rt1 = new PointF();
            lat_axis = (GPTSs[0] + GPTSs[2] + GPTSs[4] + GPTSs[6]) / 4;
            long_axis = (GPTSs[1] + GPTSs[3] + GPTSs[5] + GPTSs[7]) / 4;
            for (int i = 0; i < GPTSs.length; i = i + 2){
                if (GPTSs[i] < lat_axis) {
                    if (GPTSs[i + 1] < long_axis){
                        pt_lb.x = GPTSs[i];
                        pt_lb.y = GPTSs[i + 1];
                    } else {
                        pt_rb.x = GPTSs[i];
                        pt_rb.y = GPTSs[i + 1];
                    }
                } else {
                    if (GPTSs[i + 1] < long_axis){
                        pt_lt.x = GPTSs[i];
                        pt_lt.y = GPTSs[i + 1];
                    } else {
                        pt_rt.x = GPTSs[i];
                        pt_rt.y = GPTSs[i + 1];
                    }
                }
            }
            //构建LPTS 矩形
            //预处理LPTS
            for (int i = 0; i < LPTSs.length; ++i){
                LPTSs[i] = Float.valueOf(df.format(LPTSs[i]));
            }
            //
            PointF pt_lb1 = new PointF(), pt_rb1 = new PointF(), pt_lt1 = new PointF(), pt_rt1 = new PointF();
            lat_axis1 = (LPTSs[0] + LPTSs[2] + LPTSs[4] + LPTSs[6]) / 4;
            long_axis1 = (LPTSs[1] + LPTSs[3] + LPTSs[5] + LPTSs[7]) / 4;
            for (int i = 0; i < LPTSs.length; i = i + 2){
                if (LPTSs[i] < lat_axis1) {
                    if (LPTSs[i + 1] < long_axis1){
                        pt_lb1.x = LPTSs[i];
                        pt_lb1.y = LPTSs[i + 1];
                    } else {
                        pt_rb1.x = LPTSs[i];
                        pt_rb1.y = LPTSs[i + 1];
                    }
                } else {
                    if (LPTSs[i + 1] < long_axis1){
                        pt_lt1.x = LPTSs[i];
                        pt_lt1.y = LPTSs[i + 1];
                    } else {
                        pt_rt1.x = LPTSs[i];
                        pt_rt1.y = LPTSs[i + 1];
                    }
                }
            }
            float delta_lat = ((pt_lt.x - pt_lb.x) + (pt_rt.x - pt_rb.x)) / 2, delta_long = ((pt_rb.y - pt_lb.y) + (pt_rt.y - pt_lt.y)) / 2;
            float delta_width = pt_rb1.y - pt_lb1.y, delta_height = pt_lt1.x - pt_lb1.x;
            pt_lb.x = pt_lb.x - (delta_lat / delta_height * (pt_lb1.x - 0));
            pt_lb.y = pt_lb.y - (delta_long / delta_width * (pt_lb1.y - 0));
            pt_lt.x = pt_lt.x - (delta_lat / delta_height * (pt_lt1.x - 1));
            pt_lt.y = pt_lt.y - (delta_long / delta_width * (pt_lt1.y - 0));
            pt_rb.x = pt_rb.x - (delta_lat / delta_height * (pt_rb1.x - 0));
            pt_rb.y = pt_rb.y - (delta_long / delta_width * (pt_rb1.y - 1));
            pt_rt.x = pt_rt.x - (delta_lat / delta_height * (pt_rt1.x - 1));
            pt_rt.y = pt_rt.y - (delta_long / delta_width * (pt_rt1.y - 1));
            GPTS = Float.toString(pt_lb.x) + " " + Float.toString(pt_lb.y) + " " + Float.toString(pt_lt.x) + " " + Float.toString(pt_lt.y) + " " + Float.toString(pt_rt.x) + " " + Float.toString(pt_rt.y) + " " + Float.toString(pt_rb.x) + " " + Float.toString(pt_rb.y);
        }
        return GPTS;
    }

    //距离量测(输入参数为 两点的经纬度)
    public static double algorithm(double longitude1, double latitude1, double longitude2, double latitude2) {

        double Lat1 = rad(latitude1); // 纬度

        double Lat2 = rad(latitude2);

        double a = Lat1 - Lat2;//两点纬度之差

        double b = rad(longitude1) - rad(longitude2); //经度之差

        double s = 2 * Math.asin(Math

                .sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(Lat1) * Math.cos(Lat2) * Math.pow(Math.sin(b / 2), 2)));//计算两点距离的公式

        s = s * 6378137.0;//弧长乘地球半径（半径为米）

        s = Math.round(s * 10000d) / 10000d;//精确距离的数值

        return s;

    }

    //将角度转化为弧度
    private static double rad(double d) {
        return d * Math.PI / 180.00; //角度转换成弧度
    }

}
