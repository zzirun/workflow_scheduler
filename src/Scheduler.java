// Online Java Compiler
// Use this editor to write, compile and run your Java code online
import java.util.*;
//import org.json.*;

class Job {
  private Integer id;
  private Integer dueDate;
  private Integer processingTime;
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

  public void setProcessingTime(Integer processingTime) {
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

  public Integer getId() {
    return id;
  }

  public Integer getDueDate() {
    return dueDate;
  }

  public Integer getProcessingTime() {
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
  private int remainingTime;
  private List<Job> availJobs;
  private int cost;

  public Schedule(Schedule schedule) {
    this.jobs = new ArrayDeque<>(schedule.getJobs());
    this.availJobs = new ArrayList<>(schedule.getAvailJobs());
    this.remainingTime = schedule.getRemainingTime();
    this.cost = schedule.getCost();
  }

  public Schedule(int remainingTime, List<Job> availJobs) {
    this.remainingTime = remainingTime;
    this.availJobs = new ArrayList<>(availJobs);
    jobs = new ArrayDeque<>();
    cost = 0;
  }

  public void addJob(Job job) {
    int tardiness
        = Math.max(0, remainingTime - job.getDueDate());
    this.cost += tardiness;
    jobs.push(job);
    for (Job parent : job.getParents()) {
      if (jobs.containsAll(Arrays.asList(parent.getChildren()))) {
        availJobs.add(parent);
      }
    }
    availJobs.remove(job);
    remainingTime -= job.getProcessingTime();
    return;
  }

  public int getRemainingTime() {
    return remainingTime;
  }

  public List<Job> getAvailJobs() {
    return availJobs;
  }

  public Deque<Job> getJobs() {
    return jobs;
  }

  public int getCost() {
    return cost;
  }
}

class Scheduler {
  static List<Integer> PROCESS_TIMES = new ArrayList<>(List.of(0, 4, 17, 2, 2, 6, 2, 21, 6, 13, 6, 6, 2, 4, 4, 6, 13, 13, 13, 2, 4, 2, 4, 21, 6, 25, 17, 2, 4, 13, 2, 17));
  static List<Integer> DUE_DATES =
      new ArrayList<>(List.of(0, 172, 82, 18, 61, 93, 71, 217, 295, 290,
          287, 253, 307, 279, 73, 355, 34, 233, 77, 88, 122, 71, 181, 340, 141,
          209, 217, 256, 144, 307, 329, 269));
  static int TOTAL_PROCESSING_TIME = PROCESS_TIMES.stream().mapToInt(Integer::intValue).sum();
  static int MAX_ITERATIONS = 30000;

  static List<Job> jobNums;
  static Map<Integer, String> jobNames;

  public static void main(String[] args) {
    System.out.println("Hello :(");
    System.out.println("Total processing time is " + TOTAL_PROCESSING_TIME);

    initializeJobs();
    jobNames = initialiseJobNames();

    // parsing input.json for jobs and due dates
//    JSONParser parser = new JSONParser();
//    JSONObject json = (JSONObject) parser.parse(new FileReader("input.json"));
//    JSONObject deadlines = json.getJSONObject("workflow0").getJSONObject("due_dates");
//    JSONArray edges = json.getJSONObject("workflow0").getJSONArray("edge_set");

    Schedule bestSchedule = branchAndBound();

    System.out.println("Final solution: " + bestSchedule.getJobs());

    System.out.println("Cost of final solution: " + recalcCost(bestSchedule.getJobs()));
  }

  private static int recalcCost(Deque<Job> jobs) {
    int cost = 0;
    int duration = 0;
    for (Job job : jobs) {
      duration += job.getProcessingTime();
      cost += Math.max(0, duration - job.getDueDate());
    }
    return cost;
  }

  public static Schedule branchAndBound() {
    // Pending list of solutions mapped to lower bounds
    TreeMap<Integer, Deque<Schedule>> costs = new TreeMap<>();
    Schedule currSchedule = new Schedule(TOTAL_PROCESSING_TIME, jobNums);

    int iter = 0;
    int lowestBound;

    while (iter < MAX_ITERATIONS) {
      System.out.println("Iteration: " + (iter+1));

      List<Job> unusedJobs = currSchedule.getAvailJobs();

      // Remove current solution from pending list of solutions if it can be branched on
      if (unusedJobs.size() > 0 && iter > 0) {
        lowestBound = costs.firstKey();
        Deque<Schedule> lowestBoundSchedules = costs.get(lowestBound);
        lowestBoundSchedules.pop();
        //System.out.println("Removing schedule " + currSchedule.getJobs());
        if (lowestBoundSchedules.isEmpty()) {
          //System.out.println("Removing " + lowestBound);
          costs.remove(lowestBound);
        }
      }

      // Branch on current solution
      for (Job job : unusedJobs) {
        //System.out.println("Remaining time of curr schedule is " + currSchedule.getRemainingTime());
        Schedule newSchedule = new Schedule(currSchedule);
        newSchedule.addJob(job);
        int cost = newSchedule.getCost();
//        System.out.println("Adding job " + job + " with due date " + job.getDueDate());
        Deque<Schedule> schedules = costs.getOrDefault(cost, new ArrayDeque<>());
//        System.out.println("Adding schedule " + newSchedule.getJobs() + " with lower bound " + cost);
        schedules.push(newSchedule);
        costs.put(cost, schedules);
      }

      // Choose schedule with lowest lower bound (jumptracking)
      lowestBound = costs.firstKey();
      Deque<Schedule> lowestBoundSchedules = costs.get(lowestBound);
      Schedule lowestSchedule = lowestBoundSchedules.peek();
//      System.out.println("Lowest schedule: " + lowestSchedule.getJobs());
      currSchedule = lowestSchedule;
      System.out.println("Current solution: " + currSchedule.getJobs());
      System.out.println("Cost of current solution: " + currSchedule.getCost());

      if (currSchedule.getAvailJobs().isEmpty() && lowestBound <= costs.firstKey()) {
        System.out.println("Found complete solution with smallest lower bound " + lowestBound);
        return currSchedule;
      }
      iter++;
    }

    // Generate feasible solution using EDD + tie breaking with SPT
    System.out.println("Max iterations reached. Generating feasible solution from partial solution.");
    TreeSet<Job> earliestDueDates
        = new TreeSet<>(Comparator.comparingInt(Job::getDueDate)
        .thenComparingInt(Job::getProcessingTime));
    earliestDueDates.addAll(currSchedule.getAvailJobs());
    while (!earliestDueDates.isEmpty()) {
      currSchedule.addJob(earliestDueDates.pollFirst());
      earliestDueDates.addAll(currSchedule.getAvailJobs());
    }

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

}