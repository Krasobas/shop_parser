package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.App;
import com.krasobas.shop_parser.model.Product;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SeleniumParser {
    private ChromeDriver driver;

    public SeleniumParser() {
        init();
    }

    public void init() {
        System.setProperty("webdriver.chrome.driver", "./drivers/chromedriver");
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    public void siteConnection(){

    }

    public Product parseDynamicPage(Properties config, String url) {
        Product product = new Product(url);
        try {
            driver.get(url);
            product.setTitle(driver.findElement(By.cssSelector(config.getProperty("product.title"))).getText());
            siteConnection();
            product.setPrice(driver.findElement(By.cssSelector(config.getProperty("product.price"))).getText());
            product.setDescription(driver.findElement(By.cssSelector(config.getProperty("product.description")))
                    .getText().replaceAll("\n", " "));
            product.setImages(new ArrayList<>(List.of(driver.findElement(By.cssSelector(config.getProperty("product.images")))
                    .getAttribute(config.getProperty("product.images.attr")))));
        } catch (Exception e) {
            System.out.println("Something wrong");
            e.printStackTrace();
        }
        return product;
    }

    public ChromeDriver getDriver() {
        return driver;
    }

    public void setDriver(ChromeDriver driver) {
        this.driver = driver;
    }

    public static void main(String[] args) {
        SeleniumParser sp = new SeleniumParser();
        System.out.println(sp.parseDynamicPage(sp.initConfig("shop.ledelas.properties"),
                "https://shop.ledelas.fr/index.html#Product=Product_1883"));

    }

    public Properties initConfig(String properties) {
        Properties config = new Properties();
        try (InputStream in = SeleniumParser.class.getClassLoader()
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
