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
  uuid             varchar(36) not null,
  name             varchar(55) not null,
  full_name        varchar(55) not null,
  code             varchar(2) not null,
  continent_code   varchar(2) not null,
  iso3             varchar(3) not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_country1
  unique (uuid),
  constraint UK_country2
  unique (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table user
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  deleted_datetime datetime null,
  uuid varchar(36) not null,
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
  constraint UK_user1
    unique (uuid),
  constraint UK_user2
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
  uuid varchar(36) not null,
  code varchar(3) not null,
  number varchar(15) not null,
  created_by       bigint null,
  updated_by       bigint null,
  phone_user_id bigint null,
  country_id bigint null,
  principal bit null,
  confirmation_code varchar(6) null,
  confirmed bit not null,
  constraint UK_phone1
    unique (uuid),
  constraint UK_phone2
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
  uuid               varchar(36)  not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_chart_of_accounts
    unique (uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table company
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  uuid varchar(36) not null,
  name             varchar(256) not null,
  chart_of_accounts_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint FK_company
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

create table ledger
(
  id bigint auto_increment primary key,
  created_datetime datetime null,
  updated_datetime datetime null,
  deleted_datetime datetime null,
  uuid               varchar(36)  not null,
  code               int          not null,
  description        varchar(255) null,
  name               varchar(255) not null,
  parent_uuid        varchar(36)  null,
  ledger_chart_of_accounts_id bigint null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_ledger_template1
    unique (uuid),
  constraint UK_ledger_template2
    unique (ledger_chart_of_accounts_id, code),
  constraint UK_ledger_template3
    unique (ledger_chart_of_accounts_id, name, parent_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;