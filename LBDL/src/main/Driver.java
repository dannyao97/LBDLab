package main;

import gui.GUI;
import model.*;

/**
 * Starts the main program.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class Driver
{

   public static void main(String[] args)
   {
      //Create a model for logic
      LogicModel model = new LogicModel();
      //Start up the gui
      GUI.main(model);
      //model.writeExcelFile("fakestring");
   }

}
