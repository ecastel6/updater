package app.controllers;

import app.models.ArcadiaApp;
import app.models.ArcadiaAppData;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IntersectExample
{

    public static void main(String[] args) {
        ArcadiaAppData arcadiaAppData1 = new ArcadiaAppData(ArcadiaApp.CBOS, new File("/pp"), "81", "3.4.3");
        ArcadiaAppData arcadiaAppData2 = new ArcadiaAppData(ArcadiaApp.OPENCARD, new File("/pp2"), "80", "2.4.3");

        Map<String, ArcadiaAppData> map1 = new HashMap<>();
        Map<String, ArcadiaAppData> map2 = new HashMap<>();

        map1.put("cbos", arcadiaAppData1);

        map2.put("CBOS", arcadiaAppData1);
        map2.put("opencard", arcadiaAppData2);

        for (Object str : CollectionUtils.intersection(map1.keySet(), map2.keySet()))
            System.out.println(str.toString());

        /*Customer customer1 = new Customer(1, "Daniel", "locality1", "city1");
        Customer customer2 = new Customer(2, "Fredrik", "locality2", "city2");
        Customer customer3 = new Customer(3, "Kyle", "locality3", "city3");
        Customer customer4 = new Customer(4, "Bob", "locality4", "city4");
        Customer customer5 = new Customer(5, "Cat", "locality5", "city5");
        Customer customer6 = new Customer(6, "John", "locality6", "city6");

        List<Customer> list1 = Arrays.asList(customer1, customer2, customer3);
        List<Customer> list2 = Arrays.asList(customer4, customer5, customer6);
        List<Customer> list3 = Arrays.asList(customer1, customer2);

        List<Customer> linkedList1 = new LinkedList<>(list1);

        Collection<Customer> intersection = CollectionUtils.intersection(list1, list3);
        System.out.println(intersection.size());*/
    }
}
