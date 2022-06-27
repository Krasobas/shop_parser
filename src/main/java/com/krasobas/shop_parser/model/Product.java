package com.krasobas.shop_parser.model;

import com.opencsv.bean.CsvBindByName;
import java.util.Objects;

public class Product {
    private static int ID_GENERATOR = 0;

//    @CsvBindByName(column = "id")
    private int id;
//    @CsvBindByName(column = "title")
    private String title;
//    @CsvBindByName(column = "price")
    private String price;
//    @CsvBindByName(column = "link")
    private String link;
//    @CsvBindByName(column = "description")
    private String description;
//    @CsvBindByName(column = "info")
    private String info;

    public Product(int id, String title, String price, String link, String description, String info) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.link = link;
        this.description = description;
        this.info = info;
    }

    public Product() {
        this.id = ++ID_GENERATOR;
    }

    public Product(String title, String price, String link, String description, String info) {
        this.id = ++ID_GENERATOR;
        this.title = title;
        this.price = price;
        this.link = link;
        this.description = description;
        this.info = info;
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
                ", description='" + description + '\'' +
                ", info='" + info + '\'' +
                '}';
    }

    public String toHumainString() {
        return String.format("%nTitle: %s%nPrice: %s%nLink: %s%nDescription: %s%nInfo: %s%n",
                title, price, link, description, info);
    }
}
