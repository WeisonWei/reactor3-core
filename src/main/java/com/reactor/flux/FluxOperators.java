package com.reactor.flux;

import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FluxOperators {
    public static void main(String[] args) throws InterruptedException {
        printLine("buffer");
        //buffer();
        printLine("filter");
        //filter();
        printLine("window");
        //window();
        printLine("zipWith");
        //zipWith();
        printLine("take");
        //take();
        printLine("reduce");
        //reduce();
        printLine("merge");
        //merge();
        printLine("flatMap");
        //flatMap();
        printLine("concat");
        //concat();
        printLine("concatMap");
        //concatMap();
        printLine("combineLatest");
        //combineLatest();
        printLine("compositeOperate");
        compositeOperate();
    }

    private static void buffer() {
        //每次缓存一定数量的元素到List buckets里，并push出去
        Flux.range(1, 30)
                .buffer(20)
                .subscribe(System.out::println);
        System.out.println("---------- 分割线1 ----------");

        //每次缓存一定数量，并跳过一定数量的元素到List buckets里，并push出去
        Flux.range(1, 30)
                .buffer(10, 10)
                .subscribe(System.out::println);
        System.out.println("---------- 分割线2 ----------");

        //每次缓存一定数量，并跳过一定数量的元素到指定的Set buckets里，并push出去
        Flux.range(1, 30)
                .buffer(20, 20, HashSet::new)
                .subscribe(System.out::println);
        System.out.println("---------- 分割线3 ----------");

        //指定时间内，每次缓存一定数量的元素到List buckets里，并push出去
        Flux.intervalMillis(100)
                .bufferMillis(500)
                .take(3).toStream()
                .forEach(System.out::println);
        System.out.println("---------- 分割线4 ----------");

        //缓存元素到List buckets里当符合条件时，把元素push出去
        Flux.range(1, 10)
                .bufferUntil(i -> i % 2 == 0)
                .subscribe(System.out::println);
        System.out.println("---------- 分割线5 ----------");

        //把符合条件的缓存元素到List buckets里并把元素push出去
        Flux.range(1, 10)
                .bufferWhile(i -> i % 2 == 0)
                .subscribe(System.out::println);
    }

    private static void filter() {
        Flux.range(1, 10)
                .filter(i -> i % 2 == 0)
                .subscribe(System.out::println);
    }

    private static void window() {
        //从起始位置开始将source sequence拆分成固定大小的flux
        Flux<Flux<Integer>> window = Flux.range(1, 10)
                .window(3);
        //subscribe1
        window.subscribe(integerFlux -> {
            System.out.println("+++++分隔符+++++");
            integerFlux.subscribe(System.out::println);
        });
        System.out.println("---------- 分割线1 ----------");

        //subscribe2
        window.toStream().forEach(integerFlux -> {
            System.out.println("+++++分隔符+++++");
            integerFlux.subscribe(System.out::println);
        });
        System.out.println("---------- 分割线2 ----------");

        //在一定时间内将flux sequence拆分成连接的不同的flux
        Flux.intervalMillis(1)
                .windowMillis(3)
                .take(3)
                .toStream()
                .forEach(longFlux -> {
                    System.out.println("+++++分隔符+++++");
                    longFlux.subscribe(System.out::println);
                });
    }

    private static void zipWith() {
        //zipWith 将多个元素合并成元组，并从每个source产生元素直至结束
        final AtomicInteger index = new AtomicInteger();
        Flux<Tuple2<String, String>> tuple2Flux = Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"));
        //subscribe1
        tuple2Flux.subscribe(System.out::println);
        System.out.println("---------- 分割线1 ----------");

        //subscribe2
        tuple2Flux.subscribe(tupleFlux -> {
            System.out.println("t1--" + index + "-->" + tupleFlux.getT1());
            System.out.println("t2--" + index + "-->" + tupleFlux.getT2());
            index.incrementAndGet();
        });
        System.out.println("---------- 分割线2 ----------");

        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);
    }

    private static void take() {
        Flux.range(1, 20).take(10).subscribe(System.out::println);
        Flux.range(1, 20).takeLast(10).subscribe(System.out::println);
        Flux.range(1, 20).takeWhile(i -> i < 10).subscribe(System.out::println);
        Flux.range(1, 20).takeUntil(i -> i == 10).subscribe(System.out::println);
    }

    private static void reduce() {
        Flux.just(1, 2, 3)
                .reduce((x, y) -> x + y)
                .subscribe(System.out::println);
        Flux.just(1, 2, 3)
                .reduceWith(() -> 4, (x, y) -> x + y)
                .subscribe(System.out::println);
    }

    private static void merge() {
        //将多个源publisher中的元素合并到一个新的publisher序列中 重复的元素不会覆盖
        Flux.merge(Flux.just(0, 1, 2, 3), Flux.just(7, 5, 6), Flux.just(4, 7), Flux.just(4, 7))
                .toStream()
                .forEach(System.out::print);
        System.out.println();
        //将多个源publisher中的元素合并到一个新的publisher序列中 重复的元素不会覆盖
        //新publisher中元素的顺序为老元素的订阅顺序
        Flux<Integer> flux = Flux.mergeSequential(Flux.just(9, 8, 7), Flux.just(0, 1, 2, 3),
                Flux.just(6, 5, 4));
        System.out.println();
        flux.sort().subscribe(System.out::print);
        System.out.println();
        flux.subscribe(System.out::print);

    }

    private static void flatMap() {
        Flux.just(1, 2)
                .flatMap(x -> Flux.just(x * 10, 100).take(x))
                .toStream()
                .forEach(System.out::println);
    }

    private static void concat() {
        Flux.just(1, 2)
                .concat(Flux.just(5, 8))
                .toStream()
                .forEach(System.out::println);

        Flux.concat(Mono.just(3), Mono.just(4), Flux.just(1, 2))
                .subscribe(System.out::println);
    }

    private static void concatMap() {
        //绑定给定输入序列的动态序列，如flatMap，但保留顺序和串联发射，而不是合并(没有交织)
        Flux.just(5, 10, 100)
                .concatMap(x -> Flux.just(x * 10, 100))
                .toStream()
                .forEach(System.out::println);
    }

    private static void combineLatest() {
        //构建一个flux，其数据是由所有发布者最近发布的值组合而生成的
        Flux.combineLatest(
                Arrays::toString,
                Flux.just(1, 3, 1, 3, 2),
                Flux.just(1, 2, 3, 4),
                Mono.just(9),
                Flux.just(5, 6),
                Flux.just(7, 8, 9)
        ).toStream().forEach(System.out::println);
    }

    private static void compositeOperate() throws InterruptedException {
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);
        Flux.fromIterable(set)
                .delayElements(Duration.ofMillis(1))
                .doOnNext(FluxOperators::doSomeThing) //the callback to call on {@link Subscriber#onNext}
                .publishOn(Schedulers.elastic())
                .map(d -> d * 2)
                .take(3)
                .doOnCancel(() -> System.out.println("操作被取消了"))
                .doOnComplete(() -> System.out.println("正常结束了"))
                .onErrorResumeWith(e -> e instanceof RuntimeException, e -> {
                    System.out.println("出现了 RuntimeException");
                    return Flux.error(e);
                })//某种异常时才处理
                .doOnError(e -> System.out.println("出现了 Exception"))
                .doAfterTerminate(() -> System.out.println("不管正常异常，反正是结束了"))
                .onErrorReturn(444)
                .doFinally(signalType -> System.out.println("finally--->" + signalType))
                .subscribe(System.out::println);

        TimeUnit.SECONDS.sleep(5);
    }

    public static Integer doSomeThing(Integer integer) {
        //自行调节看效果
        if (1 != 1)
            throw new RuntimeException("a RuntimeException error");
        else if (2 != 2)
            throw new NullPointerException("a NullPointerException error");
        return integer + 1;
    }

    private static void printLine(final String operator) {
        System.out.printf(
                "%s %s %s%n",
                StringUtils.repeat("-", 10),
                operator,
                StringUtils.repeat("-", 10)
        );
    }
}
