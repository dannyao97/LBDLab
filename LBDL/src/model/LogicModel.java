package model;

import java.io.*;
import java.util.Observable;
import java.util.*;
import java.util.Map.Entry;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Logic model performs all scheduling logic and notifies the GUI.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class LogicModel extends Observable {

   /**
    * The map of available days. Column index is the key
    */
   public HashMap<Integer, Day> dayList;
   /**
    * An object to perform operations on the excel file
    */
   private final ExcelHandler xlHandler;
   /**
    * The total number of days
    */
   public int TotalDays;
   /**
    * The number of students seated in total.
    */
   private int seated;
   /**
    * Text to notify the GUI.
    */
   public String notifyText;
   /**
    * An id counter for new schools.
    */
   protected static int schoolId = 0;
   /**
    * An split id counter for split schools.
    */
   protected static int schoolSplitId = 0;
   
   private int currentSeed = 0;
   private int highestSeed = 0;

   /**
    * Determines if the file has been read.
    */
   public boolean fileRead = false;

   public LogicModel() {
      dayList = new HashMap<>();
      schoolList = new ArrayList<>();
      xlHandler = new ExcelHandler(this);
      seated = 0;
   }

   public enum NotifyCmd {
      TEXT, LIST, PROG, ERROR, LOG;
   }

   /**
    * Notifies the gui with the given object.
    *
    * @param cmd The command to perform - 1 : Notify Text - 2 : Update List
    */
   public void notify(NotifyCmd cmd) {
      setChanged();
      notifyObservers(cmd);
   }

   /**
    * Read the provided excel file.
    *
    * @param filename The name of the file.
    * @param start The start date.
    * @param end The end date.
    */
   public void readExcelFile(String filename, String start, String end) {
      try
      {
         fileRead = false;
         xlHandler.readXLFile(filename, start, end);
         fileRead = true;
      } catch (InvalidFormatException e)
      {
         notifyText = "Error: File contains an invalid format.";
         notify(NotifyCmd.ERROR);
      }
   }

   /**
    * Writes the schedule to an excel file.
    *
    * @param outputFile The name of the output file.
    */
   public void writeExcelFile(String outputFile) {
      try
      {
         xlHandler.writeXLFile(outputFile);
         notifyText = "File Written!";
      } catch (IOException e)
      {
         notifyText = "Error: Could not write to file.";
      }
      notify(NotifyCmd.LOG);
   }

   /**
    * Copies all keys and values from hash into a new HashMap
    *
    * @param hash
    * @return A new HashMap with duplicate values.
    */
   private HashMap<Integer, Day> cloneDayList(HashMap<Integer, Day> hash) {
      HashMap<Integer, Day> newHash = new HashMap<>();

      for (Entry<Integer, Day> entry : hash.entrySet())
      {
         newHash.put(entry.getKey(), entry.getValue());
      }
      return newHash;
   }

   private HashMap<Integer, ArrayList<School>> cloneHashMap(HashMap<Integer, ArrayList<School>> hash) {
      HashMap<Integer, ArrayList<School>> newHash = new HashMap<>();
      ArrayList<School> newSchoolList;

      for (Integer key : hash.keySet())
      {
         newSchoolList = new ArrayList<>(hash.get(key));
         newHash.put(key, newSchoolList);
      }
      return newHash;
   }

   /**
    * Fills the dynamic table up.
    *
    * @param dynTable The table to fill up.
    * @param availSchools The list of schools that are available.
    * @param numWeights The number of seats the day has left.
    */
   private Double[][] fillTable(Double[][] dynTable, ArrayList<School> availSchools, int numWeights) {
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
   private Double[][] initializeDynTable(int items, int weights) {
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
    * Returns a random order in which the days will be filled.
    *
    * @param seed A seed for the random number generator
    *
    * @return A list representing the order to schedule the schools.
    */
   private ArrayList<Integer> randOrder(int seed) {
      ArrayList<Integer> arr = new ArrayList<>();
      Random rand;
      int randNum;
      boolean randExists;

      switch (seed)
      {
         case -1:
            currentSeed = (int) System.currentTimeMillis();
            rand = new Random(currentSeed);
            break;
         case 0:
            for (int num = 1; num <= TotalDays; num++)
            {
               arr.add(num);
            }
            return arr;
         case 1:
            for (int num = TotalDays; num > 0; num--)
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
         } while (randExists);
      }
      return arr;
   }

   /**
    * Returns the list of schools.
    *
    * @return The list of schools.
    */
   public ArrayList<School> getSchoolList() {
      return schoolList;
   }

   /**
    * Reset the model. Clear all schools and days for reinitialization.
    */
   public void resetModel() {
      schoolList.clear();
      dayList.clear();
   }

   //Alternate variables
   public ArrayList<School> schoolList;
   public ArrayList<School> schoolListSplit;
   public ArrayList<School> toAdd = new ArrayList<>();
   public ArrayList<School> scheduledSchools = new ArrayList<>();
   public ArrayList<School> preScheduled = new ArrayList();
   public ArrayList<School> unscheduled;
   public HashMap<Integer, ArrayList<School>> needAdd = new HashMap<>();
   public HashMap<Integer, ArrayList<School>> mustAdd = new HashMap<>();
   public int average = 0;
   public int iter;  //The number of schools scheduled per iteration
   public long constant = 0;
   public boolean done = false;
   public int totalSeated = 0, totalSchools = 0;
   public int totalStudents = 0;
   public int iterations = 0;
   public int index = 0;
   public Thread thread;
   public ArrayList<FinalDay> finalSchedule = new ArrayList<>();
   public ArrayList<School> finalUnscheduled = new ArrayList<>(); //Final unscheduled schools
   public ArrayList<School> excludedSchools = new ArrayList<>();

   public void knapsack() {

      thread = new Thread() {
         @Override
         public void run() {
            long startTime;
            long endTime;
            startTime = System.currentTimeMillis();
            altKnapsack();
            endTime = System.currentTimeMillis();
            System.out.println("Run Time: " + ((endTime - startTime)/1000.0) + " seconds");
         }
      };
      
      thread.start();
      thread.interrupt();
   }

   //Create split school list
   public void createSplitSchoolList() {
      School tempSchool;
      Iterator schoolListIter = schoolList.iterator();
      ArrayList<School> newSplits;
      int totalNumKids = 0;

      schoolListSplit = new ArrayList<>();
      //Split up all the schools
      while (schoolListIter.hasNext())
      {
         tempSchool = (School) schoolListIter.next();
         totalNumKids += tempSchool.totalNumStudents;

         //IF school can be split and numStudents > avg
         if (tempSchool.split)
         {
            newSplits = splitSchool(tempSchool);

            //Split up mustadd schools
            if (toAdd.contains(tempSchool))
            {
               mustAdd.put(tempSchool.id, newSplits);
            }

            //Add array of same school thats split up.
            for (School newSchool : newSplits)
            {
               schoolListSplit.add(newSchool);
            }
         }
         else
         {
            //Add to must add if in toAdd
            if (toAdd.contains(tempSchool))
            {
               newSplits = new ArrayList<>();
               newSplits.add(tempSchool);
               mustAdd.put(tempSchool.id, newSplits);
            }
            schoolListSplit.add(tempSchool);
         }
      }
      totalStudents = totalNumKids;
   }

   protected void altKnapsack() {
      ArrayList<School> leftover, chosen, exclude;
      ArrayList<Integer> unschedExists;
      Iterator unschedIter;
      School temp;
      createSplitSchoolList();

      for (index = 0; index < iterations; index++)
      {
         //Reset seated schools
         this.reset();
         //Recalculate average
         calculateAverage(schoolList);

         //Currently no schools are scheduled
         unscheduled = new ArrayList<>(schoolListSplit);

         //Schedule schools
         do
         {
            runAlgorithm(unscheduled);

            exclude = new ArrayList<>();
            //If a split school was scheduled.
            for (School sch : scheduledSchools)
            {
               for (School uSch : unscheduled)
               {
                  if (sch.splitId == uSch.splitId)
                  {
                     exclude.add(sch);
                  }
               }
            }

            for (School toRemove : exclude)
            {
               removeScheduledSchool(toRemove);
               unschedIter = unscheduled.iterator();
               while (unschedIter.hasNext())
               {
                  temp = (School) unschedIter.next();
                  if (temp.splitId == toRemove.splitId)
                  {
                     unschedIter.remove();
                  }
               }
            }
            needAdd.clear();

         } while (!exclude.isEmpty());

         //Get number of seated students
         seated = 0;
         for (Day d : dayList.values())
         {
            for (School s : d.getSchools())
            {
               seated += s.numStudents;
            }
         }

         //Get most students
         if (seated >= totalSeated)
         {
            totalSeated = seated;
            totalSchools = 0;//countSchools(scheduledSchools);
            finalSchedule.clear();
            finalUnscheduled.clear();

            chosen = new ArrayList<>(); //temp list of chosen schools
            totalSeated = 0;
            for (Day d : dayList.values())
            {
               finalSchedule.add(new FinalDay(d.date, d.schools, d.index, d.seatsLeft));
               for (School s : d.getSchools())
               {
                  chosen.add(s);
                  totalSeated += s.numStudents;
                  totalSchools++;
               }
            }

            //create list of unscheduled schools
            leftover = new ArrayList<>(schoolListSplit);
            for (School sch : chosen)
            {
               if (leftover.contains(sch))
               {
                  leftover.remove(sch);
               }
            }
            unschedExists = new ArrayList<>();

            totalSchools = schoolList.size();
            for (School sch : leftover)
            {
               if (!unschedExists.contains(sch.splitId))
               {
                  unschedExists.add(sch.splitId);
                  finalUnscheduled.add(sch);
                  totalSchools--;
               }
            }

         }

         notify(NotifyCmd.PROG);
      }

      //Keep track of highest seed
      highestSeed = currentSeed;
      System.out.println(highestSeed);
      
      notify(NotifyCmd.LIST);
      notifyText = "# of Seated Students: " + "<b>" + totalSeated + "</b>" + "<br/>Empty Seats: " + "<b>" + (xlHandler.totalSeats - totalSeated) + "</b>"
              + "<br/>Total # of Students: " + "<b>" + totalStudents + "</b>"
              + "<br/><br/># of Scheduled Schools: " + "<b>" + totalSchools + "</b>" + "<br/>Total # of Schools: " + "<b>" + schoolList.size() + "</b>";
      notify(NotifyCmd.TEXT);

      //DEBUG
      for (School sch : finalUnscheduled)
      {
         System.out.println(500 - sch.priority + " " + sch.name + " ");
      }
   }

   public void runAlgorithm(ArrayList<School> notScheduled) {
      int smallSchool, bigDay;
      ArrayList<Integer> order;

      //WHILE smallest school is smaller than smallest day. Can fit more students
      do
      {
         //scheduleNeedAdds();

         order = new ArrayList<>(needAdd.keySet());

         //Sort order by priority
         Collections.sort(order, new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
               return Integer.compare(i1, i2);
            }
         });

         for (int ord : order)
         {
            schedule(needAdd.get(ord));
         }

         schedule(unscheduled);

         smallSchool = getSmallestSchool(unscheduled);
         bigDay = getBiggestDay();
         //System.out.printf("smallSchool: %3d | bigDay %3d\n", smallSchool, bigDay);
      } while (smallSchool <= bigDay && !done);
   }

   public void schedule(ArrayList<School> toSchedule) {
      Double[][] dynTable, filledTable;
      ArrayList<Integer> order = randOrder(-1);
      ArrayList<School> availSchools, selected;
      boolean isSelect = false;
      Day day;

      //Reset iter
      iter = 0;

      for (int ord : order)
      {
         //IF topten is empty, break
         if (toSchedule.isEmpty())
         {
            return;
         }

         day = dayList.get(ord);
         availSchools = getAvail(toSchedule, day);

         //remove scheduled schools from avail schools
         for (School s : scheduledSchools)
         {
            if (availSchools.contains(s))
            {
               availSchools.remove(s);
            }
         }

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
            if (scheduledSchools.contains(sch))
            {
               day.removeSchool(sch);
               toSchedule.remove(sch);
            }
            else
            {
               //seated += sch.numStudents;
               scheduledSchools.add(sch);
               unscheduled.remove(sch);
               toSchedule.remove(sch);
               //availSchools.remove(sch);
               addNeedAdds(sch);
               iter++;
               isSelect = true;
            }
         }
      }

      if (!isSelect)
      {
         done = true;
      }
   }

   public void scheduleNeedAdds() {
      int smallSchool, bigDay;
      ArrayList<Integer> order = new ArrayList<>(needAdd.keySet());
      Iterator schedIter;
      School temp;

      //Sort order by priority
      Collections.sort(order, new Comparator<Integer>() {
         @Override
         public int compare(Integer i1, Integer i2) {
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
               schedIter = scheduledSchools.iterator();
               while (schedIter.hasNext())
               {
                  temp = (School) schedIter.next();
                  if (temp.id == needAdd.get(ord).get(0).id)
                  {
                     unscheduled.add(temp);
                  }
               }
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
   public void addNeedAdds(School school) {
      boolean exists;
      ArrayList<School> temp;

      for (School sch : unscheduled)
      {
         exists = false;
         //IF id's match, not in needAdds, and not in final schedule
         if ((sch.splitId == school.splitId))
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
               if (needAdd.containsKey(school.splitId))
               {
                  needAdd.get(school.splitId).add(sch);
               } //ELSE make a new mapping
               else
               {
                  temp = new ArrayList<>();
                  temp.add(sch);
                  needAdd.put(school.splitId, temp);
               }
            }
         }
      }
   }

   public void reset() {
      scheduledSchools = new ArrayList<>(preScheduled);
      for (Day day : dayList.values())
      {
         day.clearSchools();
      }
      //needAdd = new HashMap<>();
      needAdd = cloneHashMap(mustAdd);
      done = false;
   }

   /**
    * Get Available schools for this date
    *
    * @param arr The list to check from
    * @param day The Day to check
    * @return A list of available schools
    */
   public ArrayList<School> getAvail(ArrayList<School> arr, Day day) {
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

   public ArrayList<School> splitSchool(School school) {
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
   public ArrayList<School> altChooseSchedule(Double[][] dynTable, ArrayList<School> availSchools, int dayIndex) {
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
            day.addSchool(selected, false);
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
   public int getSmallestSchool(ArrayList<School> arr) {
      int smallest = Integer.MAX_VALUE;
      for (School school : arr)
      {
         if (school.numStudents < smallest)
         {
            smallest = school.numStudents;
         }
      }
      return smallest;
   }

   public int getBiggestDay() {
      int big = Integer.MIN_VALUE;

      for (Day day : dayList.values())
      {
         if (day.seatsLeft >= big)
         {
            big = day.seatsLeft;
         }
      }
      return big;
   }

   /**
    * Calculate the average number of students per school.
    *
    * @param arr The list of schools
    */
   private void calculateAverage(ArrayList<School> arr) {
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
   public int countSchools(ArrayList<School> arr) {
      Set uniqueSchools = new HashSet();

      for (School school : arr)
      {
         uniqueSchools.add(school.id);
      }

      return uniqueSchools.size();
   }

   public int getTotalSeats() {
      int result = 0;

      for (FinalDay day : finalSchedule)
      {
         for (School school : day.getSchools())
         {
            result += school.numStudents;
         }
      }
      return result;
   }

   public void setIterations(int iter) {
      this.iterations = iter;
   }

   /**
    * Removes a scheduled school from dayList and final schedule based on id.
    *
    * @param school The school to remove.
    */
   public void removeScheduledSchool(School school) {
      Iterator<School> scheduleIter = scheduledSchools.iterator();
      School tempSchool;

      //Remove schools from the daylist
      for (Day d : dayList.values())
      {
         d.removeSchool(school);
      }

      //Remove schools from the scheduledschools
      while (scheduleIter.hasNext())
      {
         tempSchool = scheduleIter.next();
         if (tempSchool.id == school.id)
         {
            scheduleIter.remove();
         }
      }
   }
}
