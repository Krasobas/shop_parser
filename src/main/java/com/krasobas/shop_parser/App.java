package com.krasobas.shop_parser;

import com.krasobas.shop_parser.model.Product;
import com.krasobas.shop_parser.parse.HTMLParser;
import com.krasobas.shop_parser.store.CSVStore;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class App {

    public static void main(String[] args) {
        Properties config = getConfig(args[0]);
        HTMLParser parser = new HTMLParser(config);
        List<Product> resultList = parser.parse();
        CSVStore store = new CSVStore();
        store.store(resultList, config);
        resultList = store.getList(config);
        for (Product product : resultList) {
            System.out.println(product);
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
}
