package services

trait PowerDnsService { self =>
  
  case class PdnsRecord(
    id: Option[Int],
    domainId: Option[Int],
    name: Option[String],
    recordType: Option[String],
    content: Option[String],
    ttl: Option[Int],
    prio: Option[Int],
    changeDate: Option[Int]
  )
  
  case class PdnsDomain()
  case class PdnsZone()
  
  def incrementSOA() {
    val SQL = """
CREATE OR REPLACE FUNCTION update_soa() RETURNS TRIGGER AS '
DECLARE
	s1 text;
	s2 text;
	s3 int4;
	s4 text;
	s5 text;
	s6 text;
	s7 text;
	str text;
	ctx text;
BEGIN
	IF NEW.type = ''SOA'' THEN
		RETURN NEW;
	END IF;

	SELECT name INTO str FROM domains WHERE id = NEW.domain_id AND type = ''MASTER'' OR type = ''NATIVE'';
	IF NOT FOUND THEN
		RETURN NEW;
	END IF;


	SELECT content INTO ctx FROM records WHERE domain_id = NEW.domain_id AND type = ''SOA'';
	s1 := split_part(ctx, '' '',1);
	s2 := split_part(ctx, '' '',2);
	s3 := round(extract(epoch FROM now()))::int;
	s4 := split_part(ctx, '' '',4);
	s5 := split_part(ctx, '' '',5);
	s6 := split_part(ctx, '' '',6);
	s7 := split_part(ctx, '' '',7);


	UPDATE records SET content =
	(s1 || '' '' || s2 || '' '' || s3 || '' '' || s4 || '' '' || s5 || '' '' || s6 || '' '' || s7)
	WHERE domain_id = NEW.domain_id AND type = ''SOA'';
	RETURN NEW;
END;
' LANGUAGE 'plpgsql';
      
      --DROP TRIGGER update_soa ON records;
CREATE TRIGGER update_soa BEFORE INSERT OR UPDATE ON records FOR EACH ROW EXECUTE PROCEDURE update_soa();
      
      
      select * from records where type = 'SRV';

insert into records
(domain_id, name, type, content, ttl, prio, change_date)
values
(5, '_sip._udp.sip-39266.accounts-lab03.vocalocity.com', 'SRV', '1 5060 registrar2.qa3.vocal-dev.com', 60, 0, (select (round(extract(epoch from now()))::int)));

select (round(extract(epoch from now()))::int);

select * from records where type = 'SOA' and domain_id = 5;

update records set content = 'ns1.accounts-lab03.vocalocity.com noc.vocalocity.com 11 900 450 604800 600' where id = 336692;
    """
    
  }

  def updateAccountIdFromRecords() {
  	val SQL = """
select * from domains;

CREATE OR REPLACE FUNCTION update_records_with_account_id() AS '
DECLARE
  accountid int5;
  content text;
  row RECORD;
  
BEGIN
  FOR row IN SELECT * FROM records ORDER BY id LOOP
    
  END LOOP;

END;
' LANGUAGE 'plpgsql';


begin;
update records
set account_id = substring(name from 5 for 5)::int
where type='A'
and name like 'sip-%vocalocity.com'
commit;

begin;
update records
set account_id = substring(name from 4 for 5)::int
where type='A'
and name ~ 'sip[0-9]{5}.media'
commit;
   """
  }

}