package com.krasobas.shop_parser;

import com.krasobas.shop_parser.loader.ShopsLoader;
import com.krasobas.shop_parser.model.Product;
import com.krasobas.shop_parser.parse.ParserHTML;
import com.krasobas.shop_parser.parse.ParserPDF;
import com.krasobas.shop_parser.store.CSVStore;
import com.krasobas.shop_parser.store.Store;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class App {
    ShopsLoader loader = new ShopsLoader();
    private List<String> urlErrors = new ArrayList<>();
    private Store store = new CSVStore();

    public void run(String path) {
        File src = Paths.get(path).toFile();
        if (src.exists()) {
            if (src.isDirectory()) {
                Arrays.stream(Objects.requireNonNull(src.listFiles()))
                        .sequential()
                        .forEach(file -> setStrategy(file.getAbsolutePath()));
            } else {
                setStrategy(path);
            }
        }
    }

    private void setStrategy(String path) {
        if (path.endsWith(".txt")) {
            web(path);
        }
        if (path.endsWith(".pdf")) {
            pdf(path);
        }
    }

    public void web(String path) {
        Map<String, Set<String>> shops = loader.getShops(path);
        Properties config;
        ParserHTML parser = null;
        for (Map.Entry<String, Set<String>> entry : shops.entrySet()) {
            config = initConfig("shops/".concat(entry.getKey()));
            System.out.printf("Shop in parsing: %s%n", config.getProperty("shop.url"));
            if (parser != null) {
                parser.setConfig(config);
            }
            for (String url : entry.getValue()) {
                if (parser == null) {
                    parser = new ParserHTML(config, url);
                } else {
                    parser.setUrl(url);
                }
                System.out.printf("\tPage in parsing: %s%n", url);
                parser.parse();
                retryIfNeed(parser);
            }
            checkErrors(parser);
            List<Product> products = parser.getProducts();
            System.out.printf("%d products got from %s%n%n", products.size(), config.getProperty("shop.url"));
            store.store(products, config);
            parser.getProducts().clear();
        }
        if (parser != null) {
            parser.close();
        }
    }

    private void retryIfNeed(ParserHTML parser) {
        String ulrError = parser.getUrlError();
        if (!ulrError.isEmpty()) {
            parser.setUrl(ulrError);
            System.out.printf(" Retrying for page: %s%n", ulrError);
            parser.setUrlError("");
            parser.parse();
        }
        ulrError = parser.getUrlError();
        if (!ulrError.isEmpty()) {
            urlErrors.add(ulrError);
        }
    }

    private void checkErrors(ParserHTML parser) {
        if (!urlErrors.isEmpty()) {
            System.out.println("Pages which weren't parsed:");
            urlErrors.forEach(p -> System.out.printf(" * %s%n", p));
        }
        if (!parser.getProductErrors().isEmpty()) {
            System.out.println("Products which weren't got:");
            parser.getProductErrors().forEach(p -> System.out.printf(" * %s%n", p.getLink()));
            parser.getProducts().removeAll(parser.getProductErrors());
        }
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

    public void pdf(String path) {
        for (String resource : loader.getResources()) {
            if (path.contains(resource.split("\\.properties")[0])) {
                Properties config = initConfig("shops/".concat(resource));
                ParserPDF parser = new ParserPDF(config, path);
                List<Product> list = parser.parse();
                store.store(list, config);
                break;
            }

        }
    }

    public static void main(String[] args) {
        new App().run(args[0]);
    }

    private static void checkSite(String url, String css) {
        Connection connection = Jsoup.connect(url);
        try {
            Document document = connection.get();
            System.out.println(document.select(css).isEmpty());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
