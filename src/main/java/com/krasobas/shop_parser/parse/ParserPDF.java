package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.App;
import com.krasobas.shop_parser.model.Product;
import io.github.jonathanlink.PDFLayoutTextStripper;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ParserPDF implements Parser {
    private Properties config;
    private String path;
    private List<Product> products;

    public ParserPDF(Properties productConfig, String path) {
        this.config = productConfig;
        this.path = path;
    }

    @Override
    public List<Product> parse() {
        if (products == null) {
            products = new ArrayList<>();
        }
        products.addAll(readPDF().stream()
                .map(p -> {
                    Product product = new Product();
                    product.setTitle(findField(p, "title"));
                    System.out.printf("\t\t* Product in parsing: %s%n", product.getTitle());
                    product.setOrigin(findField(p, "origin"));
                    product.setDescription(findField(p, "description"));
                    product.setInfo(findField(p, "info"));
                    product.setPrice(findField(p, "price"));
                    return product;})
                .collect(Collectors.toList()));
        System.out.printf("%d products got from %s%n%n", products.size(), path);
        return products;
    }

    private List<String> readPDF() {
        String pdfContent = "";
        try {
            PDFParser pdfParser = new PDFParser(new RandomAccessFile(new File(path), "r"));
            System.out.printf("PDF in parsing: %s%n", path);
            pdfParser.parse();
            PDDocument pdDocument = new PDDocument(pdfParser.getDocument());
            PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
            pdfContent = pdfTextStripper.getText(pdDocument);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringJoiner toRemove = new StringJoiner("|");
        toRemove.add(config.getProperty("pdf.header"))
                .add(config.getProperty("pdf.categories"))
                .add(config.getProperty("pdf.errors"));
        pdfContent = pdfContent.replaceAll(toRemove.toString(), "");
        return Arrays.stream(pdfContent.split("\\n")).sequential()
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private String findField(String position, String field) {
        String rsl =  position.substring(
                Integer.parseInt(config.getProperty("product.".concat(field).concat(".start"))),
                Integer.parseInt(config.getProperty("product.".concat(field).concat(".end")))
        ).replaceAll(" {2,}", " ");
        if (rsl.isBlank()) {
            rsl = "";
        }
        if (rsl.startsWith(" ")){
            rsl = rsl.substring(1);
        }
        if (rsl.endsWith(" ")) {
            rsl = rsl.substring(0, rsl.length() - 1);
        }
        return rsl;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    private static void findIndexes(String...fileds) {
        for (int i = 0; i < fileds.length; i++) {
            System.out.println((i + 1) + ") " + fileds[i].length());
        }
    }

    public static void main(String[] args) throws IOException {
        ParserPDF parser = new ParserPDF(initConfig("shops/mercuriale_bratigny.properties"), "/Users/vasilijkrasov/Desktop/AMANDA/datasamanda/mercuriale_bratigny_04-07.pdf");
//        ParserPDF parser = new ParserPDF(initConfig("moorea.properties"), "/Users/vasilijkrasov/Desktop/AMANDA/datasamanda/Catalogue Leìgumes.pdf");
        List<String> lines = parser.readPDF().stream().map(s -> s.replaceAll(" ", "*")).collect(Collectors.toList());
        lines.forEach(System.out::println);

    }

    public static Properties initConfig(String properties) {
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
}
