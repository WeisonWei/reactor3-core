package com.reactor.flux;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FluxCreator {
	public static void main(final String[] args) throws InterruptedException {
		staticMethod();
		generate();
		create();
	}


	private static void staticMethod() throws InterruptedException {
		// 1 just()：可以指定序列中包含的全部元素
		// 创建出来的 Flux 序列在发布这些元素之后会自动结束
		Flux.just("1", "2", "3").subscribe(System.out::println);
		// 2 fromArray()，fromIterable()和 fromStream():
		// 可以从一个数组、Iterable 对象或 Stream 对象中创建 Flux 对象
		Flux.fromArray(new Integer[]{4, 5, 6}).subscribe(System.out::println);
		// 3 empty()：创建一个不包含任何元素，只发布结束消息的序列
		Flux.empty().subscribe(System.out::println);
		// 4 range(int start, int count)：
		// 创建包含从 start 起始的 count 个数量的 Integer 对象的序列。
		Flux.range(7, 3).subscribe(System.out::println);
		System.out.println("-------分割线-------");
		// 5 interval(Duration period)和 interval(Duration delay, Duration period)：
		// 创建一个包含了从 0 开始递增的 Long 对象的序列。其中包含的元素按照指定的间隔来发布。除了间隔时间之外，
		// 还可以指定起始元素发布之前的延迟时间
		// 每2秒产生一个元素
		Flux.interval(Duration.of(2, ChronoUnit.SECONDS)).subscribe(System.out::println);
		// 每1000微秒产生一个元素
		Flux.intervalMillis(1000).subscribe(System.out::println);
		TimeUnit.SECONDS.sleep(5);
		System.out.println("-------分割线-------");
	}

	/**
	 * generate是一种同步地，逐个地发出数据的方法;
	 * 它提供的sink是一个SynchronousSink，而且其next()方法在每次回调的时候最多只能被调用一次。
	 */
	private static void generate() {
		final AtomicInteger count = new AtomicInteger(1);
		Flux.generate(synchronousSink -> {
			synchronousSink.next(count + " --> " + new Random().nextInt());
			// 不complete() 将一直产生"Hello" 可以注掉看下效果
			if (count.getAndIncrement() >= 5) {
				synchronousSink.complete();
			}
		})
				.doOnNext(number -> System.out.println(number))
				.doOnNext(number -> System.out.println(number.toString() + 4))
				.subscribe();
		System.out.println("-------分割线-------");

		// 将names中姓名的首字母转成大写
		final AtomicInteger index = new AtomicInteger(0);
		List<String> names = Arrays.asList("weison", "evan", "elen");
		Flux.generate(() -> names, (nameList, synchronousSink) -> {
			String s = nameList.get(index.get());
			String s1 = s.substring(0, 1).toUpperCase() + s.substring(1);
			synchronousSink.next(s1);
			if (index.getAndIncrement() == nameList.size() - 1)
				synchronousSink.complete();
			return nameList;
		}).subscribe(System.out::println);
		System.out.println("-------分割线-------");


		//第三个参数 stateConsumer 是第一个参数的consumer
		final AtomicInteger index1 = new AtomicInteger(0);
		Flux.generate(() -> names, (nameList, synchronousSink) -> {
			String s = nameList.get(index1.get());
			String s1 = s.substring(0, 1).toUpperCase() + s.substring(1);
			synchronousSink.next(s1);
			if (index1.getAndIncrement() == nameList.size() - 1)
				synchronousSink.complete();
			return nameList;
		}, System.out::println).subscribe(System.out::println);
		System.out.println("-------分割线-------");
	}


	/**
	 * Flux.create用于阻塞方法与异步代码之间的交互 例3
	 * TODO FluxSink.OverflowStrategy.ERROR 等一直不起作用
	 *
	 * @throws InterruptedException
	 */
	private static void create() throws InterruptedException {
		Flux.create(sink -> {
			for (int i = 0; i < 5; i++) {
				sink.next(new Random().nextInt());
			}
			sink.complete();
		}).subscribe(System.out::println);
		System.out.println("-------分割线-------");

		Flux.create(sink -> {
			for (int i = 0; i < 5; i++) {
				sink.next(new Random().nextInt());
			}
			sink.complete();
		}, FluxSink.OverflowStrategy.BUFFER).subscribe(System.out::println);
		System.out.println("-------分割线-------");

		Flux.create((FluxSink<String> sink) -> {
			List<String> articals = getArticalsFromServer();
			articals.forEach(sink::next);
		}, FluxSink.OverflowStrategy.LATEST)
				.doOnNext(number -> System.out.println(number))
				.doOnNext(number -> System.out.println(number + 4))
				.subscribe(System.out::println);
		System.out.println("-------分割线-------");

		Flux.create(fluxSink -> {
			fluxSink.next("Weison");
			fluxSink.next("Even");
			fluxSink.error(new Exception("Err"));
		}, FluxSink.OverflowStrategy.ERROR)
				.onErrorReturn("我出错了～")
				.publishOn(Schedulers.parallel())
				.subscribe(System.out::println);
	}

	private static List<String> getArticalsFromServer() {
		return Arrays.asList("weison", "evan", "elen");
	}
}