create table IF NOT EXISTS user (
  id_user			bigint auto_increment not null,
  name				varchar(255),
  email				varchar(255) not null,
  password			varchar(255) not null,
  location			varchar(255),
  profession        varchar(255),
  additional        varchar(1000),
  admin_wms         tinyint default 30,
  admin_mapcatalog  tinyint default 30,
  admin_wps         tinyint default 30,
  verification      varchar(255),
  reset_pass           varchar(255),
  constraint pk_user primary key (id_user)
);

create table IF NOT EXISTS workspace (
  id_workspace			bigint auto_increment not null,
  id_creator			bigint ,
  name				    varchar(255),
  all_read              tinyint(1) default 0,
  all_write             tinyint(1) default 0,
  all_manage            tinyint(1) default 0,
  description           varchar(1000),
  constraint pk_workspace primary key (id_workspace)
);

create table IF NOT EXISTS folder (
  id_folder			bigint auto_increment not null,
  id_root			bigint not null,
  id_parent			bigint,
  name				varchar(255),
  constraint pk_folder primary key (id_folder)
);

create table IF NOT EXISTS comment (
  id_comment			bigint auto_increment not null,
  id_writer			    bigint,
  id_map			    bigint not null,
  content			    clob,
  title                 varchar(255),
  date                  timestamp default CURRENT_TIMESTAMP,
  constraint pk_comment primary key (id_comment)
);

create table IF NOT EXISTS owscontext (
  id_owscontext			bigint auto_increment not null,
  id_root			    bigint not null,
  id_parent		    	bigint,
  id_uploader			bigint,
  content               clob,
  title                 varchar(255),
  description           varchar(1000),
  date                  timestamp default CURRENT_TIMESTAMP,
  constraint pk_owscontext primary key (id_owscontext)
);

create table IF NOT EXISTS user_workspace (
  id_user			    bigint,
  id_workspace			bigint,
  read                  tinyint(1) default 0,
  write			    	tinyint(1) default 0,
  manage_user			tinyint(1) default 0,
  constraint pk_user_workspace primary key (id_user, id_workspace)
);

create table IF NOT EXISTS downloader_ows (
  id_user		    	bigint,
  id_owscontext			bigint,
  constraint pk_downloader_ows primary key (id_user, id_owscontext)
);

create table IF NOT EXISTS version (
  version		    	bigint
);

alter table workspace add constraint IF NOT EXISTS fk_workspace_user_1 foreign key (id_creator) references user (id_user) on delete set null on update restrict;
create index IF NOT EXISTS ix_workspace_id_creator_1 on workspace (id_creator);

alter table folder add constraint IF NOT EXISTS fk_folder_workspace_2 foreign key (id_root) references workspace (id_workspace) on delete cascade on update restrict;
create index IF NOT EXISTS ix_folder_id_root_2 on folder (id_root);
alter table folder add constraint IF NOT EXISTS fk_folder_folder_3 foreign key (id_parent) references folder (id_folder) on delete cascade on update restrict;
create index IF NOT EXISTS ix_folder_id_parent_3 on folder (id_parent);

alter table comment add constraint IF NOT EXISTS fk_comment_user_4 foreign key (id_writer) references user (id_user) on delete set null on update restrict;
create index IF NOT EXISTS ix_comment_id_writer_4 on comment (id_writer);
alter table comment add constraint IF NOT EXISTS fk_comment_owscontext_5 foreign key (id_map) references owscontext (id_owscontext) on delete cascade on update restrict;
create index IF NOT EXISTS ix_comment_id_map_5 on comment (id_map);

alter table owscontext add constraint IF NOT EXISTS fk_owscontext_workspace_6 foreign key (id_root) references workspace (id_workspace) on delete cascade on update restrict;
create index IF NOT EXISTS ix_owscontext_id_root_6 on owscontext (id_root);
alter table owscontext add constraint IF NOT EXISTS fk_owscontext_folder_7 foreign key (id_parent) references folder (id_folder) on delete cascade on update restrict;
create index IF NOT EXISTS ix_owscontext_id_parent_7 on owscontext (id_parent);
alter table owscontext add constraint IF NOT EXISTS fk_owscontext_user_8 foreign key (id_uploader) references user (id_user) on delete set null on update restrict;
create index IF NOT EXISTS ix_owscontext_id_uploader_8 on owscontext (id_uploader);