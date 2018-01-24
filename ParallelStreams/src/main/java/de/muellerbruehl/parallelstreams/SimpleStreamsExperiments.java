package de.muellerbruehl.parallelstreams;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.muellerbruehl.parallelstreams.StreamsDemo.invokeMethod;

public class SimpleStreamsExperiments {

    public static void main(String[] args) {
        simpleStreamsExperiments(Persons.getInstance().getPersons());
    }

    public static void simpleStreamsExperiments(List<Person> persons) {
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

}
