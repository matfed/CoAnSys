--
-- This file is part of CoAnSys project.
-- Copyright (c) 2012-2013 ICM-UW
-- 
-- CoAnSys is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.

-- CoAnSys is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
-- GNU Affero General Public License for more details.
-- 
-- You should have received a copy of the GNU Affero General Public License
-- along with CoAnSys. If not, see <http://www.gnu.org/licenses/>.
--
%default RAW_DATA '/raw'
%default DOC_TERM_ALL '/term/all'
%default DOC_TERM_KEYWORDS '/term/keywords'
%default DOC_TERM_TITLE '/term/title'
%default TFIDF_NON_WEIGHTED_SUBDIR '/tfidf/nonweighted'
%default TFIDF_TOPN_WEIGHTED_SUBDIR '/tfidf/weighted-topn'
%default TFIDF_TOPN_ALL_TEMP '/tfidf/all-topn-tmp'
%default TFIDF_TOPN_ALL_SUBDIR '/tfidf/all-topn'
%default TFIDF_TF_ALL_SUBDIR '/tfidf/tf-all-topn'
%default SIMILARITY_ALL_DOCS_SUBDIR '/similarity/alldocs'
%default SIMILARITY_TOPN_DOCS_SUBDIR '/similarity/topn'
%default TERM_COUNT '/term-count'
%default WORD_RANK_PRE '/ranked-word-count-part';
%default WORD_RANK '/ranked-word-count';
%default WORD_RANK_HR '/ranked-word-count-human-readable';
%default WORD_COUNT '/filtered-by-ranked-word-count';
%default WORD_COUNT_NEG '/filtered-by-ranked-word-count_rejected';

%default tfidfTopnTermPerDocument 20
%default similarityTopnDocumentPerDocument 20
%default removal_rate 1.1
%default removal_least_used -1

%default sample 1.0
%default parallel 40
%default tmpCompressionCodec gz
%default mapredChildJavaOpts -Xmx8000m

%default inputPath 'hdfs://hadoop-master.vls.icm.edu.pl:8020/srv/bwndata/seqfile/springer-metadata/springer-20120419-springer0*.sq'
%default outputPath 'hdfs://hadoop-master.vls.icm.edu.pl:8020/srv/bwndata/seqfile/springer-metadata/springer-20120419-springer0*.sq'

%default jars '*.jar'
%default commonJarsPath 'lib/$jars'
REGISTER '$commonJarsPath'

DEFINE WeightedTFIDF pl.edu.icm.coansys.similarity.pig.udf.TFIDF('weighted');
DEFINE StemmedPairs pl.edu.icm.coansys.similarity.pig.udf.ExtendedStemmedPairs();
DEFINE KeywordSimilarity pl.edu.icm.coansys.similarity.pig.udf.AvgSimilarity('dks');
DEFINE DocsCombinedSimilarity pl.edu.icm.coansys.similarity.pig.udf.AvgSimilarity('dkcs');
DEFINE DocToTupleMap pl.edu.icm.coansys.similarity.pig.udf.DocumentProtobufToTupleMap();

SET default_parallel $parallel
SET mapred.child.java.opts $mapredChildJavaOpts
SET pig.tmpfilecompression true
SET pig.tmpfilecompression.codec $tmpCompressionCodec
set mapred.task.timeout 0
%DEFAULT scheduler default
SET mapred.fairscheduler.pool $scheduler
IMPORT 'macros.pig';

-------------------------------------------------------
-- business code section
-------------------------------------------------------
--fs -rm -r -f $outputPath
/**********
fs -rm -r -f '$outputPath$DOC_TERM_TITLE';
fs -rm -r -f '$outputPath$DOC_TERM_KEYWORDS';
fs -rm -r -f '$outputPath$DOC_TERM_ALL';
fs -rm -r -f '$outputPath$TERM_COUNT';
fs -rm -r -f '$outputPath$WORD_COUNT';
fs -rm -r -f '$outputPath$TFIDF_NON_WEIGHTED_SUBDIR';
fs -rm -r -f '$outputPath$TFIDF_TOPN_ALL_TEMP';
***********/
docIn = LOAD '$inputPath' USING pl.edu.icm.coansys.commons.pig.udf.
	RichSequenceFileLoader('org.apache.hadoop.io.Text','org.apache.hadoop.io.BytesWritable') 
	as (key:chararray, value:bytearray);
B = SAMPLE docIn $sample;
--B = limit docIn 100;
doc = FOREACH B GENERATE $0 as docId, DocToTupleMap($1) as document;

doc_raportX = foreach doc generate docId, document.title as title, document.abstract as abstract, document.keywords as keywords;
STORE doc_raportX INTO '$outputPath$RAW_DATA';
doc_raport = LOAD '$outputPath$RAW_DATA' as (docId:chararray, title:chararray, abstract:chararray, keywords:{keyword:(value:chararray)});

doc_raw = foreach doc_raport generate docId, title, abstract;
doc_keyword_raw = foreach doc_raport generate docId, FLATTEN(keywords) as keywords;

-- stem, clean, filter out
doc_keyword_all = stem_words(doc_keyword_raw, docId, keywords);
doc_title_all = stem_words(doc_raw, docId, title);
doc_abstract_all = stem_words(doc_raw, docId, abstract);

-- get all words (with duplicates for tfidf)
doc_allX = UNION doc_keyword_all, doc_title_all, doc_abstract_all;

-- store document and terms
--STORE doc_title_all INTO '$outputPath$DOC_TERM_TITLE';
--STORE doc_keyword_all INTO '$outputPath$DOC_TERM_KEYWORDS';
STORE doc_allX INTO '$outputPath$DOC_TERM_ALL';

doc_all = LOAD '$outputPath$DOC_TERM_ALL' as (docId:chararray, term:chararray);
--**************** term count **********************
terms = foreach doc_all generate term;
group_by_terms = group terms by term;
X = foreach group_by_terms generate group as term;
X1 = group X all; 
tcX = foreach X1 generate COUNT(X) as count;

store tcX into '$outputPath$TERM_COUNT';
--**************** term count **********************

--**************** word count rank *****************
tc = load '$outputPath$TERM_COUNT' as (val:double);

group_by_terms = group doc_all by term;
wcX = foreach group_by_terms generate COUNT(doc_all) as count, group as term, doc_all.docId as docs;

STORE wcX INTO '$outputPath/tc_term_docs';
wcX1 = LOAD '$outputPath/tc_term_docs' as (count:long, term:chararray,docs:{t:(docId:chararray)});

-- The RANK operator on OAP cluster rise "Java heap-space error".
-- Due to this issue the "quick replacement" (using Hadoop Streaming)
-- has been proposed. As soon as the issue with the RANK operation will be resolved,
-- the line below should be used:
-- wc_rankedX = rank wcX1 by count asc;
-- instead of "the quick replacement":
/******************************************
wc = foreach wcX1 generate count, term;
wcZ1 = foreach wcX1 generate term,docs;

define QuickReplacementRank `rank.py` ship('rank.py');

wcRank_In1 = GROUP wc all;

wcRank_In2 = FOREACH wcRank_In1 {
      D = ORDER wc BY $0;
      GENERATE D;
}

wc_rankedX1 = stream wcRank_In2 
	through QuickReplacementRank 
	as (rank_num:long, count:long,term:chararray);
	
wc_rankedX2 = join wc_rankedX1 by term, wcZ1 by term;
  
wc_rankedX = foreach wc_rankedX2 generate 
	rank_num as rank_num, 
	count as count, 
	wc_rankedX1::term as term,
	docs as docs;
	
store wc_rankedX into '$outputPath$WORD_RANK';
******************************************/
-- third approach to RANK opp
wcX11 = foreach wcX1 generate count, term;
wcX12 = order wcX11 by count asc parallel 1; 
STORE wcX12 INTO '$outputPath$WORD_RANK_PRE' using pl.edu.icm.coansys.similarity.pig.serializers.RankStorage();
wc_rankedX1 = LOAD '$outputPath$WORD_RANK_PRE' as (rank_num:long, count:long, term:chararray);

wcZ1 = foreach wcX1 generate term,FLATTEN(docs) as docs;

wc_rankedX2 = join wc_rankedX1 by term, wcZ1 by term;
  
wc_rankedX = foreach wc_rankedX2 generate 
	rank_num as rank_num, 
	count as count, 
	wc_rankedX1::term as term,
	docs as docs;
	
store wc_rankedX into '$outputPath$WORD_RANK';

/********
wc_ranked = load '$outputPath$WORD_RANK' as (rank_num:long,count:long,term:chararray,docs:{t:(docId:chararray)});
wc_ranked_hr = foreach wc_ranked generate rank_num,count,term;
store wc_ranked_hr into '$outputPath$WORD_RANK_HR'; 
********/

wc_ranked = load '$outputPath$WORD_RANK' as (rank_num:long,count:long,term:chararray,docId:chararray);

--SPLIT wc_ranked INTO
--  term_condition_accepted_tmp IF ($0 <= (double)tc.val*$removal_rate and $1 >= $removal_least_used),
--  term_condition_not_accepted_tmp IF ($0 > (double)tc.val*$removal_rate or $1 < $removal_least_used); 

term_condition_accepted_tmp = filter wc_ranked by ($0 <= (double)tc.val*$removal_rate and $1 >= $removal_least_used);
term_condition_not_accepted_tmp = filter wc_ranked by ($0 > (double)tc.val*$removal_rate or $1 < $removal_least_used);
		
-- doc_selected_termsX = foreach term_condition_accepted_tmp generate FLATTEN(docs) as docId, term;
-- store doc_selected_termsX into '$outputPath$WORD_COUNT';

store term_condition_accepted_tmp into '$outputPath$WORD_COUNT';
doc_selected_termsX2 = foreach term_condition_not_accepted_tmp generate term;
store doc_selected_termsX2 into '$outputPath$WORD_COUNT_NEG';
--**************** word count rank *****************

--****************** tfidf calc ********************
doc_selected_terms = load '$outputPath$WORD_COUNT' as (docId:chararray, term:chararray);
tfidf_allX = calculate_tfidf_nofiltering(doc_selected_terms, docId, term);
-- store tfidf values into separate direcotires
STORE tfidf_allX INTO '$outputPath$TFIDF_NON_WEIGHTED_SUBDIR';
tfidf_all = load '$outputPath$TFIDF_NON_WEIGHTED_SUBDIR' as (docId:chararray, term:chararray, tfidf:float);
-- calculate and store topn terms per document in all results
tfidf_all_topn = get_topn_per_group(tfidf_all, docId, tfidf, 'desc', $tfidfTopnTermPerDocument);
tfidf_all_topn_projectedX = FOREACH tfidf_all_topn GENERATE top::docId AS docId, top::term AS term, top::tfidf AS tfidf;
STORE tfidf_all_topn_projectedX  INTO '$outputPath$TFIDF_TOPN_ALL_TEMP';

