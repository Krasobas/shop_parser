package com.krasobas.shop_parser;

import com.krasobas.shop_parser.loader.ShopsLoader;
import com.krasobas.shop_parser.parse.HTMLParser;
import com.krasobas.shop_parser.store.CSVStore;
import com.krasobas.shop_parser.store.Store;


import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class App {

    public void run(ShopsLoader loader) {
        Map<String, Set<String>> shops = loader.getShops();
        Properties config;
        HTMLParser parser = null;
        Store store = new CSVStore();
        for (Map.Entry<String, Set<String>> entry : shops.entrySet()) {
            config = initConfig(entry.getKey());
            System.out.printf("Shop in parsing: %s%n", config.getProperty("shop.url"));
            if (parser != null) {
                parser.setConfig(config);
            }
            for (String url : entry.getValue()) {
                if (parser == null) {
                    parser = new HTMLParser(config, url);
                } else {
                    parser.setUrl(url);
                }
                System.out.printf("\tPage in parsing: %s%n", url);
                parser.parse();
            }
            System.out.printf("%d products got from %s%n%n", parser.getProducts().size(), config.getProperty("shop.url"));
            store.store(parser.getProducts(), config);
            parser.getProducts().clear();
        }
        parser.close();
    }

    private Properties initConfig(String properties) {
        Properties config = new Properties();
        try (InputStream in = App.class.getClassLoader()
                .getResourceAsStream(properties)) {
            if (in != null) {
                config.load(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public static void main(String[] args) {
        App app = new App();
        ShopsLoader loader = new ShopsLoader(args[0]);
        app.run(loader);
    }



    private static boolean inputValidation(String[] args) {
        return Pattern.matches(".+\\.properties", args[0]);
    }

    private static boolean configValidation(Properties config) {
        List<String> expected = new ArrayList<>(List.of("app.url", "app.store.csv",
                "product.element", "product.title", "product.link",
                "product.price", "product.description", "product.images",
                "product.images.attr", "product.images.carousel"));
        if (config.keySet().containsAll(expected) && "true".equals(config.getProperty("product.images.carousel"))) {
            expected.addAll(List.of("product.images.pattern", "product.images.groups", "product.images.size"));
        }
        return config.keySet().containsAll(expected);
    }
}
