REGISTER 'lib/*.jar'

%default input 'input'
%default output 'output'

A = load '$input' 
	using pl.edu.icm.coansys.commons.pig.udf.RichSequenceFileLoader
	('org.apache.hadoop.io.Text',
	'org.apache.hadoop.io.BytesWritable') 
	as (k:chararray,v:bytearray);

B = foreach A generate 
	k, FLATTEN(pl.edu.icm.coansys.commons.pig.udf.ByteArrayToText(v))
	as (v:chararray);
C = filter B by 
	(
	k is not null 
	and k!=''
	and v is not null 
	and v!=''
	);

store C into '$output';

