INSERT INTO user (name,email,password,location,profession,additional) VALUES ('toto','toto@toto.com','','Nantes','dev','');
INSERT INTO WORKSPACE (id_creator, name, all_read, all_write, all_manage, description) VALUES (1,'name',1,1,1,'description');
INSERT INTO WORKSPACE (id_creator, name, all_read, all_write, all_manage, description) VALUES (1,'namesrc',1,1,1,'description');
INSERT INTO WORKSPACE (id_creator, name, all_read, all_write, all_manage, description) VALUES (1,'namesrc',1,1,1,'description');
INSERT INTO folder (id_root,id_parent,name) VALUES (1 , null , 'name');
INSERT INTO folder (id_root,id_parent,name) VALUES (1 , null , 'namesrc');
INSERT INTO user_workspace (id_user,id_workspace,read,write,manage_user) VALUES (1 , 1 , 0 , 0 , 0);
INSERT INTO user_workspace (id_user,id_workspace,read,write,manage_user) VALUES (2 , 2 , 1 , 1 , 1);
