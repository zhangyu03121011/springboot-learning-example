package org.spring.springboot.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.spring.springboot.domain.City;
import org.spring.springboot.service.CityService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 城市业务逻辑实现类
 * <p>
 * Created by bysocket on 07/02/2017.
 */
@Service
public class CityServiceImpl implements CityService {


    // 模拟数据库存储
    private Map<String, City> cityMap = new HashMap<String, City>();

    public void saveCity(City city){
        // 模拟数据库插入操作
        cityMap.put(city.getCityName(), city);
    }
    
    /**
     * Cacheable:主要用来配置方法，能够根据方法的请求参数对其结果进行缓存。即当重复使用相同参数调用方法的时候，方法本身不会被调用执行，即方法本身被略过了，取而代之的是方法的结果直接从缓存中找到并返回了。
     * value：缓存的名字，必须指定至少一个。
     * key:缓存的key，可以为空，如果指定要按照SpEL表达式编写；如果不指定，则缺省按照方法的所有参数进行组合。
     * condition：缓存的条件，可以为空，使用SpEL编写，返回true或者false，只有为true才能缓存。
     */
    @Cacheable(value = "baseCityInfo",key = "'cityName:'+#p0")
    public City getCityByName(String cityName){
        // 模拟数据库查询并返回
        return cityMap.get(cityName);
    }
    
    /**
     * CachePut:主要针对方法的配置，能够根据方法的请求参数对其结果进行缓存，和@Cacheable不同的是，它每次都会触发真实方法的调用。
     * value：缓存的名字，必须指定至少一个。
     * key:缓存的key，可以为空，如果指定要按照SpEL表达式编写；如果不指定，则缺省按照方法的所有参数进行组合。
     * condition：缓存的条件，可以为空，使用SpEL编写，返回true或者false，只有为true才能缓存。
     */
    @CachePut(value = "baseCityInfo")
    public void updateCityDescription(String cityName, String description){
        City city = cityMap.get(cityName);
        city.setDescription(description);
        // 模拟更新数据库
        cityMap.put(cityName, city);
    }
    
    /**
     * @CacheEvict：主要对方法配置，用来标记要清空缓存的方法，当这个方法被调用并满足一定条件后，即会清空缓存。
     * value：缓存的位置，不能为空。
     * key：缓存的key，默认为空。
     * condition：触发的条件，只有满足条件的情况才会清楚缓存，默认为空，支持SpEL。
     * allEntries：true表示清除value中的全部缓存，默认为false。
     * @Title: cleanCache 
     * @Description: TODO
     * @param 
     * @return void
     * @throws
     */
    @CacheEvict(value = "baseCityInfo", allEntries = true)
    public void cleanCache() {}

}
