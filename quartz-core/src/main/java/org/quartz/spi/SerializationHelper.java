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
package org.quartz.spi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.quartz.Trigger;

/**
 *
 * @author KZ
 */
public interface SerializationHelper {
    
    public void initialize();
    
    public Object readObject(InputStream binaryInput) throws IOException, ClassNotFoundException;
    
    public void outputObject(Object object, ByteArrayOutputStream output) throws IOException;
    
    public void outputTrigger(Trigger trigger, ByteArrayOutputStream output) throws IOException;
    
}
