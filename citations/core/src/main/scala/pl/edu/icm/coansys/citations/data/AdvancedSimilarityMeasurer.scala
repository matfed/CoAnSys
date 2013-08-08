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

package pl.edu.icm.coansys.citations.data

import feature_calculators._
import pl.edu.icm.coansys.citations.util.classification.features.FeatureVectorBuilder
import pl.edu.icm.coansys.citations.util.SvmClassifier

/**
 * @author Mateusz Fedoryszak (m.fedoryszak@icm.edu.pl)
 */
class AdvancedSimilarityMeasurer {
  val featureVectorBuilder = new FeatureVectorBuilder(List(
    AuthorMatchFactor,
    AuthorTrigramMatchFactor,
    AuthorTokenMatchFactor,
    PagesMatchFactor,
    PagesRawTextMatchFactor,
    SourceMatchFactor,
    SourceRawTextMatchFactor,
    TitleMatchFactor,
    TitleTokenMatchFactor,
    YearMatchFactor,
    YearRawTextMatchFactor))

  val classifier = SvmClassifier.fromResource("/pl/edu/icm/coansys/citations/pubmedMatchingBetter.model")

  def similarity(e1: MatchableEntity, e2: MatchableEntity): Double =
    classifier.predictProbabilities(featureVectorBuilder.calculateFeatureVectorValues((e1, e2)))(1)
}


