package com.reactor.flux;

import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.HashSet;

public class FluxOperators {
    public static void main(String[] args) {
        printLine("buffer");
        buffer();
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
        printLine("concatMap");
        //concatMap();
        printLine("combineLatest");
        //combineLatest();

        /*Flux.fromIterable(getSomeLongList())
                .delayElements(Duration.ofMillis(100))
                .doOnNext(serviceA::someObserver)
                .map(d -> d * 2)
                .take(3)
                .onErrorResumeWith(errorHandler::fallback)
                .doAfterTerminate(serviceM::incrementTerminate)
                .subscribe(System.out::println);*/
    }

    private static void buffer() {
        //每次缓存一定数量的元素到List buckets里，并push出去
        Flux.range(1, 30).buffer(20).subscribe(System.out::println);
        System.out.println("----------分割线---------1-");

        //每次缓存一定数量，并跳过一定数量的元素到List buckets里，并push出去
        Flux.range(1, 30).buffer(10,10).subscribe(System.out::println);
        System.out.println("----------分割线---------2-");

        //每次缓存一定数量，并跳过一定数量的元素到指定的Set buckets里，并push出去
        Flux.range(1, 30).buffer(20,20, HashSet::new).subscribe(System.out::println);
        System.out.println("----------分割线---------3-");

        //指定时间内，每次缓存一定数量的元素到List buckets里，并push出去
        Flux.intervalMillis(100).bufferMillis(500).take(3).toStream().forEach(System.out::println);
        System.out.println("----------分割线---------4-");

        //每次根据条件缓存一定数量的元素到List buckets里，并push出去
        Flux.range(1, 10).bufferUntil(i -> i % 2 == 0).subscribe(System.out::println);
        System.out.println("----------分割线---------5-");

        //每次缓存一定数量，并跳过一定数量的元素到指定的Set buckets里，并push出去
        Flux.range(1, 10).bufferWhile(i -> i % 2 == 0).subscribe(System.out::println);
        System.out.println("----------分割线----------");

    }

    private static void filter() {
        Flux.range(1, 10).filter(i -> i % 2 == 0).subscribe(System.out::println);
    }

    private static void window() {
        Flux.range(1, 100).window(20).subscribe(System.out::println);
        Flux.intervalMillis(100).windowMillis(1001).take(2).toStream().forEach(System.out::println);
    }

    private static void zipWith() {
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"))
                .subscribe(System.out::println);
        Flux.just("a", "b")
                .zipWith(Flux.just("c", "d"), (s1, s2) -> String.format("%s-%s", s1, s2))
                .subscribe(System.out::println);
    }

    private static void take() {
        Flux.range(1, 1000).take(10).subscribe(System.out::println);
        Flux.range(1, 1000).takeLast(10).subscribe(System.out::println);
        Flux.range(1, 1000).takeWhile(i -> i < 10).subscribe(System.out::println);
        Flux.range(1, 1000).takeUntil(i -> i == 10).subscribe(System.out::println);
    }

    private static void reduce() {
        Flux.range(1, 100).reduce((x, y) -> x + y).subscribe(System.out::println);
        Flux.range(1, 100).reduceWith(() -> 100, (x, y) -> x + y).subscribe(System.out::println);
    }

    private static void merge() {
        Flux.merge(Flux.intervalMillis(0, 100).take(5), Flux.intervalMillis(50, 100).take(5))
                .toStream()
                .forEach(System.out::println);
        Flux.mergeSequential(Flux.intervalMillis(0, 100).take(5), Flux.intervalMillis(50, 100).take(5))
                .toStream()
                .forEach(System.out::println);
    }

    private static void flatMap() {
        Flux.just(5, 10)
                .flatMap(x -> Flux.intervalMillis(x * 10, 100).take(x))
                .toStream()
                .forEach(System.out::println);
    }

    private static void concatMap() {
        Flux.just(5, 10)
                .concatMap(x -> Flux.intervalMillis(x * 10, 100).take(x))
                .toStream()
                .forEach(System.out::println);
    }

    private static void combineLatest() {
        Flux.combineLatest(
                Arrays::toString,
                Flux.intervalMillis(100).take(5),
                Flux.intervalMillis(50, 100).take(5)
        ).toStream()
                .forEach(System.out::println);
    }

    private static void printLine(final String operator) {
        System.out.printf(
                "%s %s %s%n",
                StringUtils.repeat("*", 10),
                operator,
                StringUtils.repeat("*", 10)
        );
    }
}
