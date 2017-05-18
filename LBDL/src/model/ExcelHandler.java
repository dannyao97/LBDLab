package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Handles reading and writing from Excel files.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class ExcelHandler {

   /**
    * A reference to the LogicModel
    */
   private LogicModel model;
   /**
    * Smallest number of students encountered
    */
   private int smallest = Integer.MAX_VALUE;
   /**
    * The start of the date column
    */
   protected int dateStart = 7;
   /**
    * The end of the dates columns HARDCODED 28 dates. CHANGE
    */
   protected int dateEnd = 34;
   /**
    * The # of important schools to schedule first.
    */
   protected int topPriority = 10;
   /**
    * The top priority counter
    */
   protected int needAddCounter = 0;
   /**
    * Total number of students count
    */
   protected int totalStudentCount = 0;
   /**
    * Total number of seats available
    */
   protected int totalSeats = 0;
   
   public ExcelHandler(LogicModel model) {
      this.model = model;
      //this.totalStudents = 0;
      LogicModel.schoolId = 0;
   }

   /**
    * Read the Excel file.
    *
    * @param filename The name of the file.
    * @throws InvalidFormatException If file is malformed.
    */
   protected void readXLFile(String filename, String start, String end) throws InvalidFormatException {

      dateStart = convertColumn(start);
      dateEnd = convertColumn(end);

      if (dateEnd < dateStart)
      {
         model.notifyText = "Error: Invalid column dates selected";
         model.notify(LogicModel.NotifyCmd.ERROR);
         return;
      }

      try
      {
         XSSFWorkbook wb = new XSSFWorkbook(new File(filename));
         XSSFSheet wkSheet = wb.getSheetAt(0);
         XSSFRow xlrow;
         XSSFCell cell;
         int numRows = 2; // Schools start from row 2 and on
         //numRows = wkSheet.getPhysicalNumberOfRows();
         int numCols = 0; // Num of columns
         int tmp = 0;
         int schoolCount = 0;
         needAddCounter = 0;

         //Checks each row for a school name to calculate total number of schools
         do
         {
            cell = wkSheet.getRow(numRows).getCell(1);
            numRows++;
         } while (cell != null);

         model.resetModel();
         // Get number of columns. Go through 10 rows to make sure
         for (int i = 0; i < 10 || i < numRows; i++)
         {
            xlrow = wkSheet.getRow(i);
            if (xlrow != null)
            {
               tmp = wkSheet.getRow(i).getPhysicalNumberOfCells();
               if (tmp > numCols)
               {
                  numCols = tmp;
               }
            }
         }
         //Initialize available days from first row of sheet
         initializeDayList(wkSheet);

         // For every row in the sheet starting from the third row after seats per day
         for (int row = 2; row < numRows - 1; row++)
         {
            xlrow = wkSheet.getRow(row);
            if (xlrow != null)
            {
               parseSchool(xlrow, numCols);
            }
            schoolCount++;
         }

         //Sort the list by priority
         Collections.sort(model.schoolList, new Comparator<School>() {
            @Override
            public int compare(School s1, School s2) {
               return Double.compare(s2.priority, s1.priority);
            }
         });

         //Temp # of avail seats
         model.notifyText = "Total # of Schools: " + "<b>" + schoolCount + "</b>" + "<br/>Total # of Available Seats: " + "<b>" + totalSeats + "</b>"
                 + "<br/>Total # of Students: " + "<b>" + totalStudentCount + "</b>";
         ;
         model.notify(LogicModel.NotifyCmd.TEXT);

         //DEBUG
//<editor-fold defaultstate="collapsed" desc="DEBUG Print Priority/Name">
         /*for (School s : model.schoolList) {
                System.out.printf("%6.2f | %s\n", s.priority, s.name);
            }*/
//</editor-fold>
      } catch (Exception e)
      {
         throw new InvalidFormatException(e.toString());
      }
   }

   /**
    * Initialize all the days that are offered.
    *
    * @param xlRow The row with the days.
    */
   private void initializeDayList(XSSFSheet sheet) throws InvalidFormatException {
      //Count of days
      int dayCount = 0;
      //The current cell
      XSSFCell cell;
      //The row
      XSSFRow xlRow = sheet.getRow(0);
      //Value of the entire cell
      String cellStr;
      //The date extracted from the cell in mm/dd format
      String cellDate;
      //Array of month/day
      String[] dateArr;
      //The begin date
      int beginDate;
      //The max students for that school
      int maxStudents;

      try
      {
         beginDate = dateStart;
         totalSeats = 0;
         //FOR each date in the sheet
         while (beginDate <= dateEnd)
         {
            cell = xlRow.getCell(beginDate);
            cellStr = cell.toString();
            cellDate = cellStr.substring(cellStr.indexOf(" "), cellStr.length()).trim();
            dateArr = cellDate.split("/");
            if (dateArr.length != 2)
            {
               model.notifyText = "Bad cell format: Sheet: " + xlRow.getSheet().getSheetName()
                       + "| Cell(" + xlRow.getRowNum() + ", " + beginDate + ")";
               model.notify(LogicModel.NotifyCmd.TEXT);
               break;
            }
            else
            {
               maxStudents = Double.valueOf(sheet.getRow(1).getCell(beginDate).toString()).intValue();
               Day newDay = new Day(++dayCount, maxStudents); 
               totalSeats += newDay.seatsLeft;
               //Get the month, subtract 1 because index starts at 0
               int month = Integer.valueOf(cellDate.split("/")[0]) - 1;
               //Get the day
               int day = Integer.valueOf(cellDate.split("/")[1]);
               //Get the current year
               int year = Calendar.getInstance().get(Calendar.YEAR);
               //Set the date for the day
               newDay.date.set(year, month, day);
               //Add date to the map
               model.dayList.put(dayCount, newDay);
            }
            beginDate++;
         }
      } catch (Exception e)
      {
         throw new InvalidFormatException(e.toString());
      }

      //Set total number of days
      model.TotalDays = dayCount;
   }

   protected void writeXLFile(String outputFile) throws IOException {
      XSSFWorkbook wb = new XSSFWorkbook();
      XSSFSheet sheet = wb.createSheet("Main Schedule");
      XSSFSheet sheet2 = wb.createSheet("Remaining Seats");
      XSSFSheet sheet3 = wb.createSheet("Unscheduled Schools");

      ArrayList<FinalDay> schedule = new ArrayList<>(model.finalSchedule);
      ArrayList<School> unscheduled = new ArrayList<>(model.finalUnscheduled);

      XSSFRow row, row2, row3;
      int count = 0, countf = 0, countU = 0;

      sheet.setColumnWidth(0, 45 * 256);
      sheet.setColumnWidth(1, 10 * 256);
      sheet.setColumnWidth(2, 20 * 256);

      sheet2.setColumnWidth(0, 20 * 256);
      sheet2.setColumnWidth(1, 10 * 256);

      sheet3.setColumnWidth(0, 8 * 256);
      sheet3.setColumnWidth(1, 45 * 256);
      sheet3.setColumnWidth(2, 10 * 256);

      row = sheet.createRow(count++);
      row.createCell(0).setCellValue("School");
      row.createCell(1).setCellValue("Seats");
      row.createCell(2).setCellValue("Date");

      row2 = sheet2.createRow(countf++);
      row2.createCell(0).setCellValue("Date");
      row2.createCell(1).setCellValue("Seats Left");

      row3 = sheet3.createRow(countU++);
      row3.createCell(0).setCellValue("Priority");
      row3.createCell(1).setCellValue("School Name");
      row3.createCell(2).setCellValue("Seats");

      for (FinalDay day : schedule)
      {
         for (School school : day.getSchools())
         {
            row = sheet.createRow(count++);
            row.createCell(0).setCellValue(school.name);
            row.createCell(1).setCellValue(school.getNumStudents());
            row.createCell(2).setCellValue(day.toString());
         }
         row2 = sheet2.createRow(countf++);
         row2.createCell(0).setCellValue(day.toString());
         row2.createCell(1).setCellValue(day.seatsLeft);
      }

      countf++;
      row2 = sheet2.createRow(countf++);
      row2.createCell(0).setCellValue("Total Seated");
      row2.createCell(1).setCellValue(model.totalSeated);
      row2 = sheet2.createRow(countf++);
      row2.createCell(0).setCellValue("Total Schools");
      row2.createCell(1).setCellValue(model.totalSchools);

      //Write unscheduled schools
      for (School school : unscheduled)
      {
         row3 = sheet3.createRow(countU++);
         row3.createCell(0).setCellValue(500 - school.priority);
         row3.createCell(1).setCellValue(school.name);
         row3.createCell(2).setCellValue(school.totalNumStudents);
      }

      // Write the output to a file
      FileOutputStream fileOut = new FileOutputStream(outputFile);
      wb.write(fileOut);
      fileOut.close();
      wb.close();
   }

   private void parseSchool(XSSFRow xlRow, int totalCols) {
      XSSFCell cell;
      School school = new School(LogicModel.schoolId++);
      int dayCount = 1;

      // For every column in the row
      for (int col = 0; col < totalCols; col++)
      {
         cell = xlRow.getCell(col);
         if (cell != null)
         {
            //SWITCH over each column
            switch (col)
            {
               //Priority
               case 0:
                  //Subtract from 100 to reorder priority. Lowest value is now biggest value/priority.
                  school.priority = 500.0 - Double.valueOf(cell.toString());
                  break;

               //School Name
               case 1:
                  school.name = cell.toString();
                  break;

               //Previously visited
               case 2:
                  if (cell.toString().toLowerCase().contains("no"))
                  {
                     school.visited = false;
                  }
                  break;

               //Grade levels
               case 3:
                  //Ignoring grade levels
                  break;

               //Total num students
               case 4:
                  school.numStudents = new Double(cell.getNumericCellValue()).intValue();
                  school.totalNumStudents = new Double(cell.getNumericCellValue()).intValue();
                  totalStudentCount += school.totalNumStudents;
                  break;

               //Split
               case 5:
                  if (new Double(cell.getNumericCellValue()).intValue() == 1)
                  {
                     school.split = true;
                  }
                  break;

               //Split
               case 6:
                  for (String num : cell.toString().split(","))
                  {
                     if (!num.equals(""))
                     {
                        school.splitNums.add(Double.valueOf(num.trim()).intValue());
                     }
                  }
                  break;
               //Check available dates. Cols 7 - 33 inclusive
               default:
                  if ((col >= dateStart && col <= dateEnd) && !cell.toString().trim().equals("")
                          && (cell.toString().toLowerCase().equals("y")
                          || cell.toString().equals("1")))
                  {
                     school.addDay(model.dayList.get(dayCount));
                  }
                  dayCount++;
                  break;
            }
         }
      }

      //Add school to needAdds
      if (needAddCounter < topPriority)
      {

         model.toAdd.add(school);
         needAddCounter++;
      }

      model.schoolList.add(school);
   }

   /**
    * Convert column name to number where A = 0
    *
    * @param name The name of the column
    * @return The column number
    */
   public static int convertColumn(String name) {
      int number = 0;
      for (int i = 0; i < name.length(); i++)
      {
         number = number * 26 + (name.charAt(i) - ('A' - 1));
      }
      return number - 1;
   }

}
