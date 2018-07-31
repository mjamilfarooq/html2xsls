package com.h2e.webservice.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Html2Xsls {

    private final String license_notice = "Not a licensed Copy!!";

    private int getWidthFromColumns(Element row, int from, int to)
    {

        int sum = 0;
        int i = 1;
        for (Element col: row.getElementsByTag("td"))
        {
            if (i>=from&&i< (from + to))
                sum += Integer.parseInt(col.attr("width"));
            i++;
        }

        return sum;
    }



    private void CreateWaterMarkForLicensing(XSSFSheet sheet)
    {



        XSSFWorkbook workbook = sheet.getWorkbook();

        //creating font
        XSSFFont font = workbook.createFont();
        font.setColor((short) 27);
        font.setBold(true);
        font.setFontHeightInPoints((short) 50);
        font.setFontName("Verdana");

        //text to overlay
        XSSFRichTextString rtxt = new XSSFRichTextString(license_notice);
        rtxt.applyFont(font);

        //create drawing object
        XSSFDrawing drawing = sheet.createDrawingPatriarch();

        //generate watermark for full length of report
        for (int row = 4; row < sheet.getLastRowNum(); row += 10)
        {

            XSSFClientAnchor anchor = new XSSFClientAnchor
                    (0, 0, 1023, 255, (short) 2, row, (short) 13, row+4);


            XSSFTextBox textbox = drawing.createTextbox(anchor);
            textbox.setText(rtxt);
            textbox.setNoFill(true);

        }




    }

    public XSSFWorkbook CreateExcelFromHtml(File report, boolean licensed) throws IOException {

        Document document = Jsoup.parse(report, "WINDOWS-1252");

        XSSFWorkbook workBook = new XSSFWorkbook();
        XSSFSheet workSheet = workBook.createSheet();

        Elements bodies = document.getElementsByTag("body");

        int rownum = 0;
        for (Element body: bodies)
        {

            Elements tables = body.getElementsByTag("table");
            for (Element table: tables)
            {

                int tableWidthInPixels = Integer.parseInt(table.attr("width"));
                Element firstRow = table.getElementsByTag("tr").first();
                Elements allColumnsOfFirstRow = firstRow.getElementsByTag("td");

//                System.out.println("all columns first row: " + allColumnsOfFirstRow.size());
                int sumOfWidthOfAllColumns = 0;
                for(Element tds : allColumnsOfFirstRow)
                {
                    sumOfWidthOfAllColumns += Integer.parseInt(tds.attr("width"));
                }
//                System.out.println("sum of columsn and width of table: " + tableWidthInPixels + " " + sumOfWidthOfAllColumns);




//                XSSFSheet workSheet = workBook.createSheet();
                int defaultColumnWidth = workSheet.getDefaultColumnWidth();
                float pixelWidth = workSheet.getColumnWidthInPixels(0);
                short pixelHeigth = workSheet.getDefaultRowHeight();
//                System.out.println("default Column width " + defaultColumnWidth + " pixel width " + pixelWidth + " pxiel height: " + pixelHeigth);
                //read each row in the table
//                int rownum = 0;

                //if (true) break;


                for ( Element row: table.getElementsByTag("tr"))
                {

                    if (rownum ==0) {rownum++;continue;} //ignore first row.



                    Row newrow = workSheet.createRow(rownum);
                    int rowheight = newrow.getHeight();
//                    System.out.println("row height is : " + rowheight);
                    int cellnum = 0;
                    boolean isRowEmpty = true;

                    int columnIndex = 1;

                    int widthofColumnInPixel = 0;
                    int pixelOffsetOfCellFromStart = 0;
                    int mergecells=0;
                    for ( Element cell : row.getElementsByTag("td"))
                    {
                        int colspan = cell.hasAttr("colspan")?Integer.parseInt(cell.attr("colspan")):1;
                        pixelOffsetOfCellFromStart += widthofColumnInPixel;
                        widthofColumnInPixel = getWidthFromColumns(firstRow, columnIndex, colspan);
                        columnIndex += colspan;

                        String text = cell.text();



                        int prevcellnum = cellnum;
                        cellnum = (int ) (pixelOffsetOfCellFromStart/pixelWidth);
               //         cellnum = ( (prevcellnum + mergecells ) > cellnum && prevcellnum != 0) ? cellnum+1:cellnum;
               //         mergecells = (int)(widthofColumnInPixel/pixelWidth);

               //         System.out.println("rownum: " + rownum + " cellnum: " + cellnum + " columIndex: " + columnIndex + " widthCell " + widthofColumnInPixel + " pixelOffset " + pixelOffsetOfCellFromStart + " text: " + text +  " mrgecell: " + mergecells);

//                        if (mergecells>1 && (!text.equalsIgnoreCase("") || text != null) ) {
//                            workSheet.addMergedRegion(new CellRangeAddress(rownum, rownum, cellnum, cellnum + mergecells-1));
//                            isRowEmpty = false;
//                        }

                        Cell newcell = newrow.createCell(cellnum);
                        newcell.setCellValue(cell.text());


                        XSSFFont cellFont = workBook.createFont();
                        XSSFCellStyle cellStyle = workBook.createCellStyle();

                        //Setting font for the cell
                        Element font = cell.getElementsByTag("font").first();
                        if ( font != null )
                        {
                            if (font.hasAttr("face"))
                                cellFont.setFontName(font.attr("face"));
                            if (font.hasAttr("size")) {
                                int size = Integer.parseInt(font.attr("size"));
//                                cellFont.setFontHeight(cellFont.getFontHeight()+size);
                            }


                            if ( font.getElementsByTag("b").first() != null )
                            {
                                cellFont.setBold(true);
                            }
                        }


                        cellStyle.setFont(cellFont);
                        newcell.setCellStyle(cellStyle);


                        if (text.compareTo("") == 0)
                        {
                            continue;
                        }

                        else

                        try {

                            NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
                            double temp = nf.parse(text).doubleValue();
//                            double temp = Double.parseDouble(text);
                            newcell.setCellType(CellType.NUMERIC);
                            newcell.setCellValue(temp);
//                            System.out.println("double " + temp);
                        } catch(ParseException ex)
                        {
                            if ( text.startsWith("(") && text.endsWith(")")) {

                                try {
                                    String temp = text.substring(1, text.length() - 1);
                                    NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
                                    newcell.setCellType(CellType.NUMERIC);
                                    double value = -nf.parse(temp).doubleValue();
                                    newcell.setCellValue(value);

                                } catch (ParseException exAgain)
                                {

                                    newcell.setCellType(CellType.STRING);
                                    newcell.setCellValue(text);
                                }
                            }


                        }

                        isRowEmpty = false;

                        cellnum++;
                    }

                    if (!isRowEmpty)
                        rownum++;
                }

//                workSheet.autoSizeColumn(-1);
            }


            break;

        }


        if (!licensed)
            CreateWaterMarkForLicensing(workSheet);

    return workBook;


    }
}
