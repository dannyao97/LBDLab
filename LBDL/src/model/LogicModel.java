package model;

import java.io.*;
import java.util.Observable;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CreationHelper;

/**
 * Logic model performs logic and notifies the GUI.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class LogicModel extends Observable
{
   /** The workbook that is read */
   XSSFWorkbook wb;
   /** The current excel spreadsheet */
   XSSFSheet sheet;
   /** The map of available days. Column index is the key */
   protected HashMap<Integer, Day> dayList;
   /** A list of all the schools */
   protected ArrayList<School> schoolList;

   public LogicModel()
   {
      dayList = new HashMap<>();
      schoolList = new ArrayList<>();
   }

   /**
    * Notifies the gui with the given object.
    *
    * @param obj Object to give the gui
    */
   public void notify(Object obj)
   {
      setChanged();
      notifyObservers(obj);
   }

   /**
    * Read the provided excel file.
    *
    * @param filename The name of the file.
    */
   public void readExcelFile(String filename)
   {
      try
      {
         readXLFile(filename);
      }
      catch (IOException | InvalidFormatException e)
      {
         notify("Error: Could not open file.");
      }
   }

   private void readXLFile(String filename) throws FileNotFoundException, IOException, InvalidFormatException
   {
      XSSFWorkbook wb = new XSSFWorkbook(new File(filename));
      XSSFSheet wkSheet = wb.getSheetAt(0);
      XSSFRow xlrow;
      int numRows; // Num of rows
      numRows = wkSheet.getPhysicalNumberOfRows();
      int numCols = 0; // Num of columns
      int tmp = 0;

      // This ensures that we get the data properly even if it doesn't start
      // from first few rows
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
      initializeDayList(wkSheet.getRow(0), numCols);

      // For every row in the sheet starting from the second row
      for (int row = 1; row < numRows; row++)
      {
         xlrow = wkSheet.getRow(row);
         if (xlrow != null)
         {
            parseSchool(xlrow, numCols);
         }
      }
      wb.close();
   }

   /**
    * Reads and stores each school from each row.
    *
    * @param xlRow The row to use.
    * @param totalSchools The total number of schools.
    */
   private void parseSchool(XSSFRow xlRow, int totalSchools)
   {
      XSSFCell cell;
      School school = new School();
      // For every column in the row
      for (int col = 0; col < totalSchools; col++)
      {
         cell = xlRow.getCell(col);
         if (cell != null)
         {
            //SWITCH over each column
            switch (col)
            {
               case 0:  //Priority
                  school.priority = Double.valueOf(cell.toString());
                  break;
               case 1:  //School Name
                  school.name = cell.toString();
                  break;
               case 2:  //Previously visited
                  if (cell.toString().toLowerCase().contains("no"))
                  {
                     school.visited = false;
                  }
                  break;
               case 3:  //Grade levels
                  //Ignoring grade levels
                  break;
               case 4:  //Total from dup schools
                  //Ignoring total from duplicate schools
                  break;
               case 5:  //Number of students
                  school.numStudents = new Double(cell.getNumericCellValue()).intValue();
                  break;
               case 6:  //Extraneous split
                  break;
               case 7:  //Split
                  if (new Double(cell.getNumericCellValue()).intValue() == 1)
                  {
                     school.split = true;
                  }
                  break;
               case 8:  //Split numbers
                  for (String num : cell.toString().split(","))
                  {
                     if (!num.equals(""))
                     {
                        school.splitNums.add(Double.valueOf(num).intValue());
                     }
                  }
                  break;
               case 36: //Spring break
                  break;
               case 37: //Last day of school
                  break;
               case 38: //Comments
                  school.comments = cell.getStringCellValue();
                  break;
               default: //Check available dates. Cols 9 - 35 inclusive
                  if ((col > 8 && col < 36) && !cell.toString().equals("") &&
                     (Double.valueOf(cell.getNumericCellValue()).intValue() == 1))
                  {
                     school.addDay(dayList.get(col));
                  }
                  break;
            }
         }
      }
      schoolList.add(school);
   }

   /**
    * Initialize all the days that are offered.
    *
    * @param xlRow The row with the days.
    * @param numCols The total number of columns
    */
   private void initializeDayList(XSSFRow xlRow, int numCols)
   {
      //First date starts at column 9
      int index = 9;
      //HARDCODED 27 dates. Last date col = 9 + 27
      int numDates = 36;
      //The current cell
      XSSFCell cell;
      //Value of the entire cell
      String cellStr;
      //The date extracted from the cell in mm/dd format
      String cellDate;
      //Array of month/day
      String[] dateArr;

      //FOR each date in the sheet
      for (index = 9; index < numDates; index++)
      {
         cell = xlRow.getCell(index);
         cellStr = cell.toString();
         cellDate = cellStr.substring(cellStr.indexOf(" "), cellStr.length()).trim();
         dateArr = cellDate.split("/");
         if (dateArr.length != 2)
         {
            notify("Bad cell format: Sheet: " + xlRow.getSheet().getSheetName()
                    + "| Cell(" + xlRow.getRowNum() + ", " + index + ")");
            break;
         }
         else
         {
            Day newDay = new Day(index);
            //Get the month, subtract 1 because index starts at 0
            int month = Integer.valueOf(cellDate.split("/")[0]) - 1;
            //Get the day
            int day = Integer.valueOf(cellDate.split("/")[1]);
            //Get the current year
            int year = Calendar.getInstance().get(Calendar.YEAR);
            //Set the date for the day
            newDay.date.set(year, month, day);
            //Add date to the map
            dayList.put(index, newDay);
         }
      }
   }

   /**
    * NOT IMPLEMENTED YET Write output to the specified file.
    *
    * @param outputFile The name of the output file.
    */
   public void writeExcelFile(String outputFile)
   {
      try
      {
         writeXLFile("testOutput.xlsx");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void writeXLFile(String outputFile) throws IOException
   {
      XSSFWorkbook wb = new XSSFWorkbook();
      CreationHelper createHelper = wb.getCreationHelper();
      XSSFSheet sheet = wb.createSheet("Sheet_1");

      // Create a row and put some cells in it. Rows are 0 based.
      XSSFRow row = sheet.createRow(0);

      // Or do it on one line.
      row.createCell(0).setCellValue(1.2);
      row.createCell(1).setCellValue(createHelper.createRichTextString("This is a string"));
      row.createCell(2).setCellValue(true);

      // Write the output to a file
      FileOutputStream fileOut = new FileOutputStream(outputFile);
      wb.write(fileOut);
      fileOut.close();
      wb.close();
   }

   /**
    * Returns the list of schools.
    * @return The list of schools.
    */
   public ArrayList<School> getSchoolList()
   {
      return schoolList;
   }
}
