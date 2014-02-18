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

package pl.edu.icm.coansys.citations.reducers

import collection.JavaConversions._
import org.apache.hadoop.io.{Text, BytesWritable}
import org.apache.hadoop.mapreduce.Reducer
import pl.edu.icm.coansys.citations.data.{MatchableEntity, MarkedBytesWritable, MarkedText}
import pl.edu.icm.coansys.citations.util.misc._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


/**
 * @author Mateusz Fedoryszak (m.fedoryszak@icm.edu.pl)
 */
class AuthorJoinerCounter extends Reducer[MarkedText, MarkedBytesWritable, Text, Text] {
  type Context = Reducer[MarkedText, MarkedBytesWritable, BytesWritable, BytesWritable]#Context

  val outKey = new Text()
  val outValue = new Text()

  override def reduce(key: MarkedText, values: java.lang.Iterable[MarkedBytesWritable], context: Context) {
    var docs = 0
    var cits = 0

    for (value <- values) {
      if (value.isMarked.get()) {
        docs += 1
      } else {
        cits += 1
      }
      outKey.set(key.text)
      outValue.set(docs.toString + " " + cits.toString)
      context.write(outKey, outValue)
    }
  }
}
