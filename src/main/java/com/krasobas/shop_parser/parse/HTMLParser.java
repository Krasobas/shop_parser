package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.model.Product;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HTMLParser implements Parser {
    private Properties productConfig;

    public HTMLParser(Properties productConfig) {
        this.productConfig = productConfig;
    }

    @Override
    public List<Product> parse() {
        List<Product> products = new ArrayList<>();
        Connection connection = Jsoup.connect(productConfig.getProperty("app.url"));
        Predicate<Element> filter = element -> !element.select(productConfig.getProperty("product.link"))
                .first()
                .attr("href").
                isEmpty();
        try {
            Document document = connection.get();
            document.select(productConfig.getProperty("product.element"))
                    .stream()
                    .filter(filter)
                    .forEach(element -> products.add(createProduct(element)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    private Product createProduct(Element element) {
        Product product = new Product();
        Element title = element.select(productConfig.getProperty("product.title")).first();
        product.setTitle(title.text());
        Element price = element.select(productConfig.getProperty("product.price")).first();
        product.setPrice(price.text());
        String link = element.select(productConfig.getProperty("product.link"))
                .first()
                .attr("href");
        if (link.startsWith("/")) {
            link = getDomain().concat(link);
        }
        product.setLink(link);
        parseProductPage(product);
        return product;
    }

    private void parseProductPage(Product product) {
        Connection connection = Jsoup.connect(product.getLink());
        try {
            Document productPage = connection.get();
            product.setDescription(getProductDescription(productPage));
            if (!(productConfig.getProperty("product.info") == null)
                    && !productConfig.getProperty("product.info").isEmpty()) {
                product.setInfo(getProductInfo(productPage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getProductDescription(Document document) {
        return Objects.requireNonNull(document.select(productConfig.getProperty("product.description"))
                .first()).text();
    }

    private String getProductInfo(Document document) {
        return Objects.requireNonNull(document.select(productConfig.getProperty("product.info"))
                        .first())
                .children()
                .stream()
                .map(Element::text)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String getDomain() {
        String domain = "";
        Pattern pattern = Pattern.compile("((http[s]?|ftp)://)([^:^/]*)");
        Matcher matcher = pattern.matcher(productConfig.getProperty("app.url"));
        if (matcher.find()) {
            domain = matcher.group();
        }
        return domain;
    }
}
