package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;

/**
 * Contains information regarding who can come on a certain date.
 * 
 * @author Daniel Yao
 * @year 2016
 */
public class Day
{
   /** The date this day represents */
   protected Calendar date = Calendar.getInstance();
   /** The schools coming this day */
   private ArrayList<School> schools;
   /** The max number of students that can attend */
   private int maxStudents;
   /** The dates column in the excel sheet */
   protected int index;
   /** Remaining seats left */
   private int seatsLeft;
   /** Formats the date to a readable format */
   private SimpleDateFormat formatter;

   /**
    * Creates a day object to hold schools.
    * 
    * @param index The index of the day in LogicModel's dayList.
    */
   public Day(int index)
   {
      this.schools = new ArrayList<>();
      this.maxStudents = 110;
      this.seatsLeft = this.maxStudents;
      this.index = index;
      this.formatter = new SimpleDateFormat("EEE. MMM d, yyyy");
   }
   
   public Day(Day newDay)
   {
      this.date = newDay.date;
      this.schools = newDay.schools;
      this.maxStudents = newDay.maxStudents;
      this.index = newDay.index;
      this.seatsLeft = newDay.seatsLeft;
      this.formatter = newDay.formatter;
   }
   
   /**
    * Adds a school to this day.
    * @param newSchool The school to add.
    */
   public void addSchool(School newSchool)
   {    
      //Update remaining number of seats
      if (seatsLeft - newSchool.numStudents >= 0)
      {
         schools.add(newSchool);
         seatsLeft -= newSchool.numStudents;
      }
   }
   
   /**
    * Returns the remaining number of seats.
    * @return The remaining number of seats.
    */
   public int getSeats()
   {
      return seatsLeft;
   }
   
   /**
    * Returns the date as a readable string.
    * 
    * @return The date this Day represents.
    */
   @Override
   public String toString()
   {
      return formatter.format(date.getTime());
   }
   
   /**
    * Returns the schools scheduled to come on this day.
    * 
    * @return schools The schools going on this day.
    */
   public ArrayList<School> getSchools()
   {
      return schools;
   }
   
   /**
    * Clears all scheduled schools for this day.
    */
   public void clearSchools()
   {
      schools.clear();
      this.seatsLeft = this.maxStudents;
   }
}