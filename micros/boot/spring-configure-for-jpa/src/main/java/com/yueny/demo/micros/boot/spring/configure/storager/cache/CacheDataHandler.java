/**
 *
 */
package com.yueny.demo.micros.boot.spring.configure.storager.cache;

/**
 * 缓存数据执行器
 *
 * @author yueny09 <deep_blue_yang@163.com>
 *
 * @DATE 2016年11月17日 下午2:15:41
 * @since 1.5.3
 */
public interface CacheDataHandler<T> {
	/**
	 * @return
	 */
	T caller();
}
