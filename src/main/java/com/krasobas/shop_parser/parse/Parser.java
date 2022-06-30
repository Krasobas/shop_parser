package com.krasobas.shop_parser.parse;

import com.krasobas.shop_parser.model.Product;
import java.util.List;

public interface Parser {
    List<Product> parse();

    void close();
}
