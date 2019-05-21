create table role
(
  id uuid not null primary key,
  authority varchar(68) not null,
  constraint UK_role1 unique (authority),
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  created_by       uuid null,
  updated_by       uuid null
);

create table country
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name             varchar(55) not null,
  full_name        varchar(55) not null,
  code             varchar(2) not null,
  continent_code   varchar(2) not null,
  iso3             varchar(3) not null,
  created_by       uuid null,
  updated_by       uuid null,
  constraint UK_country unique (name)
);

create table configuration
(
  id uuid not null primary key,
  configuration varchar not null,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  created_by       uuid null,
  updated_by       uuid null
);

create table "user"
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  phone_number varchar(15) not null,
  accepted_terms boolean not null,
  account_non_expired boolean not null,
  account_non_locked boolean not null,
  credentials_non_expired boolean not null,
  enabled boolean not null,
  failed_login_attempts int not null,
  first_name varchar(32) null,
  password varchar(128) null,
  country_id uuid null,
  configuration_id uuid null,
  created_by       uuid null,
  updated_by       uuid null,
  constraint UK_user
    unique (phone_number),
  constraint FK_user_country
    foreign key (country_id) references country (id),
  constraint FK_user_configuration
    foreign key (configuration_id) references configuration (id)
);

create table user_roles
(
  user_id uuid not null,
  roles_id uuid not null,
  primary key (user_id, roles_id),
  constraint FK_user_roles_user
    foreign key (user_id) references "user" (id),
  constraint FK_user_roles_role
    foreign key (roles_id) references role (id)
);

create table chart_of_accounts
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  created_by       uuid not null,
  updated_by       uuid not null
);

create table company
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name varchar(256) not null,
  chart_of_accounts_id uuid not null,
  country_id uuid null,
  configuration_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_company_chart_of_accounts
  foreign key (chart_of_accounts_id) references chart_of_accounts (id),
  constraint FK_company_country
    foreign key (country_id) references country (id),
  constraint FK_company_configuration
    foreign key (configuration_id) references configuration (id)
);

create table company_owners
(
  company_id uuid not null,
  owners_id uuid not null,
  primary key (company_id, owners_id),
  constraint FK_company_owners_company
  foreign key (company_id) references company (id),
  constraint FK_company_owners_user
  foreign key (owners_id) references "user" (id)
);

create table company_employees
(
  company_id uuid not null,
  employees_id uuid not null,
  primary key (company_id, employees_id),
  constraint FK_company_employees_company
  foreign key (company_id) references company (id),
  constraint FK_company_employees_user
  foreign key (employees_id) references "user" (id)
);

create table phone
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  code varchar(3) not null,
  number varchar(15) not null,
  created_by       uuid null,
  updated_by       uuid null,
  owner_id uuid null,
  country_id uuid null,
  principal boolean null,
  confirmation_code varchar(6) null,
  confirmed boolean not null,
  constraint UK_phone
    unique (number),
  constraint FK_phone_country
    foreign key (country_id) references country (id)
);

create table user_phones
(
  user_id uuid not null,
  phones_id uuid not null,
  primary key (user_id, phones_id),
  constraint FK_user_phones_user
    foreign key (user_id) references "user" (id),
  constraint FK_user_phones_phone
    foreign key (phones_id) references phone (id)
);

create table company_phones
(
  company_id uuid not null,
  phones_id uuid not null,
  primary key (company_id, phones_id),
  constraint FK_company_phones_company
    foreign key (company_id) references company (id),
  constraint FK_company_phones_phone
    foreign key (phones_id) references phone (id)
);

create table b2b_account
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  balance       decimal(19,2) null,
  created_by       uuid not null,
  updated_by       uuid not null
);

create table location
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name             varchar(256) not null,
  code             varchar(36) not null,
  location_type    varchar(50) not null,
  company_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_location_company
  foreign key (company_id) references company (id),
  constraint UK_location1 unique (name, company_id),
  constraint UK_location2 unique (code, company_id)
);

create table account
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
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
  parent_id        uuid null,
  parent          boolean null,
  enable          boolean null,
  can_delete          boolean null,
  chart_of_accounts_id uuid not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint UK_account1
    unique (chart_of_accounts_id, code),
  constraint UK_account2
    unique (chart_of_accounts_id, name, account_type, parent_id),
  constraint FK_account_chart_of_accounts
    foreign key (chart_of_accounts_id) references chart_of_accounts (id)
);

create table transaction
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  transaction_type varchar(50) null,
  transaction_type_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null
);

create table journal_entry
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  transaction_id   uuid null,
  account_id        uuid not null,
  amount           decimal(19,2) null,
  transaction_datetime timestamp not null,
  short_description  varchar(16) not null,
  description        varchar(1000) null,
  transaction_side varchar(255) not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_journal_entry_transaction
    foreign key (transaction_id) references transaction (id)
);

create table supplier
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name varchar(256) not null,
  b2b_account_id       uuid null,
  principal_company_id  uuid not null,
  supplier_company_id  uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint UK_supplier
    unique (name, principal_company_id),
  constraint FK_supplier_company1
    foreign key (principal_company_id) references company (id),
  constraint FK_supplier_company2
    foreign key (supplier_company_id) references company (id),
  constraint FK_supplier_b2b_account
    foreign key (b2b_account_id) references b2b_account (id)
);

create table customer
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name varchar(256) not null,
  principal_company_id  uuid not null,
  customer_company_id  uuid null,
  b2b_account_id       uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint UK_customer
    unique (name, principal_company_id),
  constraint FK_principal_company1
    foreign key (principal_company_id) references company (id),
  constraint FK_principal_company2
    foreign key (customer_company_id) references company (id),
  constraint FK_customer_b2b_account
    foreign key (b2b_account_id) references b2b_account (id)
);

create table product
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name varchar(128) not null,
  manufacturer varchar(128) not null,
  title varchar(512) not null,
  company_id uuid null,
  gtin_type varchar(7) null,
  gtin varchar(14) null,
  product_tracking_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_product_company
    foreign key (company_id) references company (id),
  constraint UK_product unique (product_tracking_id)
);

create table product_variant
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  type varchar(50) not null,
  attribute varchar(50) not null,
  value varchar(50) not null,
  created_by       uuid not null,
  updated_by       uuid not null
);

create table product_product_variants
(
  product_id uuid not null,
  product_variants_id uuid not null,
  primary key (product_id, product_variants_id),
  constraint FK_product_product_variants_product
    foreign key (product_id) references product (id),
  constraint FK_product_product_variants_product_variant
    foreign key (product_variants_id) references product_variant (id)
);

create table supplier_products
(
  supplier_id uuid not null,
  products_id uuid not null,
  primary key (supplier_id, products_id),
  constraint FK_supplier_products_supplier
    foreign key (supplier_id) references supplier (id),
  constraint FK_supplier_products_product
    foreign key (products_id) references product (id)
);

create table purchase_order
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  number       uuid not null,
  supplier_id  uuid not null,
  transaction_id uuid null,
  total_price decimal(19,2) not null,
  paid decimal(19,2) not null,
  payment_due_datetime timestamp null,
  delivery_datetime timestamp null,
  payment_term varchar(50) not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_purchase_order_supplier
    foreign key (supplier_id) references supplier (id),
  constraint FK_purchase_order_transaction
    foreign key (transaction_id) references transaction (id)
);

create table purchase_order_event
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  purchase_order_id uuid null,
  purchase_order_event_type varchar(50) not null,
  description        varchar(1000) null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_purchase_order_event_purchase_order
    foreign key (purchase_order_id) references purchase_order (id)
);

create table purchase_order_line_item
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  purchase_order_id uuid null,
  quantity double precision not null,
  sub_total decimal(19,2) not null,
  unit_price decimal(19,2) not null,
  product_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_purchase_order_line_item_purchase_order
    foreign key (purchase_order_id) references purchase_order (id),
      constraint FK_purchase_order_line_item_product
    foreign key (product_id) references product (id)
);

create table purchase_invoice
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  number uuid not null,
  purchase_order_id uuid null,
  total_price decimal(19,2) not null,
  description        varchar(1000) null,
  payment_due_datetime timestamp null,
  delivery_datetime timestamp null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_purchase_invoice_purchase_order
    foreign key (purchase_order_id) references purchase_order (id)
);

create table purchase_invoice_line_item
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  purchase_invoice_id uuid  null,
  purchase_order_line_item_id uuid null,
  quantity double precision not null,
  sub_total decimal(19,2) not null,
  unit_price decimal(19,2) not null,
  product_id uuid not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_purchase_invoice_line_item_purchase_order_line_item
    foreign key (purchase_order_line_item_id) references purchase_order_line_item (id),
  constraint FK_purchase_invoice_line_item_purchase_invoice
    foreign key (purchase_invoice_id) references purchase_invoice (id),
  constraint FK_purchase_invoice_line_item_product
    foreign key (product_id) references product (id)
);

create table sales_order
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  number       uuid not null,
  customer_id  uuid  not null,
  transaction_id uuid null,
  total_price decimal(19,2) not null,
  paid decimal(19,2) not null,
  payment_due_datetime timestamp null,
  delivery_datetime timestamp null,
  payment_term varchar(50) not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_sales_order_customer
    foreign key (customer_id) references customer (id),
  constraint FK_sales_order_transaction
    foreign key (transaction_id) references transaction (id)
);

create table sales_order_event
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  sales_order_id uuid null,
  sales_order_event_type varchar(50) not null,
  description        varchar(1000) null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_sales_order_event_sales_order
    foreign key (sales_order_id) references sales_order (id)
);

create table sales_order_line_item
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  sales_order_id uuid null,
  quantity double precision not null,
  sub_total decimal(19,2) not null,
  unit_price decimal(19,2) not null,
  product_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_sales_order_line_item_sales_order
    foreign key (sales_order_id) references sales_order (id),
  constraint FK_sales_order_line_item_product
    foreign key (product_id) references product (id)
);

create table sales_invoice
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  number       uuid not null,
  sales_order_id uuid null,
  total_price decimal(19,2) not null,
  description        varchar(1000) null,
  payment_due_datetime timestamp null,
  delivery_datetime timestamp null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_sales_invoice_sales_order
    foreign key (sales_order_id) references sales_order (id)
);

create table sales_invoice_line_item
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  sales_invoice_id uuid null,
  sales_order_line_item_id uuid null,
  quantity double precision not null,
  sub_total decimal(19,2) not null,
  unit_price decimal(19,2) not null,
  product_id uuid null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_sales_invoice_line_item_sales_invoice
    foreign key (sales_invoice_id) references sales_invoice (id),
  constraint FK_sales_invoice_line_item_sales_order_line_item
    foreign key (sales_order_line_item_id) references sales_order_line_item (id),
  constraint FK_sales_invoice_line_item_product
    foreign key (product_id) references product (id)
);

create table stock
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  sku             varchar(256) null,
  product_id       uuid null,
  company_stock_id   uuid null,
  location_id       uuid null,
  quantity double precision not null,
  cost_price decimal(19,2) not null,
  sell_price decimal(19,2) not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint UK_stock
    unique (sku, product_id, company_stock_id, location_id),
  constraint FK_stock_product
    foreign key (product_id) references product (id),
  constraint FK_stock_location
      foreign key (location_id) references location (id)
);

create table stock_accounts
(
  stock_id uuid not null,
  accounts_id uuid not null,
  primary key (stock_id, accounts_id),
  constraint UK_stock_accounts
    unique (accounts_id),
  constraint FK_stock_accounts_stock
    foreign key (stock_id) references stock (id),
  constraint FK_product_accounts_account
    foreign key (accounts_id) references account (id)
);

create table stock_transaction
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  purchase_invoice_id       uuid null,
  sales_invoice_id       uuid null,
  product_id           uuid null,
  location_id       uuid null,
  quantity          double precision not null,
  created_by       uuid not null,
  updated_by       uuid not null,
  constraint FK_stock_transaction_location
    foreign key (location_id) references location (id)
);

create table subscription_plan
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  name varchar(256) not null,
  credits_per_month int not null,
  duration int not null,
  price_per_month decimal(19,2) not null,
  price_per_year decimal(19,2) not null,
  currency_code varchar(3) not null,
  created_by       uuid null,
  updated_by       uuid null,
  constraint UK_subscription_plan
    unique (name, credits_per_month, duration, price_per_month, price_per_year, currency_code)
);

create table subscription
(
  id uuid not null primary key,
  created_datetime timestamp default current_timestamp,
  updated_datetime timestamp null,
  deleted_datetime timestamp null,
  user_id       uuid not null,
  subscription_plan_id       uuid not null,
  subscription_start_datetime timestamp not null,
  subscription_end_datetime timestamp not null,
  created_by       uuid null,
  updated_by       uuid null,
  constraint UK_subscription
    unique (user_id, subscription_plan_id, subscription_start_datetime, subscription_end_datetime),
  constraint FK_subscription_user
    foreign key (user_id) references "user" (id),
  constraint FK_subscription_subscription_plan
    foreign key (subscription_plan_id) references subscription_plan (id)
);