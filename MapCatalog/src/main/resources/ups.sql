create table user (
  id_user			bigint auto_increment not null,
  name				varchar(255),
  email				varchar(255) not null,
  password			varchar(255) not null,
  avatar			varchar(255),
  location			varchar(255),
  constraint pk_user primary key (id_user)
);

create table workspace (
  id_workspace			bigint auto_increment not null,
  id_creator			bigint ,
  name				    varchar(255),
  isPublic              tinyint(1) default 0,
  constraint pk_workspace primary key (id_workspace)
);

create table folder (
  id_folder			bigint auto_increment not null,
  id_root			bigint not null,
  id_parent			bigint,
  name				varchar(255),
  constraint pk_folder primary key (id_folder)
);

create table comment (
  id_comment			bigint auto_increment not null,
  id_writer			    bigint,
  id_map			    bigint,
  content			    clob,
  date                  timestamp default CURRENT_TIMESTAMP,
  constraint pk_comment primary key (id_comment)
);

create table owscontext (
  id_owscontext			bigint auto_increment not null,
  id_root			    bigint not null,
  id_parent		    	bigint,
  id_uploader			bigint,
  content               clob,
  title                 varchar(255),
  date                  timestamp default CURRENT_TIMESTAMP,
  constraint pk_owscontext primary key (id_owscontext)
);

create table user_workspace (
  id_user			    bigint,
  id_workspace			bigint,
  READ                  tinyint(1) default 0,
  WRITE			    	tinyint(1) default 0,
  MANAGE_USER			tinyint(1) default 0,
  constraint pk_user_workspace primary key (id_user, id_workspace)
);

create table downloader_ows (
  id_user		    	bigint,
  id_owscontext			bigint,
  constraint pk_downloader_ows primary key (id_user, id_owscontext)
);

alter table workspace add constraint fk_workspace_user_1 foreign key (id_creator) references user (id_user) on delete set null on update restrict;
create index ix_workspace_id_creator_1 on workspace (id_creator);

alter table folder add constraint fk_folder_workspace_2 foreign key (id_root) references workspace (id_workspace) on delete cascade on update restrict;
create index ix_folder_id_root_2 on folder (id_root);
alter table folder add constraint fk_folder_folder_3 foreign key (id_parent) references folder (id_folder) on delete cascade on update restrict;
create index ix_folder_id_parent_3 on folder (id_parent);

alter table comment add constraint fk_comment_user_4 foreign key (id_writer) references user (id_user) on delete set null on update restrict;
create index ix_comment_id_writer_4 on comment (id_writer);
alter table comment add constraint fk_comment_owscontext_5 foreign key (id_map) references owscontext (id_owscontext) on delete cascade on update restrict;
create index ix_comment_id_map_5 on comment (id_map);

alter table owscontext add constraint fk_owscontext_workspace_6 foreign key (id_root) references workspace (id_workspace) on delete cascade on update restrict;
create index ix_owscontext_id_root_6 on owscontext (id_root);
alter table owscontext add constraint fk_owscontext_folder_7 foreign key (id_parent) references folder (id_folder) on delete cascade on update restrict;
create index ix_owscontext_id_parent_7 on owscontext (id_parent);
alter table owscontext add constraint fk_owscontext_user_8 foreign key (id_uploader) references user (id_user) on delete set null on update restrict;
create index ix_owscontext_id_uploader_8 on owscontext (id_uploader);