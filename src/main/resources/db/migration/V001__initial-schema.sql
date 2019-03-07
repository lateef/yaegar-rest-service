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
  accepted_terms bit null,
  account_non_expired bit null,
  account_non_locked bit null,
  credentials_non_expired bit null,
  enabled bit null,
  failed_login_attempts int null,
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
  chart_of_accounts_id bigint null,
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

create table location
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name             varchar(256) not null,
  code             varchar(10) null,
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
  product_classifier      varchar(50) null,
  description         varchar(1000) null,
  day_total                   decimal(19,2) null,
  week_to_date_total          decimal(19,2) null,
  month_to_date_total         decimal(19,2) null,
  year_to_date_total          decimal(19,2) null,
  last_one_year_total         decimal(19,2) null,
  parent_id        bigint null,
  parent          bit null,
  account_chart_of_accounts_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_account1
    unique (account_chart_of_accounts_id, code),
  constraint UK_account2
    unique (account_chart_of_accounts_id, name, account_type, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table transaction
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
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
  transaction_id   bigint null,
  account_id        bigint null,
  amount           decimal(19,2) null,
  transaction_datetime datetime null,
  entry            int not null,
  description        varchar(1000) null,
  transaction_side varchar(255) null,
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
  company_id  bigint null,
  company_supplier_id  bigint null,
  user_supplier_id  bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_supplier
    unique (name, company_id),
  constraint FK_supplier_company1
    foreign key (company_id) references company (id),
  constraint FK_supplier_company2
    foreign key (company_supplier_id) references company (id),
  constraint FK_supplier_company3
    foreign key (user_supplier_id) references company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table product
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name varchar(256) not null,
  cost_price decimal(19,2) null,
  sell_price decimal(19,2) null,
  company_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_product_supplier
    foreign key (company_id) references company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table product_accounts
(
  product_id bigint not null,
  accounts_id bigint not null,
  constraint UK_product_accounts
    unique (accounts_id),
  constraint FK_product_accounts_product
    foreign key (product_id) references product (id),
  constraint FK_product_accounts_account
    foreign key (accounts_id) references account (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table payment
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  transaction_id  bigint null,
  payment_type varchar(50) null,
  payment_type_id  bigint null,
  description        varchar(1000) null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_payment_transaction
    foreign key (transaction_id) references transaction (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table purchase_order
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  number       bigint null,
  company_id  bigint null,
  supplier_id  bigint null,
  total_price decimal(19,2) null,
  paid_amount      decimal(19,2) null,
  description        varchar(1000) null,
  purchase_order_state varchar(50) null,
  delivery_datetime datetime null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_purchase_order_company
    foreign key (company_id) references company (id),
  constraint FK_purchase_order_supplier
    foreign key (supplier_id) references supplier (id)
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

create table sales_order
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  number       bigint null,
  company_id  bigint null,
  customer_id  bigint null,
  total_price decimal(19,2) null,
  received_amount    decimal(19,2) null,
  description        varchar(1000) null,
  sales_order_state varchar(50) null,
  order_supply_state varchar(50) null,
  delivery_datetime datetime null,
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

create table line_item
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  line_item_purchase_order_id bigint null,
  sales_order_id bigint null,
  item_type varchar(255) null,
  quantity double null,
  sub_total decimal(19,2) null,
  unit_price decimal(19,2) null,
  product_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_line_item_product
    foreign key (product_id) references product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table customer
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  name varchar(256) not null,
  company_id  bigint null,
  company_customer_id  bigint null,
  user_customer_id  bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_customer
    unique (name, company_id),
  constraint FK_customer_company1
    foreign key (company_id) references company (id),
  constraint FK_customer_company2
    foreign key (company_customer_id) references company (id),
  constraint FK_customer_company3
    foreign key (user_customer_id) references company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table stock
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  code             varchar(10) null,
  product_id       bigint null,
  location_id       bigint null,
  quantity double null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_stock
    unique (product_id, location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table stock_transaction
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  purchase_order_id       bigint null,
  sales_order_id          bigint null,
  product_id           bigint null,
  from_location_id       bigint null,
  to_location_id       bigint null,
  quantity          double null,
  created_by       bigint null,
  updated_by       bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;