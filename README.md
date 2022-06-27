# shop_parser

This project represent a configurable shop pages parser.

To work with the application use the command below:

`java -jar target/shop_parser.jar pointedepenmarch.properties; open result.csv`


- `pointedepenmarch.properties` - is a config file.

The config file has a number of keys:

REQUIRED:

- `app.url:` - the page with a list of products to parse
- `app.store.csv:` - the path to a result CSV file

The keys below are CSS queries for a product and its fields:

- `product.element:`
- `product.title:`
- `product.link:`
- `product.price:`
- `product.description:`

OPTIONAL:

- `product.info:` 