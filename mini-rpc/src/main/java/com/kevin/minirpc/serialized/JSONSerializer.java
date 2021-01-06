package com.kevin.minirpc.serialized;

import com.alibaba.fastjson.JSON;

/**
 * @author kevin.lee
 * @date 2021/1/3 0003
 */
public class JSONSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
