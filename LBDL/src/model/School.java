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
   private int numStudents;
   /** The list of available dates */
   private ArrayList<Date> availDates;
   /** The School name */
   private String name;
   /** Determine if the school can split */
   private boolean split;
   /** Holds how school will divide up students */
   private ArrayList<Integer> splitNums;
   /** Priority of the school */
   private int priority;
   /** Any comments left by the school */
   private String comments;
   /** If school visited last year */
   private boolean visited;

   /**
    * Creates a School object to represent a school.
    * 
    * @param name The name of the school.
    * @param students The number of students.
    * @param priority The priority of the school.
    * @param split True if school can split up students.
    * @param visited True if school came the previous year.
    */
   public School(String name, int students, int priority, boolean split, boolean visited)
   {
      this.name = name;
      this.numStudents = students;
      this.availDates = new ArrayList<Date>();
      this.split = split;
      this.priority = priority;
      this.comments = "";
      this.visited = visited;
   }

   /**
    * Add a date to the list of available dates for the school.
    * 
    * @param month The month of the date.
    * @param day The day of the date.
    */
   public void addDates(int month, int day)
   {
      
   }
}
