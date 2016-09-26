package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Holds the final day to be scheduled. Workaround for because dayList was resetting
 * all of day's fields each iteration preventing schedules from being saved.
 * 
 * @author Daniel
 */
public class FinalDay {
   /** The date this day represents */
   protected Calendar date = Calendar.getInstance();
   /** The schools coming this day */
   private ArrayList<School> schools;
   /** The dates column in the excel sheet */
   protected int index;
   /** Remaining seats left */
   protected int seatsLeft;
   /** Formats the date to a readable format */
   private SimpleDateFormat formatter = new SimpleDateFormat("EEE. MMM d, yyyy");

   /**
    * Creates a FinalDay to put into the final schedule.
    * 
    * @param nDate The Calendar Date
    * @param nSchools The arraylist of schools
    * @param nIndex The daylist index
    * @param nSeatsLeft The seats left for this day.
    */
   public FinalDay(Calendar nDate, ArrayList<School> nSchools, int nIndex, int nSeatsLeft)
   {
      this.date = nDate;
      this.schools = new ArrayList<>(nSchools);
      this.index = nIndex;
      this.seatsLeft = nSeatsLeft;
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
   
   
}
