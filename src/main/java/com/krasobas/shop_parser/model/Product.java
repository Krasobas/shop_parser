package com.krasobas.shop_parser.model;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.util.List;
import java.util.Objects;

public class Product {
    private static int ID_GENERATOR = 0;
    @CsvBindByName(column = "id", required = true)
    private int id;
    @CsvBindByName(column = "title", required = true)
    private String title;
    @CsvBindByName(column = "price", required = true)
    private String price;
    @CsvBindByName(column = "link", required = true)
    private String link;
    @CsvBindAndSplitByName(column = "images", required = true, elementType = String.class, splitOn = ",", writeDelimiter = "\r\n")
    private List<String> images;
    @CsvBindByName(column = "description", required = true)
    private String description;
    @CsvBindByName(column = "info")
    private String info;

    public Product() {
        this.id = ++ID_GENERATOR;
    }

    public Product(String link) {
        this.id = ++ID_GENERATOR;
        this.link = link;
    }

    public Product(int id, String title, String price, String link, String description, String info, List<String> images) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.link = link;
        this.description = description;
        this.info = info;
        this.images = images;
    }

    public Product(String title, String price, String link, String description, String info, List<String> images) {
        this.id = ++ID_GENERATOR;
        this.title = title;
        this.price = price;
        this.link = link;
        this.description = description;
        this.info = info;
        this.images = images;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id && title.equals(product.title) && Objects.equals(price, product.price) && link.equals(product.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, price, link);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price='" + price + '\'' +
                ", link='" + link + '\'' +
                ", images='" + images + '\'' +
                ", description='" + description + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

    public String toHumainString() {
        return String.format("%nTitle: %s%nPrice: %s%nLink: %s%nImages: %s%nDescription: %s%nInfo: %s%n",
                title, price, link, images, description, info);
    }
}
