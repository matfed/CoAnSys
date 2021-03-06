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

package pl.edu.icm.coansys.commons.pig.udf;

import java.io.IOException;

import org.apache.pig.builtin.PigStorage;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;

public class NuberedPigStorage extends PigStorage {

	static Long index = 0l;
	TupleFactory tf = null;
	
	public NuberedPigStorage(){
		super();
		index = 0l;
	}
	
	public NuberedPigStorage(long startIndex){
		super();
		index = startIndex;
	}
	
	@Override
    public void putNext(Tuple f) throws IOException {
        try {
        	Tuple t = TupleFactory.getInstance().newTuple();
        	t.append(index);
        	for(Object o : f){
    			t.append(o);
    		}
    		index++;
        	
            writer.write(null, t);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}

