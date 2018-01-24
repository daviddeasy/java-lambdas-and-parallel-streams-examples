/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.muellerbruehl.parallelstreams;

import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author mmueller
 */
public class StreamsDemo {

  public static void main(String[] args) {

    ParallelProblem.showParallelProblem();

    List<Person> persons = getPersons().getPersons();

    SimpleStreamsExperiments.simpleStreamsExperiments(persons);

    StreamOfStreams.experiment(persons);
  }

  private static Persons getPersons() {
    return invokeMethod("Creation of persons", Persons::getInstance);
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
