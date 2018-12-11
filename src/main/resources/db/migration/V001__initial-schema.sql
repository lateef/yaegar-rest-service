create table if not exists role
(
  id        bigint auto_increment primary key,
  authority varchar(32) not null,
  constraint UK_role1
    unique (authority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists country
(
  id               bigint auto_increment primary key,
  created_datetime datetime(6) null,
  updated_datetime datetime(6) null,
  uuid             varchar(36) not null,
  name             varchar(32) not null,
  created_by       bigint null,
  updated_by       bigint null,
  constraint UK_country1
  unique (uuid),
  constraint UK_country2
  unique (name)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists user
(
  id bigint auto_increment primary key,
  created_datetime datetime(6) null,
  updated_datetime datetime(6) null,
  uuid varchar(36) not null,
  phone_number varchar(15) not null,
  accepted_terms bit null,
  account_non_expired bit null,
  account_non_locked bit null,
  credentials_non_expired bit null,
  deleted_datetime datetime(6) null,
  enabled bit null,
  failed_login_attempts int null,
  first_name varchar(32) null,
  password varchar(128) not null,
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

create table if not exists user_roles
(
  user_id bigint not null,
  roles_id bigint not null,
  primary key (user_id, roles_id),
  constraint FK_user_roles_user
    foreign key (user_id) references user (id),
  constraint FK_user_roles_role
    foreign key (roles_id) references role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create table if not exists phone
(
  id bigint auto_increment primary key,
  created_datetime datetime(6) null,
  updated_datetime datetime(6) null,
  uuid varchar(36) not null,
  code varchar(3) not null,
  number varchar(15) not null,
  created_by       bigint null,
  updated_by       bigint null,
  phone_user_id bigint null,
  country_id bigint null,
  `primary` bit null,
  constraint UK_phone1
    unique (uuid),
  constraint UK_phone2
    unique (number),
  constraint FK_phone_user
    foreign key (phone_user_id) references user (id),
  constraint FK_phone_country
    foreign key (country_id) references country (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;