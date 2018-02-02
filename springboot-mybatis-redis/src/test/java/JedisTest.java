import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

/**
 * @Description: redis主从配置+哨兵模式
 * Sentinel作用： 
 * 	1：Master状态检测 
 * 	2：如果Master异常，则会进行Master-Slave切换，将其中一个Slave作为Master，将之前的Master作为Slave 
 * 	3：Master-Slave切换后，master_redis.conf、slave_redis.conf和sentinel.conf的内容都会发生改变，即master_redis.conf中会多一行slaveof的配置，sentinel.conf的监控目标会随之调换
 * 配置步骤：
 * 	1.在redis的配置文件中添加鉴权和验证（添加requirepass和masterauth），redis主从都需要配置，配置的密码一般相同。
 * 	2、创建并修改sentinel.conf
 * 		#sentinel端口
 * 		port 26379
 * 		#工作路径，注意路径不要和主重复
 *		dir "/usr/local/redis-6379"
 *		# 守护进程模式
 *		daemonize yes
 *		#关闭保护模式
 *		protected-mode no
 *		# 指明日志文件名
 *		logfile "./sentinel.log"
 *		#哨兵监控的master，主从配置一样，这里只用输入redis主节点的ip/port和法定人数。
 *		sentinel monitor mymaster 127.0.0.1 6379 2
 *		# master或slave多长时间（默认30秒）不能使用后标记为s_down状态。
 *		sentinel down-after-milliseconds mymaster 5000
 *		#若sentinel在该配置值内未能完成failover操作（即故障时master/slave自动切换），则认为本次failover失败。
 *		sentinel failover-timeout mymaster 18000
 *		#设置master和slaves验证密码
 *		sentinel auth-pass mymaster 123456 
 *		sentinel parallel-syncs mymaster 1//指定了在执行故障转移时， 最多可以有多少个从服务器同时对新的主服务器进行同步
 * 	3、分别启动master，slave1，slave2
 * 		启动命定:
 * 			redis-server.exe redis.conf 
 * 			redis-server.exe redis6380.conf 
 * 			redis-server.exe redis6381.conf 
 * 	4、分别启动sentinel1，sentinel2，sentinel3
 * 		启动命定:
 * 			redis-server.exe sentinel.conf --sentinel  
 * 			redis-server.exe sentinel26479.conf --sentinel
 * 			redis-server.exe sentinel26579.conf --sentinel 
 * 	5、查看redis服务器状态
 * 		info replication
 * 	6、查看sentinel的状态
 * 		info sentinel
 * @author zhangxy
 * @date 2018年1月30日 上午10:16:42
 */
public class JedisTest {
	
	private Jedis jedis;
	
	@Before
	public void init() {
		jedis = RedisUtil.getSentinelPoolJedis();
	}
	
	@Test
	public void testApi() {
		System.out.println(jedis.exists("age"));
		
		Set<String> keys = jedis.keys("*");
		for (String string : keys) {
			String type = jedis.type(string);
			System.out.println(type);
			String string2 = jedis.get(string);
			System.out.println(string2);
		}
	}
	
	@Test
	public void testExpire() {
		jedis.setex("expire", 10,"expire-10s");
		System.out.println(jedis.get("expire"));
	}
	
	/**
	 * @Description: 哨兵集群模式
	 * @DateTime:2018年1月30日 上午10:15:40
	 * @return void
	 * @throws
	 */
	@Test
	public void testSentinels() {
		Set<String> sentinels = new HashSet<String>();
		sentinels.add(new HostAndPort("127.0.0.1", 26379).toString());
		sentinels.add(new HostAndPort("127.0.0.1", 26479).toString());
		sentinels.add(new HostAndPort("127.0.0.1", 26579).toString());
		System.out.println("Current master: " + RedisUtil.getCurrentHostMaster());
		Jedis master = RedisUtil.getSentinelPoolJedis();
		master.auth("zhangxiaoyu");
		master.set("sentinelTest4","ok");
		Jedis master2 = RedisUtil.getSentinelPoolJedis();
		master2.auth("zhangxiaoyu");
		String value = master2.get("sentinelTest4");
		System.out.println("sentinelTest: " + value);
		RedisUtil.closeSentinelPoolResource(master);
		RedisUtil.closeSentinelPoolResource(master2);
	}
	
	/**
     * redis存储字符串
     */
    @Test
    public void testString() {
        //-----添加数据----------  
        jedis.set("name","xinxin");//向key-->name中放入了value-->xinxin  
        System.out.println(jedis.get("name"));//执行结果：xinxin  
        
        jedis.append("name", " is my lover"); //拼接
        System.out.println(jedis.get("name")); 
        
        jedis.del("name");  //删除某个键
        System.out.println(jedis.get("name"));
        //设置多个键值对
        jedis.mset("name","liuling","age","23","qq","476777XXX");
        jedis.incr("age"); //进行加1操作
        System.out.println(jedis.get("name") + "-" + jedis.get("age") + "-" + jedis.get("qq"));
        
    }
    
    /**
     * redis操作Map
     */
    @Test
    public void testMap() {
        //-----添加数据----------  
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "xinxin");
        map.put("age", "22");
        map.put("qq", "123456");
        jedis.del("user");//先清除数据，再加入数据进行测试  
        jedis.hmset("user",map);
        //取出user中的name，执行结果:[minxr]-->注意结果是一个泛型的List  
        //第一个参数是存入redis中map对象的key，后面跟的是放入map中的对象的key，后面的key可以跟多个，是可变参数  
        List<String> rsmap = jedis.hmget("user", "name", "age", "qq");
        System.out.println(rsmap);  
  
        //删除map中的某个键值  
        jedis.hdel("user","age");
        System.out.println(jedis.hmget("user", "age")); //因为删除了，所以返回的是null  
        System.out.println(jedis.hlen("user")); //返回key为user的键中存放的值的个数2 
        System.out.println(jedis.exists("user"));//是否存在key为user的记录 返回true  
        System.out.println(jedis.hkeys("user"));//返回map对象中的所有key  
        System.out.println(jedis.hvals("user"));//返回map对象中的所有value 
  
        Iterator<String> iter=jedis.hkeys("user").iterator();  
        while (iter.hasNext()){  
            String key = iter.next();  
            System.out.println(key+":"+jedis.hmget("user",key));  
        }  
    }
    
    /** 
     * jedis操作List 
     */  
    @Test  
    public void testList(){  
        //开始前，先移除所有的内容  
        jedis.del("java framework");  
        System.out.println(jedis.lrange("java framework",0,-1));  
        //先向key java framework中存放三条数据  
        jedis.lpush("java framework","spring");  
        jedis.lpush("java framework","struts");  
        jedis.lpush("java framework","hibernate");  
        //再取出所有数据jedis.lrange是按范围取出，  
        // 第一个是key，第二个是起始位置，第三个是结束位置，jedis.llen获取长度 -1表示取得所有  
        System.out.println(jedis.lrange("java framework",0,-1));  
        
        jedis.del("java framework");
        jedis.rpush("java framework","spring");  
        jedis.rpush("java framework","struts");  
        jedis.rpush("java framework","hibernate"); 
        System.out.println(jedis.lrange("java framework",0,-1));
    }  
    
    /** 
     * jedis操作Set 
     */  
    @Test  
    public void testSet(){  
        //添加  
    	jedis.del("user");//先清除数据，再加入数据进行测试  
        jedis.sadd("user","liuling");  
        jedis.sadd("user","xinxin");  
        jedis.sadd("user","ling");  
        jedis.sadd("user","zhangxinxin");
        jedis.sadd("user","who");  
        //移除noname  
        jedis.srem("user","who");  
        System.out.println(jedis.smembers("user"));//获取所有加入的value  
        System.out.println(jedis.sismember("user", "who"));//判断 who 是否是user集合的元素  
        System.out.println(jedis.srandmember("user"));  
        System.out.println(jedis.scard("user"));//返回集合的元素个数  
    }  
  
    @Test  
    public void test() throws InterruptedException {  
        //jedis 排序  
        //注意，此处的rpush和lpush是List的操作。是一个双向链表（但从表现来看的）  
        jedis.del("a");//先清除数据，再加入数据进行测试  
        jedis.rpush("a", "1");  
        jedis.lpush("a","6");  
        jedis.lpush("a","3");  
        jedis.lpush("a","9");  
        System.out.println(jedis.lrange("a",0,-1));// [9, 3, 6, 1]  
        System.out.println(jedis.sort("a")); //[1, 3, 6, 9]  //输入排序后结果  
        System.out.println(jedis.lrange("a",0,-1));  
    }  
}
