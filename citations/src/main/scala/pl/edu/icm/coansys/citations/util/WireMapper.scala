package pl.edu.icm.coansys.citations.util

import org.apache.hadoop.mapreduce.Mapper
import com.nicta.scoobi.core.WireFormat
import org.apache.hadoop.io.BytesWritable
import java.io.{DataOutputStream, ByteArrayOutputStream, DataInputStream, ByteArrayInputStream}

/**
 * @author Mateusz Fedoryszak (m.fedoryszak@icm.edu.pl)
 */
abstract class WireMapper[K1: WireFormat, V1: WireFormat, K2: WireFormat, V2: WireFormat]
  extends Mapper[BytesWritable, BytesWritable, BytesWritable, BytesWritable]{
  type Context = Mapper[BytesWritable, BytesWritable, BytesWritable, BytesWritable]#Context
  private val outKeyWritable = new BytesWritable()
  private val outValueWritable = new BytesWritable()
  private val inKeyWire = implicitly[WireFormat[K1]]
  private val inValueWire = implicitly[WireFormat[V1]]
  private val outKeyWire = implicitly[WireFormat[K2]]
  private val outValueWire = implicitly[WireFormat[V2]]

  def wireMap(value: K1, value1: V1, context: Context): Iterable[(K2, V2)]

  override def map(keyEncoded: BytesWritable, valueEncoded: BytesWritable, context: Context) {
    val inKey = inKeyWire.fromWire(new DataInputStream(new ByteArrayInputStream(keyEncoded.getBytes)))
    val inValue = inValueWire.fromWire(new DataInputStream(new ByteArrayInputStream(valueEncoded.getBytes)))

    for ((outKey, outValue) <- wireMap(inKey, inValue, context)) {
      encodeAndWrite(outKey, outValue, context)
    }
  }

  private def encodeAndWrite(key: K2, value: V2, context: Context) {
    val outKeyStream = new ByteArrayOutputStream()
    val outValueStream = new ByteArrayOutputStream()

    outKeyWire.toWire(key, new DataOutputStream(outKeyStream))
    outValueWire.toWire(value, new DataOutputStream(outValueStream))

    val outKeyBytes = outKeyStream.toByteArray
    val outValueBytes = outValueStream.toByteArray

    outKeyWritable.set(outKeyBytes, 0, outKeyBytes.length)
    outKeyWritable.set(outValueBytes, 0, outValueBytes.length)

    context.write(outKeyWritable, outValueWritable)
  }

}
