package com.qianlima.offline.util;




import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


public class PDFUtilCopy {

    public static void main(String[] args) throws Exception {
        createPDF();
        //imageWaterMark(createPDF(), "E://1.jpg");
    }


    /**
     * 创建PDF文档
     * @return
     * @throws Exception
     */
    public static String createPDF() throws Exception {

        //输出路径
        String outPath = "E://test.pdf";//DataUtil.createTempPath(".pdf");

        //设置纸张
        Rectangle rect = new Rectangle(PageSize.A4);

        //创建文档实例
        Document doc=new Document(rect);

        //添加中文字体
        BaseFont bfChinese=BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

        //设置字体样式
        Font textFont = new Font(bfChinese,11,Font.NORMAL); //正常
        Font redTextFont = new Font(bfChinese,11,Font.NORMAL, BaseColor.RED); //正常,红色
        Font boldFont = new Font(bfChinese,11,Font.BOLD); //加粗
        Font redBoldFont = new Font(bfChinese,11,Font.BOLD,BaseColor.RED); //加粗,红色
        Font firsetTitleFont = new Font(bfChinese,22,Font.BOLD); //一级标题
        Font secondTitleFont = new Font(bfChinese,15,Font.BOLD); //二级标题
        Font underlineFont = new Font(bfChinese,11,Font.UNDERLINE); //下划线斜体

        //手指图片
        Image hand = Image.getInstance("E://12.png");

        //创建输出流
        PdfWriter.getInstance(doc, new FileOutputStream(new File(outPath)));

        doc.open();
        doc.newPage();

        //段落
        Paragraph p1 = new Paragraph();
        //短语
        Phrase ph1 = new Phrase();
        //块
        Chunk c1 = new Chunk("*********", boldFont) ;
        Chunk c11 = new Chunk("（信用报告提供机构l ogo）", textFont) ;
        //将块添加到短语
        ph1.add(c1);
        ph1.add(c11);
        //将短语添加到段落
        p1.add(ph1);
        //将段落添加到短语
        doc.add(p1);

        doc.close();
        return outPath;
    }

    /**
     * 创建单元格
     * @param table
     * @param row
     * @param cols
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    private static PdfPTable createCell(PdfPTable table, String[] title, int row, int cols) throws DocumentException, IOException{
        //添加中文字体
        BaseFont bfChinese=BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font font = new Font(bfChinese,11,Font.BOLD);

        for(int i = 0; i < row; i++){
            for(int j = 0; j < cols; j++){
                PdfPCell cell = new PdfPCell();
                if(i==0 && title!=null){//设置表头
                    cell = new PdfPCell(new Phrase(title[j], font)); //这样表头才能居中
                    if(table.getRows().size() == 0){
                        cell.setBorderWidthTop(3);
                    }
                }

                if(row==1 && cols==1){ //只有一行一列
                    cell.setBorderWidthTop(3);
                }

                if(j==0){//设置左边的边框宽度
                    cell.setBorderWidthLeft(3);
                }

                if(j==(cols-1)){//设置右边的边框宽度
                    cell.setBorderWidthRight(3);
                }

                if(i==(row-1)){//设置底部的边框宽度
                    cell.setBorderWidthBottom(3);
                }

                cell.setMinimumHeight(40); //设置单元格高度
                cell.setUseAscender(true); //设置可以居中
                cell.setHorizontalAlignment(Element.ALIGN_CENTER); //设置水平居中
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE); //设置垂直居中

                table.addCell(cell);
            }
        }
        return table;
    }

    /**
     * 加水印（字符串）
     * @param inputFile 需要加水印的PDF路径
     * @param //outputFile 输出生成PDF的路径
     * @param waterMarkName 水印字符
     */
    public static void stringWaterMark(String inputFile, String waterMarkName) {
        try {
            String[] spe = DataUtil.separatePath(inputFile);
            String outputFile = spe[0] + "_WM." + spe[1];

            PdfReader reader = new PdfReader(inputFile);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile));

           //添加中文字体
            BaseFont bfChinese=BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);

            int total = reader.getNumberOfPages() + 1;

            PdfContentByte under;
            int j = waterMarkName.length();
            char c = 0;
            int rise = 0;
            //给每一页加水印
            for (int i = 1; i < total; i++) {
                rise = 400;
                under = stamper.getUnderContent(i);
                under.beginText();
                under.setFontAndSize(bfChinese, 30);
                under.setTextMatrix(200, 120);
                for (int k = 0; k < j; k++) {
                    under.setTextRise(rise);
                    c = waterMarkName.charAt(k);
                    under.showText(c + "");
                }

                // 添加水印文字
                under.endText();
            }
            stamper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加水印（图片）
     * @param inputFile 需要加水印的PDF路径
     * @param imageFile 水印图片路径
     */
    public static void imageWaterMark(String inputFile, String imageFile) {
        try {
            String[] spe = DataUtil.separatePath(inputFile);
            String outputFile = spe[0] + "_WM." + spe[1];

            PdfReader reader = new PdfReader(inputFile);
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile));

            int total = reader.getNumberOfPages() + 1;

            Image image = Image.getInstance(imageFile);
            image.setAbsolutePosition(-100, 0);//坐标
            image.scaleAbsolute(800,1000);//自定义大小
            //image.setRotation(-20);//旋转 弧度
            //image.setRotationDegrees(-45);//旋转 角度
            //image.scalePercent(50);//依照比例缩放

            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.2f);// 设置透明度为0.2

            PdfContentByte under;
            //给每一页加水印
            for (int i = 1; i < total; i++) {
                under = stamper.getUnderContent(i);
                under.beginText();
                // 添加水印图片
                under.addImage(image);
                under.setGState(gs);
            }
            stamper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置左边距
     * @param str
     * @param i
     * @return
     */
    public static String leftPad(String str, int i) {
        int addSpaceNo = i-str.length();
        String space = "";
        for (int k=0; k<addSpaceNo; k++){
            space= " "+space;
        };
        String result =space + str ;
        return result;
    }

    /**
     * 设置模拟数据
     * @param list
     * @param num
     */
    public static void add(List<String> list,int num){
        for(int i=0;i<num;i++){
            list.add("test"+i);
        }
    }

    /**
     * 设置间距
     * @param tmp
     * @return
     */
    public static String printBlank(int tmp){
        String space="";
        for(int m=0;m<tmp;m++){
            space=space+" ";
        }
        return space;
    }


}