package com.krasobas.shop_parser.store;

import com.krasobas.shop_parser.model.Product;

import java.util.List;
import java.util.Properties;

public interface Store {
    void store(List<Product> products, Properties config);

    List<Product> getList(Properties config);
}
