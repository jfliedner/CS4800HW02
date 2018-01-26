import java.util.Scanner;
import java.util.ArrayList;
import java.util.PriorityQueue;

// Jillian Fliedner
class FirstPriority {
  public static void main(String[] args) {
    ArrayList<LineType> myRequests = new ArrayList<LineType>();
    Scanner myScanner = new Scanner(System.in);
    while (myScanner.hasNextLine()) {
      myRequests.add(LineType.parse(myScanner.nextLine()));
    }

    scheduler(myRequests); 
  }

  // to schedule the requests
  public static void scheduler(ArrayList<LineType> lines) {
    PriorityQueue<Request> requests = new PriorityQueue<Request>();
    ArrayList<Request> printed = new ArrayList<Request>();
    for (int i = 0; i < lines.size(); i++) { // sepearates requests into indivial queue 
      if (lines.get(i) instanceof Request) {
        requests.add((Request)lines.get(i));
      }

      // instance of time and the time works with the peek's request start time
      else if (lines.get(i) instanceof PassedTime && requests.size() > 0
          && lines.get(i).getEndMinute() >= requests.peek().getStartMinute()) {
        printRequests(requests, lines.get(i).getEndMinute(), printed);
      }

      // cancel
      else if (lines.get(i) instanceof Cancellation) {
        requests = processCancel(lines.get(i), requests, lines);


      }


    }


  }

  // to print out the legal requests
  public static void printRequests(PriorityQueue<Request> requests, int time, ArrayList<Request> printed) {
    ArrayList<Request> toPrint = new ArrayList<Request>(); // what will be printed
    PriorityQueue<Request> leftover = new PriorityQueue<Request>(); // what wasn't printed out from requests
    if (!printed.isEmpty()) { // Requests have been printed before
      while (requests.size() > 0 && !(requests.peek().getStartMinute() > time)) { // requests still there and still within time
        
        Request r = requests.poll();
        if (toPrint.isEmpty()) { // overlaps with most recently printed?
          if (r.getStartMinute() >= printed.get(printed.size() - 1).getEndMinute() && r.getStartMinute() <= time) {
            toPrint.add(r);

          }
        } // overlaps with what will be printed?
        else if (r.getStartMinute() >= toPrint.get(toPrint.size() - 1).getEndMinute() && r.getStartMinute() <= time) {
          toPrint.add(r);

        }
        else  { // not able to print yet, so add it to what's left
          leftover.add(r);
        }


      }
    }
    else {
      // nothing has been printed yet
      while (requests.size() > 0 && !(requests.peek().getStartMinute() > time))  {
        Request q = requests.poll();

        if (toPrint.isEmpty()) {
          if (q.getStartMinute() <= time) { // doesn't conflict with time

            toPrint.add(q);
          }
        } // conflicts with what is going to be printed?
        else if (q.getStartMinute() >= toPrint.get(toPrint.size() - 1).getEndMinute()
            && q.getStartMinute() <= time) {

          toPrint.add(q);
        }
        else {
          leftover.add(q); // not able to print yet, so add it to what's left
        }




      }
    }

    // print the Reqeuests out
    for (int j = 0; j < toPrint.size(); j++) {
      System.out.println(toPrint.get(j));
      printed.add(toPrint.get(j));
    }
    
    requests = leftover; // requests queue is now what's left over
  }

  // cancel the right request
  public static PriorityQueue<Request> processCancel(LineType c, PriorityQueue<Request> requests, ArrayList<LineType> lines) {
    Request toRemove = new Request(c.getStartMinute(), c.getEndMinute());
    requests.remove(toRemove);
    return requests;
  }


}

// to represent a type of input
abstract class LineType implements Comparable{


  public abstract int getEndMinute();
  public abstract int getStartMinute();
  public abstract String toString();

  public int toMinutes(String time) {
    String[] timeParts = time.split(":");
    int hour = new Integer(timeParts[0]);
    int minute = new Integer(timeParts[1]);
    return hour * 60 + minute;
  }

  public String timeToString(int minutes) {
    if ((minutes % 60) < 10) {
      return (minutes / 60) + ":0" + (minutes % 60);
    }
    return (minutes / 60) + ":" + (minutes % 60);
  }

  public static LineType parse(String inputLine) {
    if (inputLine.charAt(0) == 'c') {
      return new Cancellation(inputLine);
    }
    else if (inputLine.length() <= 5) {
      return new PassedTime(inputLine);
    }
    else {
      return new Request(inputLine);
    }
  }
}

// Request class would normally be a separate file, but HackerRank wants
// a single file for submission.
class Request extends LineType {
  private int startMinute;
  private int endMinute;

  public Request(String inputLine) {
    String[] inputParts = inputLine.split(",");
    this.startMinute = toMinutes(inputParts[0]);
    this.endMinute = toMinutes(inputParts[1]);
  }

  public Request(int startMinute, int endMinute) {
    this.startMinute = startMinute;
    this.endMinute = endMinute;
  }

  public int getStartMinute() {
    return startMinute;
  }

  @Override
  public int getEndMinute() {
    return endMinute;
  }

  @Override
  public String toString() {
    return timeToString(startMinute) + "," + timeToString(endMinute);
  }

  public boolean overlaps(Request r) {
    // Four kinds of overlap...
    // r starts during this request:
    if (r.getStartMinute() >=getStartMinute() && 
        r.getStartMinute() < getEndMinute()) {
      return true;
    }
    // r ends during this request:
    if (r.getEndMinute() > getStartMinute() &&
        r.getEndMinute() < getEndMinute()) {
      return true;
    }
    // r contains this request:
    if (r.getStartMinute() < getStartMinute() &&
        r.getEndMinute() >= getEndMinute()) {
      return true;
    }
    // this request contains r:
    if (r.getStartMinute() > getStartMinute() &&
        r.getEndMinute() < getEndMinute()) {  
      return true;
    }
    return false;
  }
  // Allows use of Collections.sort() on this object
  // (implements Comparable interface)
  public int compareTo(Object o) {
    if (!(o instanceof Request)) {
      throw new ClassCastException();
    }
    Request r = (Request) o;
    if (r.getEndMinute() > getEndMinute()) {
      return -1;
    }
    else if (r.getEndMinute() < getEndMinute()) {
      return 1;
    }
    else if (r.getStartMinute() < getStartMinute()) {
      // Prefer later start times, so sort these first
      return -1;
    }
    else if (r.getStartMinute() > getStartMinute()) {
      return 1;
    }
    else {
      return 0;
    }
  }

  public boolean equals(Object o) {
    if (!(o instanceof Request)) {
      return false;
    }
    Request that = (Request) o;
    return this.startMinute == that.startMinute &&
        this.endMinute == that.endMinute;
  }

}

// to represent a cancellation
class Cancellation extends LineType {
  private int startMinute;
  private int endMinute;

  Cancellation(String inputLine) {
    inputLine = inputLine.substring(7);
    String[] inputParts = inputLine.split(",");
    this.startMinute = toMinutes(inputParts[0]);
    this.endMinute = toMinutes(inputParts[1]);
  }

  public int getStartMinute() {
    return startMinute;
  }

  @Override
  public int getEndMinute() {
    return this.endMinute;
  }

  @Override
  public String toString() {
    return null;
  }

  @Override
  public int compareTo(Object o) {
    // TODO Auto-generated method stub
    return 0;
  }


}

// to represent the passing of time
class PassedTime extends LineType {
  int time;

  PassedTime(String inputLine) {
    this.time = toMinutes(inputLine);
  }

  @Override
  public int getEndMinute() {
    return this.time;
  }

  @Override
  public String toString() {
    return "time";
  }

  @Override
  public int compareTo(Object o) {
    // TODO Auto-generated method stub
    return 0;
  }


  public int getStartMinute() {
    // TODO Auto-generated method stub
    return 0;
  }
}
