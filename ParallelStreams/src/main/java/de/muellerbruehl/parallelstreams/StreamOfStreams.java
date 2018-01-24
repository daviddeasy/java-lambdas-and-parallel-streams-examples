package de.muellerbruehl.parallelstreams;

import java.util.List;

import static de.muellerbruehl.parallelstreams.StreamsDemo.invokeMethod;

public class StreamOfStreams {

    public static void main(String[] args) {
        experiment(Persons.getInstance().getPersons());
    }

    public static void experiment(List<Person> persons) {
        for (int i=0; i< 10; i++) {
            invokeMethod("", () -> sumQuantityFromStreamOfStreams(persons));
            invokeMethod("", () -> sumQuantityFromFlatmapStreamOfStreams(persons));
            invokeMethod("", () -> flatMapWithParallelStreams(persons));
            System.out.println();
        }
    }

    private static Void sumQuantityFromStreamOfStreams(List<Person> persons) {

        // NOTE the difference between Stream<Long> and LongStream
        // Here is my first cut (commented out) with local variable extraction
        // to show Stream<Long> conversion to LongStream

//    Stream<Long> totalItemsForSalePerVendor = persons.stream()
//            .filter(Person::isVendor)
//            .map(p -> p.getSelling().values().stream()
//                    .mapToLong(ArticleInfo::getQuantity)
//                    .sum());
//
//    LongStream longStream = totalItemsForSalePerVendor
//            .mapToLong(i -> i);
//
//    long totalItemsForSale = longStream
//            .sum();
//    System.out.println("totalItemsForSale = " + totalItemsForSale);

        long totalItemsForSale = persons.stream()
                .filter(Person::isVendor)
                .mapToLong(p -> p.getSelling().values().stream()
                        .mapToLong(ArticleInfo::getQuantity)
                        .sum())
                .sum();
        System.out.println("totalItemsForSale = " + totalItemsForSale);

        // to wrap with invokeMethod()
        return null;
    }

    private static Void sumQuantityFromFlatmapStreamOfStreams(List<Person> persons) {
        long totalItemsForSale = persons.stream()
                .filter(Person::isVendor)
                .flatMapToLong(p -> p.getSelling().values().stream()
                        .mapToLong(ArticleInfo::getQuantity))
                .sum();
        System.out.println("Flatmap: totalItemsForSale = " + totalItemsForSale);
        return null;
    }

    private static Void flatMapWithParallelStreams(List<Person> persons) {
        long totalItemsForSale = persons.parallelStream()
                .filter(Person::isVendor)
                .flatMapToLong(p -> p.getSelling().values().parallelStream()
                        .mapToLong(ArticleInfo::getQuantity))
                .sum();
        System.out.println("Parallel streams Flatmap: totalItemsForSale = " + totalItemsForSale);
        return null;
    }

}
