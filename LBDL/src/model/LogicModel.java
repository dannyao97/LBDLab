package model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.*;

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
   protected static HashMap<Integer, Day> dayList;
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
    * (NOT OPTIMAL) Fills up first available day with schools and 
    * then moves onto the next day.
    *
    * @pre schoolList is already sorted by priority.
    */
   public void fastAlgorithm()
   {
      HashMap<Integer, Day> days = new HashMap<>(dayList);
      ListIterator iter;
      boolean scheduled;
      Day curDay;
      Day curSchoolDay;
      int totalStudents = 0;
      int totalSchools = 0;

      //FOR EACH school in the SchoolList
      for (School curSchool : schoolList)
      {
         scheduled = false;
         iter = curSchool.availDates.listIterator();

         //WHILE school still has available days and hasn't been scheduled
         while (iter.hasNext() && !scheduled)
         {
            curSchoolDay = (Day) iter.next();
            curDay = days.get(curSchoolDay.index);

            //IF day has enough seats remaining, add school
            if (curSchool.numStudents <= curDay.getSeats())
            {
               curDay.addSchool(curSchool);
               curSchool.actualDay = curDay.date;
               scheduled = true;
               totalStudents += curSchool.numStudents;
               totalSchools += 1;
            }
         }
      }

      SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
      for (School current : schoolList)
      {
         if (current.actualDay != null)
         {
            String date = formatter.format(current.actualDay.getTime());
            System.out.println(current.name + "  :  " + date);
         }
      }
      System.out.println("TOTAL SEATED: " + totalStudents);
      System.out.println("TOTAL SCHOOL: " + totalSchools);
   }

   /**
    * Returns the list of schools.
    *
    * @return The list of schools.
    */
   public ArrayList<School> getSchoolList()
   {
      return schoolList;
   }
}
