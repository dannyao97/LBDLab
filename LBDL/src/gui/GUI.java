package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import model.CustomComparator;
import model.Day;
import model.LogicModel;
import model.School;

/**
 * GUI for user interactions. Collects user parameters and runs application.
 *
 * @author Daniel Yao
 * @year 2016
 */
public class GUI extends javax.swing.JFrame implements Observer
{
   private final LogicModel model;

   /**
    * Creates new form GUI
    *
    * @param model The LogicModel to use
    */
   public GUI(LogicModel model)
   {
      this.model = model;
      this.model.addObserver(this);
      //Set the layout of the gui
      SpringLayout springLayout = new SpringLayout();
      this.getContentPane().setLayout(springLayout);
      initComponents();
   }

   /**
    * This method is called from within the constructor to initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is always
    * regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {

      inputFileChooser = new javax.swing.JFileChooser();
      txtFieldInput = new javax.swing.JTextField();
      btnChooseFile = new javax.swing.JButton();
      lblTitle = new javax.swing.JLabel();
      jScrollPane1 = new javax.swing.JScrollPane();
      listSchools = new javax.swing.JList<>();
      lblList = new javax.swing.JLabel();
      btnRun = new javax.swing.JButton();
      btnFastAlg = new javax.swing.JButton();
      btnKnapSack = new javax.swing.JButton();
      lblDebug = new javax.swing.JLabel();
      menuBar = new javax.swing.JMenuBar();
      menuFile = new javax.swing.JMenu();
      menuExit = new javax.swing.JMenuItem();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setTitle("Learn by Doing Lab Scheduler");
      setMinimumSize(new java.awt.Dimension(500, 500));

      txtFieldInput.setText("C:\\Users\\dyao\\Documents\\NetBeansProjects\\LBDLab\\LBDL\\testInput.xlsx");

      btnChooseFile.setText("Choose File");
      btnChooseFile.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            btnChooseFileActionPerformed(evt);
         }
      });

      lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
      lblTitle.setText("Learn by Doing Lab Scheduler");

      jScrollPane1.setViewportView(listSchools);

      lblList.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
      lblList.setText("List of Schools:");

      btnRun.setText("Read File");
      btnRun.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            btnRunActionPerformed(evt);
         }
      });

      btnFastAlg.setText("Brute Force Algorithm");
      btnFastAlg.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            btnFastAlgActionPerformed(evt);
         }
      });

      btnKnapSack.setText("Knapsack Algorithm");
      btnKnapSack.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            btnKnapSackActionPerformed(evt);
         }
      });

      lblDebug.setText("DEBUG:");
      lblDebug.setVerticalAlignment(javax.swing.SwingConstants.TOP);

      menuFile.setText("File");

      menuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK));
      menuExit.setText("Exit");
      menuExit.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            menuExitActionPerformed(evt);
         }
      });
      menuFile.add(menuExit);

      menuBar.add(menuFile);

      setJMenuBar(menuBar);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(layout.createSequentialGroup()
                  .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(18, 18, 18)
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                        .addComponent(lblDebug, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                           .addComponent(btnFastAlg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                           .addComponent(btnKnapSack, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
               .addGroup(layout.createSequentialGroup()
                  .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblTitle)
                     .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                           .addComponent(txtFieldInput, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                           .addComponent(lblList))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                           .addComponent(btnChooseFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                           .addComponent(btnRun, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                  .addGap(0, 141, Short.MAX_VALUE)))
            .addContainerGap())
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(19, 19, 19)
            .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(txtFieldInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(btnChooseFile))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(lblList)
               .addComponent(btnRun))
            .addGap(4, 4, 4)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addGroup(layout.createSequentialGroup()
                  .addGap(13, 13, 13)
                  .addComponent(btnFastAlg)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(btnKnapSack)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                  .addComponent(lblDebug, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap(22, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   private void menuExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_menuExitActionPerformed
   {//GEN-HEADEREND:event_menuExitActionPerformed
      this.dispose();
   }//GEN-LAST:event_menuExitActionPerformed

   private void btnChooseFileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnChooseFileActionPerformed
   {//GEN-HEADEREND:event_btnChooseFileActionPerformed
      inputFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel Files", "xlsx", "xls", "csv", "xlsm");
      inputFileChooser.setFileFilter(filter);
      inputFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      inputFileChooser.showOpenDialog(this);
      txtFieldInput.setText(inputFileChooser.getSelectedFile().getAbsolutePath());
   }//GEN-LAST:event_btnChooseFileActionPerformed

   private void btnRunActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnRunActionPerformed
   {//GEN-HEADEREND:event_btnRunActionPerformed
      model.readExcelFile(txtFieldInput.getText());
      populateSchoolList();
   }//GEN-LAST:event_btnRunActionPerformed

   private void btnFastAlgActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnFastAlgActionPerformed
   {//GEN-HEADEREND:event_btnFastAlgActionPerformed
      model.fastAlgorithm();
   }//GEN-LAST:event_btnFastAlgActionPerformed

   private void btnKnapSackActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnKnapSackActionPerformed
   {//GEN-HEADEREND:event_btnKnapSackActionPerformed
      model.knapsack();
   }//GEN-LAST:event_btnKnapSackActionPerformed

   private void populateSchoolList()
   {
      DefaultListModel<String> listModel = new DefaultListModel<>();
      for (School sch : model.getSchoolList())
      {
         listModel.addElement(sch.getName());
      }
      listSchools.setModel(listModel);
   }

   /**
    * @param model A passed in logic model.
    */
   public static void main(final LogicModel model)
   {
      /* Set the Windows look and feel */
      //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
      /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
       */
      try
      {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex)
      {
         java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
      //</editor-fold>

      //</editor-fold>

      /* Create and display the form */
      java.awt.EventQueue.invokeLater(new Runnable()
      {
         public void run()
         {
            new GUI(model).setVisible(true);
         }
      });
   }

   @Override
   public void update(Observable o, Object arg)
   {
      LogicModel.NotifyCmd cmd = (LogicModel.NotifyCmd) arg;

      //SWITCH over the commands 
      switch (cmd)
      {
         case TEXT:
            lblDebug.setText("<html>DEBUG: <br>" + model.notifyText + "</html>");
            break;
         case LIST:
            DefaultListModel<String> listModel = new DefaultListModel<>();
            ArrayList<Day> schedule = new ArrayList<>(model.getMainSchedule().values());
            //Sort schedule by index
            Collections.sort(schedule, new CustomComparator());
            
            for (Day day : schedule)
            {
               listModel.addElement("<html><b>" + day.toString() + "</b></html>");
               for (School sch : day.getSchools())
               {
                  listModel.addElement(sch.getName() + " " + sch.getNumStudents());
               }
            }
            listSchools.setModel(listModel);
            break;
      }
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

         String[] columns =
         {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
         };
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


   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton btnChooseFile;
   private javax.swing.JButton btnFastAlg;
   private javax.swing.JButton btnKnapSack;
   private javax.swing.JButton btnRun;
   private javax.swing.JFileChooser inputFileChooser;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JLabel lblDebug;
   private javax.swing.JLabel lblList;
   private javax.swing.JLabel lblTitle;
   private javax.swing.JList<String> listSchools;
   private javax.swing.JMenuBar menuBar;
   private javax.swing.JMenuItem menuExit;
   private javax.swing.JMenu menuFile;
   private javax.swing.JTextField txtFieldInput;
   // End of variables declaration//GEN-END:variables
}
