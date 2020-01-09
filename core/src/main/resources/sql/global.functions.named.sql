[[DROP_HIGHBIT_PROC]]
drop PROCEDURE if exists getNextHighbit;

[[ADD_HIGHBIT_PROC]]
CREATE PROCEDURE getNextHighbit (OUT ret SMALLINT)
BEGIN

  create table if not exists highbit(high smallint);

  set session transaction isolation level SERIALIZABLE;
  start transaction;

  /*see if the high bit has been establised yet.  If not seed it with 10
    NOTE: reserving a single highbit value of 9 for MySQL OID generation.
    refer: getNextMysqlOid */
  if (select count(*) from highbit) = 0 then
      set ret = 10;
      insert into highbit values (ret);
  else
      select max(high) into ret from highbit;
  end if;

  /*has the high bit getting near overflow.  start back at 10 then */
  if ret > 32000 then
     set ret = 10;
  else
    set ret = ret + 1;
  end if;

  /*update the new high bit value*/
  update highbit set high = ret;

  commit;
  set session transaction isolation level READ COMMITTED;
END;

[[DROP_MYSQL_OID_GEN_PROC]]
drop PROCEDURE if exists getNextMysqlOid;

[[ADD_MYSQL_OID_GEN_PROC]]
CREATE PROCEDURE getNextMysqlOid (OUT ret BIGINT)
BEGIN

  create table if not exists mysqloid(nextOid bigint);

  set session transaction isolation level SERIALIZABLE;
  start transaction;

  /*see if the mysql base OID has been establised yet.  If not seed it with a highbit
    value of 9. decimal value of 9 << 47 is 1266637395197952 */
  if (select count(*) from mysqloid) = 0 then
      set ret = (9 << 47);
      insert into mysqloid values (ret);
  else
      select max(nextOid) into ret from mysqloid;
      if(ret is null) then
        set ret = (9 << 47);
      end if;
  end if;

  /* bl: handle overflow.  we only have 70+ trillion oids to generate in MySQL. if we ever exceed that
     value somehow, then we should return -1 to indicate an error state since overflow has occurred
     and thus we need to reconsider the MySQL OID generation policy. */
  if ret >= (9 << 47)*2 then
      set ret = -1;
  else
      /*update the next OID value*/
      update mysqloid set nextOid = ret+1;
  end if;

  commit;
  set session transaction isolation level READ COMMITTED;
END;

[[DROP_USER_REPUTATION_VIEW]]
drop view if exists `UserReputation`;

[[CREATE_USER_REPUTATION_VIEW]]
create view UserReputation
as
select userOid,
       negativeConductStartTimestamp,
       negativeConductExpirationTimestamp,
       qualityAnalysis
from reputation.CurrentReputation;

[[DROP_USER_REPUTATION_HISTORY_VIEW]]
drop view if exists `UserReputationHistory`;

[[CREATE_USER_REPUTATION_HISTORY_VIEW]]
create view `UserReputationHistory`
as select userOid, period, snapshotDate, totalScore
from `reputation`.`ReputationHistory`;

[[DROP_FORMAT_UUID_FUNCTION]]
DROP FUNCTION IF EXISTS global.formatUUID;

[[ADD_FORMAT_UUID_FUNCTION]]
CREATE FUNCTION global.formatUUID (id binary(16))
  RETURNS char(36)
RETURN LOWER(CONCAT(
    SUBSTR(HEX(id), 1, 8), '-',
    SUBSTR(HEX(id), 9, 4), '-',
    SUBSTR(HEX(id), 13, 4), '-',
    SUBSTR(HEX(id), 17, 4), '-',
    SUBSTR(HEX(id), 21)
  ));