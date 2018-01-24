package de.muellerbruehl.parallelstreams;

import java.util.stream.LongStream;

import static de.muellerbruehl.parallelstreams.StreamsDemo.invokeMethod;

public class ParallelProblem {

    private static final long[] result = new long[1];

    public static void main(String[] args) {
        showParallelProblem();
    }

    public static void showParallelProblem() {

        invokeMethod("Serial", ParallelProblem::serial);
        invokeMethod("Parallel", ParallelProblem::parallel);
        invokeMethod("ParallelOrdered", ParallelProblem::parallelOrdered);

        // Reduce runs faster alone than it does after "ParallelOrdered"
        invokeMethod("Reduce", ParallelProblem::reduce);

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
            long reduce = getRange().reduce(0, ParallelProblem::orderDependentFunction);
            System.out.println("serial reduce: " + reduce);
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static Void parallelReduce() {
        for (int i = 0; i < 10; i++) {
            long reduce = getRange().parallel().reduce(0, ParallelProblem::orderDependentFunction);
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

}
