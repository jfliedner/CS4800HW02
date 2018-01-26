import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

class PriorityA {
  public static void main(String[] args) {
    ArrayList<LineType> myRequests = new ArrayList<LineType>();
    Scanner myScanner = new Scanner(System.in);
    while (myScanner.hasNextLine()) {
      myRequests.add(LineType.parse(myScanner.nextLine()));
    }

    scheduler(myRequests); 
  }

  public static void scheduler(ArrayList<LineType> lines) {
    ArrayList<Request> requests = new ArrayList<Request>();
    ArrayList<Request> printed = new ArrayList<Request>();
    Request bestRequest = null;
    int currentTime = 0;
    for (int i = 0; i < lines.size(); i++) {
      int j = i;
      while (!(lines.get(j) instanceof PassedTime) && !(lines.get(j) instanceof Cancellation)) {
        requests.add((Request)lines.get(i));
        Collections.sort(requests);
        bestRequest = requests.get(0);
        j++;
      }

      if (lines.get(i) instanceof PassedTime && lines.get(i).getEndMinute() >= bestRequest.getStartMinute()) {
        printed = printRequests(requests, lines.get(i).getEndMinute(), printed);
      }

      else if (lines.get(i) instanceof Cancellation) {
        requests = processCancel(lines.get(i), requests, lines);
        Collections.sort(requests);
        if (requests.size() != 0) {
          bestRequest = requests.get(0);
        }
      }

    }


  }

  public static ArrayList<Request> printRequests(ArrayList<Request> requests, int time, ArrayList<Request> printed) {
    ArrayList<Request> toPrint = new ArrayList<Request>();
    int curIdx = 0;
    int initialSize = requests.size();
    int printedSize = printed.size();
    if (printedSize >= 1) {
      for (int i = 0; i < initialSize; i++) {
        if (toPrint.size() == 0 && requests.get(i).getStartMinute() <= time
            && requests.get(i).getStartMinute() >= printed.get(printedSize - 1).getEndMinute()) {
          toPrint.add(requests.get(i));
          requests.remove(requests.get(i));
        }
        else if (toPrint.size() != 0 && 
            requests.get(curIdx).getStartMinute() >= toPrint.get(toPrint.size() - 1).getEndMinute()
            && requests.get(curIdx).getStartMinute() >= printed.get(printedSize - 1).getEndMinute()) {
          toPrint.add(requests.get(curIdx));
          requests.remove(requests.get(curIdx));
        }
        else {
          curIdx++;
        }
      }
    }
    else {
      toPrint.add(requests.get(0));
      for (int i = 0; i < initialSize; i++) {
        if (requests.get(curIdx).getStartMinute() >= toPrint.get(toPrint.size() - 1).getEndMinute()) {
          toPrint.add(requests.get(curIdx));
          requests.remove(requests.get(curIdx));
        }
        else {
          curIdx++;
        }
      }
    }
    for (int j = 0; j < toPrint.size(); j++) {
      System.out.println(toPrint.get(j));
      printed.add(toPrint.get(j));
    }

    return printed;
  }

  public static ArrayList<Request> processCancel(LineType c, ArrayList<Request> requests, ArrayList<LineType> lines) {
    boolean removed = false;
    for (int i = 0; i < requests.size(); i++) {
      if (c.getStartMinute() == requests.get(i).getStartMinute()
          && c.getEndMinute() == requests.get(i).getEndMinute()) {
        requests.remove(requests.get(i));
        removed = true;
        break;
      }
    }

    if (!removed) {
      for (int j = 0; j < lines.size(); j++) {
        if (c.getStartMinute() == lines.get(j).getStartMinute()
            && c.getEndMinute() == lines.get(j).getEndMinute()) {
          lines.remove(lines.get(j));
          removed = true;
          break;
        }
      
      }
    }
    if (!removed) {
      throw new IllegalStateException("what?");
    }

      return requests;
    }
  

}


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
