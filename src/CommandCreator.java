import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by jserna18 on 11/8/16.
 */
public class CommandCreator
{
  BufferedWriter writer;

  private void makeCommands()
  {
    Random rand = new Random();
    int amt;
    int command;
    int dollars;
    int cents;

    try {
      //create a temporary file
      File logFile = new File("InputCommands1");

      // This will output the full path where the file will be written to...
      System.out.println(logFile.getCanonicalPath());

      writer = new BufferedWriter(new FileWriter(logFile));

      for (int i = 0; i < 500_000; i++) {
        amt = rand.nextInt(50);
        dollars = rand.nextInt(11);
        cents = rand.nextInt(100);
        command = rand.nextInt(3);

        if (command == 0) {
          try {
            writer.write("buy: " + amt + " " + dollars + "." + cents + "\n");
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else if (command == 1) {
          try {
            writer.write("sell: " + amt + " " + dollars + "." + cents + "\n");
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          try {
            writer.write("inventory:\n");
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
      catch(Exception e)
      {
        e.printStackTrace();
      }
      finally
      {
        try {
          // Close the writer regardless of what happens...
          writer.close();
        } catch (Exception e) {

        }
      }


  }



  public static void main(String[] args)
  {
    CommandCreator create = new CommandCreator();

    create.makeCommands();

  }
}
