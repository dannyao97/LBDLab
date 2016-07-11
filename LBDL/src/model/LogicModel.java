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
   protected HashMap<Integer, Day> dayList;
   /** A list of all the schools */
   protected ArrayList<School> schoolList;
   /** An object to perform operations on the excel file */
   private final ExcelHandler xlHandler;
   /** The total number of days */
   public final int TotalDays = 27;
   /** The total number of students per day */
   public final int TotalKids = 110;

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
      
      //DEBUG LINE
      ArrayList<School> unAdded = new ArrayList<>();

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
         
         //DEBUG
         if (!scheduled)
         {
            unAdded.add(curSchool);
         }
      }

      //DEBUG PORTION
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
      
      System.out.println("\n-----REMAINING DAYS-----");
      for (Day d : days.values())
      {
         String date = formatter.format(d.date.getTime());
         System.out.println(date + "  :  " + d.getSeats());
      }
      
      System.out.println("\n-----UNADDED-----");
      for (School s : unAdded)
      {
         System.out.println(s.name + "  :  " + s.numStudents);
      }
   }

   /**
    * Generates schedules using the knapsack algorithm.
    */
   public void knapsack()
   {
      ArrayList<Integer> order = randOrder(1);
      ArrayList<ArrayList<School>> eachDay = listEachDay();
      Double[][] dynTable;
      int numItems;
      //s**** = property of current school
      //sIndex = school, sWeight = # kids per school, sValue = priority
      int item, weight, sWeight, sIndex;
      double sValue, newValue, prevValue;
      
      //FOR EACH list of schools in each day
      //for (ArrayList arrList : eachDay)
      //{
      ArrayList<School> availSchools = eachDay.get(0);   //List of available schools for this day
      numItems = availSchools.size();
      dynTable = initializeDynTable(numItems);
      
      //FOR all items starting at 1 (inclusive) and school in list starting at 0
      for (item = 1, sIndex = 0; item <= numItems; item++, sIndex++)
      {
         sWeight = availSchools.get(sIndex).numStudents;
         sValue = availSchools.get(sIndex).priority;
         
         //FOR all weights
         for (weight = 1; weight <= TotalKids; weight++)
         {
            //IF item can be part of solution
            if (sWeight <= weight)
            {
               newValue = dynTable[item - 1][weight - sWeight] - sValue;
               prevValue = dynTable[item - 1][weight];
               if (newValue > prevValue)
               {
                  dynTable[item][weight] = newValue;
               }
               else
               {
                  dynTable[item][weight] = prevValue;
               }
            }
            else
            {
               dynTable[item][weight] = dynTable[item - 1][weight];
            }
         }
      }
      
      //DEBUG
      System.out.print(".........");
      for (int k = 0; k <= TotalKids; k++)
      {
         System.out.printf("%7f|", new Double(k));
      }
      System.out.println();
      for (int i = 0; i <= numItems; i++)
      {
         System.out.printf("ROW %2d:  \n", i);
         if (i < numItems)
            System.out.println("SCHOOL: " + availSchools.get(i).name);
         for (int j = 0; j <= TotalKids; j++)
         {
            System.out.printf("%7f|", dynTable[i][j]);
         }
         System.out.println();
      }
      chooseSchedule(dynTable, availSchools, numItems);
         
      //}
   }
   
   private ArrayList<School> chooseSchedule(Double[][] dynTable, ArrayList<School> availSchools, int numItems)
   {
      ArrayList<School> schedule = new ArrayList<>();
      School selected;
      int weight = TotalKids;
      
      //WHILE weight & items both > 0
      while(numItems > 0 && weight > 0)
      {
         //IF values are not equal with epsilon .01 Compare doubles to 2nd decimal
         if (Math.abs(Math.abs(dynTable[numItems][weight]) - Math.abs(dynTable[numItems - 1][weight])) >= .01)
         {
            selected = availSchools.get(numItems - 1);
            weight -= selected.numStudents;
            schedule.add(selected);
         }
         numItems--;         
      }
      
      return schedule;
   }
   
   /**
    * Initializes the dynamic table. The entire row 0 and the entire column 0 is 
    * filled with -100's. -100 is the lowest value.
    * 
    * @param items The total number of items. (y-axis)
    * 
    * @return A table to hold weights
    */
   private Double[][] initializeDynTable(int items)
   {
      //Add 1 because 10 items should go from 0 - 10 inclusive
      Double[][] table = new Double[items + 1][TotalKids + 1];
      int i;
      
      //Set top row to 0's
      for (i = 0; i <= TotalKids; i++)
      {
         table[0][i] = -100.0;
      }
      //Set first column to 0's
      for (i = 0; i <= items; i++)
      {
         table[i][0] = -100.0;
      }
      return table;
   }
   
   //Generates a list of available schools for each day. A school can be in 
   //multiple days.
   private ArrayList<ArrayList<School>> listEachDay()
   {
      ArrayList<ArrayList<School>> arr = new ArrayList<>();
      ArrayList<School> listSchools;
      
      for (Day day : dayList.values())
      {
         listSchools = new ArrayList<>();
         for (School school : schoolList)
         {
            if (school.availDates.contains(day))
            {
               listSchools.add(school);
            }
         }
         arr.add(listSchools);
      }
      return arr;
   }
   
   //Returns a random order in which the knapsacks will be filled.
   private ArrayList<Integer> randOrder(int seed)
   {
      ArrayList<Integer> arr = new ArrayList<>();
      Random rand = new Random(seed);
      int randNum;
      boolean randExists;
      
      for (int i = 0; i < TotalDays; i++)
      {
         randExists = true;
         do
         {
            randNum = rand.nextInt(TotalDays) + 1;
            if (!arr.contains(randNum))
            {
               arr.add(randNum);
               randExists = false;
            }
         } while(randExists);
      }
      return arr;
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
