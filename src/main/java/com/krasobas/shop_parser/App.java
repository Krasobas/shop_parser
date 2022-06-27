package com.krasobas.shop_parser;

import com.krasobas.shop_parser.model.Product;
import com.krasobas.shop_parser.parse.HTMLParser;
import com.krasobas.shop_parser.parse.Parser;
import com.krasobas.shop_parser.store.CSVStore;
import com.krasobas.shop_parser.store.Store;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class App {

    public static void main(String[] args) {
        if (inputValidation(args)) {
            Properties config = getConfig(args[0]);
            if (configValidation(config)) {
            Parser parser = new HTMLParser(config);
            List<Product> resultList = parser.parse();
            Store store = new CSVStore();
            store.store(resultList, config);
            resultList = store.getList(config);
            for (Product product : resultList) {
                System.out.println(product);
            }} else {
                System.out.println("Please check if your properties file has all required keys.");
            }
        } else {
            System.out.println("Invalid configuration file was entered. Only \".properties\" extension is acceptable.");
        }
    }

    private static Properties getConfig(String properties) {
        Properties config = new Properties();
        try (InputStream in = App.class.getClassLoader()
                .getResourceAsStream(properties)) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static boolean inputValidation(String[] args) {
        return Pattern.matches(".+\\.properties", args[0]);
    }

    private static boolean configValidation(Properties config) {
        List<String> expected = List.of("app.url", "app.store.csv",
                "product.element", "product.title", "product.link", "product.price", "product.description");
        return config.keySet().containsAll(expected);
    }
}
