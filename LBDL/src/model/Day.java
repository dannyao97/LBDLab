package model;

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
   }
   
   /**
    * Adds a school to this day.
    * @param newSchool The school to add.
    */
   public void addSchool(School newSchool)
   {
      schools.add(newSchool);
      
      //Update remaining number of seats
      seatsLeft -= newSchool.numStudents;
   }
   
   /**
    * Returns the remaining number of seats.
    * @return The remaining number of seats.
    */
   public int getSeats()
   {
      return seatsLeft;
   }
}