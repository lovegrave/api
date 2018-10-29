package cn.yanss.m.cookhouse.api.cache;

import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.util.LogUtils;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis封装类
 * Created by a on 2018/3/19.
 *
 * @author HL
 */
@Service
public class RedisService {

    /**
     * 日志对象
     */
    @Resource
    private JedisPool jedisPool;

    /**
     * 日志对象
     */
    public synchronized Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        jedis.select(4);
        return jedis;
    }

    /**
     * 删除redis一个键值对
     *
     * @param key
     */
    public void remove(String key) {
        Jedis jedis = getJedis();
        try {
            Boolean exists = jedis.exists(key);
            if (exists) {
                jedis.del(key);
            }
        } finally {
            jedis.close();
        }

    }

    /**
     * 删除hash(map)内的一个键值对
     *
     * @param key
     * @param field
     */
    public void hashDel(String key, String field) {
        Jedis jedis = getJedis();
        try {
            Boolean exists = jedis.exists(key);
            if (exists) {
                jedis.hdel(key, field);
            }
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取Map对象
     */
    public Map getMap(final String key) {
        try {
            Jedis jedis = getJedis();
            try {
                return jedis.hgetAll(key);
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            LogUtils.create().methodName("getMap").key(key).message(e.getMessage()).error();
            return Collections.emptyMap();
        }
    }

    /**
     * 写入Map对象
     * 无失效时间
     *
     * @param key
     * @param map
     * @return
     */
    public String setMap(String key, Map<String, String> map) {
        Jedis jedis = getJedis();
        String result;
        try {
            result = jedis.hmset(key, map);
        } finally {
            jedis.close();
        }
        return result;
    }

    /**
     * 将一个键值对存入redis map中
     *
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return
     */
    public Long setMapString(String key, String field, String value, int seconds) {
        Jedis jedis = getJedis();
        Long result = 0L;
        try {
            result = jedis.hset(key, field, value);
            jedis.expire(key, seconds);
        } finally {
            jedis.close();
        }
        return result;
    }

    /**
     * 存多个键值对map
     *
     * @param key
     * @param map
     * @param seconds
     * @return
     */
    public String hmset(String key, Map<String, String> map, int seconds) {
        Jedis jedis = getJedis();
        String result;
        try {
            result = jedis.hmset(key, map);
            jedis.expire(key, seconds);
        } finally {
            jedis.close();
        }
        return result;
    }

    /**
     * map 取一个键值对
     */
    public String getMapString(String key, String field) {
        Jedis jedis = getJedis();
        try {
            return jedis.hget(key, field);
        } catch (Exception e) {
            LogUtils.create().key(key).methodName("getMapString").error();
            return null;
        } finally {
            jedis.close();
        }
    }

    public Map<String, String> getAllMap(String key) {

        Jedis jedis = getJedis();
        try {
            return jedis.hgetAll(key);
        } catch (Exception e) {
            LogUtils.create().methodName("getAllMap").key(key).error();
            return null;
        } finally {
            jedis.close();
        }
    }

    /**
     * 获取String
     */
    public String getString(String key) {
        Jedis jedis = getJedis();
        String result = null;
        try {
            result = jedis.get(key);
        } finally {
            jedis.close();
        }
        return result;
    }


    /**
     * 写入String
     *
     * @param key
     * @param value
     */
    public String setString(String key, String value) {
        Jedis jedis = getJedis();
        String result;
        try {
            result = jedis.set(key, value);
        } finally {
            jedis.close();
        }
        return result;
    }

    /**
     * 写入String（有效期）
     *
     * @param key
     * @param value
     * @param seconds 有效期 单位：s
     */
    public String setString(String key, String value, int seconds) {
        Jedis jedis = getJedis();
        String result;
        try {
            result = jedis.setex(key, seconds, value);
        } finally {
            jedis.close();
        }
        return result;
    }


    /**
     * 写入object对象
     */
    public String setObject(String key, Object object, int seconds) {
        String result = null;
        Jedis jedis = getJedis();
        try {
            result = jedis.set(key, MapperUtils.obj2jsonIgnoreNull(object));
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LogUtils.create().key(key).methodName("setObject").message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return result;
    }

    /**
     * 获取object对象
     */
    public <T> T getObject(String key, Class<T> clz) {
        try {
            Jedis jedis = getJedis();
            try {
                String result = jedis.get(key);
                if (result != null) {
                    return MapperUtils.json2pojo(result, clz);
                }
            } finally {
                jedis.close();
            }

        } catch (Exception e) {
            LogUtils.create().methodName("getObject").key(key).message(e.getMessage()).error();
            return null;
        }
        return null;
    }


    /**
     * 写入一个值到redis中的list集合
     */
    public Long lpush(String key, String value, int seconds) {

        Long result = 0L;
        Jedis jedis = getJedis();
        try {
            result = jedis.lpush(key, value);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LogUtils.create().key(key).methodName("lpush").message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return result;
    }

    public Long lpush(String key, int seconds, String... value) {
        Long result = 0L;
        Jedis jedis = getJedis();
        try {
            result = jedis.lpush(key, value);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LogUtils.create().methodName("lpush").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return result;
    }


    public String lpop(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.lpop(key);
        } catch (Exception e) {
            LogUtils.create().key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return "";
    }

    public Long lrem(String key, String value) {
        Jedis jedis = getJedis();
        try {
            return jedis.lrem(key, 0, value);
        } catch (Exception e) {
            LogUtils.create().methodName("lrem").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return 0L;
    }

    /**
     * 取list
     */
    public List<String> lpList(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.lrange(key, 0, -1);
        } catch (Exception e) {
            LogUtils.create().key(key).methodName("lplist").message(e.getMessage()).error();
            return Collections.emptyList();
        } finally {
            jedis.close();
        }
    }

    /**
     * redis 自增
     *
     * @param key
     * @return
     */
    public Long incr(String key, Integer seconds) {

        Long value;
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.select(1);
            value = jedis.incr(key);
            jedis.expire(key, seconds);
        } finally {
            jedis.close();
        }
        return value;
    }

    /**
     * 检测缓存中是否有对应的value
     */
    public Boolean exists(String key) {

        Boolean result = false;
        Jedis jedis = getJedis();
        try {
            result = jedis.exists(key);
        } finally {
            jedis.close();
        }
        return result;
    }

    /**
     * SortedSet
     */

    /**
     * 添加元素到集合，元素在集合中存在则更新对应score
     * sorted Set
     *
     * @param key
     * @param score
     * @param member
     */
    public void zadd(String key, double score, String member, Integer seconds) {
        Jedis jedis = getJedis();
        try {
            jedis.zadd(key, score, member);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LogUtils.create().methodName("zadd").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
    }


    /**
     * 查询范围内的元素集合
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    public Set<String> zrange(String key, Integer start, Integer stop) {
        Jedis jedis = getJedis();
        try {
            return jedis.zrange(key, start, stop);
        } catch (Exception e) {
            LogUtils.create().methodName("zrange").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return Collections.emptySet();
    }

    /**
     * 查询有序集合的第一个元素以及score值
     *
     * @param key
     * @return
     */
    public Set<Tuple> zrangeScore(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeWithScores(key, 0, 0);
        } catch (Exception e) {
            LogUtils.create().methodName("zrangeScore").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return Collections.emptySet();
    }

    /**
     * 返回元素在在集合中的排名
     *
     * @param key
     * @param member
     * @return
     */
    public Long zrank(String key, String member) {
        Jedis jedis = getJedis();
        try {
            return jedis.zrank(key, member);
        } catch (Exception e) {
            LogUtils.create().methodName("zrank").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return null;
    }

    /**
     * 删除集合中一个或者多个元素
     *
     * @param key
     * @param member
     * @return
     */
    public Long zrem(String key, String... member) {
        Jedis jedis = getJedis();
        try {
            if (jedis.exists(key)) {
                return jedis.zrem(key, member);
            }
        } catch (Exception e) {
            LogUtils.create().methodName("zrem").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return Long.valueOf(0);
    }

    /**
     * 删除指定排名的元素
     *
     * @param key
     * @param start
     * @param stop
     * @return
     */
    public Long zremRangeRank(String key, Integer start, Integer stop) {
        Jedis jedis = getJedis();
        try {
            return jedis.zremrangeByRank(key, start, stop);
        } catch (Exception e) {
            LogUtils.create().methodName("zremRangeRank").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return Long.valueOf(0);
    }

    /**
     * 返回一个元素的score值
     *
     * @param key
     * @param member
     * @return
     */
    public Double zscore(String key, String member) {
        Jedis jedis = getJedis();
        try {
            return jedis.zscore(key, member);
        } catch (Exception e) {
            LogUtils.create().methodName("zscore").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return null;
    }

    /**
     * set
     */
    /**
     * @param key
     * @param seconds
     * @param value
     * @return
     */
    public Long sadd(String key, int seconds, String... value) {
        Jedis jedis = getJedis();
        Long len = Long.valueOf(0);
        try {
            len = jedis.sadd(key, value);
            jedis.expire(key, seconds);
        } catch (Exception e) {
            LogUtils.create().methodName("sadd").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return len;
    }

    /**
     * 删除set集合中的元素
     *
     * @param key
     * @param value
     * @return
     */
    public Long srem(String key, String... value) {
        Jedis jedis = getJedis();
        try {
            return jedis.srem(key, value);
        } catch (Exception e) {
            LogUtils.create().methodName("srem").key(key).message(e.getMessage()).error();
            return Long.valueOf(0);
        } finally {
            jedis.close();
        }
    }

    /**
     * 查询set集合中所有元素
     *
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.smembers(key);
        } catch (Exception e) {
            LogUtils.create().methodName("smembers").key(key).message(e.getMessage()).error();
            return Collections.emptySet();
        } finally {
            jedis.close();
        }
    }

    /**
     * 判断该数据是否属于该集合
     *
     * @param key
     * @param value
     * @return
     */
    public Boolean sismember(String key, String value) {
        Jedis jedis = getJedis();
        try {
            return jedis.sismember(key, value);
        } catch (Exception e) {
            LogUtils.create().methodName("sismember").key(key).message(e.getMessage()).error();
            return false;
        } finally {
            jedis.close();
        }
    }

    public Set<String> sdiff(String key1, String key2) {
        Jedis jedis = getJedis();
        try {
            return jedis.sdiff(key1, key2);
        } catch (Exception e) {
            LogUtils.create().methodName("sdiff").key(key1).message(e.getMessage()).error();
            return Collections.emptySet();
        } finally {
            jedis.close();
        }
    }

    /**
     * 将一个元素从key1集合移动到key2集合
     *
     * @param key1    源集合
     * @param key2    目标集合
     * @param value   值
     * @param seconds
     * @return
     */
    public Long smove(String key1, String key2, String value, int seconds) {
        Jedis jedis = getJedis();
        try {
            Long l = jedis.smove(key1, key2, value);
            jedis.expire(key2, seconds);
            jedis.expire(key1, seconds);
            return l;
        } catch (Exception e) {
            LogUtils.create().methodName("smove").key(key1 + "..." + key2).message(e.getMessage()).error();
            return null;
        } finally {
            jedis.close();
        }
    }

    /**
     * redis 乐观锁 ，监控key
     *
     * @param key
     * @return
     */
    public String watch(String key) {
        Jedis jedis = getJedis();
        try {
            return jedis.watch(key);
        } catch (Exception e) {
            LogUtils.create().methodName("watch").key(key).message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return null;
    }

    /**
     * 将接下来的写操作放入事物队列
     *
     * @return
     */
    public Transaction multi() {
        Jedis jedis = getJedis();
        try {
            return jedis.multi();
        } catch (Exception e) {
            LogUtils.create().methodName("multi").message(e.getMessage()).error();
        } finally {
            jedis.close();
        }
        return null;
    }


}