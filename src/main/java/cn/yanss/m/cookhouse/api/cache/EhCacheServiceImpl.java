package cn.yanss.m.cookhouse.api.cache;

import cn.yanss.m.cookhouse.api.utils.MapperUtils;
import cn.yanss.m.util.LogUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hl
 */
@Component
public class EhCacheServiceImpl {


    @Autowired
    private CacheManager cacheManager;

    private volatile Cache cache;

    @PostConstruct
    public void init() {
        cache = cacheManager.getCache("cache");
    }

    public void save(String key, Object value) {
        Element element = new Element(key, value);
        cache.put(element);
        LogUtils.create().methodName("EhCacheServiceImpl").key(key).message("订单缓存或修改").info();
    }

    /**
     * 注意:后期测试去除json转换
     *
     * @param key
     * @return
     */
    public <T> T getValue(String key, Class<T> clz) {
        Element element = cache.get(key);
        if (null == element) {
            return null;
        }
        Object obj = element.getObjectValue();
        return MapperUtils.obj2pojo(obj, clz);
    }

    public Object getObj(String key) {
        Element element = cache.get(key);
        if (null == element) {
            return null;
        }
        return element.getObjectValue();

    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void update(String key, Object value) {
        cache.replace(new Element(key, value));
    }

    public List<Object> getList(List<String> key) {
        Map<Object, Element> map = cache.getAll(key);
        if (null == map || map.size() == 0) {
            return Collections.emptyList();
        }
        return map.values().stream().filter(d -> null != d).map(d -> d.getObjectValue()).collect(Collectors.toList());
    }

    public boolean exists(Object key) {
        return cache.isKeyInCache(key);
    }
}
