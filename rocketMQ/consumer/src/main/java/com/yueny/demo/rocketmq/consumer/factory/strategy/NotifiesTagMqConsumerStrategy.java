package com.yueny.demo.rocketmq.consumer.factory.strategy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.yueny.demo.rocketmq.MqConstants;
import com.yueny.demo.rocketmq.MqConstants.Tags;
import com.yueny.demo.rocketmq.common.CounterHepler;
import com.yueny.demo.rocketmq.consumer.data.ScallorEvent;
import com.yueny.demo.rocketmq.core.factory.consumer.strategy.AbstractMqConsumerStrategy;
import com.yueny.demo.rocketmq.core.factory.consumer.strategy.IConsumerStrategy;
import com.yueny.demo.rocketmq.data.JSONEvent;
import com.yueny.rapid.lang.json.JsonUtil;

/**
 * 普通的 消息订阅
 *
 * @author yueny09 <deep_blue_yang@163.com>
 *
 * @DATE 2016年3月13日 下午3:15:14
 */
@Service
public class NotifiesTagMqConsumerStrategy extends AbstractMqConsumerStrategy<ScallorEvent>
		implements IConsumerStrategy<MqConstants.Tags> {
	/**
	 * 数据组装任务
	 */
	private class AssemblyEventDataTask implements Runnable {
		@Override
		public void run() {
			while (true) {
				ScallorEvent event = null;
				try {
					event = getQueue().poll(500L, TimeUnit.MILLISECONDS);

					if (event == null) {
						try {
							TimeUnit.MILLISECONDS.sleep(500L);
						} catch (final Exception e) {
							logger.error("监听" + getCondition() + "消息无数据，等待下次操作！");
						}
					}
				} catch (final InterruptedException ignore) {
					continue;
				}

				if (event == null) {
					continue;
				}

				try {
					CounterHepler.increment();

					logger.info("{} -->完成{}消息处理 in {} :{}/{}.", CounterHepler.get(), getCondition(),
							Thread.currentThread().getName(), event.getMsgId(), event.getMessageId());
				} catch (final Exception e) {
					put(event);
					logger.error("监听" + getCondition() + "消息异常，重新入池进行等待下次操作！", e);
				}
			}
		}
	}

	/**
	 * 只支持一个线程的线程池，配置corePoolSize=maximumPoolSize=1，无界阻塞队列LinkedBlockingQueue；
	 * 保证任务由一个线程串行执行
	 */
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public NotifiesTagMqConsumerStrategy() {
		super();

		executorService.execute(new AssemblyEventDataTask());
	}

	/**
	 * 如果队列是满的话，报错
	 *
	 * @param events
	 */
	public boolean add(final List<ScallorEvent> events) {
		try {
			return getQueue().addAll(events);
		} catch (final IllegalStateException e) {
			e.printStackTrace();
		} catch (final Throwable e) { // 防御性容错
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 如果队列是满的话，报错
	 *
	 * @param event
	 */
	public boolean add(final ScallorEvent event) {
		return add(Arrays.asList(event));
	}

	@Override
	public ConsumeConcurrentlyStatus consumer(final MessageExt messageExt) {
		// body and json is JSONEvent
		final String json = new String(messageExt.getBody(), MqConstants.DEFAULT_CHARSET_TYPE);
		final JSONEvent jsonEvent = JsonUtil.fromJson(json, JSONEvent.class);

		final ScallorEvent event = ScallorEvent.builder().msgId(messageExt.getMsgId()).data(jsonEvent.getData())
				.messageId(jsonEvent.getMessageId()).build();

		// 幂等性

		try {
			// 放入队列 put/add
			if (super.put(event)) {
				final long rl = CounterHepler.increment();

				logger.info("{} --> Receive MessagesID[{}/{}]:{}.", rl, messageExt.getMsgId(), event.getMessageId());
				return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return ConsumeConcurrentlyStatus.RECONSUME_LATER;
	}

	@Override
	public Tags getCondition() {
		return MqConstants.Tags.MQ_NOTIFIES_TAG_MSG;
	}

}
