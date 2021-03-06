CREATE TABLE SECUREUSERDATA_USER
(
  ID          VARCHAR2(40 CHAR)                 NOT NULL,
  PERSONID    VARCHAR2(20 CHAR)                 NOT NULL,
  NETID       VARCHAR2(20 CHAR)                 NOT NULL,
  FIRST_NAME  VARCHAR2(50 CHAR),
  LAST_NAME   VARCHAR2(50 CHAR),
  EMAIL       VARCHAR2(256 CHAR),
  WORK_PHONE  VARCHAR2(20 CHAR),
  HOME_PHONE  VARCHAR2(20 CHAR)
);


CREATE UNIQUE INDEX SECUREUSERDATA_USER_PK ON SECUREUSERDATA_USER
(ID);


ALTER TABLE SECUREUSERDATA_USER ADD (
  CONSTRAINT SECUREUSERDATA_USER_PK
  PRIMARY KEY
  (ID)
  USING INDEX SECUREUSERDATA_USER_PK);

GRANT SELECT, UPDATE ON SECUREUSERDATA_USER TO FASTDEV2;

GRANT SELECT, UPDATE ON SECUREUSERDATA_USER TO FASTDEV3;

Insert into SECUREUSERDATA_USER
   (ID, PERSONID, 
    NETID, FIRST_NAME, LAST_NAME, EMAIL, WORK_PHONE, 
    HOME_PHONE)
 Values
   ('A3', '12345', 'js', 'John', 'Smith', 'js@a.edu', 
    '3-1234', '123-4567');
Insert into SECUREUSERDATA_USER
   (ID, PERSONID, 
    NETID, FIRST_NAME, LAST_NAME, EMAIL, WORK_PHONE, 
    HOME_PHONE)
 Values
   ('B4', '98765', 'sd', 'Sara', 'Davis', 'sd@a.edu', 
    '5-2345', '234-5678');
Insert into SECUREUSERDATA_USER
   (ID, PERSONID, 
    NETID, FIRST_NAME, LAST_NAME, EMAIL, WORK_PHONE, 
    HOME_PHONE)
 Values
   ('C5', '54321', 'rj', 'Ryan', 'Jones', 'rj@a.edu', 
    '7-4567', '345-6789');
Insert into SECUREUSERDATA_USER
   (ID, PERSONID, 
    NETID, FIRST_NAME, LAST_NAME, EMAIL, WORK_PHONE, 
    HOME_PHONE)
 Values
   ('T7', '56789', 'jc', 'Julia', 'Clark', 'jc@a.edu', 
    '9-6789', '456-7890');
COMMIT;
