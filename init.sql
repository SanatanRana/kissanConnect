CREATE DATABASE IF NOT EXISTS kisan_user_db;
CREATE DATABASE IF NOT EXISTS kisan_product_db;
CREATE DATABASE IF NOT EXISTS kisan_order_db;
GRANT ALL PRIVILEGES ON kisan_user_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON kisan_product_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON kisan_order_db.* TO 'root'@'%';
FLUSH PRIVILEGES;