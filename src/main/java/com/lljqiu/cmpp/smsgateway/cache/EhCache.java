/**
 * Project Name smsgateway
 * File Name EhCache.java
 * Package Name com.lljqiu.cmpp.smsgateway.cache
 * Create Time 2017年5月13日
 * Create by name：liujie -- email: liujie@lljqiu.com
 * Copyright © 2015, 2017, www.lljqiu.com. All rights reserved.
 */
package com.lljqiu.cmpp.smsgateway.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.InputStream;

/**
 * ClassName: EhCache.java <br>
 * Description: <br>
 * Create by: name：liujie <br>email: liujie@lljqiu.com <br>
 * Create Time: 2017年5月13日<br>
 */
public class EhCache {
    public static CacheManager manager;

    static {
        try (InputStream is = EhCache.class
                .getClassLoader()
                .getResourceAsStream("ehcache.xml")) {

            if (is == null) {
                throw new RuntimeException("ehcache.xml not found in classpath");
            }

            manager = CacheManager.create(is);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static final String CACHE_NAME = "gatewayCache";

    public EhCache() {
    }

    /**
     * Description：获取数据
     *
     * @param cacheName
     * @param key
     * @return
     * @return Object
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static Object get(String cacheName, Object key) {
        Cache cache = manager.getCache(cacheName);
        if (cache != null) {
            Element element = cache.get(key);
            if (element != null)
                return element.getObjectValue();
        }
        return null;
    }

    /**
     * Description：存储
     *
     * @param cacheName
     * @param key
     * @param value
     * @return void
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static void put(String cacheName, Object key, Object value) {
        Cache cache = manager.getCache(cacheName);
        if (cache != null) {
            cache.put(new Element(key, value));
        }
    }

    /**
     * Description：清除
     *
     * @param cacheName
     * @param key
     * @return
     * @return boolean
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static boolean remove(String cacheName, Object key) {
        Cache cache = manager.getCache(cacheName);
        if (cache != null)
            return cache.remove(key);
        else
            return false;
    }

    /**
     * Description：清空所有缓存
     *
     * @return void
     * @author name：liujie <br>email: liujie@lljqiu.com
     **/
    public static void clearAll() {
        manager.clearAll();
    }
}
