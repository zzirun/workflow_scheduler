package q3;// Online Java Compiler
// Use this editor to write, compile and run your Java code online
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

class Job {
  private Integer id;
  private Integer dueDate;
  private Double processingTime;
  private Job[] parents;
  private Job[] children;

  public Job(Integer id) {
    this.id = id;
    this.parents = new Job[0];
    this.children = new Job[0];
  }

  public void setDueDate(Integer dueDate) {
    this.dueDate = dueDate;
  }

  public void setProcessingTime(Double processingTime) {
    this.processingTime = processingTime;
  }

  public void setParents(Job[] parents) {
    this.parents = parents;
    for (Job parent : parents) {
      List<Job> children = new ArrayList<>(Arrays.asList(parent.getChildren()));
      children.add(this);
      parent.setChildren(children.toArray(new Job[children.size()]));
    }
  }

  public void setChildren(Job[] children) {
    this.children = children;
  }

  public Job[] getChildren() {
    return children;
  }

  public Integer getDueDate() {
    return dueDate;
  }

  public Double getProcessingTime() {
    return processingTime;
  }

  public Job[] getParents() {
    return parents;
  }

  @Override
  public String toString() {
    return id.toString();
  }
}

class Schedule {
  private Deque<Job> jobs;
  private Double remainingTime;
  private List<Job> availJobs;
  private Double cost;

  public Schedule(Schedule schedule) {
    this.jobs = new ArrayDeque<>(schedule.getJobs());
    this.availJobs = new ArrayList<>(schedule.getAvailJobs());
    this.remainingTime = schedule.getRemainingTime();
    this.cost = schedule.getCost();
  }

  public Schedule(Double remainingTime, List<Job> availJobs) {
    this.remainingTime = remainingTime;
    this.availJobs = new ArrayList<>(availJobs);
    jobs = new ArrayDeque<>();
    cost = 0.0;
  }

  public void addJob(Job job) {
    Double tardiness
        = Math.max(0.0, remainingTime - job.getDueDate());
    jobs.push(job);
    this.cost += tardiness * (1 + (1/Math.pow(jobs.size(), 0.5)));
    for (Job parent : job.getParents()) {
      if (jobs.containsAll(Arrays.asList(parent.getChildren()))) {
        availJobs.add(parent);
      }
    }
    availJobs.remove(job);
    remainingTime -= job.getProcessingTime();
  }

  public Double getTardiness() {
    Double tardiness = 0.0;
    Double duration = 0.0;
    for (Job job : this.jobs) {
      duration += job.getProcessingTime();
      tardiness += Math.max(0.0, duration - (double) job.getDueDate());
    }
    return tardiness;
  }

  public Double getRemainingTime() {
    return remainingTime;
  }

  public List<Job> getAvailJobs() {
    return availJobs;
  }

  public Deque<Job> getJobs() {
    return jobs;
  }

  public int getNumJobs() { return jobs.size(); }

  public Double getCost() {
    return cost;
  }
}

class Scheduler {
//  static List<Double> PROCESS_TIMES = new ArrayList<>(List.of(0.0, 3.8951, 16.9654, 2.2260, 2.2260, 5.9416,
//      2.2260, 20.6727, 5.9416, 12.7247, 5.9416, 5.9416, 2.2260, 3.8951, 3.8951, 5.9416, 12.7247, 12.7247,
//      12.7247, 2.2260, 3.8951, 2.2260, 3.8951, 20.6727, 5.9416, 24.4191, 16.9654, 2.2260, 3.8951, 12.7247,
//      2.2260, 16.9654));
  static List<Double> PROCESS_TIMES = new ArrayList<>(List.of(0.0, 4.0, 17.0, 2.0, 2.0, 6.0, 2.0, 21.0,
  6.0, 13.0, 6.0, 6.0, 2.0, 4.0, 4.0, 6.0, 13.0, 13.0, 13.0, 2.0, 4.0, 2.0, 4.0, 21.0, 6.0, 25.0,
  17.0, 2.0, 4.0, 13.0, 2.0, 17.0));
  static List<Integer> DUE_DATES =
      new ArrayList<>(List.of(0, 172, 82, 18, 61, 93, 71, 217, 295, 290,
          287, 253, 307, 279, 73, 355, 34, 233, 77, 88, 122, 71, 181, 340, 141,
          209, 217, 256, 144, 307, 329, 269));
  static Double TOTAL_PROCESSING_TIME = PROCESS_TIMES.stream().mapToDouble(Double::doubleValue).sum();
  static int MAX_ITERATIONS = 100000;

  static List<Job> jobNums;
  static Map<Integer, String> jobNames;

  static String CSV_FILE_NAME = "bnbImproved.csv";

  public static void main(String[] args) {
    System.out.println("Hello :(");
    System.out.println("Total processing time is " + TOTAL_PROCESSING_TIME);

    initializeJobs();
    jobNames = initialiseJobNames();
    Schedule bestSchedule = branchAndBound();

    System.out.println("Final solution: " + bestSchedule.getJobs());

    System.out.println("Cost of final solution: " + bestSchedule.getCost());
    System.out.println("Tardiness of final solution: " + bestSchedule.getTardiness());

    writeToCSV(bestSchedule);
  }

  public static Schedule branchAndBound() {
    // Pending list of solutions mapped to lower bounds
    int largestPendingNodes = Integer.MIN_VALUE;
    PriorityQueue<Schedule> schedules
        = new PriorityQueue<>(Comparator.comparingDouble(Schedule::getCost)
                                        .thenComparing(Comparator.comparingInt(Schedule::getNumJobs)
                                            .reversed()));
    Schedule currSchedule = new Schedule(TOTAL_PROCESSING_TIME, jobNums);

    int iter = 0;

    while (iter < MAX_ITERATIONS) {
      System.out.println("Iteration: " + (iter+1));

      List<Job> unusedJobs = currSchedule.getAvailJobs();

      // Remove current solution from pending list of solutions if it can be branched on
      if (unusedJobs.size() > 0 && iter > 0) {
        schedules.poll();
      }

      // Branch on current solution
      PriorityQueue<Schedule> incomingSchedules
          = new PriorityQueue<>(Comparator.comparingDouble(Schedule::getCost)
                                  .thenComparing(Comparator.comparingInt(Schedule::getNumJobs)
                                  .reversed()));
      for (Job job : unusedJobs) {
        Schedule newSchedule = new Schedule(currSchedule);
        newSchedule.addJob(job);
        incomingSchedules.add(newSchedule);
      }

      //Double BEAM_WIDTH = (double)1/Math.pow(iter, 0.1);

      // Beam search: blahblahblah explanation
      int incomingNum = 2;
      //(int) Math.ceil(incomingSchedules.size() * BEAM_WIDTH);
      for (int i = 0; i < incomingNum && incomingSchedules.peek() != null; i++) {
        Schedule newSchedule = incomingSchedules.poll();
        schedules.add(newSchedule);
        // Phatoming
        if (newSchedule.getAvailJobs().isEmpty() && newSchedule != schedules.peek()) {
          Double cost = newSchedule.getCost();
          schedules.removeIf(n -> (n.getCost() > cost));
        }
      }

      largestPendingNodes = Math.max(largestPendingNodes, schedules.size());

      // Choose schedule with lowest lower bound (jumptracking)
      currSchedule = schedules.peek();
      System.out.println("Current solution: " + currSchedule.getJobs());
      System.out.println("Cost of current solution: " + currSchedule.getCost());
      System.out.println("Tardiness of current solution: " + currSchedule.getTardiness());

      if (currSchedule.getAvailJobs().isEmpty()) {
        System.out.println("Found complete solution with smallest lower bound "
            + currSchedule.getCost());
        System.out.println("Largest size reached by list of pending nodes: " + largestPendingNodes);
        return currSchedule;
      }
      iter++;
    }

    // Generate feasible solution using EDD + tie breaking with SPT
    System.out.println("Max iterations reached. Generating feasible solution from partial solution.");
    TreeSet<Job> earliestDueDates
        = new TreeSet<>(Comparator.comparingInt(Job::getDueDate)
        .thenComparingDouble(Job::getProcessingTime));
    earliestDueDates.addAll(currSchedule.getAvailJobs());
    while (!earliestDueDates.isEmpty()) {
      currSchedule.addJob(earliestDueDates.pollFirst());
      earliestDueDates.addAll(currSchedule.getAvailJobs());
    }

    System.out.println("Largest size reached by list of pending nodes: " + largestPendingNodes);

    return currSchedule;
  }

  private static Map<Integer, String> initialiseJobNames() {
    Map<Integer, String> jobNames = new HashMap<>();
    String[] names = {
        "onnx_1",
        "muse_1",
        "emboss_1",
        "emboss_2",
        "blur_1",
        "emboss_3",
        "vii_1",
        "blur_2",
        "wave_1",
        "blur_3",
        "blur_4",
        "emboss_4",
        "onnx_2",
        "onnx_3",
        "blur_5",
        "wave_2",
        "wave_3",
        "wave_4",
        "emboss_5",
        "onnx_4",
        "emboss_6",
        "onnx_5",
        "vii_2",
        "blur_6",
        "night_1",
        "muse_2",
        "emboss_7",
        "onnx_6",
        "wave_5",
        "emboss_8",
        "muse_3"
    };

    for (int i = 0; i < names.length; i++) {
      jobNames.put(i + 1, names[i]);
    }

    return jobNames;
  }

  private static void initializeJobs() {
    int jobCount = 31;
    Job[] allJobs = new Job[jobCount+1];
    for (int i = 1; i < jobCount+1; i++) {
      allJobs[i] = new Job(i);
      allJobs[i].setProcessingTime(PROCESS_TIMES.get(i));
      allJobs[i].setDueDate(DUE_DATES.get(i));
    }
    allJobs[31].setParents(new Job[]{allJobs[1]});
    allJobs[1].setParents(new Job[]{allJobs[2]});
    allJobs[2].setParents(new Job[]{allJobs[5]});
    allJobs[5].setParents(new Job[]{allJobs[11], allJobs[24], allJobs[12]});
    allJobs[11].setParents(new Job[]{allJobs[15]});
    allJobs[15].setParents(new Job[]{allJobs[16]});
    allJobs[16].setParents(new Job[]{allJobs[6], allJobs[17]});

    allJobs[6].setParents(new Job[]{allJobs[7]});
    allJobs[7].setParents(new Job[]{allJobs[8]});
    allJobs[8].setParents(new Job[]{allJobs[3], allJobs[9]});

    allJobs[3].setParents(new Job[]{allJobs[4]});
    allJobs[4].setParents(new Job[]{allJobs[30]});

    allJobs[9].setParents(new Job[]{allJobs[10]});
    allJobs[10].setParents(new Job[]{allJobs[30]});

    allJobs[17].setParents(new Job[]{allJobs[18]});
    allJobs[18].setParents(new Job[]{allJobs[19], allJobs[21]});
    allJobs[19].setParents(new Job[]{allJobs[20]});
    allJobs[20].setParents(new Job[]{allJobs[30]});

    allJobs[21].setParents(new Job[]{allJobs[22]});
    allJobs[22].setParents(new Job[]{allJobs[23]});
    allJobs[23].setParents(new Job[]{allJobs[30]});

    allJobs[12].setParents(new Job[]{allJobs[13]});
    allJobs[13].setParents(new Job[]{allJobs[14]});
    allJobs[14].setParents(new Job[]{allJobs[30]});

    allJobs[24].setParents(new Job[]{allJobs[25]});
    allJobs[25].setParents(new Job[]{allJobs[26]});
    allJobs[26].setParents(new Job[]{allJobs[27], allJobs[28]});
    allJobs[28].setParents(new Job[]{allJobs[29]});
    allJobs[27].setParents(new Job[]{allJobs[30]});
    allJobs[29].setParents(new Job[]{allJobs[30]});

    jobNums = new ArrayList<>(List.of(allJobs[31]));
  }

  private static void writeToCSV(Schedule schedule) {
    String jobs = schedule.getJobs().stream()
        .map(Job::toString)
        .collect(Collectors.joining(","));
    //System.out.println(jobs);
    File csvOutputFile = new File(CSV_FILE_NAME);
    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
      pw.println(jobs);
    } catch (FileNotFoundException e) {
      System.out.println("CSV file not found!");
    }
  }

}