/*
 * This file is part of CoAnSys project.
 * Copyright (c) 2012-2014 ICM-UW
 *
 * CoAnSys is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CoAnSys is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with CoAnSys. If not, see <http://www.gnu.org/licenses/>.
 */

package pl.edu.icm.coansys.citations.mappers

import pl.edu.icm.coansys.citations.data.{MarkedBytesWritable, MarkedText, MatchableEntity}
import org.testng.annotations.Test
import org.apache.hadoop.mrunit.mapreduce.MapDriver
import org.apache.hadoop.io.{BytesWritable, NullWritable}

/**
 * Created by matfed on 06.02.14.
 */
class EntityAuthorTaggerTest {
  val cit1 = MatchableEntity.fromParameters(id = "cit1", rawText = "Jan Kowalski 2010 1000 3000").data.toByteArray

  @Test(groups = Array("fast"))
  def mapperByteCopyTest() {
    val driver = MapDriver.newMapDriver(new EntityAuthorTagger)
    driver.getConfiguration.setBoolean("coansys.citations.mark.citations", false)
    driver.addInput(NullWritable.get(), new BytesWritable(cit1))

    driver.addOutput(new MarkedText("jan2009"), new MarkedBytesWritable(cit1))
    driver.addOutput(new MarkedText("jan2010"), new MarkedBytesWritable(cit1))
    driver.addOutput(new MarkedText("jan2011"), new MarkedBytesWritable(cit1))
    driver.addOutput(new MarkedText("kowalski2009"), new MarkedBytesWritable(cit1))
    driver.addOutput(new MarkedText("kowalski2010"), new MarkedBytesWritable(cit1))
    driver.addOutput(new MarkedText("kowalski2011"), new MarkedBytesWritable(cit1))
//    driver.addInput(new BytesWritable(cit1), new BytesWritable(doc0))
//    driver.addOutput(new Text("cit1"), new BytesWritable(cit1))
//    driver.addInput(new BytesWritable(cit1), new BytesWritable(doc1))
//    driver.addOutput(new Text("cit1"), new BytesWritable(cit1))
//    driver.addInput(new BytesWritable(cit1), new BytesWritable(doc2))
//    driver.addOutput(new Text("cit1"), new BytesWritable(cit1))

    driver.runTest(false)
  }
}