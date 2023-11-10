import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;

import java.awt.geom.Point2D;
import java.io.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class folkway_data_fire extends PDFGraphicsStreamEngine {

    /**
     * Constructor.
     *
     * @param page PDF Page
     */
    protected folkway_data_fire(PDPage page)
    {
        super(page);
    }

    public static void main(String[] args) throws IOException
    {
        System.out.println("开始处理-----------------");

        String FolderPath = "D:\\云南省地图院\\";
        //临沧市电子地图集
        //数据库路径
        String BasePath = FolderPath + "民间信仰地图集测试数据库";
        //输出的路径
        String AndroidNewPath = FolderPath + "民间信仰地图集";

        try{
            File NewFolder = new File(AndroidNewPath);
            if (!NewFolder.exists())
                NewFolder.mkdirs();
            CreateMapCollectionForAndroid(BasePath, AndroidNewPath, FolderPath);
        }
        catch (Exception e){
            System.out.println("地图集文件夹批量处理过程中出错，具体原因如下： " + "\n" + e.toString());
        }
    }

    private static void CreateMapCollectionForAndroid(String BasePath, String NewPath, String FolderPath){
        try {
            int MapTypeCount = 0;
            String GeoInfoPath = NewPath + "\\民间信仰dataForAndroid.txt";

            File file = new File(GeoInfoPath);
            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(GeoInfoPath));
            //获取其file对象
            File mfile = new File(BasePath);
            ParseFilesForAndoird(mfile, "", bw, MapTypeCount, NewPath, FolderPath);
            bw.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private static boolean IsGeoInfoPDF(String name)
    {
        if (name.contains("191-澜沧县"))
            return true;
        else if (name.contains("17-河口县") || name.contains("12-金平县"))
            return true;
        else
            return false;
    }

    private static void ParseFilesForAndoird(File file, String FolderName, BufferedWriter bw, int MapTypeCount, String NewPath, String FolderPath){
        try {
            //System.out.println(FolderName);
            File NewConcisePath = new File(NewPath + "\\" + FolderName);
            NewConcisePath.mkdirs();
            String ParentNodeName = FolderName.substring(FolderName.lastIndexOf("\\") + 1);
            int count = 1;
            File[] fs = file.listFiles();
            //遍历File[]数组
            for(File f : fs) {
                String FileName = f.toString();
                if (!f.isDirectory() && f.toString().contains(".pdf")) {

                    if (IsGeoInfoPDF(FileName))
                        SavePDFWithGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
                    else
                        SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);

//                    if (!FolderName.contains("民间信仰总览")){
//                        if (!FileName.contains( "简介"))
//                            SavePDFWithGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
//                        else
//                            SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
//                    }
//                    else {
//                        if (!FileName.contains( "简介"))
//                            SavePDFWithGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
//                        else
//                            SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
//                    }
                    //SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
                }
                else{
                    bw.write(ParentNodeName + "," + FileName.substring(FileName.lastIndexOf("\\") + 1) + "," + GetMapType(ParentNodeName) + "," + " ");
                    bw.newLine();
                    String DirectoryName = f.toString();

                    if (MapTypeCount == 0)
                    {
                        ParseFilesForAndoird(f, f.getName(), bw, MapTypeCount + count++, NewPath, FolderPath);
                    }
                    else
                    {
                        ParseFilesForAndoird(f, FolderName + DirectoryName.substring(DirectoryName.lastIndexOf("\\")), bw, MapTypeCount + count++, NewPath, FolderPath);
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    private static void SavePDFWithoutGeoInfoForAndroid(String FolderName, String FileName, String NewPath, String ParentNodeName, File f, BufferedWriter bw, String FolderPath){
        try {
            System.out.println(FileName);
            PDDocument doc = PDDocument.load(f);

            String dtName = FileName.substring(FileName.indexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".dt";
            File NewFile = new File(NewPath + "\\" + dtName);
            Files.copy(f.toPath(), NewFile.toPath());

            doc.close();

            bw.write(ParentNodeName + "," + dtName.substring(dtName.lastIndexOf("\\") + 1, dtName.lastIndexOf(".dt")) + "," + GetMapType(ParentNodeName) + "," + NewPath.replace(FolderPath, "") + "\\" + dtName + ", ");

            bw.newLine();
        }catch (Exception e){
            System.out.println("在处理不带有空间信息的PDF文件过程中出错" + "\n" + e.toString());
        }
    }

    private static void SavePDFWithGeoInfoForAndroid(String FolderName, String FileName, String NewPath, String ParentNodeName, File f, BufferedWriter bw, String FolderPath){
        try {
            System.out.println("py_" + FileName);
            PDDocument doc = PDDocument.load(f);
            String GeoInfo[] = GetGeoInfoFromPDFFile(doc);
            //String pngName = FileName.substring(FileName.lastIndexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".png";

            System.out.println("FileName: " + FileName);
            System.out.println("FolderName: " + FolderName);
            String dtName = FileName.substring(FileName.indexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".dt";
            System.out.println("dtName: " + dtName);
            File NewFile = new File(NewPath + "\\" + dtName);
            Files.copy(f.toPath(), NewFile.toPath());

            doc.close();
            //bw.write(ParentNodeName + "," + PngName + "," + MapType + "," + PngPath);
            if (GeoInfo.length <= 1) {
                //bw.write(ParentNodeName + "," + PngName + "," + MapTypeCount + "," + PngPath + ", " + ", " + ", " + ", ");
                bw.write(ParentNodeName + "," + dtName.substring(dtName.lastIndexOf("\\") + 1, dtName.lastIndexOf(".dt")) + "," + GetMapType(ParentNodeName) + "," + NewPath.replace(FolderPath, "") + "\\" + dtName + ", " + ", " + ", " + ", ");
            } else {
                //bw.write(ParentNodeName + "," + PngName + "," + MapTypeCount + "," + PngPath + "," + GeoInfo[0] + "," + GeoInfo[1] + "," + GeoInfo[2] + "," + GeoInfo[3]);
                bw.write(ParentNodeName + "," + dtName.substring(dtName.lastIndexOf("\\") + 1, dtName.lastIndexOf(".dt")) + "," + GetMapType(ParentNodeName) + "," + NewPath.replace(FolderPath, "") + "\\" + dtName + "," + GeoInfo[0] + "," + GeoInfo[1] + "," + GeoInfo[2] + "," + GeoInfo[3]);
            }
            bw.newLine();
        }catch (Exception e){
            System.out.println("在处理带有空间信息的PDF文件过程中出错" + "\n" + e.toString());
            SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
        }
    }

    private static String GetMapType(String ParentNodeName){
        switch (ParentNodeName){
            case "":
                return "0";
            case "民间信仰总览":
                return "1";
            case "中越边境云南段":
                return "2";
            case "中越边境广西段":
                return "3";
            case "中老边境":
                return "4";
            case "中缅边境云南、西藏段":
                return "5";
            case "红河州":
                return "6";
            case "文山州":
                return "7";
            case "百色市":
                return "8";
            case "崇左市":
                return "9";
            case "中老西双版纳州":
                return "10";
            case "中老普洱市":
                return "11";
            case "怒江州":
                return "12";
            case "保山市":
                return "13";
            case "德宏州":
                return "14";
            case "临沧市":
                return "15";
            case "中缅普洱市":
                return "16";
            case "中缅西双版纳州":
                return "17";
            case "林芝市":
                return "18";
        }
        return "14"; //县的
    }

    private static String[] GetGeoInfoFromPDFFile(PDDocument doc){
        String GeoInfo = "";
        String BBoxInfo = "";
        String MediaBoxInfo = "";
        String LptsInfo = "";

        //File file = new File(FilePath);
        try {
            //PDDocument doc = PDDocument.load(file);



            PDDocumentInformation info = doc.getDocumentInformation();
            List<PDAnnotation> list = doc.getPage(0).getAnnotations();
            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i).getAnnotationName());
            }

            COSDictionary d = doc.getPage(0).getCOSObject();
            Iterator ss = d.entrySet().iterator();

            while (ss.hasNext())
            {
                Map.Entry<COSName, COSBase> entry = (Map.Entry<COSName, COSBase>)ss.next();

                //System.out.println("属性" + count1++ +  ": " + entry.getKey().getName() + ", " + entry.getValue());

                String Name0 = entry.getKey().getName();
                if (entry.getValue() instanceof COSArray)
                {
                    //System.out.println(entry.getValue().getCOSObject());
                    COSArray array = (COSArray)entry.getValue();
                    for (int i = 0; i < array.size(); i++) {
                        if (Name0.equals("MediaBox")){
                            if (MediaBoxInfo.length() == 0){
                                if (array.get(i) instanceof COSFloat)
                                {
                                    COSFloat f = (COSFloat)array.get(i);
                                    MediaBoxInfo += "" + f.floatValue();
                                }
                                else if (array.get(i) instanceof COSInteger){
                                    COSInteger f = (COSInteger)array.get(i);
                                    MediaBoxInfo += "" + f.intValue();
                                }
                            }
                            else
                            {
                                if (array.get(i) instanceof COSFloat)
                                {
                                    COSFloat f = (COSFloat)array.get(i);
                                    MediaBoxInfo += " " + f.floatValue();
                                }
                                else if (array.get(i) instanceof COSInteger){
                                    COSInteger f = (COSInteger)array.get(i);
                                    MediaBoxInfo += " " + f.intValue();
                                }
                            }
                        }
                        else {
                            System.out.println(array.get(i).getCOSObject());
                            if (array.get(i).getCOSObject() instanceof COSDictionary) {
                                COSDictionary dictionary = (COSDictionary) array.get(i).getCOSObject();
                                Iterator vpDict = dictionary.entrySet().iterator();
                                int count = 0;
                                while (vpDict.hasNext()) {
                                    Map.Entry<COSName, COSBase> vpEntry = (Map.Entry<COSName, COSBase>) vpDict.next();
                                    //System.out.println("属性" + count++ +  ": " + vpEntry.getKey().getName() + ", " + vpEntry.getValue());
                                    String Name = vpEntry.getKey().getName();
                                    if (Name.equals("Measure")) {
                                        COSObject object = (COSObject) vpEntry.getValue().getCOSObject();

                                        COSArray GPTSArray = (COSArray) object.getItem(COSName.getPDFName("GPTS")).getCOSObject();

                                        for (int j = 0; j < GPTSArray.size(); j++) {
                                            COSFloat f = (COSFloat) GPTSArray.get(j).getCOSObject();
                                            if (j % 2 == 1)
                                            {
                                                if (j != GPTSArray.size()-1)
                                                    GeoInfo += f.floatValue() + " ";
                                                else
                                                    GeoInfo += f.floatValue() + "";
                                            }
                                            else
                                                GeoInfo += f.floatValue() + " ";
                                            //System.out.println("GPTS " + j + ": " + f.floatValue());
                                        }

                                        COSArray LPTSArray = (COSArray) object.getItem(COSName.getPDFName("LPTS")).getCOSObject();

                                        for (int j = 0; j < LPTSArray.size(); j++) {
                                            if (LPTSArray.get(j) instanceof COSInteger) {
                                                COSInteger cosInteger = (COSInteger) LPTSArray.get(j);

                                                if (j != LPTSArray.size() - 1)
                                                    LptsInfo += cosInteger.intValue() + " ";
                                                else
                                                    LptsInfo += cosInteger.intValue() + "";
                                            }
                                            else if (LPTSArray.get(j) instanceof COSFloat){

                                                COSFloat cosFloat = (COSFloat) LPTSArray.get(j);

                                                if (j != LPTSArray.size() - 1)
                                                    LptsInfo += cosFloat.floatValue() + " ";
                                                else
                                                    LptsInfo += cosFloat.floatValue() + "";
                                            }
                                            //System.out.println("GPTS " + j + ": " + f.floatValue());
                                        }
                                        //System.out.println(LptsInfo);
                                    }
                                    else if (Name.equals("BBox")){
                                        COSArray BBoxArray = (COSArray) vpEntry.getValue().getCOSObject();

                                        for (int j = 0; j < BBoxArray.size(); j++) {
                                            COSFloat f = (COSFloat) BBoxArray.get(j).getCOSObject();
                                            /*if (j % 2 == 1)
                                                BBoxInfo += f.floatValue() + ";";
                                            else
                                                BBoxInfo += f.floatValue() + "&";*/
                                            if (j != BBoxArray.size()-1)
                                                BBoxInfo += f.floatValue() + " ";
                                            else
                                                BBoxInfo += f.floatValue() + "";

                                            //System.out.println("GPTS " + j + ": " + f.floatValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //System.out.println(entry.getValue().getCOSObject());
            }
            /*Iterator<String> i = info.getMetadataKeys().iterator();
            while (i.hasNext())
            {
                String str = i.next();
                System.out.println(str);
                System.out.println(info.getPropertyStringValue(str));
            }

            System.out.println("标题:" + info.getTitle());
            System.out.println("主题:" + info.getSubject());
            System.out.println("作者:" + info.getAuthor());
            System.out.println("关键字:" + info.getKeywords());

            System.out.println("应用程序:" + info.getCreator());
            System.out.println("pdf 制作程序:" + info.getProducer());

            System.out.println("作者:" + info.getTrapped());

            System.out.println("创建时间:" + dateFormat(info.getCreationDate()));
            System.out.println("修改时间:" + dateFormat(info.getModificationDate()));
            //PDFieldTree tree = new PDFieldTree()
            PDFTextStripper stripper = new PDFTextStripper();
            String content = stripper.getText(doc);
            System.out.println(content);*/
            //PDFRenderer renderer = new PDFRenderer(doc);
            //BufferedImage bufferedImage = renderer.renderImage(0);


            /*ImageIO.write(bufferedImage, "png", new File("D:\\test.png"));
            JFrame frame = new ImageViewerFrame(new File("D:\\test.png"), bufferedImage.getWidth(), bufferedImage.getHeight());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);*/
        /*PDPage page = doc.getPage(0);
        CustomGraphicsStreamEngine engine = new CustomGraphicsStreamEngine(page);
        engine.run();*/
            //doc.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
        String[] strings = new String[4];
        strings[0] = GeoInfo;
        strings[1] = MediaBoxInfo;
        strings[2] = BBoxInfo;
        strings[3] = LptsInfo;

        System.out.println("原始坐标： " + GeoInfo);
        GeoInfo = DataUtil.getGPTS(GeoInfo, LptsInfo);
        System.out.println("坐标纠偏后： " + GeoInfo);
        GeoInfo = DataUtil.rubberCoordinate(MediaBoxInfo, BBoxInfo, GeoInfo);
        System.out.println("坐标拉伸后： " + GeoInfo);

        //return GeoInfo;
        //return MediaBoxInfo;
        return strings;
    }

    @Override
    public void appendRectangle(Point2D point2D, Point2D point2D1, Point2D point2D2, Point2D point2D3) throws IOException {

    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException {

    }

    @Override
    public void clip(int i) throws IOException {

    }

    @Override
    public void moveTo(float v, float v1) throws IOException {

    }

    @Override
    public void lineTo(float v, float v1) throws IOException {

    }

    @Override
    public void curveTo(float v, float v1, float v2, float v3, float v4, float v5) throws IOException {

    }

    @Override
    public Point2D getCurrentPoint() throws IOException {
        return null;
    }

    @Override
    public void closePath() throws IOException {

    }

    @Override
    public void endPath() throws IOException {

    }

    @Override
    public void strokePath() throws IOException {

    }

    @Override
    public void fillPath(int i) throws IOException {

    }

    @Override
    public void fillAndStrokePath(int i) throws IOException {

    }

    @Override
    public void shadingFill(COSName cosName) throws IOException {

    }
}
