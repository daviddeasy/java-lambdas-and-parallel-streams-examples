/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muellerbruehl.parallelstreams;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 *
 * @author mmueller
 */
public class StreamsDemo {

  private static final long[] result = new long[1];

  @SuppressWarnings({"ConstantIfStatement", "ConstantConditions"})
  public static void main(String[] args) {

    if (false) showParallelProblem();

    System.out.println("\nStarted creating persons - takes a while...");
    List<Person> persons = getPersons().getPersons();
    System.out.println("Created " + persons.size() + " persons.\n");

    if (false) simpleStreamsExperiments(persons);

    new StreamOfStreams(persons).experiment();
  }

  private static Persons getPersons() {
    return invokeMethod("Creation of persons", Persons::getInstance);
  }

  //-------------------------------------------------------------

  private static void showParallelProblem() {

    invokeMethod("Serial", StreamsDemo::serial);
    invokeMethod("Parallel", StreamsDemo::parallel);
    invokeMethod("ParallelOrdered", StreamsDemo::parallelOrdered);

    // Reduce runs faster alone than it does after "ParallelOrdered"
    invokeMethod("Reduce", StreamsDemo::reduce);

//    invokeMethod("Parallel Reduce", StreamsDemo::parallelReduce);

  }

  private static Void serial() {
    for (int i = 0; i < 10; i++) {
      result[0] = 0;
      getRange().forEach(n -> result[0] = orderDependentFunction(result[0], n));
      System.out.println("serial: " + result[0]);
    }
    return null;
  }

  private static Void parallel() {
    for (int i = 0; i < 10; i++) {
      result[0] = 0;
      getRange().parallel().forEach(n -> result[0] = orderDependentFunction(result[0], n));
      System.out.println("parallel: " + result[0]);
    }
    return null;
  }

  private static Void parallelOrdered() {
    for (int i = 0; i < 10; i++) {
      result[0] = 0;
      getRange().parallel().forEachOrdered(n -> result[0] = orderDependentFunction(result[0], n));
      System.out.println("parallel ordered: " + result[0]);
    }
    return null;
  }

  private static Void reduce() {
    for (int i = 0; i < 10; i++) {
      long reduce = getRange().reduce(0, StreamsDemo::orderDependentFunction);
      System.out.println("serial reduce: " + reduce);
    }
    return null;
  }

  @SuppressWarnings("unused")
  private static Void parallelReduce() {
    for (int i = 0; i < 10; i++) {
      long reduce = getRange().parallel().reduce(0, StreamsDemo::orderDependentFunction);
      System.out.println("parallel reduce: " + reduce);
    }
    return null;
  }

  private static LongStream getRange() {
    return LongStream.range(0, 100000000);
  }

  private static long orderDependentFunction(long a, long c) {
    // Negative values caused by overflow look like an error
    return Math.abs((a + c) * c);
  }

//------------------------------------------------------------------------------------

  private static void simpleStreamsExperiments(List<Person> persons) {
    countVendorsIn(persons);

    System.out.println("Simple streams experiments: \n");
    findYoungGirlsIn(persons);
    averageAgeOfAllGirlsUnder20In(persons);
    averageAgeOfNoSuchPeople(persons);
    youngestManIn(persons);
  }

  private static void countVendorsIn(List<Person> persons) {
    System.out.println("Comparing counting a filtered stream in different ways:\n");
    Long numVendors = invokeMethod("Vendors by counting list with external iterator", () -> getVendorCount(persons));
    System.out.println("numVendors = " + numVendors);

    Supplier<Long> countByStream = () -> (Long) persons.stream().filter(Person::isVendor).count();
    numVendors = invokeMethod("Vendors by Stream", countByStream);
    System.out.println("numVendors = " + numVendors);

    Supplier<Long> countByParallelStream = () -> (Long) persons.parallelStream().filter(Person::isVendor).count();
    numVendors = invokeMethod("Vendors by ParallelStream", countByParallelStream);
    System.out.println("numVendors = " + numVendors + "\n");
  }

  private static long getVendorCount(List<Person> persons) {
    int counter = 0;
    for (Person person : persons) {
      if (person.isVendor()) {
        counter++;
      }
    }
    return counter;
  }

  private static void findYoungGirlsIn(List<Person> persons) {
    List<Person> youngGirls = persons.stream()
            .filter(Person::isFemale)
            .filter(p -> p.getAge() < 20)
            .collect(Collectors.toList());
    System.out.println("Number of young girls = " + youngGirls.size());

    System.out.println("Young Girls = " + youngGirls.stream()
            .map(p -> p.getGivenName() + " " + p.getSurname())
            .collect(Collectors.toList()));
  }

  private static void averageAgeOfAllGirlsUnder20In(List<Person> persons) {
    collectAverageAgeOfAllGirlsUnder(20, persons);
    averageAgeOfAllGirlsUnder(20, persons);
  }

  // To see what happens if the filtered stream is empty
  // Collectors.averagingInt(): "If no elements are present, the result is 0."
  private static void averageAgeOfNoSuchPeople(List<Person> persons) {
    collectAverageAgeOfAllGirlsUnder(10, persons);
    averageAgeOfAllGirlsUnder(10, persons);
  }

  private static void collectAverageAgeOfAllGirlsUnder(int age, List<Person> persons) {
    Double meanAge = persons.stream()
            .filter(Person::isFemale)
            .filter(p -> p.getAge() < age)
            .collect(Collectors.averagingInt(Person::getAge));
    if (meanAge > 0) {
      System.out.println("Mean age of girls under " + age + " (via Collectors.averagingInt()) = " + meanAge);
    } else {
      System.out.println("No girls under " + age);
    }
  }

  private static void averageAgeOfAllGirlsUnder(int age, List<Person> persons) {
    OptionalDouble meanAge = persons.stream()
            .filter(Person::isFemale)
            .filter(p -> p.getAge() < age)
            .mapToInt(Person::getAge)
            .average();
    if (meanAge.isPresent()) {
      // String.format("%.2f", meanAge.getAsDouble())
      System.out.println("Mean age of girls under " + age + " (via IntStream.average()) = " + meanAge.getAsDouble());
    } else {
      System.out.println("No girls under " + age);
    }
  }

  private static void youngestManIn(List<Person> persons) {

    Optional<Person> youngestMan = persons.stream()
            .filter(p -> !p.isFemale())
            .min(Comparator.comparingInt(Person::getAge));

    if (!youngestMan.isPresent()) {
      System.out.println("There are no men");
    } else {
      int youngestAge = youngestMan.get().getAge();

      List<String> youngestMen = persons.stream()
              .filter(p -> p.getAge() == youngestAge)
              .filter(p -> !p.isFemale())
              .map(Person::getName)
              .collect(Collectors.toList());

      Person youngest = youngestMan.get();
      System.out.println("Youngest man = " + youngest.getName() + ", age = " + youngest.getAge());

      if (youngestMen.size() > 1) {
        System.out.println("There are " + youngestMen.size() + " men of the same age (" + youngest.getAge() + "); full list:");
        System.out.println("youngestMen = " + youngestMen);
      }
    }
  }

  @SuppressWarnings("UnusedReturnValue")
  static <T> T invokeMethod(String info, Supplier<T> method) {
    long start = System.nanoTime();
    T result = method.get();
    long elapsedTime = System.nanoTime() - start;
    System.out.println(info + ": took " + elapsedTime / 1000000 + " ms");
    return result;
  }

}
