/*
 * This file is part of CoAnSys project.
 * Copyright (c) 2012-2013 ICM-UW
 * 
 * CoAnSys is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * CoAnSys is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with CoAnSys. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.coansys.similarity.pig.udf;

import java.io.IOException;
import java.util.HashSet;

import org.apache.pig.EvalFunc;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.tools.pigstats.PigStatusReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.icm.coansys.commons.java.DiacriticsRemover;
import pl.edu.icm.coansys.commons.java.StackTraceExtractor;
import pl.edu.icm.coansys.models.DocumentProtos.Author;
import pl.edu.icm.coansys.models.DocumentProtos.DocumentMetadata;
import pl.edu.icm.coansys.models.DocumentProtos.DocumentWrapper;

/**
 * 
 * @author pdendek
 * 
 */
public class DocSimDemo_Authors extends EvalFunc<DataBag> {

	private static final Logger logger = LoggerFactory
			.getLogger(DocSimDemo_Authors.class);
	PigStatusReporter myreporter;

	@Override
	public DataBag exec(Tuple input) throws IOException {

		myreporter = PigStatusReporter.getInstance();

		if (input == null || input.size() == 0) {
			return null;
		}

		try {
			TupleFactory tf = TupleFactory.getInstance();
			DataByteArray dba = null;
			
			DocumentMetadata dm = null;
			String doi = null;

			try {
				dba = (DataByteArray) input.get(0);
			} catch (Exception e) {
				myreporter.getCounter("extraction problems [Auth]",
						"DataByteArray from tuple").increment(1);
				return null;
			}

			try {
				dm = DocumentWrapper.parseFrom(dba.get()).getDocumentMetadata();
				doi = dm.getBasicMetadata().getDoi().replaceAll("\\s++", " ").trim();
				if(doi.length()==0){
					throw new Exception("Lack of doi");
				}
			} catch (Exception e) {
				myreporter.getCounter("extraction problems [Auth]",
						"document metadata | lack of doi").increment(1);
				return null;
			}

			DataBag ret = new DefaultDataBag();

			int authNum = 0;
			
			HashSet hs = new HashSet();
			
			for (Author a : dm.getBasicMetadata().getAuthorList()) {
				try {
					String sname = a.getSurname();
					String fname = a.getForenames();
					String name = null;
					if (sname != null && !sname.trim().isEmpty()
							&& fname != null && !fname.trim().isEmpty()) {
						
						sname = DiacriticsRemover.removeDiacritics(sname);
						sname = sname.replaceAll("[^A-Za-z]", " ").replaceAll("\\s++", " ").trim();
						
						fname = DiacriticsRemover.removeDiacritics(fname);
						fname = fname.replaceAll("[^A-Za-z]", " ").replaceAll("\\s++", " ").trim();
						
						if(fname.length()>1){
							name = sname + ", " + fname.trim().substring(0, 1)+".";
						}else{
							name = sname;
						}
						
					}
					if (name != null) {
						if(hs.contains(name)){
							continue;
						}
						Tuple t = tf.newTuple();
						t.append(doi);
						t.append(authNum);
						t.append(name);
						ret.add(t);
						authNum++;
					} else {
						continue;
					}
				} catch (Exception e) {
					log.debug(StackTraceExtractor.getStackTrace(e));
				}
			}
			return ret;
		} catch (Exception e) {
			logger.debug(StackTraceExtractor.getStackTrace(e));
			throw new IOException(e);
		}
	}
}