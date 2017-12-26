/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muellerbruehl.parallelstreams;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 *
 * @author mmueller
 */
public class StreamsDemo {

  private static final long[] result = new long[1];

  public static void main(String[] args) {

    showParallelProblem();
    /*
    System.out.println("\nstarted creating persons - takes a while...");
    Persons persons = getPersons();
    System.out.println("created " + persons.getPersons().size() + " persons.\n");
    countVendors(persons.getPersons());
    */
  }

  private static Persons getPersons() {
    return invokeMethod("Creation of persons", Persons::getInstance);
  }

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

  private static Void parallelReduce() {
    for (int i = 0; i < 10; i++) {
      long preduce = getRange().parallel().reduce(0, StreamsDemo::orderDependentFunction);
      System.out.println("parallel reduce: " + preduce);
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

  private static void countVendors(List<Person> persons) {
    invokeMethod("Vendors by counting list", () -> getVendorCount(persons));
    
    Supplier<Long> countByStream = () -> persons.stream().filter(p -> p.isVendor()).collect(Collectors.counting());
    invokeMethod("Vendors by Stream", countByStream);
    Supplier<Long> countByParallelStream = () -> persons.parallelStream().filter(p -> p.isVendor()).collect(Collectors.counting());
    invokeMethod("Vendors by ParallelStream", countByParallelStream);
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

  @SuppressWarnings("UnusedReturnValue")
  private static <T> T invokeMethod(String info, Supplier<T> method) {
    long start = System.nanoTime();
    T result = method.get();
    long elapsedTime = System.nanoTime() - start;
    System.out.println(info + ": took " + elapsedTime / 1000000 + " ms");
    return result;
  }

}
