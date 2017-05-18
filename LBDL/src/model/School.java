package model;

import java.util.*;

/**
 * Representation of a School that would attend.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class School
{
   /** Number of students to bring */
   protected int numStudents;
   /** The list of available dates */
   protected ArrayList<Day> availDates;
   /** The School name */
   protected String name;
   /** Determine if the school can split. Default is false */
   protected boolean split;
   /** Holds how school will divide up students */
   protected ArrayList<Integer> splitNums;
   /** Priority of the school */
   public double priority;
   /** Any comments left by the school */
   protected String comments;
   /** If school has visited before. Default is true */
   protected boolean visited;
   /** The scheduled day for this school */
   protected Calendar actualDay;
   /** A list of schools that are split from this school */
   protected ArrayList<School> splitSchool;
   /** The id of the school */
   public int id;
   /** All split schools should have same splitId */
   protected int splitId;
   /** Total number of students including all splits*/
   protected int totalNumStudents;
   
   
   /**
    * Creates a School object to represent a school.
    */
   public School(int newId)
   {
      this.availDates = new ArrayList<>();
      this.splitNums = new ArrayList<>();
      this.splitSchool = new ArrayList<>();
      this.priority = 100.0;
      this.comments = "";
      this.visited = true;
      this.split = false;
      this.actualDay = null;
      this.id = newId;
      this.splitId = LogicModel.schoolSplitId++;
   }
   
   /**
    * Create a new School with split numbers
    * 
    * @param old The original school to create from.
    * @param splitNum The numStudents of the new school.
    */
   public School(School old, int splitNum)
   {
      this.actualDay = old.actualDay;
      this.availDates = old.availDates;
      this.comments = old.comments;
      this.id = LogicModel.schoolId++;
      this.name = old.name;
      this.numStudents = splitNum;
      this.priority = old.priority;
      this.split = false;
      this.splitNums = null;
      this.splitSchool = null;
      this.visited = old.visited;
      this.splitId = old.splitId;
   }
   
   /**
    * Add a day to the list of available dates for the school.
    *
    * @param day The day to add.
    */
   public void addDay(Day day)
   {
      availDates.add(day);
   }
   
   /**
    * Returns the school's name.
    * 
    * @return The school name.
    */
   public String getName()
   {
      return name;
   }
   
   /**
    * Adds a new split school to the array.
    * 
    * @param s The school to add.
    */
   public void addNewSplitSchool(School s)
   {
      splitSchool.add(s);
   }
   
   @Override
   public boolean equals(Object obj)
   {
      School compare = (School) obj;
      
      return id == compare.id;
   }
   
   public int getNumStudents()
   {
      return numStudents;
   }
   
   public int getTotalStudents()
   {
       return totalNumStudents;
   }
}
