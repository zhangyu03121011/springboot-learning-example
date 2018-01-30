import java.util.HashSet;
import java.util.Set;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;

public final class RedisUtil {
    
    //Redis服务器IP
//    private static String ADDR = "127.0.0.1";
    
    //Redis的端口号
//    private static int PORT = 6380;
    
    //访问密码
    private static String AUTH = "zhangxiaoyu";
    
    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
//    private static int MAX_ACTIVE = 1024;
    
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
//    private static int MAX_IDLE = 200;
    
//    private static int TIMEOUT = 10000;
    
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
//    private static boolean TEST_ON_BORROW = true;
    
    private static JedisPool jedisPool = null;
    
    private static JedisSentinelPool sentinelPool = null;
    
    /**
     * 初始化Redis连接池
     */
    static {
        try {
        	//redis连接池
//            JedisPoolConfig config = new JedisPoolConfig();
//            config.setMaxTotal(MAX_ACTIVE);
//            config.setMaxIdle(MAX_IDLE);
//            config.setTestOnBorrow(TEST_ON_BORROW);
//            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, AUTH);
            
//            JedisPoolConfig poolConfig = new JedisPoolConfig();
//        	// 最大连接数
//        	poolConfig.setMaxTotal(MAX_ACTIVE);
//        	// 最大空闲数
//        	poolConfig.setMaxIdle(MAX_IDLE);
//        	// 最大允许等待时间，如果超过这个时间还未获取到连接，则会报JedisException异常：
//        	// Could not get a resource from the pool
//        	poolConfig.setMaxWaitMillis(1000);
//        	Set<HostAndPort> nodes = new LinkedHashSet<HostAndPort>();
//        	nodes.add(new HostAndPort("127.0.0.1", 6379));
//        	nodes.add(new HostAndPort("127.0.0.1", 6380));
//        	nodes.add(new HostAndPort("127.0.0.1", 6381));
//        	JedisCluster cluster = new JedisCluster(nodes, poolConfig);
            
            Set<String> sentinels = new HashSet<String>();
    		sentinels.add(new HostAndPort("127.0.0.1", 26379).toString());
    		sentinels.add(new HostAndPort("127.0.0.1", 26479).toString());
    		sentinels.add(new HostAndPort("127.0.0.1", 26579).toString());
            sentinelPool = new JedisSentinelPool("mymaster", sentinels);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取Jedis实例
     * @return
     */
    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取sentinelPool Jedis实例
     * @return
     */
    public synchronized static Jedis getSentinelPoolJedis() {
        try {
            if (sentinelPool != null) {
                Jedis resource = sentinelPool.getResource();
                resource.auth(AUTH);
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String getCurrentHostMaster() {
    	return sentinelPool.getCurrentHostMaster().toString();
    }
    
    /**
     * 释放jedis资源
     * @param jedis
     */
	public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
        	jedis.close();
        }
    }
    
    /**
     * 释放jedis资源
     * @param jedis
     */
    public static void closeSentinelPoolResource(final Jedis jedis) {
    	if (jedis != null) {
        	jedis.close();
        }
    }
}