package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;
import javax.swing.DefaultListModel;

/**
 * Contains information regarding who can come on a certain date.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class Day {

    /**
     * The date this day represents
     */
    protected Calendar date = Calendar.getInstance();
    /**
     * The schools coming this day
     */
    protected ArrayList<School> schools;
    /**
     * The max number of students that can attend
     */
    private int maxStudents;
    /**
     * The dates column in the excel sheet
     */
    public int index;
    /**
     * Remaining seats left
     */
    protected int seatsLeft;
    /**
     * Formats the date to a readable format
     */
    protected SimpleDateFormat formatter;

    /**
     * Creates a day object to hold schools.
     *
     * @param index The index of the day in LogicModel's dayList.
     */
    public Day(int index, int maxStudents) {
        this.schools = new ArrayList<>();
        this.maxStudents = maxStudents;
        this.seatsLeft = maxStudents;
        this.index = index;
        this.formatter = new SimpleDateFormat("EEE. MMM d, yyyy");
    }

    public Day(Day newDay) {
        this.date = newDay.date;
        this.schools = new ArrayList<>(newDay.schools);
        this.maxStudents = newDay.maxStudents;
        this.index = newDay.index;
        this.seatsLeft = newDay.seatsLeft;
        this.formatter = newDay.formatter;
    }

   /**
    * Adds a school to this day.
    *
    * @param newSchool The school to add.
    */
   public void addSchool(School newSchool, boolean override) {
      //Update remaining number of seats
      if (seatsLeft - newSchool.numStudents >= 0 || override) {
         schools.add(newSchool);
         seatsLeft -= newSchool.numStudents;
      }
   }

    /**
     * Removes the schools from this day using the ID ONLY
     *
     * @param newSchool The school to remove.
     */
    public void removeSchool(School newSchool) {
        Iterator<School> schoolIter = schools.iterator();
        School tempSchool;

        while (schoolIter.hasNext()) {
            tempSchool = schoolIter.next();

            if (tempSchool.splitId == newSchool.splitId) {
                
                seatsLeft += tempSchool.numStudents;
                schoolIter.remove();
            }
        }
    }

    /**
     * Returns the remaining number of seats.
     *
     * @return The remaining number of seats.
     */
    public int getSeats() {
        return seatsLeft;
    }

    /**
     * Returns the date as a readable string.
     *
     * @return The date this Day represents.
     */
    @Override
    public String toString() {
        return formatter.format(date.getTime());
    }

    /**
     * Returns the schools scheduled to come on this day.
     *
     * @return schools The schools going on this day.
     */
    public ArrayList<School> getSchools() {
        return schools;
    }

    /**
     * Clears all scheduled schools for this day.
     */
    public void clearSchools() {
        schools.clear();
        this.seatsLeft = this.maxStudents;
    }

    /**
     * Sets the number of seats for this day.
     *
     * @param numSeats The number of seats for this day.
     */
    public void setMaxStudents(int numSeats) {
        this.maxStudents = numSeats;
        this.seatsLeft = numSeats;
    }
    
    /**
     * Get list model of all school names
     * @return DefaultListModel<String> of all the school names
     */
    public DefaultListModel<String> getSchoolNames() {
      DefaultListModel listModel = new DefaultListModel<>();
       
      String sSize, element;
      for (School s: this.schools) {
         sSize = "<b>" + s.getTotalStudents() + "</b>";
         element = String.format("<html>%2.1f %10s %s</html>", 500 - s.priority, sSize, s.getName());
         listModel.addElement(element);
      }
      
      return listModel;
    }
    
   public int getMaxSeats() {
      return maxStudents;
   }
}
