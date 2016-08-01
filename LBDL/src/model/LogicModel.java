package model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.*;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Logic model performs all scheduling logic and notifies the GUI.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class LogicModel extends Observable
{
   /** The map of available days. Column index is the key */
   protected HashMap<Integer, Day> dayList;
   /** A list of all the schools */
   static protected ArrayList<School> schoolList;
   /** A list of schools that MUST be added */
   protected ArrayList<School> mustAdd;
   /** An object to perform operations on the excel file */
   private final ExcelHandler xlHandler;
   /** The total number of days */
   public final int TotalDays = 27;
   /** The total number of students per day */
   public final int TotalKids = 110;
   /** A list of schools that can make each day, key=day index */
   protected HashMap<Integer, Day> mainSchedule;
   /** The smallest school size */
   protected School smallestSchool;
   /** The number of students seated in total */
   private int seated;
   /** The temporary number of seated students */
   private int tempSeated;
   /** The total number of schools scheduled */
   private int seatedSchools;
   /** Text to notify the GUI */
   public String notifyText;

   public LogicModel()
   {
      dayList = new HashMap<>();
      schoolList = new ArrayList<>();
      mustAdd = new ArrayList<>();
      mainSchedule = new HashMap<>();
      xlHandler = new ExcelHandler(this);
      seated = 0;
      seatedSchools = 0;
   }

   public enum NotifyCmd
   {
      TEXT, LIST;
   }

   /**
    * Notifies the gui with the given object.
    *
    * @param cmd The command to perform - 1 : Notify Text - 2 : Update List
    */
   public void notify(NotifyCmd cmd)
   {
      setChanged();
      notifyObservers(cmd);
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
         notifyText = "Error: Could not open file.";
         notify(NotifyCmd.TEXT);
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
         notifyText = "Error: Could not write to file.";
         notify(NotifyCmd.TEXT);
      }
   }

   /**
    * (NOT OPTIMAL) Fills up first available day with schools and then moves
    * onto the next day.
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
      seated = 0;
      seatedSchools = 0;

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
               seated += curSchool.numStudents;
               seatedSchools += 1;
            }
         }

         //DEBUG
         if (!scheduled)
         {
            unAdded.add(curSchool);
         }
      }

      //DEBUG PORTION
//<editor-fold defaultstate="collapsed" desc="DEBUG Brute Force">
      SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");
      for (School current : schoolList)
      {
         if (current.actualDay != null)
         {
            String date = formatter.format(current.actualDay.getTime());
            System.out.println(current.name + "  :  " + date);
         }
      }
      System.out.println("TOTAL SEATED: " + seated);
      System.out.println("TOTAL SCHOOL: " + seatedSchools);

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

      notifyText = "<br>-Seated Students: " + seated + "<br>-Schools: " + seatedSchools;
      notify(NotifyCmd.TEXT);
//</editor-fold>
   }

   /**
    * Copies all keys and values from hash into a new HashMap
    *
    * @param hash
    * @return A new HashMap with duplicate values.
    */
   private HashMap cloneHashMap(HashMap hash)
   {
      HashMap newHash = new HashMap();

      for (Object key : hash.keySet())
      {
         newHash.put(key, hash.get(key));
      }
      return newHash;
   }

   /**
    * Generates schedules using the knapsack algorithm.
    */
   public void knapsack()
   {
      //The order in which to fill the days
      ArrayList<Integer> order;
      //A list of available schools for each Day
      ArrayList<ArrayList<School>> eachDay = listEachDay();
      //A temporary list of available schools for each day. 
      //Copies eachDay array for each iteration
      ArrayList<ArrayList<School>> tempEachDay = new ArrayList<>();
      //A temporary list of schools available for a certain day
      ArrayList<School> tempDayList;
      //A temporary map of the days
      HashMap<Integer, Day> dayMap = new HashMap<>();
      //The dynamic table to represent a day.
      Double[][] dynTable;
      //Total items in the dynTable (based on schools available for a day
      int numWeights;

      //RUN 100 iterations to find most seated students
      for (int iter = 0; iter < 1000; iter++)
      {
         //Initialize variables before calculations
         order = randOrder(iter);
         //Clear the scheduled schools for each day
         for (Day newDay : dayList.values())
         {
            newDay.clearSchools();
         }
         //Clear dayMap
         dayMap.clear();
         //Clear mustAdd
         mustAdd.clear();
         //Copy eachDay into a temporary arraylist tempEachDay
         tempEachDay.clear();
         for (ArrayList<School> arr : eachDay)
         {
            tempDayList = new ArrayList<>();
            for (School sch : arr)
            {
               tempDayList.add(sch);
            }
            tempEachDay.add(tempDayList);
         }
         tempSeated = 0;
         seatedSchools = 0;
         //END Initialization


         //FOR EACH integer in order (The order in which to fill each day)
         for (int ord : order)
         {
            //IF Mustadds are not empty
            if (!mustAdd.isEmpty())
            {
               scheduleMustAdds(iter, dayMap);
            }
            
            ArrayList<School> availSchools = tempEachDay.get(ord - 1);   //List of available schools for this day
            numWeights = dayList.get(ord).getSeats();
            dynTable = initializeDynTable(availSchools.size(), numWeights);

            fillTable(dynTable, availSchools, numWeights);

            //DEBUG
//<editor-fold defaultstate="collapsed" desc="DEBUG Print DynTable">
/*System.out.print(".........");
for (int k = 0; k <= TotalKids; k++)
{
System.out.printf("%7.2f|", new Double(k));
}
System.out.println();
for (int i = 0; i <= numItems; i++)
{
System.out.printf("ROW %2d:  \n", i);
if (i < numItems)
{
//System.out.println("SCHOOL: " + availSchools.get(i).name);
}
for (int j = 0; j <= TotalKids; j++)
{
System.out.printf("%7.2f|", dynTable[i][j]);
}
System.out.println();
}*/
//</editor-fold>
            //Add day schedule to the main schedule
            dayMap.put(ord, chooseSchedule(dynTable, availSchools, dayList.get(ord)));

            //Remove added schools from overall list
            for (School addedSchool : dayMap.get(ord).getSchools())
            {
               //FOR each arraylist in the lists for each day
               for (ArrayList eachDayList : tempEachDay)
               {
                  //IF list contains school, remove it.
                  if (eachDayList.contains(addedSchool))
                  {
                     //System.out.println("REMOVED: " + addedSchool.name);
                     eachDayList.remove(addedSchool);
                     //check if there is a new smallest school
                     updateSmallest(addedSchool, tempEachDay);
                  }
               }
            }
         }
         System.out.println("ITER: " + iter);
         //IF new schedule seated more students
         if (tempSeated > seated)
         {
            seated = tempSeated;
            mainSchedule = cloneHashMap(dayMap);
         }
      }
      //DEBUG
//<editor-fold defaultstate="collapsed" desc="DEBUG Print Day Schedule">
      System.out.println("print schedule");
      int count = 0;
      for (Day d : mainSchedule.values())
      {
         System.out.printf("\n%s\n", mainSchedule.get(++count).toString());
         for (School schSchool : d.getSchools())
         {
            System.out.printf("  -%41s  %3d students\n", schSchool.name, schSchool.numStudents);
         }
      }

      for (Day day : mainSchedule.values())
      {
         System.out.println(day.toString() + "   seatsleft: " + day.getSeats());
      }
//System.out.printf("ITER: %3d | SEATED: %d\n", iter, tempSeated);
//System.out.println("ITER: " + iter);
//</editor-fold>

      System.out.println("mustAdd Size: " + mustAdd.size());
      for (School debug: mustAdd)
      {
         System.out.println(debug.name + "   " + debug.numStudents);
      }
      System.out.println("END.");
      notifyText = "<br>-Seated Students: " + seated + "<br>-Schools: " + seatedSchools + "<br>-Smallest School size: " + smallestSchool.numStudents;
      notify(NotifyCmd.TEXT);
      notify(NotifyCmd.LIST);
   }

   private void updateSmallest(School curSchool, ArrayList<ArrayList<School>> unscheduled)
   {
      int tempSmallestNum = Integer.MAX_VALUE;
      
      //Find next smallest unscheduled school
      if (curSchool.equals(smallestSchool))
      {
         for (ArrayList<School> listofschools: unscheduled)
         {
            for (School newSmall: listofschools)
            {
               if (newSmall.numStudents < tempSmallestNum)
               {
                  tempSmallestNum = newSmall.numStudents;
                  smallestSchool = newSmall;
               }
            }
         }
      }
   }
   
   /**
    * Chooses which schools will be the most optimal solution
    *
    * @param dynTable The dynamic table to choose from representing a day.
    * @param availSchools The list of available schools for this day.
    * @param day The current day to create a schedule for.
    *
    * @return An ArrayList of schools that are scheduled for this day.
    */
   private Day chooseSchedule(Double[][] dynTable, ArrayList<School> availSchools, Day day)
   {
      School selected;
      int weights;
      int numItems = availSchools.size();
      
      //WHILE weight & items both > 0
      while (numItems > 0 && day.getSeats() > 0)
      {
         weights = day.getSeats();

         //IF values are not equal with epsilon .01 Compare doubles to 2nd decimal
         if (Math.abs(dynTable[numItems][weights] - dynTable[numItems - 1][weights]) >= .01)
         {
            selected = availSchools.get(numItems - 1);
            tempSeated += selected.numStudents;
            seatedSchools++;
            day.addSchool(selected);

            //Check if selected school has a split school, not empty
            if (!selected.splitSchool.isEmpty())
            {
               //Add each split school into must add column
               for (School splitSch : selected.splitSchool)
               {
                  mustAdd.add(splitSch);
               }
            }
         }
         numItems--;
      }

      return day;
   }
      
   private void scheduleMustAdds(int seed, HashMap<Integer, Day> dayMap)
   {
      int numItems;
      int numWeights;
      Double[][] dynTable;
      Day tempDay;
      ArrayList<School> addedSchools;
      ArrayList<School> tempMustAdds = new ArrayList<>();
      ArrayList<Integer> order = randOrder(seed);
      int scheduled; //Check if a school was scheduled or not
      
      while (!mustAdd.isEmpty())
      {        
         for (int ord : order)
         {
            //Check if mustAdds are empty
            if (mustAdd.isEmpty())
            {
               return;
            }

            tempMustAdds.clear();
            //For each must add school, add schools that can make this date.
            for (School mustSchool : mustAdd)
            {
               //Check if the school can make the available date.
               for (Day day : mustSchool.availDates)
               {
                  if (day.toString().equals(dayList.get(ord).toString()))
                  {
                     tempMustAdds.add(mustSchool);
                  }
               }
            }

            numItems = tempMustAdds.size();
            numWeights = dayList.get(ord).getSeats();

            //Check if there are schools to add
            if (numItems > 0)
            {
               dynTable = initializeDynTable(numItems, numWeights);     

               //Fill in all must add schools if they can make that date.
               fillTable(dynTable, tempMustAdds, numWeights);
            }
            else
            {
               continue;
            }

               //DEBUG
   //<editor-fold defaultstate="collapsed" desc="DEBUG Print DynTable">
   /*System.out.print(".........");
   for (int k = 0; k <= numWeights; k++)
   {
   System.out.printf("%7.2f|", new Double(k));
   }
   System.out.println();
   for (int i = 0; i <= numItems; i++)
   {
   System.out.printf("ROW %2d:  \n", i);
   if (i < numItems)
   {
   //System.out.println("SCHOOL: " + availSchools.get(i).name);
   }
   for (int j = 0; j <= numWeights; j++)
   {
   System.out.printf("%7.2f|", dynTable[i][j]);
   }
   System.out.println();
   }*/
   //</editor-fold>

            //Get current number of schools in this day
            scheduled = dayList.get(ord).getSchools().size();

            tempDay = chooseSchedule(dynTable, tempMustAdds, dayList.get(ord));
            addedSchools = (ArrayList<School>) tempDay.getSchools().clone();

            //Add scheduled mustAdds to daymap
            for (School mustSch: addedSchools)
            {
               //If no school was added
               if (scheduled == addedSchools.size())
               {
                  break;
               }

               if (!mustAdd.isEmpty())
               {
                  mustAdd.remove(mustSch);
               }

               if (dayMap.containsKey(ord))
               {
                  dayMap.get(ord).addSchool(mustSch);
               }
               else
               {
                  dayMap.put(ord, tempDay);
                  return;
               }
            }
         }
         
         //Select a completely random order if mustAdds not empty.
         order = randOrder(-1);
      }
   }
     
   /**
    * Fills the dynamic table up.
    * 
    * @param dynTable The table to fill up.
    * @param availSchools The list of schools that are available.
    * @param numWeights The number of seats the day has left.
    */
   private void fillTable(Double[][] dynTable, ArrayList<School> availSchools, int numWeights)
   {
      //sIndex = school, sWeight = # kids per school, sValue = priority
      int item, weight, sWeight, sIndex;
      double sValue, newValue, prevValue;
      //System.out.println("items: " + availSchools.size() + "\nweights: " + numWeights);
      
      //FOR all items starting at 1 (inclusive) and school in list starting at 0
      for (item = 1, sIndex = 0; item <= availSchools.size(); item++, sIndex++)
      {
         sWeight = availSchools.get(sIndex).numStudents;
         sValue = availSchools.get(sIndex).priority;

         //FOR all weights
         for (weight = 1; weight <= numWeights; weight++)
         {
            //IF item can be part of solution
            if (sWeight <= weight)
            {
               newValue = dynTable[item - 1][weight - sWeight] + sValue;
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
   }
   
   /**
    * Initializes the dynamic table. The entire row 0 and the entire column 0 is
    * filled with 0's. 0 is the lowest value/priority.
    *
    * @param items The total number of items. (y-axis)
    * @param weights The total weight to carry. (x-axis)
    *
    * @return A table to hold weights
    */
   private Double[][] initializeDynTable(int items, int weights)
   {
      //Add 1 because 10 items should go from 0 - 10 inclusive
      Double[][] table = new Double[items + 1][weights + 1];
      int i;

      //Set top row to 0's
      for (i = 0; i <= weights; i++)
      {
         table[0][i] = 0.0;
      }
      //Set first column to 0's
      for (i = 0; i <= items; i++)
      {
         table[i][0] = 0.0;
      }
      return table;
   }

   /**
    * Generates a list of available schools for each day. A school can be in
    * multiple days.
    *
    * @return A list of lists containing schools available for a day.
    */
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

   /**
    * Returns a random order in which the days will be filled.
    *
    * @param seed A seed for the random number generator
    *
    * @return A list representing the order to schedule the schools.
    */
   private ArrayList<Integer> randOrder(int seed)
   {
      ArrayList<Integer> arr = new ArrayList<>();
      Random rand;
      int randNum;
      boolean randExists;

      if (seed == -1)
      {
         rand = new Random();
      }
      else
      {
         rand = new Random(seed);
      }
      
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
         }
         while (randExists);
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

   /**
    * Returns the main schedule.
    *
    * @return The main schedule.
    */
   public HashMap getMainSchedule()
   {
      return mainSchedule;
   }
}
