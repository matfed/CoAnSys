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
-- -----------------------------------------------------
-- -----------------------------------------------------
-- macros section
-- -----------------------------------------------------
-- -----------------------------------------------------

--
-- model building
--
DEFINE prepairDataLocal(srcDocSim, srcDocCateg) RETURNS ret {
	ds = getLocal($srcDocSim);
	dc = getLocal($srcDocCateg);

	$ret = prepairDataGiven(dc,ds);
};

DEFINE prepairDataGivenPart(dc,ds) RETURNS ret {
	dslim = limitByCategAndPartList(ds,dc);

	dcX = group $dc by key;
	dcXX = foreach dcX generate group as key, $dc.macro_chooseValidClasses_macro_removeUnusedClasses_valid_0_0::categ as categs;

	$ret = concatOnKeyAB(dslim,dcXX);
};
--
-- test of method of building model
--

DEFINE prepairDataLocalPart(srcDocSim, srcDocCateg) RETURNS ret {
	ds = getLocal($srcDocSim);
	dc = getLocal($srcDocCateg);

	$ret = prepairDataGivenPart(dc,ds);
};

DEFINE prepairDataGivenPart(dc,ds) RETURNS ret {
	dslim = limitByCategAndPartList(ds,dc);

	dcX = group $dc by key;
	dcXX = foreach dcX generate group as key, $dc.macro_chooseValidClasses_macro_removeUnusedClasses_valid_0_0::categ as categs;

	$ret = concatOnKeyAB(dslim,dcXX);
};






DEFINE limitByCategAndPartList(ds,dc) RETURNS dslim{
	keyslim_X = foreach $dc generate key, part;
	keyslim = distinct keyslim_X;
	keyslimB = foreach keyslim generate *;

	dslimA_X = join $ds by $0, keyslim by key;
	dslimA = foreach dslimA_X generate keyA, keyB, sim, part as partA;

	dslimB_X = join dslimA by ($1,partA), keyslimB by (key,part);
	$dslim = foreach dslimB_X generate keyA, keyB, sim, partA as part;	

--	$dslim = foreach dslimB generate keyA, keyB, sim, part;
};

DEFINE concatOnKeyAB(AB,X) RETURNS concated{

	dcs_X = join $AB by keyA, $X by key;
	dcs_A = foreach dcs_X generate keyA, keyB, sim, categs as categA, part as part;
	dcs = join dcs_A by keyB, $X by key;
	$concated = foreach dcs generate keyA, keyB, sim, categA as categA, categs as categB, part as part;
};




