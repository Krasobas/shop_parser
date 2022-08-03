package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.model.Product;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParserHTML implements Parser, AutoCloseable {
    private Properties config;
    private String url;
    private SeleniumParser selenium;
    private List<Product> products;
    private List<Product> productErrors;
    private String urlError = "";

    public ParserHTML(Properties productConfig, String url) {
        this.config = productConfig;
        this.url = url;
    }

    @Override
    public List<Product> parse() {
        if (products == null) {
            products = new ArrayList<>();
        }
        if (config.containsKey("shop.type") && "dynamic".equals(config.getProperty("shop.type"))) {
            if (selenium == null) {
                selenium = new SeleniumParser();
            }
            Product product = selenium.parseDynamicPage(config, url);
            if ("true".equals(config.getProperty("product.images.carousel"))) {
                product.getImages().replaceAll(img -> img = setImgSize(img));
            }
            products.add(product);
        } else {
            Connection connection = Jsoup.connect(url);
            try {
                Document document = connection.get();
                Elements gallery = document.select(config.getProperty("product.element"));
                if (!gallery.isEmpty()) {
                    parseGallery(gallery);
                } else if (products.stream().noneMatch(p -> p.getLink().equals(url))) {
                    Product product = new Product(url);
                    products.add(parseProductPage(product));
                }

            } catch (IOException e) {
                setUrlError(url);
                e.printStackTrace();
            }
        }
        products.sort(Comparator.comparingInt(Product::getId));
        return products;
    }

    private void parseGallery(Elements gallery) {
        Predicate<Element> filter = element -> !Objects.requireNonNull(element.select(config.getProperty("product.link"))
                        .first())
                .attr("href").
                isEmpty();
        gallery.stream()
                .filter(filter)
                .forEach(element -> {
                    String link = getProductLink(element);
                    if (products.stream().noneMatch(p -> p.getLink().equals(link))) {
                        Product product = new Product(link);
                        System.out.printf("     * Product in parsing: %s%n", product.getLink());
                        products.add(parseProductPage(product));
                    }
                });
        if (productErrors != null && !productErrors.isEmpty()) {
            retryParsingProductPage();
        }
    }

    private void retryParsingProductPage() {
        System.out.println("    Some errors were detected: ");
        Product currentProduct;
        for (int i = 0; i < productErrors.size(); i++) {
            currentProduct = productErrors.get(i);
            System.out.printf("     * Retrying for product: %s%n", currentProduct.getLink());
            products.remove(currentProduct);
            productErrors.remove(i);
            products.add(parseProductPage(currentProduct));
        }
    }

    private Product parseProductPage(Product product) {
        Connection connection = Jsoup.connect(product.getLink());
        try {
            Document productPage = connection.get();
            product.setTitle(getProductField(productPage, "product.title"));
            product.setPrice(getProductField(productPage, "product.price"));
            product.setDescription(getProductField(productPage, "product.description"));
            product.setImages(getProductImages(productPage));
            if (!(config.getProperty("product.info") == null)
                    && !config.getProperty("product.info").isEmpty()) {
                product.setInfo(getProductInfo(productPage, product.getDescription()));
            }
        } catch (IOException e) {
            registerError(product);
            e.printStackTrace();
        }
        return product;
    }

    private void registerError(Product product) {
        if (productErrors == null) {
            productErrors = new ArrayList<>();
        }
        productErrors.add(product);
    }

    private String getProductField(Document productPage, String property) {
        StringJoiner rsl = new StringJoiner(" ");
        String staticCSS = config.getProperty(property);
        if (!staticCSS.isEmpty()) {
            Arrays.stream(staticCSS.split(";"))
                    .sequential()
                    .forEach( css -> {
                        Elements elements = productPage.select(css);
                        if (!elements.isEmpty()) {
                            rsl.add(elements.first().text());
                        }
                            });
        }
        if ("product.price".equals(property)) {
            String dynamicCSS = config.getProperty(property.concat(".dynamic"));
            if (dynamicCSS != null && dynamicCSS.equals("true")
                    && !productPage.select(config.getProperty(property.concat(".dynamic.check"))).isEmpty()) {
                rsl.add(getProductFieldByAction(productPage.location(), property));
            }
        }
        return rsl.toString();
    }

    private String getProductFieldByAction(String url, String property) {
        StringJoiner rsl = new StringJoiner(System.lineSeparator());
        if (selenium == null) {
            selenium = new SeleniumParser();
        }
        return selenium.getField(config, url, property, 3);
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

    private String getProductInfo(Document productPage, String description) {
        StringJoiner rsl = new StringJoiner(System.lineSeparator());
        String prefix = config.getProperty("product.info.filter");
        Predicate<String> filter = prefix == null || prefix.isEmpty() ? s -> true : s -> !s.startsWith(prefix);
        Arrays.stream(config.getProperty("product.info").split(";"))
                .sequential()
                .forEach(css -> rsl.add(productPage.select(css)
                        .stream()
                        .map(Element::text)
                        .filter(s -> !s.isEmpty() && !s.equals(description))
                        .filter(filter)
                        .collect(Collectors.joining(System.lineSeparator()))));

        return rsl.toString();
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
                    if (!link.startsWith("http")) {
                        if (link.startsWith("//")) {
                            src.append("https:");
                        } else {
                            src.append(getDomain());
                        }
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
        return rsl.isEmpty() ? img : rsl.toString();
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
        return products == null ? Collections.emptyList(): products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Product> getProductErrors() {
        return productErrors == null ? Collections.emptyList() : productErrors;
    }

    public void setProductErrors(List<Product> productErrors) {
        this.productErrors = productErrors;
    }

    public String getUrlError() {
        return urlError;
    }

    public void setUrlError(String urlError) {
        this.urlError = urlError;
    }

    @Override
    public void close() {
        if (selenium != null) {
            selenium.getDriver().quit();
        }
    }
}
