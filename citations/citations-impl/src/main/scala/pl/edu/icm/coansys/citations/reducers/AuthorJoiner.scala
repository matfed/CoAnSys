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
import org.apache.hadoop.io.BytesWritable
import org.apache.hadoop.mapreduce.Reducer
import pl.edu.icm.coansys.citations.data.{MatchableEntity, MarkedBytesWritable, MarkedText}
import scala.collection.mutable
import scala.annotation.varargs
import scala.collection.mutable.ListBuffer
import pl.edu.icm.coansys.citations.util.misc._


/**
 * @author Mateusz Fedoryszak (m.fedoryszak@icm.edu.pl)
 */
class AuthorJoiner extends Reducer[MarkedText, MarkedBytesWritable, BytesWritable, BytesWritable] {
  /**
   * A queue that automatically dequeues when a capacity limit is reached
   */
  class LimitedPriorityQueue[A](val capacity: Int = 20)(implicit override val ord: Ordering[A]) extends mutable.PriorityQueue[A] {
    override def enqueue(elems: A*) {
      super.enqueue(elems : _*)
      while(size > capacity) {
        dequeue()
      }
    }
  }

  type Context = Reducer[MarkedText, MarkedBytesWritable, BytesWritable, BytesWritable]#Context

  val outKey = new BytesWritable
  val outValue = new BytesWritable

  val docs = new ListBuffer[MatchableEntity]

  override def reduce(key: MarkedText, values: java.lang.Iterable[MarkedBytesWritable], context: Context) {
    implicit val ordering = new Ordering[(Double, MatchableEntity)]{
      def compare(x: (Double, MatchableEntity), y: (Double, MatchableEntity)): Int = x._1 compareTo y._1
    }

    for (value <- values) {
      val entity = MatchableEntity.fromBytes(value.bytes.copyBytes())
      if (value.isMarked.get()) {
        docs.append(entity)
      } else {
        val q = new LimitedPriorityQueue[(Double, MatchableEntity)]()
        docs.foreach{ doc =>
          val srcTokens = niceTokens(entity.toReferenceString)
          val dstTokens = niceTokens(doc.toReferenceString)

          val similarity =
            if (srcTokens.size + dstTokens.size > 0)
              2.0 * (srcTokens & dstTokens).size / (srcTokens.size + dstTokens.size)
            else
              0.0
          q.enqueue((-similarity, doc))
        }

        val citBytes = entity.data.toByteArray
        outKey.set(citBytes, 0, citBytes.length)
        for((_, doc) <- q.toSeq) {
          val docBytes = doc.data.toByteArray
          outValue.set(docBytes, 0, docBytes.length)

          context.write(outKey, outValue)
        }

      }
    }
  }
}
