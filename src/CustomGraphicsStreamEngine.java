/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldTree;
import org.apache.pdfbox.pdmodel.interactive.measurement.PDMeasureDictionary;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.PDFBox;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Example of a custom PDFGraphicsStreamEngine subclass. Allows text and graphics to be processed
 * in a custom manner. This example simply prints the operations to stdout.
 *
 * <p>See {@link PDFStreamEngine} for further methods which may be overridden.
 * 
 * @author John Hewson
 */
public class CustomGraphicsStreamEngine extends PDFGraphicsStreamEngine
{
    /**
     * Constructor.
     *
     * @param page PDF Page
     */
    protected CustomGraphicsStreamEngine(PDPage page)
    {
        super(page);
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static void main(String[] args) throws IOException
    {
        //ParseFileForGrade3("D:\\三级认定图、表附件\\峨山\\三级认定图表扫描");
        //InsertJpgToPDFFile("D:\\素材\\图片\\聊天气泡\\素材图\\IMG_20210108_083925.jpg");

        /*JFrame FindFileFrame = new JFrame();
        int result = 0;
        File file = null;
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        FileSystemView fsv = FileSystemView.getFileSystemView();
        //注意了，这里重要的一句
        System.out.println(fsv.getHomeDirectory());
        //得到桌面路径
        fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
        fileChooser.setDialogTitle("请选择要上传的文件...");
        fileChooser.setApproveButtonText("确定");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        result = fileChooser.showOpenDialog(FindFileFrame);
        if (JFileChooser.APPROVE_OPTION == result) {
            path=fileChooser.getSelectedFile().getPath();
            System.out.println("path: "+path);
        }*/

        String FolderPath = "E:\\";//"C:\\Users\\54286\\Desktop\\";
        //临沧市电子地图集
        //要遍历的路径
        String BasePath = FolderPath + "临沧市地图集批量入库文件夹";
        //要遍历的路径
        String AndroidNewPath = FolderPath + "临沧市地图集安卓";
        //要遍历的路径
        String NewPath = FolderPath + "临沧市地图集";


        /*String FolderPath = "E:\\";//"C:\\Users\\54286\\Desktop\\";
        // 云南省电子地图集
        //要遍历的路径
        String BasePath = FolderPath + "省厅展示电子地图集入库文件夹";
        //要遍历的路径
        String AndroidNewPath = FolderPath + "省厅展示电子地图集安卓";
        //要遍历的路径
        String NewPath = FolderPath + "省厅展示电子地图集";*/

        //ParseDTJFolderForAndroid(BasePath, AndroidNewPath, FolderPath);

        ParseDTJFolderForWindows(BasePath, NewPath);

        /*String geoinfo = GetGeoInfoFromPDFFile("D:\\test.pdf");
        System.out.println(geoinfo);*/
        /*File file = new File("C:\\Users\\54286\\Desktop\\临沧市地图集批量入库文件夹\\区域地理图组\\县图\\凤庆县（4k）.pdf");
        PDDocument doc = PDDocument.load(file);
        String[] geoinfo = GetGeoInfoFromPDFFile(doc);
        for (int i = 0; i < geoinfo.length; i++) {
            System.out.println(geoinfo[i]);
        }*/

    }

    private static enum PageSizeEnum{
        A0, A1, A2, A3, A4, NONE
    }

    private static int A4Width = 595;
    private static int A4Height = 842;
    private static int A3Width = 842;
    private static int A3Height = 1190;
    private static int A2Width = 1190;
    private static int A2Height = 1684;
    private static int A1Width = 1684;
    private static int A1Height = 2380;
    private static int A0Width = 2380;
    private static int A0Height = 3368;

    public static ByteArrayOutputStream resizeImage(InputStream is, OutputStream os, String format, int[] PageSize) throws IOException {
        BufferedImage prevImage = ImageIO.read(is);
        /*int newWidth = 595;
        int newHeight = 842;*/
        int newWidth = PageSize[0];
        int newHeight = PageSize[1];

        /*switch (PageSize){
            case A4:
                newWidth = 595;
                newHeight = 842;
                break;
            case A3:
                newWidth = 842;
                newHeight = 1190;
                break;
            case A2:
                newWidth = 1190;
                newHeight = 1684;
                break;
            case A1:
                newWidth = 1684;
                newHeight = 2380;
                break;
            case A0:
                newWidth = 2380;
                newHeight = 3368;
                break;
        }*/

        BufferedImage image = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_BGR);
        Graphics graphics = image.createGraphics();
        graphics.drawImage(prevImage, 0, 0, newWidth, newHeight, null);
        ImageIO.write(image, format, os);
        os.flush();
        is.close();
        os.close();
        ByteArrayOutputStream b = (ByteArrayOutputStream) os;
        return b;
    }

    private static int[] ChoosePageSize(String JpgFilePath){
        try{
            BufferedImage bi = ImageIO.read(new File(JpgFilePath));
            int width = bi.getWidth();
            int height = bi.getHeight();

            bi.flush();

            return new int[]{width, height};
            /*int maxSize = height;
            if (width >= height)
                maxSize = width;

            if (maxSize<=1190)
                return PageSizeEnum.A4;
            else if (maxSize<=1684)
                return PageSizeEnum.A3;
            else if (maxSize<=2380)
                return PageSizeEnum.A2;
            else if (maxSize<=3368)
                return PageSizeEnum.A1;
            else
                return PageSizeEnum.A0;*/

            /*if (height >= width){
                if (height<=1190)
                    return 842;
                else if (height<=1684)
                    return 1190;
                else if (height<=2380)
                    return 1684;
                else if (height<=3368)
                    return 2380;
                else
                    return 3368;
            }
            else{
                if (width<=1190)
                    return -842;
                else if (width<=1684)
                    return -1190;
                else if (width<=2380)
                    return -1684;
                else if (width<=3368)
                    return -2380;
                else
                    return -3368;
            }*/
        }catch (Exception e){
            System.out.println(e.toString());
            return null;
        }
    }

    private static void InsertJpgToPDFFile(String JpgFilePath){
        try{
            String CurrentFolderPath = JpgFilePath.substring(0, JpgFilePath.lastIndexOf("\\")+1);
            String JpgFileName = JpgFilePath.substring(JpgFilePath.lastIndexOf("\\")+1);
            String PureFileName = JpgFileName.substring(0, JpgFileName.lastIndexOf("."));
            String PdfFileName = PureFileName + ".pdf";
            String PdfFilePath = CurrentFolderPath + PdfFileName;
            int[] PageSize = ChoosePageSize(JpgFilePath);
            PDRectangle pdRectangle = new PDRectangle(PageSize[0], PageSize[1]);
            //PageSizeEnum PageSize = ChoosePageSize(JpgFilePath);

            System.out.println(CurrentFolderPath);
            System.out.println(JpgFileName);
            System.out.println(PureFileName);
            System.out.println(PdfFileName);
            System.out.println(PdfFilePath);

            PDDocument document = new PDDocument();
            //Retrieving the page
            PDPage page1 = new PDPage();
            /*switch (PageSize){
                case A0:
                    page1.setMediaBox(PDRectangle.A0);
                    page1.setRotation(90);
                    break;
                case A1:
                    page1.setMediaBox(PDRectangle.A1);
                    break;
                case A2:
                    page1.setMediaBox(PDRectangle.A2);
                    break;
                case A3:
                    page1.setMediaBox(PDRectangle.A3);
                    break;
                case A4:
                    page1.setMediaBox(PDRectangle.A4);
                    break;
            }*/
            page1.setMediaBox(pdRectangle);
            //Creating PDImageXObject object

            /*File file = new File(JpgFilePath);
            InputStream input = new FileInputStream(file);

            OutputStream os = new ByteArrayOutputStream();
            OutputStream resultOs = new ByteArrayOutputStream();


            resultOs = resizeImage(input, os, "jpg", PageSize);




            //PDImageXObject pdImage1 = PDImageXObject.createFromFile("D:\\素材\\图片\\聊天气泡\\素材图\\IMG_20210108_083917.jpg", document);
            PDImageXObject pdImage1 = PDImageXObject.createFromByteArray(document, ((ByteArrayOutputStream) resultOs).toByteArray(), "图片");*/
            PDImageXObject pdImage1 = PDImageXObject.createFromFile(JpgFilePath, document);


            //creating the PDPageContentStream object
            PDPageContentStream contents = new PDPageContentStream(document, page1);



            //Drawing the image in the PDF document
            contents.drawImage(pdImage1, 0, 0);
            //contents.drawImage(pdImage1, pdImage1.getWidth(), pdImage1.getHeight());
            System.out.println("Image inserted" + pdImage1.getWidth() + "," + pdImage1.getHeight());
            //Closing the PDPageContentStream object
            contents.close();
            //creating the PDPageContentStream object
            System.out.println("Image inserted");
            //Closing the PDPageContentStream object

            document.addPage(page1);
            //Saving the document
            document.save(PdfFilePath);
            System.out.println("PDF created");
            //Closing the document
            document.close();
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
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

    private static void ParseFileForGrade3(String Path){
        try {
            //System.out.println(FolderName);
            File file = new File(Path);
            File[] fs = file.listFiles();
            //遍历File[]数组
            for(File f : fs) {
                String FilePath = f.toString();
                if (!f.isDirectory() && FilePath.contains(".jpg")) {
                    InsertJpgToPDFFile(f.toString());
                }
                else{
                    ParseFileForGrade3(FilePath);
                }
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    /*
    批量获取和转换地图集pdf
     */
    private static void ParseDTJFolderForAndroid(String BasePath, String NewPath, String FolderPath){
        try{

            File NewFolder = new File(NewPath);
            if (!NewFolder.exists())
                NewFolder.mkdirs();

            CreateMapCollectionForAndroid(BasePath, NewPath, FolderPath);
        }
        catch (Exception e){
            System.out.println("地图集文件夹批量处理过程中出错，具体原因如下： " + "\n" + e.toString());
        }
    }

    /*
    批量获取和转换地图集pdf
     */
    private static void ParseDTJFolderForWindows(String BasePath, String NewPath){
        try{

            File NewFolder = new File(NewPath);
            if (!NewFolder.exists())
                NewFolder.mkdirs();

            CreateMapCollectionForWindows(BasePath, NewPath);
        }
        catch (Exception e){
            System.out.println("地图集文件夹批量处理过程中出错，具体原因如下： " + "\n" + e.toString());
        }
    }

    /*
    获取地名志中的地名信息
     */
    private static void ParseDMZPageInfos(){
        try {
            /*
            获取地名pdf中的地名及其页码信息
             */

            String DMZFilePath = "C:\\Users\\54286\\Desktop\\盘龙区地名志——正文1103.pdf";
            File file = new File(DMZFilePath);

            PDDocument doc = PDDocument.load(file);

            System.out.println(doc.getNumberOfPages());
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            BufferedWriter bw = new BufferedWriter(new FileWriter(DMZFilePath));
            for (int i = 1; i < doc.getNumberOfPages(); i++) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                String content = stripper.getText(doc);

                String[] strs = content.split("\n");
                for (int j = 0; j < strs.length; j++) {
                    if (strs[j].indexOf("【") != -1) {
                        bw.write(strs[j].substring(0, strs[j].indexOf("【")));
                        bw.write(" ");
                        bw.write(String.valueOf(i - 6));
                        bw.newLine();
                    }
                }
            }
            //关闭流
            bw.close();
            System.out.println("写入成功");
        }
        catch (IOException e){
            System.out.println(e.toString());
        }
    }

    private static void CreateMapCollectionForAndroid(String BasePath, String NewPath, String FolderPath){
        try {
            int MapTypeCount = 0;
            String GeoInfoPath = NewPath + "\\dataForAndroid.txt";

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

    private static void CreateMapCollectionForWindows(String BasePath, String NewPath){
        try {
            int MapTypeCount = 0;
            String GeoInfoPath = NewPath + "\\data.txt";

            File file = new File(GeoInfoPath);
            file.createNewFile();

            BufferedWriter bw = new BufferedWriter(new FileWriter(GeoInfoPath));
            //获取其file对象
            File mfile = new File(BasePath);
            ParseFilesForWindows(mfile, "", bw, MapTypeCount, NewPath);
            bw.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private static void SavePDFWithoutGeoInfoForAndroid(String FolderName, String FileName, String NewPath, String ParentNodeName, File f, BufferedWriter bw, String FolderPath){
        try {
            System.out.println(FileName);
            PDDocument doc = PDDocument.load(f);

            String dtName = FileName.substring(FileName.lastIndexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".dt";
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
            System.out.println(FileName);
            PDDocument doc = PDDocument.load(f);
            String GeoInfo[] = GetGeoInfoFromPDFFile(doc);
            //String pngName = FileName.substring(FileName.lastIndexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".png";

            String dtName = FileName.substring(FileName.lastIndexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".dt";
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
                    if (FolderName.contains("社会与经济") || FolderName.contains("资源与环境") || FolderName.contains("临沧概览")){
                        if ((FileName.contains( "行政区划") || FileName.contains( "旅游交通")))
                            SavePDFWithGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
                        else
                            SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
                    }
                    else {
                        SavePDFWithGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
                    }

                    //SavePDFWithoutGeoInfoForAndroid(FolderName, FileName, NewPath, ParentNodeName, f, bw, FolderPath);
                }
                else{
                    bw.write(ParentNodeName + "," + FileName.substring(FileName.lastIndexOf("\\") + 1) + "," + GetMapType(ParentNodeName) + "," + " ");
                    bw.newLine();
                    String DirectoryName = f.toString();
                    if (MapTypeCount == 0)
                    {
                        ParseFilesForAndoird(f, DirectoryName.substring(DirectoryName.lastIndexOf("\\") + 1), bw, MapTypeCount + count++, NewPath, FolderPath);
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

    private static void SavePDFWithoutGeoInfoForWindows(String FolderName, String FileName, String NewPath, String ParentNodeName, File f, BufferedWriter bw){
        try {

            System.out.println(FileName);
            PDDocument doc = PDDocument.load(f);
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 300, ImageType.RGB);
            String pngName = FileName.substring(FileName.lastIndexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".png";

                    /*File NewConcisePath = new File(NewPath + "\\" + pngName.substring(0, pngName.lastIndexOf("\\")));
                    NewConcisePath.mkdirs();*/

            ImageIO.write(bufferedImage, "png", new File(NewPath + "\\" + pngName));
            String PngName = pngName.substring(pngName.lastIndexOf("\\") + 1, pngName.lastIndexOf(".png")).trim();
            //String MapType = GetMapType(ParentNodeName);
            String PngPath = NewPath + "\\" + pngName;
            String NewFilePath = PngPath.replace("png","xml");
            // 开始复制
            copy(new File(PngPath), new File(NewFilePath));
            doc.close();
            //bw.write(ParentNodeName + "," + PngName + "," + MapType + "," + PngPath);
            bw.write(ParentNodeName + "," + PngName + "," + GetMapType(ParentNodeName) + "," + NewFilePath + ", ");
            bw.newLine();
        }catch (Exception e){
            System.out.println("在处理带有空间信息的PDF文件过程中出错" + "\n" + e.toString());
        }
    }

    private static void SavePDFWithGeoInfoForWindows(String FolderName, String FileName, String NewPath, String ParentNodeName, File f, BufferedWriter bw){
        try {

            System.out.println(FileName);
            PDDocument doc = PDDocument.load(f);
            String GeoInfo[] = GetGeoInfoFromPDFFile(doc);
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 200, ImageType.RGB);
            String pngName = FileName.substring(FileName.lastIndexOf(FolderName), FileName.lastIndexOf(".pdf")) + ".png";

                    /*File NewConcisePath = new File(NewPath + "\\" + pngName.substring(0, pngName.lastIndexOf("\\")));
                    NewConcisePath.mkdirs();*/

            ImageIO.write(bufferedImage, "png", new File(NewPath + "\\" + pngName));
            String PngName = pngName.substring(pngName.lastIndexOf("\\") + 1, pngName.lastIndexOf(".png")).trim();
            //String MapType = GetMapType(ParentNodeName);
            String PngPath = NewPath + "\\" + pngName;
            String NewFilePath = PngPath.replace("png","xml");

            // 开始复制
            copy(new File(PngPath), new File(NewFilePath));

            doc.close();
            //bw.write(ParentNodeName + "," + PngName + "," + MapType + "," + PngPath);
            if (GeoInfo.length <= 1)
            {
                //bw.write(ParentNodeName + "," + PngName + "," + MapTypeCount + "," + PngPath + ", " + ", " + ", " + ", ");
                bw.write(ParentNodeName + "," + PngName + "," + GetMapType(ParentNodeName) + "," + NewFilePath + ", " + ", " + ", " + ", ");
            }
            else
            {
                //bw.write(ParentNodeName + "," + PngName + "," + MapTypeCount + "," + PngPath + "," + GeoInfo[0] + "," + GeoInfo[1] + "," + GeoInfo[2] + "," + GeoInfo[3]);
                bw.write(ParentNodeName + "," + PngName + "," + GetMapType(ParentNodeName) + "," + NewFilePath + "," + GeoInfo[0] + "," + GeoInfo[1] + "," + GeoInfo[2] + "," + GeoInfo[3]);
            }
            bw.newLine();
        }catch (Exception e){
            System.out.println("在处理带有空间信息的PDF文件过程中出错" + "\n" + e.toString());
        }
    }

    private static void copy(File inputFile, File outputFile) throws Exception{
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

    private static void ParseFilesForWindows(File file, String FolderName, BufferedWriter bw, int MapTypeCount, String NewPath){
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
                    /*if (FolderName.contains("社会经济图组") || FolderName.contains("资源与环境图组")){
                        SavePDFWithoutGeoInfoForWindows(FolderName, FileName, NewPath, ParentNodeName, f, bw);
                    }
                    else {
                        SavePDFWithGeoInfoForWindows(FolderName, FileName, NewPath, ParentNodeName, f, bw);
                    }*/
                    if (FolderName.contains("社会与经济") || FolderName.contains("资源与环境") || FolderName.contains("临沧概览")){
                        if ((FileName.contains( "行政区划") || FileName.contains( "旅游交通")))
                            SavePDFWithGeoInfoForWindows(FolderName, FileName, NewPath, ParentNodeName, f, bw);
                        else
                            SavePDFWithoutGeoInfoForWindows(FolderName, FileName, NewPath, ParentNodeName, f, bw);
                    }
                    else {
                        SavePDFWithGeoInfoForWindows(FolderName, FileName, NewPath, ParentNodeName, f, bw);
                    }
                    //SavePDFWithoutGeoInfoForWindows(FolderName, FileName, NewPath, ParentNodeName, f, bw);
                }
                else{
                    //bw.write(ParentNodeName + "," + FileName.substring(FileName.lastIndexOf("\\") + 1) + "," + MapTypeCount + "," + " ");
                    bw.write(ParentNodeName + "," + FileName.substring(FileName.lastIndexOf("\\") + 1) + "," + GetMapType(ParentNodeName) + "," + " ");
                    bw.newLine();
                    String DirectoryName = f.toString();
                    if (MapTypeCount == 0)
                    {
                        ParseFilesForWindows(f, DirectoryName.substring(DirectoryName.lastIndexOf("\\") + 1), bw, MapTypeCount + count++, NewPath);
                    }
                    else
                    {
                        ParseFilesForWindows(f, FolderName + DirectoryName.substring(DirectoryName.lastIndexOf("\\")), bw, MapTypeCount + count++, NewPath);
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
    }

    private static String GetMapType(String ParentNodeName){
        switch (ParentNodeName){
            case "临沧概览":
                return "1";
            case "资源与环境":
                return "2";
            case "社会与经济":
                return "3";
            case "区域地理":
                return "4";
            case "沧源佤族自治县":
                return "5";
            case "凤庆县":
                return "6";
            case "耿马傣族佤族自治县":
                return "7";
            case "临翔区":
                return "8";
            case "双江拉祜族佤族布朗族傣族自治县":
                return "9";
            case "永德县":
                return "10";
            case "云县":
                return "11";
            case "镇康县":
                return "12";
        }
        return "0";
    }

    /*private static String GetMapType(String ParentNodeName){
        switch (ParentNodeName){
            case "序图组":
                return "1";
            case "资源与环境图组":
                return "2";
            case "社会经济图组":
                return "3";
            case "区域地理图组":
                return "4";
            case "县图":
                return "5";
            case "州图":
                return "6";
            case "州市介绍":
                return "7";
            case "各州市城区影像图":
                return "8";
            case "保山市":
                return "9";
            case "楚雄彝族自治州":
                return "10";
            case "大理白族自治州":
                return "11";
            case "德宏傣族景颇族自治州":
                return "12";
            case "迪庆藏族自治州":
                return "13";
            case "红河哈尼族彝族自治州":
                return "14";
            case "昆明市":
                return "15";
            case "丽江市":
                return "16";
            case "临沧市":
                return "17";
            case "怒江傈僳族自治州":
                return "18";
            case "普洱市":
                return "19";
            case "曲靖市":
                return "20";
            case "文山壮族苗族自治州":
                return "21";
            case "西双版纳傣族自治州":
                return "22";
            case "玉溪市":
                return "23";
            case "昭通市":
                return "24";
        }
        return "0";
    }*/

    private static String GetMapType1(String ParentNodeName){
        switch (ParentNodeName){
            case "南亚东南亚图集":
                return "1";
            case "云南省地图集":
                return "2";
            case "十四五规划系列图":
                return "3";
        }
        return "0";
    }

    public static String dateFormat( Calendar calendar ) throws Exception
    {
        if( null == calendar )
            return null;
        String date = null;
        try{
            String pattern = DATE_FORMAT;
            SimpleDateFormat format = new SimpleDateFormat( pattern );
            date = format.format( calendar.getTime() );
        }catch( Exception e )
        {
            throw e;
        }
        return date == null ? "" : date;
    }

    
    /**
     * Runs the engine on the current page.
     *
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void run() throws IOException
    {
        processPage(getPage());

        for (PDAnnotation annotation : getPage().getAnnotations())
        {
            showAnnotation(annotation);
        }
    }
    
    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
    {
        System.out.printf("appendRectangle %.2f %.2f, %.2f %.2f, %.2f %.2f, %.2f %.2f%n",
                p0.getX(), p0.getY(), p1.getX(), p1.getY(),
                p2.getX(), p2.getY(), p3.getX(), p3.getY());
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException
    {
        System.out.println("drawImage");
    }

    @Override
    public void clip(int windingRule) throws IOException
    {
        System.out.println("clip");
    }

    @Override
    public void moveTo(float x, float y) throws IOException
    {
        System.out.printf("moveTo %.2f %.2f%n", x, y);
    }

    @Override
    public void lineTo(float x, float y) throws IOException
    {
        System.out.printf("lineTo %.2f %.2f%n", x, y);
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
    {
        System.out.printf("curveTo %.2f %.2f, %.2f %.2f, %.2f %.2f%n", x1, y1, x2, y2, x3, y3);
    }

    @Override
    public Point2D getCurrentPoint() throws IOException
    {
        // if you want to build paths, you'll need to keep track of this like PageDrawer does
        return new Point2D.Float(0, 0);
    }

    @Override
    public void closePath() throws IOException
    {
        System.out.println("closePath");
    }

    @Override
    public void endPath() throws IOException
    {
        System.out.println("endPath");
    }

    @Override
    public void strokePath() throws IOException
    {
        System.out.println("strokePath");
    }

    @Override
    public void fillPath(int windingRule) throws IOException
    {
        System.out.println("fillPath");
    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException
    {
        System.out.println("fillAndStrokePath");
    }

    @Override
    public void shadingFill(COSName shadingName) throws IOException
    {
        System.out.println("shadingFill " + shadingName.toString());
    }

    /**
     * Overridden from PDFStreamEngine.
     */
    @Override
    public void showTextString(byte[] string) throws IOException
    {
        System.out.print("showTextString \"");
        super.showTextString(string);
        System.out.println("\"");
    }

    /**
     * Overridden from PDFStreamEngine.
     */
    @Override
    public void showTextStrings(COSArray array) throws IOException
    {
        System.out.print("showTextStrings \"");
        super.showTextStrings(array);
        System.out.println("\"");
    }

    /**
     * Overridden from PDFStreamEngine.
     */
    @Override
    protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, Vector displacement)
            throws IOException
    {
        System.out.print("showGlyph " + code);
        super.showGlyph(textRenderingMatrix, font, code, displacement);
    }
    
    // NOTE: there are may more methods in PDFStreamEngine which can be overridden here too.
}
