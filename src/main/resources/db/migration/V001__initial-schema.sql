create table role
(
  id                bigint auto_increment primary key,
  authority         varchar(32) not null,
  constraint UK_role1
    unique (authority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table country
(
  id               bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name             varchar(55) not null,
  full_name        varchar(55) not null,
  code             varchar(2) not null,
  continent_code   varchar(2) not null,
  iso3             varchar(3) not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_country
  unique (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table user
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  deleted_datetime datetime null,
  phone_number varchar(15) not null,
  accepted_terms bit not null,
  account_non_expired bit not null,
  account_non_locked bit not null,
  credentials_non_expired bit not null,
  enabled bit not null,
  failed_login_attempts int not null,
  first_name varchar(32) null,
  password varchar(128) null,
  country_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_user
    unique (phone_number),
  constraint FK_user_country
    foreign key (country_id) references country (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table user_roles
(
  user_id bigint not null,
  roles_id bigint not null,
  primary key (user_id, roles_id),
  constraint FK_user_roles_user
    foreign key (user_id) references user (id),
  constraint FK_user_roles_role
    foreign key (roles_id) references role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table phone
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  code varchar(3) not null,
  number varchar(15) not null,
  created_by       bigint null,
  updated_by       bigint null,
  phone_user_id bigint null,
  country_id bigint null,
  principal bit null,
  confirmation_code varchar(6) null,
  confirmed bit not null,
  constraint UK_phone
    unique (number),
  constraint FK_phone_user
    foreign key (phone_user_id) references user (id),
  constraint FK_phone_country
    foreign key (country_id) references country (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table chart_of_accounts
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table company
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name             varchar(256) not null,
  chart_of_accounts_id bigint not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_company_chart_of_accounts
  foreign key (chart_of_accounts_id) references chart_of_accounts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table company_owners
(
  company_id bigint not null,
  owners_id bigint not null,
  primary key (company_id, owners_id),
  constraint FK_company_owners_company
  foreign key (company_id) references company (id),
  constraint FK_company_owners_user
  foreign key (owners_id) references user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table company_employees
(
  company_id bigint not null,
  employees_id bigint not null,
  primary key (company_id, employees_id),
  constraint FK_company_employees_company
  foreign key (company_id) references company (id),
  constraint FK_company_employees_user
  foreign key (employees_id) references user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table b2b_account
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  balance       decimal(19,2) null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table location
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name             varchar(256) not null,
  code             varchar(10) null,
  location_type    varchar(50) null,
  location_company_id bigint null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table account
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  deleted_datetime datetime null,
  code               int not null,
  name               varchar(150) not null,
  account_type       varchar(50) null,
  account_category      varchar(50) null,
  description         varchar(1000) null,
  day_total                   decimal(19,2) null,
  week_to_date_total          decimal(19,2) null,
  month_to_date_total         decimal(19,2) null,
  year_to_date_total          decimal(19,2) null,
  last_one_year_total         decimal(19,2) null,
  overdraft_limit         decimal(19,2) null,
  parent_id        bigint null,
  parent          bit null,
  enable          bit null,
  can_delete          bit null,
  chart_of_accounts_id bigint not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_account1
    unique (chart_of_accounts_id, code),
  constraint UK_account2
    unique (chart_of_accounts_id, name, account_type, parent_id),
  constraint FK_account_chart_of_accounts
    foreign key (chart_of_accounts_id) references chart_of_accounts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table transaction
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  transaction_type varchar(50) null,
  transaction_type_id bigint null,
  updated_datetime datetime null,
  deleted_datetime datetime null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table journal_entry
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  deleted_datetime datetime null,
  transaction_id   bigint not null,
  account_id        bigint not null,
  amount           decimal(19,2) null,
  transaction_datetime datetime not null,
  entry            int not null,
  short_description  varchar(16) not null,
  description        varchar(1000) null,
  transaction_side varchar(255) not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_journal_entry_transaction
    foreign key (transaction_id) references transaction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table supplier
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name varchar(256) not null,
  b2b_account_id       bigint null,
  principal_company_id  bigint null,
  supplier_company_id  bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_supplier
    unique (name, principal_company_id),
  constraint FK_supplier_company1
    foreign key (principal_company_id) references company (id),
  constraint FK_supplier_company2
    foreign key (supplier_company_id) references company (id),
  constraint FK_supplier_b2b_account
    foreign key (b2b_account_id) references b2b_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table customer
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name varchar(256) not null,
  principal_company_id  bigint null,
  customer_company_id  bigint null,
  b2b_account_id       bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_customer
    unique (name, principal_company_id),
  constraint FK_principal_company1
    foreign key (principal_company_id) references company (id),
  constraint FK_principal_company2
    foreign key (customer_company_id) references company (id),
  constraint FK_customer_b2b_account
    foreign key (b2b_account_id) references b2b_account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table product
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name varchar(128) not null,
  manufacturer varchar(128) not null,
  title varchar(512) not null,
  company_id bigint null,
  gtin_type varchar(7) null,
  gtin varchar(14) null,
  created_by       bigint null,
  updated_by       bigint null,
  deleted_datetime datetime null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table product_variant
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  type varchar(50) not null,
  attribute varchar(50) not null,
  value varchar(50) not null,
  created_by       bigint null,
  updated_by       bigint null,
  deleted_datetime datetime null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table product_product_variants
(
  product_id bigint not null,
  product_variants_id bigint not null,
  primary key (product_id, product_variants_id),
  constraint FK_product_product_variants_product
    foreign key (product_id) references product (id),
  constraint FK_product_product_variants_product_variant
    foreign key (product_variants_id) references product_variant (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table supplier_products
(
  supplier_id bigint not null,
  products_id bigint not null,
  primary key (supplier_id, products_id),
  constraint FK_supplier_products_supplier
    foreign key (supplier_id) references supplier (id),
  constraint FK_supplier_products_product
    foreign key (products_id) references product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table purchase_order
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  number       bigint null,
  supplier_id  bigint null,
  transaction_id bigint null,
  total_price decimal(19,2) null,
  paid decimal(19,2) null,
  description        varchar(1000) null,
  purchase_order_state varchar(50) not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_purchase_order_supplier
    foreign key (supplier_id) references supplier (id),
  constraint FK_purchase_order_transaction
    foreign key (transaction_id) references transaction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table purchase_invoice
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  number       bigint null,
  purchase_order_id bigint null,
  total_price decimal(19,2) null,
  description        varchar(1000) null,
  payment_due_datetime datetime null,
  delivery_datetime datetime null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table sales_order
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  number       bigint null,
  company_id  bigint null,
  customer_id  bigint null,
  transaction_id   bigint null,
  total_price decimal(19,2) null,
  paid decimal(19,2) null,
  received_amount    decimal(19,2) null,
  description        varchar(1000) null,
  sales_order_state varchar(50) not null,
  delivery_datetime datetime null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_sales_order_customer
    foreign key (customer_id) references customer (id),
  constraint FK_sales_order_transaction
    foreign key (transaction_id) references transaction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table sales_invoice
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  number       bigint null,
  sales_order_id bigint null,
  total_price decimal(19,2) null,
  description        varchar(1000) null,
  payment_due_datetime datetime null,
  delivery_datetime datetime null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table purchase_order_event
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  purchase_order_event_id bigint null,
  purchase_order_state varchar(50) null,
  description        varchar(1000) null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table sales_order_event
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  sales_order_event_id bigint null,
  sales_order_state varchar(50) null,
  description        varchar(1000) null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table purchase_order_line_item
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  entry             int not null,
  line_item_id bigint null,
  quantity double null,
  sub_total decimal(19,2) null,
  unit_price decimal(19,2) null,
  product_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_purchase_order_line_item_product
    foreign key (product_id) references product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table purchase_invoice_line_item
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  entry             int not null,
  line_item_id bigint null,
  quantity double null,
  sub_total decimal(19,2) null,
  unit_price decimal(19,2) null,
  product_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_purchase_invoice_line_item_product
    foreign key (product_id) references product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table sales_order_line_item
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  entry             int not null,
  line_item_id bigint null,
  quantity double null,
  sub_total decimal(19,2) null,
  unit_price decimal(19,2) null,
  product_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_sales_order_line_item_product
    foreign key (product_id) references product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table sales_invoice_line_item
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  entry             int not null,
  line_item_id bigint null,
  quantity double null,
  sub_total decimal(19,2) null,
  unit_price decimal(19,2) null,
  product_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_sales_invoice_line_item_product
    foreign key (product_id) references product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table stock
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  sku             varchar(256) null,
  product_id       bigint null,
  company_stock_id   bigint null,
  location_id       bigint null,
  quantity double null,
  cost_price decimal(19,2) null,
  sell_price decimal(19,2) null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_stock
    unique (sku, product_id, company_stock_id, location_id),
  constraint FK_stock_product
    foreign key (product_id) references product (id),
  constraint FK_stock_location
      foreign key (location_id) references location (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table stock_accounts
(
  stock_id bigint not null,
  accounts_id bigint not null,
  constraint UK_stock_accounts
    unique (accounts_id),
  constraint FK_stock_accounts_stock
    foreign key (stock_id) references stock (id),
  constraint FK_product_accounts_account
    foreign key (accounts_id) references account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table stock_transaction
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  purchase_invoice_id       bigint null,
  sales_invoice_id       bigint null,
  product_id           bigint null,
  location_id       bigint null,
  quantity          double null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_stock_transaction_location
    foreign key (location_id) references location (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table subscription_plan
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name varchar(256) not null,
  credits_per_month int not null,
  duration int not null,
  price_per_month decimal(19,2) not null,
  price_per_year decimal(19,2) not null,
  currencyCode varchar(3) not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_subscription_plan
    unique (name, credits_per_month, duration, price_per_month, price_per_year, currencyCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table subscription
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  user_id       bigint not null,
  subscription_plan_id       bigint not null,
  subscription_start_datetime datetime not null,
  subscription_end_datetime datetime not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_subscription
    unique (user_id, subscription_plan_id, subscription_start_datetime, subscription_end_datetime),
  constraint FK_subscription_user
    foreign key (user_id) references user (id),
  constraint FK_subscription_subscription_plan
    foreign key (subscription_plan_id) references subscription_plan (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;