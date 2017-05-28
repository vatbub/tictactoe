package com.github.vatbub.tictactoe.common;

/*-
 * #%L
 * tictactoe.common
 * %%
 * Copyright (C) 2016 - 2017 Frederik Kammel
 * %%
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
 * #L%
 */


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.esotericsoftware.kryonet.FrameworkMessage;

/**
 * Common kryo tasks
 */
public class KryoCommon {
    public static void registerRequiredClasses(Kryo kryo) {
        kryo.register(FrameworkMessage.RegisterTCP.class);
        kryo.register(OnlineMultiplayerRequestOpponentRequest.class, new JavaSerializer());
        kryo.register(OnlineMultiplayerRequestOpponentResponse.class, new JavaSerializer());
        kryo.register(OnlineMultiplayerRequestOpponentException.class, new JavaSerializer());
        kryo.register(Operation.class, new JavaSerializer());
        kryo.register(ResponseCode.class, new JavaSerializer());
    }
}
