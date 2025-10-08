package product_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOps;
    private final ListOperations<String, Object> listOps;

    @Autowired
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.listOps = redisTemplate.opsForList();
    }

    // ---------------- Value Operations ----------------

    public Object getValue(String key) {
        try {
            return valueOps.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(String key, Object value) {
        try {
            valueOps.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setValueWithTimeout(String key, Object value, long ttl, TimeUnit unit) {
        try {
            valueOps.set(key, value, ttl, unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkExistsKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void expireKey(String key, long ttl, TimeUnit unit) {
        try {
            redisTemplate.expire(key, ttl, unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- List Operations ----------------

    public void lPushAll(String key, List<?> values) {
        try {
            listOps.leftPushAll(key, values.toArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object rPop(String key) {
        try {
            return listOps.rightPop(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getListSize(String key) {
        try {
            Long size = listOps.size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
