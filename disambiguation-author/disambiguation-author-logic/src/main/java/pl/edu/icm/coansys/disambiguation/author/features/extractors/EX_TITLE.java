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

package pl.edu.icm.coansys.disambiguation.author.features.extractors;

import pl.edu.icm.coansys.disambiguation.author.features.extractors.indicators.DisambiguationExtractorDocument;
import pl.edu.icm.coansys.disambiguation.author.normalizers.PigNormalizer;
import pl.edu.icm.coansys.models.DocumentProtos.TextWithLanguage;

import org.apache.pig.data.DataBag;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

import pl.edu.icm.coansys.models.DocumentProtos.DocumentMetadata;

public class EX_TITLE extends DisambiguationExtractorDocument {

	public EX_TITLE() {
		super();
	}

	public EX_TITLE(PigNormalizer[] new_normalizers) {
		super(new_normalizers);
	}

	@Override
	public DataBag extract(Object o, String lang) {

		DocumentMetadata dm = (DocumentMetadata) o;
		DataBag db = new DefaultDataBag();

		for (TextWithLanguage title : dm.getBasicMetadata().getTitleList()) {
			if (lang != null && !lang.equalsIgnoreCase(title.getLanguage())) {
				continue;
			}
			String sTitle = title.getText();
			if (sTitle.isEmpty()) {
				continue;
			}
			Object normalized = normalizeExtracted(sTitle);
			if (normalized == null) {
				continue;
			}
			Tuple t = TupleFactory.getInstance().newTuple(normalized);
			db.add(t);
		}

		return db;
	}

	@Override
	public String getId() {
		return "A";
	}
}
