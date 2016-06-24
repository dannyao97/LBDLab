package gui;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import model.LogicModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.TextField;
import java.awt.Label;

/**
 * GUI for user interactions. Collects user parameters and runs application.
 * 
 * @author Daniel Yao
 * @year 2016
 */
public class MainGUI implements Observer
{

   private JFrame frame;
   /** Reference to the logic model */
   private final LogicModel model;
   private Label lblInputFile;
   private TextField txtFieldInput;
   private JButton btnChooseFile;
   private Label lblTitle;
   private JMenuBar menuBar;
   private JMenu mnFile;

   /**
    * Launch the application.
    */
   public static void main(final LogicModel model)
   {
      
      EventQueue.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
               MainGUI window = new MainGUI(model);
               window.frame.setVisible(true);
            } catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      });
   }
  
   /**
    * A small calendar for picking dates. Code was copied and modified from
    * http://www.javacodex.com/Swing/Swing-Calendar
    *  
    * @author Daniel Yao, JavaCodex 
    * @year 2016
    */
   class SwingCalendar extends JFrame
   {
      private static final long serialVersionUID = 1L;
      DefaultTableModel model;
      Calendar cal = new GregorianCalendar();
      JLabel label;

      SwingCalendar()
      {

         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setTitle("Swing Calandar");
         this.setSize(300, 200);
         getContentPane().setLayout(new BorderLayout());
         this.setVisible(true);

         label = new JLabel();
         label.setHorizontalAlignment(SwingConstants.CENTER);

         JButton b1 = new JButton("<-");
         b1.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               cal.add(Calendar.MONTH, -1);
               updateMonth();
            }
         });

         JButton b2 = new JButton("->");
         b2.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent ae)
            {
               cal.add(Calendar.MONTH, +1);
               updateMonth();
            }
         });

         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         panel.add(b1, BorderLayout.WEST);
         panel.add(label, BorderLayout.CENTER);
         panel.add(b2, BorderLayout.EAST);

         String[] columns = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
         model = new DefaultTableModel(null, columns);
         JTable table = new JTable(model);
         JScrollPane pane = new JScrollPane(table);

         getContentPane().add(panel, BorderLayout.NORTH);
         getContentPane().add(pane, BorderLayout.CENTER);

         this.updateMonth();

      }

      void updateMonth()
      {
         cal.set(Calendar.DAY_OF_MONTH, 1);

         String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US);
         int year = cal.get(Calendar.YEAR);
         label.setText(month + " " + year);

         int startDay = cal.get(Calendar.DAY_OF_WEEK);
         int numberOfDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
         int weeks = cal.getActualMaximum(Calendar.WEEK_OF_MONTH);

         model.setRowCount(0);
         model.setRowCount(weeks);

         int i = startDay - 1;
         for (int day = 1; day <= numberOfDays; day++)
         {
            model.setValueAt(day, i / 7, i % 7);
            i = i + 1;
         }

      }
   }
   
   /**
    * Create the application.
    */
   public MainGUI(LogicModel model)
   {
      this.model = model;
      model.addObserver(this);
      initialize();
      
      //Create a new calendar
   }

   /**
    * Initialize the contents of the frame.
    */
   private void initialize()
   {
      frame = new JFrame();
      frame.setTitle("Learn by Doing Lab Scheduler");
      frame.setMinimumSize(new Dimension(600, 600));
      frame.setBounds(100, 100, 450, 300);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      menuBar = new JMenuBar();
      menuBar.setName("MenuBar");
      frame.setJMenuBar(menuBar);
      
      mnFile = new JMenu("File");
      mnFile.setFont(new Font("Segoe UI", Font.PLAIN, 16));
      mnFile.setName("MenuFile");
      menuBar.add(mnFile);
      
      JMenuItem mntmExit = new JMenuItem("Exit");
      mntmExit.setFont(new Font("Segoe UI", Font.PLAIN, 16));
      mntmExit.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            frame.dispose();
         }
      });
      mntmExit.setName("MenuExit");
      mnFile.add(mntmExit);
      SpringLayout springLayout = new SpringLayout();
      frame.getContentPane().setLayout(springLayout);
      
      lblInputFile = new Label("Choose a File:");
      lblInputFile.setFont(new Font("Segoe UI", Font.PLAIN, 14));
      springLayout.putConstraint(SpringLayout.EAST, lblInputFile, 335, SpringLayout.WEST, frame.getContentPane());
      frame.getContentPane().add(lblInputFile);
      
      txtFieldInput = new TextField();
      springLayout.putConstraint(SpringLayout.WEST, txtFieldInput, 10, SpringLayout.WEST, frame.getContentPane());
      springLayout.putConstraint(SpringLayout.EAST, txtFieldInput, -247, SpringLayout.EAST, frame.getContentPane());
      springLayout.putConstraint(SpringLayout.WEST, lblInputFile, 0, SpringLayout.WEST, txtFieldInput);
      springLayout.putConstraint(SpringLayout.SOUTH, lblInputFile, -6, SpringLayout.NORTH, txtFieldInput);
      txtFieldInput.setName("inputFileName");
      frame.getContentPane().add(txtFieldInput);
      
      btnChooseFile = new JButton("Choose File");
      btnChooseFile.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent arg0) {
            btnChooseFileAction();
         }
      });
      springLayout.putConstraint(SpringLayout.WEST, btnChooseFile, 6, SpringLayout.EAST, txtFieldInput);
      springLayout.putConstraint(SpringLayout.SOUTH, btnChooseFile, 0, SpringLayout.SOUTH, txtFieldInput);
      frame.getContentPane().add(btnChooseFile);
      
      lblTitle = new Label("Learn by Doing Lab Scheduler");
      springLayout.putConstraint(SpringLayout.WEST, lblTitle, 10, SpringLayout.WEST, frame.getContentPane());
      springLayout.putConstraint(SpringLayout.NORTH, lblInputFile, 24, SpringLayout.SOUTH, lblTitle);
      springLayout.putConstraint(SpringLayout.NORTH, txtFieldInput, 54, SpringLayout.SOUTH, lblTitle);
      springLayout.putConstraint(SpringLayout.NORTH, btnChooseFile, 53, SpringLayout.SOUTH, lblTitle);
      lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
      springLayout.putConstraint(SpringLayout.NORTH, lblTitle, 10, SpringLayout.NORTH, frame.getContentPane());
      frame.getContentPane().add(lblTitle);
   }

   /**
    * Action for choose file button.
    */
   private void btnChooseFileAction()
   {
      JFileChooser fileDialog = new JFileChooser(System.getProperty("user.home"));
      fileDialog.showOpenDialog(frame);
      fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
      txtFieldInput.setText(fileDialog.getSelectedFile().getAbsolutePath());
   }
   
   @Override
   public void update(Observable arg0, Object arg1)
   {
      // TODO Auto-generated method stub

   }
}
