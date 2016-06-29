package model;

import java.io.*;
import java.util.Observable;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Logic model performs logic and notifies the GUI.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class LogicModel extends Observable
{
   /** The map of available days. Column index is the key */
   protected HashMap<Integer, Day> dayList;
   /** A list of all the schools */
   protected ArrayList<School> schoolList;
   /** An object to perform operations on the excel file */
   private final ExcelHandler xlHandler;

   public LogicModel()
   {
      dayList = new HashMap<>();
      schoolList = new ArrayList<>();
      xlHandler = new ExcelHandler(this);
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
         xlHandler.readXLFile(filename);
      }
      catch (IOException | InvalidFormatException e)
      {
         notify("Error: Could not open file.");
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
         xlHandler.writeXLFile("testOutput.xlsx");
      }
      catch (IOException e)
      {
         notify("Error: Could not write to file.");
      }
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
