package model;

import java.util.Comparator;

/**
 * A custom comparator for Day objects
 * 
 * @author Daniel Yao
 * @year 2016
 */
public class CustomComparator implements Comparator<FinalDay>
{
   @Override
   public int compare(FinalDay d1, FinalDay d2)
   {
      return d1.index >= d2.index ? 1 : -1;
   }
}
