package model;

import java.text.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Contains information regarding the who can come on a certain date.
 * 
 * @author Daniel Yao
 * @year 2016
 */
public class Day
{
   /** The date this day represents */
   private Date date;
   /** Determine if this day can take more students */
   protected boolean available;
   /** The schools coming this day */
   private ArrayList<School> schools;
   /** The max number of students that can attend */
   private int maxStudents;
   /** Current number of students coming */
   private int numStudents;
   /** The date's column in the excel sheet */
   protected int xlColumn;

   /**
    * Creates a day object to hold schools.
    * 
    * @param date The date on the calendar.
    */
   public Day(Date date, int column)
   {
      this.date = date;
      this.schools = new ArrayList<School>();
      this.maxStudents = 110;
      this.numStudents = 0;
      this.available = true;
      this.xlColumn = column;
   }
}