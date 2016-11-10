/**
 * Created by juaker505 on 11/5/2016.
 */
public class ThneedStore
{
  private ServerMaster master;
  private volatile int thneeds;
  private volatile double treasuryBalance;
  private int cents;
  private int totalCost;

  /**
   * Constructor with an intital balance and ServerMaster
   * @param startingBalance
   * @param master
   */
  public ThneedStore(double startingBalance, ServerMaster master)
  {
    treasuryBalance = startingBalance;
    this.master = master;
  }

  /**
   * Adds to the current Thneeds in the store and reduces the treasury's value.
   * @param wrkr
   * @param amount
   * @param price
   */
  public void buyThneeds(ServerWorker wrkr, int amount, double price)
  {

    synchronized (wrkr)
    {
      if (price > treasuryBalance)
      {
        return;
      }
      else
      {
        cents = (int)(price * 100.00);
        totalCost = (amount * cents);
        thneeds += amount;
        treasuryBalance -= (totalCost/100.00);
        master.broadcast(thneeds, treasuryBalance, wrkr.workerId);
      }
    }
  }

  /**
   * Reduces the number of Thneeds in the store and adds to the Treasury's value
   * @param wrkr
   * @param amount
   * @param price
   */
  public void sellThneeds(ServerWorker wrkr, int amount, double price)
  {
    synchronized (wrkr)
    {

      if (amount > thneeds)
      {
        return;
      }
      else
      {
        cents = (int)(price * 100.00);
        totalCost = (amount * cents);
        thneeds -= amount;
        treasuryBalance += (totalCost/100.00);
        master.broadcast(thneeds, treasuryBalance, wrkr.workerId);
      }

    }
  }
}


