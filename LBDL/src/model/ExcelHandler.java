package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CreationHelper;
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
     * A map for checking split schools
     */
    //private HashMap<Integer, School> splitSchools;
    /**
     * Smallest number of students encountered
     */
    private int smallest = Integer.MAX_VALUE;
    /**
     * Total num students read
     */
    //private int totalStudents;
    /**
     * School id
     */
    private int id;

    public ExcelHandler(LogicModel model) {
        this.model = model;
        //this.totalStudents = 0;
        this.id = 0;
    }

    /**
     * Read the Excel file.
     *
     * @param filename The name of the file.
     * @throws FileNotFoundException If the file is invalid.
     * @throws IOException If unable to read from file.
     * @throws InvalidFormatException If file is malformed.
     */
    protected void readXLFile(String filename) throws InvalidFormatException {
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new File(filename));
            XSSFSheet wkSheet = wb.getSheetAt(0);
            XSSFRow xlrow;
            int numRows; // Num of rows
            numRows = wkSheet.getPhysicalNumberOfRows();
            int numCols = 0; // Num of columns
            int tmp = 0;

            model.resetModel();
            // This ensures that we get the data properly even if it doesn't start
            // from first few rows
            for (int i = 0; i < 10 || i < numRows; i++) {
                xlrow = wkSheet.getRow(i);
                if (xlrow != null) {
                    tmp = wkSheet.getRow(i).getPhysicalNumberOfCells();
                    if (tmp > numCols) {
                        numCols = tmp;
                    }
                }
            }
            //Initialize available days from first row of sheet
            initializeDayList(wkSheet.getRow(0));

            // For every row in the sheet starting from the second row
            for (int row = 1; row < numRows; row++) {
                xlrow = wkSheet.getRow(row);
                if (xlrow != null) {
                    //parseSchool(xlrow, numCols);
                    parseAltSchool(xlrow, numCols);  //ALTERNATE METHOD OF READING EXCEL
                }
            }

            //Sort the list by priority
            Collections.sort(model.schoolList, new Comparator<School>() {
                @Override
                public int compare(School s1, School s2) {
                    return Double.compare(s2.priority, s1.priority);
                }
            });

            //DEBUG
//<editor-fold defaultstate="collapsed" desc="DEBUG Print Priority/Name">
            /*for (School s : model.schoolList) {
                System.out.printf("%6.2f | %s\n", s.priority, s.name);
            }*/
//</editor-fold>
        } catch (Exception e) {
            throw new InvalidFormatException(e.toString());
        }
    }

    /**
     * Reads and stores each school from each row.
     *
     * @param xlRow The row to use.
     * @param totalSchools The total number of schools.
     */
    /*private void parseSchool(XSSFRow xlRow, int totalSchools)
   {
      XSSFCell cell;
      School school = new School(this.id++);
      int dayCount = 1;
      School exist = null;
      
      // For every column in the row
      for (int col = 0; col < totalSchools; col++)
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
                  exist = checkExist(school.priority);
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
               //Total from dup schools
               case 4:
                  //Ignoring total from duplicate schools
                  break;
                  
               //Number of students
               case 5:
                  school.numStudents = new Double(cell.getNumericCellValue()).intValue();
                  break;
                  
               //Extraneous split
               case 6:
                  break;
                  
                //Split
               case 7:
                  if (new Double(cell.getNumericCellValue()).intValue() == 1)
                  {
                     school.split = true;
                  }
                  break;
                  
                //Split numbers
               case 8:
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
                  
               //Check available dates. Cols 9 - 35 inclusive
               default:
                  if ((col > 8 && col < 36) && !cell.toString().equals("") &&
                     (Double.valueOf(cell.getNumericCellValue()).intValue() == 1))
                  {
                     school.addDay(model.dayList.get(dayCount));
                  }
                  dayCount++;
                  break;
            }
         }
      }
      
      if (exist != null)
      {
         exist.addNewSplitSchool(school);
      }
      else
      {
         model.schoolList.add(school);
         checkSmallest(school);
      }
   }*/
    /**
     * Set the smallest school size.
     *
     * @param school The school to check.
     */
    /*private void checkSmallest(School school)
   {
      if (school.numStudents < smallest)
      {
         smallest = school.numStudents;
         model.smallestSchool = school;
         System.out.println("smallest is : " + school.numStudents + "  " + school.name);
      }
   }*/
    /**
     * Check if the current school has the same priority as another already
     * added school. If yes, then school must've been split.
     *
     * @param chkPrior The priority of the current school to check.
     * @return The already existing school
     */
    /*private School checkExist(double chkPrior)
   {
      for (School existing : model.schoolList)
      {
         if (Math.abs(chkPrior - existing.priority) < .01)
         {
            return existing;
         }
      }
      return null;
   }*/
    /**
     * Initialize all the days that are offered.
     *
     * @param xlRow The row with the days.
     */
    private void initializeDayList(XSSFRow xlRow) throws InvalidFormatException {
        //First date starts at column 9
        int index = 7;
        //Count of days
        int dayCount = 0;
        //HARDCODED 27 dates. Last date col = 7 + 27
        int numDates = 34;
        //The current cell
        XSSFCell cell;
        //Value of the entire cell
        String cellStr;
        //The date extracted from the cell in mm/dd format
        String cellDate;
        //Array of month/day
        String[] dateArr;

        try {
            //FOR each date in the sheet
            for (index = 7; index < numDates; index++) {
                cell = xlRow.getCell(index);
                cellStr = cell.toString();
                cellDate = cellStr.substring(cellStr.indexOf(" "), cellStr.length()).trim();
                dateArr = cellDate.split("/");
                if (dateArr.length != 2) {
                    model.notifyText = "Bad cell format: Sheet: " + xlRow.getSheet().getSheetName()
                            + "| Cell(" + xlRow.getRowNum() + ", " + index + ")";
                    model.notify(LogicModel.NotifyCmd.TEXT);
                    break;
                } else {
                    Day newDay = new Day(++dayCount);
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
            }
        } catch (Exception e) {
            throw new InvalidFormatException(e.toString());
        }
    }

    protected void writeXLFile(String outputFile) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Main Schedule");
        XSSFSheet sheet2 = wb.createSheet("Remaining Seats");

        ArrayList<Day> schedule = new ArrayList<>(model.getMainSchedule().values());

        XSSFRow row, row2;
        int count = 0, countf = 0;

        sheet.setColumnWidth(0, 45 * 256);
        sheet.setColumnWidth(1, 10 * 256);
        sheet.setColumnWidth(2, 20 * 256);

        sheet2.setColumnWidth(0, 20 * 256);
        sheet2.setColumnWidth(1, 10 * 256);

        row = sheet.createRow(count++);
        row.createCell(0).setCellValue("School");
        row.createCell(1).setCellValue("Seats");
        row.createCell(2).setCellValue("Date");

        row2 = sheet2.createRow(countf++);
        row2.createCell(0).setCellValue("Date");
        row2.createCell(1).setCellValue("Seats Left");

        for (Day day : schedule) {
            for (School school : day.getSchools()) {
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

        // Write the output to a file
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        wb.write(fileOut);
        fileOut.close();
        wb.close();
    }

    private void parseAltSchool(XSSFRow xlRow, int totalSchools) {
        XSSFCell cell;
        School school = new School(this.id++);
        int dayCount = 1;

        // For every column in the row
        for (int col = 0; col < totalSchools; col++) {
            cell = xlRow.getCell(col);
            if (cell != null) {
                //SWITCH over each column
                switch (col) {
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
                        if (cell.toString().toLowerCase().contains("no")) {
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
                        break;

                    //Split
                    case 5:
                        if (new Double(cell.getNumericCellValue()).intValue() == 1) {
                            school.split = true;
                        }
                        break;

                    //Split
                    case 6:
                        for (String num : cell.toString().split(",")) {
                            if (!num.equals("")) {
                                school.splitNums.add(Double.valueOf(num.trim()).intValue());
                            }
                        }
                        break;

                    case 34: //Spring break
                        break;
                    case 35: //Last day of school
                        break;
                    case 36: //Comments
                        school.comments = cell.getStringCellValue();
                        break;

                    //Check available dates. Cols 7 - 333 inclusive
                    default:
                        if ((col > 6 && col < 34) && !cell.toString().equals("")
                                && (Double.valueOf(cell.getNumericCellValue()).intValue() == 1)) {
                            school.addDay(model.dayList.get(dayCount));
                        }
                        dayCount++;
                        break;
                }
            }
        }

        model.schoolList.add(school);
    }

}
