package com.mapbox.graphhoppertest;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.shapes.GHPoint;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class GraphHopperTest 
{
    HashMap<Integer , Job> Jobs = new HashMap<>();
    public static void main(String args[]) throws IOException, FileNotFoundException, Exception
    {
        System.out.println("Inside main() ");
        GraphHopperTest ght = new GraphHopperTest();
        ght.getData();
        System.out.println("Done");
    }
    public void getData() throws FileNotFoundException, IOException, Exception
    {
        System.out.println("Inside getData()");
        for(int i=0;i<=88;i++)
        {
            String file1 = "Daywise.xlsx";
            File myfile = new File(file1);
            FileInputStream fis = new FileInputStream(myfile);
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet mySheet = wb.getSheetAt(i);
            Iterator<Row> rowIterator = mySheet.iterator();
            int j=0;
            while (rowIterator.hasNext()) 
            {
                j++;
                Row row = rowIterator.next();
                Cell cell0 = row.getCell(0);
                int c1 = (int)cell0.getNumericCellValue();  //Key
                Cell cell1 = row.getCell(1);
                double c2 = cell1.getNumericCellValue();    //Time
                Cell cell2 = row.getCell(2);
                double c3 = cell2.getNumericCellValue();    //Lat
                Cell cell3 = row.getCell(3);
                double c4 = cell3.getNumericCellValue();    //Lng
                Cell cell4 = row.getCell(4);
                String c5 = cell4.getStringCellValue();     //Driver
                Job j1 = new Job(c1,c5,c2,c3,c4);
                Jobs.put(j, j1);
            }
            GHCalc(i+1);
            Jobs.clear();
        }
    }
    public void GHCalc(int sheetIndex) throws FileNotFoundException, IOException
    {
        System.out.println("Inside GHCalc()");
        String GHAPIKey = "238240ee-0a86-4a90-8d6a-de610dae88bc";
        ArrayList<GHPoint> waypts = new ArrayList<>();
        for(int i=1;i<=Jobs.size();i++)
        {
           GHPoint pos = new GHPoint();
           pos.lat = Jobs.get(i).lat;
           pos.lon = Jobs.get(i).lng;
           waypts.add(pos);
        }
        String file1 = "GHDuration.xlsx";
        String file2 = "GHDistance.xlsx";
        File myfile1 = new File(file1);
        File myfile2 = new File(file2);
        FileInputStream fis1 = new FileInputStream(myfile1);
        FileInputStream fis2 = new FileInputStream(myfile2);
        XSSFWorkbook wb1 = new XSSFWorkbook(fis1);
        XSSFWorkbook wb2 = new XSSFWorkbook(fis2);
        String sheetname = "Sheet"+sheetIndex;
        System.out.println(sheetname);
        XSSFSheet mySheet1;
        XSSFSheet mySheet2;
        if("Sheet1".equalsIgnoreCase(sheetname))
        {
            mySheet1 = wb1.getSheet(sheetname);
            mySheet2 = wb2.getSheet(sheetname);
        }
        else
        {
            mySheet1 = wb1.createSheet(sheetname);
            mySheet2 = wb2.createSheet(sheetname);
        }
        for(int i=1;i<=waypts.size();i++)
        {
            int rownum1 = mySheet1.getLastRowNum(); 
            int rownum2 = mySheet2.getLastRowNum();
            rownum1++;
            rownum2++;
            Row row1 = mySheet1.createRow(rownum1);
            Row row2 = mySheet2.createRow(rownum2);
            for(int j=1;j<=waypts.size();j++)
            {
                if(j==i)
                {
                    double calc_time = Double.POSITIVE_INFINITY;
                    double calc_dist = Double.POSITIVE_INFINITY;
                    Cell cell1 = row1.createCell(j); 
                    cell1.setCellValue((Double) calc_time); 
                    FileOutputStream os1 = new FileOutputStream(myfile1);
                    wb1.write(os1); 
                    Cell cell2 = row2.createCell(j); 
                    cell2.setCellValue((Double) calc_dist); 
                    FileOutputStream os2 = new FileOutputStream(myfile2);
                    wb1.write(os2); 
                }
                else
                {
                    GraphHopper gh = new GraphHopper().forDesktop();
                    String ghLocation = "./";
                    gh.setOSMFile("ireland-and-northern-ireland.osm_01.osm");
                    gh.setGraphHopperLocation(ghLocation);
                    gh.setEncodingManager(new EncodingManager("car"));
                    gh.importOrLoad();
                    GHRequest req = new GHRequest(waypts.get(i) , waypts.get(j));
                    GHResponse res = gh.route(req);
                    PathWrapper pw = res.getBest();
                    double calc_time = (double)pw.getTime()/60000;
                    Cell cell1 = row1.createCell(j); 
                    cell1.setCellValue((Double) calc_time); 
                    FileOutputStream os1 = new FileOutputStream(myfile1);
                    wb1.write(os1); 
                    double calc_dist = (double)pw.getDistance()/1000;
                    Cell cell2 = row2.createCell(j); 
                    cell2.setCellValue((Double) calc_dist); 
                    FileOutputStream os2 = new FileOutputStream(myfile2);
                    wb2.write(os2); 
                }
            }
        }
    }
    class Job
    {
        private int key;
        private double lat;
        private double time;
        private String driver;
        private double lng;
        public Job(int key, String driver, double time, double lat, double lng)
        {
            this.key=key;
            this.driver = driver;
            this.time = time;
            this.lat = lat;
            this.lng = lng;
        }
        public String getDriver() 
        {
            return driver;
        }
        public double getTime() 
        {
            return time;
        }
        public double getLat() 
        {
            return lat;
        }
        public double getLng() 
        {
            return lng;
        }
        public int getKey() 
        {
            return key;
        }
        @Override
        public String toString() 
        {
            return "Job [Key=" +key +", Driver=" + driver + ", time=" + time + ", Lat=" + lat + ", Lng=" + lng + "]";
        }
    }
}
