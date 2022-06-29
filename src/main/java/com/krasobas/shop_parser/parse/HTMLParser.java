package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.model.Product;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private Properties config;
    private String url;
    private List<Product> products;

    public HTMLParser(Properties productConfig, String url) {
        this.config = productConfig;
        this.url = url;
    }

    @Override
    public List<Product> parse() {
        if (products == null) {
            products = new ArrayList<>();
        }
        Connection connection = Jsoup.connect(url);
        try {
            Document document = connection.get();
            Elements gallery = document.select(config.getProperty("product.element"));
            if (!gallery.isEmpty()) {
                parseGallery(gallery);
            } else {
                Product product = new Product(url);
                products.add(parseProductPage(product));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }

    private void parseGallery(Elements gallery) {
        Predicate<Element> filter = element -> !element.select(config.getProperty("product.link"))
                .first()
                .attr("href").
                isEmpty();
        gallery.stream()
                .filter(filter)
                .forEach(element -> {
                    Product product = new Product(getProductLink(element));
                    products.add(parseProductPage(product));
                });
    }

    private Product parseProductPage(Product product) {
        Connection connection = Jsoup.connect(product.getLink());
        try {
            Document productPage = connection.get();
            product.setTitle(getProductField(productPage, config.getProperty("product.title")));
            product.setPrice(getProductField(productPage, config.getProperty("product.price")));
            product.setDescription(getProductField(productPage, config.getProperty("product.description")));
            product.setImages(getProductImages(productPage));
            if (!(config.getProperty("product.info") == null)
                    && !config.getProperty("product.info").isEmpty()) {
                product.setInfo(getProductInfo(productPage));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return product;
    }

    private String getProductField(Document productPage, String cssQuery) {
        return Objects.requireNonNull(productPage.select(cssQuery)
                .first()).text();
    }

    private String getProductLink(Element element) {
        String link = element.select(config.getProperty("product.link"))
                .first()
                .attr("href");
        if (link.startsWith("/")) {
            link = getDomain().concat(link);
        }
        return link;
    }

    private String getProductInfo(Document productPage) {
        return Objects.requireNonNull(productPage.select(config.getProperty("product.info"))
                        .first())
                .children()
                .stream()
                .map(Element::text)
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private List<String> getProductImages(Document productPage) {
        List<String> images = new ArrayList<>();
        StringBuilder src = new StringBuilder();
        productPage.select(config.getProperty("product.images")).forEach(
                img -> {
                    /**
                     * TODO: check with all sites and refactor
                     */
                    String link = img.attr(config.getProperty("product.images.attr"));
                    if (link.startsWith("//")) {
                        src.append("https:");
                    } else {
                        src.append(getDomain());
                    }
                    if ("true".equals(config.getProperty("product.images.carousel"))) {
                        src.append(setImgSize(img.attr(config.getProperty("product.images.attr"))));
                    } else {
                        src.append(img.attr(config.getProperty("product.images.attr")));
                    }
                    images.add(src.toString());
                    src.setLength(0);
                });
        if (images.isEmpty()) {
            images.add("There is no any image.");
        }
        return images;
    }

    private String setImgSize(String img) {
        Pattern pattern = Pattern.compile(config.getProperty("product.images.pattern"));
        Matcher matcher = pattern.matcher(img);
        String[] groups = config.getProperty("product.images.groups").split(":");
        StringBuilder rsl = new StringBuilder();
        if (matcher.find()) {
            rsl.append(matcher.group(Integer.parseInt(groups[0])))
                    .append(config.getProperty("product.images.size"))
                    .append(matcher.group(Integer.parseInt(groups[1])));

        }
        return rsl.toString();
    }

    private String getDomain() {
        String domain = "";
        Pattern pattern = Pattern.compile("((http[s]?|ftp)://)([^:^/]*)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            domain = matcher.group();
        }
        return domain;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
