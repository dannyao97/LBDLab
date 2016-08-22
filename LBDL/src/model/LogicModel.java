package model;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
   protected ArrayList<School> schoolList;
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
      TEXT, LIST, PROG;
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
         xlHandler.writeXLFile(outputFile);
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
      for (School debug : mustAdd)
      {
         System.out.println(debug.name + "   " + debug.numStudents);
      }

      notifyText = "<br>-Seated Students: " + seated + "<br>-Schools: " + seatedSchools + "<br>-Smallest School size: " + smallestSchool.numStudents;
      notify(NotifyCmd.TEXT);
      notify(NotifyCmd.LIST);

      //DEBUG
//<editor-fold defaultstate="collapsed" desc="DEBUG Print Unscheduled Schools">      
      System.out.println("UNADDED");
      for (School s : schoolList)
      {
         if (s.actualDay == null)
         {
            System.out.println(s.name + " " + s.numStudents);
            if (!s.splitSchool.isEmpty())
            {
               for (School sp : s.splitSchool)
               {
                  System.out.println("-" + s.name + " " + s.numStudents);
               }
            }
         }
      }
//</editor-fold>      
   }

   private void updateSmallest(School curSchool, ArrayList<ArrayList<School>> unscheduled)
   {
      int tempSmallestNum = Integer.MAX_VALUE;

      //Find next smallest unscheduled school
      if (curSchool.equals(smallestSchool))
      {
         for (ArrayList<School> listofschools : unscheduled)
         {
            for (School newSmall : listofschools)
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
            selected.actualDay = day.date;

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
      int smallSeats = Integer.MAX_VALUE;   //Holds temp school with smallest seats
      Double[][] dynTable;
      Day tempDay;
      ArrayList<School> addedSchools;
      ArrayList<School> tempMustAdds = new ArrayList<>();
      ArrayList<Integer> order = randOrder(seed);
      int scheduled; //Check if a school was scheduled or not
      boolean skipFindAvail = false;   //Whether we can skip finding tempMustAdds again

      while (!mustAdd.isEmpty())
      {
         System.out.println("trapped");
         for (int ord : order)
         {
            //Check if mustAdds are empty
            if (mustAdd.isEmpty())
            {
               return;
            }

            //If scheduled and addSchools were equal the prev iter, then skip
            if (!skipFindAvail)
            {
               smallSeats = Integer.MAX_VALUE;
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
                        if (mustSchool.numStudents < smallSeats)
                        {
                           smallSeats = mustSchool.numStudents;
                        }
                     }
                  }
               }
            }
            numItems = tempMustAdds.size();
            numWeights = dayList.get(ord).getSeats();

            //Check if there are schools to add AND day can hold the smallest school
            if (numItems > 0 && numWeights >= smallSeats)
            {
               dynTable = initializeDynTable(numItems, numWeights);

               //Fill in all must add schools if they can make that date.
               fillTable(dynTable, tempMustAdds, numWeights);
            }
            else if (numItems == 0)
            {
               skipFindAvail = false;
               continue;
            }
            else
            {
               skipFindAvail = true;
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

            //PUT A CASE FOR WHEN THE SCHOOL COULD NOT BE ADDED OR CHOSEN BECAUSE NOT ENOUGH SEATS LEFT.
            //Add scheduled mustAdds to daymap
            for (School mustSch : addedSchools)
            {
               //If no school was added
               if (scheduled == addedSchools.size())
               {
                  skipFindAvail = true;
                  break;
               }

               if (!mustAdd.isEmpty())
               {
                  mustAdd.remove(mustSch);
               }

               skipFindAvail = false;
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
         skipFindAvail = false;
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
   private Double[][] fillTable(Double[][] dynTable, ArrayList<School> availSchools, int numWeights)
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
      
//DEBUG
//<editor-fold defaultstate="collapsed" desc="DEBUG Print DynTable">
/*System.out.print(".........");
for (int k = 0; k <= numWeights; k++)
{
System.out.printf("%7.2f|", new Double(k));
}
System.out.println();
for (int i = 0; i <= availSchools.size(); i++)
{
System.out.printf("ROW %2d:  \n", i);
if (i < availSchools.size())
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
      
      return dynTable;
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

      switch (seed)
      {
         case -1:
            rand = new Random();
            break;
         case 0:
            for (int num = 1; num <= TotalDays; num++)
            {
               arr.add(num);
            }
            return arr;
         default:
            rand = new Random(seed);
            break;
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

   
   
   
   
   
   
   
   
   
   
   
   
   
   
   
   //Alternate variables
   public ArrayList<School> schoolListAlt = new ArrayList<>();
   public ArrayList<School> scheduledSchools = new ArrayList<>();
   public ArrayList<School> unscheduled;
   public HashMap<Integer, ArrayList<School>> needAdd = new HashMap<>();
   public int average = 0;
   public int iter;  //The number of schools scheduled per iteration
   public long constant = 0;
   public Day biggestDay;
   public boolean done = false;
   public int totalSeated, totalSchools;
   public int iterations = 0;
   public int index = 0;
   
   public void Knapsack2()
   {
      Thread thead = new Thread() {
         @Override
         public void run()
         {
            altKnapsack();
         }
      };
      thead.start();
      thead.interrupt();
   }
   
   public void altKnapsack()
   {
      int i, numSchools, smallSchool, bigDay;
      ArrayList<School> newSplits, remaining, tempSchoolList;
      School tempSchool;
      
      totalSeated = 0;
      totalSchools = 0;

      for (index = 0; index < iterations; index++)
      {
         //Reset seated schools
         this.reset();
         remaining = new ArrayList<>();
         seated = 0;
         //Recalculate average
         calculateAverage(schoolListAlt);

         tempSchoolList = new ArrayList<>(schoolListAlt);
         //Get the remaining size
         numSchools = tempSchoolList.size();

         for (i = 0; i < numSchools; i++)
         {
            //0 because removing top element each time, index will be 0
            tempSchool = tempSchoolList.get(0);
            //IF school can be split and numStudents > avg
            if (tempSchool.split)
            {
               newSplits = splitSchool(tempSchool);
               //Add array of same school thats split up.
               for (School newSchool : newSplits)
               {
                  remaining.add(newSchool);
               }
            }
            else
            {
               remaining.add(tempSchool);
            }
            //Remove from school list
            tempSchoolList.remove(0);
         }

         unscheduled = new ArrayList<>(remaining);

         //Schedule remaining schools
         //WHILE smallest school is smaller than smallest day. Can fit more students
         do
         {           
            scheduleNeedAdds();
            schedule(remaining);
            smallSchool = getSmallestSchool(unscheduled);
            bigDay = getBiggestDay();
            //System.out.printf("smallSchool: %3d | bigDay %3d\n", smallSchool, bigDay);
         }while (smallSchool <= bigDay && !done);

         //Get most students
         if (seated > totalSeated)
         {
            totalSeated = seated;
            totalSchools = countSchools(scheduledSchools);
            //System.out.println("\nindex: " + index);
            //System.out.println("Seated: " + seated);
            //System.out.println("totalsize: " + totalSchools);
            mainSchedule = cloneHashMap(dayList);            
         }
         
         notify(NotifyCmd.PROG);
      }

      notify(NotifyCmd.LIST);
      notifyText = "-Seated Students: " + totalSeated + "  -Schools: " + totalSchools;
      notify(NotifyCmd.TEXT);
   }

   public void schedule(ArrayList<School> toSchedule)
   {
      Double[][] dynTable, filledTable;
      ArrayList<Integer> order = randOrder(-1);
      ArrayList<School> availSchools, selected;
      boolean isSelect = false;
      Day day;

      //IF topten is empty, break
      if (toSchedule.isEmpty())
      {
         return;
      }

      //Reset iter
      iter = 0;

      for (int ord : order)
      {
         day = dayList.get(ord);
         availSchools = getAvail(toSchedule, day);

         //IF day doesnt have enough seats
         if (getSmallestSchool(availSchools) > day.getSeats())
         {
            continue;
         }
         
         dynTable = initializeDynTable(availSchools.size(), day.getSeats());
         filledTable = fillTable(dynTable, availSchools, day.getSeats());
         selected = altChooseSchedule(filledTable, availSchools, ord);
            
         //FOR school in selected, remove
         for (School sch : selected)
         {
            seated += sch.numStudents;
            scheduledSchools.add(sch);
            unscheduled.remove(sch);
            toSchedule.remove(sch);
            availSchools.remove(sch);
            addNeedAdds(sch);
            iter++;
            isSelect = true;
         }
      }
      
      if (!isSelect)
      {
         done = true;
      }
   }

   public void scheduleNeedAdds()
   {
      int smallSchool, bigDay;
      ArrayList<Integer> order = new ArrayList<>(needAdd.keySet());

      //Sort order by priority
      Collections.sort(order, new Comparator<Integer>() {
        @Override
        public int compare(Integer i1, Integer i2)
        {
            return Integer.compare(i1, i2);
        }
      });
      //NeedAdd should be hashmap with <id,array>. check array size to remove all duplicate schools.

      //while list is not empty, schedule need add
      for (int ord : order)
      {
         smallSchool = getSmallestSchool(needAdd.get(ord));
         bigDay = getBiggestDay();

         //IF smallschool <= smallday, schedule must add
         if (smallSchool <= bigDay)
         {
            schedule(needAdd.get(ord));           
            
            //IF num split schools == iter, remove from needadds
            if (needAdd.get(ord).size() != iter && !needAdd.get(ord).isEmpty())
            {
               removeScheduledSchool(needAdd.get(ord).get(0));
            }
            needAdd.remove(ord);
         }
      }
   }

   /**
    * Adds all schools with the same id to needAdds
    *
    * @param school The just added school, to be excluded from need adds
    */
   public void addNeedAdds(School school)
   {
      boolean exists;
      ArrayList<School> temp;

      for (School sch : unscheduled)
      {
         exists = false;
         //IF id's match, not in needAdds, and not in final schedule
         if ((sch.id == school.id) && !sch.equals(school) && !scheduledSchools.contains(sch))
         {
            //Check if already in needAdds
            for (ArrayList<School> need : needAdd.values())
            {
               if (need.contains(sch))
               {
                  exists = true;
                  break;
               }
            }

            //IF not in needAdds
            if (!exists)
            {
               //IF needadd already has the key
               if (needAdd.containsKey(school.id))
               {
                  needAdd.get(school.id).add(sch);
               }
               //ELSE make a new mapping
               else
               {
                  temp = new ArrayList<>();
                  temp.add(sch);
                  needAdd.put(school.id, temp);
               }
            }
         }
      }
   }

   public void reset()
   {
      scheduledSchools.clear();
      for (Day day : dayList.values())
      {
         day.clearSchools();
      }
      needAdd = new HashMap<>();
      done = false;
   }

   /**
    * Get Available schools for this date
    *
    * @param arr The list to check from
    * @param day The Day to check
    * @return A list of available schools
    */
   public ArrayList<School> getAvail(ArrayList<School> arr, Day day)
   {
      ArrayList<School> avail = new ArrayList<>();

      for (School school : arr)
      {
         if (school.availDates.contains(day))
         {
            avail.add(school);
         }
      }
      return avail;
   }

   public ArrayList<School> splitSchool(School school)
   {
      ArrayList<School> split = new ArrayList<>();
      //FOR each split num
      for (int num : school.splitNums)
      {
         split.add(new School(school, num));
      }
      return split;
   }

   /**
    * Choose which schools will be scheduled.
    *
    * @param dynTable
    * @param availSchools
    * @param dayIndex
    * @return The scheduled schools for the dayIndex
    */
   public ArrayList<School> altChooseSchedule(Double[][] dynTable, ArrayList<School> availSchools, int dayIndex)
   {
      School selected;
      ArrayList<School> chosen = new ArrayList<>();
      int weights;
      int numItems = availSchools.size();
      Day day = dayList.get(dayIndex);

      //WHILE weight & items both > 0
      while (numItems > 0 && day.getSeats() > 0)
      {
         weights = day.getSeats();

         //IF values are not equal with epsilon .01 Compare doubles to 2nd decimal
         if (Math.abs(dynTable[numItems][weights] - dynTable[numItems - 1][weights]) >= .01)
         {
            selected = availSchools.get(numItems - 1);
            day.addSchool(selected);
            selected.actualDay = day.date;
            chosen.add(selected);
         }
         numItems--;
      }

      return chosen;
   }

   /**
    * Returns the smallest school size in the list
    *
    * @param arr The list.
    * @return The smallest school size.
    */
   public int getSmallestSchool(ArrayList<School> arr)
   {
      int smallest = Integer.MAX_VALUE;
      for (School school : arr)
      {
         if (school.numStudents < smallest)
         {
            smallest = school.numStudents;
            smallestSchool = school;
         }
      }
      return smallest;
   }

   public int getBiggestDay()
   {
      int big = Integer.MIN_VALUE;

      for (Day day : dayList.values())
      {
         if (day.seatsLeft >= big)
         {
            big = day.seatsLeft;
            biggestDay = day;
         }
      }
      return big;
   }

   /**
    * Calculate the average number of students per school.
    *
    * @param arr The list of schools
    */
   private void calculateAverage(ArrayList<School> arr)
   {
      int newAvg = 0;

      for (School s : arr)
      {
         newAvg += s.numStudents;
      }
      average = newAvg / arr.size();
   }

   /**
    * Counts the number of unique schools based on school id.
    *
    * @param arr The array to use.
    * @return The number of unique schools.
    */
   public int countSchools(ArrayList<School> arr)
   {
      Set uniqueSchools = new HashSet();

      for (School school : arr)
      {
         uniqueSchools.add(school.id);
      }

      return uniqueSchools.size();
   }

   public void setIterations(int iter)
   {
      this.iterations = iter;
   }
   
   /**
    * Removes a scheduled school from dayList and final schedule based on id.
    *
    * @param school The school to remove.
    */
   public void removeScheduledSchool(School school)
   {
      Iterator<School> scheduleIter = scheduledSchools.iterator();
      School tempSchool;
      
      //Remove schools from the daylist
      for (Day d : dayList.values())
      {
         d.removeSchool(school);
      }

      //Remove schools from the scheduledschools
      while(scheduleIter.hasNext())
      {
         tempSchool = scheduleIter.next();
         if (tempSchool.id == school.id)
         {
            scheduleIter.remove();
         }
      }
   }
}
