/*
 * Copyright 2020 Terracotta, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.quartz.simpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.quartz.Trigger;
import org.quartz.spi.SerializationHelper;


public class ObjectStreamSerializationImpl implements SerializationHelper {

    @Override
    public void initialize() {

    }

    @Override
    public Object readObject(InputStream binaryInput) throws IOException, ClassNotFoundException{
        Object obj = null;
        ObjectInputStream in = new ObjectInputStream(binaryInput);
        try {
            obj = in.readObject();
        } finally {
            in.close();
        }
        return obj;
    }

    @Override
    public void outputObject(Object obj, ByteArrayOutputStream baos) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(obj);
        out.flush();
    }

    @Override
    public void outputTrigger(Trigger trigger, ByteArrayOutputStream baos) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(trigger);
        oos.close();
    }
    
}
