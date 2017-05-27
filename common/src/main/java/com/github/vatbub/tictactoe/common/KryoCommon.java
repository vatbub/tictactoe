package com.github.vatbub.tictactoe.common;

import com.esotericsoftware.kryo.Kryo;

/**
 * Common kryo tasks
 */
public class KryoCommon {
    public static void registerRequiredClasses(Kryo kryo){
        kryo.register(OnlineMultiplayerRequestOpponentRequest.class);
        kryo.register(OnlineMultiplayerRequestOpponentResponse.class);
        kryo.register(OnlineMultiplayerRequestOpponentException.class);
    }
}
