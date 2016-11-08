/**
 * Created by juaker505 on 11/5/2016.
 */
public class ThneedStore
{
  ServerMaster master;
  private int thneeds;
  private double treasuryBalance;
  private int cents;
  private int totalCost;


  public ThneedStore(double startingBalance, ServerMaster master)
  {
    treasuryBalance = startingBalance;
    this.master = master;
  }

  public void buyThneeds(ServerWorker wrkr, int amount, double price)
  {
    synchronized (wrkr)
    {
      if (price > treasuryBalance)
      {
        //do something
        return;
      }
      else
      {
        cents = (int)(price * 100.00);
        totalCost = (amount * cents);
        thneeds += amount;
        treasuryBalance -= (totalCost/100.00);
        System.out.println(thneeds + " $" + treasuryBalance);
        master.broadcast(thneeds);
      }

    }
  }

  public void sellThneeds(ServerWorker wrkr, int amount, double price)
  {
    synchronized (wrkr)
    {

      if (amount > thneeds)
      {
        //do something
        return;
      }
      else
      {
        cents = (int)(price * 100.00);
        totalCost = (amount * cents);

        thneeds -= amount;
        treasuryBalance += (totalCost/100.00);
        master.broadcast(thneeds);
      }

    }
  }

  public int getInventory()
  {
    return thneeds;
  }

}


